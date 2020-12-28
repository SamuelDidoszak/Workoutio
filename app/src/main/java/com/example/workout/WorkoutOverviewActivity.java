package com.example.workout;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import com.example.workout.fragment.DoneExercisesFragment;
import com.example.workout.fragment.EditExerciseFragment;
import com.example.workout.model.QuantityAndReps;

import java.util.List;

public class WorkoutOverviewActivity extends AppCompatActivity {

    private String TAG = "ExerciseOverviewActivity";

    private FragmentContainerView primaryFragmentContainer, secondaryFragmentContainer;
    private DoneExercisesFragment doneExercisesFragment;
    private EditExerciseFragment editExerciseFragment;
    private Button continueButton, saveButton;
    private TextView repetitionsTextView, timeTextView;
    private List<QuantityAndReps> remainingQuantityAndReps;


    private String overallExerciseTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_overview_activity);

        overallExerciseTime = getIntent().getStringExtra("overallTime");


        setUpViews();
        setUpFragments();
        setOnClickListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(doneExercisesFragment.getChosenDone() != null) {
            doneExercisesFragment.getChosenDone().observe(this, done -> {
                doneExercisesFragment.getChosenDone();
                int id = doneExercisesFragment.getDoneId();
                setUpSecondaryFragment(id);
                editExerciseFragment.setExerciseData(done);
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(secondaryFragmentContainer.getVisibility() == View.VISIBLE) {
                secondaryFragmentContainer.setVisibility(View.GONE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 7f);
                primaryFragmentContainer.setLayoutParams(params);
            }
        }
        return true;
    }

    private void setUpViews() {
        primaryFragmentContainer = findViewById(R.id.workout_overview_activity_primaryFragmentContainer);
        secondaryFragmentContainer = findViewById(R.id.workout_overview_activity_secondaryFragmentContainer);
        continueButton = findViewById(R.id.workout_overview_activity_continueButton);
        saveButton = findViewById(R.id.workout_overview_activity_saveButton);
        repetitionsTextView = findViewById(R.id.workout_overview_activity_repetitionsTextView);
        timeTextView = findViewById(R.id.workout_overview_activity_timeTextView);

        timeTextView.setText(overallExerciseTime);
    }

    private void setUpFragments() {
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).add(R.id.workout_overview_activity_primaryFragmentContainer, DoneExercisesFragment.class, null, "doneExercisesFragment").commit();
        getSupportFragmentManager().executePendingTransactions();
        doneExercisesFragment = (DoneExercisesFragment)getSupportFragmentManager().findFragmentByTag("doneExercisesFragment");

        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).add(R.id.workout_overview_activity_secondaryFragmentContainer, EditExerciseFragment.class, null, "editExerciseFragment").commit();
        getSupportFragmentManager().executePendingTransactions();
        editExerciseFragment = (EditExerciseFragment)getSupportFragmentManager().findFragmentByTag("editExerciseFragment");
    }

    private void setUpSecondaryFragment(int id) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 5f);
        primaryFragmentContainer.setLayoutParams(params);
        secondaryFragmentContainer.setVisibility(View.VISIBLE);
        editExerciseFragment.resetLiveDatas();


        if(editExerciseFragment.isAdded()) {
            editExerciseFragment.getExerciseAmount().observe(this, number -> {
                doneExercisesFragment.notifyItemChanged(id, number, null);
            });
            editExerciseFragment.getExerciseTime().observe(this, number -> {
                doneExercisesFragment.notifyItemChanged(id, null, number);
            });
        }
    }

    private void setOnClickListeners() {
        saveButton.setOnClickListener(v -> {
            doneExercisesFragment.getDoneExercisesRecyclerViewAdapter().saveAllChanges();
            setResult(2);
            finish();
        });
        continueButton.setOnClickListener(v -> {
            Toast.makeText(this, "continue your workout", Toast.LENGTH_SHORT);
            finish();
            Intent intent = new Intent(getApplicationContext(), WorkoutActivity.class);
            intent.putExtra("quantityAndReps", getIntent().getSerializableExtra("remainingQuantityAndReps"));
            intent.putExtra("overallTimeBase", getIntent().getLongExtra("overallTimeBase", 0));
            startActivity(intent);
        });
    }
}



































