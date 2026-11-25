package com.example.chariiptv.remote

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvNsdBroadcaster @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tvLanServer: TvLanServer
) {
    companion object {
        private const val TAG = "TvNsdBroadcaster"
        const val SERVICE_TYPE = "_chari-tv._tcp."
    }

    private var nsdManager: NsdManager? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    @Volatile
    private var isRegistered = false

    @Synchronized
    fun start() {
        if (isRegistered) return
        try {
            nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
            if (nsdManager == null) {
                Log.w(TAG, "NsdManager no disponible en este dispositivo")
                return
            }

            val sanitizedModel = Build.MODEL.filter { it.isLetterOrDigit() || it == '-' || it == '_' }.ifBlank { "Box" }
            val serviceName = "Chari-TV-$sanitizedModel"

            val serviceInfo = NsdServiceInfo().apply {
                this.serviceName = serviceName
                this.serviceType = SERVICE_TYPE
                this.port = tvLanServer.port
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAttribute("model", Build.MODEL)
                    setAttribute("brand", Build.BRAND)
                    setAttribute("app", "Chari-IPTV")
                    setAttribute("version", "1.2")
                }
            }

            registrationListener = object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(registeredInfo: NsdServiceInfo) {
                    isRegistered = true
                    Log.i(TAG, "Servicio mDNS registrado exitosamente: ${registeredInfo.serviceName} en puerto ${registeredInfo.port}")
                }

                override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    isRegistered = false
                    Log.e(TAG, "Fallo al registrar servicio mDNS: errorCode=$errorCode")
                }

                override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                    isRegistered = false
                    Log.i(TAG, "Servicio mDNS desregistrado: ${serviceInfo.serviceName}")
                }

                override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.e(TAG, "Fallo al desregistrar servicio mDNS: errorCode=$errorCode")
                }
            }

            nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando TvNsdBroadcaster", e)
        }
    }

    @Synchronized
    fun stop() {
        if (!isRegistered || registrationListener == null) return
        try {
            nsdManager?.unregisterService(registrationListener)
            isRegistered = false
            registrationListener = null
        } catch (e: Exception) {
            Log.e(TAG, "Error deteniendo TvNsdBroadcaster", e)
        }
    }
}
