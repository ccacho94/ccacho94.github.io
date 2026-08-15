package com.example.weighttrackingapp_chelseacacho.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.weighttrackingapp_chelseacacho.data.GoalPreferences;
import com.example.weighttrackingapp_chelseacacho.R;

public class NotificationActivity extends AppCompatActivity {

    private EditText editTextPhone;
    private Switch toggleButton;
    private Button btnSkipSms;

    private SharedPreferences prefs;

    public static final String PREFS_NAME = "WeightAppPrefs";
    public static final String KEY_SMS_ENABLED = "sms_enabled";
    public static final String KEY_SMS_PHONE = "sms_phone";
    public static final String KEY_SMS_PROMPT_HANDLED = "sms_prompt_handled";

    private boolean isLoadingState = false;

    private final ActivityResultLauncher<String> requestSmsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                markSmsPromptHandled();

                String phone = getCleanPhone();

                if (isGranted) {
                    saveSmsEnabled(true);
                    savePhone();
                    GoalPreferences.saveSmsSettings(this, true, phone);

                    Toast.makeText(this, "SMS alerts enabled", Toast.LENGTH_SHORT).show();
                } else {
                    // Permission denied -> app still works normally
                    toggleButton.setChecked(false);
                    saveSmsEnabled(false);
                    GoalPreferences.saveSmsSettings(this, false, phone);

                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
                }

                goToWeightLog();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        editTextPhone = findViewById(R.id.editTextPhone);
        toggleButton = findViewById(R.id.toggleButton);

        // If this button exists in your XML, it will connect.
        // If not, comment this line out.
        btnSkipSms = findViewById(R.id.btnSkipSms);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        loadSavedValues();

        // Save phone when focus leaves the phone field
        editTextPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                savePhone();

                // Keep GoalPreferences in sync too
                GoalPreferences.saveSmsSettings(
                        this,
                        prefs.getBoolean(KEY_SMS_ENABLED, false),
                        getCleanPhone()
                );
            }
        });

        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Prevent trigger during loadSavedValues()
            if (isLoadingState) return;

            if (isChecked) {
                // User is trying to enable SMS alerts
                String phone = getCleanPhone();

                if (TextUtils.isEmpty(phone)) {
                    toggleButton.setChecked(false);
                    editTextPhone.setError("Enter phone number first");
                    editTextPhone.requestFocus();
                    return;
                }

                savePhone();

                if (hasSmsPermission()) {
                    // Permission already granted
                    markSmsPromptHandled();
                    saveSmsEnabled(true);
                    GoalPreferences.saveSmsSettings(this, true, phone);

                    Toast.makeText(this, "SMS alerts enabled", Toast.LENGTH_SHORT).show();
                    goToWeightLog();
                } else {
                    // Ask for permission now
                    requestSmsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
                }
            } else {
                // User turned SMS off / chose not to enable it
                markSmsPromptHandled();
                saveSmsEnabled(false);
                savePhone(); // keep phone saved if they typed it
                GoalPreferences.saveSmsSettings(this, false, getCleanPhone());

                Toast.makeText(this, "SMS alerts disabled", Toast.LENGTH_SHORT).show();
                goToWeightLog();
            }
        });

        // Skip button
        if (btnSkipSms != null) {
            btnSkipSms.setOnClickListener(v -> {
                markSmsPromptHandled();
                saveSmsEnabled(false);
                savePhone();
                GoalPreferences.saveSmsSettings(this, false, getCleanPhone());

                Toast.makeText(this, "SMS setup skipped", Toast.LENGTH_SHORT).show();
                goToWeightLog();
            });
        }
    }

    private void loadSavedValues() {
        String phone = prefs.getString(KEY_SMS_PHONE, "");
        boolean smsEnabled = prefs.getBoolean(KEY_SMS_ENABLED, false);

        isLoadingState = true;

        editTextPhone.setText(phone);

        // Only keep switch ON if permission is actually granted
        toggleButton.setChecked(smsEnabled && hasSmsPermission());

        isLoadingState = false;
    }

    private void savePhone() {
        String phone = getCleanPhone();
        prefs.edit().putString(KEY_SMS_PHONE, phone).apply();
    }

    private void saveSmsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SMS_ENABLED, enabled).apply();
    }

    private void markSmsPromptHandled() {
        prefs.edit().putBoolean(KEY_SMS_PROMPT_HANDLED, true).apply();
    }

    private String getCleanPhone() {
        String raw = editTextPhone.getText().toString().trim();
        return PhoneNumberUtils.stripSeparators(raw);
    }

    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void goToWeightLog() {
        Intent intent = new Intent(NotificationActivity.this, WeightLogActivity.class);
        startActivity(intent);
        finish();
    }
}