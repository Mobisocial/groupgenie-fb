<%@page import="org.json.*"%>
<%@page import="edu.stanford.socialflows.settings.*"%>
<%@page import="edu.stanford.socialflows.algo.AlgoStats"%>
<%@page import="edu.stanford.socialflows.sticker.Sticker"%>
<%
	// Gmail auth info
    JSONObject gmailAuthObject 
    = (JSONObject)session.getAttribute(FBAppSettings.GMAIL_AUTH_OBJ);
    String gmailToken = null;
    if (gmailAuthObject != null)
    	gmailToken = gmailAuthObject.optString(FBAppSettings.GMAIL_TOKEN, null);
    
    // FB auth info
    JSONObject fbAuthObject 
    = (JSONObject)session.getAttribute(FBAppSettings.FB_AUTH_OBJ);
    String fb_postformid = null, fb_dtsg = null;
    if (fbAuthObject != null) {
    	fb_postformid = fbAuthObject.optString(FBAppSettings.FB_POSTFORMID, null);
    	fb_dtsg       = fbAuthObject.optString(FBAppSettings.FB_DTSG, null);
    }


    JSONObject sessionObj = sfSessionObj;
    long sfTopologyId = sfLogger.getCurrentRunID();

    // Determine location of script to receive group algo input parameters
    String genGroupURL
    = FBAppSettings.DUNBAR_SOCIALFLOWS_POST_DESTINATION+"?";
    genGroupURL = response.encodeURL(genGroupURL);
    
    String analyzeFBPhotosURL 
    = response.encodeURL("analyzeFBPhotos.jsp");
    String getStatusURL 
    = response.encodeURL("getStatus.jsp");
    
    

    // Get params for algo run (for email)
    JSONObject emailTopo = suggEmailTopo;
    
    JSONObject emailAlgoParamValues = null, 
               emailAlgoParamValuesDefault = null,
               emailNewAlgoParamValues = null;
    boolean usingNewAlgo = false;
    if (emailTopo != null)
    {
        String algoType = emailTopo.optString("algoType", null);
        if (algoType != null && algoType.equals("newAlgo"))
        {
            usingNewAlgo = true;
            int numGroups = AlgoStats.DEFAULT_NUM_GROUPS;
            emailNewAlgoParamValues 
            = emailTopo.optJSONObject("algoParams");
            
            String str 
            = emailNewAlgoParamValues.optString("numGroups", 
                                                Integer.toString(numGroups));
            try { numGroups = Integer.parseInt(str); } 
            catch (NumberFormatException nfe) { }

            /*
            JSONArray filteredGroups = new JSONArray();
            for (int i = 0; i < suggested_groups.length() && i < numGroups; i++) {
                filteredGroups.put(suggested_groups.get(i));
            }
            emailTopo.putOpt("groups", filteredGroups);
            suggested_groups = filteredGroups;
            
            System.out.println("Filtering to "+numGroups+" groups...");
            */
        }
        else
        {
            emailAlgoParamValues 
            = emailTopo.optJSONObject("algoParams");
            emailAlgoParamValuesDefault 
            = emailTopo.optJSONObject("algoParamsDefault");
        }
    }
    

    // Get params for algo run (for photos)
    JSONObject photoTopo = suggPhotosTopo;
    
    JSONObject photoAlgoParamValues = null, 
               photoAlgoParamValuesDefault = null;
    if (photoTopo != null)
    {
    	photoAlgoParamValues 
    	= photoTopo.optJSONObject("algoParams");
        photoAlgoParamValuesDefault 
        = photoTopo.optJSONObject("algoParamsDefault");
    }
    
    
    
    // Remember whether it was photos or email that was last run
    if (emailAlgoParamValues != null) {
    	sessionObj.put("lastRunAlgoSourceType", Sticker.SOURCE_EMAIL);
        sessionObj.put("lastRunEmailAlgoParams", emailAlgoParamValues);
        
    	System.out.println("For topology run id "+sfTopologyId);
    	System.out.println("--> Done on EMAIL with values:\n"+emailAlgoParamValues.toString(2));
    }
    else {
    	emailAlgoParamValues 
    	= sessionObj.optJSONObject("lastRunEmailAlgoParams");

    	System.out.println("For topology run id "+sfTopologyId);
        System.out.println("--> Found previous values for email:\n"+emailAlgoParamValues);
    }
    
    if (emailNewAlgoParamValues != null) {
    	sessionObj.put("lastRunAlgoSourceType", Sticker.SOURCE_EMAIL+"NEWALGO");
        sessionObj.put("lastRunEmailNewAlgoParams", emailNewAlgoParamValues);
        
        System.out.println("For topology run id "+sfTopologyId);
        System.out.println("--> Done on EMAIL (NEW ALGO) with values:\n"+emailNewAlgoParamValues.toString(2));
    }
    else {
    	emailNewAlgoParamValues
        = sessionObj.optJSONObject("lastRunEmailNewAlgoParams");
        
        System.out.println("For topology run id "+sfTopologyId);
        System.out.println("--> Found previous values for email (NEW ALGO):\n"+emailNewAlgoParamValues);
    }

    if (photoAlgoParamValues != null) {
    	sessionObj.put("lastRunAlgoSourceType", Sticker.SOURCE_PHOTO);
    	sessionObj.put("lastRunPhotoAlgoParams", photoAlgoParamValues);
    	
    	System.out.println("For topology run id "+sfTopologyId);
        System.out.println("--> Done on PHOTO with values:\n"+photoAlgoParamValues.toString(2));
    }
    else {
    	photoAlgoParamValues
    	= sessionObj.optJSONObject("lastRunPhotoAlgoParams");
    	
    	System.out.println("For topology run id "+sfTopologyId);
        System.out.println("--> Found previous values for photos:\n"+photoAlgoParamValues);
    }
    
    
    // Show or hide certain development features
    String showDevFeatureCSS = "hidden";
    if (FBAppSettings.ENABLE_DEV_MODE)
    	showDevFeatureCSS = "";
