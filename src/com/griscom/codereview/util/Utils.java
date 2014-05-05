package com.griscom.codereview.util;

import android.content.Context;

public class Utils
{
    public static float spToPixels(float sp, Context context)
    {
        return sp*context.getResources().getDisplayMetrics().scaledDensity;
    }
}
