package com.iie.st10320489.marene.ui.filter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Category
import com.iie.st10320489.marene.data.entities.Transaction
import com.iie.st10320489.marene.data.entities.TransactionWithCategory
import com.iie.st10320489.marene.databinding.FragmentFilterBinding
import com.iie.st10320489.marene.ui.transaction.TransactionAdapter

class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TransactionAdapter
    private var userId: String? = null
    private var categoryName: String? = null
    private var subCategoryName: String? = null

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            categoryName = it.getString("categoryName")
            subCategoryName = it.getString("subCategoryName")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        val root = binding.root

        adapter = TransactionAdapter(emptyList()) { transactionWithCategory ->
            val bundle = Bundle().apply {
                putString("transactionId", transactionWithCategory.transaction.transactionId)
            }
            // Adjust navigation action ID and destination as needed
            findNavController().navigate(R.id.action_transactionFragment_to_transactionDetailsFragment, bundle)
        }

        binding.recyclerViewFilterTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFilterTransactions.adapter = adapter

        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val currentUserEmail = sharedPreferences.getString("currentUserEmail", null)
        userId = sharedPreferences.getString("currentUserId", null)

        if (!userId.isNullOrEmpty() && currentUserEmail != null) {
            loadFilteredTransactions(userId!!)
        }

        return root
    }

    private fun loadFilteredTransactions(userId: String) {
        var query = firestore.collection("transactions")
            .whereEqualTo("userId", userId)

        categoryName?.let {
            query = query.whereEqualTo("categoryName", it)
        }

        subCategoryName?.let {
            query = query.whereEqualTo("subCategoryName", it)
        }

        query.get().addOnSuccessListener { documents ->
            val transactionList = documents.mapNotNull { it.toObject(Transaction::class.java) }
            val categoryIds = transactionList.map { it.categoryId }.toSet()

            if (categoryIds.isEmpty()) {
                adapter.updateTransactions(emptyList())
                return@addOnSuccessListener
            }

            firestore.collection("categories")
                .whereIn("categoryId", categoryIds.toList())
                .get()
                .addOnSuccessListener { categoryDocs ->
                    val categoryMap = categoryDocs.associateBy { it.getString("categoryId") }
                    val transactionsWithCategories = transactionList.mapNotNull { transaction ->
                        val categoryDoc = categoryMap[transaction.categoryId]
                        val category = categoryDoc?.toObject(Category::class.java)
                        if (category != null) {
                            TransactionWithCategory(transaction, category)
                        } else {
                            null
                        }
                    }
                    adapter.updateTransactions(transactionsWithCategories)
                }
                .addOnFailureListener {
                    adapter.updateTransactions(emptyList())
                }
        }.addOnFailureListener {
            adapter.updateTransactions(emptyList())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
