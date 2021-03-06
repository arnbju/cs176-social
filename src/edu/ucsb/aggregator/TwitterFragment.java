package edu.ucsb.aggregator;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.facebook.Response;


import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TwitterFragment extends Activity {
	// Constants
	/**
	 * Register your here app https://dev.twitter.com/apps/new and get your
	 * consumer key and secret
	 * */
	static String TWITTER_CONSUMER_KEY = "n73xFF1NwQ50rIDp5P07Aw"; // place your cosumer key here
	static String TWITTER_CONSUMER_SECRET = "5diHSU9Oz36SnVeuZXbJY3n2gKH1Hs1INCNmUziiv8"; // place your consumer secret here
	
	// Preference Constants
	static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

	static final String TWITTER_CALLBACK_URL = "aggregator://twitter";

	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
	
	private UpdateAdaptor updateAdapter;
	
	// Login button
	Button btnLoginTwitter;
	// Update status button
	Button btnUpdateStatus;
	// Logout button
	Button btnLogoutTwitter;
//	TextView tweets;
	ListView tweetlist;
	private List<Update> updates;
	
	// Progress dialog
	ProgressDialog pDialog;

	// Twitter
	private static Twitter twitter;
	private static RequestToken requestToken;

	// Shared Preferences
	private static SharedPreferences mSharedPreferences;

	// Internet Connection detector
	private ConnectionDetector cd;

	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter);
		if (Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		updates = new ArrayList<Update>();
		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(TwitterFragment.this, "Internet Connection Error", "Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}

		// Check if twitter keys are set
		if(TWITTER_CONSUMER_KEY.trim().length() == 0 || TWITTER_CONSUMER_SECRET.trim().length() == 0){
			// Internet Connection is not present
			alert.showAlertDialog(TwitterFragment.this, "Twitter oAuth tokens", "Please set your twitter oauth tokens first!", false);
			// stop executing code by return
			return;
		}

		// All UI elements
		btnLoginTwitter = (Button) findViewById(R.id.btnLoginTwitter);
		btnUpdateStatus = (Button) findViewById(R.id.btnUpdateStatus);
		btnLogoutTwitter = (Button) findViewById(R.id.btnLogoutTwitter);
//		tweets = (TextView) findViewById(R.id.lblTweet);
		tweetlist = (ListView) findViewById(R.id.homeList);
		updateAdapter = new UpdateAdaptor(this, R.layout.twitter_update, updates);
		tweetlist.setAdapter(updateAdapter);
		
		// Shared Preferences
		mSharedPreferences = getApplicationContext().getSharedPreferences(
				"MyPref", 0);

		/**
		 * Twitter login button click event will call loginToTwitter() function
		 * */
		btnLoginTwitter.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Call login twitter function
				loginToTwitter();
			}
		});

		/**
		 * Button click event to Update Status, will call updateTwitterStatus()
		 * function
		 * */
		btnUpdateStatus.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Check for blank text
					new getTwitterFeed().execute();
				
			}
		});

		/**
		 * Button click event for logout from twitter
		 * */
		btnLogoutTwitter.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Call logout twitter function
				logoutFromTwitter();
			}
		});

		/** This if conditions is tested once is
		 * redirected from twitter page. Parse the uri to get oAuth
		 * Verifier
		 * */
		if (!isTwitterLoggedInAlready()) {
			Uri uri = getIntent().getData();
			if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
				// oAuth verifier
				String verifier = uri
						.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

				try {
					// Get the access token
					AccessToken accessToken = twitter.getOAuthAccessToken(
							requestToken, verifier);

					// Shared Preferences
					Editor e = mSharedPreferences.edit();

					// After getting access token, access token secret
					// store them in application preferences
					e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
					e.putString(PREF_KEY_OAUTH_SECRET,
							accessToken.getTokenSecret());
					// Store login status - true
					e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
					e.commit(); // save changes

					Log.e("Twitter OAuth Token", "> " + accessToken.getToken());

					// Hide login button
					btnLoginTwitter.setVisibility(View.GONE);

					// Show Update Twitter
					btnUpdateStatus.setVisibility(View.VISIBLE);
					btnLogoutTwitter.setVisibility(View.VISIBLE);
//					tweets.setVisibility(View.VISIBLE);
					
					// Getting user details from twitter
					// For now i am getting his name only
					long userID = accessToken.getUserId();
					User user = twitter.showUser(userID);
					String username = user.getName();

					// Displaying in xml ui
				} catch (Exception e) {
					// Check log for login errors
					Log.e("Twitter Login Error", "> " + e.getMessage());
				}
			}
		}

	}

	/**
	 * Function to login twitter
	 * */
	private void loginToTwitter() {
		// Check if already logged in
		if (!isTwitterLoggedInAlready()) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
			Configuration configuration = builder.build();

			TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();


			if(!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)) {
				try {
					requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
					this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
				} catch (TwitterException e) {
					e.printStackTrace();
				}
			}
			else
			{
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {	
							requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
							TwitterFragment.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
						} catch (TwitterException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}

		} else {
			// user already logged into twitter
			// Hide login button
			btnLoginTwitter.setVisibility(View.GONE); 

			// Show Update Twitter
			btnUpdateStatus.setVisibility(View.VISIBLE);
			btnLogoutTwitter.setVisibility(View.VISIBLE);
//			tweets.setVisibility(View.VISIBLE);
			
		}
	}

	class getTwitterFeed extends AsyncTask<String, String, ArrayList<Update>>{
		twitter4j.Status minStat;
		List<twitter4j.Status> homeTimeLisne;
		
		@Override
		protected ArrayList<Update> doInBackground(String... args) {
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

				// Access Token 
				String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
				// Access Token Secret
				String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

				AccessToken accessToken = new AccessToken(access_token, access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

				// Update statusget
				homeTimeLisne = twitter.getHomeTimeline();
				
				for(twitter4j.Status status : homeTimeLisne){
					updates.add(new TwitterPost(status));
				}
			} catch (TwitterException e) {
				// Error in updating status
				Log.d("Twitter getHomeTimeline Error", e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(ArrayList<Update> result) {
			super.onPostExecute(result);
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					tweetlist.setVisibility(View.VISIBLE);
					updateAdapter.notifyDataSetChanged();
				}
			});
		}	
	}
	
	/**
	 * Function to update status
	 * */
	class updateTwitterStatus extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(TwitterFragment.this);
			pDialog.setMessage("Updating to twitter...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Places JSON
		 * */
		@Override
		protected String doInBackground(String... args) {
			Log.d("Tweet Text", "> " + args[0]);
			String status = args[0];
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

				// Access Token 
				String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
				// Access Token Secret
				String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

				AccessToken accessToken = new AccessToken(access_token, access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

				// Update status
				twitter4j.Status response = twitter.updateStatus(status);
				List<twitter4j.Status> homeTimeLisne = twitter.getHomeTimeline();
				
				Log.d("Status", "> " + response.getText());
			} catch (TwitterException e) {
				// Error in updating status
				Log.d("Twitter Update Error", e.getMessage());
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog and show
		 * the data in UI Always use runOnUiThread(new Runnable()) to update UI
		 * from background thread, otherwise you will get error
		 * **/
		@Override
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(),
							"Status tweeted successfully", Toast.LENGTH_SHORT)
							.show();

				}
			});
		}

	}

	/**
	 * Function to logout from twitter
	 * It will just clear the application shared preferences
	 * */
	private void logoutFromTwitter() {
		// Clear the shared preferencesL
		Editor e = mSharedPreferences.edit();
		e.remove(PREF_KEY_OAUTH_TOKEN);
		e.remove(PREF_KEY_OAUTH_SECRET);
		e.remove(PREF_KEY_TWITTER_LOGIN);
		e.commit();

		// After this take the appropriate action
		// I am showing the hiding/showing buttons again
		// You might not needed this code
		btnLogoutTwitter.setVisibility(View.GONE);
		btnUpdateStatus.setVisibility(View.GONE);
		btnLoginTwitter.setVisibility(View.VISIBLE);
		tweetlist.setVisibility(View.GONE);
		
	}

	/**
	 * Check user already logged in your application using twitter Login flag is
	 * fetched from Shared Preferences
	 * */
	private boolean isTwitterLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(isTwitterLoggedInAlready()){
			btnLoginTwitter.setVisibility(View.GONE); 

			btnUpdateStatus.setVisibility(View.VISIBLE);
			btnLogoutTwitter.setVisibility(View.VISIBLE);
			new getTwitterFeed().execute();
			//tweets.setVisibility(View.VISIBLE);
		}
	}
	
	

}
