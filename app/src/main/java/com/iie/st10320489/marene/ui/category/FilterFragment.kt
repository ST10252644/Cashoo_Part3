package com.iie.st10320489.marene.ui.filter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Category
import com.iie.st10320489.marene.data.entities.SubCategory
import com.iie.st10320489.marene.data.entities.Transaction
import com.iie.st10320489.marene.data.entities.TransactionWithCategory
import com.iie.st10320489.marene.databinding.FragmentFilterBinding
import com.iie.st10320489.marene.ui.transaction.TransactionAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TransactionAdapter
    private var userId: String? = null
    private var categoryId: String? = null
    private var subCategoryId: String? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "FilterFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get arguments passed to this fragment (categoryId and subCategoryId)
        arguments?.let {
            categoryId = it.getString("categoryId")
            subCategoryId = it.getString("subCategoryId")
        }

        Log.d(TAG, "Received filter - categoryId: $categoryId, subCategoryId: $subCategoryId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)

        // Initialize adapter with empty list and click listener
        adapter = TransactionAdapter(emptyList()) { transactionWithCategory ->
            val bundle = Bundle().apply {
                putString("transactionId", transactionWithCategory.transaction.transactionId)
            }
            findNavController().navigate(R.id.action_transactionFragment_to_transactionDetailsFragment, bundle)
        }

        // Setup RecyclerView
        binding.recyclerViewFilterTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFilterTransactions.adapter = adapter

        // Get current user ID
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        userId = firebaseUser?.uid
        Log.d(TAG, "Current Firebase userId: $userId")

        if (!userId.isNullOrEmpty()) {
            // Load transactions filtered by category and subcategory (if set)
            loadFilteredTransactions(userId!!)
        } else {
            Log.w(TAG, "UserId is null or empty. Cannot load transactions.")
        }

        return binding.root
    }

    private fun loadFilteredTransactions(userId: String) {
        lifecycleScope.launch {
            try {
                var query = firestore.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .whereEqualTo("userId", userId)

                categoryId?.let {
                    query = query.whereEqualTo("categoryId", it)
                }

                subCategoryId?.let {
                    query = query.whereEqualTo("subCategoryId", it)
                }

                val snapshot = query.get().await()
                val transactions = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Transaction::class.java)?.apply { transactionId = doc.id }
                }

                val transactionWithCategoryList = transactions.map { transaction ->
                    val catSnap = firestore.collection("users")
                        .document(userId)
                        .collection("categories")
                        .document(transaction.categoryId)
                        .get()
                        .await()

                    val category = if (catSnap.exists()) {
                        catSnap.toObject(Category::class.java) ?: Category(categoryId = "unknown", name = "Unknown")
                    } else {
                        Category(categoryId = "unknown", name = "Unknown")
                    }

                    TransactionWithCategory(transaction, category)
                }

                adapter.updateTransactions(transactionWithCategoryList)

            } catch (e: Exception) {
                Log.e(TAG, "Error loading transactions", e)
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




