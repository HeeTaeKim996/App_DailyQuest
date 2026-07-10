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

    MainInterface(Context context)
    {
        mainBinding = ActivityMainBinding.inflate(LayoutInflater.from(context));

        DateConverter.Calender today = DateConverter.instance().getTodaybyCalender();
        mainCalender = new MainCalender(today.year, today.month);
//        mainCalender = new MainCalender(2026, 6);

        mainBinding.recycleCalender.setLayoutManager(
                new GridLayoutManager(context, 7));

        CalenderAdapter calenderAdapter = new CalenderAdapter(mainCalender.getDates());
        mainBinding.recycleCalender.setAdapter(calenderAdapter);
    }

    public ViewGroup getRootView()
    {
        return mainBinding.getRoot();
    }
}
