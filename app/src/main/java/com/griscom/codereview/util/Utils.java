package com.griscom.codereview.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

import android.content.Context;
import android.util.Log;

public class Utils
{
    private static final String TAG="Utils";



    /**
     * Converts sp to pixels
     *
     * @param sp        Value in sp
     * @param context   Android context
     * @return          Converted sp value in pixels
     */
    public static float spToPixels(float sp, Context context)
    {
        return sp * context.getResources().getDisplayMetrics().scaledDensity;
    }

    /**
     * Converts bytes to string representation
     *
     * @param bytes     Amount of bytes
     * @return          String representation of bytes value
     */
    public static String bytesToString(long bytes)
    {
        byte   type     = 0;
        double bytesDiv = bytes;

        while (bytesDiv >= 1024d && type < 5)
        {
            ++type;

            bytesDiv /= 1024d;
        }

        switch (type)
        {
            case 0: return String.valueOf(bytes)                             +  " B";
            case 1: return String.valueOf(Math.round(bytesDiv * 100) / 100d) + " KB";
            case 2: return String.valueOf(Math.round(bytesDiv * 100) / 100d) + " MB";
            case 3: return String.valueOf(Math.round(bytesDiv * 100) / 100d) + " GB";
            case 4: return String.valueOf(Math.round(bytesDiv * 100) / 100d) + " TB";
        }

        return String.valueOf(Math.round(bytesDiv * 100) / 100d) + " PB";
    }

    // TODO: Need to verify it
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

    /**
     * Deletes specified file or specified folder recursively
     *
     * @param filename  Path to file or path to folder
     * @return          True if deletion successful, otherwise false
     */
    public static boolean deleteFileOrFolder(String filename)
    {
        boolean res = true;

        File file = new File(filename);

        if (file.isDirectory())
        {
            String files[] = file.list();

            for (String oneFile : files)
            {
                if (!deleteFileOrFolder(filename + "/" + oneFile))
                {
                    res = false;
                }
            }
        }

        return file.delete() && res;
    }
}