%>

<!-- Control Panel -->
<!-- Feedback Panel CSS and Javascript -->
<script type="text/javascript">

    /* Control Panel methods */
    function showControlPanel()
    {
        var controlPanel = $('div#control-panel');

        // Reveal/Display control panel
        //controlPanel.show();
        controlPanel.animate({right:'-3px'}, 1500, function() {
            controlPanel.find('#toggleControlPanel').addClass('showControlPanel');
        });
        $('#toggleControlPanel-indicator').html('&#9658;'); //Shows Right-Arrow
    }
    
    function initControlPanel()
    {
        var controlPanel = $('div#control-panel');
        var controlPanelToggle = controlPanel.find('#toggleControlPanel');

        // Enable reveal/hide control panel
        controlPanelToggle.click(function() {
            if ($(this).hasClass('showControlPanel')) {
                $(this).removeClass('showControlPanel');
                controlPanel.animate({right:'-198px'}, 300);
                $('#toggleControlPanel-indicator').html('&#9668;'); //Shows Left-Arrow
            }
            else {
                $(this).addClass('showControlPanel');
                controlPanel.animate({right:'-3px'}, 300);
                $('#toggleControlPanel-indicator').html('&#9658;'); //Shows Right-Arrow
            }
            return false;
        });
        
        // "Close" control panel link
        controlPanel.find('#hideControlPanel').click(function() {
            controlPanelToggle.removeClass('showControlPanel');
            controlPanel.animate({right:'-198px'}, 300);
            controlPanelToggle.find('#toggleControlPanel-indicator').html('&#9668;'); //Shows Left-Arrow
            return false;
        });
        
                
        /*
        $('div#control-panel').tabSlideOut({
            tabHandle: '.handle',                     //class of the element that will become your tab
            pathToTabImage: null, //path to the image for the tab //Optionally can be set using css
            imageHeight: '30px',                     //height of tab image           //Optionally can be set using css
            imageWidth: '20px',                       //width of tab image            //Optionally can be set using css
            tabLocation: 'right',                      //side of screen where tab lives, top, right, bottom, or left
            speed: 300,                               //speed of animation
            action: 'click',                          //options: 'click' or 'hover', action to trigger animation
            topPos: '38px',                          //position from the top/ use if tabLocation is left or right
            leftPos: '20px',                          //position from left/ use if tabLocation is bottom or top
            fixedPosition: true,                      //options: true makes it stick(fixed position) on scroll
            toggleCallback: function(mode) {
                               if (mode == 'open')
                                  $('#toggleControlPanel-indicator').html('&#9658;'); //Shows Right-Arrow
                               else if (mode == 'close')
                                  $('#toggleControlPanel-indicator').html('&#9668;'); //Shows Left-Arrow
                            }
        });
        */
        
        // Initialize feedback/user-study panel
        initFeedbackPanel();

        // Initialize the multi-options panel
        initFacetedPanels();


        
        // Initialize the "Save My Groups" button
        $('#save_groups').click(function() {
        	if (!isEmailEnabled) {
                startFBExport();
        	}
        	else {
            	// Show "Save My Groups" panel if hidden, hide other panels
            	if (!$('a#save-groups-panel-button.accordionButton').hasClass('on'))
            	   $('a#save-groups-panel-button.accordionButton').click();
            	// Slide open control panel
            	showControlPanel();
        	}
            return false;
        });

        // Initialize the "Feedback" button
        $('#feedback-tab').click(function() {
        	// Show "Give Use Feedback" panel if hidden, hide other panels
        	if (!$('a#give-feedback-panel-button.accordionButton').hasClass('on'))
               $('a#give-feedback-panel-button.accordionButton').click();
            // Slide open control panel
            showControlPanel();
            return false;
        });
    }
    
    function initFeedbackPanel()
    {
        var feedbackPanel = $('#feedback-panel');
        if (feedbackPanel.length <= 0)
            return;

        // Slider for ratings
        var ratingLevels = feedbackPanel.find('select#ratingLevels');
        ratingLevels.selectToUISlider({
            labels: 5,
            tooltip: true, //show tooltips, boolean
            tooltipSrc: 'value',
            labelSrc: 'text',
            sliderOptions: null
        }).hide();


        // Submit feedback button
        feedbackPanel.find('#submitfeedback').click(function(){
            var sfTopologyId = feedbackPanel.find('#sfTopologyId').val();
            var rating = ratingLevels.find("option:selected");
            if (rating.length <= 0)
                return false;
            //var feedbackText = feedbackPanel.find('#feedback').text();
            var feedbackText = feedbackPanel.find('#feedback').attr('value');
            
            feedbackPanel.slideUp(1000, function(){
                $('#feedback-submitted-msg').slideDown('fast');
            });

            // POST feedback about current social topology run
            $.ajax({
                type:           'POST',
                cache:          false,
                url:            "saveFeedback.jsp",
                data:           {sfTopologyId: sfTopologyId, feedback: feedbackText, rating: rating.val() },  //{input: postdata},
                // processData:    false,
                dataType:       'text'
                // success:        function(data, textStatus) { processPOSTSuccess(data, klass, postdata, postcallback) },
                // error:          function(XMLHttpRequest, textStatus, errorThrown) { processPOSTError(textStatus, errorThrown, klass) }
            });
        });
    }
    
    function initMoreOptionsPanel()
    {
        $('#enableBrushAndLink').click(function() {
            if ($(this).attr('checked'))
                toggleBrushLink(true);
            else
                toggleBrushLink(false);
        });
    }

    function initFacetedPanels()
    {
        initMoreOptionsPanel();

        $('#parameters-panel .resetParams').click(function() {
            var currentPanel = $(this).closest('.accordionPanel');
            resetAlgoParams(currentPanel);
            return false;
        });
        
        // Rerun algo depending on dataset
        $('#parameters-panel .reanalyze').click(function() {
            var currentPanel = $(this).closest('.accordionPanel');
            runAlgo(currentPanel);
            return false;
        });
        
        // Nice bit of accordion code from 
        // "http://www.stemkoski.com/stupid-simple-jquery-accordion-menu/"
        // JQueryUI Accordion widget is too heavy/complicated, not lightweight enough
        $('a.accordionButton').click(function() {
            // Order of animations added to JQuery's event queue 
            // ensures accordion panel that is eventually revealed
            // will run after all previously-opened panels are closed
            $('a.accordionButton').removeClass('on');
            $('.accordionPanel').slideUp('slow');
            
            // Check if corresponding panel is currently hidden
            var correspondingPanel = $(this).parent().next();
            if(correspondingPanel.is(':hidden')) {
                // A currently-hidden element will not have a
                // slideUp() animation in its corresponding 'fx' queue
                $(this).addClass('on');
                correspondingPanel.slideDown('slow');
            }
            return false;
        });
        // $('.accordionPanel').hide(); 
    }
    
    function resetAlgoParams(currentPanel)
    {
        var algoParams = currentPanel.find('.algoParams');
        var panelName  = currentPanel.attr('id');
        
        if (panelName == 'parameters-email-newalgo-panel'
            || panelName == 'parameters-photos-panel') 
        {
            var errWeightDefault = algoParams.children('#errWeightDefault').val();
            var numGroupsDefault = algoParams.children('#numGroupsDefault').val();
            
            currentPanel.find('#errWeight').val(errWeightDefault);
            currentPanel.find('#numGroups').val(numGroupsDefault);
        }
        else
        {
            // Default values are always available within DOM metadata
            var minFreqDefault            = algoParams.children('#minCountDefault').val();
            var maxErrorDefault           = algoParams.children('#maxErrorDefault').val();
            var minGroupSizeDefault       = algoParams.children('#minGroupSizeDefault').val();
            var minGroupSimilarityDefault = algoParams.children('#minMergeGroupSimDefault').val();

            currentPanel.find('#minCount').val(minFreqDefault);
            currentPanel.find('#maxError').val(maxErrorDefault);
            currentPanel.find('#minGroupSize').val(minGroupSizeDefault);
            currentPanel.find('#minMergeGroupSim').val(minGroupSimilarityDefault);
        }
    }
    
    function runAlgo(currentPanel)
    {
        var algoParams = currentPanel.find('.algoParams');
        var panelName  = currentPanel.attr('id');
        
        var errWeight = null, numGroups  = null;
        if (panelName == 'parameters-email-newalgo-panel'
            || panelName == 'parameters-photos-panel') 
        {
            var errWeightDefault = algoParams.children('#errWeightDefault').val();
            var numGroupsDefault = algoParams.children('#numGroupsDefault').val();
            
            errWeight = currentPanel.find('#errWeight').val();
            if (errWeight == null || errWeight.trim().length <= 0)
                errWeight = errWeightDefault;
            numGroups = currentPanel.find('#numGroups').val();
            if (numGroups == null || numGroups.trim().length <= 0)
                numGroups = numGroupsDefault;
        }
        
        if (panelName == 'parameters-email-newalgo-panel')
        {
            /*
            var genGroupURL
            = $('#generateGroupURL').val()
              + "&useNewGroupsAlgo=true"
              + "&errWeight="+errWeight
              + "&numGroups="+numGroups;
            window.location.href = genGroupURL;
            */
            invokeMuse(numGroups, errWeight);
            
            return;
        }
        else if (panelName == 'parameters-photos-panel')
        {
            var dataToHarvest = new Object();
            //dataToHarvest.refresh = true;
            var algoParams = new Object();
            algoParams.useNewGroupsAlgo = true;
            algoParams.errWeight        = errWeight;
            algoParams.numGroups        = numGroups;
            dataToHarvest.algoParams = algoParams;

            // STRINGIFY!
            var dataToHarvestJSON = JSON.stringify(dataToHarvest, null, 1);
            
            // show harvesting photo albums dialog
            var loadingMsg = 'Deducing Your Social Groups from your Facebook Photos...';
            $.facebox.settings.loadingMessage = loadingMsg;

            // Using POST method to list of FB photo albums to harvest
            jQuery.facebox({ post: '<%=analyzeFBPhotosURL%>', progress: '<%=getStatusURL%>',
                             postdata: dataToHarvestJSON, 
                             postcallback: processAnalyzePhotosResult });
            return;
        }
        
        
        // OLD ALGO
        var minFreqDefault            = algoParams.children('#minCountDefault').val();
        var maxErrorDefault           = algoParams.children('#maxErrorDefault').val();
        var minGroupSizeDefault       = algoParams.children('#minGroupSizeDefault').val();
        var minGroupSimilarityDefault = algoParams.children('#minMergeGroupSimDefault').val();
        
        var minFreq = currentPanel.find('#minCount').val();
        if (minFreq == null || minFreq.trim().length <= 0)
            minFreq = minFreqDefault;
        var maxError = currentPanel.find('#maxError').val();
        if (maxError == null || maxError.trim().length <= 0)
            maxError = maxErrorDefault;
        var minGroupSize = currentPanel.find('#minGroupSize').val();
        if (minGroupSize == null || minGroupSize.trim().length <= 0)
            minGroupSize = minGroupSizeDefault;
        var minGroupSimilarity = currentPanel.find('#minMergeGroupSim').val();
        if (minGroupSimilarity == null || minGroupSimilarity.trim().length <= 0)
            minGroupSimilarity = minGroupSimilarityDefault;
        
        
        if (panelName == 'parameters-email-panel') 
        {
            //var genGroupURL
            //= dunbarGroupsGeneratorURL + "?"
            //  + "minCount="+minFreq+"&maxError="+maxError
            //  + "&minGroupSize="+minGroupSize+"&minMergeGroupSim="+minGroupSimilarity
            //  + "&utilityType=linear&multiplier=1.4&submit=submit";
            var genGroupURL
            = $('#generateGroupURL').val()
              + "&minCount="+minFreq+"&maxError="+maxError
              + "&minGroupSize="+minGroupSize+"&minMergeGroupSim="+minGroupSimilarity;

            window.location.href = genGroupURL;
        }
        else if (panelName == 'parameters-photos-oldalgo-panel')
        {
            var dataToHarvest = new Object();
            //dataToHarvest.refresh = true;
            var algoParams = new Object();
            algoParams.useNewGroupsAlgo = false;
            algoParams.minCount         = minFreq;
            algoParams.maxError         = maxError;
            algoParams.minGroupSize     = minGroupSize;
            algoParams.minMergeGroupSim = minGroupSimilarity;
            dataToHarvest.algoParams = algoParams;

            // STRINGIFY!
            var dataToHarvestJSON = JSON.stringify(dataToHarvest, null, 1);
            
            // show harvesting photo albums dialog
            var loadingMsg = 'Deducing Your Social Groups from your Facebook Photos...';
            $.facebox.settings.loadingMessage = loadingMsg;

            // Using POST method to list of FB photo albums to harvest
            jQuery.facebox({ post: '<%=analyzeFBPhotosURL%>', progress: '<%=getStatusURL%>',
                             postdata: dataToHarvestJSON, 
                             postcallback: processAnalyzePhotosResult });
        }
    }
    
    function processAnalyzePhotosResult(resultText, inputdata)
    {
        if (resultText == null)
           return "An error has occured. No results were returned.";
        
        var resultData = JSON.parse(resultText);
        if (!resultData.success)
           return resultData.message;

        // Hide progress dialog
        $.facebox.close();

        // Need to refresh UI
        var refreshSocialFlow = parent.document.getElementById('refreshSocialFlow');
        var refreshStickers   = parent.document.getElementById('refreshStickers');
        refreshSocialFlow.value = "true";
        refreshStickers.value   = "true";
        
        // Redirect to SocialFlows page
        parent.changeInSituTabSelected("socialflow");
        parent.changeToWindow(3, $('#generateGroupURL').val());
    }

