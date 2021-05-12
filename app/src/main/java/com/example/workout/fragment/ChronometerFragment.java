package com.example.workout.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.workout.R;
import com.example.workout.WorkoutOverviewActivity;
import com.example.workout.controller.SwipeDetection;
import com.example.workout.model.helper.Chronometer;
import com.example.workout.ui.adapter.WorkoutRecyclerViewAdapter;

import java.io.Serializable;

public class ChronometerFragment extends Fragment implements Serializable {

    private static final String TAG = "ChronometerFragment";

    private ImageView circleImageView;
    private TextView finishTextView;
    private com.example.workout.model.helper.Chronometer chronometer, workoutTimeChronometer;
    private LinearLayout workoutTimeContainer;
    private Button finishButton;

    private Context context;

        //  helper variables
    private Boolean canSaveInExerciseClick = Boolean.FALSE;
    private Boolean lastOfStartExerciseAfterRest = Boolean.FALSE;
    private boolean firstExercise;
    private Boolean saved;
    private long exerciseTime;
    private MutableLiveData<Boolean> saveDone;
    private MutableLiveData<Boolean> showDoneExercises;
    private WorkoutRecyclerViewAdapter workoutRecyclerViewAdapter;

    private long pauseTimeBase = 0, pauseExerciseTime = 0, overallTimeBase = 0;
    private boolean isWorkoutChronoVisible = false;


        //  getters and setters
        public Chronometer getChronometer() {
            return chronometer;
        }

        public Chronometer getWorkoutTimeChronometer() {
            return workoutTimeChronometer;
        }

    public Boolean getCanSaveInExerciseClick() {
        return canSaveInExerciseClick;
    }
    public void setCanSaveInExerciseClick(Boolean canSaveInExerciseClick) {
        this.canSaveInExerciseClick = canSaveInExerciseClick;
    }

    public void setSaved(Boolean saved) {
        this.saved = saved;
    }

    public long getExerciseTime() {
        return exerciseTime;
    }

    public LiveData<Boolean> getSaveDone() {
        if(saveDone == null)
            saveDone = new MutableLiveData<>();
        return saveDone;
    }

    public LiveData<Boolean> showDoneExercises() {
        if(showDoneExercises == null)
            showDoneExercises = new MutableLiveData<>();
        return showDoneExercises;
    }

    //  will be exported into the settings
    /**
     * Rest time in seconds
     */
    private int REST_TIME = 60;
    private Boolean startExerciseAfterRest = Boolean.FALSE;


    public ChronometerFragment() {
        super(R.layout.fragment_chronometer);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setRetainInstance(true);
        this.context = context;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(chronometer.isBackwards()) {
            pauseTimeBase = chronometer.getBase();
            if(chronometer.getAnimation() != null)
                pauseExerciseTime = chronometer.getTimeElapsed() * (-1);
        }
        else if(chronometer.getTimeElapsed() != 0) {
            pauseChronometer();
            pauseExerciseTime = chronometer.getTimeElapsed() * (-1);
        }
        if(workoutTimeChronometer.isStarted())
            overallTimeBase = workoutTimeChronometer.getBase();

    }

    @Override
    public void onResume() {
        super.onResume();
        if(pauseTimeBase != 0) {
            if(pauseExerciseTime == 0) {
                chronometer.setBase(pauseTimeBase);
                chronometer.setBackwards(true);
                chronometer.start();
            }
            else {
                chronometer.setBaseWithCurrentTime(pauseExerciseTime);
                chronometer.setBackwards(true);
                pauseChronometer();
            }
        }
        else if(pauseExerciseTime != 0) {
            chronometer.setBaseWithCurrentTime(pauseExerciseTime);
            pauseChronometer();
        }
        if(overallTimeBase != 0) {
            workoutTimeChronometer.setBase(overallTimeBase);
            workoutTimeChronometer.start();
            setWorkoutChronometerVisibility();
        }
        pauseTimeBase = 0;
        pauseExerciseTime = 0;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chronometer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments() != null)
            workoutRecyclerViewAdapter = (WorkoutRecyclerViewAdapter)getArguments().getSerializable("workoutRecyclerViewAdapter");
        saved = Boolean.FALSE;
        firstExercise = Boolean.TRUE;

