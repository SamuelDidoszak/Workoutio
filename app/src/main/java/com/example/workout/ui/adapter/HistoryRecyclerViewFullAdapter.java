package com.example.workout.ui.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;
import com.example.workout.data.DatabaseHandler;
import com.example.workout.interfaces.HistoryRowTypeInterface;
import com.example.workout.model.Done;
import com.example.workout.model.helper.MuscleDateTime;
import com.example.workout.model.helper.MuscleImageAllocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HistoryRecyclerViewFullAdapter extends RecyclerView.Adapter<HistoryRecyclerViewFullAdapter.ViewHolder> implements HistoryRowTypeInterface {
    private Context context;
    private List<Done> doneList;
    private List<MuscleDateTime> muscleDateTimeList;
    private List<Boolean> subListVisibleList;
    DatabaseHandler DB;
    String TAG = "HistoryRecyclerViewFullAdapter";

    public HistoryRecyclerViewFullAdapter(Context context, List<Done> doneList, List<MuscleDateTime> muscleDateTimeList) {
        this.context = context;
        this.doneList = doneList;
        this.muscleDateTimeList = muscleDateTimeList;
        DB = new DatabaseHandler(context);

        subListVisibleList = new ArrayList<>();
        for(int i = 0; i < muscleDateTimeList.size(); i++) {
            subListVisibleList.add(Boolean.FALSE);
        }
    }

    public void addToSubListVisibleList(int index) {
        subListVisibleList.add(index, Boolean.FALSE);
    }

    @NonNull
    @Override
    public HistoryRecyclerViewFullAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_row_muscle, parent, false);

        return new HistoryRecyclerViewFullAdapter.ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryRecyclerViewFullAdapter.ViewHolder holder, int position) {
        MuscleDateTime muscleDateTime = muscleDateTimeList.get(position);
        String[] dateAndDayName = muscleDateTime.getTrimmedDateAndDayNameDivided();

        holder.historyRowDay.setText(dateAndDayName[0]);
        holder.historyRowDate.setText(dateAndDayName[1]);
        holder.historyRowTime.setText(muscleDateTime.getTime());

            //  Removing allocated images is necessary, otherwise images are recycled and show at inappropriate, multiple positions. It takes 0 ms on a lower end device to clear views.
        holder.muscleIconContainer.removeAllViews();

        new MuscleImageAllocation(muscleDateTime.getMuscleList(),
                holder.muscleIconContainer, context)
                .allocateImages();

        holder.showOrHideExerciseDetails(position);
    }



    @Override
    public int getItemCount() {
        return muscleDateTimeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView historyRowDay, historyRowDate, historyRowTime;
        RecyclerView exerciseDetailsRecyclerView;
        Context context;
        LinearLayout muscleIconContainer;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            historyRowDay = itemView.findViewById(R.id.history_row_day);
            historyRowDate = itemView.findViewById(R.id.history_row_dat);
            historyRowTime = itemView.findViewById(R.id.history_row_time);
            muscleIconContainer = itemView.findViewById(R.id.history_row_muscle_container);
            exerciseDetailsRecyclerView = itemView.findViewById(R.id.history_row_details_recyclerView);

            itemView.setOnClickListener(this::onClick);
        }

        /** Resets the visibility of an adapter as well as its content. <br/>
         * Method is necessary for correct behavior, otherwise content is recycled and shows in multiple rows <br/>
         * Somehow, this method takes 0-2ms on a lower end device */
        public void showOrHideExerciseDetails(int position) {
//            if(exerciseDetailsRecyclerView.getAdapter() != null)
//                exerciseDetailsRecyclerView.getAdapter().notifyDataSetChanged();
//            exerciseDetailsRecyclerView.removeAllViews();
//            exerciseDetailsRecyclerView.removeAllViewsInLayout();
//            exerciseDetailsRecyclerView.swapAdapter(null, Boolean.TRUE);
//            exerciseDetailsRecyclerView.setAdapter(null);
            exerciseDetailsRecyclerView.setVisibility(View.GONE);

            if(subListVisibleList.get(position))
                createAndSetExerciseDetailsRVA(position);
//            else
//                exerciseDetailsRecyclerView.setAdapter(null);
        }

        /** Resets the exerciseDetailsRecyclerViewAdapter and its content */
        public void createAndSetExerciseDetailsRVA(int position) {
            exerciseDetailsRecyclerView.removeAllViews();
            exerciseDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(context));

            exerciseDetailsRecyclerView.setHasFixedSize(true);

            List<Done> subDoneList;
            if (position != 0) {
                subDoneList = doneList.subList(
                        muscleDateTimeList.get(position - 1).getSortedDoneListEndIndex(),
                        muscleDateTimeList.get(position).getSortedDoneListEndIndex());
            }
            else
                subDoneList = doneList.subList(0, muscleDateTimeList.get(0).getSortedDoneListEndIndex());

            ExerciseDetailsRecyclerViewAdapter exerciseDetailsRecyclerViewAdapter = new ExerciseDetailsRecyclerViewAdapter(subDoneList, position);

            exerciseDetailsRecyclerView.setAdapter(exerciseDetailsRecyclerViewAdapter);

            exerciseDetailsRecyclerView.setVisibility(View.VISIBLE);
            subListVisibleList.set(position, Boolean.TRUE);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            if(!subListVisibleList.get(position)) {
                createAndSetExerciseDetailsRVA(position);
            }
            else {
                exerciseDetailsRecyclerView.setVisibility(View.GONE);
                subListVisibleList.set(position, Boolean.FALSE);
            }
        }
    }

    public class ExerciseDetailsRecyclerViewAdapter extends RecyclerView.Adapter<ExerciseDetailsRecyclerViewAdapter.ViewHolder> {
        List<Done> subDoneList;
        int positiono;

        public ExerciseDetailsRecyclerViewAdapter(List<Done> subDoneList, int positiono) {
            this.subDoneList = subDoneList;
            this.positiono = positiono;
        }

        @NonNull
        @Override
        public ExerciseDetailsRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_row_exercise_details, parent, false);
            return new ExerciseDetailsRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ExerciseDetailsRecyclerViewAdapter.ViewHolder holder, int position) {
        Done done = subDoneList.get(position);
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

        holder.exerciseDetailsExerciseNameTextView.setText(DB.getExercise(done.getExerciseId()).getExerciseName());
        holder.exerciseDetailsQuantityTextView.setText(String.valueOf(done.getQuantity()));
        holder.exerciseDetailsTimeTextView.setText(time);
        }

        @Override
        public int getItemCount() {
            return subDoneList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView exerciseDetailsQuantityTextView, exerciseDetailsTimeTextView;
            AppCompatTextView exerciseDetailsExerciseNameTextView;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                exerciseDetailsQuantityTextView = itemView.findViewById(R.id.history_row_exercise_details_quantityTextView);
                exerciseDetailsTimeTextView = itemView.findViewById(R.id.history_row_exercise_details_timeTextView);
                exerciseDetailsExerciseNameTextView = itemView.findViewById(R.id.history_row_exercise_details_exerciseNameTextView);

                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(exerciseDetailsExerciseNameTextView, 12, 18, 1, TypedValue.COMPLEX_UNIT_SP);
            }
        }
    }
}

