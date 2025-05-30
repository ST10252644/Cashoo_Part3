package com.iie.st10320489.marene.ui.rewards

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.iie.st10320489.marene.data.entities.Reward
import com.iie.st10320489.marene.databinding.FragmentRewardsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RewardsFragment : Fragment() {

    // View binding for fragment layout
    private var _binding: FragmentRewardsBinding? = null
    private val binding get() = _binding!!

    // Adapters for each reward type
    private lateinit var bronClmAdapter: ClaimsAdapter
    private lateinit var silClmAdapter: ClaimsAdapter
    private lateinit var gldClmAdapter: ClaimsAdapter

    // Firestore instance
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using view binding
        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        val root: View = binding.root // (Viegen, 2022)

        // Initialize reward adapters with empty lists
        bronClmAdapter = ClaimsAdapter(emptyList())
        silClmAdapter = ClaimsAdapter(emptyList())
        gldClmAdapter = ClaimsAdapter(emptyList()) // (Viegen, 2022)

        // Setup horizontal RecyclerViews for each reward type
        binding.recyclerClmBronze.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerClmSilver.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerClmGold.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Assign the adapters to the RecyclerViews
        binding.recyclerClmBronze.adapter = bronClmAdapter
        binding.recyclerClmSilver.adapter = silClmAdapter
        binding.recyclerClmGold.adapter = gldClmAdapter

        // Load rewards from Firestore and display in RecyclerViews
        loadRewardsFromFirestore()

        // Get currently logged-in user from FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val uid = user.uid
            val email = user.email

            // If email is available, load the user's reward data
            if (email != null) {
                loadUserData(uid, email)
            } else {
                Log.e("RewardsFragment", "Email not found for user.")
            }

            // Set up click listener for the "Claim" button
            binding.ItemClaim.setOnClickListener {
                claimRewards(uid)
            }
        } ?: run {
            // Log error if no user is logged in
            Log.e("RewardsFragment", "User not logged in.")
        }

        // Navigate to the rewards history screen when button is clicked
        binding.discPage.setOnClickListener {
            findNavController().navigate(R.id.navigation_rewards_history)
        }

        return root
    }

    // Helper function to get image resource ID by name
    private fun getImageResourceByName(name: String): Int {
        return resources.getIdentifier(name, "drawable", requireContext().packageName)
    }

    // Loads reward data from Firestore and categorizes them by type
    private fun loadRewardsFromFirestore() {
        firestore.collection("rewards")
            .get()
            .addOnSuccessListener { result ->
                // Create temporary lists for each reward category
                val bronzeList = mutableListOf<ClaimItem>()
                val silverList = mutableListOf<ClaimItem>()
                val goldList = mutableListOf<ClaimItem>()

                // Parse each reward document
                for (document in result) {
                    val reward = document.toObject(RewardItem::class.java)
                    val imageResId = getImageResourceByName(reward.imageUrl)

                    val item = ClaimItem(
                        reward.name,
                        reward.amount,
                        imageResId,
                        reward.location ?: "RoseBank Mall" // default location
                    ) // (Viegen, 2022)

                    // Add reward to appropriate list based on type
                    when (reward.type.lowercase()) {
                        "bronze" -> bronzeList.add(item)
                        "silver" -> silverList.add(item)
                        "gold" -> goldList.add(item)
                    }
                }

                // Update adapters with new reward data
                bronClmAdapter.updateList(bronzeList)
                silClmAdapter.updateList(silverList)
                gldClmAdapter.updateList(goldList)
            }
            .addOnFailureListener { e ->
                // Show error if loading fails
                Toast.makeText(
                    requireContext(),
                    "Failed to load rewards: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("Firestore", "Error loading rewards", e)
            }
    }

    // Loads user data and reward-related progress from Firestore
    private fun loadUserData(uid: String, email: String) {
        lifecycleScope.launchWhenStarted {
            try {
                Log.d("RewardsFragment", "Starting loadUserData with UID: $uid and Email: $email")

                // Get main user document
                val userDoc = firestore.collection("users").document(uid).get().await()
                if (!userDoc.exists()) {
                    Log.e("RewardsFragment", "User document not found for UID: $uid")
                    return@launchWhenStarted
                }

                // Display user's Cashoos balance
                val cashoos = userDoc.getDouble("cashoos") ?: 0.0
                binding.txtPoints2.text = "C ${String.format("%.2f", cashoos)}"

                // Determine current month and year
                val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
                val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
                val startDate = "$currentYear-$currentMonth-01"
                val endDate = "$currentYear-$currentMonth-31"

                // Fetch user's goal settings
                val settingsDoc = firestore.collection("user_settings").document(uid).get().await()
                if (!settingsDoc.exists()) {
                    Log.e("RewardsFragment", "User settings not found for UID: $uid")
                    return@launchWhenStarted
                } // (Viegen, 2022)
                val minGoal = settingsDoc.getDouble("minGoal") ?: 0.0
                val maxGoal = settingsDoc.getDouble("maxGoal") ?: 0.0

                // Get category IDs by name
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
                    Log.e("RewardsFragment", "Savings or Expense category ID not found.")
                    return@launchWhenStarted
                }

                // Get savings transactions for this month
                val savingsSnapshot = firestore.collection("users").document(uid)
                    .collection("transactions")
                    .whereEqualTo("categoryId", savingsCategoryId)
                    .whereGreaterThanOrEqualTo("dateTime", startDate)
                    .whereLessThanOrEqualTo("dateTime", endDate)
                    .get()
                    .await()
                val totalSaved = savingsSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }

                // Calculate and show percentage toward min goal
                val minPercent =
                    if (minGoal > 0) (totalSaved / minGoal * 100).coerceAtMost(100.0).toInt() else 0
                binding.minGoalPercentage.text = "$minPercent%"

                // Get expenses for this month
                val expenseSnapshot = firestore.collection("users").document(uid)
                    .collection("transactions")
                    .whereEqualTo("expense", true)
                    .whereGreaterThanOrEqualTo("dateTime", startDate)
                    .whereLessThanOrEqualTo("dateTime", endDate)
                    .get()
                    .await()
                val totalExpenses =
                    expenseSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }

                // Calculate and show remaining percentage for max goal
                val expensePercent =
                    if (maxGoal > 0) (100 - (totalExpenses / maxGoal * 100)).coerceIn(0.0, 100.0).toInt() else 0
                binding.maxGoalPercentage.text = "$expensePercent%"

            } catch (e: Exception) {
                Log.e("RewardsFragment", "Error loading user data: ${e.message}", e)
            }
        }
    }


    private fun claimRewards(uid: String) {
        lifecycleScope.launchWhenStarted {
            try {
                // Get current month and year in format MM and yyyy
                val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
                val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
                val startDate = "$currentYear-$currentMonth-01"
                val endDate = "$currentYear-$currentMonth-31"

                // 1. Fetch user document from Firestore
                val userDocRef = firestore.collection("users").document(uid)
                val userDoc = userDocRef.get().await()
                if (!userDoc.exists()) {
                    Toast.makeText(requireContext(), "User not found.", Toast.LENGTH_SHORT).show()
                    return@launchWhenStarted
                } // (Viegen, 2022)

                // Extract current cashoos and last claimed month
                val currentCashoos = userDoc.getDouble("cashoos") ?: 0.0
                val lastClaimed = userDoc.getString("lastClaimedMonth") ?: ""

                // Prevent double claim within same month
                val currentClaimKey = "$currentYear-$currentMonth"
                if (lastClaimed == currentClaimKey) {
                    Toast.makeText(
                        requireContext(),
                        "You've already claimed your rewards this month.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launchWhenStarted
                }

                // 2. Fetch user settings to get min/max goals
                val userSettingsDoc =
                    firestore.collection("user_settings").document(uid).get().await()
                if (!userSettingsDoc.exists()) {
                    Toast.makeText(requireContext(), "User settings not found.", Toast.LENGTH_SHORT)
                        .show()
                    return@launchWhenStarted
                } // (Viegen, 2022)

                val minGoal = userSettingsDoc.getDouble("minGoal") ?: 0.0
                val maxGoal = userSettingsDoc.getDouble("maxGoal") ?: 0.0

                // 3. Get all category IDs associated with the user
                val categoriesSnapshot = firestore.collection("users").document(uid)
                    .collection("categories")
                    .get()
                    .await()

                // Create a map of lowercase category name to category ID
                val categoryMap = categoriesSnapshot.documents.associateBy(
                    { it.getString("name")?.lowercase() ?: "" },
                    { it.id }
                )

                val savingsCategoryId = categoryMap["savings"]
                if (savingsCategoryId == null) {
                    Toast.makeText(
                        requireContext(),
                        "Savings category not found.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launchWhenStarted
                } // (Viegen, 2022)

                // 4. Calculate how much was saved this month
                val savingsSnapshot = firestore.collection("users").document(uid)
                    .collection("transactions")
                    .whereEqualTo("categoryId", savingsCategoryId)
                    .whereGreaterThanOrEqualTo("dateTime", startDate)
                    .whereLessThanOrEqualTo("dateTime", endDate)
                    .get()
                    .await()

                val totalSaved = savingsSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }

                // Compute percentage of savings goal met
                val minPercent =
                    if (minGoal > 0) (totalSaved / minGoal * 100).coerceAtMost(100.0).toInt() else 0

                // 5. Calculate how much was spent this month
                val expenseSnapshot = firestore.collection("users").document(uid)
                    .collection("transactions")
                    .whereEqualTo("expense", true)
                    .whereGreaterThanOrEqualTo("dateTime", startDate)
                    .whereLessThanOrEqualTo("dateTime", endDate)
                    .get()
                    .await()

                val totalExpenses =
                    expenseSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }

                // Compute remaining percentage of spending goal
                val maxPercent =
                    if (maxGoal > 0) (100 - (totalExpenses / maxGoal * 100)).coerceIn(0.0, 100.0)
                        .toInt() else 0

                // 6. Decide reward based on goals met
                val minGoalMet = minPercent >= 100
                val maxGoalMet = maxPercent > 0
                var reward = 0.0
                var message = "No cashoos to claim. Come back at the end of the month."

                when {
                    minGoalMet && maxGoalMet -> {
                        reward = 20.0
                        message =
                            "You earned 20 cashoos for meeting both your savings and spending goals!"
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

                // 7. Update user's cashoos and last claimed month if reward is given
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
                Toast.makeText(
                    requireContext(),
                    "An error occurred while claiming rewards.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    } // (Viegen, 2022)

    companion object {
        fun saveClaimedRewardToFirestore( // Function to save claimed reward and reward history to Firestore
            context: Context,
            rewardId: String,
            reward: Reward,
            historyItem: RewardHistoryItem,
            onComplete: (Boolean) -> Unit
        ) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val db = FirebaseFirestore.getInstance()
            val rewardDocRef = db.collection("rewards").document(rewardId)

            val rewardData = mapOf(   // Prepare reward metadata
                "rewardId" to reward.rewardId,
                "name" to reward.name,
                "description" to reward.description,
                "amount" to reward.amount,
                "type" to reward.type,
                "code" to reward.code
            )

            val historyData = mapOf(   // Prepare reward history metadata
                "rewardId" to rewardId,
                "userId" to uid,
                "title" to historyItem.title,
                "imageResId" to historyItem.imageResId,
                "location" to historyItem.location,
                "dateClaimed" to historyItem.dateClaimed,
                "expiryTimestamp" to historyItem.expiryTimestamp,
                "status" to historyItem.status
            )

            rewardDocRef.set(rewardData)  // Save main reward data
                .addOnSuccessListener {
                    rewardDocRef.collection(uid).document("history")  // On success, save user's specific reward history under subcollection
                        .set(historyData)
                        .addOnSuccessListener { onComplete(true) }
                        .addOnFailureListener {
                            Log.e("Firestore", "Failed to write reward history", it)
                            onComplete(false)
                        }
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Failed to write reward", it)
                    onComplete(false)
                }
        } // (Viegen, 2022)
    }


            override fun onDestroyView() { // Called when the fragment view is destroyed to avoid memory leaks
        super.onDestroyView()
        _binding = null
    }
}

//Reference List

//Raikwar, A., 2023. Ge=ng Started with Room Database in Android. [Online]
//Available at: hRps://developer.android.com/develop#core-areas
//[Accessed 28 April 2025].
//Cal, C. W., 2023. Room Database Android Studio Kotlin Example Tutorial. [Online] Available at: hRps://youtu.be/-LNg-K7SncM?si=y8cbMdvhhp48Pp9-
//[Accessed 27 April 2025].
//College, I. V., 2025. PROG7313 Module-Manual / Module-Outline. Pretoria: Varsity College Pretoria.
//Viegen, F. v., 2022. A PracKcal introducKon to Android Room-3 : EnKty, Dao and Database objects.. [Online]
//Available at: hRps://youtu.be/RstQg7f4Edk?si=8RoAGp-OKPpMNVdY
//[Accessed 28 April 2025].
//androidbyexample, 2024. EnKKes ,Dao and Database -Android By Example. [Online] Available at: hRps://androidbyexample.com/modules/movie-db/STEP-050_Repo.html [Accessed 25 April 2025].
//AndroidDevelopers, 2023. Layouts in Views. [Online]
//Available at: hRps://developer.android.com/developer/ui/views/layout/declaring-layout [Accessed 23 April 2025].
//Kay, R. M., 2022. IntroducKon To Development WithAndroid Studio: XML The Five Minute Language. [Online]
//Available at: hRps://youtu.be/94tm21PIBMs?si=BpJQ9meXr1_ynL2m
//[Accessed 15 April 2025].
//Angga Risky. 2017. Rewards UI Design to Android XML Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=fjXMx_iLkTY [Accessed on 10 April 2025]
//GeeksforGeeks. 2025. Android UI Layouts. [online]. Available at: https://www.geeksforgeeks.org/android-ui-layouts/ [Accessed on 10 April 2025]
//Muhammadumarch. 2023. Implementing Navigation in Your Android App with Android Navigation Component. [online]. Available at: https://medium.com/@muhammadumarch321/implementing-navigation-in-your-android-app-with-android-navigation-component-ff22a3d300a [Accessed on 11 April 2025]
//Android Developers. 2025. Fragment lifecycle. [online]. Available at: https://developer.android.com/guide/fragments/lifecycle [Accessed on 12 April 2025]
//Android Knowledge. 2022. RecyclerView in Android Studio using Kotlin | Source Code | 2024. [online]. Available at: https://www.youtube.com/watch?v=IYhmpUmeGOQ [Accessed on 12 April 2025]
//Android Developers. 2025. Add an Image composition. [online]. Available at: https://developer.android.com/codelabs/basic-android-kotlin-compose-add-images#2 [Accessed on 9 April 2025]
//StackOverflow. 2021. Trying to create a simple recyclerView in Kotlin, but the adapter is not applying properly. [online]. Available at: https://stackoverflow.com/questions/43012903/trying-to-create-a-simple-recyclerview-in-kotlin-but-the-adapter-is-not-applyin [Accessed on 10 April 2025]
//Android Knowledge. 2024. ViewModel in Android Studio using Kotlin | Android Knowledge. [video online]. Available at: https://www.youtube.com/watch?v=v32hSKtlH9A [Accessed on 11 April 2025]
//Code With Cal. 2025. Room Database Android Studio Kotlin Example Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=-LNg-K7SncM [Accessed on 12 April 2025]
//Android Developers. 2025. Accessing data using Room DAOs. [online]. Available at: https://developer.android.com/training/data-storage/room/accessing-data [Accessed on 15 April 2025]