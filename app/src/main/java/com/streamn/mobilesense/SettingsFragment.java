package com.streamn.mobilesense;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import java.net.CookieHandler;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.util.List;


public class SettingsFragment extends PreferenceFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    SharedPreferences sharedPref;
    public static OAuthService oAuthService;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MainActivity mActivity;
    private WebActivity wActivity;
    SharedPreferences.Editor editor;


    private String TAG = "SettingsFragment";

    private OnFragmentInteractionListener mListener;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {


        mActivity =  (MainActivity) getActivity();
        wActivity = new WebActivity();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = sharedPref.edit();
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceToBoolean(findPreference("collect_sensor_data"));
        bindPreferenceToString(findPreference("host_name"));
        bindPreferenceToString(findPreference("port"));


        Preference facebookAuth = (Preference) findPreference("facebook");
        facebookAuth.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                //User selected authenticate with facebook
                RequestTask mAuthTask = new RequestTask("facebook");
                mAuthTask.execute((Void) null);// Execute background task

                return true;
            }
        });

        Preference twitterAuth = (Preference) findPreference("twitter");
        twitterAuth.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                //User selected authenticate with twitter
                RequestTask mAuthTask = new RequestTask("twitter");
                mAuthTask.execute((Void) null);// Execute background task

                return true;
            }
        });

        Preference googleAuth = (Preference) findPreference("google");
        googleAuth.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                //User selected authenticate with google
                RequestTask mAuthTask = new RequestTask("google");
                mAuthTask.execute((Void) null);// Execute background task

                return true;
            }
        });
        Preference clearAuth = (Preference) findPreference("clearauth");
        clearAuth.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {



                //Clear webActivity cookies for login
                CookieSyncManager.createInstance(mActivity);
                CookieManager cookieManager = CookieManager.getInstance();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cookieManager.removeAllCookies(null);
                }
                else {
                    cookieManager.removeAllCookie();
                }

                //clear cookies stored in shared prefa
                editor.putString("cookies", "");
                editor.commit();
                return true;
            }
        });


    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;


        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
        public void getOauthService(OAuthService oAuthService);

    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            Log.d("SettingsFragment", value.toString());
            Log.d("SettingsFragment", preference.getKey());
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceToValueListener
     */
    private static void bindPreferenceToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getInt(preference.getKey(), 0));
    }

    private static void bindPreferenceToBoolean(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getBoolean(preference.getKey(), false));
    }

    private static void bindPreferenceToString(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    public class RequestTask extends AsyncTask<Void, Void, Boolean> {

        private String serviceProvider;

        public RequestTask(String serviceProvider){

            this.serviceProvider = serviceProvider;

        }

        protected void onPreExecute() {
        }

        protected Boolean doInBackground(Void... params) {

            String authURL;
            if(this.serviceProvider.equals("facebook") ){

                editor.putString("provider", "facebook");
                editor.commit();
                String APIKEY = "XXXXXXX"; // your App ID Facebook
                String APISECRET = "XXXXXXX"; // your App secret Facebook
                String CALLBACK = "https://" + sharedPref.getString("host_name","mobsense.net")  + "/auth/facebook/callback";

                //set up service and get request token as seen on scribe website
                //https://github.com/fernandezpablo85/scribe-java/wiki/Getting-Started
                oAuthService = new ServiceBuilder()
                        .provider(FacebookApi.class)
                        .apiKey(APIKEY)
                        .apiSecret(APISECRET)
                        .callback(CALLBACK)
                        .build();



                authURL = oAuthService.getAuthorizationUrl(null);
                Log.d(TAG + " OAuthURL: " + this.serviceProvider, authURL);
                //this intent is used to open other activity witch contains authentication webView
                Intent intent = new Intent(getActivity(),WebActivity.class);
                intent.putExtra("authURL", "https://" + sharedPref.getString("host_name","mobsense.net")   + "/auth/facebook");
                intent.putExtra("provider", this.serviceProvider);

                startActivity(intent);
                wActivity.getOauthService(oAuthService);
            }
            else if(this.serviceProvider.equals("twitter")){


                editor.putString("provider", "twitter");
                editor.commit();
                String APIKEY = "XXXXXXX";  // your App ID Twitter
                String APISECRET = "XXXXXXX"; // your App secret Twitter
                String CALLBACK = "https://" + sharedPref.getString("host_name","mobsense.net")   + "/auth/twitter/callback";


                //set up service and get request token as seen on scribe website
                //https://github.com/fernandezpablo85/scribe-java/wiki/Getting-Started
                oAuthService = new ServiceBuilder()
                        .provider(TwitterApi.SSL.class)
                        .apiKey(APIKEY)
                        .apiSecret(APISECRET)
                        .callback(CALLBACK)
                        .build();

                final Token requestToken = oAuthService.getRequestToken();
                authURL = oAuthService.getAuthorizationUrl(requestToken);

                Log.d(TAG + " OAuthURL: " + this.serviceProvider, authURL);
                //this intent is used to open other activity witch contains authentication webView
                Intent intent = new Intent(getActivity(),WebActivity.class);
                intent.putExtra("authURL", "https://" + sharedPref.getString("host_name","mobsense.net")   + "/auth/twitter");
                intent.putExtra("token", requestToken.getToken());
                intent.putExtra("secret", requestToken.getSecret());
                intent.putExtra("provider", this.serviceProvider);
                startActivity(intent);
                wActivity.getOauthService(oAuthService);
            }
            else if(this.serviceProvider.equals("google")) {


                editor.putString("provider", "google");
                editor.commit();
                String APIKEY = "XXXXXXX"; // your App ID Google
                String APISECRET = "XXXXXXX"; // your App secret Google
                String CALLBACK = "https://" + sharedPref.getString("host_name","mobsense.net")  + "/auth/google/callback";
                String SCOPE = "https://www.googleapis.com/auth/plus.me";


                //set up service and get request token as seen on scribe website
                //https://github.com/fernandezpablo85/scribe-java/wiki/Getting-Started
                oAuthService = new ServiceBuilder()
                        .provider(Google2Api.class)
                        .apiKey(APIKEY)
                        .apiSecret(APISECRET)
                        .callback(CALLBACK)
                        .scope(SCOPE)
                        .build();


                authURL = oAuthService.getAuthorizationUrl(null);
                Log.d(TAG + " OAuthURL: " + this.serviceProvider, authURL);
                //this intent is used to open other activity witch contains authentication webView
                Intent intent = new Intent(getActivity(),WebActivity.class);
                intent.putExtra("authURL", "https://" + sharedPref.getString("host_name","mobsense.net")   + "/auth/google");
                intent.putExtra("provider", this.serviceProvider);
                startActivity(intent);
                wActivity.getOauthService(oAuthService);
            }
            else{

                Log.d(TAG, "No Provider");

            }




            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success == true) {
                Log.e("RequestTaskAsyncTask", "onPostExecute");
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
            }
        }

        @Override
        protected void onCancelled() {
            //mAuthTask = null;
        }


    }


}



