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

}
