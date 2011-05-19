<%@page import="java.util.*"%>
<%@page import="edu.stanford.prpl.insitu.*"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@page import="edu.stanford.socialflows.data.*"%>
<%@page import="edu.stanford.socialflows.sticker.*"%>
<%@page import="edu.stanford.socialflows.contacts.*"%>
<%@page import="edu.stanford.socialflows.util.*"%>
<link type="text/css" rel="stylesheet" media="screen" href="common/collection.css"/>

  <!-- Selection to add more Collection items -->
  <div id="collectionSelection" class="faceUI" style="display: none; z-index: 500;">
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
                <span>Add Collection Items</span>
            </h2>
    <div class="content" style="overflow-y: auto; overflow-x: hidden; height: 212px;">
    <div id="friend_guesser" class="full" style="width: 500px;">
    <div class="content clearfix">
    <div class="clearfix">
    <div id="colItemSelector" class="collection friend_grid clearfix">
    <!-- Start of grid -->
    <%
          List<BaseAlbumInfo> photoAlbums = myPhotoAlbums;

          if (photoAlbums != null)
          {
               // Create the Collection selection dialog box
               int numPhotoAlbums = photoAlbums.size();
               int albumCount = 0;
               Iterator<BaseAlbumInfo> k = photoAlbums.iterator();
               
               while (k.hasNext())
               {
            	     PrPlAlbumInfo fbAlbum = (PrPlAlbumInfo)k.next();
                     String albumId = "fbAid_"+fbAlbum.getAid();
                     String albumLink = fbAlbum.getLink();
                     String albumName = fbAlbum.getName();
                     String albumCaption = fbAlbum.getSize()+((fbAlbum.getSize() > 1)? " photos" : " photo");
                     String albumResourceURI = fbAlbum.getResourceURI();
                     
                     // find album image cover
                     String albumCoverImgUrl = fbAlbum.getAlbumCover();
                     if (albumCoverImgUrl == null || albumCoverImgUrl.equals("null"))
                         albumCoverImgUrl = "";

                     // System.out.println("Printing out photo album "+albumName+" with albumCoverUrl="+albumCoverImgUrl);

                     // Start of a row
                     if (albumCount % 3 == 0) {
                           %>
    <div class="friend_grid_row collectionRow clearfix" style="margin: 0px;">
                           <%
                     }

                     // Print out Photo Album
                     %>
    <div class="friend_grid_col colItem clearfix " style="width: 150px;">
    <div class="UIImageBlock clearfix">
        <div class="album_cell" style="padding: 12px 0px 0px;">
            <div class="metadata" style="display: none;">
                <span id="<%=albumResourceURI%>" class="<%=albumResourceURI%>">
                    <input type="hidden" class="resourceURI" name="resourceURI" value="<%=albumResourceURI%>" />
                </span>
            </div>
            <div class="album_thumb">
            <a href="<%=albumLink%>" target="_blank" class="UIPhotoGrid_PhotoLink clearfix"><img src="<%=albumCoverImgUrl%>" class="UIPhotoGrid_Image colItemImg"/></a>
            </div>
            <div class="album_title"><a href="<%=albumLink%>" target="_blank" class="colItemUrl"><%=albumName%></a></div>
            <div class="photo_count"><div class="colItemCaption item_caption"><%=albumCaption%></div><input type="checkbox" value="<%=albumResourceURI%>" class="fg_action_hide UIImageBlock_Ext selectItem"/></div>
        </div>
    </div>
    </div>

                     <%
              
                     // End of a row
                     if (!k.hasNext() || (albumCount % 3 == 2)) {
                        %>
    </div>
                        <%
                     }
                     albumCount++;
               }
          }

               %>
    <!-- End of grid -->
    </div>  
    </div></div></div></div>
                <div class="info"></div>
                <div class="footer">
                    <input type="button" class="inputsubmit addToCollection" name="save" value="Add" style="float: left; vertical-align: top;"/>
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