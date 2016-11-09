package com.griscom.codereview.util;

import android.util.Log;

/**
 * Application log
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class AppLog
{
    @SuppressWarnings("unused")
    private static final String TAG = "AppLog";



    /**
     * Disabled default constructor
     */
    private AppLog()
    {
    }

    /**
     * Send a VERBOSE log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int v(String tag, String msg)
    {
        return Log.v(tag, msg);
    }

    /**
     * Send a VERBOSE log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int v(String tag, String msg, Throwable tr)
    {
        return Log.v(tag, msg, tr);
    }

    /**
     * Send a DEBUG log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int d(String tag, String msg)
    {
        return Log.d(tag, msg);
    }

    /**
     * Send a DEBUG log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int d(String tag, String msg, Throwable tr)
    {
        return Log.d(tag, msg, tr);
    }

    /**
     * Send an INFO log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int i(String tag, String msg)
    {
        return Log.i(tag, msg);
    }

    /**
     * Send a INFO log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int i(String tag, String msg, Throwable tr)
    {
        return Log.i(tag, msg, tr);
    }

    /**
     * Send a WARN log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int w(String tag, String msg)
    {
        return Log.w(tag, msg);
    }

    /**
     * Send a WARN log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int w(String tag, String msg, Throwable tr)
    {
        return Log.w(tag, msg, tr);
    }

    /**
     * Send a WARN log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param tr An exception to log
     */
    public static int w(String tag, Throwable tr)
    {
        return Log.w(tag, tr);
    }

    /**
     * Send an ERROR log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int e(String tag, String msg)
    {
        return Log.e(tag, msg);
    }

    /**
     * Send a ERROR log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int e(String tag, String msg, Throwable tr)
    {
        return Log.e(tag, msg, tr);
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen. The error will always
     * be logged at level ASSERT with the call stack. Depending on system configuration, a report
     * may be added to the DropBoxManager and/or the process may be terminated immediately with
     * an error dialog
     * @param tag    Used to identify the source of a log message
     * @param msg    The message you would like logged
     */
    public static int wtf(String tag, String msg)
    {
        try
        {
            //noinspection ProhibitedExceptionThrown
            throw new Exception("Logged stacktrace");
        }
        catch (Exception e)
        {
            return Log.e(tag, msg, e);
        }
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen. Similar to
     * wtf(String, String), with an exception to log
     * @param tag    Used to identify the source of a log message
     * @param msg    The message you would like logged
     * @param tr     An exception to log. May be null
     */
    public static int wtf(String tag, String msg, Throwable tr)
    {
        try
        {
            //noinspection ProhibitedExceptionThrown
            throw new Exception("Logged stacktrace");
        }
        catch (Exception e)
        {
            return Log.e(tag, msg + '\n' + Log.getStackTraceString(tr), e);
        }
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen. Similar to
     * wtf(String, String), with an exception to log
     * @param tag    Used to identify the source of a log message
     * @param tr     An exception to log
     */
    public static int wtf(String tag, Throwable tr)
    {
        try
        {
            //noinspection ProhibitedExceptionThrown
            throw new Exception("Logged stacktrace");
        }
        catch (Exception e)
        {
            return Log.e(tag, Log.getStackTraceString(tr), e);
        }
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     * @param tr An exception to log
     */
    public static String getStackTraceString(Throwable tr)
    {
        return Log.getStackTraceString(tr);
    }

    /**
     * Low-level logging call.
     * @param priority The priority/type of this log message
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    public static int println(int priority, String tag, String msg)
    {
        return Log.println(priority, tag, msg);
    }

    /**
     * Checks to see whether or not a log for the specified tag is loggable at the specified level.
     * The default level of any tag is set to INFO. This means that any level above and including
     * INFO will be logged. Before you make any calls to a logging method you should check to see
     * if your tag should be logged. You can change the default level by setting a system property:
     * 'setprop log.tag.<YOUR_LOG_TAG> <LEVEL>' Where level is either VERBOSE, DEBUG, INFO, WARN,
     * ERROR, ASSERT, or SUPPRESS. SUPPRESS will turn off all logging for your tag. You can also
     * create a local.prop file that with the following in it: 'log.tag.<YOUR_LOG_TAG>=<LEVEL>'
     * and place that in /data/local.prop.
     * @param tag      The tag to check
     * @param level    The level to check
     * @return Whether or not that this is allowed to be logged
     */
    public static boolean isLoggable(String tag, int level)
    {
        return Log.isLoggable(tag, level);
    }
}
