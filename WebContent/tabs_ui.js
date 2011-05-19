/*    HTTP Host:  static.ak.fbcdn.net                                          */
/*    Generated:  October 9th 2009 2:43:04 PM PDT                              */
/*      Machine:  10.16.139.106                                                */
/*       Source:  Global Cache                                                 */
/*     Location:  js/page/photos_dashboard.js h:4p584tjd                       */
/*       Locale:  nu_ll                                                        */
/*         Path:  js/page/photos_dashboard.js                                  */

(location=='about:blank'?window.parent.eval_global:window.eval_global)("\n" +
		"" +
		"function PhotoDashboardController(view,s,users_to_search)" +
		"{" +
		"	this.displayContainer=$('photo_container');" +
		" 	this.displayBar=$('photo_bar');" +
		"	this.displayTabs=$('photo_tabs');" +
		"	this.toggle={'recent':$('toggle_recent')," +
		"				 'mobile':$('toggle_mobile')," +
		"				 'tagged':$('toggle_tagged')," +
		"				 'search':$('toggle_search')};" +
		"	this.view=view;" +
		"	this.s=s;" +
		"	this.users_to_search=users_to_search;" +
		"	this.tokenizer=ge('psids');" +
		"" +
		"	PhotoDashboardController.singleton=this;" +
		"	PageTransitions.registerHandler(bind(this,'transitionHandler'));" +
		"}" +
		"" +
		
		/*
		<script language="javascript">
		function changeInSituTabSelected(view)
		{
			this.toggle={'butler' :document.getElementById("toggle_butler"),
						 'dunbar' :document.getElementById("toggle_dunbar"),
						 'cliques':document.getElementById("toggle_cliques")};
						 
			document.getElementById(view).setAttribute("class", "selected");
			
			
			for(var tab in this.toggle)
		    {
				if (tab==this.view){
				   this.toggle[tab].setAttribute("class", "selected");
				}
				else{
				   this.toggle[tab].setAttribute("class", "");
				}
			}
			
			
		}
		</script>
		 */
		
		"\nPhotoDashboardController.search=function(users_to_search)" +
		"	{" +
		"		var singleton=PhotoDashboardController.singleton;" +
		"		singleton.users_to_search=users_to_search;" +
		"		singleton.s=0;singleton.view='search';" +
		"		singleton.refreshToggles();" +
		"		singleton.refreshContent();" +
		"	};" +
		"" +
		"PhotoDashboardController.goPage=function(s)" +
		"{" +
		"	var singleton=PhotoDashboardController.singleton;" +
		"	  	singleton.s=s;singleton.refreshContent();" +
		"};" +
		"" +
		"PhotoDashboardController.goView=function(view)" +
		"{" +
		"	var singleton=PhotoDashboardController.singleton;" +
		"	singleton.view=view;" +
		"	singleton.s=0;" +
		"	singleton.refreshToggles();" +
		"	singleton.refreshContent();" +
		"};" +
		"" +
		"copy_properties(PhotoDashboardController.prototype," +
		"" +
		"{" +
		"	refreshToggles:function()" +
		"	{" +
		"		show(this.displayTabs);" +
		"		for(var tab in this.toggle)" +
		"		{" +
		"			if(tab==this.view)" +
		"			{" +
		"				CSS.addClass(this.toggle[tab],'selected');" +
		"			}" +
		"			else" +
		"			{" +
		"				CSS.removeClass(this.toggle[tab],'selected');" +
		"			}" +
		"		}" +
		"	}," +
		"" +
		"	refreshContent:function()" +
		"	{" +
		"		var query_data={};" +
		"		if(this.users_to_search&&this.view=='search')" +
		"		{" +
		"			query_data.psids=this.users_to_search;" +
		"		}" +
		"		query_data.view=this.view;" +
		"		if(this.s>0)" +
		"		{query_data.s=this.s;}" +
		"" +
		"		PageTransitions.go(URI.getRequestURI().setQueryData(query_data));" +
		"	}," +
		"" +
		"	_refreshContent:function(r)" +
		"	{" +
		"		var blank=r.getPayload().blank;" +
		"		if(blank)" +
		"		{" +
		"			hide(this.displayBar);" +
		"			DOM.setContent(this.displayContainer,HTML(blank));" +
		"			return;" +
		"		}" +
		"		var view=r.getPayload().view;" +
		"		if(view)" +
		"		{" +
		"			this.view=view;" +
		"			if(this.view=='search')" +
		"			{	CSS.removeClass(this.toggle['search'],'hidden');	}" +
		"			else" +
		"			{	" +
		"				CSS.addClass(this.toggle['search'],'hidden');" +
		"				this.users_to_search=null;" +
		"				if(this.tokenizer&&this.tokenizer.tokenizer)" +
		"				{" +
		"					this.tokenizer.tokenizer.clear();" +
		"					this.tokenizer.tokenizer._onblur();" +
		"				}" +
		"			}" +
		"			this.refreshToggles();" +
		"		}" +
		"		" +
		"		var summaryBar=r.getPayload().summaryBar;" +
		"		if(summaryBar)" +
		"		{" +
		"			DOM.setContent(this.displayBar,HTML(summaryBar));" +
		"			show(this.displayBar);" +
		"		}" +
		"		else" +
		"		{" +
		"			hide(this.displayBar);" +
		"		}" +
		
		"		var empty=r.getPayload().empty;" +
		"		if(empty)" +
		"		{" +
		"			DOM.setContent(this.displayContainer,HTML(empty));" +
		"			return;" +
		"		}" +
		"" +
		"		DOM.setContent(this.displayContainer,HTML(r.getPayload().html));" +
		"		show(this.displayContainer);" +
		"	}," +
		"" +
		"	transitionHandler:function(uri)" +
		"	{" +
		"		var uri_path=uri.getPath();" +
		"		if(uri_path=='\/photos\/'||uri_path=='\/photos\/index.php')" +
		"		{" +
		"			var params=uri.getQueryData();" +
		"			this.view=params.view;" +
		"			this.s=params.s?parseInt(params.s):0;" +
		"			this.users_to_search=params.psids;" +
		"			var bar_loading=ge('bar_loading');" +
		"			if(bar_loading)" +
		"			{bar_loading.style.display='inline';}" +
		"" +
		"			var data={view:this.view,s:this.s,psids:this.users_to_search};" +
		"			new AsyncRequest().setURI('\/photos\/ajax\/index.php')" +
		"							  .setReadOnly(true)" +
		"							  .setMethod('GET')" +
		"							  .setData(data)" +
		"							  .setHandler(this._refreshContent.bind(this))" +
		"							  .setFinallyHandler(PageTransitions.transitionComplete)" +
		"							  .send();" +
		"			return true;" +
		"		}" +
		"		return false;" +
		"	}" +
		"});" +
		"" +
		"if (window.Bootloader) " +
		"{ Bootloader.done([\"js\\\/page\\\/photos_dashboard.js\"]); }");









