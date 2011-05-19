package edu.stanford.socialflows.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.json.*;

import edu.stanford.socialflows.connector.FBService;
import edu.stanford.socialflows.contacts.SocialFlowsContactInfo;
import edu.stanford.socialflows.db.DBConnectionManager;
import edu.stanford.socialflows.settings.FBAppSettings;
import edu.stanford.socialflows.sticker.Sticker;

public class SocialFlowsLogger
{
	public static int RECORD_GROUP_DELETE = 1;
	public static int RECORD_GROUP_SAVE   = 2;
	public static String SOCIALFLOWS_METRICS_PATH = "../socialflowsLogs";
	public static SocialFlowsLogger instance = null; /* singleton model */
	
	private DBConnectionManager sfDBConnManager = null;
	private String sfUserID = null;
	private long sfSessionID  = -1;
	private long currentRunID  = -1;
	
	private SocialFlowsLogger()	{ }
	
	private SocialFlowsLogger(String sfUserID, DBConnectionManager dbconnManager)
	{
		this.sfDBConnManager = dbconnManager;
		this.sfUserID = sfUserID;
  	  	this.sfSessionID = getSessionID();
	}
	
	public static void initialize(String sfUserID, DBConnectionManager dbconnManager)
	{
		instance = new SocialFlowsLogger(sfUserID, dbconnManager);
	}
	
	public static SocialFlowsLogger getInstance()
	{
		return instance;
	}
	
	public String getSfUserID() {
		return this.sfUserID;
	}
	
	public void setSfUserID(String sfUserID) {
		this.sfUserID = sfUserID;
	}

	
	private static final String startLogSession
	= "INSERT INTO socialflows_log_sessions (sfId, timestamp, appId) "+
	  "   VALUES (?, NOW(), 'socialflows') ";
	
	// Not JSP session, but SocialFlows-defined session
	public long getSessionID()
	{
		if (this.sfSessionID == -1)
		{
			/*
			this.sfSessionID = new Date().toString();
	  	  	this.sfSessionID = "sfSession_"+this.sfSessionID.replaceAll("[\\s:]", "_");
	  	  	*/
	  	  	
			// Connect to DB and insert new session row
			Connection dbconn = null;
			try {
				dbconn = sfDBConnManager.getConnection();
			}
			catch (SQLException sqle) {
				System.out.println("TROUBLE GETTING connection to DB: "+sqle.getMessage());
				sqle.printStackTrace();
				return this.sfSessionID;
			}
			
			PreparedStatement pstmt = null;
	        try 
	        {
	            pstmt 
	            = dbconn.prepareStatement(startLogSession,
	            		                  Statement.RETURN_GENERATED_KEYS);
	            pstmt.setLong(1, Long.parseLong(this.sfUserID));
	            pstmt.executeUpdate();
	    		
	            // Get "Session ID"
	    		ResultSet rs = pstmt.getGeneratedKeys();
	    		while (rs.next()) {
	    			this.sfSessionID = rs.getLong(1);
	    		}
	    		pstmt.close();
	    		rs.close();
			}
			catch (SQLException sqle) {
				System.out.println("ERROR WHILE CREATING NEW LOG SESSION: "+sqle.getMessage());
				sqle.printStackTrace();
			}
			finally {
				try {
				  pstmt.close();
				  dbconn.close();
				} catch (SQLException sqle) {}
			}

		}
		return this.sfSessionID;
	}
	
	public void setSessionID(long sessionID) {
		this.sfSessionID = sessionID;
	}

	// Run ID gets incremented every time a topology is logged
	public long getCurrentRunID() {
		return this.currentRunID;
	}
	
	public void setCurrentRunID(long runID) {
		this.currentRunID = runID;
	}
	
	
	/*** ERROR LOGS ***/
	public void logError(String errorMsg)
	{
		ErrorDBLogger el = new ErrorDBLogger(errorMsg);
		new Thread(el).start();
	}	
	
	
    /*** STATS COLLECTION ***/
	
	public void logTopologyRunToDB(JSONObject topology, 
			                       List<Sticker> flatList)
	{
		TopologyRunDBLogger trl 
		= new TopologyRunDBLogger(topology, flatList);
		this.currentRunID = trl.getRunID();
		new Thread(trl).start();
	}
	
	public void logTopologyRun(String topologyId, JSONObject topology)
	{
		TopologyRunLogger trl = new TopologyRunLogger(topologyId, topology);
		new Thread(trl).start();
	}
	
	public void logTopologyRun(String topologyId, String topologyJSON)
	{
		TopologyRunLogger trl = new TopologyRunLogger(topologyId, topologyJSON);
		new Thread(trl).start();
	}

	public void logFeedback(long topologyId, String rating, String feedback)
	{
		//FeedbackLogger fl = new FeedbackLogger(topologyId, rating, feedback);
		FeedbackDBLogger fl = new FeedbackDBLogger(topologyId, rating, feedback);
		new Thread(fl).start();
	}
	
	public void logTopologyEdits(long topologyId, int mode, JSONObject moveData)
	{
		TopologyEditsDBLogger tel 
		= new TopologyEditsDBLogger(topologyId, mode, moveData);
		new Thread(tel).start();
	}

	public static String encodeUTF8(String str) {
		String encoderStr = null;
		if (str != null) {
			try {
				encoderStr = URLEncoder.encode(str, "UTF-8");
			}
			catch (UnsupportedEncodingException e) {
				return str;
			}
		}
		return encoderStr;
	}





	class ErrorDBLogger implements Runnable {
		String errorMsg = null;
		
		private static final String logError
		= "INSERT INTO socialflows_log_errors "+
		  "   (sessionId, sfId, exceptionMsg, timestamp) "+
		  "   VALUES (?, ?, ?, NOW()) ";

		
		public ErrorDBLogger(String error) {
			this.errorMsg = error;
		}
		
		@Override
		public void run()
		{
			if (this.errorMsg == null)
				return;
			
			Connection dbconn = null;
			try {
				dbconn = sfDBConnManager.getConnection();
			}
			catch (SQLException sqle) {
				System.out.println("TROUBLE GETTING connection to DB: "+sqle.getMessage());
				sqle.printStackTrace();
				return;
			}
		    
			// Record the feedback and rating
			PreparedStatement pstmt = null;
	        try 
	        {
	            pstmt 
	            = dbconn.prepareStatement(logError);
	            pstmt.setLong(1, sfSessionID);
	            pstmt.setLong(2, Long.parseLong(sfUserID));
	            pstmt.setString(3, this.errorMsg);
	            pstmt.executeUpdate();
	    		
	    		pstmt.close();
			}
			catch (SQLException sqle) {
				System.out.println("ERROR WHILE LOGGING APP EXCEPTION: "+sqle.getMessage());
				sqle.printStackTrace();
			}
			finally {
				try {
				  pstmt.close();
				  dbconn.close();
				} catch (SQLException sqle) {}
			}
		}
		
	}



