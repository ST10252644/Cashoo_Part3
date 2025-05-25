package com.iie.st10320489.marene.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.LoginActivity
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.ui.profile.EditProfileFragment

class SettingsFragment : Fragment() { // (Code With Cal, 2025)

    // Variable to store the user's ID
    private var userId: Int = 0

    // UI elements
    private lateinit var profileImageView: ImageView
    private lateinit var profileNameTextView: TextView
    private lateinit var usernameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI elements
        profileImageView = view.findViewById(R.id.profileImage)
        profileNameTextView = view.findViewById(R.id.profileName)
        usernameTextView = view.findViewById(R.id.username)

        // Find views by their IDs from the layout
        val logoutButton: RelativeLayout = view.findViewById(R.id.btnLogout)
        val editProfileRow: RelativeLayout = view.findViewById(R.id.btnEditProfile)
        val privacyRow: RelativeLayout = view.findViewById(R.id.btnPrivacy)
        val helpRow: RelativeLayout = view.findViewById(R.id.btnHelp)

        // Load user data and avatar
        loadUserData()

        logoutButton.setOnClickListener {
            showLogoutDialog() // Show confirmation dialog before logging out
        }

        editProfileRow.setOnClickListener {
            //Toast.makeText(context, "Continue in Part 3", Toast.LENGTH_SHORT).show()
            // Navigate to Edit Profile screen
            findNavController().navigate(R.id.action_settingFragment_to_edit_profile_Fragment)
        }

        // Set click listener for Privacy row
        privacyRow.setOnClickListener {
            findNavController().navigate(R.id.privacyFragment)
        }

        // Set click listener for Help row
        helpRow.setOnClickListener {
            findNavController().navigate(R.id.helpFragment)
        }

    } // (Code With Cal, 2025)

    // Load user data and chinchilla avatar from Firebase
    private fun loadUserData() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val firebaseUserId = auth.currentUser?.uid

        firebaseUserId?.let { uid ->
            // Load user basic information
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name") ?: ""
                        val surname = document.getString("surname") ?: ""
                        val email = document.getString("email") ?: ""

                        // Update UI with user data
                        val fullName = "$name $surname".trim()
                        val sentenceCaseName = fullName.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        profileNameTextView.text = if (sentenceCaseName.isNotEmpty()) sentenceCaseName else "User"

                        // Use email as username or extract username from email
                        val username = if (email.isNotEmpty()) {
                            email.substringBefore("@").lowercase()
                        } else {
                            "user"
                        }
                        usernameTextView.text = username

                        // Load user settings for chinchilla avatar
                        loadUserChinchillaAvatar(uid)
                    } else {
                        Log.w("SettingsFragment", "User document does not exist")
                        // Set default values
                        profileNameTextView.text = "User"
                        usernameTextView.text = "user"
                        loadUserChinchillaAvatar(uid)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("SettingsFragment", "Failed to load user data: ", exception)
                    // Set default values on error
                    profileNameTextView.text = "User"
                    usernameTextView.text = "user"
                    loadUserChinchillaAvatar(uid)
                }
        } ?: run {
            Log.w("SettingsFragment", "No authenticated user found")
            // Set default values if no user is authenticated
            profileNameTextView.text = "User"
            usernameTextView.text = "user"
            // Set default avatar
            profileImageView.setImageResource(R.drawable.ic_profile)
        }
    }

    private fun loadUserChinchillaAvatar(uid: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("userSettings").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val chinchilla = document.getString("chinchilla") ?: "default_chinchilla"
                    val chinchillaResId = resources.getIdentifier(
                        chinchilla, "drawable", requireContext().packageName
                    )

                    // Use the chinchilla image if found, otherwise use default profile image
                    if (chinchillaResId != 0) {
                        profileImageView.setImageResource(chinchillaResId)
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_profile)
                    }
                } else {
                    // Set default avatar if no user settings found
                    profileImageView.setImageResource(R.drawable.ic_profile)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("SettingsFragment", "Failed to load chinchilla avatar: ", exception)
                // Set default avatar on error
                profileImageView.setImageResource(R.drawable.ic_profile)
            }
    }

    // Shows a confirmation dialog for logging out
    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log out")
            .setMessage("Are you sure you want to log out? You'll need to login again to use the app.")
            .setPositiveButton("Log out") { _, _ ->
                performLogout() // Perform logout if user confirms
            }
            .setNegativeButton("Cancel", null) // Dismiss dialog if user cancels
            .show()
    }

    // Clears the user's session and navigates back to the login screen
    private fun performLogout() {
        // Sign out from Firebase Auth
        FirebaseAuth.getInstance().signOut()

        // Clear user session from shared preferences
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Create intent to start LoginActivity and clear activity backstack
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        requireActivity().finish()
    } // (Code With Cal, 2025)
}

//Reference List:
//Android Developers. 2025. Add an Image composition. [online]. Available at: https://developer.android.com/codelabs/basic-android-kotlin-compose-add-images#2 [Accessed on 9 April 2025]
//Code With Cal. 2025. Color Picker Android Studio Kotlin Custom Spinner Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=YsKjl8ZbM4g [Accessed on 9 April 2025]
//Code With Cal. 2025. Room Database Android Studio Kotlin Example Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=-LNg-K7SncM [Accessed on 12 April 2025]
//Programming w/ Professor Sluiter. 2023. Learn Kotlin 08 how to use the if conditional statement. [online]. Available at: https://www.youtube.com/watch?v=usFfxlnTPHc [Accessed on 13 April 2025]