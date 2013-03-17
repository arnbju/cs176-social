
package edu.ucsb.aggregator;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;

import com.facebook.widget.ProfilePictureView;


public class SelectionFragment extends Fragment {


	private static final String TAG = "SelectionFragment";
	private static final String POST_ACTION_PATH = "me/fb_sample_scrumps:eat";
	private static final String PENDING_ANNOUNCE_KEY = "pendingAnnounce";
	private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");

	private static final int REAUTH_ACTIVITY_CODE = 100;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");

	private ProfilePictureView profilePictureView;
	private ProfilePictureView profilePicture;
	private TextView userNameView;
	//private TextView fstorry;
	private ImageView user_picture;

	private TextView feedText;
	private Button fbutton;
	private TextView likes;
	private TextView time;
	
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(final Session session, final SessionState state, final Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.selection, container, false);

		profilePicture = (ProfilePictureView) view.findViewById(R.id.friend_profile_pic);
		profilePictureView = (ProfilePictureView) view.findViewById(R.id.selection_profile_pic);
		profilePictureView.setCropped(true);
		userNameView = (TextView) view.findViewById(R.id.selection_user_name);
		feedText = (TextView) view.findViewById(R.id.feed);
		user_picture = (ImageView) view.findViewById(R.id.imageView);
		likes = (TextView) view.findViewById(R.id.like);
		time = (TextView) view.findViewById(R.id.time);
		
		
		fbutton = (Button) view.findViewById(R.id.fbutton);
		fbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				refreshFeed();

			}
		});

		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REAUTH_ACTIVITY_CODE) {
			uiHelper.onActivityResult(requestCode, resultCode, data);
		} 
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		uiHelper.onSaveInstanceState(bundle);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
		if (session != null && session.isOpened()) {
			if (state.equals(SessionState.OPENED_TOKEN_UPDATED)) {

			} else {
				makeMeRequest(session);
				//getFeedRequest(session);
			}
		}
	}

	private void makeMeRequest(final Session session) {
		Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
			@Override
			public void onCompleted(GraphUser user, Response response) {
				if (session == Session.getActiveSession()) {
					if (user != null) {
						profilePictureView.setProfileId(user.getId());
						userNameView.setText(user.getName());

					}
				}
				if (response.getError() != null) {
					handleError(response.getError());
				}
			}
		});
		request.executeAsync();

	}

	private void refreshFeed(){
		Session session = Session.getActiveSession();
		getFeedRequest(session);
	}



	private void handleError(FacebookRequestError error) {
		DialogInterface.OnClickListener listener = null;
		String dialogBody = null;

		if (error == null) {
			dialogBody = getString(R.string.error_dialog_default_text);
		} else {
			switch (error.getCategory()) {
			case AUTHENTICATION_RETRY:
				// tell the user what happened by getting the message id, and
				// retry the operation later
				String userAction = (error.shouldNotifyUser()) ? "" :
					getString(error.getUserActionMessageId());
				dialogBody = getString(R.string.error_authentication_retry, userAction);
				listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						Intent intent = new Intent(Intent.ACTION_VIEW, M_FACEBOOK_URL);
						startActivity(intent);
					}
				};
				break;

			case AUTHENTICATION_REOPEN_SESSION:
				// close the session and reopen it.
				dialogBody = getString(R.string.error_authentication_reopen);
				listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						Session session = Session.getActiveSession();
						if (session != null && !session.isClosed()) {
							session.closeAndClearTokenInformation();
						}
					}
				};
				break;

			case PERMISSION:
				// request the publish permission
				dialogBody = getString(R.string.error_permission);
				listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						//pendingAnnounce = true;
						//requestPublishPermissions(Session.getActiveSession());
					}
				};
				break;

			case SERVER:
			case THROTTLING:
				// this is usually temporary, don't clear the fields, and
				// ask the user to try again
				dialogBody = getString(R.string.error_server);
				break;

			case BAD_REQUEST:
				// this is likely a coding error, ask the user to file a bug
				dialogBody = getString(R.string.error_bad_request, error.getErrorMessage());
				break;

			case OTHER:
			case CLIENT:
			default:
				// an unknown issue occurred, this could be a code error, or
				// a server side issue, log the issue, and either ask the
				// user to retry, or file a bug
				dialogBody = getString(R.string.error_unknown, error.getErrorMessage());
				break;
			}
		}

		new AlertDialog.Builder(getActivity())
		.setPositiveButton(R.string.error_dialog_button_text, listener)
		.setTitle(R.string.error_dialog_title)
		.setMessage(dialogBody)
		.show();
	}

	//henter newsfeed fra facebook
	private void getFeedRequest(final Session session) { // må ta inn from også
		//Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
		Log.i(TAG, "Kom hit");  
		Request request = Request.newGraphPathRequest(session, "/me/home", new Request.Callback() {


			@Override
			public void onCompleted(Response response) {
				GraphObject fpost = response.getGraphObject();

				if(fpost == null){
					feedText.setText("Query failed");
				}
				else{

					// Example: typed access (name)
					// - no special permissions required

					feedText.setText("");
					likes.setText("");
					time.setText("");

					JSONArray posts = (JSONArray) fpost.getProperty("data");

					try {
						displayRequest(posts.getJSONObject(0));
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (response.getError() != null) {
					handleError(response.getError());
				}
			}
		});
		request.executeAsync();

	}

	//method for displaying a facebook message
	public void displayRequest(JSONObject post){
		//feedText.setText("");

		profilePicture.setCropped(true);

		try {
			profilePicture.setProfileId(post.getJSONObject("from").getString("id"));


			feedText.append(post.getJSONObject("from").getString("name") );
			feedText.append("\n");



			//feedText.append("\n\n");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}


		try {
			feedText.append(post.getString("message"));
			feedText.append("\n");
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			feedText.append(post.getString("story"));
			feedText.append("\n");
		} catch (Exception e) {
			// TODO: handle exception
		}

		try {

			URL img_value = new URL(post.getString("picture"));
			new DownloadFile().execute(img_value);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			time.append(post.getString("created_time"));
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			likes.append("Likes: ");
			likes.append(post.getJSONObject("likes").getString("count"));
			
		} catch (Exception e) {
			likes.append("0");
			// TODO: handle exception
		}
		try {
			likes.append("\n");
			likes.append("Comments: ");
			likes.append(post.getJSONObject("comments").getString("count"));
		} catch (Exception e) {
			// TODO: handle exception
		}
	

	}

	 class DownloadFile extends AsyncTask<URL, Integer, Long> {

		 	Bitmap mIcon1;
		 	
			protected Long doInBackground(URL... urls) {
				
				try {
					mIcon1 = BitmapFactory.decodeStream(urls[0].openConnection().getInputStream());
					
				} catch (IOException e) {
					// TODO Auto-generated catch block[
					e.printStackTrace();
				}
				
				return null;

			}
		
			

			protected void onPostExecute(Long result) {
				user_picture.setImageBitmap(mIcon1);
			}

		}

}



