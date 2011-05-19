<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="com.restfb.types.Photo"%>
<%@page import="com.restfb.exception.FacebookException"%>
<%@page import="com.restfb.LegacyFacebookClient"%>
<%@page import="com.restfb.DefaultLegacyFacebookClient"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@page import="edu.stanford.socialflows.data.*"%>
<%@page import="edu.stanford.socialflows.db.DBConnectionManager"%>
<%@include file="sessionCheck.jsp"%>
<%
	HashMap<String, HashSet<String>> fbAcct = null;
      long fbId = -1;
      if (fbMyInfo != null) {
    	  fbAcct = fbMyInfo.getAccounts();
    	  fbId = Long.parseLong(fbAcct.get(FBService.FACEBOOK_PROVIDER).iterator().next());
    	  System.out.println("\nYour name is '"+fbMyInfo.getName()+"' with FB id "+fbId);
      }
      
      String socialflowsURL 
      = response.encodeURL(FBAppSettings.DUNBAR_SOCIALFLOWS_POST_DESTINATION);
      String analyzeFBPhotosURL 
      = response.encodeURL("analyzeFBPhotos.jsp");
      String getStatusURL 
      = response.encodeURL("getStatus.jsp");
      
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xml:lang="en" xmlns="http://www.w3.org/1999/xhtml" lang="en"><head>
<meta http-equiv="Content-type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-language" content="en">
<META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
<META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">

<title><%=FBAppSettings.appNameDisplay%></title>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/faceUIBody.css"/>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/facebox.css"/>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/faceUI.css"/>

<script type="text/javascript" src="common/json2min.js"></script>
<script type="text/javascript" src="common/jscommon.js"></script>
<script type="text/javascript" src="common/statusUpdate.js"></script>
<script type="text/javascript" src="jquery/jquery-1.5.1.min.js"></script>
<script type="text/javascript" src="facebox/facebox.js"></script>
<script type="text/javascript" src="jquery/jquery.form.js"></script>
<script type="text/javascript">
var isDevMode = <%=FBAppSettings.ENABLE_DEV_MODE%>;

