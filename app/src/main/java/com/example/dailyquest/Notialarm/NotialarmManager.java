package com.example.dailyquest.Notialarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import java.io.File;

import javax.net.ssl.SSLEngineResult;

public class NotialarmManager
{
    private static NotialarmManager _instance = new NotialarmManager();
    public static NotialarmManager instance()
    {
        // 백그라운드 서비스에서도 사용됨
        if(_instance == null)
        {
            _instance = new NotialarmManager();
        }

        return _instance;
    }

    public final String CHANNEL_ID = "daily_quest_channel";
    public final int NOTIFICATION_ID = 1001; // 1001 말고도 임의의 정수로 가능

    public final String CHANNEL_ID_POST_ALARM = "daily_quest_post_alarm_channel";
    public final int NOTIFICATION_ID_POST_ALARM = 1002;

    public final String PUT_EXTRA_ALARM_TEXT = "ALARM_TEXT";
    public final String PUT_EXTRA_ALARM_REP_TIME = "BYTE_REP_TIME";

    private File baseFile;
    private NotialarmManager()
    {

    }

    public File getBaseFile(Context context)
    {
        return new File(context.getFilesDir(), "alarm");
    }
    public File getAlarmFile(Context context)
    {
        return new File(getBaseFile(context), "alarm.alm");
    }



    public boolean isNotificationActive(Context context, String channelId,
                                        int notificationId)
    {
        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if(manager == null) return false;

        StatusBarNotification[] activeNotifications = manager.getActiveNotifications();
        for(StatusBarNotification notification : activeNotifications)
        {
            if(notification.getId() == notificationId)
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    Notification n = notification.getNotification();
                    if(n != null && TextUtils.equals(n.getChannelId(), channelId))
                    {
                        return true;
                    }
                }
                else
                {
                    return true;
                }
            }
        }

        return false;
    }
}
