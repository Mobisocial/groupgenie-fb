<%// Prevents browser-side caching of page results
response.setHeader("Cache-Control","no-cache");
response.setHeader("Pragma","no-cache");
response.setDateHeader ("Expires", 0);%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="com.restfb.LegacyFacebookClient"%>
<%@page import="com.restfb.DefaultLegacyFacebookClient"%>
<%@page import="com.restfb.exception.FacebookException"%>
<%@page import="edu.stanford.prpl.insitu.*"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@page import="edu.stanford.prpl.insitu.util.PrPlConnectionManager"%>
<%@page import="edu.stanford.socialflows.db.DBConnectionManager"%>
<%
      String errorType = request.getParameter("problem");
      boolean errorDetected = true;
      if (errorType == null || errorType.trim().length() <= 0) {
    	  errorDetected = false;
    	  %><%@include file="sessionInit.jsp"%><%
      }

      String mainURL = response.encodeURL("main.jsp");
      if (errorDetected) {
    	  errorType = errorType.trim().toLowerCase();
    	  if (errorType.equals("nocookies"))
    	     mainURL = response.encodeURL("enableCookiesMsg.jsp");
    	  else
    		 mainURL = response.encodeURL("enableBetterBrowserMsg.jsp");
      }
      String configURL      = response.encodeURL("config.jsp");
      String socialflowsURL = response.encodeURL("socialflowProgress.jsp");
      String stickersURL    = response.encodeURL("stickers.jsp");
      String cliquesMapURL  = response.encodeURL("cliquesMap.jsp");
      String finishURL      = response.encodeURL("finish.jsp");
      String publishToFBFeedURL = response.encodeURL("publishFeedToFB.jsp");

      String butlerTabCss = "first", butlerLinkCss = "selected";
      String fbdataTabCss = "", fbdataLinkCss = "";
      butlerTabCss = "hidden"; butlerLinkCss = "hidden";
      fbdataTabCss = "first"; fbdataLinkCss = "selected";
      
      String emailTabCss = "";
      if (!FBAppSettings.ENABLE_EMAILS)
    	  emailTabCss = "hidden";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html lang="en" xml:lang="en" xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-language" content="en">
<title>SocialFlows</title>

<script type="text/javascript" src="jquery/jquery-1.5.1.min.js"></script>
<script type="text/javascript" src="jquery/jquery.popupWindow.js"></script>
<script type="text/javascript">
jQuery(document).ready(function($) {

	// Help window
	$('#socialflowsHelp').click(function() {
		$(this).popupWindow({
	        height:580, 
	        width:728, 
	        windowName:'SocialFlows - Help',
	        windowURL :'help.html',
	        centerBrowser:1,
	        location:0,
	        resizable:0,
	        scrollbars:1
	    });
	});
	
    
});

function changeInSituTabSelected(view)
{
    this.toggle={'butler'    :document.getElementById("toggle_butler"),
    		     'fbdata'    :document.getElementById("toggle_fbdata"),
                 'dunbar'    :document.getElementById("toggle_dunbar"),
                 'socialflow':document.getElementById("toggle_socialflow"),
                 'finish'    :document.getElementById("toggle_finish"),
                 'stickers'  :document.getElementById("toggle_stickers")};
    for(var tab in this.toggle)
    {
        if (tab==view){
           this.toggle[tab].setAttribute("class", "selected");
        }
        else{
           this.toggle[tab].setAttribute("class", "");
        }
    }  
}

