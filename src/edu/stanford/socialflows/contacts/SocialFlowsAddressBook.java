package edu.stanford.socialflows.contacts;

import java.sql.*;
import java.util.*;
import org.json.*;

import edu.stanford.socialflows.algo.util.EntityDirectory;
import edu.stanford.socialflows.algo.util.EntityInfo;
import edu.stanford.socialflows.connector.FBService;
import edu.stanford.socialflows.contacts.PrPlPersonIdentityMatcher;
import edu.stanford.socialflows.db.DBConnectionManager;
import edu.stanford.socialflows.db.SocialFlowsDBSettings;
import edu.stanford.socialflows.util.StatusProvider;


public class SocialFlowsAddressBook implements StatusProvider, EntityDirectory<String>
{
	private DBConnectionManager sfDBConnManager = null;
	private List<SocialFlowsContactInfo> myFriends = null;
	private HashMap<String, SocialFlowsContactInfo> myFriendsByEmail = null;
	private HashMap<String, SocialFlowsContactInfo> myFriendsByFbId = null;
	private HashMap<String, SocialFlowsContactInfo> myFriendsBySfContactId = null;  // SocialFlows Contact ID to Person
	
	private int numFriends = 0;
	//private HashMap<String, PrPlContactInfo> myFriendsByPrPlIdentity = null; // include just-only Identities


	public SocialFlowsAddressBook(DBConnectionManager dbconnManager)
	{
		this.sfDBConnManager = dbconnManager;
	}
	
	public static SocialFlowsContactInfo createContact()
	{
		return new SocialFlowsContactInfo();
	}

	private void initializeAddressBook()
	{
		this.myFriends              = new Vector<SocialFlowsContactInfo>();
		this.myFriendsByEmail       = new HashMap<String, SocialFlowsContactInfo>();
		this.myFriendsByFbId        = new HashMap<String, SocialFlowsContactInfo>();
		this.myFriendsBySfContactId = new HashMap<String, SocialFlowsContactInfo>();
		this.numFriends = 0;
	}
	
	private boolean buildAddressBook(List<SocialFlowsContactInfo> fbFriends)
	{
		initializeAddressBook();
		this.numFriends = 0;
		
		// First, get all Persons/Contacts from DB that are mapped to a user's FB social graph
		// Second, get all other Persons/Contacts from PCB that are not sourced from a user's FB social graph
		try {
			processPersonResources(fbFriends);
		}
		catch (SQLException e) {
			System.out.println("A SQL Exception Occurred: "+e.getMessage());
			e.printStackTrace();
			return false;
		}

		this.setStatusMessage("");
		return true;
	}
	
	public List<SocialFlowsContactInfo> getUnifiedFriendsList()
	{
		return getUnifiedFriendsList(null);
	}
	
	public List<SocialFlowsContactInfo> getUnifiedFriendsList(List<SocialFlowsContactInfo> FBFriends)
	{
		if (this.myFriends == null)
			this.buildAddressBook(FBFriends);
		return this.myFriends;
	}
	
	public HashMap<String, SocialFlowsContactInfo> getEmailToFriendsMap()
	{
		if (this.myFriendsByEmail == null)
			this.buildAddressBook(null);
		return this.myFriendsByEmail;
	}
	
	public HashMap<String, SocialFlowsContactInfo> getSfContactIdToFriendsMap()
	{
		if (this.myFriendsBySfContactId == null)
			this.buildAddressBook(null);
		return this.myFriendsBySfContactId;
	}
	
	public SocialFlowsContactInfo queryFriendByEmail(String email)
	{
		if (this.myFriendsByEmail == null)
			this.buildAddressBook(null);
		return this.myFriendsByEmail.get(email);
	}
	
