package com.example.dailyquest.Interface.FixedTodo;


import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.dailyquest.Data.FixedTodo;
import com.example.dailyquest.FixedTodo.FixedTodoManager;
import com.example.dailyquest.R;
import com.example.dailyquest.Utils.InformUtils;

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
    private LinearLayout todosLayout;

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        addTodoButton = findViewById(R.id.button_fixedTodos_addFixedTodo);
        todosLayout = findViewById(R.id.linearLayout_fixedTodos);
    }

    public void initialize(Context context)
    {
        FixedTodoManager manager = FixedTodoManager.instance();
        if(manager == null) return;


        Runnable onEmptyTodos = ()->
        {
            ViewGroup.LayoutParams layoutParams = todosLayout.getLayoutParams();
            float density = context.getResources().getDisplayMetrics().density;
            layoutParams.height = (int)(density * 40);
            todosLayout.setLayoutParams(layoutParams);
        };

        ArrayList<FixedTodo> todos = manager.getTodos();
        if(todos.size() > 0)
        {
            BiConsumer<FixedTodo, FixedTodoShortInfo> deleteTodoFunc
                    = (FixedTodo dTodo, FixedTodoShortInfo dInterface)->
            {
                Consumer<Boolean> isYes = (Boolean bYes)->
                {
                    if(bYes)
                    {
                        deleteTodo(dTodo);

                        todosLayout.removeView(dInterface);
                        if(todosLayout.getChildCount() == 0)
                        {
                            onEmptyTodos.run();
                        }
                    }
                };

                InformUtils.instance().ShowYesOrNo(context,
                        String.format("[%s]\n을 삭제하겠습니까?", dTodo.mainText), isYes);
            };

            for(int i = 0; i < todos.size(); i++)
            {
                FixedTodo todo = todos.get(i);

                // TODO : 추가 처리
            }
        }
        addTodoButton.setOnClickListener(v->{ setAddTodoButton(context);});
    }

    private void setAddTodoButton(Context context)
    {

    }

    private void deleteTodo(FixedTodo todo)
    {
        // TODO : 삭제처리
    }
}
