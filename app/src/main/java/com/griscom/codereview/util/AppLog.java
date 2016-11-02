package com.griscom.codereview.util;

import android.util.Log;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class AppLog
{
    @SuppressWarnings("unused")
    private static final String TAG = "AppLog";



    private AppLog()
    {
    }

    public static int v(String tag, String msg)
    {
        return Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr)
    {
        return Log.v(tag, msg, tr);
    }

    public static int d(String tag, String msg)
    {
        return Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr)
    {
        return Log.d(tag, msg, tr);
    }

    public static int i(String tag, String msg)
    {
        return Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr)
    {
        return Log.i(tag, msg, tr);
    }

    public static int w(String tag, String msg)
    {
        return Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr)
    {
        return Log.w(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr)
    {
        return Log.w(tag, tr);
    }

    public static int e(String tag, String msg)
    {
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr)
    {
        return Log.e(tag, msg, tr);
    }

    public static int wtf(String tag, String msg)
    {
        try
        {
            throw new Exception("Logged stacktrace");
        }
        catch (Exception e)
        {
            return Log.e(tag, msg, e);
        }
    }

    public static int wtf(String tag, String msg, Throwable tr)
    {
        try
        {
            throw new Exception("Logged stacktrace");
        }
        catch (Exception e)
        {
            return Log.e(tag, msg + '\n' + Log.getStackTraceString(tr), e);
        }
    }

    public static int wtf(String tag, Throwable tr)
    {
        try
        {
            throw new Exception("Logged stacktrace");
        }
        catch (Exception e)
        {
            return Log.e(tag, Log.getStackTraceString(tr), e);
        }
    }

    public static String getStackTraceString(Throwable tr)
    {
        return Log.getStackTraceString(tr);
    }

    public static int println(int priority, String tag, String msg)
    {
        return Log.println(priority, tag, msg);
    }

    public static boolean isLoggable(String s, int i)
    {
        return Log.isLoggable(s, i);
    }
}
