package com.example.dailyquest;

import java.util.Calendar;

public class CalenderUtils
{
    private static CalenderUtils _instance = new CalenderUtils();
    private CalenderUtils() {}

    private final int _400_years_to_dates = 146097;
    private final int _100_years_to_dates = 36524;
    private final int _4_years_to_dates = 1461;
    private final int _1_year_to_dates = 365;

    private final int[] MonthToSumDays = new int[]
            { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365};
    private final int[] MonthToSumDays_LeapYear = new int []
            { 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366};

    private final int[] MonthToDays = new int[]
            { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    public static class Calender
    {
        public Calender(int InYear, int InMonth, int InDate)
        {
            year = InYear;
            month = InMonth;
            date = InDate;
        }
        public final int year;
        public final int month;
        public final int date;
    }

    public static CalenderUtils instance()
    {
        return _instance;
    }


    public int getDatesFromCalender(int year, int month, int date)
    {
        year--;
        int dates = year * 365
                + year / 4 - year / 100 + year / 400;
        year++;

        if(isLeapYear(year))
        {
            dates += MonthToSumDays_LeapYear[month - 1];
        }
        else
        {
            dates += MonthToSumDays[month - 1];
        }

        dates += date;

        return dates;
    }

    public Calender getCalenderFromDates(int dates)
    {
        int year = 1;

        int _400 = (dates - 1) / _400_years_to_dates;
        year += _400 * 400;
        dates -= _400 * _400_years_to_dates;

        int _100 = (dates - 1) / _100_years_to_dates;
        if(_100 == 4) _100 = 3;
        year += _100 * 100;
        dates -= _100 * _100_years_to_dates;

        int _4 = (dates - 1) / _4_years_to_dates;
        if(_4 == 25) _4 = 24;
        year += _4 * 4;
        dates -= _4 * _4_years_to_dates;

        int _1 = (dates - 1 / _1_year_to_dates);
        if(_1 == 4) _1 = 3;
        year += _1;
        dates -= _1 * _1_year_to_dates;

        int month = 0;
        if(isLeapYear(year))
        {
            for(int i = 1; i <= 12; i++)
            {
                if(dates <= MonthToSumDays_LeapYear[i])
                {
                    month = i;
                    dates -= MonthToSumDays_LeapYear[i - 1];
                    break;
                }
            }
        }
        else
        {
            for(int i = 1; i <= 12; i++)
            {
                if(dates <= MonthToSumDays[i])
                {
                    month = i;
                    dates -= MonthToSumDays[i - 1];
                    break;
                }
            }
        }

        int date = dates;
        return new Calender(year, month, date);
    }

    public boolean isLeapYear(int year)
    {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400) == 0;
    }

    public int getFirstDayFromYearMonth(int year, int month)
    {
        int dates = getDatesFromCalender(year, month, 1);
        return dates % 7;   // 0(일), 1(월)... 6(토)
    }
    public int getLastDateFromYearMonth(int year, int month)
    {
        if(month == 2 && isLeapYear(year))
        {
            return 29;
        }

        return MonthToDays[month - 1];
    }

    public Calender getTodaybyCalender()
    {
        java.util.Calendar c = java.util.Calendar.getInstance();

        int year = c.get(java.util.Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int date = c.get(Calendar.DATE);

        return new Calender(year, month, date);
    }

}
