package com.griscom.codereview.util;

import android.content.Context;

public class Utils
{
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
}
