<html>
<head>
<title>Test POST to FB</title>
</head>
<body>
Test if can create friends list in FB...
<form method="post" action="http://www.facebook.com/friends/ajax/superfriends_add.php?__a=1" >
<!-- <form method="post" id="postToSocialFlows" name="reg" onsubmit="return false;">  -->

<input type="hidden" name="action" value="create" />
<input type="hidden" name="members[0]" value="562068812" />
<input type="hidden" name="members[1]" value="222915" />
<input type="hidden" name="members[2]" value="24537" />
<input type="hidden" name="list_id" value="609564760228" />
<input type="hidden" name="name" value="SOCIALFLOWS" />

<input type="hidden" name="redirect" value="false" />
<input type="hidden" name="post_form_id" value="59dd7d7ed0193f29a8d8174ffe92f08d" />
<input type="hidden" name="fb_dtsg" value="5CMf7" />
<input type="hidden" name="lsd" value="08hNK" />
<input type="hidden" name="post_form_id_source" value="AsyncRequest" />

<input type="submit" value="Submit" />
</form>

<%@page import="edu.stanford.socialflows.settings.*"%>
<%
    // Construct integration URL
    String integrationUrl = "";
    
    // Get the base URL for SocialFlows webapp
    String originalURL = request.getRequestURL().toString();
    originalURL = originalURL.substring(0, originalURL.lastIndexOf("/")+1);

    if (originalURL.charAt(originalURL.length()-1) != '/')
        originalURL += "/";
    integrationUrl = originalURL + "processFBAuth.jsp";
    
    String js_url = originalURL + "export/exportGroupsToFB.js";
%>
<br/>
<br/>
<div align="center">
<a href="javascript:
         var sfAppUrl       = '<%=FBAppSettings.canvasURL%>';
         var integrationUrl = '<%=integrationUrl%>';
         (function()
          {  mc_script=document.createElement('SCRIPT'); 
             mc_script.type='text/javascript'; 
             mc_script.src='<%=js_url%>?'; 
             document.getElementsByTagName('head')[0].appendChild(mc_script);
          })();">Export Groups to Facebook</a>
</div>

</body>
</html>