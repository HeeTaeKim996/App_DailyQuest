package com.example.dailyquest.Interface.FixedTodo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.dailyquest.Data.Fixed.FixedCategory;
import com.example.dailyquest.Data.Fixed.FixedCategoryEnum;
import com.example.dailyquest.Data.Fixed.FixedTodo;
import com.example.dailyquest.Interface.Alarm.AlarmEnum;
import com.example.dailyquest.Interface.Alarm.AlarmFunc;
import com.example.dailyquest.Interface.Alarm.AlarmTimeSetPage;
import com.example.dailyquest.R;
import com.example.dailyquest.Utils.BackgroundColorUtils;
import com.example.dailyquest.databinding.DialogColorPaletteBinding;
import com.example.dailyquest.databinding.OthersFixedTodoSetCategoryBinding;
import com.example.dailyquest.databinding.TodoInfoSetAlarmPageBinding;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FixedTodoSetInterface extends LinearLayout
{
    public static enum FuncEnum
    {
        Save
    }


    private FixedTodo todo;
    private boolean isEditMode;
    private boolean isInit;
    public void setIsInitFalse() { isInit = false;}
    public boolean isInit() { return isInit; }

    private EditText mainText;
    private EditText explainText;

    private LinearLayout topLayout;
    private Button buttonLeft;
    private Button buttonSecondRight;
    private TextView categoryExplainText;
    private Button setCategoryButton;
    private TextView alarmExplainText;
    private Button alarmSetButton;

    private BiConsumer<FixedTodo, FuncEnum> upperFuncListener;


    public FixedTodoSetInterface(@NonNull Context context)
    { super(context); }

    public FixedTodoSetInterface(@NonNull Context context, @Nullable AttributeSet attrs)
    { super(context, attrs); }

    public FixedTodoSetInterface(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr); }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        mainText = findViewById(R.id.textView_fixedTodoSet_mainText);
        explainText = findViewById(R.id.textView_fixedTodoSet_explainText);
        buttonLeft = findViewById(R.id.button_fixedTodoSet_left);
        buttonSecondRight = findViewById(R.id.button_fixedTodoSet_secondRight);
        topLayout = findViewById(R.id.linearLayout_fixedTodoSet_topLayout);
        categoryExplainText = findViewById(R.id.textView_fixedTodoSet_categoryExplainText);
        setCategoryButton = findViewById(R.id.button_fixedTodoSet_setCategory);
        alarmExplainText = findViewById(R.id.textView_fixedTodoSet_alarmExplainText);
        alarmSetButton = findViewById(R.id.button_fixedTodoSet_alarmSetButton);
    }

    public void initialize(FixedTodo InTodo, BiConsumer<FixedTodo, FuncEnum> InUpperFunc,
                           boolean InIsInit)
    {
        Context context = getContext();

        todo = InTodo;
        upperFuncListener = InUpperFunc;
        isInit = InIsInit;

        mainText.setText(todo.mainText);
        explainText.setText(todo.explainText);

        topLayout.setBackgroundColor(BackgroundColorUtils.getColorByLight(context,
                todo.getColor()));

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
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable)
            {
                todo.mainText = editable.toString();
            }
        });
        explainText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable)
            {
                todo.explainText = editable.toString();
            }
        });

        setCategoryButton.setOnClickListener(v->{ show_setCategoryPanel(context);});
        update_categoryExplainText();

        alarmSetButton.setOnClickListener(v->
        {
            show_setAlarmPage(context);
        });
        updateAlarmExplainText();
    }


    private TypedArray makeEditTextBackground()
    {
        int[] attrs = new int[]{android.R.attr.editTextBackground};
        return getContext().obtainStyledAttributes(attrs);
    }

    public boolean isEditMode() { return isEditMode;}

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
        buttonSecondRight.setText("C");
        buttonSecondRight.setBackgroundColor(BackgroundColorUtils.getColorByDark(context,
                todo.getColor()));

        setCategoryButton.setVisibility(VISIBLE);
        alarmSetButton.setVisibility(VISIBLE);
    }

    public boolean toViewMode()
    {
        Context context = getContext();
        isEditMode = false;

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
        buttonSecondRight.setBackgroundColor(ContextCompat.getColor(context, R.color.purple_500));

        InputMethodManager imm = (InputMethodManager) context
        .getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
        {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }

        setCategoryButton.setVisibility(INVISIBLE);
        alarmSetButton.setVisibility(INVISIBLE);




        if(mainText.getText().toString().trim().isEmpty()
                && explainText.getText().toString().trim().isEmpty())
        {
            return false;
        }
        return true;
    }



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

    private Runnable save = ()->
    {
        if(upperFuncListener != null)
        {
            upperFuncListener.accept(todo, FuncEnum.Save);
        }
    };

    private void update_categoryExplainText()
    {
        categoryExplainText.setText(todo.getCategory().getSummary());
    }


    private void show_setCategoryPanel(Context context)
    {
        OthersFixedTodoSetCategoryBinding binding = OthersFixedTodoSetCategoryBinding
                .inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();


        Consumer<FixedCategory> setCategory = (FixedCategory newCategory)->
        {
            todo.setCategory(newCategory);
            update_categoryExplainText();
            dialog.dismiss();
        };

        FixedTodoCategorySetPanel setPanel = (FixedTodoCategorySetPanel) binding.getRoot();
        setPanel.initialize(setCategory, todo.getCategory());

        dialog.show();
    }

    private void show_setAlarmPage(Context context)
    {
        TodoInfoSetAlarmPageBinding binding = TodoInfoSetAlarmPageBinding.inflate(LayoutInflater
                .from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();

        AlarmTimeSetPage alarmTimeSetPage = binding.getRoot();
        AlarmFunc alarmFunc = new AlarmFunc()
        {
            @Override
            public void accept(AlarmEnum alarmEnum, short alarmTime, byte alarmRepTime)
            {
                switch(alarmEnum)
                {
                    case Cancel:
                        dialog.dismiss();
                        break;
                    case Ok:
                        todo.setAlarmTime(alarmTime);
                        todo.alarmRepTime = alarmRepTime;
                        dialog.dismiss();
                        updateAlarmExplainText();
                        break;
                }
            }
        };
        alarmTimeSetPage.initialize(todo.getAlarmTime(), todo.alarmRepTime, alarmFunc);

        dialog.show();
    }
    private void updateAlarmExplainText()
    {
        short alarmTime = todo.getAlarmTime();
        if(alarmTime == -1)
        {
            alarmExplainText.setText("알람 미설정");
        }
        else
        {
            int hour = alarmTime >> 6;
            int minute = alarmTime & 0x3F;
            int repTime = todo.alarmRepTime;


            alarmExplainText.setText(String.format
                    ("%2d:%2d(%d)", hour, minute, repTime));
        }
    }

}
