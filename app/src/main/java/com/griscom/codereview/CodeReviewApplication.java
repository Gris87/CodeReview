package com.griscom.codereview;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.griscom.codereview.other.ApplicationSettings;

/**
 * CodeReview application
 */
@SuppressWarnings({"ClassWithoutConstructor", "PublicConstructor"})
public class CodeReviewApplication extends Application
{
    @SuppressWarnings("unused")
    private static final String TAG = "CodeReviewApplication";



    private Tracker mTracker = null;



    /** {@inheritDoc} */
    @Override
    public void onCreate()
    {
        super.onCreate();

        ApplicationSettings.update(this);
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return Google Analytics tracker
     */
    public Tracker getDefaultTracker()
    {
        if (mTracker == null)
        {
            mTracker = GoogleAnalytics.getInstance(this).newTracker(R.xml.tracker_config);
        }

        return mTracker;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "CodeReviewApplication{" +
                "mTracker=" + mTracker +
                '}';
    }
}
