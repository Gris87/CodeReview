package com.griscom.codereview.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.griscom.codereview.CodeReviewApplication;
import com.griscom.codereview.R;
import com.griscom.codereview.dialogs.DeleteDialog;
import com.griscom.codereview.dialogs.NoteDialog;
import com.griscom.codereview.dialogs.OpenBigFileDialog;
import com.griscom.codereview.dialogs.RenameDialog;
import com.griscom.codereview.dialogs.SortDialog;
import com.griscom.codereview.lists.FilesAdapter;
import com.griscom.codereview.other.ApplicationExtras;
import com.griscom.codereview.other.ApplicationPreferences;
import com.griscom.codereview.other.ApplicationSettings;
import com.griscom.codereview.other.FileEntry;
import com.griscom.codereview.other.SortType;
import com.griscom.codereview.util.AppLog;

import junit.framework.Assert;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Activity for displaying files
 */
public class FilesActivity extends AppCompatActivity implements OnItemClickListener, SortDialog.OnFragmentInteractionListener, RenameDialog.OnFragmentInteractionListener, NoteDialog.OnFragmentInteractionListener, DeleteDialog.OnFragmentInteractionListener, OpenBigFileDialog.OnFragmentInteractionListener
{
    @SuppressWarnings("unused")
    private static final String TAG = "FilesActivity";



    private static final int REQUEST_SETTINGS = 1;
    private static final int REQUEST_REVIEW   = 2;



    private static final int TIME_FOR_CLOSE = 1000;



    private static final String SAVED_STATE_SELECTION = "SELECTION";



    private ListView     mFilesListView = null;
    private FilesAdapter mAdapter       = null;
    private ActionBar    mActionBar     = null;
    private ActionMode   mActionMode    = null;
    private Tracker      mTracker       = null;
    private long         mBackPressTime = 0;



    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mFilesListView  = (ListView)findViewById(R.id.filesListView);



        setSupportActionBar(toolbar);



        mAdapter       = new FilesAdapter(this);
        mActionBar     = getSupportActionBar();
        mActionMode    = null;
        mTracker       = ((CodeReviewApplication)getApplication()).getDefaultTracker();
        mBackPressTime = 0;



        mFilesListView.setAdapter(mAdapter);
        mFilesListView.setOnItemClickListener(this);

        setChoiceListener();

        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setTitle(mAdapter.getCurrentPath());



        try
        {
            loadPath();
            loadLastFile();
        }
        catch (FileNotFoundException e)
        {
            // Nothing
        }

