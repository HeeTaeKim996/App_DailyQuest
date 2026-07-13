package com.example.dailyquest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class SubTodoInterface extends ConstraintLayout
{
    public SubTodo subTodo;
    public SubTodoMainText subText;

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
    }


    public void initialize(SubTodo InSubTodo)
    {
        subTodo = InSubTodo;

        subText.bCompleted = subTodo.bCompleted;
        subText.setText(subTodo.subText);

        subText.setOnClickListener(v->
        {
            if(subText.bCompleted)
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

            subText.bCompleted = !subText.bCompleted;
            subTodo.bCompleted = subText.bCompleted;
        });

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




    public static class SubTodoMainText extends androidx.appcompat.widget.AppCompatEditText
    {
        public boolean bCompleted;

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

        public void onViewMode()
        {
            setFocusable(false);
            setFocusableInTouchMode(false);
            setClickable(false);
            setCursorVisible(false);
            setBackgroundColor(Color.TRANSPARENT);

            if(bCompleted)
            {
                setPaintFlags(getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                setTextColor(Color.parseColor("#AAAAAA"));
            }

            setClickable(true);
        }
        public void onEditMode()
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
