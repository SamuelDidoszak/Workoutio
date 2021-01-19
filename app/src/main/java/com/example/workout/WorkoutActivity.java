package com.example.workout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.fragment.ChronometerFragment;
import com.example.workout.fragment.CurrentWorkoutFragment;
import com.example.workout.fragment.DoneExercisesFragment;
import com.example.workout.fragment.EditExerciseFragment;
import com.example.workout.model.Done;
import com.example.workout.model.QuantityAndReps;
import com.example.workout.model.helper.Chronometer;
import com.example.workout.ui.adapter.WorkoutRecyclerViewAdapter;

import java.io.Serializable;
import java.util.List;

public class WorkoutActivity extends AppCompatActivity {

    private static final String TAG = "WorkoutActivity";

    private FragmentContainerView primaryFragmentContainer, secondaryFragmentContainer;
    private ChronometerFragment chronometerFragment;
    private CurrentWorkoutFragment currentWorkoutFragment;
    private EditExerciseFragment editExerciseFragment;
    private DoneExercisesFragment doneExercisesFragment;

    private CardView containerCardView;
    private RecyclerView workoutRecyclerView;
    private WorkoutRecyclerViewAdapter workoutRecyclerViewAdapter;
    private ImageButton addButton;

    private ConstraintLayout doneExercises_bottomContainer;
    private TextView doneExercises_remainingExercisesNumber;
    private Button doneExercises_continueButton, doneExercises_finishButton;
    private Chronometer doneExercises_workoutTimeChronometer;

    private boolean lastExercise = false;

    private DatabaseHandler DB;
    private Context context;
    private List<QuantityAndReps> quantityAndRepsList;

        //  helper variable
    private int currentExerciseId;

