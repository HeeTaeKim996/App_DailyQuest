package com.example.dailyquest.Data.Fixed.FixedCategoryChild;

import com.example.dailyquest.Data.Fixed.FixedCategory;
import com.example.dailyquest.Data.Fixed.FixedCategoryEnum;

import java.io.DataInputStream;
import java.io.DataOutputStream;

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
    public boolean saveToDos(DataOutputStream dos)
    {
        return true;
    }
}
