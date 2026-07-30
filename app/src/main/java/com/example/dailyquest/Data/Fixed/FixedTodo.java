package com.example.dailyquest.Data.Fixed;


import com.example.dailyquest.Data.Fixed.FixedCategoryChild.FixedCategory_None;
import com.example.dailyquest.Data.ParentTodo;
import com.example.dailyquest.Data.SubTodo;

public class FixedTodo extends ParentTodo
{
    public FixedTodo()
    {
        fixedCategory = new FixedCategory_None(); // None
    }
    private FixedCategory fixedCategory;

    public void setCategory(FixedCategory InCategory) { fixedCategory = InCategory;}
    public FixedCategory getCategory() { return fixedCategory; }
    public FixedCategoryEnum getCategoryEnum() { return fixedCategory.fixedCategoryEnum;}

    @Override
    public String getSummary()
    {
        String ret = mainText;
        if(alarmTime != -1)
        {
            int hour = (int)(alarmTime >> 6);
            int minute = (int)(alarmTime & 0x3F);
            ret += String.format("(%02d:%02d)", hour, minute);
        }

        ret += String.format(" | %s", fixedCategory.getSummary());
        return ret;
    }
}
