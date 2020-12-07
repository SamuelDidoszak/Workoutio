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
import android.widget.LinearLayout;
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

import com.example.workout.controller.SwipeDetection;
import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Done;
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
    private com.example.workout.model.helper.Chronometer chronometer, workoutTimeChronometer;
    private RecyclerView pickerSlider, workoutRecyclerView;
    private WorkoutRecyclerViewAdapter workoutRecyclerViewAdapter;
    private PickerSliderAdapter pickerSliderAdapter;
    private CheckableImageView negativeCheckbox, canMoreCheckbox;
    private LinearLayout workoutTimeContainer;

    private boolean firstExercise = Boolean.TRUE;

    private Boolean saved = Boolean.FALSE;
    private int currentExerciseId;
    private int exerciseAmount;
    private long exerciseTime;
    
    //  will be exported into the settings
    /**
     * Rest time in seconds
     */
    private final int REST_TIME = 5;
    private final Boolean startExerciseAfterRest = Boolean.FALSE;

    Boolean canSaveInExerciseClick = Boolean.FALSE;

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
            //  variable is only temporary  ================================================================================================
        QuantityAndReps temporaryQuantityAndReps = null;
        if(quantityAndRepsList.size() != 0) {
            temporaryQuantityAndReps = quantityAndRepsList.get(0);
            currentExerciseId = quantityAndRepsList.get(0).getExerciseId();
            currentWorkoutTextView.setText(quantityAndRepsList.get(0).getExerciseName());
        }
        setUpRecyclerViews();

        if(temporaryQuantityAndReps != null)
            setExerciseData(temporaryQuantityAndReps);
    }

        //  add time is amount =======================================
    private void setUpRecyclerViews() {
        workoutRecyclerViewAdapter = new WorkoutRecyclerViewAdapter(context, quantityAndRepsList);
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        workoutRecyclerView.setAdapter(workoutRecyclerViewAdapter);

        MutableLiveData<QuantityAndReps> chosenExercise = workoutRecyclerViewAdapter.getChosenExercise();
            //  add time is amount =======================================
        chosenExercise.observe(this, quantityAndReps -> {
            if(chronometer.isBackwards()) {
                if(canSaveInExerciseClick) {
                    canSaveInExerciseClick = Boolean.FALSE;
                    saveDone(false);
                    saved = Boolean.TRUE;
                }
            }
            else {
                chronometer.stop();
                chronometer.init();
            }
                //  maybe assign it in a few places or some shit
            currentExerciseId = quantityAndReps.getExerciseId();
            setExerciseData(quantityAndReps);
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
                    exerciseAmount = number;
                }, 1000);

                return false;
            }
        });

        clickedNumber.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                pickerSlider.smoothScrollToPosition(integer);
                exerciseAmount = integer;
            }
        });

    }

    private void setExerciseData(QuantityAndReps quantityAndReps) {
        Exercise exercise = DB.getExercise(quantityAndReps.getExerciseId());
        currentWorkoutTextView.setText(exercise.getExerciseName());
        negativeCheckbox.setChecked(exercise.isDefaultNegative());
        int position = quantityAndReps.getQuantity();
        if(position != -1) {
            ((LinearLayoutManager)pickerSlider.getLayoutManager()).scrollToPositionWithOffset(position, -10);
        }

            //  add time is amount =======================================
        if(exercise.isTimeAsAmount())
            Log.d(TAG, "Time is the amount");

    }

    private void setUpViews() {
        containerCardView = findViewById(R.id.workout_activity_containerCardView);
        circleImageView = findViewById(R.id.workout_activity_circleImageView);
            //  Chronometers
        chronometer = findViewById(R.id.workout_activity_Chronometer);
        workoutTimeChronometer = findViewById(R.id.workout_activity_workoutTimeChronometer);

        currentWorkoutTextView = findViewById(R.id.workout_activity_currentWorkoutTextView);
        pickerSlider = findViewById(R.id.workout_activity_pickerSlider);
        workoutRecyclerView = findViewById(R.id.workout_activity_workoutRecyclerView);
            //  CheckBoxes
        negativeCheckbox = findViewById(R.id.workout_activity_negativeCheckBox);
        canMoreCheckbox = findViewById(R.id.workout_activity_canMoreCheckBox);

        workoutTimeContainer = findViewById(R.id.workout_activity_workoutTimeContainer);
    }

    private void setOnClickListeners() {
        ClickHandler clickHandler = new ClickHandler();
        circleImageView.setOnClickListener(clickHandler.onCircleClick);
        workoutTimeContainer.setOnClickListener(clickHandler.onWorkoutTimeClick);

        negativeCheckbox.setOnClickListener(clickHandler.onNegativeCheckboxClick);
        canMoreCheckbox.setOnClickListener(clickHandler.onCanMoreCheckboxClick);


        SwipeDetection detection = new SwipeDetection(context);
        currentWorkoutTextView.setOnTouchListener((v, event) -> detection.onTouch(currentWorkoutTextView, event));
        detection.getSwipeDirection().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.equals("right")) {
                    canSaveInExerciseClick = Boolean.FALSE;
                    saveDone(true);
                }
            }
        });
    }

    private void saveDone(Boolean addNewExercise) {
        Done done = new Done(currentExerciseId, exerciseAmount,
                (int)exerciseTime,
                negativeCheckbox.isChecked(), canMoreCheckbox.isChecked());
        DB.addDone(done);

        if(addNewExercise)
            workoutRecyclerViewAdapter.exerciseFinished();
    }

    private void countTime() {
        canSaveInExerciseClick = Boolean.TRUE;
        if(chronometer.isStarted() && !chronometer.isBackwards()) {
            chronometer.stop();
            exerciseTime = chronometer.getTimeElapsed();

            if(firstExercise) {
                workoutTimeChronometer.setDelayMillis(1000);
                workoutTimeChronometer.setBase(chronometer.getBase());
                workoutTimeChronometer.start();
                workoutTimeChronometer.setVisibility(View.VISIBLE);
                firstExercise = Boolean.FALSE;
            }

            chronometer.startBackwards(REST_TIME);
            chronometer.start();
            workoutRecyclerViewAdapter.setDuringRest(Boolean.TRUE);

            if(startExerciseAfterRest) {
                Log.d(TAG, "starting observer");
                chronometer.observeIfTimeFinished().observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        chronometer.stop();
                        workoutRecyclerViewAdapter.setDuringRest(Boolean.FALSE);
                        if(!saved) {
                            Log.d(TAG, "saving done from observer");
                            canSaveInExerciseClick = Boolean.FALSE;
                            saveDone(true);
                        }
                        saved = Boolean.FALSE;
                        countTime();
                    }
                });
            }
        }
        else {
            if(!startExerciseAfterRest && chronometer.isStarted() && !firstExercise) {
                if(!saved) {
                    canSaveInExerciseClick = Boolean.FALSE;
                    saveDone(true);
                    saved = Boolean.TRUE;
                }
            }
            chronometer.init();
            chronometer.start();
            workoutRecyclerViewAdapter.setDuringRest(Boolean.FALSE);
            saved = Boolean.FALSE;
        }
    }

    private class ClickHandler {
        View.OnClickListener onCircleClick = v -> {
            countTime();
        };
        View.OnClickListener onWorkoutTimeClick = v -> {
            Log.d(TAG, "clicked: ");
            if(workoutTimeChronometer.getVisibility() == View.VISIBLE)
                workoutTimeChronometer.setVisibility(View.INVISIBLE);
            else
                workoutTimeChronometer.setVisibility(View.VISIBLE);
        };
            //  Checkboxes
        View.OnClickListener onNegativeCheckboxClick = v -> {
        };
        View.OnClickListener onCanMoreCheckboxClick = v -> {
        };
    }
}


































