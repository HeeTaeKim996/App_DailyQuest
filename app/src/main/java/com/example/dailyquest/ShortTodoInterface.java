package com.example.dailyquest;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class ShortTodoInterface extends LinearLayout
{
    private Todo todo;
    private BCompletedButton completedButton;
    private TextView shortText;

    public ShortTodoInterface(Context context)
    { super(context); }

    public ShortTodoInterface(Context context, @Nullable AttributeSet attrs)
    { super(context, attrs); }

    public ShortTodoInterface(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr); }

    public void initialize(Todo InTodo)
    {
        todo = InTodo;

        completedButton = findViewById(R.id.button_isFinished);
        completedButton.setCompleted(todo.isCompleted);

        shortText = findViewById(R.id.textView_shortMainText);
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
}
