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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

public class FilesActivity extends AppCompatActivity
{
    private static final String TAG = "FilesActivity";

    private static final String FILE_NAMES_SHARED_PREFERENCES = "FileNames";
    private static final String FILE_NOTES_SHARED_PREFERENCES = "FileNotes";

    private static final int REQUEST_SETTINGS = 1;
    private static final int REQUEST_REVIEW   = 2;

    private static final int TIME_FOR_CLOSE   = 1000;



    private PlaceholderFragment mPlaceholderFragment = null;
    private Tracker             mTracker             = null;
    private long                mBackPressTime       = 0;



    /**
     * Called when the activity is starting. This is where most initialization should go: calling setContentView(int) to inflate the activity's UI, using findViewById(int) to programmatically interact with widgets in the UI, calling managedQuery(android.net.Uri, String[], String, String[], String) to retrieve cursors for data being displayed, etc.
     * You can call finish() from within this function, in which case onDestroy() will be immediately called without any of the rest of the activity lifecycle (onStart(), onResume(), onPause(), etc) executing.
     * Derived classes must call through to the super class's implementation of this method. If they do not, an exception will be thrown.
     *
     * @param savedInstanceState    If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        CodeReviewApplication application = (CodeReviewApplication) getApplication();
        mTracker = application.getDefaultTracker();

        if (savedInstanceState==null)
        {
            getSupportFragmentManager().beginTransaction()
                                       .add(R.id.container, new PlaceholderFragment())
                                       .commit();
        }
    }

    /**
     * Called after onRestoreInstanceState(Bundle), onRestart(), or onPause(), for your activity to start interacting with the user. This is a good place to begin animations, open exclusive-access devices (such as the camera), etc.
     * Keep in mind that onResume is not the best indicator that your activity is visible to the user; a system window such as the keyguard may be in front. Use onWindowFocusChanged(boolean) to know for certain that your activity is visible to the user (for example, to resume a game).
     * Derived classes must call through to the super class's implementation of this method. If they do not, an exception will be thrown.
     */
    @Override
    protected void onResume()
    {
        super.onResume();

        String name = "FilesActivity";

        Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName(name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /**
     * Perform any final cleanup before an activity is destroyed. This can happen either because the activity is finishing (someone called finish() on it, or because the system is temporarily destroying this instance of the activity to save space. You can distinguish between these two scenarios with the isFinishing() method.
     * Note: do not count on this method being called as a place for saving data! For example, if an activity is editing data in a content provider, those edits should be committed in either onPause() or onSaveInstanceState(Bundle), not here. This method is usually implemented to free resources like threads that are associated with an activity, so that a destroyed activity does not leave such things around while the rest of its application is still running. There are situations where the system will simply kill the activity's hosting process without calling this method (or any others) in it, so it should not be used to do things that are intended to remain around after the process goes away.
     * Derived classes must call through to the super class's implementation of this method. If they do not, an exception will be thrown.
     */
    @Override
    protected void onDestroy()
    {
        mPlaceholderFragment = null;

        super.onDestroy();
    }

    /**
     * Initialize the contents of the Activity's standard options menu. You should place your menu items in to menu.
     * This is only called once, the first time the options menu is displayed. To update the menu every time it is displayed, see onPrepareOptionsMenu(Menu).
     * The default implementation populates the menu with standard system menu items. These are placed in the CATEGORY_SYSTEM group so that they will be correctly ordered with application-defined menu items. Deriving classes should always call through to the base implementation.
     * You can safely hold on to menu (and any items created from it), making modifications to it as desired, until the next time onCreateOptionsMenu() is called.
     * When you add items to the menu, you can implement the Activity's onOptionsItemSelected(MenuItem) method to handle them there.
     *
     * @param menu  The options menu in which you place your items.
     * @return      You must return true for the menu to be displayed; if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_files, menu);
        return true;
    }

    /**
     * This hook is called whenever an item in your options menu is selected. The default implementation simply returns false to have the normal processing happen (calling the item's Runnable or sending a message to its Handler as appropriate). You can use this method for any items for which you would like to do processing without those other facilities.
     * Derived classes should call through to the base class for it to perform the default menu handling.
     *
     * @param item  The menu item that was selected.
     * @return      Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId())
        {
            case R.id.action_sort:
            {
                if (mPlaceholderFragment != null)
                {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.action_sort)
                        .setSingleChoiceItems(R.array.sort_types,
                                mPlaceholderFragment.getAdapter().getSortType().ordinal() - 1,
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
                                        SortType selectedType = SortType.values()[which + 1];

                                        mTracker.send(
                                                      new HitBuilders.EventBuilder()
                                                                                    .setCategory("Action")
                                                                                    .setAction("Sort")
                                                                                    .setLabel("By" + selectedType.toString())
                                                                                    .build()
                                                     );

                                        mPlaceholderFragment.getAdapter().sort(selectedType);
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

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with, the resultCode it returned, and any additional data from it. The resultCode will be RESULT_CANCELED if the activity explicitly returned that, didn't return any result, or crashed during its operation.
     * You will receive this call immediately before onResume() when your activity is re-starting.
     * This method is never invoked if your activity sets noHistory to true.
     *
     * @param requestCode   The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode    The integer result code returned by the child activity through its setResult().
     * @param data          An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
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

    /**
     * Called when the activity has detected the user's press of the back key. The default implementation simply finishes the current activity, but you can override this to do whatever you want.
     */
    @Override
    public void onBackPressed()
    {
        if (mPlaceholderFragment == null || !mPlaceholderFragment.onBackPressed())
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

    /**
     * Clears LAST_PATH preference
     */
    private void clearPath()
    {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(ApplicationPreferences.LAST_PATH, "");

        editor.apply();
    }

    /**
     * Saves sort type
     */
    private void saveSortType()
    {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(ApplicationPreferences.SORT_TYPE, mPlaceholderFragment.getAdapter().getSortType().ordinal());

        editor.apply();
    }

    /**
     * Returns placeholder fragment
     *
     * @return  Placeholder fragment
     */
    public PlaceholderFragment getPlaceholderFragment()
    {
        return mPlaceholderFragment;
    }

    /**
     * Sets placeholder fragment
     *
     * @param fragment  Placeholder fragment
     */
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



        /**
         * PlaceholderFragment constructor
         */
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

            editor.apply();
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

            editor.apply();
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
            SharedPreferences prefs=mActivity.getSharedPreferences(FILE_NAMES_SHARED_PREFERENCES, Context.MODE_PRIVATE);

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
            SharedPreferences prefs=mActivity.getSharedPreferences(FILE_NAMES_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=prefs.edit();

            editor.putInt(ApplicationPreferences.LAST_FILENAMES, fileNames.size());

            for (int i=0; i<fileNames.size(); ++i)
            {
                editor.putString(ApplicationPreferences.ONE_FILENAME+"_"+String.valueOf(i+1), fileNames.get(i).toString());
            }

            editor.apply();
        }

        public ArrayList<CharSequence> loadLastFileNotes()
        {
            SharedPreferences prefs=mActivity.getSharedPreferences(FILE_NOTES_SHARED_PREFERENCES, Context.MODE_PRIVATE);

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
            SharedPreferences prefs=mActivity.getSharedPreferences(FILE_NOTES_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=prefs.edit();

            editor.putInt(ApplicationPreferences.LAST_FILENOTES, filenotes.size());

            for (int i=0; i<filenotes.size(); ++i)
            {
                editor.putString(ApplicationPreferences.ONE_FILENOTE+"_"+String.valueOf(i+1), filenotes.get(i).toString());
            }

            editor.apply();
        }

        public FilesAdapter getAdapter()
        {
            return mAdapter;
        }
    }
}
