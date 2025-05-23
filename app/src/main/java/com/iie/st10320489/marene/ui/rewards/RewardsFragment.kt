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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.databinding.FragmentRewardsBinding
import com.iie.st10320489.marene.ui.rewards.RewardsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RewardsFragment : Fragment() { // (Code With Cal, 2025)

    private var _binding: FragmentRewardsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //ADAPTER
    private lateinit var bronClmAdapter: ClaimsAdapter
    private lateinit var silClmAdapter: ClaimsAdapter
    private lateinit var gldClmAdapter: ClaimsAdapter

    private lateinit var rewardsViewModel: RewardsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val homeViewModel =
            ViewModelProvider(this).get(RewardsViewModel::class.java)

        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        val root: View = binding.root


        // RecyclerViews setup
        binding.recyclerClmBronze.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerClmSilver.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerClmGold.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        // (Code With Cal, 2025)
        // Sample Rewards
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

        // Adapters
        bronClmAdapter = ClaimsAdapter(bronzeClaims)
        silClmAdapter = ClaimsAdapter(silverClaims)
        gldClmAdapter = ClaimsAdapter(goldClaims)

        binding.recyclerClmBronze.adapter = bronClmAdapter
        binding.recyclerClmSilver.adapter = silClmAdapter
        binding.recyclerClmGold.adapter = gldClmAdapter

        val sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        // Set the Button listeners
        binding.ItemClaim.setOnClickListener {
            // Check if the user has already claimed rewards this month
            val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
            val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

            lifecycleScope.launch {
                val currentUserEmail = sharedPreferences.getString("currentUserEmail", null)

                if (currentUserEmail != null) {
                    val db = DatabaseInstance.getDatabase(requireContext().applicationContext)
                    val userDao = db.userDao()
                    val transactionDao = db.transactionDao()
                    val userSettingsDao = db.userSettingsDao()

                    val user = withContext(Dispatchers.IO) {
                        userDao.findUserByEmail(currentUserEmail)
                    }

                    user?.let {
                        // Get the current user's total expenses and goals
                        val userSettings = withContext(Dispatchers.IO) {
                            userSettingsDao.getUserSettingsByUserId(it.userId)
                        }

                        val expensesTransactions = withContext(Dispatchers.IO) {
                            transactionDao.getMonthlyExpenses(it.userId, currentMonth, currentYear)
                        }

                        val totalExpenses = expensesTransactions.sumOf { txn -> txn.amount }
                        val maxGoal = userSettings?.maxGoal ?: 0.0

                        val expensePercent = if (maxGoal > 0) (totalExpenses / maxGoal * 100).coerceAtMost(100.0).toInt() else 0

                        val savingsTransactions = withContext(Dispatchers.IO) {
                            transactionDao.getMonthlySavings(it.userId, currentMonth, currentYear)
                        }

                        val totalSaved = savingsTransactions.sumOf { txn -> txn.amount }
                        val minGoal = userSettings?.minGoal ?: 0.0

                        val minPercent = if (minGoal > 0) (totalSaved / minGoal * 100).coerceAtMost(100.0).toInt() else 0

                        // Check conditions and apply the logic
                        if (expensePercent < 100 && minPercent == 100) {
                            // Update cashoos with 20 if both conditions are met
                            val newCashoos = it.cashoos + 20
                            it.cashoos = newCashoos

                            // Update the cashoos in the database
                            withContext(Dispatchers.IO) {
                                userDao.updateUser(it)
                            }

                            // Display a toast message
                            Toast.makeText(requireContext(), "You earned 20 Cashoos!", Toast.LENGTH_SHORT).show()
                        } else if (expensePercent < 100) {
                            // Only increase cashoos by 10 if the expense condition is met
                            val newCashoos = it.cashoos + 10
                            it.cashoos = newCashoos

                            // Update the cashoos in the database
                            withContext(Dispatchers.IO) {
                                userDao.updateUser(it)
                            }

                            Toast.makeText(requireContext(), "You earned 10 Cashoos!", Toast.LENGTH_SHORT).show()
                        } else if (minPercent == 100) {
                            // Only increase cashoos by 5 if the savings goal condition is met
                            val newCashoos = it.cashoos + 5
                            it.cashoos = newCashoos

                            // Update the cashoos in the database
                            withContext(Dispatchers.IO) {
                                userDao.updateUser(it)
                            }

                            Toast.makeText(requireContext(), "You earned 5 Cashoos!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "You don't qualify for rewards this month.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } // (Code With Cal, 2025)


        /*binding.helpButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_rewards_help)
        }*/

        binding.discPage.setOnClickListener {
            findNavController().navigate(R.id.navigation_rewards_discount)
        }

        // LOAD USER POINTS FROM DATABASE
        //val sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val currentUserEmail = sharedPreferences.getString("currentUserEmail", null)

        if (currentUserEmail != null) {
            val db = DatabaseInstance.getDatabase(requireContext().applicationContext)
            val userDao = db.userDao()
            val transactionDao = db.transactionDao()
            val userSettingsDao = db.userSettingsDao()

            lifecycleScope.launch {
                val currentUserEmail = sharedPreferences.getString("currentUserEmail", null)

                if (currentUserEmail != null) {
                    val db = DatabaseInstance.getDatabase(requireContext().applicationContext)
                    val userDao = db.userDao()

                    val user = withContext(Dispatchers.IO) {
                        // Log the email to check if it's being fetched correctly
                        Log.d("UserEmail", "Fetching user with email: $currentUserEmail")
                        userDao.findUserByEmail(currentUserEmail)
                    }

                    // Check if the user is found
                    user?.let {
                        // User is found, proceed with your logic
                        Log.d("UserInfo", "User found: ${it.userId}, ${it.cashoos}")
                        val formattedCash = "C ${String.format("%.2f", it.cashoos)}"
                        view?.findViewById<TextView>(R.id.txtPoints2)?.text = formattedCash

                        // Get current month and year
                        val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
                        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

                        // Fetch user settings and transactions
                        val userSettings = withContext(Dispatchers.IO) {
                            userSettingsDao.getUserSettingsByUserId(it.userId)
                        }

                        // Fetch monthly savings
                        val savingsTransactions = withContext(Dispatchers.IO) {
                            transactionDao.getMonthlySavings(it.userId, currentMonth, currentYear)
                        }

                        val totalSaved = savingsTransactions.sumOf { txn -> txn.amount }
                        val minGoal = userSettings?.minGoal ?: 0.0
                        val maxGoal = userSettings?.maxGoal ?: 0.0

                        val minPercent = if (minGoal > 0) (totalSaved / minGoal * 100).coerceAtMost(100.0).toInt() else 0

                        view?.findViewById<TextView>(R.id.minGoalPercentage)?.text = "$minPercent%"

                        // Fetch monthly expenses
                        val expensesTransactions = withContext(Dispatchers.IO) {
                            transactionDao.getMonthlyExpenses(it.userId, currentMonth, currentYear)
                        }

                        val totalExpenses = expensesTransactions.sumOf { txn -> txn.amount }

                        // Calculate expense percentage
                        val expensePercent = if (maxGoal > 0) (totalExpenses / maxGoal * 100).coerceAtMost(100.0).toInt() else 0

                        view?.findViewById<TextView>(R.id.maxGoalPercentage)?.text = "$expensePercent%"
                    } ?: run {
                        // Handle case where user is not found
                        Log.e("UserInfo", "User not found for email: $currentUserEmail")
                    }
                }
            }


        } // (Code With Cal, 2025)


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} // (Code With Cal, 2025)

//Reference List:
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