</script>
<script type="text/javascript" src="slider/selectToUISlider.jQuery.js"></script>
<link rel="stylesheet" href="slider/redmond/jquery-ui-1.7.1.custom.css" type="text/css" />
<link rel="stylesheet" href="slider/ui.slider.extras.css" type="text/css" />
<style type="text/css">
.ui-slider {
    clear: both; 
    width:155px;
    margin-top: 8px;
    margin-left: auto;
    margin-right: auto;
}
.ui-slider li, .ui-slider dd {
    color:#555555;
    font-family:Arial,Helvetica,sans-serif;
    font-size:10px;
    text-align:center;
}
.ui-slider .ui-slider-handle {
    width:0.8em;
}
.ui-helper-reset {
    line-height:1.0;
}



/* Control Panel */
div#control-panel a {
    color:#555555;
}

div#control-panel a:hover {
    color:#057fac;
}

div#control-panel {
    padding: 8px;
    width: 180px;
    z-index: 280;
    background: none repeat scroll 0 0 rgba(217, 217, 217, 1); /* rgba(217, 217, 217, 0.6) #D9D9D9 #FFFFCC; */
    border: 1px solid #BBBBBB;
    line-height:1;
    position:fixed;
    right:-3px;
    top:38px;
}

div#control-panel a#toggleControlPanel {
    display: table;
    position: absolute;
    background:none repeat scroll 0 0 #FFFFFF;
    border:1px solid #BBBBBB;
    text-decoration:none;
    text-align: center;
    vertical-align: middle;
    height:30px;
    left:-22px;
    outline:medium none;
    top:0;
    width:20px;
}

