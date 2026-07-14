package com.example.dailyquest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.gridlayout.widget.GridLayout;

import com.example.dailyquest.databinding.ActivityMainBinding;
import com.example.dailyquest.databinding.ItemDateTodoListBinding;
import com.example.dailyquest.databinding.ItemTodoShortInfoBinding;
import com.example.dailyquest.databinding.TodoInfoBinding;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MainInterface
{
    private ActivityMainBinding mainBinding;

    private int year;
    private int month;

    private MainCalender calender;
    private CalenderUtils.Calender today;
    private boolean isCurrMonth;

    private final View[] cellViews = new View[42];

    private GestureDetector gestureDetector;
    private float touchY = -1;




    MainInterface(Context context)
    {
        mainBinding = ActivityMainBinding.inflate(LayoutInflater.from(context));

        CalenderUtils.Calender today = CalenderUtils.instance().getTodaybyCalender();
        year = today.year;
        month = today.month;

        initializeCells(context);
        changeMainCalenderByYearMonth(context);

        mainBinding.buttonToLowerMonth.setOnClickListener(v->
        {
            deductMonth();
            changeMainCalenderByYearMonth(context);
        });

        mainBinding.buttonToUpperMonth.setOnClickListener(v->
        {
            addMonth();
            changeMainCalenderByYearMonth(context);
        });

        mainBinding.gridLayout.SetSwipeListener(new InterceptGridLayout.OnSwipeListener()
        {
            @Override
            public void OnSwipe(boolean isUp)
            {
                if(isUp)
                {
                    addMonth();
                }
                else
                {
                    deductMonth();
                }
                changeMainCalenderByYearMonth(context);
            }
        });
    }


    public ViewGroup getRootView()
    {
        return mainBinding.getRoot();
    }

    private void addMonth()
    {
        if(month == 12)
        {
            year++;
            month = 1;
        }
        else
        {
            month++;
        }
    }
    private void deductMonth()
    {
        if(month == 1)
        {
            year--;
            month = 12;
        }
        else
        {
            month--;
        }
    }



    private void initializeCells(Context context)
    {
        int totalCells = 42;
        mainBinding.gridLayout.post(()->
        {
            for(int i = 0; i < totalCells; i++)
            {
                View cellView = LayoutInflater.from(context)
                        .inflate(R.layout.item_calender_date, mainBinding.gridLayout,
                                false);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(i / 7, 1.f),
                        GridLayout.spec(i % 7, 1f)
                );
                params.width = 0;
                params.height = 0;
                cellView.setLayoutParams(params);

                cellViews[i] = cellView;
                mainBinding.gridLayout.addView(cellView);
            }
        });
    }

    private void changeMainCalenderByYearMonth(Context context)
    {
        mainBinding.textViewYearMonth.setText(String.format("%4d년 %2d월", year, month));

        calender = new MainCalender(year, month);
        today = CalenderUtils.instance().getTodaybyCalender();
        isCurrMonth = (calender.year == today.year
                && calender.month == today.month);



        mainBinding.gridLayout.post(()->
        {
            int gridHeight = mainBinding.gridLayout.getHeight();

            for(int i = 0; i < cellViews.length; i++)
            {
                View cellView = cellViews[i];

                Date date = calender.getDates().get(i);
                TextView dayText = cellView.findViewById(R.id.textView_date);
                dayText.setText(String.valueOf(date.date));

                if(date.isCurrMonth == false)
                {
                    cellView.setBackgroundResource(R.drawable.date_background_not_used);
                    dayText.setTextColor(Color.parseColor("#888888"));
                }
                else
                {
                    cellView.setBackgroundResource(R.drawable.date_background);
                    cellView.setClickable(true);

                    final int pos = i;
                    cellView.setOnClickListener(v->
                    {
                        show_date_todoListDialog(context, date, pos);
                    });

                    if(isCurrMonth && date.date == today.date)
                    {
                        cellView.setBackgroundResource(R.drawable.date_background_today);
                    }
                }
            }
        });
    }








    private void show_date_todoListDialog(Context context, Date date, int position)
    {
        ItemDateTodoListBinding binding = ItemDateTodoListBinding
                .inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(binding.getRoot()).create();
        dialog.show();

        if(date.todos != null)
        {
            BiConsumer<Todo, ShortTodoInterface> deleteTodo
                    = (Todo dTodo, ShortTodoInterface dInterface)->
            {
                Consumer<Boolean> isYes = (Boolean bYes) ->
                {
                    if(bYes)
                    {
                        date.todos.remove(dTodo);
                        binding.linearLayoutScrollView.removeView(dInterface);
                        saveDate.accept(date);
                    }
                };

                InformUtils.instance().ShowYesOrNo(getRootView().getContext(),
                        "할 일을 삭제하겠습니까?", isYes);
            };


            for(int i = 0; i < date.todos.size(); i++)
            {
                Todo todo = date.todos.get(i);

                ItemTodoShortInfoBinding shortInfo = ItemTodoShortInfoBinding
                        .inflate(LayoutInflater.from(context));
                ShortTodoInterface shortInterface = shortInfo.getRoot();
                shortInterface.initialize(todo, deleteTodo);

                shortInfo.buttonIsFinished.setOnClickListener(v->
                {
                    if(shortInterface.isCompleted()) return;

                    shortInterface.setCompleted(true);
                    calender.inform_dateUpdated(date);
                    saveDate.accept(date);
                });

                shortInfo.textViewShortMainText.setOnClickListener(v->
                {
                    show_todo_info(context, date, todo, position, false);
                    dialog.dismiss();
                });

                binding.linearLayoutScrollView.addView(shortInterface);
            }
        }
        else
        {
            ViewGroup.LayoutParams layoutParams = binding.scrollView.getLayoutParams();
            float density = context.getResources().getDisplayMetrics().density;
            layoutParams.height = (int)(density * 40);
            binding.scrollView.setLayoutParams(layoutParams);
        }
        binding.textViewMonthDate.setText(String.format("%d월 %d일 (%c)",
                calender.month, date.date, CalenderUtils.instance().INDEX_TO_DAY[position % 7]
        ));
        binding.buttonToBeforeDate.setOnClickListener(v->
        {
            if(position == 0) return;
            Date beforeDate = calender.getDates().get(position - 1);
            if(beforeDate.isCurrMonth == false) return;

            show_date_todoListDialog(context, beforeDate, position - 1);
            dialog.dismiss();
        });
        binding.buttonToNextDate.setOnClickListener(v->
        {
            if(position == calender.getDates().size() - 1) return;
            Date nextDate = calender.getDates().get(position + 1);
            if(nextDate.isCurrMonth == false) return;

            show_date_todoListDialog(context, nextDate, position + 1);
            dialog.dismiss();
        });
        binding.buttonAddTodoButton.setOnClickListener(v->
        {
            if(date.todos == null)
            {
                date.todos = new ArrayList<Todo>(1);
            }

            Todo newTodo = new Todo();
            newTodo.parentDate = new WeakReference<Date>(date);

            date.todos.add(newTodo);

            show_todo_info(context, date, newTodo, position, true);
            dialog.dismiss();
        });

    }

    private void show_todo_info(Context context, Date date, Todo todo, int position,
                                boolean isDirectEditing)
    {
        TodoInfoBinding binding = TodoInfoBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();

        final boolean[] isEditMode = {false};
        Supplier<TypedArray> makeEditTextBackground = ()->
        {
            int[] attrs = new int[]{android.R.attr.editTextBackground};
            return context.obtainStyledAttributes(attrs);
        };


        TodoInfoInterface infoInterface = binding.getRoot();
        infoInterface.initialize(todo, saveDate);



        binding.buttonLeft.setOnClickListener(v->
        {
            if(infoInterface.isEditMode())
            {
                infoInterface.toViewMode();
                saveDate.accept(date);
            }
            else
            {
                dialog.dismiss();
            }
        });
        binding.buttonSecondRight.setOnClickListener(v->
        {
            if(infoInterface.isEditMode() == false)
            {
                infoInterface.toEditMode();
            }
        });


        dialog.setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent)
            {
                if(keyCode == KeyEvent.KEYCODE_BACK
                        && keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                {
                    if(infoInterface.isEditMode())
                    {
                        infoInterface.toViewMode();
                        saveDate.accept(date);
                        return true; // 이벤트를 소비하여, 기존 KEYCODE_BACK 이 발동하지 않음
                    }
                }

                return false; // 이벤트를 소비하지 않아, 기존 KEYCODE_BACK 을 발동
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




        if(isDirectEditing && isEditMode[0] == false)
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

    public Consumer<Date> saveDate = (Date date)->
    {
        calender.inform_dateUpdated(date);
    };



}
