package com.example.workout.util;

public class Constants {
    public static final double APPLICATION_VERSION = 0.555;
        //  Database related constants
    public static final String DATABASE_NAME = "workoutDatabase";
    public static final int DATABASE_VERSION = 1;
        //  table names
    public static final String TABLE_EXERCISE = "exercise";
    public static final String TABLE_MUSCLE = "muscle";
    public static final String TABLE_DAY = "day";
    public static final String TABLE_DONE = "done";
    public static final String TABLE_MUSCLE_EXERCISE_CONNECTOR = "muscleExerciseConnector";
    public static final String TABLE_DAY_EXERCISE_CONNECTOR = "dayExerciseConnector";
    public static final String TABLE_NOTE = "note";
        //  column names
            //  exercise
    public static final String COLUMN_EXERCISE_ID = "exerciseID";
    public static final String COLUMN_EXERCISE_NAME = "exerciseName";
    public static final String COLUMN_CUSTOM_EXERCISE = "customExercise";
    public static final String COLUMN_TIME_AS_AMOUNT = "timeAsAmount";
    public static final String COLUMN_DEFAULT_NEGATIVE = "defaultNegative";
            //  Muscle
    public static final String COLUMN_MUSCLE_ID = "muscleID";
    public static final String COLUMN_MUSCLE_NAME = "muscleName";
    public static final String COLUMN_MUSCLE_ICON = "muscleIcon";
            //  day
    public static final String COLUMN_DAY_ID = "dayID";
    public static final String COLUMN_DAY_NAME = "dayName";
            //  done
    public static final String COLUMN_DONE_ID = "doneID";
    public static final String COLUMN_DATE= "date";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_NEGATIVE = "negative";
    public static final String COLUMN_CAN_MORE = "canMore";
            //  notes
    public static final String COLUMN_NOTE_ID = "noteID";
    public static final String COLUMN_NOTE = "note";
            //  connectors
    public static final String COLUMN_MUSCLE_EXERCISE_CONNECTOR_ID = "MuscleExerciseConnectorID";
    public static final String COLUMN_DAY_EXERCISE_CONNECTOR_ID = "dayExerciseConnectorID";
}
