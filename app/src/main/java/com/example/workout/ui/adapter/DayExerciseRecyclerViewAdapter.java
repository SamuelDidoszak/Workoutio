package com.example.workout.ui.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workout.model.QuantityAndReps;
import com.example.workout.R;

import java.util.List;

public class DayExerciseRecyclerViewAdapter extends RecyclerView.Adapter<DayExerciseRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<QuantityAndReps> quantityAndRepsList;

    public DayExerciseRecyclerViewAdapter(Context context, List<QuantityAndReps> quantityAndRepsList) {
        this.context = context;
        this.quantityAndRepsList = quantityAndRepsList;
    }

    @NonNull
    @Override
    public DayExerciseRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.day_row_exercise, parent, false);

        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuantityAndReps quantityAndReps = quantityAndRepsList.get(position);

        String quantityAndCanMore = quantityAndReps.getQuantity() + "";
            //  exercise is new and quantity is not yet known
        if(quantityAndCanMore.equals("-1"))
            quantityAndCanMore = "";
        else
            quantityAndCanMore += quantityAndReps.isCanMore() ? " +" : "";

        holder.exerciseNameTextView.setText(quantityAndReps.getExerciseName());
        holder.amountTextView.setText(quantityAndCanMore);
        holder.repsTextView.setText(String.valueOf(quantityAndReps.getReps()));
    }

    @Override
    public int getItemCount() {
        return quantityAndRepsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView exerciseNameTextView;
        TextView amountTextView, repsTextView;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);

            exerciseNameTextView = itemView.findViewById(R.id.day_row_exercise_exerciseNameTextView);
            amountTextView = itemView.findViewById(R.id.day_row_exercise_quantityTextView);
            repsTextView = itemView.findViewById(R.id.day_row_exercise_repsTextView);

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(exerciseNameTextView, 12, 18, 1, TypedValue.COMPLEX_UNIT_SP);
        }
    }
}





































