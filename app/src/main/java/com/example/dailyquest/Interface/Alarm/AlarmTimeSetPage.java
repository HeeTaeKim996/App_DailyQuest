package com.example.dailyquest.Interface.Alarm;

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
import android.widget.NumberPicker;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.dailyquest.Interface.YearMonthPicker;
import com.example.dailyquest.R;
import com.example.dailyquest.databinding.TodoInfoSetAlarmPageSetAlarmRepTimeBinding;

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
    private Button alarmRepTimeButton;

    private byte alarmRepTime;

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
        alarmRepTimeButton = findViewById(R.id.button_todoInfoSetAlarmPage_setAlarmReptime);
    }

    public void initialize(short alarmTime, byte InAlarmRepTime, AlarmFunc InAlarmFunc)
    {
        alarmFuncListener = InAlarmFunc;
        alarmRepTime = InAlarmRepTime;

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

                alarmFuncListener.accept(AlarmEnum.Ok, outAlarmTime, alarmRepTime);
            }
        });
        cancelButton.setOnClickListener(v->
        {
            if(alarmFuncListener != null)
            {
                alarmFuncListener.accept(AlarmEnum.Cancel, (short)-1, alarmRepTime);
            }
        });

        setBUseAlarmButton.setOnClickListener(v->
        {
            setbUseAlarm(!getBUseAlarm());
        });

        updateAlarmReptimeButtonInterface();
        alarmRepTimeButton.setOnClickListener(v->
        {
            show_setRepTimePage(getContext());
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
            alarmRepTimeButton.setVisibility(INVISIBLE);

            setBUseAlarmButton.setBackgroundColor(Color.GRAY);
            setBUseAlarmButton.setText("X");
        }
        else
        {
            timeMinuteLayout.setVisibility(VISIBLE);
            alarmRepTimeButton.setVisibility(VISIBLE);

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

    private void updateAlarmReptimeButtonInterface()
    {
        if(alarmRepTime == -1)
        {
            alarmRepTimeButton.setText("-");
            alarmRepTimeButton.setBackgroundColor(Color.GRAY);

        }
        else
        {
            alarmRepTimeButton.setText(String.valueOf(alarmRepTime));
            alarmRepTimeButton.setBackgroundColor(Color.BLUE);
        }
    }


    private void show_setRepTimePage(Context context)
    {
        TodoInfoSetAlarmPageSetAlarmRepTimeBinding binding =
                TodoInfoSetAlarmPageSetAlarmRepTimeBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();

        Button okButton = binding.buttonAlarmPageSetSetRepAlarmTimeOkButton;
        Button setBUseButton = binding.buttonAlarmPageSetSetRepAlarmTimeBUseRepButton;
        NumberPicker repPicker = binding.numberPickerRepAlarmTime;

        repPicker.setMinValue(1);
        repPicker.setMaxValue(30);

        Runnable updateInterfaceByBUse = ()->
        {
            if(alarmRepTime == -1)
            {
                setBUseButton.setText("-");
                setBUseButton.setBackgroundColor(Color.GRAY);
                repPicker.setVisibility(INVISIBLE);
            }
            else
            {
                setBUseButton.setText("O");
                setBUseButton.setBackgroundColor(Color.BLUE);
                repPicker.setVisibility(VISIBLE);
                repPicker.setValue(alarmRepTime);
            }
        };

        updateInterfaceByBUse.run();

        setBUseButton.setOnClickListener(v->
        {
            if(alarmRepTime == -1)
            {
                alarmRepTime = 1;

                updateInterfaceByBUse.run();
            }
            else
            {
                alarmRepTime = -1;
                updateInterfaceByBUse.run();
            }
        });

        okButton.setOnClickListener(v->
        {
            if(alarmRepTime != -1)
            {
                alarmRepTime = (byte)repPicker.getValue();
                updateAlarmReptimeButtonInterface();
                dialog.dismiss();
            }
            else
            {
                updateAlarmReptimeButtonInterface();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
