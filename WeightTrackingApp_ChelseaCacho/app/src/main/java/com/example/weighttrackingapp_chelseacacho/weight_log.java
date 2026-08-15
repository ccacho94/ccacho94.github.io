package com.example.weighttrackingapp_chelseacacho;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class weight_log extends AppCompatActivity {

    private TextView tvCurrentGoal;
    private EditText etGoalWeight, etDate, etWeight;
    private GridLayout gridEntries;

    private DatabaseHelper databaseHelper;
    private int loggedInUserId = -1;

    private static final String PREFS_NAME = "WeightAppPrefs";
    private static final String KEY_LOGGED_IN_USER_ID = "logged_in_user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_log);

        // Match XML IDs
        tvCurrentGoal = findViewById(R.id.tvCurrentGoal);
        etGoalWeight = findViewById(R.id.etGoalWeight);
        Button btnSetGoal = findViewById(R.id.btnSetGoal);

        etDate = findViewById(R.id.etDate);
        etWeight = findViewById(R.id.etWeight);
        Button btnAddEntry = findViewById(R.id.btnAddEntry);

        gridEntries = findViewById(R.id.gridEntries);

        databaseHelper = new DatabaseHelper(this);

        // Get logged in user id from SharedPreferences (saved in MainActivity after login)
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loggedInUserId = prefs.getInt(KEY_LOGGED_IN_USER_ID, -1);

        if (loggedInUserId == -1) {
            Toast.makeText(this, "User session not found. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadGoalFromDatabase();
        loadEntriesFromDatabase();

        btnSetGoal.setOnClickListener(v -> saveGoalToDatabase());
        btnAddEntry.setOnClickListener(v -> addEntryToDatabase());
    }

    @SuppressLint("SetTextI18n")
    private void loadGoalFromDatabase() {
        float goal = databaseHelper.getGoalWeightByUserId(loggedInUserId);

        if (goal > 0) {
            tvCurrentGoal.setText(String.format("Current Goal: %s lbs", goal));
        } else {
            tvCurrentGoal.setText("Current Goal: Not set");
        }
    }

    private void saveGoalToDatabase() {
        String goalText = etGoalWeight.getText().toString().trim();

        if (TextUtils.isEmpty(goalText)) {
            etGoalWeight.setError("Enter goal weight");
            etGoalWeight.requestFocus();
            return;
        }

        float goalValue;
        try {
            goalValue = Float.parseFloat(goalText);
        } catch (NumberFormatException e) {
            etGoalWeight.setError("Enter a valid number");
            etGoalWeight.requestFocus();
            return;
        }

        boolean saved = databaseHelper.saveOrUpdateGoal(loggedInUserId, goalValue);

        if (saved) {
            // Also save goal to GoalPreferences for goal checking + SMS
            Float latestWeight = getLatestWeightForUser();
            if (latestWeight != null) {
                GoalPreferences.saveGoal(this, latestWeight, goalValue);
            }

            tvCurrentGoal.setText(String.format("Current Goal: %s lbs", goalValue));
            etGoalWeight.setText("");

            if (latestWeight != null) {
                Toast.makeText(this, "Goal saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Goal saved. Add a weight entry to start tracking progress.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Failed to save goal", Toast.LENGTH_SHORT).show();
        }
    }

    private void addEntryToDatabase() {
        String date = etDate.getText().toString().trim();
        String weightText = etWeight.getText().toString().trim();

        if (TextUtils.isEmpty(date)) {
            etDate.setError("Enter date");
            etDate.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(weightText)) {
            etWeight.setError("Enter weight");
            etWeight.requestFocus();
            return;
        }

        float weightValue;
        try {
            weightValue = Float.parseFloat(weightText);
        } catch (NumberFormatException e) {
            etWeight.setError("Enter a valid number");
            etWeight.requestFocus();
            return;
        }

        boolean inserted = databaseHelper.insertWeightEntry(loggedInUserId, date, weightValue);

        if (inserted) {
            Toast.makeText(this, "Entry added", Toast.LENGTH_SHORT).show();

            etDate.setText("");
            etWeight.setText("");
            etDate.requestFocus();

            // Refresh grid from DB
            loadEntriesFromDatabase();

            // Check goal and send SMS if reached
            checkGoalAndSendSmsIfNeeded(weightValue);
        } else {
            Toast.makeText(this, "Failed to add entry", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkGoalAndSendSmsIfNeeded(double newWeight) {
        GoalManager goalManager = GoalPreferences.getGoalManager(this);

        if (goalManager == null) {
            return; // no goal saved in preferences yet
        }

        if (!goalManager.isGoalReached(newWeight)) {
            return; // not reached yet
        }

        // Prevent duplicate SMS for the same goal
        if (GoalPreferences.isGoalSmsSent(this)) {
            Toast.makeText(this, "Goal reached!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean smsEnabled = GoalPreferences.isSmsEnabled(this);
        String phone = GoalPreferences.getSmsPhone(this);

        if (smsEnabled && phone != null && !phone.trim().isEmpty()) {
            SmsHelper smsHelper = new SmsHelper(this);
            boolean sent = smsHelper.sendGoalReachedSms(phone, newWeight, goalManager);

            if (sent) {
                GoalPreferences.setGoalSmsSent(this, true);
                Toast.makeText(this, "Goal reached! SMS sent.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Goal reached! SMS not sent.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // SMS not enabled, still mark as notified to avoid repeat alerts
            GoalPreferences.setGoalSmsSent(this, true);
            Toast.makeText(this, "Goal reached!", Toast.LENGTH_SHORT).show();
        }
    }

    private Float getLatestWeightForUser() {
        ArrayList<WeightEntry> entries = databaseHelper.getWeightEntriesByUserId(loggedInUserId);

        if (entries == null || entries.isEmpty()) {
            return null;
        }

        // Assumes most recent entry is first.
        // If your DB returns oldest first, switch to entries.get(entries.size() - 1)
        return entries.get(0).getWeight();
    }

    private void loadEntriesFromDatabase() {
        // Clear current grid rows (dynamic rows only, not header)
        gridEntries.removeAllViews();

        ArrayList<WeightEntry> entries = databaseHelper.getWeightEntriesByUserId(loggedInUserId);

        for (WeightEntry entry : entries) {
            addEntryRowToGrid(entry);
        }
    }

    private void addEntryRowToGrid(WeightEntry entry) {
        // Date cell
        TextView tvDate = new TextView(this);
        tvDate.setText(entry.getDate());
        tvDate.setPadding(dp(6), dp(6), dp(6), dp(6));

        GridLayout.LayoutParams dateParams = new GridLayout.LayoutParams();
        dateParams.width = 0;
        dateParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1.2f);
        dateParams.setGravity(Gravity.FILL_HORIZONTAL);
        tvDate.setLayoutParams(dateParams);

        // Weight cell
        TextView tvWeightCell = new TextView(this);
        tvWeightCell.setText(String.valueOf(entry.getWeight()));
        tvWeightCell.setPadding(dp(6), dp(6), dp(6), dp(6));

        GridLayout.LayoutParams weightParams = new GridLayout.LayoutParams();
        weightParams.width = 0;
        weightParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        weightParams.setGravity(Gravity.FILL_HORIZONTAL);
        tvWeightCell.setLayoutParams(weightParams);

        // Delete button cell
        Button btnDelete = new Button(this);
        btnDelete.setText("Delete");
        btnDelete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        GridLayout.LayoutParams deleteParams = new GridLayout.LayoutParams();
        deleteParams.width = dp(72);
        btnDelete.setLayoutParams(deleteParams);

        // Add row to grid
        gridEntries.addView(tvDate);
        gridEntries.addView(tvWeightCell);
        gridEntries.addView(btnDelete);

        // Delete from database + UI
        btnDelete.setOnClickListener(v -> {
            boolean deleted = databaseHelper.deleteWeightEntry(entry.getId());

            if (deleted) {
                gridEntries.removeView(tvDate);
                gridEntries.removeView(tvWeightCell);
                gridEntries.removeView(btnDelete);
                Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete entry", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}