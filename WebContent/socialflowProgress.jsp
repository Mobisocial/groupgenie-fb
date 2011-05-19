<%// Prevents browser-side caching of page results
response.setHeader("Cache-Control","no-cache");
response.setHeader("Pragma","no-cache");
response.setDateHeader ("Expires", 0);%>
<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="edu.stanford.socialflows.algo.AlgoStats"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@page import="edu.stanford.prpl.insitu.data.PrPlPhotoBook"%>
<%@page import="edu.stanford.socialflows.db.DBConnectionManager"%>
<%@page import="edu.stanford.socialflows.sticker.StickerIcons.StickerIcon"%>
<%@page import="edu.stanford.socialflows.contacts.SocialFlowsAddressBook"%>
<%@page import="edu.stanford.socialflows.contacts.SocialFlowsContactInfo"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@include file="sessionCheck.jsp"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>SocialFlows</title>
<script type="text/javascript" src="common/json2min.js"></script>
<script type="text/javascript" src="common/jscommon.js"></script>
<script type="text/javascript" src="jquery/jquery-1.5.1.min.js"></script>
<script type="text/javascript" src="jquery/jquery.jeditable.js"></script>
<script type="text/javascript" src="jquery/jquery-ui-1.8.11.custom.min.js"></script>

<link type="text/css" rel="stylesheet" media="screen" href="facebox/facebox.css"/>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/faceUI.css"/>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/faceUIBody.css"/>
<link type="text/css" rel="stylesheet" media="screen" href="stickers/stickers.css"/>
<style>

.faceUI .friend_grid_col.fbFriend {
    margin-bottom:10px;
}

div.progress-display {
    padding-bottom:0px;
    padding-top:50px;
    text-align:center;
    padding-left:5px;
    padding-right:5px;
    /*
    -moz-border-radius-bottomleft:4px;
    -moz-border-radius-bottomright:4px;
    -moz-border-radius-topleft:4px;
    -moz-border-radius-topright:4px;
    background-image:url(images/bluetext.jpg);
    border:1px solid #C0C0C0;
    */
    
    
}

.progress-display #progressmessage {
    font-size:125%;
}

</style>

<link href="socialflow/socialflow.css" rel="stylesheet" type="text/css" media="screen"/>
<script type="text/javascript">
var socialflowProcessDone = false;
var dunbarGroupsGeneratorURL = "<%=FBAppSettings.DUNBAR_GROUPGENERATOR_URL%>";
var isEmailEnabled = <%=FBAppSettings.ENABLE_EMAILS%>;
var isDevMode      = <%=FBAppSettings.ENABLE_DEV_MODE%>;
var isDemoMode     = false;

function processSocialflow()
{
	if (socialflowProcessDone)
		return;
    $.ajax({
    	  url: '<%=response.encodeURL("getStatus.jsp")%>',
    	  success: function(response) {
               // jQuery.trim(response);
    	       response = response.replace(/^\s+|\s+$/g, ''); // remember to strip whitespace, used to be a bug
    	       if (response.length > 0)
    	    	  $('#progressmessage').html(response);
    	       setTimeout ("processSocialflow()", 1000); // keep polling till cleared
    	  }
    });
}

