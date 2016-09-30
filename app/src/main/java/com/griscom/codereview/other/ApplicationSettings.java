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
public class ApplicationSettings
{
    @SuppressWarnings("unused")
    private static final String TAG = "ApplicationSettings";



    private static ArrayList<String> mIgnoreFiles    = null;
    private static int               mReviewedColor  = 0;
    private static int               mInvalidColor   = 0;
    private static int               mNoteColor      = 0;
    private static int               mSelectionColor = 0;
    private static int               mFontSize       = 0;
    private static int               mTabSize        = 0;
    private static int               mBigFileSize    = 0;



    /**
     * Updates application settings from SharedPreferences
     * @param context    context
     */
    public static void update(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(ApplicationPreferences.MAIN_SHARED_PREFERENCES, Context.MODE_PRIVATE);

        String[] ignoreFiles = prefs.getString(ApplicationPreferences.IGNORE_FILES, "").split("\\|");
        mReviewedColor       = prefs.getInt(context.getString(R.string.pref_key_reviewed_color),  context.getResources().getInteger(R.integer.pref_default_reviewed_color));
        mInvalidColor        = prefs.getInt(context.getString(R.string.pref_key_invalid_color),   context.getResources().getInteger(R.integer.pref_default_invalid_color));
        mNoteColor           = prefs.getInt(context.getString(R.string.pref_key_note_color),      context.getResources().getInteger(R.integer.pref_default_note_color));
        mSelectionColor      = prefs.getInt(context.getString(R.string.pref_key_selection_color), context.getResources().getInteger(R.integer.pref_default_selection_color));
        mFontSize            = prefs.getInt(context.getString(R.string.pref_key_font_size),       context.getResources().getInteger(R.integer.pref_default_font_size));
        mTabSize             = prefs.getInt(context.getString(R.string.pref_key_tab_size),        context.getResources().getInteger(R.integer.pref_default_tab_size));
        mBigFileSize         = prefs.getInt(context.getString(R.string.pref_key_big_file_size),   context.getResources().getInteger(R.integer.pref_default_big_file_size));



        mIgnoreFiles = new ArrayList<>();

        for (String fileName : ignoreFiles)
        {
            fileName = Utils.replaceIncorrectIgnoreFileName(fileName);

            if (!TextUtils.isEmpty(fileName) && !mIgnoreFiles.contains(fileName))
            {
                mIgnoreFiles.add(fileName);
            }
        }

        Collections.sort(mIgnoreFiles);



        ColorCache.update(context);
    }

    /**
     * Gets list of ignorable files
     * @return list of ignorable files
     */
    public static ArrayList<String> getIgnoreFiles()
    {
        return mIgnoreFiles;
    }

    /**
     * Gets reviewed color
     * @return reviewed color
     */
    public static int getReviewedColor()
    {
        return mReviewedColor;
    }

    /**
     * Gets invalid color
     * @return invalid color
     */
    public static int getInvalidColor()
    {
        return mInvalidColor;
    }

    /**
     * Gets note color
     * @return note color
     */
    public static int getNoteColor()
    {
        return mNoteColor;
    }

    /**
     * Gets selection color
     * @return selection color
     */
    public static int getSelectionColor()
    {
        return mSelectionColor;
    }

    /**
     * Gets font size
     * @return font size
     */
    public static int getFontSize()
    {
        return mFontSize;
    }

    /**
     * Gets tab size
     * @return tab size
     */
    public static int getTabSize()
    {
        return mTabSize;
    }

    /**
     * Gets big file size
     * @return big file size
     */
    public static int getBigFileSize()
    {
        return mBigFileSize;
    }
}
