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

class MonthlySummaryFragment : Fragment() {//(Firebase, 2023),(Technology, 2020),(GeeksforGeeks, 2024)


    // UI components for displaying totals
    private lateinit var totalIncomeText: TextView
    private lateinit var totalExpenseText: TextView
    private lateinit var barGraph: ProgressBar
    private lateinit var percentageText: TextView
    private lateinit var balanceText: TextView  // Displays remaining balance

    // Firestore reference
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_monthly_summary, container, false)

        // Bind views from layout
        totalIncomeText = view.findViewById(R.id.value_total_balance)
        totalExpenseText = view.findViewById(R.id.value_total_expense)
        barGraph = view.findViewById(R.id.bar_graph)
        percentageText = view.findViewById(R.id.text_percentage_spent)
        balanceText = view.findViewById(R.id.text_balance_available)  // Link to TextView in layout

        // Fetch data when fragment is created
        lifecycleScope.launch {
            updateMonthlySummary()
        }

        return view
    }

    // Load and calculate summary data for the current month
    private suspend fun updateMonthlySummary() {
        Log.d("MonthlySummaryFragment", "Starting updateMonthlySummary")

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val uid = firebaseUser?.uid ?: return

        try {
            // Get current month and year
            val currentDate = LocalDate.now()
            val month = currentDate.format(DateTimeFormatter.ofPattern("MM"))
            val year = currentDate.format(DateTimeFormatter.ofPattern("yyyy"))

            // Fetch all user transactions
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("transactions")
                .get().await()

            val allTransactions = snapshot.toObjects(Transaction::class.java)

            // Filter only transactions for the current month/year
            val filtered = allTransactions.filter { tx ->
                try {
                    val date = LocalDate.parse(tx.dateTime.substring(0, 10))
                    date.monthValue.toString().padStart(2, '0') == month &&
                            date.year.toString() == year
                } catch (e: Exception) {
                    false
                }
            }

            // Separate income and expenses
            val income = filtered.filter { !it.expense }
            val expenses = filtered.filter { it.expense }

            // Calculate totals
            val totalIncome = income.sumOf { it.amount }
            val totalExpense = expenses.sumOf { it.amount }
            val balanceAvailable = totalIncome - totalExpense  // Calculate balance

            // Calculate remaining balance as percentage
            val percentage = if (totalIncome == 0.0) 0 else ((balanceAvailable / totalIncome) * 100).toInt()

            // Update UI with values
            totalIncomeText.text = "R %.2f".format(totalIncome)
            totalExpenseText.text = "R %.2f".format(totalExpense)
            percentageText.text = "$percentage% available"
            balanceText.text = "Balance Available: R %.2f".format(balanceAvailable)  // Show balance

            // Set progress bar
            barGraph.progress = percentage

        } catch (e: Exception) {
            Log.e("MonthlySummaryFragment", "Error: ${e.message}", e)//(Firebase, 2023)
        }
    }//(Firebase, 2023),(Technology, 2020),(GeeksforGeeks, 2024)
}

//Bibliography

//College, I. V., 2025. PROG7313 Module-Manual / Module-Outline. Pretoria: Varsity College Pretoria.
//Available at: hRps://developer.android.com/developer/ui/views/layout/declaring-layout [Accessed 23 April 2025].
//Kay, R. M., 2022. IntroducKon To Development WithAndroid Studio: XML The Five Minute Language. [Online]
//Available at: hRps://youtu.be/94tm21PIBMs?si=BpJQ9meXr1_ynL2m
//[Accessed 15 April 2025].
//Team, G. D. T., 2024. Add repository and Manual DI. [Online]
//Available at: hRps://developer.android.com/codelabs/basic-android-kotlin-compose-add- repository#0
//[Accessed 22 April 2025].
//Coder, O., 2022. Implament Pie Chart in Android Studio Using Kotlin. [Online] Available at: hRps://youtu.be/TUJHcU0FOkA?si=jk90LRSO1_eyMyIG
//[Accessed 24 April 2025].
//Coder, E. O., 2024. hot to create bar chart | MP Android Chart | Android Studio 2024. [Online]
//Available at: hRps://youtu.be/WdsmQ3Zyn84?si=jz2AtkIRsNEUwNbX
//[Accessed 23 April 2025].
//Firebase, 2023. Ge=ng Started with Firebase on Android. [Online] Available at: hLps://youtu.be/jbHfJpoOzkl?si=rQ0hPeu_qKWpuAlm [Accessed 27 May 2025].
//Technology, S., 2020. 017 How to create MP Android Chart from Firebase RealKme Database. [Online]
//Available at: hLps://youtu.be/C0O9u0jd6nQ?si=c-H-xO4ISG2DWqQx [Accessed 22 May 2025].
//GeeksforGeeks, 2024. How to Create and Add Data to Firebase Firestore in Android. [Online] Available at: hLps://www.geeksforgeeks.org/create-and-add-data-to-firebase-firestore-in- android/
//[Accessed 23 May 2025].