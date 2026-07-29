package com.example.dailyquest.Interface.FixedTodo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dailyquest.Data.FixedTodo;
import com.example.dailyquest.R;
import com.example.dailyquest.Utils.InformUtils;

import java.util.function.BiConsumer;

public class FixedTodoShortInfo extends FrameLayout
{
    private FixedTodo todo;
    private TextView shortText;
    private ProgressBar swipeProgressBar;


    private float xPos;
    private boolean bSwiping = false;

    private BiConsumer<FixedTodo, FixedTodoShortInfo> deleteTodoListener;

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

    public void initialize(FixedTodo InTodo, BiConsumer<FixedTodo,
            FixedTodoShortInfo> InDeleteTodo)
    {
        todo = InTodo;
        deleteTodoListener = InDeleteTodo;

        setOnClickListener(v->
        {
            // TODO : 상세 내용 보이기
            int i = 0;
        });

        shortText.setText(todo.mainText);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {
        switch(motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                xPos = motionEvent.getX();
                bSwiping = false;
                break;

            case MotionEvent.ACTION_MOVE:
                float diff = motionEvent.getX() - xPos;
                boolean isPlus = diff >= 0;
                diff = Math.abs(diff);

                if(bSwiping == false && diff > HALF_DELETE_THRESHOLD)
                {
                    bSwiping = true;
                }

                if(bSwiping)
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
                if(bSwiping)
                {
                    swipeProgressBar.setProgress(0);
                    float finalDiff = Math.abs(motionEvent.getX() - xPos);
                    if(finalDiff >= DELETE_THRESHOLD)
                    {
                        if(deleteTodoListener != null)
                        {
                            deleteTodoListener.accept(todo, this);
                        }
                    }

                    return true;
                }
                break;
        }

        return super.onTouchEvent(motionEvent);
    }

}
