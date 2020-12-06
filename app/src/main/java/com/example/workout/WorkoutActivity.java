package com.example.workout;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.QuantityAndReps;
import com.example.workout.model.helper.CheckableImageView;
import com.example.workout.ui.adapter.WorkoutRecyclerViewAdapter;

import java.util.List;

public class WorkoutActivity extends AppCompatActivity {

    private static final String TAG = "WorkoutActivity";
    private CardView containerCardView;
    private ImageView circleImageView;
    private TextView currentWorkoutTextView;
    private com.example.workout.model.helper.Chronometer chronometer;
    private RecyclerView timePickerSlider, workoutRecyclerView;
    private WorkoutRecyclerViewAdapter workoutRecyclerViewAdapter;
    private CheckableImageView negativeCheckbox, canMoreCheckbox;

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

        if(quantityAndRepsList.size() != 0)
            currentWorkoutTextView.setText(quantityAndRepsList.get(0).getExerciseName());
        setUpRecyclerViews();
    }

    private void setUpRecyclerViews() {
        workoutRecyclerViewAdapter = new WorkoutRecyclerViewAdapter(context, quantityAndRepsList);
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        workoutRecyclerView.setAdapter(workoutRecyclerViewAdapter);

        MutableLiveData<Integer> chosenExercise = workoutRecyclerViewAdapter.getChosenExercise();
        chosenExercise.observe(this, integer -> {
            currentWorkoutTextView.setText(DB.getExercise(integer).getExerciseName());
        });

    }

    private void setUpViews() {
        containerCardView = findViewById(R.id.workout_activity_containerCardView);
        circleImageView = findViewById(R.id.workout_activity_circleImageView);
        chronometer = findViewById(R.id.workout_activity_Chronometer);
        currentWorkoutTextView = findViewById(R.id.workout_activity_currentWorkoutTextView);
        timePickerSlider = findViewById(R.id.workout_activity_timePickerSlider);
        workoutRecyclerView = findViewById(R.id.workout_activity_workoutRecyclerView);
            //  CheckBoxes
        negativeCheckbox = findViewById(R.id.workout_activity_negativeCheckBox);
        canMoreCheckbox = findViewById(R.id.workout_activity_canMoreCheckBox);
    }

    private void setOnClickListeners() {
        ClickHandler clickHandler = new ClickHandler();
        circleImageView.setOnClickListener(clickHandler.onCircleClick);
        currentWorkoutTextView.setOnClickListener(clickHandler.onCurrentWorkoutClick);

        negativeCheckbox.setOnClickListener(clickHandler.onNegativeCheckboxClick);
        canMoreCheckbox.setOnClickListener(clickHandler.onCanMoreCheckboxClick);
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
            //  Checkboxes
        View.OnClickListener onNegativeCheckboxClick = v -> {
        };
        View.OnClickListener onCanMoreCheckboxClick = v -> {
        };
    }
}


































