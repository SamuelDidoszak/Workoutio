package com.example.workout.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;

public class PickerSliderAdapter extends RecyclerView.Adapter<PickerSliderAdapter.ViewHolder> {
    private final int MAXIMUM_RANGE = 50 + 1;
    private Context context;
    private PickerSliderAdapter thisAdapter = this;
    private MutableLiveData<Integer> clickedNumber;
    private int exerciseAmount;

    public void setExerciseAmount(int exerciseAmount) {
        this.exerciseAmount = exerciseAmount;
    }

    public MutableLiveData<Integer> getClickedNumber() {
        return clickedNumber;
    }

    public PickerSliderAdapter(Context context) {
        this.context = context;
        clickedNumber = new MutableLiveData<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.picker_slider_row, parent, Boolean.FALSE);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//              Deletes the item at 0
        holder.cardView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if(position == 0) {
            holder.cardView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }

        holder.numberTextView.setText(Integer.toString(position));
        holder.cardView.setOnClickListener(holder.onNumberClick);
    }

    @Override
    public int getItemCount() {
        return MAXIMUM_RANGE;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView numberTextView;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            numberTextView = itemView.findViewById(R.id.picker_slider_row_numberTextView);
            cardView = itemView.findViewById(R.id.picker_slider_row_cardView);
        }

        public View.OnClickListener onNumberClick = v -> {
            int position = getAdapterPosition();
            clickedNumber.setValue(position);
        };
    }



//    public class SliderLayoutManager extends LinearLayoutManager {
//
//        public SliderLayoutManager(Context context) {
//            super(context);
//        }
//
//        @Override
//        public void onScrollStateChanged(int state) {
//            super.onScrollStateChanged(state);
//            if(state == RecyclerView.SCROLL_STATE_IDLE) {
//                int recyclerViewCenter =
//            }
//        }
//    }
}






















