package com.example.chariiptv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoriteChannel::class, CachedChannel::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cachedChannelDao(): CachedChannelDao
}
