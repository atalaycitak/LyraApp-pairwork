package com.turkcell.lyraapp.data.playlist

import retrofit2.http.GET
import retrofit2.http.Path

interface PlaylistApiService {

    @GET("api/v1/playlists")
    suspend fun getPlaylists(): PlaylistsResponseDto

    @GET("api/v1/playlists/{id}")
    suspend fun getPlaylistDetail(@Path("id") id: String): PlaylistDetailResponseDto
}
