package com.example.dailyquest;

import android.app.AlertDialog;
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
import androidx.core.content.ContextCompat;

import com.example.dailyquest.databinding.DateInfoSettingBinding;
import com.example.dailyquest.databinding.DialogColorPaletteBinding;
import com.example.dailyquest.databinding.ItemSubTodoBinding;
import com.example.dailyquest.databinding.TodoInfoBinding;

import java.util.ArrayList;
import java.util.function.BiConsumer;
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
    private Button buttonSetting;

    private LinearLayout topLayout;
    private LinearLayout subTodosLayout;

    private BiConsumer<Todo, MainFuncEnum> mainFuncListener;
    private Runnable shutDownThisDialogListener;


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
        topLayout = findViewById(R.id.linearlayout_top);
        buttonSetting = findViewById(R.id.button_setting);
    }


    public void initialize(Todo InTodo, BiConsumer<Todo, MainFuncEnum> InMainFunc,
                           Runnable shutDownThisDialogFunc)
    {
        Context context = getContext();

        todo = InTodo;
        mainFuncListener = InMainFunc;
        shutDownThisDialogListener = shutDownThisDialogFunc;

        mainText.setText(todo.mainText);
        explainText.setText(todo.explainText);

        int color = 0;
        switch(todo.getColor())
        {
            case 1:
                color = ContextCompat.getColor(context, R.color._1_Light);
                break;
            case 2:
                color = ContextCompat.getColor(context, R.color._2_Light);
                break;
            case 3:
                color = ContextCompat.getColor(context, R.color._3_Light);
                break;
            case 4:
                color = ContextCompat.getColor(context, R.color._4_Light);
                break;
            case 5:
                color = ContextCompat.getColor(context, R.color._5_Light);
                break;
            case 6:
                color = ContextCompat.getColor(context, R.color._6_Light);
                break;
            case 7:
                color = ContextCompat.getColor(context, R.color._7_Light);
                break;
        }
        topLayout.setBackgroundColor(color);

        isEditMode = false;

        buttonSecondRight.setOnClickListener(v->
        {
            if(isEditMode() == false)
            {
                toEditMode();
            }
            else
            {
                showColorDialog(context);
            }
        });

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


        ISwapCompleteFunc swapCompleteFunc = new ISwapCompleteFunc()
        {
            @Override
            public void swapCompleted(ISwapableItem swappedItem, int fromIndex, int toIndex)
            {
                SubTodoInterface subTodoInterface = (SubTodoInterface) swappedItem;
                SubTodo swappedSubTodo = subTodoInterface.getSubTodo();

                if(fromIndex == toIndex) return;
                if(todo.subTodos.get(fromIndex) != swappedSubTodo) return;

                todo.subTodos.remove(fromIndex);
                todo.subTodos.add(toIndex, swappedSubTodo);

                invokeSaveDate.run();
            }
        };


        SwapableItemsContainer swapableItemsContainer = findViewById(R.id.linearLayout_SubTodos);
        swapableItemsContainer.setSwapCompleteFunc(swapCompleteFunc);

        BiConsumer<SubTodo, SubTodoInterface> deleteFunc
                = (SubTodo dSubtodo, SubTodoInterface dSubtodoInterface)->
        {
            Consumer<Boolean> isYes = (Boolean bYes) ->
            {
                if(bYes)
                {
                    todo.subTodos.remove(dSubtodo);
                    subTodosLayout.removeView(dSubtodoInterface);
                    invokeSaveDate.run();
                }
            };

            InformUtils.instance().ShowYesOrNo(context,
                    String.format("[%s]\n을 삭제하겠습니까?", dSubtodo.subText), isYes);
        };


        for(int i = 0; i < todo.subTodos.size(); i++)
        {
            SubTodo subTodo = todo.subTodos.get(i);
            ItemSubTodoBinding subTodoBinding = ItemSubTodoBinding
                    .inflate(LayoutInflater.from(context),
                            subTodosLayout, false);

            SubTodoInterface subInterface = subTodoBinding.getRoot();
            subInterface.initialize(subTodo, invokeSaveDate, deleteFunc);

            subTodosLayout.addView(subInterface);
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
            subInterface.initialize(subTodo, invokeSaveDate, deleteFunc);

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
        buttonSetting.setOnClickListener(v->
        {
            show_settingInterface(context);
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
        buttonSetting.setText("");
        buttonSetting.setClickable(false);

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
        buttonSecondRight.setText("C");
        int color = 0;
        switch(todo.getColor())
        {
            case 1:
                color = ContextCompat.getColor(context, R.color._1_Dark);
                break;
            case 2:
                color = ContextCompat.getColor(context, R.color._2_Dark);
                break;
            case 3:
                color = ContextCompat.getColor(context, R.color._3_Dark);
                break;
            case 4:
                color = ContextCompat.getColor(context, R.color._4_Dark);
                break;
            case 5:
                color = ContextCompat.getColor(context, R.color._5_Dark);
                break;
            case 6:
                color = ContextCompat.getColor(context, R.color._6_Dark);
                break;
            case 7:
                color = ContextCompat.getColor(context, R.color._7_Dark);
                break;
        }
        buttonSecondRight.setBackgroundColor(color);

        buttonAddSubtodo.setVisibility(View.VISIBLE);

        int size = subTodosLayout.getChildCount();
        for(int i = 0; i < size; i++)
        {
            SubTodoInterface subInterface
                    = (SubTodoInterface) subTodosLayout.getChildAt(i);

            subInterface.onEditMode();
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

        buttonSetting.setClickable(true);
        buttonSetting.setText("S");

        buttonLeft.setText("B");
        buttonSecondRight.setText("M");
        buttonSecondRight.setBackgroundColor(ContextCompat.getColor(context, R.color.purple_500));

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

            subInterface.onViewMode();
        }
    }
    private TypedArray makeEditTextBackground()
    {
        int[] attrs = new int[]{android.R.attr.editTextBackground};
        return getContext().obtainStyledAttributes(attrs);
    }

    public Runnable invokeSaveDate = ()->
    {
        if(mainFuncListener != null)
        {
            mainFuncListener.accept(todo, MainFuncEnum.SaveDate);
        }
    };

    private void showColorDialog(Context context)
    {
        DialogColorPaletteBinding binding = DialogColorPaletteBinding
                .inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(binding.getRoot()).create();

        binding.colorBtn1.setOnClickListener(v->
        {
            todo.setColor(1);
            topLayout.setBackgroundColor(ContextCompat.getColor(context, R.color._1_Light));
            buttonSecondRight.setBackgroundColor(ContextCompat.getColor(context, R.color._1_Dark));
            dialog.dismiss();
        });
        binding.colorBtn2.setOnClickListener(v->
        {
            todo.setColor(2);
            topLayout.setBackgroundColor(ContextCompat.getColor(context, R.color._2_Light));
            buttonSecondRight.setBackgroundColor(ContextCompat.getColor(context, R.color._2_Dark));
            dialog.dismiss();
        });
        binding.colorBtn3.setOnClickListener(v->
        {
            todo.setColor(3);
            topLayout.setBackgroundColor(ContextCompat.getColor(context, R.color._3_Light));
            buttonSecondRight.setBackgroundColor(ContextCompat.getColor(context, R.color._3_Dark));
            dialog.dismiss();
        });
        binding.colorBtn4.setOnClickListener(v->
        {
            todo.setColor(4);
            topLayout.setBackgroundColor(ContextCompat.getColor(context, R.color._4_Light));
            buttonSecondRight.setBackgroundColor(ContextCompat.getColor(context, R.color._4_Dark));
            dialog.dismiss();
        });
        binding.colorBtn5.setOnClickListener(v->
        {
            todo.setColor(5);
            topLayout.setBackgroundColor(ContextCompat.getColor(context, R.color._5_Light));
            buttonSecondRight.setBackgroundColor(ContextCompat.getColor(context, R.color._5_Dark));
            dialog.dismiss();
        });
        binding.colorBtn6.setOnClickListener(v->
        {
            todo.setColor(6);
            topLayout.setBackgroundColor(ContextCompat.getColor(context, R.color._6_Light));
            buttonSecondRight.setBackgroundColor(ContextCompat.getColor(context, R.color._6_Dark));
            dialog.dismiss();
        });
        binding.colorBtn7.setOnClickListener(v->
        {
            todo.setColor(7);
            topLayout.setBackgroundColor(ContextCompat.getColor(context, R.color._7_Light));
            buttonSecondRight.setBackgroundColor(ContextCompat.getColor(context, R.color._7_Dark));
            dialog.dismiss();
        });

        dialog.show();
    }


    private void show_settingInterface(Context context)
    {
        DateInfoSettingBinding binding = DateInfoSettingBinding.inflate(LayoutInflater
                .from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();
        binding.buttonDateInfoSettingChangeByCalender.setOnClickListener(v->
        {
            if(mainFuncListener != null)
            {
                mainFuncListener.accept(todo, MainFuncEnum.LoadCalender);
            }

            dialog.dismiss();
        });
        binding.buttonDateInfoSettingDelete.setOnClickListener(v->
        {
            Consumer<Boolean> deleteIfTrue = (Boolean bYes)->
            {
                if(bYes)
                {
                    if(mainFuncListener != null)
                    {
                        mainFuncListener.accept(todo, MainFuncEnum.DeleteTodo);
                        shutDownThisDialog();
                    }
                }
            };

            InformUtils.instance().ShowYesOrNo(context,
                    String.format("[%s]\n를 삭제하겠습니까?", todo.mainText),
                    deleteIfTrue);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void shutDownThisDialog()
    {
        if(shutDownThisDialogListener != null)
        {
            shutDownThisDialogListener.run();
        }
    }

}
