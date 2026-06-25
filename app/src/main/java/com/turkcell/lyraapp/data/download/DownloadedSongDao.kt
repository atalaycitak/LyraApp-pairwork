package com.turkcell.lyraapp.data.download

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DownloadedSongDao {
    @Query("SELECT * FROM downloaded_songs WHERE songId = :songId")
    suspend fun getBySongId(songId: String): DownloadedSongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DownloadedSongEntity): Long

    @Query("DELETE FROM downloaded_songs WHERE songId = :songId")
    suspend fun deleteBySongId(songId: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_songs WHERE songId = :songId)")
    suspend fun isDownloaded(songId: String): Boolean
}
