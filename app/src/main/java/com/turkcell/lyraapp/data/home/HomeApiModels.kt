package com.turkcell.lyraapp.data.home

import com.google.gson.annotations.SerializedName

data class RecordPlayRequestDto(
    @SerializedName("songId") val songId: String
)
