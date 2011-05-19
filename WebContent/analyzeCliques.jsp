<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="edu.stanford.prpl.insitu.*"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@page import="edu.stanford.socialflows.data.*"%>
<%@page import="edu.stanford.socialflows.util.*"%>
<%@page import="edu.stanford.socialflows.sticker.*"%>
<%@page import="edu.stanford.socialflows.contacts.*"%>
<%@page import="edu.stanford.socialflows.db.DBConnectionManager"%>
<%@include file="sessionCheck.jsp"%>
<%
	// Interpret JSON data (provide support for URL or file location)
      String rawJSON;
      try
      {
          BufferedReader in;
          String dataLoc = URLDecoder.decode(request.getParameter("dataLoc"));
          
          if (dataLoc == null)
          {
              // ERROR: PRINT OUT ERROR MESSAGE
              System.out.println("Cliques and groups were not generated.");
              out.println("Cliques and groups were not generated.");
              return;
          }
          if (dataLoc.startsWith("http"))
          {
              URL dataJson = new URL(dataLoc);
              in = new BufferedReader(new InputStreamReader(dataJson.openStream()));
              System.out.println("Fetch data from URL "+dataLoc);
          }
          else
          {
              String inputFilePath = application.getRealPath(dataLoc);
              // String inputFilePath = application.getRealPath("test/GROUPS_json.txt");
              in = new BufferedReader(new FileReader(inputFilePath));
              System.out.println("Fetch data from within server at location "+dataLoc);
          }

          StringBuilder jsonData = new StringBuilder(1000);
          char[] buf = new char[1024];
          int numRead=0;
          while((numRead=in.read(buf)) != -1)
          {
              jsonData.append(buf, 0, numRead);
          }
          in.close();
          rawJSON = jsonData.toString().trim();
      }
      catch (Exception e)
      {
    	    // ERROR: PRINT OUT ERROR MESSAGE
            System.out.println("Exception here: "+e.toString()+" \n");
            e.printStackTrace(System.out);
            out.println("An error occured: "+e.toString());
            return;
      }

      
      JSONObject data = new JSONObject(rawJSON);
      JSONArray suggested_groups = data.getJSONArray("groups");
      // No suggested groups
      if (suggested_groups == null || suggested_groups.length() <= 0)
      {
    	  // ERROR: PRINT OUT ERROR MESSAGE
          System.out.println("No suggested groups found.");
          out.println("No suggested groups found.");
          return;
      }
      
      // Get the user's unified list of social contacts (representing Friends/Contacts)
      SocialFlowsAddressBook myAddressBook 
      = (SocialFlowsAddressBook)session.getAttribute(FBAppSettings.MY_ADDRESS_BOOK);
      if (myAddressBook == null) {
         myAddressBook = new SocialFlowsAddressBook(dbconnManager);
         session.setAttribute(FBAppSettings.MY_ADDRESS_BOOK, myAddressBook);
      }
      
      // So that we can get continuous status updates about retrieving the unified addressbook
      session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, myAddressBook);
      List<SocialFlowsContactInfo> myFriends
      = myAddressBook.getUnifiedFriendsList();
      HashMap<String, SocialFlowsContactInfo> myFriendsByEmail
      = myAddressBook.getEmailToFriendsMap();
      //HashMap<String, SocialFlowsContactInfo> myFriendsByURI = myAddressBook.getSfContactIdToFriendsMap();

      
      // Get existing, saved stickers/social groups from SocialFlows DB
      StatusProviderInstance statusUpdate = new StatusProviderInstance("Retrieving your existing social groups...");
      session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, statusUpdate);
      SocialFlowsStickerBook myStickerBook
      = (SocialFlowsStickerBook)session.getAttribute(FBAppSettings.MY_STICKER_BOOK);
      if (myStickerBook == null) {
    	  myStickerBook = new SocialFlowsStickerBook(dbconnManager);
    	  session.setAttribute(FBAppSettings.MY_STICKER_BOOK, myStickerBook);
      }      

      // Construct the list of suggested stickers
      List<Sticker> suggestedStickers = null;
      HashMap<String, Sticker> suggestedStickersLookup = null;
      List<SocialFlowsGroup> suggestedHierarchicalGroups = null;
      
      session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, myStickerBook);
      myStickerBook.constructSuggStickers(suggested_groups, Sticker.SOURCE_EMAIL, myAddressBook);

      
      // Generate the JSON file containing the saved and suggested social groups data
      data.put("groups", suggested_groups);
      // String jsonOutput = data.toString(3);
      
      // System.out.println("The JSON file data will be: \n"+jsonOutput+"\n");
      
      /*
      // Create the dir structure
      String documentRootPath = application.getRealPath("/insitudata").toString();
      String dirPath = documentRootPath + File.separatorChar + prplUserID;
      new File(dirPath).mkdirs();
      
      String fileLoc = dirPath + File.separator + "insitugroups.json.txt";
      
      // System.out.println("Output to file: "+fileLoc);
      
      PrintWriter pw = new PrintWriter(new FileOutputStream(fileLoc));
      pw.println(jsonOutput);
      pw.close();
      // System.out.println("File successfully created!");
      
      // Set where JSON file is generated
      String relativeLoc = "insitudata/" + prplUserID + "/" + "insitugroups.json.txt";
      session.setAttribute(FBAppSettings.JSON_INSITU_GROUPS_LOC, relativeLoc);
      */
      
      out.print(suggestedStickers.size()+" possible groups have been suggested.");
      session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
%>