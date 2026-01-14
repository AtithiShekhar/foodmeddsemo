package com.foodmess.platform.domain.model

data class Subscription(
    val subscriptionId: String = "",
    val userId: String = "",
    val messId: String = "",
    val messName: String = "", // Denormalized for easy display
    val planType: String = "fullMonth", // halfMonth, fullMonth
    val deliveryTime: String = "both", // morning, evening, both
    val tiffinsPerDelivery: Int = 2,
    val totalTiffins: Int = 116,
    val consumedTiffins: Int = 0,
    val pricePerTiffin: Double = 40.0,
    val totalAmount: Double = 4640.0,
    val startDate: Long = 0,
    val expectedEndDate: Long = 0,
    val actualEndDate: Long? = null,
    val status: String = "pending", // pending, active, completed, cancelled
    val paymentId: String = "",
    val absentDates: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val activatedAt: Long? = null,
    val completedAt: Long? = null
) {
    val remainingTiffins: Int
        get() = totalTiffins - consumedTiffins

    val progressPercentage: Float
        get() = if (totalTiffins > 0) (consumedTiffins.toFloat() / totalTiffins) else 0f
}