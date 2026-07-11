package com.example.dailyquest;

import java.util.ArrayList;

public class MainCalender
{
    private ArrayList<Date> dates;
    public int year;
    public int month;
    public int maxDate;

    public MainCalender(int InYear, int InMonth)
    {
        year = InYear;
        month = InMonth;


        int startDay = CalenderUtils.instance().getFirstDayFromYearMonth(InYear, InMonth);
        maxDate = CalenderUtils.instance().getLastDateFromYearMonth(InYear, InMonth);
        int indexMaxDate = startDay + maxDate - 1;

        dates = new ArrayList<Date>(42);

        for(int i = 0; i < startDay; i++)
        {
            dates.add(new Date.Builder().setIsCurrMonth(false).create());
        }
        for(int i = startDay; i <= indexMaxDate; i++)
        {
            int dateNumber = i - startDay + 1;
            dates.add(new Date.Builder().setDate(dateNumber).setIsCurrMonth(true).create());
        }
        int firstDate = 1;
        for(int i = indexMaxDate + 1; i < 42; i++)
        {
            dates.add(new Date.Builder().setDate(firstDate++).setIsCurrMonth(false).create());
        }

        int lastMonthsLastDate = 0;
        if(InMonth == 1)
        {
            lastMonthsLastDate = CalenderUtils.instance().getLastDateFromYearMonth(InYear - 1, 12);
        }
        else
        {
            lastMonthsLastDate = CalenderUtils.instance().getLastDateFromYearMonth(InYear, InMonth - 1);
        }

        for(int i = startDay - 1; i >= 0; i--)
        {
            dates.get(i).date = lastMonthsLastDate--;
        }
    }

    public ArrayList<Date> getDates()
    {
        return dates;
    }


    public void inform_dateUpdated(Date date)
    {
        // TODO : SAVE
    }
}
