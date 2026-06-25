package com.turkcell.lyraapp.data.auth

import com.turkcell.lyraapp.data.common.TokenManager
import com.turkcell.lyraapp.data.profile.ProfileApiService
import com.turkcell.lyraapp.data.profile.UpdateProfileRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitAuthRepository @Inject constructor(
    private val apiService: AuthApiService,
    private val profileApiService: ProfileApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun requestOtp(phoneNumber: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.requestOtp(OtpRequest(phoneNumber))
            if (response.isSuccessful && response.body() != null) {
                // Return whether it's a firstTime user
                Result.success(response.body()!!.data.firstTime)
            } else {
                Result.failure(Exception("OTP isteği başarısız oldu: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyOtp(phoneNumber: String, code: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.verifyOtp(VerifyOtpRequest(phoneNumber, code))
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data
                tokenManager.saveTokens(data.accessToken, data.refreshToken)
                Result.success(data.firstTime)
            } else {
                Result.failure(Exception("Doğrulama başarısız. Hatalı kod olabilir."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeProfile(
        firstName: String,
        lastName: String,
        birthDate: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = profileApiService.updateProfile(
                UpdateProfileRequest(firstName, lastName, birthDate)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Profil güncellenemedi."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken != null) {
                apiService.logout(RefreshTokenRequest(refreshToken))
            }
            tokenManager.clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            // Even if api fails, clear tokens locally
            tokenManager.clearTokens()
            Result.failure(e)
        }
    }
}
