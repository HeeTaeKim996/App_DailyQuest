package com.example.dailyquest;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.function.BiConsumer;

public class ShortTodoInterface extends FrameLayout implements ISwapableItem
{
    private Todo todo;
    private BCompletedButton completedButton;
    private TextView shortText;
    private ProgressBar swipeProgressBar;

    private float xPos;
    private boolean bIntercepting = false;
    private boolean bMoveOn = false;

    private BiConsumer<Todo, ShortTodoInterface> deleteTodoListener;

    private static final float DELETE_THRESHOLD = 400f;
    private static final float HALF_DELETE_THRESHOLD = DELETE_THRESHOLD / 2.f;
    private static final int MAX_PROGRESS = 500;

    public ShortTodoInterface(Context context)
    { super(context); }

    public ShortTodoInterface(Context context, @Nullable AttributeSet attrs)
    { super(context, attrs); }

    public ShortTodoInterface(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr); }


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        completedButton = findViewById(R.id.button_isFinished);
        shortText = findViewById(R.id.textView_shortMainText);
        swipeProgressBar = findViewById(R.id.progressBar_swipe);
        swipeProgressBar.setMax(MAX_PROGRESS);
        swipeProgressBar.setProgress(0);
    }

    public void initialize(Todo InTodo, BiConsumer<Todo, ShortTodoInterface> InDeleteTodo,
                           boolean InBPastedDate)
    {
        todo = InTodo;
        deleteTodoListener = InDeleteTodo;
        completedButton.bPastedDate = InBPastedDate;

        completedButton.setCompleted(todo.isCompleted);
        completedButton.setOnLongClickListener(v->
        {
            bMoveOn = true;
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

            return true;
        });

        shortText.setText(todo.mainText);
    }

    public void saveTodoFromInterface()
    {
        todo.isCompleted = completedButton.bCompleted;
    }

    public boolean isCompleted()
    {
        return completedButton.bCompleted;
    }

    public void setCompleted(boolean InCompleted)
    {
        completedButton.setCompleted(InCompleted);
        saveTodoFromInterface();
    }

    @Override
    public void changeBackgroundToPicked()
    {
        findViewById(R.id.linearLayout_shortInfo)
                .setBackgroundResource(R.drawable.date_background_today);
    }

    @Override
    public void changeBackgroundToNormal()
    {
        findViewById(R.id.linearLayout_shortInfo).setBackgroundColor(Color.TRANSPARENT);
    }


    public static class BCompletedButton extends androidx.appcompat.widget.AppCompatButton
    {
        public boolean bCompleted;
        public boolean bPastedDate;

        public BCompletedButton(Context context)
        { super(context); }

        public BCompletedButton(Context context, AttributeSet attrs)
        { super(context, attrs); }

        public BCompletedButton(Context context, AttributeSet attrs, int defStyleAttr)
        { super(context, attrs, defStyleAttr);}

        public void setCompleted(boolean InCompleted)
        {
            bCompleted = InCompleted;
            int colorId;
            if(bCompleted)
            {
                colorId = R.color.done_color;
            }
            else
            {
                if(bPastedDate)
                {
                    colorId = R.color.not_done_color;
                }
                else
                {
                    colorId = R.color.todo_color;
                }
            }
            int color = ContextCompat.getColor(getContext(), colorId);
            setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent)
    {
        switch(motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                xPos = motionEvent.getX();
                bIntercepting = false;

                bMoveOn = false;
                break;

            case MotionEvent.ACTION_MOVE:
                float diff = Math.abs(motionEvent.getX() - xPos);
                if(bMoveOn == false
                        && bIntercepting == false && diff > HALF_DELETE_THRESHOLD)
                {
                    bIntercepting = true;
                    if(getParent() != null)
                    {
                        // 여기서 인터셉트시, 부모(SwapableItemsContainer) 가 인터셉트 하는 것을 차단
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }


                    return true;
                }
                break;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {
        if(bIntercepting == false) return false;

        switch(motionEvent.getAction())
        {
            case MotionEvent.ACTION_MOVE:
                float diff = motionEvent.getX() - xPos;
                boolean isPlus = diff >= 0;
                diff = Math.abs(diff);

                if(isPlus == false)
                {
                    if(swipeProgressBar.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR)
                    {
                        swipeProgressBar.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                    }
                }
                else
                {
                    if(swipeProgressBar.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL)
                    {
                        swipeProgressBar.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                    }
                }

                int progress = (int) Math.min(MAX_PROGRESS,
                        Math.max(0, (diff - HALF_DELETE_THRESHOLD) / HALF_DELETE_THRESHOLD
                                * MAX_PROGRESS ));
                swipeProgressBar.setProgress(progress);

                break;

            case MotionEvent.ACTION_UP:
                swipeProgressBar.setProgress(0);

                float finalDiff = Math.abs(motionEvent.getX() - xPos);
                if(finalDiff >= DELETE_THRESHOLD)
                {
                    if(deleteTodoListener != null)
                    {
                        deleteTodoListener.accept(todo, this);
                    }
                    return true;
                }
                break;
        }

        return true;
    }


    public Todo getTodo()
    {
        return todo;
    }


}
