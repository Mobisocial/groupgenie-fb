<%@page import="org.json.*"%>
<%@page language="java" import="edu.stanford.socialflows.util.StatusProvider"%>
<%@page language="java" import="edu.stanford.socialflows.util.StatusProviderInstance"%>
<%@page language="java" import="edu.stanford.socialflows.settings.FBAppSettings"%>
<%
	session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
    session.removeAttribute(FBAppSettings.GMAIL_AUTH_OBJ);

	// update status if needed
	/*
    StatusProviderInstance statusUpdate = new StatusProviderInstance("Updating...");
    session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, statusUpdate);
    */
    
    int elapsedTime = 0, timeoutLimit = 60000;
    JSONObject gmailAuthObject = null;
    while (gmailAuthObject == null) {
    	gmailAuthObject = (JSONObject)session.getAttribute(FBAppSettings.GMAIL_AUTH_OBJ);
    	if (gmailAuthObject != null)
    		break;
    	
    	if (elapsedTime >= timeoutLimit) {
            System.out.println(request.getServletPath()+": Reached timeout limit...");
            break;
        }
    	System.out.println(request.getServletPath()+": GMAIL_AUTH_OBJ still null, going back to sleep...");
    	Thread.sleep(2000); // sleep for 2 seconds
        elapsedTime += 2000;
    }
    
    if (gmailAuthObject != null) {
    	System.out.println(request.getServletPath()+": Obtained GMAIL_AUTH_OBJ !!!");
        System.out.println(request.getServletPath()+": GMAIL_AUTH_OBJ is ");
        System.out.println(gmailAuthObject.toString()+" \n");
        
        out.println(gmailAuthObject.toString());
        if (!gmailAuthObject.optBoolean("success", false))
            session.removeAttribute(FBAppSettings.FB_AUTH_OBJ); // remove negative auth object
    }
    else {
    	gmailAuthObject = new JSONObject();
    	gmailAuthObject.put("success", false);
    	gmailAuthObject.put("message", "Without an authorization from you, no groups were saved to your Gmail account.");
        out.println(gmailAuthObject.toString());
    }
%>