package com.example.dailyquest.Data;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class Todo extends ParentTodo
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



    public ArrayList<SubTodo> subTodos = new ArrayList<>();

    public String getSummary()
    {
        String ret = mainText;
        if(subTodos.size() > 0)
        {
            String addedText = "";
            for(SubTodo subtodo : subTodos)
            {
                if(subtodo.bCompleted == false)
                {
                    if(addedText.equals(""))
                    {
                        addedText = subtodo.subText;
                    }
                    else
                    {
                        addedText += ("/" + subtodo.subText);
                    }
                }
            }
            if(addedText.equals("") == false)
            {
                ret += ("[" + addedText + "]");
            }
        }
        if(alarmTime != -1)
        {
            int hour = (int)(alarmTime >> 6);
            int minute = (int)(alarmTime & 0x3F);
            ret += String.format("(%02d:%02d)", hour, minute);
        }

        return ret;
    }

}
