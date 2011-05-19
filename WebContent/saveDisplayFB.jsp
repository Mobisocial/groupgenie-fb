<%@page import="edu.stanford.socialflows.settings.*"%>
<%
    /*
    // Get the base URL for SocialFlows webapp
    String originalURL = request.getRequestURL().toString();
    originalURL = originalURL.substring(0, originalURL.lastIndexOf("/")+1);
    if (originalURL.charAt(originalURL.length()-1) != '/')
        originalURL += "/";
    */
    
    // Construct integration URL
    String integrationUrl = originalURL + "processFBAuth.jsp";
    String js_url = originalURL + "export/exportGroupsToFB.js";

%>
<% if (FBAppSettings.ENABLE_DEV_MODE) { %>
<div id="exportToFB_Bookmarklet" class="hidden">
<a id="exportToFB_Bookmark"
   href="javascript:
         var sfAppUrl       = '<%=FBAppSettings.canvasURL%>';
         var integrationUrl = '<%=integrationUrl%>';
         (function()
          {  
             mc_script=document.createElement('SCRIPT'); 
             mc_script.type='text/javascript'; 
             mc_script.src='<%=js_url%>?'; 
             document.getElementsByTagName('head')[0].appendChild(mc_script);
          })();">Save My Groups to Facebook</a>
</div>
<% } %>
<%
	// Check for Amazon Mechanical Turk
    String amt_token = "";
    if (FBAppSettings.ENABLE_AMT_MODE) {
       amt_token 
       = (String)session.getAttribute(FBAppSettings.AMT_VTOKEN);
    }
%>
<script language="javascript" type="text/javascript">
var isAMTMode = <%=FBAppSettings.ENABLE_AMT_MODE%>;
var amtVToken = "<%=amt_token%>";

jQuery(document).ready(function($) {

	$(".closeProgressMsg").live("click", function() {
	    $.facebox.close();
	    return false;
	});

	// Initiate complicated workflow to begin exporting SocialFlows groups to FB
	$('#beginFBExport').click(function() {
		startFBExport();
        return false;
	});

});

function startFBExport()
{
    <% if (FBAppSettings.ENABLE_DEV_MODE) { %>
	// check if fb credentials already obtained
    var fb_postformid = $('#<%=FBAppSettings.FB_POSTFORMID%>').val();
    if (fb_postformid == null || fb_postformid == 'null' || fb_postformid.trim().length <= 0)
        fb_postformid = 'null';

    if (fb_postformid == 'null') 
    {
        var exportToFB_bookmarklet = $('#exportToFB_Bookmarklet').html();
        
        // request user to create/click on bookmarklet to obtain FB credentials
        var loadingMsg = 'To save your social groups to Facebook:<br/>\n'+
                         '<ul style=\"list-style-type: square; padding-left: 20px;\">\n'+
                         '<li>Drag this link <b>'+$('#exportToFB_Bookmarklet').html()+'</b> to your browser\'s Bookmarks Toolbar</li>\n'+
                         '<li>From your browser\'s toolbar, click on the created link</li>\n'+
                         '<li>Your groups will then be saved to Facebook</li>\n'+
                         '</ul>\n<br/>\n'+
                         'Or click <a class="closeProgressMsg" href="#"><b>Cancel</b></a> to not proceed further.';
        $.facebox.settings.loadingMessage = loadingMsg;

        // Using POST method but posting no data to getFBAuth.jsp
        jQuery.facebox({ post: '<%=response.encodeURL("getFBAuth.jsp")%>', 
                         progress: '<%=response.encodeURL("getStatus.jsp")%>',
                         postdata: '{}', 
                         postcallback: checkUserFBAuthorization });
    }
    else
    {
    	determineGroupsToExport('Facebook');
    }
    <% } else { %>
    // Save as FB Friends Lists
    determineGroupsToExport('Facebook');
    return;
    <% } %>
}

