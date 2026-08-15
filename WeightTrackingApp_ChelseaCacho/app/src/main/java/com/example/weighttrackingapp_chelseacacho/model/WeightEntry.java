package com.example.weighttrackingapp_chelseacacho.model;

public class WeightEntry {

    //unique ID for weight entry, ID of user who owns entry, date recorded, and user's recorded weight
    private int entryId;
    private int userID;
    private String date;
    private double weight;

    //creates a weight entry object with all required info
    public WeightEntry(int entryId, int userId, String date, double weight) {

        this.entryId = entryId;
        this.userID = userId;
        this.date = date;
        this.weight = weight;
    }
//returns the unique entry id
    public int getId() {
        return entryId;
    }

    //return the unique user id
    public int getUserID() {
        return userID;
    }
//return date
    public String getDate() {
        return date;
    }
//return recorded date
    public double getWeight() {
        return weight;
    }
}