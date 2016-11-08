package com.griscom.codereview.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.griscom.codereview.R;
import com.griscom.codereview.dialogs.InputDialog;
import com.griscom.codereview.lists.IgnoreFilesAdapter;
import com.griscom.codereview.util.AppLog;

import junit.framework.Assert;

import java.util.ArrayList;

/**
 * Activity that allow to choose files for ignoring
 */
@SuppressWarnings({"ClassWithoutConstructor", "PublicConstructor"})
public class IgnoreFilesActivity extends AppCompatActivity implements OnItemClickListener, InputDialog.OnFragmentInteractionListener
{
    @SuppressWarnings("unused")
    private static final String TAG = "IgnoreFilesActivity";



    private static final int INPUT_DIALOG_ID_ADD  = 1;
    private static final int INPUT_DIALOG_ID_EDIT = 2;



    private static final String DATA_POSITION = "POSITION";



    private static final String SAVED_STATE_SELECTION = "SELECTION";



    private ListView           mIgnoreFilesListView = null;
    private IgnoreFilesAdapter mAdapter             = null;



    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "IgnoreFilesActivity{" +
                "mIgnoreFilesListView=" + mIgnoreFilesListView +
                ", mAdapter="           + mAdapter             +
                '}';
    }

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ignore_files);



        Toolbar toolbar      = (Toolbar) findViewById(R.id.toolbar);
        mIgnoreFilesListView = (ListView)findViewById(R.id.ignoreFilesListView);



        setSupportActionBar(toolbar);



        mAdapter = IgnoreFilesAdapter.newInstance(this);

        mIgnoreFilesListView.setAdapter(mAdapter);
        mIgnoreFilesListView.setOnItemClickListener(this);

        setChoiceListener();
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
        Assert.assertNotNull("selection is null", selection);

        if (!selection.isEmpty())
        {
            mAdapter.setSelectionMode(IgnoreFilesAdapter.SELECTION_MODE_ENABLED);

            for (int i = 0; i < selection.size(); ++i)
            {
                mAdapter.setSelected(selection.get(i), IgnoreFilesAdapter.ITEM_SELECTED);
            }
        }
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
                InputDialog dialog = InputDialog.newInstance(INPUT_DIALOG_ID_ADD, R.string.ignore_files_dialog_title, R.string.ignore_files_dialog_message, null, null);
                dialog.show(getSupportFragmentManager(), "InputDialog");

                return true;
            }

            default:
            {
                AppLog.wtf(TAG, "Unknown action ID: " + item.getItemId());
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    /** {@inheritDoc} */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Bundle data = new Bundle();
        data.putInt(DATA_POSITION, position);

        InputDialog dialog = InputDialog.newInstance(INPUT_DIALOG_ID_EDIT, R.string.ignore_files_dialog_title, R.string.ignore_files_dialog_message, (String)mAdapter.getItem(position), data);
        dialog.show(getSupportFragmentManager(), "InputDialog");
    }

    /** {@inheritDoc} */
    @Override
    public void onTextEntered(int id, String text, Bundle data)
    {
        switch (id)
        {
            case INPUT_DIALOG_ID_ADD:
            {
                mAdapter.add(text);
            }
            break;

            case INPUT_DIALOG_ID_EDIT:
            {
                mAdapter.replace(data.getInt(DATA_POSITION), text);
            }
            break;

            default:
            {
                AppLog.wtf(TAG, "Unknown id: " + id);
            }
            break;
        }
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
                mAdapter.setSelected(position, checked ? IgnoreFilesAdapter.ITEM_SELECTED : IgnoreFilesAdapter.ITEM_DESELECTED);

                int selectedCount = mIgnoreFilesListView.getCheckedItemCount();
                mode.setSubtitle(getResources().getQuantityString(R.plurals.ignore_files_items_selected, selectedCount, selectedCount));
            }

            /** {@inheritDoc} */
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu)
            {
                mode.setTitle(R.string.ignore_files_select_items);
                mode.getMenuInflater().inflate(R.menu.context_menu_ignore_files, menu);

                mAdapter.setSelectionMode(IgnoreFilesAdapter.SELECTION_MODE_ENABLED);

                return true;
            }

            /** {@inheritDoc} */
            @SuppressWarnings("MethodReturnAlwaysConstant")
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
                    case R.id.action_select_all:
                    {
                        if (mAdapter.getSelection().size() != mAdapter.getCount())
                        {
                            for (int i = 0; i < mAdapter.getCount(); ++i)
                            {
                                if (!mIgnoreFilesListView.isItemChecked(i))
                                {
                                    mIgnoreFilesListView.setItemChecked(i, true);
                                }
                            }
                        }
                        else
                        {
                            for (int i = 1; i < mAdapter.getCount(); ++i)
                            {
                                mIgnoreFilesListView.setItemChecked(i, false);
                            }
                        }
                    }
                    break;

                    case R.id.action_delete:
                    {
                        mAdapter.removeSelected();

                        mode.finish();
                    }
                    break;

                    default:
                    {
                        AppLog.wtf(TAG, "Unknown action ID: " + item.getItemId());
                    }
                    break;
                }

                return true;
            }

            /** {@inheritDoc} */
            @Override
            public void onDestroyActionMode(ActionMode mode)
            {
                mAdapter.setSelectionMode(IgnoreFilesAdapter.SELECTION_MODE_DISABLED);
            }
        });
    }
}
