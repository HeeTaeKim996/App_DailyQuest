package com.example.dailyquest.Data;

import java.util.Calendar;

public class Time
{
    public Time(Calendar calendar)
    {
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        date = calendar.get(Calendar.DATE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
    }
    public boolean isFutureTimeFromThis(short compare)
    {
        return toAlarmTime() < compare;
    }

    public short toAlarmTime()
    {
        return (short) (((short)hour << 6) + (short) minute);
    }

    public int year;
    public int month;
    public int date;
    public int hour;
    public int minute;
}
