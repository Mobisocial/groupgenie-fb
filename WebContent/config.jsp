<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="edu.stanford.prpl.api.PRPLAppClient"%>
<%@page import="edu.stanford.prpl.insitu.*"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.prpl.insitu.util.PrPlConnectionManager.PrPlHostInfo"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@page import="edu.stanford.socialflows.settings.FBAppSettings"%>
<%@page import="edu.stanford.socialflows.db.DBConnectionManager"%>
<%@include file="sessionCheck.jsp"%>
<%
	// Now get the previous page user was at
      String prevPage // = "main.jsp?"+FBAppSettings.FACEBOOK_SESSION_KEY+"="+URLEncoder.encode(newSessionKey);
      = response.encodeURL("main.jsp");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xml:lang="en" xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
<meta http-equiv="Content-type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-language" content="en">
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7">
<META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
<META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">

<title>Configuration</title>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/faceUIBody.css"/>
<script type="text/javascript" src="common/json2min.js"></script>
<script type="text/javascript" src="jquery/jquery-1.5.1.min.js"></script>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/facebox.css"/>   
<script type="text/javascript" src="facebox/facebox.js"></script>
<script type="text/javascript" src="common/statusUpdate.js"></script>
<script type="text/javascript">
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
    
  });

var prevPage = '<%=prevPage%>';

function saveSocialFlowsConfig()
{
    var newSfID  = document.getElementById('sfID').value;
    var newSfPwd = document.getElementById('sfPassword').value;
    if (newSfID && newSfID.length > 0)
    {
        var sfConfigObj    = new Object();
        sfConfigObj.sfID   = newSfID;
        sfConfigObj.sfPwd  = newSfPwd;
        var sfConfigJSON   = JSON.stringify(sfConfigObj, null, 1);

        $.facebox.settings.loadingMessage = 'Configuring your SocialFlows account...';
        jQuery.facebox({ post: 'saveSocialFlowsConfig.jsp', progress: 'getStatus.jsp',
                         postdata: sfConfigJSON, postcallback: function(result, inputdata){return result;} });
    }
}

function saveButlerConfig()
{
	var newPcbID = document.getElementById('pcbID').value;
	var newPcbPwd = document.getElementById('pcbPassword').value;
    var newPcbHost = document.getElementById('pcbHosting').value;
	if (newPcbID && newPcbID.length > 0)
	{
	    var pcbConfigObj = new Object();
	    pcbConfigObj.pcbID   = newPcbID;
	    pcbConfigObj.pcbPwd  = newPcbPwd;
	    pcbConfigObj.pcbHost = newPcbHost;
	    var pcbConfigJSON = JSON.stringify(pcbConfigObj, null, 1);

		$.facebox.settings.loadingMessage = 'Saving Configuration for Personal Cloud Butler...';
        jQuery.facebox({ post: 'saveButlerConfig.jsp', progress: 'getStatus.jsp',
                         postdata: pcbConfigJSON, postcallback: function(result, inputdata){return result;} });

	    var refreshSocialFlow = parent.document.getElementById('refreshSocialFlow');
	    var refreshStickers   = parent.document.getElementById('refreshStickers');
	    refreshSocialFlow.value = "true";
	    refreshStickers.value   = "true";
	}
}

function cancel()
{
	window.location = prevPage;
}


function startButler()
{
    $.facebox.settings.loadingMessage = 'Starting Personal Cloud Butler...';
    jQuery.facebox({ ajax: 'startButler.jsp?operation=startButler' });
}

function stopButler()
{
    $.facebox.settings.loadingMessage = 'Stopping Butler...';
    jQuery.facebox({ ajax: 'stopButler.jsp?operation=stopButler' });
}

function deleteButler()
{
    $.facebox.settings.loadingMessage = 'Deleting Butler...';
    jQuery.facebox({ ajax: 'deleteButler.jsp?operation=deleteButler' });
}

function urlEncode(inputString)
{
    var encodedInputString=escape(inputString);
    encodedInputString=encodedInputString.replace("+", "%2B");
    encodedInputString=encodedInputString.replace("/", "%2F");
    return encodedInputString;
}
</script>
</head>
<body>
<br/>
Configure your SocialFlows account to store your personal Social Topology and make it exportable to your mobile and social applications.
<br/><br/>
<div id="insitu_status">Current SocialFlows ID: </div>
<div class="UIComposer_Attachment_TDTextArea" style="padding-bottom: 5px;">
     <input type="text" value="<%=sfUserID%>" id="sfID" class="inputtext UIComposer_Attachment_Input DOMControl_placeholder" style="width: 180px;"/>
</div>
<div id="insitu_status">SocialFlows Password: </div>
<div class="UIComposer_Attachment_TDTextArea" style="padding-bottom: 5px;">
     <input type="password" id="sfPassword" class="inputtext UIComposer_Attachment_Input DOMControl_placeholder" style="width: 180px;"/>
</div>
<br/>
<div class="UIComposer_Buttons">
    <div class="UIComposer_FormButtons">
        <span class="UIComposer_SubmitButton UIButton UIButton_Blue UIFormButton" style="margin-right: 3px;">
            <input type="button" onclick="saveSocialFlowsConfig();" class="UIButton_Text" value="Apply"/>
        </span>
        <span class="UIComposer_SubmitButton UIButton UIButton_Blue UIFormButton">
            <input type="button" onclick='cancel();' class="UIButton_Text" value="Cancel"/>
        </span>
    </div>
</div>
<br/><br/><br/>



