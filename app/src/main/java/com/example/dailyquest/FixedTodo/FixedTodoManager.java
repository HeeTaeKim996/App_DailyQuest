package com.example.dailyquest.FixedTodo;

import android.content.Context;

import com.example.dailyquest.Data.FixedTodo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

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


    private ArrayList<FixedTodo> todos = new ArrayList<>();
    private File baseFile;


    private FixedTodoManager(Context context)
    {
        baseFile = new File(context.getFilesDir(), "FT");
        if(baseFile.exists() == false)
        {
            baseFile.mkdirs();
        }

        File todoInfoFile = getTodoInfoFile();
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
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }



    public boolean addFixedTodo(FixedTodo addedTodo)
    {
        todos.add(addedTodo);

        File todoInfoFile = getTodoInfoFile();

        try(RandomAccessFile raf = new RandomAccessFile(todoInfoFile, "rw"))
        {
            raf.seek(0);
            raf.writeShort((short)todos.size());

            // 기존에 데이터가 있는 경우 끝에서 입력 시작. 아니라면, 변동 없음
            raf.seek(raf.length());

            

            raf.writeUTF(addedTodo.mainText);
            raf.writeUTF(addedTodo.explainText);

            raf.writeShort(addedTodo.getAlarmTime());
            raf.writeByte(addedTodo.alarmRepTime);

            raf.writeByte((byte)addedTodo.getColor());
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    public boolean removeFixedTodo(FixedTodo removedTodo)
    {
        if(todos.remove(removedTodo) == false)
        {
            return false;
        }

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
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
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
}
