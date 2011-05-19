<%@page import="java.net.*"%>
<%@page import="edu.stanford.socialflows.settings.FBAppSettings"%>
<%@page import="edu.stanford.socialflows.algo.AlgoStats"%>
<%
	//Check for Muse status
    String museCheckURL 
    = FBAppSettings.MUSE_CHECK_URL + "?callback=redirectToMuse";
    
    // Set the URL properly for SocialFlows-Dunbar integration    
    String socialflowsInputURL 
    = originalURL+response.encodeURL(FBAppSettings.DUNBAR_SOCIALFLOWS_POST_DESTINATION);
    String socialflowsIntegrationParams
    = FBAppSettings.DUNBAR_SOCIALFLOWS_POST_PARAMETER
      +"="+URLEncoder.encode(socialflowsInputURL, "UTF-8");
    
    String museIntegrationURL 
    = FBAppSettings.MUSE_URL+"?"+socialflowsIntegrationParams;
    System.out.println("MUSE INTEGRATION URL IS: "+museIntegrationURL);
    
    String nocookiespage = response.encodeURL("enableCookiesMsg.jsp");
    
%>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/faceUI.css"/>
<link type="text/css" rel="stylesheet" media="screen" href="common/collection.css"/>
<script type="text/javascript" src="jquery/jquery.jsonp-2.1.3.min.js"></script>
<script type="text/javascript">
jQuery(document).ready(function($) {
	// Initialize integration UI workflow
    initMuseDialog();
    initEmailDialog();

    // Toggle advanced email options
    $('.toggleEmailOptions').click(function(e) {
        var currentToggle = $('#toggleEmailOptions-indicator');
        if (currentToggle.hasClass('conceal')) {
            // Change to reveal
            currentToggle.removeClass('conceal');
            currentToggle.addClass('reveal');
            $('#toggleEmailOptions-indicator').html('&#9660;'); //Shows Down-Arrow

            // Reveal email options
            $('#emailOptions').slideDown("slow");
        }
        else {
            // Change to hide
            currentToggle.removeClass('reveal');
            currentToggle.addClass('conceal');
            $('#toggleEmailOptions-indicator').html('&#9658;'); //Shows Right-Arrow

            // Reveal email options
            $('#emailOptions').slideUp("slow");
        }

        return false;
    });
    
});

var numGroups   = <%=String.valueOf(AlgoStats.DEFAULT_NUM_GROUPS)%>;
var errorWeight = <%=String.valueOf(AlgoStats.DEFAULT_ERROR_WEIGHT)%>;
function invokeMuse(numGroupsParam, errorWeightParam)
{
    if (numGroupsParam != null) 
        numGroups = numGroupsParam;
    if (errorWeightParam != null) 
    	errorWeight = errorWeightParam;
	
	var checkForMuseDialog = $('#dialogBox_downloadMuse');
    checkForMuseDialog.fadeIn('fast');
    checkForMuse = true;
    checkMuse();
}

function checkMuse()
{
    $.jsonp({
        url: "<%=museCheckURL%>",
        dataType: "jsonp",
        callbackParameter: "jsonp_callback",
        timeout: 5000, // 5 seconds
        success: checkMuseCookies,
        error: recheckMuse
    });
}

var recheckMuseDelay = 300;
var checkForMuse = false;
function recheckMuse()
{
    var checkForMuseDialog = $('#dialogBox_downloadMuse');
    if (checkForMuseDialog.is(':hidden')) {
        checkForMuse = false;
        return;
    }
    
    setTimeout(function(){
            if (checkForMuse)
               checkMuse();
        }, 
        recheckMuseDelay);
}

function checkMuseCookies()
{
    $.jsonp({
        url: "<%=museCheckURL%>&verifyCookies=true",
        dataType: "jsonp",
        callbackParameter: "jsonp_callback",
        timeout: 5000, // 5 seconds
        success: verifyMuseCookiesEnabled,
        error: recheckMuse
    });
}

