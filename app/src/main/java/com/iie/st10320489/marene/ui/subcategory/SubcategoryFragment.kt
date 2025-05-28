package com.iie.st10320489.marene.ui.subcategory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.SubCategory
import com.iie.st10320489.marene.databinding.FragmentSubcategoryBinding

class SubcategoryFragment : Fragment() {

    private var _binding: FragmentSubcategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SubcategoryAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubcategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = SubcategoryAdapter(mutableListOf()) { selectedSubcategory ->
            val bundle = Bundle().apply {
                putString("categoryName", selectedSubcategory.name)
                putString("subCategoryName", selectedSubcategory.name)
                putString("subCategoryId", selectedSubcategory.subCategoryId) // from parent
            }
            findNavController().navigate(R.id.action_subcategoryFragment_to_filterFragment, bundle)
        }

        binding.recyclerViewSubcategories.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewSubcategories.adapter = adapter

        loadSubcategories()
    }

    private var subcategoriesListener: ListenerRegistration? = null

    fun loadSubcategories() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            subcategoriesListener?.remove() // Remove old listener if any

            subcategoriesListener = firestore.collection("users")
                .document(currentUser.uid)
                .collection("categories")
                .document("Other")
                .collection("subcategories")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Toast.makeText(context, "Listen failed: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val subcategories = snapshot.documents.mapNotNull { it.toObject(SubCategory::class.java) }
                        adapter.updateList(subcategories)
                    } else {
                        adapter.updateList(emptyList())
                    }
                }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        subcategoriesListener?.remove()
        _binding = null
    }

} //(Firebase, 2023),(Technology, 2020),(GeeksforGeeks, 2024)

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