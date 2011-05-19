<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="edu.stanford.socialflows.settings.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv="Content-type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-language" content="en">
<META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
<META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">

<title>Enable Third-Party Cookies for Your Browser</title>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/faceUIBody.css"/>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/facebox.css"/>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/faceUI.css"/>

<script type="text/javascript" src="common/jscommon.js"></script>
<script type="text/javascript" src="jquery/jquery-1.5.1.min.js"></script>
<script type="text/javascript">

$(document).ready(function() {
    $('#reloadSocialFlows').click(function(){
    	top.location.href="<%=FBAppSettings.canvasURL%>";
        return false;
    });
});

</script>
<style>
#title {
    font-size:125%;
    font-weight:bold;
}
</style>
</head>
<body>        
        <!-- Enable Third-Party Cookies -->
        <div class="enable-third-party-cookies-display">
        <br/>
        <span id="title">Enable Third-Party Cookies for Your Browser</span>
        <br/><br/>
        Facebook Applications such as <b><%=FBAppSettings.appNameDisplay%></b> rely on third-party cookies to work. 
        Please enable third-party cookies for your browser.
        <br/><br/>
        To enable third-party cookies, please follow the instructions below for your particular browser:
        <br/><br/>
        <ul style="list-style-type: decimal; padding-left: 20px;">
        <li>For Firefox, 
            see <a href="http://support.mozilla.com/en-US/kb/Enabling%20and%20disabling%20cookies" target="_blank"><b>Enabling and disabling cookies</b></a>.</li>
        <li>For Google Chrome, 
            see <a href="http://www.google.com/support/chrome/bin/answer.py?answer=95647" target="_blank"><b>Adjust cookie permissions</b></a>.</li>
        <li>For Internet Explorer, 
            we recommend at this time that you use 
            <a href="http://www.mozilla.com/firefox/" target="_blank"><b>Firefox</b></a>, 
            <a href="http://www.google.com/chrome" target="_blank"><b>Google Chrome</b></a> or 
            <a href="http://www.apple.com/safari/download/" target="_blank"><b>Safari</b></a> 
            to access <b><%=FBAppSettings.appNameDisplay%></b>, since our application uses features not supported by IE at this time. 
            Click to 
            <a href="http://www.mozilla.com/firefox/" target="_blank"><b>Download Firefox</b></a>, 
            <a href="http://www.google.com/chrome" target="_blank"><b>Download Chrome</b></a> or 
            <a href="http://www.apple.com/safari/download/" target="_blank"><b>Download Safari</b></a>.</li>
        </ul>
        <br/>
        <a id="reloadSocialFlows" href="javascript:;"><b>Reconnect to <%=FBAppSettings.appNameDisplay%></b></a> once you have enabled third-party cookies for your browser.
        <br/>
        </div>
</body>
</html>