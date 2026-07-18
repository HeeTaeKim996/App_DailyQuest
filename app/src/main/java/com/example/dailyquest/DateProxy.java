package com.example.dailyquest;

import android.os.Build;

public class DateProxy
{
    public int date;
    public boolean isCurrMonth;
    public int todos;

    public static class Builder
    {
        private DateProxy _proxy;

        public Builder()
        {
            _proxy = new DateProxy();
        }

        public DateProxy create()
        {
            return _proxy;
        }

        public Builder setDate(int InDate)
        {
            _proxy.date = InDate;
            return this;
        }

        public Builder setIsCurrMonth(boolean InIsCurrMonth)
        {
            _proxy.isCurrMonth = InIsCurrMonth;
            return this;
        }

        public Builder setTodos(int InTodos)
        {
            _proxy.todos = InTodos;
            return this;
        }




    }
}
