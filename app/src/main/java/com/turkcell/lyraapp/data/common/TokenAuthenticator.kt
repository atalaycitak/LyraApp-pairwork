package com.turkcell.lyraapp.data.common

import com.turkcell.lyraapp.data.auth.AuthApiService
import com.turkcell.lyraapp.data.auth.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * HTTP 401 Unauthorized hatalarını yakalayarak arka planda sessizce token yenileyen (refresh) sınıf.
 *
 * Eğer Access Token süresi dolmuşsa, Retrofit üzerinden gelen istek kesilir ve bu sınıfa düşer.
 * [TokenManager] üzerinden alınan `refreshToken` kullanılarak yeni token'lar istenir.
 * İşlem başarılı olursa orijinal istek yeni token ile tekrar gönderilir.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiServiceProvider: Provider<AuthApiService> // Döngüsel bağımlılığı (Circular Dependency) kırmak için Provider kullanılır.
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val currentRefreshToken = tokenManager.getRefreshToken()
        
        // Refresh token yoksa veya zaten /auth/refresh uçundan 401 dönmüşsek çıkış yap.
        if (currentRefreshToken == null || response.request.url.encodedPath.contains("auth/refresh")) {
            return null
        }

        // Authenticator senkron çalışır ancak AuthApiService suspend metot içerir.
        // runBlocking ile IO iş parçacığı bloke edilir ki bu Authenticator'lar için beklenen/doğru bir yöntemdir.
        return runBlocking {
            try {
                val refreshRequest = RefreshTokenRequest(currentRefreshToken)
                val refreshResponse = authApiServiceProvider.get().refresh(refreshRequest)

                if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                    val data = refreshResponse.body()!!.data
                    
                    // Yeni token'ları kaydet
                    tokenManager.saveTokens(data.accessToken, data.refreshToken)

                    // İsteği yeni token ile tekrar oluştur
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${data.accessToken}")
                        .build()
                } else {
                    // Yenileme başarısız (örn: Refresh Token'ın da süresi dolmuş)
                    tokenManager.clearTokens()
                    null
                }
            } catch (e: Exception) {
                // Herhangi bir ağ problemi
                tokenManager.clearTokens()
                null
            }
        }
    }
}
