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
<title>POST AutoLike SocialFlows to FB</title>
<script type="text/javascript" src="jquery/jquery-1.5.1.min.js"></script>
<script type="text/javascript">
$(document).ready(function () {
    // Create or identify form
    var selectedForm = document.forms["postAutoLikeSFToFB"];

    // Submit form automatically & programmatically using JS
    // to publish this social group as a FB friendlist
    selectedForm.submit();

});
</script>
</head>
<body>

<form id="postAutoLikeSFToFB" method="post" action="http://www.facebook.com/ajax/pages/fan_status.php?__a=1" >
<!--  ... name="reg" onsubmit="return false;">  -->

<input type="hidden" name="fbpage_id" value="114642771882062" />
<input type="hidden" name="add" value="1" />
<input type="hidden" name="reload" value="1" />
<input type="hidden" name="preserve_tab" value="1" />
<input type="hidden" name="use_primer" value="1" />
<input type="hidden" name="nctr[_mod]" value="pagelet_top_bar" />

<input type="hidden" name="post_form_id" value="<%=post_form_id%>" />
<input type="hidden" name="fb_dtsg" value="<%=fb_dtsg%>" />
<input type="hidden" name="lsd" value="<%=lsd%>" />
<input type="hidden" name="post_form_id_source" value="AsyncRequest" />
</form>
</body>
</html>