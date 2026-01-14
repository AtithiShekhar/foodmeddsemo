package com.foodmess.platform.domain.repository

import com.foodmess.platform.domain.model.TrialOrder
import com.foodmess.platform.utils.Result

interface TrialOrderRepository {
    suspend fun createTrialOrder(
        messId: String,
        messName: String,
        deliveryTime: String,
        deliveryDate: String,
        amount: Double
    ): Result<String>

    suspend fun getUserTrialOrders(userId: String): Result<List<TrialOrder>>

    suspend fun checkIfTrialOrderExists(userId: String, messId: String): Result<Boolean>
}