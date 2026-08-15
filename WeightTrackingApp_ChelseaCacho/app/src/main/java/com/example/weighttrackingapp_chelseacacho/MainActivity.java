package com.example.weighttrackingapp_chelseacacho;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText editUserName, editTextPassword;
    private DatabaseHelper databaseHelper;

    private static final String PREFS_NAME = "WeightAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LOGGED_IN_USER_ID = "logged_in_user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_SMS_PROMPT_HANDLED = "sms_prompt_handled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Views from activity_main.xml
        editUserName = findViewById(R.id.editUserName);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonNewAccount = findViewById(R.id.buttonNewAccount);
        Button buttonForgot = findViewById(R.id.buttonForgot);

        databaseHelper = new DatabaseHelper(this);

        // Login button
        buttonLogin.setOnClickListener(v -> loginUser());

        // Sign Up button
        buttonNewAccount.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, new_account.class);
            startActivity(intent);
        });

        // Forgot Password (placeholder)
        buttonForgot.setOnClickListener(v ->
                Toast.makeText(MainActivity.this, "Forgot Password feature coming soon", Toast.LENGTH_SHORT).show()
        );
    }

    private void loginUser() {
        String email = editUserName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editUserName.setError("Enter email");
            editUserName.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editUserName.setError("Enter a valid email");
            editUserName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Enter password");
            editTextPassword.requestFocus();
            return;
        }

        boolean isValid = databaseHelper.checkUserLogin(email, password);

        if (isValid) {
            int userId = databaseHelper.getUserIdByEmail(email);

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit()
                    .putBoolean(KEY_IS_LOGGED_IN, true)
                    .putInt(KEY_LOGGED_IN_USER_ID, userId)
                    .putString(KEY_USER_EMAIL, email)
                    .apply();

            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

            boolean smsPromptHandled = prefs.getBoolean(KEY_SMS_PROMPT_HANDLED, false);

            Intent intent;
            if (!smsPromptHandled) {
                // Show SMS setup screen only once if not addressed yet
                intent = new Intent(MainActivity.this, notification.class);
            } else {
                // Go straight to app if already addressed
                intent = new Intent(MainActivity.this, weight_log.class);
            }

            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }
}