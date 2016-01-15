package com.griscom.codereview.other;

import android.content.Context;
import android.graphics.Color;

public class ColorCache
{
    private static final int SELECTION_COLOR = 4;

    private static int mCache[] = new int[5];



    public static void update(Context context)
    {
        mCache[SelectionColor.REVIEWED.ordinal()] = ApplicationSettings.reviewedColor(context);
        mCache[SelectionColor.INVALID.ordinal()]  = ApplicationSettings.invalidColor(context);
        mCache[SelectionColor.NOTE.ordinal()]     = ApplicationSettings.noteColor(context);
        mCache[SelectionColor.CLEAR.ordinal()]    = Color.WHITE;

        mCache[SELECTION_COLOR]                   = ApplicationSettings.selectionColor(context);
    }

    public static int get(SelectionColor selectionColor)
    {
        return mCache[selectionColor.ordinal()];
    }

    public static int getSelectionColor()
    {
        return mCache[SELECTION_COLOR];
    }
}
