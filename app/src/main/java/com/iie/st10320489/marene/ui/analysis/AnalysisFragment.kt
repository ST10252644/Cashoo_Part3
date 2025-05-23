package com.iie.st10320489.marene.ui.analysis

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AnalysisFragment : Fragment() {

    private val TAG = "AnalysisFragment"
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var tabLayout: TabLayout
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_analysis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pieChart = view.findViewById(R.id.pieChart)
        barChart = view.findViewById(R.id.barChart)
        tabLayout = view.findViewById(R.id.tabLayout)

        firestore = FirebaseFirestore.getInstance()

        setupTabs()
        setupPieChart()
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Weekly"))
        tabLayout.addTab(tabLayout.newTab().setText("Monthly"), true)
        tabLayout.addTab(tabLayout.newTab().setText("Yearly"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                loadBarChartData(tab.text.toString())
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        loadBarChartData("Monthly")
    }

    private fun setupPieChart() {
        val email = getCurrentUserEmail() ?: return
        val detailsLayout = view?.findViewById<LinearLayout>(R.id.detailsLayout)

        firestore.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { users ->
                val userId = users.firstOrNull()?.id ?: return@addOnSuccessListener
                firestore.collection("transactions").whereEqualTo("userId", userId).get()
                    .addOnSuccessListener { docs ->
                        val expenses = docs.filter { it.getBoolean("expense") == true }
                        if (expenses.isEmpty()) return@addOnSuccessListener

                        val categorySums = expenses.groupBy { it.getString("category") ?: "Unknown" }
                            .mapValues { it.value.sumOf { doc -> doc.getDouble("amount") ?: 0.0 } }

                        val total = categorySums.values.sum()
                        val entries = categorySums.map { (cat, amount) -> PieEntry((amount / total * 100).toFloat(), cat) }

                        val dataSet = PieDataSet(entries, "").apply {
                            colors = categorySums.keys.map { Color.GRAY } // customize as needed
                            valueTextColor = Color.TRANSPARENT
                        }

                        pieChart.data = PieData(dataSet)
                        pieChart.description.isEnabled = false
                        pieChart.legend.isEnabled = false
                        pieChart.setDrawEntryLabels(false)
                        pieChart.setDrawCenterText(true)
                        pieChart.centerText = "Total Expense\nR %.2f".format(total)
                        pieChart.setCenterTextSize(16f)
                        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD)
                        pieChart.animateY(1000)
                        pieChart.invalidate()

                        detailsLayout?.removeAllViews()
                        categorySums.forEach { (cat, amt) ->
                            val row = LinearLayout(requireContext()).apply {
                                orientation = LinearLayout.HORIZONTAL
                            }
                            val label = TextView(requireContext()).apply { text = "● $cat"; setTextColor(Color.DKGRAY) }
                            val amount = TextView(requireContext()).apply { text = "R %.2f".format(amt) }
                            row.addView(label)
                            row.addView(amount)
                            detailsLayout?.addView(row)
                        }
                    }
            }
    }

    private fun loadBarChartData(mode: String) {
        val email = getCurrentUserEmail() ?: return
        val incomeEntries = ArrayList<BarEntry>()
        val expenseEntries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        val now = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        firestore.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { users ->
                val userId = users.firstOrNull()?.id ?: return@addOnSuccessListener

                firestore.collection("transactions").whereEqualTo("userId", userId).get()
                    .addOnSuccessListener { docs ->
                        val transactions = docs.map {
                            val isExpense = it.getBoolean("expense") == true
                            val date = LocalDate.parse(it.getString("dateTime") ?: now.toString(), formatter)
                            Triple(date, isExpense, it.getDouble("amount") ?: 0.0)
                        }

                        val grouped = when (mode) {
                            "Weekly" -> (0..6).map { i ->
                                val day = now.with(java.time.DayOfWeek.MONDAY).plusDays(i.toLong())
                                val daily = transactions.filter { it.first == day }
                                labels.add(day.dayOfWeek.name.take(3))
                                Pair(daily, i)
                            }
                            "Monthly" -> listOf(0, 7, 14, 21).mapIndexed { i, offset ->
                                val start = now.withDayOfMonth(1).plusDays(offset.toLong())
                                val end = start.plusDays(6)
                                val weekly = transactions.filter { it.first in start..end }
                                labels.add("W${i + 1}")
                                Pair(weekly, i)
                            }
                            else -> (1..12).map { month ->
                                val monthly = transactions.filter { it.first.monthValue == month }
                                labels.add(java.time.Month.of(month).name.take(3))
                                Pair(monthly, month - 1)
                            }
                        }

                        grouped.forEach { (txs, index) ->
                            val income = txs.filter { !it.second }.sumOf { it.third }.toFloat()
                            val expense = txs.filter { it.second }.sumOf { it.third }.toFloat()
                            incomeEntries.add(BarEntry(index.toFloat(), income))
                            expenseEntries.add(BarEntry(index.toFloat(), expense))
                        }

                        val barData = BarData(
                            BarDataSet(incomeEntries, "Income").apply {
                                color = ContextCompat.getColor(requireContext(), R.color.income)
                            },
                            BarDataSet(expenseEntries, "Expenses").apply {
                                color = ContextCompat.getColor(requireContext(), R.color.outcome)
                            }
                        ).apply {
                            barWidth = 0.4f
                            groupBars(0f, 0.2f, 0f)
                        }

                        barChart.data = barData
                        barChart.xAxis.apply {
                            valueFormatter = IndexAxisValueFormatter(labels)
                            granularity = 1f
                            setCenterAxisLabels(true)
                            axisMinimum = 0f
                            axisMaximum = labels.size.toFloat()
                            position = XAxis.XAxisPosition.BOTTOM
                        }
                        barChart.axisLeft.axisMinimum = 0f
                        barChart.axisRight.isEnabled = false
                        barChart.description.isEnabled = false
                        barChart.legend.isEnabled = true
                        barChart.invalidate()
                    }
            }
    }

    private fun getCurrentUserEmail(): String? {
        return requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
            .getString("currentUserEmail", null)
    }
}
