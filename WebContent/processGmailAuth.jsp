<%@page import="org.json.*"%>
<%@page import="com.google.gdata.client.*"%>
<%@page import="com.google.gdata.client.http.*"%>
<%@page import="com.google.gdata.client.contacts.*"%>
<%@page import="com.google.gdata.data.*"%>
<%@page import="com.google.gdata.data.contacts.*"%>
<%@page import="com.google.gdata.data.extensions.*"%>
<%@page import="com.google.gdata.util.*"%>
<%@page language="java" import="edu.stanford.socialflows.settings.FBAppSettings"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	String token = request.getParameter("token");
    //String token = AuthSubUtil.getTokenFromReply(request.getRequestURL().toString());
    JSONObject gmailAuthObject = new JSONObject();
    
    try
    {
        if (token != null)
        {
            String sessionToken = AuthSubUtil.exchangeForSessionToken(token, null);
            
            System.out.println("Initial Google token: "+token);
            System.out.println("Session token: "+sessionToken);

            ContactsService gmailService = new ContactsService("socialflows");
            gmailService.setAuthSubToken(sessionToken, null);
            session.setAttribute(FBAppSettings.GMAIL_SERVICE, gmailService);
            
            gmailAuthObject.put("success", true);
            gmailAuthObject.put(FBAppSettings.GMAIL_TOKEN, sessionToken);
        }
        else
        {
            gmailAuthObject.put("success", false);
            gmailAuthObject.put("message", "Error: User did not authorize access to Gmail and Google Contacts");
        }
    }
    catch (Exception e)
    {
    	System.out.println("Exception when getting Google Contacts: "+e.getMessage());
    	e.printStackTrace();
    	
    	gmailAuthObject.put("success", false);
        gmailAuthObject.put("message", "Error due to the following exception: "+e.getMessage());
    }
    

    session.setAttribute(FBAppSettings.GMAIL_AUTH_OBJ, gmailAuthObject);
    System.out.println(request.getServletPath()+": Setting GMAIL_AUTH_OBJ...");
%>
<script language="javascript" type="text/javascript">
    // close current window once done with gmail auth process
    //window.opener='x';
    //window.open(",'_parent',");
    self.close();
</script>
