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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.graphs.MonthlySummaryFragment
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AnalysisFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var tabLayout: TabLayout
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_analysis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pieChart = view.findViewById(R.id.pieChart)
        barChart = view.findViewById(R.id.barChart)
        tabLayout = view.findViewById(R.id.tabLayout)

        setupPieChart()
        setupTabs()
        addMonthlySummaryFragment()
    }

    private fun setupPieChart() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("transactions").get()
            .addOnSuccessListener { result ->
                val transactions = result.documents.mapNotNull { it.data }
                if (transactions.isEmpty()) {
                    Toast.makeText(context, "No expense data", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val expenses = transactions.filter { it["expense"] as Boolean }
                val totalExpenses = expenses.sumOf { (it["amount"] as Number).toDouble() }

                val categorySums = expenses.groupBy { it["categoryName"] as String }
                    .mapValues { entry -> entry.value.sumOf { (it["amount"] as Number).toDouble() } }

                val entries = categorySums.map { (name, amount) ->
                    val percentage = (amount / totalExpenses * 100).toFloat()
                    PieEntry(percentage, name)
                }

                val colors = expenses.map {
                    val colorHex = it["categoryColor"] as? String ?: "#AAAAAA"
                    try {
                        Color.parseColor(colorHex)
                    } catch (_: Exception) {
                        Color.GRAY
                    }
                }

                val dataSet = PieDataSet(entries, "").apply {
                    this.colors = colors
                    valueTextColor = Color.TRANSPARENT
                }

                pieChart.apply {
                    data = PieData(dataSet)
                    description.isEnabled = false
                    legend.isEnabled = false
                    setDrawEntryLabels(false)
                    setHoleColor(Color.WHITE)
                    setDrawCenterText(true)
                    centerText = "Total Expense\nR %.2f".format(totalExpenses)
                    setCenterTextSize(16f)
                    setCenterTextTypeface(Typeface.DEFAULT_BOLD)
                    animateY(1000)
                    invalidate()
                }

                val detailsLayout = view?.findViewById<LinearLayout>(R.id.detailsLayout)
                detailsLayout?.removeViews(1, detailsLayout.childCount - 1)

                categorySums.forEach { (category, amount) ->
                    val percentage = amount / totalExpenses * 100
                    val color = try {
                        Color.parseColor(
                            expenses.find { it["categoryName"] == category }?.get("categoryColor") as? String
                                ?: "#AAAAAA"
                        )
                    } catch (e: Exception) {
                        Color.GRAY
                    }

                    val row = LinearLayout(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 0, 0, 8)
                        }
                        orientation = LinearLayout.HORIZONTAL
                    }

                    val dot = TextView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        text = "● $category"
                        setTextColor(color)
                        textSize = 14f
                    }

                    val amountView = TextView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        text = "R %.2f".format(amount)
                        textSize = 14f
                    }

                    val percentView = TextView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        text = "%.1f%%".format(percentage)
                        setTypeface(null, Typeface.BOLD)
                        textSize = 14f
                        gravity = Gravity.END
                    }

                    row.addView(dot)
                    row.addView(amountView)
                    row.addView(percentView)
                    detailsLayout?.addView(row)
                }
            }
            .addOnFailureListener {
                Log.e("AnalysisFragment", "Failed to load transactions", it)
            }
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Weekly"))
        tabLayout.addTab(tabLayout.newTab().setText("Monthly"), true)
        tabLayout.addTab(tabLayout.newTab().setText("Yearly"))

        setChartData("Monthly")

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                setChartData(tab.text.toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setChartData(mode: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("transactions").get()
            .addOnSuccessListener { result ->
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val now = LocalDate.now()
                val transactions = result.documents.mapNotNull { doc ->
                    doc.data?.let {
                        it["dateTime"] = it["dateTime"] ?: return@let null
                        it
                    }
                }

                val incomeEntries = mutableListOf<BarEntry>()
                val expenseEntries = mutableListOf<BarEntry>()
                val labels = mutableListOf<String>()

                when (mode) {
                    "Weekly" -> {
                        val weekStart = now.with(java.time.DayOfWeek.MONDAY)
                        for (i in 0..6) {
                            val day = weekStart.plusDays(i.toLong())
                            val dayStr = day.toString()
                            val daily = transactions.filter {
                                it["dateTime"]?.toString()?.startsWith(dayStr) == true
                            }
                            val income = daily.filter { it["expense"] == false }.sumOf { (it["amount"] as Number).toDouble() }.toFloat()
                            val expense = daily.filter { it["expense"] == true }.sumOf { (it["amount"] as Number).toDouble() }.toFloat()

                            incomeEntries.add(BarEntry(i.toFloat(), income))
                            expenseEntries.add(BarEntry(i.toFloat(), expense))
                            labels.add(day.dayOfWeek.name.take(3))
                        }
                    }
                    // Add Monthly/Yearly modes similarly...
                }

                val groupSpace = 0.08f
                val barSpace = 0.03f
                val barWidth = 0.43f

                val incomeSet = BarDataSet(incomeEntries, "Income").apply {
                    color = ContextCompat.getColor(requireContext(), R.color.teal_700)
                }
                val expenseSet = BarDataSet(expenseEntries, "Expense").apply {
                    color = ContextCompat.getColor(requireContext(), R.color.red)
                }

                val data = BarData(incomeSet, expenseSet)
                data.barWidth = barWidth
                barChart.data = data

                barChart.xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    valueFormatter = IndexAxisValueFormatter(labels)
                    granularity = 1f
                    setCenterAxisLabels(true)
                }

                barChart.axisLeft.axisMinimum = 0f
                barChart.axisRight.isEnabled = false
                barChart.description.isEnabled = false
                barChart.setVisibleXRangeMaximum(labels.size.toFloat())
                barChart.groupBars(0f, groupSpace, barSpace)
                barChart.invalidate()
            }
    }

    private fun addMonthlySummaryFragment() {
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MonthlySummaryFragment())
            .addToBackStack(null)
            .commit()
    }
}
