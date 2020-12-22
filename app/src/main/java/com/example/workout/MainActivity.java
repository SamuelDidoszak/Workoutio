package com.example.workout;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Done;
import com.example.workout.model.Exercise;
import com.example.workout.model.Muscle;
import com.example.workout.model.QuantityAndReps;
import com.example.workout.model.helper.LinearLayoutManagerHorizontalSwipe;
import com.example.workout.model.helper.MuscleDateTime;
import com.example.workout.ui.adapter.DayExerciseRecyclerViewAdapter;
import com.example.workout.ui.adapter.DayMuscleRecyclerViewAdapter;
import com.example.workout.ui.adapter.DoneExercisesRecyclerViewAdapter;
import com.example.workout.ui.adapter.HistoryRecyclerViewFullAdapter;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";

    private Context context;
    private DatabaseHandler DB;

    private List<Done> doneList;
    private List<Done> sortedDoneList;

    private List<QuantityAndReps> quantityAndRepsList;
    private List<Muscle> dayMuscleList;
    private List<MuscleDateTime> muscleDateTimeList;

    private RecyclerView dayRecyclerView;
    private DayExerciseRecyclerViewAdapter dayExerciseRecyclerViewAdapter;
    private DayMuscleRecyclerViewAdapter dayMuscleRecyclerViewAdapter;
    private DoneExercisesRecyclerViewAdapter doneExercisesRecyclerViewAdapter;
    private RecyclerView historyRecyclerView;
    private HistoryRecyclerViewFullAdapter historyRecyclerViewFullAdapter;
    private boolean workoutIsDone;

    private ImageButton addButton, accountButton;

    private String dateOfDayExercisesShowing;

        //  These will be settings based
    /**
     * Default repetition amount
     */
    private int defaultRepCount = 2;
    /**
     * Value of maximum weeks before this day to look for exercises
     */
    private int maximumWeeksBefore = 10;

    /** TRUE if current RecyclerView = muscle.
     * FALSE if current RecyclerView = exercise*/
    Boolean isDayRecyclerViewMuscle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        DB = new DatabaseHandler(this);

        doneList = DB.getAllDones();
        new SetUp().setAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LinearLayoutManagerHorizontalSwipe layoutManager = (LinearLayoutManagerHorizontalSwipe)dayRecyclerView.getLayoutManager();
        layoutManager.isSwipeHorizontal().observe(MainActivity.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                layoutManager.isSwipeHorizontal().removeObserver(this);
                Intent intent = new Intent(context, WorkoutActivity.class);
                intent.putExtra("quantityAndReps", (Serializable) quantityAndRepsList);
                startActivityForResult(intent, 2);
            }
        });
    }

    @Override
    /** Gets the result from AddMenu. Processes it and shows the data in HistoryRecyclerView */
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_FIRST_USER) {
            int doneId = data.getIntExtra("doneId", -1);
            if(doneId != -1) {
                new DoneOperations().addDone(doneId);
                /**
                 * Refreshes the dayExerciseRecyclerView if the done had a date that's already showing
                 */
                if((DB.getDone(doneId).getTrimmedDate()).equals(dateOfDayExercisesShowing)) {
                    quantityAndRepsList = getExercisesForThisDay();
                    dayExerciseRecyclerViewAdapter = new DayExerciseRecyclerViewAdapter(context, quantityAndRepsList);
                    dayRecyclerView.setAdapter(dayExerciseRecyclerViewAdapter);
                }
            }
        }
        //  if it takes too long, change to do the refresh only for a current day
        else if(requestCode == 2) {
            new SetUp().refreshHistory();
            doneExercisesRecyclerViewAdapter = new DoneExercisesRecyclerViewAdapter(DB.getDonesByDate(null), context);
            dayRecyclerView.setAdapter(doneExercisesRecyclerViewAdapter);
            workoutIsDone = true;
            isDayRecyclerViewMuscle = false;

            //  refreshes muscles for a current day
            dayMuscleList = DB.getMusclesByCurrentDay();
            dayMuscleRecyclerViewAdapter.notifyDataSetChanged();
        }

        //  checks if data needs to refresh
        int changesInExercises = data.getIntExtra("changesInExercises", 0);
        Log.d(TAG, "onActivityResult: " + changesInExercises);
        switch (changesInExercises) {
            case 1:
                new SetUp().refreshMuscles();
                break;
            case 2:
                new SetUp().refreshDayExercise();
                break;
            case 3:
                new SetUp().refreshMuscles();
                new SetUp().refreshDayExercise();
                break;
        }
    }

    /** checks value of {@code isDayRecyclerViewMuscle} <br/>
     *  sets RecyclerViewAdapter to <br/>
     *  DayMuscleRecyclerViewAdapter if TRUE <br/>
     *  DayExerciseRecyclerViewAdapter if FALSE
     */
        //  it has a commented out part and i dont know why ======================================================================================================================================================
    private void dayRecyclerViewSetAdapter() {
        if(isDayRecyclerViewMuscle) {
//            if(quantityAndRepsList.size() == 0)
//                populateQuantityAndReps(null);
//            getExercisesForThisDay();
            if(workoutIsDone)
                dayRecyclerView.setAdapter(doneExercisesRecyclerViewAdapter);
            else
                dayRecyclerView.setAdapter(dayExerciseRecyclerViewAdapter);
        }
        else {
            dayRecyclerView.setAdapter(dayMuscleRecyclerViewAdapter);
        }
        isDayRecyclerViewMuscle = !isDayRecyclerViewMuscle;
    }

    /**
     * Gets proper exercises for this day. Tries to populate them with amount.
     * @return list of exercises, their quantity and repetitions
     */
    private List<QuantityAndReps> getExercisesForThisDay() {
        List<Exercise> todayExercises = DB.getExercisesByCurrentDay();

            //  Gets the last time that exercises were performed in that day
        List<QuantityAndReps> localQuantityAndRepsList = new ArrayList<>();
        List<QuantityAndReps> quantityAndRepsList = new ArrayList<>();
        Long currentDate = Calendar.getInstance().getTimeInMillis();
        SimpleDateFormat dayName = new SimpleDateFormat("EEEE");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            //  find the latest exercises for this day
        for(int i = 0; i < maximumWeeksBefore; i++) {
                //  subtracts a week in milliseconds from current date
            currentDate -= 604800000;
            localQuantityAndRepsList = populateQuantityAndReps(dateFormat.format(currentDate));
            if(localQuantityAndRepsList.size() != 0) {
                dateOfDayExercisesShowing = dateFormat.format(currentDate);
                break;
            }
        }

        /**
         * Checks if there were done exercises and populates quatityAndRepsList
         */
        for(Exercise exercise : todayExercises) {
            Boolean isBreak = Boolean.FALSE;
            for(QuantityAndReps quantityAndReps : localQuantityAndRepsList) {
                if(quantityAndReps.getExerciseId() == exercise.getExerciseId()) {
                    quantityAndRepsList.add(quantityAndReps);
                    localQuantityAndRepsList.remove(quantityAndReps);
                    isBreak = Boolean.TRUE;
                    break;
                }
            }
            if(!isBreak) {
                quantityAndRepsList.add(new QuantityAndReps(exercise.getExerciseId(),
                        exercise.getExerciseName(),
                        -1, Boolean.FALSE, defaultRepCount));
            }
        }
        return quantityAndRepsList;
    }

    /**
     * @param date String containing the date for which quantityAndRepsList should be returned.
     *             If not set, method calculates list for a current day
     * @return list of exercises, their quantity and repetitions
     */
    private List<QuantityAndReps> populateQuantityAndReps(@Nullable String date) {
        if(date == null) {
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            date = dateFormat.format(currentDate);
        }
        List<Done> donesListByDate = DB.getDonesByDate(date);
        List<QuantityAndReps> quantityAndRepsList = new ArrayList<>();
        for(Done done : donesListByDate) {
            int doneExerciseId = done.getExerciseId();
            Boolean isBreak = Boolean.FALSE;
            for(int i = 0; i < quantityAndRepsList.size(); i++) {
                if(quantityAndRepsList.get(i).getExerciseId() == doneExerciseId) {
                    quantityAndRepsList.get(i).setQuantity(quantityAndRepsList.get(i).getQuantity() + done.getQuantity());
                    quantityAndRepsList.get(i).setReps(quantityAndRepsList.get(i).getReps() + 1);
                    if(done.isCanMore())
                        quantityAndRepsList.get(i).setCanMore(true);
                    isBreak = Boolean.TRUE;
                    break;
                }
            }
            if(!isBreak) {
                quantityAndRepsList.add(new QuantityAndReps(done.getExerciseId(), DB.getExercise(done.getExerciseId()).getExerciseName(), done.getQuantity(), done.isCanMore(), 1));
            }
        }
        for(QuantityAndReps quantityAndReps : quantityAndRepsList) {
            quantityAndReps.setQuantity(quantityAndReps.getQuantity() / quantityAndReps.getReps());
        }
        return quantityAndRepsList;
    }

    /**
     * Class for setting up everything necessary for MainActivity to work
     */
    private class SetUp {
        /**
         * Sets up the views, actionBar, buttons and recyclerViews
         */
        void setAll() {
            workoutIsDone = false;

            setViews();
            setUpActionBar();
            setUpButtons();
            setUpRecyclerViews();
        }

        /**
         *Sets up the necessary views
         */
        void setViews() {
            dayRecyclerView = findViewById(R.id.dayRecyclerView);
            historyRecyclerView = findViewById(R.id.historyRecyclerView);
            addButton = findViewById(R.id.app_toolbar_addButton);
            accountButton = findViewById(R.id.app_toolbar_accountButton);
        }

        /**
         * Sets up the custom actionBar for the application
         */
        void setUpActionBar() {
            Toolbar appToolbar = (Toolbar) findViewById(R.id.appToolbar);       //  make Toolbar to use logo
            setSupportActionBar(appToolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        /**
         * Expands area of the actionBar buttons and sets onClicks
         */
        void setUpButtons() {
            ButtonHandler buttonHandler = new ButtonHandler();
            buttonHandler.expandTouchableArea(addButton, null);
            addButton.setOnClickListener(buttonHandler.addButtonOnClickListener);

            //buttonHandler.expandTouchableArea(accountButton, null);
            accountButton.setOnClickListener(buttonHandler.accountButtonOnClickListener);
        }

        /**
         * Sets up dayRecyclerView and HistoryRecyclerView
         */
        void setUpRecyclerViews() {
                //  Instantiate variables
            List<Done> todaysDoneList = DB.getDonesByDate(null);
            if(todaysDoneList.size() != 0) {
                doneExercisesRecyclerViewAdapter = new DoneExercisesRecyclerViewAdapter(todaysDoneList, context);
                workoutIsDone = true;
            }

                //  Create RecyclerViews
                    //  HistoryRecyclerView
            historyRecyclerView.setHasFixedSize(true);
            historyRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            refreshHistory();
            historyRecyclerView.setAdapter(historyRecyclerViewFullAdapter);

                    //  DayRecyclerView
            dayRecyclerView.setHasFixedSize(true);
            LinearLayoutManagerHorizontalSwipe layoutManager = new LinearLayoutManagerHorizontalSwipe(context, LinearLayoutManager.VERTICAL, false);
            dayRecyclerView.setLayoutManager(layoutManager);

            isDayRecyclerViewMuscle = Boolean.TRUE;
            refreshMuscles();
            refreshDayExercise();
            dayRecyclerView.setAdapter(dayMuscleRecyclerViewAdapter);

            dayRecyclerView.setOnTouchListener((v, event) -> {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    dayRecyclerViewSetAdapter();
                    return true;
                }
                else
                    return false;
            });
        }

        private void refreshHistory() {
            sortedDoneList = new DoneOperations().sortDoneListByDateGroup();
            quantityAndRepsList = getExercisesForThisDay();

            if(historyRecyclerViewFullAdapter == null)
                historyRecyclerViewFullAdapter = new HistoryRecyclerViewFullAdapter(context, sortedDoneList, muscleDateTimeList);
            else
                historyRecyclerViewFullAdapter.notifyDataSetChanged();
        }

        private void refreshMuscles() {
            dayMuscleList = DB.getMusclesByCurrentDay();
            // Bruteforce way
            dayMuscleRecyclerViewAdapter = new DayMuscleRecyclerViewAdapter(context, dayMuscleList);
            if(isDayRecyclerViewMuscle)
                dayRecyclerView.setAdapter(dayMuscleRecyclerViewAdapter);
            //  Optimal way, somehow not working
//            if(dayMuscleRecyclerViewAdapter == null)
//                dayMuscleRecyclerViewAdapter = new DayMuscleRecyclerViewAdapter(context, dayMuscleList);
//            else {
//                dayMuscleRecyclerViewAdapter.notifyDataSetChanged();
//            }
        }

        private void refreshDayExercise() {
            populateQuantityAndReps(null);
            // Bruteforce way
            dayExerciseRecyclerViewAdapter = new DayExerciseRecyclerViewAdapter(context, quantityAndRepsList);
            if(!isDayRecyclerViewMuscle)
                dayRecyclerView.setAdapter(dayExerciseRecyclerViewAdapter);
            //  Optimal way, somehow not working
//            if(dayExerciseRecyclerViewAdapter == null)
//                dayExerciseRecyclerViewAdapter = new DayExerciseRecyclerViewAdapter(context, quantityAndRepsList);
//            else
//                dayExerciseRecyclerViewAdapter.notifyDataSetChanged();
        }

        class ButtonHandler {

            private View.OnClickListener addButtonOnClickListener = v -> {
                Intent addIntent = new Intent(context, AddMenuActivity.class);
                startActivityForResult(addIntent, RESULT_FIRST_USER);
            };

            private View.OnClickListener accountButtonOnClickListener = v -> {

            };

            /** expands the area returning touchEvent for the provided view.
             * @param view view which touchable area should be expanded
             * @param sizeOfExpansion @Nullable size of expansion. If not provided, the size will be of the actionBarSize - 1.5 * preferable size of the button
             */
            private void expandTouchableArea(View view, @Nullable Integer sizeOfExpansion) {
                final View parent = (View) view.getParent();
                if(sizeOfExpansion == null) {
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_baseline_add_24);
                    final TypedArray actionBar = parent.getContext().getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
                    sizeOfExpansion = (int) actionBar.getDimension(0, 0) - (int)(1.5 * drawable.getIntrinsicWidth());
                }

                Integer finalSizeOfExpansion = sizeOfExpansion;
                parent.post(() -> {
                    final Rect area = new Rect();
                    view.getHitRect(area);
                    area.top -= finalSizeOfExpansion;
                    area.bottom += finalSizeOfExpansion;
                    area.left -= finalSizeOfExpansion;
                    area.right += finalSizeOfExpansion;
                    parent.setTouchDelegate(new TouchDelegate(area, view));
                });
            }
        }
    }

    /**
     * Class containing operations related to getting a new Done and making changes in MainActivity class fields
     */
    private class DoneOperations {
        /* Optimization possibilities: Depending on dateIntoMillis() execution time and the frequency of adding dones via AddMenu,
            save the date in milliseconds for each done. Then, dateIntoMillis() will be redundant
        */

        /**
         * Returns sortedDoneList which is sorted from newest to the oldest and creates muscleDateTimeList -
         * a helper class for HistoryRecyclerViewFullAdapter
         * to show simplified information about exercises done in a particular day <br/>
         * muscleDateTimeList is sorted from the newest to the oldest <br/>
         * sorts doneList from oldest to newest
         * @return new doneList sorted by newest dates and from the first exercise in a date to the last
         */
        List<Done> sortDoneListByDateGroup() {
            List<Done> sortedDoneList = new ArrayList<>();
            muscleDateTimeList = new ArrayList<>();
            if(doneList.size() == 0)
                return sortedDoneList;

            sortDoneList();

            List<Done> tempDoneList = new ArrayList<>();
            muscleDateTimeList = new ArrayList<>();
            /** Indexes are used to know the amount of Dones to allocate for a particular MuscleDateTime in HistoryRecyclerViewFullAdapter */
            List<Integer> listOfIndexes = new ArrayList<>();

            tempDoneList.add(doneList.get(doneList.size() - 1));
            int numberOfIndexes = 1;
            /**Makes sorted lists of Done and MuscleDateTime
             * If dates match, adds Done to tempDoneList.
             * If not, makes a new MuscleDateTime out of the tempDoneList*/
            for(int i = doneList.size() - 2; i >= 0; i--) {
                if(doneList.get(i).getTrimmedDate().equals(doneList.get(i + 1).getTrimmedDate()))
                {
                    /** Sorts Dones from first to last */
                    tempDoneList.add(0, doneList.get(i));
                    numberOfIndexes++;
                }
                else {
                    /** Adds all Dones for a date and adds a new muscleDateTime */
                    sortedDoneList.addAll(tempDoneList);
                    muscleDateTimeList.add(new MuscleDateTime(
                            DB.getMusclesByExerciseDate(tempDoneList.get(0).getTrimmedDate()),
                            tempDoneList.get(0).getTrimmedDate(),
                            tempDoneList.get(0).getTrimmedDateTime(),
                            tempDoneList.get(tempDoneList.size() - 1).getTrimmedDateTime()));
                        //  Fills the time of exercise sum if there is only one exercise
                    if(tempDoneList.size() == 1)
                        muscleDateTimeList.get(muscleDateTimeList.size() - 1).setDateTimeFirstDone(tempDoneList.get(0).getTime());
                    tempDoneList.clear();
                    tempDoneList.add(doneList.get(i));
                    listOfIndexes.add(numberOfIndexes);
                    numberOfIndexes = 1;
                }
            }
            /** adds the last portion of Dones and the last muscleDateTimeList */
            sortedDoneList.addAll(tempDoneList);
            muscleDateTimeList.add(new MuscleDateTime(
                    DB.getMusclesByExerciseDate(tempDoneList.get(0).getTrimmedDate()),
                    tempDoneList.get(0).getTrimmedDate(),
                    tempDoneList.get(0).getTrimmedDateTime(),
                    tempDoneList.get(tempDoneList.size() - 1).getTrimmedDateTime()));
                //  Fills the time of exercise sum if there is only one exercise
            if(tempDoneList.size() == 1)
                muscleDateTimeList.get(muscleDateTimeList.size() - 1).setDateTimeFirstDone(tempDoneList.get(0).getTime());
            listOfIndexes.add(numberOfIndexes);

            /** Calculates the end index of a Done allocated to the particular MuscleDateTime */
            int sum;
            for(int i = 0; i < listOfIndexes.size(); i++) {
                sum = 0;
                for(int n = 0; n <= i; n++) {
                    sum += listOfIndexes.get(n);
                }
                muscleDateTimeList.get(i).setSortedDoneListEndIndex(sum);
            }
            return sortedDoneList;
        }

        /**
         * Adds a new Done to the muscleDateTimeList.
         * Creates new MuscleDateTime if necessary
         * @param doneId id of Done to add
         */
        void addDone(int doneId) {
            Done newDone = DB.getDone(doneId);
            Boolean added = Boolean.FALSE;
            /**
             * Holds a value of the index at which new done is added to the sortedDoneList
             */
            int newDoneIndex;

            long newDoneDate = dateIntoMillis(newDone.getDate());
            long newDoneHourMillis = hourIntoMillis(newDone.getDate());
            /** Adds the new done at an adequate index*/
            for(newDoneIndex = 0; newDoneIndex < sortedDoneList.size(); newDoneIndex++) {
                Done done = sortedDoneList.get(newDoneIndex);
                long doneDate = dateIntoMillis(done.getDate());

                if(newDoneDate >= doneDate) {
                    /**
                     * Runs, if new done has the same done and is of earlier hour
                     * Or, if new done's date is greater than the done date
                     */
                    if(newDoneDate == doneDate &&
                            newDoneHourMillis < hourIntoMillis(done.getDate())) {
                        sortedDoneList.add(newDoneIndex, newDone);
                        added = Boolean.TRUE;
                        break;
                    }
                    else if(newDoneDate > doneDate) {
                        sortedDoneList.add(newDoneIndex, newDone);
                        added = Boolean.TRUE;
                        break;
                    }
                }
            }
            /** Adds the new done as the eldest one */
            if(!added) {
                sortedDoneList.add(newDoneIndex, newDone);
                /** Checks if a muscleDateTime for the date of newDone exists */
                if(!(newDone.getTrimmedDate()).equals(muscleDateTimeList.get(muscleDateTimeList.size() - 1).getDate())) {
                    muscleDateTimeList.add(new MuscleDateTime(DB.getMusclesByExerciseDate(newDone.getTrimmedDate()),
                            newDone.getTrimmedDate(),
                            newDone.getTrimmedDateTime(),
                            newDoneIndex + 1));
                    historyRecyclerViewFullAdapter.addToSubListVisibleList(muscleDateTimeList.size() - 1);
                    historyRecyclerViewFullAdapter.notifyDataSetChanged();
                }
                else {
                    changeOverallExerciseTime(newDoneIndex, muscleDateTimeList.size() - 1);
                    moveSortedDoneListEndIndexesByOne(muscleDateTimeList.size() - 1);
                    historyRecyclerViewFullAdapter.notifyItemChanged(muscleDateTimeList.size() - 1);
                }
                return;
            }

            /**
             * Function checks, if the muscleDateTimeList at first end index is different, and adds a new muscleDateTime.
             * Later, if the date is the same, adds the done inside of the muscleDateTimeList.
             * Possibility of the done being last was excluded by previous function.
             *
             * If muscleDateTime wasnt added, historyRecyclerViewAdapter only needs to be notified that the item at this position changed.
             * It also calls the next rows to refresh their data, because their endIndexes were moved by one.
             * If muscleDateTime was added, list of muscleDateTime visibility has to be updated.
             * Otherwise, it's too small for current array size
             */
            int previousEndIndex = 0;
            for(int i = 0; i < muscleDateTimeList.size(); i++) {
                if(newDoneIndex >= previousEndIndex &&
                        newDoneIndex <= muscleDateTimeList.get(i).getSortedDoneListEndIndex()) {
                    /**
                     * Checks, if the index is first of the range. If so, adds a new muscleDateTime if the dates are different
                     */
                    if(newDoneIndex == previousEndIndex &&
                            !(newDone.getTrimmedDate()).equals(muscleDateTimeList.get(i).getDate())) {
                        muscleDateTimeList.add(i, new MuscleDateTime(DB.getMusclesByExerciseDate(newDone.getTrimmedDate()),
                                newDone.getTrimmedDate(),
                                newDone.getTrimmedDateTime(),
                                newDoneIndex));
                        moveSortedDoneListEndIndexesByOne(i);
                            //  Fills the time of exercise sum because there is only one exercise
                        muscleDateTimeList.get(i).setDateTimeFirstDone(sortedDoneList.get(newDoneIndex).getTime());
                        historyRecyclerViewFullAdapter.addToSubListVisibleList(i);
                        historyRecyclerViewFullAdapter.notifyDataSetChanged();
                        return;
                    }
                    if((newDone.getTrimmedDate()).equals(muscleDateTimeList.get(i).getDate())) {
                        changeOverallExerciseTime(newDoneIndex, i);
                        moveSortedDoneListEndIndexesByOne(i);
                        historyRecyclerViewFullAdapter.notifyItemChanged(i);
                        historyRecyclerViewFullAdapter.notifyItemRangeChanged(i, muscleDateTimeList.size() - i - 1);
                        return;
                    }
                }
                previousEndIndex = muscleDateTimeList.get(i).getSortedDoneListEndIndex();
            }

//            /** Adds the new done as the latest one */
//            if(newDoneIndex == 0) {
//                Log.d(TAG, "addDone: adding as the latest one");
//                    // checks if the dates are different
//                if(!newDone.getTrimmedDate().equals(muscleDateTimeList.get(0).getDate())) {
//                    muscleDateTimeList.add(0, new MuscleDateTime(DB.getMusclesByExerciseDate(newDone.getTrimmedDate()),
//                            newDone.getTrimmedDate(),
//                            newDone.getTrimmedDateTime(),
//                            0));
//                }
//                    //  if they are or are not, moves all indexes by one
//                moveSortedDoneListEndIndexesByOne(0);
//                Log.d(TAG, "addDone: latest one");
//                Log.d(TAG, "adapterNotify: item inserted at 0");
//                historyRecyclerViewFullAdapter.notifyItemInserted(0);
//                return 0;
//            }
//
//            for(int i = 0; i < muscleDateTimeList.size(); i ++) {
//                if(newDoneIndex > muscleDateTimeList.get(i).getSortedDoneListEndIndex() &&
//                        newDoneIndex <= muscleDateTimeList.get(i).getSortedDoneListEndIndex()) {
//                }
//
//            }
//            if(newDoneIndex < muscleDateTimeList.get(0).getSortedDoneListEndIndex())
//
//            /**
//             * Adds the new done into the first muscleDateTime
//             */
//            if(newDoneIndex <= muscleDateTimeList.get(0).getSortedDoneListEndIndex()) {
//                Log.d(TAG, "addDone: first muscledatetime");
//                if((newDone.getTrimmedDate()).equals(muscleDateTimeList.get(0).getDate())) {
//                    Log.d(TAG, "trimmedDate: " + newDone.getTrimmedDate());
//                    Log.d(TAG, "date: " + muscleDateTimeList.get(0).getDate());
//
//                    Log.d(TAG, "addDone: added in the same date 0");
//                }
//                else {
//                    if(newDoneIndex)
//                    muscleDateTimeList.add(0, new MuscleDateTime(DB.getMusclesByExerciseDate(newDone.getTrimmedDate()),
//                            newDone.getTrimmedDate(),
//                            newDone.getTrimmedDateTime(),
//                            newDoneIndex));
//
//                    historyRecyclerViewFullAdapter.notifyItemInserted(0);
//                    Log.d(TAG, "addDone: added in another date 0");
//                }
//
//                //  move the range of the next muscleDateTime endIndexes
//                moveSortedDoneListEndIndexesByOne(0);
//                Log.d(TAG, "adapterNotify: item inserted at 0");
//                historyRecyclerViewFullAdapter.notifyDataSetChanged();
//                return 0;
//            }
//
//            /** Default: Adds the new done in the middle */
//            Log.d(TAG, "addDone: default behavior");
//            for(int i = 1; i < muscleDateTimeList.size(); i++) {
//                    //  checks if new done is in the muscleDateTimeList[i] range of this particular muscleDateTime
//                if(newDoneIndex >= muscleDateTimeList.get(i - 1).getSortedDoneListEndIndex() &&
//                        newDoneIndex < muscleDateTimeList.get(i).getSortedDoneListEndIndex()) {
//                        //  runs if the dates are different
//                    if((newDone.getTrimmedDate()).equals(muscleDateTimeList.get(i).getDate())) {
//                        Log.d(TAG, "addDone: they equal");
//                        Log.d(TAG, "trimmedDate: " + newDone.getTrimmedDate());
//                        Log.d(TAG, "date: " + muscleDateTimeList.get(i).getDate());
//                        Log.d(TAG, "addDone: added in the same date");
//
//                        /**
//                         * throws concurrentModificationException
//                         */
//                        historyRecyclerViewFullAdapter.notifyDataSetChanged();
//                        historyRecyclerViewFullAdapter.notifyDataSetChanged();
//                        historyRecyclerViewFullAdapter.notifyDataSetChanged();
//                    }
//                        //  checks if index is the last index of the previous muscleDateTime. If so, inserts it into its boundaries
//                    else if(newDoneIndex == muscleDateTimeList.get(i - 1).getSortedDoneListEndIndex()) {
//                        Log.d(TAG, "trimmedDate: " + newDone.getTrimmedDate());
//                        Log.d(TAG, "date: " + muscleDateTimeList.get(i - 1).getDate());
//                        moveSortedDoneListEndIndexesByOne(i - 1);
//                        Log.d(TAG, "addDone: added done as a last index of the muscleDateTime");
//                        return i - 1;
//                    }
//                        //  runs if the dates are different
//                    else {
//                        Log.d(TAG, "trimmedDate: " + newDone.getTrimmedDate());
//                        Log.d(TAG, "date: " + muscleDateTimeList.get(i).getDate());
//                        Log.d(TAG, "trimmedDate: " + dateIntoMillis(newDone.getTrimmedDate()));
//                        Log.d(TAG, "date: " + dateIntoMillis(muscleDateTimeList.get(i).getDate()));
//                        //  if the date is smaller that the date at i, inserts it after this date and not before
//                        if (dateIntoMillis(newDone.getTrimmedDate()) < dateIntoMillis(muscleDateTimeList.get(i).getDate()))
//                            i++;
//                        muscleDateTimeList.add(i, new MuscleDateTime(DB.getMusclesByExerciseDate(newDone.getTrimmedDate()),
//                                newDone.getTrimmedDate(),
//                                newDone.getTrimmedDateTime(),
//                                newDoneIndex));
//                        historyRecyclerViewFullAdapter.notifyItemInserted(i);
//                        Log.d(TAG, "addDone: added in another date");
//                    }
//
//                    //  move the range of the next muscleDateTime endIndexes
//                    moveSortedDoneListEndIndexesByOne(i);
//                    Log.d(TAG, "adapterNotify: item inserted at " + i);
//                    historyRecyclerViewFullAdapter.notifyDataSetChanged();
//                    return i;
//                }
//            }
//            Log.d(TAG, "addDone: nothing added");
//            return 0;
        }

        void changeOverallExerciseTime(int newDoneIndex, int muscleDateTimeIndex) {
            long newDoneDate = dateIntoMillis(sortedDoneList.get(newDoneIndex).getTrimmedDate());
            Log.d(TAG, "changeOverallExerciseTime: " + newDoneDate);
            Log.d(TAG, newDoneIndex + "");
            Log.d(TAG, sortedDoneList.size() + "");
                //  change time newD, lastD
            if(newDoneIndex == 0 || dateIntoMillis(sortedDoneList.get(newDoneIndex - 1).getTrimmedDate()) > newDoneDate) {
                Log.d(TAG, "changeOverallExerciseTime: smoler");
                int i = newDoneIndex;
                for(; i < sortedDoneList.size() - 1; i++) {
                    Log.d(TAG, sortedDoneList.get(i + 1).getTrimmedDate());
                    if(newDoneDate > dateIntoMillis(sortedDoneList.get(i + 1).getTrimmedDate())) {
                        Log.d(TAG, "changeOverallExerciseTime: breaking at " + i);
                        break;
                    }
                }
                muscleDateTimeList.get(muscleDateTimeIndex).setTime(sortedDoneList.get(newDoneIndex).getTrimmedDateTime(), sortedDoneList.get(i).getTrimmedDateTime());
                Log.d(TAG, "changeOverallExerciseTime: " + muscleDateTimeList.get(muscleDateTimeIndex).getTime());
            }
            //  change time lastD, newD
            else if(newDoneIndex == sortedDoneList.size() - 1 || dateIntoMillis(sortedDoneList.get(newDoneIndex + 1).getTrimmedDate()) < newDoneDate) {
                int i = newDoneIndex;
                Log.d(TAG, "changeOverallExerciseTime: biger");
                for(; i > 0; i--) {
                    if(newDoneDate < dateIntoMillis(sortedDoneList.get(i - 1).getTrimmedDate())) {
                        break;
                    }
                }
                muscleDateTimeList.get(muscleDateTimeIndex).setTime(sortedDoneList.get(i).getTrimmedDateTime(), sortedDoneList.get(newDoneIndex).getTrimmedDateTime());
                Log.d(TAG, "changeOverallExerciseTime: " + muscleDateTimeList.get(muscleDateTimeIndex).getTime());
            }
        }

        /** Changes formatted date string into the time in milliseconds */
        long dateIntoMillis(String date) {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            try {
                return format.parse(date).getTime();
            }
            catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        }

        /** Changes formatted date string into the time in milliseconds */
        long hourIntoMillis(String date) {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            try {
                return format.parse(date).getTime();
            }
            catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        }

        /**Sorts dones from the latest to the newest
         * Method uses merge sort, which is appropriate for such small array sizes
         *  If the typical data turn out to be mostly sorted, may change it to Bubble sort or Insertion sort */
        void sortDoneList() {
            doneList.sort((o1, o2) -> {
                long o1Time = hourIntoMillis(o1.getDate());
                long o2Time = hourIntoMillis(o2.getDate());

                if(o1Time < o2Time)
                    return -1;
                else if(o1Time == o2Time)
                    return 0;
                else
                    return 1;
            });
        }

        /**
         * Moves sortedDoneListEndIndexes of all muscleDateTimes starting from startingIndex
         * @param startingIndex index from which the increment should start
         */
        void moveSortedDoneListEndIndexesByOne(int startingIndex) {
            for(int i = startingIndex; i < muscleDateTimeList.size(); i++) {
                muscleDateTimeList.get(i).setSortedDoneListEndIndex(
                        muscleDateTimeList.get(i).getSortedDoneListEndIndex() + 1);
            }
        }
    }

}
































