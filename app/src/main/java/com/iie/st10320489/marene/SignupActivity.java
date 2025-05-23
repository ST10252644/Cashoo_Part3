package com.iie.st10320489.marene;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.iie.st10320489.marene.ui.onboarding.OnboardingActivity;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    // Method to check if a password is strong: at least 8 characters, includes uppercase, digit, and special character
    private boolean isPasswordStrong(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[!@#\\$%^&*].*");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        Button signupButton = findViewById(R.id.signupLoginButton);
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText surnameEditText = findViewById(R.id.surnameEditText);
        EditText emailEditText = findViewById(R.id.signupEmailEditText);
        EditText passwordEditText = findViewById(R.id.createPasswordEditText);
        EditText confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        ImageView toggleCreatePassword = findViewById(R.id.toggleCreatePassword);
        ImageView toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);

        final boolean[] isCreatePasswordVisible = {false};
        final boolean[] isConfirmPasswordVisible = {false};

        // Toggle visibility of "Create Password"
        toggleCreatePassword.setOnClickListener(v -> {
            if (isCreatePasswordVisible[0]) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleCreatePassword.setImageResource(R.drawable.ic_eye_off);
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleCreatePassword.setImageResource(R.drawable.ic_eye);
            }
            passwordEditText.setSelection(passwordEditText.length());
            isCreatePasswordVisible[0] = !isCreatePasswordVisible[0];
        });

        // Toggle visibility of "Confirm Password"
        toggleConfirmPassword.setOnClickListener(v -> {
            if (isConfirmPasswordVisible[0]) {
                confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_off);
            } else {
                confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye);
            }
            confirmPasswordEditText.setSelection(confirmPasswordEditText.length());
            isConfirmPasswordVisible[0] = !isConfirmPasswordVisible[0];
        });

        signupButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String surname = surnameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(SignupActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(SignupActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            } else if (!isPasswordStrong(password)) {
                Toast.makeText(SignupActivity.this, "Password is not complex enough. Use 8+ chars, uppercase, number, and symbol.", Toast.LENGTH_LONG).show();
            } else {
                // Firebase create user
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // User created successfully
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    // Optionally update user profile with display name (name + surname)
                                    String displayName = name + " " + surname;

                                    // Save email and UID in SharedPreferences
                                    SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("currentUserEmail", firebaseUser.getEmail());
                                    editor.putString("currentUserId", firebaseUser.getUid());
                                    editor.apply();

                                    Toast.makeText(SignupActivity.this, "Account created!", Toast.LENGTH_SHORT).show();

                                    // Navigate to onboarding
                                    Intent intent = new Intent(SignupActivity.this, OnboardingActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                // Registration failed - display error message
                                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                                Toast.makeText(SignupActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        TextView signInText = findViewById(R.id.signUpText);
        signInText.setOnClickListener(v -> startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
    }
}
