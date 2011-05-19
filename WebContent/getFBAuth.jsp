<%@page import="org.json.*"%>
<%@page language="java" import="edu.stanford.socialflows.util.StatusProvider"%>
<%@page language="java" import="edu.stanford.socialflows.util.StatusProviderInstance"%>
<%@page language="java" import="edu.stanford.socialflows.settings.FBAppSettings"%>
<%
	session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);

	// update status if needed
	/*
    StatusProviderInstance statusUpdate = new StatusProviderInstance("Updating...");
    session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, statusUpdate);
    */
    
    int elapsedTime = 0, timeoutLimit = 60000;
    JSONObject fbAuthObject = null;
    while (fbAuthObject == null) {
    	fbAuthObject = (JSONObject)session.getAttribute(FBAppSettings.FB_AUTH_OBJ);
    	if (fbAuthObject != null)
    		break;
    	
    	if (elapsedTime >= timeoutLimit) {
    		System.out.println(request.getServletPath()+": Reached timeout limit...");
    		break;
    	}
    	System.out.println(request.getServletPath()+": FB_AUTH_OBJ still null, going back to sleep...");
    	Thread.sleep(2000); // sleep for 2 seconds
        elapsedTime += 2000;
    }
    
    if (fbAuthObject != null) {
    	System.out.println(request.getServletPath()+": Obtained FB_AUTH_OBJ !!!");
        System.out.println(request.getServletPath()+": FB_AUTH_OBJ is ");
        System.out.println(fbAuthObject.toString()+" \n");
        
        out.println(fbAuthObject.toString());
        if (!fbAuthObject.optBoolean("success", false))
        	session.removeAttribute(FBAppSettings.FB_AUTH_OBJ); // remove negative auth object
    }
    else {
    	fbAuthObject = new JSONObject();
    	fbAuthObject.put("success", false);
    	fbAuthObject.put("message", "Without a response from you, no groups were saved to your Facebook account.");
    	out.println(fbAuthObject.toString());
    }
%>