<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="edu.stanford.prpl.insitu.*"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.socialflows.sticker.*"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@page import="edu.stanford.socialflows.contacts.*"%>
<%@page import="edu.stanford.socialflows.data.PrPlAlbumInfo"%>
<%@page import="edu.stanford.socialflows.log.SocialFlowsLogger"%>
<%@page import="edu.stanford.socialflows.sticker.*"%>
<%@page import="edu.stanford.socialflows.sticker.StickerIcons.StickerIcon"%>
<%@include file="sessionCheck.jsp"%>
<%
	// Get the user's unified list of Person resources (representing Friends/Contacts)
    SocialFlowsAddressBook myAddressBook 
    = (SocialFlowsAddressBook)session.getAttribute(FBAppSettings.MY_ADDRESS_BOOK);
    if (myAddressBook == null) {
        myAddressBook = new SocialFlowsAddressBook(dbconnManager);
        //session.setAttribute(FBAppSettings.MY_ADDRESS_BOOK, myAddressBook);
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


    // TODO: Save sticker image somehow
    String stickerJsonData = request.getParameter("input");
    System.out.println("SAVE STICKER - JSON data is (in String format):\n"+stickerJsonData);

    JSONObject result = new JSONObject();
    if (stickerJsonData == null || stickerJsonData.trim().length() <= 0) {
    	result.put("success", false);
    	result.put("message", "The sticker JSON data given is empty!");
    	out.println(result.toString());
    	return;
    }
    
    // Set where JSON file is generated
    //String relativeLoc = "insitudata/" + prplUserID + "/" + "insitugroups.json.txt";
    //session.setAttribute(FBAppSettings.JSON_INSITU_GROUPS_LOC, relativeLoc);

    String saveResult = myStickerBook.saveSticker(stickerJsonData, myAddressBook);
    //result = new JSONObject(saveResult);
    //if (result.getBoolean("success")) {
    //}

    // So that we can get continuous status updates
    // session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, fbh);
    
    
    JSONObject stickerData = new JSONObject(stickerJsonData);
    long topologyRunID 
    = Long.parseLong(stickerData.optString("stickerRunID", "0"));
    sfLogger.logTopologyEdits(topologyRunID,
    		                  SocialFlowsLogger.RECORD_GROUP_SAVE, 
    		                  stickerData);

    
    // out.println("Successfully saved Sticker.");
    session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);

    out.println(saveResult);
%>