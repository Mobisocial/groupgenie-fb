<%@page import="java.util.Iterator"%>
<%@page import="edu.stanford.socialflows.sticker.*"%>
<%@page import="edu.stanford.socialflows.sticker.StickerIcons.StickerIcon"%>
  <!-- Selection to choose Sticker icon -->
  <div id="stickerIconSelection" class="faceUI" style="display: none; z-index: 500;">
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
                <span>Select Sticker Icon</span>
            </h2>
    <div class="content" style="overflow-y: auto; overflow-x: hidden; height: 195px;">
    <div id="friend_guesser" class="full" style="width: 420px;">
    <div class="content clearfix">
    <div class="clearfix">
    <div class="friend_grid clearfix">
    
    <!-- Start of grid -->
<%
    //Create the Sticker Icon selection dialog box
    Iterator<StickerIcon> stickerCol = StickerIcons.getStickerCollection();
    int numStickers = StickerIcons.getStickerCollectionSize();
    int icon_count = 0;

    while (stickerCol.hasNext())
    {
        StickerIcon stickerIcon = stickerCol.next();
        String iconURL = stickerIcon.getIconURL();
        String iconName = stickerIcon.getCaption();
        
        // Start of a row
        if (icon_count % 5 == 0) {
            if (icon_count+5 >= numStickers) {
%>
    <!-- Start of row -->
    <div class="friend_grid_row clearfix" style="margin-top: 5px;">
                <%
            }
            else {
                %>
    <!-- Start of row -->
    <div class="friend_grid_row last clearfix" style="margin-top: 5px;">
                <%
            }
        }

        // Print out sticker icon
        %>
    <div class="friend_grid_col clearfix" id="" style="width: 68px;">
    <div class="UIImageBlock clearfix">
        <a class="UIImageBlock_Image UIImageBlock_SMALL_Image iconlink" >
            <img title="<%=iconName%>" class="UIProfileImage UIProfileImage_LARGE square icon" src="<%=iconURL%>" style="height: 50px; width: 50px;">
        </a>
    </div>
    </div>

        <%      

        // End of a row
        if (!stickerCol.hasNext() || (icon_count % 5 == 4)) {
            %>
    </div>
            <%
        }
        icon_count++;
    }

%>
    </div></div></div></div>
                </div>
                <div class="info"></div>
                <div class="footer">
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