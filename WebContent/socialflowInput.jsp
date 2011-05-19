<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@page import="edu.stanford.socialflows.algo.AlgoStats"%>
<%@page import="edu.stanford.prpl.insitu.*"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@page import="edu.stanford.socialflows.sticker.*"%>
<%@page import="edu.stanford.socialflows.log.*"%>
<%@include file="sessionCheck.jsp"%>
<%
	boolean handleBackButton = true;

      // Check if there are parameter inputs
      String MINCOUNT = request.getParameter("minCount");
      String MAX_ERROR = request.getParameter("maxError");
      String MIN_GROUP_SIZE = request.getParameter("minGroupSize");
      String MIN_MERGE_GROUP_SIM = request.getParameter("minMergeGroupSim");
      if (MINCOUNT != null || MAX_ERROR != null 
    	  || MIN_GROUP_SIZE != null || MIN_MERGE_GROUP_SIM != null)
    	  handleBackButton = false;

      if (MINCOUNT == null || MINCOUNT.trim().length() <= 0)
          MINCOUNT = String.valueOf(AlgoStats.DEFAULT_MIN_FREQUENCY);
      if (MAX_ERROR == null || MAX_ERROR.trim().length() <= 0)
          MAX_ERROR = String.valueOf(AlgoStats.DEFAULT_MAX_ERROR);
      if (MIN_GROUP_SIZE == null || MIN_GROUP_SIZE.trim().length() <= 0)
          MIN_GROUP_SIZE = String.valueOf(AlgoStats.DEFAULT_MIN_GROUP_SIZE);
      if (MIN_MERGE_GROUP_SIM == null || MIN_MERGE_GROUP_SIM.trim().length() <= 0)
          MIN_MERGE_GROUP_SIM = String.valueOf(AlgoStats.DEFAULT_MIN_GROUP_SIMILARITY);
      
      // Check if new algo
      String USENEWALGO = request.getParameter("useNewGroupsAlgo");
      String ERROR_WEIGHT = request.getParameter("errWeight");
      String NUM_GROUPS = request.getParameter("numGroups");
      if (USENEWALGO != null || ERROR_WEIGHT != null || NUM_GROUPS != null)
          handleBackButton = false;
      
      if (USENEWALGO == null || USENEWALGO.trim().length() <= 0)
    	  USENEWALGO = "false";
      if (ERROR_WEIGHT == null || ERROR_WEIGHT.trim().length() <= 0)
    	  ERROR_WEIGHT = String.valueOf(AlgoStats.DEFAULT_ERROR_WEIGHT);
      if (NUM_GROUPS == null || NUM_GROUPS.trim().length() <= 0)
    	  NUM_GROUPS = String.valueOf(AlgoStats.DEFAULT_NUM_GROUPS);
      
      
      // Check for suggested social cliques data (deduced from email) from POST request
      String suggEmailTopoJSON = request.getParameter("input");
      
      // Check for suggested social cliques data (deduced from co-tagged photos)
      JSONObject suggPhotosTopo 
      = (JSONObject)session.getAttribute(FBAppSettings.MY_FB_PHOTOTAG_TOPO);
      session.removeAttribute(FBAppSettings.MY_FB_PHOTOTAG_TOPO); // save memory
      
      if (suggEmailTopoJSON != null || suggPhotosTopo != null)
      {
    	  handleBackButton = false;
    	  if (suggEmailTopoJSON != null) {
    		  sfSessionObj.put(FBAppSettings.MY_SOCIALFLOW_JSON, suggEmailTopoJSON);
    		  
    		  System.out.println(request.getServletPath()+": Received a generated topology from EMAIL data");
    	  }
    	  if (suggPhotosTopo != null) {
    		  sfSessionObj.put(FBAppSettings.MY_FB_PHOTOTAG_TOPO, suggPhotosTopo);
    		  
    		  System.out.println(request.getServletPath()+": Received a generated topology from PHOTOS data");
    	  }
      }


      // Generate the social topology URL
      Date currentTime = new Date();
      String socialflowsURL = "socialflowProgress.jsp?rand="+currentTime.getTime();
      socialflowsURL = response.encodeURL(socialflowsURL);
      
      if (handleBackButton)
      {
%>
         <script type="text/javascript" src="common/jscommon.js"></script>
         <script type="text/javascript">
         var socialflowsIframe = parent.document.getElementById("SOCIALFLOW_IFRAME");
         socialflowsIframe.src = "<%=socialflowsURL%>";
         </script>
         <%
         	return;
               }

               // set to change to correct frame displaying SocialFlows
               // and reset Dunbar frame to algo params page
         %>
      <script type="text/javascript" src="common/jscommon.js"></script>
      <script type="text/javascript">
      
         // Need to refresh UI
         var refreshSocialFlow = parent.document.getElementById('refreshSocialFlow');
         var refreshStickers   = parent.document.getElementById('refreshStickers');
         refreshSocialFlow.value = "true";
         refreshStickers.value   = "true";

         <%if (suggEmailTopoJSON != null || suggPhotosTopo != null)
         {%>
             var currentFrameID = window.frameElement.id;
             // window.location.href = "..."; // bug: somehow does not update iframe url
             parent.changeInSituTabSelected("socialflow");
             parent.changeToWindow(3, '<%=socialflowsURL%>');
             <%if (suggEmailTopoJSON != null) {%>
             if (currentFrameID != "SOCIALFLOW_IFRAME") {
                 var dunbarIframe = parent.document.getElementById("INSITU_DUNBAR_IFRAME");
                 dunbarIframe.src = "blank.html";
             }
             <%}
         }
         else
         {
        	 if (USENEWALGO.equals("true"))
        	 {
        		 // Rerun algorithm using new parameters
                 String genGroupURL
                 = FBAppSettings.DUNBAR_GROUPGENERATOR_URL + "?"
                   + "errweight="+ERROR_WEIGHT
                   + "&numGroups="+NUM_GROUPS;
        		 
        		 System.out.println("RUNNING WITH NEW ALGO PARAMETERS: errorWeight="+ERROR_WEIGHT
        				            +", numGroups="+NUM_GROUPS);%>
                 parent.changeInSituTabSelected("socialflow"); 
                 parent.changeToWindow(3, "<%=genGroupURL%>");
                 <%}
        	 else
        	 {
        		 // Rerun algorithm using new parameters
                 String genGroupURL
                 = FBAppSettings.DUNBAR_GROUPGENERATOR_URL + "?"
                   + "useOldGroupsAlgo=true"
                   + "&minCount="+MINCOUNT+"&maxError="+MAX_ERROR
                   + "&minGroupSize="+MIN_GROUP_SIZE+"&minMergeGroupSim="+MIN_MERGE_GROUP_SIM
                   + "&utilityType=linear&multiplier=1.4&submit=submit";

                 /*
                 JSONObject algoParamValues = new JSONObject();
                 algoParamValues.put("minCount", MINCOUNT);
                 algoParamValues.put("maxError", MAX_ERROR);
                 algoParamValues.put("minGroupSize", MIN_GROUP_SIZE);
                 algoParamValues.put("minMergeGroupSim", MIN_MERGE_GROUP_SIM);
                 session.setAttribute(FBAppSettings.MY_SOCIALFLOW_PARAMS, algoParamValues);
                 */
                 System.out.println("RUNNING WITH NEW ALGO PARAMETERS: minCount="+MINCOUNT+"&maxError="+MAX_ERROR
                         + "&minGroupSize="+MIN_GROUP_SIZE+"&minMergeGroupSim="+MIN_MERGE_GROUP_SIM);%>
                 parent.changeInSituTabSelected("socialflow"); 
                 parent.changeToWindow(3, "<%=genGroupURL%>");
                 <%
        	 } 
         }
         
         %>
      </script>
      <%

%>