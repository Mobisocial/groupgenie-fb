<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="com.restfb.exception.FacebookException"%>
<%@page import="edu.stanford.prpl.api.PRPLAppClient"%>
<%@page import="edu.stanford.prpl.insitu.data.PrPlPhotoBook"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.socialflows.data.*"%>
<%@page import="edu.stanford.socialflows.settings.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@include file="sessionGetButler.jsp"%>
<%
	// Initialize Facebook Harvester
      try 
      {
    	  System.out.println("Getting FB Harvester...");
          DBFacebookConnector fbh = new DBFacebookConnector("FB", dbconnManager, fbs);
          
          // So that we can get continuous status updates about the harvesting
          session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, fbh);
          System.out.println("Harvesting FB photo albums...");
          
          String selectedAlbumsJsonData = request.getParameter("input");
          if (selectedAlbumsJsonData == null || selectedAlbumsJsonData.trim().length() <= 0) {
        	  fbh.processPhotoAlbums();
          }
          else {
        	  JSONObject selectedAlbumsData = new JSONObject(selectedAlbumsJsonData);
        	  JSONArray selectedAlbumsArray = selectedAlbumsData.getJSONArray("selectedAlbums");
        	  List<String> selectedAlbumsIds = new Vector<String>();
        	  for (int i = 0; i < selectedAlbumsArray.length(); i++) {
        		  selectedAlbumsIds.add(selectedAlbumsArray.getString(i));
              }
        	  
        	  // Harvest/extract only the selected photo albums
        	  fbh.processPhotoAlbums(selectedAlbumsIds);
          }
      } 
      catch(FacebookException ex) 
      {
          // response.sendError(response.SC_INTERNAL_SERVER_ERROR, "Error while fetching user's facebook data");
          System.out.println("FBException: "+ex.getMessage());
          out.println("FBException: "+ex.getMessage());
          session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
          return;
      }
      catch(Exception e) 
      {
          // response.sendError(response.SC_INTERNAL_SERVER_ERROR, "Error while fetching user's facebook data");
          System.out.println("Exception: "+e.getMessage());
          out.println("Exception: "+e.getMessage());
          session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
          return;
      }
      
      // Need to get a fresh unified list of Photo Album resources after a reharvest
      session.removeAttribute(FBAppSettings.MY_PHOTO_BOOK);
      
      // Retrieve Photo Albums data from PrPl
      if (prplService_ != null)
      {
    	  PrPlPhotoBook myPhotoBook = new PrPlPhotoBook(prplService_);
          myPhotoBook.buildPrPlPhotoBook(prplService_);
          List<PrPlAlbumInfo> myPhotoAlbums = myPhotoBook.getPhotoAlbumsList();
          HashMap<String, PrPlAlbumInfo> myPhotoAlbumsByURI = myPhotoBook.getURIToPhotoAlbumMap();
          if (myPhotoAlbums == null || myPhotoAlbumsByURI == null) {
              // ERROR: PRINT OUT THAT SESSION HAS EXPIRED
              System.out.println("Error: Trouble contacting your Personal Cloud Butler to obtain your unified collection of photo albums");
              out.println("Error: Trouble contacting your Personal Cloud Butler to obtain your unified collection of photo albums");
              session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
              return;
          }
          session.setAttribute(FBAppSettings.MY_PHOTO_BOOK, myPhotoBook);
          
          out.println("Successfully harvested your Facebook Photo Albums.");  
      }
      else {
    	  out.println("Connection to PrPl not found!");
      }
      session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
%>