function verifyMuseCookiesEnabled(status)
{
	var museDialogBox  = $('#dialogBox_downloadMuse');
    var emailDialogBox = $('#dialogBox_provideEmailAcct');
    
    if (status.cookiesEnabled) {
        museDialogBox.fadeOut('fast', function(){
               emailDialogBox.fadeIn('fast');
        });
    }
    else {
        museDialogBox.fadeOut('fast', function(){
               // display 'enable 3rd-party cookies' msg
               showEnableCookiesMsg();
        });
    }
}

function showEnableCookiesMsg()
{
	parent.changeInSituTabSelected("fbdata"); 
	parent.changeToWindow(1, "<%=nocookiespage%>");
}

function initMuseDialog()
{
    var dialogBox = $('#dialogBox_downloadMuse');

    dialogBox.find('.close').unbind('click').click(function() {
        checkForMuse = false;
        dialogBox.fadeOut();
        return false;
      });
    dialogBox.css({
        top:    getPageScroll()[1] + (getPageHeight() / 10),
        left:   81
    });
}

function initEmailDialog()
{
    var dialogBox = $('#dialogBox_provideEmailAcct');

    var confirmButton = dialogBox.find('#dialogBoxConfirmButton');
    confirmButton.unbind('click').click(function() {
        checkForMuse = false;
        dialogBox.fadeOut();

        // Display progress message
        var loadingMsg = 'Logging in...';
        $.facebox.settings.loadingMessage = loadingMsg;
        $.facebox.loading();
        
        // Submit info to Muse
        submitEmailAcctToMuse();
        return false;
    });
    
    dialogBox.find('.close').unbind('click').click(function() {
        checkForMuse = false;
        dialogBox.fadeOut();
        return false;
      });
    dialogBox.css({
        top:    getPageScroll()[1] + (getPageHeight() / 10),
        left:   81
    });
}


function displayMuseError(xOptions, textStatus)
{
    // Display error message
    if (textStatus == 'timeout') {
       $.facebox.reveal(
           'The expected response from our email analysis utility has timed out. '
          +'It could be that a connection to your email service could not be established. '
          +'Please check that you have a working network connection. '
       );
    }
    else {
       $.facebox.reveal(
           'Our email analysis utility seems to be having a problem trying to connect to '
          +'the email account you specified. Please verify that your email account information '
          +'is valid.'
       );
    }
}

var updateMuseStatusDelay = 1000;
function submitEmailAcctToMuse()
{
    var emailServer = $('#emailServer').val();
    if (emailServer == null || emailServer.trim().length <= 0)
    	emailServer = null;

    if (emailServer != null)
        identifyEmailFolders();
    else
        submitToSimpleGroups();
}

function redirectToMuseIntegration(integrationURL)
{
    parent.setDunbarHiddenIframe("blank.html");
    
    // Hide progress message
    $.facebox.close();

    var redirectURL
    = '<%=FBAppSettings.MUSE_URL%>'+'/'+integrationURL
      +'?numGroups='+numGroups+'&errweight='+errorWeight;

    // Redirect to Muse-SocialFlows integration page
    parent.changeInSituTabSelected("dunbar");
    parent.changeToWindow(2, redirectURL);
}

// Simple workflow for Muse-SocialFlows integration
function submitToSimpleGroups()
{
    //var folder = 'Sent';
	var musePage = '/ajax/simpleGroups.jsp?';

    var emailAddress = encodeURI($('#emailId').val());
    var post_params 
    = '&emailAddress=' + emailAddress
      + '&password=' + encodeURI($('#emailPwd').val())
      + '&alternateEmailAddrs=' + encodeURI($('#emailAliases').val())
      + '&jsonp_callback=socialflows';

    var page = '<%=FBAppSettings.MUSE_URL%>'+musePage
               + '<%=socialflowsIntegrationParams%>'
               + post_params;

    log("Submitting email credentials to "+page);

    // Submit login credentials first to /ajax/simpleGroups.jsp
    parent.setDunbarHiddenIframe(page);
    checkMuseSimpleGroupsStatus();
}

