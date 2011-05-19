<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%

String userAgent = request.getHeader("user-agent");
System.out.println("\n\n\nUserAgent: "+userAgent);
System.out.println("Your IP address is: "+request.getRemoteAddr());

if (userAgent.indexOf("MSIE") > -1) {
   System.out.println("Your browser is Microsoft Internet Explorer<br/>");
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>My Title</title>
</head>
<body>
<% java.util.Date d = new java.util.Date(); %>
<h1>
Today's date is <%= d.toString() %> and this jsp page worked!
</h1>
<br>
<br>
<h2>Hi there! Hello World!</h2>
<br>
<%
    String input = request.getParameter("input");
    if (input != null && !input.isEmpty())
    	out.println("Optional input is <b>"+input+"</b>");

%>
</body>
</html>