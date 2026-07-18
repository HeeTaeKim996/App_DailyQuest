package com.example.dailyquest;

import java.util.ArrayList;

public class Date
{
    public Date(){}
    public Date(int InDate) { date = InDate;}
    public int date;
    public ArrayList<Todo> todos = new ArrayList<>();



    public static class Builder
    {
        private Date _date;
        public Builder()
        {
            _date = new Date();
        }

        public Date create()
        {
            return _date;
        }

        public Builder setDate(int InDate)
        {
            _date.date = InDate;
            return this;
        }


    }
}
