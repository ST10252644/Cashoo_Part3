// HomeFragment.kt
package com.iie.st10320489.marene.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Category
import com.iie.st10320489.marene.data.entities.Transaction
import com.iie.st10320489.marene.data.entities.TransactionWithCategory
import com.iie.st10320489.marene.databinding.FragmentHomeBinding
import com.iie.st10320489.marene.graphs.MonthlySummaryFragment
import com.iie.st10320489.marene.ui.transaction.TransactionAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TransactionAdapter
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var userId: String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = homeViewModel.userId

        if (userId != null) {
            profileButton()
            setupButton()
            setupRecyclerView()
            observeViewModel()
            homeViewModel.loadUserSettings()
            homeViewModel.loadTop5Transactions()
            addMonthlySummaryFragment()
        } else {
            Log.e("HomeFragment", "User not logged in")
        }
    }

    private fun observeViewModel() {
        homeViewModel.minGoal.observe(viewLifecycleOwner) { min ->
            binding.minGoalTextView.text = "R ${"%,.0f".format(min)}"
        }

        homeViewModel.maxGoal.observe(viewLifecycleOwner) { max ->
            binding.maxGoalTextView.text = "R ${"%,.0f".format(max)}"
        }

        homeViewModel.chinchilla.observe(viewLifecycleOwner) { chinchilla ->
            val resId = resources.getIdentifier(chinchilla, "drawable", requireContext().packageName)
            binding.profileImageView.setImageResource(resId)
            binding.greenCardImageView.setImageResource(resId)
        }

        homeViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter = TransactionAdapter(transactions) {}
            binding.recyclerRecentTransactions.adapter = adapter
        }

        homeViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.loadingOverlay.visibility = if (loading) View.VISIBLE else View.GONE
        }


    }


    private fun setupButton() {
        binding.seeMoreTransactions.setOnClickListener {
            val bundle = Bundle().apply { putString("userId", userId) }
            findNavController().navigate(R.id.action_homeFragment_to_transactionFragment, bundle)
        }
    }

    private fun profileButton() {
        binding.profileImageView.setOnClickListener {
            val bundle = Bundle().apply { putString("userId", userId) }
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment, bundle)
        }
    }


    private fun setupRecyclerView() {
        adapter = TransactionAdapter(emptyList()) {}
        binding.recyclerRecentTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRecentTransactions.adapter = adapter
    }


    private fun addMonthlySummaryFragment() {
        val fragment = childFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MonthlySummaryFragment())
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
