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
    }

    public static void updateTodayNotification(Context context)
    {
        CalenderUtils.Calender today = CalenderUtils.instance().getTodaybyCalender();

        File file = new File(StaticValues.rootFile + "/"
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
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TodoMidnightReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);

        if(alarmManager != null)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(), pendingIntent);
            }
            else
            {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        pendingIntent);
            }
        }
    }
}
