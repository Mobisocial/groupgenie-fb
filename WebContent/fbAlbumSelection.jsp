<%@page import="java.util.*"%>
<%@page import="edu.stanford.socialflows.data.BaseAlbumInfo"%>
<%@page import="edu.stanford.socialflows.data.BasePhotoInfo"%>
<link type="text/css" rel="stylesheet" media="screen" href="common/collection.css"/>
<%
          String albumSelectionClass = "";
          List<BaseAlbumInfo> fbPhotoAlbums
          = photoBook.getPhotoAlbumsList();
          if (fbPhotoAlbums == null || fbPhotoAlbums.size() <= 0) 
        	  albumSelectionClass = "empty";
%>
  <!-- Selection to add more Collection items -->
  <div id="fbAlbumSelection" class="faceUI <%=albumSelectionClass%>" style="display: none; z-index: 500;">
    <div class="popup">
    <table>
        <tbody>
        <tr>
            <td class="tl"></td>
            <td class="b"></td>
            <td class="tr"></td>
        </tr>
        <tr>
            <td class="b"></td>
            <td class="body">
            <h2 class="dialog_title">
                <span id="fbAlbumSelectionTitle"></span>
            </h2>
    <div class="content" style="overflow-y: auto; overflow-x: hidden; height: 212px;">
    <div id="friend_guesser" class="full" style="width: 500px;">
    <div class="content clearfix">
    <div class="clearfix">
    <div id="colItemSelector" class="collection friend_grid clearfix">
    <!-- Start of grid -->
    <%
    
          if (fbPhotoAlbums != null) 
          {
               // Create the Collection selection dialog box
               int numPhotoAlbums = fbPhotoAlbums.size();
               //System.out.println("Displaying "+numPhotoAlbums+" photo albums ");
               
               int count = 0;
               Iterator<BaseAlbumInfo> k = fbPhotoAlbums.iterator();
               while (k.hasNext())
               {
                     BaseAlbumInfo fbAlbum = (BaseAlbumInfo)k.next();
                     String albumId = "fbAid_"+fbAlbum.getAid();
                     String albumLink = fbAlbum.getLink();
                     String albumName = fbAlbum.getName();
                     String albumCaption = fbAlbum.getSize()+((fbAlbum.getSize() > 1)? " photos" : " photo");
                     //String albumResourceURI = fbAlbum.getResourceURI();
                     
                     // get URL of album image cover
                     String albumCoverImgUrl = null;
                     String imgUrl    = fbAlbum.getAlbumCover();
                     String imgBigUrl = fbAlbum.getAlbumCoverBig();
                     String imgSmlUrl = fbAlbum.getAlbumCoverSmall();
                     
                     if (imgUrl != null && !imgUrl.equals("null") 
                         && imgUrl.trim().length() > 0)
                         albumCoverImgUrl = imgUrl;
                     else if (imgBigUrl != null && !imgBigUrl.equals("null") 
                              && imgBigUrl.trim().length() > 0)
                         albumCoverImgUrl = imgBigUrl;
                     else
                         albumCoverImgUrl = imgSmlUrl; 
                     
                     if (albumCoverImgUrl == null || albumCoverImgUrl.equals("null")
                         || albumCoverImgUrl.trim().length() <= 0)
                         albumCoverImgUrl = "";

                     
                     // Start of a row
                     if (count % 3 == 0) {
                           %>
    <div class="friend_grid_row collectionRow clearfix" style="margin: 0px;">
                           <%
                     }

                     // Print out Photo Album
                     %>
    <div class="friend_grid_col colItem clearfix" style="width: 150px;">
    <div class="UIImageBlock clearfix">
        <div class="album_cell" style="padding: 12px 0px 0px;">
            <div class="metadata" style="display: none;">
                <span id="<%=albumId%>" class="<%=albumId%>">
                    <input type="hidden" class="fbAid" name="fbAid" value="<%=fbAlbum.getAid()%>" />
                </span>
            </div>
            <div class="album_thumb">
            <a href="<%=albumLink%>" target="_blank" class="UIPhotoGrid_PhotoLink clearfix"><img src="<%=albumCoverImgUrl%>" class="UIPhotoGrid_Image colItemImg" style="max-height:130px; max-width:130px;"/></a>
            </div>
            <div class="album_title"><a href="<%=albumLink%>" target="_blank" class="colItemUrl"><%=albumName%></a></div>
            <div class="photo_count"><div class="colItemCaption item_caption"><%=albumCaption%></div><input type="checkbox" value="<%=fbAlbum.getAid()%>" class="fg_action_hide UIImageBlock_Ext selectItem"/></div>
        </div>
    </div>
    </div>

                     <%
              
                     // End of a row
                     if (!k.hasNext() || (count % 3 == 2)) {
                        %>
    </div>
                        <%
                     }
                     count++;
               }
          }

               %>
    <!-- End of grid -->
    </div>  
    </div></div></div></div>
                <div class="info"></div>
                <div class="footer">
                    <input id="fbAlbumSelectionOKButton" type="button" class="inputsubmit extractSelectedAlbums" name="save" value="" style="float: left; vertical-align: top;"/>
                    <a href="#" class="close"><img src="facebox/closelabel.gif" title="close" class="close_image"></a>          
                </div>            
            </td>            
            <td class="b"></td>           
        </tr>          
        <tr>            
            <td class="bl"></td><td class="b"></td><td class="br"></td>           
        </tr>         
        </tbody>
    </table>     
    </div>   
  </div>