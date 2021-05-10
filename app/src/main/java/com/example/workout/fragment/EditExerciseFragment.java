package com.example.workout.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.workout.R;
import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Done;
import com.example.workout.model.Exercise;
import com.example.workout.model.helper.CheckableImageView;
import com.example.workout.ui.adapter.PickerSliderAdapter;

public class EditExerciseFragment extends Fragment {

    private static final String TAG = "EditExerciseFragment";
    private RecyclerView pickerSlider, timePickerSlider;
    private PickerSliderAdapter pickerSliderAdapter, timePickerSliderAdapter;
    private CheckableImageView negativeCheckbox, canMoreCheckbox;
    private boolean changed = false;

    Context context;
    DatabaseHandler DB;

    MutableLiveData<Integer> exerciseAmount, exerciseTime;

    public LiveData<Integer> getExerciseAmount() {
        if(exerciseAmount == null)
            exerciseAmount = new MutableLiveData<>();
        return exerciseAmount;
    }

    public LiveData<Integer> getExerciseTime() {
        if(exerciseTime == null)
            exerciseTime = new MutableLiveData<>();
        return exerciseTime;
    }

    public void resetLiveDatas() {
        exerciseAmount = new MutableLiveData<>();
        exerciseTime = new MutableLiveData<>();
    }

    public boolean isChanged() {
        return changed;
    }

    public EditExerciseFragment() {
    }

    public EditExerciseFragment(Context context) {
        this.context = context;
        DB = new DatabaseHandler(context);
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
        return inflater.inflate(R.layout.fragment_edit_exercise, container, false);
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
        //  RecyclerViews
        pickerSlider = view.findViewById(R.id.workout_activity_pickerSlider);
        timePickerSlider = view.findViewById(R.id.workout_activity_timePickerSlider);
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
                    changed = true;
                    exerciseAmount.setValue(number);
                }, 1000);

                return false;
            }
        });

        clickedNumber.observe(getViewLifecycleOwner(), integer -> {
            pickerSlider.smoothScrollToPosition(integer);
            changed = true;
            exerciseAmount.setValue(integer);
        });


            //  timePickerSlider

        timePickerSliderAdapter = new PickerSliderAdapter(context);
        timePickerSliderAdapter.setMaximumRange(999);
        RecyclerView.LayoutManager timePickerLayoutManager = new LinearLayoutManager(context, RecyclerView.HORIZONTAL, Boolean.FALSE);
        timePickerSlider.setLayoutManager(timePickerLayoutManager);
        timePickerSlider.setAdapter(timePickerSliderAdapter);
        SnapHelper timeSnapHelper = new LinearSnapHelper();
        timeSnapHelper.attachToRecyclerView(timePickerSlider);

        //  Gives the recyclerView paddings that able first and last items to be centered
        padding = screenWidth / 2 - (int)px;
        timePickerSlider.setPadding(padding, 0, padding, 0);

        MutableLiveData<Integer> clickedTime = timePickerSliderAdapter.getClickedNumber();

        timePickerSlider.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {

                new Handler().postDelayed(() -> {
                    View view = timeSnapHelper.findSnapView(timePickerLayoutManager);
                    int number = timePickerSlider.getChildAdapterPosition(view);
                    timePickerSliderAdapter.setExerciseAmount(number);
                    changed = true;
                    exerciseTime.setValue(number * 1000);
                }, 1000);

                return false;
            }
        });

        clickedTime.observe(getViewLifecycleOwner(), integer -> {
            timePickerSlider.smoothScrollToPosition(integer);
            changed = true;
            exerciseTime.setValue(integer * 1000);
        });

    }

    public void setExerciseData(Done done) {
        Exercise exercise = DB.getExercise(done.getExerciseId());
        negativeCheckbox.setChecked(exercise.isDefaultNegative());
        int position = done.getQuantity();
        if(position != -1) {
            ((LinearLayoutManager)pickerSlider.getLayoutManager()).scrollToPositionWithOffset(position, -10);
        }
        int time = done.getTime() / 1000;
        ((LinearLayoutManager)timePickerSlider.getLayoutManager()).scrollToPositionWithOffset(time, -10);
    }

    private void setOnClickAndSwipeListeners() {
        ClickHandler clickHandler = new ClickHandler();

        negativeCheckbox.setOnClickListener(clickHandler.onNegativeCheckboxClick);
        canMoreCheckbox.setOnClickListener(clickHandler.onCanMoreCheckboxClick);
    }

    private class ClickHandler {
        //  Checkboxes
        View.OnClickListener onNegativeCheckboxClick = v -> {
        };
        View.OnClickListener onCanMoreCheckboxClick = v -> {
        };
    }
}