package edu.stanford.socialflows.contacts;

import com.restfb.Facebook;

/*
 * Very basic info about a social contact
 */
public class BaseContactInfo
{
	@Facebook("name")
	private String fullname   = null;

	@Facebook("first_name")
	private String firstname  = null;
	
	@Facebook("last_name")
	private String lastname   = null;
	
	@Facebook("profile_url")
	private String profileUrl = null;
	
	@Facebook("pic")
	private String profileImgUrl       = null; /* medium-size */
	
	@Facebook("pic_big")
	private String profileImgBigUrl    = null;
	
	@Facebook("pic_small")
	private String profileImgSmallUrl  = null; /* thumbnail */
	
	@Facebook("pic_square")
	private String profileImgSquareUrl = null; /* square-thumbnail */
	
	@Facebook("uid")
	private String fbUid = null;
	private String dummyVal = null;
	

	/*
	public BaseContactInfo(String name, String profileImgUrl, String type, JSONObject data)
	{
		this.fullname = name;
		this.profileImgUrl = profileImgUrl;
		this.type = type;
		this.contactData = data;
	}
	
	public BaseContactInfo(String name, String profileImgUrl, int socialWeight, 
						   String type, JSONObject data)
	{
		this.fullname = name;
		this.profileImgUrl = profileImgUrl;
		this.socialWeight = socialWeight;
		this.type = type;
		this.contactData = data;
	}
	
	public BaseContactInfo(String name, String firstname, String lastname, 
			               String profileImgUrl, String type, JSONObject data)
	{
		this.fullname = name;
		this.firstname = firstname;
		this.lastname = lastname;
		this.profileImgUrl = profileImgUrl;
		this.type = type;
		this.contactData = data;
	}
	*/

	public void setName(String name)
	{
		this.fullname = name;
	}
	
	public String getName()
	{
		return this.fullname;
	}

	public void setFirstName(String firstname)
	{
		this.firstname = firstname;
	}
	
	public String getFirstName()
	{
		return this.firstname;
	}
	
	public void setLastName(String lastname)
	{
		this.lastname = lastname;
	}
	
	public String getLastName()
	{
		return this.lastname;
	}
	
	public void setProfileUrl(String url)
	{
		this.profileUrl = url;
	}
	
	public String getProfileUrl()
	{
		return this.profileUrl;
	}
		
	public void setPic(String picUrl)
	{
		this.profileImgUrl = picUrl;
	}
	
	public String getPic()
	{
		return this.profileImgUrl;
	}
	
	public void setPicBig(String picUrl)
	{
		this.profileImgBigUrl = picUrl;
	}
	
	public String getPicBig()
	{
		return this.profileImgBigUrl;
	}
	
	public void setPicSmall(String picUrl)
	{
		this.profileImgSmallUrl = picUrl;
	}
	
	public String getPicSmall()
	{
		return this.profileImgSmallUrl;
	}

	public void setPicSquare(String picUrl)
	{
		this.profileImgSquareUrl = picUrl;
	}
	
	public String getPicSquare()
	{
		return this.profileImgSquareUrl;
	}
	
	public void setFBUid(String fbUid)
	{
		this.fbUid = fbUid;
	}
	
	public String getFBUid()
	{
		return this.fbUid;
	}

	
	public void setDummyVal(String val)
	{
		this.dummyVal = val;
	}
	
	public String getDummyVal()
	{
		return this.dummyVal;
	}
	
}