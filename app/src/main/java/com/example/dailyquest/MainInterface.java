package com.example.dailyquest;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.DebugUtils;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;

import com.example.dailyquest.databinding.ActivityMainBinding;
import com.example.dailyquest.databinding.ItemDateTodoListBinding;
import com.example.dailyquest.databinding.ItemTodoShortInfoBinding;
import com.example.dailyquest.databinding.OthersBinding;
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
    private enum YearMonthState
    {
        PAST,
        CURR,
        FUTURE
    }
    private YearMonthState yearMonthState;

    private final View[] cellViews = new View[42];
    private BroadcastReceiver dateChangedReceiver;


    MainInterface(Context context)
    {
        StaticValues.rootFile = context.getFilesDir();

        mainBinding = ActivityMainBinding.inflate(LayoutInflater.from(context));

        today = CalenderUtils.instance().getTodaybyCalender();
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

        mainBinding.buttonOthers.setOnClickListener(v->
        {
            show_others_panel(context);
        });

        registerDateChangedReceiver(context);
//        TodoMidnightReceiver.scheduleNextMidnightAlarm(context);
//        TodoMidnightReceiver.updateTodayNotification(context);
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
                int length = screenWidth / 60;

                for(int j = 0; j < StaticValues.shortTodoCount; j++)
                {
                    View box = new View(context);
                    GridLayout.LayoutParams boxParams = new GridLayout.LayoutParams();
                    boxParams.width = length;
                    boxParams.height = length;
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

        calender = new MainCalender(context, year, month);
        today = CalenderUtils.instance().getTodaybyCalender();

        {
            int todayValue = today.year * 12 + today.month;
            int calenderValue = calender.year * 12 + calender.month;

            if(todayValue < calenderValue)
            {
                yearMonthState = YearMonthState.FUTURE;
            }
            else if(todayValue > calenderValue)
            {
                yearMonthState = YearMonthState.PAST;
            }
            else
            {
                yearMonthState = YearMonthState.CURR;
            }
        }




        mainBinding.gridLayout.post(()->
        {
            for(int i = 0; i < cellViews.length; i++)
            {
                View cellView = cellViews[i];

                DateProxy proxy = calender.getProxies()[i];

                updateDateCell(context, cellView, proxy, i);
            }
        });
    }

    private void updateDateCell(Context context, View cellView, DateProxy proxy, int pos)
    {
        TextView dayText = cellView.findViewById(R.id.textView_date);
        dayText.setText(String.valueOf(proxy.date));

        if(proxy.isCurrMonth == false)
        {
            cellView.setBackgroundResource(R.drawable.date_background_not_used);
            dayText.setTextColor(Color.parseColor("#888888"));

            GridLayout boxGrid = cellView.findViewById(R.id.gridLayout_calenderDate);
            for(int j = 0; j < StaticValues.shortTodoCount; j++)
            {
                View box = boxGrid.getChildAt(j);
                box.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        else
        {
            cellView.setBackgroundResource(R.drawable.date_background);
            cellView.setClickable(true);

            cellView.setOnClickListener(v->
            {
                show_date_todoListDialog(context, proxy, pos);
            });

            if(yearMonthState == YearMonthState.CURR && proxy.date == today.date)
            {
                cellView.setBackgroundResource(R.drawable.date_background_today);
            }

            GridLayout boxGrid = cellView.findViewById(R.id.gridLayout_calenderDate);
            int temp = proxy.todos & 0x3F_FF_FF_FF; // 개당 3비트. 총 10개 사용
            for(int j = 0; j < StaticValues.shortTodoCount; j++)
            {
                View box = boxGrid.getChildAt(j);
                if(temp == 0)
                {
                    box.setBackgroundColor(Color.TRANSPARENT);
                }
                else
                {
                    int colInt = temp & 7;
                    int color = 0;
                    switch(colInt)
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
                    box.setBackgroundColor(color);
                    temp = temp >> 3;
                }
            }
        }
    }






    private void show_date_todoListDialog(Context context, DateProxy proxy, int position)
    {
        ItemDateTodoListBinding binding = ItemDateTodoListBinding
                .inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(binding.getRoot()).create();
        dialog.show();

        Date date = calender.loadDate(proxy);

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



        Runnable onEmptyTodos = ()->
        {
            ViewGroup.LayoutParams layoutParams = binding.linearLayoutScrollView.getLayoutParams();
            float density = context.getResources().getDisplayMetrics().density;
            layoutParams.height = (int)(density * 40);
            binding.linearLayoutScrollView.setLayoutParams(layoutParams);
        };






        if(date.todos.size() > 0)
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

                        if(date.todos.size() == 0)
                        {
                            onEmptyTodos.run();
                        }
                    }
                };

                InformUtils.instance().ShowYesOrNo(getRootView().getContext(),
                        String.format("[%s]\n을 삭제하겠습니까?", dTodo.mainText), isYes);
            };

            boolean bPastedDate;
            if(yearMonthState == YearMonthState.PAST)
            {
                bPastedDate = true;
            }
            else if(yearMonthState == YearMonthState.FUTURE)
            {
                bPastedDate = false;
            }
            else
            {
                if(today.date <= date.date)
                {
                    bPastedDate = false;
                }
                else
                {
                    bPastedDate = true;
                }
            }


            for(int i = 0; i < date.todos.size(); i++)
            {
                Todo todo = date.todos.get(i);


                ItemTodoShortInfoBinding shortInfo = ItemTodoShortInfoBinding
                        .inflate(LayoutInflater.from(context));
                ShortTodoInterface shortInterface = shortInfo.getRoot();
                shortInterface.initialize(todo, deleteTodo, bPastedDate);

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

                    if(shortInterface.setCompleted(true))
                    {
                        saveDate.accept(date);
                    }
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
            onEmptyTodos.run();
        }








        binding.textViewMonthDate.setText(String.format("%d월 %d일 (%c)",
                calender.month, date.date, CalenderUtils.instance().INDEX_TO_DAY[position % 7]
        ));
        binding.buttonToBeforeDate.setOnClickListener(v->
        {
            if(position == 0) return;
            DateProxy beforeProxy = calender.getProxies()[position - 1];
            if(beforeProxy.isCurrMonth == false) return;

            show_date_todoListDialog(context, beforeProxy, position - 1);
            dialog.dismiss();
        });
        binding.buttonToNextDate.setOnClickListener(v->
        {
            if(position == calender.getProxies().length - 1) return;
            DateProxy nextProxy = calender.getProxies()[position + 1];
            if(nextProxy.isCurrMonth == false) return;

            show_date_todoListDialog(context, nextProxy, position + 1);
            dialog.dismiss();
        });
        binding.buttonAddTodoButton.setOnClickListener(v->
        {
            if(date.todos == null)
            {
                date.todos = new ArrayList<Todo>(1);
            }

            Todo newTodo = new Todo();
            newTodo.setParentDate(date);

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



        Runnable toViewMode = ()->
        {
            infoInterface.toViewMode();
            if(todo.mainText.equals("") && todo.explainText.equals("")
                && todo.subTodos.size() == 0)
            {
                date.todos.remove(todo);
                dialog.dismiss();
            }
            saveDate.accept(date);
        };

        binding.buttonLeft.setOnClickListener(v->
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
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent)
            {
                if(keyCode == KeyEvent.KEYCODE_BACK
                        && keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                {
                    if(infoInterface.isEditMode())
                    {
                        toViewMode.run();
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
        DateProxy proxy = calender.saveDate(date);
        int pos = calender.getOffset() + proxy.date - 1;

        View cellView = cellViews[pos];

        updateDateCell(getRootView().getContext(), cellView, proxy, pos);


        if(yearMonthState == YearMonthState.CURR && date.date == today.date)
        {
            // TODO? : 알림 업데이트

            NotificationHelper.updateTodayNotification(getRootView().getContext(), date.todos);
        }
    };



    private void show_others_panel(Context context)
    {
        OthersBinding binding = OthersBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();

        binding.buttonShowAllFiles.setOnClickListener(v->
        {
            InformUtils.instance().ShowInformYes(context,
                    DevelopUtils.instance().getAllFiles().toString());
            dialog.dismiss();
        });


        binding.buttonClearAllData.setOnClickListener(v->
        {
            Consumer<Boolean> onCheck = (Boolean bYes)->
            {
                if(bYes)
                {
                    DevelopUtils.instance().clearAllFiles();
                    changeMainCalenderByYearMonth(context);
                }
            };

            InformUtils.instance().ShowYesOrNo(context,
                    "데이터가 모두 삭제됩니다. 진행하겠습니까?", onCheck);
            dialog.dismiss();
        });

        dialog.show();
    }


    private void registerDateChangedReceiver(Context context)
    {
        dateChangedReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if(Intent.ACTION_DATE_CHANGED.equals(intent.getAction())    // 시스템에서 자정일 때 
                || Intent.ACTION_TIME_CHANGED.equals(intent.getAction()))   // 사용자가 수동으로 바꿀 때
                {
                    today = CalenderUtils.instance().getTodaybyCalender();
                    changeMainCalenderByYearMonth(context);

                    InformUtils.instance().ShowInformYes(context,
                            "디버그 : DateChangeReceiver 에서 날짜가 변경됨을 확인");
                }
            }
        };

        // 콘텍스트에서 filter 에 추가한 action 만 수신하여 context 에 전달
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);

        context.registerReceiver(dateChangedReceiver, filter);
    }

    // MainInterface 가 싱글턴처럼 종속하기에, 호출할 필요 없지만, 만약 부모 클래스가 파괴되는 구조라면, 
    // 아래처럼 수동으로 메모리 해제 필요
    private void unregisterReceiver(Context context)
    {
        if(dateChangedReceiver != null)
        {
            context.unregisterReceiver(dateChangedReceiver);
            dateChangedReceiver = null;
        }
    }
}
