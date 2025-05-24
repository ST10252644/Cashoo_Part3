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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.databinding.FragmentRewardsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize adapters
        bronClmAdapter = ClaimsAdapter(emptyList())
        silClmAdapter = ClaimsAdapter(emptyList())
        gldClmAdapter = ClaimsAdapter(emptyList())

        // RecyclerViews setup
        binding.recyclerClmBronze.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerClmSilver.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerClmGold.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Assign adapters
        binding.recyclerClmBronze.adapter = bronClmAdapter
        binding.recyclerClmSilver.adapter = silClmAdapter
        binding.recyclerClmGold.adapter = gldClmAdapter

        // Load rewards
        loadRewardsFromFirestore()

        // 🔑 Get current user info from FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val uid = user.uid
            val email = user.email

            if (email != null) {
                loadUserData(uid, email)
            } else {
                Log.e("RewardsFragment", "Email not found for user.")
            }

            // ✅ Set OnClickListener for Claim button
            binding.ItemClaim.setOnClickListener {
                claimRewards(uid)
            }
        } ?: run {
            Log.e("RewardsFragment", "User not logged in.")
        }

        return root
    }

    private fun getImageResourceByName(name: String): Int {
        return resources.getIdentifier(name, "drawable", requireContext().packageName)
    }

    private fun loadRewardsFromFirestore() {
        firestore.collection("rewards")
            .get()
            .addOnSuccessListener { result ->
                val bronzeList = mutableListOf<ClaimItem>()
                val silverList = mutableListOf<ClaimItem>()
                val goldList = mutableListOf<ClaimItem>()

                for (document in result) {
                    val reward = document.toObject(RewardItem::class.java)
                    val imageResId = getImageResourceByName(reward.imageUrl)

                    val item = ClaimItem(
                        reward.name,
                        "${reward.amount} pts",
                        imageResId
                    )

                    when (reward.type.lowercase()) {
                        "bronze" -> bronzeList.add(item)
                        "silver" -> silverList.add(item)
                        "gold" -> goldList.add(item)
                    }
                }

                // Update adapters with loaded data
                bronClmAdapter.updateList(bronzeList)
                silClmAdapter.updateList(silverList)
                gldClmAdapter.updateList(goldList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load rewards: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error loading rewards", e)
            }
    }

    private fun loadUserData(uid: String, email: String) {
        lifecycleScope.launchWhenStarted {
            try {
                Log.d("RewardsFragment", "Starting loadUserData with UID: $uid and Email: $email")

                val userDoc = firestore.collection("users").document(uid).get().await()
                if (!userDoc.exists()) {
                    Log.e("RewardsFragment", "User document not found for UID: $uid")
                    return@launchWhenStarted
                }
                Log.d("RewardsFragment", "User document found for UID: $uid")

                val cashoos = userDoc.getDouble("cashoos") ?: 0.0
                binding.txtPoints2.text = "C ${String.format("%.2f", cashoos)}"

                val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
                val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
                val startDate = "$currentYear-$currentMonth-01"
                val endDate = "$currentYear-$currentMonth-31"

                val settingsDoc = firestore.collection("user_settings").document(uid).get().await()
                if (!settingsDoc.exists()) {
                    Log.e("RewardsFragment", "User settings not found for UID: $uid")
                    return@launchWhenStarted
                }
                val minGoal = settingsDoc.getDouble("minGoal") ?: 0.0
                val maxGoal = settingsDoc.getDouble("maxGoal") ?: 0.0
                Log.d("RewardsFragment", "minGoal: $minGoal, maxGoal: $maxGoal")

                // Fetch all categories and find categoryIds for "Savings" and "Expense"
                val categoriesSnapshot = firestore.collection("users").document(uid)
                    .collection("categories")
                    .get()
                    .await()

                val categoryMap = categoriesSnapshot.documents.associateBy(
                    { it.getString("name")?.lowercase() ?: "" },
                    { it.id }
                )

                val savingsCategoryId = categoryMap["savings"]

                // Check for null category IDs
                if (savingsCategoryId == null) {
                    Log.e("RewardsFragment", "Savings or Expense category ID not found.")
                    return@launchWhenStarted
                }

                // Fetch savings
                val savingsSnapshot = firestore.collection("users").document(uid)
                    .collection("transactions")
                    .whereEqualTo("categoryId", savingsCategoryId)
                    .whereGreaterThanOrEqualTo("dateTime", startDate)
                    .whereLessThanOrEqualTo("dateTime", endDate)
                    .get()
                    .await()


                val totalSaved = savingsSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }
                val minPercent = if (minGoal > 0) (totalSaved / minGoal * 100).coerceAtMost(100.0).toInt() else 0
                binding.minGoalPercentage.text = "$minPercent%"
                Log.d("RewardsFragment", "Total saved: $totalSaved")
                Log.d("RewardsFragment", "Min goal percentage: $minPercent%")

                // Fetch expenses
                val expenseSnapshot = firestore.collection("users").document(uid)
                    .collection("transactions")
                    .whereEqualTo("expense", true)
                    .whereGreaterThanOrEqualTo("dateTime", startDate)
                    .whereLessThanOrEqualTo("dateTime", endDate)
                    .get()
                    .await()


                val totalExpenses = expenseSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }
                val expensePercent = if (maxGoal > 0) (100 - (totalExpenses / maxGoal * 100)).coerceIn(0.0, 100.0).toInt() else 0
                binding.maxGoalPercentage.text = "$expensePercent%"
                Log.d("RewardsFragment", "Total expenses: $totalExpenses")
                Log.d("RewardsFragment", "Max goal percentage: $expensePercent%")


            } catch (e: Exception) {
                Log.e("RewardsFragment", "Error loading user data: ${e.message}", e)
            }
        }
    }





    private fun claimRewards(uid: String) {
        lifecycleScope.launchWhenStarted {
            try {
                val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
                val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
                val startDate = "$currentYear-$currentMonth-01"
                val endDate = "$currentYear-$currentMonth-31"

                // 1. Get user document and settings
                val userDocRef = firestore.collection("users").document(uid)
                val userDoc = userDocRef.get().await()
                if (!userDoc.exists()) {
                    Toast.makeText(requireContext(), "User not found.", Toast.LENGTH_SHORT).show()
                    return@launchWhenStarted
                }
                val currentCashoos = userDoc.getDouble("cashoos") ?: 0.0
                val lastClaimed = userDoc.getString("lastClaimedMonth") ?: ""

                // Prevent multiple claims
                val currentClaimKey = "$currentYear-$currentMonth"
                if (lastClaimed == currentClaimKey) {
                    Toast.makeText(requireContext(), "You've already claimed your rewards this month.", Toast.LENGTH_LONG).show()
                    return@launchWhenStarted
                }

                val userSettingsDoc = firestore.collection("user_settings").document(uid).get().await()
                if (!userSettingsDoc.exists()) {
                    Toast.makeText(requireContext(), "User settings not found.", Toast.LENGTH_SHORT).show()
                    return@launchWhenStarted
                }
                val minGoal = userSettingsDoc.getDouble("minGoal") ?: 0.0
                val maxGoal = userSettingsDoc.getDouble("maxGoal") ?: 0.0

                // 2. Get category IDs
                val categoriesSnapshot = firestore.collection("users").document(uid)
                    .collection("categories")
                    .get()
                    .await()

                val categoryMap = categoriesSnapshot.documents.associateBy(
                    { it.getString("name")?.lowercase() ?: "" },
                    { it.id }
                )

                val savingsCategoryId = categoryMap["savings"]
                if (savingsCategoryId == null) {
                    Toast.makeText(requireContext(), "Savings category not found.", Toast.LENGTH_SHORT).show()
                    return@launchWhenStarted
                }

                // 3. Calculate min goal percentage
                val savingsSnapshot = firestore.collection("users").document(uid)
                    .collection("transactions")
                    .whereEqualTo("categoryId", savingsCategoryId)
                    .whereGreaterThanOrEqualTo("dateTime", startDate)
                    .whereLessThanOrEqualTo("dateTime", endDate)
                    .get()
                    .await()

                val totalSaved = savingsSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }
                val minPercent = if (minGoal > 0) (totalSaved / minGoal * 100).coerceAtMost(100.0).toInt() else 0

                // 4. Calculate expense percentage
                val expenseSnapshot = firestore.collection("users").document(uid)
                    .collection("transactions")
                    .whereEqualTo("expense", true)
                    .whereGreaterThanOrEqualTo("dateTime", startDate)
                    .whereLessThanOrEqualTo("dateTime", endDate)
                    .get()
                    .await()

                val totalExpenses = expenseSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }
                val maxPercent = if (maxGoal > 0) (100 - (totalExpenses / maxGoal * 100)).coerceIn(0.0, 100.0).toInt() else 0

                // 5. Decide rewards
                val minGoalMet = minPercent >= 100
                val maxGoalMet = maxPercent > 0
                var reward = 0.0
                var message = "No cashoos to claim. Come back at the end of the month."

                when {
                    minGoalMet && maxGoalMet -> {
                        reward = 20.0
                        message = "You earned 20 cashoos for meeting both your savings and spending goals!"
                    }
                    minGoalMet -> {
                        reward = 10.0
                        message = "You earned 10 cashoos for meeting your savings goal!"
                    }
                    maxGoalMet -> {
                        reward = 5.0
                        message = "You earned 5 cashoos for staying within your spending goal!"
                    }
                }

                if (reward > 0.0) {
                    val updatedCashoos = currentCashoos + reward
                    userDocRef.update(
                        mapOf(
                            "cashoos" to updatedCashoos,
                            "lastClaimedMonth" to currentClaimKey
                        )
                    ).await()
                    binding.txtPoints2.text = "C ${String.format("%.2f", updatedCashoos)}"
                }

                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

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
