package com.turkcell.lyraapp.data.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/otp/request")
    suspend fun requestOtp(@Body request: OtpRequest): Response<OtpResponse>

    @POST("api/v1/auth/otp/verify")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(): Response<VerifyOtpResponse> // It returns same auth token format

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<LogoutResponse>

    @POST("api/v1/me/update-informations")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UpdateProfileResponse>
}
