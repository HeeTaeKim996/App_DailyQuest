package com.example.dailyquest;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.dailyquest.databinding.YearMonthPickerBinding;

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
        public YearMonthDate(CalenderUtils.Calender InCalender)
        {
            year = InCalender.year;
            month = InCalender.month;
            date = InCalender.date;
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


    private YearMonthDate picked;
    private YearMonthDate originDate;
    private YearMonthDate today;


    private boolean isPickedMonth;
    private boolean isOriginMonth;
    private boolean isTodayMonth;


    private Button toBeforeMonthButton;
    private Button toNextMonthButton;
    private TextView pickedYearText;

    private TextView monthDateDayText;
    private TextView yearMonthText;
    private InterceptGridLayout datesGrid;

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
        pickedYearText = findViewById(R.id.textView_calenderPicker_pickedYear);
        monthDateDayText = findViewById(R.id.textView_calenderPicker_monthDateDay);
        yearMonthText = findViewById(R.id.textView_calenderPicker_yearMonth);
        datesGrid = findViewById(R.id.gridLayout_calenderPicker);
    }

    public void initialize(CalenderPicker.YearMonthDate InPicked)
    {
        year = InPicked.year;
        month = InPicked.month;

        picked = InPicked;              // 얕은 복사로 결과 전달
        originDate = InPicked.clone();       // 클론으로 깊은 복사
        today = new YearMonthDate(CalenderUtils.instance().getTodaybyCalender());

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
            deductMonth();
            updateCalenderByYearMonth();
        });
        toNextMonthButton.setOnClickListener(v->
        {
            addMonth();
            updateCalenderByYearMonth();
        });
        yearMonthText.setOnClickListener(v->
        {
            show_yearMonthPicker(getContext());
        });

        datesGrid.SetSwipeListener(new InterceptGridLayout.OnSwipeListener()
        {
            @Override
            public void OnSwipe(boolean isUp)
            {
                if(isUp)
                {
                    addMonth();
                }
                else
                {
                    deductMonth();
                }
                updateCalenderByYearMonth();
            }
        });


        setPickedText();
        updateCalenderByYearMonth();
    }

    private void addMonth()
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
    }

    private void deductMonth()
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
    }

    private void updateCalenderByYearMonth()
    {
        firstDay = CalenderUtils.instance().getFirstDayFromYearMonth(year, month);
        lastDate = CalenderUtils.instance().getLastDateFromYearMonth(year, month);
        indexLastDate = firstDay + lastDate;

        yearMonthText.setText(String.format("%04d년 %2d월", year, month));

        {
            int currValue = year * 12 + month;

            int pickedValue = picked.year * 12 + picked.month;
            isPickedMonth = currValue == pickedValue;

            int originValue = originDate.year * 12 + originDate.month;
            isOriginMonth = currValue == originValue;

            int todayValue = today.year * 12 + today.month;
            isTodayMonth = currValue == todayValue;
        }




        for(int i = 0; i < firstDay; i++)
        {
            View dateView = datesGrid.getChildAt(i);
            TextView textView = dateView.findViewById(R.id.textView_calenderPickerItem_date);

            textView.setTextColor(Color.TRANSPARENT);
            dateView.setBackgroundResource(R.drawable.date_background);
            setBackgroundColor(dateView.getBackground(), Color.WHITE);
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
            dateView.setBackgroundResource(R.drawable.date_background);
            setBackgroundColor(dateView.getBackground(), Color.WHITE);
        }
    }

    private void updateDateByIndex(int i)
    {
        View dateView = datesGrid.getChildAt(i);
        TextView textView = dateView.findViewById(R.id.textView_calenderPickerItem_date);

        int date = i - firstDay + 1;

        dateView.setBackgroundResource(getBackgroundResourceByDate(date));

        setBackgroundColorByDate(date, dateView.getBackground());

        textView.setTextColor(getTextColorByDate(date));
        textView.setText(String.valueOf(date));
    }


    private int getBackgroundResourceByDate(int date)
    {
        if(isTodayMonth && date == today.date)
        {
            return R.drawable.date_background_today;
        }
        return R.drawable.date_background;
    }

    private void setBackgroundColorByDate(int date, Drawable background)
    {
        if(isPickedMonth && date == picked.date)
        {
            int color = ContextCompat.getColor(getContext(), R.color.light_blue);
            setBackgroundColor(background, color);
            return;
        }

        setBackgroundColor(background, Color.WHITE);
    }
    private void setBackgroundColor(Drawable background, int color)
    {
        if(background instanceof GradientDrawable)
        {
            GradientDrawable shape = (GradientDrawable) background.mutate();
            shape.setColor(color);
        }
    }

    private int getTextColorByDate(int date)
    {
        if(isOriginMonth && date == originDate.date)
        {
            return Color.RED;
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

            int beforeDate = picked.date;
            int beforeIndex = beforeDate + firstDay - 1;
            View beforeView = datesGrid.getChildAt(beforeIndex);

            setBackgroundColor(beforeView.getBackground(), Color.WHITE);
        }


        picked.year = year;
        picked.month = month;
        picked.date = date;


        View currView = datesGrid.getChildAt(i);
        int color = ContextCompat.getColor(getContext(), R.color.light_blue);
        setBackgroundColor(currView.getBackground(), color);

        setPickedText();
    }

    private void setPickedText()
    {
        char c = CalenderUtils.instance().getDayFromYearMonthDate(picked.year, picked.month,
                picked.date);


        pickedYearText.setText(String.format("  %4d년", picked.year));
        monthDateDayText.setText(String.format("%2d월 %2d일 (%c)",
                picked.month, picked.date, c));
    }



    private void show_yearMonthPicker(Context context)
    {
        YearMonthPickerBinding binding = YearMonthPickerBinding.inflate(LayoutInflater
                .from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();
        YearMonthPicker yearMonthPicker = binding.getRoot();
        yearMonthPicker.initialize(new YearMonthPicker.YearMonth(year, month));
        binding.buttonYearMonthPickerCancel.setOnClickListener(v->
        {
            dialog.dismiss();
        });
        binding.buttonYearMonthPickerOk.setOnClickListener(v->
        {
            YearMonthPicker.YearMonth yearMonth = yearMonthPicker.getYearMonth();
            year = yearMonth.year;
            month = yearMonth.month;

            updateCalenderByYearMonth();

            dialog.dismiss();
        });


        dialog.show();
    }
}
