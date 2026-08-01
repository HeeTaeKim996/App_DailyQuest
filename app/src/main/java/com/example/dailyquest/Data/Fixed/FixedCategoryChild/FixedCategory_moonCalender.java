package com.example.dailyquest.Data.Fixed.FixedCategoryChild;
import android.icu.util.Calendar;
import android.icu.util.ChineseCalendar;
import android.util.Log;

import com.example.dailyquest.Data.Fixed.FixedCategory;
import com.example.dailyquest.Data.Fixed.FixedCategoryEnum;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class FixedCategory_moonCalender extends FixedCategory
{
    public FixedCategory_moonCalender()
    {
        super(FixedCategoryEnum.MOON_EVERY_YEAR);
    }

    private byte month;
    public int getMonth(){ return (int)month;}
    public void setMonth(int InMonth) { month = (byte) InMonth;}

    private byte date;
    public int getDate(){ return (int)date;}
    public void setDate(int InDate) { date = (byte)InDate; }

    public static class YearMonthDate
    {
        public YearMonthDate(short InYear, byte InMonth, byte InDate)
        {
            year = InYear;
            month = InMonth;
            date = InDate;
        }
        public YearMonthDate(){}
        public short year;
        public byte month;
        public byte date;
    }

    public YearMonthDate moonToSolar(YearMonthDate moon)
    {
        ChineseCalendar moonCalender = new ChineseCalendar();
        moonCalender.clear();

        moonCalender.set(ChineseCalendar.EXTENDED_YEAR, moon.year + 2637);  // 기본 +2637
        moonCalender.set(ChineseCalendar.MONTH, moon.month - 1); // 1월 == 0, 12월 == 11
        moonCalender.set(ChineseCalendar.DAY_OF_MONTH, moon.date);

        long timeInMillis = moonCalender.getTimeInMillis();

        Calendar solar = Calendar.getInstance();
        solar.setTimeInMillis(timeInMillis);

        return new YearMonthDate((short) solar.get(Calendar.YEAR),
                (byte)(solar.get(Calendar.MONTH) + 1),
                (byte)solar.get(Calendar.DAY_OF_MONTH));
    }


    @Override
    public String getSummary()
    {
        return String.format("月%d-%d",  month, date);
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
        int minMonth = quarter * 3 + 1;
        int maxMonth = quarter * 3 + 3;

        YearMonthDate[] solars = new YearMonthDate[]
                {
                        moonToSolar(new YearMonthDate((short) (year - 1), month, date)),
                        moonToSolar(new YearMonthDate((short) year, month, date))
                };

        for(YearMonthDate solar : solars)
        {
            if(solar.year == year && solar.month >= minMonth && solar.month <= maxMonth)
            {
                TreeMap<Byte, ArrayList<Short>> targetFilled
                        = filled.get((solar.month - 1) % 3);
                if(targetFilled.containsKey(solar.date) == false)
                {
                    targetFilled.put(solar.date, new ArrayList<>());
                }
                targetFilled.get(solar.date).add(fixedTodoIndex);
            }
        }



    }









//    public void tempTest()
//    {
//        test(2026, 3, 25);
//        test(2026, 4, 1);
//        test(2024, 4, 1);
//        test(2025, 4, 1);
//        test(2027, 4, 1);
//    }
//    private void test(int year, int month, int date)
//    {
//        YearMonthDate solar = moonToSolar(new YearMonthDate(year, month, date));
//        Log.d("MOON_TO_SOLAR", String.format("%d-%d-%d -> %d-%d-%d",
//                year, month, date,
//                solar.year, solar.month, solar.date));
//    }
}
