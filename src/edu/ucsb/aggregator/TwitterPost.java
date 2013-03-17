package edu.ucsb.aggregator;

import twitter4j.MediaEntity;
import twitter4j.Status;

public class TwitterPost extends Update {

	public TwitterPost(Status status) {
		
		setTitle(status.getUser().getName());
		setMessage(status.getText());
		
		//Retweets
//		setCountCommet(status.getRetweetCount());
//		if(status.getMediaEntities() != null) {
//			if (status.getMediaEntities().length > 0 ){
//				MediaEntity[] mediaEntities = status.getMediaEntities();
//				setImageUrl(mediaEntities[0].getMediaURL());				
//			}
//		}

//		setProfilePictureUrl(status.getUser().getBiggerProfileImageURL());
		setProfilePictureUrl(status.getUser().getProfileImageURL().toString());
		setUpdatedTime(status.getCreatedAt().toString());
		
	}

}