	class TopologyRunDBLogger implements Runnable {
		long runID              = -1;
		JSONObject topologyInfo = null;
		List<Sticker> topology  = null;
		String sourceType       = null;
		
		// For temporary email contacts
		long latestIdentityId = 0;
		HashMap<String, Long> emailToIdentities = null;
		
		// Log
		PrintWriter log = null;
		FileOutputStream fos = null;
		
		private static final String insertRun
		= "INSERT INTO socialflows_log_runs "+
		  "   (sessionId, sfId, runTimestamp, runInfo) "+
		  "   VALUES (?, ?, NOW(), ?) ";
		
		private static final String updateRunAlgoStats
		= "UPDATE socialflows_log_runs "+
		  "   SET runAlgoStats=? "+
		  "   WHERE sfId=? AND sessionId=? AND runId=? ";
		
		private static final String insertTopology
		= "INSERT IGNORE INTO socialflows_log_topology "+
		  "   (groupId, runId, sessionId, sfId, contactId, identityId) "+
		  "   VALUES (?, ?, ?, ?, ?, ?) ";
		
		private static final String insertHierarchy
		= "INSERT IGNORE INTO socialflows_log_hierarchy "+
		  "   (groupId, runId, sessionId, sfId, parentGroupId) "+
		  "   VALUES (?, ?, ?, ?, ?) ";



		public TopologyRunDBLogger(JSONObject topologyInfo,
				                   List<Sticker> flatList)
		{
			this.topologyInfo = topologyInfo;
			this.topology     = flatList;
			this.runID        = getRunID();
			
			// Tag all stickers with this topology/run ID
			for (Sticker group : flatList) {
				group.stickerTopologyRun = this.runID;
			}
		}

		// Also known as the 'topology Id'
		public long getRunID()
		{
			if (this.runID == -1)
			{
				JSONObject runInfo = new JSONObject();
				try 
				{
					sourceType = topologyInfo.optString("sourceType");
					runInfo.putOpt("sourceType", sourceType);
					runInfo.putOpt("algoParams", topologyInfo.optJSONObject("algoParams"));

					//System.out.println("TOPOLOGYRUNDBLOGGER: Has the following topology run info\n");
					//System.out.println(runInfo.toString(2));
				}
				catch (JSONException jsone) {
					System.out.println("\nA JSONException occured: "+jsone.getMessage());
					jsone.printStackTrace();
				}
		  	  	
				// Connect to DB and insert new run row
				Connection dbconn = null;
				try {
					dbconn = sfDBConnManager.getConnection();
				}
				catch (SQLException sqle) {
					System.out.println("TROUBLE GETTING connection to DB: "+sqle.getMessage());
					sqle.printStackTrace();
					return this.runID;
				}
				
				PreparedStatement pstmt = null;
		        try 
		        {
		            pstmt 
		            = dbconn.prepareStatement(insertRun,
		            		                  Statement.RETURN_GENERATED_KEYS);
		            pstmt.setLong(1, sfSessionID);
		            pstmt.setLong(2, Long.parseLong(sfUserID));
		            pstmt.setString(3, runInfo.toString());
		            pstmt.executeUpdate();
		    		
		            // Get "Run ID"
		    		ResultSet rs = pstmt.getGeneratedKeys();
		    		while (rs.next()) {
		    			this.runID = rs.getLong(1);
		    		}
		    		pstmt.close();
		    		rs.close();
				}
				catch (SQLException sqle) {
					System.out.println("ERROR WHILE CREATING NEW LOG SESSION: "+sqle.getMessage());
					sqle.printStackTrace();
				}
				finally {
					try {
					  pstmt.close();
					  dbconn.close();
					} catch (SQLException sqle) {}
				}

			}
			return this.runID;
		}
		
		
		
		private void setupLogging() throws FileNotFoundException
		{
			String documentRootPath = SocialFlowsLogger.SOCIALFLOWS_METRICS_PATH;
		    String dirPath = documentRootPath + File.separatorChar + encodeUTF8(sfUserID) 
		                     + File.separatorChar + getSessionID();
		    File dirLoc = new File(dirPath);
		    dirLoc.mkdirs();
		    
		    System.out.println("TOPOLOGYRUNLOGGER: Logging to dir at: "+dirLoc.getAbsolutePath());
		    
		    String runFileLoc 
		    = dirPath + File.separatorChar 
		      + "sfTopology_"+this.runID+".run";

		    this.fos = new FileOutputStream(runFileLoc, true);
	    	this.log = new PrintWriter(fos);
	    	this.log.println("\n   \n");
	    	this.log.flush();
		}
		
		private void closeLogging()
		{
			try {
				this.fos.flush();
				this.fos.close();
			}
			catch (IOException ioe) {}
			this.log.flush();
			this.log.close();
		}



		private static final String insertEmailForIdentity
		= "INSERT INTO socialflows_log_email_directory "+
		  "   (ownerSfId, identityId, email) "+
		  "   VALUES(?, ?, ?) ";
		
		private void queryForIdentities(Connection dbconn)
		{
			this.emailToIdentities
			= buildEmailToIdentityIds(dbconn, log, Long.parseLong(sfUserID));

			// Determine most recent identityID
			Vector<Long> identities = new Vector<Long>();
			identities.addAll(this.emailToIdentities.values());
			
			Collections.sort(identities, Collections.reverseOrder());
		    if (identities.size() > 0) {
		    	this.latestIdentityId = identities.firstElement().longValue();
		    }
			
			System.out.println("THE LATEST IDENTITY ID IS: "+this.latestIdentityId);
		}

