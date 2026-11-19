package com.example.chariiptv.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "cached_channels",
    primaryKeys = ["url", "countryCode"],
    indices = [Index(value = ["countryCode"])]
)
data class CachedChannel(
    val url: String,
    val name: String,
    val logo: String,
    val countryCode: String,
    val userAgent: String = "",
    val referer: String = "",
    val alternateSourcesJson: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
