package com.example.weighttrackingapp_chelseacacho.repository;

import android.content.Context;

import com.example.weighttrackingapp_chelseacacho.data.DatabaseHelper;
import com.example.weighttrackingapp_chelseacacho.model.WeightEntry;

import java.util.List;

// Provides weight-entry data to the WeightLogViewModel.
public class WeightRepository {

    private final DatabaseHelper databaseHelper;

    // Creates access to the application's database.
    public WeightRepository(Context context) {
        databaseHelper = new DatabaseHelper(
                context.getApplicationContext()
        );
    }

    // Adds a new weight entry to the database.
    public boolean addWeightEntry(
            int userId,
            String date,
            double weight) {

        long result = databaseHelper.addWeightEntry(
                userId,
                date,
                weight
        );

        return result != -1;
    }

    // Retrieves all weight entries belonging to one user.
    public List<WeightEntry> getWeightEntries(int userId) {
        return databaseHelper.getWeightEntries(userId);
    }

    // Updates an existing weight entry.
    public boolean updateWeightEntry(
            int entryId,
            String date,
            double weight) {

        int rowsUpdated = databaseHelper.updateWeightEntry(
                entryId,
                date,
                weight
        );

        return rowsUpdated > 0;
    }

    // Deletes a weight entry from the database.
    public boolean deleteWeightEntry(int entryId) {

        int rowsDeleted =
                databaseHelper.deleteWeightEntry(entryId);

        return rowsDeleted > 0;
    }
}