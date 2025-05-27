package com.iie.st10320489.marene.ui.analysis

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Category
import com.iie.st10320489.marene.data.entities.Transaction
import com.iie.st10320489.marene.graphs.MonthlySummaryFragment
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AnalysisFragment : Fragment() {//(Firebase, 2023),(Technology, 2020),(GeeksforGeeks, 2024)

    private val TAG = "AnalysisFragment"
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var tabLayout: TabLayout
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_analysis2, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind the views
        pieChart = view.findViewById(R.id.pieChart)
        barChart = view.findViewById(R.id.barChart)
        tabLayout = view.findViewById(R.id.tabLayout)
        firestore = FirebaseFirestore.getInstance()

        // Load pie chart and tabs
        setupPieChart()
        setupTabs()
        addMonthlySummaryFragment()
    }

    private fun setupPieChart() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                // Get all expense transactions
                val txSnapshot = firestore.collection("users").document(uid).collection("transactions").get().await()
                val transactions = txSnapshot.toObjects(Transaction::class.java).filter { it.expense }

                // If no data, show default
                if (transactions.isEmpty()) {
                    pieChart.clear()
                    pieChart.centerText = "Total Expense\nR 0.00"
                    pieChart.setCenterTextSize(16f)
                    pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD)
                    pieChart.invalidate()
                    return@launch
                }

                val totalExpenses = transactions.sumOf { it.amount }
                val categoryMap = mutableMapOf<String, Double>()

                // Aggregate amounts by category
                for (tx in transactions) {
                    categoryMap[tx.categoryId] = categoryMap.getOrDefault(tx.categoryId, 0.0) + tx.amount
                }

                val entries = mutableListOf<PieEntry>()
                val colors = mutableListOf<Int>()

                // Find layout to populate category breakdown
                val detailsLayout = view?.findViewById<LinearLayout>(R.id.detailsLayout)
                detailsLayout?.removeAllViews()
                val inflater = LayoutInflater.from(requireContext())

                // Add heading row for catagory, amount and percentage
                val headingRow = inflater.inflate(R.layout.item_transaction_summary_row, detailsLayout, false)
                headingRow.findViewById<TextView>(R.id.categoryText).apply {
                    text = "Category"
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_heading))
                }
                headingRow.findViewById<TextView>(R.id.amountText).apply {
                    text = "Amount"
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_heading))
                }
                headingRow.findViewById<TextView>(R.id.percentText).apply {
                    text = "%"
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_heading))
                }
                headingRow.findViewById<View>(R.id.colorDot).visibility = View.INVISIBLE
                detailsLayout?.addView(headingRow)

                // Add each category row to layout and pie chart
                for ((catId, amount) in categoryMap) {
                    val categoryDoc = firestore.collection("users").document(uid).collection("categories").document(catId).get().await()
                    if (!categoryDoc.exists()) continue
                    val category = categoryDoc.toObject(Category::class.java) ?: continue

                    val percent = ((amount / totalExpenses) * 100).toFloat()
                    entries.add(PieEntry(percent, category.name))

                    val colorInt = try {
                        ContextCompat.getColor(requireContext(), category.colour)
                    } catch (e: Exception) {
                        Log.e(TAG, "Invalid color resource ID: ${category.colour}", e)
                        Color.GRAY
                    }
                    colors.add(colorInt)

                    // Add row to UI layout
                    val row = inflater.inflate(R.layout.item_transaction_summary_row, detailsLayout, false)
                    row.findViewById<TextView>(R.id.categoryText).text = category.name
                    row.findViewById<TextView>(R.id.amountText).text = "R%.2f".format(amount)
                    row.findViewById<TextView>(R.id.percentText).text = "%.1f%%".format(percent)
                    row.findViewById<View>(R.id.colorDot).setBackgroundColor(colorInt)
                    row.findViewById<TextView>(R.id.categoryText).setTextColor(colorInt)
                    detailsLayout?.addView(row)
                }

                // Create pie chart data set
                val dataSet = PieDataSet(entries, "").apply {
                    this.colors = colors
                    valueTextSize = 12f
                    valueTextColor = Color.BLACK
                    sliceSpace = 3f
                    selectionShift = 5f
                }

                // Format the value labels
                val pieData = PieData(dataSet).apply {
                    setValueFormatter(object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String = "%.1f%%".format(value)
                    })
                }

                // Configure pie chart fully
                pieChart.apply {
                    data = pieData
                    setUsePercentValues(true)
                    setEntryLabelColor(Color.BLACK)
                    setDrawEntryLabels(true)
                    legend.isEnabled = false  // disable auto legend
                    legend.textColor = Color.DKGRAY
                    legend.textSize = 12f
                    legend.isWordWrapEnabled = true
                    legend.orientation = Legend.LegendOrientation.VERTICAL
                    legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    setHoleColor(Color.WHITE)
                    setDrawCenterText(true)
                    centerText = "Total Expense\nR %.2f".format(totalExpenses)
                    setCenterTextSize(16f)
                    setCenterTextTypeface(Typeface.DEFAULT_BOLD)
                    animateY(1000)
                    invalidate()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Pie chart error: ${e.message}", e)
            }
        }
    }
    //(Firebase, 2023),(Technology, 2020),(GeeksforGeeks, 2024)
    private fun setupTabs() {
        // Add tabs for chart view options
        tabLayout.addTab(tabLayout.newTab().setText("Weekly"))
        tabLayout.addTab(tabLayout.newTab().setText("Monthly"), true)
        tabLayout.addTab(tabLayout.newTab().setText("Yearly"))

        // Load default view
        setChartData("Monthly")

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = setChartData(tab.text.toString())
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun addMonthlySummaryFragment() {
        // Attach the summary fragment
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MonthlySummaryFragment())
            .commit()
    }

    private fun setChartData(mode: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                // Fetch all transactions
                val txSnapshot = firestore.collection("users").document(uid).collection("transactions").get().await()
                val transactions = txSnapshot.toObjects(Transaction::class.java)
                val now = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

                // Calculate income and expense totals
                val incomeTotal = transactions.filter { !it.expense }.sumOf { it.amount }
                val expenseTotal = transactions.filter { it.expense }.sumOf { it.amount }

                // Update totals on screen
                view?.findViewById<TextView>(R.id.incomeAmountTextView)?.text = "R %.2f".format(incomeTotal)
                view?.findViewById<TextView>(R.id.expenseAmountTextView)?.text = "R %.2f".format(expenseTotal)

                val incomeEntries = ArrayList<BarEntry>()
                val expenseEntries = ArrayList<BarEntry>()
                val labels = ArrayList<String>()

                val filteredTransactions = transactions.filter { it.dateTime.isNotBlank() }

                // Weekly view
                when (mode) {
                    "Weekly" -> {
                        val weekStart = now.with(java.time.DayOfWeek.MONDAY)
                        for (i in 0..6) {
                            val day = weekStart.plusDays(i.toLong())
                            val filtered = filteredTransactions.filter {
                                try { LocalDate.parse(it.dateTime, formatter) == day } catch (_: Exception) { false }
                            }
                            incomeEntries.add(BarEntry(i.toFloat(), filtered.filter { !it.expense }.sumOf { it.amount }.toFloat()))
                            expenseEntries.add(BarEntry(i.toFloat(), filtered.filter { it.expense }.sumOf { it.amount }.toFloat()))
                            labels.add(day.dayOfWeek.name.take(3))
                        }
                    }
                    // Monthly view
                    "Monthly" -> {
                        val monthStart = now.withDayOfMonth(1)
                        val weeks = listOf(0, 7, 14, 21)
                        for ((i, offset) in weeks.withIndex()) {
                            val weekStart = monthStart.plusDays(offset.toLong())
                            val weekEnd = weekStart.plusDays(6)
                            val filtered = filteredTransactions.filter {
                                try {
                                    val txDate = LocalDate.parse(it.dateTime, formatter)
                                    txDate in weekStart..weekEnd
                                } catch (_: Exception) { false }
                            }
                            incomeEntries.add(BarEntry(i.toFloat(), filtered.filter { !it.expense }.sumOf { it.amount }.toFloat()))
                            expenseEntries.add(BarEntry(i.toFloat(), filtered.filter { it.expense }.sumOf { it.amount }.toFloat()))
                            labels.add("W${i + 1}")
                        }
                    }
                    // Yearly view
                    "Yearly" -> {
                        for (i in 1..12) {
                            val filtered = filteredTransactions.filter {
                                try {
                                    val txDate = LocalDate.parse(it.dateTime, formatter)
                                    txDate.monthValue == i && txDate.year == now.year
                                } catch (_: Exception) { false }
                            }
                            incomeEntries.add(BarEntry((i - 1).toFloat(), filtered.filter { !it.expense }.sumOf { it.amount }.toFloat()))
                            expenseEntries.add(BarEntry((i - 1).toFloat(), filtered.filter { it.expense }.sumOf { it.amount }.toFloat()))
                            labels.add(java.time.Month.of(i).name.take(3))
                        }
                    }
                }

                // Build bar chart data sets
                val incomeSet = BarDataSet(incomeEntries, "Income").apply {
                    color = ContextCompat.getColor(requireContext(), R.color.income)
                }

                val expenseSet = BarDataSet(expenseEntries, "Expenses").apply {
                    color = ContextCompat.getColor(requireContext(), R.color.outcome)
                }

                val barData = BarData(incomeSet, expenseSet).apply {
                    barWidth = 0.4f
                    groupBars(0f, 0.2f, 0f)
                }

                // Configure bar chart
                barChart.apply {
                    data = barData
                    xAxis.apply {
                        valueFormatter = IndexAxisValueFormatter(labels)
                        granularity = 1f
                        setCenterAxisLabels(true)
                        axisMinimum = 0f
                        axisMaximum = labels.size.toFloat()
                        position = XAxis.XAxisPosition.BOTTOM
                    }
                    axisLeft.axisMinimum = 0f
                    axisRight.isEnabled = false
                    description.isEnabled = false
                    legend.isEnabled = true
                    invalidate()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Bar chart error: ${e.message}", e)
            }//(Firebase, 2023),(Technology, 2020),(GeeksforGeeks, 2024)
        }
    }
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