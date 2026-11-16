package com.example.chariiptv.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface M3uApiService {
    @GET("languages/spa.m3u")
    suspend fun getSpanishM3u(): Response<ResponseBody>

    @GET("index.m3u")
    suspend fun getGlobalM3u(): Response<ResponseBody>
}
