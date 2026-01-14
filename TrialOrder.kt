package com.foodmess.platform.domain.model

data class TrialOrder(
    val trialOrderId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val userAddress: String = "",
    val messId: String = "",
    val messName: String = "",
    val deliveryTime: String = "", // morning or evening
    val deliveryDate: String = "", // yyyy-MM-dd format
    val amount: Double = 0.0,
    val status: String = "pending", // pending, confirmed, delivered, cancelled
    val paymentId: String = "",
    val createdAt: Long = 0L,
    val deliveredAt: Long? = null
)