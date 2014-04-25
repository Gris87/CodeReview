package com.griscom.codereview.lists;

import java.io.File;
import java.io.FileNotFoundException;
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

public class IgnoreFilesAdapter extends BaseAdapter
{
    private Context           mContext;
    private ArrayList<String> mFiles;



    private static class ViewHolder
    {
        TextView  mFileName;
    }



    public IgnoreFilesAdapter(Context context)
    {
        mContext = context;
        mFiles   = new ArrayList<String>();
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

        View resView=inflater.inflate(R.layout.ignorefiles_list_item, parent, false);

        ViewHolder aHolder=new ViewHolder();

        aHolder.mFileName = (TextView)resView.findViewById(R.id.fileNameTextView);

        resView.setTag(aHolder);

        return resView;
    }

    private void bindView(int position, View view)
    {
        String fileName=mFiles.get(position);

        ViewHolder holder=(ViewHolder)view.getTag();
		
        holder.mFileName.setText(fileName);
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
}
