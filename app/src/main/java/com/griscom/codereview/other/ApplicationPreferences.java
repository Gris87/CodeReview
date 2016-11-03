package com.griscom.codereview.other;

/**
 * Application preferences
 */
public final class ApplicationPreferences
{
    @SuppressWarnings("unused")
    private static final String TAG = "ApplicationPreferences";



    public static final String MAIN_SHARED_PREFERENCES       = "Application";
    public static final String FILE_NAMES_SHARED_PREFERENCES = "FileNames";
    public static final String NOTES_SHARED_PREFERENCES      = "Notes";
    public static final String COMMENTS_SHARED_PREFERENCES   = "Comments";



    public static final String LAST_PATH      = "LastPath";
    public static final String LAST_FILE      = "LastFile";
    public static final String SORT_TYPE      = "SortType";

    public static final String IGNORE_FILES   = "IgnoreFiles";

    public static final String LAST_FILENAMES = "LastFileNames";
    public static final String ONE_FILENAME   = "FileName";

    public static final String LAST_NOTES     = "LastNotes";
    public static final String ONE_NOTE       = "Note";

    public static final String LAST_COMMENTS  = "LastComments";
    public static final String ONE_COMMENT    = "Comment";



    /**
     * Disabled default constructor
     */
    private ApplicationPreferences()
    {
        // Nothing
    }
}
