package edu.stanford.socialflows.contacts;

import java.net.MalformedURLException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.stanford.prpl.api.Identity;
import edu.stanford.prpl.api.Resource;
import edu.stanford.prpl.api.PRPLAppClient;
import edu.stanford.prpl.api.QueryResultIterator;
import edu.stanford.prpl.api.QueryResultIterator.Solution;
import edu.stanford.prpl.app.common.resource.type.Account;
import edu.stanford.prpl.app.common.resource.type.Person;
import edu.stanford.prpl.app.common.resource.type.Photo;
import edu.stanford.prpl.impl.client.directory.DirectoryClient;
import edu.stanford.prpl.impl.directory.DirectoryCommon;

import edu.stanford.socialflows.contacts.PrPlContactInfo;
import edu.stanford.socialflows.contacts.PrPlPersonIdentityMatcher;
import edu.stanford.socialflows.util.StatusProvider;


public class PrPlAddressBook implements StatusProvider
{
	private PRPLAppClient prplService_ = null;
	private DirectoryClient dirClient_ = null;
	
	private List<PrPlContactInfo> myFriends = null;
	private HashMap<String, PrPlContactInfo> myFriendsByEmail = null;
	private HashMap<String, PrPlContactInfo> myFriendsByURI = null;  // resource uri to Person
	
	private int numFriends = 0;
	private HashMap<String, PrPlContactInfo> myFriendsByPrPlIdentity = null; // include just-only Identities

	
	private static String datalogQuerySimple
	= "PERSON(?p, ?fullname)? \n" +
	  "PERSON(?p, ?fullname):- " +
	  "(?p a '<http://prpl.stanford.edu/#Person>'), " +
	  "(?p, '<http://prpl.stanford.edu/#fullName>', ?fullname).";
	
	
	
	private static String datalogQueryFBData
	= "PERSON(?p, ?fullname, ?fbUid, ?fbProfileUrl)? \n" +
	  "PERSON(?p, ?fullname, ?fbUid, ?fbProfileUrl):- " +
	  "  PERSONINFO(?p, ?fullname, ?acct), " +
	  "  ACCTINFO(?acct, ?fbUid, ?fbProfileUrl). \n" +
	  "PERSONINFO(?p, ?fullname, ?acct):- " +
	  "  (?p a '<http://prpl.stanford.edu/#Person>'), " +
	  "  (?p, '<http://prpl.stanford.edu/#fullName>', ?fullname), " +
	  "  (?p, '<http://prpl.stanford.edu/#account>', ?acct). \n" +
	  "ACCTINFO(?acct, ?fbUid, ?fbProfileUrl):- " +
	  "  (?acct, '<http://prpl.stanford.edu/#acctName>', 'Facebook'), " +
	  "  (?acct, '<http://prpl.stanford.edu/#fbUid>', ?fbUid), " +
	  "  (?acct, '<http://prpl.stanford.edu/#fbProfileUrl>', ?fbProfileUrl).";
	
	
	private static String datalogQueryNonFBData
	= "PERSON(?p, ?fullname)? \n" +
	  "PERSON(?p, ?fullname):- " +
	  "  NOACCT(?p, ?fullname). \n" +
	  "PERSON(?p, ?fullname):- " +
	  "  NONFBACCT(?p, ?fullname). \n" +
	  "NOACCT(?p, ?fullname):- " +
	  "  (?p a '<http://prpl.stanford.edu/#Person>'), " +
	  "  !(?p, '<http://prpl.stanford.edu/#account>', _), " +
	  "  (?p, '<http://prpl.stanford.edu/#fullName>', ?fullname). \n" +
	  "NONFBACCT(?p, ?fullname):- " +
	  "  (?p a '<http://prpl.stanford.edu/#Person>'), " +
	  "  (?p, '<http://prpl.stanford.edu/#account>', ?acct), " +
	  "  !(?acct, '<http://prpl.stanford.edu/#acctName>', 'Facebook'), " +
	  "  (?p, '<http://prpl.stanford.edu/#fullName>', ?fullname). ";	
	

	
	public PrPlAddressBook(PRPLAppClient prplConn)
	{
		this.prplService_ = prplConn;
	}
	
	public static PrPlContactInfo createContact()
	{
		return new PrPlContactInfo();
	}
	
	public void setPrPlButlerConnection(PRPLAppClient prplConn)
	{
		this.prplService_ = prplConn;
	}
	
	private void initializeAddressBook()
	{
		this.myFriends        = new ArrayList<PrPlContactInfo>();
		this.myFriendsByEmail = new HashMap<String, PrPlContactInfo>();
		this.myFriendsByURI   = new HashMap<String, PrPlContactInfo>();
		this.myFriendsByPrPlIdentity = new HashMap<String, PrPlContactInfo>();
		this.numFriends = 0;
	}
	
	private boolean buildPrPlAddressBook()
	{
		initializeAddressBook();
		
		//System.out.println("Datalog query for FB Data:\n"+datalogQueryFBData);
		this.numFriends = 0;
		
		// First, get all Persons/Contacts from PCB that are mapped to a user's FB social graph
		processPersonResources(datalogQueryFBData);
		
		//System.out.println("Datalog query for Non-FB Data:\n"+datalogQueryNonFBData);
		
		// Second, get all other Persons/Contacts from PCB that are not sourced from a user's FB social graph
		processPersonResources(datalogQueryNonFBData);
		
		//System.out.println("buildPrPlAddressBook(): Synching PrPl Identities with Persons, and matching unassigned Persons with current PrPl users");
		syncPersonToPrPlIdentity();
		//matchUnassignedPersonsToPrPlIdentities();
		
		this.setStatusMessage("");
		return true;
	}
	
	public List<PrPlContactInfo> getUnifiedFriendsList()
	{
		if (this.myFriends == null)
			this.buildPrPlAddressBook();
		return this.myFriends;
	}
	
	public HashMap<String, PrPlContactInfo> getEmailToFriendsMap()
	{
		if (this.myFriendsByEmail == null)
			this.buildPrPlAddressBook();
		return this.myFriendsByEmail;
	}
	
	public HashMap<String, PrPlContactInfo> getURIToFriendsMap()
	{
		if (this.myFriendsByURI == null)
			this.buildPrPlAddressBook();
		return this.myFriendsByURI;
	}
	
