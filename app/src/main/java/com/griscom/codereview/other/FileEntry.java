package com.griscom.codereview.other;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.griscom.codereview.R;
import com.griscom.codereview.db.MainDatabase;
import com.griscom.codereview.util.AppLog;

import java.io.File;
import java.util.Locale;

/**
 * File entry in files list
 */
public final class FileEntry
{
    @SuppressWarnings("unused")
    private static final String TAG = "FileEntry";



    private String       mFileName      = null;
    private boolean      mIsDirectory   = false;
    private String       mType          = null;
    private long         mSize          = 0;
    private int          mImageId       = 0;
    private long         mDbFileId      = 0;
    private int          mReviewedCount = 0;
    private int          mInvalidCount  = 0;
    private int          mNoteCount     = 0;
    private int          mRowCount      = 0;
    private String       mFileNote      = null;
    private final Object mLock          = new Object();



    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        String fileName;
        long   dbFileId;
        int    reviewedCount;
        int    invalidCount;
        int    noteCount;
        int    rowCount;
        String fileNote;

        synchronized(mLock)
        {
            fileName      = mFileName;
            dbFileId      = mDbFileId;
            reviewedCount = mReviewedCount;
            invalidCount  = mInvalidCount;
            noteCount     = mNoteCount;
            rowCount      = mRowCount;
            fileNote      = mFileNote;
        }

