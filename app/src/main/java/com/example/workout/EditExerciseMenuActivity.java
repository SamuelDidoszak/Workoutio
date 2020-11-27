package com.example.workout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Day;
import com.example.workout.model.Exercise;
import com.example.workout.model.Muscle;
import com.example.workout.model.helper.CheckableImageView;
import com.example.workout.model.helper.MuscleImageAllocation;

import java.util.ArrayList;
import java.util.List;

public class EditExerciseMenuActivity extends AppCompatActivity {

    private TextView exerciseNameTextView;
    private RecyclerView daysRecyclerView, musclesRecyclerView;
    private CheckableImageView timeAsCountCheckbox, defaultNegativeCheckbox;

    private List<Day> allDaysList, dayList;
    private List<Muscle> allMusclesList, muscleList;
    private List<Boolean> inDayInclusionList, inMuscleInclusionList;

    int exerciseId;
    private Context context;
    private DatabaseHandler DB;
    private Boolean madeChanges = Boolean.FALSE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_exercise_menu);

        exerciseId = getIntent().getIntExtra("exerciseId", -1);
        this.context = getApplicationContext();
        DB = new DatabaseHandler(context);

        setViews();
        fillViews(exerciseId);
    }

    private void setViews() {
        exerciseNameTextView = findViewById(R.id.edit_exercise_menu_exerciseNameTextView);
        daysRecyclerView = findViewById(R.id.edit_exercise_menu_daysRecyclerView);
        musclesRecyclerView = findViewById(R.id.edit_exercise_menu_musclesRecyclerView);
        timeAsCountCheckbox = findViewById(R.id.edit_exercise_menu_timeAsCountCheckbox);
        defaultNegativeCheckbox = findViewById(R.id.edit_exercise_menu_defaultNegativeCheckbox);
    }

    private void fillViews(int exerciseId) {
        assignItems();
        Exercise exercise = DB.getExercise(exerciseId);

        exerciseNameTextView.setText(exercise.getExerciseName());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0, 1);

        DaysRecyclerViewAdapter daysRecyclerViewAdapter = new DaysRecyclerViewAdapter();

        daysRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        daysRecyclerView.setAdapter(daysRecyclerViewAdapter);

        MusclesRecyclerViewAdapter musclesRecyclerViewAdapter = new MusclesRecyclerViewAdapter();

        musclesRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        musclesRecyclerView.setAdapter(musclesRecyclerViewAdapter);

    }

    private void assignItems() {
        allDaysList = DB.getAllDays();
        allMusclesList = DB.getAllMuscles();

        dayList = DB.getDaysByExerciseId(exerciseId);
        muscleList = DB.getMusclesByExerciseId(exerciseId);

        inMuscleInclusionList = new ArrayList<>();
        inDayInclusionList = new ArrayList<>();

            //  Creates a list of booleans which are necessary to show which elements are related to this exercise and which are not
        Boolean added;
        for(Day day : allDaysList) {
            added = Boolean.FALSE;
            for(Day inDay : dayList) {
                if(day.getDayId() == inDay.getDayId()) {
                    inDayInclusionList.add(Boolean.TRUE);
                    added = Boolean.TRUE;
                    break;
                }
            }
            if(!added)
                inDayInclusionList.add(Boolean.FALSE);
        }

        for(Muscle muscle : allMusclesList) {
            added = Boolean.FALSE;
            for(Muscle inMuscle : muscleList) {
                if(muscle.getMuscleId() == inMuscle.getMuscleId()) {
                    inMuscleInclusionList.add(Boolean.TRUE);
                    added = Boolean.TRUE;
                    break;
                }
            }
            if(!added)
                inMuscleInclusionList.add(Boolean.FALSE);
        }
    }

    private class DaysRecyclerViewAdapter extends RecyclerView.Adapter<DaysRecyclerViewAdapter.ViewHolder> {

        public DaysRecyclerViewAdapter() {
        }

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
            holder.changeDayBackground(inDayInclusionList.get(position));
        }

        @Override
        public int getItemCount() {
            return allDaysList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            TextView dayNameTextView;
            
            public void changeDayBackground(Boolean inDayInclusion) {
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
                inDayInclusionList.set(getAdapterPosition(), !inDayInclusionList.get(position));
                changeDayBackground(inDayInclusionList.get(position));
                madeChanges = Boolean.TRUE;
            };
        }
    }

    private class MusclesRecyclerViewAdapter extends RecyclerView.Adapter<MusclesRecyclerViewAdapter.ViewHolder> {

        private int currentMuscleId = 0;

        public MusclesRecyclerViewAdapter() {

        }

        @NonNull
        @Override
        public MusclesRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.edit_exercise_menu_muscle_row, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MusclesRecyclerViewAdapter.ViewHolder holder, int position) {

            for(int i = 0; i < 3; i++) {
                new MuscleImageAllocation(holder.muscleContainer, context)
                        .allocateSingleImage(allMusclesList.get(currentMuscleId));

                holder.muscleContainer.getChildAt(i).setOnClickListener(holder.onMuscleClickListener);

                currentMuscleId++;
            }
        }

        @Override
        public int getItemCount() {
            return (int)Math.ceil(allMusclesList.size() / 3);
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            LinearLayout muscleContainer;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                muscleContainer = itemView.findViewById(R.id.edit_exercise_menu_muscle_row_muscleContainer);
            }
                //  I don't see a way to find which child of 3 was clicked
            public View.OnClickListener onMuscleClickListener = (v) -> {
                int id = getAdapterPosition() + muscleContainer.getChildCount();
                Log.d("TAG", "Clicked " + v.getId());
            };

        }
    }
}






























