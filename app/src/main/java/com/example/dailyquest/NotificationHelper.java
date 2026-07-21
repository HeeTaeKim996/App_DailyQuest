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
    private static final int NOTIFICATION_ID = 1001; // 1001 말고도 임의의 정수로 가능

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


        // NotificationManager : 앱이 아닌 OS 에 알람을 띄워주길 요청할 때 사용
        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)  // O == 오레오(안드로이드 8.0 버전). 안드로이드 8.0 부터는 모든 알림이 특정 채널에 속해야 함
        {
            // ※ 하단의 채널 설정의 내용들은, 모두 스마트폰 설정 → 애플리케이션 → DailyQuest → 알림
            // → 알림 카테고리 내에 표시되는 문구들임 (설정에서의 문구들이지, 알림의 내용과는 무관)
            
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,             // 채널 식별자
                    "오늘의 할 일",      // 설정에서 나오는 알림의 제목
                    NotificationManager.IMPORTANCE_LOW);    // LOW로 설정시, 알림 생성시 소리/진동 없이 알람 등록만 됨
            channel.setDescription("오늘 완료되지 않은 할 일의 상태를 표시합니다.");  // 알림 상세 항목을 터치하면 상세 화면 에서 보여지는 문구
            if(manager != null)
            {
                manager.createNotificationChannel(channel);
                // 구성한 채널 정보를 OS 시스템에 등록. 이미 같은 채널이 존재하면, 새로 생성하지 않고 무시되기에,
                // 매번 호출해도 안전
            }


            // 하단의 내용은 알림 내용과 직접적인 연관 내용
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)    // 알림 아이콘 설정
                    .setContentTitle("DailyQuest 오늘의 할 일")              // 알림 제목
                    .setContentText(contentText)                            // 세부 내용
                    .setOngoing(true)   // ※ 밀어서 삭제되는 것을 방지.
                    .setPriority(NotificationCompat.PRIORITY_LOW);
                        // 앞선 IMPORTANCE_LOW 랑 같은 의미로 이해하자

            if(manager != null)
            {
                manager.notify(NOTIFICATION_ID, builder.build());
                // 세부 알림내용 등록 ( NOTIFICATION_ID 를 식별자로 사용 )
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
