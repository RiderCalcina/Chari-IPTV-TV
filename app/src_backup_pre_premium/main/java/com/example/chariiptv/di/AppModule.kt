package com.example.chariiptv.di

import android.content.Context
import androidx.room.Room
import com.example.chariiptv.data.local.AppDatabase
import com.example.chariiptv.data.local.FavoriteDao
import com.example.chariiptv.data.local.CachedChannelDao
import com.example.chariiptv.data.remote.M3uApiService
import com.example.chariiptv.player.LocalHlsProxyServer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import retrofit2.Retrofit
import java.io.File
import java.net.InetAddress
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val DEFAULT_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

    @Provides
    @Singleton
    fun provideDnsOverHttps(@ApplicationContext context: Context): Dns {
        return try {
            val appCache = Cache(File(context.cacheDir, "doh_cache"), 5 * 1024 * 1024)
            val bootstrapClient = OkHttpClient.Builder()
                .cache(appCache)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()

            val doh = DnsOverHttps.Builder()
                .client(bootstrapClient)
                .url("https://1.1.1.1/dns-query".toHttpUrl())
                .bootstrapDnsHosts(
                    InetAddress.getByName("1.1.1.1"),
                    InetAddress.getByName("1.0.0.1"),
                    InetAddress.getByName("8.8.8.8"),
                    InetAddress.getByName("8.8.4.4")
                )
                .includeIPv6(false)
                .build()

            object : Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    return try {
                        doh.lookup(hostname)
                    } catch (_: Exception) {
                        Dns.SYSTEM.lookup(hostname)
                    }
                }
            }
        } catch (_: Exception) {
            Dns.SYSTEM
        }
    }

    @Provides
    @Singleton
    fun provideUnsafeOkHttpClient(dohDns: Dns): OkHttpClient {
        val headerInterceptor = Interceptor { chain ->
            val request = chain.request()
            val builder = request.newBuilder()

            if (request.header("User-Agent") == null) {
                builder.header("User-Agent", DEFAULT_UA)
            }
            if (request.header("Accept") == null) {
                builder.header("Accept", "*/*")
            }
            if (request.header("Accept-Language") == null) {
                builder.header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
            }
            chain.proceed(builder.build())
        }

        return try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
            })
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            OkHttpClient.Builder()
                .dns(dohDns)
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .addInterceptor(headerInterceptor)
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            OkHttpClient.Builder()
                .dns(dohDns)
                .addInterceptor(headerInterceptor)
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
        }
    }

    @Provides
    @Singleton
    @Named("pingOkHttpClient")
    fun providePingOkHttpClient(okHttpClient: OkHttpClient): OkHttpClient {
        return okHttpClient.newBuilder()
            .connectTimeout(2500, TimeUnit.MILLISECONDS)
            .readTimeout(2500, TimeUnit.MILLISECONDS)
            .callTimeout(3000, TimeUnit.MILLISECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideLocalHlsProxyServer(okHttpClient: OkHttpClient): LocalHlsProxyServer {
        return LocalHlsProxyServer(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://iptv-org.github.io/iptv/")
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideM3uApiService(retrofit: Retrofit): M3uApiService {
        return retrofit.create(M3uApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tvdroid_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    @Singleton
    fun provideCachedChannelDao(database: AppDatabase): CachedChannelDao {
        return database.cachedChannelDao()
    }

    @Provides
    @Singleton
    fun provideRssRepository(okHttpClient: OkHttpClient): com.example.chariiptv.domain.RssRepository {
        return com.example.chariiptv.domain.RssRepositoryImpl(okHttpClient)
    }
}
