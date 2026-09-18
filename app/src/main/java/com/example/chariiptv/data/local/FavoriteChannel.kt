package com.example.chariiptv.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteChannel(
    @PrimaryKey val url: String,
    val addedAt: Long = System.currentTimeMillis()
)
