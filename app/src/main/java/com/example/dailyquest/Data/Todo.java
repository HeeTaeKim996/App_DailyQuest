package com.example.dailyquest.Data;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class Todo extends ParentTodo
{
    private Date parentDate;
    public void setParentDate(Date InParentDate)
    {
        parentDate = InParentDate;
    }
    public Date getParentDate()
    {
        return parentDate;
    }
    // 자바는 순환 참조해도 GC가 수거하니, 안심하고 사용하자.
    // 처음에 weakReference 로 뒀었는데,
    // parentDate 가 함수부에 호출되고 아무도 클래스로 들고 있지 않아,
    // weakReference<Date> parentDate 가 null 이 돼서 오류가 자주 났었다.

    public boolean isCompleted = false;



    public ArrayList<SubTodo> subTodos = new ArrayList<>();

    @Override
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