div#toggleControlPanel-indicator {
    font-size: 110%;
    display: table-cell;
    text-align: center;
    vertical-align: middle;
    margin:0 auto;
}


div#control-panel .input-param-entry {
    margin-bottom: 10px;
}

div#control-panel a.resetParams {
    position: absolute; 
    left: 0px; 
    bottom: 3px; 
    text-decoration: underline;
}

div#control-panel a#hideControlPanel {
    position: absolute; 
    left: 8px; 
    top: 8px; 
    text-decoration: underline;
}

</style>

<%
   String panelToggleCSS = "display: none;";
   if (FBAppSettings.ENABLE_DEV_MODE)
	   panelToggleCSS = "display: block;";
%>
<div id="control-panel" style="display:block; z-index: 200; right:-198px;">
<a href="#" id="toggleControlPanel" class="handle" style="<%=panelToggleCSS%>"><div id="toggleControlPanel-indicator">&#9668;</div></a>
<a href="#" id="hideControlPanel">Close</a>
<div id=control-panel-spacer" style="height: 20px;"></div>

<%
	String controlPanelCSS    = "display: block;", 
           emailParamPanelCSS = "display: block;", 
           emailNewAlgoParamPanelCSS = "display: none;", 
           photoParamPanelCSS = "display: none;";

    String lastRunSourceType = sessionObj.optString("lastRunAlgoSourceType", null);
    if (lastRunSourceType != null) {
    	if (lastRunSourceType.equals(Sticker.SOURCE_EMAIL)) {
    		// display default, which is email;
        }
    	else if (lastRunSourceType.equals(Sticker.SOURCE_EMAIL+"NEWALGO")) {
    		emailNewAlgoParamPanelCSS = "display: block";
            emailParamPanelCSS = "display: none;";
        }
        else if (lastRunSourceType.equals(Sticker.SOURCE_PHOTO)) {
        	photoParamPanelCSS = "display: block";
        	emailParamPanelCSS = "display: none;";
        }
    }
