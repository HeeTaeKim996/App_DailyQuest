package com.example.dailyquest.Data.Fixed;

import java.io.DataInputStream;
import java.io.DataOutputStream;

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
}
