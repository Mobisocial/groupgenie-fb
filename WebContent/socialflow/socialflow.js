/*
 * SocialFlows JS
 * by Seng Keat Teh [ skteh@cs.stanford.edu ]
 * version: 1.1 (19 March 2010)
 * @requires jQuery v1.4 or later
 *
 */

/* BRUSH & LINK global variables */
var enableBrushLink = false;
var setOpacityTimer = null;
var setOpacityDelay = 500; //1500; // milliseconds
var removeOpacityDelay = 500; // milliseconds

jQuery(document).ready(function($) {

    // Enable functionality of expand/contract buttons
    $("#socialtree span.socialflow").live("click", function() {
        var parentGroup = $(this).parent("li.socialflow");
        var subList = $(this).siblings('ul.socialflow');

        if (subList.is(':visible')) {
            $(this).removeClass("expanded");
            $(this).addClass("collapsed");
            subList.slideUp("slow");
        }
        else {
            $(this).removeClass("collapsed");
            $(this).addClass("expanded");
            subList.slideDown("slow", function(){
            	// Lazily apply drag & drop, editable title UI interactions
            	lazyApplyStickerInteraction($(this));
            });
        }
    });
    
    
    
    /* ACCESS CONTROL UI */
    var ACsticker = $("#access-control-sticker");
    ACsticker.find("#close_ac").click(function(e) {
    	hideAccessControlUI();
    	return false;
    });
    
    // Change sticker icon selection to work
    $("#selectStickerIcon").click(function(e) {
        e.stopPropagation();
        e.preventDefault();
        var sticker_icon = $(this).find('.sticker_icon');

        $('#stickerIconSelection .iconlink').unbind('click').bind('click', {stickericon:sticker_icon},
            function(event) {
              var iconSrc = $(this).children('.icon').attr("src");
              event.data.stickericon.attr("src",iconSrc);
              event.preventDefault();
              $('#stickerIconSelection').fadeOut();
              return false;
        });
        $('#stickerIconSelection .close').click(function() {
        	$('#stickerIconSelection').fadeOut();
            return false;
        });
        
        $('#stickerIconSelection').css({
            top:    getPageScroll()[1] + (getPageHeight() / 10),
            left:   145.5
        });

        $('#stickerIconSelection').fadeIn('fast');
        return false;
    });
    
    // Get "Add New Collection Item" to work
    $("#collection_link").click(function() {
        var sticker = $(this).closest('#access-control-sticker');
        $('#collectionSelection input.selectItem:checked').each(
            function() {
              $(this).attr('checked', false);
         });
        $('#collectionSelection .close').click(function() {
            $('#collectionSelection').fadeOut();
            return false;
          });
        $('#collectionSelection .addToCollection').unbind('click').click(function() {
            // process the adding of new collection items
            addCollectionItems(sticker);
            $('#collectionSelection').fadeOut();
            return false;
          });
        
        $('#collectionSelection').css({
            top:    getPageScroll()[1] + (getPageHeight() / 10),
            left:   81
         });
        
        $('#collectionSelection').fadeIn('fast');
        return false;
    });

    // Get "Delete Collection Item" to work
    $(".remove_collection_item").live("click", function() {
        // console.log('Trying to delete collection member...');
        var collectionMemberElem = $(this).closest('li.collection_item');
        collectionMemberElem.fadeOut('slow', function() {
        	$(this).remove();
        });
        return false;
    });
    
    // Edit-in-place Sticker title
    ACsticker.find('#sticker-title').editable(function(value, settings) {
       return(value);
    }, {
        width     : '200px',
        maxlength : '50',
        height    : '12px',
        onblur    : 'submit',
        select	  : 'true',
        style     : 'display: inline',
        placeholder : 'Click to name this group'
    });



    /* UI INTERACTIONS */
    
    /* Brushing & linking social contacts and social groups */
    $('li.contact').live('mouseenter mouseleave', function(event) {
    	if (!enableBrushLink)
    	   return;
        var cliqueMemberMetadata = $(this).find('.metadata:first');
        var cliqueMemberId = cliqueMemberMetadata.children('span').attr('id');
        if (event.type == 'mouseover')
        {
            if (setOpacityTimer) {
                clearTimeout(setOpacityTimer);
                setOpacityTimer = null;
                $('li.contact:visible').each(function() {
                    var id = $(this).attr('id');
                    if (id == cliqueMemberId)
                        $(this).removeClass('opaque');
                    else
                        $(this).addClass('opaque');
                });
            }
            else {
                setOpacityTimer
                = setTimeout(function() {
                    setOpacityTimer = null;
                    $('li.contact:visible').each(function() {
                        var id = $(this).attr('id');
                        if (id == cliqueMemberId)
                            $(this).removeClass('opaque');
                        else
                            $(this).addClass('opaque');
                    });
                }, setOpacityDelay);
            }
        }
        else if (event.type == 'mouseout')
        {
            if (setOpacityTimer) {
                clearTimeout(setOpacityTimer);
                setOpacityTimer = null;
            }
            setOpacityTimer
            = setTimeout(function() {
                setOpacityTimer = null;
                $('li.contact:visible').each(function() {
                    $(this).removeClass('opaque');
                });
            }, removeOpacityDelay);
        }
    });
    

    $(".fbFriend").each(function() {
        var friendPhoto = $(this).find(".socialContactProfilePhoto");
        var friendLink  = $(this).find(".socialContactDisplayName");
        var selectedBox = $(this).find("input.selectFriend");

        friendPhoto.click(function() {
            if (selectedBox.attr('checked'))
            	selectedBox.attr('checked', false);
            else
            	selectedBox.attr('checked', true);
        	return false;
        });

        friendLink.click(function() {
            if (selectedBox.attr('checked'))
                selectedBox.attr('checked', false);
            else
                selectedBox.attr('checked', true);
            return false;
        });
    });
    
    
    /*
    // Make Sticker selectable by clicking on Sticker [DISABLED FOR NOW]
    $(".sticker").click(function() {
        var suggestedGroupElem = $(this);
        if (suggestedGroupElem.hasClass("deleted"))
            return;

        var selectedCheckbox = $(this).find('input.sticker-selected');
        if (suggestedGroupElem.hasClass("selected")) {
            selectedCheckbox.attr('checked', false);
            suggestedGroupElem.removeClass("selected");
        }
        else {
            selectedCheckbox.attr('checked', true);
            suggestedGroupElem.addClass("selected");
        }
    });
    */

    // Make Sticker selectable by clicking on checkbox
    $('input.sticker-selected').each(function() {
        if ($(this).attr('checked'))
            $(this).attr('checked', false);
    });

    $('input.sticker-selected').live("click", function(event) {
        event.stopPropagation();
        // event.preventDefault();
              
        var suggestedGroupElem = $(this).closest('.sticker');
        if (suggestedGroupElem.hasClass("deleted"))
            return;
        if (suggestedGroupElem.hasClass("selected"))
            suggestedGroupElem.removeClass("selected");
        else
            suggestedGroupElem.addClass("selected");     
    });
    
    // Get "Add New Clique Member" to work
    $(".add_clique_member").live("click", function() {
        var sticker = $(this).closest('div.sticker');
        //console.log('addNewCliqueMembers() being called...');

        $('#cliqueSelection input.selectFriend:checked').each(
            function() {
              $(this).attr('checked', false);
         });
        $('#cliqueSelection').css({
            top:    getPageScroll()[1] + (getPageHeight() / 10),
            left:   145.5
            // visibility: "visible"
            // display: block
          });
        $('#cliqueSelection .close').click(function() {
            //$('#cliqueSelection').css({
            //    visibility: "hidden"
            //  });
            $('#cliqueSelection').fadeOut();
            return false;
          });
        $('#cliqueSelection .addToClique').unbind('click').click(function() {
            // process the adding of new clique members
            addCliqueMembers(sticker);
            $('#cliqueSelection').fadeOut();
            return false;
          });
        $('#cliqueSelection').fadeIn('fast');
        return false;
    });
    
    // Get "Delete Clique Member" to work       
    $(".remove_clique_member").live("click", function() {
    	var cliqueMemberMetadata = $(this).siblings('.metadata');
    	var cliqueMemberId = cliqueMemberMetadata.children('span').attr('id');
    	var socialTreeElem = $(this).closest('li.socialflow');
    	
        var stickerElem = $(this).closest('div.sticker');
        var stickerElemMetadata = stickerElem.children('.stickerMetadata');
        var numDeletedContacts = stickerElemMetadata.children('#numDeletedContacts');
        numDeletedContacts.val(parseInt(numDeletedContacts.val())+1);
        var totalClicks = stickerElemMetadata.children('#totalClicks');
        totalClicks.val(parseInt(totalClicks.val())+1);

        // Remove clique member from current social group and from all subsets
        var hasClearOpaque = false;
        socialTreeElem.find("span."+cliqueMemberId).each(function() {
        	var clique = $(this).closest('#clique');
            var cliqueMemberElem = $(this).closest('li.contact');
        	cliqueMemberElem.fadeOut('slow', function() {
            	$(this).remove();
            	// No more clique members (left placeholder & Add Clique Members button),
            	// then should always display Add Clique Members button
                if (clique.children('.contact').length == 0) {
                    var addCliqueMember = clique.children('.addContact');
                    //addCliqueMember.switchClass( "options", "options-display", "normal" );
                    addCliqueMember.removeClass( "options" );
                    addCliqueMember.addClass( "options-display" );
                }
                if (!hasClearOpaque) {
                	clearBrushLinkOpacity();
                	hasClearOpaque = true;
                }
            });
        });
        return false;
    });
    
    
    // Share Data button
    $(".share_data").live("click", function(event) {
    	var stickerElem = $(this).closest('div.sticker');
    	showAccessControlUI(stickerElem);
        return false;
    });
        
    // Get Save Group to work
    $(".save_group").live("click", function(event) {
        event.stopPropagation();
        event.preventDefault();
        
        var suggestedGroupElem = $(this).closest('div.sticker');
        // Timeout needed due to delay by Jeditable widget for group naming
        setTimeout(function() {
        	saveSticker(suggestedGroupElem, processSaveGroupResult);
        }, 500);
        return false;
    });

    // Get Delete Group to work
    $(".delete_group").live("click", function(event) {
        event.stopPropagation();
        event.preventDefault();
        
        var suggestedGroupElem = $(this).closest('div.sticker');
        var stickerID = suggestedGroupElem.attr('id');
        var stickerName = suggestedGroupElem.find('.sticker-title').text();
        var stickerMetadata = suggestedGroupElem.children('.stickerMetadata');
        var stickerSfID  = stickerMetadata.children('#stickerSfID').val();
        if (stickerSfID == null || stickerSfID == 'null' || stickerSfID.trim().length <= 0)
        	stickerSfID = 'null';
        var stickerRunID  = stickerMetadata.children('#stickerTopologyRunID').val();
        if (stickerRunID == null || stickerRunID == 'null' || stickerRunID.trim().length <= 0)
        	stickerRunID = '0';
        
        //var stickerURI  = stickerMetadata.children('#stickerResourceURI').val();
        //if (stickerURI == null || stickerURI == 'null' || stickerURI.trim().length <= 0)
        //   stickerURI = 'null';
        
        // Create a Javascript complex data object representing a Sticker
        var stickerObj = new Object();
        stickerObj.stickerSfID = stickerSfID;
        stickerObj.name = stickerName;
        stickerObj.stickerID = stickerID;
        stickerObj.stickerRunID = stickerRunID;
        var stickerJSON = JSON.stringify(stickerObj, null, 1);
        
        if (stickerSfID == 'null') 
        {
           // Do quick deletion
           $.ajax({
                  type:           'POST',
                  cache:          false,
                  url:            'deleteStickerSocialflow.jsp',
                  data:           {input: stickerJSON},
                  // processData:    false,
                  dataType:       'json'
           });
           renderGroupAsDeleted(suggestedGroupElem);
        }
        else
        {
           // ajax request to get an existing Sticker to be deleted at backend

           // TODO: Confirm deletion message?
           // TODO: Show result message, then auto-fade it away
           
           // Using POST method to send sticker-editing/manipulation data
           $.facebox.settings.loadingMessage = 'Deleting Group...';
           jQuery.facebox({ post: 'deleteStickerSocialflow.jsp', progress: 'getStatus.jsp',
                            postdata: stickerJSON, postcallback: processDeleteGroupResult });
        }
        
        return false;
    });


    // Get Merge Group to work
    $('.merge_group').live("click", function() {
        // console.log('Trying to merge group...');
        var targetSticker = $(this).closest('div.sticker');
        var targetStickerID = targetSticker.attr('id');
        var targetStickerMetadata = targetSticker.children('.stickerMetadata');
        var mergedStickersList = targetStickerMetadata.children('.merged-stickers-metadata');
        // console.log('Target is sticker '+targetStickerID);
        
        // Ignore merge if this target sticker was not selected
        //var isSelected = targetSticker.find('input.sticker-selected:checked').length; // 'li.sticker-options > '
        //if (!isSelected)
        //    return false;

        var numMergesClicks = 1; // account for single-click on Merge button
        $('div.sticker > input.sticker-selected:checked').each(
            function() {
              var selectedSticker = $(this).closest('div.sticker');
              var selectedStickerID = selectedSticker.attr('id');
              var selectedStickerName = selectedSticker.find('.sticker-title').text();
              
              if (selectedStickerID != "null" && selectedStickerID == targetStickerID)
                 return true; // continue;

              // console.log('Selected sticker '+selectedStickerID);
              numMergesClicks++;
              copyAllCliqueMembers(selectedSticker, targetSticker);
              // copyAllCollectionItems(selectedSticker, targetSticker);

              var isSuggestedGroup = false;
              var selectedStickerMetadata = selectedSticker.children('.stickerMetadata');              
              //var selectedStickerURI = selectedStickerMetadata.children('#stickerResourceURI').val();
              var stickerRunID  = selectedStickerMetadata.children('#stickerTopologyRunID').val();
              if (stickerRunID == null || stickerRunID == 'null' || stickerRunID.trim().length <= 0)
              	 stickerRunID = '0';

              // Remove selected suggested groups that were merged together into target sticker
              if (selectedSticker.hasClass('suggested'))
              {
                  var mergedSticker = '<input type="hidden" class="stickerToBeMerged" value="'+selectedStickerID+'" /> \n';
                  mergedStickersList.append(mergedSticker);
                  
                  // Create a Javascript complex data object representing a Sticker
                  var stickerObj = new Object();
                  stickerObj.name = selectedStickerName;
                  stickerObj.stickerID = selectedStickerID;
                  stickerObj.deleteThroughMerge = true;
                  stickerObj.stickerRunID = stickerRunID;
                  //stickerObj.stickerURI = selectedStickerURI;
                  
                  var stickerJSON = JSON.stringify(stickerObj, null, 1);
                  
                  // Do quick deletion
                  $.ajax({
                         type:           'POST',
                         cache:          false,
                         url:            'deleteStickerSocialflow.jsp',
                         data:           {input: stickerJSON},
                         // processData:    false,
                         dataType:       'json'
                  });
                  renderGroupAsDeleted(selectedSticker);
                  //renderGroupAsUnselected(selectedSticker);
              }
              else {
            	  // Currently does not remove saved stickers that are merged together
            	  renderGroupAsUnselected(selectedSticker);
              }
              
              
              
         });
        renderGroupAsUnselected(targetSticker);

        // UI User Study statistics
        var numMerges = targetStickerMetadata.children('#numMerges');
        numMerges.val(parseInt(numMerges.val())+numMergesClicks);
        var totalClicks = targetStickerMetadata.children('#totalClicks');
        totalClicks.val(parseInt(totalClicks.val())+numMergesClicks);

        return false;
    });

    
    // Creating New Groups and Subgroups
    var newStickerNum = Math.ceil(100*Math.random());
    var shareButtonHTML =
        '        <div class="inline-block pillbutton options options-saved-hover"> \n' +
        '        <div class="inline-block pillbutton-label"> \n' +
        '           <div class="inline-block pillbutton-label-outer-box pillbutton-outer-box-color"> \n' +
        '             <div class="inline-block pillbutton-label-inner-box pillbutton-inner-box-color"> \n' +
        '             <div>SHARE</div> \n' +
        '        </div></div></div></div> \n';
    if (!socialflowsEnableAccessControl) 
    	shareButtonHTML = '';
    
    var newGroupHTML =
        '<li class="socialflow" style="display: none;"> \n' +
        '<div style="padding-left: 23px;"> \n' +
        '    <div class="sticker suggested" id="{PLACEHOLDER}"> \n' +
        '    <div class="stickerMetadata" style="display: none;"> \n' +
        '        <input type="hidden" id="stickerSfID" name="stickerSfID" value="null" /> \n' +
        '        <input type="hidden" id="stickerResourceURI" name="stickerResourceURI" value="null" /> \n' +
        '        <input type="hidden" id="stickerIconURI" name="stickerIconURI" value="null" /> \n' +
        '        <span class="merged-stickers-metadata"></span> \n' +
        '        <input type="hidden" id="numDroppedIn" name="numDroppedIn" value="0" /> \n' +
        '        <input type="hidden" id="numDraggedOut" name="numDraggedOut" value="0" /> \n' +
        '        <input type="hidden" id="numAddedContactsByAdd" name="numAddedContactsByAdd" value="0" /> \n' +
        '        <input type="hidden" id="numAddedContactsByMerge" name="numAddedContactsByMerge" value="0" /> \n' +
        '        <input type="hidden" id="numClicksToAddContacts" name="numClicksToAddContacts" value="0" /> \n' +
        '        <input type="hidden" id="numDeletedContacts" name="numDeletedContacts" value="0" /> \n' +
        '        <input type="hidden" id="numMerges" name="numMerges" value="0" /> \n' +
        '        <input type="hidden" id="totalClicks" name="totalClicks" value="1" /> \n' +
        '    </div> \n' +
        '    <div class="sticker-title" style="float:left;"></div> \n' +
        shareButtonHTML +
        '        <img class="delete_group options options-bar options-icon options-right options-saved options-selected" src="stickers/cross.gif" title="Delete"/> \n' +
        //'        <img class="save_group   options options-bar options-icon options-right options-saved options-selected" src="stickers/disk.gif" title="Save"/> \n' +
        '        <img class="create_subgroup options options-bar options-icon options-right options-saved options-selected" src="stickers/plus.png" title="Create New Subgroup"/> \n' +
        '        <input title="Merge Cliques" type="button" value="Merge" name="merge-cliques" class="merge_group button-fbstyle options options-bar options-button options-right options-selected"/> \n' +
        '        <input title="Select Clique" type="checkbox" class="sticker-selected options options-bar options-right options-selected"/> \n' +
        '    <br/> \n' +
        '    <ul id="clique" class="clique"> \n' +
        '    <li class="placeholder" style="display:none;"></li> \n' +
        '    <li title="Add a Social Contact" class="add_clique_member addContact options-display"> \n' +
        '        <img title="Add a Social Contact" src="stickers/q_silhouette.gif" class="contact"/> \n' +
        '        <div class="outer-image-overlay" title="Add a Social Contact"><div class="inner-image-overlay"><img src="stickers/plus.png"/></div></div> \n' +
        '    </li> \n' +
        '    </ul> \n' +
        '    </div> \n' +
        '</div> \n' +
        '</li>';
    
    // Get Create Group to work
    $('#create_group').click(function() {

    	// Add the newly created group
        var newStickerId = "newSticker_"+newStickerNum;
        newStickerNum++;
        var newGroup = $(newGroupHTML);
        newGroup.find('div.sticker').attr('id', newStickerId);
        
        var topGroupsList = $("ul#socialtree");
        newGroup.prependTo(topGroupsList);
        // Enable necessary UI interactions
        // - Drag & Drop
        // - Editable group names/label (click-to-edit)
        newGroup.find("ul.clique").each(
                function() {
                    var clique = $(this);
                    makeSortable(clique);
        });
        newGroup.find(".sticker-title").editable(function(value, settings) { 
            return(value);
         }, {
             width     : '200px',
             maxlength : '50',
             height    : '12px',
             onblur    : 'submit',
             select	   : 'true',
             style     : 'display: inline',
             placeholder : 'Click to name this group'
         });
        
        // Slide down to reveal newly created group
        newGroup.slideDown("slow");
    	
    	return false;
    });
    
    // Get Create New Subgroup to work
    $(".create_subgroup").live("click", function(event) {
        event.stopPropagation();
        // event.preventDefault();

        // <img class="create_new_group options options-icon options-left" src="stickers/plus.png" title="Create New Subgroup"/>
        
        // console.log('Trying to create new subgroup...');
        var stickerElem = $(this).closest('div.sticker');
        var stickerElemID = stickerElem.attr('id');
        //var stickerElemMetadata = stickerElem.children('.stickerMetadata');
        //var mergedStickersList = stickerElemMetadata.children('.merged-stickers-metadata');

        // <li class="socialflow">
        var stickerLevel = stickerElem.closest('li.socialflow');
        var socialflowNavi = stickerLevel.children("span.socialflow");
        if (socialflowNavi.length <= 0) 
        {
           // Create the expand-collapse navigation
           // Create span if it doesn't already exist
           socialflowNavi = $('<span class="socialflow socialflow-navi collapsed"/>');
           socialflowNavi.appendTo(stickerLevel);
           // Note: click functionality assigned by live handler
        }

        // <ul class="socialflow" style="display: none;" ></ul>
        var subList = stickerLevel.children("ul.socialflow");
        if (subList.length <= 0) 
        {
            // Create the sublist of subgroups
            var subList = $('<ul class="socialflow ui-initialized" style="display: none;" ></ul>');
            subList.appendTo(stickerLevel);
        }

        // Add the newly created subgroup
        var newStickerId = "newSticker_"+newStickerNum;
        newStickerNum++;
        var newSubgroup = $(newGroupHTML);
        newSubgroup.find('div.sticker').attr('id', newStickerId);
        
        newSubgroup.prependTo(subList);
        // Enable necessary UI interactions
        // - Drag & Drop
        // - Editable group names/label (click-to-edit)
        newSubgroup.find("ul.clique").each(
                function() {
                    var clique = $(this);
                    makeSortable(clique);
        });
        newSubgroup.find(".sticker-title").editable(function(value, settings) { 
            return(value);
         }, {
             width     : '200px',
             maxlength : '50',
             height    : '12px',
             onblur    : 'submit',
             select	   : 'true',
             style     : 'display: inline',
             placeholder : 'Click to name this group'
         });


        // Expand if currently collapsed
        if (!subList.is(':visible')) {
            socialflowNavi.removeClass("collapsed");
            socialflowNavi.addClass("expanded");
        }
        
        // Reveal the sublist if it is currently hidden
        if (subList.children().length == 1) {
        	newSubgroup.show();
            subList.slideDown("slow");
        }
        else {
            subList.slideDown("slow", function(event) {
            	// Lazily apply drag & drop, editable title 
            	// UI interactions to existing subgroups
            	lazyApplyStickerInteraction($(this));
                // Slide down to reveal newly created subgroup
                newSubgroup.slideDown("slow");
            });
        }

    });
    

    // Enable more advanced/expensive UI interactions
    socialflowProcessDone = true;
    $("ul.socialflow-init-display").each(function(){
    	var displayedLevel = $(this);
    	lazyApplyStickerInteraction(displayedLevel);
    });
    
    /* Advance options for SocialFlows user interface */
    initControlPanel();
    
    // Hide progress display and reveal UI
    if (isDemoMode)
    {
    	setTimeout(function() {
    		 $('#progress-display').slideUp('slow', function(event){
    	        	$(this).hide();
    	       	    $('.buttons.socialflow').slideDown('slow');
    	       	    $('#socialtree').slideDown('10000', function(){
    	       	    	showControlPanel();
    	       	    });
    	     });
        }, 5500);
    }
    else {
        $('#progress-display').slideUp('slow', function(event){
        	$(this).hide();
       	    $('.buttons.socialflow').slideDown('slow');
       	    $('#div-feedback').slideDown('slow');
       	    $('#socialtree').slideDown('5000');
        });
    }
  
});

    
    function lazyApplyStickerInteraction(hierarchyLevel)
    {
    	if (hierarchyLevel.hasClass('ui-initialized'))
    		return;
    	
    	hierarchyLevel.children('li.socialflow').each(
                function() {
                	var stickerElem = $(this).find('div.sticker:first');
                	var stickerTitle = stickerElem.children(".sticker-title");
                    var clique = stickerElem.children("ul.clique");
                    
                    // Edit-in-place Sticker title      
                    stickerTitle.editable(function(value, settings) {
                       return(value);
                    }, {
                        width     : '200px',
                        maxlength : '50',
                        height    : '12px',
                        onblur    : 'submit',
                        select	  : 'true',
                        style     : 'display: inline',
                        placeholder : 'Click to name this group'
                    });
                    
                    // Enable Drag-and-Drop interaction
                    //setTimeout( 
                    //    function() { 
                        	//numSuggCliques++;
                        	//if (numSuggCliques > 1)
                        	//   $('#progressmessage').html('Rendering '+numSuggCliques+' social groups...');
                        	//else
                        	//   $('#progressmessage').html('Rendering '+numSuggCliques+' social group...');
                            makeSortable(clique);
                    //    }, 
                    //    30 ); // milliseconds
        });
    	
    	hierarchyLevel.addClass('ui-initialized');
    }
    
    function makeSortable(clique)
    {
    	clique.sortable({
    	    start: startDragDrop,
    	    stop:  stopDragDrop,
        	receive: receiveDrop,
            //revert: true,
            //delay: 500, // milliseconds
            helper: 'clone',
            connectWith: 'ul.clique',
            items: 'li.contact, li.placeholder'
        });
        clique.disableSelection();
    }
    
    function stopDragDrop(event, ui)
    {
    	var sender = ui.sender;
    	enableBrushLink
    	= $('#enableBrushAndLink').attr('checked');
    }
    
    function startDragDrop(event, ui)
    {
    	ui.item.show();
    	enableBrushLink = false;
    	clearBrushLinkOpacity();
    }

    function receiveDrop(event, ui)
    {
    	var cliqueMember = ui.item;
        var cliqueMemberMetadata = cliqueMember.find('.metadata:first');
    	var cliqueMemberId = cliqueMemberMetadata.children('span').attr('id');

    	var contactAlreadyExists = false;
    	var destClique = $(this);
    	// Check to see if this friend is already in target sticker's Clique
        var matchingCliqueMember = destClique.find("li.contact div.metadata > span[id='"+cliqueMemberId+"']");
        // Don't add person if already exists in target sticker's Clique
        contactAlreadyExists = (matchingCliqueMember.length > 1); // due to original contact obj already moved to receiver/dest
               													  // by sortable code when 'receive' callback invoked

        // Enables 'Drag-n-Copy'
        var copy = null;
        if (!contactAlreadyExists) {
        	copy = ui.item.before(ui.item.clone()).hide();
        }
        
        // Don't do 'Drag-n-Copy' if dragging and dropping within same clique
    	if (ui.sender != null && $(this) != ui.sender) {
    	   $(ui.sender).sortable('cancel');
    	   if (!contactAlreadyExists) {
    	      copy.show();
    	      // records contact being receive, then propogates added contact to all supersets
          	  receiveContact(event, ui, $(this));
    	   }
    	}
    }

    
    /* BRUSHING & LINKING methods */
    function toggleBrushLink(enable)
    {
    	if (enable) {
    		enableBrushLink = true;
    	}
    	else {
    		enableBrushLink = false;
    		clearBrushLinkOpacity();
    	}
    }

    function clearBrushLinkOpacity()
    {
    	if (setOpacityTimer) {
            clearTimeout(setOpacityTimer);
            setOpacityTimer = null;
    	}
        $('li.contact:visible').each(function() {
        	if ($(this).hasClass('opaque'))
               $(this).removeClass('opaque');
        });
    }

    
    
    /* UI User Study statistics */
    function receiveContact(event, ui, receiver)
    { 
        //event.stopPropagation();
        //$(this), event.target  //(the clique that received the contact)
        //ui.item //(the social contact being manipulated)
        var stickerElem = receiver.closest('div.sticker');
        var stickerElemID = stickerElem.attr('id');
        var stickerElemMetadata = stickerElem.children('.stickerMetadata');

        var numDroppedIn = stickerElemMetadata.children('#numDroppedIn');
        numDroppedIn.val(parseInt(numDroppedIn.val())+1);
        var totalClicks = stickerElemMetadata.children('#totalClicks');
        totalClicks.val(parseInt(totalClicks.val())+1);
        
        // Ensure supersets are updated with received contact
        var cliqueMembers = new Array();
        cliqueMembers[0]  = ui.item;
        percolateUpCliqueMembers(stickerElem, cliqueMembers);


        // [DISABLED due to now doing Drag-n-Copy instead of Drag-n-Drop]
        // Remove clique member from its original clique
        /*
        var cliqueMember = ui.item;
        var cliqueMemberMetadata = cliqueMember.find('.metadata:first');
    	var cliqueMemberId = cliqueMemberMetadata.children('span').attr('id');
        
        var originClique = ui.sender;
        var originStickerElem = originClique.closest('div.sticker');
        var originStickerElemID = originStickerElem.attr('id');
        var originStickerElemMetadata = originStickerElem.children('.stickerMetadata');
        var originSocialTreeElem = originStickerElem.closest('li.socialflow');
        
        // Do not do anything if destination clique is actually a subset of origin clique
        if (originSocialTreeElem.find('#'+stickerElemID+':first').length > 0)
        	return;
        
        // Remove clique member from origin social group and from all its subsets
        var hasClearOpaque = false;
        originSocialTreeElem.find("span."+cliqueMemberId).each(function() {
        	var clique = $(this).closest('#clique');
            var cliqueMemberElem = $(this).closest('li.contact');
        	cliqueMemberElem.fadeOut('slow', function() {
            	$(this).remove();
            	// No more clique members (left placeholder & Add Clique Members button),
            	// then should always display Add Clique Members button
                if (clique.children('.contact').length == 0) {
                    var addCliqueMember = clique.children('.addContact');
                    //addCliqueMember.switchClass( "options", "options-display", "normal" );
                    addCliqueMember.removeClass( "options" );
                    addCliqueMember.addClass( "options-display" );
                }
                if (!hasClearOpaque) {
                	clearBrushLinkOpacity();
                	hasClearOpaque = true;
                }
            });
        });
        */
    }

    function removeContact(event, ui)
    {
        //event.stopPropagation();
        //$(this), event.target  //(the clique that lost the contact)
        //ui.item //(the social contact being manipulated)
        var originClique = $(this);
        var stickerElem = originClique.closest('div.sticker');
        var stickerElemID = stickerElem.attr('id');
        var stickerElemMetadata = stickerElem.children('.stickerMetadata');
        var socialTreeElem = stickerElem.closest('li.socialflow');

        var numDraggedOut = stickerElemMetadata.children('#numDraggedOut');
        numDraggedOut.val(parseInt(numDraggedOut.val())+1);
        var totalClicks = stickerElemMetadata.children('#totalClicks');
        totalClicks.val(parseInt(totalClicks.val())+1);

        // Show Add Contact option when clique becomes empty
        //if (originClique.children('.contact').length == 0) {
        //   var addCliqueMember = originClique.children('.addContact');
        //   addCliqueMember.removeClass( "options" );
        //   addCliqueMember.addClass( "options-display" );
        //}

        var cliqueMember = ui.item;
        var cliqueMemberMetadata = cliqueMember.find('.metadata:first');
    	var cliqueMemberId = cliqueMemberMetadata.children('span').attr('id');

    }



    function renderGroupAsDeleted(stickerElem)
    {
        var selectCheckbox = stickerElem.find('input.sticker-selected');
        if (selectCheckbox.attr('checked'))
        	selectCheckbox.attr('checked', false);

    	var parentSubgroups = stickerElem.closest('ul.socialflow');
    	var parentNavi = parentSubgroups.siblings('span.socialflow-navi');
    	var parentSubgroupsWillBeEmpty = (parentSubgroups.children().length == 1);
    	if (parentSubgroups.hasClass('socialflow-root'))
    		parentSubgroupsWillBeEmpty = false;

        // Disable drag and drop on this sticker
        var clique = stickerElem.find('ul.clique');
    	clique.sortable('disable'); // 'destroy'
        
        if (stickerElem.hasClass("suggested")) {
        	stickerElem.removeClass("selected");
        	if (parentNavi.length != 0 && parentSubgroupsWillBeEmpty)
                parentNavi.remove();
        	stickerElem.switchClass( "suggested", "deleted", "slow", 
        		function() {
        		   //if (clique.children('.contact').length == 0) {
                      stickerElem.closest('li.socialflow')
                                 .slideUp( 'slow', 
                                           function() { 
                                	           $(this).remove();
                                	           if (parentSubgroupsWillBeEmpty)
                                	        	   parentSubgroups.remove();
                                	       });
        		   //}  
			    });
        }
        else if (stickerElem.hasClass("saved")) {
        	stickerElem.removeClass("selected");
        	if (parentNavi.length != 0 && parentSubgroupsWillBeEmpty)
                parentNavi.remove();
        	stickerElem.switchClass( "saved", "deleted", "slow",
                function() {
        	       //if (clique.children('.contact').length == 0) {
                      stickerElem.closest('li.socialflow')
                                 .slideUp( 'slow', 
                                		   function() { 
                                               $(this).remove();
                                               if (parentSubgroupsWillBeEmpty)
                                                   parentSubgroups.remove();
                                           });
                   //}  
            	});
        }
    }

    function renderGroupAsUnselected(stickerElem)
    {
        var selectCheckbox = stickerElem.find('input.sticker-selected');
        if (selectCheckbox.attr('checked'))
            selectCheckbox.attr('checked', false);

        stickerElem.removeClass("selected");
    }


    //var placeholderProfilePic = "common/contact_placeholder.gif";
    function addCliqueMembers(stickerElem)
    {
        var newContacts = 0;
        var numClicks = 2; // single-click to bring up Add Clique Member selection, single-click on Add button
        var clique = stickerElem.find('#clique');
        var cliqueMembers = new Array();

        $('#cliqueSelection input.selectFriend:checked').each(
          function()
          {
        	numClicks++;

            // Check to see if selected friend is already in Clique
            var fbId = $(this).val();
            var cliqueMember = stickerElem.find("li.contact div.metadata > span[class='"+fbId+"']");
            // Don't add person if already exists in current Clique
            if (cliqueMember.length != 0)
               return;

            var fbFriend = $(this).closest('div.fbFriend');
            var fbProfileImg  = fbFriend.find('img.fbProfileImage').attr('src');
            var fbProfileName = fbFriend.find('img.fbProfileImage').attr('title');
            var fbProfileUrl  = fbFriend.find('a.fbProfileUrl').attr('href');
            var fbDisplayName = fbFriend.find('a.fbDisplayName').text();

            // Generate UI rendering for this social contact
            var contact =
            '<li id="'+fbId+'" class="contact" title="'+fbProfileName+'"> \n' +
            '    <div class="metadata" style="display: none;"> \n' +
            fbFriend.find("div.metadata").html() +
            '    </div> \n' +
            '    <img title="'+fbProfileName+'" src="'+fbProfileImg+'" class="contact" /> \n';
            if (fbProfileImg.endsWith(".gif")) {
                contact += '    <div title="'+fbProfileName+'" class="outer-name-overlay"><div class="inner-name-overlay">'+fbDisplayName+'</div></div> \n'
            }
            contact +=
            '    <a title="Delete" class="delete deletelink remove_clique_member">X</a> \n' +
            '</li> \n';

            // Insert social contact into the UI rendering of the clique
            newContacts++;
            //var addContactObj = stickerElem.find('#clique > .addContact');
            //var newCliqueMember = $(contact).hide().insertBefore(addContactObj).fadeIn(3000);
            cliqueMembers[cliqueMembers.length] = $(contact);
        });

        percolateUpCliqueMembers(stickerElem, cliqueMembers);
        
        // Don't always show Add Contact option
        //if (newContacts > 0) {
        //    var addCliqueMember = clique.children('.addContact');
        //    if (addCliqueMember.hasClass("options-display")) {
        //    	addCliqueMember.removeClass( "options-display" );
        //        addCliqueMember.addClass( "options" );
        //    }
        //}
        
        var stickerElemMetadata = stickerElem.children('.stickerMetadata');
        var numAddedContactsByAdd = stickerElemMetadata.children('#numAddedContactsByAdd');
        numAddedContactsByAdd.val(parseInt(numAddedContactsByAdd.val())+newContacts);
        var numClicksToAddContacts = stickerElemMetadata.children('#numClicksToAddContacts');
        numClicksToAddContacts.val(parseInt(numClicksToAddContacts.val())+numClicks);
        var totalClicks = stickerElemMetadata.children('#totalClicks');
        totalClicks.val(parseInt(totalClicks.val())+numClicks);
    }


    function copyAllCliqueMembers(sourceSticker, targetSticker)
    {
    	var newContacts = 0;
        var sourceClique = sourceSticker.find('#clique');
        //var targetClique = targetSticker.find('#clique');
        //var addCliqueMember = targetClique.children('.addContact');
        var cliqueMembers = new Array();

        sourceClique.find('li.contact').each(
           function()
           {
        	   cliqueMembers[cliqueMembers.length] = $(this);
        	   
               //var metadata = $(this).find('div.metadata > span');
               //var userID = metadata.attr('id');

               // Check to see if this friend is already in target sticker's Clique
               //var targetCliqueMember = targetClique.find("li.contact div.metadata > span[id='"+userID+"']");
               // Don't add person if already exists in target sticker's Clique
               //if (targetCliqueMember.length != 0)
               //   return true; // continue;

               newContacts++;
               //var addedCliqueMember = $(this).clone(true).hide().insertBefore(addCliqueMember).fadeIn(3000);
           }
        );

        percolateUpCliqueMembers(targetSticker, cliqueMembers);

        var stickerElemMetadata = targetSticker.children('.stickerMetadata');
        var numAddedContactsByMerge = stickerElemMetadata.children('#numAddedContactsByMerge');
        numAddedContactsByMerge.val(parseInt(numAddedContactsByMerge.val())+newContacts);        
    }

    function percolateUpCliqueMembers(currentSticker, cliqueMembers)
    {
    	if (cliqueMembers.length <= 0)
    		return;

    	// Add clique members to current sticker
    	var clique = currentSticker.find('#clique');
    	var addCliqueMember = clique.children('.addContact');
    	
    	for (var i = 0; i < cliqueMembers.length; i++) {
    		var cliqueMember = cliqueMembers[i];
    		var metadata = cliqueMember.find('div.metadata > span');
            var userID = metadata.attr('id');

            // Check to see if this friend is already in target sticker's Clique
            var matchingCliqueMember = clique.find("li.contact div.metadata > span[id='"+userID+"']");
            // Don't add person if already exists in target sticker's Clique
            if (matchingCliqueMember.length != 0)
               continue;
    		
            // Insert social contact into the UI rendering of the clique
            var addedCliqueMember = cliqueMember.clone(true).hide().insertBefore(addCliqueMember).fadeIn(3000);
    	}
    	
    	// Don't always show Add Contact option
        if (addCliqueMember.hasClass("options-display")) {
          	addCliqueMember.removeClass( "options-display" );
            addCliqueMember.addClass( "options" );
        }
        
        var currentSocialTree = currentSticker.closest('li.socialflow');
        if (currentSocialTree.length != 0) {
        	var parentSocialTree = currentSocialTree.parent().closest('li.socialflow');
        	if (parentSocialTree.length != 0) {
        		var parentSticker = parentSocialTree.find('div.sticker:first');
        		if (parentSticker.length != 0)
        		   percolateUpCliqueMembers(parentSticker, cliqueMembers);
        	}
        }
    }
    
    
    


    /* Save Group/Sticker */
    function saveSticker(stickerElem, saveStickerCallback)
    {
        //var stickerMetadata = stickerElem.children('.stickerMetadata');
        var stickerName = stickerElem.find('.sticker-title').text();

        if (stickerName == null || stickerName.trim().length <= 0 ||
            stickerName == 'Click to name this group' || 
            stickerName == 'Click to edit' || stickerName == 'New Group') 
        {
           jQuery.facebox('Please label this group.');
           return false;
        }

        if (stickerElem.find('#clique > .contact').length == 0)
        {
           jQuery.facebox('This social group is empty. Please add a contact.');
           return false;
        }

        var stickerJSON = getGroupJSON(stickerElem);
        
        // POST request to send complex metadata about social group being saved
        $.facebox.settings.loadingMessage = 'Saving Social Group...';
        jQuery.facebox({ post: 'saveStickerSocialflow.jsp', progress: 'getStatus.jsp',
                         postdata: stickerJSON, postcallback: saveStickerCallback });
        
    }
    
    function getGroupJSON(stickerElem)
    {
    	var stickerObj = getGroupJSObject(stickerElem);

    	// STRINGIFY!
        var stickerJSON = JSON.stringify(stickerObj, null, 1);
        // console.log('\nJSON STICKER REPRESENTATION: \n'+stickerJSON);

        return stickerJSON;
    }
    
    function getGroupJSObject(stickerElem)
    {
        var stickerID   = stickerElem.attr('id');
        var stickerName = stickerElem.find('.sticker-title').text();
        var stickerMetadata = stickerElem.children('.stickerMetadata');
        
    	// Create Javascript complex data object representing a Sticker object
        var stickerObj = new Object();
        var stickerSfID = stickerMetadata.children('#stickerSfID').val();
        if (stickerSfID == null || stickerSfID == 'null') {
            stickerSfID = 'null';
        }
        var stickerRunID = stickerMetadata.children('#stickerTopologyRunID').val();
        if (stickerRunID == null || stickerRunID == 'null' || stickerRunID.trim().length <= 0) {
        	stickerRunID = '0';
        }
        //var stickerURI = stickerMetadata.children('#stickerResourceURI').val();
        //if (stickerURI == null || stickerURI == 'null') {
        //    stickerURI = 'null';
        //}
        
        stickerObj.name         = stickerName;
        stickerObj.stickerSfID  = stickerSfID;
        stickerObj.stickerRunID = stickerRunID;
        stickerObj.stickerID    = stickerID;
        //stickerObj.stickerURI = stickerURI;
        
        // Save the Sticker Icon
    	var stickerIconSrc = stickerMetadata.find('#stickerIconURI').val();
    	if (stickerIconSrc != null && stickerIconSrc != 'null'
    	    && stickerIconSrc.trim().length > 0 
    	    && stickerIconSrc != 'stickers_icons/sticker_new.png')
        {
    		stickerObj.stickerIconSrc = stickerIconSrc;
        }

        // UI metrics metadata for this Sticker
        var UImetrics = new Object();
        UImetrics.numAddedContactsByAdd   = parseInt(stickerMetadata.children('#numAddedContactsByAdd').val());
        UImetrics.numAddedContactsByMerge = parseInt(stickerMetadata.children('#numAddedContactsByMerge').val());
        UImetrics.numClicksToAddContacts = parseInt(stickerMetadata.children('#numClicksToAddContacts').val());
        UImetrics.numDeletedContacts = parseInt(stickerMetadata.children('#numDeletedContacts').val());
        UImetrics.numDroppedIn  = parseInt(stickerMetadata.children('#numDroppedIn').val());
        UImetrics.numDraggedOut = parseInt(stickerMetadata.children('#numDraggedOut').val());
        UImetrics.numMerges     = parseInt(stickerMetadata.children('#numMerges').val());
        UImetrics.totalClicks   = parseInt(stickerMetadata.children('#totalClicks').val());
        stickerObj.stickerMetrics = UImetrics;
        
                
        // Clique Members
        // <ul id="clique">
        //    <li class="friend_grid_col clearfix clique_member" style="height:50px;" userid=" ">
        //    <span id="metadata" class="userID" style="display: none;">
        //        <input type="hidden" id="sfContactID" name="sfContactID" value=" " />
        //        <input type="hidden" id="resourceURI" name="resourceURI" value=" " />
        //        <input type="hidden" id="socialWeight" name="socialWeight" value=" " />
        //        <input type="hidden" id="fullName" name="fullName" value="" />
        //        <input type="hidden" id="firstName" name="firstName" value="" />
        //        <input type="hidden" id="lastName" name="lastName" value="" /> 
        //        <input type="hidden" id="alias" name="alias" class="alias" value="..." />
        //        <input type="hidden" id="emails" name="emails" class="emails" value="..." />
        //    </span>
        //    </li>
        // ...
        // </ul>       
        var arryCliqueMembers = new Array();
        stickerElem.find('#clique > .contact').each(
          function()
          {
              var cliqueMember = new Object();
              var metadata = $(this).find('div.metadata > span');
              var userID = metadata.attr('class');

              /*
              var resourceURI = metadata.children('.resourceURI').val();
              if (resourceURI != null && resourceURI != 'null')
              {
                  cliqueMember.resourceURI = resourceURI;
                  arryCliqueMembers[arryCliqueMembers.length] = cliqueMember;
                  return true;
              }
              */
              
              var sfContactID = metadata.children('.sfContactID').val();
              if (sfContactID != null && sfContactID != 'null')
              {
                  cliqueMember.sfContactID = sfContactID;
                  arryCliqueMembers[arryCliqueMembers.length] = cliqueMember;
                  return true;
              }

              // console.log('Found a New Friend/Contact from email');
              cliqueMember.userID = userID;
              //cliqueMember.resourceURI = 'null';
              cliqueMember.sfContactID = 'null';
              cliqueMember.fullName = metadata.children('.fullName').val();
              cliqueMember.firstName = metadata.children('.firstName').val();
              cliqueMember.lastName = metadata.children('.lastName').val();
              cliqueMember.socialWeight =  metadata.children('.socialWeight').val();

              var alias = new Array();
              metadata.find('.alias').each(
                  function() {
                      alias[alias.length] = $(this).val();
                      // console.log('Found alias '+alias[alias.length-1]);
                  }
              );
              if (alias.length > 0)
                  cliqueMember.alias = alias;

              var emails = new Array();
              metadata.find('.emails').each(
                  function() {
                      emails[emails.length] = $(this).val();
                      // console.log('Found emails '+emails[emails.length-1]);
                  }
              );
              if (emails.length > 0)
                  cliqueMember.emails = emails;

              arryCliqueMembers[arryCliqueMembers.length] = cliqueMember;
          }
        );
        stickerObj.clique = arryCliqueMembers;
        //console.log('This sticker has '+arryCliqueMembers.length+' clique members');


        // Collection Items
        var arryCollectionItems = new Array();
        stickerMetadata.find('.collection > .collection_item').each(
          function()
          {
              var collectionItem = new Object();
              collectionItem.resourceURI = $(this).val();
              arryCollectionItems[arryCollectionItems.length] = collectionItem;
          }
        );
        stickerObj.collection = arryCollectionItems;
        if (stickerMetadata.find('.collection').hasClass('modified'))
        	stickerObj.collectionModified = true;
        else
        	stickerObj.collectionModified = false;
        //console.log('This sticker has '+arryCollectionItems.length+' collection items');
        
        /*
        // Suggested stickers to be deleted because they have been merged into this
        var suggStickersToDelete = new Array();
        stickerMetadata
           .find('.merged-stickers-metadata .stickerToBeMerged').each(
                function() {
                    suggStickersToDelete[suggStickersToDelete.length] = $(this).val();
                }
        );
        stickerObj.deleteStickers = suggStickersToDelete;
        */

        return stickerObj;
    }
    
    function processSaveGroupWithCollectionResult(resultText, inputdata)
    {
        if (resultText == null)
            return processSaveGroupResult(resultText, inputdata);
         
        var resultData = JSON.parse(resultText);
        if (!resultData.success)
            return processSaveGroupResult(resultText, inputdata);

        // Process and set StickerURI
        if (resultData.stickerID == null || resultData.stickerID.length <= 0)
            return processSaveGroupResult(resultText, inputdata);

        var isSuggestedGroup = false;
        var stickerData = JSON.parse(inputdata);
        var stickerObj = $('#'+resultData.stickerID);
        if (stickerObj == null)
            return processSaveGroupResult(resultText, inputdata);
    	
    	// If successful, hide dialog and everything
    	hideAccessControlUI();
    	return processSaveGroupResult(resultText, inputdata);
    }
    
    function processSaveGroupResult(resultText, inputdata)
    {
        if (resultText == null)
           return "An error has occured. No results were returned and the social group may not have been saved correctly.";
        
        var resultData = JSON.parse(resultText);
        if (!resultData.success)
           return "An error has occured: "+resultData.message;

        // Process and set StickerURI
        if (resultData.stickerID == null || resultData.stickerID.length <= 0)
            return "An error has occured: Could not identify id of Social Group that was saved!";

        var isSuggestedGroup = false;
        var stickerData = JSON.parse(inputdata);
        var stickerObj = $('#'+resultData.stickerID);
        if (stickerObj == null)
            return "An error has occured: Could not identify within the UI the Social Group that was saved!";

        //    <div class="stickerMetadata" style="display: none;">
        //      <input type="hidden" id="stickerSfID" name="stickerSfID" value="..." />
        //      <input type="hidden" id="stickerResourceURI" name="stickerResourceURI" value="..." />
        //      <input type="hidden" id="stickerIconURI" name="stickerIconURI" value="..." />
        //      <span class="merged-stickers-metadata">
        //      </span>
        //    </div>

        var stickerMetadata = stickerObj.children('.stickerMetadata');
        //var elemURI = stickerMetadata.children('#stickerResourceURI');
        var stickerSfID = stickerMetadata.children('#stickerSfID');
        if (stickerSfID.val() == 'null')
            isSuggestedGroup = true;
        stickerSfID.val(resultData.stickerSfID);
        stickerMetadata.children('.merged-stickers-metadata').empty();
        stickerMetadata.children('.collection').removeClass('modified');

        //console.log('resultData.newContacts != null => '+(resultData.newContacts != null));
        //console.log('resultData.newContacts != undefined && resultData.newContacts.length > 0 => '+(resultData.newContacts != undefined && resultData.newContacts.length > 0));
        
        // Set SocialFlows ContactID for email contacts that were newly saved
        if (resultData.newContacts != undefined && resultData.newContacts.length > 0) {
            for (var i  = 0; i < resultData.newContacts.length; i++) 
            {
                var contact = resultData.newContacts[i];
                $("span[class='"+contact.userID+"']").each(
                    function() {
                       $(this).children(".sfContactID").val(contact.sfContactID);
                       //$(this).children(".resourceURI").val(contact.resourceURI);
                       // console.log('Set resourceURI for '+contact.userID+' to <'+contact.resourceURI+'>');
                    }
                );
            }  
        }

        if (stickerObj.hasClass("suggested")) {
        	stickerObj.switchClass( "suggested", "saved", "slow" );
        }

        // Success message
        $.facebox.close();
        //var successMsg = "<b>"+stickerData.name+"</b> successfully saved."; // resultData.message;
        return null;
    }
    
    function updateSavedGroups(resultData)
    {
        // Set SocialFlows GroupID for stickers that were newly saved
        if (resultData.savedStickers != undefined && resultData.savedStickers.length > 0) 
        {
        	for (var i  = 0; i < resultData.savedStickers.length; i++) 
            {
        		var isSuggestedGroup = false;
                var sticker = resultData.savedStickers[i];
                var stickerObj = $('#'+sticker.stickerID);
                if (stickerObj == null)
                	continue;
                
                //    <div class="stickerMetadata" style="display: none;">
                //      <input type="hidden" id="stickerSfID" name="stickerSfID" value="..." />
                //      <input type="hidden" id="stickerResourceURI" name="stickerResourceURI" value="..." />
                //      <input type="hidden" id="stickerIconURI" name="stickerIconURI" value="..." />
                //      <span class="merged-stickers-metadata">
                //      </span>
                //    </div>
                
                var stickerMetadata = stickerObj.children('.stickerMetadata');
                var stickerSfID = stickerMetadata.children('#stickerSfID');
                if (stickerSfID.val() == 'null')
                    isSuggestedGroup = true;
                stickerSfID.val(sticker.stickerSfID);
                stickerMetadata.children('.merged-stickers-metadata').empty();
                stickerMetadata.children('.collection').removeClass('modified');

                // Render saved sticker
                if (stickerObj.hasClass("suggested")) {
                	stickerObj.switchClass( "suggested", "saved", "slow" );
                }
            }
        }

        // Set SocialFlows ContactID for email contacts that were newly saved
        if (resultData.newContacts != undefined && resultData.newContacts.length > 0)
        {
            for (var i  = 0; i < resultData.newContacts.length; i++) 
            {
                var contact = resultData.newContacts[i];
                $("span[class='"+contact.userID+"']").each(
                    function() {
                       $(this).children(".sfContactID").val(contact.sfContactID);
                       //$(this).children(".resourceURI").val(contact.resourceURI);
                       // console.log('Set resourceURI for '+contact.userID+' to <'+contact.resourceURI+'>');
                    }
                );
            }
        }

    }
    


    /* Delete Group/Sticker */
    function processDeleteGroupResult(resultText, inputdata)
    {
        // console.log('\nGOT STICKER results: \n'+resultText);
        if (resultText == null)
           return "An error has occured. No results were returned and the social group may not have been deleted correctly.";
        
        var resultData = JSON.parse(resultText);
        if (!resultData.success)
           return "An error has occured: "+resultData.message;

        var stickerData = JSON.parse(inputdata);
        var stickerObj = $('#'+stickerData.stickerID);
        if (stickerObj == null)
            return "An error has occured: Could not identify within the UI the Social Group to be deleted!";

        $.facebox.close();
        renderGroupAsDeleted(stickerObj);
        //return "<b>"+stickerData.name+"</b> was successfully deleted.";
        return null;
    }
    
    
    
    /* ACCESS CONTROL UI methods */
    function showAccessControlUI(stickerElem)
    {
    	var stickerMetadata = stickerElem.children('.stickerMetadata');
        var stickerID   = stickerElem.attr('id');
        var stickerName = stickerElem.find('.sticker-title').text();
        var stickerIconSrc = stickerMetadata.find('#stickerIconURI').val();
    	
        // Set the sticker name
        var ACsticker = $("#access-control-sticker");
        ACsticker.find('#sticker-title').text(stickerName);
        
        // Set the sticker icon
        if (stickerIconSrc != null && stickerIconSrc != 'null'
        	&& stickerIconSrc.trim().length > 0 
            && stickerIconSrc != 'stickers_icons/sticker_new.png')
            ACsticker.find('.sticker_icon').attr('src', stickerIconSrc);
        else
        	ACsticker.find('.sticker_icon').attr('src', 'stickers_icons/sticker_new.png');
        
        // Set the sticker's collection of items being shared
        // <ul id="collection" style="overflow: hidden; width: 100%; margin: 0px; padding: 0px;"></ul>
        // Render the following Collection Items
        var collectionItems = new Object();
        stickerMetadata.find('.collection > .collection_item').each(
          function()
          {
              var resourceURI = $(this).val();
              collectionItems[resourceURI] = true;
          }
        );
        renderCollectionItems(ACsticker, collectionItems);
        
        // Save assigned Collection items for this group
        ACsticker.find("#save_ac").unbind('click').click(function(e) {

            // Set the sticker name
            var newStickerName = ACsticker.find('#sticker-title').text();
            if (newStickerName == null || newStickerName.trim().length <= 0 ||
            	newStickerName == 'Click to name this group' ||
            	newStickerName == 'Click to edit' || newStickerName == 'New Group')            	
            {
                jQuery.facebox('Please label this group.');
                return false;
            }
            else {
            	stickerElem.find('.sticker-title').text(newStickerName);	
            }

        	// Set the Sticker Icon
        	var newStickerIconSrc = ACsticker.find('.sticker_icon').attr('src');
        	if (newStickerIconSrc != null && newStickerIconSrc != 'null'
        	    && newStickerIconSrc.trim().length > 0 
        	    && newStickerIconSrc != 'stickers_icons/sticker_new.png')
            {
        		stickerMetadata.find('#stickerIconURI').val(newStickerIconSrc);
            }

        	// Set the Collection Items
        	var collection = stickerMetadata.find('.collection');
        	var collectionSize = 0;
      	  	collection.empty();
      	  	collection.addClass('modified');
        	ACsticker.find('#collection > .collection_item').each(
              function()
              {
            	  var metadata = $(this).find('div.metadata > span');
            	  var resourceURI = metadata.children('.resourceURI').val();
            	  var colItemRecord
            	  = '<input type="hidden" class="collection_item" value="'+resourceURI+'" />';
            	  collection.append(colItemRecord);
            	  collectionSize++;
              }
            );
        	
        	// Set the "Sharing" mark
        	var shareDataMark = stickerElem.find('.share_data');
        	if (shareDataMark.size() > 0) {
        		if (collectionSize >= 1) {
        			shareDataMark.addClass('sharing options-saved').removeClass('options-saved-hover');
        			shareDataMark.find('.share_msg').text('SHARING');
        		}
        		else {
        			shareDataMark.addClass('options-saved-hover').removeClass('sharing options-saved');
        			shareDataMark.find('.share_msg').text('SHARE');
        		}
        	}

        	// Show message and save the whole sticker
            saveSticker(stickerElem, processSaveGroupWithCollectionResult);
        	return false;
        });
        
        // Render the sharing/access control UI
        $('#access-control-sticker').css({
            top:    getPageScroll()[1] + (getPageHeight() / 10),
            left:   38
        });
        $('#overlayCover').fadeIn('fast', function() {
        	ACsticker.fadeIn('fast');
        });        
        return false;
    }
    
    function hideAccessControlUI()
    {
    	$('#collectionSelection').fadeOut();
    	$('#stickerIconSelection').fadeOut();
    	$('#access-control-sticker').fadeOut(function(){
    		$('#overlayCover').fadeOut('fast');
    	});        
        return false;
    }
    
    function addCollectionItems(stickerElem)
    {
        // $('input:checkbox');

        $('#collectionSelection input.selectItem:checked').each(
          function()
          {
            // Check to see if selected item is already in Collection
            var itemId = $(this).val();
            var collectionMember = stickerElem.find("li.collection_item div.metadata > span[class='"+itemId+"']");
            // Don't add item if already exists in current Collection
            if (collectionMember.length != 0)
               return;
            
            var colItem = $(this).parents('div.colItem');
            var colItemImg  = colItem.find('img.colItemImg').attr('src');
            var colItemUrl  = colItem.find('a.colItemUrl').attr('href');
            var colItemName = colItem.find('a.colItemUrl').text();
            var colItemCaption = colItem.find('div.colItemCaption').text();

            var htmlOutput = 
            '<li class="album_cell collection_item"> \n' +
            '    <div class="metadata" style="display: none;"> \n' +
            colItem.find("div.metadata").html() +
            '    </div> \n' +
            '    <div class="album_thumb"> \n' +
            '    <a target="_blank" href="'+colItemUrl+'" class="UIPhotoGrid_PhotoLink clearfix"><img src="'+colItemImg+'" class="UIPhotoGrid_Image"/></a> \n' +
            '    </div> \n' +
            '    <div class="album_title"><a target="_blank" href="'+colItemUrl+'">'+colItemName+'</a></div> \n' +
            '    <div class="photo_count"><div class="item_caption">'+colItemCaption+'</div><a class="fg_action_hide UIImageBlock_Ext remove_collection_item" title="Delete">X</a></div> \n' +
            '</li> \n';

            var newCollectionMember = stickerElem.find('#collection').append(htmlOutput);
          }
        );

    }  
    
    function renderCollectionItems(stickerElem, collectionItems)
    {
        // Clear old data
        stickerElem.find('#collection').empty();

    	$('#collectionSelection input.selectItem').each(
    	  function()
          {    
    		  var itemResourceURI = $(this).val();
    		  if (collectionItems[itemResourceURI])
    			  $(this).attr('checked', true);
    		  else
    			  $(this).attr('checked', false);
          }
    	);
    	
    	addCollectionItems(stickerElem);
    }
    
    
    
    /*** EXPORT TO ACCOUNT methods ***/

    function getJSONSocialFlowsTree()
    {
    	var stickerNames = new Object();
    	var stickerArray = new Object();
    	var numStickersUnnamed = 0;

    	$('div.sticker').each(function(){
    		
    		var stickerElem = $(this);
            //var stickerMetadata = stickerElem.children('.stickerMetadata');
            var stickerName = stickerElem.find('.sticker-title').text();

            if (stickerName == null || stickerName.trim().length <= 0 ||
                stickerName == 'Click to name this group' || 
                stickerName == 'Click to edit')  // || stickerName == 'New Group'
            {
               numStickersUnnamed++;
               return true;
            }

            if (stickerElem.find('#clique > .contact').length <= 1)
            {
               // skip from exporting stickers with 1 or less social contacts
               return true;
            }

            // Found at least two or more stickers with the same name
            var stickerNameLowerCase = stickerName.toLowerCase();
            if (stickerNameLowerCase in stickerNames) {
            	stickerNames[stickerNameLowerCase]++;
            	return true;
            }
            else {
            	stickerNames[stickerNameLowerCase] = 1;
            }
            
            // Save JSObject representation of sticker to be exported
            var stickerJSObj = getGroupJSObject(stickerElem);
            stickerArray[stickerName] = stickerJSObj;
    	});
    	
    	// Clear out stickers which have duplicate names
    	for (var key in stickerArray) {
    		var stickerNameLowerCase = key.toLowerCase();
    		if (stickerNames[stickerNameLowerCase] > 1) {
    			delete stickerArray[key];
    			// stickerArray[key] = null;
    		}
    		else {
    			delete stickerNames[stickerNameLowerCase];
    		}
    	}
    	
    	var socialflowsTreeInfo = new Object();
    	socialflowsTreeInfo.stickerNames = stickerNames;
    	socialflowsTreeInfo.stickerArray = stickerArray;
    	socialflowsTreeInfo.numStickersUnnamed = numStickersUnnamed;
    	
    	return socialflowsTreeInfo;
    }
        
    function determineGroupsToExport(accountType)
    {
    	if (accountType != 'Gmail')
    		accountType = 'Facebook';

        // show progress message determining which groups to export
        var loadingMsg = 'Determining Groups to Save to Your '+accountType+' Account...';
        $.facebox.settings.loadingMessage = loadingMsg;

        jQuery.facebox({ progress: 'getStatus.jsp'
             });

        setTimeout(function() {
            // Traverse SocialFlows groups/tree
            // - Determine which are named, keep count
            // - Determine which has no names, keep count
            // - Highlight which groups have the same names, can't export them
            var exportGroupsJSONInfo = getJSONSocialFlowsTree();

            // show 'Save Groups' dialog box
            var dialogBox = $($('#dialogBox').html());
            
            if (accountType == 'Facebook')
               initExportGroupsToFBDialog(accountType, dialogBox, exportGroupsJSONInfo);
            else
               initExportGroupsDialog(accountType, dialogBox, exportGroupsJSONInfo);
            
            // Show confirmation dialog box
            dialogBox.appendTo('body').fadeIn('fast');

            // Hide previous progress message
            $.facebox.close();
        }, 500);
    }

    function initExportGroupsDialog(accountType, dialogBox, exportGroupsJSONInfo)
    {
        var text = "";
        var groupsArray = new Array();

        if (exportGroupsJSONInfo.stickerArray == null ||
        	Object.size(exportGroupsJSONInfo.stickerArray) == 0) 
        {
            text 
            = "No social groups can be saved to your "+accountType+" account due to the following issues:<br/>\n" +
              "<ul style=\"list-style-type: square; padding-left: 20px;\">\n";

            var noSocialGroups = true;
            if (exportGroupsJSONInfo.numStickersUnnamed > 0) {
                noSocialGroups = false;
            	text += "<li>"+exportGroupsJSONInfo.numStickersUnnamed+" groups were unnamed</li>\n";
            }
            if (exportGroupsJSONInfo.stickerNames != null &&
            	Object.size(exportGroupsJSONInfo.stickerNames) > 0) 
            {
            	noSocialGroups = false;
                var numDups = 0;
                var dupGroupNames = "";
                var stickerNames = exportGroupsJSONInfo.stickerNames;
                for (var key in stickerNames) {
                    if (numDups > 0)
                    	dupGroupNames += ", ";
                	var dupGroupName = cnvrt2Upper(key);
                	numDups += stickerNames[key];
                	dupGroupNames += "<b>"+dupGroupName+"</b> ("+stickerNames[key]+" groups)";
                }
                
                text += "<li>"+numDups+" groups had duplicate group names: " +
                        dupGroupNames + "</li>\n";
            }
            if (noSocialGroups)
            	text += "<li>No social groups were created</li>\n";
            text += "</ul>\n<br/>\n";
        }
        else
        {
            var stickerArray = exportGroupsJSONInfo.stickerArray;
            var numStickers  = Object.size(stickerArray);
            if (numStickers > 1)
               text = "The following <b>"+numStickers+"</b> social groups will be saved to your "+accountType+" account:<br/>\n";
            else
               text = "The following social group will be saved to your "+accountType+" account:<br/>\n";
            text += "<ul style=\"list-style-type: square; padding-left: 20px;\">\n";

            /* Do a for-loop and iterate through the keys */
            for (var key in stickerArray) {
                var stickerObj = stickerArray[key];
            	text += "<li><b>"+stickerObj.name+"</b> ("
            	        +stickerObj.clique.length+" contacts)</li>\n";

            	groupsArray[groupsArray.length] = stickerObj;
            }
            text += "</ul>\n";

            if (exportGroupsJSONInfo.numStickersUnnamed > 0 ||
            	Object.size(exportGroupsJSONInfo.stickerNames) > 0)
            {
                var numDups = 0;
                var dupGroupNames = "";
                var stickerNames = exportGroupsJSONInfo.stickerNames;
            	if (stickerNames != null && Object.size(stickerNames) > 0) 
                {
                    for (var key in stickerNames) {
                        if (numDups > 0)
                            dupGroupNames += ", ";
                        var dupGroupName = cnvrt2Upper(key);
                        numDups += stickerNames[key];
                        dupGroupNames += "<b>"+dupGroupName+"</b> ("+stickerNames[key]+" groups)"; 
                    }
                }

                var nonExported = numDups + exportGroupsJSONInfo.numStickersUnnamed;
            	
            	text += "<br/>\n";
            	if (nonExported > 1)
            		text += nonExported+" social groups ";
            	else
            		text += nonExported+" social group ";
            	text += "will not be saved due to the following issues:<br/>\n" +
                        "<ul style=\"list-style-type: square; padding-left: 20px;\">\n";

                if (exportGroupsJSONInfo.numStickersUnnamed > 0) {
                	if (exportGroupsJSONInfo.numStickersUnnamed > 1)
                       text += "<li>"+exportGroupsJSONInfo.numStickersUnnamed+" groups were unnamed</li>\n";
                	else
                	   text += "<li>"+exportGroupsJSONInfo.numStickersUnnamed+" group was unnamed</li>\n";
                }
                if (numDups > 0) {
                   text += "<li>"+numDups+" groups had duplicate group names: " +
                           dupGroupNames + "</li>\n";
                }
                   
                text += "</ul>\n";
            }

            var numStickersText = "";
            if (numStickers > 1)
            	numStickersText = numStickers+" identified social groups";
            else
            	numStickersText = " identified social group";
            	
            text 
            += "<br/>" +
               "Click <b>Confirm</b> to save the "+numStickersText +
               " to your "+accountType+" account or click <b>Close</b>" +
               " to make further changes.\n"; 
        }
        
        text += "Please provide a unique group name to each social group " +
        	    "that you would like to save to "+accountType+".\n";  
        text = "<div style=\"margin-top: 8px; margin-left: 8px; margin-right: 8px;\">\n"+text+"</div>\n";


        // Setup dialog confirmation box
        var exportGroupsObject = new Object();
        exportGroupsObject.groupsToExport = groupsArray;
        exportGroupsObject.accountType    = accountType;
        if (accountType == 'Gmail')
           exportGroupsObject.gmailAuthToken = $('#gmailAuthToken').val();
        else if (accountType == 'Facebook') {
           exportGroupsObject.fb_postformid = $('#fb_postformid').val();
           exportGroupsObject.fb_dtsg       = $('#fb_dtsg').val();
        }
        
        var exportGroupsJSON
        = JSON.stringify(exportGroupsObject, null, 1);
        
    	dialogBox.find('#dialogBoxTitle').text("Save Groups to "+accountType);
    	dialogBox.find('#contentHolder').html(text);
        dialogBox.find('.close').unbind('click').click(function() {
        	dialogBox.fadeOut(function(){
        		$(this).remove();
        	});
            return false;
          });
        
        var confirmButton = dialogBox.find('#dialogBoxConfirmButton');
        if (groupsArray.length == 0) {
        	confirmButton.unbind('click').hide();
    	}
        else
        {
        	confirmButton.unbind('click').click(function() {
                // show progress message exporting groups
                var loadingMsg = 'Saving Your Groups to '+accountType+'...';
                $.facebox.settings.loadingMessage = loadingMsg;

                // Using POST method to provide info on groups to export
                if (accountType == 'Gmail') {
                	jQuery.facebox({ post: 'saveGroupsToGmail.jsp', 
                        progress: 'getStatus.jsp',
                        postdata: exportGroupsJSON, 
                        postcallback: exportGroupsToGmailResult });
                }
                else {
                	jQuery.facebox({ post: 'saveGroupsToFB.jsp', 
                        progress: 'getStatus.jsp',
                        postdata: exportGroupsJSON, 
                        postcallback: exportGroupsToFBResult });
                }
                
                dialogBox.fadeOut(function(){
            		$(this).remove();
            	});
                return false;
              });
        	confirmButton.show();
        }

        dialogBox.css({
            top:    getPageScroll()[1] + (getPageHeight() / 10),
            left:   81
        });
    }

