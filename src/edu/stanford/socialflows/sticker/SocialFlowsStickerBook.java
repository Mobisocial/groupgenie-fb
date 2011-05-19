package edu.stanford.socialflows.sticker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.*;
import java.util.*;

import org.json.*;

import edu.stanford.socialflows.contacts.SocialFlowsAddressBook;
import edu.stanford.socialflows.contacts.SocialFlowsContactInfo;
import edu.stanford.socialflows.db.DBConnectionManager;
import edu.stanford.socialflows.util.StatusProvider;


public class SocialFlowsStickerBook implements StatusProvider
{
	private DBConnectionManager sfDBConnManager = null;
	private List<Sticker> savedStickers = null;
	private HashMap<String, Sticker> savedStickersBySfGroupId = null;
	//private boolean refreshMyStickers = false;
	
	// HIERARCHY covers both saved and non-saved groups
	private List<Sticker> hierarchicalStickers = new Vector<Sticker>();
	
	/* Deal with the suggested hierarchical groups, suggested stickers */
	private int suggStickerCounter = 0;
	private List<Sticker> suggestedStickers = null;
	private HashMap<String, Sticker> suggestedStickersLookup = null;
	
	
    //private List<Sticker> suggestedHierarchicalStickers = null;


	public SocialFlowsStickerBook(DBConnectionManager dbconnManager)
	{
		this.sfDBConnManager = dbconnManager;
		this.suggStickerCounter = (new Random()).nextInt();
        if (this.suggStickerCounter < 0) 
        	this.suggStickerCounter = -this.suggStickerCounter;
	}
	
	
	/* REMOVING STICKERS */
	private static final String queryRemoveSticker
	= "DELETE FROM socialflows_stickerbooks "+
	      "WHERE ownerSfId=? AND sfgroupId=?";
	
	private static final String queryRemoveStickerTopology
	= "DELETE FROM socialflows_topology "+
	      "WHERE ownerSfId=? AND sfgroupId=?";
	
	// Remove saved sticker
	public void removeSticker(long sfGroupID, boolean removeUnsavedSubsets)
	{
		Connection dbconn = null;
		long ownerSfId = this.sfDBConnManager.getCurrentUser().getSfUserId();
		PreparedStatement pstmt = null;
		int numRowsDeleted = 0;
		
		try {
			dbconn = this.sfDBConnManager.getConnection();
			pstmt = dbconn.prepareStatement(queryRemoveStickerTopology);
			pstmt.setLong(1, ownerSfId);
			pstmt.setLong(2, sfGroupID);
			numRowsDeleted = pstmt.executeUpdate();
			pstmt.close();
			
			pstmt = dbconn.prepareStatement(queryRemoveSticker);
			pstmt.setLong(1, ownerSfId);
			pstmt.setLong(2, sfGroupID);
			pstmt.executeUpdate();
		}
		catch (SQLException sqle) {
			System.out.println("SQLException occured when deleting existing sticker (with sfGroupID "+sfGroupID+"): "+sqle.getMessage());
			sqle.printStackTrace();
		}
		finally {
			try {
				if (dbconn != null) dbconn.close();
				if (pstmt != null) pstmt.close();
			} catch (SQLException sqle) {}
		}
		
		// Remove sticker from memory cache if it exists
		Sticker removedSticker = null;
		if (this.savedStickersBySfGroupId != null)
			removedSticker = this.savedStickersBySfGroupId.remove(String.valueOf(sfGroupID));
		if (savedStickers != null && removedSticker != null)
			savedStickers.remove(removedSticker);
		if (removedSticker == null)
			return;

		Sticker newParent = removedSticker.parentSticker;
        // Has no parent, so subset groups now promoted to top-level groups
        if (newParent == null) {
        	newParent = new Sticker();
        	newParent.setSubsets(this.hierarchicalStickers);
        }
		Sticker.deleteSticker(removedSticker, newParent,
							  savedStickersBySfGroupId,
							  savedStickers, 
							  removeUnsavedSubsets);
		
		System.out.println("Removed existing sticker '"+removedSticker.getStickerName()+
				           "' (with sfGroupID "+sfGroupID+") with "+numRowsDeleted+" group members");
	}	

