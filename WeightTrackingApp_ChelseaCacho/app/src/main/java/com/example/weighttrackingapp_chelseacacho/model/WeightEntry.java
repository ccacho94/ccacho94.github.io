package com.example.weighttrackingapp_chelseacacho.model;

// Stores one weight record from the database.
public class WeightEntry {

    private int entryId;
    private int userId;
    private String date;
    private double weight;

    // Used when an entry is retrieved from the database.
    public WeightEntry(
            int entryId,
            int userId,
            String date,
            double weight) {

        this.entryId = entryId;
        this.userId = userId;
        this.date = date;
        this.weight = weight;
    }

    // Used before a new entry has received a database ID.
    public WeightEntry(
            int userId,
            String date,
            double weight) {

        this(-1, userId, date, weight);
    }

    public int getEntryId() {
        return entryId;
    }

    public int getUserId() {
        return userId;
    }

    public String getDate() {
        return date;
    }

    public double getWeight() {
        return weight;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}