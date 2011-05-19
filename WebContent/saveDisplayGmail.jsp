<%@page import="com.google.gdata.client.*"%>
<%@page import="com.google.gdata.client.http.*"%>
<%@page import="com.google.gdata.client.contacts.*"%>
<%@page import="com.google.gdata.data.*"%>
<%@page import="com.google.gdata.data.contacts.*"%>
<%@page import="com.google.gdata.data.extensions.*"%>
<%@page import="com.google.gdata.util.*"%>
<%@page import="edu.stanford.socialflows.settings.*"%>
<%
    // Sets the redirect URL properly
    String next  = response.encodeURL(originalURL + "processGmailAuth.jsp");
    String scope = "http://www.google.com/m8/feeds/";
    boolean isSecure  = false;
    boolean isSession = true;
    String authSubLogin = AuthSubUtil.getRequestUrl(next, scope, isSecure, isSession);

%>
<script type="text/javascript" src="jquery/jquery.popupWindow.js"></script>
<script language="javascript" type="text/javascript">

jQuery(document).ready(function($) {

	// Do AJAX checks to determine when user has authorized Gmail access
	$('#beginGmailAuth').click(function() {
	    // check if gmail token already obtained
	    var gmailToken = $('#<%=FBAppSettings.GMAIL_TOKEN%>').val();
        if (gmailToken == null || gmailToken == 'null' || gmailToken.trim().length <= 0)
        	gmailToken = 'null';
	    
        if (gmailToken == 'null') 
        {
            // show checking if authorized
            var loadingMsg = 'Waiting for Your Gmail Authorization...'
                             +'(or <a class="closeProgressMsg" href="#"><b>Cancel</b></a>)';            
            $.facebox.settings.loadingMessage = loadingMsg;

            // Using POST method but posting no data to getGmailAuth.jsp
            jQuery.facebox({ post: '<%=response.encodeURL("getGmailAuth.jsp")%>', 
                             progress: '<%=response.encodeURL("getStatus.jsp")%>',
                             postdata: '{}', 
                             postcallback: checkUserGmailAuthorization });
            
            // if not, popup window to get authorization token
            $(this).popupWindow({
                height:380, 
                width:630, 
                windowName:'Export SocialFlows Groups to Gmail',
                windowURL :'<%=authSubLogin%>',
                centerBrowser:1
            });
        }
        else
        {
        	determineGroupsToExport('Gmail');
        }
        
        return false;
	});

});

function checkUserGmailAuthorization(response, inputdata)
{
    if (response == null)
        return "An error has occured. No Gmail authorization response was found. "+
               "Please check that you have authorized SocialFlows to access your Gmail.";
    
    response = response.replace(/^\s+|\s+$/g, ''); // remember to strip whitespace, used to be a bug
    if (response.length > 0) {
        var resultData = JSON.parse(response);
        if (!resultData.success) {
           // hide progress message
           // Show error message
           // Reshow button to get Gmail auth
           return resultData.message;
        }
        else {
           // Set Gmail auth token
           $('#gmailAuthToken').val(resultData.gmailAuthToken);
           
           // reveal "Save Groups to Gmail" dialog window
           determineGroupsToExport('Gmail');
        }
    }

    // Hide progress dialog
    //$.facebox.close();
}

function exportGroupsToGmailResult(response, inputdata)
{
    if (response == null)
        return "An error has occured. No results were returned.";
     
     var resultData = JSON.parse(response);
     if (!resultData.success)
        return resultData.message;

     updateSavedGroups(resultData);
     return resultData.message;
}

/* NOT USED */
function revealGmailDisplay() {
    $('#gmail-display').css({
        top:    getPageScroll()[1] + (getPageHeight() / 10),
        left:   38
    });
    
    $('#GMAIL_IFRAME').attr('src', '...');
    $('#gmail-display').fadeIn('fast');
    return false;
}

</script>
<div id="gmail-display" class="gmail-display hidden" style="position: absolute; z-index: 300; border: 4px solid rgb(204, 204, 204); height: 380px; width: 630px;">
<iframe name="GMAIL_WINDOW" id="GMAIL_IFRAME" src ="#" frameborder=0 scrolling="auto" style="width: 100%; height: 100%; border: medium none;">
  <p>Your browser does not support iframes.</p>
</iframe>
</div>