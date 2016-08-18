package com.griscom.codereview.other;

import android.content.Context;
import android.content.SharedPreferences;

import com.griscom.codereview.R;

/**
 * Application settings
 */
public class ApplicationSettings
{
    @SuppressWarnings("unused")
    private static final String TAG = "ApplicationSettings";



    private static String[] mIgnoreFiles    = null;
    private static int      mReviewedColor  = 0;
    private static int      mInvalidColor   = 0;
    private static int      mNoteColor      = 0;
    private static int      mSelectionColor = 0;
    private static int      mFontSize       = 0;
    private static int      mTabSize        = 0;



    /**
     * Updates application settings from SharedPreferences
     * @param context    context
     */
    public static void update(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(ApplicationPreferences.MAIN_SHARED_PREFERENCES, Context.MODE_PRIVATE);

        mIgnoreFiles    = prefs.getString(ApplicationPreferences.IGNORE_FILES, "").split("\\|");
        mReviewedColor  = prefs.getInt(context.getString(R.string.pref_key_reviewed_color),  context.getResources().getInteger(R.integer.pref_default_reviewed_color));
        mInvalidColor   = prefs.getInt(context.getString(R.string.pref_key_invalid_color),   context.getResources().getInteger(R.integer.pref_default_invalid_color));
        mNoteColor      = prefs.getInt(context.getString(R.string.pref_key_note_color),      context.getResources().getInteger(R.integer.pref_default_note_color));
        mSelectionColor = prefs.getInt(context.getString(R.string.pref_key_selection_color), context.getResources().getInteger(R.integer.pref_default_selection_color));
        mFontSize       = prefs.getInt(context.getString(R.string.pref_key_font_size),       context.getResources().getInteger(R.integer.pref_default_font_size));
        mTabSize        = prefs.getInt(context.getString(R.string.pref_key_tab_size),        context.getResources().getInteger(R.integer.pref_default_tab_size));

        ColorCache.update();
    }

    /**
     * Gets list of ignorable files
     * @return list of ignorable files
     */
    public static String[] getIgnoreFiles()
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
}
