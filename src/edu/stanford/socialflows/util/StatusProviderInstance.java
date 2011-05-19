package edu.stanford.socialflows.util;


public class StatusProviderInstance implements StatusProvider
{
	public String statusMessage = "";
	
	public StatusProviderInstance(String statusMsg)
	{
		this.statusMessage = statusMsg;
	}
	
	public void setStatusMessage(String msg) /* synchronized */
	{
	    this.statusMessage = msg;	
	}
	
	public String getStatusMessage()
	{
		return new String(this.statusMessage);
	}
}
