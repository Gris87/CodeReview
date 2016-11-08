package com.griscom.codereview.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.griscom.codereview.CodeReviewApplication;
import com.griscom.codereview.R;
import com.griscom.codereview.dialogs.CommentDialog;
import com.griscom.codereview.dialogs.HighlightSyntaxDialog;
import com.griscom.codereview.listeners.OnCommentDialogRequestedListener;
import com.griscom.codereview.listeners.OnFileNoteLoadedListener;
import com.griscom.codereview.listeners.OnNoteSupportListener;
import com.griscom.codereview.listeners.OnProgressChangedListener;
import com.griscom.codereview.other.ApplicationExtras;
import com.griscom.codereview.other.ApplicationSettings;
import com.griscom.codereview.other.SelectionType;
import com.griscom.codereview.other.SyntaxParserType;
import com.griscom.codereview.review.ReviewSurfaceView;
import com.griscom.codereview.util.AppLog;

import junit.framework.Assert;

/**
 * Activity for performing code review
 */
@SuppressWarnings({"ClassWithoutConstructor", "PublicConstructor"})
public class ReviewActivity extends FragmentActivity implements OnTouchListener, OnClickListener, OnNoteSupportListener, OnFileNoteLoadedListener, OnProgressChangedListener, OnCommentDialogRequestedListener, CommentDialog.OnFragmentInteractionListener, HighlightSyntaxDialog.OnFragmentInteractionListener
{
    @SuppressWarnings("unused")
    private static final String TAG = "ReviewActivity";



    public static final int RESULT_CLOSE = RESULT_FIRST_USER;



    private static final int REQUEST_SETTINGS       = 1;

    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;



