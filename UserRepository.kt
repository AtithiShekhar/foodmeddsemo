package com.foodmess.platform.domain.repository

import com.foodmess.platform.domain.model.User
import com.foodmess.platform.utils.Result

interface UserRepository {
    suspend fun createUserProfile(user: User): Result<Unit>
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit>
    suspend fun getUserProfile(uid: String): Result<User?>

}