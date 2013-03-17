package edu.ucsb.aggregator;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.facebook.widget.ProfilePictureView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UpdateAdaptor extends ArrayAdapter<Update> {
	
	private int resource;
	
	public UpdateAdaptor(Context context, int resource,	List<Update> objects) {
		super(context, resource, objects);
		this.resource = resource;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout postView;
		Update tweet = getItem(position);

		// Inflate view
		LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		postView = (LinearLayout) vi.inflate(resource, null);

		// Find all elements in view
		Log.i("moster", "logger");
			if(tweet.getProfilePictureUrl() != null){
				ImageView profile_pic = (ImageView)postView.findViewById(R.id.userImg);
				Log.i("Kake", "Downloading: " + tweet.getProfilePictureUrl());
				profile_pic.setTag(tweet.getProfilePictureUrl());
				profile_pic.setVisibility(View.VISIBLE);
				new DownloadFile().execute(profile_pic);
				//	new DownloadImageTask(profile_pic).execute();
			}

		//ImageView post_picture = (ImageView) postView.findViewById(R.id.post_picture);
		TextView postTitle = (TextView) postView.findViewById(R.id.userScreen);
		TextView postMessage = (TextView) postView.findViewById(R.id.updateText);
		//TextView countComment = (TextView) postView.findViewById(R.id.count_comments);
		TextView updatedTime = (TextView) postView.findViewById(R.id.updateTime);
		
		postTitle.setText(tweet.getTitle());

		if (tweet.getImageUrl() != null){
			//Log.i(TAG, "Downloading: " + post.getImageUrl());
			//post_picture.setTag(post.getImageUrl());
			//post_picture.setVisibility(View.VISIBLE);
			//new DownloadImageTask(post_picture).execute();
		}
		else {
			//Log.i(TAG, "No image... " + post.getTitle());
			//post_picture.setVisibility(View.GONE);
		}
		postMessage.setText(tweet.getMessage());
		//countComment.setText(post.getCountComment() + "");
		updatedTime.setText(tweet.getUpdatedTime());

		return postView;
	}
	
	class DownloadFile extends AsyncTask<ImageView, Integer, Long> {

		Bitmap mIcon1;
		ImageView picture;

		protected Long doInBackground(ImageView... imgs) {
			
			
			try {
				URL url = new URL((String) imgs[0].getTag());
				picture = imgs[0];
				mIcon1 = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				

			} catch (IOException e) {
				// TODO Auto-generated catch block[
				                                   e.printStackTrace();
			}

			return null;

		}



		protected void onPostExecute(Long result) {
			picture.setImageBitmap(mIcon1);
		}

	}

}
