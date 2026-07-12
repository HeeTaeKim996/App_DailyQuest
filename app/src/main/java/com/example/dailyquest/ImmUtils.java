package com.example.dailyquest;

import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class ImmUtils
{
    private static ImmUtils _instance;
    private ImmUtils(){}

    public static ImmUtils instance()
    {
        return _instance;
    }

    public void toTyping(Context context, EditText editText, Window window)
    {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
        {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }
}
