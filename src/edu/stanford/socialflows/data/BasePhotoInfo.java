package edu.stanford.socialflows.data;

import com.restfb.Facebook;
import com.restfb.types.Photo;

public class BasePhotoInfo extends Photo {

	@Facebook("aid")
	String aid = null;
	
	@Facebook("pid")
	String pid = null;
	
	@Facebook("owner")
	long owner = 0;
	
	@Facebook("src")
	private String imgUrl     = null; /* medium-size */
	@Facebook("src_height")
	private Integer imgHeight = null;
	@Facebook("src_width")
	private Integer imgWidth  = null;
	
	@Facebook("src_big")
	private String imgBigUrl     = null;
	@Facebook("src_big_height")
	private Integer imgBigHeight = null;
	@Facebook("src_big_width")
	private Integer imgBigWidth  = null;
	
	@Facebook("src_small")
	private String imgSmallUrl     = null; /* thumbnail */
	@Facebook("src_small_height")
	private Integer imgSmallHeight = null;
	@Facebook("src_small_width")
	private Integer imgSmallWidth  = null;
	
	
	public void setAid(String aid) {
		this.aid = aid;
	}
	
	public String getAid() {
		return this.aid;
	}
	
	public void setPid(String pid) {
		this.pid = pid;
	}
	
	public String getPid() {
		return this.pid;
	}
	
	public void setOwner(long owner) {
		this.owner = owner;
	}
	
	public long getOwner() {
		return this.owner;
	}

	/**
	 * The album-sized view of the photo.
	 * 
	 * @return The album-sized view of the photo.
	 */
	public String getPicture() {
		return this.imgUrl;
	}
	public Integer getPictureWidth() {
		return this.imgWidth;
	}
	public Integer getPictureHeight() {
		return this.imgHeight;
	}
	
	
	/**
	 * The full-sized source of the photo.
	 * 
	 * @return The full-sized source of the photo.
	 */
	public String getSource() {
		return this.imgBigUrl;
	}
	public Integer getSourceWidth() {
		return this.imgBigWidth;
	}
	public Integer getSourceHeight() {
		return this.imgBigHeight;
	}
	
	  
	/**
	 * The icon-sized source of the photo.
	 * 
	 * @return The icon-sized source of the photo.
	 */
	public String getIcon() {
		return this.imgSmallUrl;
	}
	public Integer getIconWidth() {
		return this.imgSmallWidth;
	}
	public Integer getIconHeight() {
		return this.imgSmallHeight;
	}

}
