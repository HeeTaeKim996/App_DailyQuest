package com.example.dailyquest;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;



public class SwapableItemsContainer extends LinearLayout
{
    private ISwapableItem swapingItem = null;


    public SwapableItemsContainer(Context context)
    { super(context); }

    public SwapableItemsContainer(Context context, @Nullable AttributeSet attrs)
    { super(context, attrs); }

    public SwapableItemsContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr); }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        setClickable(true);
        setFocusable(true);

    }



    public void startSwap(ISwapableItem requester)
    {
        swapingItem = requester;
        swapingItem.changeBackgroundToPicked();


        indicater = new SwapInsertIndicater(getContext());
        indiIndex = indexOfChild((View)swapingItem) + 1;
        addView(indicater, indiIndex);

        invalidate();

        long now = SystemClock.uptimeMillis();
        MotionEvent downEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, 0, 0, 0);


        this.dispatchTouchEvent(downEvent);

        downEvent.recycle();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent)
    {
        if(swapingItem == null) return super.onInterceptTouchEvent(motionEvent);

        return true;
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {
        if(swapingItem == null) return false;

        switch (motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                float yPos = motionEvent.getY();

                int i = 0;
                for(; i < getChildCount(); i++)
                {
                    View view = getChildAt(i);
                    if(view instanceof ISwapableItem == false) continue;

                    int middle = ( view.getTop() + view.getBottom() ) / 2;

                    if(yPos < middle)
                    {
                        break;
                    }
                }

                if(i == 0) break;

                if(i - 1 != indiIndex)
                {
                    if(i - 1 > indiIndex)
                    {
                        removeViewAt(indiIndex);
                        addView(indicater, i - 1);
                        indiIndex = i - 1;
                    }
                    else
                    {
                        removeViewAt(indiIndex);
                        addView(indicater, i);
                        indiIndex = i;
                    }
                }

                break;

            case MotionEvent.ACTION_UP:
                int fromIndex = indexOfChild((View) swapingItem);

                if(fromIndex != indiIndex - 1)
                {
                    int toIndex = indiIndex - 1;

                    View fromItem = (View) swapingItem;
                    View toItem = getChildAt(indiIndex - 1);

                    if(fromIndex < toIndex)
                    {
                        removeViewAt(toIndex);
                        removeViewAt(fromIndex);

                        addView(toItem, fromIndex);
                        addView(fromItem, toIndex);
                    }
                    else
                    {
                        removeViewAt(fromIndex);
                        removeViewAt(toIndex);

                        addView(fromItem, toIndex);
                        addView(toItem, fromIndex);
                    }


                }

                swapingItem.changeBackgroundToNormal();
                swapingItem = null;

                removeView(indicater);
                indicater = null;
                return false;
        }


        return true;
    }





}
