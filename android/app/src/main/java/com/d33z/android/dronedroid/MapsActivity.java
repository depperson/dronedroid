package com.d33z.android.dronedroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;


public class MapsActivity extends FragmentActivity {

    public static GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public static TextView mDebugTextView;
    public static Button mGoButton;

    // modules
    public LocationManager      myLocationManager;
    public static Sensing       mSensing;
    public static Navigation    mNavigation;
    public static Webserver     mWebserver;

    // in preferences
    public boolean location_trace;

    final String tag = "MapsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(tag, "onCreate called");
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        // keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // catch uncaught exceptions
        final Thread.UncaughtExceptionHandler oldUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {

                writeLogcat();

                // re-throw the exception
                oldUncaughtExceptionHandler.uncaughtException(thread, throwable);
            }
        });

        // get preferences
        updatePreferences();

        // use the Android location manager
        myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // start sensing
        mSensing = new Sensing(this);
        mSensing.init();

        // start navigation
        mNavigation = new Navigation(this);

        // start webserver
        try {
            mWebserver = new Webserver();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // on-screen debug TextView
        mDebugTextView = (TextView) findViewById(R.id.text);

        mGoButton = (Button) findViewById(R.id.go_btn);
    }


    @Override
    protected void onResume() {

        super.onResume();
        Log.i(tag, "onResume called");

        setUpMapIfNeeded();

        // start the ioio thread
        startService(new Intent(this, Mechanical.class));

        // start the web server
        try {
            mWebserver.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // start the navigator
        Handler handler = new Handler();
        handler.postDelayed(mNavigation.runnable, 100);

        // request location updates
        myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,1,mNavigation);

        drawEditableWaypoints();
    }


    @Override
    protected void onPause()
    {
        super.onPause();
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.i(tag, "onDestroy called");

        // stop the ioio service
        stopService(new Intent(this, Mechanical.class));

        // stop the navigator
        mNavigation.nav_abort = true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(tag, "onActivityResult() called");

        // update the navigator first
        mNavigation.updatePreferences();

        // then update the activity preferences (and re-draw the waypoints)
        updatePreferences();

        // restart the ioio service
        stopService(new Intent(this, Mechanical.class));
        startService(new Intent(this, Mechanical.class));
    }


    /**
     * Write logcat output to /sdcard.
     */
    public void writeLogcat()
    {
        // run logcat and save to sdcard
        String[] cmd = new String[]{"logcat", "-f", "/sdcard/logcat.txt", "-v", "long", "time"};
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Get or update preferences values.
     */
    public void updatePreferences()
    {
        Log.d(tag, "updatePreferences() called");
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        location_trace  = SP.getBoolean("locationtrace", false);
        if (null != mMap) { mMap.clear(); drawEditableWaypoints(); }
    }


    /**
     * Draw the editable waypoints on the map, if possible.
     */
    private void drawEditableWaypoints()
    {
        if ((null != mNavigation) && (null != mMap))
        {
            // do nothing when location trace is active
            if (!location_trace)
            {
                // draw the waypoint list
                mMap.clear();
                for (int i = 0; i < mNavigation.waypoint_list.size(); i++)
                {
                    mMap.addMarker(new MarkerOptions()
                                    .position(mNavigation.waypoint_list.get(i))
                                    .title("" + i)
                                    .draggable(true)
                    );
                }
            } // end location_trace if
        } // end mNavigation null check
    } // end drawEditableWaypoints


    /**
     * Sets up the map if it is possible to do so.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }

        if (mMap != null) {
            setUpMap();
        } else {
            Log.e(tag, "failed to acquire the Map object");
        }

    }


    /**
     * This should only be called when we are sure that {@link #mMap} is not null.
     *
     */
    private void setUpMap()
    {
        Log.d(tag, "setUpMap() called");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.16, -97.74), 17));
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMyLocationEnabled(true);

        // handle waypoint editing
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker arg0) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

                // TODO: move to Navigation.java

                // save the waypoint to memory
                mNavigation.waypoint_list.set(Integer.parseInt(marker.getTitle()), marker.getPosition());

                // notify the user
                Context context = getApplicationContext();
                CharSequence text = "waypoint " + marker.getTitle() + " saved";
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

                // save waypoint_list to file on sdcard
                mNavigation.saveWaypoints();

                drawEditableWaypoints();
            }
        });
    }


    /**
     * Supports the "GO!" button on the screen.
     * @param view is the pressed button
     */
    public void go_button_pressed(View view)
    {
        CharSequence text;
        Button b = (Button) view;

        if (mNavigation.nav_active)
        {
            text = "navigation stopped";
            b.setText("start");
            mNavigation.nav_active = false;
            mNavigation.waypoint_idx = 0;
            drawEditableWaypoints();
            writeLogcat();

        } else {

            text = "navigation started";
            b.setText("stop");
            mNavigation.nav_active = true;
        }

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();

    }


    /**
     * Supports the "preferences" button on the screen by launching the PreferencesActivity activity.
     * This button is ignored if navigation is active.
     * @param view is the pressed button
     */
    public void prefs_button_pressed(View view)
    {
        // do nothing while navigation is active
        if (!mNavigation.nav_active)
        {
            startActivityForResult(new Intent(this, PreferencesActivity.class), 1);
        }
    }


    /**
     * Supports the "+" button on the screen to add a new waypoint to the edit map. This button
     * is ignored if navigation is active.
     * @param view is the pressed button
     */
    public void add_button_pressed(View view)
    {
        // do nothing while navigation is active
        if (!mNavigation.nav_active)
        {
            int next_waypoint_idx = mNavigation.waypoint_list.size();

            // TODO: test me
            LatLng new_waypoint = mMap.getCameraPosition().target;

            mNavigation.waypoint_list.add(next_waypoint_idx, new_waypoint);
            drawEditableWaypoints();

            // notify the user
            Context context = getApplicationContext();
            CharSequence text = "waypoint " + next_waypoint_idx + " added";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();
        }
    }


    /**
     * Supports the "-" button on the screen to add a delete a waypoint from the edit map. Ignored
     * if navigation is active.
     *
     * @param view is the pressed button
     */
    public void del_button_pressed(View view)
    {
        // do nothing while navigation is active
        if (!mNavigation.nav_active)
        {
            // don't remove waypoint 0
            int last_waypoint_idx = mNavigation.waypoint_list.size() - 1;
            if (last_waypoint_idx != 0) {

                // remove the last waypoint
                mNavigation.waypoint_list.remove(last_waypoint_idx);

                drawEditableWaypoints();

                // notify the user
                Context context = getApplicationContext();
                CharSequence text = "waypoint " + last_waypoint_idx + " deleted";
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, text, duration).show();

                mNavigation.saveWaypoints();
            }
        } // end nav_active if
    } // end del_button_pressed

}
