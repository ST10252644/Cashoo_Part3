package com.iie.st10320489.marene.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R

class EditProfileFragment : Fragment() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var paydaySpinner: Spinner
    private lateinit var maxSpendingSlider: SeekBar
    private lateinit var salarySlider: SeekBar
    private lateinit var minSavingsSlider: SeekBar

    private lateinit var maxSpendingValue: TextView
    private lateinit var salaryValue: TextView
    private lateinit var minSavingsValue: TextView
    private lateinit var updateButton: Button
    private lateinit var profileImageView: ImageView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI elements
        nameEditText = view.findViewById(R.id.nameEditText)
        emailEditText = view.findViewById(R.id.signupEmailEditText)
        paydaySpinner = view.findViewById(R.id.paydaySpinner)
        maxSpendingSlider = view.findViewById(R.id.maxSpendingSlider)
        maxSpendingValue = view.findViewById(R.id.maxSpendingValue)
        salarySlider = view.findViewById(R.id.salarySlider)
        salaryValue = view.findViewById(R.id.salaryValue)
        minSavingsSlider = view.findViewById(R.id.minSavingsSlider)
        minSavingsValue = view.findViewById(R.id.minSavingsValue)
        updateButton = view.findViewById(R.id.updateButton)
        profileImageView = view.findViewById(R.id.profileImage)

        setupSpinner()
        loadUserChinchillaAvatar()
        loadUserData() // This will now populate all fields with existing data

        // Salary slider listener
        salarySlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val salaryAmount = progress * 1000 // Scale up the salary (0-100 becomes 0-100,000)
                salaryValue.text = "R$salaryAmount"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Min savings slider listener
        minSavingsSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val savingsAmount = progress * 100 // Scale up savings (0-100 becomes 0-10,000)
                minSavingsValue.text = "R$savingsAmount"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Max spending slider listener
        maxSpendingSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val spendingAmount = progress * 100 // Scale up spending (0-100 becomes 0-10,000)
                maxSpendingValue.text = "R$spendingAmount"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        updateButton.setOnClickListener {
            updateUserProfile()
        }
    }

    private fun setupSpinner() {
        val options = arrayOf("Weekly", "Bi-weekly", "Monthly")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paydaySpinner.adapter = adapter
    }

    private fun loadUserChinchillaAvatar() {
        val userId = auth.currentUser?.uid

        userId?.let { uid ->
            db.collection("userSettings").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val chinchilla = document.getString("chinchilla") ?: "default_chinchilla"
                        val chinchillaResId = resources.getIdentifier(
                            chinchilla, "drawable", requireContext().packageName
                        )
                        if (chinchillaResId != 0) {
                            profileImageView.setImageResource(chinchillaResId)
                        } else {
                            profileImageView.setImageResource(R.drawable.ic_profile)
                        }
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_profile)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("EditProfileFragment", "Failed to load chinchilla avatar: ", exception)
                    profileImageView.setImageResource(R.drawable.ic_profile)
                }
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        // Load basic user data from the subcollection where it's actually stored
        db.collection("users")
            .document(userId)
            .collection("userProfiles")
            .document("profile")
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    // Get name and surname separately
                    val name = doc.getString("name") ?: ""
                    val surname = doc.getString("surname") ?: ""
                    val fullName = "$name $surname".trim()

                    // Populate the name field
                    nameEditText.setText(fullName)
                    Log.d("EditProfileFragment", "Loaded name: $fullName")

                    // Populate email field
                    val email = doc.getString("email") ?: ""
                    emailEditText.setText(email)
                    Log.d("EditProfileFragment", "Loaded email: $email")
                } else {
                    Log.w("EditProfileFragment", "User profile document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("EditProfileFragment", "Failed to load user data: ", exception)
            }

        // Load user settings from "userSettings" collection (matching your SettingsFragment)
        db.collection("userSettings").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    // Populate payday spinner
                    val payday = doc.getString("payday") ?: "Monthly"
                    val spinnerAdapter = paydaySpinner.adapter as ArrayAdapter<String>
                    val index = spinnerAdapter.getPosition(payday)
                    if (index >= 0) {
                        paydaySpinner.setSelection(index)
                    }

                    // Populate salary slider and value
                    val salary = doc.getDouble("salary") ?: 0.0
                    val salaryProgress = (salary / 100).toInt() // Scale down for slider
                    salarySlider.progress = salaryProgress
                    salaryValue.text = "R${salary.toInt()}"

                    // Populate min savings slider and value
                    val minGoal = doc.getDouble("minGoal") ?: 0.0
                    val minProgress = (minGoal / 10).toInt() // Scale down for slider
                    minSavingsSlider.progress = minProgress
                    minSavingsValue.text = "R${minGoal.toInt()}"

                    // Populate max spending slider and value
                    val maxGoal = doc.getDouble("maxGoal") ?: 0.0
                    val maxProgress = (maxGoal / 10).toInt() // Scale down for slider
                    maxSpendingSlider.progress = maxProgress
                    maxSpendingValue.text = "R${maxGoal.toInt()}"
                } else {
                    Log.w("EditProfileFragment", "User settings document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("EditProfileFragment", "Failed to load user settings: ", exception)
            }
    }

    private fun updateUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        val fullName = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val payday = paydaySpinner.selectedItem.toString()

        // Calculate actual values from slider positions
        val salary = (salarySlider.progress * 1000).toDouble()
        val minGoal = (minSavingsSlider.progress * 100).toDouble()
        val maxGoal = (maxSpendingSlider.progress * 100).toDouble()

        // Split the full name into name and surname
        val nameParts = fullName.split(" ", limit = 2)
        val firstName = nameParts.getOrNull(0) ?: ""
        val lastName = nameParts.getOrNull(1) ?: ""

        // Update user info in the subcollection where it's actually stored
        val userMap = mapOf(
            "name" to firstName,
            "surname" to lastName,
            "email" to email
        )

        db.collection("users")
            .document(userId)
            .collection("userProfiles")
            .document("profile")
            .set(userMap)
            .addOnSuccessListener {
                Log.d("EditProfileFragment", "User info updated successfully")
                Toast.makeText(requireContext(), "User info updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("EditProfileFragment", "Failed to update user info: ", e)
                Toast.makeText(requireContext(), "Failed to update user info: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Update user settings in "userSettings" collection
        val settingsMap = mapOf(
            "payday" to payday,
            "salary" to salary,
            "minGoal" to minGoal,
            "maxGoal" to maxGoal
        )

        db.collection("userSettings").document(userId).set(settingsMap)
            .addOnSuccessListener {
                Log.d("EditProfileFragment", "Settings updated successfully")
                Toast.makeText(requireContext(), "Settings updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("EditProfileFragment", "Failed to update settings: ", e)
                Toast.makeText(requireContext(), "Failed to update settings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}