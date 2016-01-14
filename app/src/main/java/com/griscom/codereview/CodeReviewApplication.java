package com.griscom.codereview;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class CodeReviewApplication extends Application
{
    private Tracker mTracker = null;



    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return  Google Analytics tracker
     */
    synchronized public Tracker getDefaultTracker()
    {
        if (mTracker == null)
        {
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = GoogleAnalytics.getInstance(this).newTracker(R.xml.global_tracker);
        }

        return mTracker;
    }
}