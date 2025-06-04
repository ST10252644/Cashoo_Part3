package com.iie.st10320489.marene.ui.search

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Category
import com.iie.st10320489.marene.data.entities.Transaction
import com.iie.st10320489.marene.data.entities.TransactionWithCategory
import com.iie.st10320489.marene.ui.transaction.TransactionDetailsFragment
import java.text.SimpleDateFormat
import java.util.*

class SearchTransactionFragment : Fragment() {

    private lateinit var spinnerFilter: Spinner
    private lateinit var filterInputContainer: LinearLayout
    private lateinit var dateInputGroup: LinearLayout
    private lateinit var nameInputGroup: LinearLayout
    private lateinit var amountInputGroup: LinearLayout
    private lateinit var transDate: EditText
    private lateinit var btnPickDate: Button
    private lateinit var nameInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var btnSearch: Button
    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: SearchTransactionAdapter
    private lateinit var firestore: FirebaseFirestore
    private var userId: String? = null

    private var selectedDate: String = ""

    // Initialize the variables
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    // Inflate the layout for this fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_search, container, false)
    }

    //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
    // Set up the views and listeners
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get current logged in user ID from FirebaseAuth
        userId = FirebaseAuth.getInstance().currentUser?.uid

        // Check if user is logged in
        if (userId == null) {
            Log.e("SearchTransactionFragment", "User not logged in!")
            Toast.makeText(requireContext(), "Please log in to search transactions.", Toast.LENGTH_SHORT).show()

            return
        } else {
            Log.d("SearchTransactionFragment", "Current userId: $userId")
        }

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        spinnerFilter = view.findViewById(R.id.spinner_filter)
        filterInputContainer = view.findViewById(R.id.filterInputContainer)
        dateInputGroup = view.findViewById(R.id.dateInputGroup)
        nameInputGroup = view.findViewById(R.id.nameInputGroup)
        amountInputGroup = view.findViewById(R.id.amountInputGroup)
        transDate = view.findViewById(R.id.transDate)
        btnPickDate = view.findViewById(R.id.btnPickDate)
        nameInput = view.findViewById(R.id.nameInput)
        amountInput = view.findViewById(R.id.amountInput)
        btnSearch = view.findViewById(R.id.btn_search)
        recyclerView = view.findViewById(R.id.transactionRecyclerView)

        adapter = SearchTransactionAdapter(emptyList()) { transactionWithCategory ->
            val transactionDetailsFragment = TransactionDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString("transactionId", transactionWithCategory.transaction.transactionId)
                    putString("userId", userId)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace((view?.parent as ViewGroup).id, transactionDetailsFragment)
                .addToBackStack(null)
                .commit()
        }


        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter


        setupSpinner()
        setupDatePicker()
        setupSearchButton()
    }

    // Set up the spinner for filtering
    private fun setupSpinner() {
        val options = arrayOf("Select Filter", "Name", "Date", "Amount")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, options)
        spinnerFilter.adapter = spinnerAdapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Always hide the previous results
                adapter.updateTransactions(emptyList())

                filterInputContainer.visibility = View.VISIBLE
                btnSearch.visibility = View.VISIBLE

                dateInputGroup.visibility = View.GONE
                nameInputGroup.visibility = View.GONE
                amountInputGroup.visibility = View.GONE

                when (position) {
                    1 -> nameInputGroup.visibility = View.VISIBLE
                    2 -> dateInputGroup.visibility = View.VISIBLE
                    3 -> amountInputGroup.visibility = View.VISIBLE
                    else -> {
                        filterInputContainer.visibility = View.GONE
                        btnSearch.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
    //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
// Set up the date picker
    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        btnPickDate.setOnClickListener {
            DatePickerDialog(requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    selectedDate = formatter.format(calendar.time)
                    transDate.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
// Set up the search button
    private fun setupSearchButton() {
        btnSearch.setOnClickListener {
            val filterType = spinnerFilter.selectedItem.toString()
            val query = when (filterType) {
                "Name" -> nameInput.text.toString().trim()
                "Date" -> selectedDate
                "Amount" -> amountInput.text.toString().trim()
                else -> ""
            }

            if (query.isNotEmpty()) {
                searchTransactions(filterType, query)
            } else {
                Toast.makeText(requireContext(), "Please enter a value to search", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
    private fun searchTransactions(filterType: String, query: String) {
        val uid = userId
        if (uid == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val transactionsRef = firestore.collection("users").document(uid).collection("transactions")
        val categoriesRef = firestore.collection("users").document(uid).collection("categories")

        // Fetch all categories first
        categoriesRef.get().addOnSuccessListener { categorySnapshot ->
            val categoryMap = categorySnapshot.documents.associateBy(
                { it.id },
                {
                    Category(
                        name = it.getString("name") ?: "Uncategorized",
                        icon = (it.getLong("icon") ?: R.drawable.ic_default).toInt(),
                        colour = (it.getLong("colour") ?: R.color.black).toInt()
                    )
                }
            )

            // Then fetch transactions
            transactionsRef.get().addOnSuccessListener { transactionSnapshot ->
                val transactions = transactionSnapshot.mapNotNull { doc ->
                    val transaction = doc.toObject(Transaction::class.java)
                    transaction.transactionId = doc.id
                    transaction
                }

                val filtered = when (filterType) {
                    "Name" -> transactions.filter { it.name.contains(query, ignoreCase = true) }
                    "Date" -> transactions.filter { it.dateTime.startsWith(query) }
                    "Amount" -> {
                        val amount = query.toDoubleOrNull()
                        if (amount == null) {
                            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        } else {
                            transactions.filter { it.amount == amount }
                        }
                    }
                    else -> emptyList()
                }

                // Map transactions with their actual categories
                val wrappedResults = filtered.map { tx ->
                    val category = categoryMap[tx.categoryId] ?: Category(
                        name = "Uncategorized",
                        icon = R.drawable.ic_default,
                        colour = R.color.black
                    )
                    TransactionWithCategory(transaction = tx, category = category)
                }

                adapter.updateTransactions(wrappedResults)

                if (wrappedResults.isEmpty()) {
                    Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show()
                }

            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch transactions", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to fetch categories", Toast.LENGTH_SHORT).show()
        }
    }

} //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
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