	public void removeSuggSticker(String suggStickerID, boolean removeUnsavedSubsets)
	{
		if (suggStickerID == null)
			return;
		
    	Sticker removedSticker = null;
        if (suggestedStickersLookup != null)
           removedSticker = suggestedStickersLookup.remove(suggStickerID);
        if (suggestedStickers != null && removedSticker != null)
           suggestedStickers.remove(removedSticker);
        if (removedSticker == null)
        	return;
        
        // TODO: Remove suggested subsets of deleted suggested group
        Sticker newParent = removedSticker.parentSticker;
        // Has no parent, so subset groups now promoted to top-level groups
        if (newParent == null) {
        	newParent = new Sticker();
        	newParent.setSubsets(this.hierarchicalStickers);
        }
        Sticker.deleteSticker(removedSticker, newParent,
                              suggestedStickersLookup,
                              suggestedStickers,
                              removeUnsavedSubsets);
        
        //System.out.println("Successfully deleted suggested sticker "+removedSticker.getCliqueMemberIDs().toString());
	}


	/* SAVING/UPDATING STICKERS */
	public static final String queryStickerInfo
	= "SELECT * FROM socialflows_stickerbooks "+
	      "WHERE ownerSfId=? AND sfgroupId=?";
	
	public static final String queryStickerTopologyInfo
	= "SELECT * FROM socialflows_topology "+
	      "WHERE ownerSfId=? AND sfgroupId=?";
	
	public static final String createNewSticker
	= "INSERT INTO socialflows_stickerbooks(ownerSfId, groupName, stickerIconUrl) "+
	      "VALUES(?, ?, ?)";
	
	public static final String updateStickerInfo
	= "UPDATE socialflows_stickerbooks "+
	      "SET groupName=?, stickerIconUrl=? "+
	      "WHERE ownerSfId=? AND sfgroupId=?";
	
	public static final String deleteGroupMember
	= "DELETE FROM socialflows_topology "+
	     "WHERE ownerSfId=? AND sfgroupId=? AND contactId=?";
	
	public static final String addGroupMember
	= "INSERT INTO socialflows_topology(ownerSfId, sfgroupId, contactId) "+
	     "VALUES(?, ?, ?)";
	
	public String saveSticker(String stickerJSON,
			                  SocialFlowsAddressBook addressBook) throws JSONException
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
  							{"sfContactID": "32009"},
  							{"sfContactID": "28"},
  							{"sfContactID": "prpl://resource/8b92c078-75f7-33bd-a172-0b16116cda71#6663df6f-2951-11b2-802c-d4fcd2baa454"},
  							{
   								"lastName": "null",
   								"userID": "darlene@csl.stanford.edu",
   								"alias": ["Darlene Hadding"],
   								"sfContactID": "null",
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
 				"stickerSfID": "null"
			}
		*/
				
		String stickerName    = stickerData.getString("name");
		String stickerID      = stickerData.getString("stickerID");
		String stickerSfID    = stickerData.optString("stickerSfID", null);
		String stickerIconURI = stickerData.optString("stickerIconSrc", null);
		
		//boolean saveCollection = stickerData.getBoolean("collectionModified");
		boolean createSticker = false;


		Connection dbconn = null;
		long ownerSfId = this.sfDBConnManager.getCurrentUser().getSfUserId();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		HashSet<String> previousCliqueMembers = new HashSet<String>();
		//HashMap<String, Resource> previousCollectionItems = new HashMap<String, Resource>();
		
		Sticker sticker = new Sticker();
		sticker.setStickerName(stickerName);
		sticker.setStickerIconURI(stickerIconURI);
		sticker.setStickerSfID(stickerSfID);
		sticker.stickerID = stickerID;
		
		try {
			dbconn = this.sfDBConnManager.getConnection();
		}
		catch (SQLException sqle) {
			System.out.println("Problem trying to get database connection when "+
					           "saving Sticker due to SQLException: "+sqle.getMessage());
			sqle.printStackTrace();
			
			result.put("success", false);
			result.put("message", "Problem trying to get connection to SocialFlows DB "+
					              "due to the following error: "+sqle.getMessage());

			System.out.println("Return result is:\n"+result.toString(1));
			return result.toString();
		}
		
