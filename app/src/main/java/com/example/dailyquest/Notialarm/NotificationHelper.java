package com.example.dailyquest.Notialarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import com.example.dailyquest.Data.Fixed.FixedTodo;
import com.example.dailyquest.Data.ParentTodo;
import com.example.dailyquest.Data.SubTodo;
import com.example.dailyquest.Data.Time;
import com.example.dailyquest.Data.Todo;
import com.example.dailyquest.FixedTodo.FixedTodoManager;
import com.example.dailyquest.MainActivity;
import com.example.dailyquest.Notialarm.Receiver.AlarmReceiver;
import com.example.dailyquest.R;
import com.example.dailyquest.Utils.CalenderUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NotificationHelper
{
    private static class AlarmSaveInfo
    {
        AlarmSaveInfo(){}
        AlarmSaveInfo(String InText, byte InAlarmTime)
        {
            text = InText;
            alarmTime = InAlarmTime;
        }
        public String text;
        public byte alarmTime;
    }

    public static void updateTodayNotification(Context context, Time time, ArrayList<Todo> todos,
                                               ArrayList<FixedTodo> fixedTodos, boolean isMidnightCalled)
    {
        // 자정 호출은 백그라운드 호출이므로, FixedTodoManager 를 initialize
        if(isMidnightCalled)
        {
            FixedTodoManager.initialize(context);
        }

        File file = new File(context.getFilesDir() + "/Y/"
                + String.valueOf(time.year) + "/" + String.valueOf(time.month) + "/D/"
                + String.valueOf(time.date) + ".dat");


        // isMidnightCalled인 경우, MidnightAlarmReceiver 에서 준 경우기에, todos 를 찾아본다
        if(isMidnightCalled)    // (isMidnightCAlled == true) == ( t.odo == null)
        {
            if(file.exists())
            {
                todos = new ArrayList<Todo>();
                try(DataInputStream dis = new DataInputStream(new FileInputStream(file)))
                {
                    int todoCount = dis.readInt();
                    while(todoCount-- > 0)
                    {
                        Todo todo = new Todo();
                        todos.add(todo);

                        todo.isCompleted = dis.readBoolean();

                        todo.mainText = dis.readUTF();
                        todo.explainText = dis.readUTF();

                        todo.setAlarmTime(dis.readShort());
                        todo.alarmRepTime = dis.readByte();

                        todo.setColor((int)dis.readByte());

                        int subTodoCount = dis.readInt();
                        while(subTodoCount-- > 0)
                        {
                            SubTodo subTodo = new SubTodo();
                            todo.subTodos.add(subTodo);

                            subTodo.bCompleted = dis.readBoolean();
                            subTodo.subText = dis.readUTF();
                        }
                    }
                }
                catch(IOException e) { e.printStackTrace(); }
            }

            fixedTodos = FixedTodoManager.instance().getDateInfo(time.year, time.month, time.date);
        }

        

        if(
                (todos == null || todos.size() == 0)
            && (fixedTodos == null || fixedTodos.size() == 0)
        )
        {
            cancelNotification(context);
            return;
        }



        String titleText = "";

        String contentText = "";
        if(false)
        {
            contentText = String.format("[%02d-%02d-%02d(%02d:%02d)]  "
                    , time.year % 100, time.month, time.date, time.hour, time.minute);
        }


        List<String> textList = new ArrayList<>();
        final TreeMap<Short, AlarmSaveInfo> alarmMap = new TreeMap<>();


        Consumer<ParentTodo> addressValidTodo
                = (ParentTodo todo)->
        {
            textList.add(todo.getSummary());

            short alarmTime = todo.getAlarmTime();
            if(alarmTime != -1)
            {
                if(time.isFutureTimeFromThis(alarmTime) == false)
                {
                    if(isMidnightCalled)
                    {
                        alarmTime = time.toAlarmTime();  // TodoMidnightReceiver 가 늦게 호출되어, 자정에 호출되는 알람이 생략된 경우이므로, alarmTime 을 변경
                    }
                    else
                    {
                        return;
                    }
                }

                if(alarmMap.containsKey(alarmTime))
                {
                    AlarmSaveInfo saveInfo = alarmMap.get(alarmTime);
                    String curr = saveInfo.text;
                    saveInfo.text = curr + "/" + todo.getSummary();

                    if(todo.alarmRepTime != -1)
                    {
                        if(saveInfo.alarmTime != -1)
                        {
                            saveInfo.alarmTime =
                                    (byte) Math.min(saveInfo.alarmTime, todo.alarmRepTime);
                        }
                        else
                        {
                            saveInfo.alarmTime = todo.alarmRepTime;
                        }
                    }
                }
                else
                {
                    AlarmSaveInfo saveInfo
                            = new AlarmSaveInfo(todo.getSummary(), todo.alarmRepTime);

                    alarmMap.put(alarmTime, saveInfo);
                }
            }
        };

        if(fixedTodos != null)
        {
            for(FixedTodo todo : fixedTodos)
            {
                addressValidTodo.accept(todo);
            }
        }

        for(Todo todo : todos)
        {
            if(todo.isCompleted == false)
            {
                addressValidTodo.accept(todo);
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

            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // FLAG_ACTIVITY_CLEAR_TOP : 서브 화면들이 활성화된 경우, 모두 제거하고 메인 화면으로 돌아옴
            // FLAG_ACTIVITY_SINGLE_TOP : 대상 ACTIVITY 가 이미 존재하는 경우, 액티비티를 새로 생성(onCreate 호출)
            //                            하지 않고, 기존 인스턴스를 재활용 (onNewIntent 호출)한다

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, NotialarmManager.instance().NOTIFICATION_ID,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);



            // 하단의 내용은 알림 내용과 직접적인 연관 내용
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotialarmManager.instance().CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)    // 알림 아이콘 설정
                    .setContentTitle(titleText)              // 알림 제목
                    .setContentText(contentText)                            // 세부 내용
                    .setOngoing(true)   // ※ 밀어서 삭제되는 것을 방지.
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                        // 앞선 IMPORTANCE_LOW 랑 같은 의미로 이해하자
                    .setContentIntent(pendingIntent);

            if(manager != null)
            {
                manager.notify(NotialarmManager.instance().NOTIFICATION_ID, builder.build());
                // 세부 알림내용 등록 ( NOTIFICATION_ID 를 식별자로 사용 )
            }
        }



        // 알람 처리
        if(alarmMap != null && alarmMap.isEmpty() == false)
        {
            Map.Entry<Short, AlarmSaveInfo>[] entries = alarmMap.entrySet().toArray(new Map.Entry[0]);


            File alarmFile = NotialarmManager.instance().getAlarmFile(context);
            File parentDir = alarmFile.getParentFile();
            if(parentDir.exists() == false)
            {
                parentDir.mkdirs();
            }


            try(RandomAccessFile raf = new RandomAccessFile(alarmFile, "rw"))
            {
                raf.setLength(0);

                int dates = CalenderUtils.instance().getDatesFromCalender(
                        time.year, time.month, time.date);

                raf.writeInt(dates);                            // 0-3
                raf.writeLong(0L);                          // 4-11 (더미)

                long offset = raf.getFilePointer();

                // 스택 형식으로 저장 ( 가장 이른 시간 부터 꺼내 쓸 수 있게 )
                for(int i = entries.length - 1; i >= 0; i--)
                {
                    Map.Entry<Short, AlarmSaveInfo> entry = entries[i];
                    raf.writeShort(entry.getKey());

                    AlarmSaveInfo saveInfo = entry.getValue();
                    raf.writeByte(saveInfo.alarmTime);
                    raf.writeUTF(saveInfo.text);

                    raf.writeLong(offset);
                    offset = raf.getFilePointer();
                }

                raf.seek(4);
                raf.writeLong(raf.length());
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }


            AlarmReceiver.postAndScheduleAlarm(context, time);
        }
        else
        {
            cancelAlarm(context);
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

        cancelAlarm(context);
    }

    private static void cancelAlarm(Context context)
    {
        AlarmReceiver.cancelIfExists(context);
    }

}
