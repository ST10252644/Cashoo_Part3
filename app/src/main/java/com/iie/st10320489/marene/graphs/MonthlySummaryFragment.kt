package com.iie.st10320489.marene.graphs

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MonthlySummaryFragment : Fragment() {

    private lateinit var totalIncomeText: TextView
    private lateinit var totalExpenseText: TextView
    private lateinit var barGraph: ProgressBar
    private lateinit var percentageText: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_monthly_summary, container, false)

        totalIncomeText = view.findViewById(R.id.value_total_balance)
        totalExpenseText = view.findViewById(R.id.value_total_expense)
        barGraph = view.findViewById(R.id.bar_graph)
        percentageText = view.findViewById(R.id.text_percentage_spent)

        lifecycleScope.launch {
            updateMonthlySummary()
        }

        return view
    }

    private suspend fun updateMonthlySummary() {
        Log.d("MonthlySummaryFragment", "Starting updateMonthlySummary")

        val sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("currentUserEmail", null)
        Log.d("MonthlySummaryFragment", "Email: $email")

        if (email != null) {
            try {
                // Get userId using the email
                val userQuerySnapshot = db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                val userId = userQuerySnapshot.documents.firstOrNull()?.id

                if (userId != null) {
                    val currentDate = LocalDate.now()
                    val month = currentDate.format(DateTimeFormatter.ofPattern("MM"))
                    val year = currentDate.format(DateTimeFormatter.ofPattern("yyyy"))

                    val transactionsSnapshot = db.collection("transactions")
                        .whereEqualTo("userId", userId)
                        .get()
                        .await()

                    val allTransactions = transactionsSnapshot.documents.mapNotNull {
                        it.toObject(Transaction::class.java)
                    }

                    // Filter by current month & year
                    val filteredTransactions = allTransactions.filter {
                        val date = it.dateTime // Assume format: yyyy-MM-dd or yyyy-MM-ddTHH:mm
                        val dateObj = LocalDate.parse(date.substring(0, 10))
                        dateObj.monthValue.toString().padStart(2, '0') == month &&
                                dateObj.year.toString() == year
                    }

                    val incomeList = filteredTransactions.filter { !it.expense }
                    val expenseList = filteredTransactions.filter { it.expense }

                    val totalIncome = incomeList.sumOf { it.amount }
                    val totalExpense = expenseList.sumOf { it.amount }

                    withContext(Dispatchers.Main) {
                        totalIncomeText.text = "R %.2f".format(totalIncome)
                        totalExpenseText.text = "-R %.2f".format(totalExpense)

                        val percentageSpent = if (totalIncome == 0.0) 0 else (totalExpense / totalIncome * 100).toInt()
                        barGraph.progress = 100 - percentageSpent
                        percentageText.text = "You've spent $percentageSpent% of your income"

                        Log.d("MonthlySummaryFragment", "TotalIncome: $totalIncome, TotalExpense: $totalExpense, %Spent: $percentageSpent")
                    }
                } else {
                    Log.e("MonthlySummaryFragment", "User ID not found for email: $email")
                }
            } catch (e: Exception) {
                Log.e("MonthlySummaryFragment", "Error fetching data: ${e.message}", e)
            }
        }
    }
}
