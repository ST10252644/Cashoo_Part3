package com.iie.st10320489.marene.ui.filter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Category
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
                putString("userId", userId) // Add this
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
                var query: com.google.firebase.firestore.Query = firestore.collection("users")
                    .document(userId)
                    .collection("transactions")

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

                if (transactions.isEmpty()) {
                    adapter.updateTransactions(emptyList())

                    // Show toast if no transactions found
                    Toast.makeText(
                        requireContext(),
                        "No transactions found in this category",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@launch
                }

                val categoryIds = transactions.map { it.categoryId }.toSet()

                val categorySnapshot = firestore.collection("users")
                    .document(userId)
                    .collection("categories")
                    .whereIn(FieldPath.documentId(), categoryIds.toList())
                    .get()
                    .await()

                val categoryMap = categorySnapshot.documents.associateBy(
                    { it.id },
                    { it.toObject(Category::class.java) ?: Category(categoryId = "Other", name = "Other") }
                )

                val transactionsWithCategory = transactions.map { transaction ->
                    val category = categoryMap[transaction.categoryId] ?: Category(categoryId = "Other", name = "Other")
                    TransactionWithCategory(transaction, category)
                }

                adapter.updateTransactions(transactionsWithCategory)

            } catch (e: Exception) {
                Log.e(TAG, "Error loading filtered transactions", e)
            }
        }
    } //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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


