package com.iie.st10320489.marene.ui.transaction

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FieldPath
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
        Log.d("TransactionFragment", "Loading transactions for userId: $userId")

        // Correctly query the user's subcollection of transactions
        firestore.collection("users")
            .document(userId)
            .collection("transactions")
            .get()
            .addOnSuccessListener { result ->
                Log.d("TransactionFragment", "Transaction query success. Documents found: ${result.size()}")

                val transactions = result.mapNotNull { doc ->
                    val transaction = doc.toObject(Transaction::class.java)
                    transaction.transactionId = doc.id
                    Log.d("TransactionFragment", "Loaded transaction: ${transaction.transactionId}, categoryId: ${transaction.categoryId}")
                    transaction
                }

                val categoryIds = transactions.map { it.categoryId }.toSet()
                Log.d("TransactionFragment", "Unique categoryIds: $categoryIds")

                if (categoryIds.isEmpty()) {
                    Log.d("TransactionFragment", "No category IDs found. Using fallback 'Other' category.")
                    val fallback = transactions.map {
                        val otherCategory = Category(categoryId = "Other", name = "Other")
                        TransactionWithCategory(it, otherCategory)
                    }
                    adapter.updateTransactions(fallback)
                    return@addOnSuccessListener
                }

                firestore.collection("users")
                    .document(userId)
                    .collection("categories")
                    .whereIn(FieldPath.documentId(), categoryIds.toList())
                    .get()
                    .addOnSuccessListener { categoryResult ->
                        Log.d("TransactionFragment", "Categories query success. Categories found: ${categoryResult.size()}")

                        val categoryMap = categoryResult.associateBy(
                            { it.id },
                            {
                                val cat = it.toObject(Category::class.java)
                                Log.d("TransactionFragment", "Loaded category: ${cat.categoryId}, name: ${cat.name}")
                                cat
                            }
                        )

                        val transactionsWithCategory = transactions.map { transaction ->
                            val category = categoryMap[transaction.categoryId]
                                ?: Category(categoryId = "Other", name = "Other")
                            TransactionWithCategory(transaction, category)
                        }

                        Log.d("TransactionFragment", "Transactions with categories: ${transactionsWithCategory.size}")
                        adapter.updateTransactions(transactionsWithCategory)
                    }
                    .addOnFailureListener { e ->
                        Log.e("TransactionFragment", "Failed to load categories", e)
                        Toast.makeText(requireContext(), "Failed to load categories: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("TransactionFragment", "Failed to load transactions", e)
                Toast.makeText(requireContext(), "Failed to load transactions: ${e.message}", Toast.LENGTH_SHORT).show()
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
