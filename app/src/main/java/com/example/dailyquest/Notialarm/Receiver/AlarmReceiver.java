package com.example.dailyquest.Notialarm.Receiver;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.collection.CircularArray;
import androidx.core.app.NotificationCompat;

import com.example.dailyquest.Data.Time;
import com.example.dailyquest.Notialarm.AlarmActivity;
import com.example.dailyquest.Notialarm.NotialarmManager;
import com.example.dailyquest.R;
import com.example.dailyquest.Utils.CalenderUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
            byte repTime = -1;

            while(alarmTime <= currTime)
            {
                byte newRepTime = raf.readByte();
                String newContent = raf.readUTF();

                if(newRepTime != -1)
                {
                    if(repTime != -1)
                    {
                        repTime = (byte)Math.min(repTime, newRepTime);
                    }
                    else
                    {
                        repTime = newRepTime;
                    }
                }

                if(content.equals(""))
                {
                    content = newContent;
                }
                else
                {
                    String beforeSt = content;
                    content = beforeSt + " / " + newContent;
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
                postAlarm(context, content, repTime);
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
                    context, NotialarmManager.instance().REQUEST_CODE_ALARM_RECEIVER,
                    intent,
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
//                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
//                            calendar.getTimeInMillis(), pendingIntent);
                    
                    // 제미나이왈 이게 doze 모드 우회하여 정확한 시간에 발동한다 함
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
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void postAlarm(Context context, String content, byte alarmRepTime)
    {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); // 기본 알람 소리 Uri(음원 파일의 위치를 나타내는 식별자) 가져오기
        long[] vibration = new long[] {0, 500, 500, 500}; // { 대기시간, 진동시간, 대기시간, 진동시간 ..}  ==> 0초 대기후 0.5초 진동. 0.5초 대기후 0.5초 진동

        NotificationManager manager = (NotificationManager) context.getSystemService
                (Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(
                    NotialarmManager.instance().CHANNEL_ID_POST_ALARM,
                    "알람", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("알람 용도");

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            channel.setSound(alarmUri, audioAttributes);
            channel.enableVibration(true);
            channel.setVibrationPattern(vibration);

            if(manager != null)
            {
                manager.createNotificationChannel(channel);
            }
        }



        Intent intent = new Intent(context, AlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(NotialarmManager.instance().PUT_EXTRA_ALARM_TEXT, content);
        intent.putExtra(NotialarmManager.instance().PUT_EXTRA_ALARM_REP_TIME, alarmRepTime);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                NotialarmManager.instance().REQUEST_CODE_ALARM_RECEIVER, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                NotialarmManager.instance().CHANNEL_ID_POST_ALARM)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(alarmUri)
                .setVibrate(vibration)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true);

        if(manager != null)
        {
            manager.notify(NotialarmManager.instance().NOTIFICATION_ID_POST_ALARM, builder.build());
        }


        if(alarmRepTime != -1)
        {
            CirculationReceiver.scheduleAlarm(context, content, alarmRepTime);
        }
    }

    public static boolean isAlarmScheduled(Context context)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, NotialarmManager.instance().REQUEST_CODE_ALARM_RECEIVER, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        return pendingIntent != null;
    }

    public static void cancelIfExists(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService
                (Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, NotialarmManager.instance().REQUEST_CODE_ALARM_RECEIVER, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if(alarmManager != null && pendingIntent != null)
        {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

}
