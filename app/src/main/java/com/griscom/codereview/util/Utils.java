package com.griscom.codereview.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

/**
 * Class for useful functions
 */
public class Utils
{
    @SuppressWarnings("unused")
    private static final String TAG = "Utils";



    private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };



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

    /**
     * Converts byte array to hex string
     * @param bytes    byte array
     * @return hex string
     */
    @SuppressWarnings("WeakerAccess")
    public static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; ++i)
        {
            int v = bytes[i] & 0xFF;

            hexChars[i * 2]     = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }

        return new String(hexChars);
    }

    /**
     * Calculates MD5 hash for specified file
     * @param filePath    path to file
     * @return string with MD5 hash for specified file
     */
    public static String md5ForFile(String filePath)
    {
        String res = "";
        FileInputStream in = null;



        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte buffer[] = new byte[4096];
            in = new FileInputStream(filePath);

            do
            {
                int len = in.read(buffer);

                if (len > 0)
                {
                    md.update(buffer, 0, len);
                }
                else
                {
                    break;
                }
            } while (true);

            res = bytesToHex(md.digest());
        }
        catch (Exception e)
        {
            // Nothing
        }



        if (in != null)
        {
            try
            {
                in.close();
            }
            catch (Exception e)
            {
                // Nothing;
            }
        }

        return res;
    }

    /**
     * Deletes specified file or specified folder recursively
     *
     * @param filePath  Path to file or path to folder
     * @return          True if deletion successful, otherwise false
     */
    public static boolean deleteFileOrFolder(String filePath)
    {
        boolean res = true;

        File file = new File(filePath);

        if (file.isDirectory())
        {
            String files[] = file.list();

            for (String oneFile : files)
            {
                if (!deleteFileOrFolder(filePath + "/" + oneFile))
                {
                    res = false;
                }
            }
        }

        return res && file.delete();
    }

    /**
     * Replaces incorrect characters to underscore in ignore file name
     * @param fileName    file name
     * @return specified text without incorrect characters
     */
    public static String replaceIncorrectIgnoreFileName(String fileName)
    {
        return fileName.replace("\\", "_")
                .replace("/",  "_")
                .replace(":",  "_")
                .replace("\"", "_")
                .replace("<",  "_")
                .replace(">",  "_")
                .replace("|",  "_");
    }
}
