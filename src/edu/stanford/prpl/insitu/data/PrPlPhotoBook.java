package edu.stanford.prpl.insitu.data;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import edu.stanford.prpl.api.Identity;
import edu.stanford.prpl.api.PRPLAppClient;
import edu.stanford.prpl.api.QueryResultIterator;
import edu.stanford.prpl.api.Resource;
import edu.stanford.prpl.api.QueryResultIterator.Solution;

import edu.stanford.prpl.app.common.resource.type.Person;
import edu.stanford.socialflows.contacts.PrPlContactInfo;
import edu.stanford.socialflows.data.PrPlAlbumInfo;

public class PrPlPhotoBook
{	
	private PRPLAppClient prplService_ = null;
	private List<PrPlAlbumInfo> myPhotoAlbums = null;
	private HashMap<String, PrPlAlbumInfo> myPhotoAlbumsByURI = null;
	
	/*
		ALBUM(?album, ?name, ?size, ?albumCoverUrl)?
		ALBUM(?album, ?name, ?size, ?albumCoverUrl):-
		ALBUMINFO(?album, ?name, ?size, ?albumCover), PHOTO(?albumCover, ?albumCoverUrl). 
		ALBUMINFO(?album, ?name, ?size, ?albumCover):-
  			(?album a '<http://prpl.stanford.edu/#PhotoAlbum>'),
  			(?album, '<http://prpl.stanford.edu/#name>', ?name),
  			(?album, '<http://prpl.stanford.edu/#albumSize>', ?size),
  			(?album, '<http://prpl.stanford.edu/#albumCover>', ?albumCover).
		PHOTO(?photo, ?photoUrl):-
  			(?photo, '<http://prpl.stanford.edu/#albumViewHttpURL>', ?photoUrl).
		PHOTO(?photo, ?photoUrl):-
  			!(?photo, '<http://prpl.stanford.edu/#albumViewHttpURL>', _),
  			(?photo, '<http://prpl.stanford.edu/#photoHttpURL>', ?photoUrl).
	 */
	private static String datalogQuery
	= "ALBUM(?album, ?name, ?size, ?albumCoverUrl)? \n" +
	  "ALBUM(?album, ?name, ?size, ?albumCoverUrl):- " +
	  "ALBUMINFO(?album, ?name, ?size, ?albumCover), PHOTO(?albumCover, ?albumCoverUrl). \n" +
	  "ALBUMINFO(?album, ?name, ?size, ?albumCover):- " +
	  "  (?album a '<http://prpl.stanford.edu/#PhotoAlbum>'), " +
	  "  (?album, '<http://prpl.stanford.edu/#name>', ?name), " +
	  "  (?album, '<http://prpl.stanford.edu/#albumSize>', ?size), " +
	  "  (?album, '<http://prpl.stanford.edu/#albumCover>', ?albumCover). \n" +
	  "PHOTO(?photo, ?photoUrl):- " +
	  "  (?photo, '<http://prpl.stanford.edu/#albumViewHttpURL>', ?photoUrl). \n" +
	  "PHOTO(?photo, ?photoUrl):- " +
	  "  !(?photo, '<http://prpl.stanford.edu/#albumViewHttpURL>', _), " +
	  "  (?photo, '<http://prpl.stanford.edu/#photoHttpURL>', ?photoUrl).";
	
	public PrPlPhotoBook(PRPLAppClient prplConn)
	{
		this.prplService_ = prplConn;
	}

	public void setPrPlButlerConnection(PRPLAppClient prplConn)
	{
		prplService_ = prplConn;
	}	
	
	public boolean buildPrPlPhotoBook(PRPLAppClient prplConn)
	{
		setPrPlButlerConnection(prplConn);
		myPhotoAlbums = new ArrayList<PrPlAlbumInfo>();
		myPhotoAlbumsByURI = new HashMap<String, PrPlAlbumInfo>();
		
		processPhotoAlbumResources(datalogQuery, myPhotoAlbums, myPhotoAlbumsByURI);
		return true;
	}
	
