package com.example.workout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private Button addButton;
    private int chosenDay = -1, chosenMonth, chosenYear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        setViews();
        calendarView.setOnDateChangeListener(onCalendarDateChange);
        addButton.setOnClickListener(onAddButtonClick);
    }


    private CalendarView.OnDateChangeListener onCalendarDateChange = new CalendarView.OnDateChangeListener() {
        @Override
        public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
            chosenDay = dayOfMonth;
            chosenMonth = month + 1;
            chosenYear = year;
        }
    };

    /** checks if the date is set. If it is, returns the date to the previous activity */
    View.OnClickListener onAddButtonClick = v -> {
        /** default value of chosenDay is -1. If date is chosen, this value changes */
        String date;
        if(chosenDay == -1) {
            Date currentDate = new Date(calendarView.getDate());
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            date = format.format(currentDate);
        }
        else {
            date = (chosenDay >= 10) ? Integer.toString(chosenDay) : "0" + chosenDay;
            date += ".";
            date += (chosenMonth >= 10) ? Integer.toString(chosenMonth) : "0" + chosenMonth;
            date += "." + chosenYear;
        }
        setResult(2, new Intent().putExtra("date", date));
        finish();
    };

    private void setViews() {
        calendarView = findViewById(R.id.calendar_calendarView);
        addButton = findViewById(R.id.calendar_addButton);
    }
}




















