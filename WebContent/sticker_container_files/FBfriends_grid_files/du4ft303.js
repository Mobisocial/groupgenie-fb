/*    HTTP Host:  b.static.ak.fbcdn.net                                        */
/*    Generated:  October 21st 2009 8:52:14 PM PDT                             */
/*      Machine:  10.16.139.108                                                */
/*       Source:  Global Cache                                                 */
/*     Location:  js/2w9ovm0wvxa8w4ow.pkg.js h:du4ft303                        */
/*       Locale:  nu_ll                                                        */
/*       JSXMIN:  Success                                                      */
/*         Path:  js/2w9ovm0wvxa8w4ow.pkg.js                                   */

((location=='about:blank'&&(window.parent.eval_global||window.parent.eval))||(window.eval_global||window.eval))("function Beep(_L0,_L1){copy_properties(this,_L1);this.unread=true;this._owner=_L0;this._node=$(HTML(this.html).getNodes()[0]);CSS.setOpacity(this._node,'0');this._node.find('a.beeper_x').listen('click',this.remove.bind(this));var _L2=this._node.scry('a.undo_link');if(_L2.length==1)_L2[0].listen('click',this.onUndoClick.bind(this,_L2[0]));this._node.listen('mouseover',this.onMouseOver.bind(this));this._node.listen('mouseout',this.onMouseOut.bind(this));}copy_properties(Beep,{REMOVE_BEEP_ANIM_MS:250,UNDONE_BEEP_DELAY_MS:1000});copy_properties(Beep.prototype,{onMouseOver:function(_L0){this._node.addClass('UIBeep_Selected');this._selected=true;this.setEdges();},onMouseOut:function(_L0){this._node.removeClass('UIBeep_Selected');this._selected=false;this.setEdges();},onUndoClick:function(_L0){new AsyncRequest().setURI('\/ajax\/presence\/undo_send.php').setData({alert_id:this.alertId}).setHandler(function(){DOM.replace(_L0,$N('span',{className:'undo_link'},_tx(\"Undone\")));this.remove.bind(this).defer(Beep.UNDONE_BEEP_DELAY_MS);}.bind(this)).send();},xout:function(){this._owner.pause();var _L0=function(){this._owner.unpause();}.bind(this);ChatNotifications.showReportDialog(this.applicationName,this.onHideBeepSpam.bind(this,false),this.onHideBeepSpam.bind(this,true),_L0);},onHideBeepSpam:function(_L0){ChatNotifications.markAppNotificationSpam(this.applicationId,this.platformType,_L0);this._owner.hideBeepsByApplication(this.applicationId);presenceNotifications.hideNotifications(this.applicationId);this._owner.unpause(Beeper.DEFAULT_DELAY_MS);},remove:function(){animation(this._node).to('width',30).to('height',27).to('margin-left',169).to('opacity',0).duration(Beep.REMOVE_BEEP_ANIM_MS).ease(animation.ease.both).ondone(function(){this._node.remove();this._owner.beeps.remove(this);if(!this._owner.beeps.length)this._owner.reset();else this._owner.updateEdges();}.bind(this)).go();this.fadeOut(Beep.REMOVE_BEEP_ANIM_MS);},markRead:function(){this.unread=false;},endFade:function(){if(this._node)this._node.removeClass('UIBeep_Fading');this._owner.setFading(false,this._isTop,this._isBottom);},fadeOut:function(_L0){this._node.addClass('UIBeep_Fading');this._owner.setFading(true,this._isTop,this._isBottom);var _L1=this._node.scry('img.beeper_pic');if(_L1&&_L1.length){_L1=_L1[0];animation(_L1).to('width',20).to('height',20).duration(_L0).blind().go();}var _L2=this._node.find('div.UIBeep_Icon');animation(_L2).from('width',40).to(20).duration(_L0).ondone(this.endFade.bind(this)).go();},fadeIn:function(_L0){this._owner.fadeIn(this._isTop,this._isBottom,_L0);animation(this._node).to('opacity',1).ease(animation.ease.both).duration(_L0).show().go();},setEdges:function(){if(this._isTop)this.setTop(true);if(this._isBottom)this.setBottom(true);},setBottom:function(_L0){this._isBottom=_L0;CSS.conditionClass(this._node,'UIBeep_Bottom',_L0);if(_L0)this._owner.setBottom(this._selected);},setTop:function(_L0){this._isTop=_L0;CSS.conditionClass(this._node,'UIBeep_Top',_L0);if(_L0)this._owner.setTop(this._selected);},getNode:function(){return this._node;}});function Beeper(_L0){this._baseRoot=_L0;this._receiver=new LiveMessageReceiver(Beeper.BEEP_EVENT).setAppId(Beeper.APP_ID).setHandler(this.fromMessage.bind(this)).register();Arbiter.subscribe(PresenceMessage.TAB_OPENED,this.onPresenceTabOpen.bind(this));}copy_properties(Beeper,{BEEP_EVENT:'beep_event',APP_ID:30729425562,CONTENT_ID:'BeeperBox',DEFAULT_DELAY_MS:10000,DEFAULT_UNPAUSE_MS:500,SLIDE_DOWN_ANIMATION_MS:700,FADE_IN_ANIMATION_MS:500,FADE_OUT_ANIMATION_MS:1250,_instance:null,setInstance:function(_L0){Beeper._instance=_L0;},getInstance:function(){Beeper.ensureInitialized();return Beeper._instance;},ensureInitialized:function(){if(!Beeper._instance)Beeper.setInstance(new Beeper($(Beeper.CONTENT_ID)));}});copy_properties(Beeper.prototype,{beeps:[],reset:function(){this.beeps=[];if(this._root)this._root.remove();this._root=$(this._baseRoot.cloneNode(true));this._root.listen('mouseover',this.mouseover.bind(this));this._root.listen('mouseout',this.mouseout.bind(this));DOM.insertAfter(this._baseRoot,this._root);this._full=this._root.find('div.UIBeeper_Full');this._inside=this._root.find('div.Beeps');this._top=this._root.find('img.UIBeeper_Top');this._top_selected=this._root.find('img.UIBeeper_Top_Selected');this._bottom=this._root.find('img.UIBeeper_Bottom');this._bottom_selected=this._root.find('img.UIBeeper_Bottom_Selected');if(this._timer)clearInterval(this._timer);this._timer=null;this._animation=null;this._animatingOut=false;this._paused=0;Arbiter.inform(Arbiter.BEEPS_EXPIRED);},onPresenceTabOpen:function(_L0,_L1){tab=_L1.tabID;if(tab&&tab=='presence_notifications_tab')this.reset();},mouseover:function(_L0){this.pause();this.markRead();},mouseout:function(_L0){this.unpause();},fromMessage:function(_L0){if(!this._root)this.reset();var _L1=new Beep(this,_L0);this.addBeep(_L1);},addBeep:function(_L0){if(presence.focusedTab=='presence_notifications_tab')return;if(this._animatingOut){this.addBeep.bind(this,_L0).defer(500);return;}this.beeps.push(_L0);_L0.setTop(true);if(this.beeps.length==1){_L0.setBottom(true);this._root.addClass('UIBeeper_Active');}else{_L0.setBottom(false);this.beeps[this.beeps.length-2].setTop(false);}this._restartTimer(Beeper.DEFAULT_DELAY_MS);this._inside.prependContent(_L0.getNode());_L0.fadeIn(Beeper.FADE_IN_ANIMATION_MS);},hasIncoming:function(){for(var _L0=0;_L0<this.beeps.length;_L0++)if(this.beeps[_L0].type=='NotificationBeep'&&!this.beeps[_L0].outgoing)return true;return false;},expireBeeps:function(){if(this.beeps.length<=0)return;this.fadeOut();},shrinkOut:function(){animation(this._full).to('width',30).to('height',27*this.beeps.length).to('margin-left',169).to('margin-bottom',5).duration(Beeper.SLIDE_DOWN_ANIMATION_MS).ease(animation.ease.both).checkpoint(.3).to('opacity',0).to('height',0).ease(animation.ease.both).ondone(function(){this.reset();}.bind(this)).go();this._animatingOut=true;for(var _L0=0;_L0<this.beeps.length;_L0++)this.beeps[_L0].fadeOut(Beeper.SLIDE_DOWN_ANIMATION_MS);},fadeOut:function(){this._animation=animation(this._full).duration(Beeper.FADE_OUT_ANIMATION_MS).to('opacity',0).blind().ease(animation.ease.begin).ondone(function(){this.reset();}.bind(this));this._animation.undo=function(){this._animation.stop();CSS.setOpacity(this._full,1);}.bind(this);this._animation.go();},markRead:function(_L0,_L1){if(!_L1){_L1=[];this.beeps.each(function(_L3){if(_L3.unread&&_L3.type=='NotificationBeep'&&!_L3.outgoing)_L1.push(_L3.alertId);});}var _L2=Object.from(_L1);this.beeps.each(function(_L3){if(_L2[_L3.alertId])_L3.markRead();});if(!_L0&&_L1.length)presenceNotifications.notifyRead(_L1);},pause:function(){if(this._animatingOut)return;if(this._animation&&this._animation.undo){this._animation.undo();this._animation=null;return;}this._pauseTimer();this._root.addClass('UIBeeper_Paused');this._paused++;},_pauseTimer:function(){if(this._timer)clearInterval(this._timer);},unpause:function(_L0){if(!_L0)_L0=Beeper.DEFAULT_UNPAUSE_MS;this._paused--;if(this._paused<=0){this._root.removeClass('UIBeeper_Paused');this._restartTimer(_L0);}},_restartTimer:function(_L0){if(this._timer)clearInterval(this._timer);this._timer=this.expireBeeps.bind(this).defer(_L0);},updateEdges:function(){if(this.beeps.length<=0)return;this.beeps[0].setBottom(true);this.beeps[this.beeps.length-1].setTop(true);},hideBeepsByApplication:function(_L0){for(var _L1=0;_L1<this.beeps.length;_L1++)if(this.beeps[_L1].applicationId==_L0)this.beeps[_L1].remove();},fadeIn:function(_L0,_L1,_L2){var _L3=[];if(_L0){_L3.push(this._top);_L3.push(this._top_selected);}if(_L1){_L3.push(this._bottom);_L3.push(this._bottom_selected);}for(var _L4=0;_L4<_L3.length;_L4++)animation(_L3[_L4]).from('opacity',0).to('opacity',1).ease(animation.ease.both).duration(_L2).go();},setFading:function(_L0,_L1,_L2){var _L3=(_L0?CSS.addClass:CSS.removeClass);if(_L1)_L3(this._root,'UIBeeper_Fading_Top');if(_L2)_L3(this._root,'UIBeeper_Fading_Bottom');},setTop:function(_L0){CSS.conditionClass(this._top,'display_none',_L0);CSS.conditionClass(this._top_selected,'display_none',!_L0);},setBottom:function(_L0){CSS.conditionClass(this._bottom,'display_none',_L0);CSS.conditionClass(this._bottom_selected,'display_none',!_L0);}});\n\nif (window.Bootloader) { Bootloader.done([\"js\\\/2w9ovm0wvxa8w4ow.pkg.js\"]); }")