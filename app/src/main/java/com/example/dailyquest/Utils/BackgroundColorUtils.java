package com.example.dailyquest.Utils;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.dailyquest.Data.FixedTodo;
import com.example.dailyquest.R;

public class BackgroundColorUtils
{
    public static int getColorByLight(Context context, int colorIndex)
    {
        switch(colorIndex)
        {
            case 1:
                return ContextCompat.getColor(context, R.color._1_Light);
            case 2:
                return ContextCompat.getColor(context, R.color._2_Light);
            case 3:
                return ContextCompat.getColor(context, R.color._3_Light);
            case 4:
                return ContextCompat.getColor(context, R.color._4_Light);
            case 5:
                return ContextCompat.getColor(context, R.color._5_Light);
            case 6:
                return ContextCompat.getColor(context, R.color._6_Light);
            case 7:
                return ContextCompat.getColor(context, R.color._7_Light);
        }

        return 0;
    }

    public static int getColorByDark(Context context, int colorIndex)
    {
        switch(colorIndex)
        {
            case 1:
                return ContextCompat.getColor(context, R.color._1_Dark);
            case 2:
                return ContextCompat.getColor(context, R.color._2_Dark);
            case 3:
                return ContextCompat.getColor(context, R.color._3_Dark);
            case 4:
                return ContextCompat.getColor(context, R.color._4_Dark);
            case 5:
                return ContextCompat.getColor(context, R.color._5_Dark);
            case 6:
                return ContextCompat.getColor(context, R.color._6_Dark);
            case 7:
                return ContextCompat.getColor(context, R.color._7_Dark);
        }

        return 0;
    }
}
