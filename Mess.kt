package com.foodmess.platform.domain.model

import com.google.firebase.firestore.GeoPoint

data class Mess(
    val messId: String = "",
    val ownerId: String = "",
    val messName: String = "",
    val description: String = "",
    val address: String = "",
    val location: GeoPoint? = null,
    val geoHash: String = "",
    val foodType: String = "veg", // veg, non_veg, both
    val status: String = "pending", // pending, active, suspended, rejected
    val deliveryTimes: DeliveryTimes = DeliveryTimes(),
    val deliveriesPerDay: Int = 2,
    val plans: MessPlans = MessPlans(),
    val pausedDates: List<String> = emptyList(),
    val regularOffDays: List<Int> = emptyList(),
    val isPausedToday: Boolean = false,
    val photos: List<String> = emptyList(),
    val rating: Double = 0.0,
    val totalSubscribers: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val verifiedAt: Long? = null
)

data class DeliveryTimes(
    val morning: DeliveryTimeSlot = DeliveryTimeSlot(),
    val evening: DeliveryTimeSlot = DeliveryTimeSlot()
)

data class DeliveryTimeSlot(
    val time: String = "08:00",
    val cutoffHours: Int = 3,
    val available: Boolean = true
)

data class MessPlans(
    val morningOnly: PlanOption = PlanOption(),
    val eveningOnly: PlanOption = PlanOption(),
    val both: PlanOption = PlanOption()
)

data class PlanOption(
    val halfMonth: PlanDetails = PlanDetails(29, 50.0),
    val fullMonth: PlanDetails = PlanDetails(58, 45.0)
)

data class PlanDetails(
    val tiffins: Int = 0,
    val pricePerTiffin: Double = 0.0
) {
    val totalPrice: Double
        get() = tiffins * pricePerTiffin
}