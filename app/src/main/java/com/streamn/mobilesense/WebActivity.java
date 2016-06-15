package com.streamn.mobilesense;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.scribe.oauth.OAuthService;

import java.io.IOException;


/**
 * Created by lol on 2/20/15.
 */
public class WebActivity extends Activity implements SettingsFragment.OnFragmentInteractionListener{

    private static String TAG = "WebActivity";

    private WebView webView;
    public String authURL;
    public String twitToken;
    public String twitSecret;
    private MainActivity mActivity;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private static OAuthService oAuthService;
    private String provider;
    private String token;
    private String verifier;
    private String code;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_webview);
        mActivity = new MainActivity();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPref.edit();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
             this.authURL = extras.getString("authURL");
             this.provider = extras.getString("provider");
             this.twitToken = extras.getString("token");
             this.twitSecret = extras.getString("secret");
        }



        webView = (WebView) findViewById(R.id.web_view);
        WebSettings webSettings = webView.getSettings();
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        CookieManager.getInstance().setAcceptCookie(true);
        webView.loadUrl(this.authURL);







        webView.setWebViewClient(new WebViewClient() {

//            public void onPageFinished(WebView view, String url) {
//
//                String cookies0 = CookieManager.getInstance().getCookie(authURL);
//                Log.d(TAG, "All the cookies in a string0: " + cookies0);
//
//
//                // do your stuff here
//            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Here put your code
                Uri uri = Uri.parse(url);
                String code = uri.getQueryParameter("code");
                String token = uri.getQueryParameter("oauth_token");
                String verifier = uri.getQueryParameter("oauth_verifier");

                Uri authUri = Uri.parse(authURL);
                String redirect_uri = authUri.getQueryParameter("redirect_uri");
                String client_id = authUri.getQueryParameter("client_id");



                if (code != null) {

                    Log.d(TAG + " Provider", provider);
                    Log.d(TAG + " Code", code);
                    Log.d(TAG + " authURL", authURL);
                    Log.d(TAG + " url", url);



                    //Log.d(TAG + " client_id", client_id);
                    //Log.d(TAG + " redirect_uri", redirect_uri + "?code=" + code);

                    String cookies = CookieManager.getInstance().getCookie(url);
                    Log.d(TAG, "All the cookies in a string: " + cookies);

                    editor.putString("cookies", cookies);
                    editor.commit();

//                    getRequest mAuthTask = new getRequest(authURL + "/callback0?code=" + code, cookies, null, null, provider);
//                    mAuthTask.execute((Void) null);// Execute background task


                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("code", code);
                    intent.putExtra("provider", provider);
                    intent.putExtra("cookies", cookies);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);



                    finish();
                    startActivity(intent);

                    return false;  //Indicates WebView to NOT load the url;

                } else if (token != null || verifier != null) {


                    Log.d(TAG + " Provider", provider);
                    Log.d(TAG + " Token", token);

                    if(verifier == null){

                        return false;
                    }

//                    Log.d(TAG + " Verifier", verifier);
//                    Log.d(TAG + " authURL", authURL);
                    Log.d(TAG + " url", url);
//
//                    Log.d(TAG + " twitToken ", twitToken);
//                    Log.d(TAG + " twitSecret ", twitSecret);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("token", token);
                    intent.putExtra("verifier", verifier);
                    intent.putExtra("provider", provider);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    editor.putString("provider", "twitter");
                    editor.commit();


                    String cookies = CookieManager.getInstance().getCookie(url);
                    Log.d(TAG, "All the cookies in a string: " + cookies);
                    editor.putString("cookies", cookies);
                    editor.commit();

//                    //You do not need the get request here like you do for oauth2.0
//                    getRequest mAuthTask = new getRequest(url, cookies, token, verifier, provider);
//                    mAuthTask.execute((Void) null);// Execute background task


                    webView.loadUrl(url);
                    Log.d("WebActivity", "All the cookies in a string: " + cookies);
                    intent.putExtra("cookies", cookies);




                    finish();
                    startActivity(intent);
                    return false; //Indicates WebView to NOT load the url;


                } else {
                    return false; //Allow WebView to load url
                }


            }
        });

    }

    public String getHostName() {

        return sharedPref.getString("host_name", "mobsense.net");


    }

    public String getPort() {

        return sharedPref.getString("port", "80");


    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void getOauthService(OAuthService oAuthService) {

        Log.d("MainActivity", "Get Oauth Service");
        this.oAuthService = oAuthService;

    }


    public class getRequest extends AsyncTask<Void, Void, Boolean> {

        private String uri;
        private String cookies;
        private String token;
        private String verifier;
        private String provider;

        public getRequest(String uri, String cookies, String token, String verifier, String provider){

            this.uri = uri;
            this.cookies = cookies;
            this.token = token;
            this.verifier = verifier;
            this.provider = provider;


        }

        protected void onPreExecute() {
        }

        protected Boolean doInBackground(Void... params) {




                String ACCOUNT_RESOURCE_URL = this.uri;
                HttpClient httpclient = new DefaultHttpClient();


                HttpGet httpget = new HttpGet(ACCOUNT_RESOURCE_URL);
                //httpget.setHeader("Cookie", this.cookies);
                Log.d(TAG, "HEADERS: " + httpget.getFirstHeader("Cookie"));

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String responseBody = null;

                try {
                    responseBody = httpclient.execute(httpget, responseHandler);

                    Log.d(TAG, "RESPONSE: " + responseBody);
                    Log.d(TAG, "HEADERS: " + httpget.getFirstHeader("Cookie"));
                } catch (IOException e) {
                    e.printStackTrace();
                }


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
