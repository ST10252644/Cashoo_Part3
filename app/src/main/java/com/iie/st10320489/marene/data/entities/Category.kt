package com.iie.st10320489.marene.data.entities

data class Category(
    val categoryId: String = "", // Firestore document ID
    val userId: String = "",
    val categoryName: String = "", // Fixed typo here
    val icon: Int = 0,
    val colour: Int = 0
)
