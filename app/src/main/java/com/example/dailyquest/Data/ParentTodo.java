package com.example.dailyquest.Data;

public class ParentTodo
{
    public String mainText = "";
    public String explainText = "";


    protected short alarmTime = -1;    // 패딩(5) / 시간(5) / 분(6)
    public short getAlarmTime() { return alarmTime;}
    public void setAlarmTime(short InAlarmTime)
    {
        if((InAlarmTime & ~0x7FF) > 0) // 하위 11비트 외의 값이 1이면, 오류. -1 로 할당
        {
            InAlarmTime = -1;
        }

        alarmTime = InAlarmTime;
    }
    public byte alarmRepTime = -1;

    protected int color = 1; // Color Must Be in 1 ~ 7. setted color in values/colors.xml
    public void setColor(int InColor)
    {
        color = Math.min(7, Math.max(1, InColor));
    }
    public int getColor() { return color; }
}
