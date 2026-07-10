package com.example.dailyquest;

import java.util.ArrayList;

public class MainCalender
{
    private ArrayList<Date> dates;

    public MainCalender(int year, int month)
    {
        int startDay = DateConverter.instance().getFirstDayFromYearMonth(year, month);
        int lastDate = DateConverter.instance().getLastDateFromYearMonth(year, month);
        int maxDate = startDay + lastDate - 1;

        dates = new ArrayList<Date>(42);

        for(int i = 0; i < startDay; i++)
        {
            dates.add(new Date(""));
        }
        for(int i = startDay; i <= maxDate; i++)
        {
            int dateNumber = i - startDay + 1;
            dates.add(new Date(String.valueOf(dateNumber)));
        }
        int firstDate = 1;
        for(int i = maxDate + 1; i < 42; i++)
        {
            dates.add(new Date(String.valueOf(firstDate++)));
        }

        int lastMonthsLastDate = 0;
        if(month == 1)
        {
            lastMonthsLastDate = DateConverter.instance().getLastDateFromYearMonth(year - 1, 12);
        }
        else
        {
            lastMonthsLastDate = DateConverter.instance().getLastDateFromYearMonth(year, month - 1);
        }

        for(int i = startDay - 1; i >= 0; i--)
        {
            dates.get(i).tempString = String.valueOf(lastMonthsLastDate--);
        }

    }

    public ArrayList<Date> getDates()
    {
        return dates;
    }

}
