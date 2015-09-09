package com.d33z.android.dronedroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

/**
 * The purpose of this class is to deal with physical outputs such as LEDs, motors, servos, etc
 * via a connected IOIO. Processing should be kept to a minimum inside the IOIO loop to reduce
 * control latency.
 *
 * Thanks:
 * Ytai
 *
 * Created by Daniel Epperson on 7/19/15.
 */
public class Mechanical extends IOIOService {

    // ioio outputs
    private PwmOutput servo, motor;
    private DigitalOutput servo_enable;

    // in preferences
    private int nav_accuracy,       // for movement
                reverse_time,       // when stuck
                pwm_frequency,      // in hertz
                steering_max,       // uS from center
                steering_trim,      // servo center pwm uS
                stuck_timeout,      // in IOIO loops
                throttle_max,       // uS from center
                throttle_trim;      // ESC center pwm uS

    // local variables
    public  int throttle = 0, stuck_count = 0;

    final String tag = "Mechanical";


    @Override
    protected IOIOLooper createIOIOLooper() {
        return new BaseIOIOLooper() {
            private DigitalOutput statled;

            @Override
            protected void setup() throws ConnectionLostException, InterruptedException {

                // get preferences
                updatePreferences();

                // configure ioio pins
                servo       = ioio_.openPwmOutput(new DigitalOutput.Spec(40,
                        DigitalOutput.Spec.Mode.OPEN_DRAIN), pwm_frequency);

                servo_enable= ioio_.openDigitalOutput(new DigitalOutput.Spec(41,
                                DigitalOutput.Spec.Mode.NORMAL), false);

                statled     = ioio_.openDigitalOutput(IOIO.LED_PIN);

                motor       = ioio_.openPwmOutput(new DigitalOutput.Spec(39,
                                DigitalOutput.Spec.Mode.OPEN_DRAIN), pwm_frequency);

                // enable the servo
                servo_enable.write(true);

                // center the outputs
                servo.setPulseWidth(steering_trim);
                motor.setPulseWidth(throttle_trim);

            } // end setup()


            @Override
            public void loop() throws ConnectionLostException, InterruptedException {

                float nav_speed = 0;
                float location_accuracy = 0;
                int steeringval = 0;

                // is navigation active?
                if (null != MapsActivity.mNavigation && MapsActivity.mNavigation.nav_active) {

                    // TODO: rollover detection
                    // if MapsActivity.mSensing.accMagOrientation[2] is over some amount, stop

                    // set steering position
                    // in m/s -- 1m/s = 2.2mph
                    if (null != MapsActivity.mNavigation.nav_location) {

                        nav_speed = MapsActivity.mNavigation.nav_location.getSpeed();
                        Log.d(tag, "nav_speed=" + nav_speed);

                        location_accuracy = MapsActivity.mNavigation.nav_location.getAccuracy();
                        Log.d(tag, "nav_accuracy=" + location_accuracy);

                        // invert steering for new navigation code
                        steeringval = (int) (MapsActivity.mNavigation.heading_error * steering_max) * -1;
                        Log.d(tag, "input steeringval=" + steeringval);
                    }
                    if (nav_speed > 1) {
                        // speed controlled steering
                        steeringval = (int) (steeringval / nav_speed);
                        float new_max = steering_max / nav_speed;
                        if (steeringval > new_max) steeringval = (int) new_max;
                        if (steeringval < -new_max) steeringval = (int) -new_max;
                        Log.d(tag, "steering new_max=" + new_max);

                    } else {

                        // steering maximum from preferences
                        if (steeringval > steering_max) steeringval = steering_max;
                        if (steeringval < -steering_max) steeringval = -steering_max;
                    }
                    Log.d(tag, "output steeringval=" + steeringval);
                    servo.setPulseWidth(steering_trim + steeringval);

                    // set throttle position
                    if (location_accuracy < nav_accuracy) {

                        // ok to go
                        throttle = throttle_max;

                    } else {

                        // gps is too inaccurate, go to neutral
                        throttle = 0;
                    }
                    Log.d(tag, "throttle=" + throttle);
                    motor.setPulseWidth(throttle_trim + throttle);

                    // detect blockage
                    if ((throttle > 0) && (nav_speed == 0)) stuck_count++;
                    else stuck_count = 0;
                    if (stuck_count > stuck_timeout) {

                        // we are not moving but should be
                        stuck_count = 0;
                        Log.d(tag, "stuck timeout exceeded");

                        // TODO: test me!
                        // steer left
                        //servo.setPulseWidth(steering_trim + steering_max);
                        // steer to the opposite direction
                        servo.setPulseWidth(steering_trim - steeringval);

                        // shift into reverse
                        motor.setPulseWidth(throttle_trim - throttle_max);
                        Thread.sleep(200);
                        motor.setPulseWidth(throttle_trim);
                        Thread.sleep(200);

                        // drive in reverse
                        motor.setPulseWidth(throttle_trim - throttle_max);
                        Thread.sleep(reverse_time);
                        motor.setPulseWidth(throttle_trim);
                        Thread.sleep(200);

                        // shift into drive
                        motor.setPulseWidth(throttle_trim + throttle_max);
                        Thread.sleep(200);
                        motor.setPulseWidth(throttle_trim);
                        Thread.sleep(200);

                        // restore steering
                        servo.setPulseWidth(steering_trim + steeringval);

                    } // end blockage if

                } else {

                    // nav is not active, full stop
                    motor.setPulseWidth(throttle_trim);
                    servo.setPulseWidth(steering_trim);

                    // reset the stuck counter
                    stuck_count = 0;

                } // end nav_active if

                // pulse the stat LED on the IOIO
                statled.write(false);
                Thread.sleep(50);
                statled.write(true);
                Thread.sleep(50);

            } // end loop()
        }; // end BaseIOIOLooper()
    } // end createIOIOLooper()


    /**
     * Get or update preferences values.
     */
    public void updatePreferences()
    {
        Log.d(tag, "updatePreferences() called");
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        nav_accuracy    = Integer.parseInt(SP.getString("navaccuracy",  "10"));
        pwm_frequency   = Integer.parseInt(SP.getString("pwmfreq",      "100"));
        reverse_time    = Integer.parseInt(SP.getString("reversetime",  "1000"));
        steering_max    = Integer.parseInt(SP.getString("steermax",     "360"));
        steering_trim   = Integer.parseInt(SP.getString("steertrim",    "1500"));
        stuck_timeout   = Integer.parseInt(SP.getString("stucktime",    "100"));
        throttle_max    = Integer.parseInt(SP.getString("throttlemax",  "60"));
        throttle_trim   = Integer.parseInt(SP.getString("throttletrim", "1500"));
    }


    @Override
    public void onCreate()
    {
        super.onCreate();
    }


    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }


    /**
     * Allow binding to the service.
     */

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public Mechanical getService() {
            return Mechanical.this;
        }
    }
}
