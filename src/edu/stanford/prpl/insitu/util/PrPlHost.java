package edu.stanford.prpl.insitu.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.host.Event;

import edu.stanford.socialflows.settings.FBAppSettings;

public class PrPlHost 
{
	WebClient webClient = new WebClient();
	
	private String registerPCB_URL = null;
    private String launchPCB_URL   = null;
    
    public enum pcbStatus 
    { CREATENEWPCB, PCBRUNNING, PCBCREATEFAIL, PCBCREATESUCCESS, PCBSTOPPED, PCBDELETESUCCESS, PCBDELETEFAIL }
    
    public PrPlHost(String registerURL, String launchURL)
    {
    	if (registerURL != null)
    		this.registerPCB_URL = registerURL;
    	if (launchURL != null)
    		this.launchPCB_URL = launchURL;
    }

	
    public void setRegisterURL(String registerURL)
    {
    	if (registerURL != null)
    		this.registerPCB_URL = registerURL;
    }
    
    public void setLaunchURL(String launchURL)
    {
    	if (launchURL != null)
    		this.launchPCB_URL = launchURL;
    }

    public pcbStatus startPCB(String userKey, String userPwd) throws PrPlHostException, FailingHttpStatusCodeException, IOException
    {
    	HtmlPage launchPCB_webpage = webClient.getPage(this.launchPCB_URL);
        
        // Get the form that we are dealing with and within that form, 
        // find the submit button and the field that we want to change.
        HtmlForm form = (HtmlForm)launchPCB_webpage.getElementById("launch");

        HtmlButton startbutton = null;
        List <HtmlButton> buttons = form.getButtonsByName("submit");
        Iterator<HtmlButton> buttonIterator = buttons.iterator();
        while (buttonIterator.hasNext())
        {
      	  HtmlButton button = buttonIterator.next();
      	  if (button.getValueAttribute().equals("start"))
      		  startbutton = button;
        }
        if (startbutton == null)
        {
        	throw new PrPlHostException("Start button not found!");
        }
        
     	// Change the value of the 'PrPl ID' text field
        HtmlTextInput textField = form.getInputByName("userId");
        textField.setValueAttribute(userKey);
        
        // Change the value of the 'Password' field
        HtmlPasswordInput pwdField = (HtmlPasswordInput)launchPCB_webpage.getElementById("launch_");
        if (userPwd != null && userPwd.trim().length() >= 1) {
        	pwdField.type(userPwd);
        	// pwdField.setValueAttribute(userPwd);
            // pwdField.setTextContent(userPwd);
            // pwdField.fireEvent(Event.TYPE_KEY_UP);
        }

        // Now submit the form by clicking the button and get back the second page.
        HtmlPage launchPCB_result_webpage = startbutton.click();
        boolean createPCB = false;
        String msg = launchPCB_result_webpage.asText();
        if (msg == null) {
        	throw new PrPlHostException("Result Message is null!");
        }
     	else if (msg.contains("PrPl Id is not registered or not found")) {
     		return pcbStatus.CREATENEWPCB;
        }
        else if (msg.contains("PRPL PCB already running")) {
        	return pcbStatus.PCBRUNNING;
        }
        else if (msg.contains("is running") || msg.contains("started")) {
        	return pcbStatus.PCBRUNNING; 
        }
        
        throw new PrPlHostException("Unknown message: "+msg);
    }
    
    public pcbStatus stopPCB(String userKey, String userPwd) throws PrPlHostException, FailingHttpStatusCodeException, IOException
    {
    	HtmlPage launchPCB_webpage = webClient.getPage(this.launchPCB_URL);
        
        // Get the form that we are dealing with and within that form, 
        // find the submit button and the field that we want to change.
        HtmlForm form = (HtmlForm)launchPCB_webpage.getElementById("launch");

        HtmlButton stopbutton = null;
        List <HtmlButton> buttons = form.getButtonsByName("submit");
        Iterator<HtmlButton> buttonIterator = buttons.iterator();
        while (buttonIterator.hasNext())
        {
      	  HtmlButton button = buttonIterator.next();
      	  if (button.getValueAttribute().equals("stop"))
      		  stopbutton = button;
        }
        if (stopbutton == null)
        {
        	throw new PrPlHostException("Stop button not found!");
        }
        
     	// Change the value of the text field
        HtmlTextInput textField = form.getInputByName("userId");
        textField.setValueAttribute(userKey);
        
        // Change the value of the 'Password' field
        HtmlPasswordInput pwdField = (HtmlPasswordInput)launchPCB_webpage.getElementById("launch_");
        if (userPwd != null && userPwd.trim().length() >= 1) {
        	pwdField.type(userPwd);
        	// pwdField.setValueAttribute(userPwd);
            // pwdField.setTextContent(userPwd);
            // pwdField.fireEvent(Event.TYPE_KEY_UP);
        }

        // Now submit the form by clicking the button and get back the second page.
        HtmlPage stopPCB_result_webpage = stopbutton.click();
        String msg = stopPCB_result_webpage.asText();
        if (msg == null) {
        	throw new PrPlHostException("Result Message is null!");
        }
        else if (msg.contains("PRPL not running")) {
        	return pcbStatus.PCBSTOPPED;
        }
        else if (msg.contains("Stopped PCB")) {
        	return pcbStatus.PCBSTOPPED; 
        }
        
        throw new PrPlHostException("Unknown message: "+msg);
    }   


