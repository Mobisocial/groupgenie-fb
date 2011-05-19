/*
 * Stickers JS
 * by Seng Keat Teh [ skteh@cs.stanford.edu ]
 * version: 1.0 (6 April 2010)
 * @requires jQuery v1.4 or later
 *
 */

    jQuery(document).ready(function($) {
        $('a[rel*=facebox]').facebox({
          loading_image : 'loading.gif',
          close_image   : 'closelabel.gif'
        }) 
      });
    

    function processSaveGroupResult(result, inputdata)
    {
    	// console.log('\nGOT STICKER results: \n'+result);  
        
    	if (result == null)
    	   return "An error has occured. No results were returned and the sticker may not have been saved correctly.";
    	else if (!result.success)
		   return "An error has occured: "+result.message;

	    // Process and set StickerURI
	    if (result.stickerID == null || result.stickerID.length <= 0)
	    	return "An error has occured: Could not identify id of Sticker that was saved!";

    	var isSuggestedGroup = false;
    	var stickerData = JSON.parse(inputdata);
        var stickerObj = $('#'+result.stickerID);
        if (stickerObj == null)
        	return "An error has occured: Could not identify within the UI the Sticker that was saved!";

        // <li id="..." class="person-box you sticker">
        //    ...
        //    <div class="stickerMetadata" style="display: none;">
        //      <input type="hidden" id="stickerResourceURI" name="stickerResourceURI" value="..." />
        //      <input type="hidden" id="stickerIconURI" name="stickerIconURI" value="..." />
        //      <span class="merged-stickers-metadata">
        //      </span>
        //    </div>

        var stickerMetadata = stickerObj.children('.stickerMetadata');
	    var elemURI = stickerMetadata.children('#stickerResourceURI');
        if (elemURI.val() == 'null')
            isSuggestedGroup = true;
        elemURI.val(result.stickerURI);
        stickerMetadata.children('.merged-stickers-metadata').empty();

        //console.log('result.newContacts != null => '+(result.newContacts != null));
        //console.log('result.newContacts != undefined && result.newContacts.length > 0 => '+(result.newContacts != undefined && result.newContacts.length > 0));
	    
	    // Set ResourceURI for email contacts that were newly saved
	    if (result.newContacts != undefined && result.newContacts.length > 0) {
            for (var i  = 0; i < result.newContacts.length; i++) 
            {
                var contact = result.newContacts[i];
                $("span[class='"+contact.userID+"']").each(
                    function() {
                       $(this).children(".resourceURI").val(contact.resourceURI);
                       // console.log('Set resourceURI for '+contact.userID+' to <'+contact.resourceURI+'>');
                    }
                );
            }  
	    }
	    
        // Move Sticker to under "Current Groups" list
        if (isSuggestedGroup) {
        	stickerObj.animate({opacity: 'hide', height: 'hide'}, 800, "swing",
                    function() {
        		      var suggStickerList = $(this).parents('ul.stickers-list');
        		      var sticker = $(this).clone(true);
        		      $(this).remove();

        		      // Hide the 'None available' message if it is shown
                      $('ul.current-groups .noGroupsMsg').hide('fast',
                    	function() {
                            $('ul.current-groups').prepend(sticker);

                            // Show the 'None available' message if needed
                            var remainingStickers = suggStickerList.children('li.sticker');
                            if (remainingStickers.length <= 0) {
                            	  suggStickerList.find('.noGroupsMsg').show('fast',
                            		function() {
                            		    // Reveal Sticker
                                        sticker.animate({opacity: 'show', height: 'show'}, 500, "swing");
                            		});
                            }
                            else {
                            	// Reveal Sticker
                                sticker.animate({opacity: 'show', height: 'show'}, 500, "swing");
                            }
                        })
                      })
                      // <span class="noGroupsMsg" style="margin-left: 50px;"><h4 class="heavy">None available.</h4></span>           
        }
        

		// Success message
		var successMsg = "Sticker <b>"+stickerData.name+"</b> successfully saved."; // result.message;
		return successMsg;
	}

    function processDeleteGroupResult(result, inputdata)
    {
        // console.log('\nGOT STICKER results: \n'+result);  
        
        if (result == null)
           return "An error has occured. No results were returned and the sticker may not have been deleted correctly.";
        else if (!result.success)
           return "An error has occured: "+result.message;

        var stickerData = JSON.parse(inputdata);
        var stickerObj = $('#'+stickerData.stickerID);
        if (stickerObj == null)
            return "An error has occured: Could not identify within the UI the Sticker to be deleted!";

        stickerObj.animate({opacity: 'hide', height: 'hide'}, 800, "swing",
                function() {
                  var stickerList = $(this).parents('ul.stickers-list');
                  $(this).remove();
                  var remainingStickers = stickerList.children('li.sticker');
                  // Show the 'None available' message
                  if (remainingStickers.length <= 0) {
                      stickerList.find('.noGroupsMsg').show('fast');
                  }
                });

        return "Sticker <b>"+stickerData.name+"</b> was successfully deleted.";
    }
    
    function addCliqueMembers(stickerElem)
    {
        //console.log('addCliqueMembers(sticker) being called...');

        $('#cliqueSelection input.selectFriend:checked').each(
          function()
          {
            // Check to see if selected friend is already in Clique
            var fbId = $(this).val();
            var cliqueMember = stickerElem.find("li.clique_member div.metadata > span[class='"+fbId+"']");
            // Don't add person if already exists in current Clique
            if (cliqueMember.length != 0)
               return;

            var fbFriend = $(this).parents('div.fbFriend');
            var fbProfileImg  = fbFriend.find('img.fbProfileImage').attr('src');
            var fbProfileUrl  = fbFriend.find('a.fbProfileUrl').attr('href');
            var fbProfileName = fbFriend.find('a.fbProfileName').text();

            var htmlOutput =
            '<li class="friend_grid_col clearfix clique_member" style="height:50px;"> \n' +
            '<div class="UIImageBlock clearfix"> \n' +
            '    <a target="_blank" href="'+fbProfileUrl+'" class="UIImageBlock_Image UIImageBlock_SMALL_Image"> \n' +
            '        <img title="'+fbProfileName+'" class="UIProfileImage UIProfileImage_LARGE" src="'+fbProfileImg+'"> \n' +
            '    </a> \n' +
            '    <div class="metadata" style="display: none;"> \n' +
            fbFriend.find("div.metadata").html() +
            '    </div> \n' +
            '    <a class="fg_action_hide UIImageBlock_Ext remove_clique_member" title="Delete">X</a> \n' +
            '    <div class="fg_links UIImageBlock_Content UIImageBlock_SMALL_Content"> \n'+
            '        <div class="fg_name"> \n' +
            '        <a target="_blank" href="'+fbProfileUrl+'">'+fbProfileName+'</a> \n' +
            '        </div> \n' +
            '        <div class="fg_action_add"></div> \n' +
            '    </div> \n' +
            '</div> \n' +
            '</li> \n';

            var newCliqueMember = stickerElem.find('#clique').append(htmlOutput);
            // Applied necesary JS abilities
            newCliqueMember.find('a.remove_clique_member').click(function() {
                // console.log('Trying to delete clique member...');
                var cliqueMemberElem = $(this).parents('li.clique_member');
                cliqueMemberElem.remove();
                return false;
            }); 
          }
        );

    }
    
    function copyAllCliqueMembers(sourceSticker, targetSticker)
    {
    	var sourceClique = sourceSticker.find('#clique');
    	var targetClique = targetSticker.find('#clique');

    	sourceClique.find('li.clique_member').each(
           function()
           {
        	   var metadata = $(this).find('div.metadata > span');
        	   var userID = metadata.attr('id');

        	   // Check to see if this friend is already in target sticker's Clique
        	   var targetCliqueMember = targetClique.find("li.clique_member div.metadata > span[id='"+userID+"']");
        	   // Don't add person if already exists in target sticker's Clique
        	   if (targetCliqueMember.length != 0)
        	      return true; // continue;

        	   $(this).clone(true).appendTo(targetClique);  
           }
        );
    }
    
    function addCollectionItems(stickerElem)
    {
        //console.log('addCliqueMembers(sticker) being called...');
        // $('input:checkbox')

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
            // Applied necesary JS abilities
            newCollectionMember.find('a.remove_collection_item').click(function() {
                // console.log('Trying to delete collection item...');
                var collectionMemberElem = $(this).parents('li.collection_item');
                collectionMemberElem.remove();
                return false;
            }); 
          }
        );

    }
    
    function copyAllCollectionItems(sourceSticker, targetSticker)
    {
        var sourceCollection = sourceSticker.find('#collection');
        var targetCollection = targetSticker.find('#collection');

        sourceCollection.find('li.collection_item').each(
           function()
           {
               var metadata = $(this).find('div.metadata > span');
               var itemId = metadata.attr('id');

               // Check to see if this item is already in target sticker's Collection
               var targetCollectionItem = targetCollection.find("li.collection_item div.metadata > span[id='"+itemId+"']");
               // Don't add item if already exists in target sticker's Collection
               if (targetCollectionItem.length != 0)
                  return true; // continue;

               $(this).clone(true).appendTo(targetCollection);  
           }
        );
    }
    
    
    
 // Same thing as jQuery(document).ready(function($) {, $(document).ready(function(){
    $(function() {
          // Edit-in-place Sticker title      
          $(".click").editable(function(value, settings) { 
              return(value);
           }, { 
              width  : "200px",
              onblur : "submit",
              style  : "inherit"
          });

          // Get sticker icon selection to work
          $(".sticker_link").click(function(e) {
              e.stopPropagation();
              e.preventDefault();
              var sticker_icon = $(this).find('.sticker_icon');

              //console.log('Sticker ID is '+stickerID+'...');
              $('#stickerSelection').css({
                  top:    getPageScroll()[1] + (getPageHeight() / 10),
                  left:   145.5,
                  visibility: "visible"
                });

              $('#stickerSelection .iconlink').unbind('click').bind('click', {stickericon:sticker_icon},
                  function(event) {
                    var iconSrc = $(this).children('.icon').attr("src");
                    event.data.stickericon.attr("src",iconSrc);
                    $('#stickerSelection').css({
                        visibility: "hidden"
                      });
                    event.preventDefault();
                    return false;
                  });
                  
                $('#stickerSelection .close').click(function() {
                    $('#stickerSelection').css({
                        visibility: "hidden"
                      });
                    return false;
                });

              return false;
          });

          
          // Get "Add New Clique Member" to work
          $(".clique_link").click(function() {
              var sticker = $(this).parents('li.sticker');

              //console.log('addCliqueMembers() being called...');

              $('#cliqueSelection input.selectFriend:checked').each(
                  function() {
                    $(this).attr('checked', false);
               });
              $('#cliqueSelection').css({
                  top:    getPageScroll()[1] + (getPageHeight() / 10),
                  left:   145.5,
                  visibility: "visible"
                });
              $('#cliqueSelection .close').click(function() {
                  $('#cliqueSelection').css({
                      visibility: "hidden"
                    });
                  return false;
                });
              $('#cliqueSelection .addToClique').unbind('click').click(function() {
                  // process the adding of new clique members
                  addCliqueMembers(sticker);
                  $('#cliqueSelection').css({
                      visibility: "hidden"
                    });
                  return false;
                });
              
              return false;
          });
          
          // Get "Delete Clique Member" to work       
          $(".remove_clique_member").live("click", function() {
              // console.log('Trying to delete clique member...');
              var cliqueMemberElem = $(this).parents('li.clique_member');
              cliqueMemberElem.remove();
              return false;
          });


          // Get "Add New Collection Item" to work
          $(".collection_link").click(function() {
              var sticker = $(this).parents('li.sticker');

              //console.log('addCollectionItem() being called...');

              $('#collectionSelection input.selectItem:checked').each(
                  function() {
                    $(this).attr('checked', false);
               });
              $('#collectionSelection').css({
                  top:    getPageScroll()[1] + (getPageHeight() / 10),
                  left:   81,
                  visibility: "visible"
                });
              $('#collectionSelection .close').click(function() {
                  $('#collectionSelection').css({
                      visibility: "hidden"
                    });
                  return false;
                });
              $('#collectionSelection .addToCollection').unbind('click').click(function() {
                  // process the adding of new collection items
                  addCollectionItems(sticker);
                  $('#collectionSelection').css({
                      visibility: "hidden"
                    });
                  return false;
                });
              
              return false;
          });

          // Get "Delete Collection Item" to work       
          $(".remove_collection_item").live("click", function() {
              // console.log('Trying to delete collection member...');
              var collectionMemberElem = $(this).parents('li.collection_item');
              collectionMemberElem.remove();
              return false;
          });


          // Get Delete Group to work
          $(".delete_group").click(function() {
              // console.log('Trying to delete group...');
              var suggestedGroupElem = $(this).parents('li.sticker');

              var stickerID   = suggestedGroupElem.attr('id');
              var stickerName = suggestedGroupElem.find('#sticker_title').text();              
              var stickerMetadata = suggestedGroupElem.children('.stickerMetadata');
              var stickerURI  = stickerMetadata.children('#stickerResourceURI').val();
              if (stickerURI == null || stickerURI == 'null' || stickerURI.trim().length <= 0)
            	  stickerURI = 'null';

              // Create a Javascript complex data object representing a Sticker
              var stickerObj = new Object();
              stickerObj.stickerURI = stickerURI;
              stickerObj.name = stickerName;
              stickerObj.stickerID = stickerID;
              var stickerJSON = JSON.stringify(stickerObj, null, 1);

              // console.log('\nJSON STICKER REPRESENTATION: \n'+stickerJSON);

              if (stickerURI == 'null') 
              {
            	 // Do quick deletion
            	 $.ajax({
            	        type:           'POST',
            	        cache:          false,
            	        url:            'deleteSticker.jsp',
            	        data:           {input: stickerJSON},
            	        // processData:    false,
            	        dataType:       'json'
            	 });
              }
              else
              {
            	 // ajax request to get an existing Sticker to be deleted at backend

            	 // TO-DO: Confirm deletion message?
                 // show message
                 
            	 // Using POST method to send sticker-editing/manipulation data
                 $.facebox.settings.loadingMessage = 'Deleting Sticker...';
                 jQuery.facebox({ post: 'deleteSticker.jsp', progress: 'getStatus.jsp',
                                  postdata: stickerJSON, postcallback: processDeleteGroupResult });

                 return false;
              }
              
              suggestedGroupElem.animate({opacity: 'hide', height: 'hide'}, 800, "swing",
                      function() {
            	        var stickerList = $(this).parents('ul.stickers-list');
            	        $(this).remove();
            	        var remainingStickers = stickerList.children('li.sticker');
            	        // Show the 'None available' message
            	        if (remainingStickers.length <= 0) {
            	            stickerList.find('.noGroupsMsg').show('fast');
                            // <span class="noGroupsMsg" style="margin-left: 50px;"><h4 class="heavy">None available.</h4></span>
            	        }
                      });
              return false;
          });


          // Get Merge Group to work
          $('.merge_group').click(function() {
        	  // console.log('Trying to merge group...');
              var targetSticker = $(this).parents('li.sticker');
              var targetStickerID = targetSticker.attr('id');
              var targetStickerMetadata = targetSticker.children('.stickerMetadata');
              var mergedStickersList = targetStickerMetadata.children('.merged-stickers-metadata');
              // console.log('Target is sticker '+targetStickerID);
              
              // Ignore merge if this target sticker was not selected
              var isSelected = targetSticker.find('li.sticker-options > input.sticker-selected:checked').length;
              if (!isSelected)
            	  return false;

              $('li.sticker-options > input.sticker-selected:checked').each(
                  function() {
                	$(this).attr('checked', false);

                	var selectedSticker = $(this).parents('li.sticker');
                	var selectedStickerID = $(this).val();
                    if (selectedStickerID == targetStickerID)
                       return true; // continue;

                    // console.log('Selected sticker '+selectedStickerID);
                    copyAllCliqueMembers(selectedSticker, targetSticker);
                    copyAllCollectionItems(selectedSticker, targetSticker);

                    var isSuggestedGroup = false;
                    var selectedStickerMetadata = selectedSticker.children('.stickerMetadata');
                    var selectedStickerURI = selectedStickerMetadata.children('#stickerResourceURI');
                    if (selectedStickerURI) {
                        if (selectedStickerURI.val() == 'null')
                            isSuggestedGroup = true;
                    }
                    else
                    	isSuggestedGroup = true;

                    // Remove selected suggested groups that were merged together into target sticker
                	if (isSuggestedGroup)
                    {
                		var mergedSticker = '<input type="hidden" class="stickerToBeMerged" value="'+selectedStickerID+'" /> \n';
                		mergedStickersList.append(mergedSticker);
                		selectedSticker.animate({opacity: 'hide', height: 'hide'}, 800, "swing",
                                function() {
                                  $(this).remove();
                        });   
                	}
                    
               });
              return false;
          });


          // Get Save Group to work
          $(".save_group").click(function() {
              var suggestedGroupElem = $(this).parents('li.sticker');
              var suggestedGroupMetadata = suggestedGroupElem.children('.stickerMetadata');
              var stickerID = suggestedGroupElem.attr('id');
              var stickerName = suggestedGroupElem.find('#sticker_title').text();
              var stickerIconSrc = suggestedGroupElem.find('.sticker_icon').attr('src');

              //    <div class="stickerMetadata" style="display: none;">
              //      <input type="hidden" id="stickerResourceURI" name="stickerResourceURI" value="..." />
              //      <input type="hidden" id="stickerIconURI" name="stickerIconURI" value="..." />
              //      <span class="merged-stickers-metadata">
              //         <input type="hidden" class="stickerToBeMerged" value="..." />
              //         ...
              //      </span>
              //    </div>
              //    ...
              // <h3 id="sticker_title" class="heavy editableTitle click">Click to name this suggested group</h3>
              
              if (stickerName == null || stickerName.trim().length <= 0 ||
                  stickerName == 'Click to name this suggested group') 
              {
                 jQuery.facebox('Please specify a suitable name for this Sticker!');
                 return false;
              }
              if (stickerIconSrc == null || stickerIconSrc.trim().length <= 0 ||
            	  stickerIconSrc == 'stickers_icons/sticker_new.png')
              {
                 jQuery.facebox('Please select a Sticker Icon!');
                 return false;
              }
              
              // Create Javascript complex data object representing a Sticker object
              var stickerObj = new Object();
              var stickerURI = suggestedGroupMetadata.children('#stickerResourceURI').val();
              if (stickerURI == null || stickerURI == 'null') {
                  stickerURI = 'null';
              }
              stickerObj.stickerURI = stickerURI;
              stickerObj.name = stickerName;
              stickerObj.stickerID = stickerID;
              stickerObj.stickerIconSrc = stickerIconSrc;
              
              var arryCliqueMembers = new Array();
              var arryCollectionItems = new Array();

              // <ul id="clique">
              //    <li class="friend_grid_col clearfix clique_member" style="height:50px;" userid=" ">
              //    <span id="metadata" class="userID" style="display: none;">
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
              
              // Clique Members
              suggestedGroupElem.find('#clique > .clique_member').each(
            	function()
            	{
            	    var cliqueMember = new Object();
            	    var metadata = $(this).find('div.metadata > span');
            	    var userID = metadata.attr('class');

            	    var resourceURI = metadata.children('.resourceURI').val();
            	    if (resourceURI != null && resourceURI != 'null')
                	{
            	    	cliqueMember.resourceURI = resourceURI;
            	    	arryCliqueMembers[arryCliqueMembers.length] = cliqueMember;
            	    	// console.log('Found Existing Friend/Contact');
            	    	return true;
            	    }

            	    // console.log('Found a New Friend/Contact from InSitu email');
            	    cliqueMember.userID = userID;
            	    cliqueMember.resourceURI = 'null';
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
              suggestedGroupElem.find('#collection > .collection_item').each(
                function()
                {
                    var collectionItem = new Object();
                    var metadata = $(this).find('div.metadata > span');
                    collectionItem.resourceURI = metadata.children('.resourceURI').val();
                    arryCollectionItems[arryCollectionItems.length] = collectionItem;
                }
              );
              stickerObj.collection = arryCollectionItems;
              //console.log('This sticker has '+arryCollectionItems.length+' collection items');
              
              // Suggested stickers to be deleted because they have been merged into this
              var suggStickersToDelete = new Array();
              suggestedGroupMetadata
                 .find('.merged-stickers-metadata .stickerToBeMerged').each(
                      function() {
                    	  suggStickersToDelete[suggStickersToDelete.length] = $(this).val();
                      }
              );
              stickerObj.deleteStickers = suggStickersToDelete;

              // STRINGIFY!
              var stickerJSON = JSON.stringify(stickerObj, null, 1);
              // console.log('\nJSON STICKER REPRESENTATION: \n'+stickerJSON);

              // USE <POST> METHOD!!!
              $.facebox.settings.loadingMessage = 'Saving Sticker...';
              jQuery.facebox({ post: 'saveSticker.jsp', progress: 'getStatus.jsp',
                               postdata: stickerJSON, postcallback: processSaveGroupResult });
              return false;
          });


    });
    
    
