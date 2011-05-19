<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="com.gargoylesoftware.htmlunit.*"%>
<%@page import="com.gargoylesoftware.htmlunit.html.*"%>
<%@page import="edu.stanford.prpl.app.common.resource.type.Person"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@page import="edu.stanford.prpl.insitu.data.PrPlPhotoBook"%>
<%@page import="edu.stanford.socialflows.settings.*"%>
<%@page import="edu.stanford.socialflows.contacts.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@page import="edu.stanford.socialflows.data.PrPlAlbumInfo"%>
<%@page import="edu.stanford.socialflows.db.DBConnectionManager"%>
<%@page import="edu.stanford.socialflows.sticker.*"%>
<%@page import="edu.stanford.socialflows.sticker.StickerIcons.StickerIcon"%>
<%@include file="sessionGetButler.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	// Get the user's unified list of Person resources (representing Friends/Contacts)
      SocialFlowsAddressBook myAddressBook 
      = (SocialFlowsAddressBook)session.getAttribute(FBAppSettings.MY_ADDRESS_BOOK);
      if (myAddressBook == null)
      {
    	  myAddressBook = new SocialFlowsAddressBook(dbconnManager);
          if (myAddressBook == null) {
              // ERROR: PRINT OUT THAT SESSION HAS EXPIRED
              System.out.println("Error: Trouble contacting your Personal Cloud Butler to obtain your unified social contacts list");
              out.println("Error: Trouble contacting your Personal Cloud Butler to obtain your unified social contacts list");
              return;      
          }
          
          session.setAttribute(FBAppSettings.MY_ADDRESS_BOOK, myAddressBook);
      }
      List<SocialFlowsContactInfo> myFriends = myAddressBook.getUnifiedFriendsList();
      HashMap<String, SocialFlowsContactInfo> myFriendsByEmail = myAddressBook.getEmailToFriendsMap();
      HashMap<String, SocialFlowsContactInfo> myFriendsBySfContactId = myAddressBook.getSfContactIdToFriendsMap();

      // Sort contacts/friends/persons by social weights first, then by alphabetical order
      Collections.sort(myFriends);
      
      // Get our unified collection of photo albums
      PrPlPhotoBook myPhotoBook = (PrPlPhotoBook)session.getAttribute(FBAppSettings.MY_PHOTO_BOOK);
      List<PrPlAlbumInfo> myPhotoAlbums = null;
      HashMap<String, PrPlAlbumInfo> myPhotoAlbumsByURI = null;
      if (myPhotoBook == null)
      {
          myPhotoBook = new PrPlPhotoBook(prplService_);
          
          // So that we can get continuous status updates about retrieving the unified collection of photo albums
          //session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, myPhotoBook);
          myPhotoBook.buildPrPlPhotoBook(prplService_);
          myPhotoAlbums      = myPhotoBook.getPhotoAlbumsList();
          myPhotoAlbumsByURI = myPhotoBook.getURIToPhotoAlbumMap();
          if (myPhotoAlbums == null || myPhotoAlbumsByURI == null) {
              System.out.println("Error: Trouble contacting your Personal Cloud Butler to obtain your unified collection of photo albums");
              out.println("Error: Trouble contacting your Personal Cloud Butler to obtain your unified collection of photo albums");
              return;      
          }
          session.setAttribute(FBAppSettings.MY_PHOTO_BOOK, myPhotoBook);
      }
      // Sort photo albums by how recent they were last modified   
      myPhotoAlbums = myPhotoBook.getPhotoAlbumsList();
      myPhotoAlbumsByURI = myPhotoBook.getURIToPhotoAlbumMap();
      Collections.sort(myPhotoAlbums);
      
      
      // Get existing, saved stickers/social groups from PrPl
      SocialFlowsStickerBook myStickerBook 
      = (SocialFlowsStickerBook)session.getAttribute(FBAppSettings.MY_STICKER_BOOK);
      if (myStickerBook == null)
      {
         myStickerBook = new SocialFlowsStickerBook(dbconnManager);
         if (myStickerBook == null) {
            // ERROR: PRINT OUT THAT SESSION HAS EXPIRED
            System.out.println("Error: Trouble contacting your Personal Cloud Butler to obtain your existing Stickers");
            out.println("Error: Trouble contacting your Personal Cloud Butler to obtain your existing Stickers");
            return;      
         }

         myStickerBook.buildStickerBook(myAddressBook);
         session.setAttribute(FBAppSettings.MY_STICKER_BOOK, myStickerBook);
      }
      
      
      // Get InSitu suggested groups if available
      List<Sticker> suggestedStickers
      = myStickerBook.getSuggestedStickers();
      
      // Get saved stickers
      List<Sticker> savedStickers = myStickerBook.getSavedStickers(myAddressBook);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xml:lang="en" xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Cliques & Stickers</title>
    <script src="common/json2min.js" type="text/javascript"></script>
    <script type="text/javascript" src="common/jscommon.js"></script>
    <link href="sticker_container_files/main.css" media="screen" rel="stylesheet" type="text/css">
    <link href="sticker_container_files/demo.css" media="screen" rel="stylesheet" type="text/css">
    <script type="text/javascript" src="jquery/jquery-1.5.1.min.js"></script>
    <script charset="utf-8" type="text/javascript" src="sticker_container_files/jquery.jeditable.js"/>

    <!-- FOR GRID OF FRIENDS -->
    <script type="text/javascript" src="sticker_container_files/FBfriends_grid_files/51f39esz.js"></script>
    <link type="text/css" rel="stylesheet" href="sticker_container_files/FBfriends_grid_files/77icnoa9.css">    
    <link type="text/css" rel="stylesheet" href="sticker_container_files/FBfriends_grid_files/9lgnun37.css">
    <link type="text/css" rel="stylesheet" href="sticker_container_files/FBfriends_grid_files/e2s8io1r.css">
    <link type="text/css" rel="stylesheet" href="sticker_container_files/FBfriends_grid_files/9bhp7a11.css">
    <link type="text/css" rel="stylesheet" href="sticker_container_files/FBfriends_grid_files/6gtrra65.css">
    <link type="text/css" rel="stylesheet" href="sticker_container_files/FBfriends_grid_files/5c5rygii.css">

    <link type="text/css" rel="stylesheet" media="screen" href="facebox/facebox.css"/>
    <link type="text/css" rel="stylesheet" media="screen" href="facebox/faceUI.css"/>
    <link type="text/css" rel="stylesheet" media="screen" href="common/collection.css"/>
    <link type="text/css" rel="stylesheet" href="stickers/stickers.css"/>

  <!--[if lt IE 7]>
  <script defer type="text/javascript" src="/javascripts/pngfix.js"></script>
  <![endif]-->
