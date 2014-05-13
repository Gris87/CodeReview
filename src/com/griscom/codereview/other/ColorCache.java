package com.griscom.codereview.other;

import android.content.Context;
import android.graphics.Color;

public class ColorCache
{
    private static int mCache[]=new int[4];

    public static void update(Context context)
    {
        mCache[SelectionColor.REVIEWED_COLOR.ordinal()] = ApplicationSettings.reviewedColor(context);
        mCache[SelectionColor.INVALID_COLOR.ordinal()]  = ApplicationSettings.invalidColor(context);
        mCache[SelectionColor.NOTE_COLOR.ordinal()]     = ApplicationSettings.noteColor(context);
        mCache[SelectionColor.CLEAR_COLOR.ordinal()]    = Color.WHITE;
    }

    public static int get(SelectionColor selectionColor)
    {
        return mCache[selectionColor.ordinal()];
    }
}
