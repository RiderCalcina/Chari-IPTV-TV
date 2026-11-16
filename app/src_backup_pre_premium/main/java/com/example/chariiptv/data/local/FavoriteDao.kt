package com.example.chariiptv.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getFavoriteChannelsFlow(): Flow<List<FavoriteChannel>>

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    suspend fun getFavoriteChannels(): List<FavoriteChannel>

    @Query("SELECT url FROM favorites ORDER BY addedAt DESC")
    fun getFavorites(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE url = :url)")
    suspend fun isFavorite(url: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteChannel)

    @Query("DELETE FROM favorites WHERE url = :url")
    suspend fun deleteFavorite(url: String)
}
