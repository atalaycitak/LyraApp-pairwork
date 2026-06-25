package com.turkcell.lyraapp.data.common

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // /auth/otp/* ve /auth/refresh endpoint'lerine token eklenmez.
        // OTP: henuz oturum acilmamistir. Refresh: TokenAuthenticator refresh token'i
        // body'de gonderir, burada eski access token gonderilmesi yenileme dongusunu bozar.
        if (originalRequest.url.encodedPath.contains("/auth/otp/") ||
            originalRequest.url.encodedPath.contains("/auth/refresh")) {
            return chain.proceed(originalRequest)
        }

        val accessToken = tokenManager.getAccessToken()
        
        return if (accessToken != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
