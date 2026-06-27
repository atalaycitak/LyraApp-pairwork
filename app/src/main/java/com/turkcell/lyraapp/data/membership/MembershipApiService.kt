package com.turkcell.lyraapp.data.membership

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Membership (uyelik) API endpoint tanimlari.
 *
 * OpenAPI spesifikasyonundaki /api/v1/memberships/ altindaki uclari karsilar.
 */
interface MembershipApiService {

    /**
     * GET /api/v1/memberships/plans
     *
     * Satin alinabilir premium planlari listeler.
     * recurring: aylik yenilenen (139 TL), one-time: tek seferlik (159 TL).
     */
    @GET("api/v1/memberships/plans")
    suspend fun getPlans(): MembershipPlansResponseDto

    /**
     * POST /api/v1/memberships/checkout
     *
     * Mock kart odemesi ile premium uyelik satin alir.
     * Basarili: 201, Gecersiz kart/plan: 400, Token hatasi: 401, Odeme reddedildi: 402.
     */
    @POST("api/v1/memberships/checkout")
    suspend fun checkout(@Body request: CheckoutRequestDto): Response<CheckoutResponseDto>
}