jQuery(document).ready(function($) {
    $('a[rel*=facebox]').facebox({
      loading_image : 'loading.gif',
      close_image   : 'closelabel.gif'
    });


    // Toggle advanced configuration options
    $('.toggleAdvancedOptions').click(function(e) {
        var currentToggle = $('#toggleAdvancedOptions-text');
        if (currentToggle.text() == 'Show Advanced Options') {
            // Change to 'Hide Advanced Options'
            currentToggle.text('Hide Advanced Options');
            $('#toggleAdvancedOptions-indicator').html('&#9660;'); //Shows Down-Arrow

            // Reveal advanced options
            $('#advancedOptions').slideDown("slow");
            //$('#advancedOptions').show("slow");
        }
        else {
            // Change to 'Show Advanced Options'
            currentToggle.text('Show Advanced Options');
            $('#toggleAdvancedOptions-indicator').html('&#9658;'); //Shows Right-Arrow

            // Reveal advanced options
            $('#advancedOptions').slideUp("slow");
            //$('#advancedOptions').hide("slow");
        }

        return false;
    });
    
    // upload clique file options
    var options = { 
            //target:        '#output2',   // target element(s) to be updated with server response 
            beforeSubmit:  getCliquesUpload,        // pre-submit callback 
            success:       getCliquesUploadResult,  // post-submit callback 
            iframe:        true,
            dataType:      'json'        // 'xml', 'script', or 'json' (expected server response type) 
     
            // $.ajax options can be used here too, for example: 
            //timeout:   3000 
    }; 

    // attach handler to form's submit event 
    $('#uploadCliques').submit(function() { 
        // submit the form 
        $(this).ajaxSubmit(options); 
        // return false to prevent normal browser submit and page navigation 
        return false; 
    });



    // Extract selected photo albums
    $("#extractAlbums").click(function() {        
        var albumSelection = $('#fbAlbumSelection');
        
        // Error with trying to retrieve FB data
        if (albumSelection.hasClass('error')) {
            jQuery.facebox('The following error occured while trying to retrieve information about your Facebook photo albums. '+
                	       'Please try reloading the application.<br/>'+albumSelection.text());
        	return false;
        }
        else if (albumSelection.hasClass('empty')) {
        	jQuery.facebox('You do not have any Facebook photo albums.');
            return false;
        }

        // Initialize dialog box with correct title and button text
        $('#fbAlbumSelectionTitle').text('Extract Photo Albums');
        $('#fbAlbumSelectionOKButton').val('Extract');
        
        albumSelection.find('input.selectItem:checked').each(
            function() {
              $(this).attr('checked', false);
         });
        albumSelection.find('.close').unbind('click').click(function() {
        	albumSelection.fadeOut();
            return false;
          });
        albumSelection.find('.extractSelectedAlbums').unbind('click').click(function() {
            // get selected FB photo album ids
            var dataToHarvest = new Object();
            var arrySelectedAlbums = new Array();
            var numAlbums = 0;
            albumSelection.find('input.selectItem:checked').each(function(){
            	var fbAid = $(this).val();
            	arrySelectedAlbums[arrySelectedAlbums.length] = fbAid;
            	numAlbums++;
            });
            dataToHarvest.selectedAlbums = arrySelectedAlbums;

            // Check whether any photo albums were selected
            if (numAlbums <= 0) {
                jQuery.facebox('Please select one or more of your Facebook photo albums to extract.');
                return false;
            }

            // STRINGIFY!
            var dataToHarvestJSON = JSON.stringify(dataToHarvest, null, 1);
            
            // show harvesting photo albums dialog
            var loadingMsg = 'Harvesting Your Facebook Photo Albums...';
            $.facebox.settings.loadingMessage = loadingMsg;
            
            // Using POST method to list of FB photo albums to harvest
            jQuery.facebox({ post: 'harvestFBPhotos.jsp', progress: '<%=getStatusURL%>',
                             postdata: dataToHarvestJSON, 
                             postcallback: function outputResult(resultText, inputdata){return resultText;} });
            albumSelection.fadeOut();
            return false;
          });
        
        albumSelection.css({
            top:    getPageScroll()[1] + (getPageHeight() / 10),
            left:   81
         });
        
        albumSelection.fadeIn('fast');
        return false;
    });


    // Deduce social groups from email messages
    $("#analyzeEmails").click(function() {
        invokeMuse();
        return false;
    });

    // Deduce social groups from FB photos
    $("#analyzePhotos").click(function() {
        if (!isDevMode) {
        	// show harvesting photo albums dialog
            var loadingMsg = 'Deducing Your Social Groups from Facebook Photos Tagged of You...';
            $.facebox.settings.loadingMessage = loadingMsg;

            var dataToHarvest = new Object();
            dataToHarvest.refresh = false; /* was originally 'true', changed for quicker response */
            var dataToHarvestJSON = JSON.stringify(dataToHarvest);

            // Using POST method to list of FB photo albums to harvest
            jQuery.facebox({ post: '<%=analyzeFBPhotosURL%>', progress: '<%=getStatusURL%>',
                             postdata: dataToHarvestJSON, 
                             postcallback: processAnalyzePhotosResult });
            return false;
        }
        
        var albumSelection = $('#fbAlbumSelection');

        // Initialize dialog box with correct title and button text
        $('#fbAlbumSelectionTitle').text('Analyze Social Groups from Photo Albums');
        $('#fbAlbumSelectionOKButton').val('Analyze');
        
        // Error with trying to retrieve FB data
        if (albumSelection.hasClass('error')) {
            jQuery.facebox('The following error occured while trying to retrieve information about your Facebook photo albums. '+
                           'Please try reloading the application.<br/>'+albumSelection.text());
            return false;
        }
        else if (albumSelection.hasClass('empty')) {
            // show harvesting photo albums dialog
            var loadingMsg = 'Deducing Your Social Groups from Facebook Photos Tagged of You...';
            $.facebox.settings.loadingMessage = loadingMsg;

            var dataToHarvest = new Object();
            dataToHarvest.refresh = true;
            var dataToHarvestJSON = JSON.stringify(dataToHarvest);

            // Using POST method to list of FB photo albums to harvest
            jQuery.facebox({ post: '<%=analyzeFBPhotosURL%>', progress: '<%=getStatusURL%>',
                             postdata: dataToHarvestJSON, 
                             postcallback: processAnalyzePhotosResult });
            return false;
        }
        
        albumSelection.find('input.selectItem').each(
            function() {
              $(this).attr('checked', true);
         });
        albumSelection.find('.close').unbind('click').click(function() {
            albumSelection.fadeOut();
            return false;
          });
        albumSelection.find('.extractSelectedAlbums').unbind('click').click(function() {
            // get selected FB photo album ids
            var dataToHarvest = new Object();
            var arrySelectedAlbums = new Array();
            var numAlbums = 0;
            albumSelection.find('input.selectItem:checked').each(function(){
                var fbAid = $(this).val();
                arrySelectedAlbums[arrySelectedAlbums.length] = fbAid;
                numAlbums++;
            });
            dataToHarvest.selectedAlbums = arrySelectedAlbums;
            dataToHarvest.refresh = true;

            // Check whether any photo albums were selected
            if (numAlbums <= 0) {
                jQuery.facebox('Please select one or more of your Facebook photo albums to analyze.');
                return false;
            }

            // STRINGIFY!
            var dataToHarvestJSON = JSON.stringify(dataToHarvest, null, 1);
            
            // show harvesting photo albums dialog
            var loadingMsg = 'Deducing Your Social Groups from Your Facebook Photos...';
            $.facebox.settings.loadingMessage = loadingMsg;

            // Using POST method to list of FB photo albums to harvest
            jQuery.facebox({ post: '<%=analyzeFBPhotosURL%>', progress: '<%=getStatusURL%>',
                             postdata: dataToHarvestJSON, 
                             postcallback: processAnalyzePhotosResult });
            
            albumSelection.fadeOut();
            return false;
          });
        
        albumSelection.css({
            top:    getPageScroll()[1] + (getPageHeight() / 10),
            left:   81
         });
        
        albumSelection.fadeIn('fast');
        return false;
    });

    
});





