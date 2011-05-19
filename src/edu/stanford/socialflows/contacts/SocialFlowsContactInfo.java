package edu.stanford.socialflows.contacts;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import edu.stanford.socialflows.algo.util.EntityInfo;
import edu.stanford.socialflows.connector.FBService;

public class SocialFlowsContactInfo extends BaseContactInfo implements Comparable<SocialFlowsContactInfo>, EntityInfo<String> {

	private long sfUserId = -1;
	private long sfContactId = -1;
	private String tempContactId = null;
	
	private int socialWeight = 0;
	private int photoFreq    = 0;
	//JSONObject representation = null;
	JSONObject emailData   = null;
	JSONObject contactData = null;
	
	HashMap<String, HashSet<String>> appAccounts = null; /* various non-email social networking/apps accounts */
	HashSet<String> emailAddresses = null;
	HashSet<String> aliasNames = null;
	

	public void setSfUserId(long id) {
		this.sfUserId = id;
	}
	
	public long getSfUserId() {
		return this.sfUserId;
	}
	
	public void setContactId(long id) {
		this.sfContactId = id;
	}
	
	public long getContactId() {
		return this.sfContactId;
	}
	
	public HashSet<String> getFBUids() {
		// check if previously existed in records
		// - check by FB ID (is credible)
		HashMap<String, HashSet<String>> fbAcct = this.getAccounts();
		if (fbAcct == null)
			return null;
		return fbAcct.get(FBService.FACEBOOK_PROVIDER);
	}
	
	
	public void setSocialWeight(int weight) {
		this.socialWeight = weight;
	}
	
	public int getSocialWeight() {
		return this.socialWeight;
	}
	
	public void setContactData(JSONObject data) {
		this.contactData = data;
	}
	
	public JSONObject getContactData() {
		return this.contactData;
	}
	
	public void setEmailData(JSONObject data) {
		this.emailData = data;
	}
	
	public JSONObject getEmailData() {
		return this.emailData;
	}
	
	public void setTempContactId(String userId) {
		this.tempContactId = userId;
	}
	
	public String getTempContactId() {
		return this.tempContactId;
	}
	
	public void addAccount(String acctID, String acctDomain) {
		if (this.appAccounts == null)
			this.appAccounts = new HashMap<String, HashSet<String>>();
		HashSet<String> acctIDs = this.appAccounts.get(acctDomain);
		if (acctIDs == null) {
			acctIDs = new HashSet<String>();
			this.appAccounts.put(acctDomain, acctIDs);
		}
		acctIDs.add(acctID);
	}
	
	public HashMap<String, HashSet<String>> getAccounts() {
		return this.appAccounts;
	}
	
	public void addEmailAddress(String emailAddress) {
		if (this.emailAddresses == null)
			this.emailAddresses = new HashSet<String>();
		this.emailAddresses.add(emailAddress);
	}
	
	public HashSet<String> getEmailAddresses() {
		return this.emailAddresses;
	}
	
	public void addName(String name) {
		if (this.aliasNames == null)
			this.aliasNames = new HashSet<String>();
		this.aliasNames.add(name);
	}
	
	public HashSet<String> getNames() {
		return this.aliasNames;
	}

	@Override
	public int compareTo(SocialFlowsContactInfo another) 
	{
		// Want to have contacts with higher social weights appearing earlier
		/* --- Note: Disabled, based upon Jeff's recommendation (Heer)
		if (this.socialWeight > o.getSocialWeight())
			return -1;
		else if (this.socialWeight < o.getSocialWeight())
			return 1;
		*/
		
		String thisName = this.getName(), 
		       otherPersonName = another.getName();
		if (thisName == null)
			thisName = "";
		if (otherPersonName == null)
			otherPersonName = "";
		
		return thisName.compareToIgnoreCase(otherPersonName);
	}
	
	public static Comparator<SocialFlowsContactInfo> socialWeightedOrder
	= new Comparator<SocialFlowsContactInfo>() {
		public int compare(SocialFlowsContactInfo o1, SocialFlowsContactInfo o2)
		{
			if (o1.getSocialWeight() > o2.getSocialWeight())
				return -1;
			else if (o1.getSocialWeight() < o2.getSocialWeight())
				return 1;
			return o1.compareTo(o2);
		}
      };

    public String getEntityDirId() {
    	return String.valueOf(this.getContactId());
    }

    // TODO: JSON representation of SocialFlowsContactInfo
    public JSONObject toJSON()
    {
    	//if (this.representation != null)
    	//	 return this.representation;

    	JSONObject jsonRep = new JSONObject();
    	try
    	{
    		jsonRep.putOpt("id", String.valueOf(this.sfContactId));
        	jsonRep.putOpt("displayName", this.getName());
        	
        	HashMap<String, HashSet<String>> accounts = this.getAccounts();
        	if (accounts != null) {
        		JSONArray accountsJSON = new JSONArray();
        		for (String domain : accounts.keySet()) {
        			for (String userid : accounts.get(domain)) {
        				JSONObject acctEntry = new JSONObject();
        				acctEntry.putOpt("domain", domain);
        				acctEntry.putOpt("userid", userid);
        				accountsJSON.put(acctEntry);
        			}
        		}
        		if (accountsJSON.length() > 0)
        			jsonRep.putOpt("accounts", accountsJSON);
        	}
    	}
    	catch (JSONException jsone) { }

    	//this.representation = jsonRep;
    	return jsonRep;
    }

}
