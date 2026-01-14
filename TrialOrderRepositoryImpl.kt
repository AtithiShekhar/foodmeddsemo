package com.foodmess.platform.data.repository

import com.foodmess.platform.domain.model.TrialOrder
import com.foodmess.platform.domain.repository.TrialOrderRepository
import com.foodmess.platform.utils.Constants
import com.foodmess.platform.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TrialOrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : TrialOrderRepository {

    override suspend fun createTrialOrder(
        messId: String,
        messName: String,
        deliveryTime: String,
        deliveryDate: String,
        amount: Double
    ): Result<String> {
        return try {
            val currentUser = auth.currentUser ?: return Result.Error(Exception("Not authenticated"))

            // Get user details
            val userDoc = firestore.collection(Constants.COLLECTION_USERS)
                .document(currentUser.uid)
                .get()
                .await()

            val user = userDoc.toObject(com.foodmess.platform.domain.model.User::class.java)

            val trialOrder = TrialOrder(
                userId = currentUser.uid,
                userName = user?.name ?: "",
                userPhone = user?.phone ?: "",
                userAddress = user?.address ?: "",
                messId = messId,
                messName = messName,
                deliveryTime = deliveryTime,
                deliveryDate = deliveryDate,
                amount = amount,
                status = "confirmed",
                createdAt = System.currentTimeMillis()
            )

            val docRef = firestore.collection(Constants.COLLECTION_TRIAL_ORDERS)
                .add(trialOrder)
                .await()

            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getUserTrialOrders(userId: String): Result<List<TrialOrder>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_TRIAL_ORDERS)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(TrialOrder::class.java)?.copy(trialOrderId = doc.id)
            }

            Result.Success(orders)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun checkIfTrialOrderExists(userId: String, messId: String): Result<Boolean> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_TRIAL_ORDERS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("messId", messId)
                .get()
                .await()

            Result.Success(snapshot.documents.isNotEmpty())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}