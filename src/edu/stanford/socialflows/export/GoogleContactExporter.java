package edu.stanford.socialflows.export;

import java.net.*;
import java.io.*;
import java.util.*;
import org.json.*;
import com.google.gdata.client.*;
import com.google.gdata.client.http.*;
import com.google.gdata.client.contacts.*;
import com.google.gdata.data.*;
import com.google.gdata.data.contacts.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;

import edu.stanford.socialflows.contacts.SocialFlowsContactInfo;
import edu.stanford.socialflows.sticker.Sticker;
import edu.stanford.socialflows.util.StatusProvider;

public class GoogleContactExporter implements StatusProvider 
{
	ContactsService gmailService = null;
	
    // Build up list of user's current Google Contact Groups
    HashMap<String, GoogleContactGroup> nameToGoogleContactGroup
    = new HashMap<String, GoogleContactGroup>();
    HashMap<String, GoogleContactGroup> idToGoogleContactGroup
    = new HashMap<String, GoogleContactGroup>();
    HashMap<String, HashSet<GoogleContact>> idToGoogleContactGroupMembership
    = new HashMap<String, HashSet<GoogleContact>>();
    
    // Build up list of user's current Google addressbook
    HashMap<String, GoogleContact> emailToGoogleContact
    = new HashMap<String, GoogleContact>();
    
	
    public GoogleContactExporter(ContactsService service)
    {
    	this.gmailService = service;
    }
	
    public void initContactGroups() throws MalformedURLException, ServiceException, IOException
    {
        //Request the feed
        URL feedUrl = new URL("http://www.google.com/m8/feeds/groups/default/full");
        ContactGroupFeed groupFeed = gmailService.getFeed(feedUrl, ContactGroupFeed.class);
        
        this.setStatusMessage("Verifying your current Gmail contact groups...");
        
        // Print the results
        System.out.println(groupFeed.getTitle().getPlainText());

        while (groupFeed.getEntries().size() > 0) 
        {
            for (int i = 0; i < groupFeed.getEntries().size(); i++) 
            {
                ContactGroupEntry groupEntry = groupFeed.getEntries().get(i);
                if (groupEntry.hasSystemGroup())
                   continue;

                System.out.println("Id: " + groupEntry.getId());
                System.out.println("Group Name: " + groupEntry.getTitle().getPlainText());
                System.out.println("Last Updated: " + groupEntry.getUpdated());

                System.out.println("Self Link: " + groupEntry.getSelfLink().getHref());
                System.out.println("Edit Link: " + groupEntry.getEditLink().getHref());
                System.out.println("ETag: " + groupEntry.getEtag());
                
                GoogleContactGroup gContactGroup = new GoogleContactGroup();
                gContactGroup.entry = groupEntry;
                nameToGoogleContactGroup.put(groupEntry.getTitle().getPlainText().toLowerCase(),
                                             gContactGroup);
                idToGoogleContactGroup.put(groupEntry.getId(), gContactGroup);
                idToGoogleContactGroupMembership.put(groupEntry.getId(), 
                		                             new HashSet<GoogleContact>());
            }
            
            if (groupFeed.getNextLink() == null)
                break;
            
            System.out.println("\nNEXT PAGE: "+groupFeed.getNextLink().getHref());
            feedUrl = new URL(groupFeed.getNextLink().getHref());
            groupFeed 
            = gmailService.getFeed(feedUrl, ContactGroupFeed.class);
        }
    }
    
