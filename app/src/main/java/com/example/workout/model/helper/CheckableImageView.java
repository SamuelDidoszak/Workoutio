package com.example.workout.model.helper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;

public class CheckableImageView extends androidx.appcompat.widget.AppCompatImageView implements Checkable {

    public CheckableImageView(Context context) {
        super(context);
    }

    public CheckableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean mChecked = false;

    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

    @Override
    public int[] onCreateDrawableState(final int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked())
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        return drawableState;
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public void setOnClickListener(final OnClickListener l) {
        View.OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
                l.onClick(v);
            }
        };
        super.setOnClickListener(onClickListener);
    }
}