package edu.stanford.socialflows.data;

import java.math.BigDecimal;

import com.restfb.Facebook;

public class BasePhotoTagInfo extends com.restfb.types.Photo.Tag implements Comparable<BasePhotoTagInfo> {

	@Facebook("pid")
	String pid = "";
	
	@Facebook("subject")
	String subject = ""; // for FB tagged photos, usually FB uids
	
	@Facebook("text")
	String text = "";
	
	@Facebook("created")
	long created = 0;
	
	@Facebook("xcoord")
	BigDecimal xcoord = null;
	
	@Facebook("ycoord")
	BigDecimal ycoord = null;

	
	public void setPid(String pid) {
		this.pid = pid;
	}
	
	public String getPid() {
		return this.pid;
	}
		
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getSubject() {
		return this.subject;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return this.text;
	}
	
	public void setCreated(long created) {
		this.created = created;
	}
	
	public long getCreated() {
		return this.created;
	}
	
	public void setXcoord(BigDecimal xcoord) {
		this.xcoord = xcoord;
	}
	
	public BigDecimal getXcoord() {
		return this.xcoord;
	}

	public void setYcoord(BigDecimal ycoord) {
		this.ycoord = ycoord;
	}
	
	public BigDecimal getYcoord() {
		return this.ycoord;
	}

	
	public int compareTo(BasePhotoTagInfo o)
	{
		int photoOrder = this.getPid().compareTo(o.getPid());
		if (photoOrder != 0)
			return photoOrder;

		if (this.getSubject() != null && o.getSubject() != null)
		{
			int fbIdOrder = this.getSubject().compareTo(o.getSubject());	
			if (fbIdOrder != 0)
				return fbIdOrder;
		}
		return this.getText().compareToIgnoreCase(o.getText());
	}
	
}
