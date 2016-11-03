package com.griscom.codereview.other;

/**
 * Selection type
 */
@SuppressWarnings("WeakerAccess")
public final class SelectionType
{
    @SuppressWarnings("unused")
    private static final String TAG = "SelectionType";



    public static final int REVIEWED = 0;
    public static final int INVALID  = 1;
    public static final int NOTE     = 2;
    public static final int CLEAR    = 3;

    public static final int MAX = CLEAR;



    /**
     * Disabled default constructor
     */
    private SelectionType()
    {
        // Nothing
    }
}
