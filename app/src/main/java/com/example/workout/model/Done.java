package com.example.workout.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Done {
    int doneId;
    String date;
    int exerciseId;
    int quantity;
    /**length of exercise in milliseconds*/
    int time;
    boolean negative;
    boolean canMore;

    /** Example:<br/>
     *  date: "01.01.2020 20:48" <br/>
     *  return: "01.01.2020"
     * @return date without hour.
     */
    public String getTrimmedDate()
    {
        return date.substring(0, 10);
    }

    /** Example:<br/>
     *  date: "01.01.2020 20:48" <br/>
     *  return: "20:48"
     * @return hour without date.
     */
    public String getTrimmedDateTime()
    {
        return date.substring(11);
    }

    /** Example:<br/>
     *  date: "01.01.2020 20:48" <br/>
     *  return: "Wed 01.01"
     * @return dayName and date without a year.
     */
    public String getTrimmedDateWithDayName() {
        try {
            long time = new SimpleDateFormat("dd.MM.yyyy")
                    .parse(getTrimmedDate()).getTime();
            Date formattedDate = new Date(time);
            return new SimpleDateFormat("EEE dd.MM")
                    .format(formattedDate);
        }
        catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public Done() {}

    public Done(String date, int exerciseId, int quantity, int time, boolean negative, boolean canMore) {
        this.date = date;
        this.exerciseId = exerciseId;
        this.quantity = quantity;
        this.time = time;
        this.negative = negative;
        this.canMore = canMore;
    }

    public Done(int exerciseId, int quantity, int time, boolean negative, boolean canMore) {
        this.exerciseId = exerciseId;
        this.quantity = quantity;
        this.time = time;
        this.negative = negative;
        this.canMore = canMore;
    }

    public Done(int doneId, String date, int exerciseId, int quantity, int time, boolean negative, boolean canMore) {
        this.doneId = doneId;
        this.date = date;
        this.exerciseId = exerciseId;
        this.quantity = quantity;
        this.time = time;
        this.negative = negative;
        this.canMore = canMore;
    }

    public int getDoneId() {
        return doneId;
    }

    public void setDoneId(int doneId) {
        this.doneId = doneId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public boolean isNegative() {
        return negative;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    public boolean isCanMore() {
        return canMore;
    }

    public void setCanMore(boolean canMore) {
        this.canMore = canMore;
    }
}
