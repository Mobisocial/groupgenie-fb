<%@page import="java.util.*"%>
<link type="text/css" rel="stylesheet" media="screen" href="common/collection.css"/>

  <!-- Dialog Box to display info -->
  <div id="dialogBox">
  <div class="faceUI" style="display: none; z-index: 300;">
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
                <span id="dialogBoxTitle"></span>
            </h2>
            <div id="main_content_height" class="content" style="overflow-y: auto; overflow-x: hidden; max-height: 218px;">
                <div id="main_content_container" class="full" style="width: 500px;">
                    <div class="content clearfix">
                    <div class="clearfix">
                    <div id="contentHolder" class="clearfix">
                    <!-- Start of grid -->
                    Test, this is the contents!
                    <!-- End of grid -->
                    </div></div></div>
                </div>
            </div>
            <div class="info"></div>
            <div class="footer">
                <input id="dialogBoxConfirmButton" type="button" class="inputsubmit" name="Confirm" value="Confirm" style="float: left; vertical-align: top;"/>
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
  </div>

  
  
  
<style type="text/css">
img {
    border: 0 none;
}

.fbFriendListIcon {
    height: 20px;
    width: 20px;
    display: inline-block;
    background-image: url("http://static.ak.fbcdn.net/rsrc.php/zp/r/lPOQoTp3Dcs.png");
    background-position: 0pt -65px;
    background-repeat: no-repeat;
}

.saveGroupsGrid {
    width: 94%;
    padding-top: 8px;
    padding-left: 15px; /* 28px; */
}

.saveGroupsGrid thead th {
    color: #666666;
    font-weight: bold;
    padding-bottom: 5px;
    text-align: center;
    vertical-align: bottom;
}

.saveGroupsGrid tbody th {
    border-top: 1px solid #D9D9D9; /* #E5E5E5 */
    color: #666666;
    font-weight: bold;
    padding: 2px 0; /* 4px 0; */
    text-align: left;
    width: 180px;
}

.saveGroupsGrid td {
    border-top: 1px solid #D9D9D9; /* #E5E5E5 */
    color: #999999;
    font-size: 13px;
    text-align: center;
    vertical-align: middle;
    width: 80px;
}

.uiListVerticalItemBorder {
    border-width: 1px 0 0;
}

.uiListItem {
    display: block;
}

</style>