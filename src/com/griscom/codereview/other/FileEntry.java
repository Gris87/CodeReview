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

	public FileEntry(File file)
	{
		mFileName    = file.getName();
		mIsDirectory = file.isDirectory();
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

	public boolean isLess(FileEntry another, SortType sortType)
	{
	    if (mIsDirectory!=another.mIsDirectory)
	    {
	        return mIsDirectory;
	    }

	    if (mIsDirectory)
	    {
	        return mFileName.compareToIgnoreCase(another.mFileName)<0;
	    }

	    switch (sortType)
        {
            case Alphabet: return mFileName.compareToIgnoreCase(another.mFileName)<0;
            case Size:     return mType.compareToIgnoreCase(another.mType)<0;
            case Type:     return mSize<another.mSize;
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
