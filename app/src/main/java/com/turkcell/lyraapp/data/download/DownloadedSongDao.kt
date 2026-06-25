package com.turkcell.lyraapp.data.download

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedSongDao {
    @Query("SELECT * FROM downloaded_songs WHERE songId = :songId")
    fun getBySongId(songId: String): DownloadedSongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: DownloadedSongEntity): Long

    @Query("DELETE FROM downloaded_songs WHERE songId = :songId")
    fun deleteBySongId(songId: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_songs WHERE songId = :songId)")
    fun isDownloaded(songId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_songs WHERE songId = :songId)")
    fun observeIsDownloaded(songId: String): Flow<Boolean>
}
