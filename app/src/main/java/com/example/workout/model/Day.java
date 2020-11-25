package com.example.workout.model;

public class Day {
    private int dayId;
    private String dayName;

    public Day(){}

    public Day(String dayName) {
        this.dayName = dayName;
    }

    public Day(int dayId, String dayName) {
        this.dayId = dayId;
        this.dayName = dayName;
    }

    public int getDayId() {
        return dayId;
    }

    public void setDayId(int dayId) {
        this.dayId = dayId;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }
}
