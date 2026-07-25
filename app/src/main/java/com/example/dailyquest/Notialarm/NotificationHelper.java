package com.example.dailyquest.Notialarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import com.example.dailyquest.Data.Todo;
import com.example.dailyquest.R;
import com.example.dailyquest.Utils.CalenderUtils;
import com.example.dailyquest.Utils.InformUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NotificationHelper
{
    public static void updateTodayNotification(Context context, ArrayList<Todo> todos)
    {
        if(todos == null || todos.size() == 0)
        {
            cancelNotification(context);
            return;
        }


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        String titleText = "";

        String contentText = "";
        if(false)
        {
            contentText = String.format("[%02d-%02d-%02d(%02d:%02d)]  "
                    , calendar.get(Calendar.YEAR) % 100, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE),
                    calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE));
        }


        List<String> textList = new ArrayList<>();
        TreeMap<Short, String> alarmMap = null;   // textList 는 자주 사용하니 할당. alarmMap은 거의 사용 안하니 할당 안함


        for(Todo todo : todos)
        {
            if(todo.isCompleted == false)
            {
                textList.add(todo.mainText);

                if(todo.getAlarmTime() != -1)
                {
                    short alarmTime = todo.getAlarmTime();
                    if(alarmMap == null)
                    {
                        alarmMap = new TreeMap<>();
                    }

                    if(alarmMap.containsKey(alarmTime))
                    {
                        String curr = alarmMap.get(alarmTime);
                        alarmMap.put(alarmTime, curr + " / " + todo.mainText);
                    }
                    else
                    {
                        alarmMap.put(alarmTime, todo.mainText);
                    }
                }
            }
        }



        if(textList.size() == 0)
        {
            cancelNotification(context);
            return;
        }
        else
        {
            titleText = TextUtils.join(" / ", textList);
        }



        // NotificationManager : 앱이 아닌 OS 에 알람을 띄워주길 요청할 때 사용
        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)  // O == 오레오(안드로이드 8.0 버전). 안드로이드 8.0 부터는 모든 알림이 특정 채널에 속해야 함
        {
            // ※ 하단의 채널 설정의 내용들은, 모두 스마트폰 설정 → 애플리케이션 → DailyQuest → 알림
            // → 알림 카테고리 내에 표시되는 문구들임 (설정에서의 문구들이지, 알림의 내용과는 무관)
            
            NotificationChannel channel = new NotificationChannel(
                    NotialarmManager.instance().CHANNEL_ID,             // 채널 식별자
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
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotialarmManager.instance().CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)    // 알림 아이콘 설정
                    .setContentTitle(titleText)              // 알림 제목
                    .setContentText(contentText)                            // 세부 내용
                    .setOngoing(true)   // ※ 밀어서 삭제되는 것을 방지.
                    .setPriority(NotificationCompat.PRIORITY_LOW);
                        // 앞선 IMPORTANCE_LOW 랑 같은 의미로 이해하자

            if(manager != null)
            {
                manager.notify(NotialarmManager.instance().NOTIFICATION_ID, builder.build());
                // 세부 알림내용 등록 ( NOTIFICATION_ID 를 식별자로 사용 )
            }
        }



        // 알람 처리
        if(alarmMap != null && alarmMap.isEmpty() == false)
        {
            Map.Entry<Short, String>[] entries = alarmMap.entrySet().toArray(new Map.Entry[0]);

            File alarmFile = NotialarmManager.instance().getAlarmFile(context);
            File parentDir = alarmFile.getParentFile();
            if(parentDir.exists() == false)
            {
                parentDir.mkdirs();
            }

            try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(alarmFile)))
            {

                int dates = CalenderUtils.instance().getDatesFromCalender(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DATE));

                dos.writeInt(dates);            // 날짜
                dos.writeInt(0);            // 인덱스
                dos.writeInt(alarmMap.size()); // 크기

                // Alarm 들 데이터 입력
                for(Map.Entry<Short, String> entry : entries)
                {
                    dos.writeShort(entry.getKey());
                    dos.writeUTF(entry.getValue());
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void cancelNotification(Context context)
    {
        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if(manager != null)
        {
            manager.cancel(NotialarmManager.instance().NOTIFICATION_ID);
        }
    }

}
