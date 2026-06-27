package com.turkcell.lyraapp.data.membership

/**
 * Membership veri kaynaği soyutlaması.
 *
 * [RetrofitMembershipRepository] bu interface'i implement eder;
 * DI bağlaması [di/MembershipModule.kt] üzerinden yapılır.
 */
interface MembershipRepository {

    /** Satın alınabilir premium planları listeler. */
    suspend fun getPlans(): Result<List<MembershipPlan>>

    /**
     * Kart bilgileriyle premium üyelik satın alır.
     *
     * [planId]: "one-time" veya "recurring".
     * Başarılı (201): [CheckoutResult] döner.
     * 400: Geçersiz plan veya kart bilgisi.
     * 402: Ödeme reddedildi.
     */
    suspend fun checkout(planId: String, card: CardDto): Result<CheckoutResult>
}
