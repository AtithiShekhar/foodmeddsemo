package com.foodmess.platform.domain.repository

import com.foodmess.platform.domain.model.Subscription
import com.foodmess.platform.utils.Result

interface SubscriptionRepository {
    suspend fun createSubscription(
        messId: String,
        messName: String,
        planType: String,
        deliveryTime: String,
        tiffinsPerDelivery: Int,
        totalTiffins: Int,
        pricePerTiffin: Double,
        totalAmount: Double
    ): Result<String>

    suspend fun cancelSubscription(
        subscriptionId: String,
        reason: String
    ): Result<Unit>

    suspend fun getUserSubscriptions(userId: String): Result<List<Subscription>>

    suspend fun getActiveSubscriptions(userId: String): Result<List<Subscription>>

    suspend fun getSubscriptionById(subscriptionId: String): Result<Subscription?>

    suspend fun markAbsent(subscriptionId: String, date: String): Result<Unit>
}