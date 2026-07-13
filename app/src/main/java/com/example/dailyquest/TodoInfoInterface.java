package com.example.dailyquest;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.dailyquest.databinding.ItemSubTodoBinding;

import java.util.ArrayList;
import java.util.function.Consumer;

public class TodoInfoInterface extends ConstraintLayout
{
    private Todo todo;
    private boolean isEditMode;

    private EditText mainText;
    private EditText explainText;

    private Button buttonLeft;
    private Button buttonSecondRight;
    private Button buttonAddSubtodo;

    private LinearLayout subTodosLayout;

    private Consumer<Date> saveDateListener;

    public TodoInfoInterface(@NonNull Context context)
    { super(context);                       init();}

    public TodoInfoInterface(@NonNull Context context, @Nullable AttributeSet attrs)
    { super(context, attrs);                init();}

    public TodoInfoInterface(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr);  init();}

    private void init()
    {
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        mainText = findViewById(R.id.editText_mainText);
        explainText = findViewById(R.id.editText_explainText);
        buttonLeft = findViewById(R.id.button_left);
        buttonSecondRight = findViewById(R.id.button_secondRight);
        buttonAddSubtodo = findViewById(R.id.button_addSubTodo);
        subTodosLayout = findViewById(R.id.linearLayout_SubTodos);
    }


    public void initialize(Todo InTodo, Consumer<Date> SaveDateFunc)
    {
        Context context = getContext();

        todo = InTodo;

        mainText.setText(todo.mainText);
        explainText.setText(todo.explainText);

        saveDateListener = SaveDateFunc;

        isEditMode = false;

        mainText.addTextChangedListener(new TextWatcher()
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
                todo.mainText = editable.toString();
            }
        });

        explainText.addTextChangedListener(new TextWatcher()
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
                todo.explainText = editable.toString();
            }
        });


        if(todo.subTodos != null)
        {
            for(int i = 0; i < todo.subTodos.size(); i++)
            {
                SubTodo subTodo = todo.subTodos.get(i);
                ItemSubTodoBinding subTodoBinding = ItemSubTodoBinding
                        .inflate(LayoutInflater.from(context),
                                subTodosLayout, false);

                SubTodoInterface subInterface = subTodoBinding.getRoot();
                subInterface.initialize(subTodo, invokeSaveDate);

                subTodosLayout.addView(subInterface);
            }
        }

        buttonAddSubtodo.setOnClickListener(v->
        {
            SubTodo subTodo = new SubTodo();

            if(todo.subTodos == null)
            {
                todo.subTodos = new ArrayList<>(1);
            }
            todo.subTodos.add(subTodo);

            ItemSubTodoBinding subTodoBinding = ItemSubTodoBinding
                    .inflate(LayoutInflater.from(context),
                            subTodosLayout, false);
            SubTodoInterface subInterface = subTodoBinding.getRoot();
            subInterface.initialize(subTodo, invokeSaveDate);

            subTodosLayout.addView(subInterface);

            subInterface.subText.requestFocus();

            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null)
            {
                imm.showSoftInput(subInterface.subText, InputMethodManager.SHOW_IMPLICIT);
            }
//            if(dialog.getWindow() != null)
//            {
//                dialog.getWindow().setSoftInputMode
//                        (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//            }
        });
    }


    public boolean isEditMode()
    {
        return isEditMode;
    }

    public void tempSetEditModeFalse()
    {
        isEditMode = false;
    }

    public void toEditMode()
    {
        Context context = getContext();
        isEditMode = true;


        mainText.setFocusableInTouchMode(true);
        mainText.setFocusable(true);
        mainText.setCursorVisible(true);
        TypedArray mainTypedArray = makeEditTextBackground();
        mainText.setBackground(mainTypedArray.getDrawable(0));
        mainTypedArray.recycle();
        mainText.requestFocus();

        if(mainText.getText() != null)
        {
            mainText.setSelection(mainText.getText().length());
        }

        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
        {
            imm.showSoftInput(mainText, InputMethodManager.SHOW_IMPLICIT);
        }


        explainText.setFocusableInTouchMode(true);
        explainText.setFocusable(true);
        explainText.setCursorVisible(true);
        TypedArray explainTypedArray = makeEditTextBackground();
        explainText.setBackground(explainTypedArray.getDrawable(0));
        explainTypedArray.recycle();


        buttonLeft.setText("S");
        buttonSecondRight.setText("N");
        buttonAddSubtodo.setVisibility(View.VISIBLE);

        int size = subTodosLayout.getChildCount();
        for(int i = 0; i < size; i++)
        {
            SubTodoInterface subInterface
                    = (SubTodoInterface) subTodosLayout.getChildAt(i);

            subInterface.subText.onEditMode();
        }
    }

    public void toViewMode()
    {
        isEditMode = false;

        Context context = getContext();

        mainText.setFocusableInTouchMode(false);
        mainText.setFocusable(false);
        mainText.setCursorVisible(false);
        mainText.setBackgroundColor(Color.TRANSPARENT);

        explainText.setFocusableInTouchMode(false);
        explainText.setFocusable(false);
        explainText.setCursorVisible(false);
        explainText.setBackgroundColor(Color.TRANSPARENT);

        buttonLeft.setText("B");
        buttonSecondRight.setText("M");

        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
        {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }

        buttonAddSubtodo.setVisibility(View.GONE);

        int size = subTodosLayout.getChildCount();
        for(int i = 0; i < size; i++)
        {
            SubTodoInterface subInterface
                    = (SubTodoInterface) subTodosLayout.getChildAt(i);

            subInterface.subText.onViewMode();
        }
    }
    private TypedArray makeEditTextBackground()
    {
        int[] attrs = new int[]{android.R.attr.editTextBackground};
        return getContext().obtainStyledAttributes(attrs);
    }

    public Runnable invokeSaveDate = ()->
    {
        if(saveDateListener != null)
        {
            Date parentDate = todo.parentDate.get();
            if(parentDate != null)
            {
                saveDateListener.accept(parentDate);
            }
        }
    };

}
