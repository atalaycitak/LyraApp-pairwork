package com.turkcell.lyraapp.data.profile

import com.google.gson.annotations.SerializedName
import com.turkcell.lyraapp.data.auth.UserDto

data class UpdateProfileRequest(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("birthDate") val birthDate: String
)

data class UserResponseDto(
    @SerializedName("data") val data: UserDto
)