function checkMuseSimpleGroupsStatus()
{
	// Use JSONP to figure out if email acct successfully processed
    // Also updates status message while computation is ongoing
    $.jsonp({
        url: '<%=FBAppSettings.MUSE_URL%>'+'/ajax/simpleGroupsJSONP.jsp',
        dataType: "jsonp",
        callbackParameter: "jsonp_callback",
        timeout: 3000, // 3 seconds
        success: processMuseSimpleGroups,
        error: displayMuseError
    });
}

function processMuseSimpleGroups(json, textStatus)
{
    if (json.error) {
        // Hide progress message
        // Display error message
        $.facebox.reveal(json.msg);
    }
    else if (json.success) {
        redirectToMuseIntegration(json.redirectURL);
    }
    else {
        // Update status message if not empty
        if (json.statusMsg && json.statusMsg.trim().length > 0) {
        	var statusMsg = processMuseStatusMsg(json.statusMsg);
            if (statusMsg) {
               var newStatusMsg = $('<div>'+statusMsg+'</div>');
               $.facebox.setLoadingMessage(newStatusMsg.text());
            }
        }

        // Recheck status of folders computation
        setTimeout(function(){
        	checkMuseSimpleGroupsStatus();
        },
        updateMuseStatusDelay);
    }
}


// More complicated workflow for Muse-SocialFlows integration
function identifyEmailFolders()
{
    var musePage = '/folders.jsp?';

    var emailAddress = encodeURI($('#emailId').val());
    var emailServer = $('#emailServer').val();
    var post_params 
    = '&emailAddress=' + emailAddress
      + '&password=' + encodeURI($('#emailPwd').val())
      + '&imapserver=' + encodeURI(emailServer)
      + '&alternateEmailAddrs=' + encodeURI($('#emailAliases').val())
      + '&jsonp_callback=socialflows';

    var page = '<%=FBAppSettings.MUSE_URL%>'+musePage
               + '<%=socialflowsIntegrationParams%>'
               + post_params;

    log("Submitting email credentials to "+page);

    // Submit login credentials first to folders.jsp
    parent.setDunbarHiddenIframe(page);
    checkMuseFoldersStatus();
}

function checkMuseFoldersStatus()
{
    // Checks whether folders.jsp page is done computing
    // & gets the list of selected sent mail folders.
    // Also updates status message while computation is ongoing
    $.jsonp({
        url: '<%=FBAppSettings.MUSE_URL%>'+'/foldersJSONP.jsp',
        dataType: "jsonp",
        callbackParameter: "jsonp_callback",
        timeout: 3000, // 3 seconds
        success: processMuseFolders,
        error: displayMuseError
    });
}

function processMuseFolders(json, textStatus)
{
	if (json.error) {
        // Hide progress message
        // Display error message
        $.facebox.reveal(json.msg);
    }
	else if (json.success) {
		log("Selected the following email folders: "+json.emailFolders);
	    computeGroupsOnFolders(json.emailFolders);
	}
    else {
        // Update status message if not empty
        if (json.statusMsg && json.statusMsg.trim().length > 0) {
        	var statusMsg = processMuseStatusMsg(json.statusMsg);
            if (statusMsg) {
               var newStatusMsg = $('<div>'+statusMsg+'</div>');
               $.facebox.setLoadingMessage(newStatusMsg.text());
            }
        }
           
        // Recheck status of folders computation
        setTimeout(function(){
            checkMuseFoldersStatus();
        }, 
        updateMuseStatusDelay);       
    }
}

function computeGroupsOnFolders(emailFolders)
{
	var musePage = '/ajax/computeGroups.jsp?';
	
    var emailServer = $('#emailServer').val();
    if (emailServer.indexOf('gmail.com') != -1)
        emailServer = 'Gmail';
    
    var post_params = '&jsonp_callback=socialflows';
    for (var i = 0; i < emailFolders.length; i++)
    	post_params += '&folder=' + encodeURI(emailServer+'^-^'+emailFolders[i]);

    var page = '<%=FBAppSettings.MUSE_URL%>'+musePage
               + '<%=socialflowsIntegrationParams%>'
               + post_params;

    // Submit folders to computeGroups.jsp
    parent.setDunbarHiddenIframe(page);
    checkMuseComputeGroupsStatus();
}

