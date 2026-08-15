package com.example.weighttrackingapp_chelseacacho;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class new_account extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnCreateAccount;
    private TextView tvBackToLogin;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        databaseHelper = new DatabaseHelper(this);

        btnCreateAccount.setOnClickListener(v -> createAccount());

        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void createAccount() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Enter your name");
            etFullName.requestFocus();
            return;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Save to SQLite
        boolean inserted = databaseHelper.registerUser(fullName, email, password);

        if (inserted) {
            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();

            // Optional: clear fields before going back
            etFullName.setText("");
            etEmail.setText("");
            etPassword.setText("");
            etConfirmPassword.setText("");

            // Return to Login screen
            finish();
        } else {
            etEmail.setError("Email already exists");
            etEmail.requestFocus();
        }
    }
}