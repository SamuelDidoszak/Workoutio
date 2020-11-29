package com.example.workout.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.example.workout.R;
import com.example.workout.model.Day;
import com.example.workout.model.DayExerciseConnector;
import com.example.workout.model.Done;
import com.example.workout.model.Exercise;
import com.example.workout.model.Muscle;
import com.example.workout.model.MuscleExerciseConnector;
import com.example.workout.model.Note;

import java.util.List;

/**
 * Class made for testing if Database works as intended
 */
public class DatabaseTesting {
    private List<Day> dayList;
    private List<Done> doneList;
    private List<Exercise> exerciseList;
    private List<Muscle> muscleList;
    private List<MuscleExerciseConnector> muscleExerciseConnectorList;
    private List<DayExerciseConnector> dayExerciseConnectorList;
    private List<Note> noteList;

    private DatabaseHandler DB;

    public DatabaseTesting(Context context) {
        DB = new DatabaseHandler(context);
    }

    @SuppressLint("LongLogTag")
    public void testAll() {
        //Day testing
        addDays();
        dayList = DB.getAllDays();
        for(Day day : dayList) {
            Log.d("DB.Day.getAll", day.getDayId() + ", " + day.getDayName());
            Log.d("DB.Day.getSingular", DB.getDay(day.getDayId()).getDayId() + ", " + DB.getDay(day.getDayId()).getDayName());
            DB.removeDay(day.getDayId());
        }
        dayList = DB.getAllDays();
        Log.d("DB.Day", "size: " + dayList.size());

        Log.d("breakPoint", "========================================================================================================================================");

        //Exercise testing
        addExercises();
        exerciseList = DB.getAllExercises();
        for(Exercise exercise : exerciseList) {
            Log.d("DB.Exercise.getAll", exercise.getExerciseId() + ", " + exercise.getExerciseName() + ", " + exercise.isCustomExercise());
            Log.d("DB.Exercise.getSingular", DB.getExercise(exercise.getExerciseId()).getExerciseId() + ", " + DB.getExercise(exercise.getExerciseId()).getExerciseName() + ", " + DB.getExercise(exercise.getExerciseId()).getExerciseName());

            DB.removeExercise(exercise.getExerciseId());
        }
        exerciseList = DB.getAllExercises();
        Log.d("DB.Exercise", "size: " + exerciseList.size());

        Log.d("breakPoint", "========================================================================================================================================");

        //Muscle testing
        addMuscles();
        muscleList = DB.getAllMuscles();
        for(Muscle muscle : muscleList) {
            Log.d("DB.Muscle.getAll", muscle.getMuscleId() + ", " + muscle.getMuscleName());
            Log.d("DB.Muscle.getSingular", DB.getMuscle(muscle.getMuscleId()).getMuscleId() + ", " + DB.getMuscle(muscle.getMuscleId()).getMuscleName());

            DB.removeMuscle(muscle.getMuscleId());
        }
        muscleList = DB.getAllMuscles();
        Log.d("DB.Muscle", "size: " + muscleList.size());

        Log.d("breakPoint", "========================================================================================================================================");

        //Done testing
        addDones();
        doneList = DB.getAllDones();
        for(Done done : doneList) {
            Log.d("DB.Done.getAll", done.getDoneId() + ", " + done.getDate() + ", " + done.getExerciseId()
                    + ", " + done.getTime() + ", " + done.isNegative() + ", " + done.isCanMore());
            Done doneTemp = DB.getDone(done.getDoneId());
            Log.d("DB.Done.getSingular", doneTemp.getDoneId() + ", " + doneTemp.getDate() + ", " + doneTemp.getExerciseId() + ", " +
                    doneTemp.getQuantity() + ", " + doneTemp.getTime() + ", " + doneTemp.isNegative() + ", " + done.isCanMore());
            DB.removeDone(done.getDoneId());
        }
        doneList = DB.getAllDones();
        Log.d("DB.Done", "size: " + doneList.size());

        Log.d("breakPoint", "========================================================================================================================================");

        //MuscleExerciseConnector testing
        addMuscleExerciseConnectors();
        muscleExerciseConnectorList = DB.getAllMuscleExerciseConnectors();
        for(MuscleExerciseConnector muscleExerciseConnector : muscleExerciseConnectorList) {
            Log.d("DB.MuscleExerciseConnector.getAll",  muscleExerciseConnector.getMuscleExerciseConnectorId() + ", "
                    + muscleExerciseConnector.getMuscleId() + ", " + muscleExerciseConnector.getExerciseId());
            List<MuscleExerciseConnector> muscleExerciseConnectorList1 = DB.getMuscleExerciseConnectorByMuscle(muscleExerciseConnector.getMuscleId());
            for(MuscleExerciseConnector muscleExerciseConnector1 : muscleExerciseConnectorList1)
            {
                Log.d("DB.MuscExerCon.getByMuscle", "\t" + muscleExerciseConnector1.getMuscleExerciseConnectorId() + ", "
                        + muscleExerciseConnector1.getMuscleId() + ", " + muscleExerciseConnector1.getExerciseId());
            }
            muscleExerciseConnectorList1 = DB.getMuscleExerciseConnectorByExercise(muscleExerciseConnector.getExerciseId());
            for(MuscleExerciseConnector muscleExerciseConnector1 : muscleExerciseConnectorList1)
            {
                Log.d("DB.MuscExerCon.getByExercise", "\t" + muscleExerciseConnector1.getMuscleExerciseConnectorId() + ", "
                        + muscleExerciseConnector1.getMuscleId() + ", " + muscleExerciseConnector1.getExerciseId());
            }

            DB.removeMuscleExerciseConnectorById(muscleExerciseConnector.getMuscleExerciseConnectorId());
        }
        muscleExerciseConnectorList = DB.getAllMuscleExerciseConnectors();
        Log.d("DB.MuscleExerciseConnector", "size: " + muscleExerciseConnectorList.size());

        Log.d("breakPoint", "========================================================================================================================================");

        //DayExerciseConnector testing
        addDayExerciseConnectors();
        dayExerciseConnectorList = DB.getAllDayExerciseConnectors();
        for(DayExerciseConnector dayExerciseConnector : dayExerciseConnectorList) {
            Log.d("DB.DayExerciseConnector.getAll", dayExerciseConnector.getDayExerciseConnectorId() + ", " + dayExerciseConnector.getDayId() + ", " + dayExerciseConnector.getExerciseId());
            List<DayExerciseConnector> dayExerciseConnectorList1 = DB.getDayExerciseConnectorByDay(dayExerciseConnector.getDayId());
            for(DayExerciseConnector dayExerciseConnector1 : dayExerciseConnectorList1)
            {
                Log.d("DB.DayExerCon.getByDay", "\t" + dayExerciseConnector1.getDayExerciseConnectorId() + ", "
                        + dayExerciseConnector1.getDayId() + ", " + dayExerciseConnector1.getExerciseId());
            }
            dayExerciseConnectorList1 = DB.getDayExerciseConnectorByExercise(dayExerciseConnector.getExerciseId());
            for(DayExerciseConnector dayExerciseConnector1 : dayExerciseConnectorList1)
            {
                Log.d("DB.DayExerCon.getByExercise", "\t" + dayExerciseConnector1.getDayExerciseConnectorId() + ", "
                        + dayExerciseConnector1.getDayId() + ", " + dayExerciseConnector1.getExerciseId());
            }
            DB.removeDayExerciseConnectorById(dayExerciseConnector.getDayExerciseConnectorId());
        }
        dayExerciseConnectorList = DB.getAllDayExerciseConnectors();
        Log.d("DB.DayExerciseConnector", "size: " + dayExerciseConnectorList.size());

        Log.d("breakPoint", "========================================================================================================================================");

        //Note testing
        addNotes();
        noteList = DB.getAllNotes();
        for(Note note : noteList) {
            Log.d("DB.Note.getAll", note.getNoteId() + ", " + note.getDate() + ", " + note.getNote());
            Log.d("DB.Note.getSingularNote", DB.getNote(note.getNoteId()).getNoteId() + ", " +
                    DB.getNote(note.getNoteId()).getDate() + ", " +
                    DB.getNote(note.getNoteId()).getNote());
            DB.removeNote(note.getNoteId());
        }
        noteList = DB.getAllNotes();
        Log.d("DB.Note", "size: " + noteList.size());

    }

