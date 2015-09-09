package com.d33z.android.dronedroid;


import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


/**
 * Created by epperson on 8/5/15.
 *
 * NOT WORKING YET
 */
public class Webserver extends NanoHTTPD {


    private final String tag = "Webserver";


    public Webserver() throws IOException {
        super(8088);
        Log.d(tag, "created");
    }


    @Override
    public Response serve(IHTTPSession session) {

        Log.d(tag, "serve() called");

        String uri = session.getUri().toString();
        Log.d(tag, "uri=" + uri);

        String msg = "<html><body><h1>DroneDroid Server</h1>\n";

        // TODO: not working yet
        if (uri == "/logcat")
        {
            try {
                FileReader fr = new FileReader("sdcard/logcat.txt");
                BufferedReader br = new BufferedReader(fr);
                String line = "";
                while ((line = br.readLine()) != null) msg += line;
            } catch(Exception e) {
                Log.e(tag, e.toString());
            }

        } else {

            if (null != MapsActivity.mNavigation) {
                msg += "<ul><li>heading_error="
                        + MapsActivity.mNavigation.heading_error
                        + "</li>";

                msg += "<li>waypoint_idx="
                        + MapsActivity.mNavigation.waypoint_idx
                        + "</li></ul>";

                msg += "<a href='/logcat'>Logcat</a>";
            }
        }
        return newFixedLengthResponse(msg + "</body></html>\n" );
    }
}
