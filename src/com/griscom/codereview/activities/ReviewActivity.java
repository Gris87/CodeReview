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
import android.os.*;

public class ReviewActivity extends FragmentActivity
{
    public static final int RESULT_CLOSE=1;
	
	private static final int AUTO_HIDE_DELAY_MILLIS=3000;
	
	

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
		private View              mControls;
        private String            mFileName;
		private boolean           mControlsVisible;

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

            mContent  = (ReviewSurfaceView)rootView.findViewById(R.id.fullscreen_content);
			mControls = rootView.findViewById(R.id.fullscreen_content_controls);

            mContent.setFileName(mFileName);
            mContent.setOnTouchListener(this);
			
			// TODO; Looks bad
			rootView.findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
			
			mControlsVisible=true;
			delayedHide(1000);

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
			if (event.getAction()==MotionEvent.ACTION_DOWN)
			{
				if (mControlsVisible)
				{
					hideControls();
				}
				else
				{
					showControls();
				}
			}
			
            return mContent.onTouch(v, event);
        }
		
		View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent)
			{
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
				
				return false;
			}
		};

		Handler mHideHandler   = new Handler();
		Runnable mHideRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				hideControls();
			}
		};
	
        /**
         * Schedules a call to hide() in [delay] milliseconds, canceling any
         * previously scheduled calls.
         */
        private void delayedHide(int delayMillis)
		{
			mHideHandler.removeCallbacks(mHideRunnable);
			mHideHandler.postDelayed(mHideRunnable, delayMillis);
		}
		
		private void hideControls()
		{
			if (mControlsVisible)
			{
				mControlsVisible=false;
				
				mControls.setVisibility(View.GONE);
			}
		}
		
		private void showControls()
		{
			if (!mControlsVisible)
			{
				mControlsVisible=true;
				
				mControls.setVisibility(View.VISIBLE);
				
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
		}
    }
}
