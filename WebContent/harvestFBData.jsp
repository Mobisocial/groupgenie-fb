<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.socialflows.settings.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@page import="edu.stanford.socialflows.contacts.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@include file="sessionCheck.jsp"%>
<%
	// Initialize Facebook Harvester
      try 
      {
    	  System.out.println("Getting FB Harvester...");
          DBFacebookConnector dbfbh = new DBFacebookConnector("FB", dbconnManager, fbs);
          
          // So that we can get continuous status updates about the harvesting
          session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, dbfbh);
          System.out.println("Extracting FB data...");
          
          // TODO: Maybe don't put friends into database
          dbfbh.processSocialGraph();
          
          // Save for usage in the User Interface to display & manipulate groups
          List<SocialFlowsContactInfo> myFriends = dbfbh.getFriendsFBProfiles();
          session.setAttribute(FBAppSettings.MY_FB_FRIENDS_INFO, myFriends);
          
          /*
          System.out.println("\nYour name is '"+myInfo.getName()+"' with FB id "+myInfo.getUid());
          System.out.println("Your friends are:");
          for (Iterator<User> i = myFriends.iterator(); i.hasNext();) 
          {
              User friend = i.next();
              System.out.println("- "+friend.getName()+" (FB id "+friend.getUid()+") (Pic url: "+friend.getPicSquare()+")");
          }
          */
      }
      /*
      catch(FacebookException ex) 
      {
          System.out.println("FBException: "+ex.getMessage()+"\n");
          ex.printStackTrace(System.out);
          out.println("FBException: "+ex.getMessage()+"\n");
          
          Writer result = new StringWriter();
          final PrintWriter printWriter = new PrintWriter(result);
          ex.printStackTrace(printWriter);
          out.println(result.toString());
          return;
      }
      */
      catch(Exception e) 
      {
          // response.sendError(response.SC_INTERNAL_SERVER_ERROR, "Error while fetching user's facebook data");
          System.out.println("Exception: "+e.getMessage());
          out.println("Exception: "+e.getMessage()+"\n");
          
          Writer result = new StringWriter();
          final PrintWriter printWriter = new PrintWriter(result);
          e.printStackTrace(printWriter);
          out.println(result.toString());
          return;
      }
      
      
      // Need to get a fresh unified list of Person resources (representing Friends/Contacts) after a reharvest
      session.removeAttribute(FBAppSettings.MY_ADDRESS_BOOK);

      out.println("Successfully extracted your Facebook data.");
      session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
%>