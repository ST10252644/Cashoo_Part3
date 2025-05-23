package com.iie.st10320489.marene.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R

class SavingsGoalActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var salarySlider: SeekBar
    private lateinit var minSavingsSlider: SeekBar
    private lateinit var maxSpendingSlider: SeekBar
    private lateinit var salaryValue: TextView
    private lateinit var minSavingsValue: TextView
    private lateinit var maxSpendingValue: TextView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savings_goal)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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

        salarySlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                salaryValue.text = "R$progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        minSavingsSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                minSavingsValue.text = "R$progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        maxSpendingSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                maxSpendingValue.text = "R$progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<Button>(R.id.nextButton).setOnClickListener {
            saveUserSettingsToFirestore()
        }
    }

    private fun saveUserSettingsToFirestore() {
        val selectedPayday = spinner.selectedItem.toString()
        val salary = salarySlider.progress.toDouble()
        val minGoal = minSavingsSlider.progress.toDouble()
        val maxGoal = maxSpendingSlider.progress.toDouble()

        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            val settingsMap = hashMapOf(
                "payday" to selectedPayday,
                "salary" to salary,
                "minGoal" to minGoal,
                "maxGoal" to maxGoal,
                "color" to "",
                "chinchilla" to ""
            )

            firestore.collection("user_settings")
                .document(currentUser.uid)
                .set(settingsMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ChinchillaActivity::class.java))
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save settings: ${it.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }
}
