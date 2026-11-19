package com.example.chariiptv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FavoriteChannel::class, CachedChannel::class], version = 12, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cachedChannelDao(): CachedChannelDao

    companion object {
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cached_channels ADD COLUMN userAgent TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cached_channels ADD COLUMN referer TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cached_channels ADD COLUMN alternateSourcesJson TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cached_channels ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE favorites ADD COLUMN userAgent TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE favorites ADD COLUMN referer TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE favorites ADD COLUMN alternateSourcesJson TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
