package com.griscom.codereview.lists;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.griscom.codereview.R;

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

    public void addFile(String fileName)
    {
        if (!mFiles.contains(fileName))
        {
            mFiles.add(fileName);

            updateList();
        }
    }

    public void renameFile(int index, String fileName)
    {
        mFiles.remove(index);

        if (!mFiles.contains(fileName))
        {
            mFiles.add(fileName);
        }

        updateList();
    }

    private void updateList()
    {
        Collections.sort(mFiles);

        notifyDataSetChanged();
    }
}
