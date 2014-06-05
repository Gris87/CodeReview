package com.griscom.codereview.lists;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import junit.framework.Assert;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.griscom.codereview.BuildConfig;
import com.griscom.codereview.R;
import com.griscom.codereview.other.ApplicationSettings;
import com.griscom.codereview.other.FileEntry;
import com.griscom.codereview.other.SortType;
import com.griscom.codereview.util.Utils;
import com.griscom.codereview.db.*;
import android.database.sqlite.*;

public class FilesAdapter extends BaseAdapter
{
    private Context              mContext;
    private String               mCurrentPath;
    private ArrayList<FileEntry> mFiles;
    private SortType             mSortType;
    private boolean              mSelectionMode;
    private ArrayList<Integer>   mSelection;
	private SQLiteDatabase       mMainDatabase;



    private static class ViewHolder
    {
        CheckBox  mCheckBox;
        ImageView mExtenstion;
        TextView  mFileName;
        TextView  mFileSize;
    }



    public FilesAdapter(Context context)
    {
        mContext       = context;
        mCurrentPath   = Environment.getExternalStorageDirectory().getPath();
        mFiles         = new ArrayList<FileEntry>();
        mSortType      = SortType.NAME;
        mSelectionMode = false;
        mSelection     = new ArrayList<Integer>();
		mMainDatabase  = new MainDatabase(mContext).getWritableDatabase();
		
        rescan();
    }

	@Override
	protected void finalize() throws Throwable
	{
		mMainDatabase.close();
		
		super.finalize();
	}

    @Override
    public int getCount()
    {
        return mFiles.size();
    }

