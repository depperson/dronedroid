package com.d33z.android.dronedroid;

import android.test.AndroidTestCase;

/**
 * Created by epperson on 8/17/15.
 */
public class NavigationTests extends AndroidTestCase {
    public void testConstructor()
    {
        mContext = getContext();
        Navigation mNavigation = new Navigation(mContext);
        assertNotNull(mNavigation);

        mNavigation.runnable.run();
    }
}
