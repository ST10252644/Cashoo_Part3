package com.iie.st10320489.marene.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class SavingsGoalActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var salarySlider: SeekBar
    private lateinit var minSavingsSlider: SeekBar
    private lateinit var maxSpendingSlider: SeekBar
    private lateinit var salaryValue: TextView
    private lateinit var minSavingsValue: TextView
    private lateinit var maxSpendingValue: TextView

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savings_goal)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        spinner = findViewById(R.id.paydaySpinner)
        val items = arrayOf("Select your payday", "Weekly", "Bi-weekly", "Monthly")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner.adapter = adapter

        salarySlider = findViewById(R.id.salarySlider)
        minSavingsSlider = findViewById(R.id.minSavingsSlider)
        maxSpendingSlider = findViewById(R.id.maxSpendingSlider)

        salaryValue = findViewById(R.id.salaryValue)
        minSavingsValue = findViewById(R.id.minSavingsValue)
        maxSpendingValue = findViewById(R.id.maxSpendingValue)

        salarySlider.setOnSeekBarChangeListener(simpleSliderListener { value ->
            salaryValue.text = "R$value"
        })

        minSavingsSlider.setOnSeekBarChangeListener(simpleSliderListener { value ->
            minSavingsValue.text = "R$value"
        })

        maxSpendingSlider.setOnSeekBarChangeListener(simpleSliderListener { value ->
            maxSpendingValue.text = "R$value"
        })

        findViewById<Button>(R.id.nextButton).setOnClickListener {
            saveUserSettings()
        }
    }

    private fun simpleSliderListener(update: (Int) -> Unit) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            update(progress)
        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private fun getCurrentUserEmail(): String {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        return sharedPreferences.getString("currentUserEmail", "") ?: ""
    }

    private fun saveUserSettings() {
        val currentUserEmail = getCurrentUserEmail()
        val selectedPayday = spinner.selectedItem.toString()
        val salary = salarySlider.progress.toDouble()
        val minGoal = minSavingsSlider.progress.toDouble()
        val maxGoal = maxSpendingSlider.progress.toDouble()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = firestore.collection("users")
                    .whereEqualTo("email", currentUserEmail)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val userDocId = snapshot.documents[0].id

                    val userSettingsData = hashMapOf(
                        "payday" to selectedPayday,
                        "salary" to salary,
                        "minGoal" to minGoal,
                        "maxGoal" to maxGoal,
                        "color" to "",       // Placeholder if needed later
                        "chinchilla" to ""   // Placeholder if needed later
                    )

                    firestore.collection("users")
                        .document(userDocId)
                        .collection("preferences")
                        .document("userSettings")
                        .set(userSettingsData)
                        .await()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SavingsGoalActivity, "Settings saved", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SavingsGoalActivity, ChinchillaActivity::class.java))
                    }

                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SavingsGoalActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SavingsGoalActivity, "Failed to save settings: ${e.message}", Toast.LENGTH_LONG).show()
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