		private long determineIdentityIDForTempContact(Connection dbconn, 
				                                       SocialFlowsContactInfo member)
		{
			long identityId = 0;
			
			Set<String> emails = member.getEmailAddresses();
			if (emails != null && emails.size() > 0) {
				Long matchingIdentityId = null;
				for (String email : emails) {
					matchingIdentityId
					= this.emailToIdentities.get(email);
					if (matchingIdentityId != null)
						break;
                }
				
				if (matchingIdentityId != null) {
					identityId = matchingIdentityId.longValue();
				}
				else {
					identityId = this.latestIdentityId+1;
					matchingIdentityId = new Long(identityId);
				}

				// Determine if need to update/add other emails
				boolean needEmailUpdates = false;
				for (String email : emails) {
					if (this.emailToIdentities.containsKey(email))
		               continue;
					else {
						needEmailUpdates = true;
						break;
					}
				}
				if (!needEmailUpdates)
				   return identityId;
				
				// Update/add more emails for temp contact
				PreparedStatement pstmt = null;
				try {
		            pstmt = dbconn.prepareStatement(insertEmailForIdentity);
		            for (String email : emails) {
		            	if (this.emailToIdentities.containsKey(email))
		            	   continue;
		            	pstmt.setLong(1, Long.parseLong(sfUserID));
	            		pstmt.setLong(2, identityId);
	            		pstmt.setString(3, email);
	            		pstmt.addBatch();
		            }
		            pstmt.executeBatch();
		            
		            for (String email : emails)
		            	this.emailToIdentities.put(email, matchingIdentityId);
				}
				catch (SQLException sqle) {
					log.println("ERROR WHILE ADDING EMAILS for temp contact "
							    +identityId+" for sfUser "+sfUserID
							    +" : "+sqle.getMessage());
					log.println("Emails were: "+emails.toString()+"\n");
					sqle.printStackTrace(log);
					log.println("\n");
				}
				finally {
					try {
					  pstmt.close();
					} catch (SQLException sqle) {}
				}
			}
			
			// Update latest identity ID counter, if needed
			if (identityId > this.latestIdentityId)
				this.latestIdentityId = identityId;
			return identityId;
		}
		
		
		
		private void recordTopology()
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

			long userId = Long.parseLong(sfUserID);
			queryForIdentities(dbconn);

			// Process the topology
        	for (Sticker group : topology)
        	{
				// group.stickerTopologyRun;
        		if (group.getClique() == null 
        			|| group.getClique().getGroupMembers() == null
        			|| group.getClique().getGroupMembers().size() <= 0)
        			continue;
        		
        		long groupID
        		= Long.parseLong(group.stickerID.substring(group.stickerID.indexOf("_")+1));
        		

        		PreparedStatement pstmt = null;
        		try 
    			{
    				pstmt = dbconn.prepareStatement(insertTopology);
            		for (SocialFlowsContactInfo member : group.getClique().getGroupMembers())
            		{
            			long contactId = -1, identityId = -1;
            			String friendUserId  = member.getTempContactId();
    	                if (member.getContactId() > 0) {
    	                   contactId = member.getContactId();
    	                }
    	                else {
    	                   identityId = determineIdentityIDForTempContact(dbconn, member);
    	                }
    	                
    	                pstmt.setLong(1, groupID);
    		            pstmt.setLong(2, runID);
    		            pstmt.setLong(3, sfSessionID);
    		            pstmt.setLong(4, userId);
    		            if (contactId != -1) {
    		            	pstmt.setLong(5, contactId);
    		            	pstmt.setNull(6, java.sql.Types.BIGINT);
    		            }
    		            else {
    		            	pstmt.setNull(5, java.sql.Types.BIGINT);
    		            	pstmt.setLong(6, identityId);
    		            }
    		            pstmt.addBatch();
            		}
            		pstmt.executeBatch();
    			}
    			catch (SQLException sqle) {
    				log.println("ERROR WHILE RECORDING TOPOLOGY GROUP (Id:"+group.stickerID
    						    +") for sfUser "+sfUserID+" : "+sqle.getMessage());
    				sqle.printStackTrace(log);
    			}
    			finally {
    				try {
    				  pstmt.close();
    				} catch (SQLException sqle) {}
    			}
    			
    			// Record hierarchy structure
    			try 
    			{
    				pstmt = dbconn.prepareStatement(insertHierarchy);
    				pstmt.setLong(1, groupID);
		            pstmt.setLong(2, runID);
		            pstmt.setLong(3, sfSessionID);
		            pstmt.setLong(4, userId);
		            if (group.parentSticker != null) {
		            	String parentIDStr 
		            	= group.parentSticker.stickerID;
		            	long parentID
		        		= Long.parseLong(parentIDStr.substring(parentIDStr.indexOf("_")+1));
		            	pstmt.setLong(5, parentID);
		            }
		            else {
		            	pstmt.setNull(5, java.sql.Types.BIGINT);
		            }
		            pstmt.executeUpdate();
    			}
    			catch (SQLException sqle) {
    				log.println("ERROR WHILE RECORDING HIERARCHY OF GROUP (Id:"+group.stickerID
    						    +") for sfUser "+sfUserID+" : "+sqle.getMessage());
    				sqle.printStackTrace(log);
    			}
    			finally {
    				try {
    				  pstmt.close();
    				} catch (SQLException sqle) {}
    			}
			}
        	
        	recordRunAlgoStats(dbconn);
			
        	// Close DB connection
        	try {
        		dbconn.close();
			} catch (SQLException sqle) {}
		}
		
		
		private void recordRunAlgoStats(Connection dbconn)
		{
			String algoStats = "";
			String oldAlgoStats = null, logAlgoStats = null, logAnonMap = null;
			try 
			{
				oldAlgoStats = topologyInfo.optString("runInfo", null);
				logAlgoStats = topologyInfo.optString("logAlgoStats", null);
				logAnonMap   = topologyInfo.optString("logAnonMap", null);
				
				if (oldAlgoStats != null) {
					JSONObject logAlgo = new JSONObject();
					logAlgo.putOpt("oldAlgoStats", oldAlgoStats);
					algoStats = logAlgo.toString();
				}
				else if (logAlgoStats != null) {
					algoStats = logAlgoStats;
				}
			}
			catch (JSONException jsone) {
				log.println("\nA JSONException occured: "+jsone.getMessage());
				jsone.printStackTrace(log);
			}

			
			// Insert algo stats into run information
			PreparedStatement pstmt = null;
	        try 
	        {
	            pstmt 
	            = dbconn.prepareStatement(updateRunAlgoStats);
	            pstmt.setString(1, algoStats);
	            pstmt.setLong(2, Long.parseLong(sfUserID));
	            pstmt.setLong(3, sfSessionID);
	            pstmt.setLong(4, runID);
	            pstmt.executeUpdate();
	    		pstmt.close();
	    		
	    		//System.out.println("\n\nTRYING TO RECORD the following algo stats:\n"+algoStats);
	    		
	    		// Store and process anonymization maps
	    		long sfId = Long.parseLong(sfUserID);
	    		HashMap<String, Long> emailToContactIds
				= buildEmailToContactIds(dbconn, log, sfId);
				HashMap<String, Long> fbidToContactIds
				= buildFbidToContactIds(dbconn, log, sfId);
	    		processAnonMaps(dbconn, log,
	    				        sfId, sfSessionID, runID,
	    				        logAnonMap, sourceType,
	    				        emailToContactIds, 
	    				        emailToIdentities, 
	    				        fbidToContactIds);	    		
	    		
	    		// Reprocess anonymization maps
	    		if (FBAppSettings.ENABLE_DEV_MODE) {
	    			reprocessAnonMaps(dbconn, log);
	    		}
	    		
			}
			catch (SQLException sqle) {
				log.println("ERROR WHILE RECORDING ALGO STATS: "+sqle.getMessage());
				sqle.printStackTrace(log);
			}
			finally {
				try {
				  pstmt.close();
				} catch (SQLException sqle) {}
			}
		}
		
		
		