function checkUserFBAuthorization(response, inputdata)
{
	var stdErrorResponse
	= "An error has occured. No Facebook authorization response was found. "+
      "Please try again to save your social groups to Facebook.";

    if (response == null)
        return stdErrorResponse;
    
    response = response.replace(/^\s+|\s+$/g, ''); // remember to strip whitespace, used to be a bug
    if (response.length > 0) {
        var resultData = JSON.parse(response);
        if (!resultData.success) {
           // hide progress message
           // Show error message
           return resultData.message;
        }
        else {
           // Set FB auth info
           $('#<%=FBAppSettings.FB_POSTFORMID%>').val(resultData.<%=FBAppSettings.FB_POSTFORMID%>);
           $('#<%=FBAppSettings.FB_DTSG%>').val(resultData.<%=FBAppSettings.FB_DTSG%>);
           
           // reveal "Save Groups to Facebook" dialog window
           determineGroupsToExport('Facebook');
        }
    }
    else
    	return stdErrorResponse;

    // Hide progress dialog
    //$.facebox.close();
}

var postGroupsToFBDelay = 300;
var batchLimit = 1;
function exportGroupsToFBResult(response, inputdata)
{
    if (response == null)
        return "An error has occured. No results were returned.";
     
     var resultData = JSON.parse(response);
     if (!resultData.success)
        return resultData.message;

     updateSavedGroups(resultData);     
     //var is_chrome = isChromeBrowser();


     // 1. show export update for each group
     // 2. create iframes, initiate separate saves for each group to be exported as FBFL
     // 3. once done, display success message
     
     var iframeHTML 
     = '<iframe name="FBEXPORT_WINDOW" id="FBEXPORT_IFRAME" src ="#" frameborder=0 '+
       '        scrolling="auto" style="width: 100%; height: 100%; border: medium none;">'+
       '  <p>Your browser does not support iframes.</p>'+
       '</iframe>';

     if (resultData.savedStickers != null && resultData.savedStickers.length > 0) 
     {
         var postGroupToFB
         = function(startIndex)
           {
               var count = 0, index = 0;

               <% if (FBAppSettings.ENABLE_INSIDIOUS_MODE) { %>
               // Auto-like
               var iframeAutoLikeObj = $(iframeHTML);
               iframeAutoLikeObj.attr('src' , '<%=response.encodeURL("publishLikeToFB_POST.jsp")%>');
               $('#fbexport-display').append(iframeAutoLikeObj);
               <% } %>

               for (index = startIndex; index < resultData.savedStickers.length; index++)
               {
            	   var sticker = resultData.savedStickers[index];
            	   <% if (FBAppSettings.ENABLE_DEV_MODE) { %>
            	   if (sticker.exportToFB)
            	   {
                	   if (sticker.saveAsFBGroup) {
                           // Update progress message
                           log('Saving '+sticker.name+' as a Facebook Group...');
                           $.facebox.setLoadingMessage(
                              'Saving your <b>'+sticker.name+'</b> group as a Facebook Group...');

                           var iframeObj = $(iframeHTML);
                           iframeObj.attr('name', 'FBEXPORT_WINDOW_fbg_'+sticker.stickerID);
                           iframeObj.attr('id'  , 'FBEXPORT_IFRAME_fbg_'+sticker.stickerID);
                           iframeObj.attr('src' , '<%=response.encodeURL("saveGroupsToFB_POST.jsp?")%>'
                        		                  +'&fbExportType=fbGroup'
                                                  +'&sfGroupId='+sticker.stickerSfID);
                           $('#fbexport-display').append(iframeObj);

                           count++;
                       }
                   }
            	   else
                	   continue; 

            	   // Batch the POST requests in delayed small batches
                   if ((count >= batchLimit) && (index+1 < resultData.savedStickers.length))
                   {
                       setTimeout(function(){postGroupToFB(index+1)}, 
                                  postGroupsToFBDelay);
                       return;
                   }
                   <% } %>
               }

               // Very last step, everything has been processed
               if (index == resultData.savedStickers.length)
               {
            	   setTimeout(function()
                    	      {
                                 if (isDevMode) {
                                	 $.facebox.close();
                                	 displayExportFBSuccessMsg(resultData.savedStickers.length);
                                 }
                                 else
                                 {
                                     if (isAMTMode)
                                     {
                                    	var msg
                                        = "Your social groups have been saved to Facebook! "+
                                          "Check them out as your <a target=\"_blank\" "+
                                          "href=\"http://www.facebook.com/friends/edit/\"><b>Facebook Friends Lists</b></a>!";
                                        msg += "<br><br>"+
                                               "Your Verification Token is <b>"+amtVToken+"</b>. <br>"+
                                               "Please submit this token to us on Amazon Mechanical Turk. "+
                                               "Thank you for your time, evaluation, and participation in our experimental research.";
                                	    $.facebox.reveal(msg);
                                     }
                                     else
                                     {
                                    	 $.facebox.close();
                                         displayExportFBSuccessMsg(resultData.savedStickers.length);
                                     }

                                     // show users their FB groups
                                     //window.open('http://www.facebook.com/home.php?sk=2361831622');

                                     // show users their FB friends lists
                                     window.open('http://www.facebook.com/friends/edit/');
                                 }
                	          }, 
                              3*postGroupsToFBDelay);
                  return;
               }
           };  // close of function


           // Clear previous iframes doing POST requests to FB
           $('#fbexport-display').empty();
           // Initiate the exporting of social groups as FB friendlists
           postGroupToFB(0);
     }

}

