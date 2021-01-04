package com.example.workout.model;

import android.content.Context;

import androidx.annotation.Nullable;

import com.example.workout.data.DatabaseHandler;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class QuantityAndReps implements Serializable {
    int exerciseId;
    String exerciseName;
    int quantity;
    boolean canMore;
    int reps;

    public QuantityAndReps(int exerciseId, String exerciseName, int quantity, boolean canMore, int reps) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.quantity = quantity;
        this.canMore = canMore;
        this.reps = reps;
    }

    /**
     * Creates the quantityAndReps instance given the exerciseId
     * @param exerciseId exercise for which quantityAndReps should be created
     * @param context applicationContext
     * @param date @Nullable date from which quantityAndReps should be created. Iterates {@code maximumWeeksBefore} times if not provided
     */
    public QuantityAndReps(int exerciseId, Context context, @Nullable String date) {
        DatabaseHandler DB = new DatabaseHandler(context);
        Exercise exercise = DB.getExercise(exerciseId);
        this.exerciseId = exerciseId;
        exerciseName = exercise.getExerciseName();
        quantity = 0;
        reps = 0;

        long subtractionTime = 604800000;
        int maximumWeeksBefore = 10;

        List<Done> doneListByDate;
        List<Done> doneList = new ArrayList<>();

        //  variables are necessary only when the date was not provided
        Long currentDate = Calendar.getInstance().getTimeInMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        if(date == null) {
            currentDate -= subtractionTime;
            date = dateFormat.format(currentDate);
        }

        //  if date was provided, this function will execute only once. If date was incorrect, function will create quantityAndReps from a different day
        for(int i = 0; i < maximumWeeksBefore; i++) {
            doneListByDate = DB.getDonesByDate(date);
            for(Done tempDone : doneListByDate) {
                if(tempDone.getExerciseId() == exerciseId)
                    doneList.add(tempDone);
            }
            if(doneList.size() != 0)
                break;

            currentDate -= subtractionTime;
            date = dateFormat.format(currentDate);
        }

        for(Done done : doneList) {
            quantity += done.getQuantity();
            reps++;
            if(done.isCanMore())
                canMore = true;
        }
        if(reps != 0)
            quantity /= reps;
    }

    /**
     * Finds the last date in which the exercise was performed.
     * @param exerciseId exerciseId for the exercise to search for
     * @param context applicationContext
     * @return date with a pattern "dd.MM.yyyy". If performance not found, returns an epty String
     */
    public String getDateOfLastExercisePerformance(int exerciseId, Context context) {
        DatabaseHandler DB = new DatabaseHandler(context);
        long subtractionTime = 604800000;
        int maximumWeeksBefore = 10;

        List<Done> doneListByDate;
        List<Done> doneList = new ArrayList<>();

        Long currentDate = Calendar.getInstance().getTimeInMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        currentDate -= subtractionTime;
        String date = dateFormat.format(currentDate);

        for(int i = 0; i < maximumWeeksBefore; i++) {
            doneListByDate = DB.getDonesByDate(date);
            for(Done tempDone : doneListByDate) {
                if(tempDone.getExerciseId() == exerciseId) {
                    doneList.add(tempDone);
                    return date;
                }
            }
            currentDate -= subtractionTime;
            date = dateFormat.format(currentDate);
        }
        return "";
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