function processAnalyzePhotosResult(resultText, inputdata)
{
    if (resultText == null)
       return "An error has occured. No results were returned.";
    
    var resultData = JSON.parse(resultText);
    if (!resultData.success)
       return resultData.message;

    // Hide progress dialog
    $.facebox.close();

    // Need to refresh UI
    var refreshSocialFlow = parent.document.getElementById('refreshSocialFlow');
    var refreshStickers   = parent.document.getElementById('refreshStickers');
    refreshSocialFlow.value = "true";
    refreshStickers.value   = "true";
    
    // Redirect to SocialFlows page
    parent.changeInSituTabSelected("socialflow");
    parent.changeToWindow(3, '<%=socialflowsURL%>');
}


function harvestFBData(statusObjName)
{
	var loadingMsg = 'Extracting Your Facebook Social Graph...';
	$.facebox.settings.loadingMessage = loadingMsg;
	$.facebox({ ajax: 'harvestFBData.jsp', progress: '<%=getStatusURL%>' });
}

function getCliques(dataLocation)
{
	$.facebox.settings.loadingMessage = 'Analyzing Social Cliques from your email...';
	$.facebox({ ajax: 'analyzeCliques.jsp?dataLoc='+urlEncode(dataLocation), progress: '<%=getStatusURL%>' });

    var refreshSocialFlow = parent.document.getElementById('refreshSocialFlow');
    var refreshStickers   = parent.document.getElementById('refreshStickers');
    refreshSocialFlow.value = "true";
    refreshStickers.value   = "true";
}

function getCliquesUpload(formData, jqForm, options)
{
	//console.log('getCliquesUpload: formData= '+formData);
	
    $.facebox.settings.loadingMessage = 'Uploading your Social Cliques data for analysis...';
    $.facebox({ progress: '<%=getStatusURL%>' });
}

function getCliquesUploadResult(responseText, statusText, xhr, $form)
{
	//console.log('getCliquesUploadResult: responseText= '+responseText);
	//console.log('getCliquesUploadResult: statusText= '+statusText);
	
    $.facebox(responseText.message);

    var refreshSocialFlow = parent.document.getElementById('refreshSocialFlow');
    var refreshStickers   = parent.document.getElementById('refreshStickers');
    refreshSocialFlow.value = "true";
    refreshStickers.value   = "true";
}
</script>
</head>
<body>

<br/>
<%
	String usernameDisplay = fbMyInfo.getName(), 
           additionalMsg = "";
    if (FBAppSettings.ENABLE_DEV_MODE) {
	   additionalMsg = "(Facebook ID: "+fbId+")";
    }
    else if (FBAppSettings.ENABLE_AMT_MODE) {
	   usernameDisplay = "Mechanical Turk tester";
    }
    // http://static.ak.fbcdn.net/rsrc.php/zP/r/3lpM16jb1d_.png
