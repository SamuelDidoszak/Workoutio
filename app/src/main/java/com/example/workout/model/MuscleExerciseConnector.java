package com.example.workout.model;

public class MuscleExerciseConnector {
    int muscleExerciseConnectorId;
    int muscleId;
    int exerciseId;

    public MuscleExerciseConnector() {
    }

    public MuscleExerciseConnector(int muscleId, int exerciseId) {
        this.muscleId = muscleId;
        this.exerciseId = exerciseId;
    }

    public MuscleExerciseConnector(int muscleExerciseConnectorId, int muscleId, int exerciseId) {
        this.muscleExerciseConnectorId = muscleExerciseConnectorId;
        this.muscleId = muscleId;
        this.exerciseId = exerciseId;
    }

    public int getMuscleId() {
        return muscleId;
    }

    public void setMuscleId(int muscleId) {
        this.muscleId = muscleId;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getMuscleExerciseConnectorId() {
        return muscleExerciseConnectorId;
    }

    public void setMuscleExerciseConnectorId(int muscleExerciseConnectorId) {
        this.muscleExerciseConnectorId = muscleExerciseConnectorId;
    }
}