function displaySocialflowError(errorMsg)
{
	socialflowProcessDone = true;
	$('#progressmessage').html(errorMsg);
	$('#loading').hide();

	var retry = $('#retry');
    if (retry.length != 0) {
    	retry.click(function(){
            window.location.reload(true);
            //return false;
        });
    }
}
</script> 
</head>
<body>
<%
	// Determine SocialFlows title display
      String title = "SocialFlows",
             titlefullname = "", titlename = "";
      titlename = fbMyInfo.getFirstName();
      if (titlename == null || titlename.isEmpty())
    	  titlename = fbMyInfo.getName();
      titlefullname = fbMyInfo.getName();
      if (titlefullname == null || titlefullname.isEmpty())
    	  titlefullname = titlename;
      if (titlename == null) titlename = "";
      if (titlefullname == null) titlefullname = "";

      String titleCSS = ""; boolean showUserPic = false;
      if (!FBAppSettings.ENABLE_AMT_MODE &&
    	  fbMyInfo.getPicSquare() != null && !fbMyInfo.getPicSquare().isEmpty() 
    	  && !titlename.isEmpty())
      {
    	  titleCSS = "padding-top: 8px; padding-bottom: 5px; font-size: 150%;";
    	  showUserPic = true;
    	  
    	  // Construct proper title (e.g. Luke Skywalker's SocialFlows)
    	  titlename = titlename.trim();
    	  if (titlename.charAt(titlename.length()-1) == 's' 
    		  || titlename.charAt(titlename.length()-1) == 'S')
    		  title = titlename+"' "+title;
    	  else
    		  title = titlename+"'s "+title;
      }
