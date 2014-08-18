package com.griscom.codereview.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import junit.framework.Assert;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.griscom.codereview.BuildConfig;
import com.griscom.codereview.R;
import com.griscom.codereview.lists.FilesAdapter;
import com.griscom.codereview.other.ApplicationExtras;
import com.griscom.codereview.other.ApplicationPreferences;
import com.griscom.codereview.other.ColorCache;
import com.griscom.codereview.other.FileEntry;
import com.griscom.codereview.other.SortType;

public class FilesActivity extends ActionBarActivity
{
    private static final String TAG = "FilesActivity";

    private static final String FILENAMES_SHARED_PREFERENCES = "FileNames";
    private static final String FILENOTES_SHARED_PREFERENCES = "FileNotes";

    private static final int REQUEST_SETTINGS = 1;
    private static final int REQUEST_REVIEW   = 2;



    private PlaceholderFragment mPlaceholderFragment=null;
    private long                mBackPressTime=0;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        if (savedInstanceState==null)
        {
            getSupportFragmentManager().beginTransaction()
                                       .add(R.id.container, new PlaceholderFragment())
                                       .commit();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    protected void onDestroy()
    {
        mPlaceholderFragment=null;

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_files, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId())
        {
            case R.id.action_sort:
            {
                if (mPlaceholderFragment!=null)
                {
                    AlertDialog dialog=new AlertDialog.Builder(this)
                        .setTitle(R.string.action_sort)
                        .setSingleChoiceItems(R.array.sort_types,
                                              mPlaceholderFragment.getAdapter().getSortType().ordinal()-1,
                                              new DialogInterface.OnClickListener()
                                              {
                                                  @Override
                                                  public void onClick(DialogInterface dialog, int which)
                                                  {
                                                      mPlaceholderFragment.getAdapter().sort(SortType.values()[which+1]);
                                                      saveSortType();

                                                      dialog.dismiss();
                                                  }
                                              }).create();

                    dialog.show();
                }

                return true;
            }

            case R.id.action_settings:
            {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, REQUEST_SETTINGS);

                return true;
            }

            case R.id.action_close:
            {
                finish();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_SETTINGS:
            {
                ColorCache.update(this);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed()
    {
        if (mPlaceholderFragment==null || !mPlaceholderFragment.onBackPressed())
        {
            long curTime=System.currentTimeMillis();

            if (curTime-mBackPressTime<1000)
            {
                clearPath();
                super.onBackPressed();
            }
            else
            {
                mBackPressTime=curTime;

                Toast.makeText(this, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearPath()
    {
        SharedPreferences prefs=getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=prefs.edit();

        editor.putString(ApplicationPreferences.LAST_PATH, "");

        editor.commit();
    }

    private void saveSortType()
    {
        SharedPreferences prefs=getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=prefs.edit();

        editor.putInt(ApplicationPreferences.SORT_TYPE, mPlaceholderFragment.getAdapter().getSortType().ordinal());

        editor.commit();
    }

    public PlaceholderFragment getPlaceholderFragment()
    {
        return mPlaceholderFragment;
    }

    public void setPlaceholderFragment(PlaceholderFragment fragment)
    {
        mPlaceholderFragment=fragment;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements OnItemClickListener
    {
        private FilesActivity mActivity;
        private ActionBar     mActionBar;
        private ActionMode    mActionMode;
        private ListView      mFilesListView;
        private FilesAdapter  mAdapter;
        private int           mLastSelectedItem;

        public PlaceholderFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            mActivity=(FilesActivity)getActivity();

            ColorCache.update(mActivity);

            mAdapter=new FilesAdapter(mActivity);

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



            View rootView=inflater.inflate(R.layout.fragment_files, container, false);

            mFilesListView=(ListView)rootView.findViewById(R.id.fileslistView);
            mFilesListView.setAdapter(mAdapter);
            mFilesListView.setOnItemClickListener(this);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            {
                registerForContextMenu(mFilesListView);
            }
            else
            {
                setChoiceListener();
            }

            mActionBar=mActivity.getSupportActionBar();
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setTitle(mAdapter.getCurrentPath());

            mActionMode=null;

            mActivity.setPlaceholderFragment(this);

            return rootView;
        }

        @Override
        public void onResume()
        {
            super.onResume();

            if (!mAdapter.rescan())
            {
                savePath();
                updateCurrentPath();
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            if (parent==mFilesListView)
            {
                FileEntry file=(FileEntry)mFilesListView.getItemAtPosition(position);

                String fileName=file.getFileName();

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

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
        {
            mLastSelectedItem=((AdapterContextMenuInfo)menuInfo).position;

            if (mLastSelectedItem==0)
            {
                FileEntry file=(FileEntry)mAdapter.getItem(mLastSelectedItem);

                if (
                    file.isDirectory()
                    &&
                    file.getFileName().equals("..")
                   )
                {
                    return;
                }
            }

            mActivity.getMenuInflater().inflate(R.menu.context_menu_files, menu);
            super.onCreateContextMenu(menu, v, menuInfo);
        }

        @Override
        public boolean onContextItemSelected(MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.action_mark_to_rename:
                {
                    markToRename(new int[]{mLastSelectedItem});
                }
                break;
                case R.id.action_mark_to_delete:
                {
                    markToDelete(new int[]{mLastSelectedItem});
                }
                break;
                case R.id.action_note:
                {
                    assignNote(new int[]{mLastSelectedItem});
                }
                break;
                 case R.id.action_rename:
                {
                    rename(mLastSelectedItem);
                }
                break;
                case R.id.action_delete:
                {
                    delete(new int[]{mLastSelectedItem});
                }
                break;
                default:
                {
                    Log.e(TAG, "Unknown action: "+String.valueOf(item));
                }
                break;
            }

            return super.onContextItemSelected(item);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        private void setChoiceListener()
        {
            mFilesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mFilesListView.setMultiChoiceModeListener(new MultiChoiceModeListener()
            {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
                {
                    if (position==0 && checked)
                    {
                        FileEntry file=(FileEntry)mAdapter.getItem(position);

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

                    int selectedCount=mFilesListView.getCheckedItemCount();
                    mode.setSubtitle(mActivity.getResources().getQuantityString(R.plurals.files_selected, selectedCount, selectedCount));

                    mAdapter.setSelected(position, checked);
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu)
                {
                    mActionMode=mode;

                    mActionMode.setTitle(R.string.select_files);
                    mActionMode.getMenuInflater().inflate(R.menu.context_menu_files, menu);

                    mAdapter.setSelectionMode(true);

                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu)
                {
                    menu.findItem(R.id.action_rename).setVisible(mAdapter.getSelection().size()==1);

                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item)
                {
                    ArrayList<Integer> tempList=mAdapter.getSelection();
                    int items[]=new int[tempList.size()];

                    for (int i=0; i<tempList.size(); ++i)
                    {
                        items[i]=tempList.get(i).intValue();
                    }

                    boolean res=true;

                    switch (item.getItemId())
                    {
                        case R.id.action_mark_to_rename:
                        {
                            res=markToRename(items);
                        }
                        break;
                        case R.id.action_mark_to_delete:
                        {
                            res=markToDelete(items);
                        }
                        break;
                        case R.id.action_note:
                        {
                            res=assignNote(items);
                        }
                        break;
                        case R.id.action_rename:
                        {
                            res=rename(items[0]);
                        }
                        break;
                        case R.id.action_delete:
                        {
                            res=delete(items);
                        }
                        break;
                        default:
                        {
                            Log.e(TAG, "Unknown action: "+String.valueOf(item));
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
                    mActionMode=null;
                    mAdapter.setSelectionMode(false);
                }
            });
        }

        private boolean markToRename(final int items[])
        {
            if (items.length==1)
            {
                LayoutInflater inflater=(LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                View view=inflater.inflate(R.layout.dialog_input, null);

                final EditText editText     = (EditText)    view.findViewById(R.id.inputEditText);
                ImageButton    chooseButton = (ImageButton) view.findViewById(R.id.chooseButton);

                editText.setText(((FileEntry)mAdapter.getItem(items[0])).getFileName());

                chooseButton.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            ArrayList<CharSequence> filenames=loadLastFileNames();

                            if (filenames.size()>0)
                            {
                                final CharSequence items[]=new CharSequence[filenames.size()];
                                String currentFilename=editText.getText().toString();
                                int index=-1;

                                for (int i=0; i<filenames.size(); i++)
                                {
                                    String oneFilename=(String)filenames.get(i);

                                    if (index<0 && oneFilename.equals(currentFilename))
                                    {
                                        index=i;
                                    }

                                    items[i]=oneFilename;
                                }

                                if (index<0)
                                {
                                    index=0;
                                }

                                AlertDialog chooseDialog=new AlertDialog.Builder(mActivity)
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
                                Toast.makeText(mActivity, R.string.no_last_filenames, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                AlertDialog dialog=new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.dialog_input_filename_title)
                    .setMessage(R.string.dialog_input_filename_message)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                            String filename=editText.getText().toString();

                            if (!filename.equals(""))
                            {
                                ArrayList<CharSequence> filenames=loadLastFileNames();

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
                                Toast.makeText(mActivity, R.string.empty_filename, Toast.LENGTH_SHORT).show();
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
            LayoutInflater inflater=(LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view=inflater.inflate(R.layout.dialog_input, null);

            final EditText editText     = (EditText)    view.findViewById(R.id.inputEditText);
            ImageButton    chooseButton = (ImageButton) view.findViewById(R.id.chooseButton);

            if (items.length==1)
            {
                editText.setText(((FileEntry)mAdapter.getItem(items[0])).getFileNote());
            }

            chooseButton.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        ArrayList<CharSequence> filenotes=loadLastFileNotes();

                        if (filenotes.size()>0)
                        {
                            final CharSequence items[]=new CharSequence[filenotes.size()];
                            String currentFilenote=editText.getText().toString();
                            int index=-1;

                            for (int i=0; i<filenotes.size(); i++)
                            {
                                String oneFilenote=(String)filenotes.get(i);

                                if (index<0 && oneFilenote.equals(currentFilenote))
                                {
                                    index=i;
                                }

                                items[i]=oneFilenote;
                            }

                            if (index<0)
                            {
                                index=0;
                            }

                            AlertDialog chooseDialog=new AlertDialog.Builder(mActivity)
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
                            Toast.makeText(mActivity, R.string.no_last_filenotes, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            AlertDialog dialog=new AlertDialog.Builder(mActivity)
                .setTitle(R.string.dialog_input_filenote_title)
                .setMessage(R.string.dialog_input_filenote_message)
                .setView(view)
                .setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        String filenote=editText.getText().toString();

                        if (!filenote.equals(""))
                        {
                            ArrayList<CharSequence> filenotes=loadLastFileNotes();

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
            LayoutInflater inflater=(LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view=inflater.inflate(R.layout.dialog_input, null);

            final EditText editText     = (EditText)    view.findViewById(R.id.inputEditText);
            ImageButton    chooseButton = (ImageButton) view.findViewById(R.id.chooseButton);

            editText.setText(((FileEntry)mAdapter.getItem(item)).getFileName());

            chooseButton.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        ArrayList<CharSequence> filenames=loadLastFileNames();

                        if (filenames.size()>0)
                        {
                            final CharSequence items[]=new CharSequence[filenames.size()];
                            String currentFilename=editText.getText().toString();
                            int index=-1;

                            for (int i=0; i<filenames.size(); i++)
                            {
                                String oneFilename=(String)filenames.get(i);

                                if (index<0 && oneFilename.equals(currentFilename))
                                {
                                    index=i;
                                }

                                items[i]=oneFilename;
                            }

                            if (index<0)
                            {
                                index=0;
                            }

                            AlertDialog chooseDialog=new AlertDialog.Builder(mActivity)
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
                            Toast.makeText(mActivity, R.string.no_last_filenames, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            AlertDialog dialog=new AlertDialog.Builder(mActivity)
                .setTitle(R.string.dialog_input_filename_title)
                .setMessage(R.string.dialog_input_filename_message)
                .setView(view)
                .setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        String filename=editText.getText().toString();

                        if (!filename.equals(""))
                        {
                            ArrayList<CharSequence> filenames=loadLastFileNames();

                            filenames.remove(filename);
                            filenames.add(0, filename);
                            saveLastFileNames(filenames);

                            // ----------------------------------

                            if (!mAdapter.renameFile(item, filename))
                            {
                                Toast.makeText(mActivity, R.string.can_not_rename_file, Toast.LENGTH_SHORT).show();
                            }

                            hideActionMode();

                            dialog.dismiss();
                        }
                        else
                        {
                            Toast.makeText(mActivity, R.string.empty_filename, Toast.LENGTH_SHORT).show();
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
            AlertDialog dialog=new AlertDialog.Builder(mActivity)
                .setTitle(R.string.dialog_delete_files_title)
                .setMessage(mActivity.getResources().getQuantityString(R.plurals.dialog_delete_files_message, items.length, items.length, items.length))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int index)
                    {
                        ArrayList<String> keep=mAdapter.deleteFiles(items);

                        if (keep.size()>0)
                        {
                            if (keep.size()==1)
                            {
                                Toast.makeText(mActivity, getResources().getString(R.string.can_not_delete_file, keep.get(0)), Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(mActivity, getResources().getQuantityString(R.plurals.can_not_delete_files, keep.size(), keep.size()), Toast.LENGTH_SHORT).show();
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

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        private void hideActionMode()
        {
            if (mActionMode!=null)
            {
                mActionMode.finish();
            }
        }

        public boolean onBackPressed()
        {
            if (mAdapter.getCurrentPath().equals("/"))
            {
                return false;
            }

            mAdapter.goUp();
            savePath();
            updateCurrentPath();

            return true;
        }

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
                            mActivity.finish();
                        }
                        break;
                    }
                }
            }

            super.onActivityResult(requestCode, resultCode, data);
        }

        private void updateCurrentPath()
        {
            String oldPath=(String)mActionBar.getTitle();
            String newPath=mAdapter.getCurrentPath();

            if (newPath.length()<oldPath.length())
            {
                if (BuildConfig.DEBUG)
                {
                    Assert.assertTrue(oldPath.startsWith(newPath));
                }

                String tail=oldPath.substring(newPath.length());

                if (tail.startsWith("/"))
                {
                    if (BuildConfig.DEBUG)
                    {
                        Assert.assertTrue(tail.length()>1);
                    }

                    tail=tail.substring(1);
                }



                String prevFolder;

                int index=tail.lastIndexOf("/");

                if (index>=0)
                {
                    prevFolder=tail.substring(0, index);
                }
                else
                {
                    prevFolder=tail;
                }



                index=mAdapter.indexOf(prevFolder);

                if (index>=0)
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

        private void openFile(String fileName, int fileId) throws FileNotFoundException
        {
            String filePath=mAdapter.pathToFile(fileName);

            if (!(new File(filePath).exists()))
            {
                throw new FileNotFoundException();
            }

            Intent intent = new Intent(mActivity, ReviewActivity.class);
            intent.putExtra(ApplicationExtras.FILE_NAME, filePath);
            intent.putExtra(ApplicationExtras.FILE_ID,   fileId);
            startActivityForResult(intent, REQUEST_REVIEW);
        }

        private void savePath()
        {
            SharedPreferences prefs=getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=prefs.edit();

            editor.putString(ApplicationPreferences.LAST_PATH, mAdapter.getCurrentPath());

            editor.commit();
        }

        private void loadPath() throws FileNotFoundException
        {
            SharedPreferences prefs=getActivity().getPreferences(Context.MODE_PRIVATE);

            String path=prefs.getString(ApplicationPreferences.LAST_PATH, "");

            if (!TextUtils.isEmpty(path))
            {
                mAdapter.setCurrentPathBacktrace(path);

                if (!mAdapter.getCurrentPath().equals(path))
                {
                    throw new FileNotFoundException();
                }
            }
        }

        private void saveLastFile(String fileName)
        {
            SharedPreferences prefs=getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=prefs.edit();

            editor.putString(ApplicationPreferences.LAST_FILE, fileName);

            editor.commit();
        }

        private void loadLastFile() throws FileNotFoundException
        {
            SharedPreferences prefs=getActivity().getPreferences(Context.MODE_PRIVATE);

            String fileName=prefs.getString(ApplicationPreferences.LAST_FILE, "");

            if (!TextUtils.isEmpty(fileName))
            {
                openFile(fileName, 0);
            }
        }

        private void loadSortType()
        {
            SharedPreferences prefs=getActivity().getPreferences(Context.MODE_PRIVATE);

            int sortType=prefs.getInt(ApplicationPreferences.SORT_TYPE, SortType.NAME.ordinal());
            SortType sortTypes[]=SortType.values();

            if (sortType>=1 && sortType<sortTypes.length && mAdapter.getSortType().ordinal()!=sortType)
            {
                mAdapter.sort(sortTypes[sortType]);
            }
        }

        public ArrayList<CharSequence> loadLastFileNames()
        {
            SharedPreferences prefs=mActivity.getSharedPreferences(FILENAMES_SHARED_PREFERENCES, Context.MODE_PRIVATE);

            int fileNameCount=prefs.getInt(ApplicationPreferences.LAST_FILENAMES, 0);

            ArrayList<CharSequence> res=new ArrayList<CharSequence>();

            for (int i=0; i<fileNameCount; ++i)
            {
                String fileName=prefs.getString(ApplicationPreferences.ONE_FILENAME+"_"+String.valueOf(i+1),"");

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

        public void saveLastFileNames(ArrayList<CharSequence> fileNames)
        {
            SharedPreferences prefs=mActivity.getSharedPreferences(FILENAMES_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=prefs.edit();

            editor.putInt(ApplicationPreferences.LAST_FILENAMES, fileNames.size());

            for (int i=0; i<fileNames.size(); ++i)
            {
                editor.putString(ApplicationPreferences.ONE_FILENAME+"_"+String.valueOf(i+1), fileNames.get(i).toString());
            }

            editor.commit();
        }

        public ArrayList<CharSequence> loadLastFileNotes()
        {
            SharedPreferences prefs=mActivity.getSharedPreferences(FILENOTES_SHARED_PREFERENCES, Context.MODE_PRIVATE);

            int filenoteCount=prefs.getInt(ApplicationPreferences.LAST_FILENOTES, 0);

            ArrayList<CharSequence> res=new ArrayList<CharSequence>();

            for (int i=0; i<filenoteCount; ++i)
            {
                String fileNote=prefs.getString(ApplicationPreferences.ONE_FILENOTE+"_"+String.valueOf(i+1),"");

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

        public void saveLastFileNotes(ArrayList<CharSequence> filenotes)
        {
            SharedPreferences prefs=mActivity.getSharedPreferences(FILENOTES_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=prefs.edit();

            editor.putInt(ApplicationPreferences.LAST_FILENOTES, filenotes.size());

            for (int i=0; i<filenotes.size(); ++i)
            {
                editor.putString(ApplicationPreferences.ONE_FILENOTE+"_"+String.valueOf(i+1), filenotes.get(i).toString());
            }

            editor.commit();
        }

        public FilesAdapter getAdapter()
        {
            return mAdapter;
        }
    }
}
