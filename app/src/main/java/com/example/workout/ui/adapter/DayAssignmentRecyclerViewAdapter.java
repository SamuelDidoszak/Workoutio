package com.example.workout.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;
import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Exercise;
import com.example.workout.model.Muscle;
import com.example.workout.model.helper.ExerciseMenuRecyclerViewData;
import com.example.workout.model.helper.ItemTouchHelperAdapter;
import com.example.workout.model.helper.MuscleImageAllocation;

import java.util.ArrayList;
import java.util.List;

public class DayAssignmentRecyclerViewAdapter extends RecyclerView.Adapter<DayAssignmentRecyclerViewAdapter.ViewHolder> implements ExerciseMenuRecyclerViewData, ItemTouchHelperAdapter {

    public interface OnStartDragListener {

        /**
         * Called when a view is requesting a start of a drag.
         * @param viewHolder The holder of the view to drag.
         */
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private Context context;
    private List<Exercise> exercisesList;
    private MutableLiveData<Integer> chosenExercise;
    private MutableLiveData<Integer> exerciseToEdit;
    private List<List<Muscle>> listOfMuscleLists;

    private DatabaseHandler DB;

    private boolean editMode;
    private int chosenPosition;
    public MutableLiveData<Integer> getChosenExercise() {
        return chosenExercise;
    }
    public MutableLiveData<Integer> getExerciseToEdit() {
        return exerciseToEdit;
    }
    public void resetExerciseToEdit() {
        this.exerciseToEdit = new MutableLiveData<>();
    }
    private final OnStartDragListener dragStartListener;

    public DayAssignmentRecyclerViewAdapter(Context context, List<Exercise> exercisesList, OnStartDragListener dragStartListener) {
        this.context = context;
        this.exercisesList = exercisesList;
        this.dragStartListener = dragStartListener;
        chosenExercise = new MutableLiveData<>();
        exerciseToEdit = new MutableLiveData<>();
        editMode = false;

        DB = new DatabaseHandler(context);

        //  Creates a list of muscleLists. It is best to communicate with the database once at a creation, because when it's done in bindViewHolder, it's overally more resource heavy
        listOfMuscleLists = new ArrayList<>();
        for(Exercise exercise : exercisesList) {
            listOfMuscleLists.add(DB.getMusclesByExerciseId(exercise.getExerciseId()));
        }
    }

    public int getChosenPosition() {
        return chosenPosition;
    }

    public void reSetListOfMuscleListsAtPosition(int position) {
        listOfMuscleLists.set(position, DB.getMusclesByExerciseId(exercisesList.get(position).getExerciseId()));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.day_assignment_row, parent, false);

        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exercisesList.get(position);

        holder.exerciseNameTextView.setText(exercise.getExerciseName());

        holder.exerciseNameTextView.setOnClickListener(holder.clickHandler.onExerciseNameTextViewClick);
        holder.muscleIconContainer.setOnClickListener(holder.clickHandler.onMuscleIconContainerClick);
        holder.editImageButton.setOnClickListener(holder.clickHandler.onEditImageButtonClick);

        holder.exerciseNameTextView.setOnLongClickListener(holder.clickHandler.onContainerLongClick);
        holder.muscleIconContainer.setOnLongClickListener(holder.clickHandler.onContainerLongClick);
        holder.editImageButton.setOnLongClickListener(holder.clickHandler.onContainerLongClick);

        //  Removing allocated images is necessary, otherwise images are recycled and show at inappropriate, multiple positions. It takes 0 ms on a lower end device to clear views.
        holder.muscleIconContainer.removeAllViews();
        new MuscleImageAllocation(listOfMuscleLists.get(position),
                holder.muscleIconContainer, context)
                .allocateImages();

        if(editMode) {
            holder.changeMode();
            holder.reorderImageButton.setOnTouchListener((v, event) -> {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    dragStartListener.onStartDrag(holder);
                }
                return false;
            });
        }
        else
            holder.changeMode();
    }

    @Override
    public int getItemCount() {
        return exercisesList.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Exercise previousExercise = exercisesList.remove(fromPosition);
        exercisesList.add(toPosition > fromPosition ? toPosition - 1 : toPosition, previousExercise);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        exercisesList.remove(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Context context;

        AppCompatTextView exerciseNameTextView;
        LinearLayout muscleIconContainer;
        ImageButton editImageButton, reorderImageButton;
        CardView container;

        ClickHandler clickHandler;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;

            exerciseNameTextView = itemView.findViewById(R.id.day_assignment_row_exerciseNameTextView);
            muscleIconContainer = itemView.findViewById(R.id.day_assignment_row_muscleIconContainer);
            editImageButton = itemView.findViewById(R.id.day_assignment_row_editImageButton);
            muscleIconContainer = itemView.findViewById(R.id.day_assignment_row_muscleIconContainer);
            reorderImageButton = itemView.findViewById(R.id.day_assignment_row_reorderImageButton);
            container = itemView.findViewById(R.id.day_assignment_row_container);

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(exerciseNameTextView, 12, 18, 1, TypedValue.COMPLEX_UNIT_SP);

            clickHandler = new ClickHandler();
        }

        public class ClickHandler {

            View.OnLongClickListener onContainerLongClick = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d("TAG", "longClick!!!!!!!111: ");
                    editMode = !editMode;
                    notifyDataSetChanged();
                    return false;
                }
            };

            /** Returns the chosen exercise to the parent activity */
            View.OnClickListener onExerciseNameTextViewClick = v -> {
                int id = exercisesList.get(getAdapterPosition()).getExerciseId();
                chosenExercise.setValue(id);
            };
            View.OnClickListener onMuscleIconContainerClick = v -> {

            };
            /**
             *  Returns the exercise to edit to the parent activity
             */
            View.OnClickListener onEditImageButtonClick = v -> {
                int id = exercisesList.get(getAdapterPosition()).getExerciseId();
                chosenPosition = getAdapterPosition();
                exerciseToEdit.setValue(id);
            };
        }

        private void changeMode() {
            if(editMode) {
                editImageButton.setVisibility(View.GONE);
                reorderImageButton.setVisibility(View.VISIBLE);
            }
            else {
                reorderImageButton.setVisibility(View.GONE);
                editImageButton.setVisibility(View.VISIBLE);
            }
        }

    }
}





