    @Override
    public Object getItem(int position)
    {
        return position>=0 && position<mFiles.size() ? mFiles.get(position) : null;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    private View newView(Context context, ViewGroup parent)
    {
        LayoutInflater inflater = LayoutInflater.from(context);

        View resView=inflater.inflate(R.layout.list_item_files, parent, false);

        ViewHolder holder=new ViewHolder();

        holder.mCheckBox   = (CheckBox) resView.findViewById(R.id.checkbox);
        holder.mExtenstion = (ImageView)resView.findViewById(R.id.extensionImageView);
        holder.mFileName   = (TextView) resView.findViewById(R.id.fileNameTextView);
        holder.mFileSize   = (TextView) resView.findViewById(R.id.fileSizeTextView);

        resView.setTag(holder);

        return resView;
    }

    private void bindView(int position, View view)
    {
        FileEntry file=mFiles.get(position);

        ViewHolder holder=(ViewHolder)view.getTag();

        if (
            mSelectionMode
            &&
            (
             position>0
             ||
             !file.isDirectory()
             ||
             !file.getFileName().equals("..")
            )
           )
        {
            holder.mCheckBox.setVisibility(View.VISIBLE);
            holder.mCheckBox.setOnCheckedChangeListener(null);
            holder.mCheckBox.setChecked(mSelection.contains(Integer.valueOf(position)));
            holder.mCheckBox.setOnCheckedChangeListener(new CheckedChangedListener(position));
        }
        else
        {
            holder.mCheckBox.setVisibility(View.GONE);
        }

        holder.mExtenstion.setImageResource(file.getImageId());
        holder.mFileName.setText(file.getFileName());

        if (!mSelectionMode && !file.isDirectory())
        {
            holder.mFileSize.setVisibility(View.VISIBLE);
            holder.mFileSize.setText(Utils.bytesToString(file.getSize()));
        }
        else
        {
            holder.mFileSize.setVisibility(View.GONE);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view=null;

        if (convertView!=null)
        {
            view=convertView;
        }
        else
        {
            view=newView(mContext, parent);
        }

        bindView(position, view);

        return view;
    }

    public void goUp()
    {
        if (BuildConfig.DEBUG)
        {
            Assert.assertTrue(!mCurrentPath.equals("/"));
        }

        setCurrentPathBacktrace(mCurrentPath.substring(0, mCurrentPath.lastIndexOf('/')));
    }

    public boolean rescan()
    {
        if (!(new File(mCurrentPath).exists()))
        {
            setCurrentPathBacktrace(pathToFile("."));
            return false;
        }



        mFiles.clear();

        if (!mCurrentPath.equals("/"))
        {
            mFiles.add(FileEntry.createParentFolder());
        }



        File folder=new File(mCurrentPath);
        File[] files=folder.listFiles();

        if (files!=null)
        {
            ArrayList<String> ignoreFiles=new ArrayList<String>();

            String[] filterFiles=ApplicationSettings.ignoreFiles(mContext);

            if (filterFiles!=null)
            {
                for (int i=0; i<filterFiles.length; ++i)
                {
                    if (!TextUtils.isEmpty(filterFiles[i]))
                    {
                        ignoreFiles.add(filterFiles[i]);
                    }
                }
            }

            WildcardFileFilter filter=new WildcardFileFilter(ignoreFiles);

            for (int i=0; i<files.length; ++i)
            {
                if (!filter.accept(files[i]))
                {
                    FileEntry newEntry=new FileEntry(files[i]);

                    mFiles.add(newEntry);
                }
            }
        }



        sort();

        return true;
    }

    public void sort()
    {
        sort(SortType.NONE);
    }

    public void sort(SortType sortType)
    {
        if (sortType!=SortType.NONE)
        {
            mSortType=sortType;
        }

        for (int e=0; e<mFiles.size()-1; ++e)
        {
            int minIndex=e;

            for (int i=e+1; i<mFiles.size(); ++i)
            {
                if (mFiles.get(i).isLess(mFiles.get(minIndex), mSortType))
                {
                    minIndex=i;
                }
            }

            if (e!=minIndex)
            {
                FileEntry temp=mFiles.get(e);
                mFiles.set(e, mFiles.get(minIndex));
                mFiles.set(minIndex, temp);
            }
        }

        notifyDataSetChanged();
    }

    public String pathToFile(String fileName)
    {
        if (mCurrentPath.endsWith("/"))
        {
            return mCurrentPath+fileName;
        }
        else
        {
            return mCurrentPath+"/"+fileName;
        }
    }

    public int indexOf(String fileName)
    {
        for (int i=0; i<mFiles.size(); ++i)
        {
            if (mFiles.get(i).getFileName().equals(fileName))
            {
                return i;
            }
        }

        return -1;
    }

    public void setCurrentPathBacktrace(String newPath)
    {
        do
        {
            try
            {
                setCurrentPath(newPath);
                return;
            }
            catch (FileNotFoundException e)
            {
                if (BuildConfig.DEBUG)
                {
                    Assert.assertTrue(!TextUtils.isEmpty(newPath) && !newPath.equals("/"));
                }

                newPath=newPath.substring(0, newPath.lastIndexOf('/'));
            }
        } while (true);
    }

    public void setCurrentPath(String newPath) throws FileNotFoundException
    {
        if (TextUtils.isEmpty(newPath))
        {
            newPath="/";
        }

        if (!(new File(newPath).exists()))
        {
            throw new FileNotFoundException();
        }

        mCurrentPath=newPath;

        rescan();
    }

    public String getCurrentPath()
    {
        return mCurrentPath;
    }

    public SortType getSortType()
    {
        return mSortType;
    }

    public void setSelected(int index, boolean checked)
    {
        if (mSelectionMode)
        {
            if (checked)
            {
                mSelection.add(Integer.valueOf(index));
            }
            else
            {
                mSelection.remove(Integer.valueOf(index));
            }

            notifyDataSetChanged();
        }
    }

    public void setSelectionMode(boolean enable)
    {
        if (mSelectionMode!=enable)
        {
            mSelectionMode=enable;
            mSelection.clear();

            notifyDataSetChanged();
        }
    }

    private class CheckedChangedListener implements CompoundButton.OnCheckedChangeListener
    {
        private Integer mPosition;

        public CheckedChangedListener(int position)
        {
            mPosition=Integer.valueOf(position);
        }

        @Override
        public void onCheckedChanged(CompoundButton button, boolean checked)
        {
            if (checked)
            {
                mSelection.add(mPosition);
            }
            else
            {
                mSelection.remove(mPosition);
            }
        }
    }
}
