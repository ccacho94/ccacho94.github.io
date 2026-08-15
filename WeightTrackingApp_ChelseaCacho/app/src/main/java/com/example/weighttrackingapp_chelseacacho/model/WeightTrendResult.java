package com.example.weighttrackingapp_chelseacacho.model;

// Stores the result of comparing the two newest weight entries.
public class WeightTrendResult {

    // Possible weight trend outcomes.
    public enum Direction {
        INCREASING,
        DECREASING,
        STABLE,
        INSUFFICIENT_DATA
    }

    private final Direction direction;
    private final double previousWeight;
    private final double currentWeight;
    private final double difference;
    private final String previousDate;
    private final String currentDate;

    private WeightTrendResult(
            Direction direction,
            double previousWeight,
            double currentWeight,
            double difference,
            String previousDate,
            String currentDate) {

        this.direction = direction;
        this.previousWeight = previousWeight;
        this.currentWeight = currentWeight;
        this.difference = difference;
        this.previousDate = previousDate;
        this.currentDate = currentDate;
    }

    // Used when the user has fewer than two weight entries.
    public static WeightTrendResult insufficientData() {
        return new WeightTrendResult(
                Direction.INSUFFICIENT_DATA,
                0,
                0,
                0,
                "",
                ""
        );
    }

    // Creates a trend result using the two newest entries.
    public static WeightTrendResult of(
            Direction direction,
            WeightEntry previous,
            WeightEntry current) {

        return new WeightTrendResult(
                direction,
                previous.getWeight(),
                current.getWeight(),
                current.getWeight() - previous.getWeight(),
                previous.getDate(),
                current.getDate()
        );
    }

    public Direction getDirection() {
        return direction;
    }

    public double getPreviousWeight() {
        return previousWeight;
    }

    public double getCurrentWeight() {
        return currentWeight;
    }

    public double getDifference() {
        return difference;
    }

    public String getPreviousDate() {
        return previousDate;
    }

    public String getCurrentDate() {
        return currentDate;
    }
}