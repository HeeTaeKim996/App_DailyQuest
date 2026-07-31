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

public class FixedCategory_everyYear extends FixedCategory
{
    public FixedCategory_everyYear()
    {
        super(FixedCategoryEnum.EVERY_YEAR);
    }

    private byte month = 1;
    public byte getMonth() { return month;}
    public void setMonth(byte InMonth) { month = InMonth; }

    private byte date = 1;
    public byte getDate() { return date;}
    public void setDate(byte InDate) { date = InDate; }

    @Override
    public String getSummary()
    {
        return String.format("FC[EveryYear(%2d-%2d)]", month, date);
    }

    @Override
    public boolean loadFromDis(DataInputStream dis)
    {
        try
        {
            month = dis.readByte();
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
            dos.writeByte(month);
            dos.writeByte(date);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void paint(FixedTodo fixedTodo, int year, int quarter, List<TreeMap<Byte, ArrayList<FixedTodo>>> filled)
    {
        int minMonth = quarter * 3 + 1;
        int maxMonth = quarter * 3 + 3;

        if(month < minMonth || month > maxMonth) return;

        int lastDate = CalenderUtils.instance().getLastDateFromYearMonth(year, month);
        if(date > lastDate || date < 1) return;

        TreeMap<Byte, ArrayList<FixedTodo>> targetFilled = filled.get((month - 1) % 3);

        if(targetFilled.containsKey(date) == false)
        {
            targetFilled.put(date, new ArrayList<>());
        }
        targetFilled.get(date).add(fixedTodo);
    }

}
