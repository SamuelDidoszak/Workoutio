package com.example.workout;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Done;
import com.example.workout.model.Exercise;
import com.example.workout.model.helper.CheckableImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class AddMenuActivity extends AppCompatActivity {
    private String TAG = "AddMenuActivity";

    private TextView exerciseTextView;
    private EditText dateEditText, quantityEditText, timeEditText, hourEditText;
    private CheckableImageView negativeCheckBox, canMoreCheckBox;
    private Button addButton;
    private CardView cardView;
    private int chosenExerciseId;
    private String chosenDate;
    CopyTextWatcher timeEditTextWatcher = null, quantityEditTextWatcher = null;

    private int changesInExercises;
    private Context context;
    private DatabaseHandler DB;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_menu);
        this.setFinishOnTouchOutside(false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        context = this;
        DB = new DatabaseHandler(this);

        new SetUp().setUpAll();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            setResult(RESULT_FIRST_USER,
                    new Intent().putExtra("changesInExercises", changesInExercises));
            finish();
            return true;
        }
        return super.onTouchEvent(event);
    }

    /** Gets called when an activity finishes. <br/>
     *  Checkes which activity returned results and handles it */
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_FIRST_USER) {
            chosenExerciseId = data.getIntExtra("ExerciseId", 0);
            fillViewsBasedOnExercise();
        }
        else if(resultCode == 2) {
            chosenDate = data.getStringExtra("date");
            dateEditText.setText(chosenDate);
            new SetUp().setEditTextTint(dateEditText, getResources().getColorStateList(R.color.white));
        }
            //  user can edit an exercise and then click out of ExerciseMenuActivity. Then, local exercise from this activity won't be updated. Call to DB is necessary
        else if(exerciseTextView.getText().length() != 0)
            fillViewsBasedOnExercise();

        changesInExercises = data.getIntExtra("changesInExercises", 0);
    }

    /** Checkes if there is enough data to form a new Done. <br/>
     *  If no, then highlihts what needs to be added */
    public Boolean validateExercise() {
        Boolean validData = Boolean.TRUE;
        /**
         * Makes an ArrayList of EditTexts to parse their state and change color if necessary
         */
        List<EditText> editTextList = new ArrayList<>(
                Arrays.asList(dateEditText, quantityEditText, timeEditText, hourEditText)
        );

        for(EditText editText : editTextList) {
            if(editText.getText().toString().equals("")) {
                editText.setHintTextColor(getResources().getColor(R.color.mediumRed));
                new SetUp().setEditTextTint(editText, getResources().getColorStateList(R.color.mediumRed));
                validData = Boolean.FALSE;
            }
        }

        if(exerciseTextView.getText() == "") {
            exerciseTextView.setHintTextColor(getResources().getColor(R.color.mediumRed));
            validData = Boolean.FALSE;
        }
        return validData;
    }

    /**
     * Fills adequate EditTexts and CheckBoxes based on chosen exercise
     */
    public void fillViewsBasedOnExercise() {
        Exercise exercise = DB.getExercise(chosenExerciseId);
        exerciseTextView.setText(exercise.getExerciseName());
        negativeCheckBox.setChecked(exercise.isDefaultNegative());

        /**
         * Sets or removes auto filling text listeners based on timeIsQuantity Boolean variable of provided exercise
         */
        if(exercise.isTimeAsAmount()) {
            timeEditTextWatcher = new CopyTextWatcher(timeEditText, quantityEditText);
            quantityEditTextWatcher = new CopyTextWatcher(quantityEditText, timeEditText);
            timeEditText.addTextChangedListener(timeEditTextWatcher);
            quantityEditText.addTextChangedListener(quantityEditTextWatcher);
        }
        else {
            timeEditText.removeTextChangedListener(timeEditTextWatcher);
            quantityEditText.removeTextChangedListener(quantityEditTextWatcher);
            timeEditTextWatcher = null;
            quantityEditTextWatcher = null;
        }
    }

    /**
     * Watcher copies the content of copyFrom EditText and copies it into copyTo EditText
     */
    private class CopyTextWatcher implements TextWatcher {
        EditText copyFrom, copyTo;

        public CopyTextWatcher(EditText copyFrom, EditText copyTo) {
            this.copyFrom = copyFrom;
            this.copyTo = copyTo;
        }

        //  Prevents the method from looping
        Boolean ignore = Boolean.FALSE;

        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            if(ignore)
                return;

            ignore = Boolean.TRUE;
            copyTo.setText(s);
            //  Sets the cursor position at the end of text
            copyFrom.setSelection(copyFrom.getText().toString().length());
            ignore = Boolean.FALSE;
        }
    }

    /** Adds a new Done */
    public void addDone() {
        int quantity = Integer.valueOf(quantityEditText.getText().toString());
        int time = Integer.valueOf(timeEditText.getText().toString());

        Boolean negative = negativeCheckBox.isChecked() ? Boolean.TRUE : Boolean.FALSE;
        Boolean canMore = canMoreCheckBox.isChecked() ? Boolean.TRUE : Boolean.FALSE;
        chosenDate += " " + hourEditText.getText();
        time *= 1000;

        Done done = new Done(chosenDate, chosenExerciseId, quantity, time, negative, canMore);
        DB.addCustomDone(done);
        int id = done.getDoneId();

        Intent intent = new Intent();
        intent.putExtra("doneId", id);
        intent.putExtra("changesInExercises", changesInExercises);
        setResult(RESULT_FIRST_USER, intent);
        finish();
    }

    /**
     * Class for setting up everything necessary for AddMenuActivity to work
     */
    private class SetUp {

        /** Initializes the views */
        void addViews() {
        //  TextViews
        exerciseTextView = findViewById(R.id.add_menuExerciseNametextView);
        //  EditTexts
        dateEditText = findViewById(R.id.add_menuDateEditText);
        quantityEditText = findViewById(R.id.add_menuQuantityEditText);
        timeEditText = findViewById(R.id.add_menuTimeEditText);
        hourEditText = findViewById(R.id.add_menuHourEditText);
        //  CheckBoxes
        negativeCheckBox = findViewById(R.id.add_menuNegativeCheckBox);
        canMoreCheckBox = findViewById(R.id.add_menuCanMoreCheckBox);
        //  Button
        addButton = findViewById(R.id.add_menuAddButton);
        // CardView
        cardView = findViewById(R.id.add_menuCardView);
    }

        /** Adds onClickListeners and onFocusChangeListeners for all of the views */
        void addOnClickListeners() {
            ClickHandler clickHandler = new ClickHandler();
            //  TextView
            exerciseTextView.setOnClickListener(clickHandler.onExerciseTextViewClick);
            //  EditText Focus listeners
            dateEditText.setOnFocusChangeListener(clickHandler.onDateEditTextFocus);
            quantityEditText.setOnFocusChangeListener(clickHandler.onQuantityEditTextFocus);
            timeEditText.setOnFocusChangeListener(clickHandler.onTimeEditTextFocus);
            hourEditText.setOnFocusChangeListener(clickHandler.onHourEditTextFocus);
            //  CheckBoxes
            negativeCheckBox.setOnClickListener(clickHandler.onNegativeCheckBoxClick);
            canMoreCheckBox.setOnClickListener(clickHandler.onCanMoreCheckBoxClick);
            //  Button
            addButton.setOnClickListener(clickHandler.onAddButtonClick);
            //  CardView
            cardView.setOnClickListener(clickHandler.onCardViewClick);
        }

        void setUpAll() {
            addViews();
            addOnClickListeners();
        }

        /** Class for handling click and focus events */
        class ClickHandler {
            //  TextView
            public View.OnClickListener onExerciseTextViewClick = v -> {
                Intent addIntent = new Intent(context, ExerciseMenuActivity.class);
                startActivityForResult(addIntent, RESULT_FIRST_USER);
            };
            //  EditText focus listeners
            public View.OnFocusChangeListener onDateEditTextFocus = (v, hasFocus) -> {
                if(hasFocus) {
                    Intent addIntent = new Intent(context, CalendarActivity.class);
                    startActivityForResult(addIntent, 2);
                    if (getEditTextTint(dateEditText) != null &&
                            getEditTextTint(dateEditText).equals(getResources().getColorStateList(R.color.mediumRed))) {
                        dateEditText.setHintTextColor(getResources().getColor(R.color.white));
                        setEditTextTint(dateEditText, getResources().getColorStateList(R.color.white));
                    }
                }
                //  Makes the editView red when date isn't picked
//            else {
//                if(dateEditText.getText().toString().equals("")) {
//                    dateEditText.setHintTextColor(getResources().getColor(R.color.mediumRed));
//                    setEditTextTint(dateEditText, getResources().getColorStateList(R.color.mediumRed));
//                }
//                else
//                    setEditTextTint(dateEditText, getResources().getColorStateList(R.color.white));
//            }
                dateEditText.clearFocus();
            };
            public View.OnFocusChangeListener onQuantityEditTextFocus = (v, hasFocus) -> {
                changeEditTextColor(hasFocus, quantityEditText);
            };
            public View.OnFocusChangeListener onTimeEditTextFocus = (v, hasFocus) -> {
                changeEditTextColor(hasFocus, timeEditText);
            };
            public View.OnFocusChangeListener onHourEditTextFocus = (v, hasFocus) -> {
                if(hasFocus) {
                        //  Hide soft keyboard
                    InputMethodManager inputMethodManager = (InputMethodManager)AddMenuActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(hourEditText.getWindowToken(), 0);

                        //  Create timePickerDialog
                    TimePickerFragment timePickerFragment = new TimePickerFragment();
                    timePickerFragment.setHourEditText(hourEditText);
                    timePickerFragment.show(getSupportFragmentManager(), "timePicker");

                    changeEditTextColor(hasFocus, hourEditText);
                }
                //  Makes the editView red when date isn't picked
//            else {
//                if(hourEditText.getText().toString().equals("")) {
//                    setEditTextTint(hourEditText, getResources().getColorStateList(R.color.mediumRed));
//                }
//            }
                hourEditText.clearFocus();
            };
            //  CheckBoxes
            public View.OnClickListener onNegativeCheckBoxClick = v -> {
                Log.d(TAG, "clicked checkbox1: ");
            };
            public View.OnClickListener onCanMoreCheckBoxClick = v -> {
                Log.d(TAG, "clicked checkbox2: ");
            };
            //  Button
            public View.OnClickListener onAddButtonClick = v -> {
                if(validateExercise())
                    addDone();
            };
            //  CardView
            public View.OnClickListener onCardViewClick = v -> {
                cardView.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager)AddMenuActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(cardView.getWindowToken(), 0);
            };


            private void changeEditTextColor(Boolean hasFocus, EditText editText) {
                if(hasFocus) {
                    if (getEditTextTint(editText) != null &&
                            getEditTextTint(editText).equals(getResources().getColorStateList(R.color.mediumRed)))
                        setEditTextTint(editText, getResources().getColorStateList(R.color.white));
                }
                else {
                    if(editText.getText().toString().equals("")) {
                        setEditTextTint(editText, getResources().getColorStateList(R.color.mediumRed));
                    }
                }
            }
        }


        /** Uses appropriate method for the API version */
        public void setEditTextTint(EditText editText, ColorStateList tint) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) //&& editText instanceof AppCompatEditText
            {
                ((AppCompatEditText) editText).setSupportBackgroundTintList(tint);
            }
            else {
                editText.setBackgroundTintList(tint);
            }
        }

        /** Uses appropriate method for the API version */
        public ColorStateList getEditTextTint(EditText editText) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) //&& editText instanceof AppCompatEditText
            {
                return ((AppCompatEditText) editText).getSupportBackgroundTintList();
            }
            else {
                return editText.getBackgroundTintList();
            }
        }
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        EditText hourEditText;

        public void setHourEditText(EditText hourEditText) {
            this.hourEditText = hourEditText;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), R.style.timePicker, this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String time = (hourOfDay >= 10) ? Integer.toString(hourOfDay) : "0" + hourOfDay;
            time += ":";
            time += (minute >= 10) ? Integer.toString(minute) : "0" + minute;
            hourEditText.setText(time);
        }
    }
}































