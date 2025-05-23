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
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
        val sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("currentUserEmail", null)
        if (email.isNullOrEmpty()) return

        try {
            val userSnapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (userSnapshot.isEmpty) return
            val userId = userSnapshot.documents[0].id

            val currentDate = LocalDate.now()
            val currentMonth = currentDate.format(DateTimeFormatter.ofPattern("MM"))
            val currentYear = currentDate.format(DateTimeFormatter.ofPattern("yyyy"))

            val transactions = firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .get()
                .await()

            var totalIncome = 0.0
            var totalExpense = 0.0

            for (doc in transactions) {
                val dateStr = doc.getString("dateTime") ?: continue
                val amount = doc.getDouble("amount") ?: 0.0
                val isExpense = doc.getBoolean("expense") ?: true

                val transactionDate = LocalDate.parse(dateStr.substring(0, 10))
                val month = transactionDate.monthValue.toString().padStart(2, '0')
                val year = transactionDate.year.toString()

                if (month == currentMonth && year == currentYear) {
                    if (isExpense) totalExpense += amount else totalIncome += amount
                }
            }

            val percentageSpent = if (totalIncome == 0.0) 0 else (totalExpense / totalIncome * 100).toInt()

            withContext(Dispatchers.Main) {
                totalIncomeText.text = "R %.2f".format(totalIncome)
                totalExpenseText.text = "-R %.2f".format(totalExpense)
                barGraph.progress = 100 - percentageSpent
                percentageText.text = "You've spent $percentageSpent% of your income"
            }

        } catch (e: Exception) {
            Log.e("MonthlySummaryFragment", "Error updating summary: ${e.message}")
        }
    }
}


//Bibliography
//AndroidDevelopers, 2024. Save data in a local database using Room. [Online] Available at: hRps://developer.android.com/training/data-storage/room [Accessed 27 April 2025].
//AndroidDevelopers, 2024. Write asynchronous DAO queries. [Online]
//Available at: hRps://developer.android.com/training/data-storage/room/async- queries?authuser=2
//[Accessed 26 April 2025].
//Raikwar, A., 2024. Ge=ng Started with Room Database in Android. [Online]
//Available at: hRps://amitraikwar.medium.com/ge[ng-started-with-room-database-in- android-fa1ca23ce21e
//[Accessed 27 April 2025].
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
//Team, G. D. T., 2024. Add repository and Manual DI. [Online]
//Available at: hRps://developer.android.com/codelabs/basic-android-kotlin-compose-add- repository#0
//[Accessed 22 April 2025].
//Coder, O., 2022. Implament Pie Chart in Android Studio Using Kotlin. [Online] Available at: hRps://youtu.be/TUJHcU0FOkA?si=jk90LRSO1_eyMyIG
//[Accessed 24 April 2025].
//Coder, E. O., 2024. hot to create bar chart | MP Android Chart | Android Studio 2024. [Online]
//Available at: hRps://youtu.be/WdsmQ3Zyn84?si=jz2AtkIRsNEUwNbX
//[Accessed 23 April 2025].
