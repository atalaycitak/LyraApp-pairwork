package com.turkcell.lyraapp.data.membership

/**
 * Membership domain modelleri.
 *
 * DTO'lardan bagimsiz olarak UI ve is mantigi katmani bu modelleri kullanir.
 */

/** Satin alinabilir bir premium plan. */
data class MembershipPlan(
    val id: String,
    val type: String,
    val name: String,
    val description: String,
    val price: String,
    val priceKurus: Int,
    val currency: String,
    val durationDays: Int,
    val autoRenew: Boolean
)

/**
 * Aktif/suresi dolmus uyelik bilgisi.
 *
 * API'daki User schema'sindaki `membership` alanina karsilik gelir.
 * [expiresAt] ISO-8601 date-time formatidir.
 */
data class MembershipInfo(
    val planId: String,
    val type: String,
    val status: String,
    val autoRenew: Boolean,
    val startedAt: String,
    val expiresAt: String
) {
    val isActive: Boolean get() = status == "active"
}

/** Basarili checkout sonucu. */
data class CheckoutResult(
    val transactionId: String,
    val amountKurus: Int,
    val currency: String,
    val membership: MembershipInfo
)
