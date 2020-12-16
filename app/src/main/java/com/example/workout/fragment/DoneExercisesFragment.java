package com.example.workout.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;
import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Done;
import com.example.workout.ui.adapter.DoneExercisesRecyclerViewAdapter;

import java.util.List;

public class DoneExercisesFragment extends Fragment {

    private Context context;
    private DatabaseHandler DB;
    private CardView container;
    private RecyclerView doneRecyclerView;
    private DoneExercisesRecyclerViewAdapter doneExercisesRecyclerViewAdapter;
    private List<Done> doneList;
    private MutableLiveData<Boolean> showDoneExercises;

    public LiveData<Done> getChosenDone() {
        return doneExercisesRecyclerViewAdapter.getChosenDone();
    }
    public LiveData<Boolean> isTimePicker() {
        return doneExercisesRecyclerViewAdapter.isTimePicker();
    }
    public LiveData<Boolean> showDoneExercises() {
        if(showDoneExercises == null)
            showDoneExercises = new MutableLiveData<>();
        return showDoneExercises;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.done_exercises_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DB = new DatabaseHandler(context);

        container = view.findViewById(R.id.done_exercises_fragment_container);
        doneRecyclerView = view.findViewById(R.id.done_exercises_fragment_doneRecyclerView);
        doneList = DB.getDonesByDate(null);

        doneExercisesRecyclerViewAdapter = new DoneExercisesRecyclerViewAdapter(doneList, context);
        doneRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        doneRecyclerView.setAdapter(doneExercisesRecyclerViewAdapter);

        SwipeDetection containerSwipeDetection = new SwipeDetection(context);
        doneRecyclerView.setOnTouchListener((v, event) -> containerSwipeDetection.onTouch(doneRecyclerView, event));
        containerSwipeDetection.getSwipeDirection().observe(getViewLifecycleOwner(), s -> {
            switch(s) {
                case "up":
                case "down":
                    Log.d("TAG", "onViewCreated: jeff");
                    break;
                case "left":
                case "right":
                    Log.d("TAG", "left||right");
//                    showDoneExercises.setValue(Boolean.FALSE);
                    break;
                case "tap":
                    Log.d("TAG", "tap");
                    break;
            }
        });
    }

    class SwipeDetection extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {

        private MutableLiveData<String> swipeDirection;
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
//            swipeDirection.setValue("tap");
            return false;
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
            this.detector = gestureDetector;
            swipeDirection = new MutableLiveData<>();

        }
    }

}

































