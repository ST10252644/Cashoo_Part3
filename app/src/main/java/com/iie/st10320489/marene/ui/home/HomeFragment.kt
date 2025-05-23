package com.iie.st10320489.marene.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.tasks.await
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.iie.st10320489.marene.data.entities.Transaction

import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Category
import com.iie.st10320489.marene.data.entities.TransactionWithCategory
import com.iie.st10320489.marene.databinding.FragmentHomeBinding
import com.iie.st10320489.marene.graphs.MonthlySummaryFragment
import com.iie.st10320489.marene.ui.transaction.TransactionAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TransactionAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = auth.currentUser?.uid

        if (userId != null) {
            profileButton()
            setupButton()
            loadUserSettings()
            setupRecyclerView()
            loadTop5Transactions()
            addMonthlySummaryFragment()
        } else {
            // Handle user not logged in
            Log.e("HomeFragment", "User not logged in")
        }
    }

    private fun setupButton() {
        binding.seeMoreTransactions.setOnClickListener {
            val bundle = Bundle().apply {
                putString("userId", userId)
            }
            findNavController().navigate(
                R.id.action_homeFragment_to_transactionFragment, bundle
            )
        }
    }

    private fun profileButton() {
        binding.profileImageView.setOnClickListener {
            val bundle = Bundle().apply {
                putString("userId", userId)
            }
            findNavController().navigate(
                R.id.action_homeFragment_to_settingsFragment, bundle
            )
        }
    }

    private fun loadUserSettings() {
        userId?.let { uid ->
            db.collection("userSettings").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val minGoal = document.getDouble("minGoal") ?: 0.0
                        val maxGoal = document.getDouble("maxGoal") ?: 0.0
                        val chinchilla = document.getString("chinchilla") ?: "default_chinchilla"

                        binding.minGoalTextView.text = "R ${"%,.0f".format(minGoal)}"
                        binding.maxGoalTextView.text = "R ${"%,.0f".format(maxGoal)}"

                        val chinchillaResId = resources.getIdentifier(
                            chinchilla, "drawable", requireContext().packageName
                        )
                        binding.profileImageView.setImageResource(chinchillaResId)
                        binding.greenCardImageView.setImageResource(chinchillaResId)
                    } else {
                        Log.d("HomeFragment", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("HomeFragment", "get failed with ", exception)
                }
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(emptyList()) { transactionWithCategory ->
            // Optional: handle click on transaction item
        }

        binding.recyclerRecentTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRecentTransactions.adapter = adapter
    }

    private fun loadTop5Transactions() {
        userId?.let { uid ->
            db.collection("transactions")
                .whereEqualTo("userId", uid)
                .orderBy("dateTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener { documents ->
                    val transactions = documents.mapNotNull { it.toObject(Transaction::class.java) }

                    val transactionWithCategories = mutableListOf<TransactionWithCategory>()

                    // Coroutine scope for parallel loading
                    lifecycleScope.launch {
                        for (transaction in transactions) {
                            val categorySnapshot = db.collection("categories")
                                .document(transaction.categoryId)
                                .get()
                                .await()

                            val category = categorySnapshot.toObject(Category::class.java)
                            if (category != null) {
                                transactionWithCategories.add(TransactionWithCategory(transaction, category))
                            }
                        }

                        adapter = TransactionAdapter(transactionWithCategories) {
                            // Handle transaction click if needed
                        }
                        binding.recyclerRecentTransactions.adapter = adapter
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("HomeFragment", "Error getting transactions: ", exception)
                }
        }
    }



    private fun addMonthlySummaryFragment() {
        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragment = MonthlySummaryFragment()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
