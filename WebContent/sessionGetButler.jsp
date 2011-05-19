<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="edu.stanford.prpl.api.PRPLAppClient"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@include file="sessionCheck.jsp"%>
<%
	// Get connection to PrPl
      // Get PrPlConnectionManager
      PrPlConnectionManager prplConnManager 
      = (PrPlConnectionManager)session.getAttribute(FBAppSettings.PRPL_SERVICE_MANAGER);
      //session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, prplConnManager);

      PRPLAppClient prplService_ = null;
      if (prplConnManager != null)
      {
    	  try
          {
              // Starts a user's PCB if it has not already been started
              prplService_ = prplConnManager.getButlerClient();
          }
          catch (PrPlHostException phe)
          {
              // ERROR: PRINT OUT ERROR MESSAGE
              System.out.println("The following PrPlHost error occured: "+phe.getMessage());
              out.println("Attempted to connect to your Personal Cloud Butler when the following PrPlHost error occured: "+phe.getMessage());
              session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
              return;
          }
          catch (Exception e)
          {
              // ERROR: PRINT OUT ERROR MESSAGE
              System.out.println("An exception occured: "+e.getMessage());
              out.println("Attempted to connect to your Personal Cloud Butler when the following error occured: "+e.getMessage());
              session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
              return;
          }
          if (prplService_ == null) {
              String finalStatus = prplConnManager.finalStatusMessage;
              out.println(finalStatus);
              session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
              return;
          }
      }

      session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
%>