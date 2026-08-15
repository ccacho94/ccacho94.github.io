package com.example.weighttrackingapp_chelseacacho.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weighttrackingapp_chelseacacho.R;
import com.example.weighttrackingapp_chelseacacho.data.DatabaseHelper;

public class NewAccountActivity extends AppCompatActivity {

    private EditText etFullName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;

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

        btnCreateAccount.setOnClickListener(view -> createAccount());

        tvBackToLogin.setOnClickListener(view -> finish());
    }

    private void createAccount() {

        String fullName = etFullName.getText()
                .toString()
                .trim();

        String email = etEmail.getText()
                .toString()
                .trim()
                .toLowerCase();

        String password = etPassword.getText()
                .toString();

        String confirmPassword = etConfirmPassword.getText()
                .toString();

        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Enter your name");
            etFullName.requestFocus();
            return;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter your email");
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
            etPassword.setError("Enter a password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError(
                    "Password must be at least 6 characters"
            );
            etPassword.requestFocus();
            return;
        }

        // Validate password confirmation
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError(
                    "Confirm your password"
            );
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(
                    "Passwords do not match"
            );
            etConfirmPassword.requestFocus();
            return;
        }

        // Check whether the email is already registered
        if (databaseHelper.emailExists(email)) {
            etEmail.setError("Email already exists");
            etEmail.requestFocus();
            return;
        }

        // addUser returns the new row ID or -1 if insertion fails
        long userId = databaseHelper.addUser(
                fullName,
                email,
                password
        );

        if (userId != -1) {
            Toast.makeText(
                    this,
                    "Account created successfully",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        } else {
            Toast.makeText(
                    this,
                    "Unable to create account",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}