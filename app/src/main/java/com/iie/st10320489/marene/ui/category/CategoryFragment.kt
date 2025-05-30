package com.iie.st10320489.marene.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.databinding.FragmentCategoryBinding
import com.iie.st10320489.marene.graphs.MonthlySummaryFragment

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CategoryViewModel
    private lateinit var adapter: CategoryAdapter
    private var userId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel = ViewModelProvider(this)[CategoryViewModel::class.java]

        binding.recyclerViewCategories.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = CategoryAdapter(emptyList()) { category ->

            val bundle = Bundle().apply {
                putString("userId", userId)
                putString("categoryId", category.categoryId) // ✅ This is needed
                putString("categoryName", category.name)     // Optional
            }


            if (category.name == "Other") {
                findNavController().navigate(R.id.action_categoryFragment_to_subcategoryFragment, bundle)
            } else {
                findNavController().navigate(R.id.action_categoryFragment_to_filterFragment, bundle)
            }
        }
        binding.recyclerViewCategories.adapter = adapter

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userId = currentUser.uid
            viewModel.getCategoriesByUser(userId)

            viewModel.categories.observe(viewLifecycleOwner) { categories ->
                adapter.updateCategories(categories ?: emptyList())
            }
            addMonthlySummaryFragment()
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
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
//(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)

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
