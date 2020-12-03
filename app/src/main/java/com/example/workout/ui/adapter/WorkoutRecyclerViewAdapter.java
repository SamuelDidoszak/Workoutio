package com.example.workout.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;
import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.QuantityAndReps;

import java.util.List;

public class WorkoutRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private List<QuantityAndReps> quantityAndRepsList;
    Context context;
    private DatabaseHandler DB;

    public WorkoutRecyclerViewAdapter(Context context, List<QuantityAndReps> quantityAndRepsList) {
        this.quantityAndRepsList = quantityAndRepsList;
        this.context = context;
        DB = new DatabaseHandler(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.workout_row, parent, Boolean.FALSE);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
