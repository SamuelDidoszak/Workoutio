package com.example.workout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.fragment.ChronometerFragment;
import com.example.workout.fragment.CurrentWorkoutFragment;
import com.example.workout.model.Done;
import com.example.workout.model.QuantityAndReps;
import com.example.workout.ui.adapter.WorkoutRecyclerViewAdapter;

import java.util.List;

public class WorkoutActivity extends AppCompatActivity {

    private static final String TAG = "WorkoutActivity";

    private ChronometerFragment chronometerFragment;
    private CurrentWorkoutFragment currentWorkoutFragment;
    private FragmentContainerView primaryFragmentContainer, secondaryFragmentContainer;
    private CardView containerCardView;
    private RecyclerView workoutRecyclerView;
    private WorkoutRecyclerViewAdapter workoutRecyclerViewAdapter;

//    private boolean firstExercise;

    private DatabaseHandler DB;
    private Context context;
    private List<QuantityAndReps> quantityAndRepsList;

        //  helper variable
    private int currentExerciseId;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_activity);

        context = this;
        DB = new DatabaseHandler(context);

//        firstExercise = Boolean.TRUE;
        quantityAndRepsList = (List<QuantityAndReps>) getIntent().getSerializableExtra("quantityAndReps");

        setUpViews();
        setUpFragments();
            //  variable is only temporary  ================================================================================================
        QuantityAndReps temporaryQuantityAndReps = null;
        if(quantityAndRepsList.size() != 0) {
            temporaryQuantityAndReps = quantityAndRepsList.get(0);
            currentExerciseId = quantityAndRepsList.get(0).getExerciseId();
            currentWorkoutFragment.setExerciseData(quantityAndRepsList.get(0));
        }
        setUpRecyclerView();

        if(temporaryQuantityAndReps != null)
            currentWorkoutFragment.setExerciseData(temporaryQuantityAndReps);

        chronometerFragment.getSaveDone().observe(this, aBoolean -> {
            saveDone(aBoolean);
            if(workoutRecyclerViewAdapter.getItemCount() == 0) {
                currentWorkoutFragment.setCurrentWorkoutTextViewText("");
            }
        });
    }

        //  add time is amount =======================================
    private void setUpRecyclerView() {
        workoutRecyclerViewAdapter = new WorkoutRecyclerViewAdapter(context, quantityAndRepsList);
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        workoutRecyclerView.setAdapter(workoutRecyclerViewAdapter);

        MutableLiveData<QuantityAndReps> chosenExercise = workoutRecyclerViewAdapter.getChosenExercise();
            //  add time is amount =======================================
        chosenExercise.observe(this, quantityAndReps -> {
            if(chronometerFragment.getChronometer().isBackwards()) {
                if(chronometerFragment.getCanSaveInExerciseClick()) {
                    chronometerFragment.setCanSaveInExerciseClick(Boolean.FALSE);
                    saveDone(false);
                    chronometerFragment.setSaved(Boolean.TRUE);
                }
            }
            else {
                chronometerFragment.getChronometer().stop();
                chronometerFragment.getChronometer().init();
            }
            currentExerciseId = quantityAndReps.getExerciseId();
            currentWorkoutFragment.setExerciseData(quantityAndReps);
        });
    }

    private void setUpViews() {
        containerCardView = findViewById(R.id.workout_activity_containerCardView);
            //  RecyclerViews
        workoutRecyclerView = findViewById(R.id.workout_activity_workoutRecyclerView);
            //  FragmentContainers
        primaryFragmentContainer = findViewById(R.id.workout_activity_primaryFragmentContainer);
        secondaryFragmentContainer = findViewById(R.id.workout_activity_secondaryFragmentContainer);
    }

    private void setUpFragments() {
        Bundle chronometerBundle = new Bundle();
        chronometerBundle.putSerializable("WorkoutRecyclerViewAdapter", workoutRecyclerViewAdapter);

        Bundle workoutBundle = new Bundle();

        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).add(R.id.workout_activity_primaryFragmentContainer, ChronometerFragment.class, chronometerBundle, "Chronometer").commit();
//        transaction.setReorderingAllowed(true).add(R.id.workout_activity_currentWorkoutTextView, CurrentWorkoutFragment.class, workoutBundle, "Workout");
//        transaction.commit();

        chronometerFragment = (ChronometerFragment)getSupportFragmentManager().findFragmentByTag("Chronometer");
        currentWorkoutFragment = (CurrentWorkoutFragment)getSupportFragmentManager().findFragmentByTag("Workout");

        Log.d(TAG, "setUpFragments: " + chronometerFragment);
        Log.d(TAG, "setUpFragments: " + currentWorkoutFragment);
    }

    public void saveDone(Boolean addNewExercise) {
        Done done = new Done(currentExerciseId, currentWorkoutFragment.getExerciseAmount(),
                (int)chronometerFragment.getExerciseTime(),
                currentWorkoutFragment.isNegativeCheckboxChecked(), currentWorkoutFragment.isCanMoreCheckboxChecked());
        //DB.addDone(done);

        if(addNewExercise)
            workoutRecyclerViewAdapter.exerciseFinished();
    }
}


































