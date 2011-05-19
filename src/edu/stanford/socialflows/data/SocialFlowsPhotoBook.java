package edu.stanford.socialflows.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.restfb.exception.FacebookException;

import edu.stanford.socialflows.connector.FBService;
import edu.stanford.socialflows.contacts.*;
import edu.stanford.socialflows.db.DBConnectionManager;
import edu.stanford.socialflows.log.SocialFlowsLogger;
import edu.stanford.socialflows.util.StatusProvider;

public class SocialFlowsPhotoBook implements StatusProvider
{
	// For face image collection status
	public static final int NOTSTARTED = 0;
	public static final int INPROGRESS = 1;
	public static final int COMPLETED  = 2;
	
	private DBConnectionManager sfDBConnManager = null;
	private List<BaseAlbumInfo> myPhotoAlbums   = new Vector<BaseAlbumInfo>();
	//private HashMap<String, BaseAlbumInfo> myPhotoAlbumsByURI = null;
	private List<BasePhotoTagInfo> myPhotoTags = null;
	
	private String userID = null;
	private int collectionStatus = NOTSTARTED;
	
	public SocialFlowsPhotoBook(DBConnectionManager dbconnManager)
	{
		this.sfDBConnManager = dbconnManager;
		this.userID = dbconnManager.getCurrentUserName();
	}

	public boolean buildPhotoBook()
	{
		myPhotoAlbums = new Vector<BaseAlbumInfo>();
		//myPhotoAlbumsByURI = new HashMap<String, BaseAlbumInfo>();
		return true;
	}
	
	public void getFBPhotoAlbums(FBService fbs) throws IOException, FacebookException, JSONException
	{
		long fbMyUserID = fbs.getLoggedInUserId().longValue();
        List<BaseAlbumInfo> fbPhotoAlbums = new Vector<BaseAlbumInfo>();
        List<String> fbPhotoAlbumCovers   = new Vector<String>();
        for (BaseAlbumInfo album : fbs.getPhotoAlbums(fbMyUserID)) {
          if (album.size <= 0)
        	  continue;
      	  fbPhotoAlbums.add(album);
      	  String coverPid = album.getCoverPid();
      	  if (coverPid != null && !coverPid.trim().isEmpty())
      	     fbPhotoAlbumCovers.add(coverPid);
        }
        HashMap<String, BasePhotoInfo> albumCoverMap = new HashMap<String, BasePhotoInfo>();
        // List<BasePhotoInfo> albumCovers = fbs.getPhotosById(fbPhotoAlbumCovers);
        for (BasePhotoInfo albumCover : fbs.getPhotosById(fbPhotoAlbumCovers)) {
      	  albumCoverMap.put(albumCover.getAid(), albumCover);
        }
        
        // Match album cover to photo album
        for (BaseAlbumInfo fbAlbum : fbPhotoAlbums)
        {
        	// find album image cover
            BasePhotoInfo albumCoverPhoto = albumCoverMap.get(fbAlbum.getAid());
            String albumCoverImgUrl = null;
            if (albumCoverPhoto != null) 
            {
            	fbAlbum.setAlbumCover(albumCoverPhoto.getPicture());
            	fbAlbum.setAlbumCoverBig(albumCoverPhoto.getSource());
            	fbAlbum.setAlbumCoverSmall(albumCoverPhoto.getIcon());
            }
        }

        this.myPhotoAlbums.addAll(fbPhotoAlbums);
        
        // Sort photo albums by how recent they were last modified
        Collections.sort(this.myPhotoAlbums);
	}
	

	public List<BaseAlbumInfo> getPhotoAlbumsList()
	{
		return this.myPhotoAlbums;
	}
	
	/*
	public HashMap<String, BaseAlbumInfo> getURIToPhotoAlbumMap()
	{
		return this.myPhotoAlbumsByURI;
	}
	*/
	
	public void addPhotoTags(List<BasePhotoTagInfo> tags)
	{
		if (tags == null)
			return;
		if (this.myPhotoTags == null)
			this.myPhotoTags = new Vector<BasePhotoTagInfo>();
		this.myPhotoTags.addAll(tags);
	}
	
	public void clearPhotoTags()
	{
		if (this.myPhotoTags != null)
			this.myPhotoTags.clear();
	}
	
	public void collectFacePhotos(SocialFlowsAddressBook addressBook, FBService fbs) 
	{
		FacePhotoHarvester fph = new FacePhotoHarvester(addressBook, fbs);
		new Thread(fph).start();
	}

	
	
	public String statusMessage = "";
	
	public void setStatusMessage(String msg) /* synchronized */
	{
	    this.statusMessage = msg;
	}
	
	public String getStatusMessage()
	{
		return new String(this.statusMessage);
	}
	
	
		
	private static final String insertUpdatePhoto
	= "INSERT IGNORE INTO socialflows_fbimage_directory (fbimageId, imageUrl, width, height) "+
	  "   VALUES (?,?,?,?) ";
	  //"   ON DUPLICATE KEY UPDATE imageUrl=?, width=?, height=?";
	private static final String insertUpdateFaceImage
	= "INSERT IGNORE INTO socialflows_fbfaceimage_directory (fbUserId, fbimageId, centerX, centerY) "+
	  "   VALUES (?,?,?,?) ";
	  //"   ON DUPLICATE KEY UPDATE centerX=?, centerY=?";
	
	
	
