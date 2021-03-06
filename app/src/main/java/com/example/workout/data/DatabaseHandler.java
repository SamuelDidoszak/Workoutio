package com.example.workout.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.workout.model.Day;
import com.example.workout.model.DayExerciseConnector;
import com.example.workout.model.Done;
import com.example.workout.model.Exercise;
import com.example.workout.model.Muscle;
import com.example.workout.model.MuscleExerciseConnector;
import com.example.workout.model.Note;
import com.example.workout.model.QuantityAndReps;
import com.example.workout.util.Constants;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper implements Serializable {

    Context context;

        //  Export it into the settings
    private final boolean saveAsCustomAutomatically = false;

    public DatabaseHandler(@Nullable Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_MUSCLE = "CREATE TABLE " + Constants.TABLE_MUSCLE + "(" + Constants.COLUMN_MUSCLE_ID + " INTEGER PRIMARY KEY, " +
                Constants.COLUMN_MUSCLE_NAME + " TEXT, " + Constants.COLUMN_MUSCLE_ICON + " INTEGER)";
        String CREATE_TABLE_EXERCISE = "CREATE TABLE " + Constants.TABLE_EXERCISE + "(" + Constants.COLUMN_EXERCISE_ID + " INTEGER PRIMARY KEY, " +
                Constants.COLUMN_EXERCISE_NAME + " TEXT, " + Constants.COLUMN_CUSTOM_EXERCISE + " INTEGER, " + Constants.COLUMN_TIME_AS_AMOUNT + " INTEGER, " + Constants.COLUMN_DEFAULT_NEGATIVE + " INTEGER)";
        String CREATE_TABLE_DONE = "CREATE TABLE " + Constants.TABLE_DONE + "(" + Constants.COLUMN_DONE_ID + " INTEGER PRIMARY KEY, " +
                Constants.COLUMN_DATE + " TEXT, " + Constants.COLUMN_EXERCISE_ID + " INTEGER, " + Constants.COLUMN_QUANTITY + " INTEGER, " +
                Constants.COLUMN_TIME + " INTEGER, " + Constants.COLUMN_NEGATIVE + " INTEGER, " + Constants.COLUMN_CAN_MORE + " INTEGER)";
        String CREATE_TABLE_DAY = "CREATE TABLE " + Constants.TABLE_DAY + "(" + Constants.COLUMN_DAY_ID + " INTEGER PRIMARY KEY, " + Constants.COLUMN_DAY_NAME + " TEXT, " + Constants.COLUMN_CUSTOM_DAY + " INTEGER)";
        String CREATE_TABLE_MUSCLE_EXERCISE_CONNECTOR = "CREATE TABLE " + Constants.TABLE_MUSCLE_EXERCISE_CONNECTOR + "(" + Constants.COLUMN_MUSCLE_EXERCISE_CONNECTOR_ID
                + " INTEGER PRIMARY KEY, " + Constants.COLUMN_MUSCLE_ID + " INTEGER, " + Constants.COLUMN_EXERCISE_ID + " INTEGER)";
        String CREATE_TABLE_DAY_EXERCISE_CONNECTOR = "CREATE TABLE " + Constants.TABLE_DAY_EXERCISE_CONNECTOR + "(" + Constants.COLUMN_DAY_EXERCISE_CONNECTOR_ID
                + " INTEGER PRIMARY KEY, " + Constants.COLUMN_DAY_ID + " INTEGER, " + Constants.COLUMN_EXERCISE_ID + " INTEGER, " + Constants.COLUMN_DAY_EXERCISE_CONNECTOR_POSITION + " INTEGER)";
        String CREATE_TABLE_NOTE = "CREATE TABLE " + Constants.TABLE_NOTE + "(" + Constants.COLUMN_NOTE_ID + " INTEGER PRIMARY KEY, " + Constants.COLUMN_DATE + " TEXT, " + Constants.COLUMN_NOTE + " TEXT)";

        db.execSQL(CREATE_TABLE_MUSCLE);
        db.execSQL(CREATE_TABLE_EXERCISE);
        db.execSQL(CREATE_TABLE_DONE);
        db.execSQL(CREATE_TABLE_DAY);
        db.execSQL(CREATE_TABLE_MUSCLE_EXERCISE_CONNECTOR);
        db.execSQL(CREATE_TABLE_DAY_EXERCISE_CONNECTOR);
        db.execSQL(CREATE_TABLE_NOTE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int nevVersion) {
        String DROP_TABLE = "DROP TABLE IF EXISTS " + Constants.DATABASE_NAME;
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    public void deleteDatabase(){
        context.deleteDatabase(Constants.DATABASE_NAME);
    }

        //  add items

    public void addDay(Day day) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_DAY_NAME, day.getDayName());
        values.put(Constants.COLUMN_CUSTOM_DAY, day.isCustom() ? 1 : 0);

        long id = db.insert(Constants.TABLE_DAY, null, values);
        day.setDayId((int)id);
        db.close();
    }

    public void addCustomDone(Done done) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //  Set exercise to be custom
        if(saveAsCustomAutomatically && !getExercise(done.getExerciseId()).isCustomExercise())
            transferExercise(done.getExerciseId());

        values.put(Constants.COLUMN_DATE, done.getDate());
        values.put(Constants.COLUMN_EXERCISE_ID, done.getExerciseId());
        values.put(Constants.COLUMN_QUANTITY, done.getQuantity());
        values.put(Constants.COLUMN_TIME, done.getTime());

        int negative = done.isNegative() ? 1 : 0;
        values.put(Constants.COLUMN_NEGATIVE, negative);
        int canMore = done.isCanMore() ? 1 : 0;
        values.put(Constants.COLUMN_CAN_MORE, canMore);

        long id = db.insert(Constants.TABLE_DONE, null, values);
        done.setDoneId((int)id);

        db.close();
    }

    public void addDone(Done done) {
        SQLiteDatabase db = this.getWritableDatabase();

        Date currentDate = Calendar.getInstance().getTime();
        currentDate.setTime(currentDate.getTime() - done.getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        String formattedDate = dateFormat.format(currentDate);

        Log.d("TAG", "addDone: " + formattedDate);

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_DATE, formattedDate);
        values.put(Constants.COLUMN_EXERCISE_ID, done.getExerciseId());
        values.put(Constants.COLUMN_QUANTITY, done.getQuantity());
        values.put(Constants.COLUMN_TIME, done.getTime());
        int negative = done.isNegative() ? 1 : 0;
        values.put(Constants.COLUMN_NEGATIVE, negative);
        int canMore = done.isCanMore() ? 1 : 0;
        values.put(Constants.COLUMN_CAN_MORE, canMore);

        long id = db.insert(Constants.TABLE_DONE, null, values);
        done.setDoneId((int)id);
        done.setDate(formattedDate);
        db.close();
    }

    public void addExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_EXERCISE_NAME, exercise.getExerciseName());
        int custom = exercise.isCustomExercise() ? 1 : 0;
        int timeAsAmount = exercise.isTimeAsAmount() ? 1 : 0;
        int defaultNegative = exercise.isDefaultNegative() ? 1 : 0;
        values.put(Constants.COLUMN_CUSTOM_EXERCISE, custom);
        values.put(Constants.COLUMN_TIME_AS_AMOUNT, timeAsAmount);
        values.put(Constants.COLUMN_DEFAULT_NEGATIVE, defaultNegative);

        long id = db.insert(Constants.TABLE_EXERCISE, null, values);
        exercise.setExerciseId((int)id);
        db.close();
    }

    public void addMuscle(Muscle muscle) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_MUSCLE_NAME, muscle.getMuscleName());
        values.put(Constants.COLUMN_MUSCLE_ICON, muscle.getMuscleIcon());

        long id = db.insert(Constants.TABLE_MUSCLE, null, values);
        muscle.setMuscleId((int)id);
        db.close();
    }

    public void addMuscleExerciseConnector(MuscleExerciseConnector muscleExerciseConnector) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_MUSCLE_ID, muscleExerciseConnector.getMuscleId());
        values.put(Constants.COLUMN_EXERCISE_ID, muscleExerciseConnector.getExerciseId());

        long id = db.insert(Constants.TABLE_MUSCLE_EXERCISE_CONNECTOR, null, values);
        muscleExerciseConnector.setMuscleExerciseConnectorId((int)id);
        db.close();
    }

    public void addDayExerciseConnector(DayExerciseConnector dayExerciseConnector) {

        if(dayExerciseConnector.getPosition() == null) {
            int position = 0;
            List<DayExerciseConnector> dayExerciseConnectorList = getDayExerciseConnectorByDay(dayExerciseConnector.getDayId());
            for(DayExerciseConnector tempDayExerciseConnector : dayExerciseConnectorList) {
                if(tempDayExerciseConnector.getPosition() > position)
                    position = tempDayExerciseConnector.getPosition() + 1;
            }
            dayExerciseConnector.setPosition(position);
        }

        ContentValues values = new ContentValues();
        //  Set exercise to be custom
        if(saveAsCustomAutomatically && !getExercise(dayExerciseConnector.getExerciseId()).isCustomExercise())
            transferExercise(dayExerciseConnector.getExerciseId());

        values.put(Constants.COLUMN_EXERCISE_ID, dayExerciseConnector.getExerciseId());
        values.put(Constants.COLUMN_DAY_ID, dayExerciseConnector.getDayId());
        values.put(Constants.COLUMN_DAY_EXERCISE_CONNECTOR_POSITION, dayExerciseConnector.getPosition());

        SQLiteDatabase DB = this.getWritableDatabase();
        long id = DB.insert(Constants.TABLE_DAY_EXERCISE_CONNECTOR, null, values);
        dayExerciseConnector.setDayExerciseConnectorId((int)id);

        DB.close();
    }

    public void addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_DATE, note.getDate());
        values.put(Constants.COLUMN_NOTE, note.getNote());
        long id = db.insert(Constants.TABLE_NOTE, null, values);
        note.setNoteId((int)id);
        db.close();
    }

        //  remove items

    public void removeDay(int dayId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Constants.TABLE_DAY, Constants.COLUMN_DAY_ID + "=" + dayId, null);
        removeDayExerciseConnectorByDay(dayId);
        db.close();
    }

    public void removeDone(int doneId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Constants.TABLE_DONE, Constants.COLUMN_DONE_ID + "=" + doneId, null);
        db.close();
    }

    public void removeExercise(int exerciseId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int deleted = db.delete(Constants.TABLE_EXERCISE, Constants.COLUMN_EXERCISE_ID + "=" + exerciseId, null);
        if(deleted != 0) {
            Cursor cursor = db.rawQuery("SELECT " + Constants.COLUMN_EXERCISE_ID + " FROM " + Constants.TABLE_DONE +
                    " WHERE " + Constants.COLUMN_EXERCISE_ID + " = " + exerciseId, null);

            String[] ids = new String[cursor.getCount()];
            if(cursor.moveToFirst()) {
                for(int i=0; i < cursor.getCount(); i++) {
                    ids[i] = String.valueOf(cursor.getInt(0));
                }
                db.delete(Constants.TABLE_DONE, Constants.COLUMN_EXERCISE_ID + "=?", ids);
            }

            removeMuscleExerciseConnectorByExercise(exerciseId);
            removeDayExerciseConnectorByDay(exerciseId);
            cursor.close();
        }
        db.close();
    }

    public void removeMuscle(int muscleId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int deleted = db.delete(Constants.TABLE_MUSCLE, Constants.COLUMN_MUSCLE_ID + "=" + muscleId, null);
        if(deleted != 0) {
            Cursor cursor = db.rawQuery("SELECT " + Constants.COLUMN_MUSCLE_ID + " FROM " + Constants.TABLE_MUSCLE_EXERCISE_CONNECTOR +
                    " WHERE " + Constants.COLUMN_MUSCLE_ID + " = " + muscleId, null);

            String[] ids = new String[cursor.getCount()];
            if(cursor.moveToFirst()) {
                for(int i=0; i < cursor.getCount(); i++) {
                    ids[i] = String.valueOf(cursor.getInt(0));
                    db.delete(Constants.TABLE_MUSCLE_EXERCISE_CONNECTOR, Constants.COLUMN_MUSCLE_ID + "=" + ids[i], null);
                }
            }
            cursor.close();
        }
        db.close();
    }

    public void removeMuscleExerciseConnectorById(int muscleExerciseConnectorId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Constants.TABLE_MUSCLE_EXERCISE_CONNECTOR, Constants.COLUMN_MUSCLE_EXERCISE_CONNECTOR_ID + "=" + muscleExerciseConnectorId, null);
        db.close();
    }

    public void removeMuscleExerciseConnectorByMuscle(int muscleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Constants.TABLE_MUSCLE_EXERCISE_CONNECTOR, Constants.COLUMN_MUSCLE_ID + "=" + muscleId, null);
        db.close();
    }

    public void removeMuscleExerciseConnectorByExercise(int exerciseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Constants.TABLE_MUSCLE_EXERCISE_CONNECTOR, Constants.COLUMN_EXERCISE_ID + "=" + exerciseId, null);
        db.close();
    }

    public void removeDayExerciseConnectorById(int dayExerciseConnectorId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Constants.TABLE_DAY_EXERCISE_CONNECTOR, Constants.COLUMN_DAY_EXERCISE_CONNECTOR_ID + "=" + dayExerciseConnectorId, null);
        db.close();
    }

    public void removeDayExerciseConnectorByDay(int dayId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Constants.TABLE_DAY_EXERCISE_CONNECTOR, Constants.COLUMN_DAY_ID + "=" + dayId, null);
        db.close();
    }

    public void removeDayExerciseConnectorByExercise(int exerciseId) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(Constants.TABLE_DAY_EXERCISE_CONNECTOR, Constants.COLUMN_EXERCISE_ID + "=" + exerciseId, null);
        db.close();
    }

    public void removeNote(int noteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Constants.TABLE_NOTE, Constants.COLUMN_NOTE_ID + "=" + noteId, null);
        db.close();
    }

        //  get items

    public Day getDay(int dayId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Constants.TABLE_DAY,
                new String[]{Constants.COLUMN_DAY_ID,
                        Constants.COLUMN_DAY_NAME,
                        Constants.COLUMN_CUSTOM_DAY},
                Constants.COLUMN_DAY_ID + "=?",
                new String[]{String.valueOf(dayId)},
                null, null, null, null);

        if(cursor.getCount() != 0)
            cursor.moveToFirst();

        Day day = new Day();
        if(cursor.getCount() != 0) {
            day.setDayId(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_DAY_ID)));
            day.setDayName(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DAY_NAME)));
            day.setCustom(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_CUSTOM_DAY)) == 1);
        }

        cursor.close();
        db.close();
        return day;
    }

        public Day getCurrentDay() {
            SQLiteDatabase db = this.getReadableDatabase();
            Day currentDay;

            //  Gets the current day and parses it into day name
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
            String date = dateFormat.format(currentDate);

            //  Fetches the day from Database using current dayName
            Cursor dayCursor = db.query(Constants.TABLE_DAY,
                    new String[]{Constants.COLUMN_DAY_ID,
                            Constants.COLUMN_DAY_NAME,
                            Constants.COLUMN_CUSTOM_DAY},
                    Constants.COLUMN_DAY_NAME + "='" + date +"'",
                    null, null, null, null);

            if(dayCursor.getCount() == 0) {
                db.close();
                return new Day();
            }

            dayCursor.moveToFirst();
            currentDay = new Day(dayCursor.getInt(0), dayCursor.getString(1), dayCursor.getInt(2) == 1);
            dayCursor.close();
            db.close();
            return currentDay;
        }

    /**
     * Returns all of the days in which an exercise is performed
     * @param exerciseId identifier of an exercise for which days should be returned
     * @return list of days
     */
        public List<Day> getDaysByExerciseId(int exerciseId) {
            SQLiteDatabase DB = this.getWritableDatabase();
            List<DayExerciseConnector> dayExerciseConnectorList = getDayExerciseConnectorByExercise(exerciseId);
            List<Day> dayList = new ArrayList<>();
            for(DayExerciseConnector dayExerciseConnector : dayExerciseConnectorList) {
                dayList.add(getDay(dayExerciseConnector.getDayId()));
            }

            return dayList;
        }

    public Done getDone(int doneId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Constants.TABLE_DONE,
                new String[]{Constants.COLUMN_DONE_ID,
                    Constants.COLUMN_DATE,
                    Constants.COLUMN_EXERCISE_ID,
                    Constants.COLUMN_QUANTITY,
                    Constants.COLUMN_TIME,
                    Constants.COLUMN_NEGATIVE,
                    Constants.COLUMN_CAN_MORE},
                Constants.COLUMN_DONE_ID + "=?",
                new String[]{String.valueOf(doneId)},
                null, null, null, null);

        if(cursor.getCount() != 0)
            cursor.moveToFirst();

        Done done = new Done();

        if(cursor.getCount() != 0) {
            done.setDoneId(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_DONE_ID)));
            done.setDate(cursor.getString(1));
            done.setExerciseId(cursor.getInt(2));
            done.setQuantity(cursor.getInt(3));
            done.setTime(cursor.getInt(4));
            Boolean negative = cursor.getInt(5) == 1 ? Boolean.TRUE : Boolean.FALSE;
            done.setNegative(negative);
            Boolean canMore = cursor.getInt(6) == 1 ? Boolean.TRUE : Boolean.FALSE;
            done.setCanMore(canMore);
        }

        cursor.close();
        db.close();
        return done;
    }

        /**
         * Fetches Dones by Date.
         * @param date date with a pattern of "dd.MM.yyyy". If not provided, method fetches dones for a current date.
         * @return list of dones.
         */
        public List<Done> getDonesByDate(@Nullable String date) {
            SQLiteDatabase db = this.getReadableDatabase();
            List<Done> doneList = new ArrayList<>();

            if(date == null) {
                Date currentDate = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                date = dateFormat.format(currentDate);
            }
            Cursor cursor = db.query(Constants.TABLE_DONE,
                    new String[]{Constants.COLUMN_DONE_ID,
                            Constants.COLUMN_DATE,
                            Constants.COLUMN_EXERCISE_ID,
                            Constants.COLUMN_QUANTITY,
                            Constants.COLUMN_TIME,
                            Constants.COLUMN_NEGATIVE,
                            Constants.COLUMN_CAN_MORE},
                    Constants.COLUMN_DATE + " LIKE '" + date + "%'",
                    null,
                    null, null, null, null);

            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                do {
                    Boolean negative = cursor.getInt(5) == 1 ? Boolean.TRUE : Boolean.FALSE;
                    Boolean canMore = cursor.getInt(6) == 1 ? Boolean.TRUE : Boolean.FALSE;
                    doneList.add(new Done(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), negative, canMore));
                }
                while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return doneList;
        }

    public Exercise getExercise(int exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + Constants.COLUMN_EXERCISE_ID + ", " + Constants.COLUMN_EXERCISE_NAME + ", " +
                Constants.COLUMN_CUSTOM_EXERCISE + ", " + Constants.COLUMN_TIME_AS_AMOUNT + ", " + Constants.COLUMN_DEFAULT_NEGATIVE +
                " FROM " + Constants.TABLE_EXERCISE + " WHERE " + Constants.COLUMN_EXERCISE_ID +
                "=" + exerciseId, null);

        if(cursor.getCount() != 0)
            cursor.moveToFirst();

        Exercise exercise = new Exercise();
        Boolean custom, timeAsAmount, defaultNegative;

        if(cursor.getCount() != 0) {
            exercise.setExerciseId(cursor.getInt(0));
            exercise.setExerciseName(cursor.getString(1));
            custom = cursor.getInt(2) == 1;
            timeAsAmount = cursor.getInt(3) == 1;
            defaultNegative = cursor.getInt(4) == 1;
            exercise.setCustomExercise(custom);
            exercise.setTimeAsAmount(timeAsAmount);
            exercise.setDefaultNegative(defaultNegative);
        }

        cursor.close();
        db.close();
        return exercise;
    }

        public List<Exercise> getExercisesByCurrentDay() {
            SQLiteDatabase db = this.getReadableDatabase();
            List<Exercise> exerciseList = new ArrayList<>();

            //  Gets the current day and parses it into day name
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
            String date = dateFormat.format(currentDate);

            //  Fetches the day from Database using current dayName
            Cursor dayCursor = db.query(Constants.TABLE_DAY,
                    new String[]{Constants.COLUMN_DAY_ID,
                            Constants.COLUMN_DAY_NAME},
                    Constants.COLUMN_DAY_NAME + "='" + date +"'",
                    null, null, null, null);

            if(dayCursor.getCount() == 0) {
                db.close();
                return exerciseList;
            }

            dayCursor.moveToFirst();
            int dayId = dayCursor.getInt(0);
            dayCursor.close();

            List<DayExerciseConnector> dayExerciseConnectorList = getDayExerciseConnectorByDay(dayId);

            for(DayExerciseConnector dayExerciseConnector : dayExerciseConnectorList) {
                exerciseList.add(getExercise(dayExerciseConnector.getExerciseId()));
            }
            return exerciseList;
        }

    public Muscle getMuscle(int muscleId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + Constants.COLUMN_MUSCLE_ID + ", " + Constants.COLUMN_MUSCLE_NAME + ", " + Constants.COLUMN_MUSCLE_ICON +
                " FROM " + Constants.TABLE_MUSCLE + " WHERE " + Constants.COLUMN_MUSCLE_ID + "=" + muscleId, null);

        if(cursor.getCount() != 0)
            cursor.moveToFirst();

        Muscle muscle = new Muscle();

        if(cursor.getCount() != 0)
        {
            muscle.setMuscleId(cursor.getInt(0));
            muscle.setMuscleName(cursor.getString(1));
            muscle.setMuscleIcon(cursor.getInt(2));
        }

        db.close();
        return muscle;
    }

    /**
     * Returns list of muscles that are used in a current day <br/>
     * First, checks {@code Constants.TABLE_DAY} for a dayId. <br/>
     * Using that Id, gets all exercises from {@code Constants.TABLE_DAY_EXERCISE_CONNECTOR} with {@code DayId == dayId} <br/>
     * Finally, fetches all Muscle instances from {@code Constants.TABLE_MUSCLE_EXERCISE_CONNECTOR} and returns them
     * @return a List of Muscle
     */
        public List<Muscle> getMusclesByCurrentDay() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Muscle> muscleList = new ArrayList<>();

            //  Gets the current day and parses it into day name
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
        String date = dateFormat.format(currentDate);

            //  Fetches the day from Database using current dayName
        Cursor dayCursor = db.query(Constants.TABLE_DAY,
                new String[]{Constants.COLUMN_DAY_ID,
                        Constants.COLUMN_DAY_NAME},
                Constants.COLUMN_DAY_NAME + "='" + date +"'",
                null, null, null, null);

        if(dayCursor.getCount() == 0){
            db.close();
            return muscleList;
        }

        dayCursor.moveToFirst();
        int dayId = dayCursor.getInt(0);

        List<DayExerciseConnector> dayExerciseConnectorList = getDayExerciseConnectorByDay(dayId);

        if(dayExerciseConnectorList.size() == 0){
            db.close();
            return muscleList;
        }

        db = this.getReadableDatabase();

        /**returns FALSE if exercise exists.
         * Default TRUE*/
        Boolean breakCode;
        List<Integer> muscleIdList = new ArrayList<>();
        /**
         * For each dayExerciseConnectorList fetches muscles connected to that day.
         * When muscleIdList is empty, adds the first one
         * Then, checks if this muscle already exists and adds to muscleIdList if not
         */
        for(int i = 0; i < dayExerciseConnectorList.size(); i++) {
            List<MuscleExerciseConnector> tempMuscleExerciseConnectorList = getMuscleExerciseConnectorByExercise(dayExerciseConnectorList.get(i).getExerciseId());
            db = this.getReadableDatabase();
            for(MuscleExerciseConnector tempMuscleExerciseConnector : tempMuscleExerciseConnectorList)
            {
                breakCode = Boolean.TRUE;
                for(int n = 0; n < muscleIdList.size(); n++)
                {
                    if(muscleIdList.get(n) == tempMuscleExerciseConnector.getMuscleId()){
                        breakCode = Boolean.FALSE;
                        break;
                    }
                }
                if(breakCode)
                {
                    muscleIdList.add(tempMuscleExerciseConnector.getMuscleId());
                }
            }
        }

        for(int i = 0; i < muscleIdList.size(); i++)
        {
            muscleList.add(getMuscle(muscleIdList.get(i)));
        }
        db.close();
        return muscleList;
    }

        public List<Muscle> getMusclesByExerciseDate(String date) {
            SQLiteDatabase db = this.getReadableDatabase();
            List<Done> doneListByDate = getDonesByDate(date);

            /**returns FALSE if exercise exists.
             * Default TRUE*/
            Boolean breakCode;
            List<Integer> muscleIdList = new ArrayList<>();
            for(int i = 0; i < doneListByDate.size(); i++) {
                List<MuscleExerciseConnector> tempMuscleExerciseConnectorList = getMuscleExerciseConnectorByExercise(doneListByDate.get(i).getExerciseId());
                for(MuscleExerciseConnector tempMuscleExerciseConnector : tempMuscleExerciseConnectorList) {
                    breakCode = Boolean.TRUE;
                    for(int n = 0; n < muscleIdList.size(); n++) {
                        if(muscleIdList.get(n) == tempMuscleExerciseConnector.getMuscleId()){
                            breakCode = Boolean.FALSE;
                            break;
                        }
                    }
                    if(breakCode) {
                        muscleIdList.add(tempMuscleExerciseConnector.getMuscleId());
                    }
                }
            }

            List<Muscle> muscleList = new ArrayList<>();
            for(int i = 0; i < muscleIdList.size(); i++) {
                muscleList.add(getMuscle(muscleIdList.get(i)));
            }
            db.close();
            return muscleList;
        }

        public List<Muscle> getMusclesByExerciseId(int exerciseId) {
            SQLiteDatabase db = this.getReadableDatabase();
            List<MuscleExerciseConnector> muscleExerciseConnectorList;
            List<Muscle> muscleList = new ArrayList<>();

            muscleExerciseConnectorList = getMuscleExerciseConnectorByExercise(exerciseId);

            if(!db.isOpen())
                db = this.getReadableDatabase();

            for(MuscleExerciseConnector muscleExerciseConnector : muscleExerciseConnectorList) {
                muscleList.add(getMuscle(muscleExerciseConnector.getMuscleId()));
            }
            db.close();
            return muscleList;
        }

    public List<MuscleExerciseConnector> getMuscleExerciseConnectorByMuscle(int muscleId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + Constants.COLUMN_MUSCLE_EXERCISE_CONNECTOR_ID + ", " + Constants.COLUMN_MUSCLE_ID + ", " + Constants.COLUMN_EXERCISE_ID +
                " FROM " + Constants.TABLE_MUSCLE_EXERCISE_CONNECTOR + " WHERE " + Constants.COLUMN_MUSCLE_ID + "=" + muscleId, null);

        List<MuscleExerciseConnector> muscleExerciseConnectorList  = new ArrayList<>();

        if(cursor.getCount() != 0)
        {
            cursor.moveToFirst();
            do {
                muscleExerciseConnectorList.add(new MuscleExerciseConnector(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2)));
            }
            while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return muscleExerciseConnectorList;
    }

    public List<MuscleExerciseConnector> getMuscleExerciseConnectorByExercise(int exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + Constants.COLUMN_MUSCLE_EXERCISE_CONNECTOR_ID + ", " + Constants.COLUMN_MUSCLE_ID + ", " + Constants.COLUMN_EXERCISE_ID +
                " FROM " + Constants.TABLE_MUSCLE_EXERCISE_CONNECTOR + " WHERE " + Constants.COLUMN_EXERCISE_ID + "=" + exerciseId, null);

        List<MuscleExerciseConnector> muscleExerciseConnectorList  = new ArrayList<>();

        if(cursor.getCount() != 0) {
            cursor.moveToFirst();
            do {
                muscleExerciseConnectorList.add(new MuscleExerciseConnector(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2)));
            }
            while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return muscleExerciseConnectorList;
    }

        /**
         * Fetches the muscleExerciseConnector that has muscleId and exerciseId provided
         * @param muscleId
         * @param exerciseId
         * @return if connector exists, returns muscleExerciseConnector. Else, returns an empty muscleExerciseConnector
         */
        public MuscleExerciseConnector getMuscleExerciseConnector(int muscleId, int exerciseId) {
            SQLiteDatabase DB = this.getReadableDatabase();

            List<MuscleExerciseConnector> muscleExerciseConnectorList = new ArrayList<>();
            Cursor cursor = DB.rawQuery("SELECT " + Constants.COLUMN_MUSCLE_EXERCISE_CONNECTOR_ID + ", " + Constants.COLUMN_MUSCLE_ID + ", " + Constants.COLUMN_EXERCISE_ID +
                    " FROM " + Constants.TABLE_MUSCLE_EXERCISE_CONNECTOR + " WHERE " + Constants.COLUMN_EXERCISE_ID + " = " + exerciseId, null);

            if(cursor.getCount() != 0) {
                cursor.moveToFirst();
                do {
                    muscleExerciseConnectorList.add(new MuscleExerciseConnector(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2)));
                }
                while(cursor.moveToNext());
            }
            cursor.close();

            for(MuscleExerciseConnector muscleExerciseConnector : muscleExerciseConnectorList) {
                if(muscleExerciseConnector.getMuscleId() == muscleId) {
                    DB.close();
                    return muscleExerciseConnector;
                }
            }
            return new MuscleExerciseConnector();
        }

    public List<DayExerciseConnector> getDayExerciseConnectorByDay(int dayId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + Constants.COLUMN_DAY_EXERCISE_CONNECTOR_ID + ", " + Constants.COLUMN_DAY_ID + ", " + Constants.COLUMN_EXERCISE_ID + ", " +
                Constants.COLUMN_DAY_EXERCISE_CONNECTOR_POSITION + " FROM " + Constants.TABLE_DAY_EXERCISE_CONNECTOR + " WHERE " + Constants.COLUMN_DAY_ID + "=" + dayId, null);

        List<DayExerciseConnector> dayExerciseConnectorList = new ArrayList<>();

        if(cursor.getCount() != 0) {
            cursor.moveToFirst();
            do {
                dayExerciseConnectorList.add(new DayExerciseConnector(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3)));
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        db.close();

        //insertion sort
        for(int i = 0; i < dayExerciseConnectorList.size(); i++) {
            DayExerciseConnector current = dayExerciseConnectorList.get(i);
            int j = i - 1;
            while(j >= 0 && current.getPosition() < dayExerciseConnectorList.get(j).getPosition()) {
                dayExerciseConnectorList.set(j + 1, dayExerciseConnectorList.get(j));
                j--;
            }
            dayExerciseConnectorList.set(j + 1, current);
        }

        return dayExerciseConnectorList;
    }

    public List<DayExerciseConnector> getDayExerciseConnectorByExercise(int exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + Constants.COLUMN_DAY_EXERCISE_CONNECTOR_ID + ", " + Constants.COLUMN_DAY_ID + ", " + Constants.COLUMN_EXERCISE_ID + ", " +
                Constants.COLUMN_DAY_EXERCISE_CONNECTOR_POSITION + " FROM " + Constants.TABLE_DAY_EXERCISE_CONNECTOR + " WHERE " + Constants.COLUMN_EXERCISE_ID + "=" + exerciseId, null);

        List<DayExerciseConnector> dayExerciseConnectorList = new ArrayList<>();

        if(cursor.getCount() != 0) {
            cursor.moveToFirst();
            do {
                dayExerciseConnectorList.add(new DayExerciseConnector(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3)));
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        db.close();

        //insertion sort
        for(int i = 0; i < dayExerciseConnectorList.size(); i++) {
            DayExerciseConnector current = dayExerciseConnectorList.get(i);
            int j = i - 1;
            while(j >= 0 && current.getPosition() < dayExerciseConnectorList.get(j).getPosition()) {
                dayExerciseConnectorList.set(j + 1, dayExerciseConnectorList.get(j));
                j--;
            }
            dayExerciseConnectorList.set(j + 1, current);
        }

        return dayExerciseConnectorList;
    }

        /**
         * Fetches the dayExerciseConnector that has muscleId and exerciseId provided
         * @param dayId
         * @param exerciseId
         * @return if connector exists, returns dayExerciseConnector. Else, returns an empty dayExerciseConnector
         */
        public DayExerciseConnector getDayExerciseConnector(int dayId, int exerciseId) {
            SQLiteDatabase DB = this.getReadableDatabase();

            List<DayExerciseConnector> dayExerciseConnectorList = new ArrayList<>();
            Cursor cursor = DB.rawQuery("SELECT " + Constants.COLUMN_DAY_EXERCISE_CONNECTOR_ID + ", " + Constants.COLUMN_DAY_ID + ", " + Constants.COLUMN_EXERCISE_ID + ", " +
                    Constants.COLUMN_DAY_EXERCISE_CONNECTOR_POSITION + " FROM " + Constants.TABLE_DAY_EXERCISE_CONNECTOR + " WHERE " + Constants.COLUMN_DAY_ID + " = " + dayId, null);

            if(cursor.getCount() != 0) {
                cursor.moveToFirst();
                do {
                    dayExerciseConnectorList.add(new DayExerciseConnector(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3)));
                }
                while(cursor.moveToNext());
            }
            cursor.close();

            for(DayExerciseConnector dayExerciseConnector : dayExerciseConnectorList) {
                if(dayExerciseConnector.getExerciseId() == exerciseId) {
                    DB.close();
                    return dayExerciseConnector;
                }
            }
            return new DayExerciseConnector();
        }

    public Note getNote(int noteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_NOTE,
                new String[]{Constants.COLUMN_NOTE_ID,
                        Constants.COLUMN_DATE,
                        Constants.COLUMN_NOTE},
                Constants.COLUMN_NOTE_ID + "=" + noteId,
                null, null, null, null);

        if(cursor.getCount() != 0) {
            cursor.moveToFirst();
        }
        Note note = new Note();

        if(cursor.getCount() != 0) {
            note.setNoteId(cursor.getInt(0));
            note.setDate(cursor.getString(1));
            note.setNote(cursor.getString(2));
        }
        cursor.close();
        db.close();
        return note;
    }

        //  get all items

    public List<Day> getAllDays() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Day> dayList = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Constants.TABLE_DAY, null);

        if(cursor.moveToFirst()) {
            do {
                dayList.add(new Day(cursor.getInt(0), cursor.getString(1), cursor.getInt(2) == 1));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return dayList;
    }

    public List<Done> getAllDones() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Done> doneList = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Constants.TABLE_DONE, null);

        if(cursor.moveToFirst()) {
            do {
                Boolean negative = cursor.getInt(5) == 1 ? Boolean.TRUE : Boolean.FALSE;
                Boolean canMore = cursor.getInt(6) == 1 ? Boolean.TRUE : Boolean.FALSE;
                doneList.add(new Done(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), negative, canMore));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return doneList;
    }

    public List<Exercise> getAllExercises() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Exercise> exerciseList = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Constants.TABLE_EXERCISE, null);

        if(cursor.moveToFirst()) {
            Boolean customExercise, timeAsAmount, defaultNegative;
            do {
                customExercise = cursor.getInt(2) == 1;
                timeAsAmount = cursor.getInt(3) == 1;
                defaultNegative = cursor.getInt(4) == 1;
                exerciseList.add(new Exercise(cursor.getInt(0), cursor.getString(1), customExercise, timeAsAmount, defaultNegative));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return exerciseList;
    }

    public List<Muscle> getAllMuscles() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Muscle> muscleList = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Constants.TABLE_MUSCLE, null);

        if(cursor.moveToFirst()) {
            do {
                muscleList.add(new Muscle(cursor.getInt(0), cursor.getString(1), cursor.getInt(2)));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return muscleList;
    }

    public List<MuscleExerciseConnector> getAllMuscleExerciseConnectors() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<MuscleExerciseConnector> muscleExerciseConnectorList = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Constants.TABLE_MUSCLE_EXERCISE_CONNECTOR, null);

        if(cursor.moveToFirst()) {
            do {
                muscleExerciseConnectorList.add(new MuscleExerciseConnector(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2)));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return muscleExerciseConnectorList;
    }

    public List<DayExerciseConnector> getAllDayExerciseConnectors() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Constants.TABLE_DAY_EXERCISE_CONNECTOR, null);

        List<DayExerciseConnector> dayExerciseConnectorList = new ArrayList<>();

        if(cursor.moveToFirst()) {
            do {
                dayExerciseConnectorList.add(new DayExerciseConnector(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3)));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return dayExerciseConnectorList;
    }

    public List<Note> getAllNotes() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Constants.TABLE_NOTE, null);

        List<Note> noteList = new ArrayList<>();
        if(cursor.moveToFirst()) {
            do {
                noteList.add(new Note(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return noteList;
    }

    /**
     * @param date String containing the date for which quantityAndRepsList should be returned.
     *             If not set, method calculates list for a current day
     * @return list of exercises, their quantity and repetitions
     */
    public List<QuantityAndReps> getQuantityAndRepsList(@Nullable String date) {
        if(date == null) {
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            date = dateFormat.format(currentDate);
        }
        List<Done> doneListByDate = getDonesByDate(date);
        List<QuantityAndReps> quantityAndRepsList = new ArrayList<>();
        for(Done done : doneListByDate) {
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
                quantityAndRepsList.add(new QuantityAndReps(done.getExerciseId(), getExercise(done.getExerciseId()).getExerciseName(), done.getQuantity(), done.isCanMore(), 1));
            }
        }
        for(QuantityAndReps quantityAndReps : quantityAndRepsList) {
            quantityAndReps.setQuantity(quantityAndReps.getQuantity() / quantityAndReps.getReps());
        }
        return quantityAndRepsList;
    }

        //  edit items

    public void editExercise(Exercise exercise) {
        SQLiteDatabase DB = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_EXERCISE_NAME, exercise.getExerciseName());
        values.put(Constants.COLUMN_CUSTOM_EXERCISE, exercise.isCustomExercise() ? 1 : 0);
        values.put(Constants.COLUMN_TIME_AS_AMOUNT, exercise.isTimeAsAmount() ? 1 : 0);
        values.put(Constants.COLUMN_DEFAULT_NEGATIVE, exercise.isDefaultNegative() ? 1 : 0);

        DB.update(Constants.TABLE_EXERCISE, values, Constants.COLUMN_EXERCISE_ID + "=" + exercise.getExerciseId(), null);
        DB.close();
    }

    public void editDone(Done done) {
        SQLiteDatabase DB = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_DATE, done.getDate());
        values.put(Constants.COLUMN_EXERCISE_ID, done.getExerciseId());
        values.put(Constants.COLUMN_QUANTITY, done.getQuantity());
        values.put(Constants.COLUMN_TIME, done.getTime());
        int negative = done.isNegative() ? 1 : 0;
        values.put(Constants.COLUMN_NEGATIVE, negative);
        int canMore = done.isCanMore() ? 1 : 0;
        values.put(Constants.COLUMN_CAN_MORE, canMore);

        DB.update(Constants.TABLE_DONE, values, Constants.COLUMN_DONE_ID + "=" + done.getDoneId(), null);
        DB.close();
    }

    public void editDay(Day day) {
        SQLiteDatabase DB = this.getWritableDatabase();

        if(!day.isCustom()) {
            Log.e("DatabaseHandler", "editDay: day is not custom");
            DB.close();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_DAY_NAME, day.getDayName());

        DB.update(Constants.TABLE_DAY, values, Constants.COLUMN_DAY_ID + "=" + day.getDayId(), null);
        DB.close();
    }

    public void editDayExerciseConnector(DayExerciseConnector dayExerciseConnector) {
        SQLiteDatabase DB = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_DAY_ID, dayExerciseConnector.getDayId());
        values.put(Constants.COLUMN_EXERCISE_ID, dayExerciseConnector.getExerciseId());
        values.put(Constants.COLUMN_DAY_EXERCISE_CONNECTOR_POSITION, dayExerciseConnector.getPosition());

        DB.update(Constants.TABLE_DAY_EXERCISE_CONNECTOR, values, Constants.COLUMN_DAY_EXERCISE_CONNECTOR_ID + "=" + dayExerciseConnector.getDayExerciseConnectorId(), null);
        DB.close();
    }

    /**
     * Copies the whole Exercise along with MuscleExerciseConnectors and DayExerciseConnectors. <br/>
     * Creates a new one which replaces the old one
     * @param fromExerciseId exerciseId from which the transfer should be performed
     * @return id of new previous exercise
     */
    public int transferExercise(int fromExerciseId) {
        //  Changes fromExercise into custom
        Exercise exercise = getExercise(fromExerciseId);
        exercise.setCustomExercise(true);
        editExercise(exercise);

        exercise.setCustomExercise(false);
        addExercise(exercise);
        //  Copies MuscleExerciseConnectors
        List<MuscleExerciseConnector> muscleExerciseConnectorList = getMuscleExerciseConnectorByExercise(fromExerciseId);
        for(MuscleExerciseConnector muscleExerciseConnector : muscleExerciseConnectorList) {
            addMuscleExerciseConnector(new MuscleExerciseConnector(muscleExerciseConnector.getMuscleId(), exercise.getExerciseId()));
        }
        return exercise.getExerciseId();
    }




    public void checkExecs() {
        String CREATE_TABLE_MUSCLE = "CREATE TABLE " + Constants.TABLE_MUSCLE + "(" + Constants.COLUMN_MUSCLE_ID + " INTEGER PRIMARY KEY, " +
                Constants.COLUMN_MUSCLE_NAME + " TEXT, " + Constants.COLUMN_MUSCLE_ICON + " INTEGER)";
        String CREATE_TABLE_EXERCISE = "CREATE TABLE " + Constants.TABLE_EXERCISE + "(" + Constants.COLUMN_EXERCISE_ID + " INTEGER PRIMARY KEY, " +
                Constants.COLUMN_EXERCISE_NAME + " TEXT, " + Constants.COLUMN_CUSTOM_EXERCISE + " INTEGER, " + Constants.COLUMN_TIME_AS_AMOUNT + " INTEGER, " + Constants.COLUMN_DEFAULT_NEGATIVE + " INTEGER)";
        String CREATE_TABLE_DONE = "CREATE TABLE " + Constants.TABLE_DONE + "(" + Constants.COLUMN_DONE_ID + " INTEGER PRIMARY KEY, " +
                Constants.COLUMN_DATE + " TEXT, " + Constants.COLUMN_EXERCISE_ID + " INTEGER, " + Constants.COLUMN_QUANTITY + " INTEGER, " +
                Constants.COLUMN_TIME + " INTEGER, " + Constants.COLUMN_NEGATIVE + " INTEGER, " + Constants.COLUMN_CAN_MORE + " INTEGER)";
        String CREATE_TABLE_DAY = "CREATE TABLE " + Constants.TABLE_DAY + "(" + Constants.COLUMN_DAY_ID + " INTEGER PRIMARY KEY, " + Constants.COLUMN_DAY_NAME + " TEXT)";
        String CREATE_TABLE_MUSCLE_EXERCISE_CONNECTOR = "CREATE TABLE " + Constants.TABLE_MUSCLE_EXERCISE_CONNECTOR + "(" + Constants.COLUMN_MUSCLE_EXERCISE_CONNECTOR_ID
                + " INTEGER PRIMARY KEY, " + Constants.COLUMN_MUSCLE_ID + " INTEGER, " + Constants.COLUMN_EXERCISE_ID + " INTEGER)";
        String CREATE_TABLE_DAY_EXERCISE_CONNECTOR = "CREATE TABLE " + Constants.TABLE_DAY_EXERCISE_CONNECTOR + "(" + Constants.COLUMN_DAY_EXERCISE_CONNECTOR_ID
                + " INTEGER PRIMARY KEY, " + Constants.COLUMN_DAY_ID + " INTEGER, " + Constants.COLUMN_EXERCISE_ID + " INTEGER, " + Constants.COLUMN_DAY_EXERCISE_CONNECTOR_POSITION + " INTEGER)";
        String CREATE_TABLE_NOTE = "CREATE TABLE " + Constants.TABLE_NOTE + "(" + Constants.COLUMN_NOTE_ID + " INTEGER PRIMARY KEY, " + Constants.COLUMN_DATE + " TEXT, " + Constants.COLUMN_NOTE + " TEXT)";


        Log.d("CREATE MUSCLE", CREATE_TABLE_MUSCLE);
        Log.d("CREATE EXERCISE", CREATE_TABLE_EXERCISE);
        Log.d("CREATE DONE", CREATE_TABLE_DONE);
        Log.d("CREATE DAY", CREATE_TABLE_DAY);
        Log.d("CREATE MUSCLE_CONNECTOR", CREATE_TABLE_MUSCLE_EXERCISE_CONNECTOR);
        Log.d("CREATE DAY_CONNECTOR", CREATE_TABLE_DAY_EXERCISE_CONNECTOR);
        Log.d("CREATE NOTE", CREATE_TABLE_NOTE);
    }

}
















