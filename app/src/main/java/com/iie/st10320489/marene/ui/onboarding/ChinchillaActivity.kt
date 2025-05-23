package com.iie.st10320489.marene.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChinchillaActivity : AppCompatActivity() {

    private lateinit var chinchillaImage: ImageView
    private var selectedColor: String = ""
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chinchilla)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        chinchillaImage = findViewById(R.id.chinchillaImage)

        findViewById<View>(R.id.whiteButton).setOnClickListener {
            selectedColor = "white"
            chinchillaImage.setImageResource(R.drawable.white_nohat)
        }
        findViewById<View>(R.id.beigeButton).setOnClickListener {
            selectedColor = "beige"
            chinchillaImage.setImageResource(R.drawable.beige_nohat)
        }
        findViewById<View>(R.id.violetButton).setOnClickListener {
            selectedColor = "violet"
            chinchillaImage.setImageResource(R.drawable.voilet_nohat)
        }
        findViewById<View>(R.id.brownButton).setOnClickListener {
            selectedColor = "brown"
            chinchillaImage.setImageResource(R.drawable.brown_nohat)
        }
        findViewById<View>(R.id.blackButton).setOnClickListener {
            selectedColor = "black"
            chinchillaImage.setImageResource(R.drawable.black_nohat)
        }
        findViewById<View>(R.id.greyButton).setOnClickListener {
            selectedColor = "grey"
            chinchillaImage.setImageResource(R.drawable.grey_nohat)
        }

        findViewById<Button>(R.id.nextButton).setOnClickListener {
            if (selectedColor.isNotEmpty()) {
                saveSelectedColorToFirestore()
            } else {
                Toast.makeText(this, "Please select a color", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentUserEmail(): String {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        return sharedPreferences.getString("currentUserEmail", "") ?: ""
    }

    private fun saveSelectedColorToFirestore() {
        val email = getCurrentUserEmail()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userSnapshot = firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (!userSnapshot.isEmpty) {
                    val userDocId = userSnapshot.documents[0].id

                    val colorData = hashMapOf("chinchillaColor" to selectedColor)

                    firestore.collection("users")
                        .document(userDocId)
                        .collection("preferences")
                        .document("chinchilla")
                        .set(colorData)
                        .await()

                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@ChinchillaActivity, ChinchillaHatActivity::class.java)
                        intent.putExtra("selectedColor", selectedColor)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ChinchillaActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("FirestoreError", "Failed to save chinchilla color: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChinchillaActivity, "Error saving color", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


//Reference List:
//Android Developers. 2025. Add an Image composition. [online]. Available at: https://developer.android.com/codelabs/basic-android-kotlin-compose-add-images#2 [Accessed on 9 April 2025]
//Code With Cal. 2025. Color Picker Android Studio Kotlin Custom Spinner Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=YsKjl8ZbM4g [Accessed on 9 April 2025]
//Code With Cal. 2025. Room Database Android Studio Kotlin Example Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=-LNg-K7SncM [Accessed on 12 April 2025]
//Programming w/ Professor Sluiter. 2023. Learn Kotlin 08 how to use the if conditional statement. [online]. Available at: https://www.youtube.com/watch?v=usFfxlnTPHc [Accessed on 13 April 2025]
//GeeksforGeeks. 2025. Android UI Layouts. [online]. Available at: https://www.geeksforgeeks.org/android-ui-layouts/ [Accessed on 10 April 2025]
//Muhammadumarch. 2023. Implementing Navigation in Your Android App with Android Navigation Component. [online]. Available at: https://medium.com/@muhammadumarch321/implementing-navigation-in-your-android-app-with-android-navigation-component-ff22a3d300a [Accessed on 11 April 2025]
//Android Developers. 2025. Fragment lifecycle. [online]. Available at: https://developer.android.com/guide/fragments/lifecycle [Accessed on 12 April 2025]
//Android Knowledge. 2024. ViewModel in Android Studio using Kotlin | Android Knowledge. [video online]. Available at: https://www.youtube.com/watch?v=v32hSKtlH9A [Accessed on 11 April 2025]
//Code With Cal. 2025. Room Database Android Studio Kotlin Example Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=-LNg-K7SncM [Accessed on 12 April 2025]
//Android Developers. 2025. Accessing data using Room DAOs. [online]. Available at: https://developer.android.com/training/data-storage/room/accessing-data [Accessed on 15 April 2025]
