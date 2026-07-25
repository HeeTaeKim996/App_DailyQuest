package com.example.dailyquest.Notialarm;

public class NotialarmManager
{
    private static NotialarmManager _instance = new NotialarmManager();
    private NotialarmManager(){}

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
}
