/*    HTTP Host:  static.ak.fbcdn.net                                          */
/*    Generated:  October 21st 2009 8:52:24 PM PDT                             */
/*      Machine:  10.16.140.104                                                */
/*       Source:  Local Cache                                                  */
/*     Location:  rsrc:4:4bdaqfyj                                              */
/*       Locale:  nu_ll                                                        */
/*       JSXMIN:  Success                                                      */
/*         Path:  js/da9kndps07wwooc0.pkg.js                                   */

((location=='about:blank'&&(window.parent.eval_global||window.parent.eval))||(window.eval_global||window.eval))("function CIBase(_L0,_L1){copy_properties(this,{ci_config:_L0,element_ids:_L1});return this;}copy_properties(CIBase.prototype,{getId:function(_L0){return this.element_ids[_L0];},setId:function(_L0,_L1){this.element_ids[_L0]=_L1;return this;},setAjaxMode:function(_L0){this.ajax_mode=_L0;return this;},getConfigData:function(){var _L0={type:this.ci_config.type,flow:this.ci_config.flow,domain_id:this.ci_config.domain_id,import_id:this.ci_config.import_id,tracked_params:this.ci_config.tracked_params};return _L0;}});function captchaRefresh(_L0,_L1,_L2,_L3,_L4){var _L5={new_captcha_type:_L0,id:_L2,t_auth_token:_L3};_L5.skipped_captcha_data=$('captcha_persist_data').value;if(_L1)_L5.registration_page=true;new AsyncRequest().setURI('\/captcha\/refresh_ajax.php').setMethod('GET').setReadOnly(true).setData(_L5).setHandler(function(_L6){DOM.setContent($(\"captcha\"),HTML(_L6.getPayload().captcha));if(_L4)DOM.setContent($(\"captcha_error_msg\"),null);}).send();}function CICaptcha(_L0){copy_properties(this,{captcha_endpoint:'\/contact_importer\/ajax\/captcha.php',callback:_L0,form_name:'ci_captcha_form'});return this;}copy_properties(CICaptcha.prototype,{showDialog:function(){var _L0=new AsyncRequest().setMethod('GET').setReadOnly(true).setURI(this.captcha_endpoint).setData({form_name:this.form_name}).setHandler(bind(this,'handleCaptchaRender')).send();},handleCaptchaRender:function(_L0){var _L1=_L0.getPayload();var _L2=[{name:'submit',label:_tx(\"Submit\"),handler:bind(this,'submitCaptchaResponse')},Dialog.CANCEL];new Dialog().setTitle('').setBody(_L1.content).setButtons(_L2).show();},submitCaptchaResponse:function(){var _L0=serialize_form($(this.form_name));new AsyncRequest().setURI(this.captcha_endpoint).setMethod('POST').setData(_L0).setHandler(bind(this,'handleCaptchaVerification')).send();},handleCaptchaVerification:function(_L0){var _L1=_L0.getPayload();if(_L1.error)new ErrorDialog().showError(_L1.error.title,_L1.error.message);else this.callback();}});function CIInputController(_L0,_L1,_L2,_L3){this.parent.construct(this,_L0,_L1);this.parent.setAjaxMode(_L3);copy_properties(this,{show_captcha:_L2});return this;}CIInputController.extend('CIBase');copy_properties(CIInputController.prototype,{onSubmit:function(){if(this.show_captcha){var _L0=new CICaptcha(bind(this,'_submitLoginForm'));_L0.showDialog();return false;}return this._submitLoginForm();},_submitLoginForm:function(_L0){var _L1=$(this.getId('form'));var _L2=get_form_attr(_L1,'action');var _L3=serialize_form(_L1);if(_L0)copy_properties(_L3,_L0);if(!this.ajax_mode){var _L4=DOM.create('form',{action:this.ci_config.full_endpoint,method:'POST'});create_hidden_inputs(_L3,_L4);DOM.getRootElement().appendChild(_L4);_L4.submit();return false;}var _L5=new AsyncRequest();if(_L3.jsonp){var _L6=this.getId('login_input');var _L7=this.getId('password');var _L8={};_L8[_L6]=_L3[_L6];_L8[_L7]=_L3[_L7];_L3.creds=Base64.encodeObject(_L8);delete _L3[_L6];delete _L3[_L7];_L5.setOption('jsonp',true).setMethod('GET').setReadOnly(true);}_L5.setURI(_L2).setData(_L3).setHandler(bind(this,function(_L9){this.handleLoginSubmit(_L9.getPayload());})).setStatusElement(this.getId('login_status'));if(_L3.jsonp)_L5.addStatusIndicator();_L5.send();return false;},handleLoginSubmit:function(_L0){if(_L0.error){new ErrorDialog().showError(_L0.error.title,_L0.error.body);return;}var _L1=_L0.content;var _L2=this.getId('contacts_container');DOM.replace($(_L2),HTML(_L0.content));},getLoginString:function(){return trim($(this.getId('login_input')).value);},showDetailedDisclaimer:function(){var _L0=new AsyncRequest().setURI('\/contact_importer\/ajax\/disclaimer.php');new Dialog().setAsync(_L0).show();},initializeCookieChecking:function(_L0){this.clearCookie();if(this.cookieTimer)clearTimeout(this.cookieTimer);this.checkCookie(_L0);},_getAPICookies:function(){return ['fb_ms_auth','fb_api_auth_token'];},clearCookie:function(){var _L0=this._getAPICookies();for(var _L1=0;_L1<_L0.length;_L1++){var _L2=getCookie(_L0[_L1]);if(_L2)setCookie(_L0[_L1],'');}},getAuthToken:function(_L0){var _L1=this._getAPICookies();for(var _L2=0;_L2<_L1.length;_L2++){var _L3=getCookie(_L1[_L2]);if(!_L3)continue;if(_L1[_L2]=='fb_ms_auth')return _L3;var _L4=_L3.substr(0,1);if(_L4==_L0)return _L3.substr(2);}return null;},openAPIPopup:function(_L0,_L1){var _L2=this.getConfigData();copy_properties(_L2,{api_instance:_L0,login_str:_L1});var _L3=new URI(this.ci_config.api_endpoint).addQueryData(_L2);if(_L0==1)popup_attrs='status=0,toolbar=0,location=1,resizable=1,width=600,'+'height=350,left='+((screen.width-600)\/2)+',top='+((screen.height-350)\/2)+',alwaysRaised=1';else if(_L0==3)popup_attrs='height=430,width=450,left='+((screen.width-500)\/2)+',top='+((screen.height-450)\/2)+',resizable=1,toolbar=0,status=0';else popup_attrs='height=600,width=830,left='+((screen.width-830)\/2)+',top='+((screen.height-600)\/2)+',resizable=1,scrollbars=1,toolbar=0,status=0';window.open(_L3,'api_contact_importer',popup_attrs);this.initializeCookieChecking(_L0);},checkCookie:function(_L0){var _L1=this.getAuthToken(_L0);if(_L1){var _L2={api_instance:_L0,auth_token:_L1};this.clearCookie();if(this.show_captcha){var _L3=new CICaptcha(bind(this,'_submitLoginForm',_L2));_L3.showDialog();}else this._submitLoginForm(_L2);}else this.cookieTimer=setTimeout(bind(this,'checkCookie',_L0),500);}});function CIWebmailValidator(_L0,_L1,_L2){copy_properties(this,{domains:_L0,domain_api_map:_L1,api_instances:_L2});return this;}copy_properties(CIWebmailValidator.prototype,{isValidEmail:function(_L0){var _L1=new RegExp(\"[a-z0-9_!#$%&'*+\/=?^`{|}~-]+(?:\\\\.[a-z0-9_!#$%&'*+\/=?^`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\");return _L1.test(_L0);},getDomain:function(_L0){var _L1=_L0.split('@');return _L1[1];},getAPIInstance:function(_L0){return this.domain_api_map[this.getDomain(_L0)];},isAPIDomain:function(_L0,_L1){return this.getAPIInstance(_L0)==this.api_instances[_L1];},isLiveDomain:function(_L0){return this.getAPIInstance(_L0)==this.api_instances['msft'];},isGmailDomain:function(_L0){return this.getAPIInstance(_L0)==this.api_instances['gmail'];},isYahooDomain:function(_L0){return this.getAPIInstance(_L0)==this.api_instances['yahoo'];},isSupportedDomain:function(_L0){var _L1=this.getDomain(_L0);var _L2=this.domains.length;for(var _L3=0;_L3<_L2;_L3++)if(_L1==this.domains[_L3])return true;return false;}});function CIWebmailInputController(_L0,_L1,_L2,_L3,_L4){this.parent.construct(this,_L0,_L1,_L2,_L3);copy_properties(this,{validator:_L4,login_str:null,ms_login:null,timer:null});this.importer_widget=$(this.getId('widget'));var _L5=$(this.getId('login_input'));if(ua.firefox())addEventBase(_L5,'keypress',bind(this,'delayOnKeypress'));else addEventBase(_L5,'keydown',bind(this,'delayOnKeypress'));addEventBase(_L5,'focus',bind(this,'pollField'));addEventBase(_L5,'blur',bind(this,'cancelPoll'));_L5=null;this.validate();return this;}CIWebmailInputController.extend('CIInputController');copy_properties(CIWebmailInputController.prototype,{onSubmit:function(_L0){this.validate();var _L1=this.getLoginString();var _L2=this.validator.getAPIInstance(_L1);if(_L2){this.openAPIPopup(_L2,_L1);return false;}return this.parent.onSubmit(_L0);},validateOnKeypress:function(_L0){if(this.timer)clearTimeout(this.timer);if(Event.getKeyCode(_L0)==KEYS.TAB)this.validate();else this.timer=setTimeout(bind(this,this.validate),300);},delayOnKeypress:function(_L0){if(this.timer)clearTimeout(this.timer);if(Event.getKeyCode(_L0)==KEYS.TAB)this.validate();else this.timer=setTimeout(bind(this,this.pollField),300);},pollField:function(){if(this.timer)clearTimeout(this.timer);this.timer=setTimeout(bind(this,function(){this.validate();this.pollField();}),300);},cancelPoll:function(){if(this.timer)clearTimeout(this.timer);this.validate();},validate:function(){var _L0=this.getLoginString();if(!_L0){this.setDefault();return;}if(_L0==this.login_str)return;this.login_str=_L0;var _L1=this.validator;if(!_L1.isValidEmail(_L0)){this.setDefault();return;}if(_L1.isSupportedDomain(_L0))if(_L1.isLiveDomain(_L0))this.setWindowsLive();else if(_L1.isYahooDomain(_L0))this.setYahoo();else if(_L1.isGmailDomain(_L0))this.setGmail();else this.setValid();else this.setUnsupported();},setWindowsLive:function(){CSS.setClass(this.importer_widget,'windows_live_noiframe');this.disablePassword();},setGmail:function(){CSS.setClass(this.importer_widget,'gmail');this.disablePassword();},setYahoo:function(){CSS.setClass(this.importer_widget,'yahoo');this.disablePassword();},setValid:function(){this.enablePassword();CSS.setClass(this.importer_widget,'valid');},setUnsupported:function(){CSS.setClass(this.importer_widget,'unsupported');this.disablePassword();var _L0=this.getConfigData();_L0['unsupported_login']=this.login_str;new AsyncSignal(this.ci_config.log_endpoint,_L0).send();},setDefault:function(){CSS.setClass(this.importer_widget,'default');this.enablePassword();},showNormalImporter:function(_L0){this.setId('login_status',this.getId('normal_login_status'));var _L1=$(this.getId('login_input'));if(_L0){this.setValid();_L1.value='';}show(this.getId('ci_normal_div'));hide(this.getId('ci_logo_div'));_L1.focus();},canHidePassword:function(){return !CSS.hasClass(this.importer_widget,'default');},logABTestSubmitClick:function(_L0){var _L1=this.getConfigData();_L1['log_ab_test_click']=_L0;_L1['login_str']=this.getLoginString();new AsyncSignal(this.ci_config.log_endpoint,_L1).send();},disablePassword:function(){var _L0=$(this.getId('password'));if(!this.canHidePassword())return;hide(_L0.parentNode.parentNode);},enablePassword:function(){var _L0=$('password');var _L1=_L0.parentNode.parentNode;if(!shown(_L1))show(_L1);}});function CINUXToDoWebmailInputController(_L0,_L1,_L2,_L3,_L4){this.parent.construct(this,_L0,_L1,_L2,_L3,_L4);return this;}CINUXToDoWebmailInputController.extend('CIWebmailInputController');copy_properties(CINUXToDoWebmailInputController.prototype,{disablePassword:function(){},enablePassword:function(){},setYahoo:function(){},setGmail:function(){},setWindowsLive:function(){}});function domainDialog(_L0){copy_properties(this,{dialog:null});if(_L0){var _L1='Supported Email Addresses';var _L2='<p class=\"domain_description\">'+_tx(\"We currently support the domains listed below for use through our Webmail Importer. If you do not see your email domain in this list, please try finding your friends through our File Importer.\")+'<\/p>'+'<div class=\"supported_domains\">'+'<ul>';for(var _L3=0;_L3<_L0.length;_L3++)_L2+='<li>'+_L0[_L3]+'<\/li>';_L2+='<\/ul><\/div>';this.dialog=new Dialog().setTitle(_L1).setButtons(Dialog.OK).setBody(_L2);}}copy_properties(domainDialog.prototype,{show:function(){this.dialog.show();}});\n\nif (window.Bootloader) { Bootloader.done([\"js\\\/da9kndps07wwooc0.pkg.js\"]); }")