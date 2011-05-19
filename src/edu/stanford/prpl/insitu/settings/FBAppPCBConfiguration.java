package edu.stanford.prpl.insitu.settings;

import edu.stanford.socialflows.util.StatusProvider;

public class FBAppPCBConfiguration implements StatusProvider {

	public String statusMessage = "Configuring Your Personal Cloud Butler...";	

	public void setStatusMessage(String msg) /* synchronized */
	{
	    this.statusMessage = msg;	
	}
	
	public String getStatusMessage()
	{
		return new String(this.statusMessage);
	}	

}
