package com.example.dailyquest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.function.BiConsumer;

public class SubTodoInterface extends FrameLayout implements ISwapableItem
{
    public SubTodo subTodo;
    public SubTodoMainText subText;
    private Runnable invokeSaveDateListener;

    private boolean bCompleted;

    private boolean bIntercepting;
    private float xPos;
    private static final float DELETE_THRESHOLD = 400f;
    private static final float HALF_DELETE_THRESHOLD = DELETE_THRESHOLD / 2.f;
    private static final int MAX_PROGRESS = 500;
    private ProgressBar swipeProgressBar;

    private BiConsumer<SubTodo, SubTodoInterface> deleteSubtodoListener;



    private enum State
    {
        normal,
        deletePhase,
        moving
    }
    private State state;

    public SubTodoInterface(@NonNull Context context)
    { super(context);                       init();}

    public SubTodoInterface(@NonNull Context context, @Nullable AttributeSet attrs)
    { super(context, attrs);                init();}

    public SubTodoInterface(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr);  init();}

    private void init()
    {

    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        subText = findViewById(R.id.editText_subTodo);
        swipeProgressBar = findViewById(R.id.progressBar_subtodo);
        swipeProgressBar.setProgress(0);
        swipeProgressBar.setMax(MAX_PROGRESS);
    }


    public void initialize(SubTodo InSubTodo, Runnable InSaveDateListener,
                           BiConsumer<SubTodo, SubTodoInterface> InDeleteListener)
    {
        subTodo = InSubTodo;
        invokeSaveDateListener = InSaveDateListener;
        deleteSubtodoListener = InDeleteListener;

        bCompleted = subTodo.bCompleted;
        subText.setText(subTodo.subText);


        subText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {}
            @Override
            public void afterTextChanged(Editable editable)
            {
                subTodo.subText = editable.toString();
            }
        });
    }

    public void onViewMode()
    {
        bIntercepting = true;

        setClickable(true);
        setOnClickListener(v->
        {

            if(bCompleted)
            {
                subText.setTextColor(Color.parseColor("#000000"));
                subText.setPaintFlags(subText.getPaintFlags()
                        & ~(Paint.STRIKE_THRU_TEXT_FLAG));
            }
            else
            {
                subText.setTextColor(Color.parseColor("#AAAAAA"));
                subText.setPaintFlags(subText.getPaintFlags()
                        | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            bCompleted = !bCompleted;
            subTodo.bCompleted = bCompleted;

            if(invokeSaveDateListener != null)
            {
                invokeSaveDateListener.run();
            }

        });
        setOnLongClickListener(view ->
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
                    view.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();

                    parent.startSwap(this);
                }
            }


            return false;
        });

        subText.onViewMode(bCompleted);
    }

    public void onEditMode()
    {
        bIntercepting = false;

        setClickable(false);
        setOnClickListener(null);
        setOnLongClickListener(null);
        subText.onEditMode(123);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent)
    {
        if(bIntercepting) return true;
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {
        switch(motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                state = State.normal;
                xPos = motionEvent.getX();
                break;

            case MotionEvent.ACTION_MOVE:
                float diff = motionEvent.getX() - xPos;
                boolean isPlus = diff >= 0;
                diff = Math.abs(diff);

                if(state == State.normal && diff > HALF_DELETE_THRESHOLD)
                {
                    state = State.deletePhase;
                }

                if(state == State.deletePhase)
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
                            Math.max(0, (diff - HALF_DELETE_THRESHOLD) / HALF_DELETE_THRESHOLD)
                    * MAX_PROGRESS);
                    swipeProgressBar.setProgress(progress);
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                if(state == State.deletePhase)
                {
                    float finalDiff = Math.abs(motionEvent.getX() - xPos);
                    if(finalDiff >= DELETE_THRESHOLD)
                    {
                        if(deleteSubtodoListener != null)
                        {
                            deleteSubtodoListener.accept(subTodo, this);
                        }
                    }

                    swipeProgressBar.setProgress(0);
                    return true;

                }
                break;
        }

        return super.onTouchEvent(motionEvent);
    }


    @Override
    public void changeBackgroundToPicked()
    {
        setBackgroundResource(R.drawable.date_background_today);
    }

    @Override
    public void changeBackgroundToNormal()
    {
        setBackgroundResource(R.drawable.subtodo_background);
    }

    public SubTodo getSubTodo()
    {
        return subTodo;
    }











    public static class SubTodoMainText extends androidx.appcompat.widget.AppCompatEditText
    {
        public SubTodoMainText(Context context)
        { super(context);                       init();}

        public SubTodoMainText(Context context, AttributeSet attrs)
        { super(context, attrs);                init();}

        public SubTodoMainText(Context context, AttributeSet attrs, int defStyleAttr)
        { super(context, attrs, defStyleAttr);  init();}

        private void init()
        {
            setImeOptions(EditorInfo.IME_ACTION_DONE);
        }

        public void onViewMode(boolean bCompleted)
        {
            setFocusable(false);
            setFocusableInTouchMode(false);
            setClickable(false);
            setCursorVisible(false);
            setBackgroundColor(Color.WHITE);
            // 원인을 못찾겠다. TRANSPARENT로 하면 바탕색이 _1_LIGHT가 됨. 원인 못찾아서 그냥 WHITE로 오버라이드함

            if(bCompleted)
            {
                setPaintFlags(getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                setTextColor(Color.parseColor("#AAAAAA"));
            }
        }

        public void onEditMode(int tempDelAft)
        {
            setFocusable(true);
            setFocusableInTouchMode(true);
            setClickable(true);
            setCursorVisible(true);
            setBackgroundColor(Color.WHITE);

            setTextColor(Color.parseColor("#000000"));
            setPaintFlags(getPaintFlags() & ~(Paint.STRIKE_THRU_TEXT_FLAG));
            setClickable(false);
        }



    }
}
