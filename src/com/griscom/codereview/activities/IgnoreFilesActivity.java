package com.griscom.codereview.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.griscom.codereview.R;
import com.griscom.codereview.lists.*;
import android.widget.*;
import android.widget.AdapterView.*;

public class IgnoreFilesActivity extends ActionBarActivity
{
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
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements OnItemClickListener
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
			
            return rootView;
        }
		
		@Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
			// TODO: Implement it
		}
    }
}