    private void addDays() {
        DB.addDay(new Day("Monday"));
        DB.addDay(new Day("Tuesday"));
        DB.addDay(new Day("Wednesday"));
        DB.addDay(new Day("Thursday"));
        DB.addDay(new Day("Friday"));
        DB.addDay(new Day("Saturday"));
        DB.addDay(new Day("Sunday"));
    }

    private void addExercises() {
        DB.addExercise(new Exercise("Pompki bicepsowe"));
        DB.addExercise(new Exercise("Podciagniecia"));
        DB.addExercise(new Exercise("Pompki podwyzszone"));
        DB.addExercise(new Exercise("Pompki diamentowe"));
        DB.addExercise(new Exercise("Pompki szerokie"));    //  5
        DB.addExercise(new Exercise("Podciagniecia kostki"));
        DB.addExercise(new Exercise("Podciagniecia szerokie"));
        DB.addExercise(new Exercise("Podniesienie konczyn TRZYM"));
        DB.addExercise(new Exercise("Mostki"));
        DB.addExercise(new Exercise("Hollow Body"));    //  10
        DB.addExercise(new Exercise("Skladanki"));
        DB.addExercise(new Exercise("Skrety kolanolokcie"));
        DB.addExercise(new Exercise("Nozyce"));
        DB.addExercise(new Exercise("Deska"));
        DB.addExercise(new Exercise("Pompki")); //  15
        DB.addExercise(new Exercise("Przysiad bulgarski"));
        DB.addExercise(new Exercise("Wykroki z wyskokiem"));
        DB.addExercise(new Exercise("Przysiad z wyskokiem"));
        DB.addExercise(new Exercise("Przysiady"));
        DB.addExercise(new Exercise("Krzeselko"));  //  20
    }

