package com.example.workout.controller;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SwipeDetection extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {

    private MutableLiveData<String> swipeDirection;
    private Context context;
    private GestureDetector detector;

    public LiveData<String> getSwipeDirection() {
        return swipeDirection;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(Math.abs(velocityX) > Math.abs(velocityY))
        {
            if(e1.getX() > e2.getX())
                swipeDirection.setValue("left");
            else
                swipeDirection.setValue("right");
        }
        else {
            if(e1.getY() > e2.getY())
                swipeDirection.setValue("up");
            else
                swipeDirection.setValue("down");
        }
        return true;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        swipeDirection.setValue("tap");
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        detector.onTouchEvent(event);
        return true;
    }

    public GestureDetector getDetector()
    {
        return detector;
    }

    public SwipeDetection(Context context) {
        this(context, null);
    }

    public SwipeDetection(Context context, GestureDetector gestureDetector) {
        if(gestureDetector == null)
            gestureDetector = new GestureDetector(context, this);
        this.context = context;
        this.detector = gestureDetector;
        swipeDirection = new MutableLiveData<>();

    }
}