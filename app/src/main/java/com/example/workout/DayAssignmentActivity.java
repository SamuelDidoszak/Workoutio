package com.example.workout;

import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Exercise;
import com.example.workout.model.helper.ExerciseMenuRecyclerViewTypes;
import com.example.workout.ui.adapter.ExerciseMenuExercisesRecyclerViewAdapter;

import java.util.List;

public class DayAssignmentActivity extends AppCompatActivity implements ExerciseMenuRecyclerViewTypes {
    TextView dayName, myExercises, exercises;
    LinearLayout spaceTop, spaceBottom;
    RecyclerView dayExercisesRecyclerView, exerciseRecyclerView;
    private ExerciseMenuExercisesRecyclerViewAdapter myExercisesRecyclerViewAdapter, availableExercisesRecyclerViewAdapter;

    private List<Exercise> myExercisesList, availableExercisesList;
    private DatabaseHandler DB;
    private Context context;
    private int currentRecyclerViewType = MY_EXERCISE_RECYCLER_VIEW;
    private MutableLiveData<Integer> editExercise;
    private int editPosition;
    /**
     * 0 = no changes <br/>
     * 1 = changes in muscles <br/>
     * 2 = changes in exercises <br/>
     * 3 = changes in both
     */
    private int changesInExercises;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_menu);

        context = getApplicationContext();
        changesInExercises = 0;

        DB = new DatabaseHandler(this);

        addViews();
//        addOnClickHandlers();
//
//        exerciseDivision();
//        setUpRecyclerView(currentRecyclerViewType);
    }

    private void addViews() {
        //  TextViews
        dayName = findViewById(R.id.day_assignment_dayName);
        //  RecyclerViews
        dayExercisesRecyclerView = findViewById(R.id.day_assignment_dayExercisesRecyclerView);
        //  Spaces
        spaceTop = findViewById(R.id.day_assignment_spaceTop);
        spaceBottom = findViewById(R.id.day_assignment_spaceBottom);
    }



}
































