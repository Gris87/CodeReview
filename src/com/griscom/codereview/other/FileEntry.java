package com.griscom.codereview.other;

import java.io.File;

import android.annotation.SuppressLint;
import android.util.Log;

import com.griscom.codereview.R;

@SuppressLint("DefaultLocale")
public class FileEntry
{
    private static final String TAG="FileEntry";

    private String  mFileName;
    private boolean mIsDirectory;
    private String  mType;
    private long    mSize;
    private int     mImageId;

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
                mImageId=R.drawable.icon_file;
            }
        }
        else
        {
            mImageId=R.drawable.icon_folder;
        }
    }

    public static FileEntry createParentFolder()
    {
        FileEntry parentFolder=new FileEntry();

        parentFolder.mFileName    = "..";
        parentFolder.mIsDirectory = true;
        parentFolder.mType        = "";
        parentFolder.mSize        = 0;
        parentFolder.mImageId     = R.drawable.icon_folder;

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

    public String getFileName()
    {
        return mFileName;
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
}
