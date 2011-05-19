package edu.stanford.prpl.insitu.util;

import java.io.*;
import java.util.*;
import java.security.MessageDigest;

import edu.stanford.prpl.api.PRPLAppClient;
import edu.stanford.prpl.impl.client.app.PRPLAppClientImpl;

import edu.stanford.prpl.insitu.settings.*;
import edu.stanford.socialflows.settings.FBAppSettings;
import edu.stanford.socialflows.util.*;

public class PrPlConnectionManager implements StatusProvider
{	
	PRPLAppClient prplService_ = null;
	String prplPropFilePath = null, prplKeysFilePath = null;
	PrPlHost pcbHost = new PrPlHost(null, null);
	String currentUserID = null, currentUserPwd = null, currentUserName = null;
	String currentHostName = null;
	
	
	public String getPrplPropFilePath() {
		return this.prplPropFilePath;
	}
	
	public void setPrplPropFilePath(String filePath) {
		this.prplPropFilePath = filePath;
	}
	
	public String getPrplKeysFilePath() {
		return this.prplKeysFilePath;
	}
	
	public void setPrplKeysFilePath(String filePath) {
		this.prplKeysFilePath = filePath;
	}
	
	public String getCurrentUserID() {
		return this.currentUserID;
	}
	
	public void setCurrentUserID(String userID) {
		this.currentUserID = userID;
		if (this.currentUserID != null && !this.currentUserID.isEmpty())
			this.prplPropFilePath = "/tmp/"+FBAppSettings.appName+"/prpl/"+this.currentUserID+".properties";
	}
	
	public String getCurrentUserName() {
		return this.currentUserName;
	}
	
	public void setCurrentUserName(String userName) {
		this.currentUserName = userName;
	}
	
	public void setCurrentUserPwd(String userPwd) {
		if (userPwd != null && userPwd.length() > 0) {
			String pwdHash = org.apache.commons.codec.digest.DigestUtils.md5Hex(userPwd);
			this.currentUserPwd = pwdHash;
		}
		else
			this.currentUserPwd = "";
	}
	
	public String getCurrentHostName() 
	{
		try {
			if (this.currentHostName == null) {
				if (FBAppSettings.ENABLE_PRPL)
					setCurrentHostName(PrPlConnectionManager.DEMO_BUTLERS); // demo machines
	            else
	            	setCurrentHostName(PrPlConnectionManager.PERSONAL_BUTLERS); // self-hosting
			}			
		}
		catch (Exception e) { }
		
		return this.currentHostName;
	}
	
	public void setCurrentHostName(String hostName) throws Exception {
		PrPlHostInfo hostInfo = PrPlHosts.get(hostName);
		if (hostInfo == null)
			throw new Exception("Invalid PrPl Host! No information available about selected PrPl Hosting Service '"+hostName+"'!");
		this.pcbHost.setLaunchURL(hostInfo.launchPrPlButlerURL);
		this.pcbHost.setRegisterURL(hostInfo.registerPrPlButlerURL);
		this.currentHostName = hostName;
	}
	
	public boolean startButler()
	{
		System.out.println("startButler(): userID="+this.currentUserID+", userName="+this.currentUserName);
		if (currentHostName.equals("personal")) {
			this.setFinalStatusMessage("For a self-hosted Personal Cloud Butler, please manually start the PCB hosted at your machine.");
	        return false;
		}
		if (this.currentUserID == null || this.currentUserID.isEmpty())
		{
			// ERROR: PRINT OUT ERROR MESSAGE
			System.out.println("The PrPl UserID cannot be empty!");
			this.setFinalStatusMessage("No PrPl UserID is currently specified. "+
			                           "Please specify a valid PrPl UserID under the Configuration page.");
			return false;
	    }     
	    if (this.currentUserName == null || this.currentUserName.isEmpty()) {
	    	this.currentUserName = this.currentUserID;
	    }
	      
	    try {
	    	return this.startButler(this.currentUserID, this.currentUserPwd, this.currentUserName);
	    }
	    catch (PrPlHostException phe) {
	    	System.out.println("The following PrPlHost error occured: "+phe.getMessage());
	    	this.setFinalStatusMessage("The following PrPlHost error occured: "+phe.getMessage());
	    	return false;
	    }
	    catch (Exception e) {
	        System.out.println("An exception occured: "+e.getMessage());
	        this.setFinalStatusMessage("An exception occured: "+e.getMessage());
	        return false;
	    }
	}
	
