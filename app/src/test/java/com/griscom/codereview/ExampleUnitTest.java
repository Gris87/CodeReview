package com.griscom.codereview;

import org.junit.Assert;
import org.junit.Test;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@SuppressWarnings({"PublicConstructor", "JUnitTestNG", "ClassWithoutConstructor"})
public class ExampleUnitTest
{
    @SuppressWarnings("unused")
    private static final String TAG = "ExampleUnitTest";



    @Test
    public void addition_isCorrect() throws Exception
    {
        Assert.assertEquals("WTF", 4, 2 + 2);
    }
}
