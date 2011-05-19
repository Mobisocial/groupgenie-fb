package edu.stanford.socialflows.sticker;

import java.lang.Comparable;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.stanford.prpl.insitu.util.InSituUtils;
import edu.stanford.socialflows.contacts.SocialFlowsAddressBook;
import edu.stanford.socialflows.contacts.SocialFlowsContactInfo;
import edu.stanford.socialflows.log.SocialFlowsLogger;
import edu.stanford.socialflows.settings.FBAppSettings;
import edu.stanford.socialflows.sticker.Sticker;


public class SocialFlowsGroup implements Comparable<SocialFlowsGroup>
{
	List<SocialFlowsContactInfo> groupMembers = new Vector<SocialFlowsContactInfo>();
	
	// quantitative & qualitative weightings of group
	int utility = 0;
	int frequency = 0;
	public Sticker groupSticker = null;
	
	
	public SocialFlowsGroup()
	{	}	
	
	public SocialFlowsGroup(int utility, int frequency)
	{
		this.utility = utility;
		this.frequency = frequency;
	}
	
	public void setUtility(int utility)
	{
		this.utility = utility;
	}
	
	public int getUtility()
	{
		return this.utility;
	}
	
	public void setFrequency(int frequency)
	{
		this.frequency = frequency;
	}
	
	public int getFrequency()
	{
		return this.frequency;
	}
	
	public int compareTo(SocialFlowsGroup o)
	{
		if (this.utility > o.getUtility())
			return -1;
		else if (this.utility < o.getUtility())
			return 1;
		
		if (this.frequency > o.getFrequency())
			return -1;
		else if (this.frequency < o.getFrequency())
			return 1;
		
		if (this.groupMembers.size() > o.getGroupMembers().size())
			return -1;
		else if (this.groupMembers.size() < o.getGroupMembers().size())
			return 1;
		
		return 0;
	}
	
	
	public void addGroupMember(SocialFlowsContactInfo member)
	{
		if (member != null)
		   groupMembers.add(member);
	}
	
	public void sortGroupMembers()
	{
		Collections.sort(this.groupMembers, SocialFlowsContactInfo.socialWeightedOrder);
	}
	
	public List<SocialFlowsContactInfo> getGroupMembers()
	{
		return this.groupMembers;
	}
	
	public int getGroupMemberSize()
	{
		if (this.groupMembers == null)
			return 0;
		return this.groupMembers.size();
	}
	
	
	
	/*** Suggested Groups Rendering ***/
	
	public static SocialFlowsGroup generateSocialFlowsGroup(Set<String> groupMembersIDs, SocialFlowsAddressBook addressBook)
	{
		HashSet<String> defunctGroupMembers 
		= new HashSet<String>(); // for debug purposes on reconstituting saved groups
		
		SocialFlowsGroup group = new SocialFlowsGroup();
		for (String sfContactID : groupMembersIDs)
		{
			long sfcid = Long.parseLong(sfContactID);
			SocialFlowsContactInfo matchingSfContact
			= addressBook.lookupFriendBySfContactId(sfcid);
			if (matchingSfContact != null)
			    group.addGroupMember(matchingSfContact);
			else {
				// Record when a matching contact is NULL
				defunctGroupMembers.add(sfContactID);
			}
		}

		if (group.groupMembers != null && group.groupMembers.size() > 1)
		    Collections.sort(group.groupMembers);
		
		// Debug purposes
		if (defunctGroupMembers.size() > 0) {
			SocialFlowsLogger sfLog = SocialFlowsLogger.getInstance();
			if (sfLog != null) {
				sfLog.logError("Apparently the following sfContacts from user's "+
						       "previously stored topology are no longer valid: "+
						       defunctGroupMembers.toString());
			}
		}

		return group;
	}

