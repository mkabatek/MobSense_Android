package com.streamn.mobilesense;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;


/**
 * Fragment that appears in the "content_frame"
 */

//TODO: this class needs to be updated, still using planet stuff
public class MainFragment extends PreferenceFragment implements View.OnClickListener {

    public static final String ARG_PLANET_NUMBER = "planet_number";
    private static final int ANIMATION_DURATION = 500;
    ImageView mIcon, mIconOn;
    ImageView mBackgroundShape;
    MainActivity activity;
    private float mFullScreenScale;
    private int mAppOn = 2;




    public MainFragment() {
        // Empty constructor required for fragment subclasses
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.main_fragment, container, false);

        mIcon = (ImageView) mainView.findViewById(R.id.waveform);
        mIconOn = (ImageView) mainView.findViewById(R.id.waveform_on);
        mBackgroundShape = (ImageView) mainView.findViewById(R.id.bg);
        mIcon.setOnClickListener(this);



        //get an instance of our activity so we can call the service from the fragment
        activity = (MainActivity) getActivity();

        if(activity.mEnabled){


            if (mFullScreenScale <= 0.0f) {

                mFullScreenScale = getMeasureScale();
            }
            if (Build.VERSION.SDK_INT > 11) {

                mBackgroundShape.setScaleX(mFullScreenScale);
                mBackgroundShape.setScaleY(mFullScreenScale);


                mIconOn.animate()
                        .alpha(1f)
                        .setDuration(0);
            }
            mAppOn = 3;

        }
        else{
            mAppOn = 2;


        }



        return mainView;
    }

    @Override
    public void onClick(View v) {

        if (mAppOn % 2 == 0) {

                onAppOn();
                mAppOn++;


        } else {

            onAppOff();
            mAppOn++;

        }


    }



    private void onAppOn() {

        activity.mEnabled= true;


        if (mBackgroundShape == null) {

            return;
        }
        if (mFullScreenScale <= 0.0f) {

            mFullScreenScale = getMeasureScale();
        }
        if (Build.VERSION.SDK_INT > 11) {


            mBackgroundShape.animate()
                    .scaleX(mFullScreenScale)
                    .scaleY(mFullScreenScale)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(ANIMATION_DURATION);

            mIconOn.animate()
                    .alpha(1f)
                    .setDuration(ANIMATION_DURATION);
        }


        activity.bindService(activity.intent, activity.mConnection, getActivity().BIND_AUTO_CREATE);

        activity.startService(activity.intent);








    }


    private void onAppOff() {

        activity.mEnabled= false;
        if (mBackgroundShape == null) {

            return;
        }
        if (Build.VERSION.SDK_INT > 11) {

            mBackgroundShape.animate()
                    .scaleX(1)
                    .scaleY(1)
                    .setInterpolator(new OvershootInterpolator())
                    .setDuration(ANIMATION_DURATION);

            mIconOn.animate()
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION);
        }


        //This is the case when the user turns the service off

        //Unregister any broadcastrx
        //getActivity().unregisterReceiver(activity.broadcastReceiver);

        // Unbind from the service if bound
        if (activity.mBound) {
            activity.unbindService(activity.mConnection);
            activity.mBound = false;
        }

        activity.stopService(activity.intent);


    }

    private float getMeasureScale() {

        WindowManager wm = getActivity().getWindowManager();
        Display display = wm.getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float displayHeight = outMetrics.heightPixels;
        float displayWidth = outMetrics.widthPixels;

        return (Math.max(displayHeight, displayWidth) /
                this.getResources().getDimensionPixelSize(R.dimen.button_size)) * 2;
    }


}