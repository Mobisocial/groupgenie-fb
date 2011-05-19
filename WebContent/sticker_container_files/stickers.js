/*
 *  JS file for Stickers
 */
// getPageScroll() by quirksmode.com
function getPageScroll() {
  var xScroll, yScroll;
  if (self.pageYOffset) {
    yScroll = self.pageYOffset;
    xScroll = self.pageXOffset;
  } else if (document.documentElement && document.documentElement.scrollTop) {     // Explorer 6 Strict
    yScroll = document.documentElement.scrollTop;
    xScroll = document.documentElement.scrollLeft;
  } else if (document.body) {// all other Explorers
    yScroll = document.body.scrollTop;
    xScroll = document.body.scrollLeft;   
  }
  return new Array(xScroll,yScroll) 
}

// Adapted from getPageSize() by quirksmode.com
function getPageHeight() {
  var windowHeight
  if (self.innerHeight) { // all except Explorer
    windowHeight = self.innerHeight;
  } else if (document.documentElement && document.documentElement.clientHeight) { // Explorer 6 Strict Mode
    windowHeight = document.documentElement.clientHeight;
  } else if (document.body) { // other Explorers
    windowHeight = document.body.clientHeight;
  }   
  return windowHeight
}

function addCliqueMembers(stickerElem)
{
    //console.log('addCliqueMembers(sticker) being called...');
    // $('input:checkbox')

    $('.cliqueSelection input.selectFriend:checked').each(
      function()
      {
        // Check to see if selected friend is already in Clique
        var fbId = $(this).val();
        var cliqueMember = stickerElem.find("li.clique_member[userid='"+fbId+"']");
        // Don't add person if already exists in current Clique
        if (cliqueMember.length != 0)
           return;
        
        var fbFriend = $(this).parents('div.fbFriend');
        var fbProfileImg  = fbFriend.find('img.fbProfileImage').attr('src');
        var fbProfileUrl  = fbFriend.find('a.fbProfileUrl').attr('href');
        var fbProfileName = fbFriend.find('a.fbProfileName').text();

        var htmlOutput =
        '<li class="friend_grid_col clearfix clique_member" userid="'+fbId+'"> \n' +
        '<div class="UIImageBlock clearfix"> \n' +
        '    <a target="_blank" href="'+fbProfileUrl+'" class="UIImageBlock_Image UIImageBlock_SMALL_Image"> \n' +
        '        <img alt="'+fbProfileName+'" class="UIProfileImage UIProfileImage_LARGE" src="'+fbProfileImg+'"> \n' +
        '    </a> \n' +
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


    $(function() {
    	  // Edit-in-place Sticker title	  
    	  $(".click").editable(function(value, settings) { 
              return(value);
           }, { 
    		  onblur : "submit",
    	      style  : "inherit"
    	  });

    	  // Get sticker icon selection to work
    	  $(".sticker_link").click(function(e) {
    		  e.stopPropagation();
    		  e.preventDefault();
    		  var sticker_icon = $(this).find('.sticker_icon');

    		  //console.log('Sticker ID is '+stickerID+'...');
              $('.stickerSelection').css({
                  top:    getPageScroll()[1] + (getPageHeight() / 10),
                  left:   145.5,
                  visibility: "visible"
                });

              $('.stickerSelection .iconlink').unbind('click').bind('click', {stickericon:sticker_icon},
                  function(event) {
    		    	var iconSrc = $(this).children('.icon').attr("src");
    		    	event.data.stickericon.attr("src",iconSrc);     
    		        $('.stickerSelection').css({
    		            visibility: "hidden"
    		          });
  		            event.preventDefault();
    		        return false;
    		      });
    		      
    		    $('.stickerSelection .close').click(function() {
    		        $('.stickerSelection').css({
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

              $('.cliqueSelection input.selectFriend:checked').each(
            	  function() {
            		$(this).attr('checked', false);
               });
              $('.cliqueSelection').css({
                  top:    getPageScroll()[1] + (getPageHeight() / 10),
                  left:   145.5,
                  visibility: "visible"
                });
              $('.cliqueSelection .close').click(function() {
                  $('.cliqueSelection').css({
                      visibility: "hidden"
                    });
                  return false;
                });
              $('.cliqueSelection .addToClique').unbind('click').click(function() {
                  // process the adding of new clique members
                  addCliqueMembers(sticker);                 
                  $('.cliqueSelection').css({
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

    	  // Get Delete Suggested Group to work
    	  $(".delete_suggested_group").click(function() {
              // console.log('Trying to delete suggested group...');
              var suggestedGroupElem = $(this).parents('li.sticker');
              suggestedGroupElem.remove();
              return false;
          });
    	  
          
    });