        loadSortType();
    }

    /** {@inheritDoc} */
    @Override
    protected void onResume()
    {
        super.onResume();

        if (!mAdapter.rescan())
        {
            savePath();
            updateCurrentPath();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putIntegerArrayList(SAVED_STATE_SELECTION, mAdapter.getSelection());
    }

    /** {@inheritDoc} */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        ArrayList<Integer> selection = savedInstanceState.getIntegerArrayList(SAVED_STATE_SELECTION);
        Assert.assertNotNull(selection);

        if (selection.size() > 0)
        {
            mAdapter.setSelectionMode(true);

            for (int i = 0; i < selection.size(); ++i)
            {
                mAdapter.setSelected(selection.get(i), true);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_files, menu);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_sort:
            {
                SortDialog dialog = SortDialog.newInstance(mAdapter.getSortType());
                dialog.show(getSupportFragmentManager(), "SortDialog");

                return true;
            }

            case R.id.action_settings:
            {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, REQUEST_SETTINGS);

                return true;
            }

            case R.id.action_donate:
            {
                mTracker.send(
                        new HitBuilders.EventBuilder()
                                .setCategory("Action")
                                .setAction("Donate")
                                .build()
                );

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=68VPVB8SNFHJ4"));
                startActivity(intent);

                return true;
            }

            case R.id.action_close:
            {
                mTracker.send(
                        new HitBuilders.EventBuilder()
                                .setCategory("Action")
                                .setAction("Exit")
                                .build()
                );

                finish();

                return true;
            }

            default:
            {
                AppLog.wtf(TAG, "Unknown action ID: " + String.valueOf(item.getItemId()));
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    /** {@inheritDoc} */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_REVIEW:
            {
                switch (resultCode)
                {
                    case ReviewActivity.RESULT_CANCELED:
                    {
                        clearLastFile();
                    }
                    break;

                    case ReviewActivity.RESULT_CLOSE:
                    {
                        finish();
                    }
                    break;

                    default:
                    {
                        AppLog.wtf(TAG, "Unexpected result code: " + String.valueOf(resultCode));
                    }
                    break;
                }
            }
            break;

            case REQUEST_SETTINGS:
            {
                ApplicationSettings.update(this);
            }
            break;

            default:
            {
                AppLog.wtf(TAG, "Unexpected request code: " + String.valueOf(requestCode));
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /** {@inheritDoc} */
    @Override
    public void onBackPressed()
    {
        if (!mAdapter.getCurrentPath().equals("/"))
        {
            mAdapter.goUp();

            savePath();
            updateCurrentPath();
        }
        else
        {
            long curTime = System.currentTimeMillis();

            if (curTime - mBackPressTime < TIME_FOR_CLOSE)
            {
                clearPath();

                super.onBackPressed();
            }
            else
            {
                mBackPressTime = curTime;

                Toast.makeText(this, R.string.files_press_again_to_exit, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (parent == mFilesListView)
        {
            FileEntry file = (FileEntry)mAdapter.getItem(position);

            String fileName = file.getFileName();

            if (file.isDirectory())
            {
                if (fileName.equals(".."))
                {
                    mAdapter.goUp();
                }
                else
                {
                    mAdapter.setCurrentPathBacktrace(mAdapter.pathToFile(fileName));
                }

                savePath();
                updateCurrentPath();
            }
            else
            {
                saveLastFile(fileName);

                try
                {
                    openFile(fileName, file.getDbFileId(), file.getFileNote());
                }
                catch (FileNotFoundException e)
                {
                    mAdapter.setCurrentPathBacktrace(mAdapter.pathToFile("."));

                    savePath();
                    updateCurrentPath();
                }
            }
        }
        else
        {
            AppLog.wtf(TAG, "Unexpected parent: " + String.valueOf(parent));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onSortTypeSelected(int sortType)
    {
        String typeName;

        switch (sortType)
        {
            case SortType.NAME:
            {
                typeName = "name";
            }
            break;

            case SortType.TYPE:
            {
                typeName = "type";
            }
            break;

            case SortType.SIZE:
            {
                typeName = "size";
            }
            break;

            default:
            {
                typeName = "UNKNOWN";
                AppLog.wtf(TAG, "Unknown sort type: " + String.valueOf(sortType));
            }
            break;
        }

        mTracker.send(
                new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Sort")
                        .setLabel("By " + typeName)
                        .build()
        );

        mAdapter.sort(sortType);
        saveSortType();
    }

    /** {@inheritDoc} */
    @Override
    public void onFileRenamed(boolean addNote, int item, String fileName)
    {
        String oldFileName = ((FileEntry)mAdapter.getItem(item)).getFileName();

        if (addNote)
        {
            ArrayList<Integer> items = new ArrayList<>();

            items.add(item);

            if (!oldFileName.equals(fileName))
            {
                mAdapter.assignNote(items, getString(R.string.files_rename_to, fileName));
            }
            else
            {
                mAdapter.assignNote(items, "");
            }
        }
        else
        {
            if (!oldFileName.equals(fileName))
            {
                if (!mAdapter.renameFile(item, fileName))
                {
                    Toast.makeText(this, R.string.files_can_not_rename_file, Toast.LENGTH_SHORT).show();
                }
            }
        }

        hideActionMode();
    }

    /** {@inheritDoc} */
    @Override
    public void onNoteEntered(ArrayList<Integer> items, String note)
    {
        mAdapter.assignNote(items, note);

        hideActionMode();
    }

    /** {@inheritDoc} */
    @Override
    public void onDeleteConfirmed(ArrayList<Integer> items)
    {
        ArrayList<String> keepFolders = new ArrayList<>();
        ArrayList<String> keepFiles   = new ArrayList<>();

        mAdapter.deleteFiles(items, keepFolders, keepFiles);

        if (keepFolders.size() > 0 || keepFiles.size() > 0)
        {
            Resources resources = getResources();

            if (keepFolders.size() == 1 && keepFiles.size() == 0)
            {
                Toast.makeText(FilesActivity.this, resources.getString(R.string.files_can_not_delete_folder, keepFolders.get(0)), Toast.LENGTH_SHORT).show();
            }
            else
            if (keepFolders.size() == 0 && keepFiles.size() == 1)
            {
                Toast.makeText(FilesActivity.this, resources.getString(R.string.files_can_not_delete_file, keepFiles.get(0)), Toast.LENGTH_SHORT).show();
            }
            else
            {
                String folders = keepFolders.size() > 0 ? resources.getQuantityString(R.plurals.files_delete_folders_plurals, keepFolders.size(), keepFolders.size()) : null;
                String files   = keepFiles.size()   > 0 ? resources.getQuantityString(R.plurals.files_delete_files_plurals,   keepFiles.size(),   keepFiles.size())   : null;

                if (keepFolders.size() > 1 && keepFiles.size() == 0)
                {
                    Toast.makeText(FilesActivity.this, resources.getString(R.string.files_can_not_delete_folders_or_files, folders), Toast.LENGTH_SHORT).show();
                }
                else
                if (keepFolders.size() == 0 && keepFiles.size() > 1)
                {
                    Toast.makeText(FilesActivity.this, resources.getString(R.string.files_can_not_delete_folders_or_files, files), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(FilesActivity.this, resources.getString(R.string.files_can_not_delete_folders_and_files, folders, files), Toast.LENGTH_SHORT).show();
                }
            }
        }

        hideActionMode();
    }

    /** {@inheritDoc} */
    @Override
    public void onBigFileOpeningConfirmed(String filePath, int fileId, String fileNote)
    {
        openFileAtPath(filePath, fileId, fileNote);
    }

    /**
     * Sets choice listener on ActionMode
     */
    private void setChoiceListener()
    {
        mFilesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mFilesListView.setMultiChoiceModeListener(new MultiChoiceModeListener()
        {
            /** {@inheritDoc} */
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
            {
                if (position == 0 && checked)
                {
                    FileEntry firstFile = (FileEntry)mAdapter.getItem(0);

                    if (
                        firstFile.isDirectory()
                        &&
                        firstFile.getFileName().equals("..")
                       )
                    {
                        mFilesListView.setItemChecked(0, false);

                        return;
                    }
                }

                mAdapter.setSelected(position, checked);



                ArrayList<Integer> items = mAdapter.getSelection();

                int foldersCount = 0;
                int filesCount   = 0;

                for (int item : items)
                {
                    if (((FileEntry)mAdapter.getItem(item)).isDirectory())
                    {
                        ++foldersCount;
                    }
                    else
                    {
                        ++filesCount;
                    }
                }

                Resources resources = getResources();

                String folders = foldersCount > 0 ? resources.getQuantityString(R.plurals.files_selected_folders_plurals, foldersCount, foldersCount) : null;
                String files   = filesCount   > 0 ? resources.getQuantityString(R.plurals.files_selected_files_plurals,   filesCount,   filesCount)   : null;

                if (foldersCount == 0 && filesCount == 0)
                {
                    mode.setSubtitle(null);
                }
                else
                if (foldersCount > 0 && filesCount == 0)
                {
                    mode.setSubtitle(resources.getQuantityString(R.plurals.files_folders_selected, foldersCount, folders));
                }
                else
                if (foldersCount == 0 && filesCount > 0)
                {
                    mode.setSubtitle(resources.getQuantityString(R.plurals.files_files_selected, filesCount, files));
                }
                else
                {
                    mode.setSubtitle(resources.getString(R.string.files_folders_and_files_selected, folders, files));
                }
            }

            /** {@inheritDoc} */
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu)
            {
                mActionMode = mode;

                mActionMode.setTitle(R.string.files_select_files);
                mActionMode.getMenuInflater().inflate(R.menu.context_menu_files, menu);

                mAdapter.setSelectionMode(true);

                return true;
            }

            /** {@inheritDoc} */
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu)
            {
                menu.findItem(R.id.action_rename).setVisible(mAdapter.getSelection().size() == 1);

                return true;
            }

            /** {@inheritDoc} */
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item)
            {
                ArrayList<Integer> items = mAdapter.getSelection();

                boolean res = true;

                switch (item.getItemId())
                {
                    case R.id.action_select_all:
                    {
                        res = selectAll(items);
                    }
                    break;

                    case R.id.action_note:
                    {
                        res = assignNote(items);
                    }
                    break;

                    case R.id.action_note_to_rename:
                    {
                        res = noteToRename(items);
                    }
                    break;

                    case R.id.action_note_to_delete:
                    {
                        res = noteToDelete(items);
                    }
                    break;

                    case R.id.action_mark_as_reviewed:
                    {
                        res = markAsReviewed(items);
                    }
                    break;

                    case R.id.action_mark_as_invalid:
                    {
                        res = markAsInvalid(items);
                    }
                    break;

                    case R.id.action_rename:
                    {
                        res = rename(items.get(0));
                    }
                    break;

                    case R.id.action_delete:
                    {
                        res = delete(items);
                    }
                    break;

                    default:
                    {
                        AppLog.wtf(TAG, "Unknown action ID: " + String.valueOf(item));
                    }
                    break;
                }

                if (res)
                {
                    hideActionMode();
                }

                return true;
            }

            /** {@inheritDoc} */
            @Override
            public void onDestroyActionMode(ActionMode mode)
            {
                mActionMode = null;

                mAdapter.setSelectionMode(false);
            }
        });
    }

    /**
     * Selects/Deselects all files
     * @param items    selected files
     * @return true, if need to close ActionMode
     */
    private boolean selectAll(ArrayList<Integer> items)
    {
        int startIndex;

        FileEntry firstFile = (FileEntry)mAdapter.getItem(0);

        if (
            firstFile.isDirectory()
            &&
            firstFile.getFileName().equals("..")
           )
        {
            startIndex = 1;
        }
        else
        {
            startIndex = 0;
        }

        if (items.size() != mAdapter.getCount() - startIndex)
        {
            for (int i = startIndex; i < mAdapter.getCount(); ++i)
            {
                if (!mFilesListView.isItemChecked(i))
                {
                    mFilesListView.setItemChecked(i, true);
                }
            }
        }
        else
        {
            for (int i = startIndex + 1; i < mAdapter.getCount(); ++i)
            {
                mFilesListView.setItemChecked(i, false);
            }
        }

        return false;
    }

    /**
     * Assigns note for selected files
     * @param items    selected files
     * @return true, if need to close ActionMode
     */
    private boolean assignNote(ArrayList<Integer> items)
    {
        String note = ((FileEntry)mAdapter.getItem(items.get(0))).getFileNote();

        for (int i = 1; i < items.size(); ++i)
        {
            if (!note.equals(((FileEntry)mAdapter.getItem(items.get(i))).getFileNote()))
            {
                note = "";

                break;
            }
        }

        NoteDialog dialog = NoteDialog.newInstance(items, note);
        dialog.show(getSupportFragmentManager(), "NoteDialog");

        return false;
    }

    /**
     * Assigns note for renaming for selected files
     * @param items    selected files
     * @return true, if need to close ActionMode
     */
    private boolean noteToRename(ArrayList<Integer> items)
    {
        if (items.size() == 1)
        {
            int item = items.get(0);

            RenameDialog dialog = RenameDialog.newInstance(true, item, ((FileEntry)mAdapter.getItem(item)).getFileName());
            dialog.show(getSupportFragmentManager(), "RenameDialog");

            return false;
        }
        else
        {
            mAdapter.assignNote(items, getString(R.string.files_need_to_rename));
        }

        return true;
    }

    /**
     * Assigns note for deleting for selected files
     * @param items    selected files
     * @return true, if need to close ActionMode
     */
    private boolean noteToDelete(ArrayList<Integer> items)
    {
        mAdapter.assignNote(items, getString(R.string.files_need_to_delete));

        return true;
    }

    /**
     * Marks selected files as reviewed
     * @param items    selected files
     * @return true, if need to close ActionMode
     */
    private boolean markAsReviewed(ArrayList<Integer> items)
    {
        mAdapter.markAsFinished(items, true);

        return true;
    }

    /**
     * Marks selected files as invalid
     * @param items    selected files
     * @return true, if need to close ActionMode
     */
    private boolean markAsInvalid(ArrayList<Integer> items)
    {
        mAdapter.markAsFinished(items, false);

        return true;
    }

    /**
     * Rename selected file
     * @param item    selected file
     * @return true, if need to close ActionMode
     */
    private boolean rename(int item)
    {
        RenameDialog dialog = RenameDialog.newInstance(false, item, ((FileEntry)mAdapter.getItem(item)).getFileName());
        dialog.show(getSupportFragmentManager(), "RenameDialog");

        return false;
    }

    /**
     * Deletes selected files
     * @param items    selected files
     * @return true, if need to close ActionMode
     */
    private boolean delete(ArrayList<Integer> items)
    {
        int foldersCount = 0;
        int filesCount   = 0;

        for (int item : items)
        {
            if (((FileEntry)mAdapter.getItem(item)).isDirectory())
            {
                ++foldersCount;
            }
            else
            {
                ++filesCount;
            }
        }

        DeleteDialog dialog = DeleteDialog.newInstance(items, foldersCount, filesCount);
        dialog.show(getSupportFragmentManager(), "DeleteDialog");

        return false;
    }

    /**
     * Hides ActionMode
     */
    private void hideActionMode()
    {
        Assert.assertNotNull(mActionMode);

        mActionMode.finish();
    }

    /**
     * Updates title in action bar and selects previous folder if possible
     */
    private void updateCurrentPath()
    {
        String oldPath = (String)mActionBar.getTitle();
        String newPath = mAdapter.getCurrentPath();

        Assert.assertNotNull(oldPath);

        if (newPath.length() < oldPath.length())
        {
            Assert.assertTrue(oldPath.startsWith(newPath));

            String tail = oldPath.substring(newPath.length());

            if (tail.startsWith("/"))
            {
                Assert.assertTrue(tail.length() > 1);

                tail = tail.substring(1);
            }



            String prevFolder;

            int index = tail.indexOf("/");

            if (index >= 0)
            {
                prevFolder = tail.substring(0, index);
            }
            else
            {
                prevFolder = tail;
            }



            index = mAdapter.indexOf(prevFolder);

            if (index >= 0)
            {
                mFilesListView.setSelection(index);
            }
            else
            {
                mFilesListView.setSelection(0);
            }
        }
        else
        {
            mFilesListView.setSelection(0);
        }

        mActionBar.setTitle(newPath);
    }

    /**
     * Opens specified file in ReviewActivity if allowed
     * @param fileName    file name
     * @param fileId      file ID in database
     * @param fileNote    file note
     * @throws FileNotFoundException if file not found
     */
    private void openFile(String fileName, int fileId, String fileNote) throws FileNotFoundException
    {
        String filePath = mAdapter.pathToFile(fileName);

        File file = new File(filePath);

        if (!file.exists())
        {
            throw new FileNotFoundException();
        }

        if (
            ApplicationSettings.getBigFileSize() == 0
            ||
            file.length() <= ApplicationSettings.getBigFileSize() * 1024
           )
        {
            openFileAtPath(filePath, fileId, fileNote);
        }
        else
        {
            OpenBigFileDialog dialog = OpenBigFileDialog.newInstance(filePath, fileId, fileNote);
            dialog.show(getSupportFragmentManager(), "OpenBigFileDialog");
        }
    }

    /**
     * Opens specified file in ReviewActivity
     * @param filePath    path to file
     * @param fileId      file ID in database
     * @param fileNote    file note
     */
    private void openFileAtPath(String filePath, int fileId, String fileNote)
    {
        Intent intent = new Intent(this, ReviewActivity.class);

        intent.putExtra(ApplicationExtras.FILE_PATH, filePath);
        intent.putExtra(ApplicationExtras.FILE_ID,   fileId);
        intent.putExtra(ApplicationExtras.FILE_NOTE, fileNote);

        startActivityForResult(intent, REQUEST_REVIEW);
    }

    /**
     * Saves last opened path
     */
    private void savePath()
    {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(ApplicationPreferences.LAST_PATH, mAdapter.getCurrentPath());

        editor.apply();
    }

    /**
     * Loads last opened path
     * @throws FileNotFoundException if path not found
     */
    private void loadPath() throws FileNotFoundException
    {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        String path = prefs.getString(ApplicationPreferences.LAST_PATH, "");

        if (!TextUtils.isEmpty(path))
        {
            mAdapter.setCurrentPathBacktrace(path);

            if (!mAdapter.getCurrentPath().equals(path))
            {
                throw new FileNotFoundException();
            }
        }
    }

    /**
     * Clears last opened path
     */
    private void clearPath()
    {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove(ApplicationPreferences.LAST_PATH);

        editor.apply();
    }

    /**
     * Saves last opened file
     * @param fileName    last opened file
     */
    private void saveLastFile(String fileName)
    {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(ApplicationPreferences.LAST_FILE, fileName);

        editor.apply();
    }

    /**
     * Loads last opened file
     * @throws FileNotFoundException if file not found
     */
    private void loadLastFile() throws FileNotFoundException
    {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        String fileName = prefs.getString(ApplicationPreferences.LAST_FILE, "");

        if (!TextUtils.isEmpty(fileName))
        {
            openFile(fileName, 0, "");
        }
    }

    /**
     * Clears last opened file
     */
    private void clearLastFile()
    {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove(ApplicationPreferences.LAST_FILE);

        editor.apply();
    }

    /**
     * Saves sort type
     */
    private void saveSortType()
    {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(ApplicationPreferences.SORT_TYPE, mAdapter.getSortType());

        editor.apply();
    }

    /**
     * Loads sort type
     */
    private void loadSortType()
    {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        int sortType = prefs.getInt(ApplicationPreferences.SORT_TYPE, SortType.NAME);

        if (sortType >= SortType.MIN && sortType <= SortType.MAX && mAdapter.getSortType() != sortType)
        {
            mAdapter.sort(sortType);
        }
    }
}
