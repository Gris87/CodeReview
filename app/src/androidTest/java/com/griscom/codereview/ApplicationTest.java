package com.griscom.codereview;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@SuppressWarnings("PublicConstructor")
public class ApplicationTest extends ApplicationTestCase<Application>
{
    @SuppressWarnings("unused")
    private static final String TAG = "ApplicationTest";



    public ApplicationTest()
    {
        super(Application.class);
    }
}