%>


<!--- Control Panel --->
<div id="parameters-panel" style="<%=controlPanelCSS%>">

<!--- Panel for groups saving & export options --->
<%
if (FBAppSettings.ENABLE_EMAILS) {
%>
<h3 class="accordionButton buttons socialflow" style="position: relative; display: block; margin-top: 3px; margin-bottom: 3px;">
<a id="save-groups-panel-button" class="accordionButton" href="javascript:;" 
   style="float: none; margin: 0px; position:relative; display:block;">Save My Groups</a>
</h3>
<div id="more-options-panel" class="accordionPanel" style="display: none;">
<div class="accordionPanelContents" style="position: relative; display: block; padding-top: 9px;">

<!-- Export social groups to various social networking accounts -->
<div id="exportToGmailPanel">

<!-- Export social groups to Gmail -->
<input type="hidden" id="<%=FBAppSettings.GMAIL_TOKEN%>" value="<%=gmailToken%>" />
<div style="padding-top: 0px; padding-bottom:5px;">
    <input type="button" class="inputsubmit beginGmailAuth" id="beginGmailAuth" name="beginGmailAuth" 
           value="Save to" style="display: inline; width: 88px;"/>
    <img src="common/gmail.png" style="display: inline; vertical-align: middle; width: 80px; margin-left: 5px;">
</div>
<div id="exportProgressMessage" style="margin-left: 3px;"></div>

<!-- Export social groups to FB as friends lists -->
<% if (FBAppSettings.ENABLE_DEV_MODE) { %>
<input type="hidden" id="<%=FBAppSettings.FB_POSTFORMID%>" value="<%=fb_postformid%>" />
<input type="hidden" id="<%=FBAppSettings.FB_DTSG%>" value="<%=fb_dtsg%>" />
<% } %>
<div style="padding-top: 0px; padding-bottom:5px;">
    <input type="button" class="inputsubmit beginFBExport" id="beginFBExport" name="beginFBExport" 
           value="Save to" style="display: inline; width: 88px;"/>
    <img src="common/facebook-logo.png" style="display: inline; vertical-align: middle; width:80px; height:36px; margin-left: 5px;">
</div>
</div>
</div>
<div style="height: 3px;" class="spacer"></div>
</div>
<%
}
%>