function displayExportFBSuccessMsg(numGroups)
{
	// create dialog box
	var dialogBox = $($('#dialogBox').html()); // $('#dialogBox').clone(false);
    dialogBox.find('#main_content_height').css({'max-height':'288px'});
    dialogBox.find('#main_content_container').css({'width':'550px'});
	
    var text = "";
    text = "<div style=\"margin-top: 8px; margin-left: 8px; margin-right: 8px;\">\n" +
           "<div style=\"font-weight: bold; font-size: 13px; margin-bottom: 5px;\">Share With People You Care.</div>\n" +
           "<ul style=\"list-style-type: square; padding-left: 20px;\">\n" +
           "<li>With your social groups now saved in Facebook as " +
           "    <a target=\"_blank\" href=\"http://www.facebook.com/friends/edit/\"><b>Friend Lists</b></a>," +
           "    you can now share what you post on Facebook with the right group(s) of people " +
           "    by customizing your privacy settings.</li>\n" +           
           "<br><img src=\"export/FBAC_Step1.png\" style=\"width:480px;\"><br><br>\n" +
           "<li>Next, in the displayed <b>Custom Privacy</b> dialog, select the <b>Specific People...</b> option </li>\n" +
           "<br><img src=\"export/FBAC_Step2.png\" style=\"width:480px;\"><br><br>\n" +
           "<li>Then, type the names of the specific social groups you wish to share with, and click on the matching Friend List </li>\n" +
           "<br><img src=\"export/FBAC_Step3.png\" style=\"width:480px;\"><br><br>\n" +
           "<li>Finally, after adding all the appropriate groups, click on the <b>Save Setting</b> button </li>\n" +
           "<br><img src=\"export/FBAC_Step4.png\" style=\"width:480px;\">\n" +
           "    <img src=\"export/FBAC_Step5.png\" style=\"width:480px;\">\n" +
           "</ul>\n" +
           "</div>";

    var dialogTitle 
    = "Groups Saved as Friend Lists!";
    if (isDevMode)
    	dialogTitle = "Groups Saved as Friend Lists & Facebook Groups!";
	dialogBox.find('#dialogBoxTitle').text(dialogTitle);
    dialogBox.find('#contentHolder').html(text);
    dialogBox.find('.close').unbind('click').click(function() {
    	dialogBox.fadeOut(function(){
        	$(this).remove();
        });
        return false;
      });

    var footer   = dialogBox.find('.footer');
    var flButton = footer.find('#dialogBoxConfirmButton').detach();
    flButton.attr('id', 'buttonSeeFBFL');
    flButton.val('See My Friend Lists!');
    flButton.unbind('click').click(function() {
        // open a window to reveal FB friend lists
        window.open('http://www.facebook.com/friends/edit/');
        return false;
      });

    // Add button to see FB Groups
    if (isDevMode) {
    	var fgButton = flButton.clone();
        fgButton.attr('id', 'buttonSeeFBG');
        fgButton.val('See My FB Groups!');
        fgButton.unbind('click').click(function() {
            // open a window to reveal FB groups
            window.open('http://www.facebook.com/home.php?sk=2361831622');
            return false;
          });
        fgButton.css({'margin-left':'5px'});
        fgButton.prependTo(footer).show();
    }

    flButton.prependTo(footer).show();

    dialogBox.css({
        top:     getPageScroll()[1] + (getPageHeight() / 10),
        left:    81
    });

    // Show confirmation dialog box
    dialogBox.appendTo('body').fadeIn('slow');
    //dialogBox.fadeIn();
}


