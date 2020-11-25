package com.example.workout.ui.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.model.Muscle;
import com.example.workout.R;

import java.util.List;

public class DayMuscleRecyclerViewAdapter extends RecyclerView.Adapter<DayMuscleRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<Muscle> muscleList;

    public DayMuscleRecyclerViewAdapter(Context context, List<Muscle> muscleList) {
        this.context = context;
        this.muscleList = muscleList;
    }

    @NonNull
    @Override
    public DayMuscleRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.day_row_muscle, parent, false);

        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Muscle muscle = muscleList.get(position);

        holder.muscleNameTextView.setText(muscle.getMuscleName());
        holder.muscleIconImageView.setImageDrawable(context.getResources().getDrawable(muscle.getMuscleIcon()));
    }

    @Override
    public int getItemCount() {
        return muscleList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView muscleIconImageView;
        AppCompatTextView muscleNameTextView;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);

            muscleIconImageView = itemView.findViewById(R.id.day_row_muscle_muscleIconImageView);
            muscleNameTextView = itemView.findViewById(R.id.day_row_muscle_muscleNameTextView);

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(muscleNameTextView, 16, 24, 1, TypedValue.COMPLEX_UNIT_SP);
        }
    }
}