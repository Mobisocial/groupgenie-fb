<%@page import="java.util.*"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="edu.stanford.prpl.insitu.*"%>
<%@page import="edu.stanford.prpl.insitu.util.*"%>
<%@page import="edu.stanford.socialflows.connector.*"%>
<%@page import="edu.stanford.prpl.insitu.settings.*"%>
<%@page import="edu.stanford.socialflows.data.*"%>
<%@page import="edu.stanford.socialflows.sticker.*"%>
<%@page import="edu.stanford.socialflows.contacts.*"%>
<%@page import="edu.stanford.socialflows.util.*"%>
<!-- Selection to add more Clique members -->
<div id="cliqueSelection" class="faceUI" style="display: none;">
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
                <span>Add Social Contacts</span>
            </h2>
    <div class="content" style="overflow-y: auto; overflow-x: hidden; height: 188px;">
    <div id="friend_guesser" class="full" style="width: 460px;">
    <div class="content clearfix">
    <div class="clearfix">
    <div class="friend_grid clearfix">
    <!-- Start of grid -->
    <%
        List<SocialFlowsContactInfo> friends = myFriends;
    
        // Sort contacts/friends/persons by alphabetical order
        Collections.sort(friends);
    
        // Create the Clique selection dialog box
        int numCombinedFriends = friends.size();
    
        //System.out.println("Displaying "+numFBFriends+" fb friends ");
    
        int count = 0;
        Iterator<SocialFlowsContactInfo> j = friends.iterator();
        while (j.hasNext())
        {
        	SocialFlowsContactInfo cFriend = j.next();
            String friendName = cFriend.getName();
            String friendTitle = friendName;
            String displayName = friendName;
            // Special processing for email-based names
            if (InSituUtils.isEmailAddress(friendName)) {
                displayName = friendName.replace("@", "<br/>@");
            }
            else {
            	// Append email if available
                Set<String> emails = cFriend.getEmailAddresses();
                if (emails != null) {
                   for (String email : emails) {
                       friendTitle += " <"+email+">";
                       break;
                   }
                }
            }

            String friendProfileUrl = cFriend.getProfileUrl();
            if (friendProfileUrl == null)
                friendProfileUrl = "#";
            String friendUserId = cFriend.getTempContactId();
            if (cFriend.getContactId() > 0)
            	friendUserId = String.valueOf(cFriend.getContactId());
            try {
                friendUserId = URLEncoder.encode(friendUserId, "UTF-8");
                friendUserId = friendUserId.replaceAll("[%$.+!*'(),]", "_");
            }
            catch (Exception e) { }
            String profileImgUrl = cFriend.getPicSquare();
            if (profileImgUrl == null || profileImgUrl.trim().length() <= 0 || profileImgUrl.trim().equals("null"))
                profileImgUrl = "common/contact_placeholder.gif";
            
            // System.out.println("Printing out friend "+friendName+" with profilePicUrl="+profileImgUrl);

            // Start of a row
            if (count % 3 == 0) {
                if (count+3 >= numCombinedFriends) {
    %>
    <div class="friend_grid_row last clearfix" style="margin: 10px -10px 5px;">
    <%
                }
                else {
    %>
    <div class="friend_grid_row clearfix" style="margin: 10px -10px 5px;">
    <%
                }
            }
                                         
            
            String friendSfContactId = null;
            if (cFriend.getContactId() > 0) {
            	friendSfContactId = String.valueOf(cFriend.getContactId());            
            }
            
            // Print out picture of social contact
    %>
    <div class="friend_grid_col clearfix fbFriend" style="width: 138px; height: 50px;">
    <div class="UIImageBlock clearfix">
        <a href="#" class="socialContactProfilePhoto UIImageBlock_Image UIImageBlock_SMALL_Image fbProfileUrl">
            <img title="<%=friendTitle%>" class="UIProfileImage UIProfileImage_LARGE fbProfileImage" src="<%=profileImgUrl%>" style="width: 50px; height: 50px;" />
        </a>
        <input type="checkbox" value="<%=friendUserId%>" class="fg_action_hide UIImageBlock_Ext selectFriend"/>
        <div class="metadata" style="display: none;">
        <span id="<%=friendUserId%>" class="<%=friendUserId%>">
            <input type="hidden" class="sfContactID" name="sfContactID" value="<%=friendSfContactId%>" />
            <input type="hidden" class="resourceURI" name="resourceURI" value="null" />
            <input type="hidden" class="socialWeight" name="socialWeight" value="<%=cFriend.getSocialWeight()%>" />  <%
            
            // New InSitu contact not yet saved into PrPl
            // Needs: *Fullname, *Firstname, *Lastname, *Social Weight, *Aliases, *Emails 
            if (cFriend.getContactId() <= 0)
            {
    %>  
            <input type="hidden" class="fullName" name="fullName" value="<%=cFriend.getName()%>" />
            <input type="hidden" class="firstName" name="firstName" value="<%=cFriend.getFirstName()%>" />
            <input type="hidden" class="lastName" name="lastName" value="<%=cFriend.getLastName()%>" />  <%
            
               // Record the aliases
               Set<String> names = cFriend.getNames();
               if (names != null) {
                  Iterator<String> namesIter = names.iterator();
                  while (namesIter.hasNext()) {
                      String name = namesIter.next();
    %>  
            <input type="hidden" class="alias" name="alias" value="<%=name%>" />            <%
                  }
               }
                   
               // Record the emails
               Set<String> emails = cFriend.getEmailAddresses();
               if (emails != null) {
                  Iterator<String> emailsIter = emails.iterator();
                  while (emailsIter.hasNext()) {
                      String email = emailsIter.next();
    %>  
            <input type="hidden" class="emails" name="emails" value="<%=email%>" />         <%
                  }
               }
               
            }
    %>
        </span>
        </div>
        <div class="fg_links UIImageBlock_Content UIImageBlock_SMALL_Content">
            <div class="fg_name">
            <a href="#" class="socialContactDisplayName fbDisplayName"><%=displayName%></a>
            </div>
        </div>
    </div>
    </div>

    <%
            // End of a row
            if (!j.hasNext() || (count % 3 == 2)) {
                %>
    </div>
                <%
            }
            count++;
        }

    %>
    </div></div></div></div>
                </div>
                <div class="info"></div>
                <div class="footer">
                    <input type="button" class="inputsubmit addToClique" name="save" value="Add" style="float: left; vertical-align: top;"/>
                    <a href="#" class="close"><img src="facebox/closelabel.gif" title="close" class="close_image" /></a>          
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