%>
Welcome <b><%=usernameDisplay%></b>! <%=additionalMsg%>
            <%
            	if (!FBAppSettings.ENABLE_IUI_MODE && FBAppSettings.ENABLE_PHOTOS) {
            %>
<br/><br/>
<div>
Let's analyze tagged photos of you to reveal your closest circles of friends!
</div><br/>
            <ul class="UIComposer_Buttons">
                <li class="UIComposer_FormButtons">
                <span id="analyzePhotos" class="UIComposer_SubmitButton UIButton UIButton_Blue UIFormButton">
                    <div id="photos_icon"
                         style="height: 16px; width: 16px; display: inline; 
                                top: 3.5px; left: 6px; position: relative; 
                                background-image: url('common/photos.png'); 
                                background-repeat: no-repeat; display: inline-block;"></div>
                    &nbsp;<input type="button" class="UIButton_Text" style="display: inline;" value="Analyze Photos of Me"/>
                </span>
                </li>
            </ul>
            <%
            	}
                        
            if (FBAppSettings.ENABLE_EMAILS) {
            %>
<br/><br/>
<div>       <%
       	if (!FBAppSettings.ENABLE_PHOTOS) {
       %>
<b><%=FBAppSettings.appNameDisplay%></b> is an experimental version of SocialFlows that helps you identify your closest circles of friends from your email correspondences.
<br/><br/>     <%
     	}
     %>
Let's now analyze your emails!
</div><br/>
            <ul class="UIComposer_Buttons">
                <li class="UIComposer_FormButtons">
                <span id="analyzeEmails" class="UIComposer_SubmitButton UIButton UIButton_Blue UIFormButton">
                    <img style="display: inline; position: relative; vertical-align: bottom;
                                left: 6px; bottom: 1.5px; height: 20px; width: 22px;" 
                         src="export/letter.png">
                    &nbsp;<input type="button" class="UIButton_Text" style="display: inline;" value="Analyze My Emails"/>
                </span>
                </li>
            </ul>
            <%
                       	}
                       %>

<!-- Info messages -->
<%
	if (FBAppSettings.ENABLE_AMT_MODE)
    {
%>
<div style="text-align:justify;">
<br/><br/>
No personal information is collected in this experiment from your Facebook account.
The algorithm you are evaluating for this Mechanical Turk task will try to deduce your social groups 
by analyzing tagged Facebook photos from you and your friends, and the results of this analysis will be
presented for your evaluation, but no personal identifiable data of yours will be recorded.
<br/><br/>
We merely want your evaluation of the quality of the social groups suggested and deduced by
our algorithm. At the end, you will have the opportunity to share your groups with your friends
on Facebook as Facebook Groups, and a <b>Verification Token</b> will be generated for you to
submit to us to verify that this Mechanical Turk task was indeed done to completion.
<br/><br/>
Thank you for participating in this experimental research.
</div>
    <%
    	}
        
        String title = "Friend Lists & Groups";
        String firstSentence 
        = "<a target=\"_blank\" href=\"http://www.facebook.com/friends/edit/\"><b>Facebook Friend Lists</b></a> "+
          "and <a target=\"_blank\" href=\"http://www.facebook.com/home.php?sk=2361831622\"><b>Groups</b></a>";
        String lastSentence = "Friend Lists & Groups";
        if (!FBAppSettings.ENABLE_DEV_MODE) {
           title = "Friend Lists";
           firstSentence 
           = "<a target=\"_blank\" href=\"http://www.facebook.com/friends/edit/\"><b>Facebook Friend Lists</b></a>";
           lastSentence = "Facebook Friend Lists";
        }
    %>
