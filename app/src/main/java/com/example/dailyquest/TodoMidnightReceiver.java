package com.example.dailyquest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class TodoMidnightReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        updateTodayNotification(context);
        scheduleNextMidnightAlarm(context); // 다음 날에도 알림이 다시 울리도록 갱신
    }

    public static void updateTodayNotification(Context context)
    {
        CalenderUtils.Calender today = CalenderUtils.instance().getTodaybyCalender();


        File file = new File(context.getFilesDir() + "/"
                + String.valueOf(today.year) + "/" + String.valueOf(today.month) + "/D/"
                + String.valueOf(today.date));
        if(file.exists())
        {
            ArrayList<Todo> todos = new ArrayList<Todo>();
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

                    todo.alarmTimes = dis.readInt();
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

                NotificationHelper.updateTodayNotification(context, todos);
            }
            catch(IOException e) { e.printStackTrace(); }
        }
        else
        {
            NotificationHelper.updateTodayNotification(context, null);
        }
    }

    public static void scheduleNextMidnightAlarm(Context context)
    {
        // ※ @@ !! @@
        // 하단의 AlarManager + PendingIntent 로 작동하는 코드는 백그라운드에서 작동하므로,
        // 앱이 실행되지 않을 때에도 작동한다. 따라서 앱의 static 변수는 물론 싱글턴 클래스들도 모두
        // 백그라운드 환경에서는 null 이다.
        // static 변수 및 싱글턴 클래스 사용에 주의해야 하며, 싱글턴 클래스는 생성자에서
        // if(_instance == null) { _instance = new .. } 로 백그라운드에서 메모리를 새로 할당해야 한다


        AlarmManager alarmManager = (AlarmManager) context               // OS 의 알림 서비스인 AlarmManager 를 사용
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TodoMidnightReceiver.class); // 알림이 발동할 때 실행할 클래스를 지정

        PendingIntent pendingIntent = PendingIntent.getBroadcast(   // PendingIntent 는 당장 사용하는 것이 아닌, 미래에 OS 가 사용할 것임을 알려주는 역할
                context,
                0,                                      // 0 : rqeustCode. 서로 다른 PendingIntent를 구분하기 위한 식별자.
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT           // 이미 등록된 알림이 있다면, 기존 Intent 의 데이터를 갱신
                        | PendingIntent.FLAG_IMMUTABLE);        // 안드로이드 12(API 31) 이상 필수 요구사항으로, 외부에서 이 PendingIntent 의 내용 변경을 막음

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());   // 현재 시간을 기준으로 Calender 객체를 생성

        calendar.add(Calendar.DAY_OF_YEAR, 1);               // 날짜를 하루 뒤로 이동
        calendar.set(Calendar.HOUR_OF_DAY, 0);                  // 시간을 0시로 지정
        calendar.set(Calendar.MINUTE, 0);                       // 분을 0분으로 지정
        calendar.set(Calendar.SECOND, 1);                       // 초를 1초 로 지정
                                                                // -> 다음날 0시 0분 1초 에 알림이 작동하도록 설정

        if(alarmManager != null)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)  // M : 마시멜로우(6.0) M 이후부터 배터리 절약 모드 도입으로, 백그라운드에서의 호출 함수가 바뀜
            {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, // RTC(RealTimeClock) : 기기의 실제 시간 기준. WAKEUP : 절전 모드여도, CPU를 강제로 깨운다
                        calendar.getTimeInMillis(), pendingIntent);     // 설정한 시간(calender) 에 설정한 클래스(pendingItent 내 할당) 을 onReceive
            }
            else
            {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }
}
