package com.foodmess.platform.domain.model

data class PaymentItem(
    val id: String,
    val type: PaymentType,
    val messName: String,
    val amount: Double,
    val paymentId: String,
    val date: Long,
    val status: PaymentStatus
)

enum class PaymentType {
    TRIAL_TIFFIN,
    SUBSCRIPTION
}

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED
}