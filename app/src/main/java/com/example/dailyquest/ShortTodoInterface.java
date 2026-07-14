package com.example.dailyquest;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.function.BiConsumer;

public class ShortTodoInterface extends FrameLayout
{
    private Todo todo;
    private BCompletedButton completedButton;
    private TextView shortText;
    private ProgressBar swipeProgressBar;

    private float xPos;
    private boolean bIntercepting = false;

    private BiConsumer<Todo, ShortTodoInterface> deleteTodoListener;

    private final float DELETE_THRESHOLD = 400f;
    private final float HALF_DELETE_THRESHOLD = DELETE_THRESHOLD / 2.f;

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
    }

    public void initialize(Todo InTodo, BiConsumer<Todo, ShortTodoInterface> InDeleteTodo)
    {
        todo = InTodo;
        deleteTodoListener = InDeleteTodo;

        completedButton.setCompleted(todo.isCompleted);
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




    public static class BCompletedButton extends androidx.appcompat.widget.AppCompatButton
    {
        public boolean bCompleted;

        public BCompletedButton(Context context)
        { super(context); }

        public BCompletedButton(Context context, AttributeSet attrs)
        { super(context, attrs); }

        public BCompletedButton(Context context, AttributeSet attrs, int defStyleAttr)
        { super(context, attrs, defStyleAttr);}

        public void setCompleted(boolean InCompleted)
        {
            bCompleted = InCompleted;
            if(bCompleted)
            {
                setText("C");
            }
            else
            {
                setText("Y");
            }

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
                break;

            case MotionEvent.ACTION_MOVE:
                float diff = Math.abs(motionEvent.getX() - xPos);
                if(bIntercepting == false && diff > HALF_DELETE_THRESHOLD)
                {
                    bIntercepting = true;
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

                int progress = (int) Math.min(100f,
                        Math.max(0, (diff - HALF_DELETE_THRESHOLD) / HALF_DELETE_THRESHOLD * 100f ));
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
}