	public PrPlContactInfo queryFriendByEmail(String email)
	{
		if (this.myFriendsByEmail == null)
			this.buildPrPlAddressBook();
		return this.myFriendsByEmail.get(email);
	}
	
	
	
	
	private static String compressedDatalogQueryContacts
	= "All(?person, ?s, ?p, ?o)? \n" +
	  "All(?s, ?s, ?p, ?o) :- PERSONINFO(?s, ?p, ?o). \n" +
	  "All(?person, ?s, ?p, ?o) :- ACCOUNTINFO(?person, ?s, ?p, ?o). \n" +
	  "All(?person, ?s, ?p, ?o) :- PHOTOINFO(?person, ?s, ?p, ?o). \n" +
	  "PERSONINFO(?s, ?p, ?o) :- PERSON(?s), (?s, ?p, ?o). \n" +
	  "ACCOUNTINFO(?person, ?s, ?p, ?o) :- " +
	  "  PERSON(?person), (?person, '<http://prpl.stanford.edu/#account>', ?s), (?s, ?p, ?o). \n" +
	  "PHOTOINFO(?person, ?s, ?p, ?o) :- " +
	  "  PERSON(?person), (?person, '<http://prpl.stanford.edu/#profilePhoto>', ?s), (?s, ?p, ?o). \n" +
	  "PHOTOINFO(?person, ?s, ?p, ?o) :- " +
	  "  PERSON(?person), (?person, '<http://prpl.stanford.edu/#selectedProfilePhoto>', ?s), (?s, ?p, ?o).";
	
	
	/*** CAUTION: Too slow! ***/
	public List<PrPlContactInfo> queryFriendsByURI(List<String> uris)
	{
		// Fetch all related social contacts data        	
		String datalogQuery;
		List<PrPlContactInfo> friends = new ArrayList<PrPlContactInfo>();

		if (uris == null || uris.isEmpty())
 		   return null;
		else
		{
		   if (this.myFriendsByURI == null)
			   this.myFriendsByURI = new HashMap<String, PrPlContactInfo>();
		
		   // Construct query to retrieve data from given URIs
		   datalogQuery = PrPlAddressBook.compressedDatalogQueryContacts;
		   StringBuilder friendURIs = new StringBuilder(100);
		 
		   int numFriendsQueried = 0;
		   for (String uri : uris)
		   {
			   if (uri != null) 
			   {
				  PrPlContactInfo contact = this.myFriendsByURI.get(uri);
				  if (contact != null) {
					  friends.add(contact);
				  }
				  else {
					  numFriendsQueried++;
					  friendURIs.append("\nPERSON('<"+uri+">').");  
				  }
			   }
		   }
		   
		   if (numFriendsQueried == 0)
			   return friends;
		   datalogQuery += friendURIs.toString();
		}
		
		System.out.println("getFriendsByURI(): The datalog query is:\n"+datalogQuery);
		
		System.err.println("START OF QUERY FOR FRIENDS INFO");
		
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
    				return null;
    			}
    		}
    	}
		
		System.err.println("END OF QUERY FOR FRIENDS INFO");
		
		System.err.println("START OF METADATA RETRIEVAL FOR FRIENDS INFO");
		List<PrPlContactInfo> friendsQueried = processPersonResourcesOptimized(queryResults);
		System.err.println("END OF METADATA RETRIEVAL FOR FRIENDS INFO");
		
		friends.addAll(friendsQueried);
		return friends;
	}
	
	
	private List<PrPlContactInfo> processPersonResourcesOptimized(QueryResultIterator queryResults)
	{
		HashMap<String, PrPlContactInfo> friends = new HashMap<String, PrPlContactInfo>();
		PrPlContactInfo currentFriend = null;
		
		HashMap<String, JSONObject> metadataObjs = new HashMap<String, JSONObject>();
		JSONObject currentMetadataObj = null;
		
		// Don't add duplicates of a PrPl identity result 
    	// (Identity with a profile pic will appear twice in query results)
    	while (queryResults.hasNext()) 
        {
        	GregorianCalendar modifiedDate = null;
        	Resource person = null, subject = null;
        	Object object = null;
        	String metadata = null;
        	        	
            Solution s = queryResults.next();
            List<String> metadataNames = queryResults.getResultVars();
            for (String var : metadataNames) 
            {
                if (var.equals("?t"))
                    continue;
                Object value = s.get(var);
                
                //if (var.equals("modifiedDate") && value instanceof GregorianCalendar)
                //	 modifiedDate = (GregorianCalendar)value;
                if (var.equals("person"))
                	person = (Resource)value;
                else if (var.equals("s"))
                	subject = (Resource)value;
                else if (var.equals("p"))
                	metadata = (String)value;
                else if (var.equals("o"))
                	object = value;
            }
            
            System.out.println("QUERY RESULT: Processing a row of query results...");
            
            
            // Processing a Person resource/Contact object
            if (person.getURI().equals(subject.getURI())) {
            	if (currentFriend == null || !currentFriend.getResourceURI().equals(subject.getURI()))
            	{
            		currentFriend = friends.get(subject.getURI());
                	if (currentFriend == null) {
                		currentFriend = PrPlAddressBook.createContact();
                		currentFriend.setResourceURI(subject.getURI());
                		friends.put(subject.getURI(), currentFriend);
                	}
            	}
            	fillPrPlContactInfo(currentFriend, metadata, object);
            }
            else {
            	try 
            	{
            		if (currentMetadataObj == null || !currentMetadataObj.getString("PrPlResourceURI").equals(subject.getURI()))
                	{
                		currentMetadataObj = metadataObjs.get(subject.getURI());
                    	if (currentMetadataObj == null) {
                    		currentMetadataObj = new JSONObject();
                    		currentMetadataObj.put("PrPlResourceURI", subject.getURI());
                    		metadataObjs.put(subject.getURI(), currentMetadataObj);
                    	}
                	}
                	fillMetadataObj(currentMetadataObj, metadata, object);	
            	}
            	catch (JSONException jsone)
            	{
            		System.out.println("processPersonResourcesOptimized(): JSON error while storing non-Person resources");
        			jsone.printStackTrace();
            	}
            	
            	
            }
        }
    	
    	
    	// Fill in the remaining details for PrPl contacts
    	for (PrPlContactInfo friend : friends.values())
    	{	
    		try {
    			completePrPlContactInfo(friend, metadataObjs);
    		}
    		catch (JSONException jsone)
        	{
        		System.out.println("processPersonResourcesOptimized(): "+
        				           "Problem trying to complete PrPlContactInfo details for "+friend.getName()+" due to JSON error");
    			jsone.printStackTrace();
        	}
    	}
    	
    	List<PrPlContactInfo> friendsList = new ArrayList<PrPlContactInfo>(friends.size());
    	friendsList.addAll(friends.values());
    	return friendsList;
	}
	
	
	private void fillPrPlContactInfo(PrPlContactInfo friend, String metadata, Object value)
	{
		if (metadata == null)
			return;

		// System.out.println("METADATA-PERSON: Retrieving metadata for Person resource");
		
		GregorianCalendar modifiedDate = null;
		String fullname = null, firstname = null, lastname = null;
    	Identity identity = null;
		int socialWeight = 0;
		
    	// Get first name if it exists
    	if (metadata.equals(Person.FIRSTNAME_URI)) {
    		firstname = (String)value;
    		friend.setFirstName(firstname);
    	}
    	
    	// Get last name if it exists
    	else if (metadata.equals(Person.LASTNAME_URI)) {
			lastname = (String)value;
			friend.setLastName(lastname);
		}
		
		// Get full name if it exists
    	else if (metadata.equals(Person.FULLNAME_URI)) {
    		fullname = (String)value;
    		friend.setName(fullname);
    	}
		
    	// Get social weight if it exists
		else if (metadata.equals(PrPlContactInfo.SOCIALWEIGHT_URI)) {
			Integer socialWeightObj = (Integer)value;
			if (socialWeightObj != null) {
				socialWeight = socialWeightObj.intValue();
				friend.setSocialWeight(socialWeight);
			}
		}
		
		// Get selected profile pic if it exists
		else if (metadata.equals(PrPlContactInfo.SELECTED_PROFILEPHOTO_URI)) {
			Resource selectedPic = (Resource)value;
			if (selectedPic != null) {
				friend.setSelectedPicSquare(selectedPic.getURI());
			}
		}
		
		// Get default profile pic if it exists
		else if (metadata.equals(Person.PROFILE_PHOTO_URI)) {
			Resource defaultPic = (Resource)value;
			if (defaultPic != null) {
				friend.setPicSquare(defaultPic.getURI());
			}
		}
		
		// Get matched Identity if it exists
		else if (metadata.equals(PrPlContactInfo.PRPL_ID_URI)) {
			identity = (Identity)value;
			friend.setPrPlIdentity(identity);
		}
		
		// Get list of email addresses
		else if (metadata.equals(Person.EMAILADDRESS_URI)) 
		{
			String emailAddr = (String)value;
			if (emailAddr != null && emailAddr.trim().length() > 0) 
			{
				friend.addEmailAddress(emailAddr);
				// This might be an email-only contact with no fb profile
				if (friend.getProfileUrl() == null) {
					friend.setProfileUrl("mailto:"+emailAddr);
					friend.setTempContactId(emailAddr);
				}
			}
		}
		
		// Get list of aliases/names of the same person
		else if (metadata.equals(PrPlContactInfo.ALIAS_URI)) {
			String alias = (String)value;
			friend.addName(alias);
		}
    	
		// Get FB account & details if it exists
		else if (metadata.equals(Person.ACCOUNT_URI)) {
			Resource account = (Resource)value;
			if (account != null) {
				//friend.addAccount(account.getURI());
			}			
		}	

	}
	
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
	
	private void completePrPlContactInfo(PrPlContactInfo friend, 
										 HashMap<String, JSONObject> metadataObjs) throws JSONException
	{
		// Get selected profile pic if it exists
		if (friend.getSelectedPicSquare() != null) {
			String selectedPicUrl = null;
			JSONObject selectedPic = metadataObjs.get(friend.getSelectedPicSquare());
			if (selectedPic != null) {
				if (selectedPic.has(PrPlContactInfo.SQUARE_THUMBNAILHTTPURL_URI))
					friend.setSelectedPicSquare(selectedPic.getString(PrPlContactInfo.SQUARE_THUMBNAILHTTPURL_URI));
				if (selectedPic.has(Photo.THUMBNAILHTTPURL_URI))
					friend.setSelectedPicSmall(selectedPic.getString(Photo.THUMBNAILHTTPURL_URI));
				if (selectedPic.has(Photo.PHOTOHTTPURL_URI))
					friend.setSelectedPicBig(selectedPic.getString(Photo.PHOTOHTTPURL_URI));
			}
		}
		
		// Get default profile pic if it exists
		if (friend.getPicSquare() != null) {
			String defaultPicUrl = null;
			JSONObject defaultPic = metadataObjs.get(friend.getPicSquare());
			if (defaultPic != null) {
				if (defaultPic.has(PrPlContactInfo.SQUARE_THUMBNAILHTTPURL_URI))
					friend.setPicSquare(defaultPic.getString(PrPlContactInfo.SQUARE_THUMBNAILHTTPURL_URI));
				if (defaultPic.has(Photo.THUMBNAILHTTPURL_URI))
					friend.setPicSmall(defaultPic.getString(Photo.THUMBNAILHTTPURL_URI));
				if (defaultPic.has(Photo.PHOTOHTTPURL_URI))
					friend.setPicBig(defaultPic.getString(Photo.PHOTOHTTPURL_URI));
			}
		}
		
		/*
		// Get FB account & details if it exists
		HashSet<String> accounts = friend.getAccounts();
		if (accounts != null) {
			for (String account : accounts) {
				processAccount(friend, metadataObjs.get(account));
			}	
		}
		*/
		
		/*** Caching of social graph addressbook ***/
		
		// Makes it easier to pinpoint a person based on email address
		if (this.myFriendsByEmail == null)
			this.myFriendsByEmail = new HashMap<String, PrPlContactInfo>();
		if (friend.getEmailAddresses() != null) {
			for (String emailAddr : friend.getEmailAddresses()) {
				this.myFriendsByEmail.put(emailAddr.trim(), friend);
			}	
		}
		
		// Build up list of friends lazily
		if (this.myFriends == null)
			this.myFriends = new ArrayList<PrPlContactInfo>();
		this.myFriends.add(friend);
		this.myFriendsByURI.put(friend.getResourceURI(), friend);
		
		// Know which are PrPl friends
		if (friend.getPrPlIdentity() != null)
			this.assignedPersons.put(friend.getPrPlIdentity().getKey(), friend);
		else
			this.unassignedPersons.put(friend.getResourceURI(), friend);

	}
	
	private void processAccount(PrPlContactInfo friend, JSONObject account) throws JSONException
	{
		if (friend == null || account == null)
			return;
			
		String acctName = null;
		if (account.has(Account.ACCTNAME_URI)) 
			acctName = (String)account.get(Account.ACCTNAME_URI);
		if (acctName == null)
			return;
		else if (acctName.equals("Facebook"))
		{
			Object obj = null;
			String fbProfileUrl = null;
	    	long fbUid = -1;
			
			if (account.has(PrPlContactInfo.FB_PROFILEURL_URI))
				fbProfileUrl = (String)account.get(PrPlContactInfo.FB_PROFILEURL_URI);
			if (account.has(PrPlContactInfo.FB_UID_URI)) {
				obj = account.get(PrPlContactInfo.FB_UID_URI);
				if (obj instanceof Integer)
					fbUid = ((Integer)obj).longValue();
				else if (obj instanceof Long)
					fbUid = ((Long)obj).longValue();
			}

			if (fbUid >= 0) {
				// contact also has a FB acct
				//friend.setDummyVal(String.valueOf(fbUid));
				friend.setTempContactId("fbid"+fbUid);
			}
			friend.setProfileUrl(fbProfileUrl);
		}
		
	}
	
	
	
	
	public PrPlContactInfo lookupFriendByURI(String uri)
	{
		if (this.myFriendsByURI == null)
			return null;
		return this.myFriendsByURI.get(uri);
	}
	
	/*** Slower (maybe not), less optimized social graph retrieval functions ***/
	
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
	
	private void storeContact(PrPlContactInfo contact)
	{
		this.myFriendsByURI.put(contact.getResourceURI(), contact);
		
		// Makes it easier to pinpoint a person based on email address
		if (this.myFriendsByEmail == null)
			this.myFriendsByEmail = new HashMap<String, PrPlContactInfo>();
		if (contact.getEmailAddresses() != null) {
			for (String emailAddr : contact.getEmailAddresses()) {
				this.myFriendsByEmail.put(emailAddr.trim(), contact);
			}	
		}
		
		// Build up list of friends lazily
		if (this.myFriends == null)
			this.myFriends = new ArrayList<PrPlContactInfo>();
		this.myFriends.add(contact);
		
		// Know which are PrPl friends
		if (contact.getPrPlIdentity() != null)
			this.assignedPersons.put(contact.getPrPlIdentity().getKey(), contact);
		else
			this.unassignedPersons.put(contact.getResourceURI(), contact);
	}
	
	public PrPlContactInfo queryFriendByResource(Resource friend)
	{
		if (this.myFriendsByURI == null) {
			this.myFriendsByURI = new HashMap<String, PrPlContactInfo>();
		}

		PrPlContactInfo contact = this.myFriendsByURI.get(friend.getURI());
		if (contact != null)
			return contact;
		contact = getPrPlContactInfo(friend);
		storeContact(contact);
		return contact;
	}

	
	public PrPlContactInfo addIdentityAsFriend(Identity prplFriend)
	{
		if (prplFriend == null)
			return null;
		if (this.myFriendsByPrPlIdentity == null)
			this.initializeAddressBook();

		PrPlContactInfo identityOnly = this.myFriendsByPrPlIdentity.get(prplFriend.getURI());
		if (identityOnly == null) {
			identityOnly = PrPlAddressBook.createContact();
			identityOnly.setPrPlIdentity(prplFriend);
			identityOnly.setName(prplFriend.getName());
			this.myFriendsByPrPlIdentity.put(prplFriend.getURI(), identityOnly);
		}
		return identityOnly;
	}
	
	
	// Add Friend (adds if new, replaces if existing) to local 
	// application cache but does not save it to PrPl backend
	public PrPlContactInfo addFriend(PrPlContactInfo newFriend)
	{
		if (newFriend == null)
			return null;
		if (this.myFriends == null)
			this.initializeAddressBook();
		
		// Find if this contact previously existed
		PrPlContactInfo existingEntry = null;
		String uri = newFriend.getResourceURI();
		if (uri != null && uri.trim().length() > 0 && !uri.trim().equals("null"))
			existingEntry = this.myFriendsByURI.get(uri);
		
		// Look up by known emails
		if (existingEntry == null && newFriend.getEmailAddresses() != null) {
			for (String email : newFriend.getEmailAddresses()) {
			    existingEntry = this.myFriendsByEmail.get(email);
			    if (existingEntry != null)
			       break;
			}
		}
		
		// Remove existing entry if it exists
		if (existingEntry != null) {
			this.myFriends.remove(existingEntry);
			if (existingEntry.getResourceURI() != null)
				this.myFriendsByURI.remove(existingEntry.getResourceURI());
			if (existingEntry.getEmailAddresses() != null) {
				for (String email : existingEntry.getEmailAddresses()) {
				    this.myFriendsByEmail.remove(email);
				}
			}	
		}
		
		
		// Add the new contact
		this.myFriends.add(newFriend);
		
		// Update the other two lists
		if (uri != null && uri.trim().length() > 0 && !uri.trim().equals("null")) {
			this.myFriendsByURI.put(uri, newFriend);
			
			System.out.println("ADDED FRIEND "+newFriend.getName()+" into AddressBook as <"+newFriend.getResourceURI()+">");
			
		}
		if (newFriend.getEmailAddresses() != null) {
			for (String email : newFriend.getEmailAddresses())
				this.myFriendsByEmail.put(email, newFriend);
		}

		return newFriend;
	}


	
	
	private void processPersonResources(String datalogQuery)
	{
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
    			// this.prplService_.renewSession();
    		}
    	}
		
		
		// Don't add duplicates of a PrPl identity result 
    	// (Identity with a profile pic will appear twice in query results)
    	while (queryResults.hasNext()) 
        {
        	GregorianCalendar modifiedDate = null;
        	Resource person = null;
        	
        	String fullname = null, firstname = null, lastname = null, fbProfileUrl = null;
        	long fbUid = -1;
        	
            Solution s = queryResults.next();
            List<String> metadataNames = queryResults.getResultVars();
            for (String var : metadataNames) 
            {
                if (var.equals("?t"))
                    continue;
                Object value = s.get(var);
                
                if (var.equals("modifiedDate") && value instanceof GregorianCalendar)
                	modifiedDate = (GregorianCalendar)value;
                else if (var.equals("fullname"))
                	fullname = (String)value;
                else if (var.equals("firstname"))
                	firstname = (String)value;
                else if (var.equals("lastname"))
                	lastname = (String)value;
                else if (var.equals("fbUid")) {
                	if (value instanceof Integer) {
                		fbUid = ((Integer)value).longValue();
                		//System.out.println("fbUid is Integer object");
                	}
                	else if (value instanceof Long) {
                		fbUid = ((Long)value).longValue();
                		//System.out.println("fbUid is Long object");
                	}
                }
                else if (var.equals("fbProfileUrl"))
                	fbProfileUrl = (String)value;
                else if (var.equals("p"))
                	person = (Resource)value;
            }

            if (person != null)
            {
            	numFriends++;
            	if (numFriends == 1) {
            	   this.setStatusMessage("Integrating "+numFriends+" individual into your Unified Social Contacts List...");
            	}
            	else {
            	   this.setStatusMessage("Integrating "+numFriends+" individuals into your Unified Social Contacts List...");
            	}
            	
            	// DEBUG
            	//if (fbUid != 0)
            	//	System.out.println("Retrieving Person resource fullname='"+fullname+"' (FB id "+fbUid+") (URI: "+person.getURI()+")");
            	//else
            	//	System.out.println("Retrieving Person resource fullname='"+fullname+"' (URI: "+person.getURI()+")");
            	
            	// Get contact info object
    			PrPlContactInfo contact 
    			= getPrPlContactInfo(person, fullname, firstname, lastname,
                                     fbProfileUrl, fbUid);
    			
    			// Makes it easier to pinpoint a person based on email address
    			if (contact.getEmailAddresses() != null) {
    				for (String emailAddr : contact.getEmailAddresses()) {
        				this.myFriendsByEmail.put(emailAddr.trim(), contact);
        			}	
    			}
    			this.myFriends.add(contact);
    			this.myFriendsByURI.put(contact.getResourceURI(), contact);
    			if (contact.getPrPlIdentity() != null)
    				this.assignedPersons.put(contact.getPrPlIdentity().getKey(), contact);
    			else
    				this.unassignedPersons.put(contact.getResourceURI(), contact);
    		}

        }
	}
	
	

	private void processPersonResourcesSimple()
	{
		QueryResultIterator queryResults;        		
		for (int i = 0; true; i++)
    	{
    		try {
    			queryResults = this.prplService_.runDatalogQuery(datalogQuerySimple);
    			break;
    		}
    		catch (Exception e) {
    			if (i == 3) {
    				e.printStackTrace();
    				System.out.println("Problem trying to query for PrPl Person resources from PCB");
    				return;
    			}
    			// this.prplService_.renewSession();
    		}
    	}
		
		
		// Don't add duplicates of a PrPl identity result 
    	// (Identity with a profile pic will appear twice in query results)
    	while (queryResults.hasNext()) 
        {
        	String fullname = null;
        	GregorianCalendar modifiedDate = null;
        	Identity identity = null;
        	Resource person = null;
        	
            Solution s = queryResults.next();
            List<String> metadataNames = queryResults.getResultVars();
            for (String var : metadataNames) 
            {
                if (var.equals("?t"))
                    continue;
                Object value = s.get(var);
                
                if (var.equals("modifiedDate") && value instanceof GregorianCalendar)
                	modifiedDate = (GregorianCalendar)value;
                else if (var.equals("fullname"))
                	fullname = (String)value;
                else if (var.equals("p"))
                	person = (Resource)value;
            }
            
            // DEBUG
        	System.out.println("Retrieving Person resource fullname='"+fullname+"'");
        	
            String firstname = null, lastname = null, acctName = null,
            	   fbProfileUrl = null, selectedPicUrl = null, defaultPicUrl = null;
            int socialWeight = 0; long fbUid = -1;
            Resource fbAccount = null;
            
            if (person != null)
            {
            	// Get first name
    			Object[] attributes = person.getMetadata(Person.FIRSTNAME_URI);
    			if (attributes != null)
    				firstname = (String)attributes[0];  
    			
    			// Get last name
    			attributes = person.getMetadata(Person.LASTNAME_URI);
    			if (attributes != null)
    				lastname = (String)attributes[0];
    			
    			// Get FB account if it exists
    			attributes = person.getMetadata(Person.ACCOUNT_URI);
    			if (attributes != null) {
    				fbAccount = (Resource)attributes[0];
    				if (fbAccount != null) {
    					attributes = fbAccount.getMetadata(Account.ACCTNAME_URI);
    					if (attributes != null) {
    						acctName = (String)attributes[0];
    						if (acctName.equals("Facebook"))
    						{
    							attributes = fbAccount.getMetadata(PrPlContactInfo.FB_UID_URI);
    							if (attributes != null) {
    								Object fbUidObj = attributes[0];
    								if (fbUidObj instanceof Integer) {
    			                		fbUid = ((Integer)fbUidObj).longValue();
    			                		System.out.println("fbUid is Integer object");
    			                	}
    			                	else if (fbUidObj instanceof Long) {
    			                		fbUid = ((Long)fbUidObj).longValue();
    			                		System.out.println("fbUid is Long object");
    			                	}
    							}

    							attributes = fbAccount.getMetadata(PrPlContactInfo.FB_PROFILEURL_URI);
    							if (attributes != null)
    								fbProfileUrl = (String)attributes[0];
    						}
    					}
    					
    				}
    				else {
    					System.out.println("Account resource is null for '"+fullname+"'");
    				}
    			}
    			
    			// Get matched Identity if it exists
    			attributes = person.getMetadata(PrPlContactInfo.PRPL_ID_URI);
    			if (attributes != null) {
    				identity = (Identity)attributes[0];
    			}
    			
    			// Get social weight if it exists
    			Integer socialWeightObj = null;
    			attributes = person.getMetadata(PrPlContactInfo.SOCIALWEIGHT_URI);
    			if (attributes != null) {
    				socialWeightObj = (Integer)attributes[0];
    				if (socialWeightObj != null) {
    					socialWeight = socialWeightObj.intValue();
    				}
    			}
    			
    			// Get selected profile pic if it exists
    			Resource selectedPic = null;
    			attributes = person.getMetadata(PrPlContactInfo.SELECTED_PROFILEPHOTO_URI);
    			if (attributes != null) {
    				selectedPic = (Resource)attributes[0];
    				if (selectedPic != null) {
    					attributes = selectedPic.getMetadata(PrPlContactInfo.SQUARE_THUMBNAILHTTPURL_URI);
    					if (attributes != null)
    						selectedPicUrl = (String)attributes[0];
    					else
    					{
    						attributes = selectedPic.getMetadata(Photo.THUMBNAILHTTPURL_URI);
    						if (attributes != null)
        						selectedPicUrl = (String)attributes[0];
    					}
    				}
    			}
    			
    			// Get default profile pic if it exists
    			Resource defaultPic = null;
    			attributes = person.getMetadata(Person.PROFILE_PHOTO_URI);
    			if (attributes != null) {
    				defaultPic = (Resource)attributes[0];
    				if (defaultPic != null) {
    					attributes = defaultPic.getMetadata(PrPlContactInfo.SQUARE_THUMBNAILHTTPURL_URI);
    					if (attributes != null)
    						defaultPicUrl = (String)attributes[0];
    					else
    					{
    						attributes = defaultPic.getMetadata(Photo.THUMBNAILHTTPURL_URI);
    						if (attributes != null)
    							defaultPicUrl = (String)attributes[0];
    					}
    				}
    			}
    			
    			// Create In-Situ object
    			PrPlContactInfo contact = PrPlAddressBook.createContact();
    			contact.setResourceURI(person.getURI());
    			contact.setName(fullname);
    			contact.setFirstName(firstname);
    			contact.setLastName(lastname);
    			contact.setSocialWeight(socialWeight);
    			if (fbUid >= 0) {
    				// contact also has a FB acct
    				//contact.setDummyVal(String.valueOf(fbUid));
    				contact.setTempContactId("fbid"+fbUid);
    			}
    			contact.setSelectedPicSquare(selectedPicUrl);
    			contact.setPicSquare(defaultPicUrl);
    			contact.setProfileUrl(fbProfileUrl);
    			contact.setPrPlIdentity(identity);

    			// Get list of email addresses
    			String emailAddr = null;
    			attributes = person.getMetadata(Person.EMAILADDRESS_URI);
    			if (attributes != null) 
    			{
    				boolean setMailto = false;
    				for (int i = 0; i < attributes.length; i++) {
    					emailAddr = (String)attributes[i];
    					if (emailAddr != null && emailAddr.trim().length() > 0) 
    					{
    						contact.addEmailAddress(emailAddr);
    						// Makes it easier to pinpoint a person based on email address
        					this.myFriendsByEmail.put(emailAddr.trim(), contact);
        					
        					// This might be an email-only contact with no fb profile
        					if (fbProfileUrl == null && !setMailto) {
        						contact.setProfileUrl("mailto:"+emailAddr);
        						setMailto = true;
        					}
    					}
    				}
    			}
    			
    			// Get list of aliases/names of the same person
    			String altName = null;
    			attributes = person.getMetadata(PrPlContactInfo.ALIAS_URI);
    			if (attributes != null) {
    				for (int i = 0; i < attributes.length; i++) {
    					altName = (String)attributes[i];
    					contact.addName(altName);
    				}
    			}
    			
    			this.myFriends.add(contact);
    			this.myFriendsByURI.put(contact.getResourceURI(), contact);
    			if (identity != null)
    				this.assignedPersons.put(identity.getKey(), contact);
    			else
    				this.unassignedPersons.put(contact.getResourceURI(), contact);
    		}

        }
	}

	private PrPlContactInfo getPrPlContactInfo(Resource person)
	{
		return getPrPlContactInfo(person, null, null, null, null, -1);
	}
	
	private PrPlContactInfo getPrPlContactInfo(Resource person, 
			                                   String fullname, String firstname, String lastname,
			                                   String fbProfileUrl, long fbUid)
	{
		Object[] attributes = null;
		Identity identity = null;
		int socialWeight = 0;
		
		// person.getMetadataProperties()
		//System.out.println("\nGETTING CONTACT METADATA FOR "+person.getName());
    	
    	// Get first name if it exists
    	if (firstname == null) {
    		//System.out.println("METADATA-PERSON: Retrieving metadata for Person resource");
			attributes = person.getMetadata(Person.FIRSTNAME_URI);
			if (attributes != null)
				firstname = (String)attributes[0];	
    	}
    	
    	// Get last name if it exists
		if (lastname == null) {
			//System.out.println("METADATA-PERSON: Retrieving metadata for Person resource");
			attributes = person.getMetadata(Person.LASTNAME_URI);
			if (attributes != null)
				lastname = (String)attributes[0];	
		}
		
		// Get full name if it exists
    	if (fullname == null) {
    		//System.out.println("METADATA-PERSON: Retrieving metadata for Person resource");
			attributes = person.getMetadata(Person.FULLNAME_URI);
			if (attributes != null)
				fullname = (String)attributes[0];	
    	}
		
    	// Get social weight if it exists
		Integer socialWeightObj = null;
		//System.out.println("METADATA-PERSON: Retrieving metadata for Person resource");
		attributes = person.getMetadata(PrPlContactInfo.SOCIALWEIGHT_URI);
		if (attributes != null) {
			socialWeightObj = (Integer)attributes[0];
			if (socialWeightObj != null) {
				socialWeight = socialWeightObj.intValue();
			}
		}
		
		// Get selected profile pic if it exists
    	String selectedPicUrl = null;
		Resource selectedPic = null;
		//System.out.println("METADATA-PERSON: Retrieving metadata for Person resource");
		attributes = person.getMetadata(PrPlContactInfo.SELECTED_PROFILEPHOTO_URI);
		if (attributes != null) {
			selectedPic = (Resource)attributes[0];
			if (selectedPic != null) {
				//System.out.println("METADATA-PERSON: Retrieving metadata for Person resource");
				attributes = selectedPic.getMetadata(PrPlContactInfo.SQUARE_THUMBNAILHTTPURL_URI);
				if (attributes != null)
					selectedPicUrl = (String)attributes[0];
				else
				{
					//System.out.println("METADATA-PERSON: Retrieving metadata for Person resource");
					attributes = selectedPic.getMetadata(Photo.THUMBNAILHTTPURL_URI);
					if (attributes != null)
						selectedPicUrl = (String)attributes[0];
				}
			}
		}
		
		// Get default profile pic if it exists
		String defaultPicUrl = null;
		Resource defaultPic = null;
		//System.out.println("METADATA-PERSON: Retrieving metadata for Person resource");
		attributes = person.getMetadata(Person.PROFILE_PHOTO_URI);
		if (attributes != null) {
			defaultPic = (Resource)attributes[0];
			if (defaultPic != null) {
				//System.out.println("METADATA-PERSON: Retrieving metadata for Person resource");
				attributes = defaultPic.getMetadata(PrPlContactInfo.SQUARE_THUMBNAILHTTPURL_URI);
				if (attributes != null)
					defaultPicUrl = (String)attributes[0];
				else
				{
					//System.out.println("METADATA-PERSON: Retrieving metadata for Person resource");
					attributes = defaultPic.getMetadata(Photo.THUMBNAILHTTPURL_URI);
					if (attributes != null)
						defaultPicUrl = (String)attributes[0];
				}
			}
		}
		
		// Get matched Identity if it exists
		//System.out.println("METADATA-PERSON: Retrieving metadata for Person resource");
		attributes = person.getMetadata(PrPlContactInfo.PRPL_ID_URI);
		if (attributes != null) {
			identity = (Identity)attributes[0];
		}
		
		// Get FB account & details if it exists
		if (fbProfileUrl == null || fbUid < 0) {
			Resource account = null;
			//System.out.println("METADATA-PERSON: Retrieving metadata for Person resource");
			attributes = person.getMetadata(Person.ACCOUNT_URI);
			if (attributes != null) {
				for (int i = 0; i < attributes.length; i++) {
					account = (Resource)attributes[i];
					if (account != null)
					{
						Object[] acctAttributes = null;
						String acctName = null;
						
						// Skip if not a Facebook account
						//System.out.println("METADATA-ACCOUNT: Retrieving metadata for Account resource");
						acctAttributes = account.getMetadata(Account.ACCTNAME_URI);
						if (acctAttributes != null) {
							acctName = (String)acctAttributes[0];
							if (acctName == null || !acctName.equals("Facebook"))
								continue;
						}
						else
							continue;
						
						//System.out.println("METADATA-ACCOUNT: Retrieving metadata for Account resource");
						acctAttributes = account.getMetadata(PrPlContactInfo.FB_PROFILEURL_URI);
						if (acctAttributes != null)
							fbProfileUrl = (String)acctAttributes[0];
						//System.out.println("METADATA-ACCOUNT: Retrieving metadata for Account resource");
						acctAttributes = account.getMetadata(PrPlContactInfo.FB_UID_URI);
						if (acctAttributes != null) {
							if (acctAttributes[0] instanceof Integer)
								fbUid = ((Integer)acctAttributes[0]).longValue();
							else if (acctAttributes[0] instanceof Long)
								fbUid = ((Long)acctAttributes[0]).longValue();
						}
						break;
					}
				}
			}			
		}		


		// Create PrPlContactInfo object
		PrPlContactInfo contact = PrPlAddressBook.createContact();
		contact.setResourceURI(person.getURI());
		contact.setName(fullname);
		contact.setFirstName(firstname);
		contact.setLastName(lastname);
		contact.setSocialWeight(socialWeight);
		if (fbUid >= 0) {
			// contact also has a FB acct
			//contact.setDummyVal(String.valueOf(fbUid));
			contact.setTempContactId("fbid"+fbUid);
		}
		contact.setSelectedPicSquare(selectedPicUrl);
		contact.setPicSquare(defaultPicUrl);
		contact.setProfileUrl(fbProfileUrl);
		contact.setPrPlIdentity(identity);

		// Get list of email addresses
		String emailAddr = null;
		//System.out.println("METADATA-PERSON: Retrieving metadata for Person resource");
		attributes = person.getMetadata(Person.EMAILADDRESS_URI);
		if (attributes != null) 
		{
			boolean setMailto = false;
			for (int i = 0; i < attributes.length; i++) {
				emailAddr = (String)attributes[i];
				if (emailAddr != null && emailAddr.trim().length() > 0) 
				{
					contact.addEmailAddress(emailAddr);
					// This might be an email-only contact with no fb profile
					if (fbProfileUrl == null && !setMailto) {
						contact.setProfileUrl("mailto:"+emailAddr);
						contact.setTempContactId(emailAddr);
						setMailto = true;
					}
				}
			}
		}
		
		// Get list of aliases/names of the same person
		String altName = null;
		//System.out.println("METADATA-PERSON: Retrieving metadata for Person resource");
		attributes = person.getMetadata(PrPlContactInfo.ALIAS_URI);
		if (attributes != null) {
			for (int i = 0; i < attributes.length; i++) {
				altName = (String)attributes[i];
				contact.addName(altName);
			}
		}
		
		return contact;
	}
	

	
	// prpl-id-key to Person
	private HashMap<String, PrPlContactInfo> assignedPersons = new HashMap<String, PrPlContactInfo>();
	// resource uri to Person
	private HashMap<String, PrPlContactInfo> unassignedPersons = new HashMap<String, PrPlContactInfo>();
	
	private void syncPersonToPrPlIdentity()
	{
		String datalogQuery
		= "FRIENDINFO(?i, ?name, ?key)? \n" +
		  "FRIENDINFO(?i, ?name, ?key):- " +
  	      "(?i a '<http://prpl.stanford.edu/#Identity>'), !(?i, '<http://prpl.stanford.edu/#isOwnDefault>', _), " +
  	      "(?i, '<http://prpl.stanford.edu/#name>', ?name), (?i, '<http://prpl.stanford.edu/#key>', ?key).";

  	    // Identity[] identities = null;
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
    				System.out.println("Problem trying to query for Person resources from PCB");
    				return;
    			}
    		}
    	}
		

		// Look for Identities not yet assigned to Person resources
        while (queryResults.hasNext()) 
        {
        	String name = null, key = null; 
        	GregorianCalendar modifiedDate = null;
        	Identity identity = null;
        	
            Solution s = queryResults.next();
            List<String> metadataNames = queryResults.getResultVars();
            for (String var : metadataNames) 
            {
                if (var.equals("?t"))
                    continue;
                Object value = s.get(var);
                
                // System.out.println(var + " --> " + value);
                
                if (var.equals("modifiedDate") && value instanceof GregorianCalendar)
                	modifiedDate = (GregorianCalendar)value;
                else if (var.equals("name"))
                	name = (String)value;
                else if (var.equals("key"))
                	key = (String)value;
                else if (var.equals("i")) {
                	if (value instanceof Identity)
                		identity = (Identity)value;
                }
            }
            
            if (key != null && this.assignedPersons.containsKey(key))
               continue;
            if (identity == null)
               continue;
            	
            if (this.assignedPersons.containsKey(identity.getKey()))
            	continue;

			String matchingPersonURI
			= PrPlPersonIdentityMatcher.matchIdentityToPerson(identity, getEmailToFriendsMap(), unassignedPersons);

			if (matchingPersonURI == null)
				continue;
			Resource matchingPerson = this.prplService_.getResource(matchingPersonURI);
			matchingPerson.setMetadata(PrPlContactInfo.PRPL_ID_URI, identity);
			identity.setMetadata(Person.CLASS_NAME_URI, matchingPerson);
			// unassignedPersons.remove(matchingPersonURI);
            
			this.assignedPersons.put(identity.getKey(), unassignedPersons.get(matchingPersonURI));
        }

	}
	
	
	private void matchUnassignedPersonsToPrPlIdentities()
	{
		List<String> emailArguments = new Vector<String>();
		HashMap<String, PrPlContactInfo> unassignedPersonsByEmail = new HashMap<String, PrPlContactInfo>();
		
		try {
			if (this.dirClient_ == null)
			   this.dirClient_ = DirectoryClient.getGlobalDirectoryServiceClient();

			for (PrPlContactInfo unassignedContact : this.unassignedPersons.values())
			{
				HashSet<String> emails = unassignedContact.getEmailAddresses();
				if (emails == null)
					continue;
				emailArguments.addAll(emails);
				System.out.println("PRPLADDRESSBOOK: Adding "+emails.size()+" emails...");
				
				for (String email : emails) {
					unassignedPersonsByEmail.put(email, unassignedContact);
				}
			}
			
			/*
			String[] varargsEmails = emailArguments.toArray(new String[0]);
			System.out.println("PRPLADDRESSBOOK: Total emails is "+varargsEmails.length);
			for (int i = 0; i < varargsEmails.length; i++) {
				System.out.println("PRPLADDRESSBOOK: -> "+varargsEmails[i]);
			}
			*/
			
			String[] varargsEmails = {"lam@cs.stanford.edu", "hangal@gmail.com", "jiwon@stanford.edu"};
			//System.out.println("PRPLADDRESSBOOK: Going to check DirectoryService for PCB info for lam@cs.stanford.edu, hangal@gmail.com, jiwon@stanford.edu ...");
			
			List<Set<Map<String, String>>> prplPCBAliases = this.dirClient_.findAliases(varargsEmails);
			//List<Set<Map<String, String>>> prplPCBAliases = this.dirClient_.findAliases("lam@cs.stanford.edu", "hangal@gmail.com", "jiwon@stanford.edu");
			System.out.println("PRPLADDRESSBOOK: Number of matching current PrPl users found: "+prplPCBAliases.size());
			
			for (Set<Map<String, String>> prplPCB : prplPCBAliases) {
				
				System.out.println("PRPLADDRESSBOOK: Current set has "+prplPCB.size()+" entries");
				
				for (Map<String, String> prplDirEntry : prplPCB) {
					if (prplDirEntry == null) 
						continue;
					
					String identityURI  = prplDirEntry.get(DirectoryCommon.IDENTITY_URI);
					String identityKey  = prplDirEntry.get(DirectoryCommon.IDENTITY_KEY);
					String identityName = prplDirEntry.get(DirectoryCommon.IDENTITY_NAME);
					
					
					if (!unassignedPersonsByEmail.containsKey(identityKey)) {
						System.out.println("No unassigned person has the following email: "+identityKey);
						continue;
					}
					
					
					System.out.println("PRPLADDRESSBOOK: Found unassigned person "+identityName+" mapping to PrPl user <"+identityURI+">");
					
					/*
					// TODO: Fix bad PrPl API - this call apparently is createIdentity()
					Identity newlyAddedIdentity = this.prplService_.getIdentityByURI(identityURI, true);
					newlyAddedIdentity.setName(identityName);
					
					// Set PrPl metadata match to PrPl Identity
					InSituContact contact = unassignedPersonsByEmail.get(identityKey);
					if (contact.getResourceURI() != null) {
						Resource matchingPerson = this.prplService_.getResource(contact.getResourceURI());
						matchingPerson.setMetadata(InSituContact.PRPL_ID_URI, newlyAddedIdentity);
						newlyAddedIdentity.setMetadata(Person.CLASS_NAME_URI, matchingPerson);
						
						this.assignedPersons.put(identityKey, contact);
						this.unassignedPersons.values().remove(contact);
						for (String email : contact.getEmailAddresses()) {
							unassignedPersonsByEmail.remove(email);
						}
					}
					*/
					
					// just process one alias per user
					break;
				}
			}
			
			
			
			
		}
		catch (MalformedURLException murlException)
		{
			System.out.println("Problem trying to obtain PrPl DirectoryClient due to the following exception: "+murlException.getMessage());
			murlException.printStackTrace();
			return;
		}
		
		
	}
	
	/*
	public HashMap<String, Identity> matchEmailsToPrPlIdentities(List<String> emailAddrs)
	{
		if (emailAddrs == null)
			return null;
		
		HashMap<String, Identity> emailToPrPlMatches = new HashMap<String, Identity>();
		try
		{
			if (this.dirClient_ == null)
			   this.dirClient_ = DirectoryClient.getGlobalDirectoryServiceClient();
			
			String[] varargsEmails = emailAddrs.toArray(new String[0]);
			List<Set<Map<String, String>>> prplPCBAliases = this.dirClient_.findAliases(varargsEmails);
			for (Set<Map<String, String>> prplPCB : prplPCBAliases) {
				Identity samePCB = null;
				
				for (Map<String, String> prplDirEntry : prplPCB) {
					if (prplDirEntry == null) 
						continue;
					
					String identityURI  = prplDirEntry.get(DirectoryCommon.IDENTITY_URI);
					String identityKey  = prplDirEntry.get(DirectoryCommon.IDENTITY_KEY);
					String identityName = prplDirEntry.get(DirectoryCommon.IDENTITY_NAME);
					if (samePCB != null) {
						emailToPrPlMatches.put(identityKey, samePCB);
						continue;
					}

					// TODO: Fix bad PrPl API - this call apparently is createIdentity()
					samePCB = this.prplService_.getIdentityByURI(identityURI, true);
					samePCB.setName(identityName);
					emailToPrPlMatches.put(identityKey, samePCB);
					System.out.println("matchEmailsToPrPlIdentities(): "+identityKey+" -> <"+samePCB.getURI()+">");
				}
			}
			
			return emailToPrPlMatches;
		}
		catch (MalformedURLException murlException)
		{
			System.out.println("Problem trying to obtain PrPl DirectoryClient due to the following exception: "+murlException.getMessage());
			murlException.printStackTrace();
			return null;
		}
	}
	*/

	
	
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