%>
<div id="container">
	<div id="header" class="socialflow">
	   <div title="Save My Groups" class="buttons socialflow" style="display:none;"><a id="save_groups" href="javascript:;"><img src="stickers/disk.gif" title="Save My Groups"/> Save My Groups</a></div>
	   <div title="Add a New Group" class="buttons socialflow" style="display:none;"><a id="create_group" href="javascript:;" style="padding-right: 5px; padding-bottom: 9px;"><img src="stickers/plus.png" title="Add a New Group"/></a></div>
	   <div class="socialflow-title">
       <h1 style="<%=titleCSS%>"><%
       	if (showUserPic) {
       %>
       <img style="vertical-align: middle; padding-right: 8px; height: 50px; width: 50px;" class="contact" src="<%=fbMyInfo.getPicSquare()%>" title="<%=titlefullname%>"><%
       	}
       %><%=title%>
       &nbsp;<script src="http://connect.facebook.net/en_US/all.js#xfbml=1"></script><fb:like href="http://www.facebook.com/apps/application.php?id=149801675080418" layout="button_count" show_faces="false" width="80" font="lucida grande"></fb:like>       
       </h1>
       </div>
    </div>
	<div id="content">
		
		<noscript><p><strong>Requires Javascript for viewing</strong></p></noscript>
		
		<!--- Progress Display --->
        <div id="progress-display" class="progress-display">
        <img id="loading" src="facebox/loading.gif">
        <br/><br/>
        <!-- Analyzing Social Cliques from your email... -->
        <span id="progressmessage">Building Your Unified Contacts List... </span>
        </div>

        <script type="text/javascript">
        processSocialflow();
        </script>

        <ul id="socialtree" class="socialflow socialflow-root socialflow-init-display" style="display: none;"> <%
        out.flush();
         
         // Make sure session is not yet expired
         JSONObject fbSessionObj = (JSONObject)session.getAttribute(FBAppSettings.FACEBOOK_SESSION_OBJ);
         if (fbSessionObj == null)
         {
            // ERROR: PRINT OUT THAT SESSION HAS EXPIRED
            System.out.println("Error: Your session has expired! Please reaccess the application again.");%>
            <script type="text/javascript">
            displaySocialflowError("Error: Your session has expired! Please reaccess the application again.");
            </script>
            <%
            return;
         }

         // Get the user's unified list of Person resources (representing Friends/Contacts)
         SocialFlowsAddressBook myAddressBook 
         = (SocialFlowsAddressBook)session.getAttribute(FBAppSettings.MY_ADDRESS_BOOK);
         if (myAddressBook == null) {
             myAddressBook = new SocialFlowsAddressBook(dbconnManager);
             session.setAttribute(FBAppSettings.MY_ADDRESS_BOOK, myAddressBook);
             
             // Get latest FB social graph
             DBFacebookConnector dbfbh = new DBFacebookConnector("FB", dbconnManager, fbs);
             session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, dbfbh);
             dbfbh.processSocialGraph();
             session.setAttribute(FBAppSettings.MY_FB_FRIENDS_INFO, dbfbh.getFriendsFBProfiles());
         }

         // Check if current FB social graph is available
         List<SocialFlowsContactInfo> myFBFriends 
         = (List<SocialFlowsContactInfo>)session.getAttribute(FBAppSettings.MY_FB_FRIENDS_INFO);
         session.removeAttribute(FBAppSettings.MY_FB_FRIENDS_INFO); // save memory

         // So that we can get continuous status updates about retrieving the unified addressbook
         session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, myAddressBook);
         List<SocialFlowsContactInfo> myFriends = myAddressBook.getUnifiedFriendsList(myFBFriends);
         if (myFriends == null)
         {
            // TODO: Change to relevant error message
                                                            
            // ERROR: PRINT OUT THAT SESSION HAS EXPIRED
            System.out.println("Error: Trouble obtainining your unified social contacts list from SocialFlows database");
            %>
            <script type="text/javascript">
            displaySocialflowError("Error: Trouble obtaining your unified social contacts list. Please <a id=\"retry\" href=\"#\">retry</a>.");
            </script>
            <%
            return;
         }
         System.out.println("OBTAINED UNIFIED ADDRESSBOOK!!!");
                    
         // Get existing, saved stickers/social groups from DB
         StatusProviderInstance statusUpdate = new StatusProviderInstance("Retrieving your existing social groups...");
         session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, statusUpdate);

                    
         /*** ERROR HANDLING ***/
         try
         {
        	 SocialFlowsStickerBook myStickerBook
             = (SocialFlowsStickerBook)session.getAttribute(FBAppSettings.MY_STICKER_BOOK);
             if (myStickerBook == null)
             {
                myStickerBook = new SocialFlowsStickerBook(dbconnManager);
                if (myStickerBook == null) {
                   // TODO: Show relevant, better message
                   // ERROR: PRINT OUT THAT SESSION HAS EXPIRED
                   System.out.println("Error: Trouble obtaining your existing Stickers (social groups)");
                }
                else {
                   myStickerBook.buildStickerBook(myAddressBook);
                   session.setAttribute(FBAppSettings.MY_STICKER_BOOK, myStickerBook);
                }
             }
             //System.out.println("OBTAINED STICKERBOOK!!!");

             // Get current SocialFlows session info
             boolean showPhotosTopo = false,  showEmailTopo = false;
             JSONObject suggPhotosTopo = null, suggEmailTopo = null;
             if (sfSessionObj != null) {
            	 String suggEmailTopoJSON 
                 = sfSessionObj.optString(FBAppSettings.MY_SOCIALFLOW_JSON, null);
                 if (suggEmailTopoJSON != null)
                     suggEmailTopo = new JSONObject(suggEmailTopoJSON);
                 sfSessionObj.remove(FBAppSettings.MY_SOCIALFLOW_JSON);  // save memory
                     
                 suggPhotosTopo = (JSONObject)sfSessionObj.optJSONObject(FBAppSettings.MY_FB_PHOTOTAG_TOPO);
                 sfSessionObj.remove(FBAppSettings.MY_FB_PHOTOTAG_TOPO); // save memory
             }

                    // Basically clear all suggested stickers, but keep the saved stickers
                    if (suggEmailTopo != null && suggPhotosTopo != null) {
                    	myStickerBook.clearAllSuggStickers();
                    }
                    
                    // Construct the list of suggested stickers (for email)
                    if (suggEmailTopo != null)
                    {
                        JSONArray suggested_groups = suggEmailTopo.optJSONArray("groups");
                        if (suggested_groups != null && suggested_groups.length() > 0)
                        {
                        	// Provide status as suggested groups are constructed
                            session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, myStickerBook);
                        	myStickerBook.clearSuggStickersOfType(Sticker.SOURCE_EMAIL);
                        	
                        	showEmailTopo = true;
                        	List<Sticker> emailGroups
                            = myStickerBook.constructSuggStickers(suggested_groups,
                            		                              Sticker.SOURCE_EMAIL,
                            		                              myAddressBook);

                        	// Only on new generation of social topology do we increase the id number
                        	sfLogger.logTopologyRunToDB(suggEmailTopo, emailGroups);
                            //System.out.println("CONSTRUCTED SUGGESTED STICKERS (For Email)");
                        }
                    }

                    // Construct the list of suggested stickers (for photos)
                    if (suggPhotosTopo != null)
                    {
                    	JSONArray suggested_groups = suggPhotosTopo.optJSONArray("groups");
                    	if (suggested_groups != null && suggested_groups.length() > 0)
                        {
                            // Provide status as suggested groups are constructed
                            session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, myStickerBook);
                            myStickerBook.clearSuggStickersOfType(Sticker.SOURCE_PHOTO);
                            
                            showPhotosTopo = true;
                            List<Sticker> photoGroups
                            = myStickerBook.constructSuggStickers(suggested_groups, 
                            		                              Sticker.SOURCE_PHOTO,
                            		                              myAddressBook);
                            
                            // Only on new generation of social topology do we increase the id number
                            sfLogger.logTopologyRunToDB(suggPhotosTopo, photoGroups);
                            //System.out.println("CONSTRUCTED SUGGESTED STICKERS (For Photos)");
                        }
                    }
                    
                    // Distribute saved stickers properly into present hierarchy
                    if (suggEmailTopo != null || suggPhotosTopo != null) {
                    	session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, myStickerBook);
                    	myStickerBook.processSavedStickersForHierarchy();
                    	
                        System.out.println("DISTRIBUTED SAVED STICKERS into present hierarchy");
                        /*
                        boolean testException = true;
                        if (testException) {
                            throw new Exception("DISTRIBUTED SAVED STICKERS into present hierarchy");
                        }
                        */
                    }
                    
                    
                    
                    
                    // Generate the JSON file containing the saved and suggested social groups data
                    // data.put("groups", suggested_groups);
                    // String jsonOutput = data.toString(3);
                    
                    // System.out.println("The JSON file data will be: \n"+jsonOutput+"\n");
                    
                    /*
                    // Create the dir structure
                    String documentRootPath = application.getRealPath("/insitudata").toString();
                    String dirPath = documentRootPath + File.separatorChar + prplUserID;
                    new File(dirPath).mkdirs();
                    
                    String fileLoc = dirPath + File.separator + "insitugroups.json.txt";
                    
                    // System.out.println("Output to file: "+fileLoc);
                    
                    PrintWriter pw = new PrintWriter(new FileOutputStream(fileLoc));
                    pw.println(jsonOutput);
                    pw.close();
                    // System.out.println("File successfully created!");
                    
                    // Set where JSON file is generated
                    String relativeLoc = "insitudata/" + prplUserID + "/" + "insitugroups.json.txt";
                    session.setAttribute(FBAppSettings.JSON_INSITU_GROUPS_LOC, relativeLoc);
                    */
                    
                    
                    
                    // Generate UI display
                    List<Sticker> suggestedStickers 
                    = myStickerBook.getSuggestedStickers();
                    HashMap<String, Sticker> suggestedStickersLookup 
                    = myStickerBook.getSuggestedStickersLookup();

                    // out.print(suggestedStickers.size()+" possible groups have been suggested.");
                    session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);

                    // TODO: Determine the best way to sort list for UI display
                    List<Sticker> fullHierarchy
                    = myStickerBook.getHierarchicalStickers(myAddressBook);
                    //if (savedStickers != null)
                    //   Collections.sort(savedStickers);

                    boolean hasShownExpansionCue = false;
                    if (fullHierarchy != null)
                    {
                        for (Sticker sticker : fullHierarchy)
                        {
                            String groupRenderingHTML;
                            SocialFlowsGroup group = sticker.getCliqueLazily(myAddressBook);
                            if (!hasShownExpansionCue && !sticker.isSaved
                            	&& sticker.getSubsets() != null 
                            	&& !sticker.getSubsets().isEmpty())
                            {
                                groupRenderingHTML = SocialFlowsGroup.renderSocialFlowUI(group, 4, true);
                                hasShownExpansionCue = true;
                            }
                            else {
                                groupRenderingHTML = SocialFlowsGroup.renderSocialFlowUI(group);
                            }
                            
                            // SocialFlow HTML layout for social group and its "descendant" subsets
                            out.println(groupRenderingHTML);
                            
                        } // end-of-loop for displaying top-level groups and their subsets
                        
                    } // looping through both suggested & saved groups
            %>
        </ul>
		
	</div>
