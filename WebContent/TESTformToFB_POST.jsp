<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@include file="sessionCheck.jsp"%>
<%
	String post_form_id, fb_dtsg, lsd;
    
    JSONObject fbAuthObject
    = (JSONObject)session.getAttribute(FBAppSettings.FB_AUTH_OBJ);
    if (fbAuthObject == null) {
    	post_form_id = request.getParameter("post_form_id");
        fb_dtsg = request.getParameter("fb_dtsg");
        if (fb_dtsg == null)
            fb_dtsg = "";
        lsd = request.getParameter("lsd");
        if (lsd == null)
            lsd = "";
    }
    else {
    	post_form_id = fbAuthObject.optString(FBAppSettings.FB_POSTFORMID, "");
    	fb_dtsg = fbAuthObject.optString(FBAppSettings.FB_DTSG, "");
    	lsd = "";
    }
%>
<html>
<head>
<title>Test POST to FB</title>
<script type="text/javascript" src="jquery/jquery-1.5.1.min.js"></script>
<script type="text/javascript">
$(document).ready(function () {
    // Create or identify form
    var selectedForm = document.forms["testPOSTtoFB"];

    // Submit form automatically & programmatically using JS
    // to publish this social group as a FB friendlist
    selectedForm.submit();

});
</script>
</head>
<body>

<form id="testPOSTtoFB" method="post" action="http://www.facebook.com/ajax/groups/create_post.php?__a=1" >
<!--  ... name="reg" onsubmit="return false;">  -->

<input type="hidden" name="post_form_id" value="<%=post_form_id%>" />
<input type="hidden" name="fb_dtsg" value="<%=fb_dtsg%>" />
<input type="hidden" name="lsd" value="<%=lsd%>" />
<input type="hidden" name="post_form_id_source" value="AsyncRequest" />

<input type="hidden" name="name" value="SUPER TEST GROUP" />
<input type="hidden" name="create" value="Create" /> 
<input type="hidden" name="icon" value="0" />
<input type="hidden" name="privacy" value="closed" />
<input type="hidden" name="__d" value="1" />  

</form>
</body>
</html>