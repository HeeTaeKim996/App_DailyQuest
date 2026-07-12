package com.example.dailyquest;

import android.content.Context;
import android.graphics.Color;

public class PixelUtils
{
    private static PixelUtils _instance = new PixelUtils();
    private PixelUtils(){}

    public static PixelUtils instance()
    {
        return _instance;
    }


    public int dpToPixel(Context context, float dp)
    {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    public float pixelToDp(Context context, float px)
    {
        float density = context.getResources().getDisplayMetrics().density;
        return px / density;
    }

}
