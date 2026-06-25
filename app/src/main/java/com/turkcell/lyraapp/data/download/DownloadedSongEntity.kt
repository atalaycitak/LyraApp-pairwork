package com.turkcell.lyraapp.data.download

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_songs")
data class DownloadedSongEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val filePath: String,
    val fileSizeBytes: Long,
    val downloadedAt: Long,
)
