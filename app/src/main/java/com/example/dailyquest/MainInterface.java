package com.example.dailyquest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;

import com.example.dailyquest.databinding.ActivityMainBinding;

public class MainInterface
{
    private ActivityMainBinding mainBinding;
    private MainCalender mainCalender;
    private int year;
    private int month;

    MainInterface(Context context)
    {
        mainBinding = ActivityMainBinding.inflate(LayoutInflater.from(context));

        CalenderUtils.Calender today = CalenderUtils.instance().getTodaybyCalender();
        year = today.year;
        month = today.month;

        mainBinding.recycleCalender.setLayoutManager(
                new GridLayoutManager(context, 7));

        changeMainCalenderByYearMonth();

        mainBinding.buttonToLowerMonth.setOnClickListener(v->
        {
            if(month == 1)
            {
                year--;
                month = 12;
            }
            else
            {
                month--;
            }
            changeMainCalenderByYearMonth();
        });

        mainBinding.buttonToUpperMonth.setOnClickListener(v->
        {
            if(month == 12)
            {
                year++;
                month = 1;
            }
            else
            {
                month++;
            }
            changeMainCalenderByYearMonth();
        });
    }

    public ViewGroup getRootView()
    {
        return mainBinding.getRoot();
    }

    private void changeMainCalenderByYearMonth()
    {
        mainCalender = new MainCalender(year, month);

        CalenderAdapter calenderAdapter = new CalenderAdapter(mainCalender.getDates());
        mainBinding.recycleCalender.setAdapter(calenderAdapter);


        mainBinding.textViewYearMonth.setText(String.format("%4d년 %2d월", year, month));
    }

}
