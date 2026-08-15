package com.example.weighttrackingapp_chelseacacho.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.weighttrackingapp_chelseacacho.data.GoalPreferences;
import com.example.weighttrackingapp_chelseacacho.model.WeightEntry;
import com.example.weighttrackingapp_chelseacacho.repository.WeightRepository;
import com.example.weighttrackingapp_chelseacacho.utility.GoalManager;

import java.util.List;

// Handles weight-entry and goal logic between the View and data classes.
public class WeightLogViewModel extends AndroidViewModel {

    // Repository used for weight database operations.
    private final WeightRepository repository;

    // Saves and retrieves goal information using SharedPreferences.
    private final GoalPreferences goalPreferences;

    // List of weight records displayed by the Activity.
    private final MutableLiveData<List<WeightEntry>> weightEntries =
            new MutableLiveData<>();

    // Messages displayed by the Activity.
    private final MutableLiveData<String> message =
            new MutableLiveData<>();

    // Current saved goal.
    private final MutableLiveData<GoalManager> currentGoal =
            new MutableLiveData<>();

    // Tells the Activity when the user reaches the goal.
    private final MutableLiveData<Boolean> goalReached =
            new MutableLiveData<>(false);

    public WeightLogViewModel(@NonNull Application application) {
        super(application);

        repository = new WeightRepository(application);

        // Creates access to saved goal and SMS settings.
        goalPreferences = new GoalPreferences(application);

        // Loads the goal when the ViewModel starts.
        loadGoal();
    }

    // Returns the weight list for the Activity to observe.
    public LiveData<List<WeightEntry>> getWeightEntries() {
        return weightEntries;
    }

    // Returns messages for the Activity to display.
    public LiveData<String> getMessage() {
        return message;
    }

    // Returns the saved goal.
    public LiveData<GoalManager> getCurrentGoal() {
        return currentGoal;
    }

    // Returns whether the user has reached the goal.
    public LiveData<Boolean> getGoalReached() {
        return goalReached;
    }

    // Loads the user's saved goal from GoalPreferences.
    public void loadGoal() {
        GoalManager savedGoal = goalPreferences.getGoalManager();
        currentGoal.setValue(savedGoal);
    }

    // Validates and saves a new goal.
    public void saveGoal(
            String startWeightText,
            String targetWeightText) {

        if (startWeightText == null ||
                startWeightText.trim().isEmpty()) {

            message.setValue("Please enter your starting weight.");
            return;
        }

        if (targetWeightText == null ||
                targetWeightText.trim().isEmpty()) {

            message.setValue("Please enter your target weight.");
            return;
        }

        try {
            double startWeight =
                    Double.parseDouble(startWeightText.trim());

            double targetWeight =
                    Double.parseDouble(targetWeightText.trim());

            if (startWeight <= 0 || targetWeight <= 0) {
                message.setValue(
                        "Starting and target weights must be greater than zero.");
                return;
            }

            if (startWeight > 1000 || targetWeight > 1000) {
                message.setValue("Please enter valid weight values.");
                return;
            }

            // Saves the goal in SharedPreferences.
            goalPreferences.saveGoal(startWeight, targetWeight);

            // Loads the new goal into LiveData.
            loadGoal();

            // Resets the goal-reached value for the new goal.
            goalReached.setValue(false);

            message.setValue("Goal saved successfully.");

        } catch (NumberFormatException exception) {
            message.setValue("Weights must be valid numbers.");
        }
    }

    // Removes the saved goal.
    public void clearGoal() {
        goalPreferences.clearGoal();
        currentGoal.setValue(null);
        goalReached.setValue(false);

        message.setValue("Goal cleared.");
    }

    // Loads all weight entries belonging to the user.
    public void loadWeightEntries(int userId) {
        List<WeightEntry> entries =
                repository.getWeightEntries(userId);

        weightEntries.setValue(entries);
    }

    // Validates and saves a new weight entry.
    public void addWeightEntry(
            int userId,
            String date,
            String weightText) {

        if (date == null || date.trim().isEmpty()) {
            message.setValue("Please enter a date.");
            return;
        }

        if (weightText == null || weightText.trim().isEmpty()) {
            message.setValue("Please enter a weight.");
            return;
        }

        try {
            double weight =
                    Double.parseDouble(weightText.trim());

            if (weight <= 0 || weight > 1000) {
                message.setValue("Please enter a valid weight.");
                return;
            }

            boolean success =
                    repository.addWeightEntry(userId, date, weight);

            if (success) {
                message.setValue("Weight entry added.");
                loadWeightEntries(userId);

                // Checks the new weight against the saved goal.
                checkGoalReached(weight);

            } else {
                message.setValue(
                        "Weight entry could not be added.");
            }

        } catch (NumberFormatException exception) {
            message.setValue("Weight must be a valid number.");
        }
    }

    // Checks whether the current weight reached the saved goal.
    private void checkGoalReached(double currentWeight) {
        GoalManager goalManager =
                goalPreferences.getGoalManager();

        if (goalManager == null) {
            return;
        }

        if (goalManager.isGoalReached(currentWeight)) {
            goalReached.setValue(true);
            message.setValue(
                    "Congratulations! You reached your weight goal.");
        }
    }

    // Returns whether SMS notifications are enabled.
    public boolean isSmsEnabled() {
        return goalPreferences.isSmsEnabled();
    }

    // Returns the saved SMS phone number.
    public String getSmsPhone() {
        return goalPreferences.getSmsPhone();
    }

    // Returns whether the goal SMS has already been sent.
    public boolean isGoalSmsSent() {
        return goalPreferences.isGoalSmsSent();
    }

    // Records whether the goal SMS was sent.
    public void setGoalSmsSent(boolean sent) {
        goalPreferences.setGoalSmsSent(sent);
    }
}