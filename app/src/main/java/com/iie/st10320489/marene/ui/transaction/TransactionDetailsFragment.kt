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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionId = arguments?.getString("transactionId") ?: ""
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
        firestore.collection("transactions").document(transactionId)
            .get()
            .addOnSuccessListener { document ->
                val transaction = document.toObject(Transaction::class.java)
                transaction?.let { t ->
                    binding.txtName.text = t.name
                    binding.txtAmount.text = if (t.expense) "-R${t.amount}" else "+R${t.amount}"
                    binding.txtDateTime.text = t.dateTime
                    binding.txtMethod.text = t.transactionMethod
                    binding.txtLocation.text = t.location ?: "N/A"
                    binding.txtDescription.text = t.description.ifBlank { "N/A" }

                    // Placeholder until you fetch category details
                    binding.txtCategory.text = t.categoryId
                    binding.txtSubCategory.text = t.subCategoryId ?: "N/A"

                    if (t.photo.isNotEmpty()) {
                        Glide.with(this).load(t.photo).into(binding.imgTransactionPhoto)
                    } else {
                        binding.imgTransactionPhoto.setImageResource(R.drawable.receipt)
                    }
                }
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


//Reference List:
// Android Developers. 2025. Accessing data using Room DAOs. [online]. Available at: https://developer.android.com/training/data-storage/room/accessing-data [Accessed on 15 April 2025]
//Android Developers. 2025. Fragment lifecycle. [online]. Available at: https://developer.android.com/guide/fragments/lifecycle [Accessed on 12 April 2025]
//Android Knowledge. 2024. ViewModel in Android Studio using Kotlin | Android Knowledge. [video online]. Available at: https://www.youtube.com/watch?v=v32hSKtlH9A [Accessed on 11 April 2025]
//Code With Cal. 2025. Room Database Android Studio Kotlin Example Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=-LNg-K7SncM [Accessed on 12 April 2025]