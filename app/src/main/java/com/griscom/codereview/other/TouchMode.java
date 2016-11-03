package com.griscom.codereview.other;

/**
 * Touch mode
 */
public final class TouchMode
{
    @SuppressWarnings("unused")
    private static final String TAG = "TouchMode";



    public static final int NONE   = 0;
    public static final int DRAG   = 1;
    public static final int SELECT = 2;
    public static final int ZOOM   = 3;



    /**
     * Disabled default constructor
     */
    private TouchMode()
    {
        // Nothing
    }
}
