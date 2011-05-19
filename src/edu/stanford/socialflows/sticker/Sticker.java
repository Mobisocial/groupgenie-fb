package edu.stanford.socialflows.sticker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.stanford.prpl.api.Group;
import edu.stanford.prpl.api.Resource;
import edu.stanford.socialflows.contacts.SocialFlowsAddressBook;
import edu.stanford.socialflows.contacts.SocialFlowsContactInfo;
import edu.stanford.socialflows.sticker.SocialFlowsGroup;

public class Sticker implements Comparable<Sticker>
{
	//Properties
	public static final String STICKER = "Sticker";
	public static final String STICKERNAME = "stickerName";
	public static final String STICKERICON = "stickerIconUrl";
	public static final String CLIQUEMEMBER = "cliqueMember";
	public static final String COLLECTIONITEM = "collectionItem";
	public static final String PRPL_ACGROUP = "prplACGroup";
	
	//Property URIs
	public static final String STICKER_URI = "http://prpl.stanford.edu/#Sticker";
	public static final String STICKERNAME_URI = "http://prpl.stanford.edu/#stickerName";
	public static final String STICKERICON_URI = "http://prpl.stanford.edu/#stickerIconUrl";
	public static final String CLIQUEMEMBER_URI = "http://prpl.stanford.edu/#cliqueMember";
	public static final String COLLECTIONITEM_URI = "http://prpl.stanford.edu/#collectionItem";
	public static final String PRPL_ACGROUP_URI   = "http://prpl.stanford.edu/#prplACGroup";
	
	// Source types/Data sources from which Social Topology can be computed from
	public static final String SOURCE_EMAIL    = "email";
	public static final String SOURCE_PHOTO    = "photo";
	public static final String SOURCE_SMS      = "sms";
	public static final String SOURCE_CHAT     = "chat";
	public static final String SOURCE_COMMENTS = "comments";
	
	// For suggested stickers
	public String stickerID = null; // used by suggested stickers
	public long stickerTopologyRun = -1; // topology run which generated suggested sticker 
	
	private String stickerSfID = null;
	private String stickerName = "";
	private String stickerIconURI = null;
	private String stickerURI = null;
	
	// PrPl representation
	private Resource stickerRes = null; // PrPl representation of sticker
	private Group stickerGroup  = null; // PrPl access control primitive
	
	//TODO: Solve missing Clique problem
	public boolean isSaved = false;
	public boolean isDeleted = false;
	private String sourceType = "none"; // possible values: "photo", "email", "comments", "sms", "chat"
	
	private SocialFlowsGroup clique = null;
	private Set<String> cliqueMemberIDs = new HashSet<String>();
	private Set<Resource> cliqueMemberResources = new HashSet<Resource>();
	private Set<String> collection = new HashSet<String>();
	
	public Sticker parentSticker = null;
	public List<Sticker> subsetStickers = new Vector<Sticker>();
	
	
	public Sticker() 
	{ }
	
	public Sticker(SocialFlowsGroup group) {
		this.clique = group;
		group.groupSticker = this;
		for (SocialFlowsContactInfo groupMember : this.clique.getGroupMembers()) {
			long sfContactID = groupMember.getContactId();
			if (sfContactID <= 0)
				cliqueMemberIDs.add(groupMember.getTempContactId());
			else
				cliqueMemberIDs.add(String.valueOf(groupMember.getContactId()));
		}
	}
	
	public int compareTo(Sticker s)
	{
		if (this.clique != null && s.getClique() != null)
			return this.clique.compareTo(s.getClique());		
		return 0;
	}
	
	/*
	public Sticker(String URI, String name, String stickerIconURI)
	{
		this.stickerURI = URI;
		this.stickerName = name;
		this.stickerIconURI = stickerIconURI;
	}
	
	public Sticker(String URI, String name, String stickerIconURI, SocialFlowsGroup group)
	{
		this(URI, name, stickerIconURI);
		this.clique = group;
	}
	*/
	
	public void setPrPlResource(Resource stickerRep)
	{
		this.stickerRes = stickerRep;
	}
	
	public Resource getPrPlResource()
	{
		return this.stickerRes;
	}
	
	public void setACGroup(Group acgroup)
	{
		this.stickerGroup = acgroup;
	}
	
	public Group getACGroup()
	{
		return this.stickerGroup;
	}
	
	public void setStickerSfID(String sfID)
	{
		this.stickerSfID = sfID;
	}
	
	public String getStickerSfID()
	{
		return this.stickerSfID;
	}
	
	public void setStickerURI(String URI)
	{
		this.stickerURI = URI;
	}
	
	public String getStickerURI()
	{
		return this.stickerURI;
	}
	
	public void setStickerName(String name)
	{
		this.stickerName = name;
	}
	
	public String getStickerName()
	{
		return this.stickerName;
	}
	
	public void setStickerIconURI(String stickerIconURI)
	{
		this.stickerIconURI = stickerIconURI;
	}
	
	public String getStickerIconURI()
	{
		return this.stickerIconURI;
	}
	
