<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="edu.stanford.prpl.insitu.*"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@page import="edu.stanford.socialflows.sticker.*"%>
<%@page import="edu.stanford.socialflows.log.*"%>
<%@include file="sessionCheck.jsp"%>
<%

      // Check if there are parameter inputs
      String sfTopologyId = null;
      sfTopologyId = request.getParameter("sfTopologyId");
      if (sfTopologyId == null) return;

      String rating = null;
      rating = request.getParameter("rating");
      String feedback = null;
      feedback = request.getParameter("feedback");

      System.out.println("SAVEFEEDBACK.JSP: sfTopologyId="+sfTopologyId+", rating="+rating+" feedback="+feedback);


      // Record feedback
      sfLogger.logFeedback(Long.parseLong(sfTopologyId), rating, feedback);
      
      
%>