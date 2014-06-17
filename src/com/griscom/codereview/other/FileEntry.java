package com.griscom.codereview.other;

import java.io.File;

import android.annotation.SuppressLint;
import android.util.Log;

import com.griscom.codereview.R;
import com.griscom.codereview.db.*;
import android.content.*;
import android.database.sqlite.*;

@SuppressLint("DefaultLocale")
public class FileEntry
{
    private static final String TAG="FileEntry";

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
            int index=mFileName.lastIndexOf('.');

            if (index>0)
            {
                mType=mFileName.substring(index+1).toLowerCase();
                mImageId=ExtensionToIcon.getIcon(mType);
            }
            else
            {
                mImageId=R.drawable.__icon_file;
            }
        }
        else
        {
            mImageId=R.drawable.__icon_folder;
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
        FileEntry parentFolder=new FileEntry();

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

    public boolean isLess(FileEntry another, SortType sortType)
    {
        if (mIsDirectory!=another.mIsDirectory)
        {
            return mIsDirectory;
        }

        if (mIsDirectory)
        {
            if (mFileName.equals(".."))
            {
                return true;
            }

            return mFileName.compareToIgnoreCase(another.mFileName)<0;
        }

        switch (sortType)
        {
            case NAME: return mFileName.compareToIgnoreCase(another.mFileName)<0;
            case TYPE: return mType.compareTo(another.mType)<0;
            case SIZE: return mSize<another.mSize;
            default:
                Log.e(TAG, "Unknown sort type: "+String.valueOf(sortType));
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
	
	public void setFileName(String filename)
    {
        mFileName=filename;
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
        int res;

        synchronized(this)
        {
            res=mDbFileId;
        }

        return res;
    }

    public int getReviewedCount()
    {
        int res;

        synchronized(this)
        {
            res=mReviewedCount;
        }

        return res;
    }

    public int getInvalidCount()
    {
        int res;

        synchronized(this)
        {
            res=mInvalidCount;
        }

        return res;
    }

    public int getNoteCount()
    {
        int res;

        synchronized(this)
        {
            res=mNoteCount;
        }

        return res;
    }

    public int getRowCount()
    {
        int res;

        synchronized(this)
        {
            res=mRowCount;
        }

        return res;
    }

    public String getFileNote()
    {
        String res;

        synchronized(this)
        {
            res=mFileNote;
        }

        return res;
    }

    public void setFileNote(Context context, String filename, String note)
    {
        synchronized(this)
        {
            mFileNote=note;
			
			MainDatabase helper=new MainDatabase(context);
			
			SQLiteDatabase db=helper.getWritableDatabase();
			
			if (mDbFileId<=0)
			{
				mDbFileId=helper.getOrCreateFile(db, filename);
			}
			
			helper.updateFileNote(db, mDbFileId, note);
			
			db.close();
        }
    }
}
