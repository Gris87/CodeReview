package com.griscom.codereview.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.griscom.codereview.CodeReviewApplication;
import com.griscom.codereview.R;
import com.griscom.codereview.lists.IgnoreFilesAdapter;

/**
 * Activity that allow to choose files for ignoring
 */
public class IgnoreFilesActivity extends AppCompatActivity
{
    private static final String TAG = "IgnoreFilesActivity";



    private PlaceholderFragment mPlaceholderFragment = null;
    private Tracker             mTracker             = null;



    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ignore_files);

        CodeReviewApplication application = (CodeReviewApplication) getApplication();
        mTracker = application.getDefaultTracker();

        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                                       .add(R.id.container, new PlaceholderFragment())
                                       .commit();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onResume()
    {
        super.onResume();

        String name = "IgnoreFilesActivity";

        Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName(name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /** {@inheritDoc} */
    @Override
    protected void onDestroy()
    {
        mPlaceholderFragment = null;

        super.onDestroy();
    }

    /** {@inheritDoc} */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ignore_files, menu);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId())
        {
            case R.id.action_add:
            {
                final EditText editText = new EditText(this);

                AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_add_file_title)
                    .setMessage(R.string.dialog_add_file_message)
                    .setView(editText)
                    .setPositiveButton(android.R.string.ok,
                                       new DialogInterface.OnClickListener()
                                       {
                                            /**
                                             * Handler for click event
                                             *
                                             * @param dialog        Dialog
                                             * @param whichButton   Selected option
                                             */
                                            @Override
                                            public void onClick(DialogInterface dialog, int whichButton)
                                            {
                                                if (mPlaceholderFragment != null)
                                                {
                                                    mPlaceholderFragment.getAdapter().addFile(editText.getText().toString());
                                                }

                                                dialog.dismiss();
                                            }
                                       })
                    .setNegativeButton(android.R.string.cancel,
                                       new DialogInterface.OnClickListener()
                                       {
                                            /**
                                             * Handler for click event
                                             *
                                             * @param dialog        Dialog
                                             * @param whichButton   Selected option
                                             */
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
        mPlaceholderFragment = fragment;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements OnItemClickListener
    {
        private IgnoreFilesActivity mActivity            = null;
        private ListView            mIgnoreFilesListView = null;
        private IgnoreFilesAdapter  mAdapter             = null;



        /** {@inheritDoc} */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            mActivity = (IgnoreFilesActivity)getActivity();

            mAdapter = new IgnoreFilesAdapter(mActivity);



            View rootView = inflater.inflate(R.layout.fragment_ignore_files, container, false);

            mIgnoreFilesListView = (ListView)rootView.findViewById(R.id.ignoreFileslistView);
            mIgnoreFilesListView.setAdapter(mAdapter);
            mIgnoreFilesListView.setOnItemClickListener(this);

            setChoiceListener();

            mActivity.setPlaceholderFragment(this);

            return rootView;
        }

        /** {@inheritDoc} */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
        {
            final EditText editText = new EditText(mActivity);

            editText.setText((String)parent.getItemAtPosition(position));
            editText.selectAll();

            AlertDialog dialog = new AlertDialog.Builder(mActivity)
                .setTitle(R.string.dialog_add_file_title)
                .setMessage(R.string.dialog_add_file_message)
                .setView(editText)
                .setPositiveButton(android.R.string.ok,
                                   new DialogInterface.OnClickListener()
                                   {
                                        /**
                                         * Handler for click event
                                         *
                                         * @param dialog        Dialog
                                         * @param whichButton   Selected option
                                         */
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
                                        /**
                                         * Handler for click event
                                         *
                                         * @param dialog        Dialog
                                         * @param whichButton   Selected option
                                         */
                                        @Override
                                        public void onClick(DialogInterface dialog, int whichButton)
                                        {
                                            dialog.dismiss();
                                        }
                                   }).create();

            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            dialog.show();
        }

        /**
         * Sets choice listener on ActionMode
         */
        private void setChoiceListener()
        {
            mIgnoreFilesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mIgnoreFilesListView.setMultiChoiceModeListener(new MultiChoiceModeListener()
            {
                /** {@inheritDoc} */
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
                {
                    int selectedCount = mIgnoreFilesListView.getCheckedItemCount();
                    mode.setSubtitle(mActivity.getResources().getQuantityString(R.plurals.items_selected, selectedCount, selectedCount));

                    mAdapter.setSelected(position, checked);
                }

                /** {@inheritDoc} */
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu)
                {
                    mode.setTitle(R.string.select_items);
                    mode.getMenuInflater().inflate(R.menu.context_menu_ignore_files, menu);

                    mAdapter.setSelectionMode(true);

                    return true;
                }

                /** {@inheritDoc} */
                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu)
                {
                    return false;
                }

                /** {@inheritDoc} */
                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item)
                {
                    switch (item.getItemId())
                    {
                        case R.id.action_delete:
                        {
                            mAdapter.removeSelectedFiles();

                            mode.finish();
                        }
                        break;
                    }

                    return true;
                }

                /** {@inheritDoc} */
                @Override
                public void onDestroyActionMode(ActionMode mode)
                {
                    mAdapter.setSelectionMode(false);
                }
            });
        }

        /**
         * Gets adapter for ignore files list
         *
         * @return  Ignore files adapter
         */
        public IgnoreFilesAdapter getAdapter()
        {
            return mAdapter;
        }
    }
}
