package com.example.workout;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Exercise;
import com.example.workout.model.QuantityAndReps;
import com.example.workout.model.helper.CheckableImageView;
import com.example.workout.ui.adapter.PickerSliderAdapter;
import com.example.workout.ui.adapter.WorkoutRecyclerViewAdapter;

import java.util.List;

public class WorkoutActivity extends AppCompatActivity {

    private static final String TAG = "WorkoutActivity";
    private CardView containerCardView;
    private ImageView circleImageView;
    private TextView currentWorkoutTextView;
    private com.example.workout.model.helper.Chronometer chronometer;
    private RecyclerView pickerSlider, workoutRecyclerView;
    private WorkoutRecyclerViewAdapter workoutRecyclerViewAdapter;
    private PickerSliderAdapter pickerSliderAdapter;
    private CheckableImageView negativeCheckbox, canMoreCheckbox;

    private int exerciseAmount;

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

        //  add time is amount =======================================
    private void setUpRecyclerViews() {
        workoutRecyclerViewAdapter = new WorkoutRecyclerViewAdapter(context, quantityAndRepsList);
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        workoutRecyclerView.setAdapter(workoutRecyclerViewAdapter);

        MutableLiveData<Integer> chosenExercise = workoutRecyclerViewAdapter.getChosenExercise();
            //  add time is amount =======================================
        chosenExercise.observe(this, integer -> {
            Exercise exercise = DB.getExercise(integer);
            currentWorkoutTextView.setText(exercise.getExerciseName());
            negativeCheckbox.setChecked(exercise.isDefaultNegative());
                //  add time is amount =======================================
            if(exercise.isTimeAsAmount())
                Log.d(TAG, "Time is the amount");
        });

            //  pickerSlider
        pickerSliderAdapter = new PickerSliderAdapter(context);
        RecyclerView.LayoutManager pickerLayoutManager = new LinearLayoutManager(context, RecyclerView.HORIZONTAL, Boolean.FALSE);
        pickerSlider.setLayoutManager(pickerLayoutManager);
        pickerSlider.setAdapter(pickerSliderAdapter);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(pickerSlider);

            //  Gives the recyclerView paddings that able first and last items to be centered
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, getResources().getDisplayMetrics());
        int screenWidth = displayMetrics.widthPixels;
        int padding = (screenWidth / 5 ) * 3 / 2 - (int)px;
        pickerSlider.setPadding(padding, 0, padding, 0);

        MutableLiveData<Integer> clickedNumber = pickerSliderAdapter.getClickedNumber();

        pickerSlider.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {

                new Handler().postDelayed(() -> {
                    View view = snapHelper.findSnapView(pickerLayoutManager);
                    int number = pickerSlider.getChildAdapterPosition(view);
                    pickerSliderAdapter.setExerciseAmount(number);
                }, 1000);

                return false;
            }
        });

        clickedNumber.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                pickerSlider.smoothScrollToPosition(integer);
            }
        });

    }

    private void setUpViews() {
        containerCardView = findViewById(R.id.workout_activity_containerCardView);
        circleImageView = findViewById(R.id.workout_activity_circleImageView);
        chronometer = findViewById(R.id.workout_activity_Chronometer);
        currentWorkoutTextView = findViewById(R.id.workout_activity_currentWorkoutTextView);
        pickerSlider = findViewById(R.id.workout_activity_pickerSlider);
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


































