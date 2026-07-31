package com.example.dailyquest.Data.Fixed.FixedCategoryChild;

import com.example.dailyquest.Data.Fixed.FixedCategory;
import com.example.dailyquest.Data.Fixed.FixedCategoryEnum;
import com.example.dailyquest.Data.Fixed.FixedTodo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class FixedCategory_None extends FixedCategory
{
    public FixedCategory_None()
    {
        super(FixedCategoryEnum.NONE);
    }

    @Override
    public  String getSummary() { return "FC[NotAssigned]";}

    @Override
    public boolean loadFromDis(DataInputStream dis)
    {
        return true;
    }

    @Override
    public boolean saveToDos(DataOutputStream dos) { return true; }

    @Override
    public void paint(short fixedTodoIndex, int year, int quarter,
                      List<TreeMap<Byte, ArrayList<Short>>> filled)
    {}
}
