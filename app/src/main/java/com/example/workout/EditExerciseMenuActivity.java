package com.example.workout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Day;
import com.example.workout.model.DayExerciseConnector;
import com.example.workout.model.Exercise;
import com.example.workout.model.Muscle;
import com.example.workout.model.MuscleExerciseConnector;
import com.example.workout.model.helper.CheckableImageView;

import java.util.Arrays;
import java.util.List;

public class EditExerciseMenuActivity extends AppCompatActivity {

    private EditText exerciseNameEditText;
    private CardView cardView;
    private RecyclerView daysRecyclerView, musclesRecyclerView;
    private CheckableImageView timeAsCountCheckbox, defaultNegativeCheckbox;
    private Button saveButton;

    private List<Day> allDaysList;
    private List<Muscle> allMusclesList;
    /**
     * 0: Change in Days <br/>
     * 1: Change in Muscles <br/>
     * 2: Change in CheckBoxes <br/>
     * 3: Change in Name
     */
    private boolean[] changeList;
    /**
     * Two dimensional array of booleans set true if there is a day/muscle for corresponding position in all days/muscles
     */
    private Boolean[][] inDayInclusionList, inMuscleInclusionList;

    int exerciseId;
    private Context context;
    private DatabaseHandler DB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_exercise_menu);

        exerciseId = getIntent().getIntExtra("exerciseId", -1);
        this.context = getApplicationContext();
        DB = new DatabaseHandler(context);

        setViews();
        assignItems();
        fillViews(exerciseId);

        ClickHandler clickHandler = new ClickHandler();
        timeAsCountCheckbox.setOnClickListener(clickHandler.onTimeAsCountClick);
        defaultNegativeCheckbox.setOnClickListener(clickHandler.onDefaultNegativeClick);
        saveButton.setOnClickListener(clickHandler.onSaveButtonClick);
        //  ClickHandlers that hide keyboard
        cardView.setOnClickListener(clickHandler.onCardViewClick);
        daysRecyclerView.setOnTouchListener(clickHandler.hideKeyboardOnTouch);
        daysRecyclerView.addOnItemTouchListener(clickHandler.hideKeyboardOnItemTouch);
        musclesRecyclerView.setOnTouchListener(clickHandler.hideKeyboardOnTouch);
        musclesRecyclerView.addOnItemTouchListener(clickHandler.hideKeyboardOnItemTouch);
    }

    private void setViews() {
            //  EditText
        exerciseNameEditText = findViewById(R.id.edit_exercise_menu_exerciseNameEditText);
            //  RecyclerViews
        daysRecyclerView = findViewById(R.id.edit_exercise_menu_daysRecyclerView);
        musclesRecyclerView = findViewById(R.id.edit_exercise_menu_musclesRecyclerView);
            //  CheckBoxes
        timeAsCountCheckbox = findViewById(R.id.edit_exercise_menu_timeAsCountCheckbox);
        defaultNegativeCheckbox = findViewById(R.id.edit_exercise_menu_defaultNegativeCheckbox);
            //  Button
        saveButton = findViewById(R.id.edit_exercise_menu_saveButton);
            //  CardView
        cardView = findViewById(R.id.edit_exercise_menu_cardView);
    }

    /**
     * Checks if starting values are the same as the new ones
     */
    private void saveChanges() {
        Intent intent = new Intent();

        if(saveButton.getText().toString().equals(getResources().getString(R.string.back))){
            setResult(RESULT_CANCELED);
            finish();
        }
        if(exerciseId == -1) {
            String exerciseName = exerciseNameEditText.getText().toString();
            if(exerciseName.length() == 0) {
                exerciseNameEditText.setHintTextColor(getResources().getColor(R.color.mediumRed));
                return;
            }
            Exercise exercise = new Exercise(exerciseName, true, timeAsCountCheckbox.isChecked(), defaultNegativeCheckbox.isChecked());
            DB.addExercise(exercise);
            exerciseId = exercise.getExerciseId();
            changeList[2] = false;
        }
        if(!DB.getExercise(exerciseId).isCustomExercise()) {
            for(int i = 1; i < 4; i++) {
                if(changeList[i]) {
                    int newExerciseId = DB.transferExercise(exerciseId);
                    intent.putExtra("newExerciseId", newExerciseId);
                    break;
                }
            }
        }
        if (changeList[0]) {
            for (int i = 0; i < inDayInclusionList.length; i++) {
                if (inDayInclusionList[i][0] != inDayInclusionList[i][1]) {
                    //  connector was deleted
                    if (inDayInclusionList[i][0])
                        DB.removeDayExerciseConnectorById(DB.getDayExerciseConnector(allDaysList.get(i).getDayId(), exerciseId).getDayExerciseConnectorId());
                        //  connector was added
                    else
                        DB.addDayExerciseConnector(new DayExerciseConnector(allDaysList.get(i).getDayId(), exerciseId));
                }
            }
        }
        if (changeList[1]) {
            for (int i = 0; i < inMuscleInclusionList.length; i++) {
                if (inMuscleInclusionList[i][0] != inMuscleInclusionList[i][1]) {
                    //  connector was deleted
                    if (inMuscleInclusionList[i][0])
                        DB.removeMuscleExerciseConnectorById(DB.getMuscleExerciseConnector(allMusclesList.get(i).getMuscleId(), exerciseId).getMuscleExerciseConnectorId());
                        //  connector was added
                    else
                        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(allMusclesList.get(i).getMuscleId(), exerciseId));
                }
            }
        }
        if (changeList[2]) {
            Boolean defaultNegative = defaultNegativeCheckbox.isChecked() ? Boolean.TRUE : Boolean.FALSE;
            Boolean timeAsAmount = timeAsCountCheckbox.isChecked() ? Boolean.TRUE : Boolean.FALSE;

            Exercise exercise = DB.getExercise(exerciseId);
            exercise.setDefaultNegative(defaultNegative);
            exercise.setTimeAsAmount(timeAsAmount);
            DB.editExercise(exercise);
        }
        if (changeList[3]) {
            Exercise exercise = DB.getExercise(exerciseId);
            exercise.setExerciseName(exerciseNameEditText.getText().toString());
            DB.editExercise(exercise);
        }

        intent.putExtra("exerciseId", exerciseId);
        intent.putExtra("changeList", changeList);
        setResult(RESULT_FIRST_USER, intent);
        finish();
    }

    private class ClickHandler {
        View.OnClickListener onTimeAsCountClick = v -> {
            hideKeyboard();
            changeList[2] = Boolean.TRUE;
            saveButton.setText(R.string.save);
        };
        View.OnClickListener onDefaultNegativeClick = v -> {
            hideKeyboard();
            changeList[2] = Boolean.TRUE;
            saveButton.setText(R.string.save);
        };
        View.OnClickListener onSaveButtonClick = v -> {
            saveChanges();
        };
        //  CardView
        public View.OnClickListener onCardViewClick = v -> {
            hideKeyboard();
        };

        View.OnTouchListener hideKeyboardOnTouch = (v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboard();
            }
            return false;
        };

        RecyclerView.OnItemTouchListener hideKeyboardOnItemTouch = new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                hideKeyboard();
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        };

        private void hideKeyboard() {
            cardView.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) EditExerciseMenuActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(cardView.getWindowToken(), 0);
        }
    }

    private void fillViews(int exerciseId) {
        Exercise exercise = DB.getExercise(exerciseId);

        if(exerciseId != -1) {
            exerciseNameEditText.setText(exercise.getExerciseName());
            timeAsCountCheckbox.setChecked(exercise.isTimeAsAmount());
            defaultNegativeCheckbox.setChecked(exercise.isDefaultNegative());
        }
        exerciseNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0)
                    saveButton.setText(R.string.save);
                else
                    saveButton.setText(R.string.back);

                if(exerciseId != -1) {
                    if(!s.toString().equals(exercise.getExerciseName()))
                        changeList[3] = true;
                    else {
                        changeList[3] = false;
                        for(int i = 0; i < 3; i++) {
                            if(changeList[i])
                                return;
                        }
                        saveButton.setText(R.string.back);
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        DaysRecyclerViewAdapter daysRecyclerViewAdapter = new DaysRecyclerViewAdapter();
        daysRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        daysRecyclerView.setAdapter(daysRecyclerViewAdapter);

        MusclesRecyclerViewAdapter musclesRecyclerViewAdapter = new MusclesRecyclerViewAdapter();
        musclesRecyclerView.setLayoutManager(new GridLayoutManager(context, 3, RecyclerView.VERTICAL, Boolean.FALSE));
        musclesRecyclerView.setAdapter(musclesRecyclerViewAdapter);

    }

    private void assignItems() {
        allDaysList = DB.getAllDays();
        allMusclesList = DB.getAllMuscles();

        List<Day> dayList = DB.getDaysByExerciseId(exerciseId);
        List<Muscle> muscleList = DB.getMusclesByExerciseId(exerciseId);

        changeList = new boolean[4];
        Arrays.fill(changeList, Boolean.FALSE);

        inDayInclusionList = new Boolean[allDaysList.size()][2];
        inMuscleInclusionList = new Boolean[allMusclesList.size()][2];

            //  Creates a list of booleans which are necessary to show which elements are related to this exercise and which are not
        Boolean added;
        for(int i = 0; i < allDaysList.size(); i++) {
            added = Boolean.FALSE;
            for(Day inDay : dayList) {
                if(allDaysList.get(i).getDayId() == inDay.getDayId()) {
                    inDayInclusionList[i][0] = Boolean.TRUE;
                    inDayInclusionList[i][1] = Boolean.TRUE;
                    added = Boolean.TRUE;
                    break;
                }
            }
            if(!added) {
                inDayInclusionList[i][0] = Boolean.FALSE;
                inDayInclusionList[i][1] = Boolean.FALSE;
            }
        }

        for(int i = 0; i < allMusclesList.size(); i++) {
            Muscle muscle = allMusclesList.get(i);
            added = Boolean.FALSE;
            for(Muscle inMuscle : muscleList) {
                if(muscle.getMuscleId() == inMuscle.getMuscleId()) {
                    inMuscleInclusionList[i][0] = Boolean.TRUE;
                    inMuscleInclusionList[i][1] = Boolean.TRUE;
                    added = Boolean.TRUE;
                    break;
                }
            }
            if(!added) {
                inMuscleInclusionList[i][0] = Boolean.FALSE;
                inMuscleInclusionList[i][1] = Boolean.FALSE;
            }
        }
    }

    private class DaysRecyclerViewAdapter extends RecyclerView.Adapter<DaysRecyclerViewAdapter.ViewHolder> {

        @NonNull
        @Override
        public DaysRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.edit_exercise_menu_day_row, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DaysRecyclerViewAdapter.ViewHolder holder, int position) {
            Day day = allDaysList.get(position);

            holder.dayNameTextView.setText(day.getDayName());
            holder.changeBackground(inDayInclusionList[position][1]);
        }

        @Override
        public int getItemCount() {
            return allDaysList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            TextView dayNameTextView;
            
            public void changeBackground(Boolean inDayInclusion) {
                dayNameTextView.setBackgroundColor(getResources().getColor(R.color.hardDark));
                if(inDayInclusion) {
                    dayNameTextView.setBackgroundColor(getResources().getColor(R.color.mediumDark));
                }
            }

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                dayNameTextView = itemView.findViewById(R.id.edit_exercise_menu_day_row_dayNameTextView);
                
                dayNameTextView.setOnClickListener(onDayClick);
            }

            private View.OnClickListener onDayClick = v -> {
                int position = getAdapterPosition();
                inDayInclusionList[position][1] = !inDayInclusionList[position][1];
                changeBackground(inDayInclusionList[position][1]);
                changeList[0] = Boolean.TRUE;
                saveButton.setText(R.string.save);
            };
        }
    }

    private class MusclesRecyclerViewAdapter extends RecyclerView.Adapter<MusclesRecyclerViewAdapter.ViewHolder> {

        @NonNull
        @Override
        public MusclesRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.edit_exercise_menu_muscle_row, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MusclesRecyclerViewAdapter.ViewHolder holder, int position) {
            Muscle muscle = allMusclesList.get(position);

            holder.dynamicMuscle.setImageDrawable(context.getResources().getDrawable(muscle.getMuscleIcon()));
            holder.dynamicMuscle.setOnClickListener(holder.onMuscleClickListener);
            holder.changeBackground(inMuscleInclusionList[position][1]);
        }

        @Override
        public int getItemCount() {
            return allMusclesList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            ImageView dynamicMuscle;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                dynamicMuscle = itemView.findViewById(R.id.edit_exercise_menu_muscle_row);
            }

            public void changeBackground(Boolean inMuscleInclusion) {
                dynamicMuscle.setBackgroundColor(getResources().getColor(R.color.hardDark));
                if(inMuscleInclusion) {
                    dynamicMuscle.setBackgroundColor(getResources().getColor(R.color.mediumDark));
                }
            }

            public View.OnClickListener onMuscleClickListener = (v) -> {
                int position = getAdapterPosition();
                Muscle muscle = DB.getMuscle(allMusclesList.get(position).getMuscleId());
                inMuscleInclusionList[position][1] = !inMuscleInclusionList[position][1];
                changeBackground(inMuscleInclusionList[position][1]);
                changeList[1] = Boolean.TRUE;
                saveButton.setText(R.string.save);
            };
        }
    }
}






