        return "FileEntry{" +
                "mFileName='"       + fileName      + '\'' +
                ", mIsDirectory="   + mIsDirectory  +
                ", mType='"         + mType         + '\'' +
                ", mSize="          + mSize         +
                ", mImageId="       + mImageId      +
                ", mDbFileId="      + dbFileId      +
                ", mReviewedCount=" + reviewedCount +
                ", mInvalidCount="  + invalidCount  +
                ", mNoteCount="     + noteCount     +
                ", mRowCount="      + rowCount      +
                ", mFileNote='"     + fileNote      + '\'' +
                ", mLock="          + mLock         +
                '}';

    }

    /**
     * Default constructor. Used locally
     */
    private FileEntry()
    {
        // Nothing
    }

    /**
     * Creates FileEntry instance based on File object
     * @param file    file
     */
    private FileEntry(File file)
    {
        mFileName    = file.getName();
        mIsDirectory = file.isDirectory();
        mType        = "";
        mSize        = file.length();

        if (!mIsDirectory)
        {
            int index = mFileName.lastIndexOf('.');

            if (index > 0)
            {
                mType = mFileName.substring(index + 1).toLowerCase(Locale.getDefault());
                mImageId = ExtensionToIcon.getIcon(mType);
            }
            else
            {
                mImageId = R.drawable.__icon_file;
            }
        }
        else
        {
            mImageId = R.drawable.__icon_folder;
        }

        mDbFileId      = 0;
        mReviewedCount = 0;
        mInvalidCount  = 0;
        mNoteCount     = 0;
        mRowCount      = 0;
        mFileNote      = "";
    }

    /**
     * Creates FileEntry instance based on File object
     * @param file    file
     */
    public static FileEntry newInstance(File file)
    {
        return new FileEntry(file);
    }

    /**
     * Creates FileEntry instance for parent folder ".."
     * @return FileEntry instance
     */
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public static FileEntry createParentFolder()
    {
        FileEntry parentFolder = new FileEntry();

        parentFolder.mFileName      = "..";
        parentFolder.mIsDirectory   = true;
        parentFolder.mType          = "";
        parentFolder.mSize          = 0;
        parentFolder.mImageId       = R.drawable.__icon_folder;
        parentFolder.mDbFileId      = 0;
        parentFolder.mReviewedCount = 0;
        parentFolder.mInvalidCount  = 0;
        parentFolder.mNoteCount     = 0;
        parentFolder.mRowCount      = 0;
        parentFolder.mFileNote      = "";

        return parentFolder;
    }

    /**
     * Compares current FileEntry instance with another instance and returns -1 if it's less than another instance according to specified sort type,
     * 1 if more than another instance and 0 if instances equal
     * @param another     another FileEntry instance
     * @param sortType    sort type
     * @return -1 if it's less than another instance according to specified sort type, 1 if more than another instance and 0 if instances equal
     */
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public int compare(FileEntry another, int sortType)
    {
        if (mIsDirectory != another.mIsDirectory)
        {
            return mIsDirectory ? -1 : 1;
        }

        String fileName;
        String anotherFileName;

        synchronized(mLock)
        {
            fileName = mFileName;
        }

        synchronized(another.mLock)
        {
            anotherFileName = another.mFileName;
        }

        if (mIsDirectory)
        {
            if (fileName.equals("..") && !anotherFileName.equals(".."))
            {
                return -1;
            }

            return fileName.compareToIgnoreCase(anotherFileName);
        }

        switch (sortType)
        {
            case SortType.NAME: return fileName.compareToIgnoreCase(anotherFileName);
            case SortType.TYPE: return mType.compareToIgnoreCase(another.mType);
            case SortType.SIZE: return Long.valueOf(mSize).compareTo(another.mSize);

            default:
            {
                AppLog.wtf(TAG, "Unknown sort type: " + sortType);
            }
            break;
        }

        return 0;
    }

    /**
     * Updates data received from DB
     * @param dbFileId         file ID in DB
     * @param reviewedCount    reviewed count
     * @param invalidCount     invalid count
     * @param noteCount        note count
     * @param rowCount         row count
     * @param note             note
     */
    public void updateFromDb(long dbFileId, int reviewedCount, int invalidCount, int noteCount, int rowCount, String note)
    {
        synchronized(mLock)
        {
            mDbFileId      = dbFileId;
            mReviewedCount = reviewedCount;
            mInvalidCount  = invalidCount;
            mNoteCount     = noteCount;
            mRowCount      = rowCount;
            mFileNote      = note;
        }
    }

    /**
     * Gets file name
     * @return file name
     */
    public String getFileName()
    {
        synchronized(mLock)
        {
            return mFileName;
        }
    }

    /**
     * Sets file name
     * @param fileName    file name
     */
    public void setFileName(String fileName)
    {
        synchronized(mLock)
        {
            mFileName = fileName;
        }
    }

    /**
     * Returns true if it's directory
     * @return true if it's directory
     */
    public boolean isDirectory()
    {
        return mIsDirectory;
    }

    /**
     * Gets file type
     * @return file type
     */
    @SuppressWarnings("unused")
    public String getType()
    {
        return mType;
    }

    /**
     * Gets file size
     * @return file size
     */
    public long getSize()
    {
        return mSize;
    }

    /**
     * Gets image resource
     * @return image resource
     */
    public int getImageId()
    {
        return mImageId;
    }

    /**
     * Gets file ID in DB
     * @return file ID in DB
     */
    public long getDbFileId()
    {
        synchronized(mLock)
        {
            return mDbFileId;
        }
    }

    /**
     * Gets reviewed count
     * @return reviewed count
     */
    public int getReviewedCount()
    {
        synchronized(mLock)
        {
            return mReviewedCount;
        }
    }

    /**
     * Gets invalid count
     * @return invalid count
     */
    public int getInvalidCount()
    {
        synchronized(mLock)
        {
            return mInvalidCount;
        }
    }

    /**
     * Gets note count
     * @return note count
     */
    public int getNoteCount()
    {
        synchronized(mLock)
        {
            return mNoteCount;
        }
    }

    /**
     * Gets row count
     * @return row count
     */
    public int getRowCount()
    {
        synchronized(mLock)
        {
            return mRowCount;
        }
    }

    /**
     * Gets file note
     * @return file note
     */
    public String getFileNote()
    {
        synchronized(mLock)
        {
            return mFileNote;
        }
    }

    /**
     * Sets file stats
     * @param context          context
     * @param filePath         path to file
     * @param reviewedCount    reviewed count
     * @param invalidCount     invalid count
     * @param noteCount        note count
     * @param rowCount         row count
     */
    public void setFileStats(Context context, String filePath, int reviewedCount, int invalidCount, int noteCount, int rowCount)
    {
        synchronized(mLock)
        {
            mReviewedCount = reviewedCount;
            mInvalidCount  = invalidCount;
            mNoteCount     = noteCount;
            mRowCount      = rowCount;



            MainDatabase helper = MainDatabase.newInstance(context);
            SQLiteDatabase db = helper.getWritableDatabase();

            if (mDbFileId <= 0)
            {
                mDbFileId = MainDatabase.getOrCreateFileId(db, filePath);
            }

            MainDatabase.updateFileStats(db, mDbFileId, mReviewedCount, mInvalidCount, mNoteCount, mRowCount);

            db.close();
        }
    }

    /**
     * Sets file note
     * @param context     context
     * @param filePath    path to file
     * @param note        note
     */
    public void setFileNote(Context context, String filePath, String note)
    {
        synchronized(mLock)
        {
            if (!mFileNote.equals(note))
            {
                mFileNote = note;



                MainDatabase helper = MainDatabase.newInstance(context);
                SQLiteDatabase db = helper.getWritableDatabase();

                if (mDbFileId <= 0)
                {
                    mDbFileId = MainDatabase.getOrCreateFileId(db, filePath);
                }

                MainDatabase.updateFileNote(db, mDbFileId, note);

                db.close();
            }
        }
    }
}
