package com.example.workout;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Day;
import com.example.workout.model.Exercise;
import com.example.workout.model.Muscle;
import com.example.workout.model.helper.CheckableImageView;
import com.example.workout.model.helper.MuscleImageAllocation;

import java.util.List;

public class EditExerciseMenuActivity extends AppCompatActivity {

    private TextView exerciseNameTextView;
    private RecyclerView daysRecyclerView;
    private LinearLayout musclesLinearLayout;
    private CheckableImageView timeAsCountCheckbox, defaultNegativeCheckbox;

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
        fillViews(exerciseId);
    }

    private void setViews() {
        exerciseNameTextView = findViewById(R.id.edit_exercise_menu_exerciseNameTextView);
        daysRecyclerView = findViewById(R.id.edit_exercise_menu_daysRecyclerView);
        musclesLinearLayout = findViewById(R.id.edit_exercise_menu_musclesLinearLayout);
        timeAsCountCheckbox = findViewById(R.id.edit_exercise_menu_timeAsCountCheckbox);
        defaultNegativeCheckbox = findViewById(R.id.edit_exercise_menu_defaultNegativeCheckbox);
    }

    private void fillViews(int exerciseId) {
        Exercise exercise = DB.getExercise(exerciseId);
        List<Day> dayList = DB.getDaysByExerciseId(exerciseId);
        List<Muscle> muscleList = DB.getMusclesByExerciseId(exerciseId);

        exerciseNameTextView.setText(exercise.getExerciseName());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0, 1);

        new MuscleImageAllocation(muscleList, musclesLinearLayout, context).allocateImages();

        for(int i = 0; i < musclesLinearLayout.getChildCount(); i ++) {
            musclesLinearLayout.getChildAt(i).setLayoutParams(params);
        }



        DaysRecyclerViewAdapter daysRecyclerViewAdapter = new DaysRecyclerViewAdapter(dayList);

        daysRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        daysRecyclerView.setAdapter(daysRecyclerViewAdapter);
    }

    private class DaysRecyclerViewAdapter extends RecyclerView.Adapter<DaysRecyclerViewAdapter.ViewHolder> {

        List<Day> dayList;

        public DaysRecyclerViewAdapter(List<Day> dayList) {
            this.dayList = dayList;
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
            Day day = dayList.get(position);

            holder.dayNameTextView.setText(day.getDayName());
        }

        @Override
        public int getItemCount() {
            return dayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            TextView dayNameTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                dayNameTextView = itemView.findViewById(R.id.edit_exercise_menu_day_row_dayNameTextView);
            }
        }
    }

    private class MusclesRecyclerViewAdapter extends RecyclerView.Adapter<MusclesRecyclerViewAdapter.ViewHolder> {

        List<Muscle> muscleList;

        public MusclesRecyclerViewAdapter(List<Muscle> muscleList) {
            this.muscleList = muscleList;
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
            Muscle muscle = muscleList.get(position);

            holder.muscleCardView.removeAllViews();


        }

        @Override
        public int getItemCount() {
            return muscleList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            CardView muscleCardView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                muscleCardView = itemView.findViewById(R.id.edit_exercise_menu_muscle_row_muscleCardView);
            }
        }
    }
}






























