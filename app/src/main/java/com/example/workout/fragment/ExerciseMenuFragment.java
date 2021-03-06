package com.example.workout.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.DayAssignmentActivity;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ExerciseMenuFragment extends Fragment implements ExerciseMenuRecyclerViewTypes, ExerciseMenuDayExerciseTypes {
    private String TAG = "ExerciseMenuFragment";
    private TextView dayTextView, myExercisesTextView, exerciseTextView;
    private RecyclerView exerciseRecyclerView;
    private ExerciseMenuExercisesRecyclerViewAdapter myExercisesRecyclerViewAdapter, availableExercisesRecyclerViewAdapter;
    private ExerciseMenuDayAdapter exerciseMenuDayAdapter, customExerciseMenuDayAdapter;
    private FloatingActionButton floatingActionButton;

    private List<Exercise> myExercisesList, availableExercisesList;

    private ExerciseMenuRecyclerViewData[] recyclerViewList = {null, null, null};
    private int currentRecyclerViewType = MY_EXERCISE_RECYCLER_VIEW;
    private MutableLiveData<Integer> chosenExercise;
    private MutableLiveData<Integer> editExercise;
    private MutableLiveData<Integer> pickedExercise;
    private int editPosition;
    /**
     * 0 = no changes <br/>
     * 1 = changes in muscles <br/>
     * 2 = changes in exercises <br/>
     * 3 = changes in both
     */
    private int changesInExercises;
    private boolean dayMenuAvailable;

    /**
     * 0: !customDayAdapter
     * 1: customDayAdapter
     */
    private boolean[] resetDayAdapter;
    private boolean isDayAdapterCustom;

    private DatabaseHandler DB;
    private Context context;

    public ExerciseMenuFragment() {
    }

    public ExerciseMenuFragment(boolean dayMenuAvailable) {
        this.dayMenuAvailable = dayMenuAvailable;
    }

    public int getChangesInExercises() {
        return changesInExercises;
    }

    public LiveData<Integer> getPickedExercise() {
        return pickedExercise;
    }

    public RecyclerView getExerciseRecyclerView() {
        return exerciseRecyclerView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercise_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DB = new DatabaseHandler(context);
        changesInExercises = 0;
        chosenExercise = new MutableLiveData<>();
        pickedExercise = new MutableLiveData<>();

        resetDayAdapter = new boolean[] {true, true};
        isDayAdapterCustom = false;

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
                if(!floatingActionButton.isShown())
                    floatingActionButton.show();
                break;
            case DAY_RECYCLER_VIEW:
                if(!isDayAdapterCustom) {
                    if (resetDayAdapter[0]) {
                        exerciseMenuDayAdapter = new ExerciseMenuDayAdapter(context, dayExerciseDivision(false));
                        resetDayAdapter[0] = false;
                    }
                    dayTextView.setText("Day");
                    recyclerViewList[0] = exerciseMenuDayAdapter;
                }
                else {
                    if (resetDayAdapter[1]) {
                        customExerciseMenuDayAdapter = new ExerciseMenuDayAdapter(context, dayExerciseDivision(true));
                        resetDayAdapter[1] = false;
                    }
                    dayTextView.setText("Groups");
                    recyclerViewList[0] = customExerciseMenuDayAdapter;
                }
                exerciseRecyclerView.setAdapter((ExerciseMenuDayAdapter)recyclerViewList[0]);
                exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                if (!floatingActionButton.isShown())
                    floatingActionButton.show();
                break;
            case AVAILABLE_EXERCISE_RECYCLER_VIEW:
                if(availableExercisesRecyclerViewAdapter == null) {
                    availableExercisesRecyclerViewAdapter = new ExerciseMenuExercisesRecyclerViewAdapter(context, availableExercisesList);
                    recyclerViewList[2] = availableExercisesRecyclerViewAdapter;
                }
                exerciseRecyclerView.setAdapter(availableExercisesRecyclerViewAdapter);
                exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                if(floatingActionButton.isShown())
                    floatingActionButton.hide();
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
        recyclerView.resetChosenExercise();
        chosenExercise = recyclerView.getChosenExercise();

        //  Notify observers that an exercise was picked
        chosenExercise.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                pickedExercise.setValue(integer);
            }
        });

        recyclerView.resetExerciseToEdit();
        editExercise = recyclerView.getExerciseToEdit();

        //  start ExerciseMenuDayAdapter if edit imageButton is clicked
        editExercise.observe(getViewLifecycleOwner(), exerciseId -> {
            editPosition = recyclerView.getChosenPosition();
            Intent intent = new Intent(context, EditExerciseMenuActivity.class);
            intent.putExtra("exerciseId", exerciseId);
            intent.putExtra("startWorkout", false);
            startActivityForResult(intent, Activity.RESULT_FIRST_USER);
        });

        if(currentRecyclerViewType == DAY_RECYCLER_VIEW) {
            ((ExerciseMenuDayAdapter)recyclerViewList[0]).resetChosenDay();
            ((ExerciseMenuDayAdapter)recyclerViewList[0]).getChosenDay().observe(getViewLifecycleOwner(), integer -> {
                Log.d(TAG, "setUpObservers: " + DB.getDay(integer).getDayName());
                Intent intent = new Intent(context, DayAssignmentActivity.class);
                intent.putExtra("dayId", integer);
                intent.putExtra("startWorkout", false);
                startActivityForResult(intent, 2);
            });
        }
        exerciseRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(currentRecyclerViewType == ExerciseMenuRecyclerViewTypes.AVAILABLE_EXERCISE_RECYCLER_VIEW)
                    return;
                if(dy > 0) {
                    if (floatingActionButton.isShown())
                        floatingActionButton.hide();
                }
                else {
                    if (!floatingActionButton.isShown())
                        floatingActionButton.show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_CANCELED)
            return;
        if(requestCode == 2) {
            int changeInDay = data.getIntExtra("changeInDay", 0);
            exerciseRecyclerView.setAdapter(null);
            if(changeInDay == 1) {
                exerciseMenuDayAdapter = new ExerciseMenuDayAdapter(context, dayExerciseDivision(false));
                if(!isDayAdapterCustom) {
                    exerciseRecyclerView.setAdapter(exerciseMenuDayAdapter);
                    recyclerViewList[0] = exerciseMenuDayAdapter;
                }
                resetDayAdapter[0] = false;
                setUpObservers();
            }
            else if(changeInDay == 2) {
                List<DayExercise> dayExerciseList = dayExerciseDivision(true);
                customExerciseMenuDayAdapter = new ExerciseMenuDayAdapter(context, dayExerciseList);
                exerciseRecyclerView.setAdapter(customExerciseMenuDayAdapter);
                if(data.getBooleanExtra("newDay", false))
                    exerciseRecyclerView.scrollToPosition(customExerciseMenuDayAdapter.getItemCount() - 1);
                recyclerViewList[0] = customExerciseMenuDayAdapter;
                dayTextView.setText("Groups");
                resetDayAdapter[1] = false;
                setUpObservers();
            }
        }
        else if(resultCode == Activity.RESULT_FIRST_USER) {
            int exerciseId = data.getIntExtra("exerciseId", -1);
            if(exerciseId == -1)
                return;

            boolean[] changeList = data.getBooleanArrayExtra("changeList");

            if(requestCode == 3) {
                int position = myExercisesList.size();
                myExercisesList.add(DB.getExercise(exerciseId));
                myExercisesRecyclerViewAdapter.notifyItemInserted(position);
                myExercisesRecyclerViewAdapter.reSetListOfMuscleListsAtPosition(position);
                exerciseRecyclerView.scrollToPosition(position);
            }

            if(!changeList[0] && !changeList[1] && !changeList[3])
                return;

            if(changeList[0]) {
                setChangesInExercises(1);
                resetDayAdapter[0] = true;
                resetDayAdapter[1] = true;
                Log.d(TAG, "onActivityResult: changed days");
            }

            //  If the exercise is newly added, then the upcoming switch statement is unnecessary
            if(requestCode == 3) {
                //  everything about the exercise is changed
                changesInExercises = 3;
                return;
            }

            switch (currentRecyclerViewType) {
                case ExerciseMenuRecyclerViewTypes.MY_EXERCISE_RECYCLER_VIEW:
                    if(changeList[1] || changeList[3]) {
                        myExercisesList.set(editPosition, DB.getExercise(exerciseId));
                        myExercisesRecyclerViewAdapter.reSetListOfMuscleListsAtPosition(editPosition);
                        myExercisesRecyclerViewAdapter.notifyItemChanged(editPosition);
                        setChangesInExercises(2);
                    }
                    break;
                case ExerciseMenuRecyclerViewTypes.DAY_RECYCLER_VIEW:
                    if(changeList[0] || changeList[3]) {
                        if(isDayAdapterCustom) {
                            customExerciseMenuDayAdapter = new ExerciseMenuDayAdapter(context, dayExerciseDivision(true));
                            recyclerViewList[0] = customExerciseMenuDayAdapter;
                        }
                        else {
                            exerciseMenuDayAdapter = new ExerciseMenuDayAdapter(context, dayExerciseDivision(false));
                            recyclerViewList[0] = exerciseMenuDayAdapter;
                        }
                        resetDayAdapter[0] = false;
                        exerciseRecyclerView.setAdapter((ExerciseMenuDayAdapter)recyclerViewList[0]);
                        setUpObservers();
                    }
                    else {
                        exerciseMenuDayAdapter.reSetMuscleListForImagesAtPosition(editPosition);
                        exerciseMenuDayAdapter.notifyItemChanged(editPosition);
                    }
                    break;
                case ExerciseMenuRecyclerViewTypes.AVAILABLE_EXERCISE_RECYCLER_VIEW:
                    if(changeList[1] || changeList[2] || changeList[3]) {
                        int newExerciseId = data.getIntExtra("newExerciseId", -1);
                        availableExercisesList.set(editPosition, DB.getExercise(newExerciseId));
                        availableExercisesRecyclerViewAdapter.reSetListOfMuscleListsAtPosition(editPosition);
                        availableExercisesRecyclerViewAdapter.notifyItemChanged(editPosition);
                        setChangesInExercises(2);

                        myExercisesList.add(DB.getExercise(exerciseId));
                        myExercisesRecyclerViewAdapter.notifyItemInserted(myExercisesList.size() - 1);
                        myExercisesRecyclerViewAdapter.reSetListOfMuscleListsAtPosition(myExercisesList.size() - 1);
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
        //  Resets the background
        switch(currentRecyclerViewType) {
            case MY_EXERCISE_RECYCLER_VIEW:
                myExercisesTextView.setBackgroundColor(getResources().getColor(R.color.colorSurfaceElevation1dp));
                myExercisesTextView.setTextColor(getResources().getColor(R.color.white));
                break;
            case DAY_RECYCLER_VIEW:
                dayTextView.setBackgroundColor(getResources().getColor(R.color.colorSurfaceElevation1dp));
                dayTextView.setTextColor(getResources().getColor(R.color.white));
                break;
            case AVAILABLE_EXERCISE_RECYCLER_VIEW:
                exerciseTextView.setBackgroundColor(getResources().getColor(R.color.colorSurfaceElevation1dp));
                exerciseTextView.setTextColor(getResources().getColor(R.color.white));
                break;
        }

        //  Adds the new color
        switch(recyclerViewType) {
            case MY_EXERCISE_RECYCLER_VIEW:
                myExercisesTextView.setBackground(getResources().getDrawable(R.drawable.back_line_bottom));
                myExercisesTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
            case DAY_RECYCLER_VIEW:
                dayTextView.setBackground(getResources().getDrawable(R.drawable.back_line_bottom));
                dayTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
            case AVAILABLE_EXERCISE_RECYCLER_VIEW:
                exerciseTextView.setBackground(getResources().getDrawable(R.drawable.back_line_bottom));
                exerciseTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
        }
        currentRecyclerViewType = recyclerViewType;
    }

    /**
     * Creates a dayExerciseList which contains sorted days and exercises for days or groups
     * @Param: true when return list should contain custom exercises, false if they shouldn't be custom
     * @Return: dayExerciseList with divided exercises for days
     */
    public List<DayExercise> dayExerciseDivision(boolean custom) {
        //  Optimizations: when user added a day/group or an exercise, i can remove its DayExercise, add new DayExercise and then notifyItemRangeInserted
        List<Day> dayList = DB.getAllDays();
        for(int i = 0; i < dayList.size(); i ++) {
            if(dayList.get(i).isCustom() != custom) {
                dayList.remove(i);
                i--;
            }
        }
        List<DayExerciseConnector> dayExerciseConnectorList;
        List<DayExercise> dayExerciseList = new ArrayList<>();
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
        return dayExerciseList;
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
        //  TextView
        dayTextView = view.findViewById(R.id.exercise_menu_dayTextView);
        myExercisesTextView = view.findViewById(R.id.exercise_menu_myExercisesTextView);
        exerciseTextView = view.findViewById(R.id.exercise_menu_exerciseTextView);
        //  RecyclerView
        exerciseRecyclerView = view.findViewById(R.id.exercise_menuExerciseRecyclerView);
        if(!dayMenuAvailable)
            dayTextView.setVisibility(View.GONE);
        //  FloatingActionButton
        floatingActionButton = view.findViewById(R.id.floating_action_button);
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
        //  FloatingActionButton
        floatingActionButton.setOnClickListener(clickHandler.onFloatingActionButtonClick);
    }

    /**
     * Class containing clickHandlers for the views
     */
    class ClickHandler {
        // TextView
        public View.OnClickListener dayTextViewClick = v -> {
            if(currentRecyclerViewType == TYPE_DAY)
                isDayAdapterCustom = !isDayAdapterCustom;
            setUpRecyclerView(DAY_RECYCLER_VIEW);
        };
        public View.OnClickListener myExercisesTextViewClick = v -> setUpRecyclerView(MY_EXERCISE_RECYCLER_VIEW);
        public View.OnClickListener exerciseTextViewClick = v -> setUpRecyclerView(AVAILABLE_EXERCISE_RECYCLER_VIEW);
        public View.OnClickListener onFloatingActionButtonClick = v -> {
            if(currentRecyclerViewType == TYPE_DAY) {
                Intent intent = new Intent(context, DayAssignmentActivity.class);
                intent.putExtra("dayId", -1);
                intent.putExtra("startWorkout", false);
                startActivityForResult(intent, 2);
            }
            else {
                Intent intent = new Intent(context, EditExerciseMenuActivity.class);
                intent.putExtra("exerciseId", -1);
                startActivityForResult(intent, 3);
            }
        };
    }
}
































