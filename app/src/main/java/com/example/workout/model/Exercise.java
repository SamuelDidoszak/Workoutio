package com.example.workout.model;

public class Exercise {
    private int exerciseId;
    private String exerciseName;
    private boolean customExercise = false;

    public Exercise() {}

    public Exercise(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public Exercise(int exerciseId, String exerciseName) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
    }

    public Exercise(int exerciseId, String exerciseName, boolean customExercise) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.customExercise = customExercise;
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

    public boolean isCustomExercise() {
        return customExercise;
    }

    public void setCustomExercise(boolean customExercise) {
        this.customExercise = customExercise;
    }
}
