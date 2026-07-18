package com.example.dailyquest;

import android.content.Context;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainCalender
{
    public int year;
    public int month;
    public int maxDate;

    private int offset;
    private File yearFile;
    private File baseFile;
    private File dataFile;
    private File shortTodosFile;

    private DateProxy[] proxies;
    private int[] shortTodos;


    public MainCalender(Context context, int InYear, int InMonth)
    {
//        DevelopUtils.instance().clearAllFiles();


        yearFile = new File(StaticValues.rootFile, String.valueOf(InYear));
        baseFile = new File(yearFile, String.valueOf(InMonth));
        dataFile = new File(baseFile, "D");
        shortTodosFile = new File(baseFile, "st.std");

        year = InYear;
        month = InMonth;

        offset = CalenderUtils.instance().getFirstDayFromYearMonth(InYear, InMonth);
        maxDate = CalenderUtils.instance().getLastDateFromYearMonth(InYear, InMonth);


        makeProxies();
    }

    private void makeProxies()
    {
        int offsetMaxDate = offset + maxDate - 1;
        proxies = new DateProxy[42];

        for(int i = 0; i < offset; i++)
        {
            proxies[i] = new DateProxy.Builder().setIsCurrMonth(false).create();
        }
        for(int i = offset; i <= offsetMaxDate; i++)
        {
            int dateNumber = i - offset + 1;
            proxies[i] = new DateProxy.Builder().setIsCurrMonth(true).create();
        }
        int firstDate = 1;
        for(int i = offsetMaxDate + 1; i < 42; i++)
        {
            proxies[i] = new DateProxy.Builder().setDate(firstDate++).setIsCurrMonth(false)
                    .create();
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
            proxies[i].date = lastMonthsLastDate--;
        }

        shortTodos = new int[maxDate];

        if(baseFile.exists())
        {
            loadShortTodosFromFile();
        }

        for(int i = 0; i < maxDate; i++)
        {
            DateProxy proxy = proxies[i + offset];
            proxy.date = i + 1;
            proxy.todos = shortTodos[i];
        }
    }



    private void loadShortTodosFromFile()
    {
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
        date.date = proxy.date;

        try(DataInputStream dis = new DataInputStream(new FileInputStream(dateFile)))
        {
            int todoCount = dis.readInt();

            while(todoCount-- > 0)
            {
                Todo todo = new Todo();
                date.todos.add(todo);
                todo.setParentDate(date);

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


    public DateProxy[] getProxies()
    {
        return proxies;
    }



    public DateProxy saveDate(Date date)
    {
        int shortTodo = makeShortTodo(date);
        shortTodos[date.date - 1] = shortTodo;
        DateProxy dateProxy = proxies[offset + date.date - 1];
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

                    files = yearFile.listFiles();
                    if(files.length == 0)
                    {
                        removeYearFile();
                    }
                }
                else
                {
                    saveShortTodos();
                }
            }

        }
        // Save File
        else
        {
            // 기본 파일(BaseFile, ShortTodoFile) 이 없다면, 함께 생성
            if(baseFile.exists() == false)
            {
                dataFile.mkdirs();
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

            saveShortTodos();
        }

        return dateProxy;
    }

    private void saveShortTodos()
    {
        try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(shortTodosFile)))
        {
            dos.writeInt(shortTodos.length);

            for(int shortNum : shortTodos)
            {
                dos.writeInt(shortNum);
            }
        }
        catch(IOException e) { e.printStackTrace(); }
    }

    private int makeShortTodo(Date date)
    {
        int shortTodo = 0;
        int size = date.todos.size();
        if(size > StaticValues.shortTodoCount)
        {
            size = StaticValues.shortTodoCount;
        }


        int mul = 1;
        for(int i = 0; i < size ; i++)
        {
            int colValue = date.todos.get(i).getColor();
            shortTodo |= (colValue * mul);
            mul <<= 3;
        }

        return shortTodo;
    }



    private void removeBaseFile()
    {
        deleteChildrenFiles(baseFile);
        baseFile.delete();
    }
    private void removeYearFile()
    {
        deleteChildrenFiles(yearFile);
        yearFile.delete();
    }

    private void deleteChildrenFiles(File me)
    {
        File [] files = me.listFiles();
        for(File child : files)
        {
            if(child.isFile() == false)
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
