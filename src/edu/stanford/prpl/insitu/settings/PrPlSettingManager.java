package edu.stanford.prpl.insitu.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.stanford.prpl.impl.PRPLProperties;

public class PrPlSettingManager {
	
	private static final Logger log = Logger.getLogger(PrPlSettingManager.class);
	
	public static String propPath = "/prpl/prpl.properties";
	public static String keystorePath = "/prpl/keys/prpl_app.keystore";
    public static HashMap<String, String> setting = new HashMap<String, String>();
    
    static{
            setting.put("prpl.directory.url", "http://paneer.stanford.edu:2002/prpl-directory/");
            setting.put("prpl.device.name", "g1");
            setting.put("prpl.device.net.interface.name", "lo");
            setting.put("prpl.device.server.type", "APP");      
            setting.put("prpl.security.keystore.file", keystorePath);
            setting.put("prpl.security.keystore.password", "OBF:1y0s1w9b1wg81ugo1uh21wfq1w8f1y0y");
    }
    
    public static HashMap<String,String> loadProperties(String userName, String userKey, String userPwd) {
    	
    		System.out.println("PrPl Properties file is located at: "+PrPlSettingManager.propPath);
    	
            File propFile = new File(PrPlSettingManager.propPath);
            
            setting.put("prpl.user.name", userName);
            setting.put("prpl.user.key", userKey);
            
            if(userPwd == null || userPwd.equals(""))
            	setting.put("prpl.user.password", null);
            else
            	setting.put("prpl.user.password", PasswordHash.SHA1(userPwd) );
            
            if (! propFile.exists()) {
                    File parent  = propFile.getParentFile(); 
                    if (parent != null && !parent.exists()) {
                            parent.mkdirs();
                    }
                    try {
                            propFile.createNewFile();
                    } catch (IOException e) {
                            log.error("PRPL: Cannot create property file");
                            e.printStackTrace();
                            return null;
                    }
            }
            
            try {
                    PRPLProperties.loadProperties(PrPlSettingManager.propPath);
            } catch (IOException e) {
                    e.printStackTrace();
                    log.error("PRPL: PRPL Properties failed to load.");
                    return null;
            }               
            
            HashMap<String,String>results = new HashMap<String,String>();
            
            for (String key: PrPlSettingManager.setting.keySet()) {
                    String val = PrPlSettingManager.setting.get(key);
                    PRPLProperties.setProperty(key, val);
                    results.put(key,val);
            }

            try {
                    PRPLProperties.storeProperties(propFile);
            }
            catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
            }
            
            return results;
    }
	
    public static void setPropFilePath(String absolutePath)
    {
    	PrPlSettingManager.propPath = absolutePath;
    }
    
    public static void setKeystoreFilePath(String absolutePath)
    {
    	setting.put("prpl.security.keystore.file", absolutePath);
    }
    
    private static class PasswordHash
	{
	    private static String convertToHex(byte[] data)
	    {
	        StringBuffer buf = new StringBuffer();
	        for (int i = 0; i < data.length; i++)
	        {
	        	int halfbyte = (data[i] >>> 4) & 0x0F;
	        	int two_halfs = 0;
	        	do
	        	{
		            if ((0 <= halfbyte) && (halfbyte <= 9))
		                buf.append((char) ('0' + halfbyte));
		            else
		            	buf.append((char) ('a' + (halfbyte - 10)));
		            halfbyte = data[i] & 0x0F;
	        	} while(two_halfs++ < 1);
	        }
	        return buf.toString();
	    }
	 
	    private static String SHA1(String text)
	    {
	    	try
			{
	    		MessageDigest md;
				md = MessageDigest.getInstance("SHA-1");
				byte[] sha1hash = new byte[40];
				md.update(text.getBytes("iso-8859-1"), 0, text.length());
				sha1hash = md.digest();
				return convertToHex(sha1hash);
			}
	    	catch(Exception e)
	    	{
	    		return null;
	    	}
	    }
	}
}