package com.foodmess.platform.domain.repository

import com.foodmess.platform.domain.model.Mess
import com.foodmess.platform.utils.Result

interface MessRepository {
    suspend fun getNearbyMesses(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<Mess>>

    suspend fun getMessById(messId: String): Result<Mess?>

    suspend fun getAllActiveMesses(): Result<List<Mess>>
}