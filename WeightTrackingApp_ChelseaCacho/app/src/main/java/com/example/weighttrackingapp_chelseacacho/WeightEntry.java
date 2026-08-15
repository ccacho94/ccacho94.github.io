package com.example.weighttrackingapp_chelseacacho;

public class WeightEntry {
    private int id;
    private String date;
    private float weight;

    public WeightEntry(int entryId, String date, float weight) {
        this.id = entryId;
        this.date = date;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public float getWeight() {
        return weight;
    }
}