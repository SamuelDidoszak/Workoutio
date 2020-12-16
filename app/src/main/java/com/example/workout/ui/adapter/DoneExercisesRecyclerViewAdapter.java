package com.example.workout.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;
import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Done;
import com.example.workout.model.helper.CheckableImageView;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DoneExercisesRecyclerViewAdapter extends RecyclerView.Adapter<DoneExercisesRecyclerViewAdapter.ViewHolder> {

    private String TAG = "DoneExercisesFragment";
    private List<Done> doneList;
    private Context context;
    private MutableLiveData<Done> chosenDone;
    private MutableLiveData<Boolean> timePicker;
    private DatabaseHandler DB;

    public LiveData<Done> getChosenDone() {
        return chosenDone;
    }

    /**
     * @return true if picker should be for time. <br/> false if picker should be for amount
     */
    public LiveData<Boolean> isTimePicker() {
        return timePicker;
    }

    public DoneExercisesRecyclerViewAdapter(List<Done> doneList, Context context) {
        this.doneList = doneList;
        this.context = context;
        DB = new DatabaseHandler(context);
        chosenDone = new MutableLiveData<>();
        timePicker = new MutableLiveData<>();
        timePicker.setValue(Boolean.FALSE);
    }

    @NonNull
    @Override
    public DoneExercisesRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.done_exercises_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoneExercisesRecyclerViewAdapter.ViewHolder holder, int position) {
        Done done = doneList.get(position);

        holder.exerciseName.setText(DB.getExercise(done.getExerciseId()).getExerciseName());
        holder.amount.setText(Integer.toString(done.getQuantity()));

        int timeMillis = done.getTime();
        String time = "";
        if(timeMillis / 1000 <= 180)
            time = timeMillis / 1000 + "s";
        else
        {
            if(TimeUnit.MILLISECONDS.toHours(timeMillis) >= 1)
                time = String.format("%02d:", TimeUnit.MILLISECONDS.toHours(timeMillis));
            time += String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(timeMillis) % 60,
                    TimeUnit.MILLISECONDS.toSeconds(timeMillis) % 60);
        }
        holder.time.setText(time);

        holder.negative.setChecked(done.isNegative());
        holder.canMore.setChecked(done.isCanMore());

        holder.container.setOnClickListener(holder.clickHandler.onContainerClick);
        holder.amount.setOnClickListener(holder.clickHandler.onAmountClick);
        holder.time.setOnClickListener(holder.clickHandler.onTimeClick);
        holder.negative.setOnClickListener(holder.clickHandler.onNegativeClick);
        holder.canMore.setOnClickListener(holder.clickHandler.onCanMoreClick);
    }

    @Override
    public int getItemCount() {
        return doneList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView container;
        TextView exerciseName, amount, time;
        CheckableImageView negative, canMore;
        ClickHandler clickHandler;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.done_exercises_row_containerCardView);
            exerciseName = itemView.findViewById(R.id.done_exercises_row_exerciseNameTextView);
            amount = itemView.findViewById(R.id.done_exercises_row_amountTextView);
            time = itemView.findViewById(R.id.done_exercises_row_timeTextView);
            negative = itemView.findViewById(R.id.done_exercises_row_negativeCheckbox);
            canMore = itemView.findViewById(R.id.done_exercises_row_canMoreCheckbox);

            clickHandler = new ClickHandler();
        }

        private class ClickHandler {

            View.OnClickListener onContainerClick = v -> {
                Log.d(TAG, "container: " + getAdapterPosition());
                chosenDone.setValue(doneList.get(getAdapterPosition()));
            };

            View.OnClickListener onAmountClick = v -> {
                Log.d(TAG, "amount: " + getAdapterPosition());
                timePicker.setValue(Boolean.FALSE);
            };

            View.OnClickListener onTimeClick = v -> {
                Log.d(TAG, "time: " + getAdapterPosition());
                timePicker.setValue(Boolean.TRUE);
            };

            View.OnClickListener onNegativeClick = v -> {
            };
            View.OnClickListener onCanMoreClick = v -> {
            };

        }
    }
}





























