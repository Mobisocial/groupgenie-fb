<%@page language="java" import="java.io.*"%>
<%@page language="java" import="java.util.*"%>
<%@page language="java" import="edu.stanford.prpl.insitu.*"%>
<%@page language="java" import="edu.stanford.prpl.insitu.settings.*"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@include file="sessionCheck.jsp"%>
<%

    //String relativeLoc = "test/GROUPS_json.txt";
    String relativeLoc = "..."; // (String)session.getAttribute(FBAppSettings.JSON_INSITU_GROUPS_LOC);
    if (relativeLoc == null)
    {
        System.out.println("InSitu groups data must be generated first!");
        out.println("InSitu groups data must be generated first!");
        return;
    }

    String documentRootPath = application.getRealPath("/"+relativeLoc).toString();
    /*
    File f = new File(documentRootPath);
    if (f.exists())
    	System.out.println("File exists");
    else
    	System.out.println("File does not exist!");
    */

    // Construct URL parameter for Java applet
    String currentURL = request.getRequestURL().toString();
    String baseURL = currentURL.substring(0, currentURL.lastIndexOf("/"));
    String jsonURL = baseURL + "/"+relativeLoc;
    
    // System.out.println("JSON Url : "+jsonURL);
    /*
    System.out.println("getContextPath() : "+request.getContextPath());
    System.out.println("getQueryString() : "+request.getQueryString());
    System.out.println("getRequestURI()  : "+request.getRequestURI());
    System.out.println("getRequestURL()  : "+request.getRequestURL());
    System.out.println("getServletPath() : "+request.getServletPath());
    */
    
    // NOTES
    // 1. java.io.ObjectInputStream

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Cliques Map</title>
</head>
<body>
<APPLET CODE="prefuse.demos.applets.InSituGraphView.class" WIDTH="700" HEIGHT="480"
        ARCHIVE="viz/SignedInSituGraph.jar,viz/json-20070829.jar">
    <PARAM NAME="datasource"    VALUE="<%=jsonURL%>">
    <PARAM NAME="size"    VALUE="8">
    <PARAM NAME="string"  VALUE="Test">
</APPLET>
</body>
</html>