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
        val userId = sharedPreferences.getString("currentUserId", null)
        Log.d("MonthlySummaryFragment", "UserId from SharedPreferences: $userId")

        if (userId != null) {
            try {
                val currentDate = LocalDate.now()
                val month = currentDate.format(DateTimeFormatter.ofPattern("MM"))
                val year = currentDate.format(DateTimeFormatter.ofPattern("yyyy"))

                Log.d("MonthlySummaryFragment", "Current Month: $month, Year: $year")

                val transactionsSnapshot = db.collection("transactions")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                Log.d("MonthlySummaryFragment", "Transactions fetched: ${transactionsSnapshot.size()}")

                val allTransactions = transactionsSnapshot.documents.mapNotNull { doc ->
                    try {
                        val transaction = doc.toObject(Transaction::class.java)
                        Log.d("MonthlySummaryFragment", "Transaction parsed: ${transaction?.name} on ${transaction?.dateTime}")
                        transaction
                    } catch (e: Exception) {
                        Log.e("MonthlySummaryFragment", "Failed to parse transaction: ${doc.id}", e)
                        null
                    }
                }

                if (allTransactions.isEmpty()) {
                    Log.w("MonthlySummaryFragment", "No transactions found for user")
                }

                val filteredTransactions = allTransactions.mapNotNull { transaction ->
                    val date = transaction.dateTime
                    try {
                        if (!date.isNullOrBlank() && date.length >= 10) {
                            val dateObj = LocalDate.parse(date.substring(0, 10))
                            if (dateObj.monthValue.toString().padStart(2, '0') == month &&
                                dateObj.year.toString() == year) {
                                transaction
                            } else {
                                Log.d("MonthlySummaryFragment", "Filtered out transaction (wrong month/year): ${transaction.name} -> $date")
                                null
                            }
                        } else {
                            Log.w("MonthlySummaryFragment", "Invalid or empty date: $date")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("MonthlySummaryFragment", "Exception parsing date '$date'", e)
                        null
                    }
                }

                Log.d("MonthlySummaryFragment", "Filtered transactions: ${filteredTransactions.size}")

                val incomeList = filteredTransactions.filter { !it.expense }
                val expenseList = filteredTransactions.filter { it.expense }

                Log.d("MonthlySummaryFragment", "Income count: ${incomeList.size}, Expense count: ${expenseList.size}")

                val totalIncome = incomeList.sumOf { it.amount }
                val totalExpense = expenseList.sumOf { it.amount }

                withContext(Dispatchers.Main) {
                    totalIncomeText.text = "R %.2f".format(totalIncome)
                    totalExpenseText.text = "-R %.2f".format(totalExpense)

                    val percentageSpent = if (totalIncome == 0.0) 0 else (totalExpense / totalIncome * 100).toInt()
                    barGraph.progress = 100 - percentageSpent
                    percentageText.text = "You've spent $percentageSpent% of your income"

                    Log.d("MonthlySummaryFragment", "Displayed Summary -> Income: R$totalIncome, Expense: R$totalExpense, Spent: $percentageSpent%")
                }

            } catch (e: Exception) {
                Log.e("MonthlySummaryFragment", "Error fetching or processing data", e)
            }
        } else {
            Log.e("MonthlySummaryFragment", "User ID not found in SharedPreferences")
        }
    }


}
