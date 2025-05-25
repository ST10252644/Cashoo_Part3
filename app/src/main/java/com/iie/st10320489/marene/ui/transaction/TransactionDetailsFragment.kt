package com.iie.st10320489.marene.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Transaction
import com.iie.st10320489.marene.databinding.FragmentTransactionDetailsBinding

class TransactionDetailsFragment : Fragment() {

    private var _binding: FragmentTransactionDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private var transactionId: String = ""
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            transactionId = it.getString("transactionId") ?: ""
            userId = it.getString("userId") ?: ""
        }
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTransactionDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTransactionDetails()
    }

    private fun loadTransactionDetails() {
        firestore.collection("users")
            .document(userId)
            .collection("transactions")
            .document(transactionId)
            .get()
            .addOnSuccessListener { document ->
                val transaction = document.toObject(Transaction::class.java)
                transaction?.let { t ->
                    binding.txtName.text = t.name
                    binding.txtAmount.text = if (t.expense) "-R${t.amount}" else "+R${t.amount}"
                    binding.txtDateTime.text = t.dateTime
                    binding.txtMethod.text = t.transactionMethod
                    binding.txtLocation.text = t.location
                    binding.txtDescription.text = t.description.ifBlank { "N/A" }

                    // Fetch category name from Firestore using categoryId
                    val categoryId = t.categoryId ?: "Other"
                    firestore.collection("users")
                        .document(userId)
                        .collection("categories")
                        .document(categoryId)
                        .get()
                        .addOnSuccessListener { categoryDoc ->
                            val categoryName = categoryDoc.getString("name") ?: "Unknown"
                            binding.txtCategory.text = categoryName
                        }
                        .addOnFailureListener {
                            binding.txtCategory.text = "Unknown"
                        }

                    binding.txtSubCategory.text = t.subCategoryId ?: "N/A"

                    // Display image only if photo is not empty
                    if (!t.photo.isNullOrEmpty()) {
                        binding.imgTransactionPhoto.visibility = View.VISIBLE
                        Glide.with(this).load(t.photo).into(binding.imgTransactionPhoto)
                    } else {
                        binding.imgTransactionPhoto.visibility = View.GONE
                    }
                }
            }
            .addOnFailureListener {
                // You may want to show a Toast or error here
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
