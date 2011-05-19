LOG = true; // needs to be cleaned up, both log and LOG are also in jog.js
function log(mesg)
{
//	alert ('logging ' + mesg);
	if (!(typeof console == 'undefined') && (LOG == true))
		console.log(mesg);
}

function inc(filename)
{
	var body = document.getElementsByTagName('body').item(0);
	script = document.createElement('script');
	script.src = filename;
	script.type = 'text/javascript';
	body.appendChild(script);
}


function obtainFBEnvData()
{
	// TOFIX: check if window.Env is defined, if not we're not running on fb page, popup a warning and exit
	if (window.Env == null) {
		window.location = sfAppUrl;
		return;
	}
	
	var user_id = window.Env.user;
	var postformid = window.Env.post_form_id;
	var fb_dtsg = window.Env.fb_dtsg;
	log ('getting FB env data: user id = ' + user_id + ' post_form_id = ' + postformid + ' fb_dtsg = ' + fb_dtsg);
	try {
		inc(integrationUrl+'?fb_userid='+user_id+'&fb_postformid='+postformid+'&fb_dtsg='+fb_dtsg+'&callback=socialflowsSuccess');
	} catch (e) {
		log ('exception: ' + e.toString());
		alert ('exception: ' + e.toString());
	}
}

function socialflowsSuccess(jsonObj)
{
	if (jsonObj.success)
		log ('successfully exported FB env data');
	else
		log ('failed to export FB env data');
}



(function() {
	obtainFBEnvData();
})();