	/*** Slower (maybe not), less optimized social graph retrieval functions ***/
	/*
	public PrPlContactInfo queryFriendByURI(String uri)
	{
		if (this.myFriendsByURI == null) {
			// this.buildPrPlAddressBook();
			this.myFriendsByURI = new HashMap<String, PrPlContactInfo>();
		}
		
		PrPlContactInfo contact = this.myFriendsByURI.get(uri);
		if (contact != null)
			return contact;

		// Retrieve contact with the given URI from PrPl
		Resource person = null;
		try {
			person = this.prplService_.getResource(uri);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Problem trying to query for PrPl Person resource from PCB");
			return null;
		}
		if (person == null)
			return null;
		
		// Fill in social contact details
		System.err.println("PERSON: Retrieving Person resource <"+uri+">");
		contact = getPrPlContactInfo(person);
		storeContact(contact);
		return contact;
	}
	*/

	public EntityInfo<String> lookup(String entity) {
		SocialFlowsContactInfo match = null;
		if (this.myFriendsByFbId != null) {
			match = this.myFriendsByFbId.get(entity);
		}
		return match;
	}
	
	// TODO: JSON representation of SocialFlowsAddressBook
	public JSONObject toJSON() {
		return new JSONObject();
	}
	
	public SocialFlowsContactInfo lookupFriendByFbId(long fbId)
	{
		if (this.myFriendsByFbId == null)
			return null;
		return this.myFriendsByFbId.get(String.valueOf(fbId));
	}
	
	public SocialFlowsContactInfo lookupFriendBySfContactId(long sfContactId)
	{
		if (this.myFriendsBySfContactId == null)
			return null;
		return this.myFriendsBySfContactId.get(String.valueOf(sfContactId));
	}

	/*	
	private void fillMetadataObj(JSONObject currentMetadataObj, String metadata, Object value)
	{
		if (metadata == null)
			return;

		System.out.println("METADATA-PERSON: Retrieving metadata for Account or Photo resource");
		
		try
		{
			// If a metadata has more than one value, record it as an array of values
			if (currentMetadataObj.has(metadata))
			{
				Object currentValue = currentMetadataObj.get(metadata);
				if (currentValue instanceof JSONArray) {
					((JSONArray)currentValue).put(value);
					return;
				}
				
				JSONArray arrayValue = new JSONArray();
				arrayValue.put(currentValue);
				arrayValue.put(value);
				currentMetadataObj.put(metadata, arrayValue);
			}
			else
			{
				currentMetadataObj.put(metadata, value);
			}
		}
		catch (JSONException jsone)
		{
			System.out.println("fillMetadataObj(): JSON parsing error for metadata <"+metadata+">");
			jsone.printStackTrace();
		}
		
	}
	*/

	
	
	private static final String queryAllFriends
	= "SELECT * FROM socialflows_addressbooks WHERE ownerSfId=?";
	
	private static final String queryAllNonFBFriends
	= "SELECT * FROM socialflows_addressbooks AS sa"+
	  "   WHERE sa.ownerSfId=? AND"+
      "      NOT EXISTS (SELECT * FROM socialflows_accounts_directory AS sacct "+
      "                  WHERE sacct.ownerSfId=? AND sacct.domain='"+FBService.FACEBOOK_PROVIDER+"' AND sacct.contactId=sa.contactId)";
	
	private static final String queryAllFriendsEmails
	= "SELECT * FROM socialflows_email_directory WHERE ownerSfId=?";
	
	private static final String queryAllFriendsAliases
	= "SELECT * FROM socialflows_aliases WHERE ownerSfId=?";
	
	private static final String queryAllFBFriendsAccounts
	= "SELECT * FROM socialflows_accounts_directory WHERE ownerSfId=? AND domain='"+FBService.FACEBOOK_PROVIDER+"'";
	
