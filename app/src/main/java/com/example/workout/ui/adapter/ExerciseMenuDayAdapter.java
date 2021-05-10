package com.example.workout.ui.adapter;

import android.app.ActionBar;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;
import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Muscle;
import com.example.workout.model.helper.DayExercise;
import com.example.workout.model.helper.ExerciseMenuDayExerciseTypes;
import com.example.workout.model.helper.ExerciseMenuRecyclerViewData;
import com.example.workout.model.helper.ExerciseMenuRecyclerViewTypes;
import com.example.workout.model.helper.MuscleImageAllocation;

import java.util.ArrayList;
import java.util.List;

public class ExerciseMenuDayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ExerciseMenuRecyclerViewTypes, ExerciseMenuRecyclerViewData {

    private Context context;
    private List<DayExercise> dayExerciseList;
    private MutableLiveData<Integer> chosenExercise;
    private MutableLiveData<Integer> chosenDay;
    private MutableLiveData<Integer> exerciseToEdit;
    private List<Boolean> exerciseVisibilityList;
    private List<List<Muscle>> muscleListForImages;



    private String TAG = "DayAdapter";
    private int chosenPosition;
    DatabaseHandler DB;

    /** If true, shows all exercises at the start. <br/>
     * If false, recyclerView shows only days until clicked */
    private Boolean showAllDataAtStart = Boolean.TRUE;

    public MutableLiveData<Integer> getChosenExercise() {
        return chosenExercise;
    }
    public MutableLiveData<Integer> getExerciseToEdit() {
        return exerciseToEdit;
    }
    public LiveData<Integer> getChosenDay() {
        return chosenDay;
    }
    public void resetExerciseToEdit() {
        this.exerciseToEdit = new MutableLiveData<>();
    }
    public void resetChosenExercise() {
        this.chosenExercise = new MutableLiveData<>();
    }
    public void resetChosenDay() {
        this.chosenDay = new MutableLiveData<>();
    }

    public ExerciseMenuDayAdapter(Context context, List<DayExercise> dayExerciseList) {
        this.context = context;
        this.dayExerciseList = dayExerciseList;
        chosenExercise = new MutableLiveData<>();
        exerciseToEdit = new MutableLiveData<>();
        chosenDay = new MutableLiveData<>();
        exerciseVisibilityList = new ArrayList<>();
        muscleListForImages = new ArrayList<>();

            //  Creates exerciseVisibilityList and muscleListForImages
        DB = new DatabaseHandler(context);
        for(DayExercise dayExercise : dayExerciseList) {
            exerciseVisibilityList.add(showAllDataAtStart);
            if(dayExercise.getDataType() == ExerciseMenuDayExerciseTypes.TYPE_EXERCISE)
                muscleListForImages.add(DB.getMusclesByExerciseId(dayExercise.getTypeId()));
            else
                muscleListForImages.add(new ArrayList<>());
        }
    }

