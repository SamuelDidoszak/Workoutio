package com.example.workout.model.helper;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.DecimalFormat;

public class Chronometer extends androidx.appcompat.widget.AppCompatTextView {
    @SuppressWarnings("unused")

    public interface OnChronometerTickListener {
        void onChronometerTick(Chronometer chronometer);
    }

    private long mBase;
    private boolean mVisible;
    private boolean mStarted;
    private boolean mRunning;
    private OnChronometerTickListener mOnChronometerTickListener;
    private static final int TICK_WHAT = 2;
    private long timeElapsed;

    private boolean backwards = Boolean.FALSE;
    private int timeStop;
    private MutableLiveData<Boolean> timeFinished;

    private int delayMillis = 10;

    /**
     * Set frequency with which the chronometer will be updated. <br/>
     * 1000 = one second <br/>
     * 100 = 100 milliseconds <br/>
     * 10 = 10 milliseconds <br/>
     * 1 = 1 millisecond
     * @param delayMillis cannot be 0 or less
     */
    public void setDelayMillis(int delayMillis) {
        if(delayMillis > 0)
            this.delayMillis = delayMillis;
        else
            throw new IllegalArgumentException("Argument is 0 or less");

    }

    public int getTimeStop() {
        return timeStop;
    }

    public void setTimeStop(int timeStop) {
        this.timeStop = timeStop;
    }

    public void removeTimeStop() {
        this.timeStop = Integer.MAX_VALUE;
    }

    public Chronometer(Context context) {
        this (context, null, 0);
    }

    public Chronometer(Context context, AttributeSet attrs) {
        this (context, attrs, 0);
    }

    public Chronometer(Context context, AttributeSet attrs, int defStyle) {
        super (context, attrs, defStyle);
        timeStop = Integer.MAX_VALUE;
        init();
    }

    /**
     * Starts the chronometer set to count time backwards. Starts at 0 seconds.
     */
    public void startBackwards() {
        backwards = Boolean.TRUE;
        long base = SystemClock.elapsedRealtime();
        setBase(base);
    }

    /**
     * Starts the chronometer set to count time backwards.
     * @param secondsFrom amount of seconds to start counting from
     */
    public void startBackwards(long secondsFrom) {
        backwards = Boolean.TRUE;
        long base = SystemClock.elapsedRealtime() + secondsFrom * 1000;
        setBase(base);
    }

    public boolean isBackwards() {
        return backwards;
    }

    public void setBackwards(boolean backwards) {
        this.backwards = backwards;
    }

    /**
     * Starts observing for finish of the countdown <br/>
     * Useless without calling startBackwards method first
     * @return MutableLiveData to be observed
     */
    public LiveData<Boolean> observeIfTimeFinished(int timeStop) {
        timeFinished = new MutableLiveData<>();
        this.timeStop = timeStop;

        return timeFinished;
    }

    /**
     * Initializes a new base for the chronometer. <br/>
     * Sets the backwards value to false
     */
    public void init() {
        backwards = false;
        mBase = SystemClock.elapsedRealtime();
        updateText(mBase);
    }

    public void setBase(long base) {
        mBase = base;
        dispatchChronometerTick();
        updateText(SystemClock.elapsedRealtime());
    }

    /**
     * Sets the base as the current time + modifier <br/>
     * Can be used to continue the previous time after stopping, by passing the previous elapsedTime * (-1) as a parameter.
     * @param modifier changes the current base time. Can be negative
     */
    public void setBaseWithCurrentTime(long modifier) {
        mBase = SystemClock.elapsedRealtime() + modifier;
        dispatchChronometerTick();
        updateText(SystemClock.elapsedRealtime());
    }

    public long getBase() {
        return mBase;
    }

    public void setOnChronometerTickListener(
            OnChronometerTickListener listener) {
        mOnChronometerTickListener = listener;
    }

    public OnChronometerTickListener getOnChronometerTickListener() {
        return mOnChronometerTickListener;
    }