</head>
<body>
<div id="container">
<div id="content" class="clearfix">
<div id="main" style="width: 650px;">
<br/>
<div class="people-group">
<%
      List<Sticker> stickerList = null;

      int SUGGESTED_STICKERS = 1, CURRENT_STICKERS = 2;
      for (int stickerGroup = SUGGESTED_STICKERS; stickerGroup <= CURRENT_STICKERS; stickerGroup++)
      {
    	  String deleteStickerDisplay = "Delete Sticker";
    	  
          if (stickerGroup == SUGGESTED_STICKERS) {
        	  stickerList = suggestedStickers;
        	  deleteStickerDisplay = "Delete this suggested Sticker";
%>
  <h2 style="height: 18px;"><div style="float: left;">Suggested Groups</div><div style="float: right;"><a class="create_group"><img src="sticker_container_files/plus.png" title="Create a Sticker"/></a></div></h2>
  <ul class="people stickers-list suggested-groups" id="family-list">
<%
          }
          else if (stickerGroup == CURRENT_STICKERS) {
        	  stickerList = savedStickers;
%>
  <h2>Current Groups</h2>
  <ul class="people stickers-list current-groups" id="family-list">
<%
          }

          // Generate Sticker Display
          if (stickerList != null)
          {
        	  Iterator<Sticker> stickerIter = stickerList.iterator();
              while (stickerIter.hasNext())
              {
            	   Sticker sticker = stickerIter.next();
            	   String stickerID = sticker.stickerID;
                   SocialFlowsGroup clique = sticker.getCliqueLazily(myAddressBook);
                   
                   String stickerURI = sticker.getStickerURI();
                   if (stickerURI == null || stickerURI.trim().length() <= 0)
                	   stickerURI = "null";
                   
                   String stickerIconURI = sticker.getStickerIconURI();
                   String stickerIconURL = stickerIconURI;
                   if (stickerIconURI == null || stickerIconURI.trim().length() <= 0) {
                	   stickerIconURI = "null";
                	   stickerIconURL = "stickers_icons/sticker_new.png";
                   }
                   
                   String stickerName = sticker.getStickerName();
                   if (stickerName == null || stickerName.trim().length() <= 0)
                	   stickerName = "Click to name this suggested group";

%>
<li id="<%=stickerID%>" class="person-box you sticker">
  <div class="stickerMetadata" style="display: none;">
    <input type="hidden" id="stickerResourceURI" name="stickerResourceURI" value="<%=stickerURI%>" />
    <input type="hidden" id="stickerIconURI" name="stickerIconURI" value="<%=stickerIconURI%>" />
    <span class="merged-stickers-metadata">
    </span>
  </div>
  <div class="avatar" style="width: 60px; height: 60px;"><a class="sticker_link"><img src="<%=stickerIconURL%>" title="Sticker Icon" class="sticker_icon" style="display: block; margin-left: auto; margin-right: auto;"></a></div>
  <ul class="person-tabs">
    <li style="padding: 5px 1px;" class="on sticker-options">
    <input type="button" class="inputsubmit merge_group" name="merge-stickers" value="Merge Stickers" style="vertical-align: middle; margin-left: 5px; padding-left: 5px; padding-right: 5px;"/>
    <input type="checkbox" style="vertical-align: middle; margin-left: 1px; margin-right: 5px;" value="<%=stickerID%>" class="sticker-selected" />
    </li>
    <li style="visibility: hidden;"><a href="#">Tab 2</a></li>
  </ul>
  <h3 id="sticker_title" class="heavy editableTitle click"><%=stickerName%></h3>
  <div class="person-wrap">

    <div class="demo_delete_person_form delete_person_icon"><div style="margin: 0pt; padding: 0pt;"><input type="hidden" value="delete" name="_method"/></div>
        <input type="image" class="delete_group" title="<%=deleteStickerDisplay%>" src="sticker_container_files/cross.gif" style="float: right; vertical-align: top; margin-left: 0.5em;"/>
        <input type="image" class="save_group" title="Save Sticker" src="sticker_container_files/disk.gif" style="float: right; vertical-align: top; margin-left: 0.5em;"/>
    </div>

    <div class="person-info person-panel">
    <h3 style="left: 0px;"><img title="Clique" src="sticker_container_files/people.png"> Clique <a class="add_email clique_link"><img title="Add Another Clique Member" src="sticker_container_files/plus.png"></a></h3>
    
    <div id="clique_container" class="full" style="width: 568px; margin-top:15px;">
    <div class="content clearfix">
    <div class="clearfix">
    <div class="friend_grid clearfix">
    <ul id="clique">

               <%
               	int numCliqueMembers = clique.getGroupMembers().size();
                                  //System.out.println("Displaying a group with "+numCliqueMembers+" members ");
                                  	       
                                  clique.sortGroupMembers();
                                  Iterator<SocialFlowsContactInfo> groupMembers = clique.getGroupMembers().iterator();
                                  while (groupMembers.hasNext())
                                  {
                                        SocialFlowsContactInfo member = groupMembers.next();
                                        String friendName = member.getName();
                                        String displayName = friendName;
                                        // Special processing for email-based names
                                        if (InSituUtils.isEmailAddress(friendName))
                                           displayName = friendName.replace("@", "<br/>@");
                                                   
                                        String profileImgUrl = member.getPicSquare();
                                        if (profileImgUrl == null || profileImgUrl.trim().length() <= 0 
                                            || profileImgUrl.trim().equals("null"))
                                       	   profileImgUrl = "http://static.ak.fbcdn.net/pics/q_silhouette.gif";
                                        String friendUserId  = member.getTempContactId();
                                        if (friendUserId == null || friendUserId.trim().length() <= 0 
                                            || friendUserId.trim().equals("null")) {
                                        	friendUserId = String.valueOf(member.getContactId());
                                        	if (member.getContactId() <= 0)
                                       	       friendUserId = member.getEmailAddresses().iterator().next();
                                        }
                                        String friendProfileUrl = member.getProfileUrl();
                                        if (friendProfileUrl == null)
                                           friendProfileUrl = "#";
                                                   
                                        // System.out.println("Printing out group member "+friendName+" with profilePicUrl="+profileImgUrl);
                                                          
                                        // Print out clique member
               %>
    <li class="friend_grid_col clearfix clique_member" style="height:50px;">
    <div class="UIImageBlock clearfix">
        <a target="_blank" href="<%=friendProfileUrl%>" class="UIImageBlock_Image UIImageBlock_SMALL_Image">
            <img title="<%=friendName%>" class="UIProfileImage UIProfileImage_LARGE" src="<%=profileImgUrl%>" style="width: 50px; height: 50px;">
        </a>
        <div class="metadata" style="display: none;">
        <span id="<%=friendUserId%>" class="<%=friendUserId%>">
            <input type="hidden" class="resourceURI" name="resourceURI" value="null" />
            <input type="hidden" class="socialWeight" name="socialWeight" value="<%=member.getSocialWeight()%>" />   <%
   	// New InSitu contact not yet saved into PrPl
                            // Needs: *Fullname, *Firstname, *Lastname, *Social Weight, *Aliases, *Emails 
                            if (member.getContactId() <= 0)
                            {
   %>
            <input type="hidden" class="fullName" name="fullName" value="<%=friendName%>" />
            <input type="hidden" class="firstName" name="firstName" value="<%=member.getFirstName()%>" />
            <input type="hidden" class="lastName" name="lastName" value="<%=member.getLastName()%>" />               <%
               	// Record the aliases
                                           Set<String> names = member.getNames();
                                           if (names != null) {
                                              Iterator<String> namesIter = names.iterator();
                                              while (namesIter.hasNext()) {
                                                    String name = namesIter.next();
               %>
            <input type="hidden" class="alias" name="alias" value="<%=name%>" />             <%
             	}
                                         }
                                         
                                         // Record the emails
                                         Set<String> emails = member.getEmailAddresses();
                                         if (emails != null) {
                                             Iterator<String> emailsIter = emails.iterator();
                                             while (emailsIter.hasNext()) {
                                                 String email = emailsIter.next();
             %>  
            <input type="hidden" class="emails" name="emails" value="<%=email%>" />         <%
         	}
                                     }

                                  } // End of information block about new InSitu contact
         %>
        </span>
        </div>
        <a class="fg_action_hide UIImageBlock_Ext remove_clique_member" title="Delete">X</a>
        <div class="fg_links UIImageBlock_Content UIImageBlock_SMALL_Content">
            <div class="fg_name">
            <a target="_blank" href="<%=friendProfileUrl%>"><%=displayName%></a>
            </div>
            <div class="fg_action_add"></div>
        </div>
    </div>
    </li>

             <%
             	} // closing of while-loop for clique group members
             %>
    </ul>
    </div></div></div></div>
    <br/>

    <h3 style="left: 0px;"><img title="Collection" src="sticker_container_files/photos.png"> Collection <a class="add_email collection_link"><img title="Add Another Collection Item" src="sticker_container_files/plus.png"></a></h3>

    <div id="collection_container" class="full collection" style="width: 568px; margin-top:15px;">
    <table cellspacing="0" cellpadding="0" border="0" style="width: auto;">
    <tbody>
        <tr style="vertical-align: top;">
            <td class="main_content">
            <div id="main_album_content">
            <div id="collection_container" class="content_wrapper">
            <ul id="collection">
            <%
                               // Get collection of items
                               Set<String> collectionItemsURIs = sticker.getCollection();
                               List<PrPlAlbumInfo> collection = new Vector<PrPlAlbumInfo>();
                               
                               Iterator<String> itemsURI_iter = collectionItemsURIs.iterator();
                               while (itemsURI_iter.hasNext())
                               {
                            	   PrPlAlbumInfo photoAlbum = myPhotoAlbumsByURI.get(itemsURI_iter.next());
                            	   if (photoAlbum != null)
                            	      collection.add(photoAlbum);              	   
                               }
                               
                               Collections.sort(collection);
                               
                               // Display photo albums here
                               Iterator<PrPlAlbumInfo> collectionIter = collection.iterator();
                               while (collectionIter.hasNext())
                               {
                                   PrPlAlbumInfo photoAlbum = collectionIter.next();
                                   
                                   String albumId = "fbAid_"+photoAlbum.getAid();
                                   String albumLink = photoAlbum.getLink();
                                   String albumName = photoAlbum.getName();
                                   String albumCaption = photoAlbum.getSize()+((photoAlbum.getSize() > 1)? " photos" : " photo");
                                   String albumResourceURI = photoAlbum.getResourceURI();
                                   
                                   // find album image cover
                                   String albumCoverImgUrl = photoAlbum.getAlbumCover();
                                   if (albumCoverImgUrl == null || albumCoverImgUrl.equals("null"))
                                       albumCoverImgUrl = "";
            %>
            <li class="album_cell collection_item">
                <div class="metadata" style="display: none;">
                    <span id="<%=albumResourceURI%>" class="<%=albumResourceURI%>">
                        <input type="hidden" class="resourceURI" name="resourceURI" value="<%=albumResourceURI%>" />
                    </span>
                </div>
                <div class="album_thumb">
                    <a href="<%=albumLink%>" target="_blank" class="UIPhotoGrid_PhotoLink clearfix"><img src="<%=albumCoverImgUrl%>" class="UIPhotoGrid_Image"/></a>
                </div>
                <div class="album_title"><a target="_blank" href="<%=albumLink%>"><%=albumName%></a></div>
                <div class="photo_count"><div class="item_caption"><%=albumCaption%></div><a class="fg_action_hide UIImageBlock_Ext remove_collection_item" title="Delete">X</a></div>
            </li>
                       <%
                       	} // closing of while-loop for collection items
                       %>
            </ul>
            </div>
            </div>
            </td>
        </tr>
    </tbody>
    </table> 
    </div>
    
    <br/>
    </div><!-- Closer of "person-info person-panel" -->
    
  </div><!-- Closer of "person-wrap" -->
</li>   	  
    	  <%
   	      	  	} // closing of stickers loop
   	      	  %>
   	      <div class="noGroupsMsg" style="margin-left: 50px; display: none;"><h4 class="heavy">None available.</h4></div>
   	      </ul>
          <br/><br/><br/>
          <%
          	}
                    else
                    {
          %>
          <div class="noGroupsMsg" style="margin-left: 50px;"><h4 class="heavy">None available.</h4></div>
          </ul>
    	  <br/><br/><br/>
    	  <%
    	  	}
    	  %>


    <%
    	} // End of for-loop for sticker categories/lists
    %>
