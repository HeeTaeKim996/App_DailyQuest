package com.example.dailyquest.Data.Fixed.FixedCategoryChild;

import com.example.dailyquest.Data.Fixed.FixedCategory;
import com.example.dailyquest.Data.Fixed.FixedCategoryEnum;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
}
