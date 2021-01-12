package com.example.workout.model;

public class Day {
    private int dayId;
    private String dayName;
    private boolean custom;

    public Day(){}

    public Day(String dayName, boolean custom) {
        this.dayName = dayName;
        this.custom = custom;
    }

    public Day(int dayId, String dayName, boolean custom) {
        this.dayId = dayId;
        this.dayName = dayName;
        this.custom = custom;
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

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }
}
