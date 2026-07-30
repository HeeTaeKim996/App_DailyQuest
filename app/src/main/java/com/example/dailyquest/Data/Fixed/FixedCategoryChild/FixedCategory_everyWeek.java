package com.example.dailyquest.Data.Fixed.FixedCategoryChild;

import com.example.dailyquest.Data.Fixed.FixedCategory;
import com.example.dailyquest.Data.Fixed.FixedCategoryEnum;
import com.example.dailyquest.Utils.CalenderUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
        return String.format("FC[EveryWeek(%s)]", CalenderUtils.instance().INDEX_TO_DAY[day]);
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
}
