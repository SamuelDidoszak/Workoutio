package com.example.workout.model.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.workout.R;
import com.example.workout.model.Muscle;

import java.util.List;

public class MuscleImageAllocation {
    List<Muscle> muscleList;
    LinearLayout container;
    Context context;

    public MuscleImageAllocation(List<Muscle> muscleList, LinearLayout container, Context context) {
        this.muscleList = muscleList;
        this.container = container;
        this.context = context;
    }

    public MuscleImageAllocation(LinearLayout container, Context context) {
        this.container = container;
        this.context = context;
    }

    public void setMuscleList(List<Muscle> muscleList) {
        this.muscleList = muscleList;
    }

    public void allocateImages() {

        for(int i = 0; i < muscleList.size(); i++) {
            Muscle muscle = muscleList.get(i);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            View muscleIcon = inflater.inflate(R.layout.dynamic_muscle, container, false);
            ((ImageView)muscleIcon).setImageDrawable(context.getResources().getDrawable(muscle.getMuscleIcon()));
            container.addView(muscleIcon);
        }
    }
}
