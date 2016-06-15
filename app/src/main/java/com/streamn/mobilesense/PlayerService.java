package com.streamn.mobilesense;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.jjoe64.graphview.GraphView.GraphViewData;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


public class PlayerService extends Service {


    boolean isPlaying = false;


    //LinearLayout motionLayout;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    float movingAverage = 0;

    Timestamp T;

    float aX = 0;
    float aY = 0;
    float aZ = 0;



    public boolean collectSensorData;
    public String hostName;
    public String port;

    SharedPreferences sharedPref;

    Intent mediaActionIntent;

    public static final String BROADCAST_ACTION = "com.streamn.mobilesense.sensordata";
    private final Handler handler = new Handler();
    Intent intent;
    int counter = 0;
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private boolean mActive = true;
    HttpClient httpclient = new DefaultHttpClient();

    @Override
    public void onCreate() {
        super.onCreate();

        intent = new Intent(BROADCAST_ACTION, null);
        mediaActionIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

    }


    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            DisplayLoggingInfo();
            handler.postDelayed(this, 10); // 0.01 seconds
        }
    };

    private void DisplayLoggingInfo() {

        //Log.d(TAG, "entered DisplayLoggingInfo");
        intent.putExtra("time", new Date().toLocaleString());
        intent.putExtra("counter", String.valueOf(counter++));
        intent.putExtra("data", String.valueOf(movingAverage));
        sendBroadcast(intent);
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second

        play(0);


        return(START_NOT_STICKY);
    }




    @Override
    public void onDestroy() {
        if(isPlaying){
            handler.removeCallbacks(sendUpdatesToUI);
            stop();
        }

        super.onDestroy();

    }

    private void sensorStart() {



        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;
                if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                    //TODO: get accel values



                    T = new Timestamp(System.currentTimeMillis());


                    //gravSensorVals = lowPass(event.values.clone(), gravSensorVals);
                    aX = event.values[0];
                    aY = event.values[1];
                    aZ = event.values[2];


                    if(getCollectSensorData()){

                        new sendData(T.toString(), Float.toString(aX), Float.toString(aY), Float.toString(aZ)).execute();

                    }


                }else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    //TODO: get gyro values

                    if (timestamp != 0) {
                        final float dT = (event.timestamp - timestamp) * NS2S;

                        // Axis of the rotation sample, not normalized yet.
                        float axisX = event.values[0];
                        float axisY = event.values[1];
                        float axisZ = event.values[2];

                        // Calculate the angular speed of the sample
                        float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

                        // Normalize the rotation vector if it's big enough to get the axis
                        // (that is, EPSILON should represent your maximum allowable margin of error)
                        if (omegaMagnitude > 0.1) {
                            axisX /= omegaMagnitude;
                            axisY /= omegaMagnitude;
                            axisZ /= omegaMagnitude;
                        }

                        // Integrate around this axis with the angular speed by the timestep
                        // in order to get a delta rotation from this sample over the timestep
                        // We will convert this axis-angle representation of the delta rotation
                        // into a quaternion before turning it into the rotation matrix.
                        float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                        float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
                        float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
                        deltaRotationVector[0] = sinThetaOverTwo * axisX;
                        deltaRotationVector[1] = sinThetaOverTwo * axisY;
                        deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                        deltaRotationVector[3] = cosThetaOverTwo;
                    }
                    timestamp = event.timestamp;



                    float[] deltaRotationMatrix = new float[9];
                    SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
                    // User code should concatenate the delta rotation we computed with the current rotation
                    // in order to get the updated rotation.
                    // rotationCurrent = rotationCurrent * deltaRotationMatrix;



                }





            }
        };


        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);





    }

    private void sensorStop(){

        mSensorManager.unregisterListener(mSensorListener);

    }

    private void play(int volume) {


        //check to see if authenticated.
        if(sharedPref.getString("cookies", "").equals("")) {

            Toast.makeText(getBaseContext(), "Not Authenticated.",
                    Toast.LENGTH_LONG).show();
        }
        else{

            Log.w(getClass().getName(), "Got to play()!");
            isPlaying=true;



//        Notification note=new Notification(R.drawable.stat_notify_chat,  "Move to hear music.", System.currentTimeMillis());

            Intent i=new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pi=PendingIntent.getActivity(this, 0, i, 0);
            sensorStart();

            Notification note = new Notification.Builder(getBaseContext())
                    .setContentIntent(pi)
                    .setContentTitle("Mobsense")
                    .setContentText("Collecting Data.")
                    .setSmallIcon(R.drawable.ic_stat_notify_large)
                    .setTicker("Sending data to web server.")
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .build(); // available from API level 11 and onwards

            startForeground(1337, note);



        }



    }

    private void stop() {



        Log.w(getClass().getName(), "Got to stop()!");
        isPlaying=false;
        sensorStop();
        stopForeground(true);

    }




    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        PlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }



    public boolean getCollectSensorData() {

        collectSensorData = sharedPref.getBoolean("collect_sensor_data", true);
        return collectSensorData;


    }

    public String getHostName() {

        hostName = sharedPref.getString("host_name", "mobsense.net");
        return hostName;


    }

    public String getPort() {

        port = sharedPref.getString("port", "80");
        return port;


    }


    private class sendData extends AsyncTask<URL, Integer, Long> {

        private String t;
        private String ax;
        private String ay;
        private String az;

        public sendData(String t, String ax, String ay, String az) {
            super();
            this.t = t;
            this.ax = ax;
            this.ay = ay;
            this.az = az;

        }
        protected Long doInBackground(URL... urls) {

            long totalSize = 0;


                String ACCOUNT_RESOURCE_URL = "https://" + getHostName() + "/data";
                HttpPost httppost = new HttpPost(ACCOUNT_RESOURCE_URL);
                httppost.setHeader("cookie", sharedPref.getString("cookies",""));



                Log.d("PlayerService", "HEADERS: " + httppost.getFirstHeader("Cookie"));
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String responseBody = null;



                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<>(3);
                    nameValuePairs.add(new BasicNameValuePair("t",  this.t));
                    nameValuePairs.add(new BasicNameValuePair("ax", this.ax.toString()));
                    nameValuePairs.add(new BasicNameValuePair("ay", this.ay.toString()));
                    nameValuePairs.add(new BasicNameValuePair("az", this.az.toString()));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


                    responseBody = httpclient.execute(httppost, responseHandler);
                    Log.d("PlayerService", "RESPONSE: " + responseBody);
                } catch (IOException e) {
                    e.printStackTrace();
                }



            return totalSize;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            //showDialog("Downloaded " + result + " bytes");
        }
    }


    private final Runnable mRunnable = new Runnable() {

        public void run() {
            if (mActive) {


            }
        }
    };




}

