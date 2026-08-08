package com.example.dailyquest.FixedTodo;

import android.content.Context;

import com.example.dailyquest.Data.Fixed.FixedCategory;
import com.example.dailyquest.Data.Fixed.FixedCategoryChild.FixedCategory_everyMonth;
import com.example.dailyquest.Data.Fixed.FixedCategoryChild.FixedCategory_everyWeek;
import com.example.dailyquest.Data.Fixed.FixedCategoryChild.FixedCategory_everyYear;
import com.example.dailyquest.Data.Fixed.FixedCategoryChild.FixedCategory_moonCalender;
import com.example.dailyquest.Data.Fixed.FixedCategoryEnum;
import com.example.dailyquest.Data.Fixed.FixedTodo;
import com.example.dailyquest.Utils.CalenderUtils;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
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
    private File cacheDir;
    private RunnableDelegate runnableDelegate = new RunnableDelegate();



    private Context appContext;

    private FixedTodoManager(Context context)
    {
        appContext = context.getApplicationContext();

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

        if(initializeCacheInfo() == false)
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

                case MOON_EVERY_YEAR:
                    FixedCategory_moonCalender moonCalender = new FixedCategory_moonCalender();
                    todo.setCategory(moonCalender);
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

        if(saveTodos() == false)
        {
            return -1;
        }

        onTodoAdded_updateLog(addedTodo);
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

        onTodoDeleted_updateLog(removedTodo);
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

        onTodoUpdated_updateLog(savedTodo);
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
            InformUtils.instance().showToast(appContext,
                    "캐시 MISS");

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


            File newCacheFile = makeCacheFile(title);
            RandomAccessFile out = new RandomAccessFile(newCacheFile, "rw");

            if(saveCacheFile(out, filled))
            {
                makeCacheSpace();
                if(saveCacheInfo(title) == false)
                {
                    // 여기서 실패할 수가 있을까. 웬만한 없다 본다.
                }
            }
            else
            {
                newCacheFile.delete();
            }

            return out;
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
        try(RandomAccessFile raf = loadQuarterData(year, month))
        {
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
        try(RandomAccessFile raf = loadQuarterData(year, month))
        {
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
        File[] files = cacheDir.listFiles();
        if(files != null)
        {
            for( File file : files)
            {
                file.delete();
            }
        }


        File cacheInfoFile = getCacheInfoFile();
        resetCacheInfoFile(cacheInfoFile);


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



















    private final byte CACHE_COUNT = 7;

    private final long STATE_START_POINT = Byte.BYTES;
    private final int STATE_SIZE = Byte.BYTES * CACHE_COUNT;

    private final long TITLE_START_POINT = Byte.BYTES * (1 + CACHE_COUNT);
    private long getStatePointByIndex(byte index)
    {
        return (long) (STATE_START_POINT + Byte.BYTES * index);
    }
    private long getTitlePointByIndex(byte index)
    {
        return (long) (TITLE_START_POINT + Short.BYTES * index);
    }



    private File getCacheInfoFile()
    {
        return new File(baseFile, "cacheInfo.met");
    }

    private boolean initializeCacheInfo()
    {
        File infoFile = getCacheInfoFile();
        if(infoFile.exists() == false)
        {
            return resetCacheInfoFile(infoFile);
        }

        return true;
    }
    private boolean resetCacheInfoFile(File infoFile)
    {
        try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(infoFile)))
        {
            dos.writeByte(0); // INDEX

            int rep = CACHE_COUNT;
            while(rep -- > 0)
            {
                dos.writeByte(-1); // state [ -1(공란), 0(삭제하고 사용 가능), 1(한번만 더 기회를)
            }
            rep = CACHE_COUNT;
            while(rep -- > 0)
            {
                dos.writeShort(0); // title ( ex 20261 (2026년 2분기) )
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private boolean saveCacheInfo(short newTitle) // 추후 raf 로 필요한 부분만 수정 예정
    {
        File infoFile = getCacheInfoFile();

        try(RandomAccessFile raf = new RandomAccessFile(infoFile, "rw"))
        {
            raf.seek(0);
            byte index = raf.readByte();

            raf.seek(getStatePointByIndex(index));
            raf.writeByte(1);

            raf.seek(getTitlePointByIndex(index));
            raf.writeShort(newTitle);

            index = (byte)( (index + 1) % CACHE_COUNT );
            raf.seek(0);
            raf.writeByte(index);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }




    private boolean findCacheExists(short findingTitle)
    {
        File cacheInfoFile = getCacheInfoFile();
        try(RandomAccessFile raf = new RandomAccessFile(cacheInfoFile, "rw"))
        {
            raf.seek(TITLE_START_POINT);
            for(byte i = 0; i < CACHE_COUNT; i++)
            {
                if(raf.readShort() == findingTitle)
                {
                    raf.seek(getStatePointByIndex(i));
                    raf.writeByte(1);
                    return true;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    private void makeCacheSpace()
    {
        File cacheInfoFile = getCacheInfoFile();
        try(RandomAccessFile raf = new RandomAccessFile(cacheInfoFile, "rw"))
        {
            raf.seek(0);
            int index = (int)raf.readByte();

            byte[] stateBuffer = new byte[STATE_SIZE];
            raf.seek(STATE_START_POINT);
            raf.readFully(stateBuffer);

            while(true)
            {
                if(--stateBuffer[index] < 0)
                {
                    raf.seek(STATE_START_POINT);
                    raf.write(stateBuffer);

                    raf.seek(getTitlePointByIndex((byte)index));
                    short title = raf.readShort();

                    File deleteFile = makeCacheFile(title);
                    deleteFile.delete();

                    raf.seek(getTitlePointByIndex((byte)index));
                    raf.writeShort(0);


                    raf.seek(0);
                    raf.writeByte((byte)index);

                    return;
                }

                index = (index + 1) % CACHE_COUNT;
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
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
            raf.setLength(4 * Long.BYTES);

            raf.seek(0);
            raf.writeLong(4 * Long.BYTES);
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
                out.put(date, new ArrayList<>(size));

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

































    private void onTodoAdded_updateLog(FixedTodo todo)
    {
        addLog("ADD", todo);
    }
    private void onTodoUpdated_updateLog(FixedTodo todo)
    {
        addLog("SAV", todo);
    }
    private void onTodoDeleted_updateLog(FixedTodo todo)
    {
        addLog("DEL", todo);
    }


    private File getLogFile()
    {
        return new File(baseFile, "CatLog.log");
    }
    private void addLog(String prefix, FixedTodo todo)
    {
        CalenderUtils.Calender today = CalenderUtils.instance().getTodaybyCalender();
        String log = String.format("%s - %s (%02d-%02d-%02d)\n", prefix, todo.getSummary(),
                today.year % 100, today.month, today.date);

        File logFile = getLogFile();
        if(logFile.exists() == false)
        {
            makeEmptyLogFile(logFile);
        }

        try(RandomAccessFile raf = new RandomAccessFile(logFile, "rw"))
        {
            raf.seek(0);
            int count = raf.readInt();
            raf.seek(0);
            raf.writeInt(count + log.length());

            raf.seek(raf.length());

            byte[] buffer = log.getBytes(StandardCharsets.UTF_16BE); // BE : 헤더에 len 기록 없이, 내용물만 기록
            raf.write(buffer);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            logFile.delete();
        }
    }
    private void makeEmptyLogFile(File logFile)
    {
        try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(logFile)))
        {
            dos.writeInt(0); // count
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public String showLog()
    {
        File logFile = getLogFile();
        if(logFile.exists() == false)
        {
            return "";
        }


        try(DataInputStream dis = new DataInputStream(new FileInputStream(logFile)))
        {
            int len = dis.readInt();
            byte[] buffer = new byte[len * 2]; // WCHAR

            dis.readFully(buffer);

            return new String(buffer, StandardCharsets.UTF_16BE);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public void clearLog()
    {
        File logFile = getLogFile();
        try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(logFile)))
        {
            dos.writeInt(0);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void resetLog(String log)
    {
        File logFile = getLogFile();
        try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(logFile)))
        {
            dos.writeInt(log.length());

            byte[] buffer = log.getBytes(StandardCharsets.UTF_16BE);
            dos.write(buffer);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
