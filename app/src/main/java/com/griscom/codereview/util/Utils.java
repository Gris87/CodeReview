package com.griscom.codereview.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

@SuppressLint("DefaultLocale")
public class Utils
{
    private static final String TAG="Utils";

    public static float spToPixels(float sp, Context context)
    {
        return sp*context.getResources().getDisplayMetrics().scaledDensity;
    }

    public static String bytesToString(long bytes)
    {
        int    type     = 0;
        double bytesDiv = bytes;

        while (bytesDiv>=1024)
        {
            ++type;

            bytesDiv/=1024;
        }

        switch (type)
        {
            case 0: return String.valueOf(bytes)                         +  " B";
            case 1: return String.valueOf(Math.round(bytesDiv*100)/100f) + " KB";
            case 2: return String.valueOf(Math.round(bytesDiv*100)/100f) + " MB";
            case 3: return String.valueOf(Math.round(bytesDiv*100)/100f) + " GB";
            case 4: return String.valueOf(Math.round(bytesDiv*100)/100f) + " TB";
        }

        return String.valueOf(Math.round(bytesDiv*100)/100f)+" PB";
    }

    public static String md5ForFile(String fileName)
    {
        FileInputStream in=null;

        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte buffer[]=new byte[4096];
            in=new FileInputStream(fileName);

            while (in.available()>0)
            {
                in.read(buffer);
                md.update(buffer);
            }

            in.close();

            byte[] hash=md.digest();
            StringBuilder sb=new StringBuilder(2*hash.length);

            for (int i=0; i<hash.length; ++i)
            {
                sb.append(String.format("%02x", hash[i]));
            }

            return sb.toString().toUpperCase();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Impossible to get MD5 for file: "+fileName, e);
        }

        if (in!=null)
        {
            try
            {
                in.close();
            }
            catch (Exception e)
            {
                Log.e(TAG, "Impossible to close file: "+fileName, e);
            }
        }

        return "";
    }

    public static boolean deleteFileOrFolder(String filename)
    {
        File file=new File(filename);

        if (file.isDirectory())
        {
            String files[]=file.list();

            for (int i=0; i<files.length; ++i)
            {
                if (!deleteFileOrFolder(filename+"/"+files[i]))
                {
                    return false;
                }
            }
        }

        return file.delete();
    }
}
