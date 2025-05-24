package com.iie.st10320489.marene.ui.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class FirebaseCategory(
    val categoryId: String = "",
    val userId: String = "",
    val name: String = "",
    val icon: Int = 0,
    val colour: Int = 0
)


class OnboardingViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    fun saveSelectedCategories(userUid: String, selectedCategoryNames: List<String>) {
        println("Saving categories to Firebase for userUid: $userUid")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Map selected names to FirebaseCategory items, generating a unique categoryId for each.
                val categories = selectedCategoryNames.map { categoryName ->
                    val (iconRes, colorRes) = mapCategoryResources(categoryName)
                    // Generate a unique ID for the category.
                    val categoryId = UUID.randomUUID().toString()
                    FirebaseCategory(
                        categoryId = categoryId,
                        userId = userUid,
                        name = categoryName,
                        icon = iconRes,
                        colour = colorRes
                    )
                }.toMutableList()

                // Ensure "Other" is always added if not selected.
                if (!selectedCategoryNames.contains("Other")) {
                    val categoryId = UUID.randomUUID().toString()
                    categories.add(
                        FirebaseCategory(
                            categoryId = categoryId,
                            userId = userUid,
                            name = "Other",
                            icon = R.drawable.ic_custom,
                            colour = R.color.red
                        )
                    )
                }

                // Save each category under the user's subcollection using categoryId as document ID.
                categories.forEach { category ->
                    try {
                        firestore
                            .collection("users")
                            .document(userUid)
                            .collection("categories")
                            .document(category.categoryId)  // using unique categoryId as the document ID
                            .set(category)
                            .await()

                        Log.d("OnboardingViewModel", "Saved category: ${category.name} with ID: ${category.categoryId}")
                    } catch (e: FirebaseFirestoreException) {
                        Log.e("OnboardingViewModel", "Firestore error for category: ${category.name} - ${e.message}", e)
                    } catch (e: Exception) {
                        Log.e("OnboardingViewModel", "Unexpected error saving category: ${category.name} - ${e.message}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Fatal error while preparing categories: ${e.message}", e)
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
            "Savings" -> Pair(R.drawable.ic_savings, R.color.teal_200)
            "Salary" -> Pair(R.drawable.ic_salary, R.color.primary)
            else -> Pair(R.drawable.ic_default, R.color.black)
        }
    }
}


