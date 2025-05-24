package com.iie.st10320489.marene.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.databinding.ActivityBudgetSelectionBinding

class BudgetSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetSelectionBinding
    private lateinit var onboardingViewModel: OnboardingViewModel
    private val selectedCategories = mutableListOf<String>()
    private val categories = listOf(
        "House", "Food", "Transport", "Health", "Loans",
        "Entertainment", "Family", "Savings", "Salary"
    )

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        onboardingViewModel = ViewModelProvider(this, OnboardingViewModelFactory())[OnboardingViewModel::class.java]
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

    private fun saveUserSelectedCategories() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val userDocRef = firestore.collection("users").document(userId)

        val data = mapOf(
            "selectedCategories" to selectedCategories
        )

        userDocRef.set(data)
            .addOnSuccessListener {
                Log.d("BudgetSelectionActivity", "Categories saved successfully")

                // ✅ NOW CALL THE VIEWMODEL TO SAVE TO CATEGORIES COLLECTION
                onboardingViewModel.saveSelectedCategories(userId, selectedCategories)

                val intent = Intent(this@BudgetSelectionActivity, SavingsGoalActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Log.e("BudgetSelectionActivity", "Error saving categories", e)
                Toast.makeText(this, "Failed to save categories", Toast.LENGTH_SHORT).show()
            }
    }

}
