package com.turkcell.lyraapp.data.auth

interface AuthRepository {
    suspend fun requestOtp(phoneNumber: String): Result<Boolean>
    
    suspend fun verifyOtp(phoneNumber: String, code: String): Result<Boolean>
    
    suspend fun completeProfile(firstName: String, lastName: String, birthDate: String): Result<Unit>
    
    suspend fun logout(): Result<Unit>
}