		if (stickerSfID == null || stickerSfID.trim().length() <= 0 
		    || stickerSfID.trim().equals("null")) {
			createSticker = true;	
		}
		else
		{
			createSticker = true;
			try {
				pstmt = dbconn.prepareStatement(queryStickerInfo);
				pstmt.setLong(1, ownerSfId);
				pstmt.setLong(2, Long.parseLong(stickerSfID));
				rs = pstmt.executeQuery();
				while (rs.next()) {
					createSticker = false;
					String previousStickerName = rs.getString("groupName");
					String previousStickerIcon = rs.getString("stickerIconUrl");
					System.out.println("RETRIEVING EXISTING STICKER (sfGroupId: "+stickerSfID+
							           ") with previous sticker name '"+previousStickerName+"'");
				}
			}
			catch (SQLException sqle) {
				createSticker = true;
				System.out.println("SQLException occured when retrieving existing sticker: "+sqle.getMessage());
				sqle.printStackTrace();
			}
			finally {
				try {
					pstmt.close();
					rs.close();
				} catch (SQLException sqle) {}
			}
			
			if (!createSticker) {
				// Get previous Clique members
				try {
					pstmt = dbconn.prepareStatement(queryStickerTopologyInfo);
					pstmt.setLong(1, ownerSfId);
					pstmt.setLong(2, Long.parseLong(stickerSfID));
					rs = pstmt.executeQuery();
					while (rs.next()) {
						long groupMemberContactId = rs.getLong("contactId");
						previousCliqueMembers.add(String.valueOf(groupMemberContactId));
					}
				}
				catch (SQLException sqle) {
					System.out.println("GET PREVIOUS CLIQUE MEMBERS ERROR: "+sqle.getMessage());
					sqle.printStackTrace();
				}
				finally {
					try {
					  pstmt.close();
					  rs.close();
					} catch (SQLException sqle) {}
				}
				
				/*
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
				*/
			}
		}


		if (createSticker)
		{
			try {
				pstmt = dbconn.prepareStatement(createNewSticker, 
						                        Statement.RETURN_GENERATED_KEYS);
				pstmt.setLong(1, ownerSfId);
				pstmt.setString(2, stickerName);
				if (stickerIconURI != null && stickerIconURI.trim().length() > 0)
					pstmt.setString(3, stickerIconURI.trim());
				else
					pstmt.setNull(3, java.sql.Types.VARCHAR);
				pstmt.executeUpdate();
				
				long stickerSfIdLong = -1;
				rs = pstmt.getGeneratedKeys();
				while (rs.next()) {
					stickerSfIdLong = rs.getLong(1);
				}
				stickerSfID = String.valueOf(stickerSfIdLong);
				sticker.setStickerSfID(stickerSfID);
				
				System.out.println("CREATED STICKER "+stickerName+" with SfID="+stickerSfID);
			}
			catch (SQLException sqle) {
				System.out.println("CREATE NEW STICKER/GROUP INFO ERROR: "+sqle.getMessage());
				sqle.printStackTrace();
			}
			finally {
				try {
				  pstmt.close();
				  rs.close();
				} catch (SQLException sqle) {}
			}

			/*
			try {
				stickerGroupName = URLEncoder.encode(stickerName, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			stickerGroup = prplService_.createGroup(stickerGroupName);
			if (stickerGroup != null)
				sticker.setMetadata(Sticker.PRPL_ACGROUP_URI, stickerGroup);
			*/		
		}
		else
		{
			try {
				pstmt = dbconn.prepareStatement(updateStickerInfo);
				pstmt.setString(1, stickerName);
				if (stickerIconURI != null && stickerIconURI.trim().length() > 0)
					pstmt.setString(2, stickerIconURI.trim());
				else
					pstmt.setNull(2, java.sql.Types.VARCHAR);
				pstmt.setLong(3, ownerSfId);
				pstmt.setLong(4, Long.parseLong(stickerSfID));
				pstmt.executeUpdate();
				
				System.out.println("UPDATED STICKER "+stickerName+" with SfID="+stickerSfID);
			}
			catch (SQLException sqle) {
				System.out.println("UPDATE STICKER/GROUP INFO ERROR: "+sqle.getMessage());
				sqle.printStackTrace();
			}
			finally {
				try {
				  pstmt.close();
				} catch (SQLException sqle) {}
			}
		}
		

		// Update Clique membership
		JSONArray newlySavedContacts = new JSONArray();
		try {
			//System.out.println("NOW TRYING TO SAVE THE FOLLOWING CLIQUE: "+stickerData);
			
			saveClique(sticker, stickerData, 
	                   previousCliqueMembers,
	                   addressBook, newlySavedContacts);
		}
		catch (SQLException sqle) {
			System.out.println("UPDATE CLIQUE MEMBERSHIP ERROR: "+sqle.getMessage());
			sqle.printStackTrace();
		}

