package com.example.weighttrackingapp_chelseacacho;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "WeightTracker.db";
    private static final int DATABASE_VERSION = 1;

    // ---------- USERS TABLE ----------
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "full_name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD = "password";

    // ---------- GOALS TABLE ----------
    public static final String TABLE_GOALS = "goals";
    public static final String COL_GOAL_ID = "goal_id";
    public static final String COL_GOAL_USER_ID = "user_id";
    public static final String COL_GOAL_WEIGHT = "goal_weight";

    // ---------- WEIGHT ENTRIES TABLE ----------
    public static final String TABLE_WEIGHT_ENTRIES = "weight_entries";
    public static final String COL_ENTRY_ID = "entry_id";
    public static final String COL_ENTRY_USER_ID = "user_id";
    public static final String COL_ENTRY_DATE = "entry_date";
    public static final String COL_ENTRY_WEIGHT = "entry_weight";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_NAME + " TEXT NOT NULL, "
                + COL_USER_EMAIL + " TEXT NOT NULL UNIQUE, "
                + COL_USER_PASSWORD + " TEXT NOT NULL"
                + ")";
        db.execSQL(createUsersTable);

        // Goals table (one goal row per user)
        String createGoalsTable = "CREATE TABLE " + TABLE_GOALS + " ("
                + COL_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_GOAL_USER_ID + " INTEGER NOT NULL UNIQUE, "
                + COL_GOAL_WEIGHT + " REAL NOT NULL, "
                + "FOREIGN KEY(" + COL_GOAL_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COL_USER_ID + ")"
                + ")";
        db.execSQL(createGoalsTable);

        // Weight entries table (many rows per user)
        String createEntriesTable = "CREATE TABLE " + TABLE_WEIGHT_ENTRIES + " ("
                + COL_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ENTRY_USER_ID + " INTEGER NOT NULL, "
                + COL_ENTRY_DATE + " TEXT NOT NULL, "
                + COL_ENTRY_WEIGHT + " REAL NOT NULL, "
                + "FOREIGN KEY(" + COL_ENTRY_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COL_USER_ID + ")"
                + ")";
        db.execSQL(createEntriesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // simple reset for development/class project
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT_ENTRIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }


    // USER ACCOUNT METHODS


    public boolean registerUser(String fullName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (isEmailExists(email)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, fullName);
        values.put(COL_USER_EMAIL, email);
        values.put(COL_USER_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_EMAIL + "=?",
                new String[]{email},
                null,
                null,
                null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkUserLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{email, password},
                null,
                null,
                null
        );

        boolean valid = cursor.getCount() > 0;
        cursor.close();
        return valid;
    }

    // Get user id after login (important for storing goal + entries per user)
    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_EMAIL + "=?",
                new String[]{email},
                null,
                null,
                null
        );

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
        }
        cursor.close();
        return userId;
    }

    // GOAL METHODS (ONE GOAL PER USER)


    // Insert or update goal for a user
    public boolean saveOrUpdateGoal(int userId, float goalWeight) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if goal exists for this user
        Cursor cursor = db.query(
                TABLE_GOALS,
                new String[]{COL_GOAL_ID},
                COL_GOAL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_GOAL_USER_ID, userId);
        values.put(COL_GOAL_WEIGHT, goalWeight);

        long result;
        if (exists) {
            int rows = db.update(
                    TABLE_GOALS,
                    values,
                    COL_GOAL_USER_ID + "=?",
                    new String[]{String.valueOf(userId)}
            );
            return rows > 0;
        } else {
            result = db.insert(TABLE_GOALS, null, values);
            return result != -1;
        }
    }

    public float getGoalWeightByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_GOALS,
                new String[]{COL_GOAL_WEIGHT},
                COL_GOAL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );

        float goal = -1f;
        if (cursor.moveToFirst()) {
            goal = cursor.getFloat(cursor.getColumnIndexOrThrow(COL_GOAL_WEIGHT));
        }
        cursor.close();
        return goal;
    }


    // DAILY WEIGHT ENTRY METHODS
    public boolean insertWeightEntry(int userId, String date, float weight) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_ENTRY_USER_ID, userId);
        values.put(COL_ENTRY_DATE, date);
        values.put(COL_ENTRY_WEIGHT, weight);

        long result = db.insert(TABLE_WEIGHT_ENTRIES, null, values);
        return result != -1;
    }

    public ArrayList<WeightEntry> getWeightEntriesByUserId(int userId) {
        ArrayList<WeightEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_WEIGHT_ENTRIES,
                new String[]{COL_ENTRY_ID, COL_ENTRY_DATE, COL_ENTRY_WEIGHT},
                COL_ENTRY_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                COL_ENTRY_ID + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                int entryId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ENTRY_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_ENTRY_DATE));
                float weight = cursor.getFloat(cursor.getColumnIndexOrThrow(COL_ENTRY_WEIGHT));

                list.add(new WeightEntry(entryId, date, weight));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public boolean deleteWeightEntry(int entryId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int rows = db.delete(
                TABLE_WEIGHT_ENTRIES,
                COL_ENTRY_ID + "=?",
                new String[]{String.valueOf(entryId)}
        );

        return rows > 0;
    }
}