package com.example.dailyquest;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

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

    public int alarmTimes = -1;

    private int color = 1; // Color Must Be in 1 ~ 7. setted color in values/colors.xml
    public void setColor(int InColor)
    {
        color = Math.min(7, Math.max(1, InColor));
    }
    public int getColor() { return color; }


    public ArrayList<SubTodo> subTodos = new ArrayList<>();
}
