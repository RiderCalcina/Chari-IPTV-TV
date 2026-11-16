package com.example.chariiptv.player

import android.net.Uri
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
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
class LocalHlsProxyServer @Inject constructor(
    private val client: OkHttpClient
) {
    companion object {
        private const val TAG = "LocalHlsProxyServer"
        private const val DEFAULT_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
    }

    private var serverSocket: ServerSocket? = null
    var port: Int = 0
        private set
    private val executor = Executors.newCachedThreadPool { runnable ->
        Thread(runnable).apply { isDaemon = true }
    }

    @Volatile
    private var isRunning = false

    init {
        start()
    }

    @Synchronized
    fun start() {
        if (isRunning && serverSocket != null && !serverSocket!!.isClosed) return
        try {
            serverSocket = ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))
            port = serverSocket!!.localPort
            isRunning = true
            Log.i(TAG, "LocalHlsProxyServer iniciado en loopback puerto $port")

            executor.execute {
                while (isRunning && !serverSocket!!.isClosed) {
                    try {
                        val clientSocket = serverSocket!!.accept()
                        executor.execute { handleConnection(clientSocket) }
                    } catch (e: Exception) {
                        if (!isRunning) break
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando LocalHlsProxyServer", e)
        }
    }

    fun buildProxyUrl(
        streamUrl: String,
        referer: String = "",
        origin: String = "",
        userAgent: String = ""
    ): String {
        if (!isRunning || port == 0) start()
        val encodedUrl = Uri.encode(streamUrl)
        val encodedReferer = if (referer.isNotBlank()) Uri.encode(referer) else ""
        val encodedOrigin = if (origin.isNotBlank()) Uri.encode(origin) else ""
        val encodedUa = if (userAgent.isNotBlank()) Uri.encode(userAgent) else ""

        var proxyUrl = "http://127.0.0.1:$port/stream?url=$encodedUrl"
        if (encodedReferer.isNotBlank()) proxyUrl += "&referer=$encodedReferer"
        if (encodedOrigin.isNotBlank()) proxyUrl += "&origin=$encodedOrigin"
        if (encodedUa.isNotBlank()) proxyUrl += "&ua=$encodedUa"
        return proxyUrl
    }

    private fun handleConnection(socket: Socket) {
        try {
            socket.soTimeout = 15000
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val requestLine = reader.readLine() ?: run { socket.close(); return }
            val parts = requestLine.split(" ")
            if (parts.size < 2 || parts[0] != "GET") {
                socket.close()
                return
            }

            val rawUri = parts[1]
            val uri = Uri.parse("http://127.0.0.1$rawUri")
            val targetUrl = uri.getQueryParameter("url") ?: run {
                sendNotFound(socket)
                return
            }

            val referer = uri.getQueryParameter("referer") ?: ""
            val origin = uri.getQueryParameter("origin") ?: ""
            val userAgent = uri.getQueryParameter("ua")?.ifBlank { DEFAULT_UA } ?: DEFAULT_UA

            val reqBuilder = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", userAgent)
                .header("Accept", "*/*")
                .header("Connection", "keep-alive")

            if (referer.isNotBlank()) {
                reqBuilder.header("Referer", referer)
            }
            if (origin.isNotBlank()) {
                reqBuilder.header("Origin", origin)
            }

            val response = client.newCall(reqBuilder.build()).execute()
            val output = socket.getOutputStream()
            val writer = PrintWriter(output)

            val contentType = response.header("Content-Type") ?: ""
            val isM3u8 = targetUrl.contains(".m3u8", ignoreCase = true) ||
                    contentType.contains("mpegurl", ignoreCase = true)

            if (response.isSuccessful) {
                if (isM3u8) {
                    val bodyString = response.body?.string() ?: ""
                    val rewritten = rewriteM3u8(bodyString, targetUrl, referer, origin, userAgent)
                    val bodyBytes = rewritten.toByteArray(Charsets.UTF_8)

                    writer.print("HTTP/1.1 200 OK\r\n")
                    writer.print("Content-Type: application/vnd.apple.mpegurl\r\n")
                    writer.print("Content-Length: ${bodyBytes.size}\r\n")
                    writer.print("Access-Control-Allow-Origin: *\r\n")
                    writer.print("Connection: close\r\n\r\n")
                    writer.flush()
                    output.write(bodyBytes)
                    output.flush()
                } else {
                    writer.print("HTTP/1.1 ${response.code} ${response.message}\r\n")
                    response.headers.forEach { pair ->
                        val name = pair.first
                        if (!name.equals("Transfer-Encoding", ignoreCase = true)) {
                            writer.print("$name: ${pair.second}\r\n")
                        }
                    }
                    writer.print("Access-Control-Allow-Origin: *\r\n\r\n")
                    writer.flush()
                    response.body?.byteStream()?.use { input ->
                        input.copyTo(output)
                    }
                    output.flush()
                }
            } else {
                writer.print("HTTP/1.1 ${response.code} ${response.message}\r\n\r\n")
                writer.flush()
            }
            response.close()
            socket.close()
        } catch (e: Exception) {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun rewriteM3u8(
        content: String,
        manifestUrl: String,
        referer: String,
        origin: String,
        ua: String
    ): String {
        val baseUri = Uri.parse(manifestUrl)
        val sb = StringBuilder(content.length + 512)

        content.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                sb.append("\n")
                return@forEach
            }
            if (trimmed.startsWith("#")) {
                if (trimmed.startsWith("#EXT-X-KEY:") && trimmed.contains("URI=\"")) {
                    val keyUriRegex = "URI=\"([^\"]+)\"".toRegex()
                    val match = keyUriRegex.find(trimmed)
                    if (match != null) {
                        val keyUri = match.groupValues[1]
                        val resolvedKeyUri = resolveUrl(baseUri, keyUri)
                        val proxyKeyUri = buildProxyUrl(resolvedKeyUri, referer, origin, ua)
                        val rewrittenKeyTag = trimmed.replace(keyUri, proxyKeyUri)
                        sb.append(rewrittenKeyTag).append("\n")
                        return@forEach
                    }
                }
                sb.append(trimmed).append("\n")
            } else {
                val resolvedUrl = resolveUrl(baseUri, trimmed)
                val proxySegmentUrl = buildProxyUrl(resolvedUrl, referer, origin, ua)
                sb.append(proxySegmentUrl).append("\n")
            }
        }
        return sb.toString()
    }

    private fun resolveUrl(base: Uri, relativeOrAbsolute: String): String {
        return try {
            val baseUri = java.net.URI(base.toString())
            baseUri.resolve(relativeOrAbsolute).toString()
        } catch (_: Exception) {
            if (relativeOrAbsolute.startsWith("http://") || relativeOrAbsolute.startsWith("https://")) {
                relativeOrAbsolute
            } else if (relativeOrAbsolute.startsWith("/")) {
                "${base.scheme}://${base.host}${if (base.port != -1) ":${base.port}" else ""}$relativeOrAbsolute"
            } else {
                val lastSlash = base.toString().lastIndexOf('/')
                val basePath = if (lastSlash != -1) base.toString().substring(0, lastSlash + 1) else "$base/"
                basePath + relativeOrAbsolute
            }
        }
    }

    private fun deriveReferer(targetUrl: String): String {
        return try {
            val uri = Uri.parse(targetUrl)
            "${uri.scheme}://${uri.host}/"
        } catch (_: Exception) {
            ""
        }
    }

    private fun deriveOrigin(referer: String): String {
        return try {
            if (referer.isBlank()) return ""
            val uri = Uri.parse(referer)
            "${uri.scheme}://${uri.host}"
        } catch (_: Exception) {
            ""
        }
    }

    private fun sendNotFound(socket: Socket) {
        try {
            val writer = PrintWriter(socket.getOutputStream())
            writer.print("HTTP/1.1 404 Not Found\r\n\r\n")
            writer.flush()
            socket.close()
        } catch (_: Exception) {}
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
    }
}

