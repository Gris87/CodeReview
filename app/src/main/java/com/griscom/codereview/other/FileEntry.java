package com.griscom.codereview.other;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.griscom.codereview.R;
import com.griscom.codereview.db.MainDatabase;
import com.griscom.codereview.util.AppLog;

import java.io.File;

/**
 * File entry in files list
 */
public class FileEntry
{
    @SuppressWarnings("unused")
    private static final String TAG = "FileEntry";



    private String  mFileName;
    private boolean mIsDirectory;
    private String  mType;
    private long    mSize;
    private int     mImageId;
    private int     mDbFileId;
    private int     mReviewedCount;
    private int     mInvalidCount;
    private int     mNoteCount;
    private int     mRowCount;
    private String  mFileNote;



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
    public FileEntry(File file)
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
                mType = mFileName.substring(index + 1).toLowerCase();
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
     * Creates FileEntry instance for parent folder ".."
     * @return FileEntry instance
     */
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
    public int compare(FileEntry another, int sortType)
    {
        if (mIsDirectory != another.mIsDirectory)
        {
            return mIsDirectory ? -1 : 1;
        }

        if (mIsDirectory)
        {
            if (mFileName.equals("..") && !another.mFileName.equals(".."))
            {
                return -1;
            }

            return mFileName.compareToIgnoreCase(another.mFileName);
        }

        switch (sortType)
        {
            case SortType.NAME: return mFileName.compareToIgnoreCase(another.mFileName);
            case SortType.TYPE: return mType.compareToIgnoreCase(another.mType);
            case SortType.SIZE: return (mSize < another.mSize) ? -1 : (mSize > another.mSize) ? 1 : 0;

            default:
            {
                AppLog.wtf(TAG, "Unknown sort type: " + String.valueOf(sortType));
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
    public void updateFromDb(int dbFileId, int reviewedCount, int invalidCount, int noteCount, int rowCount, String note)
    {
        synchronized(this)
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
        synchronized(this)
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
        synchronized(this)
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
    public int getDbFileId()
    {
        synchronized(this)
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
        synchronized(this)
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
        synchronized(this)
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
        synchronized(this)
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
        synchronized(this)
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
        synchronized(this)
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
        synchronized(this)
        {
            mReviewedCount = reviewedCount;
            mInvalidCount  = invalidCount;
            mNoteCount     = noteCount;
            mRowCount      = rowCount;



            MainDatabase helper = new MainDatabase(context);
            SQLiteDatabase db = helper.getWritableDatabase();

            if (mDbFileId <= 0)
            {
                mDbFileId = helper.getOrCreateFileId(db, filePath);
            }

            helper.updateFileStats(db, mDbFileId, mReviewedCount, mInvalidCount, mNoteCount, mRowCount);

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
        synchronized(this)
        {
            if (!mFileNote.equals(note))
            {
                mFileNote = note;



                MainDatabase helper = new MainDatabase(context);
                SQLiteDatabase db = helper.getWritableDatabase();

                if (mDbFileId <= 0)
                {
                    mDbFileId = helper.getOrCreateFileId(db, filePath);
                }

                helper.updateFileNote(db, mDbFileId, note);

                db.close();
            }
        }
    }
}
