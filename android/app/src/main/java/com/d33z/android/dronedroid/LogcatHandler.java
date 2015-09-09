package com.d33z.android.dronedroid;

import java.io.IOException;

/**
 * Not implemented yet.
 * This Runnable takes the most recent logcat messages and stores them on the sdcard.
 *
 * Created by epperson on 8/20/15.
 */
public class LogcatHandler
{
    public Runnable runnable = new Runnable() {
        @Override
        public void run()
        {
            String[] cmd = new String[]{"logcat", "-f", "/sdcard/logcat.txt", "-v", "long", "time"};
            try {
                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // launch this function again
            // handler.postDelayed(this, 100);
        }
    };
}
