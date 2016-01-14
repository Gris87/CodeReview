package com.griscom.codereview.lists;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.griscom.codereview.R;
import com.griscom.codereview.other.ApplicationPreferences;
import com.griscom.codereview.other.ApplicationSettings;

public class IgnoreFilesAdapter extends BaseAdapter
{
    private Activity           mContext;
    private ArrayList<String>  mFiles;
    private boolean            mSelectionMode;
    private ArrayList<Integer> mSelection;



    private static class ViewHolder
    {
        CheckBox mCheckBox;
        TextView mFileName;
    }



    public IgnoreFilesAdapter(Activity context)
    {
        mContext       = context;
        mFiles         = new ArrayList<String>();
        mSelectionMode = false;
        mSelection     = new ArrayList<Integer>();

        // -----------------------------------------------------------------------------------

        String[] files=ApplicationSettings.ignoreFiles(mContext);

        if (files!=null)
        {
            for (int i=0; i<files.length; ++i)
            {
                if (!TextUtils.isEmpty(files[i]))
                {
                    mFiles.add(removeIncorrectChars(files[i]));
                }
            }

            Collections.sort(mFiles);
        }
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

        View resView=inflater.inflate(R.layout.list_item_ignore_files, parent, false);

        ViewHolder holder=new ViewHolder();

        holder.mCheckBox = (CheckBox)resView.findViewById(R.id.checkbox);
        holder.mFileName = (TextView)resView.findViewById(R.id.fileNameTextView);

        resView.setTag(holder);

        return resView;
    }

    private void bindView(int position, View view)
    {
        String fileName=mFiles.get(position);

        ViewHolder holder=(ViewHolder)view.getTag();

        if (mSelectionMode)
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
        fileName=removeIncorrectChars(fileName);

        if (!TextUtils.isEmpty(fileName) && !mFiles.contains(fileName))
        {
            mFiles.add(fileName);

            updateList();
        }
    }

    public void renameFile(int index, String fileName)
    {
        fileName=removeIncorrectChars(fileName);

        mFiles.remove(index);

        if (!TextUtils.isEmpty(fileName) && !mFiles.contains(fileName))
        {
            mFiles.add(fileName);
        }

        updateList();
    }

    public void removeFile(int index)
    {
        mFiles.remove(index);

        updateList();
    }

    public void removeSelectedFiles()
    {
        Collections.sort(mSelection);

        for (int i=mSelection.size()-1; i>=0; --i)
        {
            mFiles.remove(mSelection.get(i).intValue());
        }

        updateList();
    }

    private void updateList()
    {
        Collections.sort(mFiles);

        // -----------------------------------------------------------------------------------

        StringBuilder res=new StringBuilder();

        for (int i=0; i<mFiles.size(); ++i)
        {
            if (i>0)
            {
                res.append('|');
            }

            res.append(mFiles.get(i));
        }

        SharedPreferences prefs=mContext.getSharedPreferences(ApplicationPreferences.FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=prefs.edit();

        editor.putString(ApplicationPreferences.IGNORE_FILES, res.toString());

        editor.commit();

        // -----------------------------------------------------------------------------------

        notifyDataSetChanged();
    }

    private String removeIncorrectChars(String st)
    {
        return st.replace("\\", "_")
                 .replace("/",  "_")
                 .replace(":",  "_")
                 .replace("\"", "_")
                 .replace("<",  "_")
                 .replace(">",  "_")
                 .replace("|",  "_");
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
