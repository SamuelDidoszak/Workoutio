package com.example.workout.ui.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;
import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Exercise;
import com.example.workout.model.Muscle;
import com.example.workout.model.helper.MuscleImageAllocation;

import java.util.ArrayList;
import java.util.List;

public class ExerciseMenuExercisesRecyclerViewAdapter extends RecyclerView.Adapter<ExerciseMenuExercisesRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<Exercise> exercisesList;
    private MutableLiveData<Integer> chosenExercise;
    private List<List<Muscle>> listOfMuscleLists;

    private DatabaseHandler DB;

    public MutableLiveData<Integer> getChosenExercise() {
        return chosenExercise;
    }

    public ExerciseMenuExercisesRecyclerViewAdapter(Context context, List<Exercise> exercisesList) {
        this.context = context;
        this.exercisesList = exercisesList;
        chosenExercise = new MutableLiveData<>();

        DB = new DatabaseHandler(context);

            //  Creates a list of muscleLists. It is best to communicate with the database once at a creation, because when it's done in bindViewHolder, it's overally more resource heavy
        listOfMuscleLists = new ArrayList<>();
        for(Exercise exercise : exercisesList) {
            listOfMuscleLists.add(DB.getMusclesByExerciseId(exercise.getExerciseId()));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exercise_menu_row_exercise, parent, false);

        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exercisesList.get(position);

        holder.exerciseNameTextView.setText(exercise.getExerciseName());

        holder.exerciseNameTextView.setOnClickListener(holder.clickHandler.onExerciseNameTextViewClick);
        holder.muscleIconContainer.setOnClickListener(holder.clickHandler.onMuscleIconContainerClick);
        holder.editImageButton.setOnClickListener(holder.clickHandler.onEditImageButtonClick);

            //  Removing allocated images is necessary, otherwise images are recycled and show at inappropriate, multiple positions. It takes 0 ms on a lower end device to clear views.
        holder.muscleIconContainer.removeAllViews();
        new MuscleImageAllocation(listOfMuscleLists.get(position),
                holder.muscleIconContainer, context)
                .allocateImages();
    }

    @Override
    public int getItemCount() {
        return exercisesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Context context;

        AppCompatTextView exerciseNameTextView;
        LinearLayout muscleIconContainer;
        ImageButton editImageButton;

        ClickHandler clickHandler;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;

            exerciseNameTextView = itemView.findViewById(R.id.exercise_menu_row_exercise_exerciseNameTextView);
            muscleIconContainer = itemView.findViewById(R.id.exercise_menu_row_exercise_muscleIconContainer);
            editImageButton = itemView.findViewById(R.id.exercise_menu_row_exercise_editImageButton);
            muscleIconContainer = itemView.findViewById(R.id.exercise_menu_row_exercise_muscleIconContainer);

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(exerciseNameTextView, 12, 18, 1, TypedValue.COMPLEX_UNIT_SP);

            clickHandler = new ClickHandler();
        }

        public class ClickHandler {

            /** Returns the chosen exercise to the parent activity */
            View.OnClickListener onExerciseNameTextViewClick = v -> {
                int id = exercisesList.get(getAdapterPosition()).getExerciseId();
                chosenExercise.setValue(id);
            };
            View.OnClickListener onMuscleIconContainerClick = v -> {

            };
            View.OnClickListener onEditImageButtonClick = v -> {
                Toast.makeText(context, "Edit " + exercisesList.get(getAdapterPosition()).getExerciseName(), Toast.LENGTH_SHORT).show();
            };
        }

    }
}





















