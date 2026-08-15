package com.example.weighttrackingapp_chelseacacho.view;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.weighttrackingapp_chelseacacho.R;
import com.example.weighttrackingapp_chelseacacho.model.WeightEntry;
import com.example.weighttrackingapp_chelseacacho.utility.GoalManager;
import com.example.weighttrackingapp_chelseacacho.viewmodel.WeightLogViewModel;

import java.util.List;

// Displays the weight log screen and collects user input.
public class WeightLogActivity extends AppCompatActivity {

    private TextView tvCurrentGoal;
    private EditText etGoalWeight;
    private Button btnSetGoal;

    private EditText etDate;
    private EditText etWeight;
    private Button btnAddEntry;
    private GridLayout gridEntries;

    private WeightLogViewModel viewModel;

    private int loggedInUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_log);

        initializeViews();
        loadLoggedInUser();

        viewModel = new ViewModelProvider(this)
                .get(WeightLogViewModel.class);

        observeViewModel();
        setButtonListeners();

        viewModel.loadGoal();

        if (loggedInUserId != -1) {
            viewModel.loadWeightEntries(loggedInUserId);
        } else {
            Toast.makeText(
                    this,
                    "Logged-in user could not be found.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Connects the Java variables to the XML layout.
    private void initializeViews() {
        tvCurrentGoal = findViewById(R.id.tvCurrentGoal);
        etGoalWeight = findViewById(R.id.etGoalWeight);
        btnSetGoal = findViewById(R.id.btnSetGoal);

        etDate = findViewById(R.id.etDate);
        etWeight = findViewById(R.id.etWeight);
        btnAddEntry = findViewById(R.id.btnAddEntry);

        gridEntries = findViewById(R.id.gridEntries);
        gridEntries.setColumnCount(3);
    }

    // Retrieves the saved ID of the logged-in user.
    private void loadLoggedInUser() {
        loggedInUserId = getSharedPreferences(
                "user_preferences",
                MODE_PRIVATE
        ).getInt("logged_in_user_id", -1);
    }

    // Handles the Set Goal and Add Entry buttons.
    private void setButtonListeners() {

        btnSetGoal.setOnClickListener(view -> {

            String startWeight = etWeight.getText()
                    .toString()
                    .trim();

            String targetWeight = etGoalWeight.getText()
                    .toString()
                    .trim();

            viewModel.saveGoal(startWeight, targetWeight);
        });

        btnAddEntry.setOnClickListener(view -> {

            String date = etDate.getText()
                    .toString()
                    .trim();

            String weight = etWeight.getText()
                    .toString()
                    .trim();

            viewModel.addWeightEntry(
                    loggedInUserId,
                    date,
                    weight
            );
        });
    }

    // Observes information returned by the ViewModel.
    private void observeViewModel() {

        viewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(
                        WeightLogActivity.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        viewModel.getCurrentGoal().observe(this, goalManager ->
                displayGoal(goalManager)
        );

        viewModel.getWeightEntries().observe(this, entries -> {
            if (entries != null) {
                displayWeightEntries(entries);
            }
        });

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

    // Displays the user's current goal.
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

    // Displays all saved weight entries.
    private void displayWeightEntries(List<WeightEntry> entries) {

        gridEntries.removeAllViews();

        addGridText("Date");
        addGridText("Weight");
        addGridText("Action");

        for (WeightEntry entry : entries) {

            addGridText(entry.getDate());
            addGridText(entry.getWeight() + " lbs.");

            Button deleteButton = new Button(this);
            deleteButton.setText("Delete");

            deleteButton.setOnClickListener(view -> {
                        viewModel.deleteWeightEntry(
                                entry.getEntryId(),
                                loggedInUserId
                        );
                    }
            );

            gridEntries.addView(deleteButton);
        }
    }

    // Adds text to one cell of the GridLayout.
    private void addGridText(String text) {

        TextView textView = new TextView(this);

        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(16, 16, 16, 16);

        GridLayout.LayoutParams layoutParams =
                new GridLayout.LayoutParams();

        layoutParams.width = 0;
        layoutParams.columnSpec =
                GridLayout.spec(GridLayout.UNDEFINED, 1f);

        textView.setLayoutParams(layoutParams);
        gridEntries.addView(textView);
    }
}