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

/** Displays the login screen and opens the user's weight log. */
public class MainActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        observeViewModel();

        btnLogin.setOnClickListener(view -> viewModel.login(
                etEmail.getText().toString().trim(),
                etPassword.getText().toString()
        ));

        btnCreateAccount.setOnClickListener(view -> startActivity(
                new Intent(MainActivity.this, NewAccountActivity.class)
        ));
    }

    private void observeViewModel() {
        viewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoggedInUserId().observe(this, userId -> {
            if (userId == null || userId < 0) {
                return;
            }

            SharedPreferences preferences = getSharedPreferences(
                    "user_preferences",
                    MODE_PRIVATE
            );

            preferences.edit()
                    .putInt("logged_in_user_id", userId)
                    .putBoolean("is_logged_in", true)
                    .apply();

            startActivity(new Intent(
                    MainActivity.this,
                    NotificationActivity.class
            ));

            finish();
        });
    }
}