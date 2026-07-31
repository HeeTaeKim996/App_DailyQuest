package com.example.dailyquest.FixedTodo;

import android.content.Context;

import com.example.dailyquest.Data.Fixed.FixedCategory;
import com.example.dailyquest.Data.Fixed.FixedCategoryChild.FixedCategory_everyMonth;
import com.example.dailyquest.Data.Fixed.FixedCategoryChild.FixedCategory_everyWeek;
import com.example.dailyquest.Data.Fixed.FixedCategoryChild.FixedCategory_everyYear;
import com.example.dailyquest.Data.Fixed.FixedCategoryEnum;
import com.example.dailyquest.Data.Fixed.FixedShortInfo;
import com.example.dailyquest.Data.Fixed.FixedTodo;
import com.example.dailyquest.Utils.RunnableDelegate;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FixedTodoManager
{
    private static FixedTodoManager _instance = null;
    public static FixedTodoManager instance() { return _instance; }
    public static void initialize(Context context)
    {
        if(_instance == null)
        {
            _instance = new FixedTodoManager(context);
        }
    }
    public static void reset(Context context)
    {
        _instance = new FixedTodoManager(context);
    }


    private ArrayList<FixedTodo> todos = new ArrayList<>();
    private File baseFile;
    private RunnableDelegate runnableDelegate = new RunnableDelegate();

    private FixedTodoManager(Context context)
    {
        baseFile = new File(context.getFilesDir(), "FT");
        if(baseFile.exists() == false)
        {
            baseFile.mkdirs();
        }

        loadTodos();

        int i = 0;
    }
























    private boolean saveTodos()
    {
        File todoInfoFile = getTodoInfoFile();

        try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(todoInfoFile)))
        {
            dos.writeShort((short)todos.size());

            for(FixedTodo todo : todos)
            {
                dos.writeUTF(todo.mainText);
                dos.writeUTF(todo.explainText);

                dos.writeShort(todo.getAlarmTime());
                dos.writeByte(todo.alarmRepTime);

                dos.writeByte((byte)todo.getColor());


                if(saveCategoryInfo(dos, todo) == false)
                {
                    return false;
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        onFixedTodosUpdated();
        return true;
    }

    private boolean saveCategoryInfo(DataOutputStream dos, FixedTodo todo)
    {
        try
        {
            FixedCategoryEnum categoryEnum = todo.getCategoryEnum();
            if(categoryEnum == null) return false;
            dos.writeInt(categoryEnum.ordinal());

            FixedCategory category = todo.getCategory();
            if(category.saveToDos(dos) == false) return false;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }



    private boolean loadTodos()
    {
        File todoInfoFile = getTodoInfoFile();
        if(todoInfoFile.exists() == false) return true; // 아직 생성 안한것. 정상.

        try(DataInputStream dis = new DataInputStream(new FileInputStream(todoInfoFile)))
        {
            short size = dis.readShort();
            todos = new ArrayList<>(size);

            while(size-- > 0)
            {
                FixedTodo todo = new FixedTodo();
                todos.add(todo);

                todo.mainText = dis.readUTF();
                todo.explainText = dis.readUTF();

                todo.setAlarmTime(dis.readShort());
                todo.alarmRepTime = dis.readByte();

                todo.setColor((int)dis.readByte());

                if(loadFixedTodoInfo(dis, todo) == false) return false;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return true;
    }


    private boolean loadFixedTodoInfo(DataInputStream dis, FixedTodo todo)
    {
        try
        {
            FixedCategoryEnum category = FixedCategoryEnum.values()[dis.readInt()];
            if(category == null) return false;


            switch (category)
            {
                case NONE:
                    break;

                case EVERY_YEAR:
                    FixedCategory_everyYear everyYear = new FixedCategory_everyYear();
                    todo.setCategory(everyYear);
                    break;

                case EVERY_MONTH:
                    FixedCategory_everyMonth everyMonth = new FixedCategory_everyMonth();
                    todo.setCategory(everyMonth);
                    break;

                case EVERY_WEEK:
                    FixedCategory_everyWeek everyWeek = new FixedCategory_everyWeek();
                    todo.setCategory(everyWeek);
                    break;

                default:
                    return false;
            }

            if(todo.getCategory().loadFromDis(dis) == false) return false;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }



















    public int addTodo(FixedTodo addedTodo)
    {
        if(todos.size() >= Short.MAX_VALUE)
        {
            // 추후 캐시 정보의 todos 인덱스 를 short 로 저장할 예정이기에, 상한값일시 제한
            return -1;
        }

        todos.add(addedTodo);

        File todoInfoFile = getTodoInfoFile();

        if(saveTodos() == false)
        {
            return -1;
        }

        return todos.size() - 1;
    }
    public int deleteTodo(FixedTodo removedTodo)
    {
        int index =  todos.indexOf(removedTodo);

        if(todos.remove(removedTodo) == false)
        {
            return -1;
        }

        if(saveTodos() == false)
        {
            return -1;
        }

        return index;
    }
    public void onItemSwapped(int fromIndex, int toIndex)
    {
        FixedTodo todo = todos.get(fromIndex);

        todos.remove(fromIndex);
        todos.add(toIndex, todo);

        saveTodos();
    }

    public int saveTodo(FixedTodo savedTodo)
    {
        saveTodos();
        return todos.indexOf(savedTodo);
    }


















    public ArrayList<FixedTodo> getTodos() { return todos; }
















    
    private File getTodoInfoFile()
    {
        return new File(baseFile, "todoInfo.met");
    }

    public StringBuilder getDebugInfo()
    {
        StringBuilder sb = new StringBuilder(".\n");

        sb.append("\n Todos Info \n");
        for(FixedTodo todo : todos)
        {
            sb.append(String.format("%s : %s alarm(%d) rep(%d) color(%d)\n",
                    todo.mainText, todo.explainText, todo.getAlarmTime(),
                    todo.alarmRepTime, todo.getColor()));
        }

        sb.append("\n FixedTodos File \n");
        listAllFiles(sb, "", baseFile);
        return sb;
    }

    private void listAllFiles(StringBuilder sb, String parent, File dir)
    {
        String me = parent + "/" + dir.getName();
        sb.append(me + "\n");
        File[] files = dir.listFiles();
        for(File file : files)
        {
            if(file.isFile() == false)
            {
                listAllFiles(sb, me, file);
            }
            else
            {
                sb.append(me + "/" + file.getName() + "\n");
            }
        }
    }






















    private List<TreeMap<Byte, ArrayList<FixedTodo>>>  loadQuarterData(int year, int month)
    {
        int quarter = (month - 1) / 3;
        List<TreeMap<Byte, ArrayList<FixedTodo>>> filled = new ArrayList<>(3);
        for(int i = 0; i < 3; i++)
        {
            filled.add(new TreeMap<>());
        }

        for(short i = 0; i < todos.size(); i++)
        {
            FixedTodo todo = todos.get(i);
            todo.getCategory().paint(todo, year, quarter, filled);
        }

        return filled;
    }


    public TreeMap<Byte, ArrayList<FixedTodo>> getMonthInfo(int year, int month)
    {
        List<TreeMap<Byte, ArrayList<FixedTodo>>> quarterData = loadQuarterData(year, month);

        return quarterData.get((month - 1) % 3);
    }

    public ArrayList<FixedTodo> getDateInfo(int year, int month, int date)
    {
        TreeMap<Byte, ArrayList<FixedTodo>> shortInfos = getMonthInfo(year, month);

        if(shortInfos.containsKey((byte)date))
        {
            return shortInfos.get((byte)date);
        }

        return null;
    }

    public void addOnFixedTodosUpdateListener(Runnable runnable)
    {
        runnableDelegate.addListener(runnable);
    }
    public void onFixedTodosUpdated()
    {
        runnableDelegate.invoke();
    }
    public void removeOnFixedTodosUpdateListener(Runnable runnable)
    {
        runnableDelegate.removeListener(runnable);
    }

    public FixedTodo getTodoAt(short index)
    {
        return todos.get(index);
    }




}
