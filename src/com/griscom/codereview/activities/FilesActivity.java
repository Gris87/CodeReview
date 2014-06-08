package com.griscom.codereview.activities;

import java.io.File;
import java.io.FileNotFoundException;

import junit.framework.Assert;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.griscom.codereview.BuildConfig;
import com.griscom.codereview.R;
import com.griscom.codereview.lists.FilesAdapter;
import com.griscom.codereview.other.ApplicationExtras;
import com.griscom.codereview.other.ApplicationPreferences;
import com.griscom.codereview.other.FileEntry;
import com.griscom.codereview.other.SortType;

public class FilesActivity extends ActionBarActivity
{
    private static final int REQUEST_REVIEW = 1;



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
                                              new OnClickListener()
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
                startActivity(intent);

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
                    mode.setTitle(R.string.select_files);
                    mode.getMenuInflater().inflate(R.menu.context_menu_files, menu);

                    mAdapter.setSelectionMode(true);

                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu)
                {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item)
                {
                    // TODO: Implement this method
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode)
                {
                    mAdapter.setSelectionMode(false);
                }
            });
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

        public FilesAdapter getAdapter()
        {
            return mAdapter;
        }
    }
}
