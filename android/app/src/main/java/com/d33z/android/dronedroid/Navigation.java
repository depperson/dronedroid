package com.d33z.android.dronedroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;


/**
 * This class handles navigation functions but no direct control happens here. The Mechanical
 * class handles i/o via the IOIO. Here, waypoints are stored in a list for use during navigation
 * and the current location and heading are made public for external consumption.
 *
 * A Handler runs every 200ms to perform the navigation tasks.
 *
 * Thanks:
 * https://www.mathsisfun.com/algebra/trig-sin-cos-tan-graphs.html
 *
 * Daniel Epperson, 2015-08-17
 */
public class Navigation implements LocationListener {

    private Handler     handler;
    public  double      heading_current;
    public  double      heading_desired;
    public  double      heading_error;
    private Context     mContext;
    public  boolean     nav_abort = false;
    public  boolean     nav_active = false;
    public  Location    nav_location;
    public  Location    nav_location_target;
    public  List<LatLng> waypoint_list;
    public  int         waypoint_idx = 0;

    // in preferences
    private File    waypoints_file;
    private float   waypoint_proximity,
                    nav_accuracy;
    private boolean location_trace;

    final   String  tag = "Navigation";


    // runs once on creation
    public Navigation(Context context)
    {
        mContext = context;
        waypoint_list = new ArrayList<LatLng>();

        // get preferences and waypoints
        updatePreferences();

        // perform navigation in an async handler
        handler = new Handler();
        handler.postDelayed(runnable, 200);

    } // end Navigation constructor


    // handles the navigating
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (nav_active) {

                // don't use fused sendor data, it was very laggy
                double val = MapsActivity.mSensing.accMagOrientation[0];

                // map current heading to 0 to 360
                heading_current = (360 + ((val) * 60)) % 360;

                // getting this working was quite the challenge
                double hdg_err_deg = ((360 + (heading_desired - heading_current)) % 360);

                // store heading_error as -1 to 1, where 0 is on target
                heading_error = Math.sin(Math.toRadians(heading_desired - heading_current));

                // trim some of the sinewave to square after 90 degrees either way
                if (hdg_err_deg > 90  && hdg_err_deg <= 180) heading_error = 1;
                if (hdg_err_deg > 180 && hdg_err_deg <= 270) heading_error = -1;

                Log.d("bearing"," desired " + String.format("%.2f", heading_desired) +
                                " current " + String.format("%.2f", heading_current) +
                                " error "   + String.format("%.2f", heading_error));

                // TODO: acceleration isn't right yet
                double acc_z = MapsActivity.mSensing.gyro[1];
                //double acc_lat = (acc_z * Math.cos(Math.toRadians(-heading_current)));
                //double acc_lon = (acc_z * Math.sin(Math.toRadians(-heading_current)));

                // debug overlay text
                MapsActivity.mDebugTextView.setText(String.format("accel %.3f", acc_z));

                // advance to next waypoint
                if ((nav_location != null) && (nav_location_target != null))
                {
                    if (nav_location.distanceTo(nav_location_target) < waypoint_proximity)
                    {
                        // is there one more waypoint?
                        if (waypoint_list.size() > (waypoint_idx + 1))
                        {
                            waypoint_idx++;
                            Log.i(tag, "proceed to waypoint " + waypoint_idx);

                            // clear the target location to reset heading
                            //if (heading_reset) nav_location_target = null;

                            // TODO: test me!
                            // steer to the next target
                            updateDesiredHeading();

                        } else {

                            // stop navigating at last waypoint
                            Log.i(tag, "reached final waypoint " + waypoint_idx);
                            MapsActivity.mGoButton.callOnClick();
                        }
                    }
                }
            } // end nav_active if

