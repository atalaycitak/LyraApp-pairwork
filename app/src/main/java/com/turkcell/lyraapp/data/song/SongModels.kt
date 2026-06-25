package com.turkcell.lyraapp.data.song

import com.google.gson.annotations.SerializedName

/**
 * API'nın /api/v1/songs endpoint'inden dönen tek şarkı modeli.
 *
 * Alan isimleri openapi.json şemasıyla (Song) birebir eşleşir.
 * Kapak görseli API tarafından sağlanmamaktadır; UI katmanı şarkı ID'sinin
 * hash değerinden deterministik renk çifti üretir (bkz. RetrofitHomeRepository).
 */
data class SongDto(
    @SerializedName("id")          val id: String,
    @SerializedName("title")       val title: String,
    @SerializedName("artist")      val artist: String,
    @SerializedName("album")       val album: String?,
    @SerializedName("durationMs")  val durationMs: Long,
    @SerializedName("mimeType")    val mimeType: String,
    @SerializedName("sizeBytes")   val sizeBytes: Long,
    @SerializedName("createdAt")   val createdAt: String,
)

/**
 * GET /api/v1/songs yanıt zarfı.
 *
 * Cursor tabanlı sayfalama: [nextCursor] null olduğunda tüm liste tüketilmiştir.
 */
data class SongsResponseDto(
    @SerializedName("data")       val data: List<SongDto>,
    @SerializedName("nextCursor") val nextCursor: String?,
)

/**
 * GET /api/v1/songs/{id} yanıt zarfı.
 */
data class SongDetailResponseDto(
    val data: SongDto,
)

/**
 * GET /api/v1/songs/{id}/stream-url yanıt zarfı.
 *
 * [url] doğrudan ExoPlayer'a beslenir; TTL 300 saniyedir (bkz. api-overview.md).
 * [expiresAt] ve [mimeType] ek bilgi olarak tutulur; şu iterasyonda kullanılmaz.
 */
data class StreamUrlResponseDto(
    @SerializedName("data") val data: StreamUrlDataDto,
)

data class StreamUrlDataDto(
    @SerializedName("url")       val url: String,
    @SerializedName("expiresAt") val expiresAt: String,
    @SerializedName("mimeType")  val mimeType: String,
)