	public void setSourceType(String sourceType)
	{
		if (sourceType == null || sourceType.trim().isEmpty())
		   this.sourceType = "none";
		else
		   this.sourceType = sourceType;
	}
	
	public String getSourceType()
	{
		return this.sourceType;
	}
	

	// Collection methods
	public void addCollectionItem(String itemID)
	{
		if (itemID == null)
			return;
		this.collection.add(itemID);
	}
	
	public void removeCollectionItem(String itemID)
	{
		if (itemID == null)
			return;
		this.collection.remove(itemID);
	}

	public Set<String> getCollection()
	{
		return this.collection;
	}


	// Clique methods
	public void setClique(SocialFlowsGroup group)
	{
		this.clique = group;
		if (group != null) {
		   group.groupSticker = this;
		   this.cliqueMemberIDs.clear();
		   for (SocialFlowsContactInfo groupMember : group.getGroupMembers()) {
			   if (groupMember.getContactId() > 0)
				   this.cliqueMemberIDs.add(String.valueOf(groupMember.getContactId()));
			   else
				   this.cliqueMemberIDs.add(groupMember.getTempContactId());
		   }
		}
		else {
		   this.cliqueMemberIDs.clear();
		}
	}
	
	public SocialFlowsGroup getClique()
	{
		return this.clique;		
	}

	public SocialFlowsGroup getCliqueLazily(SocialFlowsAddressBook addressBook)
	{
		if (this.clique != null || addressBook == null)
			return this.clique;
		
		/*
		if (this.cliqueMemberResources == null || this.cliqueMemberResources.size() == 0) {
			// Retrieve Person resources using URIs
			this.clique = SocialFlowsGroup.generateGroup(this.cliqueMemberIDs, addressBook);
		}
		else {
			this.clique = SocialFlowsGroup.generateGroupFromMembers(this.cliqueMemberResources, addressBook);
		}
		*/
		this.clique = SocialFlowsGroup.generateSocialFlowsGroup(this.cliqueMemberIDs, addressBook);
		this.clique.groupSticker = this;
		//this.clique.groupName = this.stickerName;
		//this.clique.isSaved = this.isSaved;
		
		return this.clique;		
	}
	
	public int getCliqueSize()
	{
		if (this.cliqueMemberIDs != null)
			return this.cliqueMemberIDs.size();
		if (this.clique != null)
			return this.clique.getGroupMemberSize();

		return 0;
	}
	
	public Set<Resource> getCliqueMemberResources()
	{
		return this.cliqueMemberResources;
	}
	
	public Set<String> getCliqueMemberIDs()
	{
		return this.cliqueMemberIDs;
	}
	
	public void addCliqueMemberID(String friendID)
	{
		if (friendID == null)
			return;
		this.cliqueMemberIDs.add(friendID);
	}
	
	public void removeCliqueMemberID(String friendID)
	{
		if (friendID == null)
			return;
		this.cliqueMemberIDs.remove(friendID);
	}

	public void addCliqueMemberResource(Resource friend)
	{
		if (friend == null)
			return;
		this.cliqueMemberResources.add(friend);
		addCliqueMemberID(friend.getURI());
	}
	
	public void removeCliqueMemberResource(Resource friend)
	{
		if (friend == null)
			return;
		this.cliqueMemberResources.remove(friend);
		removeCliqueMemberID(friend.getURI());
	}
	
	
	
	

	
	
	public boolean isSubsetOf(Sticker s) {
		if (this.cliqueMemberIDs == null || s == null)
			return false;
		Set<String> supersetMembers = s.getCliqueMemberIDs();
		if (supersetMembers == null)
			return false;
		return supersetMembers.containsAll(this.cliqueMemberIDs);
		/*
		if (s == null || this.clique == null)
			return false;
		return this.clique.isSubsetOf(s.clique);
		*/
	}
	
	// Will trickle down and duplicate itself to the correct places
	public void addSubset(Sticker s, boolean discardDuplicates)
	{
		if (this.subsetStickers == null) {
			this.subsetStickers = new Vector<Sticker>();
		}
		
		boolean addAsDirectSubset = true;
		Iterator<Sticker> stickerIter = this.subsetStickers.iterator();
		while (stickerIter.hasNext()) {
			Sticker currentSubset = stickerIter.next();
			if (s.isSubsetOf(currentSubset))
			{
				System.out.println("Found ["+s.getCliqueMemberIDs().toString()+"] to be a SUBSET of ["+currentSubset.getCliqueMemberIDs().toString()+"]");
				
				if (s.getCliqueSize() == currentSubset.getCliqueSize()) {
					if (discardDuplicates) {
						System.out.println("But size is the same, so ignoring!!! \n");
						return; // already exists
					}
					else
						break;
				}
				addAsDirectSubset = false; // should not be a direct subset
				currentSubset.addSubset(s, discardDuplicates);
				
				// TODO: For now Sticker has one parent
				break;
			}
			else if (currentSubset.isSubsetOf(s))
			{
				System.out.println("Found ["+s.getCliqueMemberIDs().toString()+"] to be a SUPERSET of ["+currentSubset.getCliqueMemberIDs().toString()+"]");
				
				if (s.getCliqueSize() == currentSubset.getCliqueSize()) {
					if (discardDuplicates) {
						System.out.println("But size is the same, so ignoring!!! \n");
						return; // already exists
					}
					else
						break;
				}
				stickerIter.remove(); // trying to replace this subset as a direct subset
				currentSubset.parentSticker = null;
				s.addSubset(currentSubset, discardDuplicates);
				
				// DO NOT BREAK here, because we want to check if any other peers should be a subset of us
			}
		}
		
		if (addAsDirectSubset) {
			this.subsetStickers.add(s);
			s.parentSticker = this;
		}
	}
	