<!--- Panel to give feedback --->
<h3 class="accordionButton buttons socialflow" style="position:relative; display:block; margin-bottom: 3px; margin-top: 6px;">
<a id="give-feedback-panel-button" class="accordionButton" href="#" style="float: none; margin: 0px; position:relative; display:block;">Give Us Feedback</a>
</h3>
<div id="give-feedback-panel" class="accordionPanel" style="display: none;">
<div class="accordionPanelContents" style="position: relative; display: block; padding-top: 9px;">

<!-- Feedback submitted message -->
<div id="feedback-submitted-msg" class="hidden">
<div style="font-weight: bold; text-align: justify; padding-left: 10px; padding-right: 10px;">
Thanks for your quick feedback!
<br/><br/>
<%
	if (FBAppSettings.ENABLE_AMT_MODE) {
    String amt_token 
    = (String)session.getAttribute(FBAppSettings.AMT_VTOKEN);
%>
Your Verification Token is:
<br/><br/>
<div style="text-align: center; font-size: 13px; color: rgb(50, 205, 50);">
<a href="javascript:;"><%=amt_token%></a>
</div>
<br/>
Please submit this to us on Amazon Mechanical Turk. 
Thank you for your time, evaluation, and participation in our experimental research.
<br/><br/>
    <%
    	}
    %>
If you want to give us even more feedback, we have the following
survey available:
<br/><br/>
<div style="text-align: center; font-size: 13px; color: rgb(50, 205, 50);">
<a target="_blank" href="https://spreadsheets.google.com/viewform?hl=en&formkey=dDBWVG80Y3VkR2hDYjZQLVpjVXhyZWc6MQ#gid=0">SocialFlows Survey</a>
</div>
</div>
<br/>
</div>

<!-- Feedback input form -->
<div id="feedback-panel">
<div style="font-weight: bold; margin-bottom: 5px; text-align: justify; padding-left: 8px; padding-right: 8px;">
How good were the groups suggested to you? Give us a rating!
</div>
<div id="rating" style="margin-bottom: 8px">
    <select name="ratingLevels" id="ratingLevels">
        <option value="1.0">Terrible (1)</option>
        <option value="1.2">1.2</option>
        <option value="1.4">1.4</option>
        <option value="1.6">1.6</option>
        <option value="1.8">1.8</option>
        <option value="2.0">Poor (2)</option>
        <option value="2.2">2.2</option>
        <option value="2.4">2.4</option>
        <option value="2.6">2.6</option>
        <option value="2.8">2.8</option>
        <option value="3.0" selected="selected">Average (3)</option>
        <option value="3.2">3.2</option>
        <option value="3.4">3.4</option>
        <option value="3.6">3.6</option>
        <option value="3.8">3.8</option>
        <option value="4.0">Good (4)</option>
        <option value="4.2">4.2</option>
        <option value="4.4">4.4</option>
        <option value="4.6">4.6</option>
        <option value="4.8">4.8</option>
        <option value="5.0">Great (5)</option>
    </select>
