package com.example.dailyquest.Notialarm.Receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.example.dailyquest.Notialarm.NotialarmManager;

import java.util.Calendar;

public class CirculationReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String content = intent.getStringExtra(
                NotialarmManager.instance().PUT_EXTRA_ALARM_TEXT);
        byte repTime = intent.getByteExtra(
                NotialarmManager.instance().PUT_EXTRA_ALARM_REP_TIME, (byte)-1);
        AlarmReceiver.postAlarm(context, content, repTime);
    }

    public static void scheduleAlarm(Context context, String content, byte repTime)
    {
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CirculationReceiver.class);
        intent.putExtra(NotialarmManager.instance().PUT_EXTRA_ALARM_TEXT, content);
        intent.putExtra(NotialarmManager.instance().PUT_EXTRA_ALARM_REP_TIME, repTime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, NotialarmManager.instance().REQUEST_CODE_CIRCULATION_RECEIVER,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, repTime);

        if(alarmManager != null)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
//                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
//                        calendar.getTimeInMillis(), pendingIntent);

                // 제미나이왈 이게 doze 모드 우회하여 정확한 시간에 작동한다 함
                AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                        calendar.getTimeInMillis(), pendingIntent);
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
            }
            else
            {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    public static void cancelIfExists(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService
                (Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CirculationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, NotialarmManager.instance().REQUEST_CODE_CIRCULATION_RECEIVER,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if(alarmManager != null && pendingIntent != null)
        {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}