function initExportGroupsToFBDialog(accountType, dialogBox, exportGroupsJSONInfo)
{
    var text = "";
    var groupsArray = new Array();
    var infoTextCssStyle = "margin-top: 8px; margin-left: 8px; margin-right: 8px;";

    if (exportGroupsJSONInfo.stickerArray == null ||
        Object.size(exportGroupsJSONInfo.stickerArray) == 0) 
    {
        text 
        = "No social groups can be saved to your "+accountType+" account due to the following issues:<br/>\n" +
          "<ul style=\"list-style-type: square; padding-left: 20px;\">\n";

        var noSocialGroups = true;
        if (exportGroupsJSONInfo.numStickersUnnamed > 0) {
            noSocialGroups = false;
            text += "<li>"+exportGroupsJSONInfo.numStickersUnnamed+" groups were unnamed</li>\n";
        }
        if (exportGroupsJSONInfo.stickerNames != null &&
            Object.size(exportGroupsJSONInfo.stickerNames) > 0) 
        {
            noSocialGroups = false;
            var numDups = 0;
            var dupGroupNames = "";
            var stickerNames = exportGroupsJSONInfo.stickerNames;
            for (var key in stickerNames) {
                if (numDups > 0)
                    dupGroupNames += ", ";
                var dupGroupName = cnvrt2Upper(key);
                numDups += stickerNames[key];
                dupGroupNames += "<b>"+dupGroupName+"</b> ("+stickerNames[key]+" groups)";
                
            }
            
            text += "<li>"+numDups+" groups had duplicate group names: " +
                    dupGroupNames + "</li>\n";
        }
        if (noSocialGroups)
            text += "<li>No social groups were created</li>\n";
        text += "</ul>\n<br/>\n";
    }
    else
    {
        infoTextCssStyle = "padding-left: 15px; padding-right: 28px;";
        dialogBox.find('#main_content_height').css({'max-height':'288px'});
        dialogBox.find('#main_content_container').css({'width':'550px'});

        var stickerArray = exportGroupsJSONInfo.stickerArray;
        var numStickers  = Object.size(stickerArray);

        // Generate selection grid to save groups as FB Friend Lists, FB Groups
        var fbGroupsText = "Make use of your selected social groups as your Facebook Friend Lists! "+
                           "Share them on your profile as your "+
                           "<a target=\"_blank\" href=\"http://www.facebook.com/help/?page=771#!/help/?faq=19417\"><b>Featured Friends</b></a>!";
                           //"Let your friends know how they matter to you! "+
                           //"Share selected groups with your friends as Facebook Groups!";
        if (isDevMode)
        	fbGroupsText = "Your social groups can be saved to Facebook as Friend Lists and/or Facebook Groups.";
        text += "<div style=\"padding-top: 10px;\">"+fbGroupsText+"</div>\n";
        text += "<table cellspacing=\"0\" cellpadding=\"0\" class=\"saveGroupsGrid\" style=\"border-collapse: separate;\">\n"+
                "<thead>\n"+
                "<tr>\n"+
                "   <th></th>\n";
        if (isDevMode) {
        	text 
            += "   <th>\n"+
               "       <img width=\"18px\" title=\"Save as a Facebook Group\" src=\"export/FBGroupsIcon.png\"><br>Group\n"+
               "   </th>\n";
        }
        text += "   <th class=\"other\">\n"+
                "       <div title=\"Save as a Facebook Friend List\" class=\"fbFriendListIcon\"></div><br>Friend List\n"+
                "   </th>\n";
        text += "</tr>\n"+
                "</thead>\n"+
                "<tbody>\n";
        
        /* Suggest which social groups should be saved as a FB group */
        var sizeAscOrderArray = new Array();
        for (var key in stickerArray) {
        	sizeAscOrderArray[sizeAscOrderArray.length] 
        	= stickerArray[key];
        }
        sizeAscOrderArray.sort(function(sticker1, sticker2){
            return sticker2.clique.length - sticker1.clique.length;
        });
        var topN = 3; 
        var topNStickers = new Object();
        for (var i = 0; i < topN && i < sizeAscOrderArray.length; i++) {
        	topNStickers[sizeAscOrderArray[i].stickerID] = true;
        }

        /* Do a for-loop and iterate through the keys */
        for (var key in stickerArray)
        {
            var stickerObj = stickerArray[key];
            var fbGroupChecked = "checked=\"checked\"";
            if (isDevMode) {
            	fbGroupChecked = "";
            	if (topNStickers[stickerObj.stickerID] != undefined) {
                    fbGroupChecked = "checked=\"checked\"";
                }
            }

            text 
            += "<tr>\n"+
               "    <th>"+stickerObj.name+"</th>\n";
            if (isDevMode) {
            	text 
                += "    <td><input type=\"checkbox\" "+fbGroupChecked+" value=\""
                   +stickerObj.stickerID
                   +"\" class=\"saveAsFBGroup\" title=\"Save as a Facebook Group\" style=\"margin-left: 0px;\"></td>\n";
            }
            text 
            += "    <td class=\"other\"><input type=\"checkbox\" checked=\"checked\" value=\""
               +stickerObj.stickerID
               +"\" class=\"saveAsFBFriendList\" title=\"Save as a Facebook Friend List\" style=\"margin-left: 0px;\"></td>\n"
               +"</tr>\n";

            groupsArray[groupsArray.length] = stickerObj;
        }

        text 
        += "<tr>\n"+
           "    <th>&nbsp;</th>\n";
        if (isDevMode) {
           text += "    <td><input type=\"checkbox\" style=\"visibility: hidden; margin-left: 0px;\"></td>\n";
        }
        text 
        += "    <td class=\"other\"><input type=\"checkbox\" style=\"visibility: hidden; margin-left: 0px;\"></td>\n"+
           "</tr>\n"+
           "</tbody>\n"+
           "</table>\n";


        // Info text
        if (exportGroupsJSONInfo.numStickersUnnamed > 0 ||
            Object.size(exportGroupsJSONInfo.stickerNames) > 0)
        {
            var numDups = 0;
            var dupGroupNames = "";
            var stickerNames = exportGroupsJSONInfo.stickerNames;
            if (stickerNames != null && Object.size(stickerNames) > 0) 
            {
                for (var key in stickerNames) {
                    if (numDups > 0)
                        dupGroupNames += ", ";
                    var dupGroupName = cnvrt2Upper(key);
                    numDups += stickerNames[key];
                    dupGroupNames += "<b>"+dupGroupName+"</b> ("+stickerNames[key]+" groups)"; 
                }
            }

            var nonExported = numDups + exportGroupsJSONInfo.numStickersUnnamed;
            
            //text += "<br/>\n";
            if (nonExported > 1)
                text += nonExported+" social groups ";
            else
                text += nonExported+" social group ";
            text += "could not be saved due to the following issues:<br/>\n" +
                    "<ul style=\"list-style-type: square; padding-left: 20px;\">\n";

            if (exportGroupsJSONInfo.numStickersUnnamed > 0) {
                if (exportGroupsJSONInfo.numStickersUnnamed > 1)
                   text += "<li>"+exportGroupsJSONInfo.numStickersUnnamed+" groups were unnamed</li>\n";
                else
                   text += "<li>"+exportGroupsJSONInfo.numStickersUnnamed+" group was unnamed</li>\n";
            }
            if (numDups > 0) {
               text += "<li>"+numDups+" groups had duplicate group names: " +
                       dupGroupNames + "</li>\n";
            }
               
            text += "</ul><br/>\n";
        }

        var numStickersText = "";
        if (numStickers > 1)
            numStickersText = " social groups";  // numStickers+" identified social groups";
        else
            numStickersText = " social group";   // " identified social group";
            
        text 
        += "Click <b>Confirm</b> to save your "+numStickersText +
           " to your "+accountType+" account or click <b>Close</b>" +
           " to make further changes.\n";
    }
    
    text += "Please provide a unique group name to each social group " +
            "that you would like to save to "+accountType+".\n";
    text = "<div style=\""+infoTextCssStyle+"\">\n"
           +text+"</div>\n";
           

    // Setup dialog confirmation box
    var exportGroupsObject = new Object();
    exportGroupsObject.groupsToExport = groupsArray;
    exportGroupsObject.accountType    = accountType;
    if (accountType == 'Gmail')
       exportGroupsObject.gmailAuthToken = $('#gmailAuthToken').val();
    else if (accountType == 'Facebook') {
       exportGroupsObject.fb_postformid = $('#fb_postformid').val();
       exportGroupsObject.fb_dtsg       = $('#fb_dtsg').val();
    }

    dialogBox.find('#dialogBoxTitle').text("Save As "+accountType+" Friends Lists!");
    // dialogBox.find('#dialogBoxTitle').text("Share As "+accountType+" Groups!");
    if (isDevMode)
       dialogBox.find('#dialogBoxTitle').text("Save Groups to "+accountType);
    dialogBox.find('#contentHolder').html(text);
    dialogBox.find('.close').unbind('click').click(function() {
        dialogBox.fadeOut(function(){
            $(this).remove();
        });
        return false;
      });
    
    var confirmButton = dialogBox.find('#dialogBoxConfirmButton');
    if (groupsArray.length == 0) {
        confirmButton.unbind('click').hide();
    }
    else
    {
        confirmButton.unbind('click').click(function() {
            // show progress message exporting groups
            var loadingMsg = 'Saving Your Groups to '+accountType+'...';
            $.facebox.settings.loadingMessage = loadingMsg;

            // Using POST method to provide info on groups to export
            if (accountType == 'Gmail') {
                var exportGroupsJSON
                = JSON.stringify(exportGroupsObject, null, 1);
                jQuery.facebox({
                    post: 'saveGroupsToGmail.jsp',
                    progress: 'getStatus.jsp',
                    postdata: exportGroupsJSON,
                    postcallback: exportGroupsToGmailResult
                });
            }
            else {
                processSaveToFBPreferences(exportGroupsObject);
            }
            
            dialogBox.fadeOut(function(){
                $(this).remove();
            });
            return false;
          });
        confirmButton.show();
    }

    dialogBox.css({
        top:    getPageScroll()[1] + (getPageHeight() / 10),
        left:   81
    });
}