function checkMuseComputeGroupsStatus()
{
    // Checks whether computeGroups.jsp page is done computing
    // Also updates status message while computation is ongoing
    $.jsonp({
        url: '<%=FBAppSettings.MUSE_URL%>'+'/ajax/computeGroupsJSONP.jsp',
        dataType: "jsonp",
        callbackParameter: "jsonp_callback",
        timeout: 3000, // 3 seconds
        success: processMuseComputeGroups,
        error: displayMuseError
    });
}

function processMuseComputeGroups(json, textStatus)
{
    if (json.error) {
        // Hide progress message
        // Display error message
        $.facebox.reveal(json.msg);
    }
    else if (json.success) {
    	redirectToMuseIntegration(json.redirectURL);
    }
    else {
        // Update status message if not empty
        if (json.statusMsg && json.statusMsg.trim().length > 0) {
           var statusMsg = processMuseStatusMsg(json.statusMsg);
           if (statusMsg) {
        	  var newStatusMsg = $('<div>'+statusMsg+'</div>');
              $.facebox.setLoadingMessage(newStatusMsg.text());
           }
        }

        // Recheck status of folders computation
        setTimeout(function(){
        	checkMuseComputeGroupsStatus();
        },
        updateMuseStatusDelay);
    }
}

function processMuseStatusMsg(museStatusOutput)
{
	var museStatusMsg = museStatusOutput;
	try {
        var museStatusObj = JSON.parse(museStatusMsg);
        if (museStatusObj.message) {
        	museStatusMsg = museStatusObj.message;
            if (museStatusObj.pctComplete >= 0)
        	   museStatusMsg += " ("+museStatusObj.pctComplete+"% complete)";
        }
        else {
        	museStatusMsg = "";
        }
    }
    catch (jsonError) { }

    if (museStatusMsg && museStatusMsg.trim().length > 0)
       return museStatusMsg;
    else
       return null;
}

