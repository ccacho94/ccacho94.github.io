package com.example.weighttrackingapp_chelseacacho.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.weighttrackingapp_chelseacacho.repository.UserRepository;

// Handles login validation and user account access.
public class LoginViewModel extends AndroidViewModel {

    private final UserRepository repository;

    private final MutableLiveData<Integer> loggedInUserId =
            new MutableLiveData<>();

    private final MutableLiveData<String> message =
            new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);

        repository = new UserRepository(application);
    }

    public LiveData<Integer> getLoggedInUserId() {
        return loggedInUserId;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    // Validates the login information and checks the database.
    public void login(String email, String password) {

        if (email == null || email.trim().isEmpty()) {
            message.setValue("Please enter your email.");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            message.setValue("Please enter your password.");
            return;
        }

        int userId = repository.loginUser(
                email.trim(),
                password
        );

        if (userId != -1) {
            loggedInUserId.setValue(userId);
        } else {
            message.setValue("Email or password is incorrect.");
        }
    }
}