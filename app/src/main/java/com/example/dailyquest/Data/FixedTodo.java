package com.example.dailyquest.Data;


public class FixedTodo extends ParentTodo
{
    public static enum Category
    {
        EVERY_YEAR,
        EVERY_MONTH,
        EVERY_WEEK
    }
    private Category _category = Category.EVERY_YEAR;
    public Category getCategory() { return _category;}
    public void setCategory(Category InCategory) { _category = InCategory;}
}
