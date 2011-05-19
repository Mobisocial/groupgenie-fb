LOG = true; // needs to be cleaned up, both log and LOG are also in jog.js
function log(mesg)
{
//	alert ('logging ' + mesg);
	if (!(typeof console == 'undefined') && (LOG == true))
		console.log(mesg);
}

/* jquery.js */
function inc(filename)
{
	var body = document.getElementsByTagName('body').item(0);
	script = document.createElement('script');
	script.src = filename;
	script.type = 'text/javascript';
	body.appendChild(script)
}


//tofix: replace with assoc. arrays @fixed

//JSONscriptRequest -- a simple class for accessing  Web Services
//using dynamically generated script tags and JSON
//Constructor -- pass a REST request URL to the constructor
function JSONscriptRequest(fullUrl) {
	// REST request path
	this.fullUrl = fullUrl;
	// Keep IE from caching requests
	this.noCacheIE = '?noCacheIE=' + (new Date()).getTime();
	// Get the DOM location to put the script tag
	this.headLoc = document.getElementsByTagName("head").item(0);
	// Generate a unique script tag id
	this.scriptId = 'YJscriptId' + JSONscriptRequest.scriptCounter++;
}

//highly critical function!
function publish_facebook_groups(postformid,fbdtsgval,ownerid,groupsAsJson)
{
	var user_name = [];
	var user_id = [];
	var user_attrib=[];
	callAjax_get_friends(postformid,fbdtsgval,ownerid,user_attrib);

	var existinggroups=[];
	callAjax_get_groups(existinggroups);

	var listsCreated = 0;

	var groupnumber=0;
	for (var groupiterator=0;groupiterator<groupsAsJson.groups.length ;groupiterator++ )
	{
		var member_ids=[];
		var countofmembers=0;
		//@d2 fixed bug related to user_id1 need to fix variable names!

		for (var memberiterator=0;memberiterator < groupsAsJson.groups[groupiterator].members.length ;memberiterator++)
		{
			//blank names crashed the script!
			var indexpos=-99;

			for(var iteratenames=0; iteratenames < groupsAsJson.groups[groupiterator].members[memberiterator].names.length;iteratenames++)
			{
				if(groupsAsJson.groups[groupiterator].members[memberiterator].names[iteratenames].length>0)
				{
					//indexpos= binarySearch(user_name, groupsAsJson.groups[groupiterator].members[memberiterator].names[iteratenames]);
					var user_attrib_name=groupsAsJson.groups[groupiterator].members[memberiterator].names[iteratenames];
					if(user_attrib[user_attrib_name])
					{
						log("name is " + user_attrib_name + "user id is " + user_attrib[user_attrib_name]);	
						member_ids[countofmembers++]=user_attrib[user_attrib_name];
					}
					else
					{
						document.getElementById('light').innerHTML = "Could not find user name: "+ user_attrib_name;
					}
						
				}
			}


		}

		if (countofmembers>=2)
		{
			var list_id = callAjax_Get_New_Listid(postformid,fbdtsgval,ownerid);
			groupnumber++;
			var new_group_id = existinggroups[0] + groupnumber;
			// tofix: use actual group name if defined, not always group__
			var group_name = 'group__' + new_group_id;
			callAjax_Push_to_Facebook(group_name, member_ids, countofmembers, postformid, fbdtsgval, ownerid, list_id);//create the list here

			//removing the "" from the list id as it is not working for me!
			callAjax_Push_to_Facebook(group_name, member_ids, countofmembers, postformid, fbdtsgval, ownerid,list_id.slice(1, list_id.length-1));
			listsCreated++;
		    //update the pane
			document.getElementById('light').innerHTML = "creating lists now...";
		}
	}

	log (listsCreated + ' lists created');
	// now redirect to friends page so user can see the groups
	window.location = 'http://www.facebook.com/friends/edit/';
}

//Static script ID counter
JSONscriptRequest.scriptCounter = 1;

JSONscriptRequest.prototype.buildScriptTag = function () {

	// Create the script tag
	this.scriptObj = document.createElement("script");

	// Add script object attributes
	this.scriptObj.setAttribute("type", "text/javascript");
	this.scriptObj.setAttribute("src", this.fullUrl + this.noCacheIE);
	// TOFIX: why have script id and counter etc ?
	this.scriptObj.setAttribute("id", this.scriptId);
}

