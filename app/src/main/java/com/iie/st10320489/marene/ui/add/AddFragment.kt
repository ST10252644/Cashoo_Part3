package com.iie.st10320489.marene.ui.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.iie.st10320489.marene.data.entities.Transaction
import com.iie.st10320489.marene.databinding.FragmentAddBinding
import java.util.Calendar
import java.util.UUID

data class CategoryItem(
    val displayName: String,
    val categoryId: String,
    val subCategoryId: String? = null
)


class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private val REQUEST_CODE_PICK_IMAGE = 100
    private var selectedImageUri: Uri? = null
    private var selectedImageUrl: String = ""

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var selectedCategory: String = ""
    private var selectedSubCategory: String = ""
    private var selectedDate = ""
    private var selectedTime = ""
    //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }
    //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinner = binding.categorySpinner
        val currentUser = auth.currentUser ?: run {
            Log.e("CategoryFragment", "No authenticated user found")
            return
        }
        val uid = currentUser.uid

        Log.d("CategoryFragment", "Fetching categories for user: $uid")

        val categoryItems = mutableListOf<CategoryItem>()

        firestore.collection("users").document(uid).collection("categories")
            .get()
            .addOnSuccessListener { categoryDocs ->
                Log.d("CategoryFragment", "Fetched ${categoryDocs.size()} categories")

                val categoryDocsList = categoryDocs.toList().toMutableList()
                val existingIds = categoryDocsList.map { it.id }

                // Check for "Other" category stored with name as ID
                firestore.collection("users").document(uid).collection("categories")
                    .whereEqualTo("name", "Other")
                    .get()
                    .addOnSuccessListener { otherDocs ->
                        val realOtherDoc = otherDocs.firstOrNull()
                        if (realOtherDoc != null && !existingIds.contains(realOtherDoc.id)) {
                            categoryDocsList.add(realOtherDoc)
                        }

                        var loadedTasks = 0
                        val totalTasks = categoryDocsList.size

                        if (totalTasks == 0) {
                            setupSpinner(spinner, categoryItems)
                            return@addOnSuccessListener
                        }

                        categoryDocsList.forEach { categoryDoc ->
                            val categoryId = categoryDoc.id
                            val categoryName = categoryDoc.getString("name") ?: categoryId

                            categoryItems.add(CategoryItem(displayName = categoryName, categoryId = categoryId))

                            // 💥 Fetch subcategories using category *name* as path (since your subcategories are stored under "Other", not the ID)
                            firestore.collection("users").document(uid)
                                .collection("categories").document(categoryName) // using name instead of id
                                .collection("subcategories")
                                .get()
                                .addOnSuccessListener { subDocs ->
                                    subDocs.forEach { subDoc ->
                                        val subName = subDoc.getString("name") ?: return@forEach
                                        val subId = subDoc.getString("subCategoryId") ?: subDoc.id

                                        categoryItems.add(
                                            CategoryItem(
                                                displayName = "• $subName",
                                                categoryId = categoryId,
                                                subCategoryId = subId
                                            )
                                        )
                                    }

                                    loadedTasks++
                                    if (loadedTasks == totalTasks) {
                                        setupSpinner(spinner, categoryItems)
                                    }
                                }
                                .addOnFailureListener {
                                    loadedTasks++
                                    if (loadedTasks == totalTasks) {
                                        setupSpinner(spinner, categoryItems)
                                    }
                                }
                        }
                    }
            }
            .addOnFailureListener { e ->
                Log.e("CategoryFragment", "Failed to fetch categories", e)
            }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = categoryItems.getOrNull(position)
                if (selected != null) {
                    selectedCategory = selected.categoryId
                    selectedSubCategory = selected.subCategoryId ?: ""
                    Log.d("CategoryFragment", "Selected category: ${selected.categoryId}, subcategory: ${selected.subCategoryId ?: "none"}")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory = ""
                selectedSubCategory = ""
            }
        }


        setupUIListeners()
    }
    //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
    private fun setupSpinner(spinner: Spinner, items: List<CategoryItem>) {
        Log.d("CategoryFragment", "Setting up spinner with ${items.size} items")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items.map { it.displayName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }


    private fun setupUIListeners() {
        binding.btnChooseFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        }

        binding.btnAddEntry.setOnClickListener {
            uploadImageAndSaveTransaction()
        }

        val calendar = Calendar.getInstance()
        binding.btnPickDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                updateDateTimeField()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnPickTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                updateDateTimeField()
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
    }
    //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)

    private fun updateDateTimeField() {
        if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
            val dateTime = "$selectedDate $selectedTime"
            binding.transDate.setText(dateTime)
        }
    }
    //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
    private fun uploadImageAndSaveTransaction() {
        val name = binding.transName.text.toString()
        val amount = binding.transAmount.text.toString().toDoubleOrNull() ?: 0.0
        val method = binding.transMethod.text.toString()
        val location = binding.transLocation.text.toString()
        val dateTime = binding.transDate.text.toString()
        val description = binding.transDescription.text.toString()
        val transactionType = if (binding.rbExpense.isChecked) "Expense" else "Income"
        val isExpense = binding.rbExpense.isChecked

        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }


        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(requireContext(), "Please select both a date and time", Toast.LENGTH_SHORT).show()
            return
        }

        val fakeUploadPath = selectedImageUri?.toString() ?: ""

        saveTransaction(
            userId = userId,
            name = name,
            amount = amount,
            method = method,
            location = location,
            date = dateTime,
            description = description,
            photoUrl = fakeUploadPath,
            selectedCategoryId = selectedCategory,
            isExpense = isExpense
        )
    }

    //((Cal, 2023), (College, 2025)
    //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
    private fun saveTransaction(
        userId: String,
        name: String,
        amount: Double,
        method: String,
        location: String,
        date: String,
        description: String,
        photoUrl: String,
        selectedCategoryId: String,
        isExpense: Boolean
    ) {
        val transactionId = UUID.randomUUID().toString()

        val transaction = Transaction(
            transactionId = transactionId,
            userId = userId,
            name = name,
            amount = amount,
            transactionMethod = method,
            location = location,
            dateTime = date,
            description = description,
            photo = photoUrl,
            categoryId = selectedCategoryId,
            subCategoryId = if (selectedSubCategory.isNotEmpty()) selectedSubCategory else null,
            expense = isExpense
        )

        firestore.collection("users")
            .document(userId)
            .collection("transactions")
            .document(transactionId)
            .set(transaction)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Transaction saved", Toast.LENGTH_SHORT).show()
                resetFields()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save transaction", Toast.LENGTH_SHORT).show()
            }
    }

    //((Cal, 2023), (College, 2025)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            Glide.with(requireContext()).load(selectedImageUri).into(binding.imageView)
            binding.tvFileName.text = getFileNameFromUri(selectedImageUri)
        }
    }

    private fun getFileNameFromUri(uri: Uri?): String {
        var fileName = ""
        uri?.let {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    fileName = it.getString(index)
                }
            }
        }
        return fileName
    }
    //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
    private fun resetFields() {
        binding.transName.text.clear()
        binding.transAmount.text.clear()
        binding.transMethod.text.clear()
        binding.transLocation.text.clear()
        binding.transDate.text.clear()
        binding.transDescription.text.clear()
        binding.rbExpense.isChecked = true
        binding.imageView.setImageDrawable(null)
        binding.tvFileName.text = ""
        selectedImageUri = null
        selectedImageUrl = ""
    }
    //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
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