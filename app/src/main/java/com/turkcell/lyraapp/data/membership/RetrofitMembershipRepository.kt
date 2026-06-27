package com.turkcell.lyraapp.data.membership

import javax.inject.Inject

/**
 * [MembershipRepository]'nin Retrofit tabanli gercek implementasyonu.
 *
 * Hata yonetimi:
 * - 400: Gecersiz plan veya kart bilgisi
 * - 401: Token hatasi (AuthInterceptor/TokenAuthenticator tarafindan yonetilir)
 * - 402: Odeme reddedildi
 */
class RetrofitMembershipRepository @Inject constructor(
    private val api: MembershipApiService,
) : MembershipRepository {

    override suspend fun getPlans(): Result<List<MembershipPlan>> = runCatching {
        val response = api.getPlans()
        response.data.map { dto ->
            MembershipPlan(
                id = dto.id,
                type = dto.type,
                name = dto.name,
                description = dto.description,
                price = dto.price,
                priceKurus = dto.priceKurus,
                currency = dto.currency,
                durationDays = dto.durationDays,
                autoRenew = dto.autoRenew
            )
        }
    }

    override suspend fun checkout(planId: String, card: CardDto): Result<CheckoutResult> =
        runCatching {
            val request = CheckoutRequestDto(plan = planId, card = card)
            val response = api.checkout(request)

            if (!response.isSuccessful) {
                val errorMessage = when (response.code()) {
                    400 -> "Gecersiz plan veya kart bilgisi."
                    402 -> "Odeme reddedildi. Lutfen kart bilgilerinizi kontrol edin."
                    else -> "Odeme islemi basarisiz: ${response.code()}"
                }
                throw Exception(errorMessage)
            }

            val body = response.body()
                ?: throw Exception("Odeme yaniti alinamadi.")

            val membershipDto = body.data.membership
            CheckoutResult(
                transactionId = body.data.payment.transactionId,
                amountKurus = body.data.payment.amountKurus,
                currency = body.data.payment.currency,
                membership = MembershipInfo(
                    planId = membershipDto.planId,
                    type = membershipDto.type,
                    status = membershipDto.status,
                    autoRenew = membershipDto.autoRenew,
                    startedAt = membershipDto.startedAt,
                    expiresAt = membershipDto.expiresAt
                )
            )
        }
}
