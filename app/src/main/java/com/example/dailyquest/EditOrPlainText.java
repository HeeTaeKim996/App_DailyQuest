package com.example.dailyquest;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class EditOrPlainText extends androidx.appcompat.widget.AppCompatEditText
{
    public boolean bCompleted;

    public EditOrPlainText(Context context)
    { super(context);                       init();}

    public EditOrPlainText(Context context, AttributeSet attrs)
    { super(context, attrs);                init();}

    public EditOrPlainText(Context context, AttributeSet attrs, int defStyleAttr)
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
    }
    public void onEditMode()
    {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
        setCursorVisible(true);
        setBackgroundColor(Color.WHITE);
    }



}