		/*
		// Update Collection items
		if (saveCollection) {
			saveCollection(sticker, stickerData, 
						   previousCollectionItems);
		}
		*/

		// TODO: UPDATE/SAVE STICKERS IN CACHE IF POSSIBLE
		this.updateStickerInCache(sticker, addressBook);
		
		
	    /* // Result JSON string format
	    {
	       "success" : true or false,
	       "message" : "Sticker <b>Ninja Coders</b> successfully saved.",
	       "stickerID" : "...",
	       "stickerSfID" : "38",
	       "stickerURI" : "prpl://...", // URI address (optional)
	       "name": "InSitu Ninjas",
	       "newContacts" : [
	                        {  
	                          "userID" : "...", // the original pre-saved userID of this contact within UI
	                          "sfContactID" : "11",
	                          "resourceURI" : "prpl://..." // URI address (optional)
	                        },
	                        ...
	                       ]
	    }
	    */
		
		try {
			if (dbconn != null) dbconn.close();
		} catch (SQLException sqle) {}

		// Output JSON of results
		result.put("success", true);
		result.put("message", "Sticker "+stickerName+" successfully saved.");
		result.put("stickerID", stickerID);
		result.put("stickerSfID", sticker.getStickerSfID());
		result.put("name", stickerName);
		result.put("newContacts", newlySavedContacts);
		