  	private void processPersonResources(List<SocialFlowsContactInfo> fbFriends) throws SQLException
	{
		// Query for SocialFlowsContacts that are not on Facebook
		// - For now, look for the email-only accounts
		// - use NOT EXISTS
		// SELECT * FROM socialflows_addressbooks AS sa 
		//    WHERE sa.ownerSfId=123 AND
		//       NOT EXISTS (SELECT * FROM socialflows_accounts_dir AS saact 
		//                   WHERE sacct.ownerSfId=123 AND sacct.domain='facebook.com' AND sacct.contactId=sa.contactId);
		//
		// Also got to account for users who were on FB but no longer are (retrieve them later from our DB store)

		Connection dbconn = this.sfDBConnManager.getConnection();
		long ownerSfId = this.sfDBConnManager.getCurrentUser().getSfUserId();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		/*
    	numFriends++;
    	if (numFriends == 1) {
    	   this.setStatusMessage("Integrating "+numFriends+" individual into your Unified Social Contacts List...");
    	}
    	else {
    	   this.setStatusMessage("Integrating "+numFriends+" individuals into your Unified Social Contacts List...");
    	}
		*/
		
		// Make use of currently available FB friends info
		if (fbFriends != null && fbFriends.size() > 0)
		{
			// Add the available list of FB friends into the unified addressbook
			for (SocialFlowsContactInfo fbFriend : fbFriends) {
				myFriends.add(fbFriend);
				myFriendsBySfContactId.put(String.valueOf(fbFriend.getContactId()), 
						                   fbFriend);
				HashSet<String> fbUids = fbFriend.getFBUids();
				if (fbUids != null) {
					for (String fbUid : fbFriend.getFBUids()) {
						myFriendsByFbId.put(fbUid, fbFriend);
					}	
				}
				
				//System.out.println("Added FB friend '"+fbFriend.getName()+"' (SfContactId: "+fbFriend.getContactId()+") to Unified Addressbook");
			}
			
			// Query only for non-FB friends
			pstmt = dbconn.prepareStatement(queryAllNonFBFriends);
			pstmt.setLong(1, ownerSfId);
			pstmt.setLong(2, ownerSfId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				SocialFlowsContactInfo nonFBFriend = fillContactInfo(rs);
				myFriends.add(nonFBFriend);
				myFriendsBySfContactId.put(String.valueOf(nonFBFriend.getContactId()), 
						            nonFBFriend);
				
				//System.out.println("Added non-FB friend '"+nonFBFriend.getName()+"' (SfContactId: "
				//		           +nonFBFriend.getContactId()+") to Unified Addressbook");
			}
			pstmt.close();
			rs.close();
		}
		else
		{
			// Query for all contacts in addressbook first
			pstmt = dbconn.prepareStatement(queryAllFriends);
			pstmt.setLong(1, ownerSfId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				SocialFlowsContactInfo friend 
				= fillContactInfo(rs);
				myFriends.add(friend);
				myFriendsBySfContactId.put(String.valueOf(friend.getContactId()), friend);
				
				System.out.println("Added friend '"+friend.getName()+"' (SfContactId: "
						           +friend.getContactId()+") to Unifed Addressbook");
			}
			pstmt.close();
			rs.close();
			
			// Set FB id for contacts that do have them
			pstmt = dbconn.prepareStatement(queryAllFBFriendsAccounts);
			pstmt.setLong(1, ownerSfId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				long contactId = rs.getLong("contactId");
				String fbId    = rs.getString("userid");
				SocialFlowsContactInfo friend 
				= myFriendsBySfContactId.get(String.valueOf(contactId));
				friend.addAccount(fbId, FBService.FACEBOOK_PROVIDER);
				myFriendsByFbId.put(fbId, friend);
				
				/*
				System.out.println("Friend '"+friend.getName()+"' (SfContactId: "
						           +friend.getContactId()+") has FB id "+fbId);
				*/
			}
			pstmt.close();
			rs.close();
		}
		
		
		// Get the emails of social contacts
		pstmt = dbconn.prepareStatement(queryAllFriendsEmails);
		pstmt.setLong(1, ownerSfId);
		rs = pstmt.executeQuery();
		while (rs.next()) {
			long contactId = rs.getLong("contactId");
			String email   = rs.getString("email");
			if (email == null || email.trim().length() <= 0)
				continue;
			SocialFlowsContactInfo friend 
			= myFriendsBySfContactId.get(String.valueOf(contactId));
			
			// TODO: BUGFIX - Account for former FB friends that were defriended on FB
			if (friend == null)
				continue;
			
			friend.addEmailAddress(email);
			// contact.setProfileUrl("mailto:"+emailAddr);
			myFriendsByEmail.put(email, friend);
			
			/*
			System.out.println("Friend '"+friend.getName()+"' (SfContactId: "
					           +contactId+") has email <"+email+">");
			*/
		}
		pstmt.close();
		rs.close();
		
		// Get the various aliases of social contacts
		pstmt = dbconn.prepareStatement(queryAllFriendsAliases);
		pstmt.setLong(1, ownerSfId);
		rs = pstmt.executeQuery();
		while (rs.next()) {
			long contactId = rs.getLong("contactId");
			String alias   = rs.getString("alias");
			if (alias == null || alias.trim().length() <= 0)
				continue;
			SocialFlowsContactInfo friend 
			= myFriendsBySfContactId.get(String.valueOf(contactId));
			
			// TODO: BUGFIX - Account for former FB friends that were defriended on FB
			if (friend == null)
				continue;
			friend.addName(alias);

			/*
			System.out.println("Friend '"+friend.getName()+"' (SfContactId: "
					           +contactId+") has alias '"+alias+"'");
			*/
		}
		pstmt.close();
		rs.close();
		dbconn.close();
		
		// TODO: Handle FB Profile URL, account-specific info
		/*
		   else if (var.equals("fbProfileUrl"))
         	 fbProfileUrl = (String)value;

		// TODO: Think about last-modified timestamps
		if (var.equals("modifiedDate") && value instanceof GregorianCalendar)
        	modifiedDate = (GregorianCalendar)value;
        */
		
		/*
		QueryResultIterator queryResults;
		for (int i = 0; true; i++)
    	{
    		try {
    			queryResults = this.prplService_.runDatalogQuery(datalogQuery);
    			break;
    		}
    		catch (Exception e) {
    			if (i == 3) {
    				e.printStackTrace();
    				System.out.println("Problem trying to query for PrPl Person resources from PCB");
    				return;
    			}
    		}
    	}
		*/
	}
	
