package com.example.dailyquest;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.gridlayout.widget.GridLayout;

public class InterceptGridLayout extends GridLayout
{
    private float y;
    public interface OnSwipeListener
    {
        void OnSwipe(boolean isUp);
    }
    public OnSwipeListener swipeListener;
    public void SetSwipeListener(OnSwipeListener InSwipeListener)
    {
        swipeListener = InSwipeListener;
    }

    public InterceptGridLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public InterceptGridLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public InterceptGridLayout(Context context)
    {
        super(context);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent)
    {
        switch(motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                y = motionEvent.getY();
                break;

            case MotionEvent.ACTION_UP:
                float diffY = motionEvent.getY() - y;
                if(Math.abs(diffY) > 200)
                {
                    if(diffY > 0)
                    {
                        if(swipeListener != null)
                        {
                            swipeListener.OnSwipe(false);
                        }
                    }
                    else
                    {
                        if(swipeListener != null)
                        {
                            swipeListener.OnSwipe(true);
                        }

                        return true;
                    }
                }
                break;
        }

        return false;
    }
}