    public void start() {
        mStarted = true;
        updateRunning();
    }

    public void stop() {
        mStarted = false;
        updateRunning();
    }

    public Boolean isStarted() {
        return mStarted;
    }


    public void setStarted(boolean started) {
        mStarted = started;
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow() {
        super .onDetachedFromWindow();
        mVisible = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super .onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    /**
     * Method updates text of chronometer widget. Its look can be modified by changing the divider value and spannableText value
     */
    private synchronized void updateText(long now) {
        String minus = "";
        if(backwards) {
            timeElapsed = mBase - now;
            //  runs if the timeFinish observer was set
            if(timeStop != Integer.MAX_VALUE) {
                if(timeElapsed <= timeStop * 1000)
                    timeFinished.setValue(Boolean.TRUE);
            }
            //  a special case when milliseconds are not showing. Chronometer shows one second more to get more intuitive results
            if(delayMillis == 1000) {
                timeElapsed += 1000;
                if(timeElapsed / 10 < 0) {
                    timeElapsed -= 1000;
                    minus = "-";
                    timeElapsed = Math.abs(timeElapsed);
                }
            }
            else if(timeElapsed / delayMillis < 0) {
                timeElapsed -= 1000;
                minus = "-";
                timeElapsed = Math.abs(timeElapsed);
            }
        }
        else {
            timeElapsed = now - mBase;
            if (timeStop != Integer.MAX_VALUE) {
                if (timeElapsed / 1000 >= timeStop)
                    timeFinished.setValue(Boolean.TRUE);
            }
        }

        DecimalFormat tripleZeros = new DecimalFormat("000");
        DecimalFormat doubleZeros = new DecimalFormat("00");
        DecimalFormat singleZero = new DecimalFormat("0");

        int hours = (int)(timeElapsed / (3600 * 1000));
        int remaining = (int)(timeElapsed % (3600 * 1000));

        int minutes = remaining / (60 * 1000);
        remaining = remaining % (60 * 1000);

        int seconds = remaining / 1000;

        int milliseconds = ((int)timeElapsed % 1000) / 10;

        String text = "";
        String divider = " ";

        if(hours > 0) {
            text += doubleZeros.format(hours) + divider;
        }
        //  delete if and else if you want minutes to always show two digits
        if(minutes >= 10)
            text += doubleZeros.format(minutes) + divider;
        else
            text += singleZero.format(minutes) + divider;
        text += doubleZeros.format(seconds);

        //  Different formatting based on the millisecondDelay
        int millisLength = 0;
        switch (delayMillis) {
            case 1000:
                break;
            case 100:
                text += divider + milliseconds / 10;
                millisLength = 1;
                break;
            case 10:
                text += divider + doubleZeros.format(milliseconds);
                millisLength = 2;
                break;
            case 1:
                text += divider + tripleZeros.format(milliseconds);
                millisLength = 3;
                break;
        }

        //  makes the millisecond divider smaller
        if(delayMillis != 1000 && !divider.equals(" "))
            millisLength++;

        //  Makes the output text's milliseconds smaller
        SpannableString spannableText = new SpannableString(minus + text);
        spannableText.setSpan(new RelativeSizeSpan(0.5f), spannableText.length() - millisLength, spannableText.length(), 0);

        setText(spannableText);
    }

    private void updateRunning() {
        boolean running = mVisible && mStarted;
        if (running != mRunning) {
            if (running) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                mHandler.sendMessageDelayed(Message.obtain(mHandler,
                        TICK_WHAT), delayMillis);
            } else {
                mHandler.removeMessages(TICK_WHAT);
            }
            mRunning = running;
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if (mRunning) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                sendMessageDelayed(Message.obtain(this , TICK_WHAT),
                        delayMillis);
            }
        }
    };

    void dispatchChronometerTick() {
        if (mOnChronometerTickListener != null) {
            mOnChronometerTickListener.onChronometerTick(this);
        }
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

}