    private void addMuscles() {
        DB.addMuscle(new Muscle("biceps", R.drawable.muscle_white_ic_biceps1));
        DB.addMuscle(new Muscle("triceps", R.drawable.muscle_white_ic_arms));
        DB.addMuscle(new Muscle("chest", R.drawable.muscle_white_ic_chest));
        DB.addMuscle(new Muscle("arms", R.drawable.muscle_white_ic_arm));
        DB.addMuscle(new Muscle("back", R.drawable.muscle_white_ic_back));  //  5
        DB.addMuscle(new Muscle("abs", R.drawable.muscle_white_ic_abs));
        DB.addMuscle(new Muscle("legs", R.drawable.muscle_white_ic_leg));
        DB.addMuscle(new Muscle("core", R.drawable.muscle_white_ic_core));
        DB.addMuscle(new Muscle("neck", R.drawable.muscle_white_ic_neck));
        DB.addMuscle(new Muscle("shoulders", R.drawable.muscle_white_ic_shoulders));    //  10
    }

    private void addDones() {
        DB.addCustomDone(new Done("12.07.2020 10:15", 1, 0, 0, Boolean.FALSE, Boolean.FALSE));
        DB.addCustomDone(new Done("12.07.2020 10:16", 2, 10, 105000, Boolean.TRUE, Boolean.FALSE));
        DB.addCustomDone(new Done("12.07.2020 10:18", 3, 8, 60000, Boolean.FALSE, Boolean.FALSE));
        DB.addCustomDone(new Done("12.07.2020 10:20", 4, 0, 0, Boolean.FALSE, Boolean.FALSE));
        DB.addCustomDone(new Done("12.07.2020 10:21", 5, 10, 0, Boolean.FALSE, Boolean.TRUE));
        DB.addCustomDone(new Done("12.07.2020 10:23", 1, 0, 0, Boolean.FALSE, Boolean.FALSE));
        DB.addCustomDone(new Done("12.07.2020 10:24", 2, 8, 92000, Boolean.TRUE, Boolean.TRUE));
        DB.addCustomDone(new Done("12.07.2020 10:27", 3, 8, 35000, Boolean.FALSE, Boolean.FALSE));
        DB.addCustomDone(new Done("12.07.2020 10:29", 4, 0, 0, Boolean.FALSE, Boolean.FALSE));
        DB.addCustomDone(new Done("12.07.2020 10:31", 5, 8, 35000, Boolean.FALSE, Boolean.FALSE));
    }

