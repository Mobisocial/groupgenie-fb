//function to fetch page, polling spage to receive status.
//sdiv is shown when status messages are active
//status messages are printed to the sdiv_text
//when the page is ready, the server should send a <resultPage>link</resultPage> result
//and the script will redirect to that page.

function get_random_url()
{
	var urls = ['http://facebook.com', 'http://twitter.com', 
	        	'http://news.google.com', 
			    'http://cnn.com', 'http://sports.yahoo.com', 'http://msnbc.com',
			    'http://tmz.com', 'http://wikipedia.org', 
			    'http://last.fm', 'http://youtube.com', 'http://hulu.com'];

	var now = new Date();
	var seed = now.getSeconds();
	var random = Math.floor(Math.random(seed)*urls.length);
	return urls[random];
}

function fetch_page_with_progress(progresshref)
{
    done = false;
    status_xhr = new_xhr();
    status_page = progresshref;
	status_div = document.getElementById('loading');
	status_div_text = document.getElementById('loading_status_text');

	refresh_status();
}

function new_xhr()
{
	if (window.XMLHttpRequest) {
		return new XMLHttpRequest();
	}
	// IE
	else if (window.ActiveXObject) {
		return new ActiveXObject("Microsoft.XMLHTTP");
	}
}


function refresh_status()
{
	//    var status_xhr = new_xhr();
	status_xhr.open("GET", status_page, true);
	// hack to make status_div visible inside the callback
	status_xhr.onreadystatechange=function() {
		if (status_xhr.readyState==4) {
			if (!done) {
				//    debugger;
				response = status_xhr.responseText;
				if (response != null) {
					response = jQuery.trim(response);
					if (response.length > 0)
					   status_div_text.innerHTML = response;
				}
				   
				setTimeout(refresh_status, 2000);
			}
		}
	};
	status_xhr.send(null);
}
