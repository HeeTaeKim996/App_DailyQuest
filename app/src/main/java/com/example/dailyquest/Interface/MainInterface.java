package com.example.dailyquest.Interface;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;

import com.example.dailyquest.Data.Fixed.FixedTodo;
import com.example.dailyquest.Data.Time;
import com.example.dailyquest.FixedTodo.FixedTodoManager;
import com.example.dailyquest.Interface.FixedTodo.FixedTodoPage;
import com.example.dailyquest.Notialarm.NotiUpdateTimeEnum;
import com.example.dailyquest.Notialarm.NotialarmManager;
import com.example.dailyquest.Utils.BackgroundColorUtils;
import com.example.dailyquest.Utils.CalenderUtils;
import com.example.dailyquest.Data.Date;
import com.example.dailyquest.Data.Todo;
import com.example.dailyquest.Data.DateProxy;
import com.example.dailyquest.Utils.DevelopUtils;
import com.example.dailyquest.MainCalender;
import com.example.dailyquest.Small.MainFuncEnum;
import com.example.dailyquest.Notialarm.NotificationHelper;
import com.example.dailyquest.R;
import com.example.dailyquest.Small.ISwapCompleteFunc;
import com.example.dailyquest.Small.ISwapableItem;
import com.example.dailyquest.Small.StaticValues;
import com.example.dailyquest.Notialarm.Receiver.TodoMidnightReceiver;
import com.example.dailyquest.Utils.InformUtils;
import com.example.dailyquest.Utils.ZipUtils;
import com.example.dailyquest.databinding.ActivityMainBinding;
import com.example.dailyquest.databinding.CalenderPickerBinding;
import com.example.dailyquest.databinding.ItemDateTodoListBinding;
import com.example.dailyquest.databinding.ItemFixedTodoShortInfoInDateListBinding;
import com.example.dailyquest.databinding.ItemTodoShortInfoBinding;
import com.example.dailyquest.databinding.OthersBinding;
import com.example.dailyquest.databinding.OthersFixedTodoBinding;
import com.example.dailyquest.databinding.TodoInfoBinding;
import com.example.dailyquest.databinding.UtilsOneSpinnerPickerBinding;
import com.example.dailyquest.databinding.YearMonthPickerBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    public MainInterface(Context context)
    {
//        resetData(context);

        FixedTodoManager.initialize(context);

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
        mainBinding.textViewYearMonth.setOnClickListener(v->
        {
            show_yearMonthPicker(context);
        });


        registerDateChangedReceiver(context);

        if(NotialarmManager.instance().isNotificationActive(context,
                NotialarmManager.instance().CHANNEL_ID,
                NotialarmManager.instance().NOTIFICATION_ID) == false)
        {
            TodoMidnightReceiver.updateTodayNotification(context);
        }
        else
        {
//            InformUtils.instance().ShowInformYes(context, "디버깅 : 이미  Notification 이 활성화됨");
        }



        // [주의] 수동으로 날짜를 바꾸는 경우, Alarm 이 엉뚱하게 유지되기 때문에, 그 때에는 수동으로 수정 필요
        if(TodoMidnightReceiver.isAlarmScheduled(context) == false)
        {
            TodoMidnightReceiver.scheduleAlarm(context, NotiUpdateTimeEnum.EVERY_MIDNIGHT);
        }
        else
        {
//            InformUtils.instance().ShowInformYes(context, "디버깅 : 이미 Alarm 이 활성화됨");
        }


        FixedTodoManager.instance().addOnFixedTodosUpdateListener(onFixedTodosUpdated);
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

            cellView.setClickable(false);
            cellView.setOnClickListener(null);

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
            dayText.setTextColor(Color.parseColor("#000000"));

            cellView.setClickable(true);
            cellView.setOnClickListener(v->
            {
                show_date_todoListDialog(context, proxy, pos);
            });

            if(yearMonthState == YearMonthState.CURR && proxy.date == today.date)
            {
                cellView.setBackgroundResource(R.drawable.date_background_today);
            }

            int index = 0;

            GridLayout boxGrid = cellView.findViewById(R.id.gridLayout_calenderDate);
            int fixedTodos = proxy.fixedTodos & 0x3F_FF_FF_FF; // 개당 3비트. 총 10개 사용
            int todos = proxy.todos & 0x3F_FF_FF_FF; // 개당 3비트. 총 10개 사용

            // fixedTodos, todos 도합 20개 사용할 수 있지만, boxGrid 최대 갯수는 10개로 제한함
            for(; index < StaticValues.shortTodoCount && fixedTodos > 0; index++)
            {
                View box = boxGrid.getChildAt(index);

                int colInt = fixedTodos & 7;
                box.setBackgroundColor(BackgroundColorUtils.getColorByDark(context, colInt));
                fixedTodos >>= 3;
            }

            for(; index < StaticValues.shortTodoCount; index++)
            {
                View box = boxGrid.getChildAt(index);
                if(todos == 0)
                {
                    box.setBackgroundColor(Color.TRANSPARENT);
                }
                else
                {
                    int colInt = todos & 7;
                    box.setBackgroundColor(BackgroundColorUtils.getColorByDark(context, colInt));
                    todos >>= 3;
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

        Date date = calender.loadDate(proxy.date);



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


                saveDate(date);
            }
        };
        SwapableItemsContainer swappableContainer = binding.linearLayoutScrollView;
        swappableContainer.setSwapCompleteFunc(swapCompleteFunc);



        Runnable onEmptyTodos = ()->
        {
//            ViewGroup.LayoutParams layoutParams = binding.linearLayoutScrollView.getLayoutParams();
//            float density = context.getResources().getDisplayMetrics().density;
//            layoutParams.height = (int)(density * 40);
//            binding.linearLayoutScrollView.setLayoutParams(layoutParams);
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
                        deleteTodo(dTodo);

                        binding.linearLayoutScrollView.removeView(dInterface);
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
                        saveDate(date);
                    }
                });

                shortInfo.textViewShortMainText.setOnClickListener(v->
                {
                    show_todo_info(context, todo, false);
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

            show_todo_info(context, newTodo, true);
            dialog.dismiss();
        });


        ArrayList<FixedTodo> fixedTodos = calender.loadFixedTodos(date.date);
        if(fixedTodos != null)
        {
            LinearLayout fixedTodosLayout = binding.linearLayoutItemDateTodoListFixedTodos;

            for(FixedTodo fixedTodo : fixedTodos)
            {
                ItemFixedTodoShortInfoInDateListBinding fixedBinding
                        = ItemFixedTodoShortInfoInDateListBinding
                        .inflate(LayoutInflater.from(context));
                TextView summaryText = fixedBinding.textViewFixedTodoShortInCalenderSummaryText;
                summaryText.setText(fixedTodo.getSummary());

                fixedBinding.getRoot().setBackgroundColor(BackgroundColorUtils.getColorByLight(context,
                        fixedTodo.getColor()));

                fixedTodosLayout.addView(fixedBinding.getRoot());
            }
        }
    }

    private void show_todo_info(Context context, Todo todo, boolean isDirectEditing)
    {
        TodoInfoBinding binding = TodoInfoBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();

        final boolean[] isEditMode = {false};


        TodoInfoInterface infoInterface = binding.getRoot();
        Runnable shutDownThisDialog = ()->
        {
            if(dialog != null & dialog.isShowing())
            {
                dialog.dismiss();
            }
        };
        infoInterface.initialize(todo, mainListenerFunc, shutDownThisDialog);



        Runnable toViewMode = ()->
        {
            Date date = todo.getParentDate();
            if(date == null)
            {
                InformUtils.instance().ShowInformYes(context,
                                "디버깅 : toViewMode date == null (1)");
            }

            // 1차 삭제 여부 확인
            if(infoInterface.toViewMode() == false)
            {
                // 2차 삭제 여부 확인 ( infoInterface.toViewMode() 의 리턴값이 1차 확인)
                if(todo.mainText.equals("") && todo.explainText.equals("")
                        && todo.subTodos.size() == 0)
                {
                    date.todos.remove(todo);
                    dialog.dismiss();
                }
            }

            if(date == null)
            {
                InformUtils.instance().ShowInformYes(context,
                        "디버깅 : toViewMode date == null (2)");
            }
            saveDate(date);
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

    public BiConsumer<Todo, MainFuncEnum> mainListenerFunc = (Todo todo, MainFuncEnum mainFuncEnum)->
    {
        switch(mainFuncEnum)
        {
            case None:
                return;

            case SaveDate:
                saveDate(todo.getParentDate());
                break;

            case LoadCalender:
                loadCalender(todo);
                break;

            case DeleteTodo:
                deleteTodo(todo);
                break;
        }
    };

    private void saveDate(Date date)
    {
        Context context = getRootView().getContext();
        if(date == null)
        {
            InformUtils.instance().ShowInformYes(context,
                    "디버깅 : SaveDate 함수가 호출됐지만, date == null");
            return;
        }

        DateProxy proxy = calender.saveDate(date);
        if(proxy == null)
        {
            InformUtils.instance().ShowInformYes(context,
                    "데이터 저장 도중 오류가 발생했습니다");
        }
        else
        {
            InformUtils.instance().showToast(context, "저장됨");
        }

        int pos = calender.getOffset() + proxy.date - 1;

        View cellView = cellViews[pos];

        updateDateCell(context, cellView, proxy, pos);


        if(yearMonthState == YearMonthState.CURR && date.date == today.date)
        {
            updateNotification(context, date);
        }
    }



    private void updateNotification(Context context, Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        Time time = new Time(calendar);

        ArrayList<FixedTodo> fixedTodos = calender.loadFixedTodos(date.date);
        NotificationHelper.updateTodayNotification(context, time, date.todos, fixedTodos,
                false);
    }
    private void updateNotification(Context context)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        Time time = new Time(calendar);

        Date date = calender.loadDate(time.date);

        ArrayList<FixedTodo> fixedTodos = calender.loadFixedTodos(date.date);
        NotificationHelper.updateTodayNotification(context, time, date.todos, fixedTodos,
                false);
    }


    private void loadCalender(Todo todo)
    {
        Context context = getRootView().getContext();;
        CalenderPickerBinding binding = CalenderPickerBinding.inflate(LayoutInflater
                .from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();

        CalenderPicker calenderPicker = binding.getRoot();


        CalenderPicker.YearMonthDate picked = new CalenderPicker.YearMonthDate(year,
                month, todo.getParentDate().date);
        calenderPicker.initialize(picked);

        binding.buttonCalenderPickerOk.setOnClickListener(v->
        {
            changeTodosDate(todo, picked);
            dialog.dismiss();
        });
        binding.buttonCalenderPickerCancel.setOnClickListener(v->
        {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void changeTodosDate(Todo todo, CalenderPicker.YearMonthDate toYearMonthDate)
    {
        // 함수 호출로 연-월 이 바뀔시, changeMainCalender로 mainCalender를 해당 연-월로 바꾼다.
        // 따라서 이함수를 호출하는 t.odo 의 연-월은 언제나 mainInterface 의 year, month 이다

        Context context = getRootView().getContext();
        todo.isCompleted = false;

        Date beforeDate = todo.getParentDate();
        int fromDate = beforeDate.date;


        if(year == toYearMonthDate.year && month == toYearMonthDate.month)
        {
            if(fromDate == toYearMonthDate.date)
            {
                // isCompleted 재설정 외에 추가 처리 필요 없음
                saveDate(beforeDate);
                return;
            }
            else
            {
                beforeDate.todos.remove(todo);
                saveDate(beforeDate);
            }
        }
        else
        {
            beforeDate.todos.remove(todo);
            saveDate(beforeDate);

            year = toYearMonthDate.year;
            month = toYearMonthDate.month;
            changeMainCalenderByYearMonth(context);
        }

        Date toDate = calender.loadDate(toYearMonthDate.date);

        toDate.todos.add(todo);
        todo.setParentDate(toDate);

        saveDate(toDate);
    }


    private void resetData(Context context)
    {
        DevelopUtils.instance().clearAllFiles(context);
    }
    private void onDataResetted(Context context)
    {
        FixedTodoManager.reset(context);
        changeMainCalenderByYearMonth(context);
    }

    private void show_others_panel(Context context)
    {
        OthersBinding binding = OthersBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();

        binding.buttonShowAllFiles.setOnClickListener(v->
        {
            InformUtils.instance().ShowInformYes(context,
                    DevelopUtils.instance().getAllFiles(context).toString());
            dialog.dismiss();
        });


        binding.buttonClearAllData.setOnClickListener(v->
        {
            Consumer<Boolean> onCheck = (Boolean bYes)->
            {
                if(bYes)
                {
                    resetData(context);
                    onDataResetted(context);
                }
            };

            InformUtils.instance().ShowYesOrNo(context,
                    "데이터가 모두 삭제됩니다. 진행하겠습니까?", onCheck);
            dialog.dismiss();
        });

        binding.buttonOthersShowFixedTodoPage.setOnClickListener(v->
        {
            showFixedTodoPage(context);
            dialog.dismiss();
        });

        binding.buttonOthersShowNotialarmUpdatePage.setOnClickListener(v->
        {
            show_NotialarmUpdateTime_setPage(context);
            dialog.dismiss();
        });

        binding.buttonOthersExportZip.setOnClickListener(v->
        {
            if(ZipUtils.makeZip(context))
            {
                InformUtils.instance().ShowInformYes(context,
                        "압축 성공");
            }
            else
            {
                InformUtils.instance().ShowInformYes(context,
                        "압축 실패");
            }
        });
        binding.buttonOthersImportFromZip.setOnClickListener(v->
        {
            Consumer<Boolean> check = (Boolean succeed)->
            {
                if(succeed)
                {
                    onDataResetted(context);
                    InformUtils.instance().ShowInformYes(context,
                            "ZIP으로부터 데이터 불러오기 성공");
                }
                else
                {
                    InformUtils.instance().ShowInformYes(context,
                            "ZIP으로부터 데이터 불러오기 실패");
                }
            };

            ZipUtils.tryImportFromZip(context, check);
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
                    // 여기서 changeMainCalender 호출하면 안된다. 만약 기존 캘린더 기준으로 dialog가 열려있고,
                    // saveData 했을 때, 여기서 calender를 바꿨다면, null 참조할듯


                    InformUtils.instance().ShowInformYes(context,
                            "디버그(MainInterface.java 하단) : DateChangeReceiver 에서 날짜가 변경됨을 확인");
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

    private void show_yearMonthPicker(Context context)
    {
        YearMonthPickerBinding binding = YearMonthPickerBinding.inflate(LayoutInflater
                .from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();
        YearMonthPicker yearMonthPicker = binding.getRoot();
        yearMonthPicker.initialize(new YearMonthPicker.YearMonth(year, month));
        binding.buttonYearMonthPickerCancel.setOnClickListener(v->
        {
            dialog.dismiss();
        });
        binding.buttonYearMonthPickerOk.setOnClickListener(v->
        {
            YearMonthPicker.YearMonth yearMonth = yearMonthPicker.getYearMonth();
            year = yearMonth.year;
            month = yearMonth.month;
            changeMainCalenderByYearMonth(context);
            dialog.dismiss();
        });


        dialog.show();
    }


    private void deleteTodo(Todo todo)
    {
        Date parentDate = todo.getParentDate();
        parentDate.todos.remove(todo);
        saveDate(parentDate);
    }

    private void showFixedTodoPage(Context context)
    {
        OthersFixedTodoBinding binding = OthersFixedTodoBinding.inflate(LayoutInflater
                .from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();

        FixedTodoPage fixedTodoPage = (FixedTodoPage) binding.getRoot();
        fixedTodoPage.initialize(context);

        dialog.show();
    }


    private Runnable onFixedTodosUpdated = ()->
    {
        // FixedTodos 가 바뀌었으므로, mainCalender 를 새로 로드 ( mainCalender 에서 새로 반영 )
        changeMainCalenderByYearMonth(getRootView().getContext());

        // Notification 도 바뀔 가능성이 있으므로, 수정 요청
        updateNotification(getRootView().getContext());
    };


    private void show_NotialarmUpdateTime_setPage(Context context)
    {
        UtilsOneSpinnerPickerBinding binding = UtilsOneSpinnerPickerBinding.inflate
                (LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();

        ArrayAdapter<NotiUpdateTimeEnum> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, NotiUpdateTimeEnum.values());

        binding.spinnerUtilsOneSpinnerPicker.setAdapter(adapter);

        binding.buttonUtilsOneSpinnerPickerOk.setOnClickListener(v->
        {
            NotiUpdateTimeEnum updateTimeEnum = (NotiUpdateTimeEnum)
                    binding.spinnerUtilsOneSpinnerPicker.getSelectedItem();

            TodoMidnightReceiver.scheduleAlarm(context,
                    updateTimeEnum);

            dialog.dismiss();
        });

        dialog.show();
    }
}
