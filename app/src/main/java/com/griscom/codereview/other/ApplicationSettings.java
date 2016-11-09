package com.griscom.codereview.other;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.griscom.codereview.R;
import com.griscom.codereview.util.Utils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Application settings
 */
@SuppressWarnings("WeakerAccess")
public final class ApplicationSettings
{
    @SuppressWarnings("unused")
    private static final String TAG = "ApplicationSettings";



    private static ArrayList<String> sIgnoreFiles    = null;
    private static int               sReviewedColor  = 0;
    private static int               sInvalidColor   = 0;
    private static int               sNoteColor      = 0;
    private static int               sSelectionColor = 0;
    private static int               sFontSize       = 0;
    private static int               sTabSize        = 0;
    private static int               sBigFileSize    = 0;



    /**
     * Disabled default constructor
     */
    private ApplicationSettings()
    {
        // Nothing
    }

    /**
     * Updates application settings from SharedPreferences
     * @param context    context
     */
    public static void update(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(ApplicationPreferences.MAIN_SHARED_PREFERENCES, Context.MODE_PRIVATE);

        String[] ignoreFiles = prefs.getString(ApplicationPreferences.IGNORE_FILES, "").split("\\|");
        sReviewedColor       = prefs.getInt(context.getString(R.string.pref_key_reviewed_color),  context.getResources().getInteger(R.integer.pref_default_reviewed_color));
        sInvalidColor        = prefs.getInt(context.getString(R.string.pref_key_invalid_color),   context.getResources().getInteger(R.integer.pref_default_invalid_color));
        sNoteColor           = prefs.getInt(context.getString(R.string.pref_key_note_color),      context.getResources().getInteger(R.integer.pref_default_note_color));
        sSelectionColor      = prefs.getInt(context.getString(R.string.pref_key_selection_color), context.getResources().getInteger(R.integer.pref_default_selection_color));
        sFontSize            = prefs.getInt(context.getString(R.string.pref_key_font_size),       context.getResources().getInteger(R.integer.pref_default_font_size));
        sTabSize             = prefs.getInt(context.getString(R.string.pref_key_tab_size),        context.getResources().getInteger(R.integer.pref_default_tab_size));
        sBigFileSize         = prefs.getInt(context.getString(R.string.pref_key_big_file_size),   context.getResources().getInteger(R.integer.pref_default_big_file_size));



        sIgnoreFiles = new ArrayList<>(0);

        for (String fileName : ignoreFiles)
        {
            fileName = Utils.replaceIncorrectIgnoreFileName(fileName);

            if (!TextUtils.isEmpty(fileName) && !sIgnoreFiles.contains(fileName))
            {
                sIgnoreFiles.add(fileName);
            }
        }

        Collections.sort(sIgnoreFiles);



        ColorCache.update(context);
    }

    /**
     * Gets list of ignorable files
     * @return list of ignorable files
     */
    public static ArrayList<String> getIgnoreFiles()
    {
        return sIgnoreFiles;
    }

    /**
     * Gets reviewed color
     * @return reviewed color
     */
    public static int getReviewedColor()
    {
        return sReviewedColor;
    }

    /**
     * Gets invalid color
     * @return invalid color
     */
    public static int getInvalidColor()
    {
        return sInvalidColor;
    }

    /**
     * Gets note color
     * @return note color
     */
    public static int getNoteColor()
    {
        return sNoteColor;
    }

    /**
     * Gets selection color
     * @return selection color
     */
    public static int getSelectionColor()
    {
        return sSelectionColor;
    }

    /**
     * Gets font size
     * @return font size
     */
    public static int getFontSize()
    {
        return sFontSize;
    }

    /**
     * Gets tab size
     * @return tab size
     */
    public static int getTabSize()
    {
        return sTabSize;
    }

    /**
     * Gets big file size
     * @return big file size
     */
    public static int getBigFileSize()
    {
        return sBigFileSize;
    }
}
