package com.example.dailyquest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyquest.databinding.ItemDateTodoListBinding;
import com.example.dailyquest.databinding.ItemTodoShortInfoBinding;
import com.example.dailyquest.databinding.TodoInfoBinding;

import java.util.ArrayList;
import java.util.function.Supplier;

public class CalenderAdapter extends RecyclerView.Adapter<CalenderAdapter.CalenderViewHolder>
{
    private final MainCalender calender;
    private final CalenderUtils.Calender today;
    private final boolean isCurrMonth;

    public CalenderAdapter(MainCalender InCalender)
    {
        calender = InCalender;
        today = CalenderUtils.instance().getTodaybyCalender();
        isCurrMonth = (calender.year == today.year && calender.month == today.month);
    }

    @NonNull
    @Override
    public CalenderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calender_date, parent, false);

        int rowHeight = parent.getHeight() / 6;

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = rowHeight;
        view.setLayoutParams(layoutParams);


        return new CalenderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalenderViewHolder holder, int position)
    {
        Context context = holder.itemView.getContext();

        Date date = calender.getDates().get(position);
        holder.dayText.setText(String.valueOf(date.date));


        if(date.isCurrMonth == false)
        {
            holder.itemView.setBackgroundResource(R.drawable.date_background_not_used);
            ((TextView)holder.itemView.findViewById(R.id.textView_date))
                    .setTextColor(Color.parseColor("#888888"));
        }
        else
        {
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(v->
            {
                show_date_todoListDialog(context, date, position);
            });

            if(isCurrMonth && date.date == today.date)
            {
                holder.itemView.setBackgroundResource(R.drawable.date_background_today);
            }
        }
    }

    private void onDateUpdated(Date date, int position)
    {
        notifyItemChanged(position);
        calender.inform_dateUpdated(date);
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
            for(int i = 0; i < date.todos.size(); i++)
            {
                Todo todo = date.todos.get(i);

                ItemTodoShortInfoBinding shortInfo = ItemTodoShortInfoBinding
                        .inflate(LayoutInflater.from(context));
                if(todo.isCompleted)
                {
                    shortInfo.buttonIsFinished.setText("C");
                }
                else
                {
                    shortInfo.buttonIsFinished.setText("Y");
                }
                shortInfo.buttonIsFinished.setOnClickListener(v->
                {
                    if(todo.isCompleted) return;

                    todo.isCompleted = true;
                    shortInfo.buttonIsFinished.setText("C");
                });

                shortInfo.textViewShortMainText.setText(todo.mainText);
                shortInfo.textViewShortMainText.setOnClickListener(v->
                {
                    show_todo_info(context, date, todo, position);
                    dialog.dismiss();
                });

                binding.linearLayoutScrollView.addView(shortInfo.getRoot());
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
            if(position == getItemCount() - 1) return;
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
            date.todos.add(newTodo);


            show_todo_info(context, date, newTodo, position);
            dialog.dismiss();
        });

    }

    private void show_todo_info(Context context, Date date, Todo todo, int position)
    {
        TodoInfoBinding binding = TodoInfoBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();
        dialog.show();



        final boolean[] isEditMode = {false};
        // [] 배열은 자바에서 객체에 포함되어, 메모리를 힙메모리 고정 할당. 따라서 위 함수 발동시 힙메모리에
        // 1 바이트 할당됨. 
        // 아래의 button.setOnClickListener.. 등이 이 배열의 주소를 참조하기에, GC가 메모리를 수거해가지 않음.
        // 만약 dialog.dismiss(); 로 button 들이 소멸하면, GC는 isEditNode 배열을 수거

        Supplier<TypedArray> makeEditTextBackground = ()->
        {
            int[] attrs = new int[]{android.R.attr.editTextBackground};
            return context.obtainStyledAttributes(attrs);
        };


        binding.editTextMainText.setText(todo.mainText.toString());
        binding.editTextExplainText.setText(todo.explainText.toString());

        binding.buttonSecondRight.setOnClickListener(v->
        {
            if(isEditMode[0] == false)
            {
                isEditMode[0] = true;

                binding.editTextMainText.setFocusableInTouchMode(true);
                binding.editTextMainText.setFocusable(true);
                binding.editTextMainText.setCursorVisible(true);
                TypedArray ta = makeEditTextBackground.get();
                binding.editTextMainText.setBackground(ta.getDrawable(0));
                ta.recycle();
                binding.editTextMainText.requestFocus();

                binding.editTextExplainText.setFocusableInTouchMode(true);
                binding.editTextExplainText.setFocusable(true);
                binding.editTextExplainText.setCursorVisible(true);
                TypedArray ta2 = makeEditTextBackground.get();
                binding.editTextExplainText.setBackground(ta2.getDrawable(0));
                ta2.recycle();


                binding.buttonSecondRight.setText("N");
            }
        });

        binding.buttonLeft.setOnClickListener(v->
        {
            if(isEditMode[0])
            {
                isEditMode[0] = false;

                todo.mainText = binding.editTextMainText.getText().toString();
                todo.explainText = binding.editTextExplainText.getText().toString();
                onDateUpdated(date, position);


                binding.editTextMainText.setFocusableInTouchMode(false);
                binding.editTextMainText.setFocusable(false);
                binding.editTextMainText.setCursorVisible(false);
                binding.editTextMainText.setBackgroundColor(Color.TRANSPARENT);

                binding.editTextExplainText.setFocusableInTouchMode(false);
                binding.editTextExplainText.setFocusable(false);
                binding.editTextExplainText.setCursorVisible(false);
                binding.editTextExplainText.setBackgroundColor(Color.TRANSPARENT);

                binding.buttonSecondRight.setText("C");


            }
            else
            {
                dialog.dismiss();
            }
        });
    }


















    @Override
    public int getItemCount()
    {
        return calender.getDates().size();
    }

    public static class CalenderViewHolder extends RecyclerView.ViewHolder
    {
        TextView dayText;

        public CalenderViewHolder(@NonNull View itemView)
        {
            super(itemView);

            dayText = itemView.findViewById(R.id.textView_date);
        }
    }
}