JSONscriptRequest.prototype.removeScriptTag = function () {
	// Destroy the script tag
	this.headLoc.removeChild(this.scriptObj);
}

JSONscriptRequest.prototype.addScriptTag = function () {
	// Create the script tag
	this.headLoc.appendChild(this.scriptObj);
}

function addScript(url)
{

     log ('injecting script to read groups from url ' + url);
	//update the pane
	document.getElementById('light').innerHTML = "Getting data from muse now...";
	var obj=new JSONscriptRequest(url);
	obj.buildScriptTag(); // Build the script tag
	obj.addScriptTag(); // Execute (add) the script tag
	// run after a small delay to let getGroupsAsJson.jsp be defined
	setTimeout('waitForJsonForGroups(0)', 1000);
}

function waitForJsonForGroups(count)
{
	if (typeof jsonForGroups == 'undefined')
	{
		if (count < 10) // wait for up to 10 secs
		{
			log ('waiting, count = ' + count);
			setTimeout ('waitForJsonForGroups(' + (count+1) + ')', 1000);
		}
		else
			alert ('Sorry, unable to get groups (Muse may not be running ?)');
	}
	else
	{
		log ('got json for groups');
		//update the pane
		document.getElementById('light').innerHTML = "Got data from muse...";
		// TOFIX: check if window.Env is defined, if not we're not running on fb page, popup a warning and exit
		var user_id=window.Env.user;
		var postformid = window.Env.post_form_id;
		var fb_dtsg = window.Env.fb_dtsg;
		log ('posting groups: user id = ' + user_id + ' post_form_id = ' + postformid + ' fb_dtsg = ' + fb_dtsg);
		try {
			publish_facebook_groups(postformid, fb_dtsg, user_id, jsonForGroups);
		} catch (e) {
			log ('exception: ' + e.toString());
			alert ('exception: ' + e.toString());
		}
	}
}


//function that parses list id given a post_form_if and fbdtsg value
function callAjax_Get_New_Listid(postformid,fbdtsgval,user_id) {

	var invocation = new XMLHttpRequest();
	var url="http://www.facebook.com/friends/ajax/edit_list.php?new_list=1&__a=1";
	var queryString = "new_list=1&__d=1&post_form_id=" + postformid + "&fb_dtsg=" + fbdtsgval + "&lsd&post_form_id_source=AsyncRequest";
	if(invocation)
	{
		invocation.open("POST",url,false);
		log ('getting new list id from ' + url);
		invocation.withCredentials = "true";
		////alert(queryString);
		invocation.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		invocation.send(queryString);
		log ('status = ' + invocation.status);

		// tofix: what's the right way to parse the response JS ?
		var x = invocation.responseText.split("{");
		for (var i=0;i<x.length ;i++ )
		{
			if(x[i].indexOf("new_list_id")!=-1)
			{
				var y= x[i].split(":");
				var z= y[1].split("}");
				var templist_id = z[0].replace( /^s*/, "" ); //Function to trim the space in the left side of the string
				var length= templist_id.length;
				var id = templist_id; // templist_id.slice(1, length-1);
				return id;
			}
		}
	}
	return;
}

//tofix: returns names of existing groups so we can compare them with our names and integrate smoothly with existing groups
function callAjax_get_groups(existinggroups) {

	// tofix: factor out xmlhttprequest and handle IE
	var invocation = new XMLHttpRequest();
	var url="http://www.facebook.com/friends/edit/ajax/list_memberships.php?__a=1";
	var friendcount=0;
	if(invocation)
	{
		invocation.open("GET",url,false);
		log ('getting existing groups from ' + url);

		invocation.withCredentials = "true";
		invocation.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		invocation.send(null);
		log ('status = ' + invocation.status);

		// find # of groups
		//	,"listNames":{"10150297067160564":"group1","10150296825455564":"four","10150295923215564":"three","10150295581715564":"two","10150295577520564":"one","10150293848140564":"mytested","10150289848140564":"my"}},
		var x = invocation.responseText.split(",");
		for (var i=0;i<x.length ;i++ )
		{
			if(x[i].indexOf("listNames")!=-1)
			{
				var count_group=0;
				while(x[i+count_group].indexOf("}")==-1)
					count_group++;
				count_group++;
				existinggroups[0]=count_group;
				log (count_group + ' existing groups');
				//	alert("existing groups are :"+ count_group);
				break;

			}
		}
		// existing groups[0] = 12 if there were 12 existing groups
	}
}

