package com.example.chariiptv.data.local

import androidx.room.Entity

@Entity(tableName = "cached_channels", primaryKeys = ["url", "countryCode"])
data class CachedChannel(
    val url: String,
    val name: String,
    val logo: String,
    val countryCode: String
)