            // launch this function again
            if (!nav_abort) handler.postDelayed(this, 100);

        }
    }; // end Runnable()


    /**
     * Load or create a list of waypoints.
     */
    public void loadWaypoints()
    {
        waypoint_list.clear();

        // load waypoint_list to file on sdcard
        if (!waypoints_file.exists())
        {
            // the file didn't load, add something close
            waypoint_list.add(new LatLng(30.16, -97.74));

        } else {

            // load waypoint_list from file
            LineNumberReader reader = null;
            try {
                reader = new LineNumberReader(new FileReader(waypoints_file));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] splitline = line.split(",");
                    Log.d(tag, "read waypoint line " + line);
                    LatLng file_waypoint = new LatLng(Float.parseFloat(splitline[1]),
                            Float.parseFloat(splitline[2]));

                    // load the waypoint from the file into memory
                    waypoint_list.add(Integer.parseInt(splitline[0]), file_waypoint);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Save the map waypoints to the selected .csv file.
     */
    public void saveWaypoints()
    {
        // save waypoint_list to file on sdcard
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mContext);
        File waypointsFile = new File(SP.getString("waypointfile", "sdcard/waypoint_list.csv"));
        if (!waypointsFile.exists()) {
            try {
                waypointsFile.createNewFile();
            } catch (IOException e) {
                Log.e(tag, e.getMessage(), e);
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(waypointsFile, false));
            for (int i = 0; i < waypoint_list.size(); i++) {
                buf.append("" + i + ",")
                        .append(waypoint_list.get(i).latitude + ",")
                        .append(waypoint_list.get(i).longitude + ",");
                buf.newLine();
            }
            buf.close();
        } catch (IOException e) {

            Log.e(tag, e.getMessage(), e);
        }
        CharSequence textw = waypointsFile + " saved";
        Toast.makeText(mContext, textw, Toast.LENGTH_SHORT).show();
        Log.d(tag, textw.toString());
    }


    /**
     * Get or update preferences values.
     */
    public void updatePreferences()
    {
        Log.d(tag, "updatePreferences() called");
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mContext);
        location_trace  = SP.getBoolean("locationtrace", false);
        nav_accuracy        = Float.parseFloat(SP.getString("navaccuracy",  "10"));
        waypoint_proximity  = Float.parseFloat(SP.getString("navproximity", "4"));
        waypoints_file = new File(SP.getString("waypointfile", "sdcard/waypoint_list.csv"));

        // re-load the waypoints in case the filename changed
        loadWaypoints();
    }


    /**
     * Sets or updates the
     */
    public void updateDesiredHeading()
    {
        // get bearing to target as 0-360
        nav_location_target = new Location("");
        nav_location_target.setLatitude(waypoint_list.get(waypoint_idx).latitude);
        nav_location_target.setLongitude(waypoint_list.get(waypoint_idx).longitude);
        heading_desired = (360 + nav_location.bearingTo(nav_location_target)) % 360;
    }


    // new location received
    @Override
    public void onLocationChanged(Location location)
    {
        nav_location = location;
        Log.i("accuracy", Float.toString(location.getAccuracy()));
        if (nav_active) {
            if (location.getAccuracy() < nav_accuracy) {

                // current position
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                LatLng newpos = new LatLng(lat, lon);

                if (location_trace)
                {
                    MapsActivity.mMap.addMarker(new MarkerOptions().position(newpos));

                } else {

                    // draw a line to the target
                    MapsActivity.mMap.clear();
                    MapsActivity.mMap.addPolyline(new PolylineOptions().geodesic(true)
                                    .add(newpos)
                                    .add(waypoint_list.get(waypoint_idx))
                                    .color(Color.BLUE)
                    );

                    // draw all waypoints (not draggable)
                    for (int i = 0; i < waypoint_list.size(); i++) {
                        MapsActivity.mMap.addMarker(new MarkerOptions()
                                        .position(waypoint_list.get(i))
                                        .title("point " + i)
                        );
                    }
                } // end location_trace if

                updateDesiredHeading();

            } // end accuracy check if
        } // end nav_active if
    } // end of onLocationChanged


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
