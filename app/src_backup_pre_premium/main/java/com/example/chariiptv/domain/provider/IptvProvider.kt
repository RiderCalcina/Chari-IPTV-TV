package com.example.chariiptv.domain.provider

import com.example.chariiptv.domain.Channel

interface IptvProvider {
    val id: String
    val name: String
    val priority: Int
    suspend fun fetchChannels(): List<Channel>
}