function changeToWindow(windowNumber, url)
{
	var insituWindow = document.getElementById("insitu_window");
	var dunbarWindow = document.getElementById("dunbar_window");
	var socialflowWindow = document.getElementById("socialflow_window");
	var stickersWindow = document.getElementById("stickers_window");

	var insituIframe = document.getElementById("INSITU_IFRAME");
	var dunbarIframe = document.getElementById("INSITU_DUNBAR_IFRAME");
	var socialflowIframe = document.getElementById("SOCIALFLOW_IFRAME");
	var stickersIframe = document.getElementById("STICKERS_IFRAME");
	
	if (windowNumber == 1)
	{
		var iframeCurrentUrl = insituIframe.src;
        if (iframeCurrentUrl != null) {
            var currentPage = iframeCurrentUrl.substring(iframeCurrentUrl.lastIndexOf('/')+1, iframeCurrentUrl.length);

            // Remove query string
            var queryStringLoc = currentPage.lastIndexOf('?');
            var currentPageCleaned = currentPage;
            if (queryStringLoc != -1) {
               currentPageCleaned = currentPage.substring(0, queryStringLoc);
            }
            queryStringLoc = url.lastIndexOf('?');
            var urlCleaned = url;
            if (queryStringLoc != -1) {
               urlCleaned = url.substring(0, queryStringLoc);
            }
            
            if (currentPageCleaned != urlCleaned) { 
            	insituIframe.src = url;
            }
        }

		dunbarWindow.style.display = "none";
		socialflowWindow.style.display = "none";
		stickersWindow.style.display = "none";
		insituWindow.style.display = "block";
	}
	else if (windowNumber == 2)
	{
		var iframeCurrentUrl = dunbarIframe.src;
		if (iframeCurrentUrl == null || iframeCurrentUrl == "#") {
			dunbarIframe.src = url;
		}
		else {
			var currentPage = iframeCurrentUrl.substring(iframeCurrentUrl.lastIndexOf('/')+1, iframeCurrentUrl.length);
			if (currentPage == "blank.html")
				dunbarIframe.src = url;
		}
		
		insituWindow.style.display = "none";
        socialflowWindow.style.display = "none";
        stickersWindow.style.display = "none";
        dunbarWindow.style.display = "block";
	}
	else if (windowNumber == 3)
	{
	    var shouldRefreshSocialFlow = $('div.globalUIStateMetadata input#refreshSocialFlow').val();
	    
	    if (shouldRefreshSocialFlow == "true") {
	    	var iframeCurrentUrl = socialflowIframe.src;
	        if (iframeCurrentUrl != null) {
	           var currentPage = iframeCurrentUrl.substring(iframeCurrentUrl.lastIndexOf('/')+1, iframeCurrentUrl.length);

	           // Rand factor attached after *.jsp?randFactor to avoid browser caching the page
	           var randFactorLoc = currentPage.lastIndexOf('?');
	           var currentPageMinusRandFactor = currentPage;
	           if (randFactorLoc != -1) {
	              currentPageMinusRandFactor = currentPage.substring(0, randFactorLoc);
	           }
	           randFactorLoc = url.lastIndexOf('?');
	           var urlMinusRandFactor = url;
	           if (randFactorLoc != -1) {
	        	  urlMinusRandFactor = url.substring(0, randFactorLoc);
	           }
	           
	           if (currentPageMinusRandFactor != urlMinusRandFactor) { 
	        	   socialflowIframe.src = url;
	           }
	           else {
		           // do iframe reload
		           //socialflowIframe.src = url;
	        	   document.getElementById('SOCIALFLOW_IFRAME').contentWindow.location.reload(true);
	        	   // parent.frames[FrameID].window.location.reload(); 
	               // document.getElementById(FrameID).contentWindow.location.reload(true);
	           }
	        }
	        else {
	           socialflowIframe.src = url;
	        }

	        $('div.globalUIStateMetadata input#refreshSocialFlow').val("false");
	    }
		
		insituWindow.style.display = "none";
		dunbarWindow.style.display = "none";
		stickersWindow.style.display = "none";
        socialflowWindow.style.display = "block";
    }
	else if (windowNumber == 4)
	{
	    var shouldRefreshStickers = $('div.globalUIStateMetadata input#refreshStickers').val();
        
        if (shouldRefreshStickers == "true") {
            var iframeCurrentUrl = stickersIframe.src;
            if (iframeCurrentUrl != null) {
               var currentPage = iframeCurrentUrl.substring(iframeCurrentUrl.lastIndexOf('/')+1, iframeCurrentUrl.length);
               if (currentPage != url) { 
                   stickersIframe.src = url;
               }
               else {
                   // do iframe reload
                   document.getElementById('STICKERS_IFRAME').contentWindow.location.reload(true);
                   // parent.frames[FrameID].window.location.reload(); 
                   // document.getElementById(FrameID).contentWindow.location.reload(true);
               }
            }
            else {
               stickersIframe.src = url;
            }

            $('div.globalUIStateMetadata input#refreshStickers').val("false");
        }
        
        insituWindow.style.display = "none";
        dunbarWindow.style.display = "none";
        socialflowWindow.style.display = "none";
        stickersWindow.style.display = "block";
    }
    
}

