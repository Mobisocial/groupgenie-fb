package edu.stanford.socialflows.export;

import java.net.*;
import java.io.*;
import java.util.*;
import org.json.*;
import com.google.gdata.client.*;
import com.google.gdata.client.http.*;
import com.google.gdata.client.contacts.*;
import com.google.gdata.data.*;
import com.google.gdata.data.contacts.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;

import edu.stanford.socialflows.connector.FBService;
import edu.stanford.socialflows.contacts.SocialFlowsContactInfo;
import edu.stanford.socialflows.sticker.Sticker;
import edu.stanford.socialflows.util.StatusProvider;

public class FBFriendListExporter implements StatusProvider 
{
	FBService fbService = null;
	
    // Build up list of user's current Facebook Friend Lists
    HashMap<String, FBFriendList> nameToFacebookFriendList
    = new HashMap<String, FBFriendList>();
    HashMap<String, FBFriendList> idToFacebookFriendList
    = new HashMap<String, FBFriendList>();
    HashMap<String, HashSet<GoogleContact>> idToGoogleContactGroupMembership
    = new HashMap<String, HashSet<GoogleContact>>();
    
    // Build up list of user's current Google addressbook
    HashMap<String, GoogleContact> emailToGoogleContact
    = new HashMap<String, GoogleContact>();
    
	
    public FBFriendListExporter(FBService service)
    {
    	this.fbService = service;
    }
	
    public void initFriendLists()
    {
        // Obtain the list of existing FB Friend Lists for current user
        setStatusMessage("Obtaining your existing Facebook friend lists...");
        List<FBFriendList> fbFriendLists = fbService.getFriendLists();
        for (FBFriendList fl : fbFriendLists) {
        	 fbService.getFriendListMembers(fl);
        	 nameToFacebookFriendList.put(fl.getFriendListName().toLowerCase(), 
        			                      fl);
        	 idToFacebookFriendList.put(fl.getFriendListId(), fl);
        	 
        	 // Print the results
             System.out.println(fl.getFriendListName()+" ("+fl.getFriendListId()+") ");
        }
    }



	public String exportToFacebook(Sticker sticker)
	{
		if (sticker == null)
			return null;
		
	    System.out.println("\n\n\n");
	    
	    HashSet<String> fbFriendsToUpdate 
	    = new HashSet<String>();
	    
	    FBFriendList friendList
        = nameToFacebookFriendList.get(sticker.getStickerName().toLowerCase());
        if (friendList == null) 
        {
        	// Create a new friend list
        	this.setStatusMessage("Creating Facebook Friend List for <b>"+sticker.getStickerName()+"</b>...");
        	String flId = fbService.createFriendList(sticker.getStickerName());
        	if (flId == null || flId.trim().length() <= 0) {
        		System.out.println("Error trying to create new FB Friend List '"+sticker.getStickerName()+"'");
        		return null;
        	}
        	
        	friendList = new FBFriendList();
        	friendList.setFriendListId(flId);
        	friendList.setFriendListName(sticker.getStickerName());
        	nameToFacebookFriendList.put(sticker.getStickerName().toLowerCase(), 
        			                     friendList);
            idToFacebookFriendList.put(friendList.getFriendListId(), 
            		                   friendList);
        }


        // Update membership for given friend list
        if (sticker.getClique().getGroupMembers() != null)
        {
        	HashSet<String> previousCliqueMembers = new HashSet<String>();
        	if (friendList.getFriends() != null) {
        		for (FBFriend f : friendList.getFriends())
        			previousCliqueMembers.add(f.getId());
        	}
        	
        	HashSet<String> currentCliqueMembers = new HashSet<String>();
            HashSet<String> addCliqueMembers     = new HashSet<String>();

            // Figure out which FB friend to add and delete
        	for (SocialFlowsContactInfo sfContact : sticker.getClique().getGroupMembers())
        	{
        		if (sfContact.getFBUids() == null)
 				   continue;
 			    if (sfContact.getFBUids().iterator().hasNext()) {
 				   String fbUid = sfContact.getFBUids().iterator().next();
 				   currentCliqueMembers.add(fbUid);
	        	   if (!previousCliqueMembers.contains(fbUid))
	        		   addCliqueMembers.add(fbUid);
 			    }
        	} // end-of-loop to update contacts
        	

        	System.out.println("Updating Facebook Friend List for '"+sticker.getStickerName()+"'");
        	this.setStatusMessage("Updating Facebook Friend List for <b>"+sticker.getStickerName()+"</b>...");
        	
        	// Now update the Facebook friend list memberships
        	Vector<String> removeMembers = new Vector<String>();
        	for (String f : previousCliqueMembers)
        	{
        		// Remove group membership for this contact
        		if (!currentCliqueMembers.contains(f))
        			removeMembers.add(f);
        		if (removeMembers.size() > 0)
        		   fbService.deleteFriendListMembers(friendList.getFriendListId(),
        				                             removeMembers);
        	}
        	
        	// Add group members
        	Vector<String> addMembers = new Vector<String>(addCliqueMembers);
        	if (addMembers.size() > 0)
        		fbService.addFriendListMembers(friendList.getFriendListId(),
        				                       addMembers);
        	
        	//this.setStatusMessage("Added <b>"+socialContact.entry.getName().getFullName().getValue()+"</b> "
		    //                      +" to <b>"+sticker.getStickerName()+"</b> contact group...");
            //System.out.println("--> +ADDED "+socialContact.entry.getName().getFullName().getValue());

        } // end-of-updating group membership for a sticker

        return friendList.getFriendListId();
	}



	public String statusMessage = "";
	
	public void setStatusMessage(String msg) /* synchronized */
	{
	    this.statusMessage = msg;	
	}
	
	public String getStatusMessage()
	{
		return new String(this.statusMessage);
	}
	
}
