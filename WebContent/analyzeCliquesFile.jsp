<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload"%>
<%@page import="org.apache.commons.fileupload.FileItemFactory"%>
<%@page import="org.apache.commons.fileupload.disk.DiskFileItemFactory"%>
<%@page import="org.apache.commons.fileupload.FileItem"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.socialflows.util.*"%>
<%@page import="edu.stanford.socialflows.data.*"%>
<%@page import="edu.stanford.socialflows.sticker.*"%>
<%@page import="edu.stanford.socialflows.contacts.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@page import="edu.stanford.socialflows.db.DBConnectionManager"%>
<%@include file="sessionCheck.jsp"%>
<%
	// Interpret JSON data (provide support for URL or file location)
      String rawJSON;
      try
      {
          BufferedReader in = null;
          InputStream uploadedStream = null;
          
          // Check that we have a file upload request
          boolean isMultipart = ServletFileUpload.isMultipartContent(request);
          
          // Create a factory for disk-based file items
          DiskFileItemFactory factory = new DiskFileItemFactory();

          // Set factory constraints
          // factory.setRepository(yourTempDirectory);
          // public void setRepository(java.io.File repository)

          // Create a new file upload handler
          ServletFileUpload upload = new ServletFileUpload(factory);
          
          // Parse the request
          List /* FileItem */ items = upload.parseRequest(request);
          
          // Process the uploaded items
          Iterator iter = items.iterator();
          while (iter.hasNext()) {
              FileItem item = (FileItem) iter.next();

              if (item.isFormField()) {
            	 String name = item.getFieldName();
            	 String value = item.getString();

            	 System.out.println("Form field: "+name+" => "+value);
              }
              else {
            	 // Process a file upload
            	 String fieldName = item.getFieldName();
                 String fileName = item.getName();
                 String contentType = item.getContentType();
                 // boolean isInMemory = item.isInMemory();
            	 long sizeInBytes = item.getSize();

            	 System.out.println("Uploaded file: "+fieldName+" => "+fileName);
            	 System.out.println("content type: "+contentType+" ("+sizeInBytes+" bytes)");
            	 
            	 
            	 uploadedStream = item.getInputStream();
            	 in = new BufferedReader(new InputStreamReader(uploadedStream));
              }
          }
          

          if (uploadedStream == null || in == null)
          {
              // ERROR: PRINT OUT ERROR MESSAGE
              System.out.println("Cliques and groups were not generated.");
              
              out.println("<textarea>\n");
              JSONObject result = new JSONObject();
              result.put("success", false);
              result.put("message", "Cliques and groups were not generated.");
              out.println(result.toString());
              out.println("\n</textarea>");
              return;
          }
          
          // Read data in JSON format
          StringBuilder jsonData = new StringBuilder(1000);
          char[] buf = new char[1024];
          int numRead=0;
          while((numRead=in.read(buf)) != -1)
          {
              jsonData.append(buf, 0, numRead);
          }
          if (in != null)
             in.close();
          if (uploadedStream != null)
        	 uploadedStream.close();
          rawJSON = jsonData.toString().trim();
      }
      catch (Exception e)
      {
    	    // ERROR: PRINT OUT ERROR MESSAGE
            System.out.println("Exception here: "+e.toString()+" \n");
            e.printStackTrace(System.out);
            
            out.println("<textarea>\n");
            JSONObject result = new JSONObject();
            result.put("success", false);
            result.put("message", "An error occured: "+e.toString());
            out.println(result.toString());
            out.println("\n</textarea>");
            return;
      }
      
      
      
      JSONObject data = new JSONObject(rawJSON);
      JSONArray suggested_groups = data.getJSONArray("groups");
      // No suggested groups
      if (suggested_groups == null || suggested_groups.length() <= 0)
      {
    	  // ERROR: PRINT OUT ERROR MESSAGE
          out.println("<textarea>\n");
          JSONObject result = new JSONObject();
          result.put("success", false);
          result.put("message", "No suggested groups found.");
          out.println(result.toString());
          out.println("\n</textarea>");
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
      List<SocialFlowsContactInfo> myFriends = myAddressBook.getUnifiedFriendsList();
      HashMap<String, SocialFlowsContactInfo> myFriendsByEmail
      = myAddressBook.getEmailToFriendsMap();
      //HashMap<String, SocialFlowsContactInfo> myFriendsByURI
      //= myAddressBook.getSfContactIdToFriendsMap();
      
      
      // Get existing, saved stickers/social groups from PrPl
      StatusProviderInstance statusUpdate = new StatusProviderInstance("Retrieving your existing Stickers...");
      session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, statusUpdate);
      
      SocialFlowsStickerBook myStickerBook
      = (SocialFlowsStickerBook)session.getAttribute(FBAppSettings.MY_STICKER_BOOK);
      if (myStickerBook == null) {
          myStickerBook = new SocialFlowsStickerBook(dbconnManager);
          //myStickerBook.buildStickerBook(myAddressBook);
          session.setAttribute(FBAppSettings.MY_STICKER_BOOK, myStickerBook);
      }
      
      /*
      // ERROR: PRINT OUT THAT SESSION HAS EXPIRED
      System.out.println("Error: Trouble contacting your Personal Cloud Butler to obtain your existing Stickers");
   
      out.println("<textarea>\n");
      JSONObject result = new JSONObject();
      result.put("success", false);
      result.put("message", "Error: Trouble contacting your Personal Cloud Butler to obtain your existing Stickers");
      out.println(result.toString());
      out.println("\n</textarea>");
      return;
      */

      // Construct the list of suggested stickers
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
      
      // Use FileWriter() ???
      PrintWriter pw = new PrintWriter(new FileOutputStream(fileLoc));
      pw.println(jsonOutput);
      pw.close();
      // System.out.println("File successfully created!");
      
      // Set where JSON file is generated
      String relativeLoc = "insitudata/" + prplUserID + "/" + "insitugroups.json.txt";
      session.setAttribute(FBAppSettings.JSON_INSITU_GROUPS_LOC, relativeLoc);
      
      // Thread.sleep(2000); // sleep for 2 seconds
      */
      
      // To work in conjunction with jQuery Form Plugin
      List<Sticker> suggestedStickers = myStickerBook.getSuggestedStickers();
      out.println("<textarea>\n");
      JSONObject result = new JSONObject();
      result.put("success", true);
      result.put("message", suggestedStickers.size()+" possible groups have been suggested.");
      out.println(result.toString());
      out.println("\n</textarea>");
      
      session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
%>