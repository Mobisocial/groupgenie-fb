<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="edu.stanford.socialflows.sticker.*"%>
<%@page import="edu.stanford.socialflows.contacts.*"%>
<%@page import="edu.stanford.socialflows.util.StatusProviderInstance"%>
<%@include file="sessionCheck.jsp"%>
<%
	String stickerSfId = request.getParameter("sfGroupId");
    if (stickerSfId == null || stickerSfId.isEmpty())
        return;
    
    String fbExportType = request.getParameter("fbExportType");
    if (fbExportType == null || fbExportType.isEmpty())
    	return;

    int SFGROUP_ID = -1;
    try { SFGROUP_ID = Integer.parseInt(stickerSfId); } 
    catch (NumberFormatException nfe) { }
    if (SFGROUP_ID == -1)
        return;
    
    // FB form post params
    String post_form_id, fb_dtsg, lsd;
    
    JSONObject fbAuthObject
    = (JSONObject)session.getAttribute(FBAppSettings.FB_AUTH_OBJ);
    if (fbAuthObject == null) {
    	post_form_id = request.getParameter("post_form_id");
        fb_dtsg = request.getParameter("fb_dtsg");
        if (fb_dtsg == null)
            fb_dtsg = "";
        lsd = request.getParameter("lsd");
        if (lsd == null)
            lsd = "";
    }
    else {
    	post_form_id = fbAuthObject.optString(FBAppSettings.FB_POSTFORMID, "");
    	fb_dtsg = fbAuthObject.optString(FBAppSettings.FB_DTSG, "");
    	lsd = "";
    }



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
        StatusProviderInstance statusUpdate = new StatusProviderInstance("Retrieving your existing social groups...");
        session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, statusUpdate);
        
        myStickerBook = new SocialFlowsStickerBook(dbconnManager);
        myStickerBook.buildStickerBook(myAddressBook);
        
        session.setAttribute(FBAppSettings.MY_STICKER_BOOK, myStickerBook);
    }
    
    Sticker sticker = myStickerBook.lookupStickerBySfGroupID(stickerSfId);
    if (sticker == null)
        return;

    SocialFlowsGroup group = sticker.getClique();
    if (group == null || group.getGroupMembers() == null) {
    	System.out.println(request.getServletPath()+": Group with Sf ID "+stickerSfId+" has no group!!!");
    	return;
    }
    if (group.getGroupMembers().size() <= 0) {
        System.out.println(request.getServletPath()+": Group with Sf ID "+stickerSfId+" has 0 group members!!!");
        return;
    }
    
    
    String fbExportName = "", fbPostURL = "";
    if (fbExportType.equals("fbFriendList")) {
    	fbExportName = "Facebook Friend List";
    	fbPostURL    = "http://www.facebook.com/friends/ajax/superfriends_add.php?__a=1";
    }
    else if (fbExportType.equals("fbGroup")) {
    	fbExportName = "Facebook Group";
        fbPostURL    = "http://www.facebook.com/ajax/groups/create_post.php?__a=1";
    }
    else
    	return;
%>
<html>
<head>
<title>Saving Your '<%=sticker.getStickerName()%>' Group as a <%=fbExportName%></title>
<script type="text/javascript" src="jquery/jquery-1.5.1.min.js"></script>
<script type="text/javascript">
$(document).ready(function () {
    // Create or identify form
    var selectedForm = document.forms["postSFGroupToFB"];

    // Submit form automatically & programmatically using JS
    // to publish this social group as a FB friendlist
    selectedForm.submit();

});
</script>
</head>
<body>

<form id="postSFGroupToFB" method="post" action="<%=fbPostURL%>" >
<!--  ... name="reg" onsubmit="return false;">  -->
<%
   int i = 0;
   for (SocialFlowsContactInfo sfContact : group.getGroupMembers())
   {
	   if (sfContact.getFBUids() == null)
		   continue;
	   if (sfContact.getFBUids().iterator().hasNext()) {
		   String fbUid = sfContact.getFBUids().iterator().next();
	       out.println("\n<input type=\"hidden\" name=\"members["+i+"]\" value=\""+fbUid+"\" />");
	       i++;
	   }
   }
%>

<input type="hidden" name="name" value="<%=sticker.getStickerName()%>" />
<input type="hidden" name="post_form_id" value="<%=post_form_id%>" />
<input type="hidden" name="fb_dtsg" value="<%=fb_dtsg%>" />
<input type="hidden" name="lsd" value="<%=lsd%>" />
<input type="hidden" name="post_form_id_source" value="AsyncRequest" />

<%

    if (fbExportType.equals("fbFriendList"))
    {
    	String fbListId = request.getParameter("fbListId");
%>
<!-- FB Friend List specific -->
<input type="hidden" name="action" value="create" /> 
<input type="hidden" name="list_id" value="<%=fbListId%>" />
<input type="hidden" name="redirect" value="false" />    	
<%
    }
    else if (fbExportType.equals("fbGroup"))
    {
%>
<!-- FB Group specific -->
<input type="hidden" name="create" value="Create" /> 
<input type="hidden" name="icon" value="0" />
<input type="hidden" name="privacy" value="closed" />
<input type="hidden" name="__d" value="1" />      
<%
    }

%>

</form>
</body>
</html>