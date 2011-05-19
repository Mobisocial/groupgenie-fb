<%@page language="java" import="java.io.*"%>
<%@page language="java" import="org.json.*"%>
<%@page language="java" import="edu.stanford.socialflows.settings.FBAppSettings"%>
<%    
    // Check if session object can be accessed through JSP cookie set
    JSONObject currentfbSessionObj 
    = (JSONObject)session.getAttribute(FBAppSettings.FACEBOOK_SESSION_OBJ);
    if (currentfbSessionObj == null) {
    	out.println ("{ \"cookiesEnabled\": false }");
    }
    else {
    	out.println ("{ \"cookiesEnabled\": true }");
    }
    
%>