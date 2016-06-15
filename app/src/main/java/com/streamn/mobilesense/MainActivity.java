package com.streamn.mobilesense;


import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.streamn.mobilesense.widgets.AnimatedExpandableListView;
import com.streamn.mobilesense.PlayerService.LocalBinder;

import org.scribe.oauth.OAuthService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.widget.ExpandableListView.OnGroupClickListener;


public class MainActivity extends ActionBarActivity implements SettingsFragment.OnFragmentInteractionListener  {


    private static long back_pressed;
    public Intent intent;
    public PlayerService mService;
    public List mediaPlayers;
    public AudioManager mAudioManager;
    public Fragment mainFragment;
    public Fragment settingFragment;
    public int current;
    public Context mContext;
    boolean mBound;
    boolean mEnabled;
    boolean mDrawerOpened = false;
    ExpandableListAdapter mExpListAdapter;
    public AnimatedExpandableListView mExpListView;
    ListView mDrawerChild;
    List<String> listDataHeader;
    HashMap<String, List> listDataChild;
    //private ListView mDrawerList;
    public DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    SharedPreferences sharedPref;
    public static OAuthService oAuthService;
    public SharedPreferences.Editor editor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPref.edit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        mContext = this;

        mExpListView = (AnimatedExpandableListView) findViewById(R.id.left_drawer);

        LayoutInflater inflater = getLayoutInflater();
        View header = (View)inflater.inflate(R.layout.header, mExpListView, false);
        mExpListView.addHeaderView(header, null, false);


        prepareListData();

        mExpListAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        mExpListView.setAdapter(mExpListAdapter);


        mainFragment = new MainFragment();
        settingFragment = new SettingsFragment();



