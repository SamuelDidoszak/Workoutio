package com.example.workout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.fragment.ExerciseMenuFragment;
import com.example.workout.model.DayExerciseConnector;
import com.example.workout.model.Exercise;
import com.example.workout.model.helper.SimpleItemTouchHelperCallback;
import com.example.workout.ui.adapter.DayAssignmentRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class DayAssignmentActivity extends AppCompatActivity implements DayAssignmentRecyclerViewAdapter.OnStartDragListener {
    private TextView dayName;
    private LinearLayout spaceTop, spaceBottom;
    private RecyclerView dayExercisesRecyclerView;
    private DayAssignmentRecyclerViewAdapter dayAssignmentRecyclerViewAdapter;
    private ExerciseMenuFragment exerciseMenuFragment;
    private ItemTouchHelper itemTouchHelper;

    private List<Exercise> dayExercises;

    private Context context;
    private DatabaseHandler DB;

    int dayId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_assignment_activity);

        context = getApplicationContext();
        DB = new DatabaseHandler(this);

        dayId = getIntent().getIntExtra("dayId", -1);

        addViews();
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(dayAssignmentRecyclerViewAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(dayExercisesRecyclerView);
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
            dayName.setText(R.string.custom);
        }
        dayAssignmentRecyclerViewAdapter = new DayAssignmentRecyclerViewAdapter(context, dayExercises, this);
        dayExercisesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        dayExercisesRecyclerView.setAdapter(dayAssignmentRecyclerViewAdapter);

        // Fragment
        exerciseMenuFragment = new ExerciseMenuFragment(false);
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).add(R.id.day_assignment_exerciseFragmentContainer, ExerciseMenuFragment.class, null, "exerciseMenuFragment").commit();
        getSupportFragmentManager().executePendingTransactions();
        exerciseMenuFragment = (ExerciseMenuFragment) getSupportFragmentManager().findFragmentByTag("exerciseMenuFragment");
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        Log.d("TAG", "onStartDrag: jeff");
        itemTouchHelper.startDrag(viewHolder);
    }
}
































