package com.example.chariiptv.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CachedChannelDao {
    @Query("SELECT * FROM cached_channels WHERE countryCode = :countryCode")
    suspend fun getChannelsByCountry(countryCode: String): List<CachedChannel>

    @Query("SELECT * FROM cached_channels WHERE url IN (:urls)")
    suspend fun getChannelsByUrls(urls: List<String>): List<CachedChannel>

    @Query("SELECT * FROM cached_channels WHERE name LIKE '%' || :query || '%'")
    suspend fun searchCachedChannels(query: String): List<CachedChannel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<CachedChannel>)

    @Query("DELETE FROM cached_channels WHERE countryCode = :countryCode")
    suspend fun deleteChannelsByCountry(countryCode: String)

    @Query("DELETE FROM cached_channels")
    suspend fun clearAllCache()

    @Transaction
    suspend fun replaceChannelsForCountry(countryCode: String, channels: List<CachedChannel>) {
        deleteChannelsByCountry(countryCode)
        insertChannels(channels)
    }
}
