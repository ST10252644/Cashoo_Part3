//// TransactionAdapterTest.kt
//package com.iie.st10320489.marene.ui.transaction
//
//import android.content.Context
//import android.view.LayoutInflater
//import androidx.test.core.app.ApplicationProvider
//import com.google.common.truth.Truth.assertThat
//import com.iie.st10320489.marene.R
//import com.iie.st10320489.marene.data.entities.Category
//import com.iie.st10320489.marene.data.entities.Transaction
//import com.iie.st10320489.marene.data.entities.TransactionWithCategory
//import com.iie.st10320489.marene.databinding.ItemTransactionBinding
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.robolectric.RobolectricTestRunner
////import app.src.main.AndroidManifest.xml
//
//@RunWith(RobolectricTestRunner::class)
//class TransactionAdapterTest {
//
//    private lateinit var context: Context
//
//    @Before
//    fun setup() {
//        context = ApplicationProvider.getApplicationContext()
//    }
//
//    @Test
//    fun testBindTransactionDataDisplaysCorrectly() {
//        val inflater = LayoutInflater.from(context)
//        val binding = ItemTransactionBinding.inflate(inflater)
//
//        val transaction = Transaction(
//            name = "Coffee",
//            amount = 5.0,
//            expense = true,
//            categoryId = "food"
//        )
//        val category = Category(
//            categoryId = "food",
//            name = "Food",
//            colour = R.color.outcome
//        )
//        val item = TransactionWithCategory(transaction, category)
//
//        val adapter = TransactionAdapter(listOf(item)) { }
//        val viewHolder = adapter.TransactionViewHolder(binding)
//
//        // Bind the transaction data
//        viewHolder.bind(item)
//
//        // Verify the data is displayed correctly
//        assertThat(binding.txtTransactionName.text.toString()).isEqualTo("Coffee")
//        assertThat(binding.txtTransactionAmount.text.toString()).isEqualTo("-R5.00")
//    }
//
//    @Test
//    fun testBindIncomeTransactionDisplaysCorrectly() {
//        val inflater = LayoutInflater.from(context)
//        val binding = ItemTransactionBinding.inflate(inflater)
//
//        val transaction = Transaction(
//            name = "Salary",
//            amount = 1000.0,
//            expense = false,  // This is income
//            categoryId = "income"
//        )
//        val category = Category(
//            categoryId = "income",
//            name = "Income",
//            colour = R.color.income
//        )
//        val item = TransactionWithCategory(transaction, category)
//
//        val adapter = TransactionAdapter(listOf(item)) { }
//        val viewHolder = adapter.TransactionViewHolder(binding)
//
//        // Bind the transaction data
//        viewHolder.bind(item)
//
//        // Verify income displays without minus sign
//        assertThat(binding.txtTransactionName.text.toString()).isEqualTo("Salary")
//        assertThat(binding.txtTransactionAmount.text.toString()).isEqualTo("R1000.00")
//    }
//}