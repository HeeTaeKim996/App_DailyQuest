package com.example.dailyquest;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyquest.databinding.ItemDateTodoListBinding;

public class CalenderAdapter extends RecyclerView.Adapter<CalenderAdapter.CalenderViewHolder>
{
    private final MainCalender calender;
    private final CalenderUtils.Calender today;
    private final boolean isCurrMonth;

    public CalenderAdapter(MainCalender InCalender)
    {
        calender = InCalender;
        today = CalenderUtils.instance().getTodaybyCalender();
        isCurrMonth = (calender.year == today.year && calender.month == today.month);
    }

    @NonNull
    @Override
    public CalenderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calender_date, parent, false);

        int rowHeight = parent.getHeight() / 6;

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = rowHeight;
        view.setLayoutParams(layoutParams);


        return new CalenderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalenderViewHolder holder, int position)
    {
        Context context = holder.itemView.getContext();

        Date date = calender.getDates().get(position);
        holder.dayText.setText(String.valueOf(date.date));

        if(date.isCurrMonth == false)
        {
            holder.itemView.setBackgroundResource(R.drawable.date_background_not_used);
            ((TextView)holder.itemView.findViewById(R.id.textView_date))
                    .setTextColor(Color.parseColor("#888888"));
        }
        else
        {
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(v->
            {
                show_date_todoListDialog(context, date, position);
            });

            if(isCurrMonth && date.date == today.date)
            {
                holder.itemView.setBackgroundResource(R.drawable.date_background_today);
            }
        }
    }


    private void show_date_todoListDialog(Context context, Date date, int position)
    {
        ItemDateTodoListBinding binding = ItemDateTodoListBinding
                .inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(binding.getRoot()).create();
        dialog.show();

        if(date.todos != null)
        {
            // TODO
        }
        binding.buttonAddTodoButton.setOnClickListener(v->
        {
            // TODO
        });
        binding.textViewMonthDate.setText(String.format("%d월 %d일 (%c)",
                calender.month, date.date, CalenderUtils.instance().INDEX_TO_DAY[position % 7]
                ));
        binding.buttonToBeforeDate.setOnClickListener(v->
        {
            if(position == 0) return;
            Date beforeDate = calender.getDates().get(position - 1);
            if(beforeDate.isCurrMonth == false) return;

            show_date_todoListDialog(context, beforeDate, position - 1);
            dialog.dismiss();
        });
        binding.buttonToNextDate.setOnClickListener(v->
        {
            if(position == getItemCount() - 1) return;
            Date nextDate = calender.getDates().get(position + 1);
            if(nextDate.isCurrMonth == false) return;

            show_date_todoListDialog(context, nextDate, position + 1);
            dialog.dismiss();
        });
    }

    @Override
    public int getItemCount()
    {
        return calender.getDates().size();
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
