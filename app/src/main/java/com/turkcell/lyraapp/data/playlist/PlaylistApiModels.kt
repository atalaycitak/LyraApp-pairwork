package com.turkcell.lyraapp.data.playlist

import com.google.gson.annotations.SerializedName
import com.turkcell.lyraapp.data.song.SongDto

data class PlaylistsResponseDto(
    @SerializedName("data") val data: List<PlaylistDto>,
)

data class PlaylistDetailResponseDto(
    @SerializedName("data") val data: PlaylistWithSongsDto,
)

data class PlaylistDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("createdAt") val createdAt: String,
)

data class PlaylistWithSongsDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("songs") val songs: List<SongDto>,
)