        // In order to show animations, we need to use a custom click handler
        // for our ExpandableListView.
        mExpListView.setOnGroupClickListener(new OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // We call collapseGroupWithAnimation(int) and
                // expandGroupWithAnimation(int) to animate group
                // expansion/collapse.


                if (mExpListView.isGroupExpanded(groupPosition)) {
                    mExpListView.collapseGroupWithAnimation(groupPosition);
                }


                else{
                    if(groupPosition == 0) {


                        //logic when user clicks Home navdrawer item
                        getSupportActionBar().setTitle("");

                        if(mainFragment.isVisible()){

                        }
                        else {
                            Bundle args = new Bundle();
                            args.putInt(MainFragment.ARG_PLANET_NUMBER, groupPosition);
                            mainFragment.setArguments(args);

                            // Insert the fragment by replacing any existing fragment
                            FragmentManager fragmentManager = getFragmentManager();
                            fragmentManager.beginTransaction()
                                    .replace(R.id.content_frame, mainFragment)
                                    .commit();


                        }
                        // Highlight the selected item, update the title, and close the drawer
                        mExpListView.setItemChecked(groupPosition, true);
                        //setTitle(mDrawerTitles[position]);
                        mDrawerLayout.closeDrawer(mExpListView);
                    }
                    else if(groupPosition == 1){


                        //logic when user clicks Settings navdrawer item
                        getSupportActionBar().setTitle("Settings");


                        if(settingFragment.isVisible()){

                        }
                        else{

                            Bundle args = new Bundle();
                            args.putInt(MainFragment.ARG_PLANET_NUMBER, groupPosition);
                            settingFragment.setArguments(args);

                            // Insert the fragment by replacing any existing fragment
                            FragmentManager fragmentManager = getFragmentManager();
                            fragmentManager.beginTransaction()
                                    .replace(R.id.content_frame, settingFragment)
                                    .commit();



                        }
                        // Highlight the selected item, update the title, and close the drawer
                        mExpListView.setItemChecked(groupPosition, true);
                        //setTitle(mDrawerTitles[position]);
                        mDrawerLayout.closeDrawer(mExpListView);



                    }
                    else{
                        mDrawerLayout.closeDrawer(mExpListView);
                    }

                }
                return true;
            }

        });





        mDrawerChild = (ListView) findViewById(R.id.drawer_child_listview);
        mTitle = getTitle();
        mDrawerTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //toolbar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                mDrawerOpened = false;
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);



                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                getSupportActionBar().setTitle("");
                mDrawerOpened = true;



            }
        };


        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        if (savedInstanceState == null) {
            selectItem(0);
            current = 0;
        }

        //intenet for the PlayerService (service that does the sensing... poorly named?)
        intent = new Intent(this, PlayerService.class);
        //bind the service this method binds the service to a variable mService
        //this way we can invoke methods to pass data back and forth between the service
        this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


    }


    //Defines callbacks for service binding, passed to bindService()
    //this allows us to call methods on the service for passing data
    public ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();



            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

            mBound = false;
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();

        //get user's current volume, if volume is muted use half of max
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }


    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List>();

        listDataHeader.add("Home");
        listDataHeader.add("Settings");

        //add children to parent
        listDataChild.put(listDataHeader.get(0), null);
        listDataChild.put(listDataHeader.get(1), null);

        mExpListView.setOnChildClickListener(new ChildItemClickListener());



    }

    /**
     * MAIN FRAG SWAPPA
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {

        // Create a new fragment and specify the planet to show based on position

        Fragment fragment;
        if(position == 0){
            fragment = new SettingsFragment();

        }
        if(position == 1){
            fragment = new MainFragment();

        }else{
            fragment = new MainFragment();

        }

        Bundle args = new Bundle();
        args.putInt(MainFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mExpListView.setItemChecked(position, true);
        //setTitle(mDrawerTitles[position]);
        mDrawerLayout.closeDrawer(mExpListView);

    }

    @Override
    public void setTitle(CharSequence title) {

        mTitle = title;

    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mExpListView);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDestroy() {

        Log.i("MainActivity", "onDestroy()");
        super.onDestroy();
        //if user kills the app from task manager
        // Unbind from the service if bound
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }


        stopService(intent);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d("MainActivity", "onActivityResult()");
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
            }
        }
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();

        //Check if you got NewIntent event due to Twitter Call back only

        if (uri != null) {

            Log.d("OnNewIntent()", uri.toString());
            String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);


        } else {
            // Do something if the callback comes from elsewhere
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("MainActivity", "onResume()");



    }



    @Override
    public void onPause() {
        super.onPause();

        Log.d("MainActivity", "onPause() called");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
            Log.d("key pressed", Integer.toString(keyCode));
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (mainFragment instanceof MainFragment)
                        //((MainFragment) mainFragment).volumeUp();
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (mainFragment instanceof MainFragment)
                        //((MainFragment) mainFragment).volumeDown();
                    return true;
                case KeyEvent.KEYCODE_BACK:
                    if (back_pressed + 2000 > System.currentTimeMillis()) {

                        // Unbind from the service
                        if (mEnabled) {

                            // if we have reg and broadcast rx un register it
                            //unregisterReceiver(broadcastReceiver);

                            stopService(intent);
                        }
                        super.onBackPressed();
                    } else {
                        Toast.makeText(getBaseContext(), "Press Back again to exit, Press the Home button to run in background", Toast.LENGTH_SHORT).show();
                        back_pressed = System.currentTimeMillis();
                    }
                    return true;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    ((MainFragment) mainFragment).onClick(mainFragment.getView());
                    return true;
                case KeyEvent.KEYCODE_MENU:

                        //user the menu button to open and close the navDrawer for completness
                        if(mDrawerOpened){

                            mDrawerLayout.closeDrawer(mExpListView);

                        }
                        else{

                            mDrawerLayout.openDrawer(mExpListView);

                        }


                    return true;

                default:
                    return true;
            }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

        Log.d("MainActivity", "on fragment interaction");

    }

    @Override
    public void getOauthService(OAuthService oAuthService) {

        Log.d("MainActivity", "Get Oauth Service");
        this.oAuthService = oAuthService;

    }

    // Container Activity must implement this interface
    public interface OnArticleSelectedListener {
        public void onArticleSelected(Uri articleUri);
    }

    private class ChildItemClickListener implements OnChildClickListener {

        @Override
        public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int childPosition, long id) {

            //logic for when group/child is selected
            Log.d("groupPosition ", Integer.toString(groupPosition));
            Log.d("childPosition ", Integer.toString(childPosition));

            return true;
        }


    }





    public class RequestAuth extends AsyncTask<Void, Void, Boolean> {

        private String provider;
        private String code;
        private String token;
        private String verifier;
        private OAuthService oAuthService;
        private String cookies;

        public RequestAuth(String provider, String code, String token, String verifier, OAuthService oAuthService, String cookies){

            this.provider = provider;
            this.code = code;
            this.token = token;
            this.verifier = verifier;
            this.oAuthService = oAuthService;
            this.cookies = cookies;

        }

        protected void onPreExecute() {
        }

        protected Boolean doInBackground(Void... params) {

            //logic for authentication request - unused right now

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success == true) {
                Log.e("RequestAuthAsyncTask", "onPostExecute");

            }
        }

        @Override
        protected void onCancelled() {
            //mAuthTask = null;
        }
    }



}

