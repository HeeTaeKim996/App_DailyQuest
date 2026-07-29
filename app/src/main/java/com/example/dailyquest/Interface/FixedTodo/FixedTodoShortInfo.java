package com.example.dailyquest.Interface.FixedTodo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dailyquest.Data.FixedTodo;
import com.example.dailyquest.Interface.SwapableItemsContainer;
import com.example.dailyquest.R;
import com.example.dailyquest.Small.ISwapableItem;
import com.example.dailyquest.Utils.BackgroundColorUtils;

import java.util.function.Consumer;

public class FixedTodoShortInfo extends FrameLayout implements ISwapableItem
{
    private FixedTodo todo;
    private TextView shortText;
    private ProgressBar swipeProgressBar;


    private float xPos;
    private enum State
    {
        normal,
        deleting,
        moving
    }
    private State state;


    private Consumer<FixedTodo> deleteTodoListener;

    private static final float DELETE_THRESHOLD = 400f;
    private static final float HALF_DELETE_THRESHOLD = DELETE_THRESHOLD / 2.f;
    private static final int MAX_PROGRESS = 500;

    public FixedTodoShortInfo(@NonNull Context context)
    { super(context); }

    public FixedTodoShortInfo(@NonNull Context context, @Nullable AttributeSet attrs)
    { super(context, attrs); }

    public FixedTodoShortInfo(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr); }


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        shortText = findViewById(R.id.textView_fixedTodos_shortInfo);
        swipeProgressBar = findViewById(R.id.progressBar_fixedTodoShortInfo);
        swipeProgressBar.setMax(MAX_PROGRESS);
        swipeProgressBar.setProgress(0);
    }

    public void initialize(FixedTodo InTodo,
                           Consumer<FixedTodo> InDeleteTodo)
    {
        Context context = getContext();

        todo = InTodo;
        deleteTodoListener = InDeleteTodo;

        updateInterface();



        setOnLongClickListener(v->
        {
            if(state == State.normal)
            {
                state = State.moving;

                SwapableItemsContainer parent = (SwapableItemsContainer) getParent();
                if(parent != null)
                {
                    long now = SystemClock.uptimeMillis();
                    MotionEvent cancelEvent = MotionEvent.obtain(now, now,
                            MotionEvent.ACTION_CANCEL, 0, 0, 0);
                    v.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();

                    parent.startSwap(this);
                }
            }

            return false;
        });
    }
    public void updateInterface()
    {
        Context context = getContext();

        shortText.setText(todo.mainText);

        LayerDrawable layerDrawable = (LayerDrawable) swipeProgressBar.getProgressDrawable();
        Drawable backgroundDrawable = layerDrawable.findDrawableByLayerId
                (android.R.id.background);
        if(backgroundDrawable instanceof GradientDrawable)
        {
            GradientDrawable shape = (GradientDrawable) backgroundDrawable;
            shape.setColor(BackgroundColorUtils.getColorByLight(context, todo.getColor()));
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {
        switch(motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                xPos = motionEvent.getX();
                state = State.normal;
                break;

            case MotionEvent.ACTION_MOVE:
                float diff = motionEvent.getX() - xPos;
                boolean isPlus = diff >= 0;
                diff = Math.abs(diff);

                if(state == State.normal && diff > HALF_DELETE_THRESHOLD)
                {
                    state = State.deleting;
                }

                if(state == State.deleting)
                {
                    if(isPlus)
                    {
                        if(swipeProgressBar.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL)
                        {
                            swipeProgressBar.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                        }
                    }
                    else
                    {
                        if(swipeProgressBar.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR)
                        {
                            swipeProgressBar.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                        }
                    }

                    int progress = (int) Math.min(MAX_PROGRESS,
                            Math.max(0, (diff - HALF_DELETE_THRESHOLD) / HALF_DELETE_THRESHOLD
                            * MAX_PROGRESS));
                    swipeProgressBar.setProgress(progress);
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                if(state == State.deleting)
                {
                    swipeProgressBar.setProgress(0);
                    float finalDiff = Math.abs(motionEvent.getX() - xPos);
                    if(finalDiff >= DELETE_THRESHOLD)
                    {
                        if(deleteTodoListener != null)
                        {
                            deleteTodoListener.accept(todo);
                        }
                    }

                    return true;
                }
                break;
        }

        return super.onTouchEvent(motionEvent);
    }

    @Override
    public void changeBackgroundToPicked()
    {
        findViewById(R.id.linearLayout_fixedTodos_shortInfo)
                .setBackgroundResource(R.drawable.date_background_today);
    }

    @Override
    public void changeBackgroundToNormal()
    {
        findViewById(R.id.linearLayout_fixedTodos_shortInfo)
                .setBackgroundColor(Color.TRANSPARENT);
    }
}
