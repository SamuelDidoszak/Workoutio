package com.example.workout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Day;
import com.example.workout.model.DayExerciseConnector;
import com.example.workout.model.Exercise;
import com.example.workout.model.helper.DayExercise;
import com.example.workout.model.helper.ExerciseMenuDayExerciseTypes;
import com.example.workout.model.helper.ExerciseMenuRecyclerViewTypes;
import com.example.workout.ui.adapter.ExerciseMenuDayAdapter;
import com.example.workout.ui.adapter.ExerciseMenuExercisesRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class ExerciseMenuActivity extends AppCompatActivity implements ExerciseMenuRecyclerViewTypes, ExerciseMenuDayExerciseTypes {

    private String TAG = "ExerciseMenuActivity";
    private LinearLayout spaceTop, spaceBottom;
    private TextView dayTextView, myExercisesTextView, exerciseTextView;
    private RecyclerView exerciseRecyclerView;
    private ExerciseMenuExercisesRecyclerViewAdapter myExercisesRecyclerViewAdapter, availableExercisesRecyclerViewAdapter;
    private ExerciseMenuDayAdapter exerciseMenuDayAdapter;

    private List<Exercise> myExercisesList, availableExercisesList;
    private List<DayExercise> dayExerciseList;
    private DatabaseHandler DB;

    private int currentRecyclerViewType = MY_EXERCISE_RECYCLER_VIEW;
    private MutableLiveData<Integer> chosenExercise;

    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_menu);

        context = getApplicationContext();

        DB = new DatabaseHandler(this);

        addViews();
        addOnClickHandlers();

        exerciseDivision();
        setUpRecyclerView(currentRecyclerViewType);

    }

    /**
     * Creates and sets recyclerView which type is passed as a param
     * @param recyclerViewType type of the recyclerView to create
     */
    public void setUpRecyclerView(int recyclerViewType) {
        switch(recyclerViewType) {
            case DAY_RECYCLER_VIEW:
                if(exerciseMenuDayAdapter == null) {
                    dayExerciseDivision();
                    exerciseMenuDayAdapter = new ExerciseMenuDayAdapter(context, dayExerciseList);
                }
                exerciseRecyclerView.setAdapter(exerciseMenuDayAdapter);
                exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                chosenExercise = exerciseMenuDayAdapter.getChosenExercise();
                break;
            case MY_EXERCISE_RECYCLER_VIEW:
                if(myExercisesRecyclerViewAdapter == null)
                    myExercisesRecyclerViewAdapter = new ExerciseMenuExercisesRecyclerViewAdapter(context, myExercisesList);
                exerciseRecyclerView.setAdapter(myExercisesRecyclerViewAdapter);
                exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                chosenExercise = myExercisesRecyclerViewAdapter.getChosenExercise();
                break;
            case AVAILABLE_EXERCISE_RECYCLER_VIEW:
                if(availableExercisesRecyclerViewAdapter == null)
                    availableExercisesRecyclerViewAdapter = new ExerciseMenuExercisesRecyclerViewAdapter(context, availableExercisesList);
                exerciseRecyclerView.setAdapter(availableExercisesRecyclerViewAdapter);
                exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                chosenExercise = availableExercisesRecyclerViewAdapter.getChosenExercise();
                break;
        }
        changeTypeVisually(recyclerViewType);

            //  wait for user to chose exercise in RecyclerViewAdapter and finish the activity passing exerciseId
        chosenExercise.observe(this, exerciseId -> {
            setResult(RESULT_FIRST_USER,
                    new Intent().putExtra("ExerciseId", exerciseId));
            finish();
        });
    }
    /** changes the color of the recyclerView titles*/
    public void changeTypeVisually(int recyclerViewType) {
        /** Resets the background */
        switch(currentRecyclerViewType) {
            case DAY_RECYCLER_VIEW:
                dayTextView.setBackground(getResources().getDrawable(R.drawable.back_line_right));
                break;
            case MY_EXERCISE_RECYCLER_VIEW:
                myExercisesTextView.setBackgroundColor(getResources().getColor(R.color.mediumDark));
                break;
            case AVAILABLE_EXERCISE_RECYCLER_VIEW:
                exerciseTextView.setBackground(getResources().getDrawable(R.drawable.back_line_left));
                break;
        }

        /** Adds the new color */
        switch(recyclerViewType) {
            case DAY_RECYCLER_VIEW:
                dayTextView.setBackgroundColor(getResources().getColor(R.color.hardDark));
                break;
            case MY_EXERCISE_RECYCLER_VIEW:
                myExercisesTextView.setBackgroundColor(getResources().getColor(R.color.hardDark));
                break;
            case AVAILABLE_EXERCISE_RECYCLER_VIEW:
                exerciseTextView.setBackgroundColor(getResources().getColor(R.color.hardDark));
                break;
        }
        currentRecyclerViewType = recyclerViewType;
    }

    /**
     * Creates a dayExerciseList which contains sorted days and exercises for this day
     */
    public void dayExerciseDivision() {
        List<Day> dayList = DB.getAllDays();
        List<DayExerciseConnector> dayExerciseConnectorList;
        dayExerciseList = new ArrayList<>();
        for(Day day : dayList) {
            dayExerciseList.add(new DayExercise(day.getDayId(), day.getDayName(), TYPE_DAY));
            dayExerciseConnectorList = DB.getDayExerciseConnectorByDay(day.getDayId());
            for(DayExerciseConnector dayExerciseConnector : dayExerciseConnectorList) {
                Exercise exercise = DB.getExercise(dayExerciseConnector.getExerciseId());
                dayExerciseList.add(new DayExercise(
                        dayExerciseConnector.getDayExerciseConnectorId(),
                        exercise.getExerciseId(),
                        exercise.getExerciseName(),
                        TYPE_EXERCISE));
            }
        }
    }

    /**
     * Divides exercises into the ones that are custom and not
     */
    public void exerciseDivision() {
        List<Exercise> allExerciseList = DB.getAllExercises();
        myExercisesList = new ArrayList<>();
        availableExercisesList = new ArrayList<>();
        for(Exercise exercise : allExerciseList) {
            if(exercise.isCustomExercise())
                myExercisesList.add(exercise);
            else
                availableExercisesList.add(exercise);
        }
    }

    /**
     * Adds all of the necessary views
     */
    public void addViews() {
            // Space
        spaceTop = findViewById(R.id.exercise_menuSpaceTop);
        spaceBottom = findViewById(R.id.exercise_menuSpaceBottom);
            // TextView
        dayTextView = findViewById(R.id.exercise_menuDayTextView);
        myExercisesTextView = findViewById(R.id.exercise_menuMyExercisesTextView);
        exerciseTextView = findViewById(R.id.exercise_menuExerciseTextView);
            // RecyclerView
        exerciseRecyclerView = findViewById(R.id.exercise_menuExerciseRecyclerView);
    }

    /**
     * Adds all of the clickHandlers to the views
     */
    public void addOnClickHandlers() {
        ClickHandler clickHandler = new ClickHandler();
            // Space
        spaceTop.setOnClickListener(clickHandler.spaceClick);
        spaceBottom.setOnClickListener(clickHandler.spaceClick);
            // TextView
        dayTextView.setOnClickListener(clickHandler.dayTextViewClick);
        myExercisesTextView.setOnClickListener(clickHandler.myExercisesTextViewClick);
        exerciseTextView.setOnClickListener(clickHandler.exerciseTextViewClick);
            // RecyclerView
        //exerciseRecyclerView.setOnTouchListener(clickHandler.exerciseRecyclerViewClick);
    }

    /**
     * Class containing clickHandlers for the views
     */
    class ClickHandler {
            // Space
        public View.OnClickListener spaceClick = v -> {
            finish();
        };
            // TextView
        public View.OnClickListener dayTextViewClick = v -> {
            setUpRecyclerView(DAY_RECYCLER_VIEW);
        };
        public View.OnClickListener myExercisesTextViewClick = v -> {
            setUpRecyclerView(MY_EXERCISE_RECYCLER_VIEW);
        };
        public View.OnClickListener exerciseTextViewClick = v -> {
            setUpRecyclerView(AVAILABLE_EXERCISE_RECYCLER_VIEW);
        };
    }
}





























