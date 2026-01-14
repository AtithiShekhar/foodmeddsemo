package com.foodmess.platform.data.repository

import com.foodmess.platform.domain.repository.PaymentRepository
import com.foodmess.platform.domain.repository.RazorpayOrder
import com.foodmess.platform.utils.Constants
import com.foodmess.platform.utils.Result
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor() : PaymentRepository {

    override suspend fun createRazorpayOrder(
        amount: Double,
        currency: String
    ): Result<RazorpayOrder> {
        return try {
            // Convert amount to paise (smallest unit)
            val amountInPaise = (amount * 100).toInt()

            // For test mode, we can use a mock order ID
            // In production, you'd call your backend to create actual Razorpay order
            val orderId = "order_test_${System.currentTimeMillis()}"

            val order = RazorpayOrder(
                orderId = orderId,
                amount = amountInPaise,
                currency = currency
            )

            Result.Success(order)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}