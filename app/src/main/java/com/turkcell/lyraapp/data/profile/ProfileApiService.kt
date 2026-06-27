package com.turkcell.lyraapp.data.profile

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ProfileApiService {

    @GET("api/v1/me")
    suspend fun getMe(): Response<UserResponseDto>

    @POST("api/v1/me/update-informations")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserResponseDto>

    @POST("api/v1/me/plays")
    suspend fun recordPlay(@Body request: RecordPlayRequestDto): Response<RecordPlayResponseDto>
}

data class RecordPlayRequestDto(val songId: String)
data class RecordPlayResponseDto(
    val data: RecordPlayData?
)
data class RecordPlayData(
    val recorded: Boolean
)
