package com.turkcell.lyraapp.data.membership

import com.google.gson.annotations.SerializedName

// ─── GET /api/v1/memberships/plans ───

data class MembershipPlansResponseDto(
    @SerializedName("data") val data: List<MembershipPlanDto>
)

data class MembershipPlanDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("priceKurus") val priceKurus: Int,
    @SerializedName("price") val price: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("durationDays") val durationDays: Int,
    @SerializedName("autoRenew") val autoRenew: Boolean
)

// ─── Membership (User icinde de kullanilir) ───

data class MembershipDto(
    @SerializedName("planId") val planId: String,
    @SerializedName("type") val type: String,
    @SerializedName("status") val status: String,
    @SerializedName("autoRenew") val autoRenew: Boolean,
    @SerializedName("startedAt") val startedAt: String,
    @SerializedName("expiresAt") val expiresAt: String
)

// ─── POST /api/v1/memberships/checkout ───

data class CheckoutRequestDto(
    @SerializedName("plan") val plan: String,
    @SerializedName("card") val card: CardDto
)

data class CardDto(
    @SerializedName("number") val number: String,
    @SerializedName("expMonth") val expMonth: Int,
    @SerializedName("expYear") val expYear: Int,
    @SerializedName("cvc") val cvc: String,
    @SerializedName("holderName") val holderName: String? = null
)

data class CheckoutResponseDto(
    @SerializedName("data") val data: CheckoutDataDto
)

data class CheckoutDataDto(
    @SerializedName("payment") val payment: PaymentDto,
    @SerializedName("membership") val membership: MembershipDto
)

data class PaymentDto(
    @SerializedName("transactionId") val transactionId: String,
    @SerializedName("amountKurus") val amountKurus: Int,
    @SerializedName("currency") val currency: String
)