	private SocialFlowsContactInfo fillContactInfo(ResultSet rs) throws SQLException
	{
		if (rs == null)
			return null;
		
		SocialFlowsContactInfo contactInfo = new SocialFlowsContactInfo();
		
		long contactId   = rs.getLong("contactId");
		String fullname  = rs.getString("fullname");
		String firstname = rs.getString("firstname");
		String lastname  = rs.getString("lastname");
		int socialWeight = rs.getInt("contactSocialWeight");
		
		String sqThumbPicUrl = rs.getString("profilePicUrl_SquareThumbnail");
		String thumbPicUrl   = rs.getString("profilePicUrl_Thumbnail");
		String mediumPicUrl  = rs.getString("profilePicUrl_Medium");
		String largePicUrl   = rs.getString("profilePicUrl_Large");
		
		contactInfo.setContactId(contactId);
		contactInfo.setName(fullname);
		contactInfo.setFirstName(firstname);
		contactInfo.setLastName(lastname);
		contactInfo.setSocialWeight(socialWeight);
		
		contactInfo.setPicSquare(sqThumbPicUrl);
		contactInfo.setPicSmall(thumbPicUrl);
		contactInfo.setPic(mediumPicUrl);
		contactInfo.setPicBig(largePicUrl);

		return contactInfo;
	}
	
	
	
	
	
