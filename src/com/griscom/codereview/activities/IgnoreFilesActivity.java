package com.griscom.codereview.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.griscom.codereview.R;
import com.griscom.codereview.listeners.OnFileAddedListener;
import com.griscom.codereview.lists.IgnoreFilesAdapter;
import android.view.ContextMenu.*;
import android.view.*;
import android.widget.AbsListView.*;

public class IgnoreFilesActivity extends ActionBarActivity
{
    private OnFileAddedListener mOnFileAddedListener=null;

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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ignore_files, menu);
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

                new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_add_file_title)
                    .setMessage(R.string.dialog_add_file_message)
                    .setView(editText)
                    .setPositiveButton(android.R.string.ok,
                                       new DialogInterface.OnClickListener()
                                       {
                                            @Override
                                            public void onClick(DialogInterface dialog, int whichButton)
                                            {
                                                if (mOnFileAddedListener!=null)
                                                {
                                                    mOnFileAddedListener.onFileAdded(editText.getText().toString());
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
                                       }).show();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements OnItemClickListener, OnFileAddedListener
    {
    private IgnoreFilesActivity mActivity;
        private ListView            mIgnoreFilesListView;
        private IgnoreFilesAdapter  mAdapter;

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
			mIgnoreFilesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			mIgnoreFilesListView.setMultiChoiceModeListener(mChoiceListener);

            mActivity.setOnFileAddedListener(this);

            return rootView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
        {
            final EditText editText=new EditText(mActivity);
            editText.setText((String)parent.getItemAtPosition(position));
            editText.selectAll();

            new AlertDialog.Builder(mActivity)
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
                                   }).show();

            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        }
		
		MultiChoiceModeListener mChoiceListener=new MultiChoiceModeListener()
		{
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
			{
				// TODO: Implement this method
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu)
			{
				mode.getMenuInflater().inflate(R.menu.ignore_files_context, menu);
				return true;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu)
			{
				// TODO: Implement this method
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem menu)
			{
				// TODO: Implement this method
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode)
			{
				// TODO: Implement this method
			}
		};
		
		@Override
        public void onFileAdded(String fileName)
        {
            mAdapter.addFile(fileName);
        }
    }

    public OnFileAddedListener getOnFileAddedListener()
    {
        return mOnFileAddedListener;
    }

    public void setOnFileAddedListener(OnFileAddedListener listener)
    {
        mOnFileAddedListener=listener;
    }
}
