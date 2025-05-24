package com.iie.st10320489.marene.ui.transaction

import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.data.entities.Transaction

import com.iie.st10320489.marene.data.entities.Category
import com.iie.st10320489.marene.data.entities.TransactionWithCategory

class TransactionRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getTransactionsByUserId(
        userId: String,
        onSuccess: (List<TransactionWithCategory>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { transactionDocs ->
                val transactions = mutableListOf<TransactionWithCategory>()
                val categoryIds = transactionDocs.mapNotNull { it.getString("categoryId") }.toSet()

                firestore.collection("categories")
                    .whereIn("categoryId", categoryIds.toList())
                    .get()
                    .addOnSuccessListener { categoryDocs ->
                        val categoryMap = categoryDocs.associateBy { it.getString("categoryId") }

                        for (doc in transactionDocs) {
                            val transaction = doc.toObject(Transaction::class.java)
                            val categoryDoc = categoryMap[transaction.categoryId]
                            val category = categoryDoc?.toObject(Category::class.java)
                            if (category != null) {
                                transactions.add(TransactionWithCategory(transaction, category))
                            }
                        }
                        onSuccess(transactions)
                    }
                    .addOnFailureListener { e -> onFailure(e) }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getTransactionWithCategoryById(
        transactionId: String,
        onSuccess: (TransactionWithCategory?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("transactions").document(transactionId)
            .get()
            .addOnSuccessListener { transactionDoc ->
                val transaction = transactionDoc.toObject(Transaction::class.java)
                if (transaction != null) {
                    firestore.collection("categories").document(transaction.categoryId)
                        .get()
                        .addOnSuccessListener { categoryDoc ->
                            val category = categoryDoc.toObject(Category::class.java)
                            if (category != null) {
                                onSuccess(TransactionWithCategory(transaction, category))
                            } else {
                                onSuccess(null)
                            }
                        }
                        .addOnFailureListener { e -> onFailure(e) }
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }
}
