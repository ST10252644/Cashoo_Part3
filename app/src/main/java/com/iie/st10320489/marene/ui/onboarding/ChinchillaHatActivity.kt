package com.iie.st10320489.marene.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.MainActivity
import com.iie.st10320489.marene.R

class ChinchillaHatActivity : AppCompatActivity() {

    private lateinit var chinchillaImage: ImageView
    private var selectedColor: String = ""
    private var selectedHat: String = ""

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chinchilla_hat)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set up back button to finish the activity
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Initialize chinchilla image view
        chinchillaImage = findViewById(R.id.chinchillaImage)
        // Get selected color from intent or default to "black"
        selectedColor = intent.getStringExtra("selectedColor") ?: "black"

        // Hat selection buttons
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

        // Save and navigate on next
        findViewById<Button>(R.id.nextButton).setOnClickListener {
            val chinchillaString = "${selectedColor}_${selectedHat}"
            saveChinchillaToFirestore(chinchillaString)
        }
    }

    // Load image from drawable based on selection
    private fun getDrawableResource(color: String, hat: String): Int {
        return resources.getIdentifier("${color}_${hat}", "drawable", packageName)
    }

    // Save data to Firestore
    private fun saveChinchillaToFirestore(chinchillaString: String) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val userId = user.uid
            val userSettings = hashMapOf(
                "color" to selectedColor,
                "chinchilla" to chinchillaString
            )

            firestore.collection("userSettings")
                .document(userId)
                .set(userSettings)
                .addOnSuccessListener {
                    val intent = Intent(this@ChinchillaHatActivity, MainActivity::class.java)
                    intent.putExtra("navigateToHome", true)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }
}
