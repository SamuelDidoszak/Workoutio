package com.example.workout;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.QuantityAndReps;

import java.util.List;

public class WorkoutActivity extends AppCompatActivity {

    private CardView containerCardView;
    private ImageView circleImageView;
    private TextView currentWorkoutTextView;
    private com.example.workout.model.helper.Chronometer chronometer;
    private RecyclerView timePickerSlider, workoutRecyclerView;

    private DatabaseHandler DB;
    private Context context;

    private List<QuantityAndReps> quantityAndRepsList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_activity);

        context = this;
        DB = new DatabaseHandler(context);

        quantityAndRepsList = (List<QuantityAndReps>) getIntent().getSerializableExtra("quantityAndReps");

        setUpViews();
        setOnClickListeners();
        setUpRecyclerViews();
    }

    private void setUpRecyclerViews() {
    }

    private void setUpViews() {
        containerCardView = findViewById(R.id.workout_activity_containerCardView);
        circleImageView = findViewById(R.id.workout_activity_circleImageView);
        chronometer = findViewById(R.id.workout_activity_Chronometer);
        currentWorkoutTextView = findViewById(R.id.workout_activity_currentWorkoutTextView);
        timePickerSlider = findViewById(R.id.workout_activity_timePickerSlider);
        workoutRecyclerView = findViewById(R.id.workout_activity_workoutRecyclerView);
    }

    private void setOnClickListeners() {
        ClickHandler clickHandler = new ClickHandler();
        circleImageView.setOnClickListener(clickHandler.onCircleClick);
        currentWorkoutTextView.setOnClickListener(clickHandler.onCurrentWorkoutClick);
    }

    private void countTime() {
        if(chronometer.isStarted())
            chronometer.stop();
        else {
            chronometer.init();
            chronometer.start();
        }
    }

    private class ClickHandler {
        View.OnClickListener onCircleClick = v -> {
            countTime();
        };
        View.OnClickListener onCurrentWorkoutClick = v -> {

        };
    }
}


