function setDunbarHiddenIframe(url)
{
	var dunbarWindow = document.getElementById("dunbar_window");
	var dunbarIframe = document.getElementById("INSITU_DUNBAR_IFRAME");

	var iframeCurrentUrl = dunbarIframe.src;
	dunbarIframe.src = url;

	/*
    if (iframeCurrentUrl == null || iframeCurrentUrl == "#") {
        dunbarIframe.src = url;
    }
    else {
        var currentPage = iframeCurrentUrl.substring(iframeCurrentUrl.lastIndexOf('/')+1, iframeCurrentUrl.length);
        if (currentPage == "blank.html")
            dunbarIframe.src = url;
    }
    */
    
    dunbarWindow.style.display = "none";
}

</script>
<link type="text/css" rel="stylesheet" href="facebox/faceUIBody.css">
</head>
<body>

<!-- Facebook JS SDK -->
<div id="fb-root"></div>
<script>
  window.fbAsyncInit = function() {
    FB.init({appId: '<%=FBAppSettings.appID%>', 
             status: true, 
             cookie: true,
             xfbml: true});

    // Code to run after FB JS SDK is loaded
    
    // Set the FB Iframe window size
    //FB.Canvas.setAutoResize();
    FB.Canvas.setSize({ width: 760, height: 608 });
  };
  (function() {
    var e = document.createElement('script'); e.async = true;
    e.src = document.location.protocol +
      '//connect.facebook.net/en_US/all.js';
    document.getElementById('fb-root').appendChild(e);
  }());
</script>


<!-- Global UI state of web app -->
<div class="globalUIStateMetadata" style="display: none;">
    <input type="hidden" id="refreshSocialFlow" name="refreshSocialFlow" value="true" />
    <input type="hidden" id="refreshStickers" name="refreshStickers" value="true" />
</div>
<div id="content" class="clearfix">
<div class="UIStandardFrame_Container clearfix">
<div class="UIStandardFrame_Content" style="width: 740px;">
<div class="UIDashboardHeader_Container clearfix clearfloat">
<div class="UIDashboardHeader_TitleBar">
<div class="UIDashboardHeader_Icon UIDashboardHeader_Photos_Icon">
<!-- <img src="http://static.ak.fbcdn.net/rsrc.php/z12E0/hash/8q2anwu7.gif" class="UIDashboardHeader_IconImage spritemap_icons" /> -->
</div>
<h2 class="UIDashboardHeader_Title"><a id="sfMainLink" style="color:#000000; text-decoration: none;" onmousedown='changeInSituTabSelected("fbdata"); changeToWindow(1, "<%=mainURL%>");' href="javascript:;"><%=FBAppSettings.appNameDisplay%></a></h2>
<div class="UIDashboardHeader_Actions">
   <%
   	if (FBAppSettings.ENABLE_DEV_MODE) {
   %>
   <a onmousedown='changeInSituTabSelected("butler"); changeToWindow(1, "<%=configURL%>");' href="javascript:;">Configuration</a><!-- maybe run JS to save the url of current page being viewed in sub-iframe -->
   <%
   	}
   %>
   <span class="pipe">|</span>
   <a id="socialflowsHelp" href="javascript:;">Help</a>
</div>
</div>
</div>
<%
	String tabsClass = "tabs clearfix clearfloat",
           tabsStyle = "";
    //if (FBAppSettings.ENABLE_ONLYPHOTOS_MODE) {
    	tabsClass += " hidden";
    	tabsStyle = "margin-top:1px;";
    //}
