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
import com.example.weighttrackingapp_chelseacacho.model.WeightTrendResult;
import com.example.weighttrackingapp_chelseacacho.utility.GoalManager;
import com.example.weighttrackingapp_chelseacacho.viewmodel.WeightLogViewModel;

import java.util.List;
import java.util.Locale;

// Displays the weight log, saved entries, goal, and calculated trend.
public class WeightLogActivity extends AppCompatActivity {

    // Goal and trend views
    private TextView tvCurrentGoal;
    private TextView tvWeightTrend;

    // User input fields
    private EditText etGoalWeight;
    private EditText etDate;
    private EditText etWeight;

    // Displays saved weight entries
    private GridLayout gridEntries;

    // Connects the Activity to application logic
    private WeightLogViewModel viewModel;

    // ID of the currently logged-in user
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
        viewModel.loadWeightEntries(loggedInUserId);
    }

    // Connects Java variables to XML views
    private void initializeViews() {
        tvCurrentGoal = findViewById(R.id.tvCurrentGoal);
        tvWeightTrend = findViewById(R.id.tvWeightTrend);

        etGoalWeight = findViewById(R.id.etGoalWeight);
        etDate = findViewById(R.id.etDate);
        etWeight = findViewById(R.id.etWeight);

        gridEntries = findViewById(R.id.gridEntries);
        gridEntries.setColumnCount(3);
    }

    // Retrieves the user ID saved during login
    private void loadLoggedInUser() {
        loggedInUserId = getSharedPreferences(
                "user_preferences",
                MODE_PRIVATE
        ).getInt("logged_in_user_id", -1);
    }

    // Handles the Set Goal and Add Entry buttons
    private void setButtonListeners() {
        Button btnSetGoal = findViewById(R.id.btnSetGoal);
        Button btnAddEntry = findViewById(R.id.btnAddEntry);

        btnSetGoal.setOnClickListener(view -> {
            String startWeight = etWeight.getText()
                    .toString()
                    .trim();

            String targetWeight = etGoalWeight.getText()
                    .toString()
                    .trim();

            viewModel.saveGoal(
                    startWeight,
                    targetWeight
            );
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

    // Observes information returned by the ViewModel
    private void observeViewModel() {

        // Displays success and error messages
        viewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(
                        WeightLogActivity.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Displays the saved goal
        viewModel.getCurrentGoal().observe(
                this,
                this::displayGoal
        );

        // Displays the calculated weight trend
        viewModel.getWeightTrend().observe(
                this,
                this::displayTrend
        );

        // Displays the user's saved weight entries
        viewModel.getWeightEntries().observe(this, entries -> {
            if (entries != null) {
                displayWeightEntries(entries);
            }
        });

        // Displays a message when the goal is reached
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

    // Updates the goal TextView
    private void displayGoal(GoalManager goalManager) {
        if (goalManager == null) {
            tvCurrentGoal.setText("Current Goal: --");
            return;
        }

        tvCurrentGoal.setText(
                String.format(
                        Locale.US,
                        "Current Goal: %.1f lbs.",
                        goalManager.getTargetWeight()
                )
        );
    }

    // Displays whether the user's weight is increasing,
    // decreasing, or staying the same
    private void displayTrend(WeightTrendResult result) {
        if (result == null ||
                result.getDirection()
                        == WeightTrendResult.Direction.INSUFFICIENT_DATA) {

            tvWeightTrend.setText(
                    "Trend: Add at least two weight entries."
            );
            return;
        }

        double amountChanged = Math.abs(
                result.getDifference()
        );

        String trendText;

        switch (result.getDirection()) {

            case INCREASING:
                trendText = String.format(
                        Locale.US,
                        "Trend: Increasing by %.1f lbs. from %s to %s.",
                        amountChanged,
                        result.getPreviousDate(),
                        result.getCurrentDate()
                );
                break;

            case DECREASING:
                trendText = String.format(
                        Locale.US,
                        "Trend: Decreasing by %.1f lbs. from %s to %s.",
                        amountChanged,
                        result.getPreviousDate(),
                        result.getCurrentDate()
                );
                break;

            case STABLE:
                trendText = String.format(
                        Locale.US,
                        "Trend: Staying the same at %.1f lbs. from %s to %s.",
                        result.getCurrentWeight(),
                        result.getPreviousDate(),
                        result.getCurrentDate()
                );
                break;

            default:
                trendText = "Trend: Not enough information.";
                break;
        }

        tvWeightTrend.setText(trendText);
    }

    // Displays the weight records in the GridLayout
    private void displayWeightEntries(
            List<WeightEntry> entries) {

        // Removes old rows before rebuilding the grid
        gridEntries.removeAllViews();

        for (WeightEntry entry : entries) {

            // Date column
            addGridText(
                    entry.getDate(),
                    1.2f
            );

            // Weight column
            addGridText(
                    String.format(
                            Locale.US,
                            "%.1f lbs.",
                            entry.getWeight()
                    ),
                    1.0f
            );

            // Delete column
            Button deleteButton = new Button(this);
            deleteButton.setText("Delete");

            deleteButton.setOnClickListener(view ->
                    viewModel.deleteWeightEntry(
                            entry.getEntryId(),
                            loggedInUserId
                    )
            );

            GridLayout.LayoutParams buttonParameters =
                    new GridLayout.LayoutParams();

            buttonParameters.width = 0;
            buttonParameters.columnSpec = GridLayout.spec(
                    GridLayout.UNDEFINED,
                    0.9f
            );

            deleteButton.setLayoutParams(buttonParameters);
            gridEntries.addView(deleteButton);
        }
    }

    // Creates a TextView for a GridLayout column
    private void addGridText(
            String text,
            float columnWeight) {

        TextView textView = new TextView(this);

        textView.setText(text);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setPadding(
                12,
                16,
                12,
                16
        );

        GridLayout.LayoutParams layoutParameters =
                new GridLayout.LayoutParams();

        layoutParameters.width = 0;
        layoutParameters.columnSpec = GridLayout.spec(
                GridLayout.UNDEFINED,
                columnWeight
        );

        textView.setLayoutParams(layoutParameters);
        gridEntries.addView(textView);
    }
}