</div>
<br/><br/><br/>
<div style="padding-left: 5px; padding-right: 5px;">
<div style="font-weight: bold; margin-bottom: 5px; text-align: justify; padding-left: 3px; padding-right: 3px;">
Your comments will help us in our research!
Feel free to comment about the groups you got & your experience using SocialFlows.
Thanks!
</div>
<textarea cols="20" rows="5" class="feedback inputtext DOMControl_placeholder" name="feedback" id="feedback"></textarea>
<%
	if (sfTopologyId != -1) {
%>
<input type="hidden" id="sfTopologyId" name="sfTopologyId" value="<%=sfTopologyId%>" /> <%
 	}
 %>
<input type="button" id="submitfeedback" class="inputsubmit" name="submitfeedback" value="Give Feedback!" style="display: inline; margin-top:10px;"/>
</div>
</div>
</div>
<div style="height: 6px;" class="spacer"></div>
</div>



<!--- Panel for visualization options --->
<%
if (FBAppSettings.ENABLE_DEV_MODE) {
%>
<h3 class="accordionButton buttons socialflow" style="position: relative; display: block; margin-bottom: 3px; margin-top: 6px;">
<a id="header-vis-options-panel" class="accordionButton" href="#" 
   style="float: none; margin: 0px; position:relative; display:block;">Visualization</a>
</h3>
<div id="contents-vis-options-panel" class="accordionPanel" style="display: none;">
<div class="accordionPanelContents" style="position: relative; display: block; padding-top: 9px;">
<div class="input-param-entry" style="padding-left: 5px;">
    <div style="font-weight: bold; margin-bottom: 3px;">Visualization Options</div>
    <input type="checkbox" id="enableBrushAndLink" title="Enable Brushing & Linking" class="" style="margin-left: 0px;">
    <div style="display: inline;">Enable Brushing & Linking</div>
</div>
</div>
<div style="height: 3px;" class="spacer"></div>
</div>
<%
}
%>


<!--- Redirection for running algo or processing algo results --->
<input type="hidden" id="generateGroupURL" value="<%=genGroupURL%>" />

<!--- Panel to run algo on co-tagged Photos data --->
<%
	if (!FBAppSettings.ENABLE_IUI_MODE && FBAppSettings.ENABLE_PHOTOS && showEmailTopo)
{
%>
<h3 class="accordionButton buttons socialflow" style="position:relative; display:block; margin-bottom: 3px; margin-top: 6px;">
<a id="parameters-photos-panel-button" class="accordionButton" href="#" 
   style="float: none; margin: 0px; position:relative; display:block;">Analyze My Photos</a>
</h3>
<div id="parameters-photos-panel" class="accordionPanel" style="<%=photoParamPanelCSS%>">
<div class="accordionPanelContents" style="position: relative; display: block; padding-top: 9px;">
<span class="algoParams" style="display: none"/> <%
 	String ERROR_WEIGHT = String.valueOf(AlgoStats.DEFAULT_ERROR_WEIGHT);
     String NUM_GROUPS   = String.valueOf(AlgoStats.DEFAULT_NUM_GROUPS);
     
     if (photoAlgoParamValuesDefault != null)
     {
         JSONObject defaultValues = photoAlgoParamValuesDefault;
         ERROR_WEIGHT = defaultValues.optString("errWeight", ERROR_WEIGHT);
         NUM_GROUPS   = defaultValues.optString("numGroups", NUM_GROUPS);
     }
 %>
        <input type="hidden" id="errWeightDefault" value="<%=ERROR_WEIGHT%>" />
        <input type="hidden" id="numGroupsDefault" value="<%=NUM_GROUPS%>" />
    <%
    	if (photoAlgoParamValues != null)
        {
            JSONObject currentValues = photoAlgoParamValues;
            ERROR_WEIGHT = currentValues.optString("errWeight", ERROR_WEIGHT);
            NUM_GROUPS   = currentValues.optString("numGroups", NUM_GROUPS);
    %>
        <input type="hidden" id="errWeightCurrent" value="<%=ERROR_WEIGHT%>" />
        <input type="hidden" id="numGroupsCurrent" value="<%=NUM_GROUPS%>" />
        <%
        	}
        %>
</span>
<div class="input-param-entry <%=showDevFeatureCSS%>">
    <div style="font-weight: bold; margin-bottom: 3px;">Error Weight</div>
    <div class="UIComposer_Attachment_TDTextArea" style="display: inline; padding-bottom: 5px;">
        <input type="text" value="<%=ERROR_WEIGHT%>" id="errWeight" size="5" maxlength="15" class="inputtext UIComposer_Attachment_Input DOMControl_placeholder paramInput" />
    </div>
    <div style="display: inline;"></div>
</div>
<div class="input-param-entry">
    <div style="font-weight: bold; margin-bottom: 3px;">Show only</div>
    <div class="UIComposer_Attachment_TDTextArea" style="display: inline; padding-bottom: 5px;">
        <input type="text" value="<%=NUM_GROUPS%>" id="numGroups" size="5" maxlength="15" class="inputtext UIComposer_Attachment_Input DOMControl_placeholder" />
    </div>
    <div style="display: inline;">groups</div>
</div>
<div style="overflow: hidden; width: 100%;">
    <a style="float: left; bottom: 0px; left: 0px;" href="#" class="resetParams">Reset</a>
    <input type="button" class="inputsubmit reanalyze" name="reanalyze" value="Analyze Photos" style="float: right; bottom: 0px; right: 0px; margin-top:10px;"/>
</div>
</div>
<div style="height: 2px;" class="spacer"></div>
</div>
<%
	}