	public static SocialFlowsGroup generateGroup(JSONObject groupJSON, 
			                                     SocialFlowsAddressBook myAddressBook) throws JSONException
	{
		List<SocialFlowsContactInfo> myFriends 
		= myAddressBook.getUnifiedFriendsList();
		HashMap<String, SocialFlowsContactInfo> myFriendsByEmail 
		= myAddressBook.getEmailToFriendsMap();
		
		int util = groupJSON.optInt("utility", 0);
		int freq = groupJSON.optInt("freq", 0);
		SocialFlowsGroup group = new SocialFlowsGroup(util, freq);
		JSONArray groupMembers = groupJSON.optJSONArray("members");
		if (groupMembers == null)
		   return null;
  	  
  	  	for (int i = 0; i < groupMembers.length(); i++)
  	  	{
  	  		String memberId = null;
  	  		JSONObject memberData = groupMembers.optJSONObject(i);
  	  		if (memberData == null)
  	  			memberId = groupMembers.optString(i, null);
  	  		else
  	  			memberId = memberData.optString("id", null);
  	  		
  	  		SocialFlowsContactInfo matchingContact = null;
  	  		if (memberId != null && !memberId.isEmpty())
  	  		{
  	  			matchingContact
  	  			= myAddressBook.lookupFriendBySfContactId(Long.parseLong(memberId));
  	  			//if (matchingContact != null)
  	  			//	System.out.println("Found Social Contact "+matchingContact.getName()+" by sfContactID "+memberId);
  	  			
  	  			if (matchingContact == null) {
  	  				matchingContact
  	  				= myAddressBook.lookupFriendByFbId(Long.parseLong(memberId));
  	  				
  	  			//	if (matchingContact != null)
  	  			//		System.out.println("Found Social Contact "+matchingContact.getName()+" by fbID "+memberId);
  	  			}
  	  		}
  	  		if (matchingContact != null) {
				group.addGroupMember(matchingContact);
				continue;
			}
  	  		else if (memberData == null) {
  	  			continue;
  	  		}

  	  		
  	  		// Do entity-resolution using emails or names
            JSONArray memberEmails = memberData.optJSONArray("emailAddrs");
            JSONArray candNames  = memberData.optJSONArray("names");
            if (candNames != null) {
            	candNames = SocialFlowsGroup.processInternationalNames(candNames);
            	memberData.put("names", candNames);
            }
            int socialWeight = memberData.getInt("messageOutCount");
            
            // Check by email first (most accurate entity-resolution approach)
            if (memberEmails != null && memberEmails.length() > 0)
            {
            	for (int j = 0; j < memberEmails.length(); j++) {
            		matchingContact = myFriendsByEmail.get(memberEmails.getString(j));
            		if (matchingContact != null) {
            			
            			//System.out.println("Found Email '"+memberEmails.getString(j)+
     				    //       "' to match existing person named='"+matchingContact.getName()+
     				    //       "' (fb id "+matchingContact.getUid()+"), profilePicURL="+matchingContact.getPicSquare());
            			
            			break;
            		}
            	}
            	
            	// Update InSituContact object with latest InSitu data
            	if (matchingContact != null) {
            		matchingContact.setSocialWeight(socialWeight);
            		// Append additional JSON data needed for Java applet visualization to work
            		if (matchingContact.getPicSquare() != null)
            		   memberData.put("profileImg", matchingContact.getPicSquare());
            		if (matchingContact.getProfileUrl() != null)
                	   memberData.put("profileUrl", matchingContact.getProfileUrl());
            		matchingContact.setEmailData(memberData); // contains latest social weight info, additional names/aliases & emails, etc.
                	groupMembers.put(i, memberData);
            		
                	group.addGroupMember(matchingContact);
                	continue;
            	}
            }
            
            
            // 1. Update social weights, diffs on new additional emails, new additional names/aliases
    		// 2. Retrieve existing InSituContact object
    		// 3. Make use of EMAIL LIST
    		// 4. Make use of NAMES LIST
            
            // Could be a mailing list or simply an email contact without a name
            if (candNames == null || candNames.length() <= 0)
            {
            	if (memberEmails == null || memberEmails.length() <= 0)
            		continue;
            	
            	SocialFlowsContactInfo member = SocialFlowsAddressBook.createContact();
            	member.setSocialWeight(socialWeight);
            	member.setName(memberEmails.getString(0));
            	member.setPicSquare(null); // "http://static.ak.fbcdn.net/pics/q_silhouette.gif"
            	member.setProfileUrl("mailto:"+memberEmails.getString(0));
            	member.setTempContactId(memberEmails.getString(0));
        		
            	// Append additional JSON data needed for Java applet visualization to work
            	memberData.put("profileImg", member.getPicSquare());
            	memberData.put("profileUrl", member.getProfileUrl());
            	member.setEmailData(memberData); // contains latest social weight info, additional names/aliases & emails, etc.
            	groupMembers.put(i, memberData);
            	
            	// Add to unified list of contacts, can also be searched by email addr
            	myFriends.add(member);
            	if (memberEmails != null && memberEmails.length() > 0)
                {
                	for (int j = 0; j < memberEmails.length(); j++) {
                		myFriendsByEmail.put(memberEmails.getString(j), member);
                	}
                }
            	
            	group.addGroupMember(member);
            	continue;
            }
            
            // Search for the matching FB friend profile         
            // - put all names in small letters
            // - is shorter name the substring of longer name
            // - matching last name
            // - first name matching or matching short form           
            Iterator<SocialFlowsContactInfo> contactIterator = myFriends.iterator();
            while (contactIterator.hasNext())  // find the matching existing person/contact data from user's db/prpl
            {
            	SocialFlowsContactInfo myFriend = contactIterator.next();
            	HashSet<String> contactAliases = (HashSet<String>)myFriend.getNames(); // other possible names/ids
            	boolean isAMatch = false;
            	
            	// TODO: More vigorous entity-resolution
            	// Already matched with another email contact
            	if (myFriend.getEmailData() != null)
            		continue;
            	
            	// Cleanup and standardize FirstName, LastName, FullName data
            	String fullName = null, firstName = null, lastName = null; 
            	if (myFriend.getFirstName() != null)
            		firstName = myFriend.getFirstName().trim().toLowerCase();
            	if (myFriend.getLastName() != null)
            		lastName = myFriend.getLastName().trim().toLowerCase();
            	if (myFriend.getName() != null)
            		fullName = myFriend.getName().trim().toLowerCase();
            	
            	// Cannot do anything if even fullname is missing
            	if (fullName == null)
            		continue;
            	
            	// Determine firstname, lastname from fullname if possible
            	int lastSpace = fullName.lastIndexOf(" ");
            	if (lastSpace != -1) {
            		if (lastName == null)
            			lastName = fullName.substring(lastSpace+1, fullName.length());
            		if (firstName == null)
            			firstName = fullName.substring(0, lastSpace);
            	}
            	else {
            		firstName = fullName;
            	}
            	
            	
            	// ===>>>> Make use of NAMES LIST from PrPl data about this social contact <<<<===
            	String[] namesToCheck = null;
            	if (contactAliases != null) 
            	{
            		namesToCheck = new String[contactAliases.size()+1];
            		Iterator<String> aliasIterator = contactAliases.iterator();
            		for (int j = 1; aliasIterator.hasNext(); j++) {
            			namesToCheck[j] = aliasIterator.next();
            			if (namesToCheck[j] != null)
            				namesToCheck[j] = namesToCheck[j].trim().toLowerCase();
            		}
            	}
            	else
            		namesToCheck = new String[1];
            	namesToCheck[0] = fullName;
            	
            	
            	// 1st check: Simplest, are any of the names an exact match with names/aliases of a known contact?
            	for (int j = 0; j < namesToCheck.length; j++) 
            	{
            		String contactName = namesToCheck[j];
            		if (contactName == null || contactName.trim().length() <= 0)
             		   continue;
            		
            		if (firstCheckMatch(contactName, candNames)) {
            			
            			System.out.println("Found by 1st Check-Match ("+contactName+", "+candNames.getString(0)+")");
            			
            			isAMatch = true;
            			break;
            		}
            	}
            	
            	// Plausibility check before doing complicated name checks?
            	// - one of the ref names must contain itself in entirety in the list of email names?
            	
            	
            	// Do More further complicated name-matching checks
            	if (!isAMatch)
            	{
            		for (int j = 0; j < namesToCheck.length; j++) 
                	{
            			String contactName = namesToCheck[j];
            			String contactFirstName = null, contactLastName = null;
            			if (contactName == null || contactName.trim().length() <= 0)
                  		   continue;
            			contactName = contactName.trim().toLowerCase();
            			
            			// Checking using default initial firstname, lastname & fullname
            			if (j == 0) {
            				contactFirstName = firstName;
            				contactLastName  = lastName;
            			}
            			else {
            				if ( contactName.equals(namesToCheck[0].trim().toLowerCase()) )
            					continue;

            				int contactLastSpace = contactName.lastIndexOf(" ");
            				if (contactLastSpace != -1) {
            					contactLastName  = contactName.substring(contactLastSpace+1, contactName.length());
                        		contactFirstName = contactName.substring(0, contactLastSpace);
                        	}
            				else {
            					contactFirstName = contactName;
            				}
            			}
            			
            			// More complicated name-matching checks
                    	for (int k = 0; k < candNames.length(); k++)
                    	{
                    		String candFullname = candNames.getString(k);
                    		if (candFullname == null || candFullname.trim().length() <= 0)
                    		   continue;
                    		candFullname = candFullname.trim().toLowerCase();
                    		
                    		// Determine firstname and lastname for current name-string
                    		String candFirstName = candFullname, candLastName = candFullname;
                    		int nameFirstSpace = candFullname.indexOf(" ");
                    		int nameLastSpace  = candFullname.lastIndexOf(" ");
                    		if (nameLastSpace != -1)
                      		   candLastName = candFullname.substring(nameLastSpace+1, candFullname.length());
                     		if (nameFirstSpace != -1)
                     		   candFirstName = candFullname.substring(0, nameFirstSpace);
                    		
                     		// Don't continue checking if either first name or last name is missing
                    		if (candFirstName.equals(candLastName))
                    			continue;
                     		
                    		
                    		// 2nd check: Check that the first and last names match, 
                    		//            accounting for popular short-forms of the first name
                    		if (secondCheckMatch(contactName, contactFirstName, contactLastName, 
                    				             candFullname, candFirstName, candLastName)) {
                    			
                    			System.out.println("Found by 2nd Check-Match ("+contactName+", "+candFullname+")");
                    			
                    			isAMatch = true;
                    			break;
                    		}
                    		
                    		// 3rc check: Check that the names could be a subset of one another
                    		if (thirdCheckMatch(contactName, contactFirstName, contactLastName, 
                    				            candFullname, candFirstName, candLastName)) {
                    			
                    			System.out.println("Found by 3rd Check-Match ("+contactName+", "+candFullname+")");
                    			
                    			isAMatch = true;
                    			break;
                    		}
                    		
                    		// 4th check: check the first and last names are contained in each other
                    		if (fourthCheckMatch(contactName, contactFirstName, contactLastName, 
                    				             candFullname, candFirstName, candLastName)) {
                    			
                    			System.out.println("Found by 4th Check-Match ("+contactName+", "+candFullname+")");
                    			
                    			isAMatch = true;
                    			break;
                    		}
                    	}		
                	}
            	} // End of further complicated name-matching checks
            	

            	if (isAMatch)
            	{   
            		/*
            		System.out.println("Found Email name='"+candNames.getString(0)+
            				           "' to match existing person resource named='"+fullName+
            				           "' (fb id "+myFriend.getUid()+"), profilePicURL="+myFriend.getPicSquare());
            		*/
            		
            		matchingContact = myFriend;
            		matchingContact.setSocialWeight(socialWeight);
            		//matchingContact.setSocialWeight(matchingContact.getSocialWeight()+socialWeight);
            		
            		// Append additional JSON data needed for Java applet visualization to work
            		if (matchingContact.getPicSquare() != null)
            			memberData.put("profileImg", matchingContact.getPicSquare());
            		if (matchingContact.getProfileUrl() != null)
            			memberData.put("profileUrl", matchingContact.getProfileUrl());
            		matchingContact.setEmailData(memberData); // contains latest social weight info, additional names/aliases & emails, etc.
                	groupMembers.put(i, memberData);
                	
                	// Make contact searcheable by email addr as well
                	if (memberEmails != null && memberEmails.length() > 0)
                    {
                    	for (int j = 0; j < memberEmails.length(); j++) {
                    		myFriendsByEmail.put(memberEmails.getString(j), matchingContact);
                    	}
                    }

                	group.addGroupMember(matchingContact);
            		break;
            	}
            }

            // No matching existing person/contact data available for this email contact.
            // Create new contact and Just use email info contact.
    		if (matchingContact == null) 
    		{
    			SocialFlowsContactInfo member = SocialFlowsAddressBook.createContact();
            	member.setSocialWeight(socialWeight);
            	member.setName(candNames.getString(0));
            	member.setPicSquare(null); // "http://static.ak.fbcdn.net/pics/q_silhouette.gif"
            	member.setProfileUrl("mailto:"+memberEmails.getString(0));
            	member.setTempContactId(memberEmails.getString(0));
            	
            	// Append additional JSON data needed for Java applet visualization to work
            	if (member.getPicSquare() != null)
            		memberData.put("profileImg", member.getPicSquare());
            	if (member.getProfileUrl() != null)
            		memberData.put("profileUrl", member.getProfileUrl());
            	member.setEmailData(memberData); // contains latest social weight info, additional names/aliases & emails, etc.
            	groupMembers.put(i, memberData);
            	
            	// Add to unified list of contacts; Make contact searcheable by email addr as well
            	myFriends.add(member);
            	if (memberEmails != null && memberEmails.length() > 0)
                {
                	for (int j = 0; j < memberEmails.length(); j++) {
                		myFriendsByEmail.put(memberEmails.getString(j), member);
                	}
                }
            	group.addGroupMember(member);
    		}

  	  	}
  	  	
  	  	if (group.groupMembers != null || group.groupMembers.size() > 1)
  	  	   Collections.sort(group.groupMembers);
  	  	return group;
	}

