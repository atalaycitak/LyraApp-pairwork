package com.turkcell.lyraapp.data.membership

import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class RetrofitMembershipRepositoryTest {

    @Test
    fun `getPlans maps membership plan dtos to domain models`() = runTest {
        val repository = RetrofitMembershipRepository(
            api = FakeMembershipApiService(
                plans = listOf(
                    recurringPlanDto(),
                    oneTimePlanDto()
                )
            )
        )

        val plans = repository.getPlans().getOrThrow()

        assertEquals(2, plans.size)
        assertEquals("recurring", plans[0].id)
        assertEquals("Premium Aylik", plans[0].name)
        assertEquals(13900, plans[0].priceKurus)
        assertEquals(true, plans[0].autoRenew)
        assertEquals("one-time", plans[1].id)
        assertEquals(false, plans[1].autoRenew)
    }

    @Test
    fun `checkout maps successful response to checkout result`() = runTest {
        val repository = RetrofitMembershipRepository(
            api = FakeMembershipApiService(
                checkoutResponse = Response.success(successfulCheckoutResponse())
            )
        )

        val result = repository.checkout("recurring", validCard()).getOrThrow()

        assertEquals("txn-123", result.transactionId)
        assertEquals(13900, result.amountKurus)
        assertEquals("TRY", result.currency)
        assertEquals("recurring", result.membership.planId)
        assertTrue(result.membership.isActive)
    }

    @Test
    fun `checkout returns invalid plan or card message for 400 response`() = runTest {
        val repository = RetrofitMembershipRepository(
            api = FakeMembershipApiService(checkoutResponse = errorResponse(code = 400))
        )

        val result = repository.checkout("recurring", validCard())

        assertTrue(result.isFailure)
        assertEquals("Gecersiz plan veya kart bilgisi.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `checkout returns declined payment message for 402 response`() = runTest {
        val repository = RetrofitMembershipRepository(
            api = FakeMembershipApiService(checkoutResponse = errorResponse(code = 402))
        )

        val result = repository.checkout("recurring", validCard())

        assertTrue(result.isFailure)
        assertEquals(
            "Odeme reddedildi. Lutfen kart bilgilerinizi kontrol edin.",
            result.exceptionOrNull()?.message
        )
    }

    private class FakeMembershipApiService(
        private val plans: List<MembershipPlanDto> = emptyList(),
        private val checkoutResponse: Response<CheckoutResponseDto> = Response.success(successfulCheckoutResponse())
    ) : MembershipApiService {

        override suspend fun getPlans(): MembershipPlansResponseDto =
            MembershipPlansResponseDto(plans)

        override suspend fun checkout(request: CheckoutRequestDto): Response<CheckoutResponseDto> =
            checkoutResponse
    }

    private companion object {
        fun recurringPlanDto() = MembershipPlanDto(
            id = "recurring",
            type = "recurring",
            name = "Premium Aylik",
            description = "Aylik yenilenen premium uyelik",
            priceKurus = 13900,
            price = "139 TL",
            currency = "TRY",
            durationDays = 30,
            autoRenew = true
        )

        fun oneTimePlanDto() = MembershipPlanDto(
            id = "one-time",
            type = "one-time",
            name = "Premium Tek Seferlik",
            description = "Tek seferlik premium uyelik",
            priceKurus = 15900,
            price = "159 TL",
            currency = "TRY",
            durationDays = 30,
            autoRenew = false
        )

        fun successfulCheckoutResponse() = CheckoutResponseDto(
            data = CheckoutDataDto(
                payment = PaymentDto(
                    transactionId = "txn-123",
                    amountKurus = 13900,
                    currency = "TRY"
                ),
                membership = MembershipDto(
                    planId = "recurring",
                    type = "recurring",
                    status = "active",
                    autoRenew = true,
                    startedAt = "2026-06-27T10:00:00Z",
                    expiresAt = "2026-07-27T10:00:00Z"
                )
            )
        )

        fun validCard() = CardDto(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2028,
            cvc = "123",
            holderName = "Zeynep Kaya"
        )

        fun errorResponse(code: Int): Response<CheckoutResponseDto> =
            Response.error(code, "{}".toResponseBody())
    }
}
