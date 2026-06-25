package com.turkcell.lyraapp.data.auth

import com.google.gson.annotations.SerializedName

data class OtpRequest(
    @SerializedName("phone") val phone: String
)

data class OtpResponse(
    @SerializedName("data") val data: OtpResponseData
)

data class OtpResponseData(
    @SerializedName("sent") val sent: Boolean,
    @SerializedName("firstTime") val firstTime: Boolean
)

data class VerifyOtpRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("code") val code: String
)

data class VerifyOtpResponse(
    @SerializedName("data") val data: AuthTokensData
)

data class AuthTokensData(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("tokenType") val tokenType: String,
    @SerializedName("expiresIn") val expiresIn: Int,
    @SerializedName("user") val user: UserDto,
    @SerializedName("firstTime") val firstTime: Boolean
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("birthDate") val birthDate: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("profileCompleted") val profileCompleted: Boolean
)


data class LogoutResponse(
    @SerializedName("data") val data: LogoutData
)

data class LogoutData(
    @SerializedName("revoked") val revoked: Boolean
)
