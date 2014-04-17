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
import android.widget.ListView;

import com.griscom.codereview.R;
import com.griscom.codereview.lists.FilesAdapter;

public class MainActivity extends ActionBarActivity
{
    private PlaceholderFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState==null)
        {
            mFragment=new PlaceholderFragment();

            getSupportFragmentManager().beginTransaction()
                                       .add(R.id.container, mFragment)
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
    public static class PlaceholderFragment extends Fragment
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
            mActionBar=((ActionBarActivity)getActivity()).getSupportActionBar();

            mAdapter=new FilesAdapter(getActivity());



            View rootView=inflater.inflate(R.layout.fragment_main, container, false);

            mFilesListView=(ListView)rootView.findViewById(R.id.fileslistView);
            mFilesListView.setAdapter(mAdapter);

            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setTitle(mAdapter.getCurrentPath());

            return rootView;
        }

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
        if (!mFragment.onBackPressed())
        {
            super.onBackPressed();
        }
    }
}
