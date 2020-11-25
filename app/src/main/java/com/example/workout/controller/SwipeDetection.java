package com.example.workout.controller;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class SwipeDetection extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {

    private String swipeDirection;
    private Context context;
    private GestureDetector detector;

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(Math.abs(velocityX) > Math.abs(velocityY))
        {
            if(e1.getX() > e2.getX())
                swipeDirection = "left";
            else
                swipeDirection = "right";
        }
        return true;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        swipeDirection = "none";
        Log.d("TOUCH", "onSingleTapUp " + getGesture());
        return true;
    }

    public String getGesture() {
        return swipeDirection;
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

    public SwipeDetection(Context context)
    {
        this(context, null);
    }

    public SwipeDetection(Context context, GestureDetector gestureDetector)
    {
        if(gestureDetector == null)
            gestureDetector = new GestureDetector(context, this);
        this.context = context;
        this.detector = gestureDetector;
    }
}