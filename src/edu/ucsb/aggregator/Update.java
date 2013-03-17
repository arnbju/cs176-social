package edu.ucsb.aggregator;

public abstract class Update {
	
	private String userId;
	private String title;
	private String content;
	private String time;
	private String imageUrl = null;
	private long countComment;
	private String profilePictureUrl = null;

	private int countLikes;
	
	protected void setUserId(String userId){
		this.userId = userId;
	}
	
	public String getUserId(){
		return userId;
	}
	
	protected void setTitle(String title){
		this.title = title;
	}
	
	public String getTitle(){
		return title;
	}
	
	protected void setMessage(String message){
		this.content = message;
	}
	
	public String getMessage(){
		return content;
	}
	
	protected void setUpdatedTime(String updatedTime){
		this.time = updatedTime;
		
	}
	public String getUpdatedTime(){
		return time;
	}
		
	protected void setCountLikes(int countLikes){
		this.countLikes = countLikes;
		
	}
	public int getCountLikes(){
		return countLikes;
	}
	
	protected void setCountCommet(long l){
		this.countComment = l;
	}
	public long getCountComment(){
		return countComment;
	}
	
	protected void setImageUrl(String imageUrl){
		this.imageUrl = imageUrl;
		
	}
	public String getImageUrl(){
		return imageUrl;
	}

	protected void setProfilePictureUrl(String url) {
		this.profilePictureUrl = url;
	}
	
	public String getProfilePictureUrl() {
		return profilePictureUrl;
	}
}
