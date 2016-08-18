package com.griscom.codereview.other;

import android.graphics.Color;

/**
 * Color cache
 */
public class ColorCache
{
    @SuppressWarnings("unused")
    private static final String TAG = "ColorCache";



    private static final int SELECTION_COLOR = 4;



    private static int mCache[] = new int[5];



    /**
     * Updates color cache from ApplicationSettings
     */
    public static void update()
    {
        mCache[SelectionColor.REVIEWED.ordinal()] = ApplicationSettings.getReviewedColor();
        mCache[SelectionColor.INVALID.ordinal()]  = ApplicationSettings.getInvalidColor();
        mCache[SelectionColor.NOTE.ordinal()]     = ApplicationSettings.getNoteColor();
        mCache[SelectionColor.CLEAR.ordinal()]    = Color.WHITE;

        mCache[SELECTION_COLOR]                   = ApplicationSettings.getSelectionColor();
    }

    // TODO: SelectionColor? Maybe SelectionType
    /**
     * Gets color for specified selection color
     * @param selectionColor    selection color
     * @return color for specified selection color
     */
    public static int get(SelectionColor selectionColor)
    {
        return mCache[selectionColor.ordinal()];
    }

    // TODO: Need to remove it and use ApplicationSettings.getSelectionColor();
    /**
     * Gets selection color
     * @return selection color
     */
    public static int getSelectionColor()
    {
        return mCache[SELECTION_COLOR];
    }
}