	// Runs in a separate thread to record photo tags of current user and friends into a database
	class FacePhotoHarvester implements Runnable {
		SocialFlowsAddressBook addressBook = null;
		FBService fbs = null;
		PrintWriter log = null;
		
		public FacePhotoHarvester(SocialFlowsAddressBook addressBook, FBService fbs) 
		{
			this.addressBook = addressBook;
			this.fbs = fbs;
		}
		
		private void collectFacePhotos() 
			throws JSONException, FacebookException
		{
			Connection dbconn = null;
			try {
				dbconn = sfDBConnManager.getConnection();
			}
			catch (SQLException sqle) {
				log.println("TROUBLE GETTING connection to DB: "+sqle.getMessage());
				sqle.printStackTrace(log);
				return;
			}
			
			if (myPhotoTags != null)
			{
				Collections.sort(myPhotoTags);
				HashSet<String> photoIds = new HashSet<String>();
				for (BasePhotoTagInfo photoTag : myPhotoTags) {
		    		photoIds.add(photoTag.getPid());
				}
				
				// Insert/Update metadata about photos
				Vector<String> photoIdsList = new Vector<String>();
				photoIdsList.addAll(photoIds);
				
				HashMap<String, BasePhotoInfo> photosMap 
				= new HashMap<String, BasePhotoInfo>();
				List<BasePhotoInfo> photos = fbs.getPhotosById(photoIdsList);
				for (BasePhotoInfo photo : photos) {
					photosMap.put(photo.getPid(), photo);
				}
				
				try {
					recordTagsAndPhotos(dbconn, myPhotoTags, photosMap);
				}
				catch (SQLException sqle) {
					log.println("ERROR WHILE INSERT/UPDATING FACE TAGS & IMAGES for current user: "+sqle.getMessage());
					sqle.printStackTrace(log);
				}
			}
			log.flush();
			collectFriendFacePhotos(dbconn, addressBook, fbs);
			
			try {
				if (dbconn != null)
				   dbconn.close();	
			} catch (SQLException sqle) {}
		}
		
		// Get the face photos of our FB friends (effectively the photo tags of them).
		// We also do store others who are co-tagged in the same photo, even FB non-friends.
		private void collectFriendFacePhotos(Connection dbconn,
											 SocialFlowsAddressBook addressBook, 
											 FBService fbs)
		throws JSONException, FacebookException
		{
			List<SocialFlowsContactInfo> friendsList = addressBook.getUnifiedFriendsList();
			if (friendsList == null)
				return;
			
			HashSet<String> friendsfbUids = new HashSet<String>();
			for (SocialFlowsContactInfo friend : friendsList)
			{
				HashSet<String> fbUids = friend.getFBUids();
				if (fbUids == null) {
					// TODO: !!! Match up new FB friends to email contact friends & vice-versa
					log.println(friend.getName()+" is not a FB Friend");
					continue;
				}
				friendsfbUids.addAll(fbUids);
			}
			
			HashSet<String> processedPhotoIds = new HashSet<String>();
			for (String friendUid : friendsfbUids)
			{
				log.println("Retrieving photo tags for FB friend Uid "+friendUid+" ...");
				log.flush();
				
				HashMap<String, BasePhotoInfo> friendPhotosMap 
				= new HashMap<String, BasePhotoInfo>();
				List<String> friendPhotoIds = new Vector<String>();
				try 
				{
					List<BasePhotoInfo> friendPhotos = fbs.getPhotosTaggedOf(Long.parseLong(friendUid));
					for (BasePhotoInfo photo : friendPhotos) {
						if (processedPhotoIds.contains(photo.getPid()))
							continue;
						processedPhotoIds.add(photo.getPid());
						friendPhotoIds.add(photo.getPid());
						friendPhotosMap.put(photo.getPid(), photo);
					}
				}
				catch(FacebookException fe) 
				{
					log.println("Error from FB when retrieving photos (for FBUid "+friendUid+"): "+fe.getMessage());
					fe.printStackTrace(log);
				}
				
				if (friendPhotoIds.size() <= 0) {
					log.println("-> Skipping because obtained 0 photos...");
					continue;
				}
				log.println("-> Obtained "+friendPhotoIds.size()+" photos...");
				
				try
				{
					List<BasePhotoTagInfo> friendPhotoTags = fbs.getPhotoTags(friendPhotoIds);
					this.recordTagsAndPhotos(dbconn, friendPhotoTags, friendPhotosMap);
				}
				catch(FacebookException fe) 
				{
					log.println("Error from FB when retrieving photo tags (for FBUid "+friendUid+"): "+fe.getMessage());
					fe.printStackTrace(log);
				}
				catch (SQLException sqle) {
					log.println("ERROR WHILE INSERT/UPDATING FACE TAGS & IMAGES (for FBUid "+friendUid+"): "+sqle.getMessage());
					sqle.printStackTrace(log);
				}
			}
		}
		
