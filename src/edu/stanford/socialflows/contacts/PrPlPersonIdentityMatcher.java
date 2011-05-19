package edu.stanford.socialflows.contacts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;

import edu.stanford.prpl.api.Identity;

public class PrPlPersonIdentityMatcher {

	public static String matchIdentityToPerson(Identity identity, HashMap<String, PrPlContactInfo> myFriendsByEmail,
											   HashMap<String, PrPlContactInfo> unassignedPersons)
	{
		String prplFriendName = identity.getName();
    	String prplFriendKey  = identity.getKey();

    	if (prplFriendName == null || prplFriendName.trim().length() <= 0)
    		prplFriendName = prplFriendKey;
    	prplFriendName = prplFriendName.trim().toLowerCase();
    	
    	String[] identityNames = {prplFriendName};

        // Check by email first (most accurate entity-resolution approach)
    	PrPlContactInfo match = myFriendsByEmail.get(prplFriendKey);
		if (match != null && match.getResourceURI() != null) {
			System.out.println("Found Identity <"+prplFriendKey+
			           "> to match existing person named='"+match.getName()+
			           "', profilePicURL="+match.getPicSquare());
			
			return match.getResourceURI();
		}

		PrPlContactInfo person = null; String personURI = null;
    	Iterator<String> personUriIterator = unassignedPersons.keySet().iterator();
    	while (personUriIterator.hasNext())
    	{
    		personURI = personUriIterator.next();
    		person = unassignedPersons.get(personURI);
    		
    		HashSet<String> contactAliases = (HashSet<String>)person.getNames(); // other possible names/ids
        	boolean isAMatch = false;
    		
        	// Cleanup and standardize FirstName, LastName, FullName data
        	String fullName = null, firstName = null, lastName = null; 
        	if (person.getFirstName() != null)
        		firstName = person.getFirstName().trim().toLowerCase();
        	if (person.getLastName() != null)
        		lastName = person.getLastName().trim().toLowerCase();
        	if (person.getName() != null)
        		fullName = person.getName().trim().toLowerCase();
        	
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
    		
        	// ===>>>> Make use of ALIASES LIST of Person resource <<<<===
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
        		
        		if (firstCheckMatch(contactName, identityNames)) {
        			
        			System.out.println("Found by 1st Check-Match ("+contactName+", "+identityNames[0]+")");
        			
        			isAMatch = true;
        			break;
        		}
        	}
        	
        	
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
        				// Already checked the initial fullname
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
                	for (int k = 0; k < identityNames.length; k++)
                	{
                		String name = identityNames[k];
                		if (name == null || name.trim().length() <= 0)
                		   continue;
                		name = name.trim().toLowerCase();
                		
                		// Determine firstname and lastname for current name-string
                		String fName = name, lName = name;
                		int nameFirstSpace = name.indexOf(" ");
                		int nameLastSpace  = name.lastIndexOf(" ");
                		if (nameLastSpace != -1)
                  		   lName = name.substring(nameLastSpace+1, name.length());
                 		if (nameFirstSpace != -1)
                 		   fName = name.substring(0, nameFirstSpace);
                		
                 		// Don't continue checking if either first name or last name is missing
                		if (fName.equals(lName))
                			continue;
                 		
                		
                		// 2nd check: Check that the first and last names match, 
                		//            accounting for popular short-forms of the first name
                		if (secondCheckMatch(contactName, contactFirstName, contactLastName, 
                				             name, fName, lName)) {
                			
                			System.out.println("Found by 2nd Check-Match ("+contactName+", "+name+")");
                			
                			isAMatch = true;
                			break;
                		}
                		
                		// 3rc check: Check that the names could be a subset of one another
                		if (thirdCheckMatch(contactName, contactFirstName, contactLastName, 
   				             				name, fName, lName)) {
                			
                			System.out.println("Found by 3rd Check-Match ("+contactName+", "+name+")");
                			
                			isAMatch = true;
                			break;
                		}
                		
                		// 4th check: check the first and last names are contained in each other
                		if (fourthCheckMatch(contactName, contactFirstName, contactLastName, 
		             						 name, fName, lName)) {
                			
                			System.out.println("Found by 4th Check-Match ("+contactName+", "+name+")");
                			
                			isAMatch = true;
                			break;
                		}
                	}
                	
            	}
        	} // End of further complicated name-matching checks


        	// Found a match
        	if (isAMatch)
        	{	
        		System.out.println("Found Identity name='"+identity.getName()+"' ("+identity.getURI()+
        				           ") to match Person resource '"+person.getName()+"' ("+personURI+")");
        		return personURI;
        	}	
    	}
    	
    	System.out.println("Did not Find a match for Identity name='"+identity.getName()+"' ("+identity.getURI()+")");
    	return null;
	}
	
	
	public static String matchIdentityToPerson(Identity identity,
			   								   HashMap<String, PrPlContactInfo> unassignedPersons)
	{
		String prplFriendName = identity.getName();
    	String prplFriendKey  = identity.getKey();

    	if (prplFriendName == null || prplFriendName.trim().length() <= 0)
    		prplFriendName = prplFriendKey;
    	prplFriendName = prplFriendName.trim().toLowerCase();
    	
    	// Approximate first & last names from Identity's metadata
    	String fName = prplFriendName, lName = prplFriendName;
		int firstSpace = prplFriendName.indexOf(" ");
		int lastSpace  = prplFriendName.lastIndexOf(" ");
		if (lastSpace != -1)
 		   lName = prplFriendName.substring(lastSpace+1, prplFriendName.length());
		if (firstSpace != -1)
		   fName = prplFriendName.substring(0, firstSpace);


    	String personURI = null; PrPlContactInfo person = null;
    	boolean isAMatch = false;
    	Iterator<String> personUriIterator = unassignedPersons.keySet().iterator();
    	while (personUriIterator.hasNext())
    	{
    		personURI = personUriIterator.next();
    		person = unassignedPersons.get(personURI);
    		
    		String firstName = person.getFirstName().trim().toLowerCase();
        	String lastName  = person.getLastName().trim().toLowerCase();
        	String fullName  = person.getName().trim().toLowerCase();
        	
    		
        	//System.out.println("Comparing Email name='"+name+"', FBname='"+fullName+"'");
        	
        	// 1st check: Simplest, is the name an exact match
    		if (prplFriendName.equals(fullName)) {
    			isAMatch = true;
    			System.out.println("Found match by 1st check");
    			break;
    		}
    		
    		// 2nd check: Check that the first and last names match, 
    		//            accounting for popular short-forms of the first name
    		// Don't continue checking if either first name or last name is missing
    		if (fName.equals(lName))
    			continue;       		
    		String tempFName = shortenName(fName);
    		firstName = shortenName(firstName);
    		if (lName.equals(lastName) && (tempFName.contains(firstName)||firstName.contains(tempFName))) {
    			isAMatch = true;
    			System.out.println("Found match by 2nd check");
    			break;
    		}
    		
    		// 3rc check: Check that the names could be a subset of one another
    		String shorter, longer;
    		if (prplFriendName.length() < fullName.length()) {
    			shorter = prplFriendName;
    			longer = fullName;
    		}
    		else {
    			shorter = fullName;
    			longer = prplFriendName;
    		}
    		if (longer.startsWith(shorter)) {
    			isAMatch = true;
    			System.out.println("Found match by 3rd check");
    			break;
    		}
    		
    		// 4th check: check the first and last names are contained in each other
    		String[] nameParts = prplFriendName.split("\\s+");
    		boolean containsFirstName = false, containsLastName = false;
    		for (int l = 0; l < nameParts.length; l++) {
    			if (shortenName(nameParts[l]).equals(firstName)) {
    				containsFirstName = true;
    				continue;
    			}
    			if (nameParts[l].equals(lastName)) {
    				containsLastName = true;
    				continue;
    			}
    		}
    		if (containsFirstName && containsLastName) {
    			isAMatch = true;
    			System.out.println("Found match by 4th check");
    			break;
    		}	
    	}
    	
    	// Found a match
    	if (isAMatch)
    	{	
    		System.out.println("Found Identity name='"+identity.getName()+"' ("+identity.getURI()+
    				           ") to match Person resource '"+person.getName()+"' ("+personURI+")");
    		return personURI;
    	}
    	
    	System.out.println("Did not Find a match for Identity name='"+identity.getName()+"' ("+identity.getURI()+")");
    	return null;
	}
	
	
	private static boolean firstCheckMatch(String fullName, String[] memberNames)
	{
		if (memberNames == null || memberNames.length <= 0)
			return false;
		
		for (int k = 0; k < memberNames.length; k++)
    	{
    		String name = memberNames[k];
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
											String name, String fName, String lName)
	{
		firstName = shortenName(firstName);
		fName = shortenName(fName);
		if (lName.equals(lastName) && (fName.contains(firstName)||firstName.contains(fName))) {
			return true;
		}
		
		return false;
	}

	private static boolean thirdCheckMatch(String fullName, String firstName, String lastName,
										   String name, String fName, String lName)
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
		if (longer.startsWith(shorter)) {
			return true;
		}
		
		return false;
	}

	private static boolean fourthCheckMatch(String fullName, String firstName, String lastName,
			   								String name, String fName, String lName)
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

}
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
    		
		
		
    				


