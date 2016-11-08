package com.griscom.codereview.lists;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.griscom.codereview.R;
import com.griscom.codereview.other.ApplicationPreferences;
import com.griscom.codereview.other.ApplicationSettings;
import com.griscom.codereview.util.Utils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Adapter that used in IgnoreFilesActivity
 */
public class IgnoreFilesAdapter extends BaseAdapter
{
    @SuppressWarnings("unused")
    private static final String TAG = "IgnoreFilesAdapter";



    public static final int SELECTION_MODE_DISABLED = 0;
    public static final int SELECTION_MODE_ENABLED  = 1;



    public static final int ITEM_DESELECTED = 0;
    public static final int ITEM_SELECTED   = 1;



    private Activity           mContext       = null;
    private ArrayList<String>  mFiles         = null;
    private int                mSelectionMode = 0;
    private ArrayList<Integer> mSelection     = null;



    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "IgnoreFilesAdapter{" +
                "mContext="         + mContext       +
                ", mFiles="         + mFiles         +
                ", mSelectionMode=" + mSelectionMode +
                ", mSelection="     + mSelection     +
                '}';
    }

    /**
     * Creates instance of IgnoreFilesAdapter
     * @param context    context
     */
    @SuppressWarnings("ImplicitCallToSuper")
    private IgnoreFilesAdapter(Activity context)
    {
        mContext       = context;
        mFiles         = ApplicationSettings.getIgnoreFiles();
        mSelectionMode = SELECTION_MODE_DISABLED;
        mSelection     = new ArrayList<>(0);
    }

    /**
     * Creates instance of IgnoreFilesAdapter
     * @param context    context
     */
    public static IgnoreFilesAdapter newInstance(Activity context)
    {
        return new IgnoreFilesAdapter(context);
    }

    /** {@inheritDoc} */
    @Override
    public int getCount()
    {
        return mFiles.size();
    }

    /** {@inheritDoc} */
    @Nullable
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

        holder.setCheckBox((CheckBox)resView.findViewById(R.id.checkbox));
        holder.setFileName((TextView)resView.findViewById(R.id.fileNameTextView));

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

        if (mSelectionMode == SELECTION_MODE_ENABLED)
        {
            holder.getCheckBox().setVisibility(View.VISIBLE);
            holder.getCheckBox().setChecked(mSelection.contains(position));
        }
        else
        {
            holder.getCheckBox().setVisibility(View.GONE);
        }

        holder.getFileName().setText(fileName);
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
     * @param name    file name
     */
    public void add(String name)
    {
        String fileName = Utils.replaceIncorrectIgnoreFileName(name);

        if (!TextUtils.isEmpty(fileName) && !mFiles.contains(fileName))
        {
            mFiles.add(fileName);

            updateList();
        }
    }

    /**
     * Replaces item at specified index
     * @param index    item index
     * @param name     file name
     */
    public void replace(int index, String name)
    {
        String fileName = Utils.replaceIncorrectIgnoreFileName(name);

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

        StringBuilder res = new StringBuilder(0);

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
     * @param selectionMode    selection mode
     */
    public void setSelectionMode(int selectionMode)
    {
        if (mSelectionMode != selectionMode)
        {
            mSelectionMode = selectionMode;
            mSelection.clear();

            notifyDataSetChanged();
        }
    }

    /**
     * Selects or deselects item at specified index
     * @param index           item index
     * @param itemSelected    item selected
     */
    public void setSelected(int index, int itemSelected)
    {
        if (mSelectionMode == SELECTION_MODE_ENABLED)
        {
            if (itemSelected == ITEM_SELECTED)
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
     * Gets selection
     * @return selection
     */
    public ArrayList<Integer> getSelection()
    {
        return mSelection;
    }



    /**
     * View holder
     */
    @SuppressWarnings("ClassWithoutConstructor")
    private static class ViewHolder
    {
        private CheckBox mCheckBox = null;
        private TextView mFileName = null;



        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "ViewHolder{" +
                    "mCheckBox="   + mCheckBox +
                    ", mFileName=" + mFileName +
                    '}';
        }

        /**
         * Gets checkBox
         * @return checkBox
         */
        CheckBox getCheckBox()
        {
            return mCheckBox;
        }

        /**
         * Sets checkBox
         * @param checkBox    checkBox
         */
        void setCheckBox(CheckBox checkBox)
        {
            mCheckBox = checkBox;
        }

        /**
         * Gets file name
         * @return file name
         */
        TextView getFileName()
        {
            return mFileName;
        }

        /**
         * Sets file name
         * @param fileName    file name
         */
        void setFileName(TextView fileName)
        {
            mFileName = fileName;
        }
    }
}
