package com.example.dailyquest;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class CalenderPicker extends LinearLayout
{
    public static class YearMonthDate
    {
        public YearMonthDate(int InYear, int InMonth, int InDate)
        {
            year = InYear;
            month = InMonth;
            date = InDate;
        }
        public YearMonthDate clone()
        {
            return new YearMonthDate(year, month, date);
        }

        public boolean equals(CalenderPicker.YearMonthDate other)
        {
            return year == other.year && month == other.month && date == other.date;
        }


        int year;
        int month;
        int date;
    }

    private int year;
    private int month;

    private int firstDay;
    private int lastDate;
    private int indexLastDate;

    private YearMonthDate today;
    private YearMonthDate picked;



    private boolean isPickedMonth;
    private boolean isTodayMonth;


    private Button toBeforeMonthButton;
    private Button toNextMonthButton;
    private TextView yearText;
    private TextView monthDateDayText;
    private TextView yearMonthText;
    private androidx.gridlayout.widget.GridLayout datesGrid;
    private Button okButton;
    private Button cancelButton;

    public CalenderPicker(Context context)
    { super(context); }

    public CalenderPicker(Context context, @Nullable AttributeSet attrs)
    { super(context, attrs); }

    public CalenderPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr); }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        toBeforeMonthButton = findViewById(R.id.button_calenderPicker_toBeforeMonth);
        toNextMonthButton = findViewById(R.id.button_calenderPicker_toNextMonth);
        yearText = findViewById(R.id.textView_calenderPicker_year);
        monthDateDayText = findViewById(R.id.textView_calenderPicker_monthDateDay);
        yearMonthText = findViewById(R.id.textView_calenderPicker_yearMonth);
        datesGrid = findViewById(R.id.gridLayout_calenderPicker);
        okButton = findViewById(R.id.button_calenderPicker_ok);
        cancelButton = findViewById(R.id.button_calenderPicker_cancel);
    }

    public void initialize(CalenderPicker.YearMonthDate InPicked)
    {
        year = InPicked.year;
        month = InPicked.month;

        picked = InPicked;              // 얕은 복사로 결과 전달
        today = InPicked.clone();       // 클론으로 깊은 복사

        for(int i = 0; i < 42; i++)
        {
            View view = datesGrid.getChildAt(i);

            int index = i;
            view.setOnClickListener(v->
            {
                onDateClicked(index);
            });
        }

        toBeforeMonthButton.setOnClickListener(v->
        {
            if(month == 1)
            {
                year--;
                month = 12;
            }
            else
            {
                month--;
            }
            updateCalenderByYearMonth();
        });
        toNextMonthButton.setOnClickListener(v->
        {
            if(month == 12)
            {
                year++;
                month = 1;
            }
            else
            {
                month++;
            }
            updateCalenderByYearMonth();
        });

        setMonthDateDayText();
        updateCalenderByYearMonth();
    }

    private void updateCalenderByYearMonth()
    {
        firstDay = CalenderUtils.instance().getFirstDayFromYearMonth(year, month);
        lastDate = CalenderUtils.instance().getLastDateFromYearMonth(year, month);
        indexLastDate = firstDay + lastDate;

        yearText.setText(String.format("  %04d년", year));
        yearMonthText.setText(String.format("%04d년 %2d월", year, month));

        {
            int currValue = year * 12 + month;

            int pickedValue = picked.year * 12 + picked.month;
            isPickedMonth = currValue == pickedValue;

            int todayValue = today.year * 12 + today.month;
            isTodayMonth = currValue == todayValue;
        }




        for(int i = 0; i < firstDay; i++)
        {
            View dateView = datesGrid.getChildAt(i);
            TextView textView = dateView.findViewById(R.id.textView_calenderPickerItem_date);
            textView.setTextColor(Color.TRANSPARENT);
        }
        for(int i = firstDay; i < indexLastDate; i++)
        {
            updateDateByIndex(i);
        }
        for(int i = indexLastDate; i < 42; i++)
        {
            View dateView = datesGrid.getChildAt(i);
            TextView textView = dateView.findViewById(R.id.textView_calenderPickerItem_date);
            textView.setTextColor(Color.TRANSPARENT);
        }
    }

    private void updateDateByIndex(int i)
    {
        View dateView = datesGrid.getChildAt(i);
        TextView textView = dateView.findViewById(R.id.textView_calenderPickerItem_date);

        int date = i - firstDay + 1;

        dateView.setBackgroundColor(getBackgroundColor(date));
        textView.setTextColor(getTextColor(date));
        textView.setText(String.valueOf(date));
    }


    private int getBackgroundColor(int date)
    {
        if(isPickedMonth && date == picked.date)
        {
            return ContextCompat.getColor(getContext(), R.color.red_little_transparent);
        }

        return Color.WHITE;
    }
    private int getTextColor(int date)
    {
        if(isTodayMonth && date == today.date)
        {
            return Color.GREEN;
        }
        return Color.BLACK;
    }



    private void onDateClicked(int i)
    {
        if(i < firstDay || i > indexLastDate) return;

        int date = i - firstDay + 1;

        if(picked.year == year && picked.month == month)
        {
            if(picked.date == date) return;

            int beforeIndex = picked.date + firstDay - 1;
            View beforeView = datesGrid.getChildAt(beforeIndex);
            beforeView.setBackgroundColor(Color.WHITE);
        }


        picked.year = year;
        picked.month = month;
        picked.date = date;


        View currView = datesGrid.getChildAt(i);
        currView.setBackgroundColor(ContextCompat.getColor
                (getContext(), R.color.red_little_transparent));

        setMonthDateDayText();
    }

    private void setMonthDateDayText()
    {
        char c = CalenderUtils.instance().getDayFromYearMonthDate(picked.year, picked.month,
                picked.date);

        monthDateDayText.setText(String.format("%2d월 %2d일 (%c)",
                picked.month, picked.date, c));


    }

}