		private static final String queryUnprocessedRuns
		= "SELECT sfId, sessionId, runId FROM socialflows_log_runs AS lr "+
		  "   WHERE NOT EXISTS "+
		  "   (SELECT * FROM socialflows_log_identitymaps AS im "+
		  "       WHERE im.sfId = lr.sfId "+
		  "         AND im.sessionId = lr.sessionId "+
		  "         AND im.runId = lr.runId) ";
		
		private static final String queryUnprocessedAnonMaps
		= "SELECT runInfo, runAlgoStats FROM socialflows_log_runs AS lr "+
		  "   WHERE sfId = ? AND sessionId = ? AND runId = ? ";
		
		private void reprocessAnonMaps(Connection dbconn, PrintWriter loge)
		{
			HashMap<Long, Vector<JSONObject>> anonMapsBySfId
			= buildListOfUnprocessedRuns(dbconn, loge);
			
			for (Long sfId : anonMapsBySfId.keySet())
			{
				Vector<JSONObject> unprocessedRuns
				= anonMapsBySfId.get(sfId);
				
				// For email, need two lookup tables:
				// - Emails -> ContactIDs
				// - Temp emails -> IdentityIDs (for emails that were not saved)
				HashMap<String, Long> emailToContactIds
				= buildEmailToContactIds(dbconn, loge, sfId.longValue());
				HashMap<String, Long> emailToIdentityIds
				= buildEmailToIdentityIds(dbconn, loge, sfId.longValue());

				// For photos, need one lookup table:
				// - FBIds -> ContactIDs
				HashMap<String, Long> fbidToContactIds
				= buildFbidToContactIds(dbconn, loge, sfId.longValue());
				
				for (JSONObject runInfoIds : unprocessedRuns)
				{
					long sfIdVal, sessionIdVal, runIdVal;
					try {
						sfIdVal      = runInfoIds.getLong("sfId");
						sessionIdVal = runInfoIds.getLong("sessionId");
						runIdVal     = runInfoIds.getLong("runId");
					}
					catch (JSONException jsone) {
						continue;
					}

	    			// Query for unprocessed anon map, source type
	    			PreparedStatement pstmt = null;
	    	        try 
	    	        {
	    	            pstmt 
	    	            = dbconn.prepareStatement(queryUnprocessedAnonMaps);
	    	            pstmt.setLong(1, sfIdVal);
	    	            pstmt.setLong(2, sessionIdVal);
	    	            pstmt.setLong(3, runIdVal);
	    	            
	    	            String runInfoJSON = null, runAlgoStatsJSON = null;
	    	    		ResultSet rs = pstmt.executeQuery();
	    	    		while (rs.next()) {
	    	    			runInfoJSON = rs.getString(1);
	    	    			runAlgoStatsJSON = rs.getString(2);
	    	    		}
	    	    		pstmt.close();
	    	    		rs.close();
	    	    		
	    	    		if (runInfoJSON == null || runAlgoStatsJSON == null)
	    	    			continue;
	    	    		
	    	    		JSONObject runInfo = null, runAlgoStats = null;
	    	    		try {
	    	    			runInfo = new JSONObject(runInfoJSON);
	    	    			runAlgoStats = new JSONObject(runAlgoStatsJSON);
	    	    		}
	    	    		catch (JSONException jsone) {
	    	    			continue;
	    	    		}
	    	    		
	    	    		String sourceType = runInfo.optString("sourceType", null);
	    	    		if (sourceType == null)
	    	    			continue;
	    	    		String logAlgoStatsJSON = runAlgoStats.optString("logAlgoStats", null);
	    	    		String logAnonMapJSON   = runAlgoStats.optString("logAnonMap", null);
	    	    		
	    	    		
	    	    		//System.out.println("\n\nTRYING TO RECORD the following algo stats:\n"+algoStats);
	    	    		
	    	    		// Store and process anonymization maps
	    	    		processAnonMaps(dbconn, loge,
	    	    				        sfIdVal, sessionIdVal, runIdVal,
	    	    				        logAnonMapJSON, sourceType,
	    	    				        emailToContactIds,
	    	    				        emailToIdentityIds,
	    	    				        fbidToContactIds);
	    	    		
	    	    		
	    	    		// Now rewrite the entry for column "runAlgoStats"
	    	    		pstmt 
	    	            = dbconn.prepareStatement(updateRunAlgoStats);
	    	            pstmt.setString(1, logAlgoStatsJSON);
	    	            pstmt.setLong(2, sfIdVal);
	    	            pstmt.setLong(3, sessionIdVal);
	    	            pstmt.setLong(4, runIdVal);
	    	            pstmt.executeUpdate();
	    	    		pstmt.close();
	    	    		
	    	    		
	    	    		
	    			}
	    			catch (SQLException sqle) {
	    				loge.println("ERROR WHILE RECORDING ALGO STATS: "+sqle.getMessage());
	    				sqle.printStackTrace(log);
	    			}
	    			finally {
	    				try {
	    				  pstmt.close();
	    				} catch (SQLException sqle) {}
	    			}
				} // going by each run

			} // going by each user
			
			
		}
		
