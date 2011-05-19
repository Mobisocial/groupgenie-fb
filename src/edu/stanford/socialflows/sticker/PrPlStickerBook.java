package edu.stanford.socialflows.sticker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import org.json.*;

import edu.stanford.prpl.api.Group;
import edu.stanford.prpl.api.Identity;
import edu.stanford.prpl.api.PRPLAppClient;
import edu.stanford.prpl.api.QueryResultIterator;
import edu.stanford.prpl.api.Resource;
import edu.stanford.prpl.api.QueryResultIterator.Solution;
import edu.stanford.prpl.app.common.resource.type.Person;

import edu.stanford.prpl.impl.client.directory.DirectoryClient;
import edu.stanford.socialflows.contacts.PrPlAddressBook;
import edu.stanford.socialflows.contacts.PrPlContactInfo;
import edu.stanford.socialflows.data.PrPlAlbumInfo;
import edu.stanford.socialflows.util.StatusProvider;


public class PrPlStickerBook implements StatusProvider
{	
	private PRPLAppClient prplService_ = null;
	private DirectoryClient dirService = null;
	
	private List<Sticker> myStickers = null;
	private HashMap<String, Sticker> myStickersByURI = null;
	
	private List<Sticker> savedHierarchicalStickers = null;
	
	/* Deal with the suggested hierarchical groups, suggested stickers */
	private List<Sticker> suggestedStickers = null;
	private HashMap<String, Sticker> suggestedStickersLookup = null;
    private List<SocialFlowsGroup> suggestedHierarchicalGroups = null;
	
	private static final String datalogQuery
	= "STICKERS(?s, ?name)? \n" +
	  "STICKERS(?s, ?name):- " +
	  "  (?s a '<http://prpl.stanford.edu/#Sticker>'), " +
	  "  (?s, '<http://prpl.stanford.edu/#stickerName>', ?name).";
	
	private static final String datalogQueryCompressed
	= "STICKERS(?s, ?name, ?stickerIcon, ?acgroup, ?cliqueMember)? \n" +
	  "STICKERS(?s, ?name, ?stickerIcon, ?acgroup, ?cliqueMember):- " +
	  "  (?s a '<"+Sticker.STICKER_URI+">'), " +
	  "  (?s, '<"+Sticker.STICKERNAME_URI+">', ?name), " +
	  "  (?s, '<"+Sticker.STICKERICON_URI+">', ?stickerIcon), " +
	  "  (?s, '<"+Sticker.PRPL_ACGROUP_URI+">', ?acgroup), " +
	  "  (?s, '<"+Sticker.CLIQUEMEMBER_URI+">', ?cliqueMember). \n" +
	  "STICKERS(?s, ?name, 'blank', ?acgroup, ?cliqueMember):- " +
	  "  (?s a '<"+Sticker.STICKER_URI+">'), " +
	  "  (?s, '<"+Sticker.STICKERNAME_URI+">', ?name), " +
	  "  !(?s, '<"+Sticker.STICKERICON_URI+">', _), " +
	  "  (?s, '<"+Sticker.PRPL_ACGROUP_URI+">', ?acgroup), " +
	  "  (?s, '<"+Sticker.CLIQUEMEMBER_URI+">', ?cliqueMember). ";
	
	
	  /*
	  "ALBUMINFO(?album, ?name, ?size, ?albumCover), PHOTO(?albumCover, ?albumCoverUrl). \n" +
	  "ALBUMINFO(?album, ?name, ?size, ?albumCover):- " +
	  "  (?album a '<http://prpl.stanford.edu/#PhotoAlbum>'), " +
	  "  (?album, '<http://prpl.stanford.edu/#name>', ?name), " +
	  "  (?album, '<http://prpl.stanford.edu/#albumSize>', ?size), " +
	  "  (?album, '<http://prpl.stanford.edu/#albumCover>', ?albumCover). \n" +
	  "PHOTO(?photo, ?photoUrl):- " +
	  "  (?photo, '<http://prpl.stanford.edu/#albumViewHttpURL>', ?photoUrl). \n" +
	  "PHOTO(?photo, ?photoUrl):- " +
	  "  !(?photo, '<http://prpl.stanford.edu/#albumViewHttpURL>', _), " +
	  "  (?photo, '<http://prpl.stanford.edu/#photoHttpURL>', ?photoUrl).";
	  */

	
	public PrPlStickerBook(PRPLAppClient prplConn)
	{
		this.prplService_ = prplConn;
	}
	
	public void setPrPlButlerConnection(PRPLAppClient prplConn)
	{
		this.prplService_ = prplConn;
	}
	
