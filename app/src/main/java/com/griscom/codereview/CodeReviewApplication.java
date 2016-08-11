package com.griscom.codereview;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * CodeReview application
 */
public class CodeReviewApplication extends Application
{
    @SuppressWarnings("unused")
    private static final String TAG = "CodeReviewApplication";



    private Tracker mTracker = null;



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
}