package com.example.dailyquest.Notialarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.dailyquest.Utils.CalenderUtils;

import java.util.Calendar;
import java.util.Map;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {

    }


    public static void scheduleAlarm(Context context, Map.Entry<Short, String>[] entries,
                                     int index)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int dates = CalenderUtils.instance().getDatesFromCalender(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DATE));

        // TODO : 머리터지겠다. 내일하자
    }
}
