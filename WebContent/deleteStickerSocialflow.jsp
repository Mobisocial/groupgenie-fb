<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="edu.stanford.prpl.insitu.*"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@page import="edu.stanford.socialflows.data.*"%>
<%@page import="edu.stanford.socialflows.sticker.*"%>
<%@page import="edu.stanford.socialflows.contacts.*"%>
<%@page import="edu.stanford.socialflows.util.*"%>
<%@page import="edu.stanford.socialflows.log.SocialFlowsLogger"%>
<%@include file="sessionCheck.jsp"%>
<%
	// Get the user's unified list of Person resources (representing Friends/Contacts)
    SocialFlowsAddressBook myAddressBook 
    = (SocialFlowsAddressBook)session.getAttribute(FBAppSettings.MY_ADDRESS_BOOK);
    if (myAddressBook == null) {
        myAddressBook = new SocialFlowsAddressBook(dbconnManager);
        session.setAttribute(FBAppSettings.MY_ADDRESS_BOOK, myAddressBook);
    }
    
    // If it still exists, get StickerBook (existing, saved stickers/social groups from backend)
    SocialFlowsStickerBook myStickerBook 
    = (SocialFlowsStickerBook)session.getAttribute(FBAppSettings.MY_STICKER_BOOK);
    if (myStickerBook == null)
    {
        myStickerBook = new SocialFlowsStickerBook(dbconnManager);
        //myStickerBook.buildPrPlStickerBook(myAddressBook);
        //session.setAttribute(FBAppSettings.MY_STICKER_BOOK, myStickerBook);
    }
    
    // List<Sticker> currentStickers = myStickerBook.getStickersList(myFriendsByURI);
    // HashMap<String, Sticker> uriToStickers = myStickerBook.getURIToStickersMap(myFriendsByURI);
    // currentStickers.remove(uriToStickers.get(stickerURI));
    // uriToStickers.remove(stickerURI);
    

    // TODO: Not force a refresh everytime a new or existing sticker
    //       is saved, or an existing sticker is deleted
    //myStickerBook.buildStickerBook(myAddressBook);
    

    /*  
        // Input JSON string format
        {
            "stickerID": "sticker_1820830591",
            "name": "InSitu Ninjas",
            "stickerURI": "null"
        }
    */
    String stickerJsonData = request.getParameter("input");
    System.out.println("deleteStickerSocialflow.jsp: JSON data is (in String format):\n"+stickerJsonData);
    
    if (stickerJsonData == null || stickerJsonData.trim().length() <= 0) 
    {
    	JSONObject result = new JSONObject(stickerJsonData);
    	result.put("success", false);
    	result.put("message", "The sticker JSON data given is empty!");
    	out.println(result.toString());
    	return;
    }
    
    JSONObject stickerObj = new JSONObject(stickerJsonData);
    String stickerID   = stickerObj.getString("stickerID");
    String stickerSfID = stickerObj.optString("stickerSfID", null);
    String stickerName = stickerObj.getString("name");
    boolean deleteThroughMerge = false;
    if (stickerObj.has("deleteThroughMerge"))
        deleteThroughMerge = stickerObj.getBoolean("deleteThroughMerge");
    boolean isSuggestedSticker = true;
    long topologyRunID = Long.parseLong(stickerObj.optString("stickerRunID", "0"));
    
    
    // Need to delete Sticker from SocialFlows DB
    if (stickerSfID != null && !stickerSfID.trim().equals("null"))
    {
    	myStickerBook.removeSticker(Long.parseLong(stickerSfID), true);
    	isSuggestedSticker = false;

    	System.out.println("Successfully deleted Sticker with SfID "+stickerSfID);
    }

    if (isSuggestedSticker)
    {
    	// Remove the suggested group that was explicitly deleted by user
    	myStickerBook.removeSuggSticker(stickerID, true);
    	
    	System.out.println("Suggested sticker "+stickerID+" successfully removed.");
    }


    // Record deletion edits
    sfLogger.logTopologyEdits(topologyRunID,
    		                  SocialFlowsLogger.RECORD_GROUP_DELETE, 
    		                  stickerObj);
    
    JSONObject result = new JSONObject(stickerJsonData);
    result.put("success", true);
    if (isSuggestedSticker)
    	result.put("message", "Group was successfully deleted.");
    else
        result.put("message", stickerName+" was successfully deleted.");
    //result.put("name", stickerName);
    out.println(result.toString());
%>