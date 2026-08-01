package com.example.dailyquest.Data.Fixed.FixedCategoryChild;

import com.example.dailyquest.Data.Fixed.FixedCategory;
import com.example.dailyquest.Data.Fixed.FixedCategoryEnum;
import com.example.dailyquest.Data.Fixed.FixedTodo;
import com.example.dailyquest.Utils.CalenderUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class FixedCategory_everyWeek extends FixedCategory
{
    private byte day = 0;
    public byte getDay(){ return day;}
    public void setDay(byte InDay) { day = InDay;}

    public FixedCategory_everyWeek()
    {
        super(FixedCategoryEnum.EVERY_WEEK);
    }


    @Override
    public String getSummary()
    {
        return String.format("%s", CalenderUtils.instance().INDEX_TO_DAY[day]);
    }

    @Override
    public boolean loadFromDis(DataInputStream dis)
    {
        try
        {
            day = dis.readByte();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean saveToDos(DataOutputStream dos)
    {
        try
        {
            dos.writeByte(day);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void paint(short fixedTodoIndex, int year, int quarter,
                      List<TreeMap<Byte, ArrayList<Short>>> filled)
    {
        for(int i = 0; i < 3; i++)
        {
            TreeMap<Byte, ArrayList<Short>> targetFilled = filled.get(i);

            int month = quarter * 3 + i + 1;
            int lastDate = CalenderUtils.instance().getLastDateFromYearMonth(year, month);

            byte date = (byte)CalenderUtils.instance().getFirstDateFromDay(year, month, day);

            for(; date <= lastDate; date += 7)
            {
                if(targetFilled.containsKey(date) == false)
                {
                    targetFilled.put(date, new ArrayList<>());
                }
                targetFilled.get(date).add(fixedTodoIndex);
            }
        }
    }
}
