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

    private boolean bReverse = false;
    public boolean isReverse() { return bReverse;}
    public void setReverse(boolean set) { bReverse = set;}


    public FixedCategory_everyMonth()
    {
        super(FixedCategoryEnum.EVERY_MONTH);
    }

    @Override
    public String getSummary()

    {
        if(bReverse)
        {
            return String.format("-%d", date);
        }
        else
        {
            return String.format("%d", date);
        }
    }

    @Override
    public boolean loadFromDis(DataInputStream dis)
    {
        try
        {
            date = dis.readByte();
            bReverse = dis.readBoolean();
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
            dos.writeBoolean(bReverse);
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

            int month = quarter * 3 + 1 + i;
            int lastDate = CalenderUtils.instance().getLastDateFromYearMonth(year, month);

            byte aimDate = bReverse ? (byte) (lastDate - date + 1)
                    : date;

            if(aimDate < 1 || aimDate > lastDate) continue;

            if(targetFilled.containsKey(aimDate) == false)
            {
                targetFilled.put(aimDate, new ArrayList<>());
            }
            targetFilled.get(aimDate).add(fixedTodoIndex);
        }
    }
}