    public void initContacts() throws MalformedURLException, ServiceException, IOException
    {        
        URL feedUrl 
        = new URL("http://www.google.com/m8/feeds/contacts/default/full?max-results=50"); // ?max-results=1000
        
        this.setStatusMessage("Verifying your current Gmail contact addressbook...");

        /*
        // Create query and submit a request
        Calendar startDate = Calendar.getInstance();
        startDate.set(2010, 1, 1);
        DateTime startTime = new DateTime(startDate.getTime());
        Query myQuery = new Query(feedUrl);
        myQuery.setUpdatedMin(startTime);
        ContactFeed resultFeed = gmailService.query(myQuery, ContactFeed.class);
        */
        
        com.google.gdata.data.contacts.ContactFeed resultFeed 
        = gmailService.getFeed(feedUrl, com.google.gdata.data.contacts.ContactFeed.class);

        // Print the results
        System.out.println(resultFeed.getTitle().getPlainText());
        int numContacts = 0;
        
        while (resultFeed.getEntries().size() > 0) 
        {
            numContacts += resultFeed.getEntries().size();
            for (int i = 0; i < resultFeed.getEntries().size(); i++)
            {
               com.google.gdata.data.contacts.ContactEntry entry 
               = resultFeed.getEntries().get(i);
               
               if (entry.hasName()) 
               {
                  Name name = entry.getName();
                  if (name.hasFullName()) {
                     System.out.println("\n"+name.getFullName().getValue());
                  } 
                  else if (name.hasGivenName()) {
                     System.out.println("\n"+name.getGivenName().getValue());
                  }
                  else
                     System.out.println("\n???");
               }
               else
                   System.out.println("\n???");

               System.out.println("Email addresses:");
               GoogleContact gContact = new GoogleContact();
               gContact.entry = entry;
               for (Email email : entry.getEmailAddresses()) {
                   emailToGoogleContact.put(email.getAddress(), gContact);
                   System.out.println("--> " + email.getAddress());
               }

               Link photoLink = entry.getContactPhotoLink();
               String photoLinkHref = photoLink.getHref();
               System.out.println("Photo Link: " + photoLinkHref);
               
               String groupInfo = "Member of groups: ";
               for (GroupMembershipInfo group : entry.getGroupMembershipInfos())
               {      
                   String groupHref = group.getHref();
                   GoogleContactGroup match
                   = idToGoogleContactGroup.get(groupHref);
                   
                   if (match != null)
                      groupInfo += match.entry.getTitle().getPlainText()+", ";
                   //else
                   //  groupInfo += groupHref+", ";
                   
                   HashSet<GoogleContact> groupMembership
                   = idToGoogleContactGroupMembership.get(groupHref);
                   if (groupMembership != null)
                      groupMembership.add(gContact);
               }
               System.out.println(groupInfo);

               System.out.println("Last time Updated: " + entry.getUpdated().toStringRfc822());
            }
            
            if (resultFeed.getNextLink() == null)
                break;
            
            System.out.println("\nNEXT PAGE: "+resultFeed.getNextLink().getHref());
            feedUrl = new URL(resultFeed.getNextLink().getHref());
            resultFeed 
            = gmailService.getFeed(feedUrl, com.google.gdata.data.contacts.ContactFeed.class);
        }
    }
    
    
	public void exportToGoogle(Sticker sticker) throws MalformedURLException, ServiceException, IOException
	{
		if (sticker == null)
			return;
		
	    System.out.println("\n\n\n");
	    
	    HashSet<GoogleContact> gmailContactsToUpdate 
	    = new HashSet<GoogleContact>();
	    
	    GoogleContactGroup contactGroup
        = nameToGoogleContactGroup.get(sticker.getStickerName().toLowerCase());
        if (contactGroup == null) 
        {
        	// Create the entry to insert
        	ContactGroupEntry group = new ContactGroupEntry();
        	group.setTitle(new PlainTextConstruct(sticker.getStickerName()));
        	
        	// Ask the service to insert the new entry
        	URL postUrl = new URL("http://www.google.com/m8/feeds/groups/default/full");
        	ContactGroupEntry newGroupEntry
        	= gmailService.insert(postUrl, group);
            if (newGroupEntry != null)
            {
            	this.setStatusMessage("Creating Google Contact Group for <b>"+sticker.getStickerName()+"</b>...");
            	
            	contactGroup = new GoogleContactGroup();
            	contactGroup.entry = newGroupEntry;
                nameToGoogleContactGroup.put(newGroupEntry.getTitle().getPlainText().toLowerCase(),
                		                     contactGroup);
                idToGoogleContactGroup.put(newGroupEntry.getId(), contactGroup);
                idToGoogleContactGroupMembership.put(newGroupEntry.getId(), new HashSet<GoogleContact>());
                
                System.out.println("Created Gmail Contact Group for '"+sticker.getStickerName()+"'");
            }
            else 
            {
            	System.out.println("Error creating Gmail Contact Group for '"+sticker.getStickerName()+"'");
            	return;
            }
        }


        // Update contact membership for given contact group
        ContactGroupEntry gmailContactGrp = contactGroup.entry;
        
        if (sticker.getClique().getGroupMembers() != null)
        {
        	HashSet<GoogleContact> previousCliqueMembers
            = idToGoogleContactGroupMembership.get(gmailContactGrp.getId());
        	HashSet<GoogleContact> currentCliqueMembers = new HashSet<GoogleContact>();
            HashSet<GoogleContact> addCliqueMembers     = new HashSet<GoogleContact>();

            // Create new contacts or update existing contacts
        	for (SocialFlowsContactInfo sfContact : sticker.getClique().getGroupMembers())
        	{
        		// Check if such a contact already exists in Gmail
        		boolean createNewContact = false;
        		GoogleContact gContact = null;
        		com.google.gdata.data.contacts.ContactEntry contact = null;
        		HashSet<String> emailAddresses = sfContact.getEmailAddresses();
        		if (emailAddresses != null) {
        			for (String email : emailAddresses) {
            			gContact = emailToGoogleContact.get(email);
            			if (gContact != null)
            				break;
            		}
        		}

        		// Create a new contact in Gmail
        		if (gContact == null) 
        		{
        			System.out.println("Creating Gmail Contact for "+sfContact.getName());
        			
        			// Create the new contact to insert
        			createNewContact = true;
        			contact = new com.google.gdata.data.contacts.ContactEntry();
        			gContact = new GoogleContact();
                    gContact.entry = contact;
                    for (Email email : contact.getEmailAddresses()) {
                        emailToGoogleContact.put(email.getAddress(), gContact);
                        System.out.println("--> " + email.getAddress());
                    }
        		}
        		else {
        			contact = gContact.entry;
        		}
        		
        		// Set or update contact metadata
                Name name = new Name();
                final String NO_YOMI = null;
                name.setFullName(new FullName(sfContact.getName(), NO_YOMI));
                if (sfContact.getFirstName() != null)
                   name.setGivenName(new GivenName(sfContact.getFirstName(), NO_YOMI));
                if (sfContact.getLastName() != null)
                   name.setFamilyName(new FamilyName(sfContact.getLastName(), NO_YOMI));
                contact.setName(name);

                // Add the email addresses
                HashSet<String> currentEmails = new HashSet<String>();
                if (contact.getEmailAddresses() != null) {
                	for (Email email : contact.getEmailAddresses()) {
                		currentEmails.add(email.getAddress());
                	}
                }

                if (emailAddresses != null) {
                	for (String email : emailAddresses) {
                    	if (currentEmails.contains(email))
                    		continue;
                        Email eMailAddr = new Email();
                        eMailAddr.setAddress(email);
                        eMailAddr.setRel("http://schemas.google.com/g/2005#other");
                        eMailAddr.setPrimary(false);
                        contact.addEmailAddress(eMailAddr); 
                    }
                }
                
                // Check if extended property previously exists,
                // if so update instead of creating anew
                ExtendedProperty sfContactId = null;
                boolean addExtendedProperty = false;
                if (contact.getExtendedProperties() != null)
                {
                	for (ExtendedProperty ep : contact.getExtendedProperties())
                	{
                        if (ep.getName().equals("SocialFlows ContactID")) {
                        	sfContactId = ep;
                        	break;
                        }
                    }
                }
                
                if (sfContactId == null) {
                   sfContactId = new ExtendedProperty();
                   addExtendedProperty = true;
                }
                sfContactId.setName("SocialFlows ContactID");
                sfContactId.setValue(String.valueOf(sfContact.getContactId()));
                if (addExtendedProperty)
                   contact.addExtendedProperty(sfContactId);
                
                
                /*
                // Update contact photo
                if (contact.getContactPhotoLink() == null) {
                    // Add profile photo
                    byte[] photoData = null;
                    
                    Link photoLink = contact.getContactPhotoLink();
                    URL photoUrl = new URL(photoLink.getHref());
                    GDataRequest request
                    = gmailService.createRequest(GDataRequest.RequestType.UPDATE,
                                                 photoUrl, new ContentType("image/jpeg"));

                    OutputStream requestStream = request.getRequestStream();
                    requestStream.write(photoData);
                    request.execute();
                }
                */

                // Ask the service to insert or update contact entry
                URL postUrl = new URL("http://www.google.com/m8/feeds/contacts/default/full");
                if (createNewContact) {
                	System.out.println("Creating Gmail Contact for "+sfContact.getName());
                	
        			this.setStatusMessage("Creating Gmail Contact for <b>"+sfContact.getName()+"</b>...");
                	gContact.entry = gmailService.insert(postUrl, contact);
                	if (gContact.entry == null)
                		System.out.println("Error creating Gmail Contact for "+sfContact.getName());
                }
                else {
                	System.out.println("Updating Gmail Contact for "+sfContact.getName());
                	
                	this.setStatusMessage("Updating Gmail Contact for <b>"+sfContact.getName()+"</b>...");
                	URL editUrl = new URL(contact.getEditLink().getHref());
                	gContact.entry = gmailService.update(editUrl, contact);
                	if (gContact.entry == null)
                        System.out.println("Error updating Gmail Contact for "+sfContact.getName());
                }

        		// Figure out which contacts to add and delete
        		currentCliqueMembers.add(gContact);
        		if (!previousCliqueMembers.contains(gContact))
        			addCliqueMembers.add(gContact);

        	} // end-of-loop to update contacts
        	
        	
        	
        	System.out.println("Updating Gmail Contact Group for '"+sticker.getStickerName()+"'");
        	this.setStatusMessage("Updating Gmail Contact Group for <b>"+sticker.getStickerName()+"</b>...");
        	
        	// Now update the Gmail contact group memberships
        	for (GoogleContact socialContact : previousCliqueMembers)
        	{
        		if (!currentCliqueMembers.contains(socialContact))
        		{
        			gmailContactsToUpdate.add(socialContact);
        			Iterator<GroupMembershipInfo> iter
        			= socialContact.entry.getGroupMembershipInfos().iterator();
        			// Remove group membership for this contact 
        			while (iter.hasNext()) {
        				GroupMembershipInfo gmi = iter.next();
        				if (gmi.getHref().equals(gmailContactGrp.getId())) {
        					System.out.println("--> -REMOVED "+socialContact.entry.getName().getFullName().getValue());
        					
        					iter.remove();
        					break;
        				}
        			}
        		}
        	}
        	
        	// Add group members
        	for (GoogleContact socialContact : addCliqueMembers)
        	{
        		GroupMembershipInfo groupMembershipInfo 
        		= new GroupMembershipInfo();
        		groupMembershipInfo.setHref(gmailContactGrp.getId());
        		socialContact.entry.addGroupMembershipInfo(groupMembershipInfo);
        		gmailContactsToUpdate.add(socialContact);
        		
        		this.setStatusMessage("Added <b>"+socialContact.entry.getName().getFullName().getValue()+"</b> "
        				              +" to <b>"+sticker.getStickerName()+"</b> contact group...");
        		System.out.println("--> +ADDED "+socialContact.entry.getName().getFullName().getValue());
        	}
        	
        } // end-of-updating group membership for a sticker
        
	    
	    // Do updates on all Google contacts that had contact group membership changes
	    for (GoogleContact updateContact : gmailContactsToUpdate) {
	    	updateContact.entry = updateContact.entry.update();
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