<br/><br/><br/>

   <%
   if (FBAppSettings.ENABLE_EMAILS && !FBAppSettings.ENABLE_PHOTOS && !FBAppSettings.ENABLE_AMT_MODE) 
   { %>
<div style="padding-bottom: 25px; width: 680px;">
<div style="height: 16px; width: 16px; display: inline-block;
            top: 3.8px; left: 8px; position: relative;
            background-position: 0pt -103px;
            background-image: url(&quot;http://static.ak.fbcdn.net/rsrc.php/zP/r/3lpM16jb1d_.png&quot;); 
            background-repeat: no-repeat;"></div>
<span style="padding-left: 18px;">
<b>Have Tagged Photos on Facebook?</b>
</span>
<br/><br/>
<a target="_blank" href="http://apps.facebook.com/socialflowsnew"><b>SocialFlows</b></a> 
reveals your closest circles of friends by analyzing Facebook photos of you and your 
friends. Give it a try today!
</div>
<% } 
   else if (FBAppSettings.ENABLE_PHOTOS && !FBAppSettings.ENABLE_EMAILS && !FBAppSettings.ENABLE_AMT_MODE) 
   { %>
<div style="padding-bottom: 25px; width: 680px;">
<img style="height: 23px; width: 23px; top: 7px; left: 5px; position: relative;" src="export/letter.png">
<span style="padding-left: 12px;">
<b>Have Friends from Email?</b>
</span>
<br/><br/>
<a target="_blank" href="http://apps.facebook.com/socialflowsemail"><b>SocialFlows on Email</b></a> is 
an experimental version of SocialFlows that analyzes and reveals your closest circles of friends 
from your email correspondences. Give it a try today!
</div>
<% } %>

<div style="padding-bottom: 28px; width: 680px;">
<div style="height: 20px; width: 20px; display: inline-block;
            top: 6px; left: 8px; position: relative;
            background-position: 0pt -65px;
            background-image: url(&quot;http://static.ak.fbcdn.net/rsrc.php/zp/r/lPOQoTp3Dcs.png&quot;); 
            background-repeat: no-repeat;"></div>
<span style="padding-left: 13.8px;">
<b>SocialFlows Makes Your Facebook <%=title%> Better</b>
</span>
<br><br>
Creating and updating your <%=firstSentence%> can be a time-consuming and painful task.
Let us make this task easier by automatically figuring out your social groups, and 
editing/making them into new <%=lastSentence%> a snap.
</div>
<%

   if (!FBAppSettings.ENABLE_AMT_MODE)
   { 
      // Just for Fun: Explore Luke Skywalker's Social Topology
      // http://www.facebook.com/apps/application.php?id=149801675080418#!/photo.php?fbid=149807528413166&set=a.149807521746500.31785.149801675080418&theater
%>
<div style="width: 680px;">
<img style="height: 23px; width: 23px; top: 8px; left: 5px; position: relative;" src="egTopology/icons/tiefighter.gif">
<span style="padding-left: 12px;">
<b>Explore Luke Skywalker's Social Topology!</b>
</span>
<br/><br/>
Luke Skywalker, hero of the Rebel Alliance, is also a fan and user of SocialFlows. SocialFlows helps
him visualize and manage his all too-complicated social life and circles of friends. See who is who
in <a target="_blank" href="http://www.facebook.com/apps/application.php?id=149801675080418#!/photo.php?fbid=149807528413166&set=a.149807521746500.31785.149801675080418&theater">
<b>Luke Skywalker's SocialFlows</b></a>.
</div>
<% }



   if (FBAppSettings.ENABLE_DEV_MODE && FBAppSettings.ENABLE_ACCESS_CONTROL) { %>
            <br/>
            <ul class="UIComposer_Buttons">
                <li class="UIComposer_FormButtons">
                <span class="UIComposer_SubmitButton UIButton UIButton_Blue UIFormButton">
                    <input id="extractAlbums" type="button" class="UIButton_Text" value="Extract Your FB Photo Albums"/>
                </span>
                </li>
            </ul>
<% }
   
   if (FBAppSettings.ENABLE_DEV_MODE) { %>
            <br/><br/><br/>
            <div style="display: none;">
            <a href="#" id="toggleAdvancedOptions-indicator" class="toggleAdvancedOptions" style="text-decoration:none;">&#9658;</a>&nbsp;<a href="#" id="toggleAdvancedOptions-text" class="toggleAdvancedOptions">Show Advanced Options</a>
            <br/><br/><br/>
            <div id="advancedOptions" class="hidden">
            <ul class="UIComposer_Buttons">
                <li class="UIComposer_FormButtons">
                <span class="UIComposer_SubmitButton UIButton UIButton_Blue UIFormButton">
                    <input type="button" onclick='harvestFBData();' class="UIButton_Text" value="Extract Your FB Social Graph"/>
                </span>
                </li>
            </ul>
            <br/>
            <div style="display: block;">
            <div class="UIComposer_Attachment_TDTextArea" style="display: inline;">
                <input type="text" value="http://" name="data[url]" id="groupsDataLocation" title="http://" placeholder="http://" class="inputtext UIComposer_Attachment_Input DOMControl_placeholder" style="width: 338px;"/>
            </div>
            <div style="display: inline; margin-left: 5px;">
                <span class="UIComposer_SubmitButton UIButton UIButton_Blue UIFormButton">
                    <input type="button" onclick="getCliques(document.getElementById('groupsDataLocation').value);" class="UIButton_Text" value="Get Cliques From URL"/>
                </span>
            </div>
            </div>
            <div style="display: block; padding-top: 5px; padding-left: 150px;">
                <div style="display: inline;" class="UIComposer_Attachment_TDTextArea"><b>or</b></div>
            </div>
            <div style="display: block; margin-top: 3px; margin-bottom: 8px;">
            <form enctype="multipart/form-data" method="post" action="analyzeCliquesFile.jsp" id="uploadCliques">
                <input type="hidden" value="100000" name="MAX_FILE_SIZE"/>
                <div style="display: inline;">
                    <input name="file" id="file" type="file" class="inputtext UIComposer_Attachment_Input DOMControl_placeholder" size="38" />
                </div>
                <div style="display: inline; margin-left: 5px;">
                <span class="UIComposer_SubmitButton UIButton UIButton_Blue UIFormButton">
                        <input type="submit" onclick="" class="UIButton_Text" value="Upload Cliques Data"/>
                </span>
                </div>
            </form>
            </div>
            </div>
            </div>
<% } %>
            

            <div style="position: absolute; bottom: 18px; text-align:justify;">
            <a target="_blank" href="http://mobisocial.stanford.edu/socialflows"><b><%=FBAppSettings.appNameDisplay%></b></a> 
            is brought to you by the 
            <a target="_blank" href="http://mobisocial.stanford.edu">MobiSocial Computing Laboratory</a> 
            at Stanford University, 
            which is part of the Programmable Open Mobile Internet (POMI) 2020 Expedition funded by 
            the National Science Foundation.
            We hope to help users protect their privacy by making it easy to create social groups. 
            All information gathered in this study is only used for research purposes and will 
            not be shared with any third parties.
            </div>
<%
	// Initialize collection of user's FB photo albums
      // - Only get list of photo albums (could be a very long list) for dev mode only
      if (FBAppSettings.ENABLE_PHOTOS && FBAppSettings.ENABLE_DEV_MODE)
      {
    	  SocialFlowsPhotoBook photoBook
          = (SocialFlowsPhotoBook)session.getAttribute(FBAppSettings.MY_PHOTO_BOOK);
          
          try 
          {
              if (photoBook == null) {
                  photoBook = new SocialFlowsPhotoBook(dbconnManager);
                  // Initialize collection of FB photo albums
                  photoBook.getFBPhotoAlbums(fbs);
                  session.setAttribute(FBAppSettings.MY_PHOTO_BOOK, photoBook);
              }
%>
              <%@include file="fbAlbumSelection.jsp"%>
              <%
              	} 
                        catch(FacebookException ex) 
                        {
              %>
                <div id="fbAlbumSelection" class="error" style="display: none; z-index: 500;">
                <%
                	out.print("FBException: "+ex.getMessage());
                %> 
                </div>
              <%
              	System.out.println("FBException: "+ex.getMessage());
                            ex.printStackTrace();
                            
                            // Log exception to DB
                            Writer result = new StringWriter();
                            final PrintWriter printWriter = new PrintWriter(result);
                            ex.printStackTrace(printWriter);
                            String errorLog 
                            = request.getServletPath()+": "+
                              "A Facebook error occured while fetching the user's photo albums:\n"
                              +result.toString();
                            sfLogger.logError(errorLog);
                            printWriter.close(); result.close();
                        }
                        catch(Exception e) 
                        {
              %>
              <div id="fbAlbumSelection" class="error" style="display: none; z-index: 500;">
              <%
              	out.print("Exception: "+e.getMessage());
              %>
              </div>
              <%
              	System.out.println("Exception: "+e.getMessage());
                            e.printStackTrace();
                            
                            // Log exception to DB
                            Writer result = new StringWriter();
                            final PrintWriter printWriter = new PrintWriter(result);
                            e.printStackTrace(printWriter);
                            String errorLog 
                            = request.getServletPath()+": "+
                              "An error occured while fetching the user's photo albums.\n"
                              +result.toString();
                            sfLogger.logError(errorLog);
                            printWriter.close(); result.close();
                        }
                    }


                    // <!-- Muse Email Integration -->
                    // pageContext.include("museEmailIntegration.jsp");
                    if (FBAppSettings.ENABLE_EMAILS) {
              %>
    	  <%@include file="museEmailIntegration.jsp"%>
      <%
      }

%>


</body>
</html>