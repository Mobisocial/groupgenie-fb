<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.socialflows.settings.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@page import="edu.stanford.socialflows.db.DBConnectionManager"%>
<%@page import="edu.stanford.socialflows.log.SocialFlowsLogger"%>
<%@page import="edu.stanford.socialflows.contacts.SocialFlowsContactInfo"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	  // For IE browsers: P3P (Platform for Privacy Preferences Project) ‘compact privacy policy’
      response.addHeader("P3P","CP='IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT'");

      // Clear off any stored object that can provide its status update
      session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);

      String loginPage 
      = "http://www.facebook.com/login.php?next="
    	+URLEncoder.encode(FBAppSettings.canvasURL, "UTF-8")+"&canvas=true";

      // Make sure session is not yet expired
      JSONObject currentfbSessionObj 
      = (JSONObject)session.getAttribute(FBAppSettings.FACEBOOK_SESSION_OBJ);
      if (currentfbSessionObj == null)
      {
          // ERROR: PRINT OUT THAT SESSION HAS EXPIRED
          System.out.println("Error: FB Session Obj missing. Session could have expired! Reaccessing the application again.");
          %><script type="text/javascript">top.location.href="<%=loginPage%>";</script><%
          return;
      }

      // Get FB Service
      FBService fbs = (FBService)session.getAttribute(FBAppSettings.FACEBOOK_SERVICE);
      if (fbs == null) {
          %><script type="text/javascript">top.location.href="<%=loginPage%>";</script><%
          return;
      }
                
      DBConnectionManager dbconnManager
      = (DBConnectionManager)session.getAttribute(FBAppSettings.DB_SERVICE_MANAGER);
      if (dbconnManager == null) {
          %><script type="text/javascript">top.location.href="<%=loginPage%>";</script><%
          return;
      }

      // Get SocialFlows user information
      SocialFlowsContactInfo fbMyInfo = dbconnManager.getCurrentUser();
      String sfUserID   = dbconnManager.getCurrentUserLogin();
      String sfUserName = dbconnManager.getCurrentUserName();
      
      // Get or create current SocialFlows session info
      JSONObject sfSessionObj 
      = (JSONObject)session.getAttribute(FBAppSettings.SOCIALFLOWS_SESSION_INFO);
      if (sfSessionObj == null) {
          sfSessionObj = new JSONObject();
          /*
          String sfSessionId = new Date().toString();
          sfSessionObj.put("sfSessionId", "sfSession_"+sfSessionId.replaceAll("[\\s:]", "_"));
          int sfTopologyId = (new Random()).nextInt();
          if (sfTopologyId < 0)
              sfTopologyId = -sfTopologyId;
          sfSessionObj.put("currentSfTopologyId", sfTopologyId);
          */
          session.setAttribute(FBAppSettings.SOCIALFLOWS_SESSION_INFO, sfSessionObj);
      }

      // Get Logger utility
      SocialFlowsLogger sfLogger 
      = (SocialFlowsLogger)session.getAttribute(FBAppSettings.SOCIALFLOWS_LOGGER);
      if (sfLogger == null) {
        SocialFlowsLogger.initialize(String.valueOf(fbMyInfo.getSfUserId()), dbconnManager);
          sfLogger = SocialFlowsLogger.getInstance();
          sfSessionObj.put("sfSessionId", sfLogger.getSessionID());
          session.setAttribute(FBAppSettings.SOCIALFLOWS_LOGGER, sfLogger);
      }
                
      // Figure out the URL minus the current page
      String originalURL = request.getRequestURL().toString();
      originalURL = originalURL.substring(0, originalURL.lastIndexOf("/")+1);
      if (originalURL.charAt(originalURL.length()-1) != '/')
          originalURL += "/";
      
%>