package com.example.dailyquest.FixedTodo;

import android.content.Context;

import com.example.dailyquest.Data.Fixed.FixedCategory;
import com.example.dailyquest.Data.Fixed.FixedCategoryChild.FixedCategory_everyMonth;
import com.example.dailyquest.Data.Fixed.FixedCategoryChild.FixedCategory_everyWeek;
import com.example.dailyquest.Data.Fixed.FixedCategoryChild.FixedCategory_everyYear;
import com.example.dailyquest.Data.Fixed.FixedCategoryEnum;
import com.example.dailyquest.Data.Fixed.FixedTodo;
import com.example.dailyquest.Utils.InformUtils;
import com.example.dailyquest.Utils.RunnableDelegate;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    private File cacheDir;
    private RunnableDelegate runnableDelegate = new RunnableDelegate();

    private final int CACHE_COUNT = 7;
    private class CacheInfo
    {
        public byte state;  // -1(미사용) / 0(해제 가능한 사용공간) / 1(한번 더 기회가 있는 사용 공간)
        public short title = 0;
    }
    private CacheInfo[] cacheInfos = new CacheInfo[CACHE_COUNT];
    private byte cacheInfoIndex;

    private FixedTodoManager(Context context)
    {
        baseFile = new File(context.getFilesDir(), "FT");
        cacheDir = new File(baseFile, "C");
        if(cacheDir.exists() == false)
        {
            cacheDir.mkdirs();
        }

        if(loadTodos() == false)
        {
            InformUtils.instance().ShowInformYes(context,
                    "FixedTodoManager : loadTodos 실패");
        }

        if(loadCacheInfo() == false)
        {
            InformUtils.instance().ShowInformYes(context,
                    "FixedTodoManager : LoadCacheInfo 실패");
        }
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






















    private RandomAccessFile loadQuarterData(int year, int month) throws FileNotFoundException // -> 이 함수를 호출하는 쪽에서는 이 함수를 try 내에 호출해야 함
    {
        int quarter = (month - 1) / 3;
        short title = (short)(year * 10 + quarter);


        if(findCacheExists(title) == false)
        {
            List<TreeMap<Byte, ArrayList<Short>>> filled = new ArrayList<>(3);
            for(int i = 0; i < 3; i++)
            {
                filled.add(new TreeMap<>());
            }

            for(short i = 0; i < todos.size(); i++)
            {
                FixedTodo todo = todos.get(i);
                todo.getCategory().paint(i, year, quarter, filled);
            }

            makeCacheSpace(title);
            cacheInfos[cacheInfoIndex].state = 1;
            cacheInfos[cacheInfoIndex].title = title;
            cacheInfoIndex++; // @ ++ 하는거 맞나? 시계열그.. 거 따라한건데. ++ 하는 건지 모르겠다.
            if(saveCacheInfo())
            {
                File newCacheFile = makeCacheFile(title);
                RandomAccessFile out = new RandomAccessFile(newCacheFile, "rw");

                if(saveCacheFile(out, filled) == false)
                {
                    newCacheFile.delete();

                    cacheInfos[cacheInfoIndex].state = -1;
                    cacheInfos[cacheInfoIndex].title = 0;
                    cacheInfoIndex--;
                    saveCacheInfo();
                    return null;
                }

                return out;
            }
        }
        else
        {
            File cacheFile = makeCacheFile(title);
            if(cacheFile.exists())
            {
                return new RandomAccessFile(cacheFile, "rw");
            }

        }



        return null;
    }


    public TreeMap<Byte, ArrayList<FixedTodo>> getMonthInfo(int year, int month)
    {
        try
        {
            RandomAccessFile raf = loadQuarterData(year, month);
            return loadCacheFileByMonth(raf, (month - 1) % 3);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<FixedTodo> getDateInfo(int year, int month, int date)
    {
        try
        {
            RandomAccessFile raf = loadQuarterData(year, month);
            return loadCacheFileByMonthDate(raf, (month - 1) % 3, date);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
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






















    private File getCacheInfoFile()
    {
        return new File(baseFile, "cacheInfo.met");
    }

    private boolean loadCacheInfo()
    {
        File infoFile = getCacheInfoFile();
        if(infoFile.exists() == false) return true; // 문제 없음

        try(DataInputStream dis = new DataInputStream(new FileInputStream(infoFile)))
        {
            cacheInfoIndex = dis.readByte();

            for(int i = 0; i < CACHE_COUNT; i++)
            {
                CacheInfo cacheInfo = cacheInfos[i];
                cacheInfo.state = dis.readByte();
                cacheInfo.title = dis.readShort();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    private boolean saveCacheInfo()
    {
        File infoFile = getCacheInfoFile();
        try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(infoFile)))
        {
            dos.writeByte(cacheInfoIndex);

            for(CacheInfo cacheInfo : cacheInfos)
            {
                dos.writeByte(cacheInfo.state);
                dos.writeShort(cacheInfo.title);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    private boolean findCacheExists(short title)
    {
        byte originIndex = cacheInfoIndex;
        do
        {
            CacheInfo cacheInfo = cacheInfos[cacheInfoIndex];
            if(cacheInfo.title == title) return true;

            if(++cacheInfoIndex >= CACHE_COUNT)
            {
                cacheInfoIndex = 0;
            }
        }
        while(cacheInfoIndex != originIndex);

        return false;
    }

    private void makeCacheSpace(short title)
    {
        while(true)
        {
            CacheInfo cacheInfo = cacheInfos[cacheInfoIndex];
            if(--cacheInfo.state < 0)
            {
                File deleteFile = makeCacheFile(cacheInfo.title);
                deleteFile.delete();
                return;
            }

            if(++cacheInfoIndex >= CACHE_COUNT)
            {
                cacheInfoIndex = 0;
            }
        }
    }







    private File makeCacheFile(short title)
    {
        return new File(cacheDir, String.format("%d.dat", title));
    }
    private boolean saveCacheFile(RandomAccessFile raf, List<TreeMap<Byte, ArrayList<Short>>> quarterInfo)
    {
        try
        {
            raf.writeLong(0);
            raf.seek(4 * Long.BYTES);
            // long 4개. 1 시작(확정 0이긴 하지만, 코드 편의를 위해) / 2시작 / 3시작 / length

            for(int i = 0; i < 3; i++)
            {
                TreeMap<Byte, ArrayList<Short>> monthInfo = quarterInfo.get(i);

                Map.Entry<Byte, ArrayList<Short>>[] entries
                        = monthInfo.entrySet().toArray(new Map.Entry[0]);

                for(Map.Entry<Byte, ArrayList<Short>> entry : entries)
                {
                    raf.writeByte(entry.getKey());

                    ArrayList<Short> todoIndices = entry.getValue();
                    raf.writeShort(todoIndices.size());   // 현실적으로 2^15 - 1 만큼 차기 불가능하니 int 배제.
                    for(Short todoIndex : todoIndices)
                    {
                        raf.writeShort(todoIndex);
                    }
                }

                raf.seek((i + 1) * Long.BYTES);
                raf.writeLong(raf.length());
                raf.seek(raf.length());
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private TreeMap<Byte, ArrayList<FixedTodo>> loadCacheFileByMonth(RandomAccessFile raf, int monthIndex)
    {
        TreeMap<Byte, ArrayList<FixedTodo>> out = new TreeMap<>();
        try
        {
            raf.seek(monthIndex * Long.BYTES);
            long start = raf.readLong();
            long end = raf.readLong();

            raf.seek(start);

            while(raf.getFilePointer() < end)
            {
                byte date = raf.readByte();
                short size = raf.readShort();
                out.put(raf.readByte(), new ArrayList<>(size));

                ArrayList<FixedTodo> retTodos = out.get(date);
                while(size-- > 0)
                {
                    short index = raf.readShort();
                    retTodos.add(todos.get(index));
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

        return out;
    }

    private ArrayList<FixedTodo> loadCacheFileByMonthDate(RandomAccessFile raf, int monthIndex,
                                                          int aimDate)
    {
        try
        {
            raf.seek(monthIndex * Long.BYTES);
            long start = raf.readLong();
            long end = raf.readLong();

            raf.seek(start);

            while(raf.getFilePointer() < end)
            {
                byte date = raf.readByte();
                short size = raf.readShort();
                if(aimDate == date)
                {
                    ArrayList<FixedTodo> retTodos = new ArrayList<>(size);
                    while(size-- > 0)
                    {
                        short index = raf.readShort();
                        retTodos.add(todos.get(index));
                    }

                    return retTodos;
                }
                else if(aimDate < date) return null;

                long next = raf.getFilePointer() + ( (long)size * Short.BYTES );
                raf.seek(next);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }

        return null;
    }
}