    private ReviewSurfaceView mContent            = null;
    private View              mTitle              = null;
    private TextView          mNoteTextView       = null;
    private TextView          mProgressTextView   = null;
    private View              mControls           = null;
    private ImageButton       mReviewedButton     = null;
    private ImageButton       mInvalidButton      = null;
    private ImageButton       mNoteButton         = null;
    private ImageButton       mClearButton        = null;
    private Tracker           mTracker            = null;
    private boolean           mControlsVisible    = false;
    private ImageButton       mLastSelectedButton = null;
    private int               mDefaultColor       = -1;
    private int               mSelectedColor      = -1;
    private int               mHoverColor         = -1;



    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ReviewActivity{" +
                "mContent="              + mContent            +
                ", mTitle="              + mTitle              +
                ", mNoteTextView="       + mNoteTextView       +
                ", mProgressTextView="   + mProgressTextView   +
                ", mControls="           + mControls           +
                ", mReviewedButton="     + mReviewedButton     +
                ", mInvalidButton="      + mInvalidButton      +
                ", mNoteButton="         + mNoteButton         +
                ", mClearButton="        + mClearButton        +
                ", mTracker="            + mTracker            +
                ", mControlsVisible="    + mControlsVisible    +
                ", mLastSelectedButton=" + mLastSelectedButton +
                ", mDefaultColor="       + mDefaultColor       +
                ", mSelectedColor="      + mSelectedColor      +
                ", mHoverColor="         + mHoverColor         +
                ", mHoverTouchListener=" + mHoverTouchListener +
                ", mHideHandler="        + mHideHandler        +
                ", mHideRunnable="       + mHideRunnable       +
                '}';
    }

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);



        mContent               = (ReviewSurfaceView)findViewById(R.id.fullScreenContent);
        mTitle                 =                    findViewById(R.id.fullScreenContentTitle);
        mNoteTextView          = (TextView)         findViewById(R.id.noteTextView);
        TextView titleTextView = (TextView)         findViewById(R.id.titleTextView);
        mProgressTextView      = (TextView)         findViewById(R.id.progressTextView);
        mControls              =                    findViewById(R.id.fullScreenContentControls);
        mReviewedButton        = (ImageButton)      findViewById(R.id.reviewedButton);
        mInvalidButton         = (ImageButton)      findViewById(R.id.invalidButton);
        mNoteButton            = (ImageButton)      findViewById(R.id.noteButton);
        mClearButton           = (ImageButton)      findViewById(R.id.clearButton);



        mTracker = ((CodeReviewApplication)getApplication()).getDefaultTracker();



        Resources resources = getResources();

        //noinspection deprecation
        mDefaultColor  = resources.getColor(R.color.black_overlay);

        //noinspection deprecation
        mSelectedColor = resources.getColor(R.color.selected);

        //noinspection deprecation
        mHoverColor    = resources.getColor(R.color.hover);



        Intent intent = getIntent();

        String filePath = intent.getStringExtra(ApplicationExtras.FILE_PATH);
        long   fileId   = intent.getLongExtra  (ApplicationExtras.FILE_ID, 0);
        String fileNote = intent.getStringExtra(ApplicationExtras.FILE_NOTE);



        if (!TextUtils.isEmpty(fileNote))
        {
            mNoteTextView.setVisibility(View.VISIBLE);
            mNoteTextView.setText(fileNote);
        }
        else
        {
            mNoteTextView.setVisibility(View.GONE);
        }

        titleTextView.setText(filePath.substring(filePath.lastIndexOf('/') + 1));
        mProgressTextView.setText("0 %");



        mContent.setFilePath(filePath);
        mContent.setFileId(fileId);
        mContent.setSyntaxParserType(SyntaxParserType.AUTOMATIC);
        mContent.setOnTouchListener(this);
        mContent.setOnNoteSupportListener(this);
        mContent.setOnFileNoteLoadedListener(this);
        mContent.setOnProgressChangedListener(this);
        mContent.setOnCommentDialogRequestedListener(this);



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

    /** {@inheritDoc} */
    @Override
    public void onPause()
    {
        super.onPause();

        mContent.onPause();
    }

    /** {@inheritDoc} */
    @Override
    public void onResume()
    {
        super.onResume();

        mContent.onResume();
    }

    /** {@inheritDoc} */
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        mContent.onConfigurationChanged(newConfig);
    }

    /** {@inheritDoc} */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_review, menu);

        return true;
    }

    /** {@inheritDoc} */
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

            case R.id.action_highlight_syntax:
            {
                HighlightSyntaxDialog dialog = HighlightSyntaxDialog.newInstance(mContent.getSyntaxParserType());
                dialog.show(getSupportFragmentManager(), "HighlightSyntaxDialog");

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
                mTracker.send(
                        new HitBuilders.EventBuilder()
                                .setCategory("Action")
                                .setAction("Exit")
                                .build()
                );

                setResult(RESULT_CLOSE, null);
                finish();

                return true;
            }

            default:
            {
                AppLog.wtf(TAG, "Unknown action ID: " + item);
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    /** {@inheritDoc} */
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
            break;

            default:
            {
                AppLog.wtf(TAG, "Unexpected request code: " + requestCode);
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /** {@inheritDoc} */
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            showControls();
        }

        return mContent.onTouch(v, event);
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(View view)
    {
        if (view == mReviewedButton)
        {
            mContent.setSelectionType(SelectionType.REVIEWED);

            mLastSelectedButton.setBackgroundColor(mDefaultColor);
            mLastSelectedButton = mReviewedButton;
            mLastSelectedButton.setBackgroundColor(mSelectedColor);
        }
        else
        if (view == mInvalidButton)
        {
            mContent.setSelectionType(SelectionType.INVALID);

            mLastSelectedButton.setBackgroundColor(mDefaultColor);
            mLastSelectedButton = mInvalidButton;
            mLastSelectedButton.setBackgroundColor(mSelectedColor);
        }
        else
        if (view == mNoteButton)
        {
            mContent.setSelectionType(SelectionType.NOTE);

            mLastSelectedButton.setBackgroundColor(mDefaultColor);
            mLastSelectedButton = mNoteButton;
            mLastSelectedButton.setBackgroundColor(mSelectedColor);
        }
        else
        if (view == mClearButton)
        {
            mContent.setSelectionType(SelectionType.CLEAR);

            mLastSelectedButton.setBackgroundColor(mDefaultColor);
            mLastSelectedButton = mClearButton;
            mLastSelectedButton.setBackgroundColor(mSelectedColor);
        }
        else
        {
            AppLog.wtf(TAG, "Unknown onClick receiver: " + view);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onNoteSupport(int noteSupported)
    {
        if (noteSupported == OnNoteSupportListener.SUPPORTED)
        {
            mNoteButton.setVisibility(View.VISIBLE);
        }
        else
        {
            mNoteButton.setVisibility(View.GONE);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onFileNoteLoaded(String note)
    {
        mNoteTextView.setVisibility(View.VISIBLE);
        mNoteTextView.setText(note);
    }

    /** {@inheritDoc} */
    @Override
    public void onProgressChanged(int progress)
    {
        Assert.assertTrue("progress is out of bounds", progress >= 0 && progress <= 100);

        mProgressTextView.setText(getString(R.string.progress, progress));
    }

    /** {@inheritDoc} */
    @Override
    public void onCommentDialogRequested(int firstRow, int lastRow, String comment)
    {
        CommentDialog dialog = CommentDialog.newInstance(firstRow, lastRow, comment);
        dialog.show(getSupportFragmentManager(), "CommentDialog");
    }

    /** {@inheritDoc} */
    @Override
    public void onCommentEntered(int firstRow, int lastRow, String comment)
    {
        mContent.onCommentEntered(firstRow, lastRow, comment);
    }

    /** {@inheritDoc} */
    @Override
    public void onCommentCanceled()
    {
        mContent.onCommentCanceled();
    }

    /** {@inheritDoc} */
    @Override
    public void onSyntaxParserTypeSelected(int syntaxParserType)
    {
        mContent.setSyntaxParserType(syntaxParserType);
        mContent.forceReload();
    }

    private final View.OnTouchListener mHoverTouchListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View view, MotionEvent event)
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                view.setBackgroundColor(mHoverColor);
            }
            else
            if (event.getAction() == MotionEvent.ACTION_UP)
            {
                float x = event.getX();
                float y = event.getY();

                if (x < 0 || x > view.getWidth() || y < 0 || y > view.getHeight())
                {
                    if (view == mLastSelectedButton)
                    {
                        view.setBackgroundColor(mSelectedColor);
                    }
                    else
                    {
                        view.setBackgroundColor(mDefaultColor);
                    }
                }
            }

            delayedHide(AUTO_HIDE_DELAY_MILLIS);

            return false;
        }
    };

    private final Handler mHideHandler   = new Handler();
    private final Runnable mHideRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            hideControls();
        }
    };

    /**
     * Schedules a call to hideControls() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     * @param delayMillis   delay in milliseconds
     */
    private void delayedHide(int delayMillis)
    {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Hides controls
     */
    private void hideControls()
    {
        if (mControlsVisible)
        {
            mControlsVisible = false;

            mTitle.setVisibility   (View.GONE);
            mControls.setVisibility(View.GONE);
        }
    }

    /**
     * Shows controls
     */
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