	public boolean startButler(String prplUserID, String prplUserPwd, String prplUserName) throws PrPlHostException, Exception
	{
		PrPlHost.pcbStatus result = pcbHost.startPCB(prplUserID, prplUserPwd);
        if (result == PrPlHost.pcbStatus.CREATENEWPCB)
        {
            result = pcbHost.createPCB(prplUserName, prplUserID, prplUserPwd);
            result = pcbHost.startPCB(prplUserID, prplUserPwd); 
        }
        
        if (result == PrPlHost.pcbStatus.PCBRUNNING)
        {
      	  	// PRINT OUT SUCCESS MESSAGE
            System.out.println("PrPl PCB successfully running for "+prplUserName+" ("+prplUserID+")");
            this.setFinalStatusMessage("PrPl PCB successfully running for "+prplUserName+" ("+prplUserID+")");
            return true;
        }
        
        this.setFinalStatusMessage("An unknown error has occured while trying to start the PCB for "+prplUserName+" ("+prplUserID+")");
	    return false;
	}

	public boolean stopButler()
	{
		System.out.println("stopButler(): userID="+this.currentUserID+", userName="+this.currentUserName);
		if (currentHostName.equals("personal")) {
			this.setFinalStatusMessage("For a self-hosted Personal Cloud Butler, please manually stop the PCB hosted at your machine.");
	        return false;
		}
		if (this.currentUserID == null || this.currentUserID.isEmpty())
		{
			// ERROR: PRINT OUT ERROR MESSAGE
			System.out.println("The PrPl UserID cannot be empty!");
			this.setFinalStatusMessage("No PrPl UserID is currently specified. "+
            						   "Please specify a valid PrPl UserID under the Configuration page.");
			return false;
	    }     
	    if (this.currentUserName == null || this.currentUserName.isEmpty()) {
	    	this.currentUserName = this.currentUserID;
	    }
		
		try {
			return this.stopButler(this.currentUserID, this.currentUserPwd, this.currentUserName);
		}
	    catch (PrPlHostException phe) {
	    	System.out.println("The following PrPlHost error occured: "+phe.getMessage());
	    	this.setFinalStatusMessage("The following PrPlHost error occured: "+phe.getMessage());
	    	return false;
	    }
	    catch (Exception e) {
	        System.out.println("An exception occured: "+e.getMessage());
	        this.setFinalStatusMessage("An exception occured: "+e.getMessage());
	        return false;
	    }
	}
	
	public boolean stopButler(String prplUserID, String prplUserPwd, String prplUserName) throws PrPlHostException, Exception
	{
		PrPlHost.pcbStatus result = pcbHost.stopPCB(prplUserID, prplUserPwd);
        if (result == PrPlHost.pcbStatus.PCBSTOPPED) {
            System.out.println("PrPl PCB successfully stopped for "+prplUserName+" ("+prplUserID+")");
            this.setFinalStatusMessage("PrPl PCB successfully stopped for "+prplUserName+" ("+prplUserID+")");
            
            // TO-DO: BUG - Causes "org.apache.xmlrpc.XmlRpcException: Not authorized"
    		// this.prplService_.close();
    		this.prplService_ = null;
            return true;
        }
        else {
      	  System.out.println("Error: Could not stop PrPl PCB");
      	  this.setFinalStatusMessage("Error: Could not stop PrPl PCB");
      	  return false;
        }
	}	

