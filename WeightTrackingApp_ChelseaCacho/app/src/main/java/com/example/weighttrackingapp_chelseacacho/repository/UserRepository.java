package com.example.weighttrackingapp_chelseacacho.repository;

import android.content.Context;

import com.example.weighttrackingapp_chelseacacho.data.DatabaseHelper;

// Provides user account data to the ViewModels.
public class UserRepository {

    private final DatabaseHelper databaseHelper;

    // Creates access to the application's database.
    public UserRepository(Context context) {
        databaseHelper = new DatabaseHelper(
                context.getApplicationContext()
        );
    }

    // Checks the user's login information.
    // Returns the user's ID when successful or -1 when unsuccessful.
    public int loginUser(String email, String password) {
        return databaseHelper.loginUser(email, password);
    }

    // Creates a new user account.
    public boolean createUser(
            String fullName,
            String email,
            String password) {

        long result = databaseHelper.addUser(
                fullName,
                email,
                password
        );

        return result != -1;
    }

    // Checks whether the email already belongs to an account.
    public boolean emailExists(String email) {
        return databaseHelper.emailExists(email);
    }
}