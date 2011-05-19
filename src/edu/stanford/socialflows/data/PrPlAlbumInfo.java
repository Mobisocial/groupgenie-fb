package edu.stanford.socialflows.data;

public class PrPlAlbumInfo extends BaseAlbumInfo 
{
	//Properties
	public static final String PHOTOALBUM = "PhotoAlbum";
	public static final String PHOTO      = "photo";
	public static final String ALBUMCOVER = "albumCover";
	public static final String ALBUMSIZE  = "albumSize";
	public static final String FBAID      = "fbAid";
	public static final String FBLINK     = "fbLink";
	public static final String FBMODIFIED = "fbModified";
	
	//Property URIs	
	public static final String PHOTOALBUM_URI = "http://prpl.stanford.edu/#PhotoAlbum";
	public static final String PHOTO_URI      = "http://prpl.stanford.edu/#photo";
	public static final String ALBUMCOVER_URI = "http://prpl.stanford.edu/#albumCover";
	public static final String ALBUMSIZE_URI  = "http://prpl.stanford.edu/#albumSize";
	public static final String FBAID_URI      = "http://prpl.stanford.edu/#fbAid";
	public static final String FBLINK_URI     = "http://prpl.stanford.edu/#fbLink";
	public static final String FBMODIFIED_DATE_URI = "http://prpl.stanford.edu/#fbModified";

	String resourceURI = null;

	
	public PrPlAlbumInfo()
	{ super(); }
	
	public void setResourceURI(String resourceURI) {
		this.resourceURI = resourceURI;
	}
	
	public String getResourceURI() {
		return this.resourceURI;
	}

}