	// Removal only for reordering, not deletion
	private void detachSubset(Sticker subset)
	{
		if (this.subsetStickers != null)
		    this.subsetStickers.remove(subset);
		if (subset.parentSticker == this)
			subset.parentSticker = null;
	}
	
	public List<Sticker> getSubsets()
	{
		return this.subsetStickers;
	}
	
	public void setSubsets(List<Sticker> subsetsList)
	{
		this.subsetStickers = subsetsList;
		if (subsetsList == null)
		   this.subsetStickers = new Vector<Sticker>();  
	}

	// Will delete all unsaved subset groups, and move saved subset-groups and 
	// their children (both saved and unsaved) to the specified new parent
	public static void deleteSticker(Sticker removedGroup, Sticker newParent,
								     HashMap<String, Sticker> stickersLookup,
								     List<Sticker> stickers,
								     boolean removeUnsavedSubsets)
	{
        List<Sticker> newParentSubsets = newParent.getSubsets();
        int indexToInsert = newParentSubsets.indexOf(removedGroup);
        if (indexToInsert == -1)
        	indexToInsert = newParentSubsets.size();

        Iterator<Sticker> subsetIter = null;
        if (removeUnsavedSubsets) {
        	Vector<Sticker> savedSubsets = new Vector<Sticker>();
            Sticker.getSavedSubsets(removedGroup, savedSubsets,
            						stickersLookup, stickers);
            subsetIter = savedSubsets.iterator();
        }
        else {
        	List<Sticker> removedGroupSubsets = removedGroup.getSubsets();
        	if (removedGroupSubsets != null)
        	   subsetIter = removedGroupSubsets.iterator();
        }
        
        while(subsetIter != null && subsetIter.hasNext()) {
        	Sticker subset = subsetIter.next();
        	subset.parentSticker = newParent;
        	newParentSubsets.add(indexToInsert, subset);
        	indexToInsert++;
        }
        newParentSubsets.remove(removedGroup);
	}
	
	public static void getSavedSubsets(Sticker removedGroup, Vector<Sticker> savedSubsets,
									   HashMap<String, Sticker> stickersLookup,
									   List<Sticker> stickers)
	{
		if (removedGroup == null || removedGroup.getSubsets() == null)
			return;
		Iterator<Sticker> subsetIter = removedGroup.getSubsets().iterator();
        while(subsetIter.hasNext()) {
        	Sticker subset = subsetIter.next();
        	if (subset.isSaved) {
        		// if a subset is saved, keep all its own subsets as well, 
        		// both saved and unsaved
        		savedSubsets.add(subset);
        	}
        	else {
        		Sticker.getSavedSubsets(subset, savedSubsets,
        				                stickersLookup, stickers);
        		// Remove Sticker representing unsaved group
        		Sticker removedSticker = subset;
        		if (removedSticker != null) {
        			if (stickersLookup != null)
                        removedSticker = stickersLookup.remove(removedSticker.stickerID);
                    if (stickers != null)
                        stickers.remove(removedSticker);
        		}
        	}
        }
	}
	

	public static void flattenStickerAndSubsets(Sticker group, List<Sticker> flatList)
	{
		if (flatList == null || group == null)
			return;		
		if (!flatList.contains(group))
			flatList.add(group);
		if (group.getSubsets() != null) {
			for (Sticker subset : group.getSubsets()) {
				flattenStickerAndSubsets(subset, flatList);
			}
		}
	}
	
	public static Sticker generateStickerAndSubsets(JSONObject groupJSON, String sourceType,
			                                        SocialFlowsAddressBook myAddressBook) throws JSONException
	{
		JSONObject groupData = groupJSON.getJSONObject("group");
		SocialFlowsGroup group = SocialFlowsGroup.generateGroup(groupData, myAddressBook);
		Collections.sort(group.groupMembers);
		Sticker sticker = new Sticker(group);
		sticker.setSourceType(sourceType);
		
		try {
			JSONArray subsets = groupJSON.getJSONArray("subsets");
			for (int i = 0; i < subsets.length(); i++) {
				JSONObject subsetJSON = subsets.getJSONObject(i);
				Sticker subsetSticker
				= Sticker.generateStickerAndSubsets(subsetJSON, sourceType, myAddressBook);
				if (subsetSticker != null) {
				   sticker.addSubset(subsetSticker, true);
				}
			}
		}
		catch (JSONException jsone)
		{
			// reach base case, essentially
		}
		
		return sticker;
	}
	
}