%>
<div class="clearfloat">
    <div class="<%=tabsClass%>" style="<%=tabsStyle%>">
    <div class="left_tabs">
        <ul id="toggle_tabs_unused" class="toggle_tabs">
        <li class="<%=butlerTabCss%>"><a onmousedown='changeInSituTabSelected("butler"); changeToWindow(1, "<%=configURL%>");' id="toggle_butler" class="<%=butlerLinkCss%>" href="javascript:;">Initialize Your Butler</a></li><li class="<%=fbdataTabCss%>"><a onmousedown='changeInSituTabSelected("fbdata"); changeToWindow(1, "<%=mainURL%>");' class="<%=fbdataLinkCss%>" id="toggle_fbdata" href="javascript:;">Figure Out</a></li><li class="<%=emailTabCss%>"><a onmousedown='changeInSituTabSelected("dunbar"); changeToWindow(2, "museIntegration.jsp");' id="toggle_dunbar" class="<%=emailTabCss%>" href="javascript:;">Analyze</a></li><li><a onmousedown='changeInSituTabSelected("socialflow"); changeToWindow(3, "<%=socialflowsURL%>");' id="toggle_socialflow" href="javascript:;">Explore</a></li><li class="last"><a onmousedown='changeInSituTabSelected("finish"); changeToWindow(1, "<%=finishURL%>");' id="toggle_finish" href="javascript:;">Clear</a></li><li class="hidden"><a onmousedown='changeInSituTabSelected("stickers"); changeToWindow(4, "<%=stickersURL%>");' id="toggle_stickers" class="hidden" style="display: none;" href="javascript:;">Stickers</a></li><li class="hidden"><a onmousedown='changeInSituTabSelected("cliques"); changeToWindow(1, "<%=cliquesMapURL%>");' id="toggle_cliques" class="hidden" href="javascript:;">Cliques Map</a></li><li class="hidden"><a id="toggle_search" class="hidden" href="javascript:;">Search Results</a></li></ul>
    </div>
    </div>
    <div class="bar summary_bar clearfix" id="app_bar" style="">
        <div class="summary">Let us show you your closest circles of friends!</div>
    </div>
    <div class="content_container" id="insitu_window">
        <iframe name="INSITU_WINDOW" id="INSITU_IFRAME" src="<%=mainURL%>" width="730" height="550" frameborder=0 scrolling="auto"> <!-- style="overflow-x: hidden;" scrolling="no" -->
        <p>Your browser does not support iframes.</p>
        </iframe>
    </div>
    <div class="content_container" id="dunbar_window" style="display: none;">
        <iframe name="INSITU_DUNBAR_WINDOW" id="INSITU_DUNBAR_IFRAME" src ="blank.html" width="730" height="550" frameborder=0 scrolling="auto"> <!-- style="overflow-x: hidden;" scrolling="no" -->
        <p>Your browser does not support iframes.</p>
        </iframe>
    </div>
    <div class="content_container" id="socialflow_window" style="display: none;">
        <iframe name="SOCIALFLOW_WINDOW" id="SOCIALFLOW_IFRAME" src ="blank.html" width="730" height="550" frameborder=0 scrolling="auto">
        <p>Your browser does not support iframes.</p>
        </iframe>
    </div>
    <div class="content_container" id="stickers_window" style="display: none;">
        <iframe name="STICKERS_WINDOW" id="STICKERS_IFRAME" src ="#" width="730" height="550" frameborder=0 scrolling="auto">
        <p>Your browser does not support iframes.</p>
        </iframe>
    </div>
<%
	if (FBAppSettings.ENABLE_INSIDIOUS_MODE) {
%>
    <div id="publishToFBFeed_window" style="display: none;">
        <iframe name="PUBLISHFBFEED_WINDOW" id="PUBLISHFBFEED_IFRAME" src ="<%=publishToFBFeedURL%>" width="730" height="550" frameborder=0 scrolling="auto">
        <p>Your browser does not support iframes.</p>
        </iframe>
    </div>   <%
    }


%>
</div>
</div></div></div>

<div id="dropmenu_container"></div>
</body>
</html>