//TOFIX: confusing user_id1 and user_id ?! @Fixed - replaced with ownerid

function callAjax_get_friends(postformid,fbdtsgval,ownerid,user_attrib) {

	//----- this is the first page of the popup subsequently we need to make post requests and not gets!
	//update the pane
	document.getElementById('light').innerHTML = "getting friends now...";
	var invocation = new XMLHttpRequest();
	var url="http://www.facebook.com/ajax/social_graph/dialog/popup.php?id=" + ownerid + "&__a=1&__d=1";
	var nametoken;
	var friendcount=0;
	if(invocation)
	{
		invocation.open("GET",url,false);
		invocation.withCredentials = "true";

		invocation.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		invocation.send(null);
		// tofix: proper handling of response
		var x = invocation.responseText.split(",");
		for (var i=0;i<x.length ;i++ )
		{
			if(x[i].indexOf("alternate_title")!=-1)
			{	var y= x[i-1].split(":");
			var length= y[1].length;

			var z= x[i-2].split(":");
			var facebookmemberid=parseInt(z[2]);

			if (z[2].indexOf("id")!=-1)
			{
				nametoken=y[1].slice(2, length-2);
				user_attrib[nametoken]= z[3];
				friendcount++;
			}
			else
			{
				nametoken=y[1].slice(2, length-2);
				user_attrib[nametoken]= z[2];
				friendcount++;
			}



			}
		}
	}
	var elementcounter=0;
//	----- posts should start here

	// TOFIX: why 30 ? there must be a better way to get friends (e.g. from create new list) than making N/100 calls
	// and depending on fb not changing the # per page
	for (var pagecount=1;pagecount<30 ;pagecount++ )
	{
		var invocation_post = new XMLHttpRequest();

		var url_post="http://www.facebook.com/ajax/social_graph/fetch.php?__a=1";
		var post_params= "edge_type=everyone&page="+ pagecount+ "&limit=100&node_id=" + ownerid + "&class=FriendManager&post_form_id=" +postformid + "&fb_dtsg=" +fbdtsgval + "&lsd&post_form_id_source=AsyncRequest";

		// tofix: rename friendscounter vs friendcount -- confusing
		if (invocation_post)
		{
			log ('reading friends from: ' + url_post);
			invocation_post.open("POST",url_post,false);
			invocation_post.withCredentials = "true";
			invocation_post.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			invocation_post.send(post_params);
			log ('xhr status = ' + invocation_post.status);
			var x = invocation_post.responseText.split(",");
			for (var i=0;i<x.length ;i++ )
			{
				if(x[i].indexOf("alternate_title")!=-1)
				{
					var y= x[i-1].split(":");
					var length= y[1].length;

					var z= x[i-2].split(":");

					if (z[2].indexOf("id")!=-1)
					{
						nametoken=y[1].slice(1, length-1);
						user_attrib[nametoken]= z[3];
						friendcount++;
					}
					else
					{
						nametoken=y[1].slice(1, length-1);
						user_attrib[nametoken]= z[2];
						friendcount++;

					}
					elementcounter++;
				}
			}

		}
		log ('page has ' + elementcounter + ' friends');

		if (elementcounter<100)
		{
			// must be last page
			break;
		}
		elementcounter=0;
	}

	log ('#friends = ' + friendcount);
	return;
}

