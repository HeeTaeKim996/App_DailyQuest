package com.example.dailyquest;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.function.Consumer;

public class NotificationHelper
{
    private static final String CHANNEL_ID = "daily_quest_channel";
    private static final int NOTIFICATION_ID = 1001;

    public static void updateTodayNotification(Context context, ArrayList<Todo> todos)
    {
        if(todos == null || todos.size() == 0)
        {
            cancelNotification(context);
            return;
        }

        String contentText = "";
        boolean hasAny = false;

        if(todos.size() > 0)
        {
            Todo todo = todos.get(0);
            if(todo.isCompleted == false)
            {
                contentText += (todo.mainText);
                hasAny = true;
            }
        }
        for(int i = 1; i < todos.size(); i++ )
        {
            Todo todo = todos.get(i);
            if(todo.isCompleted == false)
            {
                contentText += (" / " + todo.mainText);
                hasAny = true;
            }
        }
        if(hasAny == false)
        {
            cancelNotification(context);
            return;
        }


        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "오늘의 할 일", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("오늘 완료되지 않은 할 일의 상태를 표시합니다.");
            if(manager != null)
            {
                manager.createNotificationChannel(channel);
            }



            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background) // TODO : 이미지 바꾸기
                    .setContentTitle("DailyQuest 오늘의 할 일")
                    .setContentText(contentText)
                    .setOngoing(true)   // ※ 밀어서 삭제를 방지.
                    .setPriority(NotificationCompat.PRIORITY_LOW);

            if(manager != null)
            {
                manager.notify(NOTIFICATION_ID, builder.build());
            }
        }
    }

    public static void cancelNotification(Context context)
    {
        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if(manager != null)
        {
            manager.cancel(NOTIFICATION_ID);
        }
    }

}
