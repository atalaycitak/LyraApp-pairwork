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
}
