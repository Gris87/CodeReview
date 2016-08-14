package com.griscom.codereview.other;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.griscom.codereview.R;
import com.griscom.codereview.db.MainDatabase;

import java.io.File;

@SuppressLint("DefaultLocale")
public class FileEntry
{
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

    private FileEntry()
    {
    }

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

    public boolean isLess(FileEntry another, int sortType)
    {
        if (mIsDirectory != another.mIsDirectory)
        {
            return mIsDirectory;
        }

        if (mIsDirectory)
        {
            return mFileName.equals("..") || mFileName.compareToIgnoreCase(another.mFileName) < 0;
        }

        switch (sortType)
        {
            case SortType.NAME: return mFileName.compareToIgnoreCase(another.mFileName) < 0;
            case SortType.TYPE: return mType.compareTo(another.mType) < 0;
            case SortType.SIZE: return mSize < another.mSize;

            default:
                Log.e(TAG, "Unknown sort type: " + String.valueOf(sortType));
            break;
        }

        return false;
    }

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

    public String getFileName()
    {
        return mFileName;
    }

    public void setFileName(String fileName)
    {
        mFileName = fileName;
    }

    public boolean isDirectory()
    {
        return mIsDirectory;
    }

    public String getType()
    {
        return mType;
    }

    public long getSize()
    {
        return mSize;
    }

    public int getImageId()
    {
        return mImageId;
    }

    public int getDbFileId()
    {
        synchronized(this)
        {
            return mDbFileId;
        }
    }

    public int getReviewedCount()
    {
        synchronized(this)
        {
            return mReviewedCount;
        }
    }

    public int getInvalidCount()
    {
        synchronized(this)
        {
            return mInvalidCount;
        }
    }

    public int getNoteCount()
    {
        synchronized(this)
        {
            return mNoteCount;
        }
    }

    public int getRowCount()
    {
        synchronized(this)
        {
            return mRowCount;
        }
    }

    public String getFileNote()
    {
        synchronized(this)
        {
            return mFileNote;
        }
    }

    public void setFileNote(Context context, String fileName, String note)
    {
        synchronized(this)
        {
            mFileNote = note;

            MainDatabase helper = new MainDatabase(context);

            SQLiteDatabase db = helper.getWritableDatabase();

            if (mDbFileId <= 0)
            {
                mDbFileId = helper.getOrCreateFile(db, fileName);
            }

            helper.updateFileNote(db, mDbFileId, note);

            db.close();
        }
    }
}