        //  will be extracted as an option
    private final boolean addAsCurrent = false;

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
            if((Integer)currentExerciseId != null)
                saveDone(aBoolean);
            if(workoutRecyclerViewAdapter.getItemCount() == 0) {
                if (lastExercise)
                    currentWorkoutFragment.setCurrentWorkoutTextViewText("");
                lastExercise = true;
            }
        });
        watchFragmentChange();
        
        addButton.setOnClickListener(v -> startActivityForResult(new Intent(this, ExerciseMenuActivity.class), RESULT_FIRST_USER));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_FIRST_USER) {
            int exerciseId = data.getIntExtra("ExerciseId", -1);
            if(exerciseId == -1)
                return;

            workoutRecyclerViewAdapter.addNewQAR(new QuantityAndReps(exerciseId, context, null), addAsCurrent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        long overallTimeBase = getIntent().getLongExtra("overallTimeBase", 0);
        if(overallTimeBase != 0) {
            chronometerFragment.initiateWorkoutTimeChronometer(overallTimeBase);
        }
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
            //  ImageButtons
        addButton = findViewById(R.id.app_toolbar_addButton);

            //  DoneExercises
        doneExercises_bottomContainer = findViewById(R.id.workout_activity_doneExercises_bottomContainer);
        doneExercises_remainingExercisesNumber = findViewById(R.id.workout_activity_doneExercises_remainingExercisesNumberTextView);
        doneExercises_workoutTimeChronometer = findViewById(R.id.workout_activity_doneExercises_workoutTimeChronometer);
        doneExercises_continueButton = findViewById(R.id.workout_activity_doneExercises_continueButton);
        doneExercises_finishButton = findViewById(R.id.workout_activity_doneExercises_finishButton);
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
                if(aBoolean == Boolean.TRUE) {
                    getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                            .replace(R.id.workout_activity_primaryFragmentContainer, DoneExercisesFragment.class, null, "doneExercises").addToBackStack("chronometerBackStack").commit();
                    getSupportFragmentManager().executePendingTransactions();
                    doneExercisesFragment = (DoneExercisesFragment) getSupportFragmentManager().findFragmentByTag("doneExercises");

                    secondaryFragmentContainer.setVisibility(View.GONE);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 8f);
                    primaryFragmentContainer.setLayoutParams(params);

                    workoutRecyclerView.setVisibility(View.GONE);
                    doneExercises_bottomContainer.setVisibility(View.VISIBLE);

                    doneExercises_remainingExercisesNumber.setText(String.valueOf(workoutRecyclerViewAdapter.getItemCount() + (chronometerFragment.getChronometer().isBackwards() ? 0 : 1)));
                    doneExercises_workoutTimeChronometer.setBase(chronometerFragment.getWorkoutTimeChronometer().getBase());
                    doneExercises_workoutTimeChronometer.start();
                    doneExercises_continueButton.setOnClickListener(v -> doneExercisesFragment.getFragmentFinished().setValue(Boolean.TRUE));
                    doneExercises_finishButton.setOnClickListener(v -> {
                        Intent intent = new Intent(context, WorkoutOverviewActivity.class);
                        intent.putExtra("overallTime", chronometerFragment.getWorkoutTimeChronometer().getText().toString());
                        intent.putExtra("overallTimeBase", chronometerFragment.getWorkoutTimeChronometer().getBase());
                        intent.putExtra("remainingQuantityAndReps", (Serializable)workoutRecyclerViewAdapter.getQuantityAndRepsList());
                        finish();
                        startActivity(intent);
                    });

                    //  Pops up the secondaryFragment with the exercise data in it
                    if(doneExercisesFragment.getChosenDone() != null) {
                        doneExercisesFragment.getChosenDone().observe(this, done -> {
                            doneExercisesFragment.getChosenDone();
                            int id = doneExercisesFragment.getDoneId();
                            setUpSecondaryFragment(id);
                            editExerciseFragment.setExerciseData(done);
                        });
                    }

                    doneExercisesFragment.getFragmentFinished().observe(this, aBoolean1 -> watchFragmentChange());
                }
            });
        }
        else {
            getSupportFragmentManager().popBackStack("chronometerBackStack", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().executePendingTransactions();

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 5f);
            primaryFragmentContainer.setLayoutParams(params);

            getSupportFragmentManager().popBackStack("currentWorkoutBackStack", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().executePendingTransactions();
            secondaryFragmentContainer.setVisibility(View.VISIBLE);

            workoutRecyclerView.setVisibility(View.VISIBLE);
            doneExercises_bottomContainer.setVisibility(View.GONE);

            currentWorkoutFragment = (CurrentWorkoutFragment)getSupportFragmentManager().findFragmentByTag("currentWorkout");
        }
    }

    private void setUpSecondaryFragment(int id) {
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                .replace(R.id.workout_activity_secondaryFragmentContainer, EditExerciseFragment.class, null, "editExercise").addToBackStack("currentWorkoutBackStack").commit();
        getSupportFragmentManager().executePendingTransactions();
        editExerciseFragment = (EditExerciseFragment) getSupportFragmentManager().findFragmentByTag("editExercise");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 6f);
        primaryFragmentContainer.setLayoutParams(params);

        secondaryFragmentContainer.setVisibility(View.VISIBLE);

        editExerciseFragment.resetLiveDatas();

        LifecycleOwner owner = this;
        do {
            try {
                if(editExerciseFragment.isAdded()) {
                    editExerciseFragment.getExerciseAmount().observe(owner, number -> {
                        quantityAndRepsList.get(id).setQuantity(number);
                        doneExercisesFragment.notifyItemChanged(id, number, null);
                    });
                    editExerciseFragment.getExerciseTime().observe(owner, number -> {
                        doneExercisesFragment.notifyItemChanged(id, null, number);
                    });
                    break;
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } while (true);
    }

    public void saveDone(Boolean addNewExercise) {
        Done done = new Done(currentExerciseId, currentWorkoutFragment.getExerciseAmount(),
                (int)chronometerFragment.getExerciseTime(),
                currentWorkoutFragment.isNegativeCheckboxChecked(), currentWorkoutFragment.isCanMoreCheckboxChecked());
        DB.addDone(done);

        if(addNewExercise)
            workoutRecyclerViewAdapter.exerciseFinished();
    }
}


































