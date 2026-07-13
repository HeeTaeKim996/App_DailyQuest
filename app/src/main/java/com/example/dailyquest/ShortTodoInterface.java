package com.example.dailyquest;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class ShortTodoInterface extends LinearLayout
{
    private Todo todo;
    private BCompletedButton completedButton;
    private TextView shortText;

    private float xPos;
    private boolean bIntercepting = false;

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
    }

    public void initialize(Todo InTodo)
    {
        todo = InTodo;

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
                if(bIntercepting == false && diff > 150f)
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
                float diff = Math.abs(motionEvent.getX() - xPos);
                // TODO : diff 에 따라 색을 다르게 처리하거나, progressBar 로 diff 를 표현
                break;

            case MotionEvent.ACTION_UP:
                float finalDiff = Math.abs(motionEvent.getX() - xPos);
                if(finalDiff > 300f)
                {
                    // TODO : 삭제 요청
                    return true;
                }
                break;
        }

        return true;
    }
}
