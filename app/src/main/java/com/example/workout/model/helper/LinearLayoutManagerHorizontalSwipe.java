package com.example.workout.model.helper;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.interfaces.HorizontalSwipe;

public class LinearLayoutManagerHorizontalSwipe extends LinearLayoutManager implements HorizontalSwipe {

    private MutableLiveData<Boolean> horizontalSwipe;

    /**
     * @return false if swipe is to the left, true if it's to the right
     */
    public LiveData<Boolean> isSwipeHorizontal() {
        horizontalSwipe = new MutableLiveData<>();
        return horizontalSwipe;
    }

    public LinearLayoutManagerHorizontalSwipe(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        horizontalSwipe = new MutableLiveData<>();
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (dx <= -70)
            horizontalSwipe.setValue(Boolean.FALSE);
        else if (dx >= 70)
            horizontalSwipe.setValue(Boolean.TRUE);
        return super.scrollHorizontallyBy(dx, recycler, state);
    }
}
