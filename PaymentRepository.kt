package com.foodmess.platform.domain.repository

import com.foodmess.platform.utils.Result

interface PaymentRepository {
    suspend fun createRazorpayOrder(
        amount: Double,
        currency: String = "INR"
    ): Result<RazorpayOrder>
}

data class RazorpayOrder(
    val orderId: String,
    val amount: Int, // Amount in paise (smallest currency unit)
    val currency: String
)