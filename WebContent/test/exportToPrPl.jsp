<%@page import="java.io.*"%>
<%@page import="java.net.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<%
    String var = "test";
    Object test = null;
    
    /*
    // Get PrPlConnectionManager
    PrPlConnectionManager connManager 
    = (PrPlConnectionManager)session.getAttribute(FBAppSettings.PRPL_SERVICE_MANAGER);
    //session.setAttribute(FBAppSettings.STATUS_OBJ_NAME, connManager);
    
    // Get connection to PrPl
    PRPLAppClient prplService_ = null;
    try
    {
        // Starts a user's PCB if it has not already been started
        prplService_ = connManager.getButlerClient();
    }
    catch (PrPlHostException phe)
    {
        // ERROR: PRINT OUT ERROR MESSAGE
        System.out.println("The following PrPlHost error occured: "+phe.getMessage());
        phe.printStackTrace();
        session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
        %>
        <script type="text/javascript">
        displaySocialflowError("Error: Trouble establishing a connection to your Personal Cloud Butler. "+
                               "The following PrPlHost error occured: <%=phe.getMessage()%><br/>"+
                               "Please <a id=\"retry\" href=\"#\">retry</a>.");
        </script>
        <%
        return;
    }
    catch (Exception e)
    {
        // ERROR: PRINT OUT ERROR MESSAGE
        System.out.println("An exception occured: "+e.getMessage());
        e.printStackTrace();
        session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
        %>
        <script type="text/javascript">
        displaySocialflowError("Error: Trouble establishing a connection to your Personal Cloud Butler. "+
                               "The following error occured: <%=e.getMessage()%><br/>"+
                               "Please <a id=\"retry\" href=\"#\">retry</a>.");
        </script>
        <%
        return;
    }
    if (prplService_ == null) {
        String finalStatus = connManager.finalStatusMessage;
        session.removeAttribute(FBAppSettings.STATUS_OBJ_NAME);
        %>
        <script type="text/javascript">
        displaySocialflowError("Error: <%=finalStatus%>");
        </script>
        <%
        return;
    }
    
    // Get PrPl login credentials
    String prplUserID   = connManager.getCurrentUserID();
    String prplUserName = connManager.getCurrentUserName();
    */



%>
</body>
</html>