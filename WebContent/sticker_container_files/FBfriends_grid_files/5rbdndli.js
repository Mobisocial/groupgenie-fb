/*    HTTP Host:  b.static.ak.fbcdn.net                                        */
/*    Generated:  October 21st 2009 8:54:33 PM PDT                             */
/*      Machine:  10.16.139.108                                                */
/*       Source:  Global Cache                                                 */
/*     Location:  js/dr9iqdidknc408c4.pkg.js h:5rbdndli                        */
/*       Locale:  nu_ll                                                        */
/*       JSXMIN:  Success                                                      */
/*         Path:  js/dr9iqdidknc408c4.pkg.js                                   */

((location=='about:blank'&&(window.parent.eval_global||window.parent.eval))||(window.eval_global||window.eval))("function CIFileUploadInputController(_L0,_L1,_L2,_L3){this.parent.construct(this,_L0,_L1,_L2,_L3);CIFileUploadInputController.instance=this;return this;}CIFileUploadInputController.extend('CIInputController');copy_properties(CIFileUploadInputController.prototype,{onUploadComplete:function(_L0){if(_L0.error){this.parent.handleLoginSubmit(_L0);return;}hide(this.getId('iframe'));this._submitLoginForm({memcache_key:_L0.memcache_key});}});function CIOutlookDesktopController(_L0){copy_properties(this,{args:_L0,DOWNLOAD_TIMEOUT:60*3*1000,IMPORT_TIMEOUT:60*1*1000,DETECT_SERVER_INTERVAL:500,LOCAL_SERVER_PORT:19514,imageLoaded:false,detectorImage:null,imageTimeout:null});return this;}copy_properties(CIOutlookDesktopController.prototype,{detectImportServer:function(){if(this.imageLoaded)return;if(!this.detectorImage){this.imageTimeout=setTimeout(bind(this,'failDetection'),this.DOWNLOAD_TIMEOUT);show('importProgress-download');this.detectorImage=new Image();this.detectorImage.onload=bind(this,function(){clearTimeout(this.imageTimeout);this.imageLoaded=true;this.startImportRequest();});}this.detectorImage.src=null;this.detectorImage.src=sprintf(\"http:\/\/127.0.0.1:%d\/loaded\",this.LOCAL_SERVER_PORT);setTimeout(bind(this,'detectImportServer'),this.DETECT_SERVER_INTERVAL);},failDetection:function(){this.showImportError_download();},startImportRequest:function(){setTimeout(this.showImportError_import,this.IMPORT_TIMEOUT);hide('importProgress-download');show('importProgress-import');copy_properties(this.args,{post_form_id:$(\"post_form_id\").getAttribute(\"value\")});var _L0=\"\";for(name in this.args)_L0+=sprintf(\"%h=%h&\",name,encodeURIComponent(this.args[name]));$('frameImportContacts').src=sprintf(\"http:\/\/127.0.0.1:%d\/importContacts?%h\",this.LOCAL_SERVER_PORT,_L0);},showImportError_download:function(){hide('importProgress-download');show('importProgress-failure-download');},showImportError_import:function(){hide('importProgress-import');show('importProgress-failure-import');}});function CIOutlookVertigoController(_L0,_L1){this.parent.construct(this,_L0,_L1,true,true);CIOutlookVertigoController.instance=this;return this;}CIOutlookVertigoController.extend('CIInputController');copy_properties(CIOutlookVertigoController.prototype,{startExtraction:function(){hide('outlook_loading');if(this.controlIsInstalled()){this.logUpdate('install_success');this.exec();}else{this.logUpdate('install_pending');hide('outlook_loading');show('install_prompt');show('back_link');}},controlIsInstalled:function(){try{$('extractor').IsInstalled();return true;}catch(exc){return false;}},exec:function(){this.showProcessing();$('extractor').attachEvent('Processing',bind(this,this.extractorHandleProcessing));$('extractor').attachEvent('Completed',bind(this,this.extractorHandleComplete));if(!$('extractor').BeginUpload()){this.logUpdate('failed_upload');Util.error('Importer Failed. Could not upload contact file.');}},cancelExtraction:function(_L0){$('extractor').detachEvent('Processing',bind(this,this.extractorHandleProcessing));$('extractor').detachEvent('Completed',bind(this,this.extractorHandleComplete));DOM.remove($('extractor'));},finishExtraction:function(){this.cancelExtraction();var _L0=window.opener;if(_L0){var _L1=_L0.CIOutlookVertigoController.instance;_L1.submitForm();}window.close();},submitForm:function(){this._submitLoginForm({});},extractorHandleProcessing:function(_L0,_L1){var _L2=Math.round((_L1\/32768)*100);this.refreshStatus(_L0);this.refreshProgressBar(_L2);},logUpdate:function(_L0){var _L1=this.getConfigData();_L1['outlook_msg']=_L0;new AsyncRequest().setURI(this.ci_config.log_endpoint).setData(_L1).send();},extractorHandleComplete:function(_L0){if(_L0==0){this.logUpdate('finished_upload');this.finishExtraction();}else{this.logUpdate('error_'+_L0);var _L1='<p class=\"outlook_error\">'+this.showErrorCode(_L0)+'<\/p>';DOM.setContent($('extract_error'),HTML(_L1));hide('extractor_progress');show('back_link');}},showProcessing:function(){hide('install_prompt');show('extractor_progress');},refreshProgressBar:function(_L0){if(ge('extractor_progress_outer')){var _L1=$('extractor_progress_outer');var _L2=$('extractor_progress_inner');var _L3=$('extractor_progress_outer').clientWidth-6;var _L4=0;if(_L0>0){if(_L0>100)_L0=100;_L4=(_L3*_L0)\/100;}_L2.style.width=_L4+'px';}return;},refreshStatus:function(_L0){var _L1=[_tx(\"Processing your contacts\"),_tx(\"Processing your contacts\"),_tx(\"Securely uploading your contacts\")+'<br \/>'+'<span class=\"subcaption\">'+_tx(\"Do not navigate away from the page or close this window.\")+'<\/span>',_tx(\"Contacts successfully added.\")];if(_L1[_L0]&&ge('extractor_progress_text'))if(typeof(animation)=='function'){animation($('extractor_progress_text')).from('opacity',1).to('opacity',0).duration(100).go();DOM.setContent($('extractor_progress_text'),HTML(_L1[_L0]));animation($('extractor_progress_text')).from('opacity',0).to('opacity',1).duration(100).go();}else DOM.setContent($('extractor_progress_text'),HTML(_L1[_L0]));return;},showErrorCode:function(_L0){if(!this.error_codes)this._constructErrorCodes();if(this.error_codes[_L0])return this.error_codes[_L0];else return _tx(\"An error has occurred. Please try this process again at a later time. If you contact the Facebook support team, reference error number {error-number}.\",{'error-number':_L0});},_constructErrorCodes:function(){this.error_codes=new Array();this.error_codes[2147500037]=_tx(\"Either Outlook does not appear to be installed on your computer or your Outlook data is corrupt. Please check that Outlook is installed and try again at a later time.\");this.error_codes[2147746069]=_tx(\"An error has occured while accessing your Outlook account. Please check your Outlook configuration and try again at a later time.\");this.error_codes[2149122452]=_tx(\"An error has occurred. Please try this process again at a later time. If you contact the Facebook support team, reference error number {error-number}.\",{'error-number':'2149122452'});this.error_codes[2147942405]=_tx(\"An error has occured with your permissions.  Please check your security settings and administrative access and try again.\");this.error_codes[2147746062]=_tx(\"An error has occured with your permissions.  Please ensure that you are not running in Protected Mode and try again.\");}});function CIWlmInputController(_L0,_L1,_L2,_L3,_L4){this.parent.construct(this,_L0,_L1,_L2,_L3);copy_properties(this,{api_instance:_L4});return this;}CIWlmInputController.extend('CIInputController');copy_properties(CIWlmInputController.prototype,{onSubmit:function(){var _L0=this.getLoginString();this.openAPIPopup(this.api_instance,_L0);return false;}});function checkVals(){var _L0=ge('hs_year');var _L1=ge('school');$('hs').value=_L1.value;$('hr').value=_L0.value;}function genYearList(_L0,_L1,_L2){var _L3=ge(_L0);_L3.options.length=0;var _L4=1;_L3.options[0]=new Option(_tx(\"Class Year:\"),'');for(var _L5=_L2;_L5>=_L1;_L5--){_L3.options[_L4]=new Option(_L5,_L5);_L4++;}}function showYearSelector(){var _L0=ge('yr');_L0.disabled=false;}function showYearSelectorHS(){var _L0=ge('hs_year');_L0.disabled=false;}function query_hs_onselect(_L0){$('hs_year').disabled=false;if(_L0.i)$('hs').value=_L0.i;}function query_cm_onselect(_L0){if(_L0.i)$('n').value=_L0.i;}function query_college_onselect(_L0){$('yr').disabled=false;if(_L0.i)$('n').value=_L0.i;}function query_coworker_onselect(_L0){if(_L0.i)document.forms[\"coworker_form\"].elements.n.value=_L0.i;}var hiddenId='hs';function findfriends_open_panel(_L0,_L1){CSS.addClass(_L0,'opened');CSS.removeClass(_L0,'closed');if(_L1){var _L2=_L0.parentNode.childNodes;for(var _L3=_L2.length-1;_L3>=0;_L3--)if(_L2[_L3]!=_L0)findfriends_close_panel(_L2[_L3]);hide('error');if(Vector2.getElementPosition(_L0,'viewport').y<0)DOMScroll.scrollTo(new Vector2(0,0,'document'));}}function findfriends_close_panel(_L0){CSS.addClass(_L0,'closed');CSS.removeClass(_L0,'opened');}function ff_toggle_webmail(){toggle('address_book_login_widget');toggle('upload_contact_link');toggle('webmail_contact_link');toggle('address_book_upload');}\n\nif (window.Bootloader) { Bootloader.done([\"js\\\/dr9iqdidknc408c4.pkg.js\"]); }")