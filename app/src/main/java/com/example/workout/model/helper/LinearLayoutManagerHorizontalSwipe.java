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

    private int swipeDistanceArray[];
    private long timeStampArray[];
    private int currentPtr;
    private long currentTimeMillis;

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
        swipeDistanceArray = new int[50];
        timeStampArray = new long[50];
        currentPtr = 0;
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        currentTimeMillis = System.currentTimeMillis();

        if(isVelocityEnough(dx))
            horizontalSwipe.setValue(Boolean.TRUE);
        return super.scrollHorizontallyBy(dx, recycler, state);
    }

    private boolean isVelocityEnough(int dx) {
        if(currentPtr == 0 || currentTimeMillis > timeStampArray[currentPtr - 1] + 350 || currentPtr == 49) {
            for (int i = 1; i <= currentPtr; i++) {
                swipeDistanceArray[i] = 0;
                timeStampArray[i] = 0;
            }
            swipeDistanceArray[0] = dx;
            timeStampArray[0] = currentTimeMillis;
            currentPtr = 1;
        }
        else {
            swipeDistanceArray[currentPtr] = dx;
            timeStampArray[currentPtr] = currentTimeMillis;
            currentPtr++;
        }
        int dxTotal = 0;
        for(int i = 0; i < currentPtr; i++) {
            dxTotal += swipeDistanceArray[i];
        }
        long t = timeStampArray[currentPtr - 1] - timeStampArray[0];
        if(t > 60 && dxTotal > 150 || dxTotal < -150)
            return true;
        else
            return false;
    }
}
