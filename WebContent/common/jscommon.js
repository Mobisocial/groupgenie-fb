/*
 * Common JS utility functions
 * by Seng Keat Teh [ skteh@cs.stanford.edu ]
 * version: 1.0 (25 April 2010)
 * @requires jQuery v1.4 or later
 *
 */

//sourced from http://javascript.crockford.com/remedial.html
if (!String.prototype.trim) {
    String.prototype.trim = function () {
        return this.replace(/^\s*(\S*(?:\s+\S+)*)\s*$/, "$1");
    };
}

if (!String.prototype.endsWith) {
	String.prototype.endsWith = function(pattern) {
	    var d = this.length - pattern.length;
	    return d >= 0 && this.lastIndexOf(pattern) === d;
	};	
}

Object.size = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};


// Logging utility for debugging
LOG = true;
function log(mesg)
{
	if (!(typeof console == 'undefined') && (LOG == true))
		console.log(mesg);
}

function cnvrt2Upper(str) {
    return str.toLowerCase().replace(/\b[a-z]/g, cnvrt);
    function cnvrt() {
        return arguments[0].toUpperCase();
    }
}

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

function urlEncode(inputString)
{
    var encodedInputString=escape(inputString);
    encodedInputString=encodedInputString.replace("+", "%2B");
    encodedInputString=encodedInputString.replace("/", "%2F");
    encodedInputString=encodedInputString.replace("@", "%40");
    return encodedInputString;
}

function getTimeStamp()
{
    var hhmmss = new Date();
    var hh = hhmmss.getHours();
    var mm = hhmmss.getMinutes();
    var ss = hhmmss.getSeconds();
    var timeval = hh*10000+mm*100+ss;
    return timeval;
}

function isInternetExplorerBrowser()
{
	log('Navigator app name: '+navigator.appName.toLowerCase());
    log('Navigator user agent: '+navigator.userAgent.toLowerCase());
    
	var is_IE = navigator.appName.toLowerCase().indexOf('internet explorer') > -1;
    if (is_IE) {
    	return is_IE;
    }

    is_IE = navigator.appName.toLowerCase().indexOf('microsoft') > -1;
    if (is_IE) {
    	return is_IE;
    }

    is_IE = navigator.userAgent.toLowerCase().indexOf('msie') > -1;
    return is_IE;
}

function isChromeBrowser()
{
	var is_chrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;
    log('Navigator user agent: '+navigator.userAgent.toLowerCase());
    if (is_chrome) {
    	log('--> Chrome browser ');
    }

    isChrome = function() {
   	    return Boolean(window.chrome);
   	}
	if (isChrome()) {
		is_chrome = true;
		log('This is indeed a Chrome browser ');
    }
	else {
		is_chrome = false;
		log('This is NOT a Chrome browser ');
	}
	return is_chrome;
}

function areCookiesEnabled()
{
	var cookieEnabled 
	= (navigator.cookieEnabled) ? true : false;
	log('areCookiesEnabled(): First Check --> '+cookieEnabled);

	if (typeof navigator.cookieEnabled == "undefined" 
		&& !cookieEnabled)
	{
		document.cookie="testcookie";
		cookieEnabled = (document.cookie.indexOf("testcookie") != -1) ? true : false;
		log('areCookiesEnabled(): 2nd Check --> '+cookieEnabled);
	}
	return (cookieEnabled);
}
