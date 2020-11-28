package com.example.workout;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Day;
import com.example.workout.model.Exercise;
import com.example.workout.model.Muscle;
import com.example.workout.model.helper.CheckableImageView;

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

        musclesRecyclerView.setLayoutManager(new GridLayoutManager(context, 3, RecyclerView.VERTICAL, Boolean.FALSE));
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
            holder.changeBackground(inDayInclusionList.get(position));
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
                inDayInclusionList.set(getAdapterPosition(), !inDayInclusionList.get(position));
                changeBackground(inDayInclusionList.get(position));
                madeChanges = Boolean.TRUE;
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
            holder.changeBackground(inMuscleInclusionList.get(position));
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
                inMuscleInclusionList.set(position, !inMuscleInclusionList.get(position));
                changeBackground(inMuscleInclusionList.get(position));
                madeChanges = Boolean.TRUE;
            };

        }
    }
}






























