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

<title>Browser Not Currently Supported</title>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/faceUIBody.css"/>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/facebox.css"/>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/faceUI.css"/>

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
        <span id="title">Browser Not Currently Supported</span>
        <br/><br/>
        <b><%=FBAppSettings.appNameDisplay%></b> uses a number of web features that 
        may not be currently supported by your browser. We recommend at this time 
        that you use one of the following browsers to access our application:
        <br/><br/>
        <ul style="list-style-type: circle; padding-left: 20px;">
        <li>Firefox, click to <a href="http://www.mozilla.com/firefox/" target="_blank"><b>download</b></a>.</li>
        <li>Google Chrome, click to <a href="http://www.google.com/chrome" target="_blank"><b>download</b></a>.</li>
        <li>Safari, click to <a href="http://www.apple.com/safari/download/" target="_blank"><b>download</b></a>.</li>
        </ul>
        <br/>
        Give <b><%=FBAppSettings.appNameDisplay%></b> a try again using one of the above recommended browsers!
        <br/>
        </div>
</body>
</html>