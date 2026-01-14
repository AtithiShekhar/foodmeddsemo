package com.foodmess.platform.domain.model

data class RefundRequest(
    val refundRequestId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val messId: String = "",
    val messName: String = "",
    val subscriptionId: String = "",
    val totalAmount: Double = 0.0,
    val consumedTiffins: Int = 0,
    val consumedAmount: Double = 0.0,
    val remainingTiffins: Int = 0,
    val remainingAmount: Double = 0.0,
    val refundAmount: Double = 0.0,
    val cancellationFee: Double = 0.0,
    val reason: String = "",
    val status: String = "pending", // pending, processed, rejected
    val requestedAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val processedBy: String? = null
)