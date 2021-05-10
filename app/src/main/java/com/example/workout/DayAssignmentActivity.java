package com.example.workout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.fragment.ExerciseMenuFragment;
import com.example.workout.model.Day;
import com.example.workout.model.DayExerciseConnector;
import com.example.workout.model.Exercise;
import com.example.workout.model.QuantityAndReps;
import com.example.workout.model.helper.SimpleItemTouchHelperCallback;
import com.example.workout.ui.adapter.DayAssignmentRecyclerViewAdapter;
import com.google.android.material.textfield.TextInputLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DayAssignmentActivity extends AppCompatActivity implements DayAssignmentRecyclerViewAdapter.OnStartDragListener {
    private TextView dayName;
    private TextInputLayout dayNameEditText;
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
    boolean startWorkout;
    String initialName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_assignment);

        context = getApplicationContext();
        DB = new DatabaseHandler(this);

        dayId = getIntent().getIntExtra("dayId", -1);
        startWorkout = getIntent().getBooleanExtra("startWorkout", false);

        addViews();
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(dayAssignmentRecyclerViewAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(dayExercisesRecyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        exerciseMenuFragment.getPickedExercise().observe(DayAssignmentActivity.this, integer -> {
            Log.d("TAG", "onStart: " + DB.getExercise(integer).getExerciseName());
            int position = dayExercises.size();
            dayExercises.add(DB.getExercise(integer));
            dayAssignmentRecyclerViewAdapter.notifyItemInserted(position);
            DayAssignmentActivity.this.runOnUiThread(() -> {
                dayAssignmentRecyclerViewAdapter.notifyItemInserted(position);
                dayAssignmentRecyclerViewAdapter.reSetListOfMuscleListsAtPosition(position);
            });
            dayAssignmentRecyclerViewAdapter.getDataChanged().setValue(Boolean.TRUE);
            dayExercisesRecyclerView.scrollToPosition(position);
        });

        if(dayId == -1) {
            FragmentContainerView fragmentContainerView = findViewById(R.id.day_assignment_exerciseFragmentContainer);
            fragmentContainerView.post(() -> {
                exerciseMenuFragment.getExerciseRecyclerView().setOnTouchListener(new ClickHandler().hideKeyboardOnTouch);
                exerciseMenuFragment.getExerciseRecyclerView().addOnItemTouchListener(new ClickHandler().hideKeyboardOnItemTouch);
            });
        }
    }

    private void addViews() {
        //  TextViews
        dayName = findViewById(R.id.day_assignment_dayName);
        //  EditText
        dayNameEditText = findViewById(R.id.day_assignment_dayNameEditText);
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
        if(dayId == -1 || DB.getDay(dayId).isCustom()) {
            //  Changes the constraints in the constraint layout
            dayName.setVisibility(View.GONE);
            dayNameEditText.setVisibility(View.VISIBLE);
            ConstraintLayout constraintLayout = findViewById(R.id.day_assignment_constraintLayout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.day_assignment_dayExercisesRecyclerView, ConstraintSet.TOP,
                    R.id.day_assignment_dayNameEditText, ConstraintSet.BOTTOM,0);
            constraintSet.applyTo(constraintLayout);
            initialName = "";
            if(dayId != -1) {
                initialName = DB.getDay(dayId).getDayName();
                dayNameEditText.getEditText().setText(initialName);
            }

            dayNameEditText.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(!startWorkout && s.toString().length() != 0)
                        dayNameEditText.setError(null);
                    if(dayId == -1 || !s.toString().equals(initialName)) {
                        if(startWorkout) {
                            String buttonText = "";
                            if(s.length() != 0)
                                buttonText = "save & ";
                            buttonText += "start";
                            saveButton.setText(buttonText);
                        }
                    }
                    if(s.length() != 0) {
                        int semicolonPos = s.toString().indexOf(';');
                        if(semicolonPos != -1) {
                            s = s.toString().substring(0, semicolonPos) + (semicolonPos != s.length() - 1 ? s.toString().substring(semicolonPos + 1, s.length()) : "");
                            dayNameEditText.getEditText().setText(s);
                            dayNameEditText.getEditText().setSelection(semicolonPos);
                        }
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
        if(dayId != -1) {
            if(!DB.getDay(dayId).isCustom())
                dayName.setText(DB.getDay(dayId).getDayName());
            dayExerciseConnectorList = DB.getDayExerciseConnectorByDay(dayId);
            for(DayExerciseConnector dayExerciseConnector : dayExerciseConnectorList) {
                dayExercises.add(DB.getExercise(dayExerciseConnector.getExerciseId()));
            }
        }

        if(startWorkout) {
            String buttonText = "";
            if(dayId != -1)
                buttonText = "save & ";
            buttonText += "start";
            saveButton.setText(buttonText);
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
        saveButton.setOnClickListener(clickHandler.save);
        spaceTop.setOnClickListener(clickHandler.onSpaceClick);
        spaceBottom.setOnClickListener(clickHandler.onSpaceClick);

        dayExercisesRecyclerView.setOnTouchListener(clickHandler.hideKeyboardOnTouch);
        dayExercisesRecyclerView.addOnItemTouchListener(clickHandler.hideKeyboardOnItemTouch);
    }

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

        View.OnClickListener save = v -> {
            Intent intent = new Intent();
            if(startWorkout) {
                List<Exercise> exerciseList = dayAssignmentRecyclerViewAdapter.getExercisesList();
                if(exerciseList.size() != 0) {
                    List<QuantityAndReps> quantityAndRepsList = new ArrayList<>();
                    for(Exercise exercise : exerciseList) {
                        quantityAndRepsList.add(new QuantityAndReps(exercise.getExerciseId(), context, null));
                    }
                    intent.putExtra("quantityAndReps", (Serializable)quantityAndRepsList);
                }
                else {
                    Toast.makeText(context, "Please choose some exercises", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            else if((dayNameEditText.getVisibility() == View.VISIBLE && dayNameEditText.getEditText().getText().length() == 0)) {
                dayNameEditText.setError(" ");
                return;
            }

            boolean newDay = false, saved = false;
            if(startWorkout || dayNameEditText.getEditText().getText().length() != 0) {
                Day day = new Day(dayNameEditText.getEditText().getText().toString(), true);
                if(dayId == -1) {
                    DB.addDay(day);
                    dayId = day.getDayId();
                    dayAssignmentRecyclerViewAdapter.setDayId(dayId);
                    newDay = true;
                }
                else {
                    day.setDayId(dayId);
                    DB.editDay(day);
                    saved = true;
                }
            }
            intent.putExtra("newDay", newDay);
            if(dayAssignmentRecyclerViewAdapter.saveChanges())
                saved = true;
            if(!startWorkout) {
                if(!saved)
                    intent.putExtra("changeInDay", 0);
                else {
                    if (DB.getDay(dayId).isCustom())
                        intent.putExtra("changeInDay", 2);
                    else
                        intent.putExtra("changeInDay", 1);
                }
            }
            setResult(RESULT_FIRST_USER, intent);
            finish();
        };

        View.OnClickListener onSpaceClick = v -> {
            setResult(RESULT_CANCELED);
            finish();
        };
        
        View.OnTouchListener hideKeyboardOnTouch = (v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                dayExercisesRecyclerView.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) DayAssignmentActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(dayExercisesRecyclerView.getWindowToken(), 0);
            }
            return false;
        };

        RecyclerView.OnItemTouchListener hideKeyboardOnItemTouch = new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                dayExercisesRecyclerView.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager)DayAssignmentActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(dayExercisesRecyclerView.getWindowToken(), 0);
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        };
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }
}
































