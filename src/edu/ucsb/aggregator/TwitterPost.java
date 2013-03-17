package edu.ucsb.aggregator;


import twitter4j.Status;

public class TwitterPost extends Update {

	public TwitterPost(Status status) {
		
		setTitle(status.getUser().getName());
		setMessage(status.getText());
		
//		setProfilePictureUrl(status.getUser().getBiggerProfileImageURL());
		setProfilePictureUrl(status.getUser().getProfileImageURL().toString());
		setUpdatedTime(status.getCreatedAt().toString());
		
	}

}
