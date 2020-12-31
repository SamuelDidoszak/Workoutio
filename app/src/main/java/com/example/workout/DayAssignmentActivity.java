package com.example.workout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.fragment.ExerciseMenuFragment;
import com.example.workout.model.DayExerciseConnector;
import com.example.workout.model.Exercise;
import com.example.workout.ui.adapter.ExerciseMenuExercisesRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class DayAssignmentActivity extends AppCompatActivity {
    TextView dayName;
    LinearLayout spaceTop, spaceBottom;
    RecyclerView dayExercisesRecyclerView;
    ExerciseMenuExercisesRecyclerViewAdapter dayExercisesRecyclerViewAdapter;
    ExerciseMenuFragment exerciseMenuFragment;

    List<Exercise> dayExercises;

    Context context;
    DatabaseHandler DB;

    int dayId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_assignment_activity);

        context = getApplicationContext();
        DB = new DatabaseHandler(this);

        dayId = getIntent().getIntExtra("dayId", -1);

        addViews();
    }

    private void addViews() {
        //  TextViews
        dayName = findViewById(R.id.day_assignment_dayName);
        //  Spaces
        spaceTop = findViewById(R.id.day_assignment_spaceTop);
        spaceBottom = findViewById(R.id.day_assignment_spaceBottom);

        //  RecyclerViews
        dayExercisesRecyclerView = findViewById(R.id.day_assignment_dayExercisesRecyclerView);

        List<DayExerciseConnector> dayExerciseConnectorList;
        dayExercises = new ArrayList<>();
        if(dayId != -1) {
            dayName.setText(DB.getDay(dayId).getDayName());
            dayExerciseConnectorList = DB.getDayExerciseConnectorByDay(dayId);
            for(DayExerciseConnector dayExerciseConnector : dayExerciseConnectorList) {
                dayExercises.add(DB.getExercise(dayExerciseConnector.getExerciseId()));
            }
        }
        else {
            Log.d("TAG", "addViews: create a custom set of exercises");
        }
        dayExercisesRecyclerViewAdapter = new ExerciseMenuExercisesRecyclerViewAdapter(context, dayExercises);
        dayExercisesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        dayExercisesRecyclerView.setAdapter(dayExercisesRecyclerViewAdapter);

        // Fragment
        exerciseMenuFragment = new ExerciseMenuFragment(false);
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).add(R.id.day_assignment_exerciseFragmentContainer, ExerciseMenuFragment.class, null, "exerciseMenuFragment").commit();
        getSupportFragmentManager().executePendingTransactions();
        exerciseMenuFragment = (ExerciseMenuFragment) getSupportFragmentManager().findFragmentByTag("exerciseMenuFragment");
    }



}
































