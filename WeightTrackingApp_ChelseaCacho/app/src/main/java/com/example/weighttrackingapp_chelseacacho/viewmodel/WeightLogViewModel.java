package com.example.weighttrackingapp_chelseacacho.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.weighttrackingapp_chelseacacho.data.GoalPreferences;
import com.example.weighttrackingapp_chelseacacho.model.WeightEntry;
import com.example.weighttrackingapp_chelseacacho.model.WeightTrendResult;
import com.example.weighttrackingapp_chelseacacho.repository.WeightRepository;
import com.example.weighttrackingapp_chelseacacho.utility.GoalManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Handles weight-entry, goal, sorting, and trend logic.
public class WeightLogViewModel extends AndroidViewModel {

    // Required date format for stored weight entries.
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter
                    .ofPattern("MM/dd/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    // Repository used for weight database operations.
    private final WeightRepository repository;

    // Saves and retrieves goal information using SharedPreferences.
    private final GoalPreferences goalPreferences;

    // List of weight records displayed by the Activity.
    private final MutableLiveData<List<WeightEntry>> weightEntries =
            new MutableLiveData<>(new ArrayList<>());

    // Result from comparing the two newest weight records.
    private final MutableLiveData<WeightTrendResult> weightTrend =
            new MutableLiveData<>(
                    WeightTrendResult.insufficientData()
            );

    // Messages displayed by the Activity.
    private final MutableLiveData<String> message =
            new MutableLiveData<>();

    // Current saved goal.
    private final MutableLiveData<GoalManager> currentGoal =
            new MutableLiveData<>();

    // Tells the Activity when the user reaches the goal.
    private final MutableLiveData<Boolean> goalReached =
            new MutableLiveData<>(false);

    public WeightLogViewModel(
            @NonNull Application application) {

        super(application);

        repository = new WeightRepository(application);
        goalPreferences = new GoalPreferences(application);

        loadGoal();
    }

    // Returns the weight list for the Activity to observe.
    public LiveData<List<WeightEntry>> getWeightEntries() {
        return weightEntries;
    }

    // Returns the calculated trend for the Activity to observe.
    public LiveData<WeightTrendResult> getWeightTrend() {
        return weightTrend;
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
        GoalManager savedGoal =
                goalPreferences.getGoalManager();

        currentGoal.setValue(savedGoal);
    }

    // Validates and saves a new goal.
    public void saveGoal(
            String startWeightText,
            String targetWeightText) {

        if (startWeightText == null ||
                startWeightText.trim().isEmpty()) {

            message.setValue(
                    "Please enter your starting weight."
            );
            return;
        }

        if (targetWeightText == null ||
                targetWeightText.trim().isEmpty()) {

            message.setValue(
                    "Please enter your target weight."
            );
            return;
        }

        try {
            double startWeight =
                    Double.parseDouble(
                            startWeightText.trim()
                    );

            double targetWeight =
                    Double.parseDouble(
                            targetWeightText.trim()
                    );

            if (startWeight <= 0 ||
                    targetWeight <= 0) {

                message.setValue(
                        "Starting and target weights must be greater than zero."
                );
                return;
            }

            if (startWeight > 1000 ||
                    targetWeight > 1000) {

                message.setValue(
                        "Please enter valid weight values."
                );
                return;
            }

            goalPreferences.saveGoal(
                    startWeight,
                    targetWeight
            );

            loadGoal();

            goalReached.setValue(false);

            message.setValue(
                    "Goal saved successfully."
            );

        } catch (NumberFormatException exception) {

            message.setValue(
                    "Weights must be valid numbers."
            );
        }
    }

    // Removes the saved goal.
    public void clearGoal() {

        goalPreferences.clearGoal();

        currentGoal.setValue(null);
        goalReached.setValue(false);

        message.setValue("Goal cleared.");
    }

    // Loads, sorts, analyzes, and displays the user's entries.
    public void loadWeightEntries(int userId) {

        if (userId < 0) {

            weightEntries.setValue(
                    new ArrayList<>()
            );

            weightTrend.setValue(
                    WeightTrendResult.insufficientData()
            );

            message.setValue(
                    "Logged-in user could not be found."
            );

            return;
        }

        /*
         * The repository checks its HashMap first.
         * The user ID is used as the HashMap key.
         */
        List<WeightEntry> storedEntries =
                repository.getWeightEntries(userId);

        /*
         * Custom MergeSort orders the entries
         * from oldest to newest.
         *
         * Runtime: O(n log n)
         */
        List<WeightEntry> chronologicalEntries =
                mergeSortChronologically(
                        storedEntries
                );

        // Compares the newest two entries.
        calculateWeightTrend(
                chronologicalEntries
        );

        /*
         * The trend calculation needs oldest-to-newest order,
         * but the screen displays newest entries first.
         */
        List<WeightEntry> newestFirst =
                new ArrayList<>(
                        chronologicalEntries
                );

        Collections.reverse(newestFirst);

        weightEntries.setValue(newestFirst);
    }

    // Validates and saves a new weight entry.
    public void addWeightEntry(
            int userId,
            String date,
            String weightText) {

        if (userId < 0) {

            message.setValue(
                    "Logged-in user could not be found."
            );
            return;
        }

        String cleanedDate =
                date == null
                        ? ""
                        : date.trim();

        if (!isValidDate(cleanedDate)) {

            message.setValue(
                    "Enter a valid date using MM/DD/YYYY."
            );
            return;
        }

        if (weightText == null ||
                weightText.trim().isEmpty()) {

            message.setValue(
                    "Please enter a weight."
            );
            return;
        }

        try {
            double weight =
                    Double.parseDouble(
                            weightText.trim()
                    );

            if (weight <= 0 ||
                    weight > 1000) {

                message.setValue(
                        "Please enter a valid weight."
                );
                return;
            }

            boolean success =
                    repository.addWeightEntry(
                            userId,
                            cleanedDate,
                            weight
                    );

            if (success) {

                /*
                 * Reloading the records sorts them and
                 * recalculates the trend automatically.
                 */
                loadWeightEntries(userId);

                checkGoalReached(weight);

                message.setValue(
                        "Weight entry added and trend updated."
                );

            } else {

                message.setValue(
                        "Weight entry could not be added."
                );
            }

        } catch (NumberFormatException exception) {

            message.setValue(
                    "Weight must be a valid number."
            );
        }
    }

    // Deletes an entry and recalculates the remaining trend.
    public void deleteWeightEntry(
            int entryId,
            int userId) {

        if (entryId < 0 ||
                userId < 0) {

            message.setValue(
                    "Unable to delete the weight entry."
            );
            return;
        }

        boolean deleted =
                repository.deleteWeightEntry(
                        entryId
                );

        if (deleted) {

            loadWeightEntries(userId);

            message.setValue(
                    "Weight entry deleted and trend updated."
            );

        } else {

            message.setValue(
                    "Weight entry could not be deleted."
            );
        }
    }

    // Strictly validates dates using MM/DD/YYYY.
    private boolean isValidDate(String date) {

        if (date == null ||
                date.isEmpty()) {

            return false;
        }

        try {
            LocalDate.parse(
                    date,
                    DATE_FORMATTER
            );

            return true;

        } catch (DateTimeParseException exception) {

            return false;
        }
    }

    /*
     * Returns a sorted copy of the list using MergeSort.
     *
     * Runtime: O(n log n)
     * Extra space: O(n)
     */
    private List<WeightEntry> mergeSortChronologically(
            List<WeightEntry> entries) {

        if (entries == null ||
                entries.isEmpty()) {

            return new ArrayList<>();
        }

        // Copies the list so the original is not changed.
        List<WeightEntry> sortedEntries =
                new ArrayList<>(entries);

        mergeSort(
                sortedEntries,
                0,
                sortedEntries.size() - 1
        );

        return sortedEntries;
    }

    // Recursively divides the list into smaller sections.
    private void mergeSort(
            List<WeightEntry> entries,
            int left,
            int right) {

        if (left >= right) {
            return;
        }

        int middle =
                left + (right - left) / 2;

        mergeSort(
                entries,
                left,
                middle
        );

        mergeSort(
                entries,
                middle + 1,
                right
        );

        merge(
                entries,
                left,
                middle,
                right
        );
    }

    // Merges two sorted sections into chronological order.
    private void merge(
            List<WeightEntry> entries,
            int left,
            int middle,
            int right) {

        List<WeightEntry> leftHalf =
                new ArrayList<>(
                        entries.subList(
                                left,
                                middle + 1
                        )
                );

        List<WeightEntry> rightHalf =
                new ArrayList<>(
                        entries.subList(
                                middle + 1,
                                right + 1
                        )
                );

        int leftIndex = 0;
        int rightIndex = 0;
        int mergedIndex = left;

        while (leftIndex < leftHalf.size() &&
                rightIndex < rightHalf.size()) {

            WeightEntry leftEntry =
                    leftHalf.get(leftIndex);

            WeightEntry rightEntry =
                    rightHalf.get(rightIndex);

            if (compareEntries(
                    leftEntry,
                    rightEntry) <= 0) {

                entries.set(
                        mergedIndex,
                        leftEntry
                );

                leftIndex++;

            } else {

                entries.set(
                        mergedIndex,
                        rightEntry
                );

                rightIndex++;
            }

            mergedIndex++;
        }

        // Adds remaining entries from the left half.
        while (leftIndex < leftHalf.size()) {

            entries.set(
                    mergedIndex,
                    leftHalf.get(leftIndex)
            );

            leftIndex++;
            mergedIndex++;
        }

        // Adds remaining entries from the right half.
        while (rightIndex < rightHalf.size()) {

            entries.set(
                    mergedIndex,
                    rightHalf.get(rightIndex)
            );

            rightIndex++;
            mergedIndex++;
        }
    }

    // Compares dates and then IDs when the dates match.
    private int compareEntries(
            WeightEntry first,
            WeightEntry second) {

        LocalDate firstDate =
                parseStoredDate(
                        first.getDate()
                );

        LocalDate secondDate =
                parseStoredDate(
                        second.getDate()
                );

        int dateComparison =
                firstDate.compareTo(
                        secondDate
                );

        if (dateComparison != 0) {
            return dateComparison;
        }

        /*
         * Entry IDs provide consistent ordering
         * when two records use the same date.
         */
        return Integer.compare(
                first.getEntryId(),
                second.getEntryId()
        );
    }

    // Parses stored dates without crashing on an invalid old record.
    private LocalDate parseStoredDate(
            String date) {

        try {
            return LocalDate.parse(
                    date,
                    DATE_FORMATTER
            );

        } catch (DateTimeParseException exception) {

            return LocalDate.MIN;
        }
    }

    // Compares the newest entry with the previous entry.
    private void calculateWeightTrend(
            List<WeightEntry> chronologicalEntries) {

        if (chronologicalEntries == null ||
                chronologicalEntries.size() < 2) {

            weightTrend.setValue(
                    WeightTrendResult.insufficientData()
            );

            return;
        }

        int newestIndex =
                chronologicalEntries.size() - 1;

        WeightEntry previousEntry =
                chronologicalEntries.get(
                        newestIndex - 1
                );

        WeightEntry newestEntry =
                chronologicalEntries.get(
                        newestIndex
                );

        int comparison =
                Double.compare(
                        newestEntry.getWeight(),
                        previousEntry.getWeight()
                );

        WeightTrendResult.Direction direction;

        if (comparison > 0) {

            direction =
                    WeightTrendResult.Direction.INCREASING;

        } else if (comparison < 0) {

            direction =
                    WeightTrendResult.Direction.DECREASING;

        } else {

            direction =
                    WeightTrendResult.Direction.STABLE;
        }

        weightTrend.setValue(
                WeightTrendResult.of(
                        direction,
                        previousEntry,
                        newestEntry
                )
        );
    }

    // Checks whether the current weight reached the saved goal.
    private void checkGoalReached(
            double currentWeight) {

        GoalManager goalManager =
                goalPreferences.getGoalManager();

        if (goalManager == null) {
            return;
        }

        if (goalManager.isGoalReached(
                currentWeight)) {

            goalReached.setValue(true);

            message.setValue(
                    "Congratulations! You reached your weight goal."
            );
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
    public void setGoalSmsSent(
            boolean sent) {

        goalPreferences.setGoalSmsSent(sent);
    }
}