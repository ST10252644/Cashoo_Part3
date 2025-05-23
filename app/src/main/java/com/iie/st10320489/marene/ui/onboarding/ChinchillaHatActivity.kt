package com.iie.st10320489.marene.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.MainActivity
import com.iie.st10320489.marene.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChinchillaHatActivity : AppCompatActivity() {

    private lateinit var chinchillaImage: ImageView
    private var selectedColor: String = ""
    private var selectedHat: String = ""

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chinchilla_hat)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        chinchillaImage = findViewById(R.id.chinchillaImage)
        selectedColor = intent.getStringExtra("selectedColor") ?: "black"

        findViewById<View>(R.id.sailorButton).setOnClickListener {
            selectedHat = "sailor"
            chinchillaImage.setImageResource(getDrawableResource(selectedColor, selectedHat))
        }
        findViewById<View>(R.id.mexicanButton).setOnClickListener {
            selectedHat = "mexican"
            chinchillaImage.setImageResource(getDrawableResource(selectedColor, selectedHat))
        }
        findViewById<View>(R.id.strawberryButton).setOnClickListener {
            selectedHat = "strawberry"
            chinchillaImage.setImageResource(getDrawableResource(selectedColor, selectedHat))
        }
        findViewById<View>(R.id.pirateButton).setOnClickListener {
            selectedHat = "pirate"
            chinchillaImage.setImageResource(getDrawableResource(selectedColor, selectedHat))
        }

        findViewById<Button>(R.id.nextButton).setOnClickListener {
            if (selectedHat.isEmpty()) {
                Toast.makeText(this, "Please select a hat", Toast.LENGTH_SHORT).show()
            } else {
                val chinchillaString = "${selectedColor}_${selectedHat}"
                saveChinchillaToFirestore(chinchillaString)
            }
        }
    }

    private fun getDrawableResource(color: String, hat: String): Int {
        return resources.getIdentifier("${color}_${hat}", "drawable", packageName)
    }

    private fun getCurrentUserEmail(): String {
        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("currentUserEmail", "") ?: ""
    }

    private fun saveChinchillaToFirestore(chinchillaString: String) {
        val currentUserEmail = getCurrentUserEmail()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userSnapshot = firestore.collection("users")
                    .whereEqualTo("email", currentUserEmail)
                    .get()
                    .await()

                if (!userSnapshot.isEmpty) {
                    val userDocId = userSnapshot.documents[0].id

                    // Update Firestore user preferences
                    val updates = hashMapOf(
                        "color" to selectedColor,
                        "chinchilla" to chinchillaString
                    )

                    firestore.collection("users")
                        .document(userDocId)
                        .collection("preferences")
                        .document("userSettings")
                        .set(updates)
                        .await()

                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@ChinchillaHatActivity, MainActivity::class.java)
                        intent.putExtra("navigateToHome", true)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ChinchillaHatActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChinchillaHatActivity, "Failed to save data: ${e.message}", Toast.LENGTH_SHORT).show()
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
