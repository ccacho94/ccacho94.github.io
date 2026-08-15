package com.example.weighttrackingapp_chelseacacho.repository;

import android.content.Context;

import com.example.weighttrackingapp_chelseacacho.data.DatabaseHelper;
import com.example.weighttrackingapp_chelseacacho.model.WeightEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Provides weight-entry data to the WeightLogViewModel.
public class WeightRepository {

    private final DatabaseHelper databaseHelper;

    /*
     * Organizes each user's entries using the user ID as the key.
     * HashMap retrieval has an average runtime of O(1).
     */
    private final Map<Integer, List<WeightEntry>> weightEntryCache =
            new HashMap<>();

    public WeightRepository(Context context) {
        databaseHelper = new DatabaseHelper(
                context.getApplicationContext()
        );
    }

    public boolean addWeightEntry(
            int userId,
            String date,
            double weight) {

        long result = databaseHelper.addWeightEntry(
                userId,
                date,
                weight
        );

        if (result != -1) {
            // Removes outdated records from the cache.
            weightEntryCache.remove(userId);
            return true;
        }

        return false;
    }

    public List<WeightEntry> getWeightEntries(int userId) {

        // Average O(1) HashMap lookup.
        List<WeightEntry> cachedEntries =
                weightEntryCache.get(userId);

        if (cachedEntries != null) {
            return new ArrayList<>(cachedEntries);
        }

        List<WeightEntry> databaseEntries =
                databaseHelper.getWeightEntries(userId);

        weightEntryCache.put(
                userId,
                new ArrayList<>(databaseEntries)
        );

        return new ArrayList<>(databaseEntries);
    }

    public boolean updateWeightEntry(
            int entryId,
            String date,
            double weight) {

        int rowsUpdated = databaseHelper.updateWeightEntry(
                entryId,
                date,
                weight
        );

        if (rowsUpdated > 0) {
            weightEntryCache.clear();
            return true;
        }

        return false;
    }

    public boolean deleteWeightEntry(int entryId) {

        int rowsDeleted =
                databaseHelper.deleteWeightEntry(entryId);

        if (rowsDeleted > 0) {
            weightEntryCache.clear();
            return true;
        }

        return false;
    }
}