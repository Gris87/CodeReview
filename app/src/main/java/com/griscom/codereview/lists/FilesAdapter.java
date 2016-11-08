package com.griscom.codereview.lists;

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
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.griscom.codereview.R;
import com.griscom.codereview.db.MainDatabase;
import com.griscom.codereview.other.ApplicationSettings;
import com.griscom.codereview.other.ColorCache;
import com.griscom.codereview.other.FileEntry;
import com.griscom.codereview.other.SelectionType;
import com.griscom.codereview.other.SortType;
import com.griscom.codereview.util.Utils;

import junit.framework.Assert;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Adapter that used in FilesActivity
 */
public class FilesAdapter extends BaseAdapter
{
    @SuppressWarnings("unused")
    private static final String TAG = "FilesAdapter";



    public static final int SELECTION_MODE_DISABLED = 0;
    public static final int SELECTION_MODE_ENABLED  = 1;



    public static final int ITEM_DESELECTED = 0;
    public static final int ITEM_SELECTED   = 1;



    public static final int MARK_TYPE_REVIEWED = 0;
    public static final int MARK_TYPE_INVALID  = 1;



    private Context              mContext       = null;
    private String               mCurrentPath   = null;
    private ArrayList<FileEntry> mFiles         = null;
    private int                  mSortType      = 0;
    private int                  mSelectionMode = 0;
    private ArrayList<Integer>   mSelection     = null;
    private DbReaderTask         mDbReaderTask  = null;



    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "FilesAdapter{" +
                "mContext="         + mContext       +
                ", mCurrentPath='"  + mCurrentPath   + '\'' +
                ", mFiles="         + mFiles         +
                ", mSortType="      + mSortType      +
                ", mSelectionMode=" + mSelectionMode +
                ", mSelection="     + mSelection     +
                ", mDbReaderTask="  + mDbReaderTask  +
                '}';
    }

    /**
     * Creates instance of FilesAdapter
     * @param context    context
     */
    @SuppressWarnings("ImplicitCallToSuper")
    private FilesAdapter(Context context)
    {
        mContext       = context;
        mCurrentPath   = Environment.getExternalStorageDirectory().getPath();
        mFiles         = new ArrayList<>(0);
        mSortType      = SortType.NAME;
        mSelectionMode = SELECTION_MODE_DISABLED;
        mSelection     = new ArrayList<>(0);
        mDbReaderTask  = null;

        rescan();
    }

    /**
     * Creates instance of FilesAdapter
     * @param context    context
     */
    public static FilesAdapter newInstance(Context context)
    {
        return new FilesAdapter(context);
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
        View resView = inflater.inflate(R.layout.list_item_files, parent, false);



        ViewHolder holder = new ViewHolder();

        holder.setCheckBox(      (CheckBox) resView.findViewById(R.id.checkbox));
        holder.setExtensionImage((ImageView)resView.findViewById(R.id.extensionImageView));
        holder.setFileNote(      (TextView) resView.findViewById(R.id.fileNoteTextView));
        holder.setFileName(      (TextView) resView.findViewById(R.id.fileNameTextView));
        holder.setFileSize(      (TextView) resView.findViewById(R.id.fileSizeTextView));

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
                int clearPercent    = 100 - (reviewedCount + invalidCount + noteCount) * 100 / rowCount;

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
                    if (reviewedCount + invalidCount + noteCount < rowCount)
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
                        reviewedPercent -= totalPercent - 100;
                    }
                    if (
                        invalidPercent > reviewedPercent
                        &&
                        invalidPercent > notePercent
                        &&
                        invalidPercent > clearPercent
                       )
                    {
                        invalidPercent  -= totalPercent - 100;
                    }

                    if (
                        notePercent > reviewedPercent
                        &&
                        notePercent > invalidPercent
                        &&
                        notePercent > clearPercent
                       )
                    {
                        notePercent     -= totalPercent - 100;
                    }
                    else
                    {
                        clearPercent    -= totalPercent - 100;
                    }
                }

                // ----------------------------------------------

                Bitmap bitmap = Bitmap.createBitmap(100, 1, Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);

                int curPercent = 0;

                if (reviewedPercent > 0)
                {
                    Paint paint = new Paint();

                    paint.setColor(ColorCache.get(SelectionType.REVIEWED));
                    paint.setAlpha(220);

                    canvas.drawLine(curPercent, 0, curPercent + reviewedPercent, 0, paint);
                    curPercent += reviewedPercent;
                }

                if (invalidPercent > 0)
                {
                    Paint paint = new Paint();

                    paint.setColor(ColorCache.get(SelectionType.INVALID));
                    paint.setAlpha(220);

                    canvas.drawLine(curPercent, 0, curPercent + invalidPercent, 0, paint);
                    curPercent += invalidPercent;
                }

                if (notePercent > 0)
                {
                    Paint paint = new Paint();

                    paint.setColor(ColorCache.get(SelectionType.NOTE));
                    paint.setAlpha(220);

                    canvas.drawLine(curPercent, 0, curPercent + notePercent, 0, paint);
                    curPercent += notePercent;
                }

                if (clearPercent > 0)
                {
                    Paint paint = new Paint();

                    paint.setColor(ColorCache.get(SelectionType.CLEAR));
                    paint.setAlpha(220);

                    canvas.drawLine(curPercent, 0, curPercent + clearPercent, 0, paint);
                }

                //noinspection deprecation
                view.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
            }
            else
            {
                //noinspection deprecation
                view.setBackgroundDrawable(null);
            }
        }
        else
        {
            //noinspection deprecation
            view.setBackgroundDrawable(null);
        }



        if (
            mSelectionMode == SELECTION_MODE_ENABLED
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
            holder.getCheckBox().setVisibility(View.VISIBLE);
            holder.getCheckBox().setChecked(mSelection.contains(position));
        }
        else
        {
            holder.getCheckBox().setVisibility(View.GONE);
        }



        String note = file.getFileNote();

        if (!TextUtils.isEmpty(note))
        {
            holder.getFileNote().setVisibility(View.VISIBLE);
            holder.getFileNote().setText(note);
        }
        else
        {
            holder.getFileNote().setVisibility(View.GONE);
        }



        holder.getExtensionImage().setImageResource(file.getImageId());
        holder.getFileName().setText(file.getFileName());

        if (!file.isDirectory())
        {
            holder.getFileSize().setVisibility(View.VISIBLE);
            holder.getFileSize().setText(Utils.bytesToString(file.getSize()));
        }
        else
        {
            holder.getFileSize().setVisibility(View.GONE);
        }
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
     * Go to the parent folder
     */
    public void goUp()
    {
        Assert.assertTrue("Current path is root", !mCurrentPath.equals("/"));

        setCurrentPathBacktrace(mCurrentPath.substring(0, mCurrentPath.lastIndexOf('/')));
    }

    /**
     * Rescans current folder
     * @return true, if successful
     */
    public final boolean rescan()
    {
        if (!new File(mCurrentPath).exists())
        {
            setCurrentPathBacktrace(pathToFile("."));

            return false;
        }



        mFiles.clear();

        if (!mCurrentPath.equals("/"))
        {
            mFiles.add(FileEntry.createParentFolder());
        }

        File folder = new File(mCurrentPath);
        File[] files = folder.listFiles();

        if (files != null)
        {
            WildcardFileFilter filter = new WildcardFileFilter(ApplicationSettings.getIgnoreFiles());

            for (File file : files)
            {
                if (!filter.accept(file))
                {
                    mFiles.add(FileEntry.newInstance(file));
                }
            }
        }

        sort();



        if (mDbReaderTask != null)
        {
            mDbReaderTask.cancel(true);
        }

        mDbReaderTask = DbReaderTask.newInstance(this);
        mDbReaderTask.execute();



        return true;
    }

    /**
     * Sorts files list with selected sort type
     */
    private void sort()
    {
        sort(SortType.NONE);
    }

    /**
     * Sorts files list with specified sort type
     * @param sortType    sort type
     */
    public void sort(int sortType)
    {
        if (sortType != SortType.NONE)
        {
            mSortType = sortType;
        }

        Collections.sort(mFiles, new Comparator<FileEntry>()
        {
            /** {@inheritDoc} */
            @Override
            public int compare(FileEntry file1, FileEntry file2)
            {
                return file1.compare(file2, mSortType);
            }
        });

        notifyDataSetChanged();
    }

    /**
     * Gets absolute path to specified file name
     * @param fileName    file name
     * @return absolute path to specified file name
     */
    public String pathToFile(String fileName)
    {
        if (!mCurrentPath.isEmpty() && mCurrentPath.charAt(mCurrentPath.length() - 1) == '/')
        {
            return mCurrentPath + fileName;
        }
        else
        {
            return mCurrentPath + '/' + fileName;
        }
    }

    /**
     * Search index of specified file name in the file list
     * @param fileName    file name
     * @return index of specified file name in the file list or -1 if not found
     */
    public int indexOf(String fileName)
    {
        for (int i = 0; i < mFiles.size(); ++i)
        {
            if (mFiles.get(i).getFileName().equals(fileName))
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Assigns note for files in specified indices
     * @param items    file indices
     * @param note     note
     */
    public void assignNote(ArrayList<Integer> items, String note)
    {
        for (int item : items)
        {
            FileEntry file = mFiles.get(item);

            file.setFileNote(mContext, pathToFile(file.getFileName()), note);
        }

        notifyDataSetChanged();
    }

    /**
     * Marks files in specified indices as finished
     * @param items    file indices
     * @param markType mark type
     */
    public void markAsFinished(ArrayList<Integer> items, int markType)
    {
        for (int item : items)
        {
            FileEntry file = mFiles.get(item);

            if (markType == MARK_TYPE_REVIEWED)
            {
                file.setFileStats(mContext, pathToFile(file.getFileName()), 1, 0, 0, 1);
            }
            else
            {
                file.setFileStats(mContext, pathToFile(file.getFileName()), 0, 1, 0, 1);
            }
        }

        notifyDataSetChanged();
    }

    /**
     * Renames file at specified index in the file list
     * @param item        index of file
     * @param fileName    new file name
     * @return true, if successful
     */
    public boolean renameFile(int item, String fileName)
    {
        if (
            fileName.contains("/")
            ||
            fileName.contains("\\")
           )
        {
            return false;
        }

        if (new File(pathToFile(mFiles.get(item).getFileName())).renameTo(new File(pathToFile(fileName))))
        {
            FileEntry file = mFiles.get(item);

            file.setFileName(fileName);

            if (file.getDbFileId() > 0)
            {
                MainDatabase helper = MainDatabase.newInstance(mContext);
                SQLiteDatabase db = helper.getWritableDatabase();

                MainDatabase.updateFilePath(db, file.getDbFileId(), pathToFile(fileName));

                db.close();
            }

            sort();

            return true;
        }

        return false;
    }

    /**
     * Deletes files at specified indices
     * @param items          file indices
     * @param keepFolders    list of folders that were not deleted
     * @param keepFiles      list of files that were not deleted
     */
    public void deleteFiles(ArrayList<Integer> items, ArrayList<String> keepFolders, ArrayList<String> keepFiles)
    {
        Collections.sort(items);

        for (int i = items.size() - 1; i >= 0; --i)
        {
            int item = items.get(i);
            FileEntry file = mFiles.get(item);

            String fileName = file.getFileName();

            if (Utils.deleteFileOrFolder(pathToFile(fileName)))
            {
                mFiles.remove(item);
            }
            else
            {
                if (file.isDirectory())
                {
                    keepFolders.add(fileName);
                }
                else
                {
                    keepFiles.add(fileName);
                }
            }
        }

        if (keepFolders.size() + keepFiles.size() < items.size())
        {
            notifyDataSetChanged();
        }
    }

    /**
     * Trying to set current path to the specified path.
     * If it's impossible to change current path then it will move upper recursively
     * @param path    path
     */
    public void setCurrentPathBacktrace(String path)
    {
        String newPath = path;

        do
        {
            try
            {
                setCurrentPath(newPath);

                return;
            }
            catch (FileNotFoundException ignored)
            {
                Assert.assertTrue("Impossible to get parent folder", !TextUtils.isEmpty(newPath) && !newPath.equals("/"));

                newPath = newPath.substring(0, newPath.lastIndexOf('/'));
            }
        } while (true);
    }

    /**
     * Sets current path to the specified path
     * @param path    path
     * @throws FileNotFoundException if specified path is not exist
     */
    private void setCurrentPath(String path) throws FileNotFoundException
    {
        String newPath = path;

        if (TextUtils.isEmpty(newPath))
        {
            newPath = "/";
        }

        if (!new File(newPath).exists())
        {
            throw new FileNotFoundException("File \"" + newPath + "\" not found");
        }

        mCurrentPath = newPath;

        rescan();
    }

    /**
     * Gets current path
     * @return current path
     */
    public String getCurrentPath()
    {
        return mCurrentPath;
    }

    /**
     * Gets selected sort type
     * @return selected sort type
     */
    public int getSortType()
    {
        return mSortType;
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
        private CheckBox  mCheckBox       = null;
        private ImageView mExtensionImage = null;
        private TextView  mFileNote       = null;
        private TextView  mFileName       = null;
        private TextView  mFileSize       = null;



        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "ViewHolder{" +
                    "mCheckBox="         + mCheckBox       +
                    ", mExtensionImage=" + mExtensionImage +
                    ", mFileNote="       + mFileNote       +
                    ", mFileName="       + mFileName       +
                    ", mFileSize="       + mFileSize       +
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
         * Gets extension image
         * @return extension image
         */
        ImageView getExtensionImage()
        {
            return mExtensionImage;
        }

        /**
         * Sets extension image
         * @param extensionImage    extension image
         */
        void setExtensionImage(ImageView extensionImage)
        {
            mExtensionImage = extensionImage;
        }

        /**
         * Gets file note
         * @return file note
         */
        TextView getFileNote()
        {
            return mFileNote;
        }

        /**
         * Sets file note
         * @param fileNote    file note
         */
        void setFileNote(TextView fileNote)
        {
            mFileNote = fileNote;
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

        /**
         * Gets file size
         * @return file size
         */
        TextView getFileSize()
        {
            return mFileSize;
        }

        /**
         * Sets file size
         * @param fileSize    file size
         */
        void setFileSize(TextView fileSize)
        {
            mFileSize = fileSize;
        }
    }



    /**
     * DB reader task
     */
    private static class DbReaderTask extends AsyncTask<Void, Void, Boolean>
    {
        @SuppressWarnings("FieldNotUsedInToString")
        private FilesAdapter         mAdapter     = null;
        private Context              mContext     = null;
        private String               mStoredPath  = null;
        private ArrayList<FileEntry> mStoredFiles = null;



        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "DbReaderTask{" +
                    "mContext="       + mContext     +
                    ", mStoredPath='" + mStoredPath  + '\'' +
                    ", mStoredFiles=" + mStoredFiles +
                    '}';
        }

        /**
         * Creates DbReaderTask instance for provided adapter
         * @param adapter    adapter
         */
        @SuppressWarnings({"ImplicitCallToSuper", "AccessingNonPublicFieldOfAnotherObject"})
        private DbReaderTask(FilesAdapter adapter)
        {
            mAdapter     = adapter;
            mContext     = mAdapter.mContext;
            mStoredPath  = mAdapter.mCurrentPath;
            mStoredFiles = new ArrayList<>(mAdapter.mFiles);
        }

        /**
         * Creates DbReaderTask instance for provided adapter
         * @param adapter    adapter
         */
        public static DbReaderTask newInstance(FilesAdapter adapter)
        {
            return new DbReaderTask(adapter);
        }

        /** {@inheritDoc} */
        @Override
        protected Boolean doInBackground(Void... params)
        {
            MainDatabase helper = MainDatabase.newInstance(mContext);
            SQLiteDatabase db = helper.getReadableDatabase();



            Cursor cursor = MainDatabase.getFiles(db, mStoredPath);

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

                for (int i = 0; i < mStoredFiles.size() && !isCancelled(); ++i)
                {
                    FileEntry fileEntry = mStoredFiles.get(i);

                    if (fileEntry.getFileName().equals(fileName))
                    {
                        entry = fileEntry;

                        break;
                    }
                }

                if (entry != null)
                {
                    String filePath;

                    if (!mStoredPath.isEmpty() && mStoredPath.charAt(mStoredPath.length() - 1) == '/')
                    {
                        filePath = mStoredPath + fileName;
                    }
                    else
                    {
                        filePath = mStoredPath + '/' + fileName;
                    }

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

            cursor.close();



            for (int i = 0; i < mStoredFiles.size() && !isCancelled(); ++i)
            {
                FileEntry entry = mStoredFiles.get(i);

                if (
                    !entry.isDirectory()
                    &&
                    entry.getSize() > 0
                    &&
                    (
                     ApplicationSettings.getBigFileSize() == 0
                     ||
                     entry.getSize() <= ApplicationSettings.getBigFileSize() << 10 // *  1024
                    )
                    &&
                    entry.getDbFileId() <= 0
                   )
                {
                    String filePath;

                    if (!mStoredPath.isEmpty() && mStoredPath.charAt(mStoredPath.length() - 1) == '/')
                    {
                        filePath = mStoredPath + entry.getFileName();
                    }
                    else
                    {
                        filePath = mStoredPath + '/' + entry.getFileName();
                    }

                    String md5 = Utils.md5ForFile(filePath);

                    if (!TextUtils.isEmpty(md5))
                    {
                        cursor = MainDatabase.getFileByMD5(db, md5);

                        cursor.moveToFirst();

                        if (!cursor.isAfterLast())
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

                        cursor.close();
                    }
                }
            }



            db.close();

            return !isCancelled();
        }

        /** {@inheritDoc} */
        @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
        @Override
        protected void onPostExecute(Boolean result)
        {
            if (result)
            {
                mAdapter.notifyDataSetChanged();

                mAdapter.mDbReaderTask = null;
                mAdapter = null;
            }
        }
    }
}
