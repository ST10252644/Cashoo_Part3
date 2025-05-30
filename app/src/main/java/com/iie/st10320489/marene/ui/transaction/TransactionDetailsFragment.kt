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

    //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            transactionId = it.getString("transactionId") ?: ""
            userId = it.getString("userId") ?: ""
        }
        firestore = FirebaseFirestore.getInstance()
    }
    //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
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
    //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
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

            }
    }


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