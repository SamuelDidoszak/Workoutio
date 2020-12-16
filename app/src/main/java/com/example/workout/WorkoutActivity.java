package com.example.workout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.fragment.ChronometerFragment;
import com.example.workout.fragment.CurrentWorkoutFragment;
import com.example.workout.fragment.DoneExercisesFragment;
import com.example.workout.model.Done;
import com.example.workout.model.QuantityAndReps;
import com.example.workout.ui.adapter.WorkoutRecyclerViewAdapter;

import java.util.List;

public class WorkoutActivity extends AppCompatActivity {

    private static final String TAG = "WorkoutActivity";

    private FragmentContainerView primaryFragmentContainer, secondaryFragmentContainer;
    private ChronometerFragment chronometerFragment;
    private CurrentWorkoutFragment currentWorkoutFragment;
    private DoneExercisesFragment doneExercisesFragment;

    private CardView containerCardView;
    private RecyclerView workoutRecyclerView;
    private WorkoutRecyclerViewAdapter workoutRecyclerViewAdapter;

    private boolean lastExercise = false;

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

        quantityAndRepsList = (List<QuantityAndReps>) getIntent().getSerializableExtra("quantityAndReps");

        setUpViews();
        setUpRecyclerView();
        setUpFragments();

        chronometerFragment.getSaveDone().observe(this, aBoolean -> {
            saveDone(aBoolean);
            if(workoutRecyclerViewAdapter.getItemCount() == 0) {
                if (lastExercise)
                    currentWorkoutFragment.setCurrentWorkoutTextViewText("");
                lastExercise = true;
            }
        });
        watchFragmentChange();
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
        chronometerBundle.putSerializable("workoutRecyclerViewAdapter", workoutRecyclerViewAdapter);

        currentWorkoutFragment = new CurrentWorkoutFragment(context);

        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).add(R.id.workout_activity_primaryFragmentContainer, ChronometerFragment.class, chronometerBundle).commit();
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).add(R.id.workout_activity_secondaryFragmentContainer, currentWorkoutFragment, "currentWorkout").commit();
        getSupportFragmentManager().executePendingTransactions();

        chronometerFragment = (ChronometerFragment)getSupportFragmentManager().findFragmentById(R.id.workout_activity_primaryFragmentContainer);
        currentWorkoutFragment = (CurrentWorkoutFragment)getSupportFragmentManager().findFragmentByTag("currentWorkout");
    }

    private void watchFragmentChange() {
        if(chronometerFragment.isAdded()) {
            chronometerFragment.showDoneExercises().observe(this, aBoolean -> {
                Log.d(TAG, "watchFragmentChange: ye");
                if(aBoolean == Boolean.TRUE) {
                    Log.d(TAG, "watchFragmentChange: true");
                    getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                            .replace(R.id.workout_activity_primaryFragmentContainer, DoneExercisesFragment.class, null, "doneExercises").addToBackStack("chronometerBackStack").commit();
                    getSupportFragmentManager().executePendingTransactions();

                    doneExercisesFragment = (DoneExercisesFragment) getSupportFragmentManager().findFragmentByTag("doneExercises");
                    Log.d(TAG, "watchFragmentChange: " + doneExercisesFragment);
                    Log.d(TAG, "watchFragmentChange: " + doneExercisesFragment.getLifecycle().getCurrentState().toString());
                }
            });
        }
        else {
            Log.d(TAG, "watchFragmentChange: ???");
            doneExercisesFragment.showDoneExercises().observe(this, aBoolean -> {
                Log.d(TAG, "watchFragmentChange: ?????");
                if(aBoolean == Boolean.FALSE) {
                    getSupportFragmentManager().popBackStack("chronometerBackStack", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getSupportFragmentManager().executePendingTransactions();
                }
            });
        }
    }

    private void replaceFragments() {
        if(chronometerFragment.isVisible()) {
            getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                    .replace(R.id.workout_activity_primaryFragmentContainer, DoneExercisesFragment.class, null, "doneExercises").addToBackStack("chronometerBackStack");
            getSupportFragmentManager().executePendingTransactions();
            doneExercisesFragment = (DoneExercisesFragment)getSupportFragmentManager().findFragmentByTag("doneExercises");
        }
        else {
            getSupportFragmentManager().popBackStack("chronometerBackStack", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    public void saveDone(Boolean addNewExercise) {
        Done done = new Done(currentExerciseId, currentWorkoutFragment.getExerciseAmount(),
                (int)chronometerFragment.getExerciseTime(),
                currentWorkoutFragment.isNegativeCheckboxChecked(), currentWorkoutFragment.isCanMoreCheckboxChecked());
        DB.addDone(done);

        Log.d(TAG, DB.getExercise(done.getExerciseId()).getExerciseName() + ", " + done.getQuantity() + ", " + done.getTime() + ", " + done.isNegative() + ", " + done.isCanMore());

        if(addNewExercise)
            workoutRecyclerViewAdapter.exerciseFinished();
    }
}


































