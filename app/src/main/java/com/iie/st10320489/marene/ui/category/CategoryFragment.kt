package com.iie.st10320489.marene.ui.category

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.databinding.FragmentCategoryBinding
import com.iie.st10320489.marene.graphs.MonthlySummaryFragment
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CategoryViewModel
    private lateinit var adapter: CategoryAdapter
    private var userId: Int = 0 // Could be changed to String if you prefer Firebase userId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel = ViewModelProvider(this)[CategoryViewModel::class.java]

        binding.recyclerViewCategories.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = CategoryAdapter(emptyList()) { category ->

            val bundle = Bundle().apply {
                putInt("userId", userId)
                putString("categoryName", category.categoryName)
            }

            if (category.categoryName == "Other") {
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

        val sharedPref = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val currentUserEmail = sharedPref.getString("currentUserEmail", null)

        if (currentUserEmail != null) {
            // Here you might fetch userId from Firebase Authentication or Firestore users collection
            // For now, let's pretend we have a userId stored in SharedPreferences for demo:
            userId = sharedPref.getInt("currentUserId", 0) // Or fetch from Firebase

            if (userId != 0) {
                viewModel.getCategoriesByUser(userId)
            }

            viewModel.categories.observe(viewLifecycleOwner) { categories ->
                adapter.updateCategories(categories ?: emptyList())
            }

            addMonthlySummaryFragment()
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
//((Cal, 2023), (College, 2025)

//Bibliography
//AndroidDevelopers, 2024. Save data in a local database using Room. [Online] Available at: hRps://developer.android.com/training/data-storage/room [Accessed 27 April 2025].
//AndroidDevelopers, 2024. Write asynchronous DAO queries. [Online]
//Available at: hRps://developer.android.com/training/data-storage/room/async- queries?authuser=2
//[Accessed 26 April 2025].
//Raikwar, A., 2024. Ge=ng Started with Room Database in Android. [Online]
//Available at: hRps://amitraikwar.medium.com/ge[ng-started-with-room-database-in- android-fa1ca23ce21e
//[Accessed 27 April 2025].
//Raikwar, A., 2023. Ge=ng Started with Room Database in Android. [Online]
//Available at: hRps://developer.android.com/develop#core-areas
//[Accessed 28 April 2025].
//Cal, C. W., 2023. Room Database Android Studio Kotlin Example Tutorial. [Online] Available at: hRps://youtu.be/-LNg-K7SncM?si=y8cbMdvhhp48Pp9-
//[Accessed 27 April 2025].
//College, I. V., 2025. PROG7313 Module-Manual / Module-Outline. Pretoria: Varsity College Pretoria.
//Viegen, F. v., 2022. A PracKcal introducKon to Android Room-3 : EnKty, Dao and Database objects.. [Online]
//Available at: hRps://youtu.be/RstQg7f4Edk?si=8RoAGp-OKPpMNVdY
//[Accessed 28 April 2025].

//androidbyexample, 2024. EnKKes ,Dao and Database -Android By Example. [Online] Available at: hRps://androidbyexample.com/modules/movie-db/STEP-050_Repo.html [Accessed 25 April 2025].
//AndroidDevelopers, 2023. Layouts in Views. [Online]
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
