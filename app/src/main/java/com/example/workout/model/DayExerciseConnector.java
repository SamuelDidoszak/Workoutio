package com.example.workout.model;

public class DayExerciseConnector {
    int dayExerciseConnectorId;
    int dayId;
    int exerciseId;

    public DayExerciseConnector() {
    }

    public DayExerciseConnector(int dayId, int exerciseId) {
        this.dayId = dayId;
        this.exerciseId = exerciseId;
    }

    public DayExerciseConnector(int dayExerciseConnectorId, int dayId, int exerciseId) {
        this.dayExerciseConnectorId = dayExerciseConnectorId;
        this.dayId = dayId;
        this.exerciseId = exerciseId;
    }

    public int getDayId() {
        return dayId;
    }

    public void setDayId(int dayId) {
        this.dayId = dayId;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getDayExerciseConnectorId() {
        return dayExerciseConnectorId;
    }

    public void setDayExerciseConnectorId(int dayExerciseConnectorId) {
        this.dayExerciseConnectorId = dayExerciseConnectorId;
    }
}