	public boolean deleteButler()
	{
		System.out.println("deleteButler(): userID="+this.currentUserID+", userName="+this.currentUserName);
		if (currentHostName.equals("personal")) {
			this.setFinalStatusMessage("For a self-hosted Personal Cloud Butler, please manually stop and delete the PCB hosted at your machine.");
	        return false;
		}
		if (this.currentUserID == null || this.currentUserID.isEmpty())
		{
			// ERROR: PRINT OUT ERROR MESSAGE
			System.out.println("The PrPl UserID cannot be empty!");
			this.setFinalStatusMessage("No PrPl UserID is currently specified. "+
			  						   "Please specify a valid PrPl UserID under the Configuration page.");
			return false;
	    }
	    if (this.currentUserName == null || this.currentUserName.isEmpty()) {
	    	this.currentUserName = this.currentUserID;
	    }
	    
	    
	    try {
	    	return this.deleteButler(this.currentUserID, this.currentUserPwd, this.currentUserName);
		}
	    catch (PrPlHostException phe) {
	    	System.out.println("The following PrPlHost error occured: "+phe.getMessage());
	    	this.setFinalStatusMessage("The following PrPlHost error occured: "+phe.getMessage());
	    	return false;
	    }
	    catch (Exception e) {
	        System.out.println("An exception occured: "+e.getMessage());
	        this.setFinalStatusMessage("An exception occured: "+e.getMessage());
	        return false;
	    }
	}
	
	
	public boolean deleteButler(String prplUserID, String prplUserPwd, String prplUserName) throws PrPlHostException, Exception
	{
  	  	// First, Stop a user's running PCB for deletion
        PrPlHost.pcbStatus result = pcbHost.stopPCB(prplUserID, prplUserPwd);
        if (result == PrPlHost.pcbStatus.PCBSTOPPED)
        {
            System.out.println("Successfully stopped PrPl PCB of "+prplUserName+" ("+prplUserID+") for deletion...");
            // TO-DO: BUG - Causes "org.apache.xmlrpc.XmlRpcException: Not authorized"
    		// this.prplService_.close();
    		this.prplService_ = null;
        }
        else {
            System.out.println("Error: Could not stop a running PrPl PCB in preparation for PCB deletion.");
            this.setFinalStatusMessage("Error: Could not stop a running PrPl PCB in preparation for PCB deletion. "+
            						   "Please contact the SocialFlows app developers about this problem.");
            return false;
        }
        
        // Give 2 second delay for the PCB to be completely stopped
        Thread.sleep(2000);
        
        // Then, delete the stopped Butler
        result = pcbHost.deletePCB(prplUserName, prplUserID, prplUserPwd);
        if (result == PrPlHost.pcbStatus.PCBDELETESUCCESS)
        {
             // PRINT OUT SUCCESS MESSAGE
            System.out.println("Deleted PrPl PCB for "+prplUserName+" ("+prplUserID+")");
            this.setFinalStatusMessage("Deleted PrPl PCB for "+prplUserName+" ("+prplUserID+")");
            return true;
        }

        this.setFinalStatusMessage("Error: Could not delete a PrPl PCB. "+
        		                   "Please contact the SocialFlows app developers about this problem.");
	    return false;
	}	
	
