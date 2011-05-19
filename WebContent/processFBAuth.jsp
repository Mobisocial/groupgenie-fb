<%@page import="org.json.*"%>
<%@page language="java" import="edu.stanford.socialflows.settings.FBAppSettings"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	String fb_postformid = request.getParameter(FBAppSettings.FB_POSTFORMID);
    String fb_dtsg       = request.getParameter(FBAppSettings.FB_DTSG);
    String fb_userid     = request.getParameter("fb_userid");
    String jsonpCallback = request.getParameter("callback");
    JSONObject fbAuthObject = new JSONObject();
    
    if (fb_postformid != null)
    {
        System.out.println("Initial FB post_form_id: "+fb_postformid);
        System.out.println("FB dtsg: "+fb_dtsg);

        fbAuthObject.put("success", true);
        fbAuthObject.put(FBAppSettings.FB_POSTFORMID, fb_postformid);
        fbAuthObject.put(FBAppSettings.FB_DTSG, fb_dtsg);
    }
    else
    {
    	fbAuthObject.put("success", false);
    	fbAuthObject.put("message", "We could not access your Facebook account in order to export your social groups.");
    }
    
    
    session.setAttribute(FBAppSettings.FB_AUTH_OBJ, fbAuthObject);
    System.out.println(request.getServletPath()+": Setting FB_AUTH_OBJ...");
    
    out.println(jsonpCallback+"("+fbAuthObject.toString()+")");
%>