		private HashMap<Long, Vector<JSONObject>> 
		    buildListOfUnprocessedRuns(Connection dbconn, PrintWriter loge)
		{
			HashMap<Long, Vector<JSONObject>> anonMapsBySfId
			= new HashMap<Long, Vector<JSONObject>>();
			
			PreparedStatement pstmt = null;
	        try 
	        {
	        	// Query for all runs that stored the contact maps without anonymization
	            pstmt = dbconn.prepareStatement(queryUnprocessedRuns);
	    		ResultSet rs = pstmt.executeQuery();
	    		while (rs.next()) {
	    			long sfId      = rs.getLong(1);
	    			long sessionId = rs.getLong(2);
	    			long runId     = rs.getLong(3);
	    			
	    			JSONObject runInfo = new JSONObject();
	    			runInfo.put("sfId", sfId);
	    			runInfo.put("sessionId", sessionId);
	    			runInfo.put("runId", runId);
	    			
	    			// Collect all unprocessed runs per a SocialFlows user
	    			Vector<JSONObject> runsBySfId
	    			= anonMapsBySfId.get(new Long(sfId));
	    			if (runsBySfId == null) {
	    				runsBySfId = new Vector<JSONObject>();
	    				anonMapsBySfId.put(new Long(sfId), runsBySfId);
	    			}
	    			runsBySfId.add(runInfo);
	    		}
	    		pstmt.close();
	    		rs.close();
			}
			catch (SQLException sqle) {
				loge.println("\nERROR WHILE BUILDING LIST OF UNPROCSSED ANON MAPS: "
						     +sqle.getMessage());
				sqle.printStackTrace(loge);
				loge.println("\n");
			}
			catch (JSONException jsone) {
				loge.println("\nA JSONException occured WHILE BUILDING LIST OF UNPROCSSED ANON MAPS: "
						     +jsone.getMessage());
				jsone.printStackTrace(loge);
			}
			finally {
				try {
				  pstmt.close();
				} catch (SQLException sqle) {}
			}

			return anonMapsBySfId;
		}
		
		
		
		
		
		
		private static final String insertAnonIdentityMap
		= "INSERT INTO socialflows_log_identitymaps "+
		  "   (sfId, sessionId, runId, runAnonIdentityMap, runOrigIdentityMap) "+
		  "   VALUES(?, ?, ?, ?, ?) ";
		
		private void processAnonMaps(Connection dbconn, PrintWriter log,
				                     long userId, long sessionId, long runId,
				                     String logAnonMap, String sourceType,
				                     HashMap<String, Long> emailToContactIds,
				                     HashMap<String, Long> emailToIdentityIds,
				                     HashMap<String, Long> fbidToContactIds)
		{
			if (logAnonMap == null || sourceType == null)
				return;
			
			String runAnonIdentityMap = "";
			try
			{
				JSONArray anonMap = new JSONArray(logAnonMap);
				//System.out.println("The original algo-anonymized map:\n"+anonMap.toString(1));
				
				/* Example is the following:
				 * { 
				 *   "320" : { "contactId":108 },
				 *   "18"  : { "identityId":42 },
				 *   "228" : { "contactId":889 }
				 * }
				 */
				JSONObject processedAnonMap = new JSONObject();
				for (int i = 0; i < anonMap.length(); i++)
				{
					JSONObject mapping = anonMap.getJSONObject(i);
					if (mapping == null)
						continue;
					Iterator keys = mapping.keys();
					while (keys.hasNext())
					{
						String key = (String) keys.next();
						long algoAnonId = mapping.getLong(key);
						JSONObject sfMapping = new JSONObject();
						
						// Check if already made mapping
						if (processedAnonMap.has(String.valueOf(algoAnonId)))
							continue;
						
						// Convert mapping of anonymized map 
						// to Sf contactId or identityId
						if (sourceType.equals(Sticker.SOURCE_EMAIL))
						{
							// Go by email
							// - lookup email table and identity table
							Long matchingContactId = null;
							matchingContactId = emailToContactIds.get(key);
							if (matchingContactId != null) {
								// Found "Sf Contact Id"
								sfMapping.put("contactId", 
										      matchingContactId.longValue());
							}
							else {
								Long matchingIdentityId = null;
								matchingIdentityId
								= emailToIdentityIds.get(key);
								if (matchingIdentityId == null)
								   continue; // no entry for this
								else {
									// Use "identity Id" instead (temp emails)
									sfMapping.put("identityId", 
											      matchingIdentityId.longValue());
								}
							}
						}
						else if (sourceType.equals(Sticker.SOURCE_PHOTO))
						{
						    // Go by photos
						    // - lookup account table
							
							// Go by email
							// - lookup email table and identity table
							Long matchingContactId = null;
							matchingContactId = fbidToContactIds.get(key);
							if (matchingContactId == null)
								continue;
							else {
								// Found "Sf Contact Id"
								sfMapping.put("contactId", 
										      matchingContactId.longValue());
							}
						}

						// Put in mapping from algo assigned anon ID
						// -> sf Contact Id or sf Identity Id
						processedAnonMap.put(String.valueOf(algoAnonId), 
								             sfMapping);
					}
				}

				runAnonIdentityMap = processedAnonMap.toString();
				
				/*
				System.out.println("The processed map:\n"+processedAnonMap.toString(1));
				System.out.println("\n\nProcessed map has "+processedAnonMap.length()
						           +" entries VS original map has "+anonMap.length()+" entries\n");
				*/
			}
			catch (JSONException jsone) {
				log.println("\nA JSONException occured "
						    +"WHILE LOGGING ANON MAPPING TABLE FOR ALGO STATS: "
						    +jsone.getMessage());
				jsone.printStackTrace(log);
				return;
			}
			
			
			storeAnonIdentityMap(dbconn, log,
                                 userId, sessionId, runId,
                                 runAnonIdentityMap, logAnonMap);
			
		}
		
		private void storeAnonIdentityMap(Connection dbconn, PrintWriter loge,
				                          long userId, long sessionId, long runId,
				                          String runAnonIdentityMap, String runOrigIdentityMap)
		{
			// Log anonymized mapping table for algo stats
			PreparedStatement pstmt = null;
	        try 
	        {
	        	// Insert algo stats into run information
	            pstmt 
	            = dbconn.prepareStatement(insertAnonIdentityMap);
	            pstmt.setLong(1, userId);
	            pstmt.setLong(2, sessionId);
	            pstmt.setLong(3, runId);
	            pstmt.setString(4, runAnonIdentityMap);
	            pstmt.setString(5, runOrigIdentityMap);
	            pstmt.executeUpdate();
	    		pstmt.close();
			}
			catch (SQLException sqle) {
				loge.println("ERROR WHILE LOGGING ANON MAPPING TABLE FOR ALGO STATS: "+sqle.getMessage());
				sqle.printStackTrace(loge);
			}
			finally {
				try {
				  pstmt.close();
				} catch (SQLException sqle) {}
			}
		}





		private static final String queryEmailsForIdentities
		= "SELECT identityId, email FROM socialflows_log_email_directory "+
		  "   WHERE ownerSfId=? ";
		