    public void reSetMuscleListForImagesAtPosition(int position) {
        muscleListForImages.set(position, DB.getMusclesByExerciseId(dayExerciseList.get(position).getTypeId()));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == ExerciseMenuDayExerciseTypes.TYPE_DAY) {
            view = LayoutInflater.from(context).inflate(R.layout.row_exercise_menu_day, parent, false);
            return new DayViewHolder(view);
        }
        else {
            view = LayoutInflater.from(context).inflate(R.layout.row_exercise_menu_exercise, parent, false);
            return new ExerciseViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DayExercise dayExercise = dayExerciseList.get(position);
        if(getItemViewType(position) == ExerciseMenuDayExerciseTypes.TYPE_DAY)
            ((DayViewHolder)holder).setItems(dayExercise);
        else {
            ((ExerciseViewHolder)holder).setItems(dayExercise);
            ((ExerciseViewHolder)holder).showOrHideExercise(position);
            ((ExerciseViewHolder)holder).refreshMuscleIcons();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return dayExerciseList.get(position).getDataType();
    }

    @Override
    public int getItemCount() {
        return dayExerciseList.size();
    }

    @Override
    public int getChosenPosition() {
        return chosenPosition;
    }

    public class DayViewHolder extends RecyclerView.ViewHolder {
        private TextView dayNameTextView;
        private ImageButton addImageButton;
        DayExercise dayExercise;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayNameTextView = itemView.findViewById(R.id.exercise_menu_row_day_dayNameTextView);
            addImageButton = itemView.findViewById(R.id.exercise_menu_row_exercise_addImageButton);
        }

        public void setItems(DayExercise dayExercise) {
            ClickHandler clickHandler = new ClickHandler();
            this.dayExercise = dayExercise;
            dayNameTextView.setText(dayExercise.getName());
            dayNameTextView.setOnClickListener(clickHandler.onDayNameTextViewClick);
            addImageButton.setOnClickListener(clickHandler.onAddImageButtonClick);
        }

        /**Warning: Has to be called after checking if position is not the last position
         * @param position index of the day
         * @return number of exercises assigned to a day
         */
        public int exerciseAmountInDay(int position) {
//            if(position == dayExerciseList.size() - 1)
//                return 0;
            int n = 0;
            position++;
            while(position < getItemCount() && dayExerciseList.get(position).getDataType() == ExerciseMenuDayExerciseTypes.TYPE_EXERCISE &&
                    dayExerciseList.get(position) != null) {
                n++;
                position++;
            }
            return n;
        }

        class ClickHandler {
            View.OnClickListener onDayNameTextViewClick = v -> {
                int position = getAdapterPosition();
                if(position == dayExerciseList.size() - 1 ||
                        dayExerciseList.get(position + 1).getDataType() != ExerciseMenuDayExerciseTypes.TYPE_EXERCISE)
                    return;
                Boolean currentVisibility = exerciseVisibilityList.get(position + 1);
                int exerciseAmount = exerciseAmountInDay(position);
                for(int i = position + 1; i < position + exerciseAmount + 1; i++) {
                    exerciseVisibilityList.set(i, !currentVisibility);
                }
                notifyItemRangeChanged(position + 1, exerciseAmount);
            };
            View.OnClickListener onAddImageButtonClick = v -> {
                chosenDay.setValue(dayExercise.getTypeId());
            };
        }
    }

    public class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView exerciseNameTextView;
        private LinearLayout muscleIconContainer;
        private ImageButton editImageButton;
        private LinearLayout itemLinearLayout;
        DayExercise dayExercise;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseNameTextView = itemView.findViewById(R.id.exercise_menu_row_exercise_exerciseNameTextView);
            muscleIconContainer = itemView.findViewById(R.id.exercise_menu_row_exercise_muscleIconContainer);
            editImageButton = itemView.findViewById(R.id.exercise_menu_row_exercise_editImageButton);
            itemLinearLayout = itemView.findViewById(R.id.exercise_menu_row_exercise_itemLinearLayout);

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(exerciseNameTextView, 12, 18, 6, TypedValue.COMPLEX_UNIT_SP);
        }

        public void setItems(DayExercise dayExercise) {
            ClickHandler clickHandler = new ClickHandler();
            this.dayExercise = dayExercise;
            exerciseNameTextView.setText(dayExercise.getName());
            exerciseNameTextView.setOnClickListener(clickHandler.onExerciseNameTextViewClick);
            editImageButton.setOnClickListener(clickHandler.onEditImageButtonViewClick);
        }

        /** Checks if the exercise should be shown and acts accordingly */
        public void showOrHideExercise(int position) {
                //  Resets visibility
            if (showAllDataAtStart) {
                itemLinearLayout.setVisibility(View.VISIBLE);
                itemLinearLayout.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
            }
            else {
                itemLinearLayout.setVisibility(View.GONE);
                itemLinearLayout.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            }

            if(exerciseVisibilityList.get(position)) {
                itemLinearLayout.setVisibility(View.VISIBLE);
                itemLinearLayout.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
            }
            else {
                itemLinearLayout.setVisibility(View.GONE);
                itemLinearLayout.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            }
        }

        /** Refreshes muscle icons for this view */
        public void refreshMuscleIcons() {
            //  Removing allocated images is necessary, otherwise images are recycled and show at inappropriate, multiple positions. It takes 0 ms on a lower end device to clear views.
            muscleIconContainer.removeAllViews();
            new MuscleImageAllocation(muscleListForImages.get(getAdapterPosition()),
                    muscleIconContainer, context)
                    .allocateImages();
        }

        public class ClickHandler {
            View.OnClickListener onExerciseNameTextViewClick = v -> {
                int id = dayExercise.getTypeId();
                chosenExercise.setValue(id);
            };
            View.OnClickListener onMuscleIconContainerViewClick = v -> {

            };
            View.OnClickListener onEditImageButtonViewClick = v -> {
                chosenPosition = getAdapterPosition();
                int id = dayExerciseList.get(getAdapterPosition()).getTypeId();
                exerciseToEdit.setValue(id);
            };
        }
    }
}





























