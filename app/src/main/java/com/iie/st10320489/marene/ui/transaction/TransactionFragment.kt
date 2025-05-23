package com.iie.st10320489.marene.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Category
import com.iie.st10320489.marene.data.entities.Transaction
import com.iie.st10320489.marene.data.entities.TransactionWithCategory
import com.iie.st10320489.marene.databinding.FragmentTransactionBinding

class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TransactionAdapter
    private lateinit var firestore: FirebaseFirestore
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString("userId") ?: ""
        }
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TransactionAdapter(emptyList()) { transactionWithCategory ->
            val bundle = Bundle().apply {
                putString("transactionId", transactionWithCategory.transaction.transactionId)
            }
            findNavController().navigate(R.id.action_transactionFragment_to_transactionDetailsFragment, bundle)
        }


        binding.recyclerViewTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewTransactions.adapter = adapter

        loadTransactions()
    }

    private fun loadTransactions() {
        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val transactions = result.mapNotNull { doc ->
                    doc.toObject<Transaction>().apply { transactionId = doc.id }
                }

                // Now fetch categories and combine
                firestore.collection("categories")
                    .get()
                    .addOnSuccessListener { categoryResult ->
                        val categoryMap = categoryResult.associateBy(
                            { it.id },                      // category ID
                            { it.toObject<Category>() }     // full Category object
                        )


                        val transactionsWithCategory = transactions.map { transaction ->
                            val category = categoryMap[transaction.categoryId] ?: Category(categoryId = "unknown", categoryName = "Unknown")
                            TransactionWithCategory(transaction, category)
                        }


                        adapter.updateTransactions(transactionsWithCategory)
                    }
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


// (Android Developers, 2025)

    //Reference List:
// Android Developers. 2025. Accessing data using Room DAOs. [online]. Available at: https://developer.android.com/training/data-storage/room/accessing-data [Accessed on 15 April 2025]
//Android Developers. 2025. Fragment lifecycle. [online]. Available at: https://developer.android.com/guide/fragments/lifecycle [Accessed on 12 April 2025]
//Android Knowledge. 2024. ViewModel in Android Studio using Kotlin | Android Knowledge. [video online]. Available at: https://www.youtube.com/watch?v=v32hSKtlH9A [Accessed on 11 April 2025]
//Code With Cal. 2025. Room Database Android Studio Kotlin Example Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=-LNg-K7SncM [Accessed on 12 April 2025]
