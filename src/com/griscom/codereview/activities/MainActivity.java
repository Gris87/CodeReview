package com.griscom.codereview.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.griscom.codereview.R;
import com.griscom.codereview.listeners.OnBackPressedListener;
import com.griscom.codereview.lists.FilesAdapter;
import com.griscom.codereview.other.FileEntry;

public class MainActivity extends ActionBarActivity
{
    private OnBackPressedListener mOnBackPressedListener=null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState==null)
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id=item.getItemId();

        if (id==R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements OnItemClickListener, OnBackPressedListener
    {
        private ActionBar    mActionBar;
        private ListView     mFilesListView;
        private FilesAdapter mAdapter;

        public PlaceholderFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            MainActivity activity=(MainActivity)getActivity();

            mAdapter=new FilesAdapter(activity);



            View rootView=inflater.inflate(R.layout.fragment_main, container, false);

            mFilesListView=(ListView)rootView.findViewById(R.id.fileslistView);
            mFilesListView.setAdapter(mAdapter);
            mFilesListView.setOnItemClickListener(this);

            mActionBar=activity.getSupportActionBar();
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setTitle(mAdapter.getCurrentPath());

            activity.setOnBackPressedListener(this);

            return rootView;
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
                        String curPath=mAdapter.getCurrentPath();

                        if (curPath.endsWith("/"))
                        {
                            mAdapter.setCurrentPath(curPath+fileName);
                        }
                        else
                        {
                            mAdapter.setCurrentPath(curPath+"/"+fileName);
                        }
                    }

                    mActionBar.setTitle(mAdapter.getCurrentPath());
                }
                else
                {

                }
            }
        }

        @Override
        public boolean onBackPressed()
        {
            if (mAdapter.getCurrentPath().equals("/"))
            {
                return false;
            }

            mAdapter.goUp();
            mActionBar.setTitle(mAdapter.getCurrentPath());

            return true;
        }
    }

    @Override
    public void onBackPressed()
    {
        if (mOnBackPressedListener==null || !mOnBackPressedListener.onBackPressed())
        {
            super.onBackPressed();
        }
    }

    public OnBackPressedListener getOnBackPressedListener()
    {
        return mOnBackPressedListener;
    }

    public void setOnBackPressedListener(OnBackPressedListener listener)
    {
        mOnBackPressedListener=listener;
    }
}