</div>
<br/>
</div><!-- Main div  -->
</div><!-- Content div  -->
</div><!-- Container div  -->


  <!-- Selection to add more Clique members -->
  <div id="cliqueSelection" class="faceUI" style="visibility:hidden;">
    <div class="popup">
    <table>
        <tbody>
        <tr>
            <td class="tl"></td>
            <td class="b"></td>
            <td class="tr"></td>
        </tr>
        <tr>
            <td class="b"></td>
            <td class="body">
            <h2 class="dialog_title">
                <span>Add Clique Members</span>
            </h2>
    <div class="content" style="overflow-y: auto; overflow-x: hidden; height: 188px;">
    <div id="friend_guesser" class="full" style="width: 460px;">
    <div class="content clearfix">
    <div class="clearfix">
    <div class="friend_grid clearfix">
    <!-- Start of grid -->
               <%
               	// Create the Clique selection dialog box
                                             int numCombinedFriends = myFriends.size();
                                             //System.out.println("Displaying "+numFBFriends+" fb friends ");
                                             
                                             int count = 0;
                                             Iterator<SocialFlowsContactInfo> j = myFriends.iterator();
                                             while (j.hasNext())
                                             {
                                          	     SocialFlowsContactInfo cFriend = j.next();
                                          	     String friendName = cFriend.getName();
                                          	     String displayName = friendName;
                                                // Special processing for email-based names
                                                if (InSituUtils.isEmailAddress(friendName))
                                                   displayName = friendName.replace("@", "<br/>@");
                                          	     
                                          	     String friendProfileUrl = cFriend.getProfileUrl();
                                          	     if (friendProfileUrl == null)
                                          	    	 friendProfileUrl = "#";
                                          	     String friendUserId = cFriend.getTempContactId();
                                          	     if (friendUserId == null || friendUserId.trim().length() <= 0 
                                          	         || friendUserId.trim().equals("null")) {
                                                     friendUserId = String.valueOf(cFriend.getContactId());
                                                     if (cFriend.getContactId() <= 0)
                                                        friendUserId = cFriend.getEmailAddresses().iterator().next();
                                                 }
                                          	     String profileImgUrl = cFriend.getPicSquare();
                                          	     if (profileImgUrl == null || profileImgUrl.trim().length() <= 0 || profileImgUrl.trim().equals("null"))
                                          	    	 profileImgUrl = "http://static.ak.fbcdn.net/pics/q_silhouette.gif";
                                                   // System.out.println("Printing out friend "+friendName+" with profilePicUrl="+profileImgUrl);

                                                   // Start of a row
                                                   if (count % 3 == 0) {
                                                      if (count+3 >= numCombinedFriends) {
               %>
    <div class="friend_grid_row last clearfix" style="margin: 10px -10px 5px;">
                           <%
                           	}
                                                   else {
                           %>
    <div class="friend_grid_row clearfix" style="margin: 10px -10px 5px;">
                           <%
                           	}
                                                }
                                         
                                                // Print out picture of friend
                           %>
    <div class="friend_grid_col clearfix fbFriend" style="width: 138px; height: 50px;">
    <div class="UIImageBlock clearfix">
        <a href="<%=friendProfileUrl%>" target="_blank" class="UIImageBlock_Image UIImageBlock_SMALL_Image fbProfileUrl">
            <img title="<%=friendName%>" class="UIProfileImage UIProfileImage_LARGE fbProfileImage" src="<%=profileImgUrl%>" style="width: 50px; height: 50px;" />
        </a>
        <input type="checkbox" value="<%=friendUserId%>" class="fg_action_hide UIImageBlock_Ext selectFriend"/>
        <div class="metadata" style="display: none;">
        <span id="<%=friendUserId%>" class="<%=friendUserId%>">
            <input type="hidden" class="resourceURI" name="resourceURI" value="null" />
            <input type="hidden" class="socialWeight" name="socialWeight" value="<%=cFriend.getSocialWeight()%>" />  <%
  	// New InSitu contact not yet saved into PrPl
             // Needs: *Fullname, *Firstname, *Lastname, *Social Weight, *Aliases, *Emails 
             if (cFriend.getContactId() <= 0)
             {
  %>  
            <input type="hidden" class="fullName" name="fullName" value="<%=cFriend.getName()%>" />
            <input type="hidden" class="firstName" name="firstName" value="<%=cFriend.getFirstName()%>" />
            <input type="hidden" class="lastName" name="lastName" value="<%=cFriend.getLastName()%>" />  <%
  	// Record the aliases
          	   Set<String> names = cFriend.getNames();
          	   if (names != null) {
          		   Iterator<String> namesIter = names.iterator();
          		   while (namesIter.hasNext()) {
          			   String name = namesIter.next();
  %>  
            <input type="hidden" class="alias" name="alias" value="<%=name%>" />      <%
      	}
              	   }
              	   
              	   // Record the emails
                     Set<String> emails = cFriend.getEmailAddresses();
                     if (emails != null) {
                         Iterator<String> emailsIter = emails.iterator();
                         while (emailsIter.hasNext()) {
                             String email = emailsIter.next();
      %>  
            <input type="hidden" class="emails" name="emails" value="<%=email%>" />  <%
  	}
                 }
          	   
             }
  %>
        </span>
        </div>
        <div class="fg_links UIImageBlock_Content UIImageBlock_SMALL_Content">
            <div class="fg_name">
            <a href="<%=friendProfileUrl%>" target="_blank" class="fbProfileName"><%=displayName%></a>
            </div>
        </div>
    </div>
    </div>

                     <%
                     	// End of a row
                                          if (!j.hasNext() || (count % 3 == 2)) {
                     %>
    </div>
                        <%
                        	}
                                             count++;
                                       }
                        %>
    </div></div></div></div>
                </div>
                <div class="info"></div>
                <div class="footer">
                    <input type="button" class="inputsubmit addToClique" name="save" value="Add" style="float: left; vertical-align: top;"/>
                    <a href="#" class="close"><img src="facebox/closelabel.gif" title="close" class="close_image" /></a>          
                </div>            
            </td>            
            <td class="b"></td>           
        </tr>          
        <tr>            
            <td class="bl"></td><td class="b"></td><td class="br"></td>           
        </tr>         
        </tbody>      
    </table>     
    </div>   
 </div>


  <!-- Selection to choose Sticker icon -->
  <div id="stickerSelection" class="faceUI" style="visibility:hidden;">
    <div class="popup">
    <table>
        <tbody>
        <tr>
            <td class="tl"></td>
            <td class="b"></td>
            <td class="tr"></td>
        </tr>
        <tr>
            <td class="b"></td>
            <td class="body">
            <h2 class="dialog_title">
                <span>Select Sticker Icon</span>
            </h2>
    <div class="content" style="overflow-y: auto; overflow-x: hidden; height: 195px;">
    <div id="friend_guesser" class="full" style="width: 420px;">
    <div class="content clearfix">
    <div class="clearfix">
    <div class="friend_grid clearfix">
    
    <!-- Start of grid -->
