package com.example.dailyquest.Data.Fixed;

import java.util.ArrayList;

public class FixedShortInfo
{
    public FixedShortInfo(Byte InDate) { date = InDate;}
    public Byte date;
    public ArrayList<FixedTodo> fixedTodos = new ArrayList<>();
}
