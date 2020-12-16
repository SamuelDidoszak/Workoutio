package com.example.workout.fragment;

import android.content.Context;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.workout.R;
import com.example.workout.controller.SwipeDetection;
import com.example.workout.model.helper.Chronometer;
import com.example.workout.ui.adapter.WorkoutRecyclerViewAdapter;

public class ChronometerFragment extends Fragment {

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


        //  getters and setters
        public Chronometer getChronometer() {
            return chronometer;
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
    private int REST_TIME = 1;
    private Boolean startExerciseAfterRest = Boolean.FALSE;


    public ChronometerFragment() {
        super(R.layout.chronometer_fragment);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setRetainInstance(true);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chronometer_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments() != null)
            workoutRecyclerViewAdapter = (WorkoutRecyclerViewAdapter)requireArguments().getSerializable("workoutRecyclerViewAdapter");
        saved = Boolean.FALSE;
        firstExercise = Boolean.TRUE;

        setUpViews(view);
        setOnClickAndSwipeListeners();
    }


    private void pauseOrStart() {
        if(chronometer.isStarted()) {
            chronometer.stop();
            Animation blinking = new AlphaAnimation(0.0f, 1.0f);
            blinking.setDuration(500);
            blinking.setRepeatMode(Animation.REVERSE);
            blinking.setRepeatCount(Animation.INFINITE);
            chronometer.startAnimation(blinking);
        }
        else {
            chronometer.clearAnimation();
            chronometer.setBaseWithCurrentTime(chronometer.getTimeElapsed() * (-1));
            chronometer.start();
        }
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
                workoutTimeChronometer.setDelayMillis(1000);
                workoutTimeChronometer.setBase(chronometer.getBase());
                workoutTimeChronometer.start();
                workoutTimeChronometer.setVisibility(View.VISIBLE);
                firstExercise = Boolean.FALSE;
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
    }

    private void setOnClickAndSwipeListeners() {
        ClickHandler clickHandler = new ClickHandler();
        circleImageView.setOnClickListener(clickHandler.onCircleClick);
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
                    countTime();
                    break;
            }
        });
    }

    private class ClickHandler {
        View.OnClickListener onCircleClick = v -> {
            countTime();
        };
        View.OnClickListener onWorkoutTimeClick = v -> {
            if(workoutTimeChronometer.getVisibility() == View.VISIBLE)
                workoutTimeChronometer.setVisibility(View.INVISIBLE);
            else
                workoutTimeChronometer.setVisibility(View.VISIBLE);
        };
        //  Button
        View.OnClickListener onFinishButtonClick = v -> {
            Toast.makeText(getContext(), "The workout is finished!", Toast.LENGTH_SHORT).show();
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





