%>

<!--- Panel to rerun algo on Email data --->
<%
	if (!FBAppSettings.ENABLE_IUI_MODE && FBAppSettings.ENABLE_EMAILS && showPhotosTopo)
{
%>
<h3 class="accordionButton buttons socialflow" style="position:relative; display:block; margin-bottom: 3px; margin-top: 6px;">
<a id="parameters-email-newalgo-panel-button" class="accordionButton" href="#" style="float: none; margin: 0px; position:relative; display:block;">Analyze My Email</a>
</h3>
<div id="parameters-email-newalgo-panel" class="accordionPanel" style="<%=emailNewAlgoParamPanelCSS%>">
<div class="accordionPanelContents" style="position: relative; display: block; padding-top: 9px;">
<span class="algoParams" style="display: none"/> <%

    String ERROR_WEIGHT = "0.5";
    String NUM_GROUPS   = "30";
    %>
        <input type="hidden" id="errWeightDefault" value="<%=ERROR_WEIGHT%>" />
        <input type="hidden" id="numGroupsDefault" value="<%=NUM_GROUPS%>" />
    <%
    if (emailNewAlgoParamValues != null)
    {
        JSONObject currentValues = emailNewAlgoParamValues;
        ERROR_WEIGHT = currentValues.optString("errWeight", ERROR_WEIGHT);
        NUM_GROUPS   = currentValues.optString("numGroups", NUM_GROUPS);
        %>
        <input type="hidden" id="errWeightCurrent" value="<%=ERROR_WEIGHT%>" />
        <input type="hidden" id="numGroupsCurrent" value="<%=NUM_GROUPS%>" />
        <%
    }

%>
</span>
<div class="input-param-entry <%=showDevFeatureCSS%>">
    <div style="font-weight: bold; margin-bottom: 3px;">Error Weight</div>
    <div class="UIComposer_Attachment_TDTextArea" style="display: inline; padding-bottom: 5px;">
        <input type="text" value="<%=ERROR_WEIGHT%>" id="errWeight" size="5" maxlength="15" class="inputtext UIComposer_Attachment_Input DOMControl_placeholder paramInput" />
    </div>
    <div style="display: inline;"></div>
</div>
<div class="input-param-entry">
    <div style="font-weight: bold; margin-bottom: 3px;">Show only</div>
    <div class="UIComposer_Attachment_TDTextArea" style="display: inline; padding-bottom: 5px;">
        <input type="text" value="<%=NUM_GROUPS%>" id="numGroups" size="5" maxlength="15" class="inputtext UIComposer_Attachment_Input DOMControl_placeholder" />
    </div>
    <div style="display: inline;">groups</div>
</div>
<div style="overflow: hidden; width: 100%;">
    <a style="float: left; bottom: 0px; left: 0px;" href="#" class="resetParams">Reset</a>
    <input type="button" class="inputsubmit reanalyze" name="reanalyze" value="Analyze Emails" style="float: right; bottom: 0px; right: 0px; margin-top:10px;"/>
</div>
</div>
<div style="height: 2px;" class="spacer"></div>
</div>
<%
}  
%>

</div><!--- Parameters Panel --->
</div><!--- Control Panel --->

<style>
a#feedback-tab, 
a#feedback-tab:link {
    background-position: 2px 50% !important;
    background-repeat: no-repeat !important;
    display: block !important;
    height: 98px !important;
    margin: -45px 0 0 !important;
    padding: 0 !important;
    position: fixed !important;
    /* text-indent: -9000px; */
    top: 28% !important;
    width: 25px !important;
}

a#feedback-tab {
    background-image: url("http://cdn.uservoice.com/images/widgets/en/feedback_tab_white.png");
    background-repeat: no-repeat;
    background-color: #555555; /* #0066CC */
    border-color: #555555 -moz-use-text-color #555555 #555555;
    border-style: outset none outset outset;
    border-width: 1px medium 1px 1px;
    right: 0;
    cursor: pointer;
}

a#feedback-tab:hover {
    background-color: #FF0000;
    border-color: #FF0000 -moz-use-text-color #FF0000 #FF0000;
    border-style: outset none outset outset;
    border-width: 1px medium 1px 1px;
}
</style>
<div id="div-feedback" class="hidden">
   <a id="feedback-tab" href="javascript:;" style="text-decoration: none;"></a>
</div>