	public PRPLAppClient switchButler(String hostName, String prplUserID, String prplUserPwd)
		throws PrPlHostException, Exception
	{
		if (hostName.equals(this.currentHostName) && prplUserID.equals(this.currentUserID)) {
			if (this.prplService_ != null) {
				this.setFinalStatusMessage("Have already started PrPl Butler with id <b>"+prplUserID+"</b>.");
				return this.prplService_;
			}
		}
		
		if (prplUserID == null || prplUserID.isEmpty())
		{
			// ERROR: PRINT OUT ERROR MESSAGE
			System.out.println("The PrPl UserID cannot be empty!");
			this.setFinalStatusMessage("No PrPl UserID is currently specified. "+
			  						   "Please specify a valid PrPl UserID.");
			return null;
	    }

		// Close current connection to previous PCB if possible
		if (!currentHostName.equals("personal")) {
			this.setStatusMessage("Stopping the previous PrPl Butler with id <b>"+this.currentUserID+"</b>...");
			
			try {
				if (this.stopButler(this.currentUserID, this.currentUserPwd, this.currentUserName)) {
					System.out.println("PrPl PCB successfully stopped for "+this.currentUserName+" ("+this.currentUserID+")");
		        	this.setStatusMessage("Successfully stopped the previous PrPl Butler with id <b>"+this.currentUserID+"</b>.");
				}
				else {
					System.out.println("Error: Could not stop previous PrPl Butler with id '"+this.currentUserID+"'");
		        	this.setStatusMessage("Could not stop previous PrPl Butler with id <b>"+this.currentUserID+"</b>. ");
				}
			}
			catch (PrPlHostException phe) {
				System.out.println("The following PrPlHost error occured: "+phe.getMessage());
				// out.println("The following PrPlHost error occured: "+phe.getMessage());
		    }
		}
		
		// TO-DO: BUG - Causes "org.apache.xmlrpc.XmlRpcException: Not authorized"
		// this.prplService_.close();
		this.prplService_ = null;
		// out.println("PrPl PCB successfully stopped for "+prplUserName+" ("+prplUserID+")");

		// Get HTML template code from HTML file residing within same java class package
        //InputStream is = getClass().getResourceAsStream(filename);

		this.setCurrentHostName(hostName);
		this.setCurrentUserID(prplUserID);
		this.currentUserPwd  = prplUserPwd;
		
		return getButlerClient();
	}
	
	public PRPLAppClient getButlerClient() throws PrPlHostException, Exception
	{
		// TO-DO: Renew PrPl session if expired
	    // this.prplService_.renewSession();
		
		if (this.prplService_ != null) {
			return this.prplService_;
		}
		
		if (this.currentUserID == null || this.currentUserID.isEmpty()) {
			System.out.println("The PrPl UserID cannot be empty!");
			this.setFinalStatusMessage("No PrPl UserID is currently specified. "+
			  						   "Please specify a valid PrPl UserID under the Configuration page.");
			return null;
	    }
		
		// Starts PCB if possible
		if (!currentHostName.equals("personal")) {
			this.setStatusMessage("Starting the PrPl Butler with id <b>"+this.currentUserID+"</b>...");
			if (!this.startButler(this.currentUserID, this.currentUserPwd, this.currentUserName))
				return null;
		}
		
		
		System.out.println("Obtaining PrPl Client...");
		this.setStatusMessage("Obtaining a connection to your Personal Cloud Butler with id <b>"+this.currentUserID+"</b>...");
		
		// Create PrPlAppClient to save data into PRPL
		try
		{
	  	   PrPlSettingManager.setPropFilePath(this.prplPropFilePath);
	       PrPlSettingManager.setKeystoreFilePath(this.prplKeysFilePath);
	       HashMap<String,String> prplSettings = PrPlSettingManager.loadProperties(this.currentUserName, this.currentUserID, this.currentUserPwd);
	       this.prplService_ = PRPLAppClientImpl.newInstance();
	    }
		catch (Exception e) {
			System.out.println("Error: Trouble establishing a connection to your Personal Cloud Butler.");
			System.out.println(e.getMessage());
            e.printStackTrace();
            
            Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);

            this.setFinalStatusMessage("Error: Trouble establishing a connection to your Personal Cloud Butler. "+
            						   "Please check that your Butler is running and please try again later."
            							+"<br/>"+e.getMessage()+"<br/>"+result.toString());
            return null;
		}
		
