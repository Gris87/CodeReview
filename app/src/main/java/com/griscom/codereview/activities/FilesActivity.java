package com.griscom.codereview.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.griscom.codereview.BuildConfig;
import com.griscom.codereview.CodeReviewApplication;
import com.griscom.codereview.R;
import com.griscom.codereview.lists.FilesAdapter;
import com.griscom.codereview.other.ApplicationExtras;
import com.griscom.codereview.other.ApplicationPreferences;
import com.griscom.codereview.other.ColorCache;
import com.griscom.codereview.other.FileEntry;
import com.griscom.codereview.other.SortType;

import junit.framework.Assert;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Activity for displaying files
 */
public class FilesActivity extends AppCompatActivity implements OnItemClickListener
{
    @SuppressWarnings("unused")
    private static final String TAG = "FilesActivity";



    private static final String FILE_NAMES_SHARED_PREFERENCES = "FileNames";
    private static final String FILE_NOTES_SHARED_PREFERENCES = "FileNotes";



    private static final int REQUEST_SETTINGS = 1;
    private static final int REQUEST_REVIEW   = 2;



    private static final int TIME_FOR_CLOSE = 1000;



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



        ColorCache.update(this);



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

        if (mActionBar != null)
        {
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setTitle(mAdapter.getCurrentPath());
        }



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

        String name = "FilesActivity";

        Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName(name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        if (!mAdapter.rescan())
        {
            savePath();
            updateCurrentPath();
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
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.action_sort)
                        .setSingleChoiceItems(R.array.sort_types,
                                mAdapter.getSortType() - 1,
                                new DialogInterface.OnClickListener()
                                {
                                    /**
                                     * Handler for click event
                                     *
                                     * @param dialog    Dialog
                                     * @param which     Selected option
                                     */
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        int selectedType = which + 1;

                                        mTracker.send(
                                                new HitBuilders.EventBuilder()
                                                        .setCategory("Action")
                                                        .setAction("Sort")
                                                        .setLabel("By " + String.valueOf(selectedType))
                                                        .build()
                                        );

                                        mAdapter.sort(selectedType);
                                        saveSortType();

                                        dialog.dismiss();
                                    }
                                }).create();

                dialog.show();

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
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=68VPVB8SNFHJ4"));
                startActivity(intent);

