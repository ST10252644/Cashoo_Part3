package com.iie.st10320489.marene;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;
import android.widget.ImageView;


import androidx.room.*;


import androidx.appcompat.app.AppCompatActivity;

import com.iie.st10320489.marene.data.database.AppDatabase;
import com.iie.st10320489.marene.data.entities.User;
import com.iie.st10320489.marene.ui.onboarding.OnboardingActivity;
import com.iie.st10320489.marene.ui.onboarding.OnboardingActivity2;

import java.util.List;

public class SignupActivity extends AppCompatActivity { // (Android Developers, 2024)

    // Method to check if a password is strong: at least 8 characters, includes uppercase, digit, and special character
    private boolean isPasswordStrong(String password) { // (Android Developers, 2024)
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[!@#\\$%^&*].*");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup); // Set the layout for the signup screen

        // Initialize Room database
        AppDatabase db;
        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "cashoo_database")
                .allowMainThreadQueries() // Allow queries on the main thread (not recommended for production)
                .build();

        // Bind UI components to variables
        Button signupButton = findViewById(R.id.signupLoginButton);
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText surnameEditText = findViewById(R.id.surnameEditText);
        EditText emailEditText = findViewById(R.id.signupEmailEditText);
        EditText passwordEditText = findViewById(R.id.createPasswordEditText);
        EditText confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        ImageView toggleCreatePassword = findViewById(R.id.toggleCreatePassword);
        ImageView toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);
        EditText createPasswordEditText = findViewById(R.id.createPasswordEditText);

        // State variables to track password visibility
        final boolean[] isCreatePasswordVisible = {false};
        final boolean[] isConfirmPasswordVisible = {false};

        // Toggle visibility of "Create Password" field
        toggleCreatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCreatePasswordVisible[0]) {
                    // Hide password
                    createPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    toggleCreatePassword.setImageResource(R.drawable.ic_eye_off);
                } else {
                    // Show password
                    createPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    toggleCreatePassword.setImageResource(R.drawable.ic_eye);
                }
                createPasswordEditText.setSelection(createPasswordEditText.length()); // Maintain cursor position
                isCreatePasswordVisible[0] = !isCreatePasswordVisible[0]; // Toggle the state
            }
        });

        // Toggle visibility of "Confirm Password" field
        toggleConfirmPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConfirmPasswordVisible[0]) {
                    // Hide password
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    toggleConfirmPassword.setImageResource(R.drawable.ic_eye_off);
                } else {
                    // Show password
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    toggleConfirmPassword.setImageResource(R.drawable.ic_eye);
                }
                confirmPasswordEditText.setSelection(confirmPasswordEditText.length()); // Maintain cursor position
                isConfirmPasswordVisible[0] = !isConfirmPasswordVisible[0]; // Toggle the state
            }
        }); // (Android Developers, 2024)

        // Handle sign-up button click
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input from fields
                String name = nameEditText.getText().toString();
                String surname = surnameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString(); // (Android Developers, 2024)

                // Validate user input
                if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(SignupActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignupActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                } else if (!isPasswordStrong(password)) {
                    Toast.makeText(SignupActivity.this, "Password is not complex enough. Use 8+ chars, uppercase, number, and symbol.", Toast.LENGTH_LONG).show();
                } else {
                    // All validations passed, proceed to create account in background thread
                    new Thread(() -> {
                        // Create new user object
                        User newUser = new User(0, name, surname, email, password, 0.0, true);
                        db.userDao().insertUserNow(newUser); // Insert user into database

                        // Save user info in SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("currentUserEmail", newUser.getEmail());
                        editor.putInt("currentUserId", newUser.getUserId());
                        editor.apply();

                        // Switch to UI thread to update UI and move to onboarding
                        runOnUiThread(() -> {
                            Toast.makeText(SignupActivity.this, "Account created!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignupActivity.this, OnboardingActivity.class);
                            startActivity(intent);
                            finish(); // Close the signup activity
                        });
                    }).start();
                }
            }
        });

        // Set up click listener to switch to login activity
        TextView signInText = findViewById(R.id.signUpText);
        signInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        }); // (Android Developers, 2024)
    }
}

//Bibliography
//AndroidDevelopers, 2024. Save data in a local database using Room. [Online] Available at: hRps://developer.android.com/training/data-storage/room [Accessed 27 April 2025].
//AndroidDevelopers, 2024. Write asynchronous DAO queries. [Online]
//Available at: hRps://developer.android.com/training/data-storage/room/async- queries?authuser=2
//[Accessed 26 April 2025].
//Raikwar, A., 2024. Ge=ng Started with Room Database in Android. [Online]
//Available at: hRps://amitraikwar.medium.com/ge[ng-started-with-room-database-in- android-fa1ca23ce21e
//[Accessed 27 April 2025].
//Raikwar, A., 2023. Ge=ng Started with Room Database in Android. [Online]
//Available at: hRps://developer.android.com/develop#core-areas
//[Accessed 28 April 2025].
//Cal, C. W., 2023. Room Database Android Studio Kotlin Example Tutorial. [Online] Available at: hRps://youtu.be/-LNg-K7SncM?si=y8cbMdvhhp48Pp9-
//[Accessed 27 April 2025].
//College, I. V., 2025. PROG7313 Module-Manual / Module-Outline. Pretoria: Varsity College Pretoria.
//Viegen, F. v., 2022. A PracKcal introducKon to Android Room-3 : EnKty, Dao and Database objects.. [Online]
//Available at: hRps://youtu.be/RstQg7f4Edk?si=8RoAGp-OKPpMNVdY
//[Accessed 28 April 2025].