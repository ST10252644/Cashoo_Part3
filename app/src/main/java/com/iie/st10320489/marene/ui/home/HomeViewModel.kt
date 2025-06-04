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

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName


    fun loadUserSettings() {
        userId?.let { uid ->
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val settingsDoc = db.collection("userSettings").document(uid).get().await()
                    val goalsDoc = db.collection("user_settings").document(uid).get().await()
                    val profileDoc = db.collection("users").document(uid)
                        .collection("profile").document("profile")
                        .get()
                        .await()

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
                        val categoryId = transaction.categoryId ?: "Other"

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