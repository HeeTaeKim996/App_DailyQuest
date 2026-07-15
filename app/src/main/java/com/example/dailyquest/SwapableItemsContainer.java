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

import java.util.function.BiConsumer;


public class SwapableItemsContainer extends LinearLayout
{
    private View swapingItem = null;
    private int originIndex;
    private ISwapCompleteFunc swapFunc;

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


    public void setSwapCompleteFunc(ISwapCompleteFunc InSwapFunc)
    {
        swapFunc = InSwapFunc;
    }



    public void startSwap(ISwapableItem requester)
    {
        swapingItem = (View) requester;
        requester.changeBackgroundToPicked();

        originIndex = indexOfChild(swapingItem);

        invalidate();

        long now = SystemClock.uptimeMillis();
        MotionEvent downEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, 0, 0, 0);

        this.dispatchTouchEvent(downEvent);
        downEvent.recycle();

        if(getParent() != null)
        {
            getParent().requestDisallowInterceptTouchEvent(true);
            // requestDisallowInterceptTouchEvent 는 현 뷰의 상위 부모들의 onInteceptTouchEvent 를 차단.
            // 스크롤 뷰는 onIntercetTouchEvent 내에서 스크롤 기능을 구현하기에, 위 함수를 호출함으로써,
            // 스크롤 기능을 차단함
        }
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
                    if(view == swapingItem) continue;

                    int middle = ( view.getTop() + view.getBottom() ) / 2;

                    if(yPos < middle)
                    {
                        break;
                    }
                }


                int swappingIndex = indexOfChild(swapingItem);
                if(i - 1 != swappingIndex)
                {
                    if(i > swappingIndex)
                    {
                        removeViewAt(swappingIndex);
                        addView(swapingItem, i - 1);
                    }
                    else
                    {
                        removeViewAt(swappingIndex);
                        addView(swapingItem, i);
                    }
                }

                break;

            case MotionEvent.ACTION_UP:
                int toIndex = indexOfChild(swapingItem);

                if(toIndex != originIndex)
                {
                    if(swapFunc != null)
                    {
                        swapFunc.swapCompleted((ISwapableItem)swapingItem, originIndex, toIndex);
                    }
                }


                ((ISwapableItem)swapingItem).changeBackgroundToNormal();
                swapingItem = null;

                if (getParent() != null)
                {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }

                return false;
        }


        return true;
    }





}