	private static boolean firstCheckMatch(String fullName, JSONArray memberNames) throws JSONException
	{
		if (memberNames == null || memberNames.length() <= 0)
			return false;
		
		for (int k = 0; k < memberNames.length(); k++)
    	{
    		String name = memberNames.getString(k);
    		if (name == null || name.trim().length() <= 0)
    		   continue;
    		name = name.trim().toLowerCase();
    		
    		//System.out.println("Comparing Email name='"+name+"', FBname='"+fullName+"'");
    		if (name.equals(fullName))
    			return true;
    	}
		
		return false;
	}
	
	private static boolean secondCheckMatch(String fullName, String firstName, String lastName,
											String name, String fName, String lName) throws JSONException
	{
		firstName = shortenName(firstName);
		fName = shortenName(fName);
		if (lName.equals(lastName) && (fName.equals(firstName)||firstName.equals(fName))) {
			return true;
		}
		// using "contains()" results in bugs for Korean names
		
		return false;
	}

	private static boolean thirdCheckMatch(String fullName, String firstName, String lastName,
										   String name, String fName, String lName) throws JSONException
	{
		String shorter, longer;
		if (name.length() < fullName.length()) {
			shorter = name;
			longer = fullName;
		}
		else {
			shorter = fullName;
			longer = name;
		}
		if (longer.startsWith(shorter)) { // longer.contains(shorter)
			return true;
		}
		
		return false;
	}

