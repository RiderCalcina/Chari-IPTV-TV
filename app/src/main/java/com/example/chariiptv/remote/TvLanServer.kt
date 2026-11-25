package com.example.chariiptv.remote

import android.os.Build
import android.util.Log
import android.view.KeyEvent
import com.example.chariiptv.data.local.FavoriteChannel
import com.example.chariiptv.data.local.FavoriteDao
import com.example.chariiptv.domain.Channel
import com.example.chariiptv.domain.StreamSource
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvLanServer @Inject constructor(
    private val eventBus: LanRemoteEventBus,
    private val favoriteDao: FavoriteDao
) {
    companion object {
        private const val TAG = "TvLanServer"
        const val DEFAULT_PORT = 8990
    }

    private fun isLocalAddress(socket: Socket): Boolean {
        val addr = socket.inetAddress ?: return false
        if (addr.isLoopbackAddress || addr.isSiteLocalAddress || addr.isLinkLocalAddress) return true
        val host = addr.hostAddress ?: ""
        val cleanHost = if (host.startsWith("::ffff:")) host.removePrefix("::ffff:") else host
        return cleanHost.startsWith("127.") ||
               cleanHost.startsWith("10.") ||
               cleanHost.startsWith("192.168.") ||
               cleanHost.startsWith("169.254.") ||
               cleanHost.matches(Regex("^172\\.(?:1[6-9]|2[0-9]|3[01])\\..*"))
    }

    private var serverSocket: ServerSocket? = null
    @Volatile
    var port: Int = DEFAULT_PORT
        private set

    @Volatile
    private var isRunning = false

    var currentChannelProvider: (() -> Channel?)? = null

    private val workerExecutor = Executors.newCachedThreadPool { runnable ->
        Thread(runnable, "tv-lan-worker").apply { isDaemon = true }
    }

    @Synchronized
    fun start() {
        if (isRunning && serverSocket != null && !serverSocket!!.isClosed) return
        try {
            // Intentar enlazar en el puerto preferido 8990, o aleatorio seguro si está ocupado
            serverSocket = try {
                ServerSocket(DEFAULT_PORT, 50, InetAddress.getByName("0.0.0.0"))
            } catch (_: Exception) {
                ServerSocket(0, 50, InetAddress.getByName("0.0.0.0"))
            }
            port = serverSocket!!.localPort
            isRunning = true
            Log.i(TAG, "TvLanServer iniciado en puerto $port")

            Thread({
                while (isRunning && serverSocket?.isClosed == false) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        workerExecutor.execute { handleConnection(clientSocket) }
                    } catch (_: Exception) {
                        break
                    }
                }
            }, "tv-lan-acceptor").start()
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando TvLanServer", e)
        }
    }

    private fun handleConnection(socket: Socket) {
        try {
            socket.use { s ->
                s.soTimeout = 10000

                // Validación de seguridad Anti-SSRF: Solo admitir conexiones de la red local
                val clientIp = s.inetAddress?.hostAddress ?: ""
                if (!isLocalAddress(s)) {
                    Log.w(TAG, "Conexión rechazada desde IP no local: $clientIp")
                    sendResponse(s, 403, "Forbidden", JSONObject().put("error", "Access denied").toString())
                    return
                }

                val reader = BufferedReader(InputStreamReader(s.getInputStream(), Charsets.UTF_8))
                val requestLine = reader.readLine() ?: return
                val parts = requestLine.split(" ")
                if (parts.size < 2) return

                val method = parts[0].uppercase()
                val path = parts[1].substringBefore("?")

                // Manejo de peticiones preflight CORS
                if (method == "OPTIONS") {
                    sendCorsPreflight(s)
                    return
                }

                // Leer cabeceras
                var contentLength = 0
                var headerLine = reader.readLine()
                while (!headerLine.isNullOrEmpty()) {
                    if (headerLine.startsWith("Content-Length:", ignoreCase = true)) {
                        contentLength = headerLine.substringAfter(":").trim().toIntOrNull() ?: 0
                    }
                    headerLine = reader.readLine()
                }

                // Leer cuerpo si es POST
                var body = ""
                if (method == "POST" && contentLength > 0) {
                    val charBuffer = CharArray(contentLength)
                    var readTotal = 0
                    while (readTotal < contentLength) {
                        val count = reader.read(charBuffer, readTotal, contentLength - readTotal)
                        if (count == -1) break
                        readTotal += count
                    }
                    body = String(charBuffer, 0, readTotal)
                }

                when {
                    method == "GET" && path == "/api/info" -> {
                        val info = JSONObject().apply {
                            put("status", "OK")
                            put("model", Build.MODEL)
                            put("brand", Build.BRAND)
                            put("device", Build.DEVICE)
                            put("version", "1.2")
                            put("port", port)
                            put("appName", "Chari-IPTV TV")
                        }
                        sendResponse(s, 200, "OK", info.toString())
                    }

                    method == "GET" && path == "/api/status" -> {
                        val activeCh = currentChannelProvider?.invoke()
                        val status = JSONObject().apply {
                            put("status", "OK")
                            put("isPlaying", activeCh != null)
                            if (activeCh != null) {
                                put("channelName", activeCh.name)
                                put("channelLogo", activeCh.logo)
                                put("channelCategory", activeCh.category)
                            }
                        }
                        sendResponse(s, 200, "OK", status.toString())
                    }

                    method == "POST" && path == "/api/command" -> {
                        handleCommand(s, body)
                    }

                    method == "POST" && path == "/api/remote" -> {
                        handleRemoteKey(s, body)
                    }

                    else -> {
                        sendResponse(s, 404, "Not Found", JSONObject().put("error", "Endpoint not found").toString())
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun handleCommand(socket: Socket, body: String) {
        try {
            val json = JSONObject(body)
            val action = json.optString("action")

            when (action) {
                "PLAY_CHANNEL" -> {
                    val chJson = json.optJSONObject("channel")
                    if (chJson != null) {
                        val channel = Channel(
                            name = chJson.optString("name", "Canal"),
                            logo = chJson.optString("logo", ""),
                            url = chJson.optString("url", ""),
                            category = chJson.optString("category", ""),
                            country = chJson.optString("country", ""),
                            referer = chJson.optString("referer", ""),
                            userAgent = chJson.optString("userAgent", "")
                        )
                        if (channel.url.isNotBlank()) {
                            eventBus.emit(LanRemoteEvent.PlayChannel(channel))
                            sendResponse(socket, 200, "OK", JSONObject().put("status", "OK").put("message", "Canal recibido").toString())
                            return
                        }
                    }
                    sendResponse(socket, 400, "Bad Request", JSONObject().put("error", "Objeto channel inválido").toString())
                }

                "STOP" -> {
                    eventBus.emit(LanRemoteEvent.StopPlayback)
                    sendResponse(socket, 200, "OK", JSONObject().put("status", "OK").toString())
                }

                "INPUT_TEXT" -> {
                    val text = json.optString("text", "")
                    eventBus.emit(LanRemoteEvent.SetSearchQuery(text))
                    sendResponse(socket, 200, "OK", JSONObject().put("status", "OK").toString())
                }

                "SYNC_FAVORITES" -> {
                    val favsArray = json.optJSONArray("favorites") ?: JSONArray()
                    val incomingUrls = ArrayList<String>()

                    for (i in 0 until favsArray.length()) {
                        val item = favsArray.optJSONObject(i)
                        if (item != null) {
                            val url = item.optString("url", "")
                            val name = item.optString("name", "")
                            val logo = item.optString("logo", "")
                            val referer = item.optString("referer", "")
                            val userAgent = item.optString("userAgent", "")
                            val alternateSourcesJson = item.optString("alternateSourcesJson", "")
                            if (url.isNotBlank()) {
                                incomingUrls.add(url)
                                runBlocking {
                                    favoriteDao.insertFavorite(
                                        FavoriteChannel(
                                            url = url,
                                            name = name,
                                            logo = logo,
                                            referer = referer,
                                            userAgent = userAgent,
                                            alternateSourcesJson = alternateSourcesJson
                                        )
                                    )
                                }
                            }
                        } else {
                            val url = favsArray.optString(i, "")
                            if (url.isNotBlank()) {
                                incomingUrls.add(url)
                            }
                        }
                    }

                    if (incomingUrls.isNotEmpty()) {
                        eventBus.emit(LanRemoteEvent.SyncFavorites(incomingUrls))
                    }

                    // Obtener la totalidad de favoritos mezclados de la TV para devolver al celular (Sincronización Bidireccional)
                    val allMerged = runBlocking {
                        favoriteDao.getFavoriteChannels()
                    }

                    val mergedJsonArray = JSONArray()
                    for (fav in allMerged) {
                        mergedJsonArray.put(JSONObject().apply {
                            put("url", fav.url)
                            put("name", fav.name)
                            put("logo", fav.logo)
                            put("referer", fav.referer)
                            put("userAgent", fav.userAgent)
                            put("alternateSourcesJson", fav.alternateSourcesJson)
                        })
                    }

                    val responseJson = JSONObject().apply {
                        put("status", "OK")
                        put("synced", allMerged.size)
                        put("favorites", mergedJsonArray)
                    }

                    sendResponse(socket, 200, "OK", responseJson.toString())
                }

                else -> {
                    sendResponse(socket, 400, "Bad Request", JSONObject().put("error", "Acción desconocida").toString())
                }
            }
        } catch (e: Exception) {
            sendResponse(socket, 500, "Error", JSONObject().put("error", e.message).toString())
        }
    }

    private fun handleRemoteKey(socket: Socket, body: String) {
        try {
            val json = JSONObject(body)
            val key = json.optString("key").uppercase()

            val keyCode = when (key) {
                "DPAD_UP" -> KeyEvent.KEYCODE_DPAD_UP
                "DPAD_DOWN" -> KeyEvent.KEYCODE_DPAD_DOWN
                "DPAD_LEFT" -> KeyEvent.KEYCODE_DPAD_LEFT
                "DPAD_RIGHT" -> KeyEvent.KEYCODE_DPAD_RIGHT
                "DPAD_CENTER", "OK", "ENTER" -> KeyEvent.KEYCODE_DPAD_CENTER
                "BACK" -> KeyEvent.KEYCODE_BACK
                "HOME" -> KeyEvent.KEYCODE_HOME
                "VOL_UP", "VOLUME_UP" -> KeyEvent.KEYCODE_VOLUME_UP
                "VOL_DOWN", "VOLUME_DOWN" -> KeyEvent.KEYCODE_VOLUME_DOWN
                "MUTE" -> KeyEvent.KEYCODE_VOLUME_MUTE
                "PLAY_PAUSE" -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                "FAST_FORWARD" -> KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
                "REWIND" -> KeyEvent.KEYCODE_MEDIA_REWIND
                else -> -1
            }

            if (keyCode != -1) {
                eventBus.emit(LanRemoteEvent.SendKeyEvent(keyCode))
                sendResponse(socket, 200, "OK", JSONObject().put("status", "OK").put("key", key).toString())
            } else {
                sendResponse(socket, 400, "Bad Request", JSONObject().put("error", "Tecla no reconocida: $key").toString())
            }
        } catch (e: Exception) {
            sendResponse(socket, 500, "Error", JSONObject().put("error", e.message).toString())
        }
    }

    private fun sendCorsPreflight(socket: Socket) {
        try {
            val writer = PrintWriter(socket.getOutputStream())
            writer.print("HTTP/1.1 204 No Content\r\n")
            writer.print("Access-Control-Allow-Origin: *\r\n")
            writer.print("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n")
            writer.print("Access-Control-Allow-Headers: Content-Type, Authorization\r\n")
            writer.print("Access-Control-Max-Age: 86400\r\n")
            writer.print("Connection: close\r\n\r\n")
            writer.flush()
        } catch (_: Exception) {}
    }

    private fun sendResponse(socket: Socket, code: Int, message: String, json: String) {
        try {
            val bytes = json.toByteArray(Charsets.UTF_8)
            val output = socket.getOutputStream()
            val header = "HTTP/1.1 $code $message\r\n" +
                         "Content-Type: application/json; charset=utf-8\r\n" +
                         "Content-Length: ${bytes.size}\r\n" +
                         "Access-Control-Allow-Origin: *\r\n" +
                         "Connection: close\r\n\r\n"
            output.write(header.toByteArray(Charsets.UTF_8))
            output.write(bytes)
            output.flush()
        } catch (_: Exception) {}
    }

    @Synchronized
    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
    }
}