<%
	if (FBAppSettings.ENABLE_PRPL)
{
	// Get connection to PrPl
    // Get PrPlConnectionManager
    PrPlConnectionManager prplConnManager 
    = (PrPlConnectionManager)session.getAttribute(FBAppSettings.PRPL_SERVICE_MANAGER);

    PRPLAppClient prplService_ = null;
    if (prplConnManager != null)
    {
        try
        {
            // Starts a user's PCB if it has not already been started
            prplService_ = prplConnManager.getButlerClient();
        }
        catch (PrPlHostException phe)
        {
            // ERROR: PRINT OUT ERROR MESSAGE
            System.out.println("The following PrPlHost error occured: "+phe.getMessage());
            out.println("Attempted to connect to your Personal Cloud Butler when the following PrPlHost error occured: "+phe.getMessage());
            return;
        }
        catch (Exception e)
        {
            // ERROR: PRINT OUT ERROR MESSAGE
            System.out.println("An exception occured: "+e.getMessage());
            out.println("Attempted to connect to your Personal Cloud Butler when the following error occured: "+e.getMessage());
            return;
        }
        if (prplService_ == null) {
            String finalStatus = prplConnManager.finalStatusMessage;
            out.println(finalStatus);
            return;
        }
    }
%>

<a href="#" id="toggleAdvancedOptions-indicator" class="toggleAdvancedOptions" style="text-decoration:none;">&#9658;</a>&nbsp;<a href="#" id="toggleAdvancedOptions-text" class="toggleAdvancedOptions">Show Advanced Options</a>
<div id="advancedOptions" class="hidden">
<br/>

Save your personal Social Topology into your own Personal Cloud Butler (PCB)! Ensure that the specified Butler is currently running.
<br/><br/>
<div id="insitu_status">Current PCB ID: </div>
<div class="UIComposer_Attachment_TDTextArea" style="padding-bottom: 5px;">
     <input type="text" id="pcbID" class="inputtext UIComposer_Attachment_Input DOMControl_placeholder" style="width: 180px;"/>
</div>
<div id="insitu_status">PCB Password: </div>
<div class="UIComposer_Attachment_TDTextArea" style="padding-bottom: 5px;">
     <input type="password" id="pcbPassword" class="inputtext UIComposer_Attachment_Input DOMControl_placeholder" style="width: 180px;"/>
</div>
<br/>
<div class="UIComposer_Buttons">
    <div class="UIComposer_FormButtons">
        <span class="UIComposer_SubmitButton UIButton UIButton_Blue UIFormButton" style="margin-right: 3px;">
            <input type="button" onclick="saveButlerConfig();" class="UIButton_Text" value="Apply"/>
        </span>
    </div>
</div>
<br/>

<p>Select where your PrPl Personal Cloud Butler will be hosted. 
If your PCB is hosted on your own machine (Personal Hosting), make sure your PCB is up and running first.</p>
<span class="label" style="font-size:11px; font-weight:bold;">PCB Hosting Service:</span>
<span class="selector" style="margin:0 0 0 10px;">
    <select style="direction: ltr; border:1px solid #BDC7D8; font-family:tahoma,verdana,arial,sans-serif; font-size:11px; padding:2px;" onchange="" name="pcbHosting" id="pcbHosting" class="">
<%
	Collection<PrPlHostInfo> prplHosts = PrPlConnectionManager.getPrPlHosts().values();

     // Create initial PrPlConnectionManager if needed
     if (prplConnManager == null) 
     {
    	 prplConnManager = new PrPlConnectionManager();
    	 prplConnManager.setCurrentUserName(fbMyInfo.getName());
    	 prplConnManager.setPrplKeysFilePath(application.getRealPath("keys/prpl_app.keystore"));
    	 if (FBAppSettings.ENABLE_PRPL) {
    		// Determine the PrPl user id
            String prplUserID = "fbid"+fbMyInfo.getFBUid();
            prplConnManager.setCurrentUserID(prplUserID);
            prplConnManager.setCurrentUserPwd("insitulicious"+prplUserID);
         }
    	 session.setAttribute(FBAppSettings.PRPL_SERVICE_MANAGER, prplConnManager);
     }

     for (PrPlHostInfo host : prplHosts) {
%>
        <option value="<%=host.name%>" <%if(prplConnManager.getCurrentHostName().equals(host.name)){%>selected="yes"<%}%>><%=host.displayText%></option>	 
     <% 
     }
%>
    </select>
</span>
<br/><br/>
    <div style="display: block;">
            <ul class="UIComposer_Buttons">
                <li class="UIComposer_FormButtons">
                <span class="UIComposer_SubmitButton UIButton UIButton_Blue UIFormButton">
                    <input type="button" onclick='startButler();' class="UIButton_Text" value="Start Butler"/>
                </span>
                </li>
            </ul>
            <br/>
            <ul class="UIComposer_Buttons">
                <li class="UIComposer_FormButtons">
                <span class="UIComposer_SubmitButton UIButton UIButton_Blue UIFormButton">
                    <input type="button" onclick='stopButler();' class="UIButton_Text" value="Stop Butler"/>
                </span>
                </li>
            </ul>
    </div>
    <br/>
            <ul class="UIComposer_Buttons">
                <li class="UIComposer_FormButtons">
                <span class="UIComposer_SubmitButton UIButton UIButton_Blue UIFormButton">
                    <input type="button" onclick='deleteButler();' class="UIButton_Text" value="Delete Butler"/>
                </span>
                </li>
            </ul>
</div>

<%
}

%>

</body>
</html>