package edu.stanford.socialflows.data;

import com.restfb.Facebook;

public class BaseAlbumInfo implements Comparable<BaseAlbumInfo>
{
	@Facebook("aid")
	//@Facebook("id")
	String aid = null;
	
	@Facebook("cover_pid")
	String coverPid = null;
	
	@Facebook("name")
	String name = null;
	
	@Facebook("description")
	String description = null;
	
	@Facebook("link")
	String link = null;
	
	@Facebook("location")
	String location = null;
	
	@Facebook("created")
	//@Facebook("created_time")
	String createdTimeStr = null;
	
	long createdTime = 0;
	
	@Facebook("modified")
	//@Facebook("updated_time")
	String modifiedTimeStr = null;
	
	long modifiedTime = 0;
	
	@Facebook("owner")
	long owner = 0;
	
	@Facebook("size")
	//@Facebook("count")
	int size = 0;
	
	String cover_thumbnailURL = null;
    String cover_albumViewURL = null;
    String cover_URL = null;
	
	/*
	public PrPlAlbumInfo(Album a)
	{
		super();
		this.setAid(a.getAid());
		this.setCoverPid(a.getCoverPid());
		// Date epochTime = new Date(1020);  // created, // modified
		this.setCreated(a.getCreated());
		this.setModified(a.getModified());
		this.setDescription(a.getDescription());
		this.setLink(a.getLink());
		this.setLocation(a.getLocation());
		this.setName(a.getName());
		this.setOwner(a.getOwner());
		this.setSize(a.getSize());
		// this.setProfileUpdateTime(u.getProfileUpdateTime());
	}
	*/
	
	public void setAid(String aid) {
		this.aid = aid;
	}
	
	public String getAid() {
		return this.aid;
	}
	
	public void setCoverPid(String coverPid) {
		this.coverPid = coverPid;
	}
	
	public String getCoverPid() {
		return this.coverPid;
	}
	
	public void setCreated(long createdTime) {
		this.createdTime = createdTime;
	}
	
	public long getCreated()
	{
		if (this.createdTime == 0)
		{
			if (this.createdTimeStr != null 
			    && !this.createdTimeStr.isEmpty())
			{
				try {
					this.createdTime 
					= Long.parseLong(this.createdTimeStr);	
				}
				catch (NumberFormatException nfe) {}
			}
		}
		return this.createdTime;
	}
	
	public void setModified(long modifiedTime) {
		this.modifiedTime = modifiedTime;
	}
	
	public long getModified()
	{
		if (this.modifiedTime == 0)
		{
			if (this.modifiedTimeStr != null 
			    && !this.modifiedTimeStr.isEmpty())
			{
				try {
					this.modifiedTime 
					= Long.parseLong(this.modifiedTimeStr);	
				}
				catch (NumberFormatException nfe) {}
			}
		}
		return this.modifiedTime;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
	
	public String getLink() {
		return this.link;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getLocation() {
		return this.location;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setOwner(long owner) {
		this.owner = owner;
	}
	
	public long getOwner() {
		return this.owner;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public int getSize() {
		return this.size;
	}
	
	
	// Album Cover Photo accessor & setter methods	
	public void setAlbumCover(String picURL) {
		this.cover_albumViewURL = picURL;
	}
	
	public String getAlbumCover() {
		return this.cover_albumViewURL;
	}
	
	// Actually near (or the same as) the original photo size
	public void setAlbumCoverBig(String picBigURL) {
		this.cover_URL = picBigURL;
	}
	
	public String getAlbumCoverBig() {
		return this.cover_URL;
	}
	
	public void setAlbumCoverSmall(String picSmallURL) {
		this.cover_thumbnailURL = picSmallURL;
	}
	
	public String getAlbumCoverSmall() {
		return this.cover_thumbnailURL;
	}
	
	public int compareTo(BaseAlbumInfo o)
	{		
		// Want to have photo albums recently updated appearing higher
		return (int)(this.getModified()-o.getModified()) * (-1);
	}
	
}
