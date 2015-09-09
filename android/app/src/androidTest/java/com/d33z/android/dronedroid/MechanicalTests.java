package com.d33z.android.dronedroid;

import android.content.Intent;
import android.test.ServiceTestCase;

/**
 * This class tests the class Mechanical, which is an IOIOService.
 *
 * Thanks:
 *
 * Daniel Epperson, 2015-08-16
 *
 */
public class MechanicalTests extends ServiceTestCase<Mechanical> {

    public MechanicalTests() {
        super(Mechanical.class);
    }


    public void testStartable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), Mechanical.class);
        startService(startIntent);
        Mechanical mMechanical = getService();
        assertNotNull("mMechanical is not null", mMechanical);
    }
}
