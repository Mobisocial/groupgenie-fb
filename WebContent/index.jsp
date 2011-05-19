<%// Prevents browser-side caching of page results
response.setHeader("Cache-Control","no-cache");
response.setHeader("Pragma","no-cache");
response.setDateHeader ("Expires", 0);%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="org.json.*"%>
<%@page import="com.restfb.exception.FacebookException"%>
<%@page import="edu.stanford.prpl.insitu.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@page import="edu.stanford.socialflows.db.DBConnectionManager"%>
<%@page import="edu.stanford.socialflows.settings.FBAppSettings"%>
<%@page import="edu.stanford.socialflows.contacts.*"%>
<%
	  // For IE browsers: P3P (Platform for Privacy Preferences Project) ‘compact privacy policy’ 
      response.addHeader("P3P","CP='IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT'");

      /* Get FB credentials for standalone SocialFlows website */
      
      // request.getContextPath() => /SocialFlows
      // request.getRequestURI() => /SocialFlows/
      // request.getRequestURL() => http://localhost:28080/SocialFlows/
      // request.getServletPath() => /index.jsp
      /*
           <!-- 
           window.onload = function(){
                if (top.location.href != self.location.href)
                top.location.href = ‘=loginPage’;
           };
           -->
      */

      String appSecret = FBAppSettings.appSecret;
      String appID     = FBAppSettings.appID;
      boolean isIframeApp = false;
      String newOAuthToken = null;
      String newUserId = null;
      
      // Get old FB session object (if it exists) and replace if needed
      JSONObject currentfbSessionObj 
      = (JSONObject)session.getAttribute(FBAppSettings.FACEBOOK_SESSION_OBJ);
      
      // FB IFrame App signed param using OAuth 2
      String newCanvasSigned = request.getParameter(FBService.FB_CANVAS_SIGNED_PARAM);
      System.out.println(request.getServletPath()+": Canvas Signed Request param is "+newCanvasSigned);
      if (newCanvasSigned == null)
      {
    	  session.invalidate(); //invalidate the current session
          session = request.getSession(true); //request a new session
          // Set session to last 60 minutes before inactivity timeout
          session.setMaxInactiveInterval(FBAppSettings.APP_PERSISTENCE_TIME);
      }
      else
      {
    	  // Accessed within FB as FB Iframe App
          JSONObject newfbSessionObj = FBService.processFBSignedRequest(newCanvasSigned);
    	  if (newfbSessionObj != null) {
    		  newUserId     = newfbSessionObj.optString("user_id", null);
    		  newOAuthToken = newfbSessionObj.optString("oauth_token", null);
    		  if (newUserId == null || newOAuthToken == null)
    			  isIframeApp = true;
    	  }
    	  
    	  // Determine whether it is a different user
    	  if (currentfbSessionObj != null)
    	  {
              String currentUserId = currentfbSessionObj.optString("user_id", null);
              if (newUserId == null || !newUserId.equals(currentUserId)) {
                  System.out.println(request.getServletPath()+": Invalidating old session ("+currentfbSessionObj+")");
                  
                  // Invalidate previous session
                  session.invalidate();
                  session = request.getSession(true); //request a new session
                  // Set session to last 60 minutes before inactivity timeout
                  session.setMaxInactiveInterval(FBAppSettings.APP_PERSISTENCE_TIME);
                  
                  if (newfbSessionObj != null && newUserId != null) {
                	  session.setAttribute(FBAppSettings.FACEBOOK_SESSION_OBJ, newfbSessionObj);
                	  currentfbSessionObj = newfbSessionObj;
                  }
              }
              else
              {
            	  // Need to update OAuthToken access string
            	  String currentOAuthToken = currentfbSessionObj.optString("oauth_token", null);
            	  if (newOAuthToken != null && !newOAuthToken.equals(currentOAuthToken)) {
            		  session.removeAttribute(FBAppSettings.FACEBOOK_SERVICE);
                      session.setAttribute(FBAppSettings.FACEBOOK_SESSION_OBJ, newfbSessionObj);
                      currentfbSessionObj = newfbSessionObj;
            	  }
              }
          }
    	  else
    	  {
    		  // For FB Iframe app access
    		  if (newOAuthToken != null) {
    			  session.setAttribute(FBAppSettings.FACEBOOK_SESSION_OBJ, newfbSessionObj);
                  currentfbSessionObj = newfbSessionObj;
    		  }
    	  }
      }



      // Check if in 2nd stage of FB OAuth process
      String errorReason = request.getParameter("error_reason");
      if (errorReason != null) {
    	  String errorDesc = request.getParameter("error_description");
          out.println("FB Open Graph access not authorized: "+errorReason+" - "+errorDesc);
          return;
      }
      
      // Get "code" string to exchange for FB OAuth token 
      // (this is for stand-alone website access to app)
      String code = request.getParameter("code");
      System.out.println(request.getServletPath()+": FB OAuth code is "+code);
      if (code != null && newOAuthToken == null) 
      {
          // Construct redirect URL
          String redirectURL = response.encodeURL(request.getRequestURL().toString());
          System.out.println(request.getServletPath()+": Final Redirect URL will be "+redirectURL);
          redirectURL = URLEncoder.encode(redirectURL, "UTF-8");

          String requestToken 
          = "https://graph.facebook.com/oauth/access_token?"+
                "client_id="+appID+
                "&redirect_uri="+redirectURL+
                "&client_secret="+appSecret+
                "&code="+code;
          
          try 
          {
        	  URL requestTokenResponse = new URL(requestToken);
              InputStream is = requestTokenResponse.openStream();
              InputStreamReader ir = new InputStreamReader(is);
              BufferedReader in = new BufferedReader(ir);
              
              // Read result from URL/REST request for token
              StringBuilder resultBuffer = new StringBuilder(1000);
              char[] buf = new char[1024];
              int numRead=0;
              while((numRead=in.read(buf)) != -1) {
                  resultBuffer.append(buf, 0, numRead);
              }

              newOAuthToken = resultBuffer.toString().trim();
              if (in != null) in.close();
              if (ir != null) ir.close();
              if (is != null) is.close();
              
              System.out.println("Obtained FB oauth access token: "+newOAuthToken);
              // String is of format
              // access_token=122021667847098|2.YIdx7Hla5NcHEypKuNZtrA__.3600.1282442400-4804827|xE3qWGOzZwU59dpP53d7VmSpJSU.&expires=4206
          }
          catch (IOException ioe)
          {
        	  System.out.println("ERROR occured!!! "+ioe.getMessage());
        	  ioe.printStackTrace();
          }


          // Build new FB session object
          // (this is for stand-alone website access to app)
          JSONObject newfbSessionObj = null;
          if (newOAuthToken != null)
          {
              // Accessed outside FB as a SocialFlows standalone website
              newfbSessionObj = new JSONObject();
              
              String[] tokens = newOAuthToken.split("[\\=\\&]");
              //System.out.println("Splitting "+oauthToken+" into:");
              for (int i = 0; i < tokens.length; i++)
              {
                  //System.out.println("["+i+"] => "+tokens[i]);
                  
                  if (tokens[i].equals("access_token")) {
                      newOAuthToken = tokens[++i];
                      newfbSessionObj.putOpt("oauth_token", newOAuthToken);
                  }
                  else if (tokens[i].equals("expires")) {
                      newfbSessionObj.putOpt("expires", tokens[++i]);
                  }
              }
              
              FBService tempFBService = new FBService(newOAuthToken);
              SocialFlowsContactInfo newUser = tempFBService.getLoggedInUser();
              newfbSessionObj.putOpt("user_id", newUser.getFBUid());
              
              System.out.println("Accessing SocialFlows as a standalone website, produced FB session info: \n"
                                 +newfbSessionObj.toString(2));
          }
          
          
          // Get old FB session object (if it exists) and replace if needed
          // (this is for stand-alone website access to app)
          if (newfbSessionObj != null)
          {
              newUserId = newfbSessionObj.optString("user_id", null);
              if (currentfbSessionObj != null) {
                  // Determine whether it is a different user
                  String currentUserId = currentfbSessionObj.optString("user_id", null);
                  if (newUserId == null || !newUserId.equals(currentUserId)) {
                      System.out.println(request.getServletPath()+": Invalidating old session ("+currentfbSessionObj+")");
                      
                      currentfbSessionObj = null;
                      session.invalidate(); //invalidate the current session
                      session = request.getSession(true); //request a new session
                      // Set session to last 60 minutes before inactivity timeout
                      session.setMaxInactiveInterval(FBAppSettings.APP_PERSISTENCE_TIME);
                  }
              }

              if (newUserId != null)
              {
                  session.removeAttribute(FBAppSettings.FACEBOOK_SERVICE);
                  session.setAttribute(FBAppSettings.FACEBOOK_SESSION_OBJ, newfbSessionObj);
                  currentfbSessionObj = newfbSessionObj;

                  // Want to get silly FB Iframe
                  System.out.println(request.getServletPath()+": Validating new session ("+newfbSessionObj+")");
                  System.out.println(request.getServletPath()+": Rerouting to "+FBAppSettings.canvasURL+" to get FB Iframes... \n");
                  %><script type="text/javascript">top.location.href="<%=FBAppSettings.canvasURL%>";</script><%
                  return;
              }
          }
      }



      /*
         Maybe check parent window, parent iframe, check if url is the same
         If url has apps.facebook.com, we know we are in fb apps iframe mode
      */
      if (currentfbSessionObj == null)
      {
          /*
          String refererPage = request.getHeader("Referer");
          System.out.println(request.getServletPath()+": Referring URL = "+refererPage);
          
          // Means this is being accessed from within FB Iframe
          if (refererPage != null && refererPage.contains("facebook.com")) {
              <script type="text/javascript">top.location.href="=loginPage";</script>
              return;
          }
          else {
              String ogLoginProcess = response.encodeURL("index.jsp");
              <script type="text/javascript">top.location.href="=ogLoginProcess";</script>
              return;
          }
          */
    	  
    	  // Construct redirect URL
          String redirectURL = response.encodeURL(request.getRequestURL().toString());
          if (isIframeApp)
        	  redirectURL = FBAppSettings.canvasURL;
          System.out.println(request.getServletPath()+": Redirect URL for FB OG = "+redirectURL);
          redirectURL = URLEncoder.encode(redirectURL, "UTF-8");

          /*
          String ogLoginPage = "https://graph.facebook.com/oauth/authorize?"+
                                 "client_id="+appID+
                                 "&redirect_uri="+redirectURL+
                                 "&scope="+FBAppSettings.FB_DATA_PERMISSIONS;
          */
          String ogLoginPage = "https://www.facebook.com/dialog/oauth?"+
                               "client_id="+appID+
                               "&redirect_uri="+redirectURL+
                               "&scope="+FBAppSettings.FB_DATA_PERMISSIONS;

          System.out.println(request.getServletPath()+": Redirecting to obtain FB OG credentials from "+ogLoginPage);
          %><script type="text/javascript">top.location.href="<%=ogLoginPage%>";</script><%
          return;
      }
      else
      {
    	  // Finally and correctly redirect to mainpage
    	  String mainpage      = response.encodeURL("indexframe.jsp");
    	  String nocookiespage = response.encodeURL("indexframe.jsp?problem=nocookies");
    	  String isIEpage      = response.encodeURL("indexframe.jsp?problem=crappybrowser");
    	  if (!FBAppSettings.ENABLE_CHECKIE_MODE) // Dont check for browser compatibility
    		  isIEpage = mainpage;
    	  //response.sendRedirect(response.encodeRedirectURL("indexframe.jsp"));
    	  %>
    	  <script type="text/javascript" src="common/jscommon.js"></script>
    	  <script type="text/javascript" src="jquery/jquery-1.5.1.min.js"></script>
          <script type="text/javascript">
          function processCookiesCheck(data, textStatus)
          {
        	    if (data.cookiesEnabled) {
            	    if (isInternetExplorerBrowser())
                       self.location.href="<%=isIEpage%>";
            	    else
            	       self.location.href="<%=mainpage%>";
                }
                else {
                    self.location.href="<%=nocookiespage%>";
                }
          }

          // Check if 3rd-party cookies enabled using an ajax GET request
          $.ajax({
                 cache:    false,
                 url:      'enableCookiesCheck.jsp',
                 dataType: 'json',
                 success:  processCookiesCheck,
                 error:    function(XMLHttpRequest, textStatus, errorThrown) { self.location.href="<%=nocookiespage%>"; }
          });
          </script>
          <%
    	  return;
      }
      

%>