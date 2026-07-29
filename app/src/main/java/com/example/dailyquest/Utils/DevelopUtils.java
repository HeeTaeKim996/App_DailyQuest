package com.example.dailyquest.Utils;

import android.content.Context;

import com.example.dailyquest.Small.StaticValues;

import java.io.File;

public class DevelopUtils
{
    private static DevelopUtils _instance = new DevelopUtils();
    private DevelopUtils() {}
    public static DevelopUtils instance() { return _instance; }

    public void clearAllFiles(Context context)
    {
        deleteChildrenFiles(context.getFilesDir());
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

    public StringBuilder getAllFiles(Context context)
    {
        StringBuilder sb = new StringBuilder(".\n\n ALL FILES \n\n");
        listAllFiles(sb, "", context.getFilesDir());
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
