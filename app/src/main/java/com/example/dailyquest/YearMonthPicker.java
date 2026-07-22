package com.example.dailyquest;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import androidx.annotation.Nullable;

import java.util.function.BiConsumer;

public class YearMonthPicker extends LinearLayout
{
    public static class YearMonth
    {
        public YearMonth(int InYear, int InMonth) { year = InYear; month = InMonth;}
        int year;
        int month;
    }

    private NumberPicker yearPicker;
    private NumberPicker monthPicker;


    public YearMonthPicker(Context context)
    { super(context); }

    public YearMonthPicker(Context context, @Nullable AttributeSet attrs)
    { super(context, attrs); }

    public YearMonthPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr); }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        yearPicker = findViewById(R.id.numberPicker_year);
        monthPicker = findViewById(R.id.numberPicker_month);

        yearPicker.setMinValue(1900);
        yearPicker.setMaxValue(2100);
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
    }

    public void initialize(YearMonth currYearMonth)
    {
        yearPicker.setValue(currYearMonth.year);
        monthPicker.setValue(currYearMonth.month);
    }

    public YearMonthPicker.YearMonth getYearMonth()
    {
        return new YearMonthPicker.YearMonth(yearPicker.getValue(), monthPicker.getValue());
    }





}
