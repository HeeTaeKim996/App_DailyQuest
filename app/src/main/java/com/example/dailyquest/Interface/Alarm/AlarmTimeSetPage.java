package com.example.dailyquest.Interface.Alarm;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.dailyquest.Interface.YearMonthPicker;
import com.example.dailyquest.R;

public class AlarmTimeSetPage extends LinearLayout
{
    private boolean bUseAlarm;
    private boolean getBUseAlarm() { return bUseAlarm; }

    private AlarmFunc alarmFuncListener;

    private NumberPicker timePicker;
    private NumberPicker minutePicker;

    private LinearLayout timeMinuteLayout;
    private Button setBUseAlarmButton;

    private Button okButton;
    private Button cancelButton;

    public AlarmTimeSetPage(Context context)
    { super(context); }

    public AlarmTimeSetPage(Context context, @Nullable AttributeSet attrs)
    { super(context, attrs); }

    public AlarmTimeSetPage(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr); }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        timePicker = findViewById(R.id.numberPicker_time);
        minutePicker = findViewById(R.id.numberPicker_minute);

        timePicker.setMinValue(0);
        timePicker.setMaxValue(23);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);

        timeMinuteLayout = findViewById(R.id.linearLayout_todoInfoSetAlarmPage_timeMinute);
        setBUseAlarmButton = findViewById(R.id.button_setAlarm_onOff);
        okButton = findViewById(R.id.button_alarmTimePicker_ok);
        cancelButton = findViewById(R.id.button_alarmTimePicker_cancel);
    }

    public void initialize(short alarmTime, AlarmFunc InAlarmFunc)
    {
        alarmFuncListener = InAlarmFunc;

        if(alarmTime == -1)
        {
            setbUseAlarm(false);
        }
        else
        {
            setbUseAlarm(true);
            int time = (int)(alarmTime >> 6);          // ※ 시간(5), 분(6) 이기에 패딩 5 가 상위 비트에 있으므로, & 0x 배제
            int minute = (int)(alarmTime & 0x3F);

            timePicker.setValue(time);
            minutePicker.setValue(minute);
        }

        okButton.setOnClickListener(v->
        {
            if(alarmFuncListener != null)
            {
                short outAlarmTime = -1;
                if(getBUseAlarm())
                {
                    short hour = (short) timePicker.getValue();
                    short minute = (short) minutePicker.getValue();
                    outAlarmTime = (short) (((short) timePicker.getValue() << 6)
                        +(short) minutePicker.getValue());
                }

                alarmFuncListener.accept(AlarmEnum.Ok, outAlarmTime);
            }
        });
        cancelButton.setOnClickListener(v->
        {
            if(alarmFuncListener != null)
            {
                alarmFuncListener.accept(AlarmEnum.Cancel, (short)-1);
            }
        });

        setBUseAlarmButton.setOnClickListener(v->
        {
            setbUseAlarm(!getBUseAlarm());
        });

    }

    public YearMonthPicker.YearMonth getYearMonth()
    {
        return new YearMonthPicker.YearMonth(timePicker.getValue(), minutePicker.getValue());
    }

    private void setbUseAlarm(boolean b)
    {
        bUseAlarm = b;



        if(b == false)
        {
            timeMinuteLayout.setVisibility(INVISIBLE);

            setBackgroundColorFromView(setBUseAlarmButton, Color.BLACK);
            setBUseAlarmButton.setBackgroundColor(Color.GRAY);
            setBUseAlarmButton.setText("X");
        }
        else
        {
            timeMinuteLayout.setVisibility(VISIBLE);
            setBUseAlarmButton.setBackgroundColor(Color.BLUE);
            setBUseAlarmButton.setText("O");
        }
    }

    private void setBackgroundColorFromView(View view, int color)
    {
        Drawable drawable = view.getBackground();
        if(drawable instanceof GradientDrawable)
        {
            GradientDrawable shape = (GradientDrawable) drawable.mutate();
            shape.setColor(color);
        }
    }


}