	public SocialFlowsContactInfo saveContact(JSONObject contactData) throws SQLException
	{
		if (contactData == null)
			return null;
		
		boolean isNewContact = false;
		SocialFlowsContactInfo matchingContact = null;
		String cliqueMemberContactID = contactData.optString("sfContactID", null);
		if (cliqueMemberContactID == null || cliqueMemberContactID.equals("null")) 
		{
			// Check by email
			JSONArray cliqueMemberEmails
			= contactData.optJSONArray("emails");
			if (cliqueMemberEmails != null) {
				for (int j = 0; j < cliqueMemberEmails.length(); j++) 
				{
					String emailAddr = cliqueMemberEmails.optString(j, null);
				  	if (emailAddr == null || emailAddr.trim().length() <= 0)
				  		continue;
				  	matchingContact = this.queryFriendByEmail(emailAddr.trim());
				  	if (matchingContact != null)
				  	   break;
			    }					
			}
		}
		else {
			long sfContactId = Long.parseLong(cliqueMemberContactID);
			matchingContact = this.lookupFriendBySfContactId(sfContactId);
		}
		
		
		// Save this new social contact into SocialFlows DB
		String fullname  = contactData.optString("fullName", null);
		String firstname = contactData.optString("firstName", null);
		String lastname  = contactData.optString("lastName", null);
		String cliqueMemberUserID = contactData.optString("userID", null);
		int socialWeight = Integer.parseInt(contactData.optString("socialWeight", "0"));

		// Person/Contact doesn't exist, create if needed
		if (matchingContact == null) {
			matchingContact = SocialFlowsAddressBook.createContact();
			isNewContact = true;
		}
		else if (matchingContact.getContactId() < 0) {
			// This is due to email contacts added to addressbook,
			// but not yet saved by user
			isNewContact = true;
		}
		

		// TODO: Get and update from addressbook cache
		matchingContact.setName(fullname);
		if ( !(firstname == null || firstname.trim().length() <= 0
			   || firstname.trim().equals("null")) )
			matchingContact.setFirstName(firstname);
		if ( !(lastname == null || lastname.trim().length() <= 0
			   || lastname.trim().equals("null")) )
			matchingContact.setLastName(lastname);
		
		// Save social weighting, as determined by Dunbar analysis
		matchingContact.setSocialWeight(socialWeight);
		
        // Save email addresses
		JSONArray cliqueMemberEmails
		= contactData.optJSONArray("emails");
		if (cliqueMemberEmails != null) {
			for (int j = 0; j < cliqueMemberEmails.length(); j++) {
				  	String emailAddr = cliqueMemberEmails.optString(j, null);
				  	if (emailAddr == null || emailAddr.trim().length() <= 0)
				  		continue;
				  	matchingContact.addEmailAddress(emailAddr.trim());
			  	}					
		}
		
		// Save names/aliases
		JSONArray cliqueMemberAlias 
		= contactData.optJSONArray("alias");
		if (cliqueMemberAlias != null) {
			for (int j = 0; j < cliqueMemberAlias.length(); j++) {
				  	String name = cliqueMemberAlias.optString(j, null);
				  	if (name == null || name.trim().length() <= 0)
				  		continue;
				  	matchingContact.addName(name.trim());
			  	}
		}			
		
		
		if (isNewContact) {
			this.insertFriendInfo(matchingContact);
			System.out.println("SAVING NEW social contact/Person resource for '"
			           +fullname+"' ("+cliqueMemberUserID+")");
		}
		else {
			this.updateFriendInfo(matchingContact);
		}
		// Updates emails and name aliases
		this.updateFriendMetadata(matchingContact);
		
		// Add it so that can be looked up by SfContactId
		if (isNewContact && this.myFriends != null 
		    && !this.myFriends.contains(matchingContact))
			this.myFriends.add(matchingContact);
		if (this.myFriendsByEmail != null) {
			for (String email : matchingContact.getEmailAddresses()) {
				this.myFriendsByEmail.put(email, matchingContact);
			}
		}
		if (this.myFriendsBySfContactId != null) {
		   this.myFriendsBySfContactId.put(String.valueOf(matchingContact.getContactId()), matchingContact);
		}
		
		return matchingContact;
	}
	
	public void saveContact(SocialFlowsContactInfo friendInfo) throws SQLException
	{
		if (friendInfo == null)
			return;
		long contactId = friendInfo.getContactId();
		if (contactId > 0)
			this.updateFriendInfo(friendInfo);
		else
			this.insertFriendInfo(friendInfo);
		this.updateFriendMetadata(friendInfo);
		
		// Add it so that can be looked up by SfContactId
		if (this.myFriendsByEmail != null && contactId <= 0) {
			SocialFlowsContactInfo currentRecord = null;
			for (String email : friendInfo.getEmailAddresses()) {
				currentRecord = this.myFriendsByEmail.get(email);
				if (currentRecord != null) break;
			}
			
			//if (currentRecord != null)
			//	this.myFriends.add(friendInfo);
			if (this.myFriendsBySfContactId != null && currentRecord != null) {
			   currentRecord.setContactId(friendInfo.getContactId());
			   this.myFriendsBySfContactId.put(String.valueOf(currentRecord.getContactId()), currentRecord);
			}
		}
		//if (this.myFriendsByEmail != null) {
		//	for (String email : friendInfo.getEmailAddresses())
		//		this.myFriendsByEmail.put(email, friendInfo);
		//}
	}
	
	
	public static final String insertNewUserInfoQuery
	= "INSERT INTO socialflows_addressbooks(fullname, firstname, lastname, ownerSfId, " +
	     "profilePicUrl_SquareThumbnail, profilePicUrl_Thumbnail, profilePicUrl_Medium, profilePicUrl_Large) " +
	     "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
	
