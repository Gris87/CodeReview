package com.griscom.codereview.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.griscom.codereview.R;
import com.griscom.codereview.other.ApplicationExtras;
import com.griscom.codereview.review.ReviewSurfaceView;

public class ReviewActivity extends FragmentActivity
{
    public static final int RESULT_CLOSE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

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
        getMenuInflater().inflate(R.menu.menu_review, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.action_settings:
            {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

                return true;
            }

            case R.id.action_close:
            {
                Intent data=new Intent();

                setResult(RESULT_CLOSE, data);
                finish();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements OnTouchListener
    {
        private ReviewActivity    mActivity;
        private ReviewSurfaceView mContent;
        private String            mFileName;

        public PlaceholderFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            mActivity=(ReviewActivity)getActivity();

            Intent intent=mActivity.getIntent();
            mFileName=intent.getStringExtra(ApplicationExtras.OPEN_FILE);

            View rootView=inflater.inflate(R.layout.fragment_review, container, false);

            mContent = (ReviewSurfaceView)rootView.findViewById(R.id.fullscreen_content);

            mContent.setFileName(mFileName);
            mContent.setOnTouchListener(this);

            return rootView;
        }

        @Override
        public void onDestroy()
        {
            super.onDestroy();

            mContent.onDestroy();
        }

        @Override
        public void onPause()
        {
            super.onPause();

            mContent.onPause();
        }

        @Override
        public void onResume()
        {
            super.onResume();

            mContent.onResume();
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig)
        {
            super.onConfigurationChanged(newConfig);

            mContent.onConfigurationChanged(newConfig);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            return mContent.onTouch(v, event);
        }
    }
}
