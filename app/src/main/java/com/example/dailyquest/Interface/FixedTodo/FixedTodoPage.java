package com.example.dailyquest.Interface.FixedTodo;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.dailyquest.Data.Fixed.FixedTodo;
import com.example.dailyquest.FixedTodo.FixedTodoManager;
import com.example.dailyquest.Interface.SwapableItemsContainer;
import com.example.dailyquest.R;
import com.example.dailyquest.Small.ISwapCompleteFunc;
import com.example.dailyquest.Small.ISwapableItem;
import com.example.dailyquest.Utils.InformUtils;
import com.example.dailyquest.databinding.OthersFixedTodoSetBinding;
import com.example.dailyquest.databinding.OthersFixedTodoShortInfoBinding;
import com.example.dailyquest.databinding.UtilsEditTextOkCancelBinding;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FixedTodoPage extends LinearLayout
{
    public FixedTodoPage(Context context)
    { super(context);}

    public FixedTodoPage(Context context, @Nullable AttributeSet attrs)
    { super(context, attrs); }

    public FixedTodoPage(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr); }


    private Button addTodoButton;
    private SwapableItemsContainer todosLayout;
    private Button showLogButton;
    private Button clearLogButton;

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        addTodoButton = findViewById(R.id.button_fixedTodos_addFixedTodo);
        todosLayout = findViewById(R.id.linearLayout_fixedTodos);
        showLogButton = findViewById(R.id.button_fixedTodos_show_log);
        clearLogButton = findViewById(R.id.button_fixedTodos_clearLog);
    }

    public void initialize(Context context)
    {
        FixedTodoManager manager = FixedTodoManager.instance();
        if(manager == null) return;

        ISwapCompleteFunc iSwapCompleteFunc = new ISwapCompleteFunc()
        {
            @Override
            public void swapCompleted(ISwapableItem swappedItem, int fromIndex, int toIndex)
            {
                onTodoSwapped(fromIndex, toIndex);
            }
        };
        todosLayout.setSwapCompleteFunc(iSwapCompleteFunc);

        ArrayList<FixedTodo> todos = manager.getTodos();
        if(todos.size() > 0)
        {
            for(int i = 0; i < todos.size(); i++)
            {
                FixedTodo todo = todos.get(i);

                addTodoInLayout(todo);
            }
        }
        else
        {
            onEmptyTodos();
        }
        addTodoButton.setOnClickListener(v->
        {
            // TODO : 기본 그.. FIXED TODO 항목 및 설정 추가 처리 생성 후 TODO 넘기기
            FixedTodo todo = new FixedTodo();
            showTodoInfo(context, todo, true);
        });

        showLogButton.setOnClickListener(v->
        {
            show_logPage(context);
        });
        clearLogButton.setOnClickListener(v->
        {
            Consumer<Boolean> checkYes = (Boolean bYes)->
            {
                if(bYes)
                {
                    FixedTodoManager.instance().clearLog();
                }
            };

            InformUtils.instance().ShowYesOrNo(context,
                    "FixedTodo 변경 내역 로그를 모두 삭제하겠습니까?", checkYes);
        });
    }

    private void onEmptyTodos()
    {
        ViewGroup.LayoutParams layoutParams = todosLayout.getLayoutParams();
        float density = getContext().getResources().getDisplayMetrics().density;
        int height = (int)(density * 40);

        todosLayout.setMinimumHeight(height);
    }

    Consumer<FixedTodo> deleteTodoFunc = (FixedTodo dTodo)->
    {
        Consumer<Boolean> isYes = (Boolean bYes)->
        {
            if(bYes)
            {
                deleteTodo(dTodo);

            }
        };

        InformUtils.instance().ShowYesOrNo(getContext(),
                String.format("[%s]\n을 삭제하겠습니까?", dTodo.mainText), isYes);
    };

    private void addTodoInLayout(FixedTodo todo)
    {
        Context context = getContext();

        OthersFixedTodoShortInfoBinding shortInfoBinding
                = OthersFixedTodoShortInfoBinding.inflate(
                LayoutInflater.from(context));
        FixedTodoShortInfo shortInfo = shortInfoBinding.getRoot();
        shortInfo.initialize(todo, deleteTodoFunc);


        shortInfo.setOnClickListener(v->
        {
            showTodoInfo(context, todo, false);
        });

        todosLayout.addView(shortInfo);
    }


    private void showTodoInfo(Context context, FixedTodo todo, boolean InIsInit)
    {
        OthersFixedTodoSetBinding binding = OthersFixedTodoSetBinding.inflate(
                LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();



        FixedTodoSetInterface infoInterface = binding.getRoot();
        infoInterface.initialize(todo, funcConsumer, InIsInit);

        Runnable toViewMode = ()->
        {
            boolean isFilled = infoInterface.toViewMode();
            if(infoInterface.isInit())
            {
                if(isFilled == false)
                {
                    // 저장하지만 않으면, fixedTodo 는 애초에 추가되지 않았기에 이상 없음
                    dialog.dismiss();
                    return;
                }

                addTodo(todo);
                infoInterface.setIsInitFalse();
            }
            else
            {
                saveTodo(todo);
            }
        };

        binding.buttonFixedTodoSetLeft.setOnClickListener(v->
        {
            if(infoInterface.isEditMode())
            {
                toViewMode.run();
            }
            else
            {
                dialog.dismiss();
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode,
                                 KeyEvent keyEvent)
            {
                if(keyCode == KeyEvent.KEYCODE_BACK
                    && keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                {
                    if(infoInterface.isEditMode())
                    {
                        toViewMode.run();
                        return true; // 이벤트 소비
                    }
                }

                return false; // 이벤트 소비 X
            }
        });

        dialog.show();
        if(dialog.getWindow() != null)
        {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);

            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
        }

        if(InIsInit)
        {
            infoInterface.toEditMode();
            if(dialog.getWindow() != null)
            {
                dialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                );
            }
        }
        else
        {
            infoInterface.toViewMode();
        }
    }




    private BiConsumer<FixedTodo, FixedTodoSetInterface.FuncEnum> funcConsumer
            = (FixedTodo todo, FixedTodoSetInterface.FuncEnum funcEnum)->
    {
        switch (funcEnum)
        {
            case Save:
                saveTodo(todo);
                break;
        }
    };

    private void saveTodo(FixedTodo todo)
    {
        int index = FixedTodoManager.instance().saveTodo(todo);
        if(index == -1)
        {
            InformUtils.instance().ShowInformYes(getContext(), 
                    "FixedPage : saveTodo 오류");
            return;
        }

        FixedTodoShortInfo shortInfo = (FixedTodoShortInfo) todosLayout.getChildAt(index);
        shortInfo.updateInterface();

        InformUtils.instance().showToast(getContext(), "수정됨");
    }
    private void addTodo(FixedTodo todo)
    {
        if(FixedTodoManager.instance().addTodo(todo) == -1)
        {
            InformUtils.instance().ShowInformYes(getContext(),
                    "FixedPage : addTodo 오류");
            return;
        }

        addTodoInLayout(todo);

        InformUtils.instance().showToast(getContext(), "추가됨");
    }
    private void deleteTodo(FixedTodo todo)
    {
        int index = FixedTodoManager.instance().deleteTodo(todo);
        if(index == -1)
        {
            InformUtils.instance().ShowInformYes(getContext(),
                    "FixedPage : deleteTodo 오류");
            return;
        }

        todosLayout.removeViewAt(index);
        if(todosLayout.getChildCount() == 0)
        {
            onEmptyTodos();
        }

        InformUtils.instance().showToast(getContext(), "삭제됨");
    }

    private void onTodoSwapped(int fromIndex, int toIndex)
    {
        FixedTodoManager.instance().onItemSwapped(fromIndex, toIndex);

        InformUtils.instance().showToast(getContext(), "스왑됨");
    }


    private void show_logPage(Context context)
    {
        UtilsEditTextOkCancelBinding binding = UtilsEditTextOkCancelBinding.inflate
                (LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();



        binding.buttonUtilsEditTextOkCancelCancel.setOnClickListener(v->
        {
            dialog.dismiss();
        });
        binding.buttonUtilsEditTextOkCancelOk.setOnClickListener(v->
        {
            FixedTodoManager.instance().resetLog
                    (binding.editTextUtilsEditTextOkCancel.getText().toString());
            dialog.dismiss();
        });


        EditText editText = binding.editTextUtilsEditTextOkCancel;
        editText.setText(FixedTodoManager.instance().showLog());

        boolean[] boo = { false };
        Button topButton = binding.buttonUtilsEditTextOkCancelTop;
        topButton.setBackgroundColor(Color.GRAY);
        topButton.setText("-");



        topButton.setOnClickListener(v->
        {
            if(boo[0])
            {
                boo[0] = false;

                topButton.setBackgroundColor(Color.GRAY);
                topButton.setText("-");

                editText.setFocusable(false);
                editText.setFocusableInTouchMode(false);
                editText.setCursorVisible(false);

                InputMethodManager imm = (InputMethodManager) context
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null)
                {
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
            }
            else
            {
                boo[0] = true;

                topButton.setBackgroundColor(context.getColor(R.color.purple_500));
                topButton.setText("O");

                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.setCursorVisible(true);

                editText.requestFocus();
                if(editText.getText() != null)
                {
                    editText.setSelection(editText.getText().length());
                }
                InputMethodManager imm = (InputMethodManager) context
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null)
                {
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
            }

        });

        dialog.show();
    }
}
