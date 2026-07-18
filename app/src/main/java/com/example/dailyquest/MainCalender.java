package com.example.dailyquest;

import android.content.Context;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainCalender
{
    public int year;
    public int month;
    public int maxDate;

    private int offset;
    private File baseFile;
    private File dataFile;
    private File shortTodosFile;

    private ArrayList<DateProxy> proxies;
    private int[] shortTodos;


    public MainCalender(Context context, int InYear, int InMonth)
    {
        baseFile = new File(context.getFilesDir(), String.valueOf(InYear));
        dataFile = new File(baseFile, "D");
        shortTodosFile = new File(baseFile, "st.std");

        year = InYear;
        month = InMonth;

        offset = CalenderUtils.instance().getFirstDayFromYearMonth(InYear, InMonth);
        maxDate = CalenderUtils.instance().getLastDateFromYearMonth(InYear, InMonth);


        if(baseFile.exists() == false)
        {
            makeDefaultProxies();
        }
        else
        {
            loadProxiesFromFile();
        }
    }

    private void makeDefaultProxies()
    {
        shortTodos = new int[maxDate];

        int offsetMaxDate = offset + maxDate - 1;
        proxies = new ArrayList<DateProxy>(42);

        for(int i = 0; i < offset; i++)
        {
            proxies.add(new DateProxy.Builder().setIsCurrMonth(false).create());
        }
        for(int i = offset; i <= offsetMaxDate; i++)
        {
            int dateNumber = i - offset + 1;
            proxies.add(new DateProxy.Builder().setDate(dateNumber).setIsCurrMonth(true)
                .create());
        }
        int firstDate = 1;
        for(int i = offsetMaxDate + 1; i < 42; i++)
        {
            proxies.add(new DateProxy.Builder().setDate(firstDate++).setIsCurrMonth(false)
                .create());
        }

        int lastMonthsLastDate;
        if(month == 1)
        {
            lastMonthsLastDate = CalenderUtils.instance()
                    .getLastDateFromYearMonth(year - 1, 12);
        }
        else
        {
            lastMonthsLastDate = CalenderUtils.instance()
                    .getLastDateFromYearMonth(year, month - 1);
        }

        for(int i = offset - 1; i >= 0; i--)
        {
            proxies.get(i).date = lastMonthsLastDate--;
        }
    }

    private void loadProxiesFromFile()
    {
        shortTodos = new int[maxDate];

        try(DataInputStream dis = new DataInputStream(new FileInputStream(shortTodosFile)))
        {
            int len = dis.readInt();
            if(shortTodos.length != len)
            {
                Log.d("MainCalender", "저장/로드의 len 길이가 다름. 로드하지 않음");
                return;
            }

            for(int i = 0; i < len; i++)
            {
                shortTodos[i]= dis.readInt();
            }
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    public Date loadDate(DateProxy proxy)
    {
        File dateFile = new File(dataFile, String.valueOf(proxy.date));
        if(dateFile.exists() == false)
        {
            return new Date.Builder().setDate(proxy.date).create();
        }

        Date date = new Date();
        try(DataInputStream dis = new DataInputStream(new FileInputStream(dateFile)))
        {
            int todoCount = dis.readInt();

            while(todoCount-- > 0)
            {
                Todo todo = new Todo();
                date.todos.add(todo);
                
                todo.isCompleted = dis.readBoolean();

                todo.mainText = dis.readUTF();
                todo.explainText = dis.readUTF();

                todo.alarmTimes = dis.readInt();
                todo.setColor((int)dis.readByte());

                int subTodoCount = dis.readInt();
                while(subTodoCount-- > 0)
                {
                    SubTodo subTodo = new SubTodo();
                    todo.subTodos.add(subTodo);

                    subTodo.bCompleted = dis.readBoolean();
                    subTodo.subText = dis.readUTF();
                }
            }
        }
        catch (IOException e) { e.printStackTrace(); }

        return date;
    }


    public List<DateProxy> getProxies()
    {
        return proxies;
    }



    public DateProxy saveDate(Date date)
    {
        int shortTodo = makeShortTodo(date);
        shortTodos[date.date - 1] = shortTodo;
        DateProxy dateProxy = proxies.get(date.date - 1);
        dateProxy.todos = shortTodo;

        // Delete File if Exists
        if(date.todos.size() == 0)
        {
            File dateFile = new File(dataFile, String.valueOf(date.date));
            if(dateFile.exists())
            {
                dateFile.delete();

                File[] files = dataFile.listFiles();
                if(files.length == 0)
                {
                    removeBaseFile();
                }
            }

        }
        // Save File
        else
        {
            // 기본 파일(BaseFile, ShortTodoFile) 이 없다면, 함께 생성
            if(baseFile.exists() == false)
            {
                baseFile.mkdirs();
                saveShortTodos();
            }

            File dateFile = new File(dataFile, String.valueOf(date.date));
            try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(dateFile)))
            {
                int todoCount = date.todos.size();
                dos.writeInt(todoCount);

                for(Todo todo : date.todos)
                {
                    dos.writeBoolean(todo.isCompleted);

                    dos.writeUTF(todo.mainText);
                    dos.writeUTF(todo.explainText);

                    dos.writeInt(todo.alarmTimes);
                    dos.writeByte((byte)todo.getColor());

                    int subTodoCount = todo.subTodos.size();
                    dos.writeInt(subTodoCount);

                    for(SubTodo subTodo : todo.subTodos)
                    {
                        dos.writeBoolean(subTodo.bCompleted);
                        dos.writeUTF(subTodo.subText);
                    }
                }
            }
            catch (IOException e) { e.printStackTrace(); }
        }

        return dateProxy;
    }

    private int makeShortTodo(Date date)
    {
        int shortTodo = 0;
        int size = date.todos.size();
        if(size > 0)
        {
            int colValue = date.todos.get(0).getColor();
            shortTodo = colValue;
        }

        for(int i = 1; i < size && i < StaticValues.shortTodoCount; i++)
        {
            shortTodo <<= 3;
            int colValue = date.todos.get(i).getColor();
            shortTodo |= colValue;
        }

        return shortTodo;
    }

    private void saveShortTodos()
    {
        try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(shortTodosFile)))
        {
            dos.writeInt(shortTodos.length);

            for(int shortTodo : shortTodos)
            {
                dos.writeInt(shortTodo);
            }
        }
        catch(IOException e) { e.printStackTrace(); }
    }


    private void removeBaseFile()
    {
        deleteChildrenFiles(baseFile);
        baseFile.delete();
    }
    private void deleteChildrenFiles(File me)
    {
        File [] files = me.listFiles();
        for(File child : files)
        {
            if(child.isFile())
            {
                deleteChildrenFiles(child);
            }
            child.delete();
        }
    }

    public int getOffset()
    {
        return offset;
    }

}
