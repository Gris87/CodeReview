package com.griscom.codereview.lists;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import junit.framework.Assert;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
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
import com.griscom.codereview.db.MainDatabase;
import com.griscom.codereview.other.ApplicationSettings;
import com.griscom.codereview.other.ColorCache;
import com.griscom.codereview.other.FileEntry;
import com.griscom.codereview.other.SelectionColor;
import com.griscom.codereview.other.SortType;
import com.griscom.codereview.util.Utils;

public class FilesAdapter extends BaseAdapter
{
    private static final String TAG = "FilesAdapter";



    private Context              mContext;
    private DbReaderTask         mDbReaderTask;
    private String               mCurrentPath;
    private ArrayList<FileEntry> mFiles;
    private SortType             mSortType;
    private boolean              mSelectionMode;
    private ArrayList<Integer>   mSelection;



    private static class ViewHolder
    {
        CheckBox  mCheckBox;
        ImageView mExtenstion;
        TextView  mFileNote;
        TextView  mFileName;
        TextView  mFileSize;
    }



    public FilesAdapter(Context context)
    {
        mContext       = context;
        mDbReaderTask  = null;
        mCurrentPath   = Environment.getExternalStorageDirectory().getPath();
        mFiles         = new ArrayList<FileEntry>();
        mSortType      = SortType.NAME;
        mSelectionMode = false;
        mSelection     = new ArrayList<Integer>();

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
        return position >= 0 && position < mFiles.size() ? mFiles.get(position) : null;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    private View newView(Context context, ViewGroup parent)
    {
        LayoutInflater inflater = LayoutInflater.from(context);

        View resView = inflater.inflate(R.layout.list_item_files, parent, false);

        ViewHolder holder = new ViewHolder();

        holder.mCheckBox   = (CheckBox) resView.findViewById(R.id.checkbox);
        holder.mExtenstion = (ImageView)resView.findViewById(R.id.extensionImageView);
        holder.mFileNote   = (TextView) resView.findViewById(R.id.fileNoteTextView);
        holder.mFileName   = (TextView) resView.findViewById(R.id.fileNameTextView);
        holder.mFileSize   = (TextView) resView.findViewById(R.id.fileSizeTextView);

        resView.setTag(holder);

        return resView;
    }

    @SuppressWarnings("deprecation")
    private void bindView(int position, View view)
    {
        FileEntry file = mFiles.get(position);

        ViewHolder holder = (ViewHolder)view.getTag();

        if (file.getDbFileId() > 0)
        {
            int reviewedCount  = file.getReviewedCount();
            int invalidCount   = file.getInvalidCount();
            int noteCount      = file.getNoteCount();
            int rowCount       = file.getRowCount();

            if (rowCount > 0)
            {
                int reviewedPercent = reviewedCount * 100 / rowCount;
                int invalidPercent  = invalidCount  * 100 / rowCount;
                int notePercent     = noteCount     * 100 / rowCount;
                int clearPercent    = 100 - ((reviewedCount + invalidCount + noteCount) * 100 / rowCount);

                if (reviewedPercent <= 0)
                {
                    if (reviewedCount > 0)
                    {
                        reviewedPercent = 1;
                    }
                    else
                    {
                        reviewedPercent = 0;
                    }
                }
                else
                if (reviewedPercent > 100)
                {
                    reviewedPercent = 100;
                }

                if (invalidPercent <= 0)
                {
                    if (invalidCount > 0)
                    {
                        invalidPercent = 1;
                    }
                    else
                    {
                        invalidPercent = 0;
                    }
                }
                else
                if (invalidPercent > 100)
                {
                    invalidPercent = 100;
                }

                if (notePercent <= 0)
                {
                    if (noteCount > 0)
                    {
                        notePercent = 1;
                    }
                    else
                    {
                        notePercent = 0;
                    }
                }
                else
                if (notePercent > 100)
                {
                    notePercent = 100;
                }

                if (clearPercent <= 0)
                {
                    if (reviewedCount + invalidCount + noteCount != rowCount)
                    {
                        clearPercent = 1;
                    }
                    else
                    {
                        clearPercent = 0;
                    }
                }
                else
                if (clearPercent > 100)
                {
                    clearPercent = 100;
                }

                int totalPercent = reviewedPercent + invalidPercent + notePercent + clearPercent;

                if (totalPercent != 100)
                {
                    if (
                        reviewedPercent > invalidPercent
                         &&
                        reviewedPercent > notePercent
                         &&
                        reviewedPercent > clearPercent
                       )
                    {
                        reviewedPercent  - = totalPercent - 100;
                    }
                    if (
                        invalidPercent > reviewedPercent
                         &&
                        invalidPercent > notePercent
                         &&
                        invalidPercent > clearPercent
                       )
                    {
                        invalidPercent   - = totalPercent - 100;
                    }

                    if (
                        notePercent > reviewedPercent
                         &&
                        notePercent > invalidPercent
                         &&
                        notePercent > clearPercent
                       )
                    {
                        notePercent      - = totalPercent - 100;
                    }
                    else
                    {
                        clearPercent     - = totalPercent - 100;
                    }
                }

                // -------------------------------------------- -  -

                Bitmap bitmap = Bitmap.createBitmap(100, 1, Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);

                int curPercent = 0;

                if (reviewedPercent > 0)
                {
                    Paint paint = new Paint();

                    paint.setColor(ColorCache.get(SelectionColor.REVIEWED));
                    paint.setAlpha(220);

                    canvas.drawLine(curPercent, 0, curPercent + reviewedPercent, 0, paint);
                    curPercent  + = reviewedPercent;
                }

                if (invalidPercent > 0)
                {
                    Paint paint = new Paint();

                    paint.setColor(ColorCache.get(SelectionColor.INVALID));
                    paint.setAlpha(220);

                    canvas.drawLine(curPercent, 0, curPercent + invalidPercent, 0, paint);
                    curPercent  + = invalidPercent;
                }

                if (notePercent > 0)
                {
                    Paint paint = new Paint();

                    paint.setColor(ColorCache.get(SelectionColor.NOTE));
                    paint.setAlpha(220);

                    canvas.drawLine(curPercent, 0, curPercent + notePercent, 0, paint);
                    curPercent  + = notePercent;
                }

                if (clearPercent > 0)
                {
                    Paint paint = new Paint();

                    paint.setColor(ColorCache.get(SelectionColor.CLEAR));
                    paint.setAlpha(220);

                    canvas.drawLine(curPercent, 0, curPercent + clearPercent, 0, paint);
                    curPercent  + = clearPercent;
                }

                view.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
            }
            else
            {
                view.setBackgroundDrawable(null);
            }
        }
        else
        {
            view.setBackgroundDrawable(null);
        }


        String note = file.getFileNote();

        if (!TextUtils.isEmpty(note))
        {
            holder.mFileNote.setVisibility(View.VISIBLE);
            holder.mFileNote.setText(note);
        }
        else
        {
            holder.mFileNote.setVisibility(View.GONE);
        }

        if (
            mSelectionMode
             &&
            (
             position > 0
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
        View view = null;

        if (convertView != null)
        {
            view = convertView;
        }
        else
        {
            view = newView(mContext, parent);
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

        synchronized (this)
        {
            mFiles.clear();

            if (!mCurrentPath.equals("/"))
            {
                mFiles.add(FileEntry.createParentFolder());
            }



            File folder = new File(mCurrentPath);
            File[] files = folder.listFiles();

            if (files != null)
            {
                ArrayList<String> ignoreFiles = new ArrayList<String>();

                String[] filterFiles = ApplicationSettings.ignoreFiles(mContext);

                if (filterFiles != null)
                {
                    for (int i = 0; i < filterFiles.length; ++i)
                    {
                        if (!TextUtils.isEmpty(filterFiles[i]))
                        {
                            ignoreFiles.add(filterFiles[i]);
                        }
                    }
                }

                WildcardFileFilter filter = new WildcardFileFilter(ignoreFiles);

                for (int i = 0; i < files.length; ++i)
                {
                    if (!filter.accept(files[i]))
                    {
                        FileEntry newEntry = new FileEntry(files[i]);

                        mFiles.add(newEntry);
                    }
                }
            }
        }

        sort();

        if (mDbReaderTask != null)
        {
            mDbReaderTask.cancel(true);
        }

        mDbReaderTask = new DbReaderTask();
        mDbReaderTask.execute();

        return true;
    }

    public void sort()
    {
        sort(SortType.NONE);
    }

    public void sort(SortType sortType)
    {
        if (sortType != SortType.NONE)
        {
            mSortType = sortType;
        }

        synchronized (this)
        {
            for (int e = 0; e < mFiles.size() - 1; ++e)
            {
                int minIndex = e;

                for (int i = e + 1; i < mFiles.size(); ++i)
                {
                    if (mFiles.get(i).isLess(mFiles.get(minIndex), mSortType))
                    {
                        minIndex = i;
                    }
                }

                if (e != minIndex)
                {
                    FileEntry temp = mFiles.get(e);
                    mFiles.set(e, mFiles.get(minIndex));
                    mFiles.set(minIndex, temp);
                }
            }
        }

        notifyDataSetChanged();
    }

    public String pathToFile(String fileName)
    {
        synchronized (this)
        {
            if (mCurrentPath.endsWith("/"))
            {
                return mCurrentPath + fileName;
            }
            else
            {
                return mCurrentPath + "/" + fileName;
            }
        }
    }

    public int indexOf(String fileName)
    {
        synchronized (this)
        {
            for (int i = 0; i < mFiles.size(); ++i)
            {
                if (mFiles.get(i).getFileName().equals(fileName))
                {
                    return i;
                }
            }
        }

        return  - 1;
    }

    public void assignNote(int files[], String note)
    {
        for (int i = 0; i < files.length; ++i)
        {
            mFiles.get(files[i]).setFileNote(mContext, pathToFile(mFiles.get(files[i]).getFileName()), note);
        }

        notifyDataSetChanged();
    }

    public boolean renameFile(int index, String filename)
    {
        if (
            filename.contains("/")
             ||
            filename.contains("\\")
           )
        {
            return false;
        }

        if (new File(pathToFile(mFiles.get(index).getFileName())).renameTo(new File(pathToFile(filename))))
        {
            synchronized (this)
            {
                mFiles.get(index).setFileName(filename);
            }

            rescan();

            return true;
        }

        return false;
    }

    public ArrayList<String> deleteFiles(int files[])
    {
        ArrayList<String> res = new ArrayList<String>();

        for (int e = 0; e < files.length - 1; ++e)
        {
            int max = files[e];
            int maxIndex = e;

            for (int i = e + 1; i < files.length; ++i)
            {
                if (files[i] > max)
                {
                    max = files[i];
                    maxIndex = i;
                }
            }

            int temp        = files[e];
            files[e]        = files[maxIndex];
            files[maxIndex] = temp;
        }

        for (int i = 0; i < files.length; ++i)
        {
            String filename = mFiles.get(files[i]).getFileName();

            if (Utils.deleteFileOrFolder(pathToFile(filename)))
            {
                synchronized (this)
                {
                    mFiles.remove(files[i]);
                }
            }
            else
            {
                res.add(filename);
            }
        }

        if (res.size() < files.length)
        {
            notifyDataSetChanged();
        }

        return res;
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

                newPath = newPath.substring(0, newPath.lastIndexOf('/'));
            }
        } while (true);
    }

    public void setCurrentPath(String newPath) throws FileNotFoundException
    {
        if (TextUtils.isEmpty(newPath))
        {
            newPath = "/";
        }

        if (!(new File(newPath).exists()))
        {
            throw new FileNotFoundException();
        }

        synchronized (this)
        {
            mCurrentPath = newPath;
        }

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
        if (mSelectionMode != enable)
        {
            mSelectionMode = enable;
            mSelection.clear();

            notifyDataSetChanged();
        }
    }

    public ArrayList<Integer> getSelection()
    {
        return mSelection;
    }

    private class CheckedChangedListener implements CompoundButton.OnCheckedChangeListener
    {
        private Integer mPosition;

        public CheckedChangedListener(int position)
        {
            mPosition = Integer.valueOf(position);
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

    private class DbReaderTask extends AsyncTask < Void, Void, Void >
    {
        @Override
        protected Void doInBackground(Void... arg0)
        {
            MainDatabase helper = new MainDatabase(mContext);
            SQLiteDatabase db = helper.getReadableDatabase();

            String path;

            synchronized (FilesAdapter.this)
            {
                path = mCurrentPath;
            }

            Cursor cursor = helper.getFiles(db, path);

            int idIndex            = cursor.getColumnIndexOrThrow(MainDatabase.COLUMN_ID);
            int nameIndex          = cursor.getColumnIndexOrThrow(MainDatabase.COLUMN_NAME);
            int modificationIndex  = cursor.getColumnIndexOrThrow(MainDatabase.COLUMN_MODIFICATION_TIME);
            int reviewedCountIndex = cursor.getColumnIndexOrThrow(MainDatabase.COLUMN_REVIEWED_COUNT);
            int invalidCountIndex  = cursor.getColumnIndexOrThrow(MainDatabase.COLUMN_INVALID_COUNT);
            int noteCountIndex     = cursor.getColumnIndexOrThrow(MainDatabase.COLUMN_NOTE_COUNT);
            int rowCountIndex      = cursor.getColumnIndexOrThrow(MainDatabase.COLUMN_ROW_COUNT);
            int noteIndex          = cursor.getColumnIndexOrThrow(MainDatabase.COLUMN_NOTE);

            cursor.moveToFirst();

            while (!cursor.isAfterLast() && !isCancelled())
            {
                String fileName = cursor.getString(nameIndex);

                FileEntry entry = null;

                synchronized (FilesAdapter.this)
                {
                    int index = indexOf(fileName);

                    if (index >= 0)
                    {
                        entry = mFiles.get(index);
                    }
                }

                if (entry != null)
                {
                    String filePath = pathToFile(entry.getFileName());

                    long modifiedTime = new File(filePath).lastModified();

                    if (cursor.getLong(modificationIndex) == modifiedTime)
                    {
                        entry.updateFromDb(
                                           cursor.getInt(idIndex),
                                           cursor.getInt(reviewedCountIndex),
                                           cursor.getInt(invalidCountIndex),
                                           cursor.getInt(noteCountIndex),
                                           cursor.getInt(rowCountIndex),
                                           cursor.getString(noteIndex)
                                          );
                    }
                }

                cursor.moveToNext();
            }

            synchronized (FilesAdapter.this)
            {
                for (int i = 0; i < mFiles.size() && !isCancelled(); ++i)
                {
                    FileEntry entry = mFiles.get(i);

                    try
                    {
                        if (
                            !entry.isDirectory()
                             &&
                            entry.getDbFileId() <= 0
                            )
                        {
                            String filePath = pathToFile(entry.getFileName());

                            String md5        = Utils.md5ForFile(filePath);
                            long modifiedTime = new File(filePath).lastModified();

                            cursor = helper.getFileByMD5(db, md5);

                            cursor.moveToFirst();

                            while (!cursor.isAfterLast())
                            {
                                if (cursor.getLong(modificationIndex) == modifiedTime)
                                {
                                    String fileName = cursor.getString(nameIndex);

                                    if (entry.getFileName().equals(fileName))
                                    {
                                        entry.updateFromDb(
                                                           cursor.getInt(idIndex),
                                                           cursor.getInt(reviewedCountIndex),
                                                           cursor.getInt(invalidCountIndex),
                                                           cursor.getInt(noteCountIndex),
                                                           cursor.getInt(rowCountIndex),
                                                           cursor.getString(noteIndex)
                                                          );
                                    }
                                }

                                cursor.moveToNext();
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        Log.e(TAG, "Impossible to get file id by MD5 for file: " + entry.getFileName(), e);
                    }
                }
            }



            db.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            notifyDataSetChanged();
            mDbReaderTask = null;
        }
    }
}
