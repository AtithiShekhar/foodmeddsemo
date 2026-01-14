package com.foodmess.platform.data.repository

import com.foodmess.platform.domain.model.User
import com.foodmess.platform.domain.repository.AuthRepository
import com.foodmess.platform.utils.Constants
import com.foodmess.platform.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import android.app.Activity
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override suspend fun getUserProfile(uid: String): Result<User?> {
        return try {
            val snapshot = firestore
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .await()

            if (snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                Result.Success(user)
            } else {
                Result.Success(null)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun isUserProfileComplete(uid: String): Result<Boolean> {
        return try {
            val snapshot = firestore
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .await()

            Result.Success(snapshot.exists())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    override suspend fun sendOtp(phoneNumber: String, activity: Activity): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-verification (rare on real devices)
                    continuation.resume(Result.Success("auto-verified"))
                }

                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    continuation.resume(Result.Error(Exception(e.message ?: "Verification failed")))
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    continuation.resume(Result.Success(verificationId))
                }
            }

            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber("+91$phoneNumber") // India country code
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    override suspend fun verifyOtp(verificationId: String, otp: String): Result<FirebaseUser> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {
                Result.Success(user)
            } else {
                Result.Error(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {
                Result.Success(user)
            } else {
                Result.Error(Exception("Google sign-in failed"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}