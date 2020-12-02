package com.example.workout.model.helper;

import androidx.lifecycle.MutableLiveData;

public interface ExerciseMenuRecyclerViewData {
    int chosenPosition = 0;
    public int getChosenPosition();

    public MutableLiveData<Integer> getChosenExercise();
    public MutableLiveData<Integer> getExerciseToEdit();
    public void resetExerciseToEdit();
}
