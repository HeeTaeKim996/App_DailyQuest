package com.example.dailyquest;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class SwapInsertIndicater extends View
{

    public SwapInsertIndicater(Context context)
    { super(context);                       init();}

    public SwapInsertIndicater(Context context, @Nullable AttributeSet attrs)
    { super(context, attrs);                init();}

    public SwapInsertIndicater(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr);  init();}

    private void init()
    {
        setBackgroundColor(Color.RED);

        int heightInDp = 10;
        int heightInPx = (int) (heightInDp * getContext().getResources()
                .getDisplayMetrics().density);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                heightInPx
        );

        setLayoutParams(params);
    }

}
