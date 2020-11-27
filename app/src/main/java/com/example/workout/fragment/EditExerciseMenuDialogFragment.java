package com.example.workout.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;
import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Day;
import com.example.workout.model.Exercise;
import com.example.workout.model.Muscle;
import com.example.workout.model.helper.CheckableImageView;
import com.example.workout.model.helper.MuscleImageAllocation;

import java.util.List;

public class EditExerciseMenuDialogFragment extends DialogFragment {

    private TextView exerciseNameTextView;
    private RecyclerView daysRecyclerView;
    private LinearLayout musclesLinearLayout;
    private CheckableImageView timeAsCountCheckbox, defaultNegativeCheckbox;

    private int exerciseId;

    private Context context;
    private DatabaseHandler DB;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        exerciseId = getArguments().getInt("exerciseId");

        Log.d("TAG", "onCreateView: breh3");
        return new AlertDialog.Builder(requireContext(), R.style.emptyPopupFullScreen).create();
    }

    /**
     * Method to pass arguments into the fragment. Call when creating the object.
     * @param exerciseId exercise which to show
     * @return a new fragment with exerciseId passed in a bundle
     */
    public static EditExerciseMenuDialogFragment newInstance(int exerciseId) {
        EditExerciseMenuDialogFragment fragment = new EditExerciseMenuDialogFragment();

        Log.d("TAG", "onCreateView: breh 1");

        Bundle bundle = new Bundle();
        bundle.putInt("exerciseId", exerciseId);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.edit_exercise_menu, null, false);

        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.emptyPopupFullScreen);
        LayoutInflater tempInflater = inflater.cloneInContext(contextThemeWrapper);

        Log.d("TAG", "onCreateView: breh2");

        View view = inflater.inflate(R.layout.edit_exercise_menu, null, true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("TAG", "onViewCreated: created view?");

        this.context = getActivity();
        DB = new DatabaseHandler(context);

        setViews();
        fillViews(exerciseId);
    }

    private void setViews() {
        exerciseNameTextView = getView().findViewById(R.id.edit_exercise_menu_exerciseNameTextView);
        daysRecyclerView = getView().findViewById(R.id.edit_exercise_menu_daysRecyclerView);
        musclesLinearLayout = getView().findViewById(R.id.edit_exercise_menu_musclesLinearLayout);
        timeAsCountCheckbox = getView().findViewById(R.id.edit_exercise_menu_timeAsCountCheckbox);
        defaultNegativeCheckbox = getView().findViewById(R.id.edit_exercise_menu_defaultNegativeCheckbox);
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

    private class DaysRecyclerViewAdapter extends RecyclerView.Adapter<EditExerciseMenuDialogFragment.DaysRecyclerViewAdapter.ViewHolder> {

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
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
/*
    private class MusclesRecyclerViewAdapter extends RecyclerView.Adapter<com.example.workout.EditExerciseMenuDialogFragment.MusclesRecyclerViewAdapter.ViewHolder> {

        List<Muscle> muscleList;

        public MusclesRecyclerViewAdapter(List<Muscle> muscleList) {
            this.muscleList = muscleList;
        }

        @NonNull
        @Override
        public com.example.workout.EditExerciseMenuDialogFragment.MusclesRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.edit_exercise_menu_muscle_row, parent, false);

            return new com.example.workout.EditExerciseMenuDialogFragment.MusclesRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull com.example.workout.EditExerciseMenuDialogFragment.MusclesRecyclerViewAdapter.ViewHolder holder, int position) {
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
    */

}

