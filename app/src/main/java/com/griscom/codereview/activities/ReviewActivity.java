package com.griscom.codereview.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

import com.griscom.codereview.BuildConfig;
import com.griscom.codereview.R;
import com.griscom.codereview.listeners.OnNoteSupportListener;
import com.griscom.codereview.listeners.OnProgressChangedListener;
import com.griscom.codereview.other.ApplicationExtras;
import com.griscom.codereview.other.ApplicationSettings;
import com.griscom.codereview.other.SelectionType;
import com.griscom.codereview.review.ReviewSurfaceView;

import junit.framework.Assert;

/**
 * Activity for performing code review
 */
public class ReviewActivity extends FragmentActivity implements OnTouchListener, OnClickListener, OnNoteSupportListener, OnProgressChangedListener
{
    @SuppressWarnings("unused")
    private static final String TAG = "ReviewActivity";



    public static final int RESULT_CLOSE = 1;



    private static final int REQUEST_SETTINGS       = 1;

    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;



    private ReviewSurfaceView mContent            = null;
    private View              mTitle              = null;
    private TextView          mProgressTextView   = null;
    private View              mControls           = null;
    private Button            mReviewedButton     = null;
    private Button            mInvalidButton      = null;
    private Button            mNoteButton         = null;
    private Button            mClearButton        = null;
    private boolean           mControlsVisible    = false;
    private Button            mLastSelectedButton = null;
    private int               mDefaultColor       = -1;
    private int               mSelectedColor      = -1;
    private int               mHoverColor         = -1;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);



        mContent               = (ReviewSurfaceView)findViewById(R.id.fullScreenContent);
        mTitle                 =                    findViewById(R.id.fullScreenContentTitle);
        TextView titleTextView = (TextView)         findViewById(R.id.titleTextView);
        mProgressTextView      = (TextView)         findViewById(R.id.progressTextView);
        mControls              =                    findViewById(R.id.fullScreenContentControls);
        mReviewedButton        = (Button)           findViewById(R.id.reviewedButton);
        mInvalidButton         = (Button)           findViewById(R.id.invalidButton);
        mNoteButton            = (Button)           findViewById(R.id.noteButton);
        mClearButton           = (Button)           findViewById(R.id.clearButton);



        Resources resources = getResources();

        //noinspection deprecation
        mDefaultColor  = resources.getColor(R.color.black_overlay);

        //noinspection deprecation
        mSelectedColor = resources.getColor(R.color.selected);

        //noinspection deprecation
        mHoverColor    = resources.getColor(R.color.hover);



        Intent intent = getIntent();

        String filePath = intent.getStringExtra(ApplicationExtras.FILE_PATH);
        int    fileId   = intent.getIntExtra   (ApplicationExtras.FILE_ID, 0);



        mContent.setFilePath(filePath, fileId);
        mContent.setOnTouchListener(this);
        mContent.setOnNoteSupportListener(this);
        mContent.setOnProgressChangedListener(this);

        titleTextView.setText(filePath.substring(filePath.lastIndexOf('/') + 1));
        mProgressTextView.setText("0 %");



        Spannable reviewedIcon = new SpannableString(" ");
        Spannable invalidIcon  = new SpannableString(" ");
        Spannable noteIcon     = new SpannableString(" ");
        Spannable clearIcon    = new SpannableString(" ");

        reviewedIcon.setSpan(new ImageSpan(this, R.drawable.reviewed, ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        invalidIcon.setSpan (new ImageSpan(this, R.drawable.invalid,  ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        noteIcon.setSpan    (new ImageSpan(this, R.drawable.note,     ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        clearIcon.setSpan   (new ImageSpan(this, R.drawable.clear,    ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mReviewedButton.setText(reviewedIcon);
        mInvalidButton.setText (invalidIcon);
        mNoteButton.setText    (noteIcon);
        mClearButton.setText   (clearIcon);

        mReviewedButton.setBackgroundColor(mDefaultColor);
        mInvalidButton.setBackgroundColor (mDefaultColor);
        mNoteButton.setBackgroundColor    (mDefaultColor);
        mClearButton.setBackgroundColor   (mDefaultColor);

        mLastSelectedButton = mReviewedButton;
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

        mControlsVisible = true;
        delayedHide(1000);
    }

    @Override
    protected void onDestroy()
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_review, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_reload:
            {
                mContent.reload();

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
                setResult(RESULT_CLOSE, null);
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
                ApplicationSettings.update(this);

                mContent.setFontSize(ApplicationSettings.getFontSize());
                mContent.setTabSize (ApplicationSettings.getTabSize());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            showControls();
        }

        return mContent.onTouch(v, event);
    }

    @Override
    public void onClick(View v)
    {
        if (v == mReviewedButton)
        {
            mContent.setSelectionType(SelectionType.REVIEWED);

            mLastSelectedButton.setBackgroundColor(mDefaultColor);
            mLastSelectedButton = mReviewedButton;
            mLastSelectedButton.setBackgroundColor(mSelectedColor);
        }
        else
        if (v == mInvalidButton)
        {
            mContent.setSelectionType(SelectionType.INVALID);

            mLastSelectedButton.setBackgroundColor(mDefaultColor);
            mLastSelectedButton = mInvalidButton;
            mLastSelectedButton.setBackgroundColor(mSelectedColor);
        }
        else
        if (v == mNoteButton)
        {
            mContent.setSelectionType(SelectionType.NOTE);

            mLastSelectedButton.setBackgroundColor(mDefaultColor);
            mLastSelectedButton = mNoteButton;
            mLastSelectedButton.setBackgroundColor(mSelectedColor);
        }
        else
        if (v == mClearButton)
        {
            mContent.setSelectionType(SelectionType.CLEAR);

            mLastSelectedButton.setBackgroundColor(mDefaultColor);
            mLastSelectedButton = mClearButton;
            mLastSelectedButton.setBackgroundColor(mSelectedColor);
        }
        else
        {
            Log.e(TAG, "Unknown onClick receiver: " + String.valueOf(v));

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
        Assert.assertTrue(progress >= 0 && progress <= 100);

        mProgressTextView.setText(getString(R.string.progress, progress));
    }

    View.OnTouchListener mHoverTouchListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                v.setBackgroundColor(mHoverColor);
            }
            else
            if (event.getAction() == MotionEvent.ACTION_UP)
            {
                float x = event.getX();
                float y = event.getY();

                if (x < 0 || x > v.getWidth() || y < 0 || y > v.getHeight())
                {
                    if (v == mLastSelectedButton)
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
     *
     * @param delayMillis   Delay in milliseconds
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
            mControlsVisible = false;

            mTitle.setVisibility   (View.GONE);
            mControls.setVisibility(View.GONE);
        }
    }

    private void showControls()
    {
        if (!mControlsVisible)
        {
            mControlsVisible = true;

            mTitle.setVisibility   (View.VISIBLE);
            mControls.setVisibility(View.VISIBLE);
        }

        delayedHide(AUTO_HIDE_DELAY_MILLIS);
    }
}
