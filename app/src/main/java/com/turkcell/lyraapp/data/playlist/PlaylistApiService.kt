package com.turkcell.lyraapp.data.playlist

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PlaylistApiService {

    @GET("api/v1/playlists")
    suspend fun getPlaylists(): PlaylistsResponseDto

    @GET("api/v1/playlists/{id}")
    suspend fun getPlaylistDetail(@Path("id") id: String): PlaylistDetailResponseDto

    @GET("api/v1/me/playlists")
    suspend fun getMyPlaylists(): PlaylistsResponseDto

    @POST("api/v1/me/playlists")
    suspend fun createPlaylist(@Body request: CreatePlaylistRequestDto): PlaylistResponseDto

    @POST("api/v1/me/playlists/{id}/tracks")
    suspend fun addTrackToPlaylist(@Path("id") playlistId: String, @Body request: AddTrackRequestDto)

    @DELETE("api/v1/me/playlists/{id}/tracks/{songId}")
    suspend fun removeTrackFromPlaylist(@Path("id") playlistId: String, @Path("songId") songId: String)

    @retrofit2.http.PATCH("api/v1/me/playlists/{id}")
    suspend fun renamePlaylist(@Path("id") playlistId: String, @Body request: RenamePlaylistRequestDto)
}

data class RenamePlaylistRequestDto(val name: String)
