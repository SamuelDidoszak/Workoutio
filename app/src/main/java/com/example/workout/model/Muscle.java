package com.example.workout.model;

public class Muscle {
    int muscleId;
    String muscleName;
    int muscleIcon;

    public Muscle() {}

    public Muscle(String muscleName) {
        this.muscleName = muscleName;
    }

    public Muscle(String muscleName, int muscleIcon) {
        this.muscleName = muscleName;
        this.muscleIcon = muscleIcon;
    }

    public Muscle(int muscleId, String muscleName, int muscleIcon) {
        this.muscleId = muscleId;
        this.muscleName = muscleName;
        this.muscleIcon = muscleIcon;
    }

    public int getMuscleId() {
        return muscleId;
    }

    public void setMuscleId(int muscleId) {
        this.muscleId = muscleId;
    }

    public String getMuscleName() {
        return muscleName;
    }

    public void setMuscleName(String muscleName) {
        this.muscleName = muscleName;
    }

    public int getMuscleIcon() {
        return muscleIcon;
    }

    public void setMuscleIcon(int muscleIcon) {
        this.muscleIcon = muscleIcon;
    }
}