	private static boolean fourthCheckMatch(String fullName, String firstName, String lastName,
			   								String name, String fName, String lName) throws JSONException
	{
		String[] nameParts = fullName.split("\\s+");
		boolean containsfName = false, containslName = false;
		for (int l = 0; l < nameParts.length; l++) {
			if (shortenName(nameParts[l]).equals(shortenName(fName))) {
				containsfName = true;
				continue;
			}
			if (nameParts[l].equals(lName)) {
				containslName = true;
				continue;
			}
		}
		if (containsfName && containslName) {
			return true;
		}
		
		return false;
	}
	
	
	private static String shortenName(String name)
	{
		String shortenedName = name;
		shortenedName = shortenedName.replace("michael", "mike");
		shortenedName = shortenedName.replace("benjamin", "ben");
		shortenedName = shortenedName.replace("jonathan", "jon");
		shortenedName = shortenedName.replace("alexander", "alex");
		shortenedName = shortenedName.replace("robert", "rob");
		return shortenedName;
	}

	private static JSONArray processInternationalNames(JSONArray names)
	{
		if (names == null || names.length() <= 0)
			return names;
		
		JSONArray cleanedNames = new JSONArray();
		for (int k = 0; k < names.length(); k++)
    	{
			String name = null;
			try {
				name = names.getString(k);
			}
    		catch (JSONException jsone) {
    			continue;
    		}
    		
    		if (name == null || name.trim().length() <= 0 
    			|| name.trim().equals("???")) // due to unprocessable international names
    		   continue;

    		cleanedNames.put(name.trim());
    	}
		
		return cleanedNames;
	}


	
	/*** Web UI Rendering ***/
	