<%
	//Create the Sticker Icon selection dialog box
    Iterator<StickerIcon> stickerCol = StickerIcons.getStickerCollection();
    int numStickers = StickerIcons.getStickerCollectionSize();
    count = 0;

    while (stickerCol.hasNext())
    {
        StickerIcon stickerIcon = stickerCol.next();
        String iconURL = stickerIcon.getIconURL();
        String iconName = stickerIcon.getCaption();
        
        // Start of a row
        if (count % 5 == 0) {
            if (count+5 >= numStickers) {
%>
    <!-- Start of row -->
    <div class="friend_grid_row clearfix" style="margin-top: 5px;">
                <%
            }
            else {
                %>
    <!-- Start of row -->
    <div class="friend_grid_row last clearfix" style="margin-top: 5px;">
                <%
            }
        }

        // Print out sticker icon
        %>
    <div class="friend_grid_col clearfix" id="" style="width: 68px;">
    <div class="UIImageBlock clearfix">
        <a class="UIImageBlock_Image UIImageBlock_SMALL_Image iconlink" >
            <img title="<%=iconName%>" class="UIProfileImage UIProfileImage_LARGE square icon" src="<%=iconURL%>" style="height: 50px; width: 50px;">
        </a>
    </div>
    </div>

        <%      

        // End of a row
        if (!stickerCol.hasNext() || (count % 5 == 4)) {
            %>
    </div>
            <%
        }
        count++;
    }

