package com.example.weighttrackingapp_chelseacacho.model;

public class Goal {

    // Unique ID assigned to the goal
    private int goalId;

    // ID of the user who owns the goal
    private int userID;

    // User's target weight
    private double goalWeight;

    // Creates a Goal object with the required goal information
    public Goal(int goalId, int userID, double goalWeight) {
        this.goalId = goalId;
        this.userID = userID;
        this.goalWeight = goalWeight;
    }

    // Returns the unique goal ID
    public int getGoalId() {
        return goalId;
    }

    // Returns the ID of the user connected to the goal
    public int getUserID() {
        return userID;
    }

    // Returns the user's target weight
    public double getGoalWeight() {
        return goalWeight;
    }
}