	public String saveStickerResource(String stickerJSON,
			                          PrPlAddressBook addressBook) throws JSONException
	{
		JSONObject result = new JSONObject();
		JSONObject stickerData = null;
		try {
			stickerData = new JSONObject(stickerJSON);
		}
		catch (JSONException jsone)
		{
			jsone.printStackTrace();
			result.put("success", false);
			result.put("message", "A JSON parsing exception has occured with the saved sticker data");
			
			System.out.println("Return result is:\n"+result.toString(1));
			return result.toString(1);
		}


		/*	// Input JSON string format
			{
				"clique": [
  							{"resourceURI": "prpl://resource/8b92c078-75f7-33bd-a172-0b16116cda71#2c04b4a9-2952-11b2-801f-fe4fc1d69398"},
  							{"resourceURI": "prpl://resource/8b92c078-75f7-33bd-a172-0b16116cda71#2c04b4a2-2952-11b2-801f-fe4fc1d69398"},
  							{"resourceURI": "prpl://resource/8b92c078-75f7-33bd-a172-0b16116cda71#6663df6f-2951-11b2-802c-d4fcd2baa454"},
  							{
   								"lastName": "null",
   								"userID": "darlene@csl.stanford.edu",
   								"alias": ["Darlene Hadding"],
   								"resourceURI": "null",
   								"socialWeight": "44",
   								"emails": ["darlene@csl.stanford.edu"],
   								"fullName": "Darlene Hadding",
   								"firstName": "null"
  							}
 						  ],
 				"stickerID": "sticker_1820830591",
 				"name": "InSitu Ninjas",
 				"collection": [{"resourceURI": "prpl://resource/8b92c078-75f7-33bd-a172-0b16116cda71#2c04b4c4-2952-11b2-801f-fe4fc1d69398"}],
 				"collectionModified": true,
 				"stickerIconSrc": "stickers_icons/sticker_ninja.png",
 				"stickerURI": "null"
			}
		*/
		
		Resource sticker = null;
		Group stickerGroup = null; // PrPl access control primitive
		String stickerGroupName = null;
		
		String stickerName = stickerData.getString("name");
		String stickerID   = stickerData.getString("stickerID");
		String stickerURI  = stickerData.getString("stickerURI");
		boolean createSticker = false;
		boolean saveCollection = stickerData.getBoolean("collectionModified");

		String stickerIconURI = null;
		if (stickerData.has("stickerIconSrc"))
			stickerIconURI = stickerData.getString("stickerIconSrc");

		String previousStickerName = null;
		HashMap<String, Resource> previousCliqueMembers   = new HashMap<String, Resource>();
		HashMap<String, Resource> previousCollectionItems = new HashMap<String, Resource>();
		
		if (stickerURI == null || stickerURI.trim().length() <= 0 
		    || stickerURI.trim().equals("null")) {
			createSticker = true;	
		}
		else {
			sticker = prplService_.getResource(stickerURI);
			if (sticker == null) {
				createSticker = true;
			}
			else {
				sticker.beginBatch();
				Object[] attributes = null;
				
				attributes = sticker.getMetadata(Sticker.STICKERNAME_URI);
				if (attributes != null) {
					previousStickerName = (String)attributes[0];
    			}
				
				// Get corresponding PrPl AC Group
				attributes = sticker.getMetadata(Sticker.PRPL_ACGROUP_URI);
				if (attributes != null) {
					stickerGroup = (Group)attributes[0];
    			}

				// Get previous Clique members
				Resource cliqueMember = null;
				attributes = sticker.getMetadata(Sticker.CLIQUEMEMBER_URI);
    			if (attributes != null) {
    				for (int i = 0; i < attributes.length; i++) {
    					cliqueMember = (Resource)attributes[i];
    					previousCliqueMembers.put(cliqueMember.getURI(), cliqueMember);
    				}
    			}
				
    			if (saveCollection) {
        			// Get previous Collection items
        			Resource collectionItem = null;
        			attributes = sticker.getMetadata(Sticker.COLLECTIONITEM_URI);
        			if (attributes != null) {
        				for (int i = 0; i < attributes.length; i++) {
        					collectionItem = (Resource)attributes[i];
        					previousCollectionItems.put(collectionItem.getURI(), collectionItem);
        				}
        			}
    			}

			}
		}

		if (createSticker) {
			sticker = prplService_.createResource();
			// begin batch
			sticker.beginBatch();
			sticker.addType("http://prpl.stanford.edu/#Sticker");
			sticker.setName(stickerName);
			sticker.setMetadata(Sticker.STICKERNAME_URI, stickerName);
			previousStickerName = stickerName;
			
			try {
				stickerGroupName = URLEncoder.encode(stickerName, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			stickerGroup = prplService_.createGroup(stickerGroupName);
			if (stickerGroup != null)
				sticker.setMetadata(Sticker.PRPL_ACGROUP_URI, stickerGroup);
		}
		
		if (stickerIconURI != null)
		    sticker.setMetadata(Sticker.STICKERICON_URI, stickerIconURI);
		if (!previousStickerName.equals(stickerName)) {
			sticker.setName(stickerName);
			sticker.setMetadata(Sticker.STICKERNAME_URI, stickerName);
		}
		
		
		JSONArray newlySavedContacts = new JSONArray();

		// Update the sticker's associated PrPl AC Group primitive
		stickerGroup.beginBatch();
		
		// Update Clique membership
		saveClique(sticker, stickerGroup, stickerData, 
                   previousCliqueMembers,
                   addressBook, newlySavedContacts);

		// Update Collection items
		if (saveCollection) {
			saveCollection(sticker, stickerGroup, stickerData, 
						   previousCollectionItems);
		}

		// Update the sticker's associated PrPl AC Group primitive
		stickerGroup.commitBatch();

		// commit
		sticker.commitBatch();
		
		/*
		// TO-DO: SAVE STICKERS IN CACHE
		if (this.myStickersByURI != null) {
			Sticker stickerEntry = this.myStickersByURI.get(sticker.getURI());
			if (stickerEntry == null) {
				
				
				this.myStickers;
			}
			
		}
		*/

	    /*	// Result JSON string format
	    {
	       "success" : true or false,
	       "message" : "Sticker <b>Ninja Coders</b> successfully saved.",
	       "stickerID" : "...",
	       "stickerURI" : "prpl://...", // URI address
	       "name": "InSitu Ninjas",
	       "newContacts" : [
	                        {  
	                          "userID" : "..." // the original userID of this contact harvested from email
	                          "resourceURI" : "prpl://..." // URI address
	                        },
	                        ...
	                       ]
	    }
	    */

		// JSONArray newlySavedContacts = new JSONArray();
		result.put("success", true);
		result.put("message", "Sticker "+stickerName+" successfully saved.");
		result.put("stickerID", stickerID);
		result.put("stickerURI", sticker.getURI());
		result.put("name", stickerName);
		result.put("newContacts", newlySavedContacts);
		
		System.out.println("Return result is:\n"+result.toString(1));
		return result.toString();	
	}
	
	
	private void saveClique(Resource sticker, Group stickerGroup, JSONObject stickerData, 
                            HashMap<String, Resource> previousCliqueMembers,
                            PrPlAddressBook addressBook, JSONArray newlySavedContacts) throws JSONException
	{
		// Identify Clique members to add & delete
		HashMap<String, Resource> currentCliqueMembers = new HashMap<String, Resource>();
		HashMap<String, Resource> addCliqueMembers     = new HashMap<String, Resource>();
		
		JSONArray cliqueArray = stickerData.getJSONArray("clique");
		for (int i = 0; i < cliqueArray.length(); i++)
		{
			Resource cliqueMember = null;
			JSONObject cliqueMemberData = cliqueArray.getJSONObject(i);
			
			String cliqueMemberURI = cliqueMemberData.getString("resourceURI");
			if (cliqueMemberURI.equals("null")) 
			{
				String fullname  = cliqueMemberData.getString("fullName");
				String firstname = cliqueMemberData.getString("firstName");
				String lastname  = cliqueMemberData.getString("lastName");
				String cliqueMemberUserID = cliqueMemberData.getString("userID");
				int socialWeight = Integer.parseInt(cliqueMemberData.getString("socialWeight"));

				// person doesn't exist, create if needed
				Person p = null;
				cliqueMember = prplService_.createResource();
				
				System.out.println("Saving new social contact/Person resource for '"+fullname+"' ("+cliqueMemberUserID+")");

				//begin batch
				cliqueMember.beginBatch();
				cliqueMember.addType(Person.CLASS_NAME_URI);
				cliqueMember.setName(fullname);
				p = new Person(cliqueMember);
				p.setFullName(fullname);
				if ( !(firstname == null || firstname.trim().length() <= 0
				       || firstname.trim().equals("null")) )
				   p.setFirstName(firstname);
				if ( !(lastname == null || lastname.trim().length() <= 0
					   || lastname.trim().equals("null")) )
				   p.setLastName(lastname);
				
				// Save social weighting, as determined by Dunbar analysis
				cliqueMember.setMetadata( PrPlContactInfo.SOCIALWEIGHT_URI, 
	    				                  new Integer(socialWeight) );
				
	            // Save email addresses
				JSONArray cliqueMemberEmails = null;
				try {
					cliqueMemberEmails = cliqueMemberData.getJSONArray("emails");
					for (int j = 0; j < cliqueMemberEmails.length(); j++) {
	  				  	String emailAddr = cliqueMemberEmails.getString(j);
	  				  	if (emailAddr == null || emailAddr.trim().length() <= 0)
	  				  		continue;
	  				  	cliqueMember.addMetadata(Person.EMAILADDRESS_URI, emailAddr.trim());
	  			  	}
				}
				catch (JSONException jsone)
				{
					System.out.println("Does not have email addresses: "+fullname);
				}
				
				// Identify matching PrPl user by checking with DirectoryService
				// TODO: FIX STUPID PRPL BUG
				/*
				HashMap<String, Identity> prplMatches = addressBook.matchEmailsToPrPlIdentities(emailAddrs);
				if (prplMatches != null) {
					for (Identity matchingIdentity : prplMatches.values()) {
						// Save match to a PrPl Identity
						cliqueMember.setMetadata(InSituContact.PRPL_ID_URI, matchingIdentity);
						
						break;
					}
				}
				*/

				// Save names/aliases
				try {
					JSONArray cliqueMemberAlias = cliqueMemberData.getJSONArray("alias");
					for (int j = 0; j < cliqueMemberAlias.length(); j++) {
	  				  	String name = cliqueMemberAlias.getString(j);
	  				  	if (name == null || name.trim().length() <= 0)
	  				  		continue;
	  				  	cliqueMember.addMetadata(PrPlContactInfo.ALIAS_URI, name.trim());
	  			  	}
				}
				catch (JSONException jsone)
				{
					System.out.println("Does not have aliases: "+fullname);
				}
				
				//commit
				cliqueMember.commitBatch();
				
				// Need to update back the UI
				JSONObject newContact = new JSONObject();
				newContact.put("userID", cliqueMemberUserID);
				newContact.put("resourceURI", cliqueMember.getURI());
				newlySavedContacts.put(newContact);
				
				// Update cache/record of unified contacts list
				if (addressBook != null)
				{
					PrPlContactInfo matchingContact = null;
					if (cliqueMemberEmails != null) {
						for (int j = 0; j < cliqueMemberEmails.length(); j++) {
		  				  	String emailAddr = cliqueMemberEmails.getString(j);
		  				  	if (emailAddr == null || emailAddr.trim().length() <= 0)
		  				  		continue;
		  				  	
		  				  	matchingContact = addressBook.queryFriendByEmail(emailAddr);
		  				  	if (matchingContact != null) {
		  				  		matchingContact.setResourceURI(cliqueMember.getURI());
		  				  		addressBook.getURIToFriendsMap().put(cliqueMember.getURI(), matchingContact);
		  				  		break;
		  				  	}
		  			  	}
					}
				}
				
				currentCliqueMembers.put(cliqueMember.getURI(), cliqueMember);
				addCliqueMembers.put(cliqueMember.getURI(), cliqueMember);
			}
			else
			{
				cliqueMember = previousCliqueMembers.get(cliqueMemberURI);
				if (cliqueMember != null)
					currentCliqueMembers.put(cliqueMemberURI, cliqueMember);
				else
				{
					// Wasn't in previous existing clique
					cliqueMember = prplService_.getResource(cliqueMemberURI);
					if (cliqueMember != null) {
						currentCliqueMembers.put(cliqueMemberURI, cliqueMember);
						addCliqueMembers.put(cliqueMemberURI, cliqueMember);
					}
				}
			}
		}

		
		// Identify old clique members to delete
		Iterator<String> iter = previousCliqueMembers.keySet().iterator();
		while (iter.hasNext()) {
			String cliqueMemberURI = iter.next();
			if (!currentCliqueMembers.containsKey(cliqueMemberURI))
			{
				Resource deleteMember = previousCliqueMembers.get(cliqueMemberURI);
				if (deleteMember != null) {
					sticker.removeMetadata(Sticker.CLIQUEMEMBER_URI, deleteMember);
					deleteMember.removeMetadata(Sticker.STICKER_URI, sticker);
					
					// Update deletion from PrPl AC Group
					Identity deleteMemberId = null;
					Object[] attributes = deleteMember.getMetadata(PrPlContactInfo.PRPL_ID_URI);
					if (attributes != null) {
						deleteMemberId = (Identity)attributes[0];
						if (deleteMemberId != null)
						   stickerGroup.removeMember(deleteMemberId);
	    			}
				}
			}
		}
		
		// Add clique members that were previously not members
		iter = addCliqueMembers.keySet().iterator();
		while (iter.hasNext()) {
			String addMemberURI = iter.next();
			Resource addMember = addCliqueMembers.get(addMemberURI);
			if (addMember != null) {
				sticker.addMetadata(Sticker.CLIQUEMEMBER_URI, addMember);
				addMember.addMetadata(Sticker.STICKER_URI, sticker);
				
				// Update addition to PrPl AC Group
				Identity addMemberId = null;
				Object[] attributes = addMember.getMetadata(PrPlContactInfo.PRPL_ID_URI);
				if (attributes != null) {
					addMemberId = (Identity)attributes[0];
					if (addMemberId != null)
					   stickerGroup.addMember(addMemberId);
    			}
			}
		}
	}
	
	
	
	private void saveCollection(Resource sticker, Group stickerGroup, JSONObject stickerData, 
			                    HashMap<String, Resource> previousCollectionItems) throws JSONException
	{
		// Identify Collection items to add & delete
		HashMap<String, Resource> currentCollectionItems = new HashMap<String, Resource>();
		HashMap<String, Resource> addCollectionItems     = new HashMap<String, Resource>();
		
		JSONArray collectionArray = stickerData.getJSONArray("collection");
		for (int i = 0; i < collectionArray.length(); i++)
		{
			Resource collectionItem = null;
			JSONObject collectionItemData = collectionArray.getJSONObject(i);
			String collectionItemURI = collectionItemData.getString("resourceURI");
			
			collectionItem = previousCollectionItems.get(collectionItemURI);
			if (collectionItem != null)
				currentCollectionItems.put(collectionItemURI, collectionItem);
			else 
			{
				// Wasn't in previous existing collection
				collectionItem = prplService_.getResource(collectionItemURI);
				if (collectionItem != null) {
					currentCollectionItems.put(collectionItemURI, collectionItem);
				    addCollectionItems.put(collectionItemURI, collectionItem);
				}					
			}
		}
		
		// Identify old collection items to delete
		Iterator<String> iter = previousCollectionItems.keySet().iterator();
		while (iter.hasNext()) {
			String collectionItemURI = iter.next();
			if (!currentCollectionItems.containsKey(collectionItemURI))
			{
				Resource deleteItem = previousCollectionItems.get(collectionItemURI);
				if (deleteItem != null) {
					sticker.removeMetadata(Sticker.COLLECTIONITEM_URI, deleteItem);
					
					System.out.println("Unsharing resource <"+deleteItem.getURI()+">");
					stickerGroup.unshare(deleteItem);
					unshareCollectionResource(deleteItem, stickerGroup);
				}
			}
		}
		
		// Add collection items that were previously not in the collection
		iter = addCollectionItems.keySet().iterator();
		while (iter.hasNext()) {
			String addItemURI = iter.next();
			Resource addItem = addCollectionItems.get(addItemURI);
			if (addItem != null) {
				sticker.addMetadata(Sticker.COLLECTIONITEM_URI, addItem);
				
				System.out.println("Sharing resource <"+addItem.getURI()+">");
				stickerGroup.share(addItem);
				shareCollectionResource(addItem, stickerGroup);
			}
		}

	}
	
	private void unshareCollectionResource(Resource collection, Group stickerGroup)
	{
		String[] resourceTypes = collection.getTypes();
		if (resourceTypes == null) {
		   System.out.println("collection.getTypes() is NULL!!!");
		   return;
		}
		
		System.out.println("collection.getTypes() has "+resourceTypes.length+" entries");
		for (int i = 0; i < resourceTypes.length; i++) {
			System.out.println("-> resourceTypes["+i+"] = "+resourceTypes[i]);
			
			if (PrPlAlbumInfo.PHOTOALBUM_URI.equals(resourceTypes[i])) {
				
				System.out.println("UNSHARING A PHOTO ALBUM!");
				
				// Unshare all the photos under this collection object
				Object[] attributes = collection.getMetadata(PrPlAlbumInfo.PHOTO_URI);
				if (attributes != null) {
					for (int j = 0; j < attributes.length; j++) {
						Resource photo = (Resource)attributes[j];
						if (photo != null) {
							stickerGroup.unshare(photo);
							System.out.println("-> Unsharing resource <"+photo.getURI()+">");
						}
					}
					
					System.out.println("Photo Album has "+attributes.length+" attributes for "+PrPlAlbumInfo.PHOTO_URI);
					
				}
				break;
			}
		}
	}
	
	private void shareCollectionResource(Resource collection, Group stickerGroup)
	{
		String[] resourceTypes = collection.getTypes();
		if (resourceTypes == null) {
		   System.out.println("collection.getTypes() is NULL!!!");
		   return;
		}
		
		System.out.println("collection.getTypes() has "+resourceTypes.length+" entries");
		for (int i = 0; i < resourceTypes.length; i++) {
			System.out.println("-> resourceTypes["+i+"] = "+resourceTypes[i]);
			
			if (PrPlAlbumInfo.PHOTOALBUM_URI.equals(resourceTypes[i])) {
				
				System.out.println("SHARING A PHOTO ALBUM!");
				
				// Share all the photos under this collection object
				Object[] attributes = collection.getMetadata(PrPlAlbumInfo.PHOTO_URI);
				if (attributes != null) {
					for (int j = 0; j < attributes.length; j++) {
						Resource photo = (Resource)attributes[j];
						if (photo != null) {
							stickerGroup.share(photo);
							System.out.println("-> Sharing resource <"+photo.getURI()+">");
						}
					}
					
					System.out.println("Photo Album has "+attributes.length+" attributes for "+PrPlAlbumInfo.PHOTO_URI);
					
				}
				break;
			}
		}
	}
	
	
	public boolean buildPrPlStickerBook(PrPlAddressBook addressBook)
	{
		this.myStickers = new Vector<Sticker>();
		this.myStickersByURI = new HashMap<String, Sticker>();
		
		processStickerResources(datalogQuery, addressBook);
		constructSavedHierarchy();
		
		return true;
	}
	
	/* Trying to process a compressed query to quicken results */
	public boolean buildPrPlStickerBookCompressed(PrPlAddressBook addressBook)
	{
		this.myStickers = new Vector<Sticker>();
		this.myStickersByURI = new HashMap<String, Sticker>();
		
		processStickerResourcesCompressed(datalogQueryCompressed, addressBook);
		return true;
	}
	
	private void processStickerResourcesCompressed(String datalogQueryCompressed,
			                                       PrPlAddressBook addressBook)
	{
		QueryResultIterator queryResults;
		for (int i = 0; true; i++)
    	{
    		try {
    			queryResults = this.prplService_.runDatalogQuery(datalogQueryCompressed);
    			break;
    		}
    		catch (Exception e) {
    			if (i == 3) {
    				e.printStackTrace();
    				System.out.println("Problem trying to query for Sticker resources from PCB");
    				return;
    			}
    		}
    	}
		
		int savedStickerNum = 1;
    	while (queryResults.hasNext()) 
        {
        	String key = null;
        	GregorianCalendar modifiedDate = null;
        	Resource sticker = null;
        	Group stickerGroup = null; // PrPl access control primitive
        	String stickerName = null, stickerIconUrl = null;
        	Resource cliqueMember = null;
        	// ?s, ?name, ?stickerIconUrl
        	
        	// STICKERS(?s, ?name, ?stickerIcon, ?acgroup, ?cliqueMember)
        	
            Solution s = queryResults.next();
            List<String> metadataNames = queryResults.getResultVars();
            for (String var : metadataNames) 
            {
                if (var.equals("?t"))
                    continue;
                Object value = s.get(var);
                
                if (var.equals("modifiedDate") && value instanceof GregorianCalendar)
                	modifiedDate = (GregorianCalendar)value;
                else if (var.equals("name"))
                	stickerName = (String)value;
                else if (var.equals("stickerIcon"))
                	stickerIconUrl = (String)value;
                else if (var.equals("s"))
                	sticker = (Resource)value;
                else if (var.equals("acgroup"))
                	stickerGroup = (Group)value;
                else if (var.equals("cliqueMember"))
                	cliqueMember = (Resource)value;
            }

            if (sticker != null)
            {
            	Object[] attributes = null;
            	
            	// Create Sticker object if needed
            	Sticker stickerObj = this.myStickersByURI.get(sticker.getURI());
            	if (stickerObj == null) {
            		stickerObj = new Sticker();
        			stickerObj.setStickerName(stickerName);
                	stickerObj.setStickerURI(sticker.getURI());
                	stickerObj.stickerID = "savedSticker_"+savedStickerNum;
                	savedStickerNum++;
                	
                	// Get Sticker icon (if it exists)
                	if (stickerIconUrl != null && !stickerIconUrl.equals("blank") 
                		&& !stickerIconUrl.equals("null") && stickerIconUrl.trim().length() != 0) {
                		stickerObj.setStickerIconURI(stickerIconUrl);
                	}
                	
                	// Get associated PrPl AC Group (if it exists)
        			if (stickerGroup != null) {
        				stickerObj.setACGroup(stickerGroup);
        			}
                	
                	this.myStickers.add(stickerObj);
        			this.myStickersByURI.put(stickerObj.getStickerURI(), stickerObj);
        			
        			// DEBUG
                	System.err.println("Retrieving Sticker '"+stickerName+"'");
            	}
            	
            	// Set Clique members (friends given/tagged/labeled with this sticker)
            	if (cliqueMember != null) {
    				stickerObj.addCliqueMemberResource(cliqueMember);
    			}
    		}
        }
	
	}
	
	
	
	private void processStickerResources(String datalogQuery,
										 PrPlAddressBook addressBook)
	{
		QueryResultIterator queryResults;
		for (int i = 0; true; i++)
    	{
    		try {
    			queryResults = this.prplService_.runDatalogQuery(datalogQuery);
    			break;
    		}
    		catch (Exception e) {
    			if (i == 3) {
    				e.printStackTrace();
    				System.out.println("Problem trying to query for Sticker resources from PCB");
    				return;
    			}
    		}
    	}
		
		HashSet<String> allCliqueMembersURIs = new HashSet<String>();
		int savedStickerNum = 1;
    	while (queryResults.hasNext()) 
        {	
        	String key = null;
        	GregorianCalendar modifiedDate = null;
        	Resource sticker = null;
        	Group stickerGroup = null; // PrPl access control primitive
        	String stickerName = null, stickerIconUrl = null;
        	// ?s, ?name, ?stickerIconUrl
        	
            Solution s = queryResults.next();
            List<String> metadataNames = queryResults.getResultVars();
            for (String var : metadataNames) 
            {
                if (var.equals("?t"))
                    continue;
                Object value = s.get(var);
                
                if (var.equals("modifiedDate") && value instanceof GregorianCalendar)
                	modifiedDate = (GregorianCalendar)value;
                else if (var.equals("name"))
                	stickerName = (String)value;
                else if (var.equals("stickerIconUrl"))
                	stickerIconUrl = (String)value;
                else if (var.equals("s"))
                	sticker = (Resource)value;
            }

            if (sticker != null)
            {
            	Object[] attributes = null;
            	
            	// Create Sticker object
    			Sticker stickerObj = new Sticker();
    			stickerObj.setStickerName(stickerName);
            	stickerObj.setStickerURI(sticker.getURI());
            	stickerObj.stickerID = "savedSticker_"+savedStickerNum;
            	savedStickerNum++;
            	
            	// DEBUG
            	System.err.println("Retrieving Sticker '"+stickerName+"'");
            	
            	// Get Sticker icon (if it exists)
            	System.out.println("METADATA: Retrieving Sticker metadata");
            	attributes = sticker.getMetadata(Sticker.STICKERICON_URI);
    			if (attributes != null) {
    				stickerIconUrl = (String)attributes[0];	
    				stickerObj.setStickerIconURI(stickerIconUrl);
    			}
    			
    			// Get associated PrPl AC Group (if it exists)
            	System.out.println("METADATA: Retrieving Sticker metadata");
            	attributes = sticker.getMetadata(Sticker.PRPL_ACGROUP_URI);
    			if (attributes != null) {
    				stickerGroup = (Group)attributes[0];
    				stickerObj.setACGroup(stickerGroup);
    			}
            	
            	// Get Clique members (friends given/tagged/labeled with this sticker)
    			Resource cliqueMember = null;
    			System.out.println("METADATA: Retrieving Sticker metadata");
    			attributes = sticker.getMetadata(Sticker.CLIQUEMEMBER_URI);
    			if (attributes != null) {
    				for (int i = 0; i < attributes.length; i++) {
    					cliqueMember = (Resource)attributes[i];
    					if (cliqueMember != null) {
    						stickerObj.addCliqueMemberResource(cliqueMember);
    						allCliqueMembersURIs.add(cliqueMember.getURI());
    					}
    				}
    			}
    			
    			/*** TODO: TOO SLOW!!! ***/
    			/*
    			SocialFlowsGroup clique = SocialFlowsGroup.generateGroup(allCliqueMembersURIs, addressBook);
    			stickerObj.setClique(clique);
    			clique.groupSticker = stickerObj;
    			clique.isSaved = true;
    			*/
    			stickerObj.isSaved = true;
    			
    			// Get Collection items (data objects given/tagged/labeled with this sticker)
    			Resource collectionItem = null;
    			System.out.println("METADATA: Retrieving Sticker metadata");
    			attributes = sticker.getMetadata(Sticker.COLLECTIONITEM_URI);
    			if (attributes != null) {
    				for (int i = 0; i < attributes.length; i++) {
    					collectionItem = (Resource)attributes[i];
    					stickerObj.addCollectionItem(collectionItem.getURI());
    				}
    			}
    			
    			/*
    			// Get FB last modified time if a FB photo album
    			long modifiedTime = 0;
    			attributes = album.getMetadata(InSituAlbum.FBMODIFIED_DATE_URI);
				if (attributes != null) {
					// Should be Long, why is it Integer in PrPl?
					// Long modifiedTime = (Long)attributes[0];					
					if (attributes[0] instanceof Long)
						modifiedTime = ((Long)attributes[0]).longValue();
					else if (attributes[0] instanceof Integer)
						modifiedTime = ((Integer)attributes[0]).longValue();
				}
            	*/

    			this.myStickers.add(stickerObj);
    			this.myStickersByURI.put(stickerObj.getStickerURI(), stickerObj);
    		}
        }

    	// TODO
    	/*
    	System.err.println("BEGIN POPULATING GROUPS/CLIQUES");
    	// Hopefully faster retrieval and processing of friends/social contacts data
    	List<PrPlContactInfo> friendsInGroups = addressBook.getFriendsByURI(new Vector(allCliqueMembersURIs));
    	for (Sticker sticker : this.myStickers) {
    		PrPlGroup clique = PrPlGroup.generateGroup(sticker.getCliqueURIs(), addressBook);
			sticker.setClique(clique);
			clique.groupSticker = sticker;
			clique.isSaved = true;
    	}
    	System.err.println("FINISHING POPULATING GROUPS/CLIQUES");
    	*/
	}
	
	private void constructSavedHierarchy()
	{
		//this.savedHierarchicalGroups = new Vector<PrPlGroup>();
		this.savedHierarchicalStickers = new Vector<Sticker>();
		
		Collections.sort(this.myStickers, sizeOrder);
		for (Sticker sticker : this.myStickers) 
		{
			boolean isTopLevelGroup = true;
			// Find out if this should be a top-level group or be a subset
			for (Sticker superset : this.savedHierarchicalStickers) 
			{
				if (sticker.isSubsetOf(superset)) {
					isTopLevelGroup = false;
					if (sticker.getCliqueSize() == superset.getCliqueSize()) {
						break; // already exists
					}
					superset.addSubset(sticker, false);
					break;
				}
			}
			if (isTopLevelGroup)
				this.savedHierarchicalStickers.add(sticker);
		}
		
		Collections.sort(this.savedHierarchicalStickers, sizeOrder);
		
		//for (Sticker sticker : this.savedHierarchicalStickers) {
		//	this.savedHierarchicalGroups.add(sticker.getClique());
		//}
	}
	

	public List<Sticker> getStickersList(PrPlAddressBook addressBook)
	{
		if (this.myStickers == null)
			buildPrPlStickerBook(addressBook);
		return this.myStickers;
	}
	
	public List<Sticker> getSavedHierarchicalStickers(PrPlAddressBook addressBook)
	{
		if (this.myStickers == null)
			buildPrPlStickerBook(addressBook);
		return this.savedHierarchicalStickers;
	}
	
	public HashMap<String, Sticker> getURIToStickersMap(PrPlAddressBook addressBook)
	{
		if (this.myStickersByURI == null)
			buildPrPlStickerBook(addressBook);
		return this.myStickersByURI;
	}
	
	/*
	public void constructSuggStickers(JSONArray suggested_groups,
            					      PrPlAddressBook addressBook) throws JSONException
    {
        if (suggested_groups == null || suggested_groups.length() <= 0)
        	return;
		
		// Process the suggested groups
		this.setStatusMessage("Constructing your Suggested Social Cliques...");
		
        // Build InSitu suggested groups and their subsets
        suggestedHierarchicalGroups = new Vector<SocialFlowsGroup>();
        HashSet<PrPlContactInfo> updatedContacts = new HashSet<PrPlContactInfo>();
        for (int i = 0; i < suggested_groups.length(); i++)
        {
            // Create a Group object
            JSONObject group = suggested_groups.getJSONObject(i);
            if (group == null)
                continue;
            
            this.setStatusMessage("Constructed "+(i+1)+" of "+suggested_groups.length()+" Suggested Social Cliques sets...");
            SocialFlowsGroup proposedGroup = SocialFlowsGroup.generateGroupAndSubsets(group, addressBook);
            if (proposedGroup == null)
                continue;
            suggestedHierarchicalGroups.add(proposedGroup);
            
            // Updating JSON data with more info on this particular group
            suggested_groups.put(i, group);
            
            // Update existing contacts in PCB with updated social weighting, additional emails, aliases/names
            // as deduced by InSitu and Dunbar social grouping algos/analysis
            Iterator<PrPlContactInfo> membersIter = proposedGroup.getGroupMembers().iterator();
            while (membersIter.hasNext())
            {
            	PrPlContactInfo member = membersIter.next();
                Resource personResource = null;
                String personURI = member.getResourceURI();
                
                // Incremental Diff update on additional emails, aliases/names
                JSONObject emailData = member.getEmailData();
                JSONArray memberEmails = emailData.getJSONArray("emailAddrs");
                JSONArray memberNames  = emailData.getJSONArray("names");
                
                // This is a new InSitu contact. Will only be saved to PrPl if it is a 
                // member of one of the few social groups configured and saved by the user
                if (personURI == null)
                {
                    // Nevertheless, need to record list of emails & aliases/names 
                    // associated with this new contact that might be potentially-saved
                    if (memberEmails != null) {
                        for (int j = 0; j < memberEmails.length(); j++) {
                            String emailAddr = memberEmails.getString(j);
                            if (emailAddr == null || emailAddr.trim().length() <= 0)
                                continue;
                            emailAddr = emailAddr.trim();
                            member.addEmailAddress(emailAddr);
                        }
                    }
                    
                    if (memberNames != null) {
                        for (int j = 0; j < memberNames.length(); j++) {
                            String name = memberNames.getString(j);
                            if (name == null || name.trim().length() <= 0)
                                continue;
                            name = name.trim();
                            member.addName(name);
                        }
                    }
                    
                    continue;
                }
                
                // Do not repeat updates
                if ( updatedContacts.contains(member) )
                   continue;
                personResource = prplService_.getResource(personURI);
                if (personResource == null) // Maybe resource no longer exists
                    continue;
                
                // Update with new social weighting, as determined by Dunbar analysis
                personResource.setMetadata( PrPlContactInfo.SOCIALWEIGHT_URI, 
                                            new Integer(member.getSocialWeight()) );
                
                // Update email addresses
                Set<String> emails = member.getEmailAddresses();
                Set<String> emailsDiff = new HashSet<String>();
                if (memberEmails != null) {
                    for (int j = 0; j < memberEmails.length(); j++) {
                        String emailAddr = memberEmails.getString(j);
                        if (emailAddr == null || emailAddr.trim().length() <= 0)
                            continue;

                        emailAddr = emailAddr.trim();
                        if (emails != null && emails.contains(emailAddr))
                            continue;
                        else {
                            member.addEmailAddress(emailAddr);
                            emailsDiff.add(emailAddr);
                        }
                    }
                }
                
                Iterator<String> emailsDiffIter = emailsDiff.iterator();
                while (emailsDiffIter.hasNext()) {
                    personResource.addMetadata(Person.EMAILADDRESS_URI, emailsDiffIter.next());
                }

                // Update aliases/alternative names
                Set<String> names = member.getNames();
                Set<String> namesDiff = new HashSet<String>();
                if (memberNames != null) {
                    for (int j = 0; j < memberNames.length(); j++) {
                        String name = memberNames.getString(j);
                        if (name == null || name.trim().length() <= 0)
                            continue;

                        name = name.trim();
                        if (names != null && names.contains(name))
                            continue;
                        else {
                            member.addName(name);
                            namesDiff.add(name);
                        }
                    }
                }
                
                Iterator<String> namesDiffIter = namesDiff.iterator();
                while (namesDiffIter.hasNext()) {
                    personResource.addMetadata(PrPlContactInfo.ALIAS_URI, namesDiffIter.next());
                }
                
                // Mark as Already updated (in PCB)
                updatedContacts.add(member);
            } 
        }
        // Collections.sort(suggestedHierarchicalGroups);
        
        
        // Flatten the stickers list
        suggestedStickersLookup = new HashMap<String, Sticker>();
        suggestedStickers = new Vector<Sticker>();

        // Should be a list where no two groups are exactly identical
        List<SocialFlowsGroup> groupsList = new Vector<SocialFlowsGroup>();
        Iterator<SocialFlowsGroup> groupIter = suggestedHierarchicalGroups.iterator();
        while (groupIter.hasNext()) {
        	SocialFlowsGroup.flattenGroupAndSubsets(groupIter.next(), groupsList);         
        }
        
        // Create stickers for Suggested groups
        // - InSitu suggested groups if available
        int stickerNum = (new Random()).nextInt();
        if (stickerNum < 0) 
            stickerNum = -stickerNum;
        
        groupIter = groupsList.iterator();
        while (groupIter.hasNext()) {
        	SocialFlowsGroup proposedGroup = groupIter.next();
            Sticker proposedSticker = new Sticker(proposedGroup);
            proposedGroup.groupSticker = proposedSticker;
            
            String stickerID = "sticker_"+stickerNum;
            proposedSticker.stickerID = stickerID;
            suggestedStickers.add(proposedSticker);
            suggestedStickersLookup.put(stickerID, proposedSticker);
            stickerNum++;
        }
        
    }
    */
	
	public List<Sticker> getSuggestedStickers()
	{
		return this.suggestedStickers;
	}
	
	public HashMap<String, Sticker> getSuggestedStickersLookup()
	{
		return this.suggestedStickersLookup;
	}
	
	public List<SocialFlowsGroup> getSuggestedHierarchicalGroups()
	{
		return this.suggestedHierarchicalGroups;
	}


	/* Access control using Stickers */
	/*
	private void saveCollection(Resource sticker, Group stickerGroup, JSONObject stickerData, 
			                    HashMap<String, Resource> previousCollectionItems) throws JSONException
	{
		// Identify Collection items to add & delete
		HashMap<String, Resource> currentCollectionItems = new HashMap<String, Resource>();
		HashMap<String, Resource> addCollectionItems     = new HashMap<String, Resource>();
		
		JSONArray collectionArray = stickerData.getJSONArray("collection");
		for (int i = 0; i < collectionArray.length(); i++)
		{
			Resource collectionItem = null;
			JSONObject collectionItemData = collectionArray.getJSONObject(i);
			String collectionItemURI = collectionItemData.getString("resourceURI");
			
			collectionItem = previousCollectionItems.get(collectionItemURI);
			if (collectionItem != null)
				currentCollectionItems.put(collectionItemURI, collectionItem);
			else 
			{
				// Wasn't in previous existing collection
				collectionItem = prplService_.getResource(collectionItemURI);
				if (collectionItem != null) {
					currentCollectionItems.put(collectionItemURI, collectionItem);
				    addCollectionItems.put(collectionItemURI, collectionItem);
				}					
			}
		}
		
		// Identify old collection items to delete
		Iterator<String> iter = previousCollectionItems.keySet().iterator();
		while (iter.hasNext()) {
			String collectionItemURI = iter.next();
			if (!currentCollectionItems.containsKey(collectionItemURI))
			{
				Resource deleteItem = previousCollectionItems.get(collectionItemURI);
				if (deleteItem != null) {
					sticker.removeMetadata(Sticker.COLLECTIONITEM_URI, deleteItem);
					
					System.out.println("Unsharing resource <"+deleteItem.getURI()+">");
					stickerGroup.unshare(deleteItem);
					unshareCollectionResource(deleteItem, stickerGroup);
				}
			}
		}
		
		// Add collection items that were previously not in the collection
		iter = addCollectionItems.keySet().iterator();
		while (iter.hasNext()) {
			String addItemURI = iter.next();
			Resource addItem = addCollectionItems.get(addItemURI);
			if (addItem != null) {
				sticker.addMetadata(Sticker.COLLECTIONITEM_URI, addItem);
				
				System.out.println("Sharing resource <"+addItem.getURI()+">");
				stickerGroup.share(addItem);
				shareCollectionResource(addItem, stickerGroup);
			}
		}

	}
	
	private void unshareCollectionResource(Resource collection, Group stickerGroup)
	{
		String[] resourceTypes = collection.getTypes();
		if (resourceTypes == null) {
		   System.out.println("collection.getTypes() is NULL!!!");
		   return;
		}
		
		System.out.println("collection.getTypes() has "+resourceTypes.length+" entries");
		for (int i = 0; i < resourceTypes.length; i++) {
			System.out.println("-> resourceTypes["+i+"] = "+resourceTypes[i]);
			
			if (PrPlAlbumInfo.PHOTOALBUM_URI.equals(resourceTypes[i])) {
				
				System.out.println("UNSHARING A PHOTO ALBUM!");
				
				// Unshare all the photos under this collection object
				Object[] attributes = collection.getMetadata(PrPlAlbumInfo.PHOTO_URI);
				if (attributes != null) {
					for (int j = 0; j < attributes.length; j++) {
						Resource photo = (Resource)attributes[j];
						if (photo != null) {
							stickerGroup.unshare(photo);
							System.out.println("-> Unsharing resource <"+photo.getURI()+">");
						}
					}
					
					System.out.println("Photo Album has "+attributes.length+" attributes for "+PrPlAlbumInfo.PHOTO_URI);
					
				}
				break;
			}
		}
	}
	
	private void shareCollectionResource(Resource collection, Group stickerGroup)
	{
		String[] resourceTypes = collection.getTypes();
		if (resourceTypes == null) {
		   System.out.println("collection.getTypes() is NULL!!!");
		   return;
		}
		
		System.out.println("collection.getTypes() has "+resourceTypes.length+" entries");
		for (int i = 0; i < resourceTypes.length; i++) {
			System.out.println("-> resourceTypes["+i+"] = "+resourceTypes[i]);
			
			if (PrPlAlbumInfo.PHOTOALBUM_URI.equals(resourceTypes[i])) {
				
				System.out.println("SHARING A PHOTO ALBUM!");
				
				// Share all the photos under this collection object
				Object[] attributes = collection.getMetadata(PrPlAlbumInfo.PHOTO_URI);
				if (attributes != null) {
					for (int j = 0; j < attributes.length; j++) {
						Resource photo = (Resource)attributes[j];
						if (photo != null) {
							stickerGroup.share(photo);
							System.out.println("-> Sharing resource <"+photo.getURI()+">");
						}
					}
					
					System.out.println("Photo Album has "+attributes.length+" attributes for "+PrPlAlbumInfo.PHOTO_URI);
					
				}
				break;
			}
		}
	}
	*/



	/* Sort stickers by size, from largest to smallest */
	public static Comparator<Sticker> sizeOrder
	= new Comparator<Sticker>() {
		public int compare(Sticker o1, Sticker o2)
		{
			if (o1.getCliqueSize() > o2.getCliqueSize())
				return -1;
			else if (o1.getCliqueSize() < o2.getCliqueSize())
				return 1;
			return o1.getStickerName().compareToIgnoreCase(o2.getStickerName());
		}
      };


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