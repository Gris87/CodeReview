package com.griscom.codereview.other;

import android.content.Context;

import com.griscom.codereview.R;

/**
 * Color cache
 */
@SuppressWarnings("WeakerAccess")
public final class ColorCache
{
    @SuppressWarnings("unused")
    private static final String TAG = "ColorCache";



    @SuppressWarnings("ConstantNamingConvention")
    private static final int[] sCache = new int[SelectionType.MAX + 1];



    /**
     * Disabled default constructor
     */
    private ColorCache()
    {
        // Nothing
    }

    /**
     * Updates color cache from ApplicationSettings
     */
    public static void update(Context context)
    {
        sCache[SelectionType.REVIEWED] = ApplicationSettings.getReviewedColor();
        sCache[SelectionType.INVALID]  = ApplicationSettings.getInvalidColor();
        sCache[SelectionType.NOTE]     = ApplicationSettings.getNoteColor();

        //noinspection deprecation
        sCache[SelectionType.CLEAR]    = context.getResources().getColor(R.color.windowBackground);
    }

    /**
     * Gets color for specified selection type
     * @param selectionType    selection type
     * @return color for specified selection type
     */
    public static int get(int selectionType)
    {
        return sCache[selectionType];
    }
}
