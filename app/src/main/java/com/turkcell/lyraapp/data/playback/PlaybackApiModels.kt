package com.turkcell.lyraapp.data.playback

import com.google.gson.annotations.SerializedName
import com.turkcell.lyraapp.data.song.SongDto

// ─── POST /api/v1/me/playback/next ───

data class PlaybackNextRequestDto(
    @SerializedName("songId") val songId: String
)

/**
 * /playback/next yanit zarfi.
 *
 * API, `data.type` alanina gore iki farkli yapidan birini doner:
 * - type == "song": Dogrudan sarki (premium veya henuz reklam sirasi gelmemis)
 * - type == "ad": Once reklam, ardindan sarki
 *
 * Gson ile polymorphic deserialization yerine tum alanlar nullable tutulur
 * ve `type` alanina gore manual branching yapilir (bkz. RetrofitPlaybackRepository).
 */
data class PlaybackNextResponseDto(
    @SerializedName("data") val data: PlaybackDataDto
)

data class PlaybackDataDto(
    @SerializedName("type") val type: String,
    // Her iki durumda da mevcut
    @SerializedName("song") val song: SongDto? = null,
    @SerializedName("stream") val stream: StreamLinkDto? = null,
    // Yalnizca type == "ad" durumunda dolu
    @SerializedName("ad") val ad: AdDto? = null,
    @SerializedName("adStream") val adStream: StreamLinkDto? = null,
    @SerializedName("impressionId") val impressionId: String? = null
)

data class StreamLinkDto(
    @SerializedName("url") val url: String,
    @SerializedName("expiresAt") val expiresAt: String,
    @SerializedName("mimeType") val mimeType: String
)

data class AdDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("advertiser") val advertiser: String,
    @SerializedName("durationMs") val durationMs: Int,
    @SerializedName("mimeType") val mimeType: String
)

// ─── POST /api/v1/me/playback/ad-complete ───

data class AdCompleteRequestDto(
    @SerializedName("impressionId") val impressionId: String
)

data class AdCompleteResponseDto(
    @SerializedName("data") val data: AdCompleteDataDto
)

data class AdCompleteDataDto(
    @SerializedName("completed") val completed: Boolean
)