                mTracker.send(
                              new HitBuilders.EventBuilder()
                                                            .setCategory("Action")
                                                            .setAction("Donate")
                                                            .build()
                             );

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
                        saveLastFile("");
                    }
                    break;
                    case ReviewActivity.RESULT_CLOSE:
                    {
                        finish();
                    }
                    break;
                }
            }
            break;

            case REQUEST_SETTINGS:
            {
                ColorCache.update(this);
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

                Toast.makeText(this, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (parent == mFilesListView)
        {
            FileEntry file = (FileEntry)mFilesListView.getItemAtPosition(position);

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
                    openFile(fileName, file.getDbFileId());
                }
                catch (FileNotFoundException e)
                {
                    mAdapter.setCurrentPathBacktrace(mAdapter.pathToFile("."));

                    savePath();
                    updateCurrentPath();
                }
            }
        }
    }

    /**
     * Sets choice listener on ActionMode
     */
    private void setChoiceListener()
    {
        mFilesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mFilesListView.setMultiChoiceModeListener(new MultiChoiceModeListener()
        {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
            {
                if (position == 0 && checked)
                {
                    FileEntry file = (FileEntry)mAdapter.getItem(position);

                    if (
                            file.isDirectory()
                                    &&
                                    file.getFileName().equals("..")
                            )
                    {
                        mFilesListView.setItemChecked(position, false);

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

                String folders = foldersCount > 0 ? resources.getQuantityString(R.plurals.selected_folders_plurals, foldersCount, foldersCount) : null;
                String files   = filesCount   > 0 ? resources.getQuantityString(R.plurals.selected_files_plurals,   filesCount,   filesCount)   : null;

                if (foldersCount == 0 && filesCount == 0)
                {
                    mode.setSubtitle(null);
                }
                else
                if (foldersCount > 0 && filesCount == 0)
                {
                    mode.setSubtitle(resources.getQuantityString(R.plurals.folders_selected, foldersCount, folders));
                }
                else
                if (foldersCount == 0 && filesCount > 0)
                {
                    mode.setSubtitle(resources.getQuantityString(R.plurals.files_selected, filesCount, files));
                }
                else
                {
                    mode.setSubtitle(resources.getString(R.string.folders_and_files_selected, folders, files));
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu)
            {
                mActionMode = mode;

                mActionMode.setTitle(R.string.select_files);
                mActionMode.getMenuInflater().inflate(R.menu.context_menu_files, menu);

                mAdapter.setSelectionMode(true);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu)
            {
                menu.findItem(R.id.action_rename).setVisible(mAdapter.getSelection().size() == 1);

                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item)
            {
                ArrayList<Integer> tempList = mAdapter.getSelection();
                int items[] = new int[tempList.size()];

                for (int i = 0; i < tempList.size(); ++i)
                {
                    items[i] = tempList.get(i);
                }

                boolean res = true;

                switch (item.getItemId())
                {
                    case R.id.action_mark_to_rename:
                    {
                        res = markToRename(items);
                    }
                    break;
                    case R.id.action_mark_to_delete:
                    {
                        res = markToDelete(items);
                    }
                    break;
                    case R.id.action_note:
                    {
                        res = assignNote(items);
                    }
                    break;
                    case R.id.action_rename:
                    {
                        res = rename(items[0]);
                    }
                    break;
                    case R.id.action_delete:
                    {
                        res = delete(items);
                    }
                    break;
                    default:
                    {
                        Log.e(TAG, "Unknown action: " + String.valueOf(item));
                    }
                    break;
                }

                if (res)
                {
                    mActionMode.finish();
                }

                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode)
            {
                mActionMode = null;
                mAdapter.setSelectionMode(false);
            }
        });
    }

    private boolean markToRename(final int items[])
    {
        if (items.length == 1)
        {
            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.dialog_input, null);

            final EditText editText     = (EditText)    view.findViewById(R.id.inputEditText);
            ImageButton    chooseButton = (ImageButton) view.findViewById(R.id.chooseButton);

            editText.setText(((FileEntry)mAdapter.getItem(items[0])).getFileName());

            chooseButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    ArrayList<CharSequence> filenames = loadLastFileNames();

                    if (filenames.size() > 0)
                    {
                        final CharSequence items[] = new CharSequence[filenames.size()];
                        String currentFilename = editText.getText().toString();
                        int index = -1;

                        for (int i = 0; i < filenames.size(); ++i)
                        {
                            String oneFilename = (String)filenames.get(i);

                            if (index < 0 && oneFilename.equals(currentFilename))
                            {
                                index = i;
                            }

                            items[i] = oneFilename;
                        }

                        if (index < 0)
                        {
                            index = 0;
                        }

                        AlertDialog chooseDialog = new AlertDialog.Builder(FilesActivity.this)
                                .setSingleChoiceItems(items, index, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int index)
                                    {
                                        editText.setText(items[index]);
                                        dialog.dismiss();
                                    }
                                })
                                .create();

                        chooseDialog.show();
                    }
                    else
                    {
                        Toast.makeText(FilesActivity.this, R.string.no_last_names, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_rename_title)
                    .setMessage(R.string.dialog_rename_message)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    String filename = editText.getText().toString();

                                    if (!filename.equals(""))
                                    {
                                        ArrayList<CharSequence> filenames = loadLastFileNames();

                                        filenames.remove(filename);
                                        filenames.add(0, filename);
                                        saveLastFileNames(filenames);

                                        // ----------------------------------

                                        mAdapter.assignNote(items, getString(R.string.rename_to, filename));
                                        hideActionMode();

                                        dialog.dismiss();
                                    }
                                    else
                                    {
                                        Toast.makeText(FilesActivity.this, R.string.empty_name, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    dialog.dismiss();
                                }
                            }).create();

            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            dialog.show();

            return false;
        }
        else
        {
            mAdapter.assignNote(items, getString(R.string.need_to_rename));
        }

        return true;
    }

    private boolean markToDelete(final int items[])
    {
        mAdapter.assignNote(items, getString(R.string.need_to_delete));

        return true;
    }

    private boolean assignNote(final int items[])
    {
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.dialog_input, null);

        final EditText editText     = (EditText)    view.findViewById(R.id.inputEditText);
        ImageButton    chooseButton = (ImageButton) view.findViewById(R.id.chooseButton);

        if (items.length == 1)
        {
            editText.setText(((FileEntry)mAdapter.getItem(items[0])).getFileNote());
        }

        chooseButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ArrayList<CharSequence> filenotes = loadLastFileNotes();

                if (filenotes.size() > 0)
                {
                    final CharSequence items[] = new CharSequence[filenotes.size()];
                    String currentFilenote = editText.getText().toString();
                    int index = -1;

                    for (int i = 0; i < filenotes.size(); ++i)
                    {
                        String oneFilenote = (String)filenotes.get(i);

                        if (index < 0 && oneFilenote.equals(currentFilenote))
                        {
                            index = i;
                        }

                        items[i] = oneFilenote;
                    }

                    if (index < 0)
                    {
                        index = 0;
                    }

                    AlertDialog chooseDialog = new AlertDialog.Builder(FilesActivity.this)
                            .setSingleChoiceItems(items, index, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int index)
                                {
                                    editText.setText(items[index]);
                                    dialog.dismiss();
                                }
                            })
                            .create();

                    chooseDialog.show();
                }
                else
                {
                    Toast.makeText(FilesActivity.this, R.string.no_last_notes, Toast.LENGTH_SHORT).show();
                }
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_input_note_title)
                .setMessage(R.string.dialog_input_note_message)
                .setView(view)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                String filenote = editText.getText().toString();

                                if (!filenote.equals(""))
                                {
                                    ArrayList<CharSequence> filenotes = loadLastFileNotes();

                                    filenotes.remove(filenote);
                                    filenotes.add(0, filenote);
                                    saveLastFileNotes(filenotes);
                                }

                                mAdapter.assignNote(items, filenote);
                                hideActionMode();

                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                dialog.dismiss();
                            }
                        }).create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();

        return false;
    }

    private boolean rename(final int item)
    {
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.dialog_input, null);

        final EditText editText     = (EditText)    view.findViewById(R.id.inputEditText);
        ImageButton    chooseButton = (ImageButton) view.findViewById(R.id.chooseButton);

        editText.setText(((FileEntry)mAdapter.getItem(item)).getFileName());

        chooseButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ArrayList<CharSequence> filenames = loadLastFileNames();

                if (filenames.size() > 0)
                {
                    final CharSequence items[] = new CharSequence[filenames.size()];
                    String currentFilename = editText.getText().toString();
                    int index = -1;

                    for (int i = 0; i < filenames.size(); ++i)
                    {
                        String oneFilename = (String)filenames.get(i);

                        if (index < 0 && oneFilename.equals(currentFilename))
                        {
                            index = i;
                        }

                        items[i] = oneFilename;
                    }

                    if (index < 0)
                    {
                        index = 0;
                    }

                    AlertDialog chooseDialog = new AlertDialog.Builder(FilesActivity.this)
                            .setSingleChoiceItems(items, index, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int index)
                                {
                                    editText.setText(items[index]);
                                    dialog.dismiss();
                                }
                            })
                            .create();

                    chooseDialog.show();
                }
                else
                {
                    Toast.makeText(FilesActivity.this, R.string.no_last_names, Toast.LENGTH_SHORT).show();
                }
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_rename_title)
                .setMessage(R.string.dialog_rename_message)
                .setView(view)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                String filename = editText.getText().toString();

                                if (!filename.equals(""))
                                {
                                    ArrayList<CharSequence> filenames = loadLastFileNames();

                                    filenames.remove(filename);
                                    filenames.add(0, filename);
                                    saveLastFileNames(filenames);

                                    // ----------------------------------

                                    if (!mAdapter.renameFile(item, filename))
                                    {
                                        Toast.makeText(FilesActivity.this, R.string.can_not_rename_file, Toast.LENGTH_SHORT).show();
                                    }

                                    hideActionMode();

                                    dialog.dismiss();
                                }
                                else
                                {
                                    Toast.makeText(FilesActivity.this, R.string.empty_name, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                dialog.dismiss();
                            }
                        }).create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();

        return false;
    }

    private boolean delete(final int items[])
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

        final Resources resources = getResources();

        String folders = foldersCount > 0 ? resources.getQuantityString(R.plurals.delete_folders_plurals, foldersCount, foldersCount) : null;
        String files   = filesCount   > 0 ? resources.getQuantityString(R.plurals.delete_files_plurals,   filesCount,   filesCount)   : null;

        String message;

        if (foldersCount > 0 && filesCount == 0)
        {
            message = resources.getString(R.string.dialog_delete_folders_or_files_message, folders);
        }
        else
        if (foldersCount == 0 && filesCount > 0)
        {
            message = resources.getString(R.string.dialog_delete_folders_or_files_message, files);
        }
        else
        {
            message = resources.getString(R.string.dialog_delete_folders_and_files_message, folders, files);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int index)
                    {
                        ArrayList<String> keepFolders = new ArrayList<>();
                        ArrayList<String> keepFiles   = new ArrayList<>();

                        mAdapter.deleteFiles(items, keepFolders, keepFiles);

                        if (keepFolders.size() > 0 || keepFiles.size() > 0)
                        {
                            if (keepFolders.size() == 1 && keepFiles.size() == 0)
                            {
                                Toast.makeText(FilesActivity.this, resources.getString(R.string.can_not_delete_folder, keepFolders.get(0)), Toast.LENGTH_SHORT).show();
                            }
                            else
                            if (keepFolders.size() == 0 && keepFiles.size() == 1)
                            {
                                Toast.makeText(FilesActivity.this, resources.getString(R.string.can_not_delete_file, keepFiles.get(0)), Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String folders = keepFolders.size() > 0 ? resources.getQuantityString(R.plurals.delete_folders_plurals, keepFolders.size(), keepFolders.size()) : null;
                                String files   = keepFiles.size()   > 0 ? resources.getQuantityString(R.plurals.delete_files_plurals,   keepFiles.size(),   keepFiles.size())   : null;

                                if (keepFolders.size() > 1 && keepFiles.size() == 0)
                                {
                                    Toast.makeText(FilesActivity.this, resources.getString(R.string.can_not_delete_folders_or_files, folders), Toast.LENGTH_SHORT).show();
                                }
                                else
                                if (keepFolders.size() == 0 && keepFiles.size() > 1)
                                {
                                    Toast.makeText(FilesActivity.this, resources.getString(R.string.can_not_delete_folders_or_files, files), Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(FilesActivity.this, resources.getString(R.string.can_not_delete_folders_and_files, folders, files), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        hideActionMode();

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int index)
                    {
                        dialog.dismiss();
                    }
                })
                .create();

        dialog.show();

        return false;
    }

    private void hideActionMode()
    {
        if (mActionMode != null)
        {
            mActionMode.finish();
        }
    }

    private void updateCurrentPath()
    {
        String oldPath = (String)mActionBar.getTitle();
        String newPath = mAdapter.getCurrentPath();

        if (newPath.length() < oldPath.length())
        {
            if (BuildConfig.DEBUG)
            {
                Assert.assertTrue(oldPath.startsWith(newPath));
            }

            String tail = oldPath.substring(newPath.length());

            if (tail.startsWith("/"))
            {
                if (BuildConfig.DEBUG)
                {
                    Assert.assertTrue(tail.length() > 1);
                }

                tail = tail.substring(1);
            }



            String prevFolder;

            int index = tail.lastIndexOf("/");

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
     * Opens specified file in ReviewActivity
     * @param fileName    path to file
     * @param fileId      file ID in database
     * @throws FileNotFoundException if file not found
     */
    private void openFile(String fileName, int fileId) throws FileNotFoundException
    {
        String filePath = mAdapter.pathToFile(fileName);

        if (!(new File(filePath).exists()))
        {
            throw new FileNotFoundException();
        }

        Intent intent = new Intent(this, ReviewActivity.class);

        intent.putExtra(ApplicationExtras.FILE_NAME, filePath);
        intent.putExtra(ApplicationExtras.FILE_ID,   fileId);

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

        editor.putString(ApplicationPreferences.LAST_PATH, "");

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
            openFile(fileName, 0);
        }
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

    public void saveLastFileNames(ArrayList<CharSequence> fileNames)
    {
        SharedPreferences prefs = getSharedPreferences(FILE_NAMES_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(ApplicationPreferences.LAST_FILENAMES, fileNames.size());

        for (int i = 0; i < fileNames.size(); ++i)
        {
            editor.putString(ApplicationPreferences.ONE_FILENAME + "_" + String.valueOf(i + 1), fileNames.get(i).toString());
        }

        editor.apply();
    }

    public ArrayList<CharSequence> loadLastFileNames()
    {
        SharedPreferences prefs = getSharedPreferences(FILE_NAMES_SHARED_PREFERENCES, Context.MODE_PRIVATE);

        int fileNameCount = prefs.getInt(ApplicationPreferences.LAST_FILENAMES, 0);

        ArrayList<CharSequence> res = new ArrayList<CharSequence>();

        for (int i = 0; i < fileNameCount; ++i)
        {
            String fileName = prefs.getString(ApplicationPreferences.ONE_FILENAME + "_" + String.valueOf(i + 1),"");

            if (
                    !TextUtils.isEmpty(fileName)
                            &&
                            !res.contains(fileName)
                    )
            {
                res.add(fileName);
            }
        }

        return res;
    }

    public void saveLastFileNotes(ArrayList<CharSequence> filenotes)
    {
        SharedPreferences prefs = getSharedPreferences(FILE_NOTES_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(ApplicationPreferences.LAST_FILENOTES, filenotes.size());

        for (int i = 0; i < filenotes.size(); ++i)
        {
            editor.putString(ApplicationPreferences.ONE_FILENOTE + "_" + String.valueOf(i + 1), filenotes.get(i).toString());
        }

        editor.apply();
    }

    public ArrayList<CharSequence> loadLastFileNotes()
    {
        SharedPreferences prefs = getSharedPreferences(FILE_NOTES_SHARED_PREFERENCES, Context.MODE_PRIVATE);

        int filenoteCount = prefs.getInt(ApplicationPreferences.LAST_FILENOTES, 0);

        ArrayList<CharSequence> res = new ArrayList<CharSequence>();

        for (int i = 0; i < filenoteCount; ++i)
        {
            String fileNote = prefs.getString(ApplicationPreferences.ONE_FILENOTE + "_" + String.valueOf(i + 1),"");

            if (
                    !TextUtils.isEmpty(fileNote)
                            &&
                            !res.contains(fileNote)
                    )
            {
                res.add(fileNote);
            }
        }

        return res;
    }
}
