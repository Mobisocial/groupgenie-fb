<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="edu.stanford.prpl.insitu.*"%>
<%@page import="edu.stanford.socialflows.settings.FBAppSettings"%>
<%@page import="edu.stanford.socialflows.db.DBConnectionManager"%>
<%@page import="edu.stanford.socialflows.sticker.SocialFlowsStickerBook"%>
<%@page import="edu.stanford.socialflows.contacts.SocialFlowsAddressBook"%>
<%@include file="sessionCheck.jsp"%>
<%
	//session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, connManager);
      //connManager.stopButler();
      //if (connManager.stopButler()) {
      //   connManager.deleteButler();
      //session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
      
      // Clear addressbook
      SocialFlowsAddressBook myAddressBook 
      = (SocialFlowsAddressBook)session.getAttribute(FBAppSettings.MY_ADDRESS_BOOK);
      if (myAddressBook == null) {
          myAddressBook = new SocialFlowsAddressBook(dbconnManager);
      }
      myAddressBook.clearAllData();

      // Clear stickerbook
      SocialFlowsStickerBook myStickerBook
      = (SocialFlowsStickerBook)session.getAttribute(FBAppSettings.MY_STICKER_BOOK);
      if (myStickerBook == null) {
    	  myStickerBook = new SocialFlowsStickerBook(dbconnManager);
      }
      myStickerBook.clearAllData();
      
      // Clear all remaining data
      dbconnManager.clearAllData();
      
      
      String finalStatus = dbconnManager.finalStatusMessage;
      //out.println(finalStatus);  
      out.println("All data cleared and deleted for "+dbconnManager.getCurrentUserName()+" ("+dbconnManager.getCurrentUserLogin()+")");
      
      session.invalidate();
%>