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

public class FixedCategory_everyMonth extends FixedCategory
{
    private byte date = 1;
    public byte getDate() { return date; }
    public void setDate(byte InDate) { date = InDate; }

    public FixedCategory_everyMonth()
    {
        super(FixedCategoryEnum.EVERY_MONTH);
    }

    @Override
    public String getSummary()
    {
        return String.format("FC[EveryMonth(%2d)]", date);
    }

    @Override
    public boolean loadFromDis(DataInputStream dis)
    {
        try
        {
            date = dis.readByte();
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
            dos.writeByte(date);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void paint(FixedTodo fixedTodo, int year, int quarter, List<TreeMap<Byte, ArrayList<FixedTodo>>> filled)
    {
        for(int i = 0; i < 3; i++)
        {
            TreeMap<Byte, ArrayList<FixedTodo>> targetFilled = filled.get(i);

            int month = quarter * 3 + 1 + i;
            int lastDate = CalenderUtils.instance().getLastDateFromYearMonth(year, month);

            if(date > lastDate || date < 1) continue;

            if(targetFilled.containsKey(date) == false)
            {
                targetFilled.put(date, new ArrayList<>());
            }
            targetFilled.get(date).add(fixedTodo);
        }

    }
}
