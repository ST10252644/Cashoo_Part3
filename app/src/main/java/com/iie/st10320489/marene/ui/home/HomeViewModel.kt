package com.iie.st10320489.marene.ui.home

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.data.entities.Category
import com.iie.st10320489.marene.data.entities.Transaction
import com.iie.st10320489.marene.data.entities.TransactionWithCategory
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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun loadUserSettings() {
        userId?.let { uid ->
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val settingsDoc = db.collection("userSettings").document(uid).get().await()
                    val goalsDoc = db.collection("user_settings").document(uid).get().await()

                    _minGoal.value = goalsDoc.getDouble("minGoal") ?: 0.0
                    _maxGoal.value = goalsDoc.getDouble("maxGoal") ?: 0.0
                    _chinchilla.value = settingsDoc.getString("chinchilla") ?: "default_chinchilla"
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error loading user settings", e)
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun loadLast2Transactions() {
        userId?.let { uid ->
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val snapshot = db.collection("users")
                        .document(uid)
                        .collection("transactions")
                        .orderBy("dateTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .limit(2)
                        .get()
                        .await()

                    val transactions = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Transaction::class.java)
                    }

                    val transactionWithCategories = transactions.map { transaction ->
                        val categoryId = transaction.categoryId

                        val categoryDoc = db.collection("users")
                            .document(uid)
                            .collection("categories")
                            .document(categoryId)
                            .get()
                            .await()

                        val category = categoryDoc.toObject(Category::class.java)
                            ?: Category(categoryId, "Other") // fallback

                        TransactionWithCategory(transaction, category)
                    }

                    _transactions.value = transactionWithCategories
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error fetching transactions or categories", e)
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

}