    public pcbStatus createPCB(String userName, String userKey, String userPwd) throws PrPlHostException, FailingHttpStatusCodeException, IOException
    {
    	// Fill in PCB registration form
        HtmlPage createPCB_webpage = webClient.getPage(registerPCB_URL);
        HtmlForm pcbregister_form = (HtmlForm)createPCB_webpage.getElementById("register");
    	
        HtmlButton registerbutton = null;
        List <HtmlButton> buttons = pcbregister_form.getButtonsByName("submit");
        Iterator<HtmlButton> buttonIterator = buttons.iterator();
        while (buttonIterator.hasNext())
        {
      	  HtmlButton button = buttonIterator.next();
      	  if (button.getValueAttribute().equals("register"))
      		  registerbutton = button;
        }
        if (registerbutton == null)
        {
        	throw new PrPlHostException("Register button not found!");
        }
    	
        
     	// Change the value of the text field
        HtmlTextInput userId_textField = pcbregister_form.getInputByName("userId");
        HtmlTextInput fullName_textField = pcbregister_form.getInputByName("userName");
        HtmlPasswordInput pwd_textField = (HtmlPasswordInput)createPCB_webpage.getElementById("register_");
        
        userId_textField.setValueAttribute(userKey);
        if (userName != null && userName.length() >= 1)
           fullName_textField.setValueAttribute(userName);
        else
           throw new PrPlHostException("Name of user cannot be empty!");
        if (userPwd != null && userPwd.trim().length() >= 1)
           pwd_textField.type(userPwd);

        // Now submit the form by clicking the button and get back the second page.
        HtmlPage createPCB_result_webpage = registerbutton.click();
        String msg = createPCB_result_webpage.asText();
        if (msg == null) {
        	throw new PrPlHostException("Result Message is null!");
        }
     	else if (msg.contains("Name was not entered")) {
     		throw new PrPlHostException("Name of user cannot be empty!");
        }
        else if (msg.contains("Click here to launch your service")) {
        	return pcbStatus.PCBCREATESUCCESS;
        }
        else if (msg.contains("Id is already registered")) {
        	return pcbStatus.PCBCREATESUCCESS;
        }
        
        throw new PrPlHostException("Error: "+msg);
    }


    public pcbStatus deletePCB(String userName, String userKey, String userPwd) throws PrPlHostException, FailingHttpStatusCodeException, IOException
    {
    	
    	// Fill in PCB registration form
        HtmlPage createPCB_webpage = webClient.getPage(registerPCB_URL);
        HtmlForm pcbregister_form = (HtmlForm)createPCB_webpage.getElementById("register");
    	
        HtmlButton unregisterbutton = null;
        List <HtmlButton> buttons = pcbregister_form.getButtonsByName("submit");
        Iterator<HtmlButton> buttonIterator = buttons.iterator();
        while (buttonIterator.hasNext())
        {
      	  HtmlButton button = buttonIterator.next();
      	  if (button.getValueAttribute().equals("unregister"))
      		  unregisterbutton = button;
        }
        if (unregisterbutton == null)
        {
        	throw new PrPlHostException("Unregister button not found!");
        }
    	
        
     	  // Change the value of the text field
        HtmlTextInput userId_textField = pcbregister_form.getInputByName("userId");
        HtmlTextInput fullName_textField = pcbregister_form.getInputByName("userName");
        HtmlPasswordInput pwd_textField = (HtmlPasswordInput)createPCB_webpage.getElementById("register_");
        
        userId_textField.setValueAttribute(userKey);
        if (userName != null && userName.length() >= 1)
           fullName_textField.setValueAttribute(userName);
        else
           throw new PrPlHostException("Name of user cannot be empty!");
        if (userPwd != null && userPwd.trim().length() >= 1)
           pwd_textField.type(userPwd);

        // Now submit the form by clicking the button and get back the second page.
        HtmlPage deletePCB_result_webpage = unregisterbutton.click();
        String msg = deletePCB_result_webpage.asText();
        if (msg == null) {
        	throw new PrPlHostException("Result Message is null!");
        }
     	else if (msg.contains("Name was not entered")) {
     		throw new PrPlHostException("Name of user cannot be empty!");
        }
     	else if (msg.contains("Id is not registered")) {
     		throw new PrPlHostException("Id is not registered/found!");
        }
        else if (msg.contains("Successfully removed PrPl")) {
        	return pcbStatus.PCBDELETESUCCESS;
        }
        
        throw new PrPlHostException("Unknown message: "+msg);
    }    
    
    
}
