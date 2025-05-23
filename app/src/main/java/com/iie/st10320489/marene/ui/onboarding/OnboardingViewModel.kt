package com.iie.st10320489.marene.ui.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    // Save selected categories to Firestore
    fun saveSelectedCategories(userId: String, selectedCategoryNames: List<String>) {
        println("Saving categories for Firestore userId: $userId")

        viewModelScope.launch(Dispatchers.IO) {
            val categoryDocs = selectedCategoryNames.map { name ->
                val (iconRes, colorRes) = mapCategoryResources(name)
                hashMapOf(
                    "name" to name,
                    "icon" to iconRes,
                    "colour" to colorRes
                )
            }.toMutableList()

            if (!selectedCategoryNames.contains("Other")) {
                categoryDocs.add(
                    hashMapOf(
                        "name" to "Other",
                        "icon" to R.drawable.ic_custom,
                        "colour" to R.color.red
                    )
                )
            }

            try {
                val batch = firestore.batch()
                val collectionRef = firestore.collection("users")
                    .document(userId)
                    .collection("categories")

                categoryDocs.forEach { category ->
                    val docRef = collectionRef.document()
                    batch.set(docRef, category)
                    Log.d("OnboardingViewModel", "Prepared category: ${category["name"]}")
                }

                batch.commit().addOnSuccessListener {
                    Log.d("OnboardingViewModel", "Categories saved successfully to Firestore")
                }.addOnFailureListener { e ->
                    Log.e("OnboardingViewModel", "Error saving categories: ${e.message}")
                }

            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Exception: ${e.message}")
            }
        }
    }

    private fun mapCategoryResources(name: String): Pair<Int, Int> {
        return when (name) {
            "House" -> Pair(R.drawable.ic_house, R.color.rose)
            "Food" -> Pair(R.drawable.ic_food, R.color.blue)
            "Transport" -> Pair(R.drawable.ic_transport, R.color.purple)
            "Health" -> Pair(R.drawable.ic_health, R.color.orange)
            "Loans" -> Pair(R.drawable.ic_loans, R.color.tangerine)
            "Entertainment" -> Pair(R.drawable.ic_entertainment, R.color.pink)
            "Family" -> Pair(R.drawable.ic_family, R.color.yellow)
            "Custom" -> Pair(R.drawable.ic_custom, R.color.red)
            "Savings" -> Pair(R.drawable.ic_savings, R.color.teal_200)
            "Salary" -> Pair(R.drawable.ic_salary, R.color.primary)
            else -> Pair(R.drawable.ic_default, R.color.black)
        }
    }
}

//Reference List:
//Android Developers. 2025. Add an Image composition. [online]. Available at: https://developer.android.com/codelabs/basic-android-kotlin-compose-add-images#2 [Accessed on 9 April 2025]
//Code With Cal. 2025. Color Picker Android Studio Kotlin Custom Spinner Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=YsKjl8ZbM4g [Accessed on 9 April 2025]
//Code With Cal. 2025. Room Database Android Studio Kotlin Example Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=-LNg-K7SncM [Accessed on 12 April 2025]
//Programming w/ Professor Sluiter. 2023. Learn Kotlin 08 how to use the if conditional statement. [online]. Available at: https://www.youtube.com/watch?v=usFfxlnTPHc [Accessed on 13 April 2025]
//GeeksforGeeks. 2025. Android UI Layouts. [online]. Available at: https://www.geeksforgeeks.org/android-ui-layouts/ [Accessed on 10 April 2025]
//Muhammadumarch. 2023. Implementing Navigation in Your Android App with Android Navigation Component. [online]. Available at: https://medium.com/@muhammadumarch321/implementing-navigation-in-your-android-app-with-android-navigation-component-ff22a3d300a [Accessed on 11 April 2025]
//Android Developers. 2025. Fragment lifecycle. [online]. Available at: https://developer.android.com/guide/fragments/lifecycle [Accessed on 12 April 2025]
//Android Knowledge. 2024. ViewModel in Android Studio using Kotlin | Android Knowledge. [video online]. Available at: https://www.youtube.com/watch?v=v32hSKtlH9A [Accessed on 11 April 2025]
//Code With Cal. 2025. Room Database Android Studio Kotlin Example Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=-LNg-K7SncM [Accessed on 12 April 2025]
//Android Developers. 2025. Accessing data using Room DAOs. [online]. Available at: https://developer.android.com/training/data-storage/room/accessing-data [Accessed on 15 April 2025]

