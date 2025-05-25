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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Removed userId argument fetching because we'll get userId directly from FirebaseAuth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get current logged in user ID from FirebaseAuth
        userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Log.e("SearchTransactionFragment", "User not logged in!")
            Toast.makeText(requireContext(), "Please log in to search transactions.", Toast.LENGTH_SHORT).show()
            // You might want to navigate to login or disable search UI here
            return
        } else {
            Log.d("SearchTransactionFragment", "Current userId: $userId")
        }

        firestore = FirebaseFirestore.getInstance()

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
            // Handle item click if needed
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        setupSpinner()
        setupDatePicker()
        setupSearchButton()
    }

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

}