	public static final String insertFBAcctQuery
	= "INSERT INTO socialflows_accounts_directory(ownerSfId, contactId, userid, domain) " +
	     "VALUES(?, ?, ?, ?) ";
	
	private void insertFriendInfo(SocialFlowsContactInfo friendInfo) throws SQLException
	{
		// Put in initial basic info
		Connection dbconn = this.sfDBConnManager.getConnection();
		long ownerSfId = this.sfDBConnManager.getCurrentUser().getSfUserId();
		PreparedStatement pstmt 
		= dbconn.prepareStatement(insertNewUserInfoQuery, 
                                  Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, friendInfo.getName());
		
		if (friendInfo.getFirstName() != null && friendInfo.getFirstName().length() > 0)
		   pstmt.setString(2, friendInfo.getFirstName());
		else
		   pstmt.setNull(2, java.sql.Types.VARCHAR);
		if (friendInfo.getLastName() != null && friendInfo.getLastName().length() > 0)
		   pstmt.setString(3, friendInfo.getLastName());
		else
		   pstmt.setNull(3, java.sql.Types.VARCHAR);
		pstmt.setLong(4, ownerSfId);
		if (friendInfo.getPicSquare() != null && friendInfo.getPicSquare().length() > 0)
		   pstmt.setString(5, friendInfo.getPicSquare());
		else
		   pstmt.setNull(5, java.sql.Types.VARCHAR);
		if (friendInfo.getPicSmall() != null && friendInfo.getPicSmall().length() > 0)
		   pstmt.setString(6, friendInfo.getPicSmall());
		else
		   pstmt.setNull(6, java.sql.Types.VARCHAR);
		if (friendInfo.getPic() != null && friendInfo.getPic().length() > 0)
		   pstmt.setString(7, friendInfo.getPic());
		else
		   pstmt.setNull(7, java.sql.Types.VARCHAR);
		if (friendInfo.getPicBig() != null && friendInfo.getPicBig().length() > 0)
		   pstmt.setString(8, friendInfo.getPicBig());
		else
		   pstmt.setNull(8, java.sql.Types.VARCHAR);
		pstmt.executeUpdate();

		// Get "Contact IDs"
		long contactId = -1;
		ResultSet rs = pstmt.getGeneratedKeys();
		while (rs.next()) {
			contactId = rs.getLong(1);
			friendInfo.setContactId(contactId);
		}
		pstmt.close();
		rs.close();

		
		HashMap<String, HashSet<String>> accounts = friendInfo.getAccounts();
		if (accounts == null)
			return;
		HashSet<String> accountIDs = accounts.get(FBService.FACEBOOK_PROVIDER);
		if (accountIDs == null)
			return;
		
		// Put in initial FB account info
		pstmt = dbconn.prepareStatement(insertFBAcctQuery);
		for (String accountID : accountIDs) {
			pstmt.setLong(1, ownerSfId);
			pstmt.setLong(2, contactId);
			pstmt.setString(3, accountID);
			pstmt.setString(4, FBService.FACEBOOK_PROVIDER);
			pstmt.executeUpdate();	
		}
		pstmt.close();
		dbconn.close();
	}
	