		private void recordTagsAndPhotos(Connection dbconn,
		 								 List<BasePhotoTagInfo> photoTags, 
		 								 HashMap<String, BasePhotoInfo> photosMap)
			throws SQLException
		{
			if (photoTags == null)
				return;
			
			Collections.sort(photoTags);
			HashSet<String> photoIds = new HashSet<String>();
			
			PreparedStatement pstmt = null;
	        try 
	        {
	            pstmt = dbconn.prepareStatement(insertUpdateFaceImage);
	            for (BasePhotoTagInfo photoTag : photoTags)
	            {
	    			if (photoTag.getSubject() == null || photoTag.getSubject().isEmpty())
	    				continue;
	    			pstmt.setLong(1, Long.parseLong(photoTag.getSubject()));
	    			pstmt.setString(2, photoTag.getPid());
	        		pstmt.setBigDecimal(3, photoTag.getXcoord());
	        		pstmt.setBigDecimal(4, photoTag.getYcoord());
	        		//pstmt.setBigDecimal(5, photoTag.getXcoord());
	        		//pstmt.setBigDecimal(6, photoTag.getYcoord());
	        		pstmt.addBatch();
	        		
	        		photoIds.add(photoTag.getPid());
	        		//log.println("Adding/Updating FACE IMAGE (FbUserId: "+photoTag.getSubject()+", PhotoId: "+photoTag.getPid()+")");
	    		}
	            pstmt.executeBatch();
			}
			catch (SQLException sqle) {
				log.println("ERROR WHILE INSERT/UPDATING FACE IMAGE: "+sqle.getMessage());
				sqle.printStackTrace(log);
			}
			finally {
				try {
				  pstmt.close();
				} catch (SQLException sqle) {}
			}
			
			
			// Insert/Update metadata about photos
			try 
	        {
	            pstmt = dbconn.prepareStatement(insertUpdatePhoto);
	            for (String pid : photoIds)
	            {
	            	BasePhotoInfo photo = photosMap.get(pid);
	            	if (photo == null)
	            		continue;
	            	
	    			pstmt.setString(1, photo.getPid());
	    			pstmt.setString(2, photo.getSource());
	        		pstmt.setInt(3, photo.getSourceWidth());
	        		pstmt.setInt(4, photo.getSourceHeight());
	        		//pstmt.setString(5, photo.getSource());
	        		//pstmt.setInt(6, photo.getSourceWidth());
	        		//pstmt.setInt(7, photo.getSourceHeight());
	        		pstmt.addBatch();
	        		
	        		//log.println("Adding/Updating PHOTO (PhotoId: "+photo.getPid()+")");
	    		}
	            pstmt.executeBatch();
			}
			catch (SQLException sqle) {
				log.println("ERROR WHILE ADDING/UPDATING PHOTO IMAGE: "+sqle.getMessage());
				sqle.printStackTrace(log);
			}
			finally {
				try {
				  pstmt.close();
				} catch (SQLException sqle) {}
			}
		}
		
		
		
		
		@Override
		public void run()
		{
			if (collectionStatus == SocialFlowsPhotoBook.NOTSTARTED)
				collectionStatus = SocialFlowsPhotoBook.INPROGRESS;
			else
				return;
			
			String documentRootPath = SocialFlowsLogger.SOCIALFLOWS_METRICS_PATH;
		    String dirPath = documentRootPath + File.separatorChar + SocialFlowsLogger.encodeUTF8(userID);
		    File dirLoc = new File(dirPath);
		    dirLoc.mkdirs();
		    
		    System.out.println("PHOTOBOOKLOGGER: Logging to dir at: "+dirLoc.getAbsolutePath());
		    String runFileLoc = dirPath + File.separatorChar + "sfFaceImageCollection.log";
		    // System.out.println("Output to file: "+fileLoc);
			
		    Date startTimestamp = new Date();
		    try {
		    	FileOutputStream fos = new FileOutputStream(runFileLoc, false);
		    	if (log == null)
		    	   log = new PrintWriter(fos);
				try {
					log.println("Face Images Collection Start: "+startTimestamp.toString()+"\n");
					this.collectFacePhotos();
				}
				catch (JSONException jsone) {
					log.println("\nA JSONException occured: "+jsone.getMessage());
					jsone.printStackTrace(log);
				}
				catch (FacebookException fe) {
					log.println("\nA FacebookException occured: "+fe.getMessage());
					fe.printStackTrace(log);
				}
				
				Date endTimestamp = new Date();
				log.println("\n\nFace Images Collection End: "+endTimestamp.toString());
				long totalTimeInMilliseconds = endTimestamp.getTime()-startTimestamp.getTime();
				log.println("\nTotal Time: "+totalTimeInMilliseconds+" milliseconds");
				log.flush();
				try {
					fos.flush();
					fos.close();
				}
				catch (IOException ioe) {}
			    log.close();
			    log = null;
		    }
		    catch (FileNotFoundException fnfe) {
		    	System.out.println("PHOTOBOOK LOGGING ERROR: "+fnfe.getMessage());
		    	fnfe.printStackTrace();
		    }
		}
	}
}
