package com.example.workout.model.helper;

import android.util.Log;
import android.util.TimingLogger;

import com.example.workout.model.Muscle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MuscleDateTime {
    List<Muscle> muscleList;
    String date;
    String time;
    int sortedDoneListEndIndex;

    public void setTime(int timeBeginning, int timeEnd) {
        int time = timeEnd - timeBeginning;
        this.time = String.format("%02d:", time / 60);
        this.time += String.format("%02d", time % 60);
    }

    public MuscleDateTime(List<Muscle> muscleList, String date, String time) {
        this.muscleList = muscleList;
        this.date = date;
        this.time = time;
    }

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
     *  return: "Wed 01.01"
     * @return dayName and date with or without a year depending on if it's a current year.
     */
    public String getTrimmedDateWithDayName() {
        TimingLogger timingLogger = new TimingLogger("timmingLogger", "old way");
        try {
            Log.d("TAG", "getTrimmedDateWithDayName: " + date);
            timingLogger.addSplit("start");
            long time = new SimpleDateFormat("dd.MM.yyyy")
                    .parse(date.substring(0, 10)).getTime();
            Date formattedDate = new Date(time);
            String newDate = new SimpleDateFormat("EEE dd.MM")
                    .format(formattedDate);
            timingLogger.addSplit("stop");
            timingLogger.dumpToLog();
            return newDate += dateIfDifferentYear();
        }
        catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    /** Example:<br/>
     *  date: "01.01.2020 20:48" <br/>
     *  return: "[0]: Wed  [1]: 01.01"
     * @return dayName and date with or without a year depending on if it's a current year.
     */
    public String[] getTrimmedDateAndDayNameDivided() {
        try {
            String[] dayAndDate = new String[2];
            dayAndDate[1] = date.substring(0, 5);
            long time = new SimpleDateFormat("dd.MM.yyyy")
                    .parse(date.substring(0, 10)).getTime();
            Date formattedDate = new Date(time);
            dayAndDate[0] = new SimpleDateFormat("EEE")
                    .format(formattedDate);
            dayAndDate[0] += dateIfDifferentYear();
            return dayAndDate;
        }
        catch (ParseException e) {
            e.printStackTrace();
            return new String[] {"", ""};
        }
    }

    /** Checks if date is in a current year. If not, returns an empty string
     * @return year if it's a different year. Otherwise, returns an empty string.
     */
    public String dateIfDifferentYear() {
        String year = date.substring(6, 10);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
        String currentYear = dateFormat.format(Calendar.getInstance().getTime());
        return year.equals(currentYear) ? "" : "." + year;
    }

    public void setDateTimeFirstDone(int time) {
        this.time = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toHours(time), TimeUnit.MILLISECONDS.toMinutes(time) % 60);
    }

    public MuscleDateTime(List<Muscle> muscleList, String date, String time, int sortedDoneListEndIndex) {
        this.muscleList = muscleList;
        this.date = date;
        this.time = time;
        this.sortedDoneListEndIndex = sortedDoneListEndIndex;
    }

    public MuscleDateTime(List<Muscle> muscleList, String date, int timeBeginningMinutes, int timeEndMinutes) {
        this.muscleList = muscleList;
        this.date = date;
        setTime(timeBeginningMinutes, timeEndMinutes);
    }

    public List<Muscle> getMuscleList() {
        return muscleList;
    }

    public void setMuscleList(List<Muscle> muscleList) {
        this.muscleList = muscleList;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    /**
     * @return index next to the last done index in sorted doneList for this muscle
     */
    public int getSortedDoneListEndIndex() {
        return sortedDoneListEndIndex;
    }

    public void setSortedDoneListEndIndex(int sortedDoneListEndIndex) {
        this.sortedDoneListEndIndex = sortedDoneListEndIndex;
    }
}
