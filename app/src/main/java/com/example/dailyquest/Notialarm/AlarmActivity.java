package com.example.dailyquest.Notialarm;


import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dailyquest.Notialarm.Receiver.CirculationReceiver;
import com.example.dailyquest.R;

public class AlarmActivity extends AppCompatActivity
{
    TextView textView;
    TextView repTimeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) // 처음 Activity 가 생성될 때 호출
    {
        super.onCreate(savedInstanceState);

        // 잠금화면 위로 액티비티를 강제로 띄우는 설정 (Android 10 이상)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
        {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        else
        {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        setContentView(R.layout.activity_alarm);

        Button okButton = findViewById(R.id.button_activityAlarm_okButton);
        textView = findViewById(R.id.textView_activityAlarm_text);
        repTimeView = findViewById(R.id.textView_activityAlarm_repTimeText);

        okButton.setOnClickListener(v->
        {
            finish();
            dismissNotification(this);
            CirculationReceiver.cancelIfExists(this);
        });

        update();
    }

    @Override
    protected void onNewIntent(Intent intent)   // Activity 가 존재하는 상태에서, AlarmReceiver에서 다시 postAlarm 할 때 호출
    {
        super.onNewIntent(intent);
        setIntent(intent);

        update();
    }

    private void update()
    {
        String alarmText = getIntent().getStringExtra
                (NotialarmManager.instance().PUT_EXTRA_ALARM_TEXT);
        byte alarmRepTime = getIntent().getByteExtra(
                NotialarmManager.instance().PUT_EXTRA_ALARM_REP_TIME, (byte) -1);

        if(alarmText != null)
        {
            textView.setText(alarmText);
        }
        if(alarmRepTime == -1)
        {
            repTimeView.setText("-");
        }
        else
        {
            repTimeView.setText(String.valueOf(alarmRepTime));
        }
    }

    private void dismissNotification(Context context)
    {
        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if(manager != null)
        {
            manager.cancel(NotialarmManager.instance().NOTIFICATION_ID_POST_ALARM);
        }
    }
}
