package com.example.dailyquest.Data;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class Todo
{
    private WeakReference<Date> parentDate;
    public void setParentDate(Date InParentDate)
    {
        parentDate = new WeakReference<Date>(InParentDate);
    }
    public Date getParentDate()
    {
        return parentDate.get();
    }


    public boolean isCompleted = false;

    public String mainText = "";
    public String explainText = "";

    private short alarmTime = -1;    // 패딩(5) / 시간(5) / 분(6)
    public short getAlarmTime() { return alarmTime;}
    public void setAlarmTime(short InAlarmTime)
    {
        if((InAlarmTime & ~0x7FF) > 0) // 하위 11비트 외의 값이 1이면, 오류. -1 로 할당
        {
            InAlarmTime = -1;
        }

        alarmTime = InAlarmTime;
    }

    private int color = 1; // Color Must Be in 1 ~ 7. setted color in values/colors.xml
    public void setColor(int InColor)
    {
        color = Math.min(7, Math.max(1, InColor));
    }
    public int getColor() { return color; }


    public ArrayList<SubTodo> subTodos = new ArrayList<>();
}
