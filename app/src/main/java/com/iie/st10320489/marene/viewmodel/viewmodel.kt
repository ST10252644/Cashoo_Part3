package com.iie.st10320489.marene.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.iie.st10320489.marene.data.entities.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    // Expose users as StateFlow<List<User>>
    val allUsers: StateFlow<List<User>> = callbackFlow {
        val listenerRegistration: ListenerRegistration = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val users = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(User::class.java)?.apply {
                    // optionally set id = doc.id if you have an id field
                }
            } ?: emptyList()

            trySend(users).isSuccess
        }

        awaitClose { listenerRegistration.remove() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Insert user document to Firestore
    fun insert(user: User) {
        viewModelScope.launch {
            try {
                // Generate a new document or use a unique user ID if available
                usersCollection.add(user).await()
            } catch (e: Exception) {
                // Handle error here (logging, etc)
            }
        }
    }
}
