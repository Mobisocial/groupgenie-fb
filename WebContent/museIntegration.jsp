<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="edu.stanford.socialflows.settings.*"%>
<%
	String museCheckURL = FBAppSettings.MUSE_CHECK_URL + "?callback=redirectToMuse";

    // Set the URL properly for SocialFlows-Dunbar integration
    String originalURL = request.getRequestURL().toString();
    originalURL = originalURL.substring(0, originalURL.lastIndexOf("/")+1);

    if (originalURL.charAt(originalURL.length()-1) != '/')
        originalURL += "/";
    String socialflowsInputURL = response.encodeURL(originalURL+FBAppSettings.DUNBAR_SOCIALFLOWS_POST_DESTINATION);
    String museIntegrationURL = FBAppSettings.MUSE_URL+"?"+
    		                    FBAppSettings.DUNBAR_SOCIALFLOWS_POST_PARAMETER+"="+socialflowsInputURL;

    System.out.println("MUSE INTEGRATION URL IS: "+museIntegrationURL);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv="Content-type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-language" content="en">
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7">
<META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
<META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">

<title>SocialFlows for Muse</title>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/faceUIBody.css"/>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/facebox.css"/>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/faceUI.css"/>

<script type="text/javascript" src="common/json2min.js"></script>
<script type="text/javascript" src="common/jscommon.js"></script>
<script type="text/javascript" src="jquery/jquery-1.5.1.min.js"></script>
<script type="text/javascript" src="jquery/jquery.jsonp-2.1.3.min.js"></script>
<script type="text/javascript">

function redirectToMuse(json, textStatus)
{
    var dunbarIframe = parent.document.getElementById("INSITU_DUNBAR_IFRAME");
    dunbarIframe.src = "<%=museIntegrationURL%>";
}

function displayMuseNotFound(xOptions, textStatus)
{
	$('#progress-display').slideUp('slow', function(){
		$('#muse-unavailable-display').slideDown('fast');
	});
}

function recheckMuse()
{
	$('#muse-unavailable-display').slideUp('slow', function(){
		$('#progress-display').slideDown('fast');
		checkMuse();
	});
}

function checkMuse()
{
    $.jsonp({
        url: "<%=museCheckURL%>",
        dataType: "jsonp",
        callbackParameter: "jsonp_callback",
        timeout: 5000, // 5 seconds
        success: redirectToMuse,
        error: displayMuseNotFound
    });
}

$(document).ready(function() {

	/*
	$.getJSON(<%=museCheckURL%>,
			  function(data) {
		         $('.result').html('<p>' + data.foo + '</p>'
		                           + '<p>' + data.baz[1] + '</p>');
		      }
    );

	$.ajax({
		url: '<%=museCheckURL%>',
        dataType: 'jsonp',
        data: 'id=28',
        jsonp: 'jsonp_callback',
        //jsonpCallback: 'redirectToMuse',
        success: redirectToMuse
    });
    */

    $('#checkMuse').click(function(){
    	recheckMuse();
        return false;
    });

    checkMuse();

});
</script>
<style>

div.progress-display {
    padding-bottom:0px;
    padding-top:50px;
    text-align:center;
    padding-left:5px;
    padding-right:5px;
}

.progress-display #progressmessage {
    font-size:125%;
}

</style>
</head>
<body>

        <!--- Progress Display --->
        <div id="progress-display" class="progress-display">
        <br/><br/><br/>
        <img id="loading" src="facebox/loading.gif">
        <br/><br/>
        <!-- Checking the status of Muse... -->
        <span id="progressmessage">Checking the status of Muse on your local machine... </span>
        </div>
        
        <!--  Muse Not Found -->
        <div id="muse-unavailable-display" class="muse-unavailable-display hidden">
        <br/>
        <b>Muse</b>, our email analysis program, was not found on your local machine.
        <br/><br/>
        Before we could analyze your emails to figure out your social groups, please 
        download <b>Muse</b>, our email analysis program that runs locally on your 
        own machine in order to preserve the privacy of your emails. 
        <br/><br/>
        To download and run <b>Muse</b> on your local machine, 
        please follow the instructions below:
        <br/><br/>
        <ul style="list-style-type: decimal; padding-left: 20px;">
        <li>Click to <a href="<%=FBAppSettings.MUSE_DOWNLOAD_URL%>"><b>download Muse</b></a> 
            and you will be prompted by your browser to open the file using <b>Java Web Start</b> 
            or save the file separately. Choose the earlier and open the file using <b>Java Web Start</b>.</li>
        <li><b>Java Web Start</b> will then begin to download <b>Muse</b> to your local machine.</li>
        <li>Once the download completes, you will be prompted to authorize <b>Muse</b> to run on your local machine.</li>
        <li>Click <i>Allow</i> to let <b>Muse</b> run the email analysis on your local machine.</li>
        <li>You may be prompted to save <b>Muse</b> on your desktop or in another folder. 
            Click <i>Save</i> to retain <b>Muse</b> on your machine.</li>
        <li><b>Muse</b> will then open a new browser window/tab displaying a page where you could provide
            it with your email account information. There might be a delay of 3 to 5 seconds before this happens.</li>
        <li>Now, you have <b>Muse</b> successfully up and running on your local machine!</li>
        </ul>
        <br/>
        Once you have 
        <a href="<%=FBAppSettings.MUSE_DOWNLOAD_URL%>"><b>Muse</b></a> 
        running on your local machine, click the button below to continue.
        <br/><br/>
        <a id="checkMuse" href="#"><b>Check for Muse Again</b></a>
        </div>
</body>
</html>