package com.example.weighttrackingapp_chelseacacho.utility;

public class GoalManager {

    private double startWeight;
    private double targetWeight;

    public GoalManager(double startWeight, double targetWeight) {
        this.startWeight = startWeight;
        this.targetWeight = targetWeight;
    }

    public void setGoal(double startWeight, double targetWeight) {
        this.startWeight = startWeight;
        this.targetWeight = targetWeight;
    }

    public double getStartWeight() {
        return startWeight;
    }

    public double getTargetWeight() {
        return targetWeight;
    }

    // Auto-detect direction from start vs target
    public boolean isDecreaseGoal() {
        return startWeight > targetWeight;
    }

    public boolean isIncreaseGoal() {
        return startWeight < targetWeight;
    }

    public boolean isAlreadyAtGoalWhenSet() {
        return Double.compare(startWeight, targetWeight) == 0;
    }

    // Check if goal is reached using auto direction
    public boolean isGoalReached(double currentWeight) {
        if (isDecreaseGoal()) {
            return currentWeight <= targetWeight;
        } else if (isIncreaseGoal()) {
            return currentWeight >= targetWeight;
        } else {
            return true; // already at target when goal was set
        }
    }

    // How much remaining until target
    public double getAmountRemaining(double currentWeight) {
        if (isDecreaseGoal()) {
            return Math.max(0, currentWeight - targetWeight);
        } else if (isIncreaseGoal()) {
            return Math.max(0, targetWeight - currentWeight);
        } else {
            return 0;
        }
    }

    public String getGoalDirectionText() {
        if (isDecreaseGoal()) return "decrease";
        if (isIncreaseGoal()) return "increase";
        return "maintain";
    }

    public String getGoalSummary() {
        return "Start weight: " + startWeight +
                ", Target weight: " + targetWeight +
                " (" + getGoalDirectionText() + ")";
    }
}