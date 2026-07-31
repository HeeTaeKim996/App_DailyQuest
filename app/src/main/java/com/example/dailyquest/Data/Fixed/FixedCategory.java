package com.example.dailyquest.Data.Fixed;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public abstract class FixedCategory
{
    protected FixedCategory(FixedCategoryEnum InFixedCategoryEnum)
    {
        fixedCategoryEnum = InFixedCategoryEnum;
    }

    public final FixedCategoryEnum fixedCategoryEnum;

    public abstract String getSummary();
    public abstract boolean loadFromDis(DataInputStream dis);

    public abstract boolean saveToDos(DataOutputStream dos);

    public abstract void paint(short fixedTodoIndex, int year, int quarter,
                               List<TreeMap<Byte, ArrayList<Short>>> filled);




}
