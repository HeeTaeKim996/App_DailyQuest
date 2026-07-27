package com.example.dailyquest.Notialarm.Receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.dailyquest.Data.Time;
import com.example.dailyquest.Notialarm.NotialarmManager;
import com.example.dailyquest.Utils.CalenderUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Map;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        postAndScheduleAlarm(context, new Time(calendar));
    }


    public static void postAndScheduleAlarm(Context context, Time time)
    {
        int dates = CalenderUtils.instance().getDatesFromCalender(
                CalenderUtils.instance().getTodaybyCalender());

        File alarmFile = NotialarmManager.instance().getAlarmFile(context);
        try(RandomAccessFile raf = new RandomAccessFile(alarmFile, "rw"))
        {
            if(raf.length() < 12) return; // 기본 0~11 차지하므로, 12 미만이라면 오류

            int fileDate = raf.readInt();       // 0-3
            long length = raf.readLong();       // 4-11

            if(dates != fileDate || length < 12) return;

            raf.seek(length - 8);
            long offset = raf.readLong();

            raf.seek(offset);
            short alarmTime = raf.readShort();

            short currTime = time.toAlarmTime();


            // 다음 알림 예약
            boolean bFinished = false;
            String content = "";
            while(alarmTime <= currTime)
            {
                if(content.equals(""))
                {
                    content = raf.readUTF();
                }
                else
                {
                    String beforeSt = content;
                    content = beforeSt + " / " + raf.readUTF();
                }

                // 해당 알림이 마지막 알림이었으므로, 다음 알림을 예약하지 않는다
                if(offset <= 12)
                {
                    bFinished = true;
                    break;
                }

                length = offset;

                raf.seek(offset - 8);
                offset = raf.readLong();
                raf.seek(offset);
                alarmTime = raf.readShort();
            }

            if(content.equals("") == false)
            {
                // TODO : 알림 POST
            }

            if(bFinished)
            {
                alarmFile.delete();
                return;
            }


            raf.seek(4);
            raf.writeLong(length);
            raf.setLength(length);


            AlarmManager alarmManager = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                            | PendingIntent.FLAG_IMMUTABLE);

            int alarmHour = (int)(alarmTime >> 6);
            int alarmMinute = (int)(alarmTime & 0x3F);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            calendar.set(Calendar.HOUR_OF_DAY, alarmHour);
            calendar.set(Calendar.MINUTE, alarmMinute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if(alarmManager != null)
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(), pendingIntent);
                }
                else
                {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(), pendingIntent);
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
