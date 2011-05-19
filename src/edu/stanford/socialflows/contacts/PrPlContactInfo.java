package edu.stanford.socialflows.contacts;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONObject;

import edu.stanford.prpl.api.Identity;

//import android.graphics.Bitmap;

/*
 * Could represent:
 * (a) PrPl Identity object (must be a PrPl user)
 * (b) Person Resource object (created from harvesting Facebook or Gmail)
 * (c) Account Resource object (created from harvesting Facebook)
 */
public class PrPlContactInfo extends SocialFlowsContactInfo
{
	//Properties
	public static final String PRPL_ID = "identity";
	public static final String ALIAS = "alias";
	public static final String SOCIALWEIGHT = "SocialWeight";
	public static final String FB_UID = "fbUid";
	public static final String FB_PROFILEURL = "fbProfileUrl";
	public static final String SELECTED_PROFILEPHOTO = "selectedProfilePhoto";
	public static final String SQUARE_THUMBNAILHTTPURL = "profilePicSquare";

	//Property URIs
	public static final String PRPL_ID_URI = "http://prpl.stanford.edu/#identity";
	public static final String ALIAS_URI = "http://prpl.stanford.edu/#alias";
	public static final String SOCIALWEIGHT_URI = "http://prpl.stanford.edu/#SocialWeight";
	public static final String FB_UID_URI = "http://prpl.stanford.edu/#fbUid";
	public static final String FB_PROFILEURL_URI = "http://prpl.stanford.edu/#fbProfileUrl";
	public static final String SELECTED_PROFILEPHOTO_URI = "http://prpl.stanford.edu/#selectedProfilePhoto";
	public static final String SQUARE_THUMBNAILHTTPURL_URI = "http://prpl.stanford.edu/#profilePicSquare"; // "http://prpl.stanford.edu/#squareThumbnailHttpURL"

	String resourceURI = null;

	String selectedPic_squareThumbnailURL = null,
	       selectedPic_thumbnailURL = null, 
           selectedPic_mediumURL = null, 
           selectedPic_bigURL = null;
	
	 // For contacts that are also PrPl users
	Identity prplIdentity = null; /* deprecated */
	String prplIdKey = null;
	
	public String type = null;
	
	
	public PrPlContactInfo()
	{ super(); }
	
	/*
	public PrPlContactInfo(BaseContactInfo u)
	{
		super();
		this.setFirstName(u.getFirstName());
		this.setLastName(u.getLastName());
		this.setName(u.getName());
		// this.setUid(u.getUid());
		//this.setPicSquare(u.getPicSquare());
		//this.setPic(u.getPic());
		//this.setPicBig(u.getPicBig());
		//this.setPicSmall(u.getPicSmall());
		// this.setProfileUrl(u.getProfileUrl());
		// this.setProfileUpdateTime(u.getProfileUpdateTime());
	}
	*/
	

	public boolean isAPrPlFriend()
	{
		if (this.prplIdentity == null && this.prplIdKey == null)
			return false;
		return true;
	}

	public void setResourceType(String resType)
	{
		this.type = resType;
	}
	
	public String getResourceType()
	{
		return this.type;
	}
	
	public void setPrPlIdKey(String prplIdKey)
	{
		this.prplIdKey = prplIdKey;
	}

	public String getPrPlIdKey()
	{
		if (this.prplIdKey == null && this.prplIdentity != null)
			this.prplIdKey = this.prplIdentity.getKey();
		return this.prplIdKey;
	}
	
	public void setPrPlIdentity(Identity prplId)
	{
		this.prplIdentity = prplId;
		if (prplId != null) {
		   this.prplIdKey = prplId.getKey();
		   setResourceType("Identity");
		}
	}
	
	public Identity getPrPlIdentity()
	{
		return this.prplIdentity;
	}

	public void setResourceURI(String resourceURI) {
		this.resourceURI = resourceURI;
	}
	
	public String getResourceURI() {
		return this.resourceURI;
	}

	public static Identity matchPrPlIdentity(PrPlContactInfo contact, HashMap<String, Identity> identityMap)
	{
		if (contact == null || contact.emailAddresses == null)
			return null;
		Iterator<String> emailIter = contact.emailAddresses.iterator();
		while (emailIter.hasNext()) {
			Identity prplId = identityMap.get(emailIter.next());
			if (prplId != null)
				return prplId;
		}
		return null;
	}
	
	@Override
	public boolean equals(Object o)
	{
		PrPlContactInfo info = (PrPlContactInfo)o;
		
		if (this.prplIdentity != null && info.prplIdentity != null) {
			return this.prplIdentity.getURI().equals(info.prplIdentity.getURI());
		}
		if (this.prplIdKey != null && info.prplIdKey != null) {
			return this.prplIdKey.equals(info.prplIdKey);
		}
		
		if (info.getName() == null)
			return false;
		return info.getName().equals(this.getName());
	}



	// Selected Photo accessor & setter methods
	public void setSelectedPicSquare(String picSquareURL) {
		this.selectedPic_squareThumbnailURL = picSquareURL;
	}
	
	public String getSelectedPicSquare() {
		return this.selectedPic_squareThumbnailURL;
	}
	
	public void setSelectedPic(String picURL) {
		this.selectedPic_mediumURL = picURL;
	}
	
	public String getSelectedPic() {
		return this.selectedPic_mediumURL;
	}
	
	public void setSelectedPicBig(String picBigURL) {
		this.selectedPic_bigURL = picBigURL;
	}
	
	public String getSelectedPicBig() {
		return this.selectedPic_bigURL;
	}
	
	public void setSelectedPicSmall(String picSmallURL) {
		this.selectedPic_thumbnailURL = picSmallURL;
	}
	
	public String getSelectedPicSmall() {
		return this.selectedPic_thumbnailURL;
	}

}