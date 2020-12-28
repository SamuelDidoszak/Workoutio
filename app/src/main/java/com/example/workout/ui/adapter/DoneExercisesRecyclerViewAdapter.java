package com.example.workout.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class DoneExercisesRecyclerViewAdapter extends RecyclerView.Adapter<DoneExercisesRecyclerViewAdapter.ViewHolder> implements View.OnTouchListener {

    private String TAG = "DoneExercisesFragment";
    private List<Done> doneList;
    private Context context;
    private MutableLiveData<Done> chosenDone;
    private boolean[] changedDoneList;
    private DatabaseHandler DB;
    private int doneId;

    private boolean showChangesMode;

    public int getDoneId() {
        return doneId;
    }

    public LiveData<Done> getChosenDone() {
        if(chosenDone == null)
            chosenDone = new MutableLiveData<>();
        return chosenDone;
    }

    /**
     * Saves all done changes if any
     */
    public void saveAllChanges() {
            //  doneList size can change. ChangedDoneList length cannot
        for(int i = 0; i < doneList.size(); i++) {
            if(changedDoneList[i] == true)
                DB.editDone(doneList.get(i));
        }
    }

    /**
     * Adds the position to the list if it doesn't already exist
     * @param position
     */
    public void addPositionToEditedList(int position) {
        changedDoneList[position] = true;
    }

    public DoneExercisesRecyclerViewAdapter(List<Done> doneList, Context context) {
        this.doneList = doneList;
        this.context = context;
        DB = new DatabaseHandler(context);
        chosenDone = new MutableLiveData<>();

        changedDoneList = new boolean[doneList.size()];
        for(int i = 0; i < doneList.size(); i++) {
            changedDoneList[i] = false;
        }
        showChangesMode = false;
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

        holder.showChangesMode(showChangesMode);

        holder.container.setOnClickListener(holder.clickHandler.onContainerClick);
        holder.container.setOnLongClickListener(holder.clickHandler.onContainerLongClick);
        holder.negative.setOnClickListener(holder.clickHandler.onNegativeClick);
        holder.canMore.setOnClickListener(holder.clickHandler.onCanMoreClick);
        holder.restore.setOnClickListener(holder.clickHandler.onRestoreClick);
        holder.delete.setOnClickListener(holder.clickHandler.onDeleteClick);
    }

    @Override
    public int getItemCount() {
        return doneList.size();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch: " + v.getId());
        Log.d(TAG, "onTouch: " + event.toString());
        return false;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView container;
        TextView exerciseName, amount, time;
        CheckableImageView negative, canMore;
        ImageButton restore, delete;
        ClickHandler clickHandler;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.done_exercises_row_containerCardView);
            exerciseName = itemView.findViewById(R.id.done_exercises_row_exerciseNameTextView);
            amount = itemView.findViewById(R.id.done_exercises_row_amountTextView);
            time = itemView.findViewById(R.id.done_exercises_row_timeTextView);
            negative = itemView.findViewById(R.id.done_exercises_row_negativeCheckbox);
            canMore = itemView.findViewById(R.id.done_exercises_row_canMoreCheckbox);
            restore = itemView.findViewById(R.id.done_exercises_row_restoreButton);
            delete = itemView.findViewById(R.id.done_exercises_row_deleteButton);

            clickHandler = new ClickHandler();
        }

        private class ClickHandler {

            View.OnClickListener onContainerClick = v -> {
                doneId = getAdapterPosition();
                chosenDone.setValue(doneList.get(getAdapterPosition()));
            };

            View.OnLongClickListener onContainerLongClick = v -> {
                showChangesMode = !showChangesMode;
                notifyDataSetChanged();
                return true;
            };

            View.OnClickListener onNegativeClick = v -> {
                doneList.get(getAdapterPosition()).setNegative(!doneList.get(getAdapterPosition()).isNegative());
                addPositionToEditedList(getAdapterPosition());
            };
            View.OnClickListener onCanMoreClick = v -> {
                doneList.get(getAdapterPosition()).setCanMore(!doneList.get(getAdapterPosition()).isCanMore());
                addPositionToEditedList(getAdapterPosition());
            };

            View.OnClickListener onRestoreClick = v -> {
                int position = getAdapterPosition();
                restore.setVisibility(View.INVISIBLE);
                changedDoneList[position] = false;
                doneList.set(position, DB.getDone(doneList.get(position).getDoneId()));
                notifyItemChanged(position);
            };
            View.OnClickListener onDeleteClick = v -> {
                int position = getAdapterPosition();
                DB.removeDone(doneList.get(position).getDoneId());
                doneList.remove(position);
                for(int i = position; i < changedDoneList.length - 1; i++) {
                    changedDoneList[i] = changedDoneList[i + 1];
                }
                notifyItemRemoved(position);
            };

        }

        private void showChangesMode(Boolean show) {
            if(show) {
                negative.setVisibility(View.GONE);
                canMore.setVisibility(View.GONE);
                delete.setVisibility(View.VISIBLE);
                if(changedDoneList[getAdapterPosition()] == true)
                    restore.setVisibility(View.VISIBLE);
                else
                    restore.setVisibility(View.INVISIBLE);
            }
            else {
                restore.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
                negative.setVisibility(View.VISIBLE);
                canMore.setVisibility(View.VISIBLE);
            }
        }
    }
}





























