package com.example.weighttrackingapp_chelseacacho.model;

public class Goal {

    //unigue ID assigned to goal, users own goal, and goal weight
    private int goalId;

    private int userID;

    private double goalWeight;

    // Creates a Goal object with the required goal information.
    public Goal(int goalId, int userID, double goalWeight){
        this.goalId = goalId;
        this.userID = userID;
        this.goalWeight = goalWeight;
    }

    //returns unique goal id
    public int getGoalId{
        return goalId
    }

    //returns the ID of the user connect to the goal
    public int getUserID() {
        return userID;
    }

    //return the user's targets weight.
    public double getGoalWeight() {
        return goalWeight;
    }
}