		private HashMap<String, Long> buildEmailToIdentityIds(Connection dbconn, PrintWriter loge, long userId)
		{
			HashMap<String, Long> emailToIdentityIds
			= new HashMap<String, Long>();
			
			PreparedStatement pstmt = null;
	        try 
	        {
	        	// Query for all temporary email contacts
	            pstmt = dbconn.prepareStatement(queryEmailsForIdentities);
	            pstmt.setLong(1, userId);

	    		ResultSet rs = pstmt.executeQuery();
	    		while (rs.next()) {
	    			long identityId = rs.getLong(1);
	    			String email = rs.getString(2);
	    			emailToIdentityIds.put(email, new Long(identityId));
	    		}
	    		pstmt.close();
	    		rs.close();
			}
			catch (SQLException sqle) {
				loge.println("\nERROR WHILE BUILDING EMAIL->IDENTITYID LOOKUP MAP: "+sqle.getMessage());
				sqle.printStackTrace(loge);
				loge.println("\n");
			}
			finally {
				try {
				  pstmt.close();
				} catch (SQLException sqle) {}
			}
			
			return emailToIdentityIds;
		}


		private static final String queryFriendsEmails
		= "SELECT * FROM socialflows_email_directory WHERE ownerSfId=?";

		private HashMap<String, Long> buildEmailToContactIds(Connection dbconn, PrintWriter loge, long userId)
		{
			HashMap<String, Long> emailToContactIds 
			= new HashMap<String, Long>();
			
			PreparedStatement pstmt = null;
			ResultSet rs = null;
	        try 
	        {
	        	// Get the emails of social contacts
				pstmt = dbconn.prepareStatement(queryFriendsEmails);
				pstmt.setLong(1, userId);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					long contactId = rs.getLong("contactId");
					String email   = rs.getString("email");
					if (email == null || email.trim().length() <= 0)
						continue;
					emailToContactIds.put(email, new Long(contactId));
					
					//System.out.println("(SfContactId: "+contactId+") has email <"+email+">");
				}
				pstmt.close();
				rs.close();

			}
			catch (SQLException sqle) {
				loge.println("ERROR WHILE BUILDING EMAIL->CONTACTID LOOKUP MAP: "+sqle.getMessage());
				sqle.printStackTrace(loge);
				loge.println("\n");
			}
			finally {
				try {
				  pstmt.close();
				} catch (SQLException sqle) {}
			}
			