//function that creates a list
function callAjax_Push_to_Facebook(group_name, member_ids, countofmembers, postformid, fbdtsgval, user_id, list_id) {

	// tofix: what if group_name exists ? we should just merge members who dont already exist
	log ('creating list with list_id ' + list_id + ' #users = ' + countofmembers);
	document.getElementById('light').innerHTML = 'creating list with list_id ' + list_id + ' #users = ' + countofmembers;
	var url="http://www.facebook.com/friends/ajax/superfriends_add.php?__a=1";
	var queryString = "action=create";
	for (var temp=0;temp<countofmembers ;temp++ )
	{
		queryString=queryString+"&members["+temp+"]="+ member_ids [temp];
	}
	queryString=queryString+ "&list_id=" + list_id;
	queryString=queryString+ "&name=" + group_name;
	queryString=queryString+ "&redirect=false&post_form_id="+ postformid+ "&fb_dtsg=" + fbdtsgval + "&lsd&post_form_id_source=AsyncRequest";

	// TOFIX: factor out new xhr creation
	var invocation = new XMLHttpRequest();
	if (invocation)
	{
		invocation.open("POST",url,false);
		invocation.withCredentials = "true";
		invocation.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		log ('sending post to url: ' + url + ' params=' + queryString);
		invocation.send(queryString);
		// tofix: handle errors gracefully
		log ('status = ' + invocation.status);
	}
	return;
}

function mycssmagic()
{
	var div = document.createElement('div');
	div.id = 'magic_display';

	var str = '';
	str += '<div id="light" class="white_content">This is the lightbox content. <a href = "javascript:void(0)" onclick = "document.getElementById';
	str += '("light").style.display="none";document.getElementById("fade").style.display="none" ">Close</a></div>';

	str += '<div id="fade" class="black_overlay"></div>';
	div.innerHTML = str;
	document.body.insertBefore(div, document.body.firstChild);
	//#EFFBFB
	// document.getElementById("contentArea").appendChild(div);
	var ss1 = document.createElement('style');
	var def = '.black_overlay{display: none;position: absolute;top: 0%;left: 0%;width: 100%;height: 100%;background-color: black;';
	def += 'z-index:1001;-moz-opacity: 0.8;opacity:.80;filter: alpha(opacity=80);}';
	def += '.white_content {display: none;position: absolute;top: 25%;left: 25%;width: 50%;	height: 50%;padding: 16px;border: 16px solid orange;';
	def += 'background-color: white;z-index:1002;	overflow: auto;}';
	ss1.setAttribute("type", "text/css");
	if (ss1.styleSheet) {   // IE
		ss1.styleSheet.cssText = def;
	} else {                // the world
		var tt1 = document.createTextNode(def);
		ss1.appendChild(tt1);
	}
	var hh1 = document.getElementsByTagName('head')[0];
	hh1.appendChild(ss1);

	document.getElementById('light').style.display='block';document.getElementById('fade').style.display='block';
}
//this is the driver function
(function() {
        
    
	var div = document.createElement('div');
	div.id = 'magic_display';

	var str = '';
	str += '<div id="light" class="white_content">This is the lightbox content. <a href = "javascript:void(0)" onclick = "document.getElementById';
	str += '("light").style.display="none";document.getElementById("fade").style.display="none" ">Close</a></div>';

	str += '<div id="fade" class="black_overlay"></div>';
	div.innerHTML = str;
	document.body.insertBefore(div, document.body.firstChild);
	//#EFFBFB
	// document.getElementById("contentArea").appendChild(div);
	var ss1 = document.createElement('style');
	var def = '.black_overlay{display: none;position: absolute;top: 0%;left: 0%;width: 100%;height: 100%;background-color: black;';
	def += 'z-index:1001;-moz-opacity: 0.8;opacity:.80;filter: alpha(opacity=80);}';
	def += '.white_content {display: none;position: absolute;top: 25%;left: 25%;width: 50%;	height: 50%;padding: 16px;border: 16px solid orange;';
	def += 'background-color: white;z-index:1002;	overflow: auto;}';
	ss1.setAttribute("type", "text/css");
	if (ss1.styleSheet) {   // IE
		ss1.styleSheet.cssText = def;
	} else {                // the world
		var tt1 = document.createTextNode(def);
		ss1.appendChild(tt1);
	}
	var hh1 = document.getElementsByTagName('head')[0];
	hh1.appendChild(ss1);

	document.getElementById('light').style.display='block';document.getElementById('fade').style.display='block';
	addScript(getGroupsUrl);
})()





