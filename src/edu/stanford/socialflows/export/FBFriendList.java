package edu.stanford.socialflows.export;

import java.util.List;
import com.restfb.Facebook;

public class FBFriendList 
{
	@Facebook("name")
	private String name = null;
	
	@Facebook("id")
	private String flId = null;
	
	List<FBFriend> friends = null;
	
	
	public void setFriendListName(String name)
	{
		this.name = name;
	}
	
	public String getFriendListName()
	{
		return this.name;
	}
	
	public void setFriendListId(String flId)
	{
		this.flId = flId;
	}
	
	public String getFriendListId()
	{
		return this.flId;
	}
	
	public void setFriends(List<FBFriend> friends)
	{
		this.friends = friends;
	}
	
	public List<FBFriend> getFriends()
	{
		return this.friends;
	}
	
}
