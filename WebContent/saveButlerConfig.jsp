<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="edu.stanford.prpl.api.PRPLAppClient"%>
<%@page import="edu.stanford.prpl.impl.client.app.PRPLAppClientImpl"%>
<%@page import="edu.stanford.prpl.insitu.*"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@include file="sessionGetButler.jsp"%>
<%
	// Clear off any stored object that can provide its status update
      session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);

      String pcbConfigData = request.getParameter("input");
      if (pcbConfigData == null || pcbConfigData.isEmpty())
      {
          // Do nothing
          System.out.println("Error: Missing PrPl Butler Configuration to switch to");
          out.println("Error: Missing PrPl Butler Configuration to switch to");
          return;
      }
      
      // Get new PCB ID
      JSONObject pcbConfig = new JSONObject(pcbConfigData);
      String newPrplUserID = pcbConfig.getString("pcbID");
      if (newPrplUserID == null || newPrplUserID.isEmpty())
      {
    	  // Do nothing
    	  System.out.println("Error: Missing PrPl Butler ID to switch to");
          out.println("Error: Missing PrPl Butler ID to switch to");
          return;
      }

      // Get new PCB Password
      String newPrplUserPwd = pcbConfig.getString("pcbPwd");
      if (newPrplUserPwd == null || newPrplUserPwd.isEmpty())
      {
    	  newPrplUserPwd = "";
      }
      // System.out.println("saveButlerConfig.jsp: pwd="+newPrplUserPwd);
      
      // Get new PCB PrPl Hosting type
      String newPrplHost = pcbConfig.getString("pcbHost");
      if (newPrplHost == null || newPrplHost.isEmpty())
      {
    	  newPrplHost = "personal";
      }
      
      // Start the PCB with the user-specified ID.
      // If it does not exists, this PCB will be created
      // Create initial PrPlConnectionManager if needed
      if (prplConnManager == null) 
      {
          prplConnManager = new PrPlConnectionManager();
          prplConnManager.setCurrentUserName(fbMyInfo.getName());
          prplConnManager.setPrplKeysFilePath(application.getRealPath("keys/prpl_app.keystore"));
          if (FBAppSettings.ENABLE_PRPL) {
             // Determine the PrPl user id
             String prplUserID = "fbid"+fbMyInfo.getFBUid();
             prplConnManager.setCurrentUserID(prplUserID);
             prplConnManager.setCurrentUserPwd("insitulicious"+prplUserID);
          }
          session.setAttribute(FBAppSettings.PRPL_SERVICE_MANAGER, prplConnManager);
      }
      
      
      session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, prplConnManager);
      
      try
      {
    	  prplConnManager.switchButler(newPrplHost, newPrplUserID, newPrplUserPwd);
      }
      catch (PrPlHostException phe)
      {
           // ERROR: PRINT OUT ERROR MESSAGE
          System.out.println("The following PrPlHost error occured: "+phe.getMessage());
          phe.printStackTrace();
          out.println("The following PrPlHost error occured: "+phe.getMessage());
          session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
          return;
      }
      catch (Exception e)
      {
           // ERROR: PRINT OUT ERROR MESSAGE
          System.out.println("An exception occured: "+e.getMessage());
          e.printStackTrace();
          out.println("An exception occured: "+e.getMessage());
          session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
          return;
      }
      
      session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
      String finalStatus = prplConnManager.finalStatusMessage;
      
      System.out.println("FINAL STATUS: "+finalStatus);
      
      out.println(finalStatus);
%>