</div>
<p><!-- Placeholder --></p>

<%
	/*
     // Get our unified collection of photo albums
     SocialFlowsPhotoBook myPhotoBook
     = (SocialFlowsPhotoBook)session.getAttribute(FBAppSettings.MY_PHOTO_BOOK);
     List<BaseAlbumInfo> myPhotoAlbums = null;

     HashMap<String, BaseAlbumInfo> myPhotoAlbumsByURI = null;
     if (myPhotoBook == null)
     {
    	 myPhotoBook = new SocialFlowsPhotoBook(prplService_);

         // So that we can get continuous status updates about retrieving the unified collection of photo albums
         //session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, myPhotoBook);
         myPhotoBook.buildPrPlPhotoBook();
         myPhotoAlbums      = myPhotoBook.getPhotoAlbumsList();
         myPhotoAlbumsByURI = myPhotoBook.getURIToPhotoAlbumMap();
         session.setAttribute(FBAppSettings.MY_PHOTO_BOOK, myPhotoBook);
     }
     // Sort photo albums by how recent they were last modified   
     myPhotoAlbums      = myPhotoBook.getPhotoAlbumsList();
     myPhotoAlbumsByURI = myPhotoBook.getURIToPhotoAlbumMap();
     Collections.sort(myPhotoAlbums);
     
     System.out.println("You have "+myPhotoAlbums.size()+" photo albums in your unified collection");
     
     //collectionSelection.jsp
     */


     // <!-- Muse Email Integration -->
     // Checks for Muse running locally in background and
     // prompts user for Muse download or email credentials
     if (FBAppSettings.ENABLE_EMAILS) { %>
        <%@include file="museEmailIntegration.jsp"%>
     <% } %>

<div class="hidden" id="overlayCover"></div>
<%@include file="cliqueSelection.jsp"%>
<% if (FBAppSettings.ENABLE_ACCESS_CONTROL) { %>
<%@include file="stickerIconSelection.jsp"%>
<%@include file="stickerDisplay.jsp"%>
<% } %>
<%@include file="controlPanel.jsp"%>
<%@include file="dialogBox.jsp"%>
<%@include file="saveDisplayFB.jsp"%>
<%@include file="saveDisplayGmail.jsp"%>
<%
	/*** ERROR HANDLING ***/
        }
        catch (Exception e) {
        	
        	// Log exception to DB
            Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            String errorLog 
            = request.getServletPath()+": "+
              "An error occured while rendering the SocialFlows UI:\n"
              +result.toString();
            sfLogger.logError(errorLog);
            printWriter.close(); result.close();
        }
%>
<script type="text/javascript" src="facebox/facebox.js"></script>
<script type="text/javascript" src="common/statusUpdate.js"></script>
<script type="text/javascript">
var socialflowsEnableAccessControl = <%=FBAppSettings.ENABLE_ACCESS_CONTROL%>;
</script>
<script type="text/javascript" src="socialflow/socialflow.js"></script>
</body></html>