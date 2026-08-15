package com.example.weighttrackingapp_chelseacacho.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.weighttrackingapp_chelseacacho.model.WeightEntry;

import java.util.ArrayList;
import java.util.List;

// Creates and manages the application's SQLite database.
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "WeightTracker.db";
    private static final int DATABASE_VERSION = 1;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_FULL_NAME = "full_name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    // Weight entries table
    private static final String TABLE_WEIGHT_ENTRIES = "weight_entries";
    private static final String COLUMN_ENTRY_ID = "entry_id";
    private static final String COLUMN_ENTRY_USER_ID = "user_id";
    private static final String COLUMN_ENTRY_DATE = "date";
    private static final String COLUMN_WEIGHT = "weight";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Enables foreign-key support.
    @Override
    public void onConfigure(SQLiteDatabase database) {
        super.onConfigure(database);
        database.setForeignKeyConstraintsEnabled(true);
    }

    // Creates the database tables when the database is first opened.
    @Override
    public void onCreate(SQLiteDatabase database) {

        String createUsersTable =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_FULL_NAME + " TEXT NOT NULL, " +
                        COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                        COLUMN_PASSWORD + " TEXT NOT NULL" +
                        ")";

        String createWeightEntriesTable =
                "CREATE TABLE " + TABLE_WEIGHT_ENTRIES + " (" +
                        COLUMN_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_ENTRY_USER_ID + " INTEGER NOT NULL, " +
                        COLUMN_ENTRY_DATE + " TEXT NOT NULL, " +
                        COLUMN_WEIGHT + " REAL NOT NULL, " +
                        "FOREIGN KEY (" + COLUMN_ENTRY_USER_ID + ") " +
                        "REFERENCES " + TABLE_USERS +
                        "(" + COLUMN_USER_ID + ") ON DELETE CASCADE" +
                        ")";

        database.execSQL(createUsersTable);
        database.execSQL(createWeightEntriesTable);
    }

    // Recreates the tables when the database version changes.
    @Override
    public void onUpgrade(
            SQLiteDatabase database,
            int oldVersion,
            int newVersion) {

        database.execSQL(
                "DROP TABLE IF EXISTS " + TABLE_WEIGHT_ENTRIES
        );

        database.execSQL(
                "DROP TABLE IF EXISTS " + TABLE_USERS
        );

        onCreate(database);
    }

    // Adds a new user account.
    public long addUser(
            String fullName,
            String email,
            String password) {

        SQLiteDatabase database = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        return database.insert(
                TABLE_USERS,
                null,
                values
        );
    }

    // Checks whether an email already belongs to an account.
    public boolean emailExists(String email) {

        SQLiteDatabase database = getReadableDatabase();

        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArguments = {email};

        try (Cursor cursor = database.query(
                TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                selection,
                selectionArguments,
                null,
                null,
                null
        )) {
            return cursor.moveToFirst();
        }
    }

    // Checks the login information and returns the user's ID.
    public int loginUser(String email, String password) {

        SQLiteDatabase database = getReadableDatabase();

        String selection =
                COLUMN_EMAIL + " = ? AND " +
                        COLUMN_PASSWORD + " = ?";

        String[] selectionArguments = {
                email,
                password
        };

        try (Cursor cursor = database.query(
                TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                selection,
                selectionArguments,
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                COLUMN_USER_ID
                        )
                );
            }
        }

        return -1;
    }

    // Adds a weight record for one user.
    public long addWeightEntry(
            int userId,
            String date,
            double weight) {

        SQLiteDatabase database = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ENTRY_USER_ID, userId);
        values.put(COLUMN_ENTRY_DATE, date);
        values.put(COLUMN_WEIGHT, weight);

        return database.insert(
                TABLE_WEIGHT_ENTRIES,
                null,
                values
        );
    }

    // Retrieves all weight records belonging to one user.
    public List<WeightEntry> getWeightEntries(int userId) {

        List<WeightEntry> entries = new ArrayList<>();

        SQLiteDatabase database = getReadableDatabase();

        String selection = COLUMN_ENTRY_USER_ID + " = ?";
        String[] selectionArguments = {
                String.valueOf(userId)
        };

        /*
         * The database returns the newest inserted records first.
         * WeightEntryMergeSort will later sort these records by date
         * for the trend-tracking algorithm.
         */
        String orderBy = COLUMN_ENTRY_ID + " DESC";

        try (Cursor cursor = database.query(
                TABLE_WEIGHT_ENTRIES,
                null,
                selection,
                selectionArguments,
                null,
                null,
                orderBy
        )) {
            int entryIdIndex = cursor.getColumnIndexOrThrow(
                    COLUMN_ENTRY_ID
            );

            int userIdIndex = cursor.getColumnIndexOrThrow(
                    COLUMN_ENTRY_USER_ID
            );

            int dateIndex = cursor.getColumnIndexOrThrow(
                    COLUMN_ENTRY_DATE
            );

            int weightIndex = cursor.getColumnIndexOrThrow(
                    COLUMN_WEIGHT
            );

            while (cursor.moveToNext()) {

                int entryId = cursor.getInt(entryIdIndex);
                int savedUserId = cursor.getInt(userIdIndex);
                String date = cursor.getString(dateIndex);
                double weight = cursor.getDouble(weightIndex);

                WeightEntry entry = new WeightEntry(
                        entryId,
                        savedUserId,
                        date,
                        weight
                );

                entries.add(entry);
            }
        }

        return entries;
    }

    // Updates an existing weight record.
    public int updateWeightEntry(
            int entryId,
            String date,
            double weight) {

        SQLiteDatabase database = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ENTRY_DATE, date);
        values.put(COLUMN_WEIGHT, weight);

        String selection = COLUMN_ENTRY_ID + " = ?";
        String[] selectionArguments = {
                String.valueOf(entryId)
        };

        return database.update(
                TABLE_WEIGHT_ENTRIES,
                values,
                selection,
                selectionArguments
        );
    }

    // Deletes one weight record.
    public int deleteWeightEntry(int entryId) {

        SQLiteDatabase database = getWritableDatabase();

        String selection = COLUMN_ENTRY_ID + " = ?";
        String[] selectionArguments = {
                String.valueOf(entryId)
        };

        return database.delete(
                TABLE_WEIGHT_ENTRIES,
                selection,
                selectionArguments
        );
    }
}