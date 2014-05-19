package com.griscom.codereview.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;
import com.griscom.codereview.R;
import com.griscom.codereview.lists.IgnoreFilesAdapter;

public class IgnoreFilesActivity extends ActionBarActivity
{
    private PlaceholderFragment mPlaceholderFragment=null;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ignore_files);

        if (savedInstanceState == null)
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
        getMenuInflater().inflate(R.menu.menu_ignore_files, menu);
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
            case R.id.action_add:
            {
                final EditText editText=new EditText(this);

                AlertDialog dialog=new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_add_file_title)
                    .setMessage(R.string.dialog_add_file_message)
                    .setView(editText)
                    .setPositiveButton(android.R.string.ok,
                                       new DialogInterface.OnClickListener()
                                       {
                                            @Override
                                            public void onClick(DialogInterface dialog, int whichButton)
                                            {
                                                if (mPlaceholderFragment!=null)
                                                {
                                                    mPlaceholderFragment.getAdapter().addFile(editText.getText().toString());
                                                }

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

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
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
        private IgnoreFilesActivity mActivity;
        private ListView            mIgnoreFilesListView;
        private IgnoreFilesAdapter  mAdapter;
        private int                 mLastSelectedItem;

        public PlaceholderFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            mActivity=(IgnoreFilesActivity)getActivity();

            mAdapter=new IgnoreFilesAdapter(mActivity);



            View rootView=inflater.inflate(R.layout.fragment_ignore_files, container, false);

            mIgnoreFilesListView=(ListView)rootView.findViewById(R.id.ignoreFileslistView);
            mIgnoreFilesListView.setAdapter(mAdapter);
            mIgnoreFilesListView.setOnItemClickListener(this);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            {
                registerForContextMenu(mIgnoreFilesListView);
            }
            else
            {
                setChoiceListener();
            }

            mActivity.setPlaceholderFragment(this);

            return rootView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
        {
            final EditText editText=new EditText(mActivity);
            editText.setText((String)parent.getItemAtPosition(position));
            editText.selectAll();

            AlertDialog dialog=new AlertDialog.Builder(mActivity)
                .setTitle(R.string.dialog_add_file_title)
                .setMessage(R.string.dialog_add_file_message)
                .setView(editText)
                .setPositiveButton(android.R.string.ok,
                                   new DialogInterface.OnClickListener()
                                   {
                                        @Override
                                        public void onClick(DialogInterface dialog, int whichButton)
                                        {
                                            mAdapter.renameFile(position, editText.getText().toString());

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
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
        {
            mLastSelectedItem=((AdapterContextMenuInfo)menuInfo).position;

            mActivity.getMenuInflater().inflate(R.menu.context_menu_ignore_files, menu);
            super.onCreateContextMenu(menu, v, menuInfo);
        }

        @Override
        public boolean onContextItemSelected(MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.action_delete:
                    mAdapter.removeFile(mLastSelectedItem);
                break;
            }

            return true;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        private void setChoiceListener()
        {
            mIgnoreFilesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mIgnoreFilesListView.setMultiChoiceModeListener(new MultiChoiceModeListener()
            {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
                {
                    int selectedCount=mIgnoreFilesListView.getCheckedItemCount();
                    mode.setSubtitle(mActivity.getResources().getQuantityString(R.plurals.items_selected, selectedCount, selectedCount));

                    mAdapter.setSelected(position, checked);
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu)
                {
                    mode.setTitle(R.string.select_items);
                    mode.getMenuInflater().inflate(R.menu.context_menu_ignore_files, menu);

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
                    switch(item.getItemId())
                    {
                        case R.id.action_delete:
                            mAdapter.removeSelectedFiles();

                            mode.finish();
                        break;
                    }

                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode)
                {
                    mAdapter.setSelectionMode(false);
                }
            });
        }

        public IgnoreFilesAdapter getAdapter()
        {
            return mAdapter;
        }
    }
}