    private void addMuscleExerciseConnectors() {
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(1, 1));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(2, 1));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(1, 2));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(1, 3));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(2, 3));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(2, 4));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(5, 5));

        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(1, 6));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(1, 7));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(10, 7));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(5, 8));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(8, 9));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(10, 9));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(8, 10))
        ;
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(3, 11));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(6, 12));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(6, 13));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(5, 14));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(6, 15));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(3, 15));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(2, 15));

        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(7, 16));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(7, 17));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(8, 18));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(7, 18));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(7, 19));
        DB.addMuscleExerciseConnector(new MuscleExerciseConnector(7, 20));
    }

    private void addDayExerciseConnectors() {
        DB.addDayExerciseConnector(new DayExerciseConnector(1, 2));
        DB.addDayExerciseConnector(new DayExerciseConnector(1, 6));
        DB.addDayExerciseConnector(new DayExerciseConnector(1, 7));
        DB.addDayExerciseConnector(new DayExerciseConnector(1, 8));
        DB.addDayExerciseConnector(new DayExerciseConnector(1, 9));
        DB.addDayExerciseConnector(new DayExerciseConnector(1, 10));
        DB.addDayExerciseConnector(new DayExerciseConnector(1, 11));
        DB.addDayExerciseConnector(new DayExerciseConnector(1, 12));
        DB.addDayExerciseConnector(new DayExerciseConnector(1, 13));
        DB.addDayExerciseConnector(new DayExerciseConnector(1, 14));

        DB.addDayExerciseConnector(new DayExerciseConnector(2, 1));
        DB.addDayExerciseConnector(new DayExerciseConnector(2, 2));
        DB.addDayExerciseConnector(new DayExerciseConnector(2, 3));
        DB.addDayExerciseConnector(new DayExerciseConnector(2, 4));
        DB.addDayExerciseConnector(new DayExerciseConnector(2, 5));

        DB.addDayExerciseConnector(new DayExerciseConnector(3, 16));
        DB.addDayExerciseConnector(new DayExerciseConnector(3, 17));
        DB.addDayExerciseConnector(new DayExerciseConnector(3, 18));
        DB.addDayExerciseConnector(new DayExerciseConnector(3, 19));
        DB.addDayExerciseConnector(new DayExerciseConnector(3, 20));

        DB.addDayExerciseConnector(new DayExerciseConnector(4, 1));
        DB.addDayExerciseConnector(new DayExerciseConnector(4, 2));
        DB.addDayExerciseConnector(new DayExerciseConnector(4, 3));
        DB.addDayExerciseConnector(new DayExerciseConnector(4, 4));
        DB.addDayExerciseConnector(new DayExerciseConnector(4, 5));

        DB.addDayExerciseConnector(new DayExerciseConnector(5, 2));
        DB.addDayExerciseConnector(new DayExerciseConnector(5, 6));
        DB.addDayExerciseConnector(new DayExerciseConnector(5, 7));
        DB.addDayExerciseConnector(new DayExerciseConnector(5, 8));
        DB.addDayExerciseConnector(new DayExerciseConnector(5, 9));
        DB.addDayExerciseConnector(new DayExerciseConnector(5, 10));
        DB.addDayExerciseConnector(new DayExerciseConnector(5, 11));
        DB.addDayExerciseConnector(new DayExerciseConnector(5, 12));
        DB.addDayExerciseConnector(new DayExerciseConnector(5, 13));
        DB.addDayExerciseConnector(new DayExerciseConnector(5, 14));

        DB.addDayExerciseConnector(new DayExerciseConnector(6, 2));
        DB.addDayExerciseConnector(new DayExerciseConnector(6, 15));
        DB.addDayExerciseConnector(new DayExerciseConnector(6, 1));
        DB.addDayExerciseConnector(new DayExerciseConnector(6, 19));
        DB.addDayExerciseConnector(new DayExerciseConnector(6, 10));
    }

    private void addNotes() {
        DB.addNote(new Note("12.07.2020", "Pierwsze cwiczenie"));
    }

    /**
     * Adds all of the fields
     */
    public void addAll() {
        addDays();
        addDones();
        addExercises();
        addMuscles();
        addMuscleExerciseConnectors();
        addDayExerciseConnectors();
        addNotes();
        dayList = DB.getAllDays();
        doneList = DB.getAllDones();
        exerciseList = DB.getAllExercises();
        muscleList = DB.getAllMuscles();
        muscleExerciseConnectorList = DB.getAllMuscleExerciseConnectors();
        dayExerciseConnectorList = DB.getAllDayExerciseConnectors();
        noteList = DB.getAllNotes();
    }

    /**
     * Removes the database and instantiates it as new
     */
    public void recreateDatabase() {
        DB.deleteDatabase();
        addAll();
    }
}