</script>

  <!-- Dialog Box to display info -->
  <div id="dialogBox_downloadMuse" class="faceUI" style="display: none; z-index: 300; position: fixed;">
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
                <span id="dialogBoxTitle">Download Our Email Analysis Utility</span>
            </h2>
            <div class="content" style="overflow-y: auto; overflow-x: hidden; max-height: 212px;">
                <div id="main_content_container" class="full" style="width: 500px;">
                    <div class="content clearfix">
                    <div class="clearfix">
                    <div id="contentHolder" class="clearfix">
                    <!-- Start of grid -->
                    
                    <div style="text-align:justify; padding-left: 8px; padding-right: 8px;">
                    <br/>
                    <!-- Checking the status of Muse... -->
                    <div id="progressmessage" style="text-align: center;">
                        <img id="loading" src="facebox/loading.gif" style="vertical-align: middle; padding-right: 8px;">
                        Waiting for our email analysis utility to be started... 
                    </div>
                    <br/>
                    To protect your privacy, SocialFlows Muse (our email analysis program) runs locally on 
                    your computer as a Java Web Start application. The only 
                    information that is uploaded to our web application is the resulting social groups identified
                    by our analysis. No other private information, such as your email messages, are uploaded.
                    <br/><br/>
                    Please click to download <a href="<%=FBAppSettings.MUSE_DOWNLOAD_URL%>"><b>SocialFlows Muse</b></a>,
                    our email analysis utility, as a Java Web Start application. When the browser prompts you, 
                    open the file using <b>Java Web Start</b>. 
                    Then, click <b>Allow</b> to let SocialFlows Muse run on your computer.
                    </div>

                    <!-- End of grid -->
                    </div></div></div>
                </div>
            </div>
            <div class="info"></div>
            <div class="footer">
                <a href="<%=FBAppSettings.MUSE_DOWNLOAD_URL%>">
                <input id="dialogBoxConfirmButton" type="button" class="inputsubmit" name="Download" value="Download & Run" style="float: left; vertical-align: top;"/>
                </a>
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
  
    <!-- Dialog Box to display info -->
  <div id="dialogBox_provideEmailAcct" class="faceUI" style="display: none; z-index: 300; position: fixed;">
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
                <span id="dialogBoxTitle">Email Account Information</span>
            </h2>
            <div class="content" style="overflow-y: auto; overflow-x: hidden; max-height: 268px;">
                <div id="main_content_container" class="full" style="width: 500px;">
                    <div class="content clearfix">
                    <div class="clearfix">
                    <div id="contentHolder" class="clearfix">
                    <!-- Start of grid -->
                    <div style="padding-left: 8px; padding-right: 8px;">
                    
                    <div id="emailHeader" class="UIImageBlock clearfix" style="padding-top: 5px; padding-bottom: 10px;">
                        <img src="export/letter.png" style="vertical-align: middle;">
                        <div style="display: inline; margin-left: 5px;">
                        Which email account would you like to analyze?
                        </div>
                    </div>
                    
                    <div>
                    <div style="display: block;">
                    <div style="display: inline; padding-right: 28px; font-weight:bold; color:rgb(102, 102, 102);">
                        Your Email:
                    </div>
                    <div class="UIComposer_Attachment_TDTextArea" style="display: inline; margin-left: 5px;">
                        <input type="text" value="" name="emailId" id="emailId" title="emailId" class="inputtext UIComposer_Attachment_Input DOMControl_placeholder" style="width: 228px;"/>
                    </div>
                    </div>
                    <div style="display: block; padding-top: 8px;">
                    <div style="display: inline; font-weight:bold; color:rgb(102, 102, 102);">
                        Email Password:
                    </div>
                    <div class="UIComposer_Attachment_TDTextArea" style="display: inline; margin-left: 5px;">
                        <input type="password" value="" name="emailPwd" id="emailPwd" title="emailPwd" class="inputtext UIComposer_Attachment_Input DOMControl_placeholder" style="width: 228px;"/>
                    </div>
                    </div>
                    <div style="padding-top: 8px;">
                    SocialFlows will never store your password. Your email privacy is preserved.
                    </div>
                    </div>
                    
                    <div style="padding-top: 14px;">
                    <b>
                    <a href="#" id="toggleEmailOptions-indicator" class="toggleEmailOptions conceal" style="text-decoration:none;">&#9658;</a>&nbsp;<a href="#" id="toggleEmailOptions-text" class="toggleEmailOptions">More Email Options</a>
                    </b>
                    <div id="emailOptions" class="hidden">
                    <div style="display: block; padding-top: 8px;">
                    <div style="display: inline; padding-right: 18px; font-weight:bold; color:rgb(102, 102, 102);">
                        Email Server:
                    </div>
                    <div class="UIComposer_Attachment_TDTextArea" style="display: inline; margin-left: 5px;">
                        <input type="text" value="" name="emailServer" id="emailServer" title="emailServer" class="inputtext UIComposer_Attachment_Input DOMControl_placeholder" style="width: 228px;"/>
                    </div>
                    </div>
                    </div>
                    </div>
                    
                    <div style="padding-top: 14px;">
                    <b>Do you have other email aliases?</b> (e.g. @hotmail.com, @yahoo.com, etc.)
                    <br/>This will help us differentiate your many other email identities from that of your friends.
                    <br/>
                    <div style="display: block; padding-top: 5px;">
                    <div style="display: inline; padding-right: 16px; font-weight:bold; color:rgb(102, 102, 102);">
                        Other Emails:
                    </div>
                    <div style="display: inline; margin-left: 5px;" class="UIComposer_Attachment_TDTextArea">
                        <input type="text" style="width: 328px;" class="inputtext UIComposer_Attachment_Input DOMControl_placeholder" title="emailAliases" id="emailAliases" name="emailAliases" value="">
                    </div>
                    </div>
                    </div>
                    
                    </div>
                    <!-- End of grid -->
                    </div></div></div>
                </div>
            </div>
            <div class="info"></div>
            <div class="footer">
                <input id="dialogBoxConfirmButton" type="button" class="inputsubmit" name="Confirm" value="Confirm" style="float: left; vertical-align: top;"/>
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