	private void updateFriendInfo(SocialFlowsContactInfo friendInfo) throws SQLException
	{
		Connection dbconn = this.sfDBConnManager.getConnection();
		long ownerSfId = this.sfDBConnManager.getCurrentUser().getSfUserId();
		
		// UPDATES
		String square = "profilePicUrl_SquareThumbnail";
		if (friendInfo.getPicSquare() != null && friendInfo.getPicSquare().trim().length() > 0)
		   square = "'"+friendInfo.getPicSquare()+"'";

		String thumbnail = "profilePicUrl_Thumbnail";
		if (friendInfo.getPicSmall() != null && friendInfo.getPicSmall().trim().length() > 0)
		   thumbnail = "'"+friendInfo.getPicSmall()+"'";
		
		String medium = "profilePicUrl_Medium";
		if (friendInfo.getPic() != null && friendInfo.getPic().trim().length() > 0)
		   medium = "'"+friendInfo.getPic()+"'";
		
		String large = "profilePicUrl_Large";
		if (friendInfo.getPicBig() != null && friendInfo.getPicBig().trim().length() > 0)
		   large = "'"+friendInfo.getPicBig()+"'";
		
		// Update profile data
		String updateUserInfoQuery
		= "UPDATE socialflows_addressbooks AS sacct SET fullname=?, firstname=?, lastname=?, " +
		     "profilePicUrl_SquareThumbnail="+square+", profilePicUrl_Thumbnail="+thumbnail+", " +
		     "profilePicUrl_Medium="+medium+", profilePicUrl_Large="+large+" " +
		     "WHERE sacct.ownerSfId=? AND sacct.contactId="+friendInfo.getContactId();

		PreparedStatement pstmt
		= dbconn.prepareStatement(updateUserInfoQuery);
		pstmt.setString(1, friendInfo.getName());
		if (friendInfo.getFirstName() != null && friendInfo.getFirstName().length() > 0)
		   pstmt.setString(2, friendInfo.getFirstName());
		else
		   pstmt.setNull(2, java.sql.Types.VARCHAR);
		if (friendInfo.getLastName() != null && friendInfo.getLastName().length() > 0)
		   pstmt.setString(3, friendInfo.getLastName());
		else
		   pstmt.setNull(3, java.sql.Types.VARCHAR);
		pstmt.setLong(4, ownerSfId);
		
		pstmt.executeUpdate();
		pstmt.close();
		dbconn.close();
	}
	
	
	private static final String updateContactSocialWeight
	= "UPDATE socialflows_addressbooks AS sfaddrbook SET contactSocialWeight=? "+
	  "   WHERE sfaddrbook.ownerSfId=? AND sfaddrbook.contactId=?";
	
	private static final String updateContactEmails
	= "INSERT IGNORE INTO socialflows_email_directory(ownerSfId, contactId, email) "+
	  "   VALUES(?, ?, ?)";
	
	private static final String updateContactAliases
	= "INSERT IGNORE INTO socialflows_aliases(ownerSfId, contactId, alias) "+
	  "   VALUES(?, ?, ?)";
	
