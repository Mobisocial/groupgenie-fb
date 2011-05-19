<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="edu.stanford.prpl.insitu.*"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xml:lang="en" xmlns="http://www.w3.org/1999/xhtml" lang="en"><head>
<meta http-equiv="Content-type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-language" content="en">
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7">
<META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
<META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">

<title>Clear Your Data</title>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/faceUIBody.css"/>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/facebox.css"/>
<link type="text/css" rel="stylesheet" media="screen" href="facebox/faceUI.css"/>

<script type="text/javascript" src="common/json2min.js"></script>
<script type="text/javascript" src="common/jscommon.js"></script>
<script type="text/javascript" src="common/statusUpdate.js"></script>
<script type="text/javascript" src="jquery/jquery-1.5.1.min.js"></script>
<script type="text/javascript" src="facebox/facebox.js"></script>
<script type="text/javascript">
jQuery(document).ready(function($) {
    $('a[rel*=facebox]').facebox({
      loading_image : 'loading.gif',
      close_image   : 'closelabel.gif'
    });

    // Extract selected photo albums
    $("#clearData").click(function() {
    	clearData();
    	return false;
    });
});


function clearData()
{
	var loadingMsg = 'Clearing Your Data...';
	$.facebox.settings.loadingMessage = loadingMsg;
    jQuery.facebox({ ajax: 'clearData.jsp', progress: 'getStatus.jsp' });
}

    </script>
</head>
<body>
<br/>
<div>Select the <b>Clear Your Data</b> button to clear your data.</div>
<br/>
            <ul class="UIComposer_Buttons">
                <li class="UIComposer_FormButtons">
                <span class="UIComposer_SubmitButton UIButton UIButton_Blue UIFormButton">
                    <input id="clearData" type="button" class="UIButton_Text" value="Clear Your Data"/>
                </span>
                </li>
            </ul>
            <br/><br/><br/>
            
</body></html>