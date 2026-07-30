package com.example.dailyquest.Data.Fixed.FixedCategoryChild;

import com.example.dailyquest.Data.Fixed.FixedCategory;
import com.example.dailyquest.Data.Fixed.FixedCategoryEnum;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

    // TODO : 주의. month(1_12), date(1_31) 할당 가능하니, 없는 일 이 있을 수 있음. 검수하거나, 아님 호환은 되도록 처리 필요
}
