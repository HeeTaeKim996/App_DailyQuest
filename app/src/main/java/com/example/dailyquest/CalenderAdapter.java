package com.example.dailyquest;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CalenderAdapter extends RecyclerView.Adapter<CalenderAdapter.CalenderViewHolder>
{
    private final ArrayList<Date> dateList;

    public CalenderAdapter(ArrayList<Date> InDateList)
    {
        dateList = InDateList;
    }

    @NonNull
    @Override
    public CalenderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calender_date, parent, false);
        return new CalenderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalenderViewHolder holder, int position)
    {
        Date date = dateList.get(position);
        holder.dayText.setText(String.valueOf(date.date));
        if(date.isCurrMonth == false)
        {
            holder.itemView.setBackgroundResource(R.drawable.date_background_not_used);
        }
    }

    @Override
    public int getItemCount()
    {
        return dateList.size();
    }

    public static class CalenderViewHolder extends RecyclerView.ViewHolder
    {
        TextView dayText;

        public CalenderViewHolder(@NonNull View itemView)
        {
            super(itemView);

            dayText = itemView.findViewById(R.id.textView_date);
        }
    }
}
