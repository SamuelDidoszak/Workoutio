package com.example.workout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.fragment.ExerciseMenuFragment;
import com.example.workout.model.DayExerciseConnector;
import com.example.workout.model.Exercise;
import com.example.workout.model.helper.SimpleItemTouchHelperCallback;
import com.example.workout.ui.adapter.DayAssignmentRecyclerViewAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DayAssignmentActivity extends AppCompatActivity implements DayAssignmentRecyclerViewAdapter.OnStartDragListener {
    private TextView dayName;
    private LinearLayout spaceTop, spaceBottom;
    private Button backButton, saveButton;
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

    @Override
    protected void onStart() {
        super.onStart();
        exerciseMenuFragment.getChosenExercise().observe(this, integer -> {
            int position = dayExercises.size();
            dayExercises.add(DB.getExercise(integer));
            dayAssignmentRecyclerViewAdapter.notifyItemInserted(position);
            DayAssignmentActivity.this.runOnUiThread(() -> {
                dayAssignmentRecyclerViewAdapter.notifyDataSetChanged();
                dayAssignmentRecyclerViewAdapter.reSetListOfMuscleListsAtPosition(position);
            });
            dayAssignmentRecyclerViewAdapter.getDataChanged().setValue(Boolean.TRUE);
            dayExercisesRecyclerView.scrollToPosition(position);
        });
    }

    private void addViews() {
        //  TextViews
        dayName = findViewById(R.id.day_assignment_dayName);
        //  Spaces
        spaceTop = findViewById(R.id.day_assignment_spaceTop);
        spaceBottom = findViewById(R.id.day_assignment_spaceBottom);
        //  Buttons
        backButton = findViewById(R.id.day_assignment_backButton);
        saveButton = findViewById(R.id.day_assignment_saveButton);

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
        dayAssignmentRecyclerViewAdapter = new DayAssignmentRecyclerViewAdapter(context, dayExercises, dayId, this);
        dayExercisesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        dayExercisesRecyclerView.setAdapter(dayAssignmentRecyclerViewAdapter);
        dayAssignmentRecyclerViewAdapter.getDataChanged().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                backButton.setText(R.string.undo);
                dayAssignmentRecyclerViewAdapter.getDataChanged().removeObserver(this);
            }
        });

        // Fragment
        exerciseMenuFragment = new ExerciseMenuFragment(false);
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).add(R.id.day_assignment_exerciseFragmentContainer, ExerciseMenuFragment.class, null, "exerciseMenuFragment").commit();
        getSupportFragmentManager().executePendingTransactions();
        exerciseMenuFragment = (ExerciseMenuFragment) getSupportFragmentManager().findFragmentByTag("exerciseMenuFragment");

        ClickHandler clickHandler = new ClickHandler();
        backButton.setOnClickListener(clickHandler.onBackButtonClick);
        saveButton.setOnClickListener(clickHandler.onSaveButtonClick);
        spaceTop.setOnClickListener(clickHandler.onSpaceClick);
        spaceBottom.setOnClickListener(clickHandler.onSpaceClick);
    }

    //  I HAVE TO CHANGE IT INTO QUANTITYANDREPS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! =========================================================================================================================================================================================================================================================================================================
    private class ClickHandler {
        View.OnClickListener onBackButtonClick = v -> {
            if(dayAssignmentRecyclerViewAdapter.getDataChanged().getValue() != null) {
                dayAssignmentRecyclerViewAdapter.resetDataChanged();
                dayExercises.clear();
                if(dayId != -1) {
                    List<DayExerciseConnector> dayExerciseConnectorList = DB.getDayExerciseConnectorByDay(dayId);
                    for(DayExerciseConnector dayExerciseConnector : dayExerciseConnectorList) {
                        dayExercises.add(DB.getExercise(dayExerciseConnector.getExerciseId()));
                    }
                }
                dayAssignmentRecyclerViewAdapter.notifyDataSetChanged();
                backButton.setText(R.string.back);
                dayAssignmentRecyclerViewAdapter.getDataChanged().observe(DayAssignmentActivity.this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        backButton.setText(R.string.undo);
                        dayAssignmentRecyclerViewAdapter.getDataChanged().removeObserver(this);
                    }
                });
            }
            else
                finish();
        };

        //  I HAVE TO CHANGE IT INTO QUANTITYANDREPS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! =========================================================================================================================================================================================================================================================================================================
        View.OnClickListener onSaveButtonClick = v -> {
            Intent intent = new Intent();
            if(dayId != -1) {
                boolean saved = dayAssignmentRecyclerViewAdapter.saveChanges();
                //  saved can be null. If it's initialized with the Boolean.FALSE value, it notifies observers upon its creation
                if(saved)
                    intent.putExtra("changeInDay", true);
                else
                    intent.putExtra("changeInDay", false);
            }
            else {
                List<Exercise> exerciseList = dayAssignmentRecyclerViewAdapter.getExercisesList();
                if(exerciseList.size() != 0)
                    intent.putExtra("quantityAndReps", (Serializable)exerciseList);
                else {
                    Toast.makeText(context, "Please choose some exercises", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            setResult(RESULT_FIRST_USER, intent);
            finish();
        };

        View.OnClickListener onSpaceClick = v -> {
            setResult(RESULT_CANCELED);
            finish();
        };
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }
}
































