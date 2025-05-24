package com.iie.st10320489.marene.data.entities

import com.google.firebase.database.PropertyName

data class Transaction(
    var transactionId: String = "",
    var userId: String = "",
    var name: String = "",
    var amount: Double = 0.0,
    var transactionMethod: String = "",
    var location: String? = null,
    var dateTime: String = "",
    var description: String = "",
    var photo: String = "",
    var categoryId: String = "",
    var subCategoryId: String? = null,
    var expense: Boolean = true,
)
