<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="javax.crypto.Mac"%>
<%@page import="javax.crypto.spec.SecretKeySpec"%>
<%@page import="org.json.*"%>
<%@page import="org.apache.commons.codec.binary.Base64"%>
<%@page import="com.restfb.exception.FacebookException"%>
<%@page import="edu.stanford.prpl.insitu.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@page import="edu.stanford.socialflows.db.DBConnectionManager"%>
<%@page import="edu.stanford.prpl.insitu.util.PrPlConnectionManager"%>
<%@page import="edu.stanford.socialflows.settings.FBAppSettings"%>
<%@page import="edu.stanford.socialflows.contacts.*"%>
<%
	// For IE browsers: P3P (Platform for Privacy Preferences Project) ‘compact privacy policy’
      response.addHeader("P3P","CP='IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT'");

      /* Get FB credentials for standalone SocialFlows website */
      
      // request.getContextPath() => /InSituNewPrPl
      // request.getRequestURI() => /InSituNewPrPl/
      // request.getRequestURL() => http://localhost:28080/InSituNewPrPl/
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

      // Redirect page for FB "open graph"
      StringBuffer origURL = request.getRequestURL();
      System.out.println(request.getServletPath()+": Request URL = "+origURL);
      String refererPage = request.getHeader("Referer");
      System.out.println(request.getServletPath()+": Referring URL = "+refererPage);
      
      String errorReason = request.getParameter("error_reason");
      if (errorReason != null) {
          out.println("FB Open Graph access not authorized: "+errorReason);
          return;
      }
      
      String accessToken = null;
      String ogLoginPage = "https://graph.facebook.com/oauth/authorize?"+
                             "client_id="+appID+
                             "&redirect_uri="+URLEncoder.encode(origURL.toString(), "UTF-8")+
                             "&scope=user_photos,user_photo_video_tags,friends_photo_video_tags,friend_photos,email"; // offline_access,read_mailbox
      String code = request.getParameter("code");
      if (code == null)
      {
%>
          <script type="text/javascript">top.location.href="<%=ogLoginPage%>";</script>
          <%
          	}
                else
                {
                    String requestToken 
                    = "https://graph.facebook.com/oauth/access_token?"+
                          "client_id="+appID+
                          "&redirect_uri="+URLEncoder.encode(origURL.toString(), "UTF-8")+
                          "&client_secret="+appSecret+
                          "&code="+code;
                    URL requestTokenResponse = new URL(requestToken);
                    InputStream is = requestTokenResponse.openStream();
                    BufferedReader in = new BufferedReader(
                                            new InputStreamReader(is)
                                        );
                    
                    // Read result from URL/REST request for token
                    StringBuilder resultBuffer = new StringBuilder(1000);
                    char[] buf = new char[1024];
                    int numRead=0;
                    while((numRead=in.read(buf)) != -1)
                    {
                        resultBuffer.append(buf, 0, numRead);
                    }
                    if (in != null) in.close();
                    if (is != null) is.close();
                    
                    accessToken = resultBuffer.toString().trim();
                    System.out.println("Obtained FB access token: "+accessToken);
                }
                
                
                // Create FB Service
                FBService fbs = (FBService)session.getAttribute(FBAppSettings.FACEBOOK_SERVICE);
                if (accessToken != null)
                {
              	  try {
                        System.out.println("Creating new FB Service using OAuth access token ("+accessToken+")...");
                        fbs = new FBService(accessToken);
                        session.setAttribute(FBAppSettings.FACEBOOK_SERVICE, fbs);
                        
                    }
                    catch (FacebookException fe) {
                        System.out.println("FBException occured while initiating a new FB Service: "+fe.getMessage());
                        fe.printStackTrace();
                        session.removeAttribute(FBAppSettings.FACEBOOK_SERVICE);
          %>
              <script type="text/javascript">top.location.href="<%=ogLoginPage%>";</script>
              <%
              return;
          }
          
          String indexURL = response.encodeURL("index.jsp");
          %>
          <script type="text/javascript">top.location.href="<%=indexURL%>";</script>
          <%
      }
      else
      {
          %>
          <script type="text/javascript">top.location.href="<%=ogLoginPage%>";</script>
          <%
      }

%>