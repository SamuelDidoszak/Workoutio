package com.example.workout.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;
import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.QuantityAndReps;
import com.example.workout.model.helper.MuscleImageAllocation;

import java.util.List;

public class WorkoutRecyclerViewAdapter extends RecyclerView.Adapter<WorkoutRecyclerViewAdapter.ViewHolder>  {

    private String TAG = "WorkoutRVA";
    private List<QuantityAndReps> quantityAndRepsList;
    public Context context;
    private DatabaseHandler DB;
    private MutableLiveData<QuantityAndReps> chosenExercise;
    private MutableLiveData<Integer> chosenExerciseQuantity;
    private WorkoutRecyclerViewAdapter thisAdapter;

    private Boolean duringRest = Boolean.FALSE;

    private int chosenExerciseIndex;
    private QuantityAndReps previousExercise;

    public void setDuringRest(Boolean duringRest) {
        this.duringRest = duringRest;
    }

    public WorkoutRecyclerViewAdapter(Context context, List<QuantityAndReps> quantityAndRepsList) {
        this.quantityAndRepsList = quantityAndRepsList;
        this.context = context;
        DB = new DatabaseHandler(context);
        this.quantityAndRepsList.addAll(quantityAndRepsList);
        chosenExercise = new MutableLiveData<>();
        thisAdapter = this;

        if(quantityAndRepsList.size() != 0) {
            previousExercise = this.quantityAndRepsList.get(0);
            chosenExerciseIndex = 0;
            this.quantityAndRepsList.remove(0);
        }
    }

    public MutableLiveData<QuantityAndReps> getChosenExercise() {
        return chosenExercise;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.workout_row, parent, Boolean.FALSE);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuantityAndReps quantityAndReps = quantityAndRepsList.get(position);

        holder.exerciseName.setText(quantityAndReps.getExerciseName());

        String quantity = Integer.toString(quantityAndReps.getQuantity());
        if(!quantity.equals("-1")) {
            if(quantityAndReps.isCanMore())
                quantity += "+";
            holder.exerciseAmount.setText(quantity);
        }
        else
            holder.exerciseAmount.setText("");

            //  Removing allocated images is necessary, otherwise images are recycled and show at inappropriate, multiple positions. It takes 0 ms on a lower end device to clear views.
        holder.muscleContainer.removeAllViews();
        new MuscleImageAllocation(DB.getMusclesByExerciseId(quantityAndReps.getExerciseId()),
                holder.muscleContainer, context)
                .allocateImages();

        holder.exerciseName.setOnClickListener(holder.onExerciseClick);
    }

    @Override
    public int getItemCount() {
        return quantityAndRepsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout muscleContainer;
        TextView exerciseName, exerciseAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            muscleContainer = itemView.findViewById(R.id.workout_row_muscleContainer);
            exerciseName = itemView.findViewById(R.id.workout_row_exerciseName);
            exerciseAmount = itemView.findViewById(R.id.workout_row_exerciseAmount);
        }

            /**
             * Exchanges the exercise from the recyclerview for the current one and vice versa
             */
        View.OnClickListener onExerciseClick = v -> {
            int position = getAdapterPosition();
                //  Handles the situation, in which user clicked an item while the recyclerview wasn't updated
            if(position == RecyclerView.NO_POSITION)
                return;

                //  if clicked during rest time, current exercise shouldn't be added again
            if(!duringRest) {
                quantityAndRepsList.add(chosenExerciseIndex, previousExercise);
                thisAdapter.notifyItemInserted(chosenExerciseIndex);

                if(position >= chosenExerciseIndex)
                    position ++;
            }
            else
                duringRest = Boolean.FALSE;

            QuantityAndReps quantityAndReps = quantityAndRepsList.get(position);
            chosenExercise.setValue(quantityAndReps);
            quantityAndRepsList.remove(position);
            thisAdapter.notifyItemRemoved(position);

            chosenExerciseIndex = position;
            previousExercise = quantityAndReps;
        };
    }

    /**
     * Adds the first exercise to become the new one
     */
    public void exerciseFinished() {
        QuantityAndReps quantityAndReps = quantityAndRepsList.get(0);
        chosenExercise.setValue(quantityAndReps);
        quantityAndRepsList.remove(0);
        thisAdapter.notifyItemRemoved(0);

        chosenExerciseIndex = 0;
        previousExercise = quantityAndReps;
    }
}
























