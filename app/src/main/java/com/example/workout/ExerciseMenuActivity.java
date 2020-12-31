package com.example.workout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workout.fragment.ExerciseMenuFragment;

public class ExerciseMenuActivity extends AppCompatActivity{

    private String TAG = "ExerciseMenuActivity";
    private ExerciseMenuFragment exerciseMenuFragment;
    private LinearLayout spaceTop, spaceBottom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_menu);

        setUpViews();
        setOnClickListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //  wait for user to chose exercise in RecyclerViewAdapter and finish the activity passing exerciseId
        exerciseMenuFragment.getChosenExercise().observe(this, exerciseId -> {
            Intent intent = new Intent();
            intent.putExtra("ExerciseId", exerciseId);
            intent.putExtra("changesInExercises", exerciseMenuFragment.getChangesInExercises());
            setResult(Activity.RESULT_FIRST_USER, intent);
            finish();
        });
    }

    private void setUpViews() {
        // Space
        spaceTop = findViewById(R.id.exercise_menu_spaceTop);
        spaceBottom = findViewById(R.id.exercise_menu_spaceBottom);
        // Fragment
        exerciseMenuFragment = new ExerciseMenuFragment(true);
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).add(R.id.exercise_menu_fragmentContainer, exerciseMenuFragment, "exerciseMenuFragment").commit();
        getSupportFragmentManager().executePendingTransactions();
        exerciseMenuFragment = (ExerciseMenuFragment) getSupportFragmentManager().findFragmentByTag("exerciseMenuFragment");
    }

    private void setOnClickListeners() {
        spaceTop.setOnClickListener(v -> {
            setResult(RESULT_CANCELED,
                    new Intent().putExtra("changesInExercises", exerciseMenuFragment.getChangesInExercises()));
            finish();
        });
        spaceBottom.setOnClickListener(v -> {
            setResult(RESULT_CANCELED,
                    new Intent().putExtra("changesInExercises", exerciseMenuFragment.getChangesInExercises()));
            finish();
        });
    }

}





























