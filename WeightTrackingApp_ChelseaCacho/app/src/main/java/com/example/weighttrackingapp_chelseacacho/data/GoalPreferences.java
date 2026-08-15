package com.example.weighttrackingapp_chelseacacho.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.weighttrackingapp_chelseacacho.R;
import com.example.weighttrackingapp_chelseacacho.viewmodel.LoginViewModel;

// Displays the login screen and collects the user's login information.
public class MainActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnCreateAccount;

    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        viewModel = new ViewModelProvider(this)
                .get(LoginViewModel.class);

        observeViewModel();
        setButtonListeners();
    }

    // Connects the Java variables to the XML layout.
    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
    }

    // Handles the login and create-account buttons.
    private void setButtonListeners() {

        btnLogin.setOnClickListener(view -> {

            String email = etEmail.getText()
                    .toString()
                    .trim();

            String password = etPassword.getText()
                    .toString()
                    .trim();

            viewModel.login(email, password);
        });

        btnCreateAccount.setOnClickListener(view -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    NewAccountActivity.class
            );

            startActivity(intent);
        });
    }

    // Observes login information returned by the ViewModel.
    private void observeViewModel() {

        viewModel.getMessage().observe(this, message -> {

            if (message != null && !message.isEmpty()) {
                Toast.makeText(
                        MainActivity.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        viewModel.getLoggedInUserId().observe(this, userId -> {

            if (userId != null && userId != -1) {

                saveLoggedInUser(userId);

                Intent intent = new Intent(
                        MainActivity.this,
                        WeightLogActivity.class
                );

                startActivity(intent);
                finish();
            }
        });
    }

    // Saves the logged-in user's ID for the WeightLogActivity.
    private void saveLoggedInUser(int userId) {

        SharedPreferences preferences =
                getSharedPreferences(
                        "user_preferences",
                        MODE_PRIVATE
                );

        preferences.edit()
                .putInt("logged_in_user_id", userId)
                .putBoolean("is_logged_in", true)
                .apply();
    }
}