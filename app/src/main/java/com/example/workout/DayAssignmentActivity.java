package com.example.workout;

import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;

public class DayAssignmentActivity extends AppCompatActivity {
    TextView dayName;
    LinearLayout spaceTop, spaceBottom;
    RecyclerView dayExercisesRecyclerView;

    Context context;
    DatabaseHandler DB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_menu);

        context = getApplicationContext();

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
