	public static String renderSocialFlowUI(SocialFlowsGroup group)
	{
		return renderSocialFlowUI(group, 1, false);
	}
	
	public static String renderSocialFlowUI(SocialFlowsGroup group, int numIndents, boolean showFirstExpansion)
	{
		StringBuilder html = new StringBuilder();
		if (group == null)
			return html.toString();
		
		StringBuilder indentStr = new StringBuilder();
		for (int i = 0; i < numIndents; i++) {
			indentStr.append("\t");
		}
		
		Sticker groupSticker  = group.groupSticker;
        String stickerID      = groupSticker.stickerID;
        String stickerSfID    = groupSticker.getStickerSfID();
        String stickerURI     = groupSticker.getStickerURI();
        String stickerIconURI = groupSticker.getStickerIconURI();
        if (stickerIconURI == null)
        	stickerIconURI = "";
        long stickerTopoRunID = groupSticker.stickerTopologyRun;
		
		// Determine title/name for group
		int numGroupMembers = group.getGroupMembers().size();
		group.sortGroupMembers();
		String groupName = groupSticker.getStickerName();
		if (groupName == null || groupName.trim().equals("null") || groupName.trim().length() <= 0)
		{
			if (numGroupMembers == 1) {
			   groupName = group.getGroupMembers().get(0).getName();
			}
			else
			   groupName = ""; // "Click to label this suggested group"; // "Click to edit";
		}
		
		
		// DEBUG
		/*
		if (groupSticker.isSaved) {
			System.out.println("RENDERING SAVED GROUP "+groupName);
			for (SocialFlowsContactInfo member : group.getGroupMembers()) {
				System.out.println("-> "+member.getName());
			}			
		}
		*/
        		
		// Generate UI for social group
        String stickerClass = "sticker";
        if (groupSticker.isSaved) {
 	       stickerClass += " saved";
 	    }
        else {
	       stickerClass += " suggested";
	    }
        
        html.append("\n");
		html.append(indentStr);
		html.append("<li class=\"socialflow\">  \n");
		html.append(indentStr);
		html.append("<div style=\"padding-left: 23px;\">  \n");
		html.append(indentStr);
		html.append("    <div class=\""+stickerClass+"\" id=\""+stickerID+"\">  \n");
		
		// sticker/group metadata
		html.append(indentStr);
		html.append("    <div class=\"stickerMetadata\" style=\"display: none;\">  \n");
		html.append(indentStr);
		html.append("        <input type=\"hidden\" id=\"stickerSfID\" name=\"stickerSfID\" value=\""+stickerSfID+"\" />  \n");
		if (stickerTopoRunID < 0)
			stickerTopoRunID = 0;
		html.append(indentStr);
		html.append("        <input type=\"hidden\" id=\"stickerTopologyRunID\" name=\"stickerTopologyRunID\" value=\""+stickerTopoRunID+"\" />  \n");
		html.append(indentStr);
		html.append("        <input type=\"hidden\" id=\"stickerResourceURI\" name=\"stickerResourceURI\" value=\""+stickerURI+"\" />  \n");
		html.append(indentStr);
		html.append("        <input type=\"hidden\" id=\"stickerIconURI\" name=\"stickerIconURI\" value=\""+stickerIconURI+"\" />  \n");
		html.append(indentStr);
		html.append("        <span class=\"merged-stickers-metadata\"></span>  \n");
		
		// collection of shared data objects
		html.append(indentStr);
		html.append("        <span class=\"collection\">  \n");
    	// Get collection of items
		if (group.groupSticker != null) {
			Set<String> collectionItemsURIs = group.groupSticker.getCollection();
			for (String uri : collectionItemsURIs) {
				html.append(indentStr);
				html.append("           <input type=\"hidden\" class=\"collection_item\" value=\""+uri+"\" />  \n");
			}
		}
		html.append(indentStr);
		html.append("        </span>  \n");
		
		// UI user-study statistics
		html.append(indentStr);
		html.append("        <input type=\"hidden\" id=\"numDroppedIn\" name=\"numDroppedIn\" value=\"0\" />  \n");
		html.append(indentStr);
		html.append("        <input type=\"hidden\" id=\"numDraggedOut\" name=\"numDraggedOut\" value=\"0\" />  \n");
		html.append(indentStr);
		html.append("        <input type=\"hidden\" id=\"numAddedContactsByAdd\" name=\"numAddedContactsByAdd\" value=\"0\" />  \n");
		html.append(indentStr);
		html.append("        <input type=\"hidden\" id=\"numAddedContactsByMerge\" name=\"numAddedContactsByMerge\" value=\"0\" />  \n");
		html.append(indentStr);
		html.append("        <input type=\"hidden\" id=\"numClicksToAddContacts\" name=\"numClicksToAddContacts\" value=\"0\" />  \n");
		html.append(indentStr);
		html.append("        <input type=\"hidden\" id=\"numDeletedContacts\" name=\"numDeletedContacts\" value=\"0\" />  \n");
		html.append(indentStr);
		html.append("        <input type=\"hidden\" id=\"numMerges\" name=\"numMerges\" value=\"0\" />  \n");
		html.append(indentStr);
		html.append("        <input type=\"hidden\" id=\"totalClicks\" name=\"totalClicks\" value=\"0\" />  \n");

		html.append(indentStr);
		html.append("    </div>  \n");
		html.append(indentStr);
		html.append("    <div class=\"sticker-title\" style=\"float:left;\">"+groupName+"</div>  \n");
		
        // access control/sharing data
		if (FBAppSettings.ENABLE_ACCESS_CONTROL)
		{
			int numColItems = 0;	
			if (group.groupSticker != null) {
				Set<String> collectionItemsURIs = group.groupSticker.getCollection();
				numColItems = collectionItemsURIs.size();
			}
			
			if (numColItems > 0) {
				html.append(indentStr);
				html.append("        <div title=\"Share Data with this Group\" class=\"share_data inline-block pillbutton options options-saved sharing\">  \n");
				html.append(indentStr);
				html.append("        <div class=\"inline-block pillbutton-label\">  \n");
				html.append(indentStr);
				html.append("           <div class=\"inline-block pillbutton-label-outer-box pillbutton-outer-box-color\">  \n");
				html.append(indentStr);
				html.append("             <div class=\"inline-block pillbutton-label-inner-box pillbutton-inner-box-color\">  \n");
				html.append(indentStr);
				html.append("             <div class=\"share_msg\">SHARING</div>  \n");
				html.append(indentStr);
				html.append("        </div></div></div></div>  \n");
			}
			else {
				html.append(indentStr);
				html.append("        <div title=\"Share Data with this Group\" class=\"share_data inline-block pillbutton options options-saved-hover\">  \n");
				html.append(indentStr);
				html.append("        <div class=\"inline-block pillbutton-label\">  \n");
				html.append(indentStr);
				html.append("           <div class=\"inline-block pillbutton-label-outer-box pillbutton-outer-box-color\">  \n");
				html.append(indentStr);
				html.append("             <div class=\"inline-block pillbutton-label-inner-box pillbutton-inner-box-color\">  \n");
				html.append(indentStr);
				html.append("             <div class=\"share_msg\">SHARE</div>  \n");
				html.append(indentStr);
				html.append("        </div></div></div></div>  \n");
			}
		}
		
		// manipulation options
		html.append(indentStr);
		html.append("        <img class=\"delete_group options options-bar options-icon options-right options-saved options-selected\" src=\"stickers/cross.gif\" title=\"Delete\"/>  \n");
		//html.append(indentStr);
		//html.append("        <img class=\"save_group   options options-bar options-icon options-right options-saved options-selected\" src=\"stickers/disk.gif\" title=\"Save\"/>  \n");
		html.append(indentStr);
		html.append("        <img class=\"create_subgroup options options-bar options-icon options-right options-saved options-selected\" src=\"stickers/plus.png\" title=\"Create New Subgroup\"/>  \n");
		html.append(indentStr);
		html.append("        <input title=\"Merge Cliques\" type=\"button\" value=\"Merge\" name=\"merge-cliques\" class=\"merge_group button-fbstyle options options-bar options-button options-right options-selected\"/>  \n");
		html.append(indentStr);
		html.append("        <input title=\"Select Clique\" type=\"checkbox\" class=\"sticker-selected options options-bar options-right options-selected\"/>  \n");
		
		html.append(indentStr);
		html.append("    <br/>  \n");
		html.append(indentStr);
		html.append("    <ul id=\"clique\" class=\"clique\">  \n");
		// Contact Placeholder
		html.append(indentStr);
		html.append("    <li class=\"placeholder\" style=\"display:none;\"></li>  \n");
		
		
        Iterator<SocialFlowsContactInfo> groupMembers
        = group.getGroupMembers().iterator();
        while (groupMembers.hasNext())
        {
              SocialFlowsContactInfo member = groupMembers.next();
              String friendName = member.getName();
              String friendTitle = friendName;
              String displayName = friendName;
              // Special processing for email-based names
              if (InSituUtils.isEmailAddress(friendName)) {
                 displayName = friendName.replace("@", "<br/>@");
              }
              else {
                  // Append email if available
                  Set<String> emails = member.getEmailAddresses();
                  if (emails != null) {
                     for (String email : emails) {
                         friendTitle += " <"+email+">";
                         break;
                     }
                  }
              }

              String friendUserId  = member.getTempContactId();
              if (member.getContactId() > 0)
            	  friendUserId = String.valueOf(member.getContactId());
              //if (friendUserId == null || friendUserId.trim().length() <= 0
              //    || friendUserId.trim().equals("null"))
              //    friendUserId = String.valueOf(member.getContactId());
              try {
            	  friendUserId = URLEncoder.encode(friendUserId, "UTF-8");
            	  // Periods (.) and colons (:) not supported by JQuery
            	  friendUserId = friendUserId.replaceAll("[%$.+!*'(),]", "_");
              }
              catch (Exception e) { }
              String friendProfileUrl = member.getProfileUrl();
              if (friendProfileUrl == null)
                 friendProfileUrl = "#";

              // System.out.println("Printing out group member "+friendName+" with profilePicUrl="+profileImgUrl);

              // Print out clique member
              boolean missingProfilePhoto = false;
              String profileImgUrl = member.getPicSquare();
              if (profileImgUrl == null || profileImgUrl.trim().length() <= 0 
                  || profileImgUrl.trim().equals("null") 
                  || profileImgUrl.trim().endsWith(".gif")) 
              {
                  missingProfilePhoto = true;
                  if (profileImgUrl == null || profileImgUrl.trim().length() <= 0 
                      || profileImgUrl.trim().equals("null"))
                     profileImgUrl = "common/contact_placeholder.gif";
              }
              
              html.append(indentStr);
        	  html.append("    <li class=\"contact\" id=\""+friendUserId+"\" title=\""+friendTitle+"\"> \n");
        	  
        	  // Metadata for social contact
        	  html.append(indentStr);
        	  html.append("       <div class=\"metadata\" style=\"display: none;\"> \n");
        	  html.append(indentStr);
        	  html.append("            <span id=\""+friendUserId+"\" class=\""+friendUserId+"\"> \n");
        	  
        	  String friendSfContactId = null;
              if (member.getContactId() > 0) {
              	friendSfContactId = String.valueOf(member.getContactId());            
              }
              html.append(indentStr);
              html.append("                <input type=\"hidden\" class=\"sfContactID\" name=\"sfContactID\" value=\""+friendSfContactId+"\" /> \n");    		  
        	  //html.append(indentStr);
        	  //html.append("                <input type=\"hidden\" class=\"resourceURI\" name=\"resourceURI\" value=\""+member.getResourceURI()+"\" /> \n");
        	  html.append(indentStr);
        	  html.append("                <input type=\"hidden\" class=\"socialWeight\" name=\"socialWeight\" value=\""+member.getSocialWeight()+"\" /> \n");
        	           
              // New SocialFlows contact not yet saved into SocialFlows DB
              // Needs: *Fullname, *Firstname, *Lastname, *Social Weight, *Aliases, *Emails 
              if (member.getContactId() <= 0)
              {
            	  html.append(indentStr);
            	  html.append("                <input type=\"hidden\" class=\"fullName\" name=\"fullName\" value=\""+friendName+"\" /> \n");
            	  html.append(indentStr);
            	  html.append("                <input type=\"hidden\" class=\"firstName\" name=\"firstName\" value=\""+member.getFirstName()+"\" /> \n");
            	  html.append(indentStr);
            	  html.append("                <input type=\"hidden\" class=\"lastName\" name=\"lastName\" value=\""+member.getLastName()+"\" /> \n");

                  // Record the aliases
                  Set<String> names = member.getNames();
                  if (names != null) {
                     Iterator<String> namesIter = names.iterator();
                     while (namesIter.hasNext()) {
                           String name = namesIter.next();
                           html.append(indentStr);
                    	   html.append("                <input type=\"hidden\" class=\"alias\" name=\"alias\" value=\""+name+"\" /> \n");
                     }
                  }
                       
                  // Record the emails
                  Set<String> emails = member.getEmailAddresses();
                  if (emails != null) {
                     Iterator<String> emailsIter = emails.iterator();
                     while (emailsIter.hasNext()) {
                           String email = emailsIter.next();
                           html.append(indentStr);
                     	   html.append("                <input type=\"hidden\" class=\"emails\" name=\"emails\" value=\""+email+"\" /> \n");
                     }
                  }

              } 
              
              // End of information block about new InSitu contact
        	  html.append(indentStr);
        	  html.append("            </span> \n");
        	  html.append(indentStr);
        	  html.append("       </div> \n");

        	  // Render UI for social contact
        	  html.append(indentStr);
        	  html.append("       <img title=\""+friendTitle+"\" src=\""+profileImgUrl+"\" class=\"contact\" />  \n");
        	  if (missingProfilePhoto) {
        		  html.append(indentStr);
            	  html.append("       <div title=\""+friendTitle+"\" class=\"outer-name-overlay\"><div class=\"inner-name-overlay\">"+displayName+"</div></div>  \n");
        	  }
        	  html.append(indentStr);
        	  html.append("       <a title=\"Delete\" class=\"delete deletelink remove_clique_member\">X</a>  \n");
        	  html.append(indentStr);
        	  html.append("    </li>  \n");

        }
        
        
        // UI feature to Add New Clique Members
    	html.append(indentStr);
	  	html.append("    <li title=\"Add a Social Contact\" class=\"add_clique_member addContact options\"> \n");
	  	html.append(indentStr);
  	    html.append("       <img title=\"Add a Social Contact\" src=\"stickers/q_silhouette.gif\" class=\"contact\"/>  \n");
  	    html.append(indentStr);
  	    html.append("       <div title=\"Add a Social Contact\" class=\"outer-image-overlay\"><div class=\"inner-image-overlay\"><img src=\"stickers/plus.png\"/></div></div>  \n");
  	    html.append(indentStr);
  	    html.append("    </li>  \n");
		html.append(indentStr);
		html.append("    </ul>  \n");
		html.append(indentStr);
		html.append("    </div>  \n");
		html.append(indentStr);
		html.append("</div>  \n");


		// Process subsets
		List<Sticker> subsets = group.groupSticker.getSubsets();
        if (subsets != null && subsets.size() > 0)
        {
           html.append(indentStr);
           if (showFirstExpansion)
        	   html.append("<ul class=\"socialflow socialflow-init-display\" style=\"display: block;\" >  \n");
           else
        	   html.append("<ul class=\"socialflow\" style=\"display: none;\" >  \n");
    	   
           for (Sticker subset : subsets)
           {
        	   SocialFlowsGroup subsetGroup = subset.getClique();
           	   String subsetHTML = SocialFlowsGroup.renderSocialFlowUI(subsetGroup, numIndents+1, false);
               html.append(subsetHTML); // SocialFlow HTML layout for subset and its descendants
               html.append("\n");
           }
           
           html.append(indentStr);
   		   html.append("</ul>  \n");
   		   html.append(indentStr);
   		   if (showFirstExpansion)
   			   html.append("<span class=\"socialflow socialflow-navi expanded\"/>  \n");
   		   else
   			   html.append("<span class=\"socialflow socialflow-navi collapsed\"/>  \n");
        }

		html.append(indentStr);
		html.append("</li>  ");

        return html.toString();
	}

}