        setUpViews(view);
        setOnClickAndSwipeListeners();
    }


    private void pauseOrStart() {
        if(chronometer.isStarted()) {
            pauseChronometer();
        }
        else {
            chronometer.clearAnimation();
            if(chronometer.isBackwards())
                chronometer.setBaseWithCurrentTime(chronometer.getTimeElapsed());
            else
                chronometer.setBaseWithCurrentTime(chronometer.getTimeElapsed() * (-1));
            chronometer.start();
        }
    }

    private void pauseChronometer() {
        chronometer.stop();
        Animation blinking = new AlphaAnimation(0.0f, 1.0f);
        blinking.setDuration(500);
        blinking.setRepeatMode(Animation.REVERSE);
        blinking.setRepeatCount(Animation.INFINITE);
        chronometer.startAnimation(blinking);
    }

    private void setWorkoutChronometerVisibility() {
        if(isWorkoutChronoVisible)
            workoutTimeChronometer.setVisibility(View.VISIBLE);
        else
            workoutTimeChronometer.setVisibility(View.INVISIBLE);
    }

    public void initiateWorkoutTimeChronometer(@Nullable Long baseTime) {
        if(baseTime == null)
            baseTime = chronometer.getBase();
        workoutTimeChronometer.setDelayMillis(1000);
        workoutTimeChronometer.setBase(baseTime);
        workoutTimeChronometer.start();
    }

    private void countTime() {
        //  runs if it was the last exercise
        if(workoutRecyclerViewAdapter.getItemCount() == 0 && !lastOfStartExerciseAfterRest && !saved) {
            exerciseTime = chronometer.getTimeElapsed();
            saveDone.setValue(false);
            finishWorkout();
            return;
        }

        canSaveInExerciseClick = Boolean.TRUE;
        if(chronometer.isStarted() && !chronometer.isBackwards()) {
            chronometer.stop();
            exerciseTime = chronometer.getTimeElapsed();

            if(firstExercise) {
                workoutTimeChronometer.setVisibility(View.VISIBLE);
                firstExercise = Boolean.FALSE;
                isWorkoutChronoVisible = true;
            }

            if(!lastOfStartExerciseAfterRest) {
                chronometer.startBackwards(REST_TIME);
                chronometer.start();
            }
            else {
                if(workoutRecyclerViewAdapter.getItemCount() == 0) {
                    saveDone.setValue(false);
                    finishWorkout();
                    return;
                }
            }

            workoutRecyclerViewAdapter.setDuringRest(Boolean.TRUE);

            if(startExerciseAfterRest) {
                chronometer.observeIfTimeFinished(0).observe(getViewLifecycleOwner(), aBoolean -> {
                    chronometer.stop();
                    workoutRecyclerViewAdapter.setDuringRest(Boolean.FALSE);
                    if(!saved) {
                        saved = true;
                        canSaveInExerciseClick = Boolean.FALSE;
                        saveDone.setValue(true);
                        if(workoutRecyclerViewAdapter.getItemCount() == 0)
                            lastOfStartExerciseAfterRest = Boolean.TRUE;
                    }
                    chronometer.removeTimeStop();
                    countTime();
                });
            }
        }
        else {
            if(!startExerciseAfterRest && chronometer.isStarted() && !firstExercise) {
                if(!saved) {
                    canSaveInExerciseClick = Boolean.FALSE;
                    saveDone.setValue(true);
                }
            }
            chronometer.init();
            chronometer.start();
            if(firstExercise)
                initiateWorkoutTimeChronometer(null);
            workoutRecyclerViewAdapter.setDuringRest(Boolean.FALSE);
        }
        saved = Boolean.FALSE;
    }


    private void setUpViews(View view) {
        circleImageView = view.findViewById(R.id.workout_activity_circleImageView);
        finishTextView = view.findViewById(R.id.workout_activity_finishTextView);
        //  Chronometers
        chronometer = view.findViewById(R.id.workout_activity_Chronometer);
        workoutTimeChronometer = view.findViewById(R.id.workout_activity_workoutTimeChronometer);
        //  Button
        finishButton = view.findViewById(R.id.workout_activity_finishButton);

        workoutTimeContainer = view.findViewById(R.id.workout_activity_workoutTimeContainer);
        Log.d(TAG, "setUpViews: views set");
    }

    private void setOnClickAndSwipeListeners() {
        ClickHandler clickHandler = new ClickHandler();
        workoutTimeContainer.setOnClickListener(clickHandler.onWorkoutTimeClick);
        //  onSwipeListeners
        SwipeDetection textSwipeDetection = new SwipeDetection(context);
        textSwipeDetection.getSwipeDirection().observe(getViewLifecycleOwner(), s -> {
            if(s.equals("right")) {
//                canSaveInExerciseClick = Boolean.FALSE;
//                saveDone.setValue(true);
            }
        });

        SwipeDetection circleSwipeDetection = new SwipeDetection(context);
        circleImageView.setOnTouchListener((v, event) -> circleSwipeDetection.onTouch(circleImageView, event));
        circleSwipeDetection.getSwipeDirection().observe(getViewLifecycleOwner(), s -> {
            switch(s) {
                case "up":
                case "down":
                    pauseOrStart();
                    break;
                case "left":
                case "right":
                    Log.d(TAG, "Left||Right");
                    showDoneExercises.setValue(Boolean.TRUE);
                    break;
                case "tap":
                    if(chronometer.getAnimation() != null) {
                        pauseOrStart();
                        return;
                    }
                    countTime();
                    break;
            }
        });
    }

    private class ClickHandler {
        View.OnClickListener onWorkoutTimeClick = v -> {
            isWorkoutChronoVisible = !isWorkoutChronoVisible;
            setWorkoutChronometerVisibility();
        };
        //  Button
        View.OnClickListener onFinishButtonClick = v -> {
            getActivity().finish();
            Intent intent = new Intent(context, WorkoutOverviewActivity.class);
            intent.putExtra("overallTime", workoutTimeChronometer.getText().toString());
            intent.putExtra("overallTimeBase", workoutTimeChronometer.getBase());
            intent.putExtra("remainingQuantityAndReps", (Serializable)workoutRecyclerViewAdapter.getQuantityAndRepsList());
            startActivity(intent);
        };
    }

    private void finishWorkout() {
        workoutTimeChronometer.stop();
        workoutTimeChronometer.setVisibility(View.INVISIBLE);
        chronometer.stop();

        String exerciseTime = workoutTimeChronometer.getText().toString();
        chronometer.setText(exerciseTime);

        finishTextView.setVisibility(View.VISIBLE);
        finishButton.setVisibility(View.VISIBLE);
        finishButton.setOnClickListener(new ClickHandler().onFinishButtonClick);
    }

}





































