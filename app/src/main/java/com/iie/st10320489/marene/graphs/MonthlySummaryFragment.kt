package com.iie.st10320489.marene.graphs

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Transaction
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MonthlySummaryFragment : Fragment() {

    private lateinit var totalIncomeText: TextView
    private lateinit var totalExpenseText: TextView
    private lateinit var barGraph: ProgressBar
    private lateinit var percentageText: TextView

    private val firestore = FirebaseFirestore.getInstance()

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

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val uid = firebaseUser?.uid ?: return

        try {
            val currentDate = LocalDate.now()
            val month = currentDate.format(DateTimeFormatter.ofPattern("MM"))
            val year = currentDate.format(DateTimeFormatter.ofPattern("yyyy"))

            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("transactions")
                .get().await()

            val allTransactions = snapshot.toObjects(Transaction::class.java)

            val filtered = allTransactions.filter { tx ->
                try {
                    val date = LocalDate.parse(tx.dateTime.substring(0, 10))
                    date.monthValue.toString().padStart(2, '0') == month &&
                            date.year.toString() == year
                } catch (e: Exception) {
                    false
                }
            }

            val income = filtered.filter { !it.expense }
            val expenses = filtered.filter { it.expense }

            val totalIncome = income.sumOf { it.amount }
            val totalExpense = expenses.sumOf { it.amount }

            totalIncomeText.text = "R %.2f".format(totalIncome)
            totalExpenseText.text = "R %.2f".format(totalExpense)

            val percentage = if (totalIncome == 0.0) 0 else ((totalExpense / totalIncome) * 100).toInt()
            percentageText.text = "$percentage% spent"

            barGraph.progress = percentage

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
