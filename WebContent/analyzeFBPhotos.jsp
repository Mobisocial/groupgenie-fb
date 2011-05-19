<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="com.restfb.types.Photo"%>
<%@page import="com.restfb.exception.FacebookException"%>
<%@page import="com.restfb.exception.FacebookNetworkException"%>
<%@page import="com.restfb.exception.FacebookResponseStatusException"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@page import="edu.stanford.socialflows.algo.*"%>
<%@page import="edu.stanford.socialflows.algo.util.*"%>
<%@page import="edu.stanford.socialflows.algo.graph.*"%>
<%@page import="edu.stanford.socialflows.data.*"%>
<%@page import="edu.stanford.socialflows.util.*"%>
<%@page import="edu.stanford.socialflows.sticker.Sticker"%>
<%@page import="edu.stanford.socialflows.contacts.SocialFlowsAddressBook"%>
<%@page import="edu.stanford.socialflows.contacts.SocialFlowsContactInfo"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@include file="sessionCheck.jsp"%>
<%
	// Get initial photobook object
      SocialFlowsPhotoBook photoBook
      = (SocialFlowsPhotoBook)session.getAttribute(FBAppSettings.MY_PHOTO_BOOK);
      if (photoBook == null) {
    	  photoBook = new SocialFlowsPhotoBook(dbconnManager);
          // Initialize collection of FB photo albums
          photoBook.getFBPhotoAlbums(fbs);
          session.setAttribute(FBAppSettings.MY_PHOTO_BOOK, photoBook);
      }
      
      // AMT (Amazon Mechanical Turk) min requirements
      int MIN_NUM_FRIENDS = 50;
      int MIN_NUM_PHOTOS  = 30;
      boolean enforceAMTLimits = false;
      
      // FB tries
      int MAX_FB_TRIES = 3;

      // Old Algo params
      int MINCOUNT              = AlgoStats.DEFAULT_MIN_FREQUENCY_PHOTOS;
      float UTILITY_MULTIPLIER  = AlgoStats.DEFAULT_UTILITY_MULTIPLIER;
      float MAX_ERROR           = AlgoStats.DEFAULT_MAX_ERROR;
      int MIN_GROUP_SIZE        = AlgoStats.DEFAULT_MIN_GROUP_SIZE;
      float MIN_MERGE_GROUP_SIM = AlgoStats.DEFAULT_MIN_GROUP_SIMILARITY;
      
      // New algo
      float ERROR_WEIGHT = AlgoStats.DEFAULT_ERROR_WEIGHT;
      int NUM_GROUPS     = AlgoStats.DEFAULT_NUM_GROUPS;
      boolean useNewGroupsAlgo = true;
      
      // Photo stats
      int numPhotosByFriends     = 0;
      int numPhotosByUser        = 0;
      int numPhotosExcludingUser = 0;
      int totalNumPhotosOfUser   = -1;
      int totalNumPhotosToQuery  = -1;

      
      JSONObject analysisResult = new JSONObject();
      try
      {
    	  List<String> selectedPhotosIdsList = new Vector<String>();
    	  boolean refresh = false;
    	  String photoAnalysisInputJsonData = request.getParameter("input");
    	  
    	  //System.out.println(request.getServletPath()+": Received JSON input: \n"+photoAnalysisInputJsonData);

    	  // Parse input JSON if exists
          if (photoAnalysisInputJsonData != null && !photoAnalysisInputJsonData.trim().isEmpty())
          {
              JSONObject selectedAlbumsData = new JSONObject(photoAnalysisInputJsonData);
              refresh = selectedAlbumsData.optBoolean("refresh", false);

              JSONArray selectedAlbumsArray = selectedAlbumsData.optJSONArray("selectedAlbums");
              if (selectedAlbumsArray != null)
              {
            	  List<String> selectedAlbumsIds = new Vector<String>();
                  for (int i = 0; i < selectedAlbumsArray.length(); i++) {
                	  String albumId = selectedAlbumsArray.getString(i);
                	  if (albumId != null && !albumId.trim().isEmpty())
                         selectedAlbumsIds.add(albumId);  
                  }
                  
                  // Analyze only the selected photo albums
                  List<Photo> selectedPhotos = null;
                  for (int numTries = 1; numTries <= MAX_FB_TRIES; numTries++) {
                      try {
                    	  selectedPhotos = fbs.getPhotosInAlbums(selectedAlbumsIds);
                          break;
                      }
                      catch (FacebookNetworkException fne) {
                          if (numTries == MAX_FB_TRIES)
                              throw fne;
                      }
                  }
                  if (selectedPhotos != null) {
                	  for (Photo photo : selectedPhotos) {
                          BasePhotoInfo p = (BasePhotoInfo)photo;
                          String photoId = p.getPid();
                          if (photoId != null && !photoId.trim().isEmpty())
                             selectedPhotosIdsList.add(photoId);
                      }  
                  }   
              }

              JSONObject algoParams = selectedAlbumsData.optJSONObject("algoParams");
              if (algoParams != null)
              {
            	  // Check if new algo
            	  useNewGroupsAlgo = algoParams.optBoolean("useNewGroupsAlgo", true);
            	  if (!useNewGroupsAlgo)
            	  {
            		  String minCount = algoParams.optString("minCount", null);
                      if (minCount != null && !minCount.trim().isEmpty()) {
                          try { MINCOUNT = Integer.parseInt(minCount); } 
                          catch (NumberFormatException nfe) { }
                      }
                      
                      String maxError = algoParams.optString("maxError", null);
                      if (maxError != null && !maxError.trim().isEmpty()) {
                          try { MAX_ERROR = Float.parseFloat(maxError); } 
                          catch (NumberFormatException nfe) { }
                      }
                      
                      String minGroupSize = algoParams.optString("minGroupSize", null);
                      if (minGroupSize != null && !minGroupSize.trim().isEmpty()) {
                          try { MIN_GROUP_SIZE = Integer.parseInt(minGroupSize); } 
                          catch (NumberFormatException nfe) { }
                      }
                      
                      String minMergeGroupSim = algoParams.optString("minMergeGroupSim", null);
                      if (minMergeGroupSim != null && !minMergeGroupSim.trim().isEmpty()) {
                          try { MIN_MERGE_GROUP_SIM = Float.parseFloat(minMergeGroupSim); } 
                          catch (NumberFormatException nfe) { }
                      }
            	  }
            	  
                  String errorWeight = algoParams.optString("errWeight", null);
                  if (errorWeight != null && !errorWeight.trim().isEmpty()) {
                      try { ERROR_WEIGHT = Float.parseFloat(errorWeight); } 
                      catch (NumberFormatException nfe) { }
                  }
                  
                  String numGroups = algoParams.optString("numGroups", null);
                  if (numGroups != null && !numGroups.trim().isEmpty()) {
                      try { NUM_GROUPS = Integer.parseInt(numGroups); } 
                      catch (NumberFormatException nfe) { }
                  }
              }
          }
    	  
    	  
    	  
    	  
          StatusProviderInstance statusUpdate = new StatusProviderInstance("Building Your Unified Contacts List...");
          session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, statusUpdate);
    	  
          // Need contacts info
          SocialFlowsAddressBook myAddressBook 
          = (SocialFlowsAddressBook)session.getAttribute(FBAppSettings.MY_ADDRESS_BOOK);

          // Get the user's unified list of Person resources (representing Friends/Contacts)
          if (myAddressBook == null) {
              myAddressBook = new SocialFlowsAddressBook(dbconnManager);
              session.setAttribute(FBAppSettings.MY_ADDRESS_BOOK, myAddressBook);
              // System.out.println("BUILDING NEW ADDRESSBOOK...");

              // Initialize Facebook Harvester
              // Get latest FB social graph
              DBFacebookConnector dbfbh = new DBFacebookConnector("FB", dbconnManager, fbs);
              session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, dbfbh);
              for (int numTries = 1; numTries <= MAX_FB_TRIES; numTries++) {
                  try {
                	  dbfbh.processSocialGraph();
                      break;
                  }
                  catch (FacebookNetworkException fne) {
                      if (numTries == MAX_FB_TRIES)
                          throw fne;
                  }
              }
              session.setAttribute(FBAppSettings.MY_FB_FRIENDS_INFO, 
            		               dbfbh.getFriendsFBProfiles());
          }

          // Check if current FB social graph is available
          List<SocialFlowsContactInfo> myFBFriends 
          = (List<SocialFlowsContactInfo>)session.getAttribute(FBAppSettings.MY_FB_FRIENDS_INFO);
          session.removeAttribute(FBAppSettings.MY_FB_FRIENDS_INFO); // save memory

          // So that we can get continuous status updates about retrieving the unified addressbook
          session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, myAddressBook);
          List<SocialFlowsContactInfo> myFriends = myAddressBook.getUnifiedFriendsList(myFBFriends);
          System.out.println("OBTAINED UNIFIED ADDRESSBOOK!!!");
          
          // Check if min # of friends criteria is fulfilled for AMT task
    	  if (FBAppSettings.ENABLE_AMT_MODE && enforceAMTLimits) {
    		  if (myFriends == null || myFriends.size() < MIN_NUM_FRIENDS) {
    			  analysisResult.put("success", false);
                  analysisResult.put("message", 
                                     "You do not fulfill one of the criteria for this Mechanical Turk task: "
                                     +"You have less than "+MIN_NUM_FRIENDS+" Facebook friends.");
                  out.println(analysisResult.toString());
                  session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
                  return;
    		  }
    	  }
          
    	  
          
          // Build map of "FB photos -> social groups"
          HashMap<String, HashSet<String>> myGroupsMap 
          = (HashMap<String, HashSet<String>>)session.getAttribute(FBAppSettings.MY_FB_PHOTOTAG_GROUPS);
          HashMap<String, HashSet<String>> friendGroupsMap 
          = (HashMap<String, HashSet<String>>)session.getAttribute(FBAppSettings.FRIEND_FB_PHOTOTAG_GROUPS);
          HashSet<String> photosOfUser 
          = (HashSet<String>)session.getAttribute(FBAppSettings.MY_FB_PHOTOTAGS);
          
          if (myGroupsMap == null && friendGroupsMap == null)
              refresh = true;
          if (refresh)
          {
        	  System.out.println("Getting FB Photo tags...");
        	  statusUpdate = new StatusProviderInstance("Getting Your Tagged Facebook Photos...");
              session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, statusUpdate);
        	  
              session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, fbs);
              List<String> photosIdsList = new Vector<String>();
              
              // Get all photos owned by current user
              // (an expensive call to FB but needed because no easy way
              //  to get all tagged photos owned by current user)
              List<Photo> yourPhotos = null;
              for (int numTries = 1; numTries <= MAX_FB_TRIES; numTries++) {
            	  try {
                      yourPhotos = fbs.getPhotosOwnedBy(fbs.getLoggedInUserId());
                      break;
                  }
                  catch (FacebookNetworkException fne) {
                      if (numTries == MAX_FB_TRIES)
                    	  throw fne;
                  }
              }
              if (yourPhotos != null) {
            	  for (Photo photo : yourPhotos) {
                      BasePhotoInfo p = (BasePhotoInfo)photo;
                      String photoId = p.getPid();
                      if (photoId != null && !photoId.trim().isEmpty())
                         photosIdsList.add(photoId);
                  }
              }

              // gets all photo tags made by user, including photos 
              // in which user tag others but not himself/herself
              photoBook.clearPhotoTags();
              totalNumPhotosToQuery = photosIdsList.size();
        	  List<BasePhotoTagInfo> yourPhotoTags = null;
        	  for (int numTries = 1; numTries <= MAX_FB_TRIES; numTries++) {
                  try {
                	  yourPhotoTags = fbs.getPhotoTags(photosIdsList);
                      break;
                  }
                  catch (FacebookNetworkException fne) {
                      if (numTries == MAX_FB_TRIES)
                          throw fne;
                  }
              }
        	  if (yourPhotoTags != null)
        	     photoBook.addPhotoTags(yourPhotoTags);

        	  // get all photos tagged of user, then keep only photos
        	  // from user's friends in which user was tagged in
        	  List<BasePhotoInfo> photosFromFriends = null;
        	  for (int numTries = 1; numTries <= MAX_FB_TRIES; numTries++) {
                  try {
                	  photosFromFriends 
                      = fbs.getPhotosTaggedOf(fbs.getLoggedInUserId());
                      break;
                  }
                  catch (FacebookNetworkException fne) {
                      if (numTries == MAX_FB_TRIES)
                          throw fne;
                  }
              }
        	  totalNumPhotosOfUser = photosFromFriends.size();
        	  System.out.println("\nGot initially "+totalNumPhotosOfUser+" photos where you are tagged...");

        	  photosOfUser  = new HashSet<String>();
        	  photosIdsList = new Vector<String>();
        	  for (Iterator<BasePhotoInfo> i = photosFromFriends.iterator(); i.hasNext();)
        	  {
                  BasePhotoInfo p = (BasePhotoInfo)i.next();
                  photosOfUser.add(p.getPid());

                  // Want to exclude photos owned by & tagging the same user
                  if ((p.getOwner() == fbs.getLoggedInUserId().longValue())
                	  && p.getAid() != null && !p.getAid().equals("0")) {
                	  i.remove();
                	  continue;
                  }
                  
                  String photoId = p.getPid();
                  if (photoId != null && !photoId.trim().isEmpty())
                     photosIdsList.add(photoId);
              }

        	  // Add the friend who is the owner of the photo where you were tagged in
        	  List<BasePhotoTagInfo> friendPhotoTags = null;
        	  for (int numTries = 1; numTries <= MAX_FB_TRIES; numTries++) {
                  try {
                	  friendPhotoTags = fbs.getPhotoTags(photosIdsList);
                      break;
                  }
                  catch (FacebookNetworkException fne) {
                      if (numTries == MAX_FB_TRIES)
                          throw fne;
                  }
              }
        	  photoBook.addPhotoTags(friendPhotoTags);
        	  for (BasePhotoInfo p : photosFromFriends) {
        		  BasePhotoTagInfo ownerTag = new BasePhotoTagInfo();
        		  ownerTag.setPid(p.getPid());
        		  ownerTag.setSubject(String.valueOf(p.getOwner()));
        		  ownerTag.setText("");
        		  friendPhotoTags.add(ownerTag);
        	  }
        	  
        	  
        	  // TODO: Have FACE-PHOTO HARVESTING be run as another program, cron job etc.
        	  if (FBAppSettings.ENABLE_FBFACE_COLLECT) {
        	     photoBook.collectFacePhotos(myAddressBook, fbs);
        	  }
        	  
        	  
        	  // DEBUG
        	  System.out.println("Got "+photosFromFriends.size()+" photos not from your albums");
        	  for (String pid : photosIdsList)
        		  System.out.println("---> Pid: "+pid);

        	  
        	  List<List<BasePhotoTagInfo>> allPhotoTags = new Vector<List<BasePhotoTagInfo>>();
        	  if (yourPhotoTags != null) {
        		  Collections.sort(yourPhotoTags);
        		  allPhotoTags.add(yourPhotoTags);
        	  }
        	  if (friendPhotoTags != null) {
        		  Collections.sort(friendPhotoTags);
        		  allPhotoTags.add(friendPhotoTags);
        	  }
              
        	  // Mapping from fb Pid to social group
        	  myGroupsMap     = new HashMap<String, HashSet<String>>();
        	  friendGroupsMap = new HashMap<String, HashSet<String>>();
        	  String currentUserFbId = String.valueOf(fbs.getLoggedInUserId());
              for (List<BasePhotoTagInfo> photoTags : allPhotoTags)
              {
            	  for (BasePhotoTagInfo photoTag : photoTags) {
                      if (photoTag.getSubject() == null || photoTag.getSubject().trim().isEmpty())
                          continue;
                      if (photoTag.getPid() == null || photoTag.getPid().trim().isEmpty())
                          continue;
                      
                      // Filter out the current user
                      long taggedFBUid = 0;
                      try {
                    	   taggedFBUid = Long.parseLong(photoTag.getSubject());
                      }
                      catch (NumberFormatException nfe) {
                    	   System.out.println("Bad tagged subject of FB Uid "+photoTag.getSubject());
                    	   continue;
                      }
                      
                      if (taggedFBUid == fbs.getLoggedInUserId().longValue())
                    	  continue;
                      
                      SocialFlowsContactInfo person
                      = myAddressBook.lookupFriendByFbId(taggedFBUid);
                      if (person == null)
                          continue; // Don't include non-friends

                      //System.out.println("For photo # "+photoTag.getPid()+" ");
                      //System.out.println(" ---> "+person.getName()+" ("+photoTag.getSubject()+")");
                      
                      HashMap<String, HashSet<String>> groupsMap = myGroupsMap;
                      if (photoTags == friendPhotoTags)
                    	  groupsMap = friendGroupsMap;
                      HashSet<String> groupSet = groupsMap.get(photoTag.getPid());
                      if (groupSet == null) {
                          groupSet = new HashSet<String>();
                          groupsMap.put(photoTag.getPid(), groupSet);
                      }
                      groupSet.add(photoTag.getSubject());
                  }
              }
              
              
              // Cache of the raw groups from photos for future rerun of algo
        	  session.setAttribute(FBAppSettings.MY_FB_PHOTOTAG_GROUPS, myGroupsMap);
        	  session.setAttribute(FBAppSettings.FRIEND_FB_PHOTOTAG_GROUPS, friendGroupsMap);
        	  session.setAttribute(FBAppSettings.MY_FB_PHOTOTAGS, photosOfUser);
          }


          // For debugging purposes
          HashMap<HashSet<String>, Integer> groupFreqCount 
          = new HashMap<HashSet<String>, Integer>();
          List<HashSet<String>> inputList = new Vector<HashSet<String>>();
          
          // Build the list of input groups
          List<Group<String>> input = new Vector<Group<String>>();
          if (selectedPhotosIdsList.size() > 0)
          {
        	  for (String pid : selectedPhotosIdsList) {
                  HashSet<String> groupSet = myGroupsMap.get(pid);
                  if (groupSet == null || groupSet.size() <= 0)
                      continue;
                  Group<String> group = new Group<String>(groupSet);
                  input.add(group);
                  inputList.add(groupSet);
                  
                  // Record num of tagged photos made by current user
                  numPhotosByUser++;
                  // Record num of tagged photos made by user but did not tag user
                  if (!photosOfUser.contains(pid))
                	  numPhotosExcludingUser++;
                  
                  Integer freqCount = groupFreqCount.get(groupSet);
                  if (freqCount == null)
                	  groupFreqCount.put(groupSet, new Integer(1));
                  else
                	  groupFreqCount.put(groupSet, new Integer(1+freqCount.intValue()));
              }
          }
          else
          {
        	  for (String pid : myGroupsMap.keySet()) {
        		  HashSet<String> groupSet = myGroupsMap.get(pid);
                  if (groupSet == null || groupSet.size() <= 0)
                      continue;
                  Group<String> group = new Group<String>(groupSet);
                  input.add(group);
                  inputList.add(groupSet);
                  
                  // Record num of tagged photos made by current user
                  numPhotosByUser++;
                  // Record num of tagged photos made by user but did not tag user
                  if (!photosOfUser.contains(pid))
                      numPhotosExcludingUser++;
                  
                  Integer freqCount = groupFreqCount.get(groupSet);
                  if (freqCount == null)
                      groupFreqCount.put(groupSet, new Integer(1));
                  else
                      groupFreqCount.put(groupSet, new Integer(1+freqCount.intValue()));
              }
          }

          // Covers groups/friends' photos where user's friends tag current user
          for (HashSet<String> groupSet : friendGroupsMap.values()) {
        	  if (groupSet == null || groupSet.size() <= 0)
                  continue;
              Group<String> group = new Group<String>(groupSet);
              input.add(group);
              inputList.add(groupSet);
              
              Integer freqCount = groupFreqCount.get(groupSet);
              if (freqCount == null)
                  groupFreqCount.put(groupSet, new Integer(1));
              else
                  groupFreqCount.put(groupSet, new Integer(1+freqCount.intValue()));
          }
          // Record num of photos where user is tagged but owned by user's friends
          numPhotosByFriends = friendGroupsMap.size();


          
          // DEBUGGING
          /*
          System.out.println("Analyzing "+input.size()+" FB photos for social groups...");
          if (myAddressBook != null)
          {
        	  for (HashSet<String> groupSet: inputList)
        	  {
        		  System.out.print("\nGroup Candidate: [");
                  for (Iterator<String> i = groupSet.iterator(); i.hasNext();) {
                	  String memberFbIdString = i.next();
                	  long memberFbId = 0;
                	  try {
                		  memberFbId = Long.parseLong(memberFbIdString);
                	  }
                	  catch (NumberFormatException nfe) {
                		  System.out.println("Bad group member FB Uid "+memberFbIdString);
                		  continue;
                	  }
                	  
                      SocialFlowsContactInfo member
                      = myAddressBook.lookupFriendByFbId(memberFbId);
                      if (member != null)
                          System.out.print(member.getName());
                      else
                    	  System.out.print(memberFbId);
                      if (i.hasNext()) System.out.print(", ");
                  }
                  System.out.print("]");  
        	  }
          }
          System.out.print("\n");
          */

          if (input.size() <= 0) {
              analysisResult.put("success", false);
              analysisResult.put("message", 
                                 "No social groups could be deduced from your current Facebook photos. "
                                 +"There are no tagged photos where you appear with others.");
              out.println(analysisResult.toString());
              session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
              return;
          }
          
          // Check if min # of tagged photos criteria is fulfilled for AMT task
          // Don't check on subsequent reruns on cached data
          if (refresh && FBAppSettings.ENABLE_AMT_MODE && enforceAMTLimits) {
              if ((totalNumPhotosOfUser+numPhotosExcludingUser) < MIN_NUM_PHOTOS) {
                  analysisResult.put("success", false);
                  analysisResult.put("message",
                                     "You do not fulfill one of the criteria for this Mechanical Turk task: "+
                                     "You have less than a total of "+MIN_NUM_PHOTOS+" tagged photos, "+
                                     "of which you are tagged by friends and/or where you tagged others.");
                  out.println(analysisResult.toString());
                  session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
                  return;
              }
              //System.out.println("Photos input size is right now: "+
              //		           (totalNumPhotosOfUser+numPhotosExcludingUser));
          }



          
          // RUN THE ALGO!!!
          GroupHierarchy<String> hierarchy = null;
          List<SimilarGroup<String>> rootGroups = new ArrayList<SimilarGroup<String>>();
          Map<SimilarGroup<String>, List<SimilarGroup<String>>> parentToChildGroupMap
          = new LinkedHashMap<SimilarGroup<String>, List<SimilarGroup<String>>>();
          StringBuilder runInformation = null;
          
          
          // Get existing, saved stickers/social groups from PrPl
          statusUpdate 
          = new StatusProviderInstance(
        		"Analyzing "+input.size()+
        		" tagged Facebook Photos for Your Social Groups..."
        	);
          session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, statusUpdate);
          
          String algoStats = null, anonMap = null;
          
          // Determine whether new or old algo should be run
    	  if (useNewGroupsAlgo)
    	  {
    		  System.out.println("RUNNING NEW algo on FB photo tags...");
              System.out.println("USING the following params...");
              System.out.println("--> ERROR_WEIGHT = "+ERROR_WEIGHT);
              System.out.println("--> NUM_GROUPS = "+NUM_GROUPS);
    		  
    		  // Running the new algo
    		  Grouper<String> grouper = new Grouper<String>();
              //session.setAttribute("grouper", grouper);
              //session.setAttribute("statusProvider", grouper);
              hierarchy = grouper.findGroups(input, NUM_GROUPS, ERROR_WEIGHT);
              algoStats = grouper.getGrouperStats();
              anonMap   = grouper.getAnonMappings();
              
              //System.out.println("\nTHE ALGO STATS (on PHOTOS):\n"+algoStats+"\n");
              //System.out.println("\nTHE ANON MAPPINGS (on PHOTOS):\n"+anonMap+"\n");
              
              //System.out.println("DONE running NEW algo on FB photo tags...");
              //throw new Exception("This is a test Exception when analyzing FB photo albums!!!");
    	  }
    	  else
    	  {
              System.out.println("RUNNING old algo on FB photo tags...");
              System.out.println("USING the following params...");
              System.out.println("--> MINCOUNT = "+MINCOUNT);
              System.out.println("--> MIN_GROUP_SIZE = "+MIN_GROUP_SIZE);
              System.out.println("--> MAX_ERROR = "+MAX_ERROR);
              System.out.println("--> MIN_MERGE_GROUP_SIM = "+MIN_MERGE_GROUP_SIM);
              
              GroupAlgorithmStats<String> stats = new GroupAlgorithmStats<String>();
              String utilityType = "linear";
              hierarchy 
              = SimilarGroupMethods.findContactGroupsOld(input, 
                                                         MINCOUNT, 
                                                         MIN_GROUP_SIZE, 
                                                         MAX_ERROR, 
                                                         MIN_MERGE_GROUP_SIM, 
                                                         utilityType, 
                                                         UTILITY_MULTIPLIER, 
                                                         stats);

              //System.out.println("DONE running old algo on FB photo tags...");
              //System.out.println ("Grouping stats: " + stats);
              runInformation = new StringBuilder();
              runInformation.append("\nGrouping stats: " + stats);
    	  }


    	  // Process the hierarchy to generate display layout
    	  parentToChildGroupMap = hierarchy.parentToChildrenMap;
    	  rootGroups = new ArrayList<SimilarGroup<String>>();
    	  rootGroups.addAll(hierarchy.rootGroups);
    	  
    	  List<SimilarGroup<String>> allGroups 
    	  = hierarchy.getAllGroups();
          Collections.sort (allGroups);

    	  //List<SimilarGroup<String>> groupsList = new ArrayList<SimilarGroup<String>>();
          //groupsList.addAll(rootGroups);
          if (rootGroups.size() <= 0) {
              analysisResult.put("success", false);
              analysisResult.put("message",
                                 "No social groups could be deduced from "+
                                 "your current Facebook photos data.");
              out.println(analysisResult.toString());
              session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
              return;
          }
          
          HashMap<String, Integer> freqMap = SocialFlowsMiningUtil.calcFreqMap(input);
          SocialFlowsMiningUtil.calcGroupMass(freqMap, rootGroups);
          SocialFlowsMiningUtil.sortByMass(rootGroups);
          
          List<SimilarGroup<String>> orderedRootGroups 
          = SimilarGroupMethods.orderGroupsBySimilarity(rootGroups);
    	  JSONObject socialflowsTopology 
    	  = SocialFlowsMiningUtil.jsonForHierarchy(myAddressBook, orderedRootGroups, parentToChildGroupMap);
    	  
    	  // Append source type info, algo params and run information
    	  socialflowsTopology.put("sourceType", Sticker.SOURCE_PHOTO);
    	  if (useNewGroupsAlgo)
    		 socialflowsTopology.put("algoType", "newAlgo");
    	  socialflowsTopology.putOpt("logAlgoStats", algoStats);
    	  socialflowsTopology.putOpt("logAnonMap", anonMap);
    	  if (runInformation != null)
    	     socialflowsTopology.putOpt("runInfo", runInformation.toString());
          socialflowsTopology.putOpt("runTimestamp", new Date().toString());
    	  
          // Current algo parameter values
    	  JSONObject algoParams = new JSONObject();
    	  if (useNewGroupsAlgo) {
    		  algoParams.put("errWeight", String.valueOf(ERROR_WEIGHT));
              algoParams.put("numGroups", String.valueOf(NUM_GROUPS));
    	  }
    	  else {
    		  algoParams.putOpt("minCount", String.valueOf(MINCOUNT));
              algoParams.putOpt("minGroupSize", String.valueOf(MIN_GROUP_SIZE));
              algoParams.putOpt("minMergeGroupSim", String.valueOf(MIN_MERGE_GROUP_SIM));
              algoParams.putOpt("maxError", String.valueOf(MAX_ERROR));  
    	  }
    	  
    	  // Photo stats
    	  algoParams.put("numPhotosByFriends", numPhotosByFriends);
    	  algoParams.put("numPhotosByUser", numPhotosByUser);
    	  algoParams.put("numPhotosExcludingUser", numPhotosExcludingUser);
    	  socialflowsTopology.putOpt("algoParams", algoParams);
    	  
    	  // The default algo parameter values
    	  JSONObject defaultAlgoParams = new JSONObject();
    	  if (useNewGroupsAlgo) {
    		  defaultAlgoParams.put("errWeight", String.valueOf(AlgoStats.DEFAULT_ERROR_WEIGHT));
    		  defaultAlgoParams.put("numGroups", String.valueOf(AlgoStats.DEFAULT_NUM_GROUPS));
    	  }
    	  else {
    		  defaultAlgoParams.putOpt("minCount", 
    				                   String.valueOf(AlgoStats.DEFAULT_MIN_FREQUENCY_PHOTOS));
              defaultAlgoParams.putOpt("minGroupSize", 
            		                   String.valueOf(AlgoStats.DEFAULT_MIN_GROUP_SIZE));
              defaultAlgoParams.putOpt("minMergeGroupSim", 
            		                   String.valueOf(AlgoStats.DEFAULT_MIN_GROUP_SIMILARITY));
              defaultAlgoParams.putOpt("maxError", 
            		                   String.valueOf(AlgoStats.DEFAULT_MAX_ERROR));
    	  }
    	  socialflowsTopology.putOpt("algoParamsDefault", defaultAlgoParams);

    	  
    	  session.setAttribute(FBAppSettings.MY_FB_PHOTOTAG_TOPO, socialflowsTopology);

    	  //System.out.println("Found the following social topology from FB photos: \n"+socialflowsTopology.toString(2));
    	  System.out.println("Successfully analyzed social groups from your Facebook Photos.");
    	  
    	  analysisResult.put("success", true);
          analysisResult.put("message", 
        		             rootGroups.size()+" top-level social groups were "
        		             +"deduced from an analysis of your tagged photos.");

          // So that we can get continuous status updates about the harvesting
          //session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, fbh);
          //System.out.println("Extracting FB photo albums...");
          
          if (FBAppSettings.ENABLE_AMT_MODE) {
        	  String amt_token 
        	  = dbconnManager.generateVerifyToken();
        	  session.setAttribute(FBAppSettings.AMT_VTOKEN, amt_token);
          }
          
      }
      catch(FacebookNetworkException fne) 
      {
          Writer result = new StringWriter();
          final PrintWriter printWriter = new PrintWriter(result);
          fne.printStackTrace(printWriter);

          // Log exception to DB
          String errorLog 
          = request.getServletPath()+": "+
            "A Facebook Network error occured while analyzing the user's photos"+
            "\n  numPhotosByFriends = "+numPhotosByFriends+", "+
            "    numPhotosByUser = "+numPhotosByUser+", "+
            "    numPhotosExcludingUser = "+numPhotosExcludingUser+
            "\n  totalNumPhotosOfUser = "+totalNumPhotosOfUser+", "+
            "    totalNumPhotosToQuery = "+totalNumPhotosToQuery+
            "\n\n"+result.toString();
          sfLogger.logError(errorLog);

          analysisResult.put("success", false);
          analysisResult.put("message", 
        		             "We seem to have a problem contacting Facebook to access "+
        		             "your photo tags, it seems that Facebook's servers are "+
        		             "overloaded right now. "+
        		             "<br/><br/>"+
        		             "Don't give up! Come back in a minute or two, and give "+
        		             "us a try again. Maybe Facebook won't screw up then :) ");
          printWriter.close(); result.close();
      }
      catch(FacebookResponseStatusException frse) 
      {
          Writer result = new StringWriter();
          final PrintWriter printWriter = new PrintWriter(result);
          frse.printStackTrace(printWriter);

          // Log exception to DB
          String errorLog 
          = request.getServletPath()+": "+
            "A FacebookResponseStatus error occured while analyzing the user's photos"+
            "\n  numPhotosByFriends = "+numPhotosByFriends+", "+
            "    numPhotosByUser = "+numPhotosByUser+", "+
            "    numPhotosExcludingUser = "+numPhotosExcludingUser+
            "\n  totalNumPhotosOfUser = "+totalNumPhotosOfUser+", "+
            "    totalNumPhotosToQuery = "+totalNumPhotosToQuery+
            "\n\n"+result.toString();
          sfLogger.logError(errorLog);

          analysisResult.put("success", false);
          analysisResult.put("message", 
                             "We seem to have a problem contacting Facebook to access "+
                             "your photo tags. Facebook is indicating that you are "+
                             "currently logged out."+
                             "<br/><br/>"+
                             "Please try logging out and then logging into Facebook, "+
                             "and reaccessing SocialFlows again.");
          printWriter.close(); result.close();
      }
      catch(FacebookException ex)
      {
          Writer result = new StringWriter();
          final PrintWriter printWriter = new PrintWriter(result);
          ex.printStackTrace(printWriter);

          // Log exception to DB
          String errorLog 
          = request.getServletPath()+": "+
            "A Facebook error occured while analyzing the user's photos"+
            "\n  numPhotosByFriends = "+numPhotosByFriends+", "+
            "    numPhotosByUser = "+numPhotosByUser+", "+
            "    numPhotosExcludingUser = "+numPhotosExcludingUser+
            "\n  totalNumPhotosOfUser = "+totalNumPhotosOfUser+", "+
            "    totalNumPhotosToQuery = "+totalNumPhotosToQuery+
            "\n\n"+result.toString();
          sfLogger.logError(errorLog);

          analysisResult.put("success", false);
          analysisResult.put("message", 
                             "A Facebook error occured while analyzing your photos: "+ex.getMessage()
                             +"<br/><br/>"+result.toString());
          printWriter.close(); result.close();
      }
      catch(Exception e) 
      {
          //System.out.println("Exception: "+e.getMessage());
          //e.printStackTrace();
          
          Writer result = new StringWriter();
          final PrintWriter printWriter = new PrintWriter(result);
          e.printStackTrace(printWriter);
          
          // Log exception to DB
          String errorLog 
          = request.getServletPath()+": "+
            "An error occured while analyzing the user's photos"+
            "\n  numPhotosByFriends = "+numPhotosByFriends+", "+
            "    numPhotosByUser = "+numPhotosByUser+", "+
            "    numPhotosExcludingUser = "+numPhotosExcludingUser+
            "\n  totalNumPhotosOfUser = "+totalNumPhotosOfUser+", "+
            "    totalNumPhotosToQuery = "+totalNumPhotosToQuery+
            "\n\n"+result.toString();
          sfLogger.logError(errorLog);

          analysisResult.put("success", false);
          analysisResult.put("message", 
        		             "An error occured while analyzing your photos: "+e.getMessage()
        		             +"<br/><br/>"+result.toString());
          printWriter.close(); result.close();
      }

      out.println(analysisResult.toString());
      session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
%>