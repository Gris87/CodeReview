package com.griscom.codereview.activities;

import junit.framework.Assert;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.griscom.codereview.BuildConfig;
import com.griscom.codereview.R;
import com.griscom.codereview.listeners.OnNoteSupportListener;
import com.griscom.codereview.listeners.OnProgressChangedListener;
import com.griscom.codereview.other.ApplicationExtras;
import com.griscom.codereview.other.ApplicationSettings;
import com.griscom.codereview.other.ColorCache;
import com.griscom.codereview.other.SelectionColor;
import com.griscom.codereview.review.ReviewSurfaceView;

public class ReviewActivity extends FragmentActivity
{
    private static final String TAG = "ReviewActivity";



    public static final int RESULT_CLOSE = 1;



    private static final int REQUEST_SETTINGS       = 1;

    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;



    private PlaceholderFragment mPlaceholderFragment=null;



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
    protected void onStart()
    {
        super.onStart();

        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        EasyTracker.getInstance(this).activityStop(this);
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
            case R.id.action_reload:
            {
                if (mPlaceholderFragment!=null)
                {
                    mPlaceholderFragment.getContent().reload();
                }

                return true;
            }

            case R.id.action_settings:
            {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, REQUEST_SETTINGS);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_SETTINGS:
            {
                ColorCache.update(this);

                mPlaceholderFragment.getContent().setFontSize(ApplicationSettings.fontSize(this));
                mPlaceholderFragment.getContent().setTabSize (ApplicationSettings.tabSize(this));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public PlaceholderFragment getPlaceholderFragment()
    {
        return mPlaceholderFragment;
    }

    public void setPlaceholderFragment(PlaceholderFragment fragment)
    {
        mPlaceholderFragment=fragment;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements OnTouchListener, OnClickListener, OnNoteSupportListener, OnProgressChangedListener
    {
        private ReviewActivity    mActivity;
        private ReviewSurfaceView mContent;
        private View              mTitle;
        private TextView          mTitleTextView;
        private TextView          mProgressTextView;
        private View              mControls;
        private Button            mReviewedButton;
        private Button            mInvalidButton;
        private Button            mNoteButton;
        private Button            mClearButton;
        private String            mFileName;
        private boolean           mControlsVisible;
        private Button            mLastSelectedButton;
        private int               mDefaultColor;
        private int               mSelectedColor;
        private int               mHoverColor;

        public PlaceholderFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            mActivity=(ReviewActivity)getActivity();

            ColorCache.update(mActivity);

            mDefaultColor  = mActivity.getResources().getColor(R.color.black_overlay);
            mSelectedColor = mActivity.getResources().getColor(R.color.selected);
            mHoverColor    = mActivity.getResources().getColor(R.color.hover);

            // ---------------------------------------------------------------------------------------

            Intent intent=mActivity.getIntent();
            mFileName=intent.getStringExtra(ApplicationExtras.OPEN_FILE);



            View rootView=inflater.inflate(R.layout.fragment_review, container, false);

            mContent          = (ReviewSurfaceView)rootView.findViewById(R.id.fullscreen_content);
            mTitle            = rootView.findViewById(R.id.fullscreen_content_title);
            mTitleTextView    = (TextView)rootView.findViewById(R.id.titleTextView);
            mProgressTextView = (TextView)rootView.findViewById(R.id.progressTextView);
            mControls         = rootView.findViewById(R.id.fullscreen_content_controls);
            mReviewedButton   = (Button)rootView.findViewById(R.id.reviewed_button);
            mInvalidButton    = (Button)rootView.findViewById(R.id.invalid_button);
            mNoteButton       = (Button)rootView.findViewById(R.id.note_button);
            mClearButton      = (Button)rootView.findViewById(R.id.clear_button);

            // ---------------------------------------------------------------------------------------

            mContent.setFileName(mFileName);
            mContent.setOnTouchListener(this);
			mContent.setOnNoteSupportListener(this);
            mContent.setOnProgressChangedListener(this);

            mTitleTextView.setText(mFileName.substring(mFileName.lastIndexOf('/')+1));
            mProgressTextView.setText("0 %");



            Spannable reviewedIcon = new SpannableString(" ");
            Spannable invalidIcon  = new SpannableString(" ");
            Spannable noteIcon     = new SpannableString(" ");
            Spannable clearIcon    = new SpannableString(" ");

            reviewedIcon.setSpan(new ImageSpan(mActivity.getApplicationContext(), R.drawable.reviewed, ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            invalidIcon.setSpan (new ImageSpan(mActivity.getApplicationContext(), R.drawable.invalid,  ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            noteIcon.setSpan    (new ImageSpan(mActivity.getApplicationContext(), R.drawable.note,     ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            clearIcon.setSpan   (new ImageSpan(mActivity.getApplicationContext(), R.drawable.clear,    ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            mReviewedButton.setText(reviewedIcon);
            mInvalidButton.setText (invalidIcon);
            mNoteButton.setText    (noteIcon);
            mClearButton.setText   (clearIcon);

            mReviewedButton.setBackgroundColor(mDefaultColor);
            mInvalidButton.setBackgroundColor (mDefaultColor);
            mNoteButton.setBackgroundColor    (mDefaultColor);
            mClearButton.setBackgroundColor   (mDefaultColor);

            mLastSelectedButton=mReviewedButton;
            mLastSelectedButton.setBackgroundColor(mSelectedColor);

            mReviewedButton.setOnTouchListener(mHoverTouchListener);
            mInvalidButton.setOnTouchListener (mHoverTouchListener);
            mNoteButton.setOnTouchListener    (mHoverTouchListener);
            mClearButton.setOnTouchListener   (mHoverTouchListener);

            mReviewedButton.setOnClickListener(this);
            mInvalidButton.setOnClickListener (this);
            mNoteButton.setOnClickListener    (this);
            mClearButton.setOnClickListener   (this);

            // ---------------------------------------------------------------------------------------

            mControlsVisible=true;
            delayedHide(1000);

            mActivity.setPlaceholderFragment(this);

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
                showControls();
            }

            return mContent.onTouch(v, event);
        }

        @Override
        public void onClick(View v)
        {
            if (v==mReviewedButton)
            {
                mContent.setSelectionColor(SelectionColor.REVIEWED);

                mLastSelectedButton.setBackgroundColor(mDefaultColor);
                mLastSelectedButton=mReviewedButton;
                mLastSelectedButton.setBackgroundColor(mSelectedColor);
            }
            else
            if (v==mInvalidButton)
            {
                mContent.setSelectionColor(SelectionColor.INVALID);

                mLastSelectedButton.setBackgroundColor(mDefaultColor);
                mLastSelectedButton=mInvalidButton;
                mLastSelectedButton.setBackgroundColor(mSelectedColor);
            }
            else
            if (v==mNoteButton)
            {
                mContent.setSelectionColor(SelectionColor.NOTE);

                mLastSelectedButton.setBackgroundColor(mDefaultColor);
                mLastSelectedButton=mNoteButton;
                mLastSelectedButton.setBackgroundColor(mSelectedColor);
            }
            else
            if (v==mClearButton)
            {
                mContent.setSelectionColor(SelectionColor.CLEAR);

                mLastSelectedButton.setBackgroundColor(mDefaultColor);
                mLastSelectedButton=mClearButton;
                mLastSelectedButton.setBackgroundColor(mSelectedColor);
            }
            else
            {
                Log.e(TAG, "Unknown onClick receiver: "+String.valueOf(v));

                if (BuildConfig.DEBUG)
                {
                    Assert.fail();
                }
            }
        }

		@Override
		public void onNoteSupport(boolean noteSupported)
		{
			if (noteSupported)
			{
				mNoteButton.setVisibility(View.VISIBLE);
			}
			else
			{
				mNoteButton.setVisibility(View.GONE);
			}
		}

        @Override
        public void onProgressChanged(int progress)
        {
            if (BuildConfig.DEBUG)
            {
                Assert.assertTrue(progress>=0 && progress<=100);
            }

            mProgressTextView.setText(String.valueOf(progress)+" %");
        }

        View.OnTouchListener mHoverTouchListener = new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction()==MotionEvent.ACTION_DOWN)
                {
                    v.setBackgroundColor(mHoverColor);
                }
                else
                if (event.getAction()==MotionEvent.ACTION_UP)
                {
                    float x=event.getX();
                    float y=event.getY();

                    if (x<0 || x>v.getWidth() || y<0 || y>v.getHeight())
                    {
                        if (v==mLastSelectedButton)
                        {
                            v.setBackgroundColor(mSelectedColor);
                        }
                        else
                        {
                            v.setBackgroundColor(mDefaultColor);
                        }
                    }
                }

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

                mTitle.setVisibility   (View.GONE);
                mControls.setVisibility(View.GONE);
            }
        }

        private void showControls()
        {
            if (!mControlsVisible)
            {
                mControlsVisible=true;

                mTitle.setVisibility   (View.VISIBLE);
                mControls.setVisibility(View.VISIBLE);
            }

            delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }

        public ReviewSurfaceView getContent()
        {
            return mContent;
        }
    }
}