        System.out.println("PrPl PCB successfully running for "+this.currentUserName+" ("+this.currentUserID+")");
        
        this.setFinalStatusMessage("Successfully started PrPl Butler with id <b>"+this.currentUserID+"</b>. "+
      							   "Please reharvest your Facebook and social groups data.");
		return this.prplService_;
	}
	
	
	public String statusMessage = "";
	public String finalStatusMessage = ""; /* a hack to get the final status message result */
	
	public void setStatusMessage(String msg) /* synchronized */
	{
	    this.statusMessage = msg;	
	}
	
	public String getStatusMessage()
	{
		return new String(this.statusMessage);
	}
	
	public void setFinalStatusMessage(String msg) /* synchronized */
	{
		this.statusMessage = "";
	    this.finalStatusMessage = msg;	
	}
	
	public String getFinalStatusMessage()
	{
		return new String(this.finalStatusMessage);
	}	
	
	
	
	// DEFAULT CONFIGS
	public static final String PERSONAL_BUTLERS = "personal";
	public static final String DEMO_BUTLERS = "colby";
	
	// PRPL DEMO BUTLERS
	public static final String COLBY_REGISTER_PRPL_BUTLER_URL = "http://colby.stanford.edu:8080/prplHost/register.jsp";
	public static final String COLBY_LAUNCH_PRPL_BUTLER_URL   = "http://colby.stanford.edu:8080/prplHost/launch.jsp";
	
	// PRPL DEVELOPMENT BUTLERS
	public static final String BERT_REGISTER_PRPL_BUTLER_URL = "http://bert.stanford.edu:8080/prplHost/register.jsp";
	public static final String BERT_LAUNCH_PRPL_BUTLER_URL   = "http://bert.stanford.edu:8080/prplHost/launch.jsp";
	
	// PRPL ADDITIONAL BUTLERS
	public static final String ROMANO_REGISTER_PRPL_BUTLER_URL = "http://romano.stanford.edu:8080/prplHost/register.jsp";
	public static final String ROMANO_LAUNCH_PRPL_BUTLER_URL   = "http://romano.stanford.edu:8080/prplHost/launch.jsp";
	
		
	public static class PrPlHostInfo {
		public String name = null;
		public String displayText = null;
		public String registerPrPlButlerURL = null;
		public String launchPrPlButlerURL = null;
		
		public PrPlHostInfo(String hostName, String displayText, String registerURL, String launchURL)
		{
			this.name = hostName;
			this.displayText = displayText;
			this.registerPrPlButlerURL = registerURL;
			this.launchPrPlButlerURL   = launchURL;
		}
	}
	
	private static HashMap<String, PrPlHostInfo> PrPlHosts = new HashMap<String, PrPlHostInfo>();
	
	static {
		PrPlHosts.put(PrPlConnectionManager.PERSONAL_BUTLERS,
					  new PrPlHostInfo(PrPlConnectionManager.PERSONAL_BUTLERS, "Personal Hosting",
				                       null, null));
		PrPlHosts.put("bert",
					  new PrPlHostInfo("bert", "bert.stanford.edu (Dev machines)",
									   BERT_REGISTER_PRPL_BUTLER_URL, BERT_LAUNCH_PRPL_BUTLER_URL));
		PrPlHosts.put("colby", 
				      new PrPlHostInfo("colby", "colby.stanford.edu (Demo machines)",
				    		  		   COLBY_REGISTER_PRPL_BUTLER_URL, COLBY_LAUNCH_PRPL_BUTLER_URL));
		PrPlHosts.put("romano", 
			      	  new PrPlHostInfo("romano", "romano.stanford.edu",
			    		  		       ROMANO_REGISTER_PRPL_BUTLER_URL, ROMANO_LAUNCH_PRPL_BUTLER_URL));
	}

	public static HashMap<String, PrPlHostInfo> getPrPlHosts() {
		return PrPlHosts;
	}
	
}