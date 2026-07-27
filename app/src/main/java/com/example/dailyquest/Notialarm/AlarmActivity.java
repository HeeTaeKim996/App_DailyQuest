package com.example.dailyquest.Notialarm;


import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.dailyquest.R;

import java.io.File;

public class AlarmActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
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
        TextView textView = findViewById(R.id.textView_activityAlarm_text);

        String alarmText = getIntent().getStringExtra
                (NotialarmManager.instance().PUT_EXTRA_ALARM_TEXT);
        if(alarmText != null)
        {
            textView.setText(alarmText);
        }



        // TODO : 반복 알림 생성 ?

        okButton.setOnClickListener(v->
        {
            finish();
            // TODO : 반복 알림 해제 ?
        });
    }
}