			return emailToContactIds;
		}


		private static final String queryFBFriends
		= "SELECT * FROM socialflows_accounts_directory AS sacct "+
	      "   WHERE sacct.ownerSfId=? AND sacct.domain='"+FBService.FACEBOOK_PROVIDER+"'";

		private HashMap<String, Long> buildFbidToContactIds(Connection dbconn, PrintWriter loge, long userId)
		{
			HashMap<String, Long> fbidToContactIds
			= new HashMap<String, Long>();
			
			PreparedStatement pstmt = null;
			ResultSet rs = null;
	        try 
	        {
	        	// Get FB ids of social contacts
				pstmt = dbconn.prepareStatement(queryFBFriends);
				pstmt.setLong(1, userId);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					long contactId = rs.getLong("contactId");
					String fbId    = rs.getString("userid");
					fbidToContactIds.put(fbId, new Long(contactId));
					
					//System.out.println("(SfContactId: "+contactId+") has FB id "+fbId);
				}
				pstmt.close();
				rs.close();
			}
			catch (SQLException sqle) {
				loge.println("ERROR WHILE BUILDING FBID->CONTACTID LOOKUP MAP: "+sqle.getMessage());
				sqle.printStackTrace(loge);
			}
			finally {
				try {
				  pstmt.close();
				} catch (SQLException sqle) {}
			}
			
			return fbidToContactIds;
		}



		@Override
		public void run()
		{
			if (this.topologyInfo == null || this.topology == null)
				return;
			if (this.runID == -1)
				return;
			
		    System.out.println("TOPOLOGYRUN_DB_LOGGER: Logging... ");
		    
		    try {
		    	setupLogging();
		    	recordTopology();
				closeLogging();
		    }
		    catch (FileNotFoundException fnfe) {
		    	System.out.println("SOCIALFLOWS LOGGING ERROR: "+fnfe.getMessage());
		    	fnfe.printStackTrace();
		    }
		}
	}
	
	class TopologyRunLogger implements Runnable {
		String topologyId = null;
		String topologyJSON = null;
		JSONObject topology = null;
		JSONObject additionalData = null;
		
		public TopologyRunLogger(String topologyId, JSONObject topology) {
			this.topologyId = topologyId;
			this.topology   = topology;
		}
		
		public TopologyRunLogger(String topologyId, String topologyJSON) {
			this.topologyId     = topologyId;
			this.topologyJSON   = topologyJSON;
		}
		
		public TopologyRunLogger(String topologyId, String topologyJSON, JSONObject additionalData) {
			this.topologyId     = topologyId;
			this.topologyJSON   = topologyJSON;
			this.additionalData = additionalData;
		}
		
		@Override
		public void run() {
			// write output to "current user id"/"sf session id"/"sfTopologyId".topology
			if ((this.topologyJSON == null && this.topology == null) || this.topologyId == null)
				return;

		    String documentRootPath = SocialFlowsLogger.SOCIALFLOWS_METRICS_PATH; //application.getRealPath("/insituUImetrics").toString();
		    String dirPath = documentRootPath + File.separatorChar + encodeUTF8(sfUserID) 
		                     + File.separatorChar + getSessionID();
		    File dirLoc = new File(dirPath);
		    dirLoc.mkdirs();
		    
		    System.out.println("TOPOLOGYRUNLOGGER: Logging to dir at: "+dirLoc.getAbsolutePath());
		    
		    
		    //String topologyFileLoc = dirPath + File.separatorChar + "sfTopology_"+encodeUTF8(topologyId)+".topology";
		    String runFileLoc = dirPath + File.separatorChar + "sfTopology_"+encodeUTF8(topologyId)+".run";
		    // System.out.println("Output to file: "+fileLoc);

		    Date currentTimestamp = new Date();
		    PrintWriter pw = null;
		    try {
		    	FileOutputStream fos = new FileOutputStream(runFileLoc, true);
		    	pw = new PrintWriter(fos);
		    	pw.println("\n   \n");
				try {
					JSONObject topologyInfo = null;
					if (this.topology == null)
						this.topology = new JSONObject(this.topologyJSON);
					topologyInfo = this.topology;
					
					System.out.println("TOPOLOGYRUNLOGGER: Has the following output\n");
					System.out.println(topologyInfo.toString(2));
					
					JSONObject runInfo = new JSONObject();
					runInfo.putOpt("sourceType", topologyInfo.optString("sourceType"));
					runInfo.putOpt("algoParams", topologyInfo.optJSONObject("algoParams"));
					runInfo.putOpt("runTimestamp", topologyInfo.optString("runTimestamp", null));
					runInfo.putOpt("runInfo", topologyInfo.optString("runInfo", null));
					runInfo.putOpt("additionalData", this.additionalData);
					runInfo.putOpt("timestampRecord", currentTimestamp.toString());
					pw.println(runInfo.toString(3));
					pw.flush();
				}
				catch (JSONException jsone) {
					pw.println("\nA JSONException occured: "+jsone.getMessage());
					jsone.printStackTrace(pw);
				}
				
				try {
					fos.flush();
					fos.close();
				}
				catch (IOException ioe) {}
				pw.flush();
			    pw.close();
		    }
		    catch (FileNotFoundException fnfe) {
		    	System.out.println("SOCIALFLOWS LOGGING ERROR: "+fnfe.getMessage());
		    	fnfe.printStackTrace();
		    }
		}
	}



	class FeedbackDBLogger implements Runnable {
		long runId    = -1;
		String rating = null;
		String feedback = null;
		
		private static final String insertFeedback
		= "INSERT INTO socialflows_log_feedback "+
		  "   (runId, sessionId, sfId, rating, feedback, timestamp) "+
		  "   VALUES (?, ?, ?, ?, ?, NOW()) "+
		  "   ON DUPLICATE KEY UPDATE rating=VALUES(rating), "+
		  "                           feedback=VALUES(feedback), "+
		  "                           timestamp=VALUES(timestamp) ";

		
		public FeedbackDBLogger(long topologyId, String rating, String feedback) {
			this.runId    = topologyId;
			this.rating   = rating;
			this.feedback = feedback;
		}
		
		@Override
		public void run()
		{
			Connection dbconn = null;
			try {
				dbconn = sfDBConnManager.getConnection();
			}
			catch (SQLException sqle) {
				System.out.println("TROUBLE GETTING connection to DB: "+sqle.getMessage());
				sqle.printStackTrace();
				return;
			}
		    
			// Record the feedback and rating
			PreparedStatement pstmt = null;
	        try 
	        {
	            pstmt 
	            = dbconn.prepareStatement(insertFeedback);
	            pstmt.setLong(1, this.runId);
	            pstmt.setLong(2, sfSessionID);
	            pstmt.setLong(3, Long.parseLong(sfUserID));
	            pstmt.setBigDecimal(4, new BigDecimal(this.rating));
	            if (this.feedback == null)
	            	this.feedback = "";
	            pstmt.setString(5, this.feedback);
	            pstmt.executeUpdate();
	    		
	    		pstmt.close();
			}
			catch (SQLException sqle) {
				System.out.println("ERROR WHILE RECORDING FEEDBACK: "+sqle.getMessage());
				sqle.printStackTrace();
			}
			finally {
				try {
				  pstmt.close();
				  dbconn.close();
				} catch (SQLException sqle) {}
			}
		}
		
	}
	
	class FeedbackLogger implements Runnable {
		String topologyId = null;
		String rating = null;
		String feedback = null;
		public FeedbackLogger(String topologyId, String rating, String feedback) {
			this.topologyId = topologyId;
			this.rating     = rating;
			this.feedback   = feedback;
		}
		
		
		@Override
		public void run() {
			// write feedback to "current user id"/"sf session id"/"sfTopologyId".feedback
		    String documentRootPath = SocialFlowsLogger.SOCIALFLOWS_METRICS_PATH; //application.getRealPath("/insituUImetrics").toString();
		    String dirPath = documentRootPath + File.separatorChar + encodeUTF8(sfUserID) 
		                     + File.separatorChar + getSessionID();
		    new File(dirPath).mkdirs();
		    
		    String fileLoc = dirPath + File.separatorChar + "sfTopology_"+encodeUTF8(topologyId)+".feedback";
		    // System.out.println("Output to file: "+fileLoc);

		    Date currentTimestamp = new Date();
		    PrintWriter pw = null;
		    try {
		    	FileOutputStream fos = new FileOutputStream(fileLoc, true);
		    	pw = new PrintWriter(fos);
				try {
					JSONObject feedback = new JSONObject();
					feedback.putOpt("timestampRecord", currentTimestamp.toString());
					feedback.putOpt("rating", this.rating);
					feedback.putOpt("feedback", this.feedback);
					
					pw.println(feedback.toString(3));
				}
				catch (JSONException jsone) {
					pw.println("\nA JSONException occured: "+jsone.getMessage());
					jsone.printStackTrace(pw);
				}
				
				try {
					fos.close();
				}
				catch (IOException ioe) {}
			    pw.close();
		    }
		    catch (FileNotFoundException fnfe) {
		    	System.out.println("SOCIALFLOWS LOGGING ERROR: "+fnfe.getMessage());
		    	fnfe.printStackTrace();
		    }
			
		}
	}



	class TopologyEditsDBLogger implements Runnable
	{
		long runId   = -1;
		int moveType = 0;
		JSONObject moveData = null;
		
		private static final String insertMove
		= "INSERT INTO socialflows_log_moves "+
		  "   (runId, sessionId, sfId, groupId, moveType, moveInfo, timestamp) "+
		  "   VALUES (?, ?, ?, ?, ?, ?, NOW()) ";
		
		
		public TopologyEditsDBLogger(long topologyId, 
				                     int mode, 
				                     JSONObject moveData)
		{
			this.runId    = topologyId;
			this.moveType = mode;
			this.moveData = moveData;
		}
		
		@Override
		public void run()
		{
			if (this.moveData == null)
				return;
			
			Connection dbconn = null;
			try {
				dbconn = sfDBConnManager.getConnection();
			}
			catch (SQLException sqle) {
				System.out.println("TROUBLE GETTING connection to DB: "+sqle.getMessage());
				sqle.printStackTrace();
				return;
			}
			
			
			
			// TODO: More detailed recording on a move 
			PreparedStatement pstmt = null;
		    try
		    {
		    	pstmt 
	            = dbconn.prepareStatement(insertMove);
		    	if (this.runId < 0)
		    		this.runId = 0;
		    	pstmt.setLong(1, this.runId);
	            pstmt.setLong(2, sfSessionID);
	            pstmt.setLong(3, Long.parseLong(sfUserID));
		    	
			    // Details about sticker/group receiving edits
	            String stickerName = this.moveData.optString("name", null);
			    String stickerID   = this.moveData.optString("stickerID", null);
			    
			    long groupID = 0;
			    if (stickerID != null)
        		   groupID = Long.parseLong(stickerID.substring(stickerID.indexOf("_")+1));
			    pstmt.setLong(4, groupID);
			    
			    
			    if (this.moveType == SocialFlowsLogger.RECORD_GROUP_SAVE)
			    {
			        try 
			        {
			        	//pw.println("Final Group Size = "+this.moveData.getJSONArray("clique").length()+" contacts");
				    	this.moveData.put("cliqueSize", 
				    			          this.moveData.getJSONArray("clique").length());
			        	
				        JSONObject stickerMetrics 
				        = this.moveData.getJSONObject("stickerMetrics");
				        
				        JSONObject explanation
				        = new JSONObject();
				        explanation.putOpt("numDroppedIn", 
				        		           "Num contacts dropped into this group");
				        explanation.putOpt("numDraggedOut", 
				        		           "Num contacts dragged out of this group");
				        explanation.putOpt("numDeletedContacts", 
				        		           "Num deleted contacts");
				        explanation.putOpt("numAddedContactsByAdd", 
				        		           "Num added contacts (through Add Clique Member)");
				        explanation.putOpt("numClicksToAddContacts", 
				        		           "Num clicks to add contacts (through Add Clique Member)");
				        explanation.putOpt("numAddedContactsByMerge", 
				        		           "Num added contacts (through Merges)");
				        explanation.putOpt("numMerges", 
				        		           "Num clicks to do Merges on this group");
				        explanation.putOpt("totalClicks", 
				        		           "TOTAL num clicks to do Friend Manipulations on this group");
				        stickerMetrics.putOpt("description", explanation);
				        this.moveData.putOpt("stickerMetrics", stickerMetrics);
				    			        	
			        }
			        catch (JSONException jsone) {
			        	System.out.println("\nA JSONException occured: "+jsone.getMessage());
						jsone.printStackTrace();
			        }
			        
			        pstmt.setString(5, "SAVE");
			        pstmt.setString(6, this.moveData.toString());
			        pstmt.executeUpdate();
			    }
			    else if (this.moveType == SocialFlowsLogger.RECORD_GROUP_DELETE)
			    {
			       boolean deleteThroughMerge 
			       = this.moveData.optBoolean("deleteThroughMerge", false);
			    
			       if (deleteThroughMerge)
			    	   pstmt.setString(5, "DELETE-MERGE");
			       else
			    	   pstmt.setString(5, "DELETE");
			       pstmt.setString(6, this.moveData.toString());
			       pstmt.executeUpdate();
			    }
			    
			    pstmt.close();
		    }
		    catch (SQLException sqle) {
				System.out.println("ERROR WHILE LOGGING EDIT/MOVE: "+sqle.getMessage());
				sqle.printStackTrace();
			}
			finally {
				try {
				  pstmt.close();
				  dbconn.close();
				} catch (SQLException sqle) {}
			}
		}
		
	}
	
	class TopologyEditsLogger implements Runnable {
		String topologyId = null;
		int mode = 0;
		JSONObject additionalData = null;
		public TopologyEditsLogger(String topologyId, int mode, JSONObject additionalData) {
			this.topologyId = topologyId;
			this.mode = mode;
			this.additionalData = additionalData;
		}
		
		@Override
		public void run() {
			if (this.additionalData == null)
				return;
			
		    // Log the deletion of groups
		    String documentRootPath = SocialFlowsLogger.SOCIALFLOWS_METRICS_PATH; //application.getRealPath("/insituUImetrics").toString();
		    String dirPath = documentRootPath + File.separatorChar + encodeUTF8(sfUserID) 
		                     + File.separatorChar + getSessionID();
		    new File(dirPath).mkdirs();
		    
		    String fileLoc = dirPath + File.separatorChar + "sfTopology_"+encodeUTF8(topologyId)+".metrics";
		    
		    try {
			    // System.out.println("Output to file: "+fileLoc);
			    Date currentTimestamp = new Date();
			    FileOutputStream fos = new FileOutputStream(fileLoc, true);
			    PrintWriter pw = new PrintWriter(fos);
			    pw.println("\n"+currentTimestamp.toString());
			    
			    if (mode == SocialFlowsLogger.RECORD_GROUP_SAVE) {
			        JSONObject stickerData = additionalData;
			        try 
			        {
				        JSONObject stickerMetrics = additionalData.getJSONObject("stickerMetrics");
				    	
				        // System.out.println("Output to file: "+fileLoc);
				        pw.println("SAVED Group '"+stickerData.getString("name")+"' (Id: "+stickerData.getString("stickerID")+")");
				        pw.println("Final Group Size = "+stickerData.getJSONArray("clique").length()+" contacts");
				        pw.println("Num contacts dropped into this group: "+stickerMetrics.getInt("numDroppedIn"));
				        pw.println("Num contacts dragged out of this group: "+stickerMetrics.getInt("numDraggedOut"));
				        pw.println("Num deleted contacts: "+stickerMetrics.getInt("numDeletedContacts"));
				        pw.println("Num added contacts (through Add Clique Member): "+stickerMetrics.getInt("numAddedContactsByAdd"));
				        pw.println("Num clicks to add contacts (through Add Clique Member): "+stickerMetrics.getInt("numClicksToAddContacts"));
				        pw.println("Num added contacts (through Merges): "+stickerMetrics.getInt("numAddedContactsByMerge"));
				        pw.println("Num clicks to do Merges on this group: "+stickerMetrics.getInt("numMerges"));
				        pw.println("TOTAL num clicks to do Friend Manipulations on this group: "+stickerMetrics.getInt("totalClicks"));			        	
			        }
			        catch (JSONException jsone) {
			        	pw.println("\nA JSONException occured: "+jsone.getMessage());
						jsone.printStackTrace(pw);
			        }
			    }
			    else if (mode == SocialFlowsLogger.RECORD_GROUP_DELETE) {
			       String stickerName = additionalData.optString("name", null);
			       String stickerID   = additionalData.optString("stickerID", null);
			       String stickerURI  = additionalData.optString("stickerURI", null);
			       boolean deleteThroughMerge = additionalData.optBoolean("deleteThroughMerge", false);
			    	
				   pw.println("DELETED Group '"+stickerName+"' (Id: "+stickerID+", URI: "+stickerURI+")");
				   if (deleteThroughMerge)
				   	  pw.println("Deleted through Merge");
			       //// pw.println("Final Group Size = "+stickerData.getJSONArray("clique").length()+" contacts");
			    }
			    
			    try {
					fos.close();
				}
				catch (IOException ioe) {}
			    pw.close();	
		    }
		    catch (FileNotFoundException fnfe) {
		    	System.out.println("SOCIALFLOWS LOGGING ERROR: "+fnfe.getMessage());
		    	fnfe.printStackTrace();
		    }

		}
	}
	
}
