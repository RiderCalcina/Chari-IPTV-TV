package com.example.chariiptv

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class TvDroidApplication : Application(), ImageLoaderFactory {

    // Inyección perezosa: evita inicializar DoH/SSL/cacheDir síncronamente en Application.onCreate (Main Thread)
    @Inject
    lateinit var okHttpClientLazy: Lazy<OkHttpClient>

    override fun newImageLoader(): ImageLoader {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memClass = am?.memoryClass ?: 192
        val isLowRamDevice = memClass < 192 || am?.isLowRamDevice == true
        val memPercent = if (isLowRamDevice) 0.15 else 0.22
        val diskSizeMb = if (isLowRamDevice) 50L else 100L

        val dispatcher = okhttp3.Dispatcher().apply {
            maxRequests = 48
            maxRequestsPerHost = 8
        }

        return ImageLoader.Builder(this)
            .okHttpClient {
                okHttpClientLazy.get().newBuilder()
                    .dispatcher(dispatcher)
                    .retryOnConnectionFailure(true)
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val request = original.newBuilder()
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                            .header("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
                            .header("Referer", "https://iptv-org.github.io/")
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            }
            .components {
                add(SvgDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(memPercent)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(diskSizeMb * 1024 * 1024)
                    .build()
            }
            .crossfade(false)
            .respectCacheHeaders(false)
            .build()
    }
}
