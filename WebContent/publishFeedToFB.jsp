<%@include file="sessionCheck.jsp"%>
<%
    // Publicize about SocialFlows on user's feed when the user 
    // accesses the app, publish only if its been 30 days since 
    // the user last used the app
    String wallPostMsg
    = "Checkout http://apps.facebook.com/socialflows, "+
      "it's a facebook app that helps you discover your "+
      "different groups of friends by analyzing your tagged photos.";
    
    // it's a facebook app that shows you your most important 
    // groups of friends by looking at your tagged photos. 
    /*
      "Checkout http://apps.facebook.com/socialflows, it's a facebook app that helps create your "+
      "facebook groups very easily by analyzing your tagged photos. "+
      "I've just used it to create several of my groups.";
     */
    
    // Check when user last used SocialFlows
    Date lastUpdate = dbconnManager.getCurrentUserLastUpdateTime();
    System.out.println(request.getServletPath()+": Last saved time of user: "+lastUpdate);
    
    boolean doUpdate = false;
    long daysThreshold = 30;
    if (lastUpdate == null || lastUpdate.getTime() == 0) {
        doUpdate = true;
    }
    else if (lastUpdate.getTime() > 0) {
        long diffInDays 
        = new Date().getTime() - lastUpdate.getTime();
        diffInDays = (diffInDays / (1000 * 60 * 60 * 24));

        System.out.println(request.getServletPath()+": Difference in days = "+diffInDays);
        if (diffInDays >= daysThreshold) {
            doUpdate = true;
        }
    }
    
    if (doUpdate) {
        dbconnManager.setCurrentUserLastUpdateTime();
        fbs.publishToFeed(wallPostMsg);
        System.out.println(request.getServletPath()+": Publish about SocialFlows to user's wall!");
    }
    else {
    	System.out.println(request.getServletPath()+": Already published to user's wall in the last 30 days.");
    }

%>