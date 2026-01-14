package com.foodmess.platform.data.repository

import com.foodmess.platform.domain.model.Mess
import com.foodmess.platform.domain.repository.MessRepository
import com.foodmess.platform.utils.Constants
import com.foodmess.platform.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.*
import com.foodmess.platform.data.repository.MessRepositoryImpl

class MessRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MessRepository {

    override suspend fun getNearbyMesses(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<Mess>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_MESSES)
                .whereEqualTo("status", "active")
                .get()
                .await()

            val messes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Mess::class.java)?.copy(messId = doc.id)
            }

            // Filter by distance
            val nearbyMesses = messes.filter { mess ->
                mess.location?.let { location ->
                    val distance = calculateDistance(
                        latitude, longitude,
                        location.latitude, location.longitude
                    )
                    distance <= radiusKm
                } ?: false
            }

            Result.Success(nearbyMesses)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getMessById(messId: String): Result<Mess?> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_MESSES)
                .document(messId)
                .get()
                .await()

            val mess = if (snapshot.exists()) {
                snapshot.toObject(Mess::class.java)?.copy(messId = snapshot.id)
            } else {
                null
            }

            Result.Success(mess)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getAllActiveMesses(): Result<List<Mess>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_MESSES)
                .whereEqualTo("status", "active")
                .get()
                .await()

            val messes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Mess::class.java)?.copy(messId = doc.id)
            }

            Result.Success(messes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Haversine formula for distance calculation
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}