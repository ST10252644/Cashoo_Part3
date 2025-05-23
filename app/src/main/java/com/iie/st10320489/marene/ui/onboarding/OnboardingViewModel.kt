package com.iie.st10320489.marene.ui.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FirebaseCategory(
    val userId: Int = 0,
    val name: String = "",
    val icon: Int = 0,
    val colour: Int = 0
)

class OnboardingViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    fun saveSelectedCategories(userId: Int, selectedCategoryNames: List<String>) {
        println("Saving categories to Firebase for userId: $userId")

        viewModelScope.launch(Dispatchers.IO) {
            val categories = selectedCategoryNames.map { categoryName ->
                val (iconRes, colorRes) = mapCategoryResources(categoryName)
                FirebaseCategory(
                    userId = userId,
                    name = categoryName,
                    icon = iconRes,
                    colour = colorRes
                )
            }.toMutableList()

            // Add default "Other" category if not already selected
            if (!selectedCategoryNames.contains("Other")) {
                categories.add(
                    FirebaseCategory(
                        userId = userId,
                        name = "Other",
                        icon = R.drawable.ic_custom,
                        colour = R.color.red
                    )
                )
            }

            // Save to Firestore under collection: users/{userId}/categories/
            categories.forEach { category ->
                try {
                    firestore
                        .collection("users")
                        .document(userId.toString())
                        .collection("categories")
                        .document(category.name) // Use category name as document ID
                        .set(category)
                        .await()
                    Log.d("OnboardingViewModel", "Saved category: ${category.name}")
                } catch (e: Exception) {
                    Log.e("OnboardingViewModel", "Error saving category: ${category.name}", e)
                }
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
