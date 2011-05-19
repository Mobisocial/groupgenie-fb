package edu.stanford.prpl.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.log4j.Logger;


public class SelfSignedSSLHostnameVerifier implements HostnameVerifier 
{
	     // Log4J
	     private static Logger logger = Logger.getLogger( SelfSignedSSLHostnameVerifier.class );

	     /**
	      * Verify that the host name is an acceptable match with the server's authentication scheme.
	      * @param hostname <code>String</code> the host name
	      * @param session <code>SSLSession</code> SSLSession used on the connection to host
	      */
	     public boolean verify( String hostname, SSLSession session ) 
	     {
	          if( !hostname.equals(session.getPeerHost()) ) 
	          {
	               logger.warn( "Hostname session peer: " + session.getPeerHost() + ", hostnamw: " + hostname );
	          }
	          return true;
	     }   
} 
	

