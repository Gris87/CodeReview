package com.griscom.codereview.other;

import android.graphics.Color;

/**
 * Color cache
 */
public class ColorCache
{
    @SuppressWarnings("unused")
    private static final String TAG = "ColorCache";



    private static final int mCache[] = new int[SelectionType.MAX + 1];



    /**
     * Updates color cache from ApplicationSettings
     */
    public static void update()
    {
        mCache[SelectionType.REVIEWED] = ApplicationSettings.getReviewedColor();
        mCache[SelectionType.INVALID]  = ApplicationSettings.getInvalidColor();
        mCache[SelectionType.NOTE]     = ApplicationSettings.getNoteColor();
        mCache[SelectionType.CLEAR]    = Color.WHITE;
    }

    /**
     * Gets color for specified selection type
     * @param selectionType    selection type
     * @return color for specified selection type
     */
    public static int get(int selectionType)
    {
        return mCache[selectionType];
    }
}
