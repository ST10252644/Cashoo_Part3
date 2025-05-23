package com.iie.st10320489.marene.ui.rewards

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.databinding.FragmentRewardsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RewardsFragment : Fragment() {

    private var _binding: FragmentRewardsBinding? = null
    private val binding get() = _binding!!

    private lateinit var bronClmAdapter: ClaimsAdapter
    private lateinit var silClmAdapter: ClaimsAdapter
    private lateinit var gldClmAdapter: ClaimsAdapter

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        val root = binding.root

        // Setup RecyclerViews (same as before)
        binding.recyclerClmBronze.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerClmSilver.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerClmGold.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Sample Rewards (unchanged)
        val bronzeClaims = listOf(
            ClaimItem("Croissant", "5 pts", R.drawable.croissants),
            ClaimItem("Cappuccino", "8 pts", R.drawable.capuccino_jpg),
            ClaimItem("Choco Cookie", "10 pts", R.drawable.cookie),
            ClaimItem("Frappe", "12 pts", R.drawable.frappe)
        )
        val silverClaims = listOf(
            ClaimItem("Sandwich", "15 pts", R.drawable.grilled),
            ClaimItem("Graduation", "18 pts", R.drawable.kanye),
            ClaimItem("Journal", "20 pts", R.drawable.journal),
            ClaimItem("Energy Pack", "22 pts", R.drawable.energy)
        )
        val goldClaims = listOf(
            ClaimItem("Shoe Cleaning", "25 pts", R.drawable.clean),
            ClaimItem("Cashoo Bag", "40 pts", R.drawable.kanbag),
            ClaimItem("Two Burritos", "35 pts", R.drawable.burrito),
            ClaimItem("Jordan 4s", "50 pts", R.drawable.jordan4)
        )

        bronClmAdapter = ClaimsAdapter(bronzeClaims)
        silClmAdapter = ClaimsAdapter(silverClaims)
        gldClmAdapter = ClaimsAdapter(goldClaims)

        binding.recyclerClmBronze.adapter = bronClmAdapter
        binding.recyclerClmSilver.adapter = silClmAdapter
        binding.recyclerClmGold.adapter = gldClmAdapter

        val sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val currentUserEmail = sharedPreferences.getString("currentUserEmail", null)

        binding.ItemClaim.setOnClickListener {
            if (currentUserEmail == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            claimRewards(currentUserEmail)
        }

        binding.discPage.setOnClickListener {
            findNavController().navigate(R.id.navigation_rewards_discount)
        }

        // Load user points and goal percentages from Firestore
        if (currentUserEmail != null) {
            loadUserData(currentUserEmail)
        }

        return root
    }

    private fun loadUserData(email: String) {
        lifecycleScope.launchWhenStarted {
            try {
                // Fetch user document by email (assuming email used as doc ID or stored field)
                val userDoc = firestore.collection("users").document(email).get().await()

                if (!userDoc.exists()) {
                    Log.e("RewardsFragment", "User document not found for email $email")
                    return@launchWhenStarted
                }

                val cashoos = userDoc.getDouble("cashoos") ?: 0.0
                val formattedCash = "C ${String.format("%.2f", cashoos)}"
                binding.txtPoints2.text = formattedCash

                // Get current month and year strings
                val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
                val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

                // Fetch user settings
                val userSettingsDoc = firestore.collection("userSettings").document(email).get().await()
                val minGoal = userSettingsDoc.getDouble("minGoal") ?: 0.0
                val maxGoal = userSettingsDoc.getDouble("maxGoal") ?: 0.0

                // Query transactions of type 'savings' for current month and year
                val savingsSnapshot = firestore.collection("transactions")
                    .whereEqualTo("userEmail", email)
                    .whereEqualTo("type", "savings")
                    .whereGreaterThanOrEqualTo("date", "$currentYear-$currentMonth-01")
                    .whereLessThanOrEqualTo("date", "$currentYear-$currentMonth-31")
                    .get()
                    .await()

                val totalSaved = savingsSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }
                val minPercent = if (minGoal > 0) (totalSaved / minGoal * 100).coerceAtMost(100.0).toInt() else 0
                binding.minGoalPercentage.text = "$minPercent%"

                // Query transactions of type 'expense' for current month and year
                val expensesSnapshot = firestore.collection("transactions")
                    .whereEqualTo("userEmail", email)
                    .whereEqualTo("type", "expense")
                    .whereGreaterThanOrEqualTo("date", "$currentYear-$currentMonth-01")
                    .whereLessThanOrEqualTo("date", "$currentYear-$currentMonth-31")
                    .get()
                    .await()

                val totalExpenses = expensesSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }
                val expensePercent = if (maxGoal > 0) (totalExpenses / maxGoal * 100).coerceAtMost(100.0).toInt() else 0
                binding.maxGoalPercentage.text = "$expensePercent%"

            } catch (e: Exception) {
                Log.e("RewardsFragment", "Error loading user data: ${e.message}", e)
            }
        }
    }

    private fun claimRewards(email: String) {
        lifecycleScope.launchWhenStarted {
            try {
                val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
                val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

                val userDocRef = firestore.collection("users").document(email)
                val userSettingsDocRef = firestore.collection("userSettings").document(email)

                val userDoc = userDocRef.get().await()
                if (!userDoc.exists()) {
                    Toast.makeText(requireContext(), "User not found.", Toast.LENGTH_SHORT).show()
                    return@launchWhenStarted
                }

                val cashoos = userDoc.getDouble("cashoos") ?: 0.0

                val userSettingsDoc = userSettingsDocRef.get().await()
                val minGoal = userSettingsDoc.getDouble("minGoal") ?: 0.0
                val maxGoal = userSettingsDoc.getDouble("maxGoal") ?: 0.0

                val savingsSnapshot = firestore.collection("transactions")
                    .whereEqualTo("userEmail", email)
                    .whereEqualTo("type", "savings")
                    .whereGreaterThanOrEqualTo("date", "$currentYear-$currentMonth-01")
                    .whereLessThanOrEqualTo("date", "$currentYear-$currentMonth-31")
                    .get()
                    .await()
                val totalSaved = savingsSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }
                val minPercent = if (minGoal > 0) (totalSaved / minGoal * 100).coerceAtMost(100.0).toInt() else 0

                val expensesSnapshot = firestore.collection("transactions")
                    .whereEqualTo("userEmail", email)
                    .whereEqualTo("type", "expense")
                    .whereGreaterThanOrEqualTo("date", "$currentYear-$currentMonth-01")
                    .whereLessThanOrEqualTo("date", "$currentYear-$currentMonth-31")
                    .get()
                    .await()
                val totalExpenses = expensesSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }
                val expensePercent = if (maxGoal > 0) (totalExpenses / maxGoal * 100).coerceAtMost(100.0).toInt() else 0

                if (minPercent >= 100 && expensePercent <= 50) {
                    // User meets criteria - add rewards points (example 10 points)
                    val newCashoos = cashoos + 10.0
                    userDocRef.update("cashoos", newCashoos).await()

                    Toast.makeText(requireContext(), "Claim Successful! 10 points added.", Toast.LENGTH_LONG).show()

                    binding.txtPoints2.text = "C ${String.format("%.2f", newCashoos)}"
                } else {
                    Toast.makeText(requireContext(), "Claim Unsuccessful! Meet your savings and expense goals first.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("RewardsFragment", "Error claiming rewards: ${e.message}", e)
                Toast.makeText(requireContext(), "An error occurred while claiming rewards.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
