package com.iie.st10320489.marene.ui.home

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.data.entities.Category
import com.iie.st10320489.marene.data.entities.Transaction
import com.iie.st10320489.marene.data.entities.TransactionWithCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val userId: String? = auth.currentUser?.uid

    private val _minGoal = MutableLiveData<Double>()
    val minGoal: LiveData<Double> get() = _minGoal

    private val _maxGoal = MutableLiveData<Double>()
    val maxGoal: LiveData<Double> get() = _maxGoal

    private val _chinchilla = MutableLiveData<String>()
    val chinchilla: LiveData<String> get() = _chinchilla

    private val _transactions = MutableLiveData<List<TransactionWithCategory>>()
    val transactions: LiveData<List<TransactionWithCategory>> get() = _transactions

    // 🔄 Loading state LiveData for progress bar
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun loadUserSettings() {
        userId?.let { uid ->
            viewModelScope.launch {
                _isLoading.value = true // Start loading
                Log.d("HomeViewModel", "Loading started for users")

                try {
                    val settingsDoc = db.collection("userSettings").document(uid).get().await()
                    val goalsDoc = db.collection("user_settings").document(uid).get().await()

                    _minGoal.value = goalsDoc.getDouble("minGoal") ?: 0.0
                    _maxGoal.value = goalsDoc.getDouble("maxGoal") ?: 0.0
                    _chinchilla.value = settingsDoc.getString("chinchilla") ?: "default_chinchilla"
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error loading user settings", e)
                } finally {
                    _isLoading.value = false // Done loading
                }
            }
        }
    }

    fun loadTop5Transactions() {
        userId?.let { uid ->
            viewModelScope.launch {
                _isLoading.value = true // Start loading
                Log.d("HomeViewModel", "Loading started for transactions")

                try {
                    val result = db.collection("transactions")
                        .whereEqualTo("userId", uid)
                        .orderBy("dateTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .limit(5)
                        .get()
                        .await()

                    val transactionsList = result.mapNotNull { it.toObject(Transaction::class.java) }

                    val combined = mutableListOf<TransactionWithCategory>()
                    for (transaction in transactionsList) {
                        if (transaction.categoryId.isNotEmpty()) {
                            val categorySnapshot = db.collection("categories")
                                .document(transaction.categoryId)
                                .get()
                                .await()

                            val category = categorySnapshot.toObject(Category::class.java)
                            if (category != null) {
                                combined.add(TransactionWithCategory(transaction, category))
                            }
                        }
                    }

                    _transactions.value = combined
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error loading transactions", e)
                } finally {
                    delay(200)
                    _isLoading.value = false // Done loading
                }
            }
        }
    }
}
