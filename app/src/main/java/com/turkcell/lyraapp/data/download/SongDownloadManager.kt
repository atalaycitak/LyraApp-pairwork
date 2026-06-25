package com.turkcell.lyraapp.data.download

import android.content.Context
import com.turkcell.lyraapp.data.song.SongRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

sealed interface DownloadStatus {
    data object Idle : DownloadStatus
    data class Downloading(val progress: Float) : DownloadStatus
    data object Completed : DownloadStatus
    data class Failed(val message: String) : DownloadStatus
}

@Singleton
class SongDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val songRepository: SongRepository,
    private val downloadedSongDao: DownloadedSongDao,
) {
    private val _downloadStatuses = mutableMapOf<String, MutableStateFlow<DownloadStatus>>()

    fun getDownloadStatus(songId: String): StateFlow<DownloadStatus> {
        return _downloadStatuses.getOrPut(songId) { MutableStateFlow(DownloadStatus.Idle) }.asStateFlow()
    }

    suspend fun downloadSong(songId: String) = withContext(Dispatchers.IO) {
        val statusFlow = _downloadStatuses.getOrPut(songId) { MutableStateFlow(DownloadStatus.Idle) }
        
        if (statusFlow.value is DownloadStatus.Downloading || statusFlow.value is DownloadStatus.Completed) {
            return@withContext
        }

        if (downloadedSongDao.isDownloaded(songId)) {
            statusFlow.value = DownloadStatus.Completed
            return@withContext
        }

        statusFlow.value = DownloadStatus.Downloading(0f)

        try {
            val songResult = songRepository.getSongById(songId)
            val song = songResult.getOrNull() ?: throw Exception("Şarkı bilgisi alınamadı.")

            val urlResult = songRepository.getStreamUrl(songId)
            val streamUrl = urlResult.getOrNull()?.url ?: throw Exception("İndirme URL'si alınamadı.")

            val request = Request.Builder().url(streamUrl).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) throw Exception("İndirme başarısız: HTTP ${response.code}")

            val body = response.body ?: throw Exception("Boş yanıt gövdesi.")
            val contentLength = body.contentLength()

            val downloadsDir = File(context.filesDir, "downloads")
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val outputFile = File(downloadsDir, "$songId.audio")

            body.byteStream().use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesCopied = 0L
                    var read: Int

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesCopied += read

                        if (contentLength > 0) {
                            val progress = bytesCopied.toFloat() / contentLength.toFloat()
                            statusFlow.value = DownloadStatus.Downloading(progress)
                        }
                    }
                }
            }

            val entity = DownloadedSongEntity(
                songId = song.id,
                title = song.title,
                artist = song.artist,
                durationMs = song.durationMs,
                filePath = outputFile.absolutePath,
                fileSizeBytes = outputFile.length(),
                downloadedAt = System.currentTimeMillis()
            )
            downloadedSongDao.insert(entity)

            statusFlow.value = DownloadStatus.Completed

        } catch (e: Exception) {
            statusFlow.value = DownloadStatus.Failed(e.message ?: "Bilinmeyen indirme hatası.")
        }
    }

    suspend fun removeDownload(songId: String) = withContext(Dispatchers.IO) {
        val entity = downloadedSongDao.getBySongId(songId)
        if (entity != null) {
            val file = File(entity.filePath)
            if (file.exists()) file.delete()
            downloadedSongDao.deleteBySongId(songId)
        }
        _downloadStatuses[songId]?.value = DownloadStatus.Idle
    }
}
