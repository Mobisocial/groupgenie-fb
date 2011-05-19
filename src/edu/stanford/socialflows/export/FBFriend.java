package edu.stanford.socialflows.export;

import com.restfb.Facebook;

public class FBFriend 
{
	@Facebook("name")
	private String name = null;
	
	@Facebook("id")
	private String id = null;
	
	public String getName()
	{ return this.name; }
	
	public void setName(String name)
	{ this.name = name; }
	
	public String getId()
	{ return this.id; }
	
	public void setId(String id)
	{ this.id = id; }
}