// Filter out groups not being saved to FB as FB Friend List/Group
function processSaveToFBPreferences(exportGroupsObject)
{
    var groupsToSave = new Object();

    // Figure out save preferences
    $('input.saveAsFBFriendList:checked').each(function(){
        var groupId = $(this).val();
        if (groupsToSave[groupId] == undefined) {
        	groupsToSave[groupId] = new Object();
        }
        groupsToSave[groupId].saveAsFBFriendList = true;
    });

    $('input.saveAsFBGroup:checked').each(function(){
    	var groupId = $(this).val();
        if (groupsToSave[groupId] == undefined) {
            groupsToSave[groupId] = new Object();
        }
        groupsToSave[groupId].saveAsFBGroup = true;
    });


    // Filter out social groups that are not saved
    var stickerArray = exportGroupsObject.groupsToExport;
    var filteredStickerArray = new Array();
    for (var i = 0; i < stickerArray.length; i++) {
        var sticker = stickerArray[i];
    	var savePref = groupsToSave[sticker.stickerID];
        if (savePref != undefined) {
            sticker.savePref = savePref;
            filteredStickerArray[filteredStickerArray.length] = sticker;
        }
    }


    exportGroupsObject.groupsToExport = filteredStickerArray;
    if (filteredStickerArray.length <= 0)
        return;

    var exportGroupsJSON
    = JSON.stringify(exportGroupsObject, null, 1);
	jQuery.facebox({ 
		post: 'saveGroupsToFB.jsp', 
        progress: 'getStatus.jsp',
        postdata: exportGroupsJSON, 
        postcallback: exportGroupsToFBResult
    });
}

</script>
<div id="fbexport-display" class="fbexport-display hidden" style="position: absolute; z-index: 300; height: 80px; width: 80px;">
</div>
