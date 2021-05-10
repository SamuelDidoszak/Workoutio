package com.example.workout.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;
import com.example.workout.data.DatabaseHandler;
import com.example.workout.model.Done;
import com.example.workout.model.helper.LinearLayoutManagerHorizontalSwipe;
import com.example.workout.ui.adapter.DoneExercisesRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class DoneExercisesFragment extends Fragment {

    private Context context;
    private DatabaseHandler DB;
    private RecyclerView doneRecyclerView;
    private TextView noExercises;
    private DoneExercisesRecyclerViewAdapter doneExercisesRecyclerViewAdapter;
    private List<Done> doneList;
    private List<Integer> editedDoneList;
    private MutableLiveData<Boolean> fragmentFinished;

    public MutableLiveData<Boolean> getFragmentFinished() {
        if(fragmentFinished == null)
            fragmentFinished = new MutableLiveData<>();
        return fragmentFinished;
    }

    public DoneExercisesRecyclerViewAdapter getDoneExercisesRecyclerViewAdapter() {
        return doneExercisesRecyclerViewAdapter;
    }

    public int getDoneId() {
        return doneExercisesRecyclerViewAdapter.getDoneId();
    }

    public void notifyItemChanged(int position, @Nullable Integer amount, @Nullable Integer time) {
        if(amount != null)
            doneList.get(position).setQuantity(amount);
        if(time != null)
            doneList.get(position).setTime(time);

        doneExercisesRecyclerViewAdapter.addPositionToEditedList(position);
        doneExercisesRecyclerViewAdapter.notifyItemChanged(position);
    }

    public LiveData<Done> getChosenDone() {
        if(doneExercisesRecyclerViewAdapter == null)
            return null;
        return doneExercisesRecyclerViewAdapter.getChosenDone();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_done_exercises, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DB = new DatabaseHandler(context);

        doneRecyclerView = view.findViewById(R.id.done_exercises_fragment_doneRecyclerView);
        doneList = DB.getDonesByDate(null);

        fragmentFinished = new MutableLiveData<>();

        editedDoneList = new ArrayList<>();

        if (doneList.size() == 0) {
            noExercises = view.findViewById(R.id.done_exercises_fragment_noExercisesTextView);
            noExercises.setVisibility(View.VISIBLE);

            noExercises.setOnTouchListener((v, event) -> {
                fragmentFinished.setValue(Boolean.TRUE);
                return false;
            });
        } else {
            doneExercisesRecyclerViewAdapter = new DoneExercisesRecyclerViewAdapter(doneList, context);
            LinearLayoutManagerHorizontalSwipe layoutManager = new LinearLayoutManagerHorizontalSwipe(context, RecyclerView.VERTICAL, false);
            doneRecyclerView.setLayoutManager(layoutManager);
            doneRecyclerView.setAdapter(doneExercisesRecyclerViewAdapter);

            layoutManager.isSwipeHorizontal().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    doneExercisesRecyclerViewAdapter.saveAllChanges();

                    layoutManager.isSwipeHorizontal().removeObserver(this);
                    fragmentFinished.setValue(Boolean.TRUE);
                }
            });
        }
    }
}































