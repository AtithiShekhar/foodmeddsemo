package com.foodmess.platform.data.repository

import com.foodmess.platform.domain.model.Subscription
import com.foodmess.platform.domain.repository.SubscriptionRepository
import com.foodmess.platform.utils.Constants
import com.foodmess.platform.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : SubscriptionRepository {

    override suspend fun createSubscription(
        messId: String,
        messName: String,
        planType: String,
        deliveryTime: String,
        tiffinsPerDelivery: Int,
        totalTiffins: Int,
        pricePerTiffin: Double,
        totalAmount: Double
    ): Result<String> {
        return try {
            val userId = firebaseAuth.currentUser?.uid
                ?: return Result.Error(Exception("User not authenticated"))

            val subscription = hashMapOf(
                "userId" to userId,
                "messId" to messId,
                "messName" to messName,
                "planType" to planType,
                "deliveryTime" to deliveryTime,
                "tiffinsPerDelivery" to tiffinsPerDelivery,
                "totalTiffins" to totalTiffins,
                "consumedTiffins" to 0,
                "pricePerTiffin" to pricePerTiffin,
                "totalAmount" to totalAmount,
                "startDate" to System.currentTimeMillis(),
                "expectedEndDate" to System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // ~30 days
                "status" to "active", // In production, this would be "pending" until payment verified
                "paymentId" to "mock_payment_${System.currentTimeMillis()}", // Mock for now
                "absentDates" to emptyList<String>(),
                "createdAt" to System.currentTimeMillis(),
                "activatedAt" to System.currentTimeMillis()
            )

            val docRef = firestore.collection(Constants.COLLECTION_SUBSCRIPTIONS)
                .add(subscription)
                .await()

            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getUserSubscriptions(userId: String): Result<List<Subscription>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_SUBSCRIPTIONS)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val subscriptions = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Subscription::class.java)?.copy(subscriptionId = doc.id)
            }

            Result.Success(subscriptions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getActiveSubscriptions(userId: String): Result<List<Subscription>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_SUBSCRIPTIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "active")
                .get()
                .await()

            val subscriptions = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Subscription::class.java)?.copy(subscriptionId = doc.id)
            }

            Result.Success(subscriptions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getSubscriptionById(subscriptionId: String): Result<Subscription?> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_SUBSCRIPTIONS)
                .document(subscriptionId)
                .get()
                .await()

            val subscription = if (snapshot.exists()) {
                snapshot.toObject(Subscription::class.java)?.copy(subscriptionId = snapshot.id)
            } else {
                null
            }

            Result.Success(subscription)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun markAbsent(subscriptionId: String, date: String): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_SUBSCRIPTIONS)
                .document(subscriptionId)
                .update("absentDates", FieldValue.arrayUnion(date))
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    override suspend fun cancelSubscription(
        subscriptionId: String,
        reason: String
    ): Result<Unit> {
        return try {
            val userId = firebaseAuth.currentUser?.uid
                ?: return Result.Error(Exception("User not authenticated"))

            // Get subscription details
            val subscriptionDoc = firestore.collection(Constants.COLLECTION_SUBSCRIPTIONS)
                .document(subscriptionId)
                .get()
                .await()

            if (!subscriptionDoc.exists()) {
                return Result.Error(Exception("Subscription not found"))
            }

            val subscription = subscriptionDoc.toObject(Subscription::class.java)
                ?: return Result.Error(Exception("Failed to parse subscription"))

            // Calculate refund
            val remainingTiffins = subscription.totalTiffins - subscription.consumedTiffins
            val consumedAmount = subscription.consumedTiffins * subscription.pricePerTiffin
            val remainingAmount = remainingTiffins * subscription.pricePerTiffin
            val cancellationFee = remainingAmount * 0.20
            val refundAmount = remainingAmount * 0.80

            // Get user details
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val userName = userDoc.getString("name") ?: "User"
            val userPhone = userDoc.getString("phone") ?: ""

            // Create refund request
            val refundRequest = hashMapOf(
                "userId" to userId,
                "userName" to userName,
                "userPhone" to userPhone,
                "messId" to subscription.messId,
                "messName" to subscription.messName,
                "subscriptionId" to subscriptionId,
                "totalAmount" to subscription.totalAmount,
                "consumedTiffins" to subscription.consumedTiffins,
                "consumedAmount" to consumedAmount,
                "remainingTiffins" to remainingTiffins,
                "remainingAmount" to remainingAmount,
                "refundAmount" to refundAmount,
                "cancellationFee" to cancellationFee,
                "reason" to reason,
                "status" to "pending",
                "requestedAt" to System.currentTimeMillis(),
                "processedAt" to null,
                "processedBy" to null
            )

            firestore.collection("refund_requests")
                .add(refundRequest)
                .await()

            // Update subscription status
            firestore.collection(Constants.COLLECTION_SUBSCRIPTIONS)
                .document(subscriptionId)
                .update(
                    mapOf(
                        "status" to "cancelled",
                        "actualEndDate" to System.currentTimeMillis()
                    )
                )
                .await()

            // DELETE ALL FUTURE DELIVERIES (FROM TOMORROW ONWARDS)
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
            val tomorrow = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.time)

            android.util.Log.d("CancelSubscription", "Looking for deliveries from: $tomorrow")
            android.util.Log.d("CancelSubscription", "Subscription ID: $subscriptionId")

            val futureDeliveries = firestore.collection("deliveries")
                .whereEqualTo("subscriptionId", subscriptionId)
                .whereGreaterThanOrEqualTo("date", tomorrow)
                .get()
                .await()

            android.util.Log.d("CancelSubscription", "Found ${futureDeliveries.documents.size} future deliveries to delete")

// Delete each future delivery
            val batch = firestore.batch()
            futureDeliveries.documents.forEach { doc ->
                android.util.Log.d("CancelSubscription", "Deleting delivery: ${doc.id} for date: ${doc.getString("date")}")
                batch.delete(doc.reference)
            }
            batch.commit().await()

            android.util.Log.d("CancelSubscription", "Successfully deleted ${futureDeliveries.documents.size} deliveries")

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}