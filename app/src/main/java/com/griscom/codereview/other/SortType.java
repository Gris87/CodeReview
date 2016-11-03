package com.griscom.codereview.other;

/**
 * Sort types
 */
public final class SortType
{
    @SuppressWarnings("unused")
    private static final String TAG = "SortType";



    public static final int NONE = 0;
    public static final int NAME = 1;
    public static final int TYPE = 2;
    public static final int SIZE = 3;

    public static final int MIN = NAME;
    public static final int MAX = SIZE;



    /**
     * Disabled default constructor
     */
    private SortType()
    {
        // Nothing
    }
}
