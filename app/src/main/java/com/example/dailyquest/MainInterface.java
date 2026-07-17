package com.example.dailyquest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
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
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

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


                GridLayout dateGrid = cellView.findViewById(R.id.gridLayout_calenderDate);

                int padding = screenWidth / 500;

                for(int j = 0; j < 8; j++)
                {
                    View box = new View(context);
                    GridLayout.LayoutParams boxParams = new GridLayout.LayoutParams();
                    boxParams.width = screenWidth / 60;
                    boxParams.height = screenHeight / 80;
                    boxParams.setMargins(0, padding, padding, 0);

                    box.setLayoutParams(boxParams);

                    box.setBackgroundColor(ContextCompat.getColor(context, R.color.purple_500));
//                  box.setBackgroundColor(Color.TRANSPARENT);

                    box.setClickable(false);
                    box.setFocusable(false);
                    box.setPadding(padding, padding, padding, padding);

                    dateGrid.addView(box);
                }





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

                    GridLayout boxGrid = cellView.findViewById(R.id.gridLayout_calenderDate);
                    for(int j = 0; j < 8; j++)
                    {
                        View box = boxGrid.getChildAt(j);
                        box.setBackgroundColor(Color.TRANSPARENT);
                    }
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


        ISwapCompleteFunc swapCompleteFunc = new ISwapCompleteFunc()
        {
            @Override
            public void swapCompleted(ISwapableItem swappedItem, int fromIndex, int toIndex)
            {
                if(fromIndex == toIndex) return;

                ShortTodoInterface shortTodoInterface = (ShortTodoInterface) swappedItem;
                Todo swappedTodo = shortTodoInterface.getTodo();
                if(date.todos.get(fromIndex) != swappedTodo) return;


                date.todos.remove(fromIndex);
                date.todos.add(toIndex, swappedTodo);


                saveDate.accept(date);
            }
        };
        SwapableItemsContainer swappableContainer = binding.linearLayoutScrollView;
        swappableContainer.setSwapCompleteFunc(swapCompleteFunc);

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
                        String.format("[%s]\n을 삭제하겠습니까?", dTodo.mainText), isYes);
            };


            for(int i = 0; i < date.todos.size(); i++)
            {
                Todo todo = date.todos.get(i);

                ItemTodoShortInfoBinding shortInfo = ItemTodoShortInfoBinding
                        .inflate(LayoutInflater.from(context));
                ShortTodoInterface shortInterface = shortInfo.getRoot();
                shortInterface.initialize(todo, deleteTodo);

                // ProgressBar 의 배경색을 date.color 에 맞춰 수정
                LayerDrawable layerDrawable = (LayerDrawable) shortInfo.progressBarSwipe
                        .getProgressDrawable();
                Drawable backgroundDrawable = layerDrawable
                        .findDrawableByLayerId(android.R.id.background);
                if(backgroundDrawable instanceof GradientDrawable)
                {
                    GradientDrawable shape = (GradientDrawable) backgroundDrawable;

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
                    shape.setColor(color);
                }

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
            ViewGroup.LayoutParams layoutParams = binding.linearLayoutScrollView.getLayoutParams();
            float density = context.getResources().getDisplayMetrics().density;
            layoutParams.height = (int)(density * 40);
            binding.linearLayoutScrollView.setLayoutParams(layoutParams);
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
