<%@page import="java.io.*"%>
<%@page import="java.net.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="com.restfb.exception.FacebookException"%>
<%@page import="edu.stanford.socialflows.export.*"%>
<%@page import="edu.stanford.socialflows.sticker.*"%>
<%@page import="edu.stanford.socialflows.contacts.*"%>
<%@page import="edu.stanford.socialflows.settings.*"%>
<%@page import="edu.stanford.socialflows.log.SocialFlowsLogger"%>
<%@page import="edu.stanford.socialflows.util.StatusProviderInstance"%>
<%@include file="sessionCheck.jsp"%>
<%
	// Get the user's unified list of Person resources (representing Friends/Contacts)
    SocialFlowsAddressBook myAddressBook 
    = (SocialFlowsAddressBook)session.getAttribute(FBAppSettings.MY_ADDRESS_BOOK);
    if (myAddressBook == null) {
        myAddressBook = new SocialFlowsAddressBook(dbconnManager);
        
        // So that we can get continuous status updates about retrieving the unified addressbook
        session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, myAddressBook);
        List<SocialFlowsContactInfo> myFriends = myAddressBook.getUnifiedFriendsList();
        
        session.setAttribute(FBAppSettings.MY_ADDRESS_BOOK, myAddressBook);
    }
    
    // If it still exists, get StickerBook (existing, saved stickers/social groups from backend)
    SocialFlowsStickerBook myStickerBook 
    = (SocialFlowsStickerBook)session.getAttribute(FBAppSettings.MY_STICKER_BOOK);
    if (myStickerBook == null)
    {
    	// Get existing, saved stickers/social groups from DB
        StatusProviderInstance statusUpdate 
        = new StatusProviderInstance("Retrieving your existing social groups...");
        session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, statusUpdate);
    	
        myStickerBook = new SocialFlowsStickerBook(dbconnManager);
        myStickerBook.buildStickerBook(myAddressBook);
        
        session.setAttribute(FBAppSettings.MY_STICKER_BOOK, myStickerBook);
    }


    
    
    String groupsToExportJsonData 
    = request.getParameter("input");
    //System.out.println(request.getServletPath()
    //                   +" - JSON input data is (in String format):\n"
    //                   +groupsToExportJsonData);

    JSONObject result = new JSONObject();
    if (groupsToExportJsonData == null || groupsToExportJsonData.trim().length() <= 0) {
    	result.put("success", false);
    	result.put("message", "The export groups JSON data given is empty!");
    	out.println(result.toString());
    	return;
    }
    
    
    
    JSONObject groupsExportInfo = new JSONObject(groupsToExportJsonData);
    String gmailSessionToken = groupsExportInfo.optString(FBAppSettings.GMAIL_TOKEN, null);
    JSONArray groupsToExport = groupsExportInfo.optJSONArray("groupsToExport");
    JSONArray savedStickers  = new JSONArray();
    HashMap<String, JSONObject> newContacts = new HashMap<String, JSONObject>();
    
    // Save the list of stickers
    StatusProviderInstance statusUpdate 
    = new StatusProviderInstance("Saving your social groups...");
    session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, statusUpdate);
    
    //System.out.println("JSON printout: \n"+groupsExportInfo.toString(1)+"\n");
    //System.out.println("Saving your social groups...\n");
    
    for (int i = 0; (groupsToExport != null) && (i < groupsToExport.length()); i++)
    {
    	JSONObject stickerData = groupsToExport.getJSONObject(i);
    	String stickerJsonData = stickerData.toString();
    	String saveResultStr   = myStickerBook.saveSticker(stickerJsonData, myAddressBook);
    	
        JSONObject saveResult = new JSONObject(saveResultStr);
        if (saveResult.getBoolean("success"))
        {
        	statusUpdate.setStatusMessage("Saving <b>"+saveResult.getString("name")+"</b> social group...");
        	JSONObject savePref = stickerData.getJSONObject("savePref");
        	if (savePref != null) {
        		saveResult.put("saveAsFBGroup", 
        			           savePref.optBoolean("saveAsFBGroup", false));
        		saveResult.put("saveAsFBFriendList", 
        				       savePref.optBoolean("saveAsFBFriendList", false));
        		
        		System.out.println("--> saveAsFBGroup: "+saveResult.getBoolean("saveAsFBGroup"));
        		System.out.println("--> saveAsFBFriendList: "+saveResult.getBoolean("saveAsFBFriendList"));
        	}
        	savedStickers.put(saveResult);
        	//System.out.println("Saving <b>"+saveResult.getString("name")+"</b> social group...");

        	// Process the list of newly saved contacts
        	JSONArray newlySavedContacts = saveResult.optJSONArray("newContacts");
        	if (newlySavedContacts != null) {
        		for (int j = 0; j < newlySavedContacts.length(); j++) {
        		    JSONObject savedContact = newlySavedContacts.optJSONObject(j);
        		    if (savedContact != null) {
        		    	String userID = savedContact.optString("userID", null);
        		    	//String sfContactID = savedContact.optString("sfContactID", null);
        		    	if (!newContacts.containsKey(userID))
        		    		newContacts.put(userID, savedContact);
        		    }
        		}
        	}

        }
        else {
        	// If sticker was not successfully saved, it should be skipped
        	continue;
        }

        long topologyRunID 
        = Long.parseLong(stickerData.optString("stickerRunID", "0"));
        sfLogger.logTopologyEdits(topologyRunID,
        		                  SocialFlowsLogger.RECORD_GROUP_SAVE, 
        		                  stickerData);
    }
    
    
    /* // Generating a random FB Friends List ID
    String initialFlId = String.valueOf(Math.round((double)Math.random()*(double)1000000))
                         + String.valueOf(Math.round((double)Math.random()*(double)1000000));
    long latestFlId = 0L;
    //long latestFlId = Math.round(Math.random()*1000000000000);
    HashMap<String, FBFriendList> fbFriendListsByName = new HashMap<String, FBFriendList>();
    if (latestFlId <= 0L)
    	latestFlId = Long.parseLong(initialFlId);
    else
    	latestFlId++;
    */
    


    


    //log('Saving '+sticker.name+' as a Friend List...');
    //    $.facebox.setLoadingMessage(
    //       'Saving your <b>'+sticker.name+'</b> group as a Friend List...');

	// Track and update status of social group exports to Facebook
    FBFriendListExporter fbfle = new FBFriendListExporter(fbs);
    session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, fbfle);
    fbfle.initFriendLists();


    // Now, check for FB Friends Lists that exist, 
    // and create new friends lists if they do not exist
    String resultMsg = "The following social groups were successfully saved to your Facebook account: ";
    int numExportedToFB = 0;
    for (int i = 0; i < savedStickers.length(); i++)
    {
        JSONObject stickerSaveResult = savedStickers.optJSONObject(i);
        if (stickerSaveResult == null)
        	continue;
        String stickerSfId = stickerSaveResult.optString("stickerSfID", null);
        if (stickerSfId == null)
            continue;
        
        Sticker sticker = myStickerBook.lookupStickerBySfGroupID(stickerSfId);
        if (sticker == null)
        	continue;
        // Check if any contacts in this social group are FB friends
        boolean exportToFB = false;
        for (SocialFlowsContactInfo contact : sticker.getClique().getGroupMembers()) {
        	if (contact.getFBUids() == null)
        		continue;
        	if (contact.getFBUids().size() > 0) {
        		exportToFB = true;
        		numExportedToFB++;
        		if (i > 0)
                    resultMsg += ", ";
                resultMsg += "<b>"+sticker.getStickerName()+"</b>";
        		break;
        	}
        }
        
        // Save to FB as friend list
        String flid = null;
        try {
        	flid = fbfle.exportToFacebook(sticker);
        }
        catch (FacebookException fe) {
        	System.out.println("Problem trying to save sticker '"
        			           +sticker.getStickerName()+"' to Facebook: \n"
        			           +fe.getMessage());
        	fe.printStackTrace();
        }
        
        
        stickerSaveResult.putOpt("exportToFB", exportToFB);
        stickerSaveResult.putOpt("FBflid", String.valueOf(flid));
        
        // Denote how sticker/group is to be saved to Facebook
        stickerSaveResult.put("saveAsFBFriendList",
        		              stickerSaveResult.optBoolean("saveAsFBFriendList", false));
        stickerSaveResult.put("saveAsFBGroup",
        		              stickerSaveResult.optBoolean("saveAsFBGroup", false));

        
        System.out.println("The sticker save result JSON:\n"+stickerSaveResult.toString(1));        
    }

    // Check if any groups can be exported to FB
    if (numExportedToFB <= 0) {
    	resultMsg = "No social groups could be saved to Facebook since none of them had any of your Facebook friends.";
    }


    // Output JSON of results
    result.put("success", true);
    result.put("message", resultMsg);

    // Include the list of newly saved social contacts
    JSONArray newlySavedContacts = new JSONArray();
    for (JSONObject savedContact : newContacts.values()) {
    	newlySavedContacts.put(savedContact);
    }
    result.put("newContacts", newlySavedContacts);
    
    // Add the list of newly saved stickers
    result.put("savedStickers", savedStickers);

    session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);

    System.out.println(request.getServletPath()+": Return result is:\n"+result.toString(1));
    out.println(result.toString());
%>