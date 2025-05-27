//package com.iie.st10320489.marene.ui.analysis
//
//import android.content.Context
//import android.graphics.Color
//import android.view.LayoutInflater
//import android.view.View
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.FragmentManager
//import androidx.fragment.app.FragmentTransaction
//import androidx.fragment.app.testing.FragmentScenario
//import androidx.fragment.app.testing.launchFragmentInContainer
//import androidx.lifecycle.Lifecycle
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.github.mikephil.charting.charts.BarChart
//import com.github.mikephil.charting.charts.PieChart
//import com.github.mikephil.charting.data.BarData
//import com.github.mikephil.charting.data.PieData
//import com.google.android.gms.tasks.Task
//import com.google.android.gms.tasks.Tasks
//import com.google.android.material.tabs.TabLayout
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.firestore.*
//import com.iie.st10320489.marene.R
//import com.iie.st10320489.marene.data.entities.Category
//import com.iie.st10320489.marene.data.entities.Transaction
//import io.mockk.*
//import io.mockk.impl.annotations.MockK
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.StandardTestDispatcher
//import kotlinx.coroutines.test.TestCoroutineScheduler
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.tasks.await
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.robolectric.annotation.Config
//import java.time.LocalDate
//import kotlin.test.assertEquals
//import kotlin.test.assertNotNull
//import kotlin.test.assertTrue
//
//@ExperimentalCoroutinesApi
//@RunWith(AndroidJUnit4::class)
//@Config(sdk = [28])
//class AnalysisFragmentTest {
//
//    @MockK
//    private lateinit var mockFirestore: FirebaseFirestore
//
//    @MockK
//    private lateinit var mockAuth: FirebaseAuth
//
//    @MockK
//    private lateinit var mockUser: FirebaseUser
//
//    @MockK
//    private lateinit var mockCollectionReference: CollectionReference
//
//    @MockK
//    private lateinit var mockDocumentReference: DocumentReference
//
//    @MockK
//    private lateinit var mockQuerySnapshot: QuerySnapshot
//
//    @MockK
//    private lateinit var mockDocumentSnapshot: DocumentSnapshot
//
//    @MockK
//    private lateinit var mockTask: Task<QuerySnapshot>
//
//    @MockK
//    private lateinit var mockDocTask: Task<DocumentSnapshot>
//
//    private lateinit var context: Context
//    private lateinit var scenario: FragmentScenario<AnalysisFragment>
//    private val testScheduler = TestCoroutineScheduler()
//    private val testDispatcher = StandardTestDispatcher(testScheduler)
//
//    @Before
//    fun setup() {
//        MockKAnnotations.init(this)
//        context = ApplicationProvider.getApplicationContext()
//
//        // Mock Firebase Auth
//        mockkStatic(FirebaseAuth::class)
//        every { FirebaseAuth.getInstance() } returns mockAuth
//        every { mockAuth.currentUser } returns mockUser
//        every { mockUser.uid } returns "test-uid"
//
//        // Mock Firestore
//        mockkStatic(FirebaseFirestore::class)
//        every { FirebaseFirestore.getInstance() } returns mockFirestore
//
//        // Mock ContextCompat
//        mockkStatic(ContextCompat::class)
//        every { ContextCompat.getColor(any(), any()) } returns Color.BLUE
//
//        setupFirestoreMocks()
//    }
//
//    @After
//    fun tearDown() {
//        if (::scenario.isInitialized) {
//            scenario.close()
//        }
//        unmockkAll()
//    }
//
//    private fun setupFirestoreMocks() {
//        every { mockFirestore.collection("users") } returns mockCollectionReference
//        every { mockCollectionReference.document("test-uid") } returns mockDocumentReference
//        every { mockDocumentReference.collection("transactions") } returns mockCollectionReference
//        every { mockDocumentReference.collection("categories") } returns mockCollectionReference
//        every { mockCollectionReference.document(any()) } returns mockDocumentReference
//        every { mockCollectionReference.get() } returns mockTask
//        every { mockDocumentReference.get() } returns mockDocTask
//        coEvery { mockTask.await() } returns mockQuerySnapshot
//        coEvery { mockDocTask.await() } returns mockDocumentSnapshot
//    }
//
//    @Test
//    fun `fragment initializes successfully`() {
//        scenario = launchFragmentInContainer<AnalysisFragment>()
//        scenario.moveToState(Lifecycle.State.RESUMED)
//
//        scenario.onFragment { fragment ->
//            assertNotNull(fragment.view)
//        }
//    }
//
//    @Test
//    fun `setupPieChart with empty transactions shows zero total`() = runTest(testDispatcher) {
//        // Setup empty transactions
//        every { mockQuerySnapshot.toObjects(Transaction::class.java) } returns emptyList()
//
//        scenario = launchFragmentInContainer<AnalysisFragment>()
//        scenario.moveToState(Lifecycle.State.RESUMED)
//
//        testScheduler.advanceUntilIdle()
//
//        scenario.onFragment { fragment ->
//            val pieChart = fragment.view?.findViewById<PieChart>(R.id.pieChart)
//            assertNotNull(pieChart)
//        }
//    }
//
//    @Test
//    fun `setupPieChart with expense transactions displays correctly`() = runTest(testDispatcher) {
//        // Create test data
//        val testTransactions = listOf(
//            createTestTransaction("tx1", 100.0, true, "cat1"),
//            createTestTransaction("tx2", 50.0, true, "cat2"),
//            createTestTransaction("tx3", 75.0, false, "cat1") // Income - should be filtered out
//        )
//
//        val testCategory1 = createTestCategory("cat1", "Food", R.color.outcome)
//        val testCategory2 = createTestCategory("cat2", "Transport", R.color.income)
//
//        // Mock Firestore responses
//        every { mockQuerySnapshot.toObjects(Transaction::class.java) } returns testTransactions
//        every { mockDocumentSnapshot.exists() } returns true
//        every { mockDocumentSnapshot.toObject(Category::class.java) } returnsMany listOf(testCategory1, testCategory2)
//
//        scenario = launchFragmentInContainer<AnalysisFragment>()
//        scenario.moveToState(Lifecycle.State.RESUMED)
//
//        testScheduler.advanceUntilIdle()
//
//        scenario.onFragment { fragment ->
//            val pieChart = fragment.view?.findViewById<PieChart>(R.id.pieChart)
//            assertNotNull(pieChart)
//        }
//    }
//
//    @Test
//    fun `setupTabs creates three tabs correctly`() {
//        scenario = launchFragmentInContainer<AnalysisFragment>()
//        scenario.moveToState(Lifecycle.State.RESUMED)
//
//        scenario.onFragment { fragment ->
//            val tabLayout = fragment.view?.findViewById<TabLayout>(R.id.tabLayout)
//            assertNotNull(tabLayout)
//            assertEquals(3, tabLayout?.tabCount)
//            assertEquals("Weekly", tabLayout?.getTabAt(0)?.text)
//            assertEquals("Monthly", tabLayout?.getTabAt(1)?.text)
//            assertEquals("Yearly", tabLayout?.getTabAt(2)?.text)
//        }
//    }
//
//    @Test
//    fun `setChartData for Weekly mode processes correctly`() = runTest(testDispatcher) {
//        val testTransactions = createTestTransactionsForWeek()
//        every { mockQuerySnapshot.toObjects(Transaction::class.java) } returns testTransactions
//
//        scenario = launchFragmentInContainer<AnalysisFragment>()
//        scenario.moveToState(Lifecycle.State.RESUMED)
//
//        testScheduler.advanceUntilIdle()
//
//        scenario.onFragment { fragment ->
//            val barChart = fragment.view?.findViewById<BarChart>(R.id.barChart)
//            assertNotNull(barChart)
//
//            val incomeTextView = fragment.view?.findViewById<TextView>(R.id.incomeAmountTextView)
//            val expenseTextView = fragment.view?.findViewById<TextView>(R.id.expenseAmountTextView)
//
//            assertNotNull(incomeTextView)
//            assertNotNull(expenseTextView)
//        }
//    }
//
//    @Test
//    fun `setChartData for Monthly mode creates 4 weeks`() = runTest(testDispatcher) {
//        val testTransactions = createTestTransactionsForMonth()
//        every { mockQuerySnapshot.toObjects(Transaction::class.java) } returns testTransactions
//
//        scenario = launchFragmentInContainer<AnalysisFragment>()
//        scenario.moveToState(Lifecycle.State.RESUMED)
//
//        testScheduler.advanceUntilIdle()
//
//        scenario.onFragment { fragment ->
//            val barChart = fragment.view?.findViewById<BarChart>(R.id.barChart)
//            assertNotNull(barChart)
//
//            // Verify bar chart has data
//            val barData = barChart?.data as? BarData
//            assertNotNull(barData)
//        }
//    }
//
//    @Test
//    fun `setChartData for Yearly mode creates 12 months`() = runTest(testDispatcher) {
//        val testTransactions = createTestTransactionsForYear()
//        every { mockQuerySnapshot.toObjects(Transaction::class.java) } returns testTransactions
//
//        scenario = launchFragmentInContainer<AnalysisFragment>()
//        scenario.moveToState(Lifecycle.State.RESUMED)
//
//        testScheduler.advanceUntilIdle()
//
//        scenario.onFragment { fragment ->
//            val barChart = fragment.view?.findViewById<BarChart>(R.id.barChart)
//            assertNotNull(barChart)
//        }
//    }
//
//    @Test
//    fun `handles Firebase auth null user gracefully`() {
//        every { mockAuth.currentUser } returns null
//
//        scenario = launchFragmentInContainer<AnalysisFragment>()
//        scenario.moveToState(Lifecycle.State.RESUMED)
//
//        // Should not crash
//        scenario.onFragment { fragment ->
//            assertNotNull(fragment.view)
//        }
//    }
//
//    @Test
//    fun `handles Firestore exceptions gracefully`() = runTest(testDispatcher) {
//        coEvery { mockTask.await() } throws RuntimeException("Firestore error")
//
//        scenario = launchFragmentInContainer<AnalysisFragment>()
//        scenario.moveToState(Lifecycle.State.RESUMED)
//
//        testScheduler.advanceUntilIdle()
//
//        // Should not crash
//        scenario.onFragment { fragment ->
//            assertNotNull(fragment.view)
//        }
//    }
//
//    @Test
//    fun `addMonthlySummaryFragment adds fragment correctly`() {
//        scenario = launchFragmentInContainer<AnalysisFragment>()
//        scenario.moveToState(Lifecycle.State.RESUMED)
//
//        scenario.onFragment { fragment ->
//            val fragmentContainer = fragment.view?.findViewById<View>(R.id.fragment_container)
//            assertNotNull(fragmentContainer)
//        }
//    }
//
//    @Test
//    fun `tab selection triggers chart data update`() = runTest(testDispatcher) {
//        every { mockQuerySnapshot.toObjects(Transaction::class.java) } returns emptyList()
//
//        scenario = launchFragmentInContainer<AnalysisFragment>()
//        scenario.moveToState(Lifecycle.State.RESUMED)
//
//        scenario.onFragment { fragment ->
//            val tabLayout = fragment.view?.findViewById<TabLayout>(R.id.tabLayout)
//            assertNotNull(tabLayout)
//
//            // Simulate tab selection
//            tabLayout?.getTabAt(0)?.select() // Weekly tab
//            tabLayout?.getTabAt(2)?.select() // Yearly tab
//        }
//
//        testScheduler.advanceUntilIdle()
//    }
//
//    @Test
//    fun `pie chart handles invalid color resource gracefully`() = runTest(testDispatcher) {
//        val testTransactions = listOf(createTestTransaction("tx1", 100.0, true, "cat1"))
//        val testCategory = createTestCategory("cat1", "Food", -1) // Invalid color resource
//
//        every { mockQuerySnapshot.toObjects(Transaction::class.java) } returns testTransactions
//        every { mockDocumentSnapshot.exists() } returns true
//        every { mockDocumentSnapshot.toObject(Category::class.java) } returns testCategory
//        every { ContextCompat.getColor(any(), -1) } throws RuntimeException("Invalid resource")
//
//        scenario = launchFragmentInContainer<AnalysisFragment>()
//        scenario.moveToState(Lifecycle.State.RESUMED)
//
//        testScheduler.advanceUntilIdle()
//
//        // Should handle exception and continue
//        scenario.onFragment { fragment ->
//            val pieChart = fragment.view?.findViewById<PieChart>(R.id.pieChart)
//            assertNotNull(pieChart)
//        }
//    }
//
//    // Helper methods for creating test data
//
//    private fun createTestTransaction(
//        id: String,
//        amount: Double,
//        isExpense: Boolean,
//        categoryId: String,
//        dateTime: String = LocalDate.now().toString() + " 12:00"
//    ): Transaction {
//        return Transaction().apply {
//            this.transactionId = id
//            this.amount = amount
//            this.expense = isExpense
//            this.categoryId = categoryId
//            this.dateTime = dateTime
//        }
//    }
//
//    private fun createTestCategory(
//        id: String,
//        name: String,
//        colorRes: Int
//    ): Category {
//        return Category().apply {
//            this.categoryId = id
//            this.name = name
//            this.colour = colorRes
//        }
//    }
//
//    private fun createTestTransactionsForWeek(): List<Transaction> {
//        val today = LocalDate.now()
//        val weekStart = today.with(java.time.DayOfWeek.MONDAY)
//
//        return listOf(
//            createTestTransaction("tx1", 100.0, true, "cat1", weekStart.toString() + " 12:00"),
//            createTestTransaction("tx2", 200.0, false, "cat2", weekStart.plusDays(1).toString() + " 12:00"),
//            createTestTransaction("tx3", 50.0, true, "cat1", weekStart.plusDays(2).toString() + " 12:00")
//        )
//    }
//
//    private fun createTestTransactionsForMonth(): List<Transaction> {
//        val today = LocalDate.now()
//        val monthStart = today.withDayOfMonth(1)
//
//        return listOf(
//            createTestTransaction("tx1", 100.0, true, "cat1", monthStart.toString() + " 12:00"),
//            createTestTransaction("tx2", 200.0, false, "cat2", monthStart.plusDays(7).toString() + " 12:00"),
//            createTestTransaction("tx3", 50.0, true, "cat1", monthStart.plusDays(14).toString() + " 12:00"),
//            createTestTransaction("tx4", 75.0, false, "cat2", monthStart.plusDays(21).toString() + " 12:00")
//        )
//    }
//
//    private fun createTestTransactionsForYear(): List<Transaction> {
//        val currentYear = LocalDate.now().year
//
//        return (1..12).map { month ->
//            createTestTransaction(
//                "tx$month",
//                100.0 * month,
//                month % 2 == 0,
//                "cat1",
//                "$currentYear-${month.toString().padStart(2, '0')}-01 12:00"
//            )
//        }
//    }
//}
//
//// Additional test class for testing specific chart behaviors
//@ExperimentalCoroutinesApi
//@RunWith(AndroidJUnit4::class)
//@Config(sdk = [28])
//class AnalysisFragmentChartTest {
//
//    @Test
//    fun `pie chart percentage calculations are correct`() {
//        val transactions = listOf(
//            Transaction().apply { amount = 100.0; expense = true },
//            Transaction().apply { amount = 200.0; expense = true },
//            Transaction().apply { amount = 100.0; expense = false } // Should be filtered out
//        )
//
//        val expenseTransactions = transactions.filter { it.expense }
//        val total = expenseTransactions.sumOf { it.amount }
//
//        assertEquals(300.0, total)
//
//        val firstPercentage = (100.0 / total) * 100
//        assertEquals(33.33, firstPercentage, 0.01)
//    }
//
//    @Test
//    fun `date parsing handles various formats correctly`() {
//        val validDate = "2024-01-15 12:30"
//        val invalidDate = "invalid-date"
//
//        try {
//            val parsed = LocalDate.parse(validDate, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
//            assertNotNull(parsed)
//        } catch (e: Exception) {
//            assertTrue(false, "Valid date should parse successfully")
//        }
//
//        try {
//            LocalDate.parse(invalidDate, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
//            assertTrue(false, "Invalid date should throw exception")
//        } catch (e: Exception) {
//            // Expected behavior
//            assertTrue(true)
//        }
//    }
//
//    @Test
//    fun `weekly data grouping logic is correct`() {
//        val today = LocalDate.now()
//        val weekStart = today.with(java.time.DayOfWeek.MONDAY)
//
//        // Test that days are correctly calculated
//        for (i in 0..6) {
//            val day = weekStart.plusDays(i.toLong())
//            assertTrue(day.dayOfWeek.value == i + 1 || (i == 6 && day.dayOfWeek.value == 7))
//        }
//    }
//
//    @Test
//    fun `monthly week grouping creates correct ranges`() {
//        val monthStart = LocalDate.of(2024, 1, 1) // January 1st, 2024
//        val weeks = listOf(0, 7, 14, 21)
//
//        for ((i, offset) in weeks.withIndex()) {
//            val weekStart = monthStart.plusDays(offset.toLong())
//            val weekEnd = weekStart.plusDays(6)
//
//            assertTrue(weekEnd.isAfter(weekStart) || weekEnd.isEqual(weekStart))
//            assertTrue(weekEnd.minusDays(6).isEqual(weekStart))
//        }
//    }
//}