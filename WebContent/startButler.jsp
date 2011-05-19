<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="edu.stanford.prpl.insitu.*"%>
<%@page import="edu.stanford.socialflows.settings.FBAppSettings"%>
<%@page import="edu.stanford.prpl.insitu.util.PrPlConnectionManager"%>
<%@include file="sessionGetButler.jsp"%>
<%
	prplConnManager.startButler();
      session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
      
      String finalStatus = prplConnManager.finalStatusMessage;
      out.println(finalStatus);
%>