<%@page language="java" import="java.io.*"%>
<%@page language="java" import="edu.stanford.socialflows.util.StatusProvider"%>
<%@page language="java" import="edu.stanford.socialflows.settings.FBAppSettings"%>
<%
	/* runs on server side, returns a status message or empty string */
    StatusProvider obj = (StatusProvider) session.getAttribute(FBAppSettings.STATUS_OBJ_NAME);
    if (obj != null) {
        out.println (obj.getStatusMessage());
    }
%>