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
    private int maximumRange = 500 + 1;
    private Context context;
    private PickerSliderAdapter thisAdapter = this;
    private MutableLiveData<Integer> clickedNumber;
    private int exerciseAmount;

    public void setMaximumRange(int maximumRange) {
        this.maximumRange = maximumRange + 1;
    }

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
        View view = LayoutInflater.from(context).inflate(R.layout.row_picker_slider, parent, Boolean.FALSE);
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
        return maximumRange;
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
//    }
}






