		//System.out.println("Return result is:\n"+result.toString(1));
		return result.toString();
	}

	private void saveClique(Sticker sticker, JSONObject stickerData,
                            HashSet<String> previousCliqueMembers,
                            SocialFlowsAddressBook addressBook, 
                            JSONArray newlySavedContacts) throws JSONException, SQLException
	{
		// Identify Clique members to add & delete
		HashSet<String> currentCliqueMembers = new HashSet<String>();
		HashSet<String> addCliqueMembers     = new HashSet<String>();
		
		JSONArray cliqueArray = stickerData.getJSONArray("clique");
		System.out.println("TRYING TO SAVE THE FOLLOWING CLIQUE: "+cliqueArray);
		
		for (int i = 0; i < cliqueArray.length(); i++)
		{
			SocialFlowsContactInfo cliqueMember = null;
			JSONObject cliqueMemberData = cliqueArray.getJSONObject(i);
			
			String cliqueMemberUserID    = cliqueMemberData.optString("userID", null);
			String cliqueMemberContactID = cliqueMemberData.optString("sfContactID", null);
			if (cliqueMemberContactID == null || cliqueMemberContactID.equals("null")) 
			{
				cliqueMember = addressBook.saveContact(cliqueMemberData);

				// Need to update back the UI
				JSONObject newContact = new JSONObject();
				newContact.put("userID", cliqueMemberUserID);
				newContact.put("sfContactID", cliqueMember.getContactId());
				newlySavedContacts.put(newContact);

				currentCliqueMembers.add(String.valueOf(cliqueMember.getContactId()));
				addCliqueMembers.add(String.valueOf(cliqueMember.getContactId()));
			}
			else
			{
				currentCliqueMembers.add(cliqueMemberContactID);
				// Wasn't in previous existing clique
				if (!previousCliqueMembers.contains(cliqueMemberContactID))
					addCliqueMembers.add(cliqueMemberContactID);
			}
		}


		System.out.println("PREVIOUS CLIQUE MEMBERS: ["+previousCliqueMembers.toString()+"]");
		System.out.println("CURRENT CLIQUE MEMBERS: ["+currentCliqueMembers.toString()+"]");
		System.out.println("ADDING CLIQUE MEMBERS: ["+addCliqueMembers.toString()+"]");

		
		Connection dbconn = this.sfDBConnManager.getConnection();
		long ownerSfId = this.sfDBConnManager.getCurrentUser().getSfUserId();
		long groupSfId = Long.parseLong(sticker.getStickerSfID());
		PreparedStatement pstmt = null;

		// Identify old clique members to delete
		pstmt = dbconn.prepareStatement(deleteGroupMember);
		for (String memberContactID : previousCliqueMembers)
		{
			if (!currentCliqueMembers.contains(memberContactID)) {
				pstmt.setLong(1, ownerSfId);
				pstmt.setLong(2, groupSfId);
				pstmt.setLong(3, Long.parseLong(memberContactID));
				pstmt.executeUpdate();
				
				System.out.println("DELETED CLIQUE MEMBER with sfContactID "+memberContactID);
			}
		}
		pstmt.close();
		
		// Add clique members that were previously not members
		pstmt = dbconn.prepareStatement(addGroupMember);
		for (String memberContactID : addCliqueMembers)
		{
			pstmt.setLong(1, ownerSfId);
			pstmt.setLong(2, groupSfId);
			pstmt.setLong(3, Long.parseLong(memberContactID));
			pstmt.executeUpdate();
			
			System.out.println("ADDED CLIQUE MEMBER with sfContactID "+memberContactID);
		}
		pstmt.close();
		
		if (addressBook != null) {
			SocialFlowsGroup currentClique 
			= SocialFlowsGroup.generateSocialFlowsGroup(currentCliqueMembers, addressBook);
			sticker.setClique(currentClique);
		}
		dbconn.close();
	}

	private void updateStickerInCache(Sticker sticker,
    		  						  SocialFlowsAddressBook addressBook)
	{
        // Update Sticker object in cache
		Sticker cached = null;
		
		if (this.savedStickers == null)
		   this.savedStickers = new Vector<Sticker>();
		if (this.savedStickersBySfGroupId == null)
		   this.savedStickersBySfGroupId = new HashMap<String, Sticker>();

		// Check if previously a suggested sticker/group
		if (this.suggestedStickersLookup != null) {
			cached = this.suggestedStickersLookup.remove(sticker.stickerID);
			if (cached != null) {
				if (this.suggestedStickers != null)
				    this.suggestedStickers.remove(cached);
				if (this.savedStickers != null)
					this.savedStickers.add(cached);
				if (this.savedStickersBySfGroupId != null)
					this.savedStickersBySfGroupId.put(sticker.getStickerSfID(), cached);
				
				System.out.println("NOW UPDATING IN CACHE, SAVED SUGGESTED STICKER "+sticker.stickerID);
			}
			else {
				// Do something???
			}
		}
		// Check if it is currently a saved group
		if (cached == null && this.savedStickersBySfGroupId != null
			&& sticker.getStickerSfID() != null) {
			cached = this.savedStickersBySfGroupId.get(sticker.getStickerSfID());
		}
		if (cached != null) {
			cached.setStickerName(sticker.getStickerName());
			cached.setStickerIconURI(sticker.getStickerIconURI());
			cached.setStickerSfID(sticker.getStickerSfID());
			cached.stickerID = sticker.stickerID;
			cached.setClique(sticker.getClique());
			cached.isSaved = true;
			
			System.out.println("UPDATING STICKER IN CACHE TO "+cached.getStickerName()+
					           " (GroupSfID: "+cached.getStickerSfID()+", UI ID: "+cached.stickerID+")");
		}
		else { 
			// Is a newly created group or subset made by user through the UI
			if (this.savedStickers != null)
				this.savedStickers.add(sticker);
			if (this.savedStickersBySfGroupId != null)
				this.savedStickersBySfGroupId.put(sticker.getStickerSfID(), sticker);
			
			// TODO: How to maintain new stickers place in the hierarchy!!! (need to track parents)
			
		}
	}
	

	
	public boolean buildStickerBook(SocialFlowsAddressBook addressBook)
	{
		this.savedStickers = new Vector<Sticker>();
		this.savedStickersBySfGroupId = new HashMap<String, Sticker>();
		
		try {
			processStickerResourcesCompressed(addressBook);
			return true;
		}
		catch (SQLException sqle) {
			System.out.println("ERROR RETRIEVING SAVED STICKERS: "+sqle.getMessage());
			sqle.printStackTrace();
			return false;
		}
	}


	private static final String queryAllStickers
	= "SELECT * FROM socialflows_stickerbooks AS stickers, socialflows_topology AS topology "+
	  "   WHERE stickers.ownerSfId=? "+
	  "         AND topology.ownerSfId=stickers.ownerSfId "+
	  "         AND topology.sfgroupId=stickers.sfgroupId";
	
	private void processStickerResourcesCompressed(SocialFlowsAddressBook addressBook) throws SQLException
	{
		Connection dbconn = this.sfDBConnManager.getConnection();
		long ownerSfId = this.sfDBConnManager.getCurrentUser().getSfUserId();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		pstmt = dbconn.prepareStatement(queryAllStickers);
		pstmt.setLong(1, ownerSfId);
		
		rs = pstmt.executeQuery();
		while (rs.next()) {
			String stickerName    = rs.getString("groupName");
			String stickerIconUrl = rs.getString("stickerIconUrl");
			long sfgroupId        = rs.getLong("sfgroupId");
			String sfgroupIdStr   = String.valueOf(sfgroupId);
			long cliqueMemberId   = rs.getLong("contactId");

			// Create Sticker object if needed
        	Sticker stickerObj = this.savedStickersBySfGroupId.get(sfgroupIdStr);
        	if (stickerObj == null) {
        		stickerObj = new Sticker();
        		stickerObj.isSaved = true;
    			stickerObj.setStickerName(stickerName);
            	stickerObj.setStickerSfID(sfgroupIdStr);
            	
            	// Identifying UI element representing this sticker
            	stickerObj.stickerID = "savedSticker_"+sfgroupIdStr;
            	
            	// Get Sticker icon (if it exists)
            	if (stickerIconUrl != null && !stickerIconUrl.equals("blank") 
            		&& !stickerIconUrl.equals("null") && stickerIconUrl.trim().length() != 0) {
            		stickerObj.setStickerIconURI(stickerIconUrl);
            	}
            	this.savedStickers.add(stickerObj);
    			this.savedStickersBySfGroupId.put(sfgroupIdStr, stickerObj);
    			
    			// DEBUG
            	System.out.println("Retrieving Sticker '"+stickerName+"'");
        	}
        	
        	// Set Clique members (friends given/tagged/labeled with this sticker)
        	stickerObj.addCliqueMemberID(String.valueOf(cliqueMemberId));
        	
        	/*** TODO: TOO SLOW!!! ***/
			/*
			SocialFlowsGroup clique = SocialFlowsGroup.generateGroup(allCliqueMembersURIs, addressBook);
			stickerObj.setClique(clique);
			clique.groupSticker = stickerObj;
			clique.isSaved = true;
			*/

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
		pstmt.close();
		rs.close();
		
		// Initialize explicit clique representation 
		// for each retrieved sticker from SocialFlows DB
		if (this.savedStickers != null) {
			for (Sticker sticker : this.savedStickers) {
				sticker.getCliqueLazily(addressBook);
			}
		}

		/*
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
    	*/

		dbconn.close();
	}

	
	
	/* Add saved stickers to existing hierarchy */
	public void processSavedStickersForHierarchy()
	{
		if (this.savedStickers == null 
			|| this.savedStickers.size() <= 0)
			return;

		// Look at saved stickers that have no parent
		Vector<Sticker> rootSavedStickers = new Vector<Sticker>();
		for (Sticker savedSticker : this.savedStickers) {
			if (savedSticker.parentSticker != null 
				&& savedSticker.parentSticker.getSubsets() != this.hierarchicalStickers) {
				continue;
			}
			savedSticker.parentSticker = null;
			this.hierarchicalStickers.remove(savedSticker);
			rootSavedStickers.add(savedSticker);
		}
		
		List<Sticker> adHocSavedHierarchy 
		= constructHierarchy(rootSavedStickers);
		this.hierarchicalStickers.addAll(0, adHocSavedHierarchy);
	}

	public static void clearHierarchy(List<Sticker> flatList)
	{
		if (flatList == null)
			return;
		for (Sticker sticker : flatList)
		{
			sticker.parentSticker = null;
			sticker.setSubsets(null);
		}
	}
	
	public static List<Sticker> constructHierarchy(List<Sticker> flatList)
	{
		List<Sticker> hierarchicalList = new Vector<Sticker>();
		if (flatList == null)
			return null;
		
		System.out.println("CONSTRUCT HIERARCHY: ");
		System.out.println("--> Has "+flatList.size()+" original stickers");
		
		Collections.sort(flatList, sizeOrder);
		for (Sticker sticker : flatList) 
		{
			sticker.parentSticker = null;
			System.out.println("--> Processing sticker ["+sticker.getCliqueMemberIDs().toString()+"] ...");
			
			boolean isTopLevelGroup = true;
			// Find out if this should be a top-level group or be a subset
			for (Sticker superset : hierarchicalList) 
			{
				if (sticker.isSubsetOf(superset)) {
					if (sticker.getCliqueSize() == superset.getCliqueSize()) {
						System.out.println("--> Found duplicate group");
						break; // duplicate group
					}
					isTopLevelGroup = false;
					System.out.println("--> Found ["+sticker.getCliqueMemberIDs().toString()+"] to be a subset of ["+superset.getCliqueMemberIDs().toString()+"]");
					superset.addSubset(sticker, false); // show duplicate saved stickers
					break;
				}
			}
			if (isTopLevelGroup) {
				hierarchicalList.add(sticker);
				System.out.println("--> Placed ["+sticker.getCliqueMemberIDs().toString()+"] as a TOP-LEVEL GROUP");
			}
		}
		
		Collections.sort(hierarchicalList, sizeOrder);
		return hierarchicalList;
	}
	

	public List<Sticker> getSavedStickers(SocialFlowsAddressBook addressBook)
	{
		if (this.savedStickers == null)
			buildStickerBook(addressBook);
		return this.savedStickers;
	}
	
	public List<Sticker> getHierarchicalStickers(SocialFlowsAddressBook addressBook)
	{
		return this.hierarchicalStickers;
	}
	
	public HashMap<String, Sticker> getSfGroupIDToStickersMap(SocialFlowsAddressBook addressBook)
	{
		if (this.savedStickersBySfGroupId == null)
			buildStickerBook(addressBook);
		return this.savedStickersBySfGroupId;
	}
	
	public Sticker lookupStickerBySfGroupID(String sfGroupID)
	{
		if (this.savedStickersBySfGroupId == null)
			return null;
		return this.savedStickersBySfGroupId.get(sfGroupID);
	}
	
	
	
	public static final String clearAllStickerbookData
	= "DELETE FROM socialflows_stickerbooks "+
	     "WHERE ownerSfId=?";
	
	public static final String clearAllStickerTopologyData
	= "DELETE FROM socialflows_topology "+
	     "WHERE ownerSfId=?";

	public void clearAllData()
	{
		Connection dbconn = null;
		long ownerSfId = this.sfDBConnManager.getCurrentUser().getSfUserId();
		//PreparedStatement pstmt = null;
		Statement stmt = null;
		
		try {
			dbconn = this.sfDBConnManager.getConnection();
			stmt = dbconn.createStatement();
			stmt.addBatch(clearAllStickerbookData.replace("?", String.valueOf(ownerSfId)));
			stmt.addBatch(clearAllStickerTopologyData.replace("?", String.valueOf(ownerSfId)));
			stmt.executeBatch();
			
			//dbconn.prepareStatement(clearAllStickerTopologyData);
			//pstmt.setLong(1, ownerSfId);
			//pstmt.executeUpdate();
		}
		catch (SQLException sqle) {
			System.out.println("CLEAR STICKERBOOK DATA ERROR: "+sqle.getMessage());
			sqle.printStackTrace();
		}
		finally {
			try {
				if (dbconn != null) dbconn.close();
				if (stmt != null) stmt.close();
			} catch (SQLException sqle) {}
		}
	}
	
	
	
	/* PROCESSING SUGGESTED STICKERS */	
	public List<Sticker> getSuggestedStickers()
	{
		return this.suggestedStickers;
	}
	
	public HashMap<String, Sticker> getSuggestedStickersLookup()
	{
		return this.suggestedStickersLookup;
	}
		
	public void clearAllSuggStickers()
	{
		this.suggestedStickers = new Vector<Sticker>();
		this.suggestedStickersLookup = new HashMap<String, Sticker>();
		
		// Go through saved stickers and clear hierarchy
		System.out.println("PROCESSING SAVED STICKERS: ");
		clearHierarchy(this.savedStickers);
		this.hierarchicalStickers.clear();
	}
	
	/* TODO: Is this clearSuggStickersOfType() call expensive? */
	public void clearSuggStickersOfType(String sourceType)
	{
		if (this.suggestedStickers == null ||
			this.suggestedStickers.size() <= 0)
			return;
		
		List<Sticker> suggStickersList 
		= (List<Sticker>) ((Vector<Sticker>)this.suggestedStickers).clone();
		for (Sticker suggSticker : suggStickersList) {
			if (suggSticker.getSourceType().equals(sourceType)) {
				this.removeSuggSticker(suggSticker.stickerID, false);
			}
		}		
	}

	public List<Sticker> 
		constructSuggStickers(JSONArray suggested_groups, 
							  String sourceType,
            				  SocialFlowsAddressBook addressBook) throws JSONException
    {
        if (suggested_groups == null || suggested_groups.length() <= 0)
        	return null;
        
        if (this.suggestedStickers == null)
        	this.suggestedStickers = new Vector<Sticker>();
        if (this.suggestedStickersLookup == null)
        	this.suggestedStickersLookup = new HashMap<String, Sticker>();
        if (this.hierarchicalStickers == null)
        	this.hierarchicalStickers = new Vector<Sticker>();
        
        String sourceTypeStr = "";
        if (sourceType != null) {
        	if (sourceType.equals(Sticker.SOURCE_EMAIL))
        		sourceTypeStr = " from your email data";
        	else if (sourceType.equals(Sticker.SOURCE_PHOTO))
        		sourceTypeStr = " from your photos data";
        }
		
		// Process the suggested groups
		this.setStatusMessage("Constructing your Suggested Social Cliques"+sourceTypeStr+"...");
		
        // Build SocialFlows suggested groups and their subsets
        List<Sticker> additionalHierarchicalStickers = new Vector<Sticker>();
        HashSet<SocialFlowsContactInfo> updatedContacts 
        = new HashSet<SocialFlowsContactInfo>();
        for (int i = 0; i < suggested_groups.length(); i++)
        {
            // Create a Sticker object and its subsets
            JSONObject groupJSON = suggested_groups.optJSONObject(i);
            if (groupJSON == null)
                continue;
            
            this.setStatusMessage("Constructed "+(i+1)+" of "+suggested_groups.length()+" Suggested Social Cliques sets"+sourceTypeStr+"...");
            Sticker proposedSticker
            = Sticker.generateStickerAndSubsets(groupJSON, sourceType, addressBook);
            if (proposedSticker == null)
                continue;
            additionalHierarchicalStickers.add(proposedSticker);
            
            // Do no per-contact metadata updating for social topologies derived from photos
            if (sourceType.equals(Sticker.SOURCE_PHOTO))
            	continue;

            // Updating JSON data with more info on this particular group
            suggested_groups.put(i, groupJSON);
            
            // Update existing contacts for current user's addressbook in SocialFlows DB 
            // with updated social weighting, additional emails, aliases/names
            // as deduced by InSitu and Dunbar social grouping algos/analysis
            for (SocialFlowsContactInfo member : proposedSticker.getClique().getGroupMembers())
            {
                // Do not repeat updates
                if ( updatedContacts.contains(member) )
                   continue;
            	
                // Incremental Diff update on additional emails, aliases/names
                long personContactID = member.getContactId();
                JSONObject emailData = member.getEmailData();
                JSONArray memberEmails = emailData.optJSONArray("emailAddrs");
                JSONArray memberNames  = emailData.optJSONArray("names");
                
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
                
                // This is a new InSitu contact. Will only be saved to SocialFlows DB if it is
                // a member of one of the few social groups configured and saved by the user.
                // Helps reduce the chances of saving a "junk"-contact
                if (personContactID <= 0)
                    continue;
                                
                // Update contact in SocialFlows DB
                try {
                    addressBook.saveContact(member);
                }
                catch (SQLException sqle) {
                	System.out.println("ERROR WHILE UPDATING SOCIAL CONTACT INFO for "+member.getName()+": "+sqle.getMessage());
        			sqle.printStackTrace();
                }

                // Mark as already updated to prevent wasteful repeats
                updatedContacts.add(member);
            } 
        }
        
        // Flatten the generated stickers hierarchy
        if (additionalHierarchicalStickers.size() > 0)
        {
        	// Should be a list where no two groups are exactly identical
            List<Sticker> stickersList = new Vector<Sticker>();
            for (Sticker topLevelSticker : additionalHierarchicalStickers) {
            	Sticker.flattenStickerAndSubsets(topLevelSticker, stickersList);       
            }
            
            // Create stickers for Suggested groups
            for (Sticker proposedSticker : stickersList) {
            	proposedSticker.getClique().groupSticker = proposedSticker;
                String stickerID = "sticker_"+this.suggStickerCounter;
                proposedSticker.stickerID = stickerID;
                this.suggStickerCounter++;
                this.suggestedStickers.add(proposedSticker);
                this.suggestedStickersLookup.put(stickerID, proposedSticker);
            }
            
            this.hierarchicalStickers.addAll(0, additionalHierarchicalStickers);
            return stickersList;
        }
        
        return additionalHierarchicalStickers;
    }




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