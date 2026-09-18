package com.example.chariiptv.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_channels")
data class CachedChannel(
    @PrimaryKey val url: String,
    val name: String,
    val logo: String,
    val countryCode: String
)