%>
    </div></div></div></div>
                </div>
                <div class="info"></div>
                <div class="footer">
                    <a href="#" class="close"><img src="facebox/closelabel.gif" title="close" class="close_image"></a>          
                </div>
            </td>            
            <td class="b"></td>           
        </tr>          
        <tr>            
            <td class="bl"></td><td class="b"></td><td class="br"></td>           
        </tr>
        </tbody>
    </table>
    </div>
  </div>


  <!-- Selection to add more Collection items -->
  <div id="collectionSelection" class="faceUI" style="visibility:hidden;">
    <div class="popup">
    <table>
        <tbody>
        <tr>
            <td class="tl"></td>
            <td class="b"></td>
            <td class="tr"></td>
        </tr>
        <tr>
            <td class="b"></td>
            <td class="body">
            <h2 class="dialog_title">
                <span>Add Collection Items</span>
            </h2>
    <div class="content" style="overflow-y: auto; overflow-x: hidden; height: 212px;">
    <div id="friend_guesser" class="full" style="width: 500px;">
    <div class="content clearfix">
    <div class="clearfix">
    <div id="colItemSelector" class="collection friend_grid clearfix">
    <!-- Start of grid -->
    <%
          if (myPhotoAlbums != null) 
          {
               // Create the Collection selection dialog box
               int numPhotoAlbums = myPhotoAlbums.size();
               //System.out.println("Displaying "+numPhotoAlbums+" photo albums ");
               
               count = 0;
               Iterator<PrPlAlbumInfo> k = myPhotoAlbums.iterator();
               while (k.hasNext())
               {
                     PrPlAlbumInfo fbAlbum = (PrPlAlbumInfo)k.next();
                     String albumId = "fbAid_"+fbAlbum.getAid();
                     String albumLink = fbAlbum.getLink();
                     String albumName = fbAlbum.getName();
                     String albumCaption = fbAlbum.getSize()+((fbAlbum.getSize() > 1)? " photos" : " photo");
                     String albumResourceURI = fbAlbum.getResourceURI();
                     
                     // find album image cover
                     String albumCoverImgUrl = fbAlbum.getAlbumCover();
                     if (albumCoverImgUrl == null || albumCoverImgUrl.equals("null"))
                         albumCoverImgUrl = "";

                     // System.out.println("Printing out photo album "+albumName+" with albumCoverUrl="+albumCoverImgUrl);

                     // Start of a row
                     if (count % 3 == 0) {
                           %>
    <div class="friend_grid_row clearfix" style="margin: 0px;">
                           <%
                     }

                     // Print out Photo Album
                     %>
    <div class="friend_grid_col clearfix colItem" style="width: 150px;">
    <div class="UIImageBlock clearfix">
        <div class="album_cell" style="padding: 12px 0px 0px;">
            <div class="metadata" style="display: none;">
                <span id="<%=albumResourceURI%>" class="<%=albumResourceURI%>">
                    <input type="hidden" class="resourceURI" name="resourceURI" value="<%=albumResourceURI%>" />
                </span>
            </div>
            <div class="album_thumb">
            <a href="<%=albumLink%>" target="_blank" class="UIPhotoGrid_PhotoLink clearfix"><img src="<%=albumCoverImgUrl%>" class="UIPhotoGrid_Image colItemImg"/></a>
            </div>
            <div class="album_title"><a href="<%=albumLink%>" target="_blank" class="colItemUrl"><%=albumName%></a></div>
            <div class="photo_count"><div class="colItemCaption item_caption"><%=albumCaption%></div><input type="checkbox" value="<%=albumResourceURI%>" class="fg_action_hide UIImageBlock_Ext selectItem"/></div>
        </div>
    </div>
    </div>

                     <%
              
                     // End of a row
                     if (!k.hasNext() || (count % 3 == 2)) {
                        %>
    </div>
                        <%
                     }
                     count++;
               }
          }

               %>
    <!-- End of grid -->
    </div>  
    </div></div></div></div>
                <div class="info"></div>
                <div class="footer">
                    <input type="button" class="inputsubmit addToCollection" name="save" value="Add" style="float: left; vertical-align: top;"/>
                    <a href="#" class="close"><img src="facebox/closelabel.gif" title="close" class="close_image"></a>          
                </div>            
            </td>            
            <td class="b"></td>           
        </tr>          
        <tr>            
            <td class="bl"></td><td class="b"></td><td class="br"></td>           
        </tr>         
        </tbody>
    </table>     
    </div>   
  </div>

<script type="text/javascript" src="facebox/facebox.js"></script>
<script type="text/javascript" src="common/statusUpdate.js"></script>
<script type="text/javascript" src="stickers/stickers.js"></script>
</body>
</html>