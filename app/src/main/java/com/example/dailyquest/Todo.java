package com.example.dailyquest;

import java.lang.ref.WeakReference;
import java.util.List;

public class Todo
{
    public WeakReference<Date> parentDate;
    public boolean isCompleted = false;
    public String mainText = "";
    public String explainText = "";
    public List<SubTodo> subTodos = null;
}
