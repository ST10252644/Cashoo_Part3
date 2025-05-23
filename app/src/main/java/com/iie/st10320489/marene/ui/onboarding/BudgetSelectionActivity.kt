package com.iie.st10320489.marene.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.databinding.ActivityBudgetSelectionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BudgetSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetSelectionBinding
    private val selectedCategories = mutableListOf<String>()
    private val categories = listOf(
        "House", "Food", "Transport", "Health", "Loans",
        "Entertainment", "Family", "Savings", "Salary"
    )

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCategoryViews()
        setupButtons()
    }

    private fun setupCategoryViews() {
        categories.forEach { category ->
            val categoryView = TextView(this).apply {
                text = category
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setPadding(32, 32, 32, 32)
                background = ContextCompat.getDrawable(context, R.drawable.category_unselected)

                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.category_width),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }

                setOnClickListener {
                    if (selectedCategories.contains(category)) {
                        selectedCategories.remove(category)
                        background = ContextCompat.getDrawable(context, R.drawable.category_unselected)
                    } else {
                        selectedCategories.add(category)
                        background = ContextCompat.getDrawable(context, R.drawable.category_selected)
                    }
                }
            }
            binding.categoriesContainer.addView(categoryView)
        }
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.nextButton.setOnClickListener {
            if (selectedCategories.isNotEmpty()) {
                saveUserSelectedCategories()
            } else {
                Toast.makeText(this, "Please select at least one category", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentUserEmail(): String {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val email = sharedPreferences.getString("currentUserEmail", "") ?: ""
        Log.d("BudgetSelectionActivity", "Retrieved email: $email")
        return email
    }

    private fun saveUserSelectedCategories() {
        val currentUserEmail = getCurrentUserEmail()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Query the user document by email
                val userSnapshot = firestore.collection("users")
                    .whereEqualTo("email", currentUserEmail)
                    .get()
                    .await()

                if (!userSnapshot.isEmpty) {
                    val userDoc = userSnapshot.documents[0]
                    val userId = userDoc.id

                    // Save selected categories to Firestore under the user document
                    val categoryData = hashMapOf("selectedCategories" to selectedCategories)
                    firestore.collection("users")
                        .document(userId)
                        .collection("preferences")
                        .document("budgetCategories")
                        .set(categoryData)
                        .await()

                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@BudgetSelectionActivity, SavingsGoalActivity::class.java)
                        startActivity(intent)
                    }

                } else {
                    Log.e("Firestore", "User not found")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@BudgetSelectionActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("FirestoreError", "Failed to save categories: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BudgetSelectionActivity, "Failed to save categories", Toast.LENGTH_SHORT).show()
                }
            }
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