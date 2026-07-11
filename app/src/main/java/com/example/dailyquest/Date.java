package com.example.dailyquest;

import java.util.ArrayList;

public class Date
{
    public Date(){}
    public Date(int InDate) { date = InDate;}
    public int date;
    public boolean isCurrMonth = false;
    public ArrayList<Todo> todos;

    public static class Builder
    {
        Date _date;
        public Builder()
        {
            _date = new Date();
        }

        public Builder setDate(int InDate)
        {
            _date.date = InDate;
            return this;
        }
        public Builder setIsCurrMonth(boolean InIsCurrMonth)
        {
            _date.isCurrMonth = InIsCurrMonth;
            return this;
        }
        public Date create()
        {
            return _date;
        }

    }
}
