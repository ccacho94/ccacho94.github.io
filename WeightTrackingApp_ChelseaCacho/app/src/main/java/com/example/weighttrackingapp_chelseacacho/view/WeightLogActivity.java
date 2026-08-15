package com.example.weighttrackingapp_chelseacacho.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.weighttrackingapp_chelseacacho.R;
import com.example.weighttrackingapp_chelseacacho.utility.GoalManager;
import com.example.weighttrackingapp_chelseacacho.viewmodel.WeightLogViewModel;

// Displays the weight log screen and collects user input.
public class WeightLogActivity extends AppCompatActivity {

    // Goal views.
    private TextView tvCurrentGoal;
    private EditText etGoalWeight;
    private Button btnSetGoal;

    // Weight-entry views.
    private EditText etDate;
    private EditText etWeight;
    private Button btnAddEntry;

    // Connects the Activity to the application logic and data.
    private WeightLogViewModel viewModel;

    // ID of the currently logged-in user.
    private int loggedInUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_log);

        // Connect Java variables to the XML views.
        initializeViews();

        // Retrieve the logged-in user's ID.
        loadLoggedInUser();

        // Create the ViewModel.
        viewModel = new ViewModelProvider(this)
                .get(WeightLogViewModel.class);

        // Observe information returned by the ViewModel.
        observeViewModel();

        // Set up button actions.
        setButtonListeners();

        // Load the user's saved goal and weight entries.
        viewModel.loadGoal();
        viewModel.loadWeightEntries(loggedInUserId);
    }

    // Connects each Java variable to its matching XML view.
    private void initializeViews() {
        tvCurrentGoal = findViewById(R.id.tvCurrentGoal);
        etGoalWeight = findViewById(R.id.etGoalWeight);
        btnSetGoal = findViewById(R.id.btnSetGoal);

        etDate = findViewById(R.id.etDate);
        etWeight = findViewById(R.id.etWeight);
        btnAddEntry = findViewById(R.id.btnAddEntry);
    }

    // Retrieves the user ID that was saved after login.
    private void loadLoggedInUser() {
        loggedInUserId = getSharedPreferences(
                "user_preferences",
                MODE_PRIVATE
        ).getInt("logged_in_user_id", -1);
    }

    // Handles the Set Goal and Add Entry buttons.
    private void setButtonListeners() {

        btnSetGoal.setOnClickListener(view -> {

            // Uses the current weight as the starting weight.
            String startWeight = etWeight.getText()
                    .toString()
                    .trim();

            String targetWeight = etGoalWeight.getText()
                    .toString()
                    .trim();

            // Sends the values to the ViewModel for validation and saving.
            viewModel.saveGoal(startWeight, targetWeight);
        });

        btnAddEntry.setOnClickListener(view -> {

            String date = etDate.getText()
                    .toString()
                    .trim();

            String weight = etWeight.getText()
                    .toString()
                    .trim();

            // Sends the entry to the ViewModel.
            viewModel.addWeightEntry(
                    loggedInUserId,
                    date,
                    weight
            );
        });
    }

    // Observes data and messages returned by the ViewModel.
    private void observeViewModel() {

        // Displays success and error messages.
        viewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(
                        WeightLogActivity.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Displays the saved goal.
        viewModel.getCurrentGoal().observe(this, goalManager -> {
            displayGoal(goalManager);
        });

        // Receives the updated list of weight entries.
        viewModel.getWeightEntries().observe(this, entries -> {
            if (entries != null) {
                displayWeightEntries(entries);
            }
        });

        // Responds when the user reaches the saved goal.
        viewModel.getGoalReached().observe(this, reached -> {
            if (Boolean.TRUE.equals(reached)) {
                Toast.makeText(
                        WeightLogActivity.this,
                        "Congratulations! You reached your weight goal.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // Updates the goal TextView.
    private void displayGoal(GoalManager goalManager) {

        if (goalManager == null) {
            tvCurrentGoal.setText("Current Goal: --");
            return;
        }

        double targetWeight = goalManager.getTargetWeight();

        tvCurrentGoal.setText(
                "Current Goal: " + targetWeight + " lbs."
        );
    }
}