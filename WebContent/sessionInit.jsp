<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="java.net.*"%>
<%@page import="org.json.*"%>
<%@page import="com.restfb.exception.FacebookException"%>
<%@page import="edu.stanford.socialflows.log.*"%>
<%@page import="edu.stanford.socialflows.settings.*"%>
<%@page import="edu.stanford.socialflows.contacts.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@page import="edu.stanford.socialflows.db.DBConnectionManager"%>
<%@page import="edu.stanford.prpl.insitu.util.PrPlConnectionManager"%>
<%
	  // For IE browsers: P3P (Platform for Privacy Preferences Project) ‘compact privacy policy’
      response.addHeader("P3P","CP='IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT'");

      // Redirect page for pure FB IFrame app
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
          %><script type="text/javascript">top.location.href="<%=FBAppSettings.canvasURL%>";</script><%
          return;
      }


      // Get FB Service
      FBService fbs = (FBService)session.getAttribute(FBAppSettings.FACEBOOK_SERVICE);
      if (fbs == null)
      {
    	  String oauthToken = currentfbSessionObj.optString("oauth_token", null);
          if (oauthToken == null || oauthToken.isEmpty())
          {
        	  System.out.println("Error: FB OAuth token missing. Session could have expired! Reaccessing the application again.");
              %><script type="text/javascript">top.location.href="<%=loginPage%>";</script><%
              return;
          }
          else 
          {
              try {
            	  System.out.println("Creating new FB Service using OAuth access token ("+oauthToken+")...");
                  fbs = new FBService(oauthToken);
              }
              catch (FacebookException fe) {
                  System.out.println("FBException occured while initiating a new FB Service: "+fe.getMessage());
                  fe.printStackTrace();
                  session.removeAttribute(FBAppSettings.FACEBOOK_SERVICE);
                  %><script type="text/javascript">top.location.href="<%=loginPage%>";</script><%
                  return;
              }
              session.setAttribute(FBAppSettings.FACEBOOK_SERVICE, fbs);  
          }
      }

      // Get current user's information
      SocialFlowsContactInfo fbMyInfo 
      = (SocialFlowsContactInfo)session.getAttribute(FBAppSettings.MY_FB_INFO);
      if (fbMyInfo == null)
      {
    	  try {
              fbMyInfo = (SocialFlowsContactInfo)fbs.getLoggedInUser();
              if (fbMyInfo == null) {
            	  %><script type="text/javascript">top.location.href="<%=loginPage%>";</script><%
                  return;
              }
          }
    	  catch (FacebookException ex)
          {
    		  System.out.println("FacebookException: "+ex.getMessage());
              ex.printStackTrace();
              session.removeAttribute(FBAppSettings.FACEBOOK_SERVICE);
              %><script type="text/javascript">top.location.href="<%=loginPage%>";</script><%
              return;
          }
          session.setAttribute(FBAppSettings.MY_FB_INFO, fbMyInfo);

          /*
          System.out.println("Obtained your FB profile info: \nName="+fbMyInfo.getName()
                  +"\nFirstName="+fbMyInfo.getFirstName()
                  +"\nLastName="+fbMyInfo.getLastName()
                  +"\nProfilePic="+fbMyInfo.getPicSquare());
          */
      }

      // Create DBConnectionManager if needed
      DBConnectionManager dbconnManager 
      = (DBConnectionManager)session.getAttribute(FBAppSettings.DB_SERVICE_MANAGER);
      if (dbconnManager == null) 
      {
          dbconnManager = new DBConnectionManager(fbMyInfo);
          dbconnManager.setCurrentUserName(fbMyInfo.getName());
          dbconnManager.setCurrentUser(fbMyInfo);
          session.setAttribute(FBAppSettings.DB_SERVICE_MANAGER, dbconnManager);
      }

      // Get SocialFlows user information
      fbMyInfo = dbconnManager.getCurrentUser();
      String sfUserID   = dbconnManager.getCurrentUserLogin();
      String sfUserName = dbconnManager.getCurrentUserName();
      
      // Get or create current SocialFlows session info
      JSONObject sfSessionObj 
      = (JSONObject)session.getAttribute(FBAppSettings.SOCIALFLOWS_SESSION_INFO);
      if (sfSessionObj == null) {
          sfSessionObj = new JSONObject();
          session.setAttribute(FBAppSettings.SOCIALFLOWS_SESSION_INFO, sfSessionObj);
      }

      // Get Logger utility
      SocialFlowsLogger sfLogger 
      = (SocialFlowsLogger)session.getAttribute(FBAppSettings.SOCIALFLOWS_LOGGER);
      if (sfLogger == null) {
        SocialFlowsLogger.initialize(String.valueOf(fbMyInfo.getSfUserId()), 
                                     dbconnManager);
          sfLogger = SocialFlowsLogger.getInstance();
          sfSessionObj.put("sfSessionId", sfLogger.getSessionID());
          session.setAttribute(FBAppSettings.SOCIALFLOWS_LOGGER, sfLogger);
      }


      /*
      // Create initial PrPlConnectionManager if needed
      PrPlConnectionManager connManager 
      = (PrPlConnectionManager)session.getAttribute(FBAppSettings.PRPL_SERVICE_MANAGER);
      if (connManager == null) 
      {
          connManager = new PrPlConnectionManager();
          connManager.setCurrentUserName(fbMyInfo.getName());
          connManager.setPrplKeysFilePath(application.getRealPath("keys/prpl_app.keystore"));
          if (FBAppSettings.AUTO_CONFIG_PRPL) {
              // Determine the PrPl user id
              String prplUserID = "fbid"+fbMyInfo.getUid();
              connManager.setCurrentUserID(prplUserID);
              connManager.setCurrentUserPwd("insitulicious"+prplUserID);
          }
          String prplHost = connManager.getCurrentHostName();
          session.setAttribute(FBAppSettings.PRPL_SERVICE_MANAGER, connManager);
      }
      */
      
      // String filePath = application.getRealPath("DynNaviList.template.xml");
      // out.println("DynNaviList.template.xml => "+filePath);
      // String templateFilePath = application.getRealPath("DynNaviList.template.xml");

      // Record first time current user ever accessed SocialFlows
      Date lastUpdate = dbconnManager.getCurrentUserLastUpdateTime();
      System.out.println(request.getServletPath()+": Last saved time of user: "+lastUpdate);
      if (lastUpdate == null || lastUpdate.getTime() == 0) {
          dbconnManager.setCurrentUserLastUpdateTime();
      }

%>