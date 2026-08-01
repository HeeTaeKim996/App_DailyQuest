package com.example.dailyquest.Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Output;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils
{
    public static boolean makeZip(Context context)
    {
        CalenderUtils.Calender today = CalenderUtils.instance().getTodaybyCalender();
        String fileName =  String.format("DailyQuest_%d-%d-%d.zip",
                today.year, today.month, today.date);
        OutputStream outputStream = null;

        try
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME,fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/zip");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                ContentResolver resolver = context.getContentResolver();
                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if(uri == null) return false;

                outputStream = resolver.openOutputStream(uri);
            }
            else
            {
                File downloadsDir = Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DOWNLOADS);
                if(downloadsDir.exists() == false)
                {
                    downloadsDir.mkdirs();
                }

                File targetFile = new File(downloadsDir, fileName);
                outputStream = new FileOutputStream(targetFile);
            }

            if(outputStream == null) return false;

            try(ZipOutputStream zos = new ZipOutputStream(outputStream))
            {
                ZipEntry sigEntry = new ZipEntry("THIS_IS_DAILY_QUEST_APP_ZIP.sig");
                zos.putNextEntry(sigEntry);
                zos.closeEntry();


                File sourceFile = context.getFilesDir();
                compressToZip(sourceFile, "", zos);
            }
            catch(IOException e)
            {
                e.printStackTrace();
                return false;
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static void compressToZip(File folder, String parentPath, ZipOutputStream zos)
            throws IOException
    {
        File[] files = folder.listFiles();
        if(files == null || files.length == 0) return;

        byte[] buffer = new byte[1024];
        for(File file : files)
        {
            String entryName;
            if(parentPath.isEmpty())
            {
                entryName = file.getName();
            }
            else
            {
                entryName = parentPath + "/" + file.getName();
            }


            if(file.isDirectory())
            {
                ZipEntry dirEntry = new ZipEntry(entryName + "/");
                zos.putNextEntry(dirEntry);
                zos.closeEntry();

                compressToZip(file, entryName, zos);
            }
            else
            {
                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);

                try(FileInputStream fis = new FileInputStream(file))
                {
                    int length;
                    while((length = fis.read(buffer)) > 0)
                    {
                        zos.write(buffer, 0, length);
                    }
                }

                zos.closeEntry();
            }
        }
    }



    public static void tryImportFromZip(Context context, Consumer<Boolean> callback)
    {
        ComponentActivity activity = (ComponentActivity) context;

        final ActivityResultLauncher<Intent>[] filePickerLauncher = new ActivityResultLauncher[1];


        filePickerLauncher[0] = activity.getActivityResultRegistry()
                .register(
                        "custom_file_picker_key",
                        new ActivityResultContracts.StartActivityForResult(),
                        result->
                        {
                            try
                            {
                                if(result.getResultCode() == Activity.RESULT_OK
                                        && result.getData() != null)
                                {
                                    Uri fileUri = result.getData().getData();
                                    boolean bSucceed = importFromZip(context, fileUri);
                                    callback.accept(bSucceed);
                                }
                            }
                            finally
                            {
                                if(filePickerLauncher[0] != null)
                                {
                                    filePickerLauncher[0].unregister();
                                }
                            }

                        });

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");

        filePickerLauncher[0].launch(intent);
    }





    private static boolean importFromZip(Context context, Uri zipUri)
    {
        if(zipUri == null) return false;

        File targetDir = context.getFilesDir();


        try(InputStream inputStream = context.getContentResolver().openInputStream(zipUri))
        {
            if(inputStream == null) return false;

            try(ZipInputStream zis = new ZipInputStream(inputStream))
            {
                ZipEntry entry = zis.getNextEntry();
                if(entry == null ||
                        entry.getName().equals("THIS_IS_DAILY_QUEST_APP_ZIP.sig") == false) return false;
                zis.closeEntry();

                DevelopUtils.instance().clearAllFiles(context);


                byte[] buffer = new byte[1024];

                while((entry = zis.getNextEntry()) != null)
                {
                    String entryName = entry.getName();

                    File newFile = new File(targetDir, entryName);

                    // 해킹 방지 용도인듯? '해당 조건문은 Zip Slip(집 슬립)이라 불리는 유명한 보안 취약점 공격을 막기 위한 구문이다' 라고함
                    if(newFile.getCanonicalPath().startsWith(targetDir.getCanonicalPath()) == false)
                    {
                        throw new SecurityException("Zip Slip 대비 코드라함. 원린 모름 : " + entryName);
                    }


                    if(entry.isDirectory())
                    {
                        if(newFile.exists() == false)
                        {
                            newFile.mkdirs();
                        }
                    }
                    else
                    {
                        try(FileOutputStream fos = new FileOutputStream(newFile))
                        {
                            int length;
                            while((length = zis.read(buffer)) > 0)
                            {
                                fos.write(buffer, 0, length);
                            }
                        }
                    }
                    zis.closeEntry();

                }

            }

        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }



}
