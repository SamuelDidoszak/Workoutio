package com.example.workout.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.EditExerciseMenuActivity;
import com.example.workout.R;
import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Day;
import com.example.workout.model.DayExerciseConnector;
import com.example.workout.model.Exercise;
import com.example.workout.model.helper.DayExercise;
import com.example.workout.model.helper.ExerciseMenuDayExerciseTypes;
import com.example.workout.model.helper.ExerciseMenuRecyclerViewData;
import com.example.workout.model.helper.ExerciseMenuRecyclerViewTypes;
import com.example.workout.ui.adapter.ExerciseMenuDayAdapter;
import com.example.workout.ui.adapter.ExerciseMenuExercisesRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class ExerciseMenuFragment extends Fragment implements ExerciseMenuRecyclerViewTypes, ExerciseMenuDayExerciseTypes {
    private String TAG = "ExerciseMenuActivity";
    private TextView dayTextView, myExercisesTextView, exerciseTextView;
    private RecyclerView exerciseRecyclerView;
    private ExerciseMenuExercisesRecyclerViewAdapter myExercisesRecyclerViewAdapter, availableExercisesRecyclerViewAdapter;
    private ExerciseMenuDayAdapter exerciseMenuDayAdapter;

    private List<Exercise> myExercisesList, availableExercisesList;
    private List<DayExercise> dayExerciseList;
    private DatabaseHandler DB;

    private ExerciseMenuRecyclerViewData[] recyclerViewList = {null, null, null};
    private int currentRecyclerViewType = MY_EXERCISE_RECYCLER_VIEW;
    private MutableLiveData<Integer> chosenExercise;
    private MutableLiveData<Integer> editExercise;
    private int editPosition;
    /**
     * 0 = no changes <br/>
     * 1 = changes in muscles <br/>
     * 2 = changes in exercises <br/>
     * 3 = changes in both
     */
    private int changesInExercises;
    private boolean dayMenuAvailable;

    private Boolean resetDayAdapter = Boolean.TRUE;

    private Context context;

    public ExerciseMenuFragment() {
    }

    public ExerciseMenuFragment(boolean dayMenuAvailable) {
        this.dayMenuAvailable = dayMenuAvailable;
    }

    public int getChangesInExercises() {
        return changesInExercises;
    }

    public LiveData<Integer> getChosenExercise() {
        return chosenExercise;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.exercise_menu_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DB = new DatabaseHandler(context);
        changesInExercises = 0;

        addViews(view);
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
            case MY_EXERCISE_RECYCLER_VIEW:
                //  Called if it's the first creation of this class
                if(myExercisesRecyclerViewAdapter == null) {
                    myExercisesRecyclerViewAdapter = new ExerciseMenuExercisesRecyclerViewAdapter(context, myExercisesList);
                    editExercise = myExercisesRecyclerViewAdapter.getExerciseToEdit();
                    recyclerViewList[1] = myExercisesRecyclerViewAdapter;
                }
                exerciseRecyclerView.setAdapter(myExercisesRecyclerViewAdapter);
                exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                break;
            case DAY_RECYCLER_VIEW:
                if(resetDayAdapter) {
                    dayExerciseDivision();
                    exerciseMenuDayAdapter = new ExerciseMenuDayAdapter(context, dayExerciseList);
                    recyclerViewList[0] = exerciseMenuDayAdapter;
                    resetDayAdapter = Boolean.FALSE;
                }
                exerciseRecyclerView.setAdapter(exerciseMenuDayAdapter);
                exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                break;
            case AVAILABLE_EXERCISE_RECYCLER_VIEW:
                if(availableExercisesRecyclerViewAdapter == null) {
                    availableExercisesRecyclerViewAdapter = new ExerciseMenuExercisesRecyclerViewAdapter(context, availableExercisesList);
                    recyclerViewList[2] = availableExercisesRecyclerViewAdapter;
                }
                exerciseRecyclerView.setAdapter(availableExercisesRecyclerViewAdapter);
                exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                break;
        }
        changeTypeVisually(recyclerViewType);
        setUpObservers();
    }

    /**
     * Sets up observers for the current RecyclerView
     */
    private void setUpObservers() {
        ExerciseMenuRecyclerViewData recyclerView = recyclerViewList[currentRecyclerViewType - 1];
        chosenExercise = recyclerView.getChosenExercise();
        recyclerView.resetExerciseToEdit();
        editExercise = recyclerView.getExerciseToEdit();

        //  start ExerciseMenuDayAdapter if edit imageButton is clicked
        editExercise.observe(getViewLifecycleOwner(), exerciseId -> {
            editPosition = recyclerView.getChosenPosition();
            startActivityForResult(new Intent(context, EditExerciseMenuActivity.class).putExtra("exerciseId", exerciseId), Activity.RESULT_FIRST_USER);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_FIRST_USER) {
            int exerciseId = data.getIntExtra("exerciseId", -1);
            if(exerciseId == -1)
                return;
            boolean[] changeList = data.getBooleanArrayExtra("changeList");
            if(!changeList[0] && !changeList[1])
                return;

            if(changeList[0]) {
                setChangesInExercises(1);
                if(exerciseMenuDayAdapter != null)
                    resetDayAdapter = Boolean.TRUE;
            }

            switch (currentRecyclerViewType) {
                case ExerciseMenuRecyclerViewTypes.MY_EXERCISE_RECYCLER_VIEW:
                    if(changeList[1]) {
                        myExercisesList.set(editPosition, DB.getExercise(exerciseId));
                        myExercisesRecyclerViewAdapter.reSetListOfMuscleListsAtPosition(editPosition);
                        myExercisesRecyclerViewAdapter.notifyItemChanged(editPosition);
                        setChangesInExercises(2);
                    }
                    break;
                case ExerciseMenuRecyclerViewTypes.DAY_RECYCLER_VIEW:
                    if(changeList[0]) {
                        dayExerciseDivision();
                        exerciseMenuDayAdapter = new ExerciseMenuDayAdapter(context, dayExerciseList);
                        exerciseRecyclerView.setAdapter(exerciseMenuDayAdapter);
                        recyclerViewList[0] = exerciseMenuDayAdapter;
                        resetDayAdapter = Boolean.FALSE;
                    }
                    else {
                        exerciseMenuDayAdapter.reSetMuscleListForImagesAtPosition(editPosition);
                        exerciseMenuDayAdapter.notifyItemChanged(editPosition);
                    }
                    break;
                case ExerciseMenuRecyclerViewTypes.AVAILABLE_EXERCISE_RECYCLER_VIEW:
                    if(changeList[1]) {
                        availableExercisesList.set(editPosition, DB.getExercise(exerciseId));
                        availableExercisesRecyclerViewAdapter.reSetListOfMuscleListsAtPosition(editPosition);
                        availableExercisesRecyclerViewAdapter.notifyItemChanged(editPosition);
                        setChangesInExercises(2);
                    }
                    break;
            }
        }
    }

    private void setChangesInExercises(int i) {
        switch (changesInExercises) {
            case 0:
                changesInExercises = i;
                break;
            case 1:
            case 2:
                if(i != changesInExercises)
                    changesInExercises = 3;
                break;
            default:
                changesInExercises = 0;
        }
    }

    /** changes the color of the recyclerView titles*/
    public void changeTypeVisually(int recyclerViewType) {
        /** Resets the background */
        switch(currentRecyclerViewType) {
            case MY_EXERCISE_RECYCLER_VIEW:
                myExercisesTextView.setBackgroundColor(getResources().getColor(R.color.mediumDark));
                break;
            case DAY_RECYCLER_VIEW:
                dayTextView.setBackground(getResources().getDrawable(R.drawable.back_line_right));
                break;
            case AVAILABLE_EXERCISE_RECYCLER_VIEW:
                exerciseTextView.setBackground(getResources().getDrawable(R.drawable.back_line_left));
                break;
        }

        /** Adds the new color */
        switch(recyclerViewType) {
            case MY_EXERCISE_RECYCLER_VIEW:
                myExercisesTextView.setBackgroundColor(getResources().getColor(R.color.hardDark));
                break;
            case DAY_RECYCLER_VIEW:
                dayTextView.setBackgroundColor(getResources().getColor(R.color.hardDark));
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
    public void addViews(View view) {
        // TextView
        dayTextView = view.findViewById(R.id.exercise_menuDayTextView);
        myExercisesTextView = view.findViewById(R.id.exercise_menuMyExercisesTextView);
        exerciseTextView = view.findViewById(R.id.exercise_menuExerciseTextView);
        // RecyclerView
        exerciseRecyclerView = view.findViewById(R.id.exercise_menuExerciseRecyclerView);
        if(!dayMenuAvailable)
            dayTextView.setVisibility(View.GONE);
    }

    /**
     * Adds all of the clickHandlers to the views
     */
    public void addOnClickHandlers() {
        ClickHandler clickHandler = new ClickHandler();
        // TextView
        dayTextView.setOnClickListener(clickHandler.dayTextViewClick);
        myExercisesTextView.setOnClickListener(clickHandler.myExercisesTextViewClick);
        exerciseTextView.setOnClickListener(clickHandler.exerciseTextViewClick);
        // RecyclerView
    }

    /**
     * Class containing clickHandlers for the views
     */
    class ClickHandler {
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































