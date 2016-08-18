package com.griscom.codereview.lists;

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

import java.util.ArrayList;
import java.util.Collections;

/**
 * Adapter that used in IgnoreFilesActivity
 */
public class IgnoreFilesAdapter extends BaseAdapter
{
    @SuppressWarnings("unused")
    private static final String TAG = "IgnoreFilesAdapter";



    private Activity           mContext;
    private ArrayList<String>  mFiles;
    private boolean            mSelectionMode;
    private ArrayList<Integer> mSelection;



    /**
     * View holder
     */
    private static class ViewHolder
    {
        CheckBox mCheckBox;
        TextView mFileName;
    }



    /**
     * Creates instance of IgnoreFilesAdapter
     * @param context    context
     */
    public IgnoreFilesAdapter(Activity context)
    {
        mContext       = context;
        mFiles         = new ArrayList<>();
        mSelectionMode = false;
        mSelection     = new ArrayList<>();

        // -----------------------------------------------------------------------------------

        String[] files = ApplicationSettings.getIgnoreFiles();

        for (String file : files)
        {
            if (!TextUtils.isEmpty(file))
            {
                mFiles.add(replaceIncorrectChars(file));
            }
        }

        Collections.sort(mFiles);
    }

    /** {@inheritDoc} */
    @Override
    public int getCount()
    {
        return mFiles.size();
    }

    /** {@inheritDoc} */
    @Override
    public Object getItem(int position)
    {
        return position >= 0 && position < mFiles.size() ? mFiles.get(position) : null;
    }

    /** {@inheritDoc} */
    @Override
    public long getItemId(int position)
    {
        return position;
    }

    /**
     * Creates new view for adapter item
     * @param parent     parent view
     * @return view for adapter item
     */
    private View newView(ViewGroup parent)
    {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View resView = inflater.inflate(R.layout.list_item_ignore_files, parent, false);

        ViewHolder holder = new ViewHolder();

        holder.mCheckBox = (CheckBox)resView.findViewById(R.id.checkbox);
        holder.mFileName = (TextView)resView.findViewById(R.id.fileNameTextView);

        resView.setTag(holder);

        return resView;
    }

    /**
     * Binds item data to the view
     * @param position    item position
     * @param view        item view
     */
    private void bindView(int position, View view)
    {
        String fileName = mFiles.get(position);

        ViewHolder holder = (ViewHolder)view.getTag();

        if (mSelectionMode)
        {
            holder.mCheckBox.setVisibility(View.VISIBLE);
            holder.mCheckBox.setOnCheckedChangeListener(null);
            holder.mCheckBox.setChecked(mSelection.contains(position));
            holder.mCheckBox.setOnCheckedChangeListener(new CheckedChangedListener(position));
        }
        else
        {
            holder.mCheckBox.setVisibility(View.GONE);
        }

        holder.mFileName.setText(fileName);
    }

    /** {@inheritDoc} */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view;

        if (convertView != null)
        {
            view = convertView;
        }
        else
        {
            view = newView(parent);
        }

        bindView(position, view);

        return view;
    }

    /**
     * Adds file name to the list
     * @param fileName    file name
     */
    public void add(String fileName)
    {
        fileName = replaceIncorrectChars(fileName);

        if (!TextUtils.isEmpty(fileName) && !mFiles.contains(fileName))
        {
            mFiles.add(fileName);

            updateList();
        }
    }

    /**
     * Replaces item at specified index
     * @param index       item index
     * @param fileName    file name
     */
    public void replace(int index, String fileName)
    {
        fileName = replaceIncorrectChars(fileName);

        mFiles.remove(index);

        if (!TextUtils.isEmpty(fileName) && !mFiles.contains(fileName))
        {
            mFiles.add(fileName);
        }

        updateList();
    }

    /**
     * Removes selected items
     */
    public void removeSelected()
    {
        Collections.sort(mSelection);

        for (int i = mSelection.size() - 1; i >= 0; --i)
        {
            mFiles.remove(mSelection.get(i).intValue());
        }

        updateList();
    }

    /**
     * Updates list in UI and in SharedPreferences
     */
    private void updateList()
    {
        Collections.sort(mFiles);

        // -----------------------------------------------------------------------------------

        StringBuilder res = new StringBuilder();

        for (int i = 0; i < mFiles.size(); ++i)
        {
            if (i > 0)
            {
                res.append('|');
            }

            res.append(mFiles.get(i));
        }

        SharedPreferences prefs = mContext.getSharedPreferences(ApplicationPreferences.MAIN_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(ApplicationPreferences.IGNORE_FILES, res.toString());

        editor.apply();

        // -----------------------------------------------------------------------------------

        notifyDataSetChanged();
    }

    /**
     * Enables or disables selection mode
     * @param enable    true, if need to enable
     */
    public void setSelectionMode(boolean enable)
    {
        if (mSelectionMode != enable)
        {
            mSelectionMode = enable;
            mSelection.clear();

            notifyDataSetChanged();
        }
    }

    /**
     * Selects or deselects item at specified index
     * @param index      item index
     * @param checked    true, if need to select
     */
    public void setSelected(int index, boolean checked)
    {
        if (mSelectionMode)
        {
            if (checked)
            {
                mSelection.add(index);
            }
            else
            {
                mSelection.remove(Integer.valueOf(index));
            }

            notifyDataSetChanged();
        }
    }

    /**
     * Replaces incorrect characters to underscore
     * @param text    text
     * @return specified text without incorrect characters
     */
    private String replaceIncorrectChars(String text)
    {
        return text.replace("\\", "_")
                   .replace("/",  "_")
                   .replace(":",  "_")
                   .replace("\"", "_")
                   .replace("<",  "_")
                   .replace(">",  "_")
                   .replace("|",  "_");
    }



    /**
     * Listener for checked changed event
     */
    private class CheckedChangedListener implements CompoundButton.OnCheckedChangeListener
    {
        private Integer mPosition;



        /**
         * Creates instance of CheckedChangedListener
         * @param position    item position
         */
        public CheckedChangedListener(int position)
        {
            mPosition = position;
        }

        /** {@inheritDoc} */
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
