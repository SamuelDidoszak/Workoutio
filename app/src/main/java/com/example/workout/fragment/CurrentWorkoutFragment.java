package com.example.workout.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.workout.R;
import com.example.workout.controller.SwipeDetection;
import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Exercise;
import com.example.workout.model.QuantityAndReps;
import com.example.workout.model.helper.CheckableImageView;
import com.example.workout.ui.adapter.PickerSliderAdapter;

public class CurrentWorkoutFragment extends Fragment {

    private static final String TAG = "WorkoutFragment";
    private TextView currentWorkoutTextView;
    private RecyclerView pickerSlider;
    private PickerSliderAdapter pickerSliderAdapter;
    private CheckableImageView negativeCheckbox, canMoreCheckbox;

    Context context;
    DatabaseHandler DB;

    private int exerciseAmount;

    public CurrentWorkoutFragment() {
    }

    public CurrentWorkoutFragment(Context context) {
        this.context = context;
        DB = new DatabaseHandler(context);
    }

    public void setCurrentWorkoutTextViewText(String text) {
        currentWorkoutTextView.setText(text);
    }

    public int getExerciseAmount() {
        return exerciseAmount;
    }
    public boolean isNegativeCheckboxChecked() {
        return negativeCheckbox.isChecked();
    }
    public boolean isCanMoreCheckboxChecked() {
        return canMoreCheckbox.isChecked();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DB = new DatabaseHandler(context);
        setUpViews(view);
        setOnClickAndSwipeListeners();
        setUpRecyclerViews();
    }

    private void setUpViews(View view) {
        //  TextViews
        currentWorkoutTextView = view.findViewById(R.id.workout_activity_currentWorkoutTextView);
        //  RecyclerViews
        pickerSlider = view.findViewById(R.id.workout_activity_pickerSlider);
        //  CheckBoxes
        negativeCheckbox = view.findViewById(R.id.workout_activity_negativeCheckBox);
        canMoreCheckbox = view.findViewById(R.id.workout_activity_canMoreCheckBox);
    }


        //  add time is amount =======================================
    private void setUpRecyclerViews() {
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

        clickedNumber.observe(getViewLifecycleOwner(), integer -> {
            pickerSlider.smoothScrollToPosition(integer);
            exerciseAmount = integer;
        });
    }

    public void setExerciseData(QuantityAndReps quantityAndReps) {
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

    private void setOnClickAndSwipeListeners() {
        ClickHandler clickHandler = new ClickHandler();

        negativeCheckbox.setOnClickListener(clickHandler.onNegativeCheckboxClick);
        canMoreCheckbox.setOnClickListener(clickHandler.onCanMoreCheckboxClick);
        //  onSwipeListeners
        SwipeDetection textSwipeDetection = new SwipeDetection(context);
        currentWorkoutTextView.setOnTouchListener((v, event) -> textSwipeDetection.onTouch(currentWorkoutTextView, event));
        textSwipeDetection.getSwipeDirection().observe(getViewLifecycleOwner(), s -> {
            if(s.equals("right")) {
//                canSaveInExerciseClick = Boolean.FALSE;
//                saveDone(true);
            }
        });
    }

    private class ClickHandler {
        //  Checkboxes
        View.OnClickListener onNegativeCheckboxClick = v -> {
        };
        View.OnClickListener onCanMoreCheckboxClick = v -> {
        };
        //  Button
        View.OnClickListener onFinishButtonClick = v -> {
            Toast.makeText(context, "The workout is finished!", Toast.LENGTH_SHORT).show();
        };
    }
}




























