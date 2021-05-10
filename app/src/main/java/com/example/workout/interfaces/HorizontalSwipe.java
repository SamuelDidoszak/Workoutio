package com.example.workout.interfaces;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public interface HorizontalSwipe {

    MutableLiveData<Boolean> horizontalSwipe = new MutableLiveData<>();
    /**
     * @return false if swipe is to the left, true if it's to the right
     */
    LiveData<Boolean> isSwipeHorizontal();
}
