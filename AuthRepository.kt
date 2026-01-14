package com.foodmess.platform.domain.repository

import com.foodmess.platform.domain.model.User
import com.foodmess.platform.utils.Result
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun getCurrentUser(): FirebaseUser?
    suspend fun getUserProfile(uid: String): Result<User?>
    suspend fun isUserProfileComplete(uid: String): Result<Boolean>
    suspend fun signOut(): Result<Unit>
    // Add these methods to the existing AuthRepository interface
    suspend fun sendOtp(phoneNumber: String, activity: android.app.Activity): Result<String>
    suspend fun verifyOtp(verificationId: String, otp: String): Result<FirebaseUser>
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser>
}