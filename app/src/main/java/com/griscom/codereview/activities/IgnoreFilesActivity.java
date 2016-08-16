package com.griscom.codereview.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.griscom.codereview.R;
import com.griscom.codereview.lists.IgnoreFilesAdapter;

/**
 * Activity that allow to choose files for ignoring
 */
public class IgnoreFilesActivity extends AppCompatActivity implements OnItemClickListener
{
    @SuppressWarnings("unused")
    private static final String TAG = "IgnoreFilesActivity";



    private ListView           mIgnoreFilesListView = null;
    private IgnoreFilesAdapter mAdapter             = null;



    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ignore_files);



        Toolbar toolbar      = (Toolbar) findViewById(R.id.toolbar);
        mIgnoreFilesListView = (ListView)findViewById(R.id.ignoreFilesListView);



        setSupportActionBar(toolbar);



        mAdapter = new IgnoreFilesAdapter(this);

        mIgnoreFilesListView.setAdapter(mAdapter);
        mIgnoreFilesListView.setOnItemClickListener(this);

        setChoiceListener();
    }

    /** {@inheritDoc} */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_ignore_files, menu);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
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
                                                mAdapter.addFile(editText.getText().toString());

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

            default:
            {
                Log.e(TAG, "Unknown action ID: " + String.valueOf(item.getItemId()));
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    /** {@inheritDoc} */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
    {
        final EditText editText = new EditText(this);

        editText.setText((String)parent.getItemAtPosition(position));
        editText.selectAll();

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
                mAdapter.setSelected(position, checked);

                int selectedCount = mIgnoreFilesListView.getCheckedItemCount();
                mode.setSubtitle(getResources().getQuantityString(R.plurals.items_selected, selectedCount, selectedCount));
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

                    default:
                    {
                        Log.e(TAG, "Unknown action ID: " + String.valueOf(item.getItemId()));
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
}