	private void processPhotoAlbumResources(String datalogQuery, List<PrPlAlbumInfo> albumsList,
										    HashMap<String, PrPlAlbumInfo> albumsByURI)
	{
		QueryResultIterator queryResults;
		for (int i = 0; true; i++)
    	{
    		try {
    			queryResults = prplService_.runDatalogQuery(datalogQuery);
    			break;
    		}
    		catch (Exception e) {
    			if (i == 3) {
    				e.printStackTrace();
    				System.out.println("Problem trying to query for PrPl Photo Album resources from PCB");
    				return;
    			}
    			// prplService_.renewSession();
    		}
    	}
		
    	while (queryResults.hasNext()) 
        {	
        	String key = null;
        	GregorianCalendar modifiedDate = null;
        	Resource album = null;
        	String albumName = null, albumCoverUrl = null;
        	int albumSize = 0;
        	
        	// ?album, ?name, ?size, ?albumCoverUrl
        	
            Solution s = queryResults.next();
            List<String> metadataNames = queryResults.getResultVars();
            for (String var : metadataNames) 
            {
                if (var.equals("?t"))
                    continue;
                Object value = s.get(var);
                
                if (var.equals("modifiedDate") && value instanceof GregorianCalendar)
                	modifiedDate = (GregorianCalendar)value;
                else if (var.equals("name"))
                	albumName = (String)value;
                else if (var.equals("size")) {
                	if (value instanceof Integer) {
                		albumSize = ((Integer)value).intValue();
                		System.out.println("size is Integer object");
                	}
                	else if (value instanceof Long) {
                		albumSize = ((Long)value).intValue();
                		System.out.println("size is Long object");
                	}
                }
                else if (var.equals("albumCoverUrl"))
                	albumCoverUrl = (String)value;
                else if (var.equals("album"))
                	album = (Resource)value;
            }

            if (album != null)
            {
            	Object[] attributes = null;
            	
            	// DEBUG
            	System.out.println("Retrieving Photo Album resource '"+albumName+"' ("+albumSize+" photos)");
            	
            	// Get FB Aid if this was originally a FB photo album
    			String fbAid = null;
    			attributes = album.getMetadata(PrPlAlbumInfo.FBAID_URI);
    			if (attributes != null) {
    				fbAid = (String)attributes[0];
    				fbAid = fbAid.replace("fbAid_", "");
    			}
    			
    			// Get FB URL link to photo album if this was originally a FB photo album
    			String fbLink = null;
    			attributes = album.getMetadata(PrPlAlbumInfo.FBLINK_URI);
    			if (attributes != null) {
    				fbLink = (String)attributes[0];
    			}
    			
    			// Get FB last modified time if a FB photo album
    			long modifiedTime = 0;
    			attributes = album.getMetadata(PrPlAlbumInfo.FBMODIFIED_DATE_URI);
				if (attributes != null) {
					// Should be Long, why is it Integer in PrPl?
					// Long modifiedTime = (Long)attributes[0];					
					if (attributes[0] instanceof Long)
						modifiedTime = ((Long)attributes[0]).longValue();
					else if (attributes[0] instanceof Integer)
						modifiedTime = ((Integer)attributes[0]).longValue();
				}
            	
    			// Create In-Situ object
    			PrPlAlbumInfo photoAlbum = new PrPlAlbumInfo();
    			photoAlbum.setResourceURI(album.getURI());
    			photoAlbum.setAid(fbAid);
    			photoAlbum.setName(albumName);
    			photoAlbum.setLink(fbLink);
    			photoAlbum.setSize(albumSize);
    			photoAlbum.setAlbumCover(albumCoverUrl);
    			photoAlbum.setModified(modifiedTime);

    			albumsList.add(photoAlbum);
    			albumsByURI.put(photoAlbum.getResourceURI(), photoAlbum);
    		}
        }

	}

	public List<PrPlAlbumInfo> getPhotoAlbumsList()
	{
		return this.myPhotoAlbums;
	}
	
	public HashMap<String, PrPlAlbumInfo> getURIToPhotoAlbumMap()
	{
		return this.myPhotoAlbumsByURI;
	}
}
