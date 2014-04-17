package com.griscom.codereview.lists;

import java.io.File;
import java.util.ArrayList;

import junit.framework.Assert;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.griscom.codereview.BuildConfig;
import com.griscom.codereview.R;
import com.griscom.codereview.other.FileEntry;
import com.griscom.codereview.other.SortType;

public class FilesAdapter extends BaseAdapter
{
    private Context              mContext;
    private String               mCurrentPath;
    private ArrayList<FileEntry> mFiles;
    private SortType             mSortType;



    private static class ViewHolder
    {
        ImageView mExtenstion;
        TextView  mFileName;
    }



    public FilesAdapter(Context context)
    {
        mContext     = context;
        mCurrentPath = Environment.getExternalStorageDirectory().getPath();
        mFiles       = new ArrayList<FileEntry>();
        mSortType    = SortType.Alphabet;

        rescan();
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

        View resView=inflater.inflate(R.layout.files_list_item, parent, false);

        ViewHolder aHolder=new ViewHolder();

        aHolder.mExtenstion = (ImageView) resView.findViewById(R.id.extensionImageView);
        aHolder.mFileName   = (TextView)  resView.findViewById(R.id.fileNameTextView);

        resView.setTag(aHolder);

        return resView;
    }

    private void bindView(int position, View view)
    {
        FileEntry file=mFiles.get(position);

        ViewHolder holder=(ViewHolder)view.getTag();

        holder.mExtenstion.setImageResource(file.getImageId());
        holder.mFileName.setText(file.getFileName());
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

        setCurrentPath(mCurrentPath.substring(0, mCurrentPath.lastIndexOf('/')));
    }

    public void rescan()
    {
        mFiles.clear();

        if (!mCurrentPath.equals("/"))
        {
            mFiles.add(FileEntry.createParentFolder());
        }



        File folder=new File(mCurrentPath);
        File[] files=folder.listFiles();

        if (files!=null)
        {
            for (int i=0; i<files.length; ++i)
            {
                FileEntry newEntry=new FileEntry(files[i]);

                mFiles.add(newEntry);
            }
        }



        sort();
    }

    public void sort()
    {
        sort(SortType.None);
    }

    public void sort(SortType sortType)
    {
        if (sortType!=SortType.None)
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

    public void setCurrentPath(String newPath)
    {
        if (TextUtils.isEmpty(newPath))
        {
            newPath="/";
        }

        if (BuildConfig.DEBUG)
        {
            Assert.assertTrue(!mCurrentPath.equals(newPath));
        }

        mCurrentPath=newPath;

        rescan();
    }

    public String getCurrentPath()
    {
        return mCurrentPath;
    }
}
