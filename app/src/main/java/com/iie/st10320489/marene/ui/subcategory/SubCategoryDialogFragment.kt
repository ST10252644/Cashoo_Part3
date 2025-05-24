package com.iie.st10320489.marene.ui.subcategory

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.SubCategory
import java.util.UUID

class SubCategoryDialogFragment : DialogFragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_subcategory, null)
        val editTextSubcategoryName = view.findViewById<EditText>(R.id.editTextSubcategoryName)

        sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        builder.setView(view)
            .setTitle("Add Subcategory")
            .setPositiveButton("Add") { _, _ ->
                val subcategoryName = editTextSubcategoryName.text.toString().trim()
                if (subcategoryName.isNotEmpty()) {
                    addSubCategoryToFirestore(subcategoryName)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        return builder.create()
    }

    private fun addSubCategoryToFirestore(subcategoryName: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val subcategoryId = UUID.randomUUID().toString()

            val subCategory = SubCategory(
                subCategoryId = subcategoryId,
                name = subcategoryName,
                userId = userId
            )

            val subCategoryRef = firestore
                .collection("users")
                .document(userId)
                .collection("categories")
                .document("Other")
                .collection("subcategories")
                .document(subcategoryId)

            subCategoryRef.set(subCategory)
                .addOnSuccessListener {
                    (parentFragment as? SubcategoryFragment)?.loadSubcategories()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to add subcategory: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
