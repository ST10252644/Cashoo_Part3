package com.iie.st10320489.marene.ui.transaction

import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.data.entities.Transaction

import com.iie.st10320489.marene.data.entities.Category
import com.iie.st10320489.marene.data.entities.TransactionWithCategory

class TransactionRepository {

    private val firestore = FirebaseFirestore.getInstance()
    // (Android Developers, 2025)
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
    // (Android Developers, 2025)
    //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
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
//Bibliography

//College, I. V., 2025. PROG7313 Module-Manual / Module-Outline. Pretoria: Varsity College Pretoria.
//Available at: hRps://developer.android.com/developer/ui/views/layout/declaring-layout [Accessed 23 April 2025].
//Kay, R. M., 2022. IntroducKon To Development WithAndroid Studio: XML The Five Minute Language. [Online]
//Available at: hRps://youtu.be/94tm21PIBMs?si=BpJQ9meXr1_ynL2m
//[Accessed 15 April 2025].
//Team, G. D. T., 2024. Add repository and Manual DI. [Online]
//Available at: hRps://developer.android.com/codelabs/basic-android-kotlin-compose-add- repository#0
//[Accessed 22 April 2025].
//Coder, O., 2022. Implament Pie Chart in Android Studio Using Kotlin. [Online] Available at: hRps://youtu.be/TUJHcU0FOkA?si=jk90LRSO1_eyMyIG
//[Accessed 24 April 2025].
//Coder, E. O., 2024. hot to create bar chart | MP Android Chart | Android Studio 2024. [Online]
//Available at: hRps://youtu.be/WdsmQ3Zyn84?si=jz2AtkIRsNEUwNbX
//[Accessed 23 April 2025].
//Firebase, 2023. Ge=ng Started with Firebase on Android. [Online] Available at: hLps://youtu.be/jbHfJpoOzkl?si=rQ0hPeu_qKWpuAlm [Accessed 27 May 2025].
//Technology, S., 2020. 017 How to create MP Android Chart from Firebase RealKme Database. [Online]
//Available at: hLps://youtu.be/C0O9u0jd6nQ?si=c-H-xO4ISG2DWqQx [Accessed 22 May 2025].
//GeeksforGeeks, 2024. How to Create and Add Data to Firebase Firestore in Android. [Online] Available at: hLps://www.geeksforgeeks.org/create-and-add-data-to-firebase-firestore-in- android/
//[Accessed 23 May 2025].