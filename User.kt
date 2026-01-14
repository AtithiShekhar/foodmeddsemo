package com.foodmess.platform.domain.model

import com.google.firebase.firestore.GeoPoint

data class User(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String? = null,
    val role: String = "customer", // customer, mess_owner, admin
    val foodPreference: String = "veg", // veg, non_veg
    val address: String = "",
    val location: GeoPoint? = null,
    val geoHash: String = "",
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)