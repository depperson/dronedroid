package com.d33z.android.dronedroid;

import android.test.ActivityInstrumentationTestCase2;

/**
 * Created by epperson on 8/13/15.
 */
public class MapsActivityTests extends ActivityInstrumentationTestCase2<MapsActivity>
{
    MapsActivity activity;

    public MapsActivityTests() {
        super(MapsActivity.class);
    }

    //public MapsActivityTests(Class<MapsActivity> activityClass) {
    //    super(activityClass);
    //}

    public void testActivityStarts()
    {
        activity = getActivity();
        assertNotNull("Activity is not null", activity);

        // TODO: why doesn't this work in testPreferences()?
        assertNotNull("Preference boolean location_trace is not null", activity.location_trace);
    }

    public void testMap()
    {
        assertNotNull("Map is not null", activity.mMap);
    }

    public void testPreferences()
    {
        // TODO: add tests
    }

}
