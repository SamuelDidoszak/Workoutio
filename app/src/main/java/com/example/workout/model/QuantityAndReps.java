package com.example.workout.model;

public class QuantityAndReps {
    int exerciseId;
    String exerciseName;
    int quantity;
    boolean canMore;
    int reps;

    public QuantityAndReps() {
    }

    public QuantityAndReps(int exerciseId, String exerciseName, int quantity, boolean biggerAmount, int reps) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.quantity = quantity;
        this.canMore = biggerAmount;
        this.reps = reps;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public boolean isCanMore() {
        return canMore;
    }

    public void setCanMore(boolean canMore) {
        this.canMore = canMore;
    }
}