	private void updateFriendMetadata(SocialFlowsContactInfo friendInfo) throws SQLException
	{
		// Update metadata of social contact
        Connection dbconn = this.sfDBConnManager.getConnection();
		long ownerSfId = this.sfDBConnManager.getCurrentUser().getSfUserId();
		PreparedStatement pstmt = null;
		
		try {
    		// Update with new social weighting, as determined by Dunbar analysis
    		pstmt = dbconn.prepareStatement(updateContactSocialWeight);
    		pstmt.setInt(1, friendInfo.getSocialWeight());
    		pstmt.setLong(2, ownerSfId);
    		pstmt.setLong(3, friendInfo.getContactId());
    		pstmt.executeUpdate();        			
		}
		catch (SQLException sqle) {
			System.out.println("ERROR WHILE UPDATING SOCIAL WEIGHT for "+friendInfo.getName()+": "+sqle.getMessage());
			sqle.printStackTrace();
		}
		finally {
			try {
			  pstmt.close();
			} catch (SQLException sqle) {}
		}
		
		/*
        // Update and insert only new email addresses
        Set<String> emails = friendInfo.getEmailAddresses();
        Set<String> emailsDiff = new HashSet<String>();
        if (additionalEmails != null) {
            for (int j = 0; j < additionalEmails.length(); j++) {
                String emailAddr = additionalEmails.optString(j, null);
                if (emailAddr == null || emailAddr.trim().length() <= 0)
                    continue;

                emailAddr = emailAddr.trim();
                if (emails != null && emails.contains(emailAddr))
                    continue;
                else {
                    friendInfo.addEmailAddress(emailAddr);
                    emailsDiff.add(emailAddr);
                }
            }
        }
        */

        try {
            pstmt = dbconn.prepareStatement(updateContactEmails);
            if (friendInfo.getEmailAddresses() != null && friendInfo.getEmailAddresses().size() > 0) {
                for (String email : friendInfo.getEmailAddresses()) {
            		pstmt.setLong(1, ownerSfId);
            		pstmt.setLong(2, friendInfo.getContactId());
            		pstmt.setString(3, email);
            		pstmt.addBatch();
            		
            		//System.out.println("ADDING TO DB EMAIL <"+email+"> for "+friendInfo.getName()+" (contactId:"+friendInfo.getContactId()+")");
                }
                pstmt.executeBatch();
            }
		}
		catch (SQLException sqle) {
			System.out.println("ERROR WHILE UPDATING EMAILS for "+friendInfo.getName()+": "+sqle.getMessage());
			sqle.printStackTrace();
		}
		finally {
			try {
			  pstmt.close();
			} catch (SQLException sqle) {}
		}

		/*
        // Update and insert only new aliases/alternative names
        Set<String> names = friendInfo.getNames();
        Set<String> namesDiff = new HashSet<String>();
        if (additionalAliases != null) {
            for (int j = 0; j < additionalAliases.length(); j++) {
                String name = additionalAliases.optString(j, null);
                if (name == null || name.trim().length() <= 0)
                    continue;
                name = name.trim();
                if (names != null && names.contains(name))
                    continue;
                else {
                    friendInfo.addName(name);
                    namesDiff.add(name);
                }
            }
        }
        */
        
        try {
            pstmt = dbconn.prepareStatement(updateContactAliases);
            if (friendInfo.getNames() != null && friendInfo.getNames().size() > 0) {
                for (String alias : friendInfo.getNames()) {
                	pstmt.setLong(1, ownerSfId);
            		pstmt.setLong(2, friendInfo.getContactId());
            		pstmt.setString(3, alias);
            		pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
		}
		catch (SQLException sqle) {
			System.out.println("ERROR WHILE UPDATING ALIASES for "+friendInfo.getName()+": "+sqle.getMessage());
			sqle.printStackTrace();
		}
		finally {
			try {
			  pstmt.close();
			} catch (SQLException sqle) {}
		}
		
		dbconn.close();
	}
	

	public static final String clearAddressbookData
	= "DELETE FROM socialflows_addressbooks WHERE ownerSfId=";
	public static final String clearAccountsData
	= "DELETE FROM socialflows_accounts_directory WHERE ownerSfId=";
	public static final String clearEmailData
	= "DELETE FROM socialflows_email_directory WHERE ownerSfId=";
	public static final String clearPhoneData
	= "DELETE FROM socialflows_phone_directory WHERE ownerSfId=";
	public static final String clearAliasData
	= "DELETE FROM socialflows_aliases WHERE ownerSfId=";
	
	public void clearAllData()
	{
		Connection dbconn = null;
		long ownerSfId = this.sfDBConnManager.getCurrentUser().getSfUserId();
		Statement stmt = null;
		
		try {
			dbconn = this.sfDBConnManager.getConnection();
			stmt = dbconn.createStatement();
			stmt.addBatch(clearAddressbookData+ownerSfId);
			stmt.addBatch(clearAccountsData+ownerSfId);
			stmt.addBatch(clearEmailData+ownerSfId);
			stmt.addBatch(clearPhoneData+ownerSfId);
			stmt.addBatch(clearAliasData+ownerSfId);
			stmt.executeBatch();
		}
		catch (SQLException sqle) {
			System.out.println("CLEAR ADDRESSBOOK DATA ERROR: "+sqle.getMessage());
			sqle.printStackTrace();
		}
		finally {
			try {
				if (dbconn != null) dbconn.close();
				if (stmt != null) stmt.close();
			} catch (SQLException sqle) {}
		}
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
	

}
