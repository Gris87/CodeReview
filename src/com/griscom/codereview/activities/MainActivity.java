package com.griscom.codereview.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.griscom.codereview.R;
import com.griscom.codereview.activities.ReviewActivity;
import com.griscom.codereview.listeners.OnBackPressedListener;
import com.griscom.codereview.lists.FilesAdapter;
import com.griscom.codereview.other.ApplicationExtras;
import com.griscom.codereview.other.ApplicationPreferences;
import com.griscom.codereview.other.FileEntry;

public class MainActivity extends ActionBarActivity
{
	private static final int REQUEST_REVIEW=1;
	
    private OnBackPressedListener mOnBackPressedListener=null;

    private long mBackPressTime=0;

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
        switch(item.getItemId())
        {
            case R.id.action_settings:
            {
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements OnItemClickListener, OnBackPressedListener
    {
        private MainActivity mActivity;
        private ActionBar    mActionBar;
        private ListView     mFilesListView;
        private FilesAdapter mAdapter;

        public PlaceholderFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            mActivity=(MainActivity)getActivity();

            mAdapter=new FilesAdapter(mActivity);
            loadPath();
			loadLastFile();



            View rootView=inflater.inflate(R.layout.fragment_main, container, false);

            mFilesListView=(ListView)rootView.findViewById(R.id.fileslistView);
            mFilesListView.setAdapter(mAdapter);
            mFilesListView.setOnItemClickListener(this);

            mActionBar=mActivity.getSupportActionBar();
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setTitle(mAdapter.getCurrentPath());

            mActivity.setOnBackPressedListener(this);

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
                        mAdapter.setCurrentPath(mAdapter.pathToFile(fileName));
                    }

                    savePath();

                    mActionBar.setTitle(mAdapter.getCurrentPath());
                }
                else
                {
					saveLastFile(fileName);
					
                    openFile(fileName);
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
            savePath();
            mActionBar.setTitle(mAdapter.getCurrentPath());

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
		
		private void openFile(String fileName)
		{
			Intent intent = new Intent(mActivity, ReviewActivity.class);
			intent.putExtra(ApplicationExtras.OPEN_FILE, mAdapter.pathToFile(fileName));
			startActivityForResult(intent, REQUEST_REVIEW);
		}
		
        private void savePath()
        {
            SharedPreferences prefs=getActivity().getPreferences(Context.MODE_PRIVATE);

            SharedPreferences.Editor editor=prefs.edit();
            editor.putString(ApplicationPreferences.LAST_PATH, mAdapter.getCurrentPath());
            editor.commit();
        }

        private void loadPath()
        {
            SharedPreferences prefs=getActivity().getPreferences(Context.MODE_PRIVATE);

            String path=prefs.getString(ApplicationPreferences.LAST_PATH, "");

            if (!TextUtils.isEmpty(path))
            {
                mAdapter.setCurrentPath(path);
            }
        }
		
		private void saveLastFile(String fileName)
		{
			SharedPreferences prefs=getActivity().getPreferences(Context.MODE_PRIVATE);

            SharedPreferences.Editor editor=prefs.edit();
            editor.putString(ApplicationPreferences.LAST_FILE, fileName);
            editor.commit();
		}
		
		private void loadLastFile()
        {
            SharedPreferences prefs=getActivity().getPreferences(Context.MODE_PRIVATE);

            String fileName=prefs.getString(ApplicationPreferences.LAST_FILE, "");

            if (!TextUtils.isEmpty(fileName))
            {
                openFile(fileName);
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        if (mOnBackPressedListener==null || !mOnBackPressedListener.onBackPressed())
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

    public OnBackPressedListener getOnBackPressedListener()
    {
        return mOnBackPressedListener;
    }

    public void setOnBackPressedListener(OnBackPressedListener listener)
    {
        mOnBackPressedListener=listener;
    }
}
