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

class EditProfileFragment : Fragment() { // (Code With Cal, 2025)

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var paydaySpinner: Spinner
    private lateinit var maxSpendingSlider: SeekBar
    private lateinit var maxSpendingValue: TextView
    private lateinit var updateButton: Button

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
        updateButton = view.findViewById(R.id.updateButton)

        setupSpinner()
        loadUserChinchillaAvatar()
        loadUserData()

        maxSpendingSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                maxSpendingValue.text = "R$progress"
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
                        val profileImageView: ImageView = requireView().findViewById(R.id.profileImage)
                        profileImageView.setImageResource(chinchillaResId)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("EditProfileFragment", "Failed to load chinchilla avatar: ", exception)
                }
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        // Load basic user data
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            nameEditText.setText(doc.getString("name") ?: "")
            emailEditText.setText(doc.getString("email") ?: "")
        }

        // Load user settings
        db.collection("user_settings").document(userId).get().addOnSuccessListener { doc ->
            val payday = doc.getString("payday") ?: "Monthly"
            val maxGoal = doc.getDouble("maxGoal") ?: 0.0
            maxSpendingSlider.progress = maxGoal.toInt()
            maxSpendingValue.text = "R${maxGoal.toInt()}"

            val index = (paydaySpinner.adapter as ArrayAdapter<String>).getPosition(payday)
            paydaySpinner.setSelection(index)
        }
    }

    private fun updateUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val payday = paydaySpinner.selectedItem.toString()
        val maxGoal = maxSpendingSlider.progress.toDouble()

        // Update user info in "users" collection
        val userMap = mapOf(
            "name" to name,
            "email" to email
        )
        db.collection("users").document(userId).update(userMap).addOnSuccessListener {
            Toast.makeText(requireContext(), "User info updated", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Failed to update user info: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        // Update user settings in "user_settings" collection
        val settingsMap = mapOf(
            "payday" to payday,
            "maxGoal" to maxGoal
        )
        db.collection("user_settings").document(userId).update(settingsMap).addOnSuccessListener {
            Toast.makeText(requireContext(), "Settings updated", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Failed to update settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
} // (Code With Cal, 2025)
