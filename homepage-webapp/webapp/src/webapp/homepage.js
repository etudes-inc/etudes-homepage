tool_obj =
{
	title: "HOME",
	showReset: true,

	currentMode: 0,

	modeBarElementId: null,

	// the homepage content data
	current: null,
	pending: null,
	drafts: null,
	past: null,
	announcementsOptions: null,
	scheduleOptions: null,
	chatOptions: null,
	optionsModifiedDate: null,
	optionsModifiedUser: null,
	mayConfigure: false,
	format: 0,
	order: "ACS",
	announcements: null,
	tools: null,
	timezone : null,
	
	siteId: null,
	siteTitle: null,
	enableVT : false,
	serverUrl : null,
	eventsDays :
	{
		year: 0,
		month: 0,
		days: []
	},

	selectedDate: null,
	eventsDate: null,
	eventsDateZone: null,
	events: null,
	chat: null,
	chatTimer: null,

	imageFinderApi: null,
	imageFinderSelection: null,
	
	editorFinderApi: null,
	editorFinderCallback: null,

	editorApi: null,

	eventsDatepickerConfig:
	{
		dayNamesMin: ["Sun", "Mon" ,"Tue", "Wed", "Thu", "Fri", "Sat"],
		onSelect: function(date, inst)
		{
			tool_obj.selectedDate = $("#home_calendar").datepicker("getDate");
			tool_obj.loadEvents(tool_obj);
		},
		beforeShowDay: function(date, inst)
		{
			return tool_obj.checkDate(tool_obj, date);
		},
		onChangeMonthYear: function(year, month, inst)
		{
			// year ~ 2013, month ~ 1..12
			tool_obj.loadEventsDays(tool_obj, year, month);
		},
		dateFormat: "M dd, yy"
	},

	dateTimePickerConfig:
	{
		dayNamesMin: ["Sun", "Mon" ,"Tue", "Wed", "Thu", "Fri", "Sat"],
		dateFormat: "M dd, yy",
		showButtonPanel: true,
		changeMonth: true,
		changeYear: true,
		showOn: "both", // "button"
		buttonImage: "support/icons/date.png",
		buttonImageOnly: true,
		timeFormat: "hh:mm TT",
		controlType: "select",
		showTime: false,
		closeText: "OK",
		hour: 8,
		minute: 0
	},

	ckconfig:
	{		
		skin: 'office2003',
		height: 460,
		width: 680,
		// Whether or not you want to show the Word Count
	    showWordCount: true,

	    // Whether or not you want to show the Char Count
	    showCharCount: false,

	     // Whether or not to include Html chars in the Char Count
	     countHTML: false,
	    audiorecorder : {
                "maxSeconds" : 120,
                "attemptAllowed" : Number.MAX_VALUE,
                "attemptsRemaining": Number.MAX_VALUE
        }, 
	    fileConnectorUrl : '/resources/connector?',
		filebrowserBrowseUrl: function(params)
		{
			if (tool_obj.editorFinderApi != null) tool_obj.editorFinderApi.destroy();
			$("#home_finder_dialog_finder").empty();
			tool_obj.editorFinderApi = null;

			openDialog("home_finder_dialog", null, 100000);

			var f = new CKFinder({}, function(){tool_obj.adjustFinder("home_finder_dialog_finder");});
			f.basePath = '/ckfinder/';
			f.selectActionFunction = tool_obj.editorFinderSelected;
			tool_obj.editorFinderApi = f.appendTo($("#home_finder_dialog_finder")[0],
			{
				connectorInfo: "siteId=" + tool_obj.siteId + "&rtype=Files",
				connectorPath: "/resources/connector",
				startupPath: tool_obj.siteTitle + " Files:/Home/",
				disableHelpButton: true,
				rememberLastFolder: false,
				startupFolderExpanded: true,
				defaultViewType: "thumbnails",
				disableThumbnailSelection: true
			});
			tool_obj.editorFinderCallback = params.CKEditorFuncNum;
		},

    	filebrowserImageBrowseUrl: function(params)
		{
			if (tool_obj.editorFinderApi != null) tool_obj.editorFinderApi.destroy();
			$("#home_finder_dialog_finder").empty();
			tool_obj.editorFinderApi = null;

			openDialog("home_finder_dialog", null, 100000);

			var f = new CKFinder({}, function(){tool_obj.adjustFinder("home_finder_dialog_finder");});
			f.basePath = '/ckfinder/';
			f.selectActionFunction = tool_obj.editorFinderSelected;
			tool_obj.editorFinderApi = f.appendTo($("#home_finder_dialog_finder")[0],
			{
				connectorInfo: "siteId=" + tool_obj.siteId + "&rtype=Images",
				connectorPath: "/resources/connector",
				startupPath: tool_obj.siteTitle + " Images:/Home/",
				disableHelpButton: true,
				rememberLastFolder: false,
				startupFolderExpanded: true,
				defaultViewType: "thumbnails",
				disableThumbnailSelection: true
			});
			tool_obj.editorFinderCallback = params.CKEditorFuncNum;
		},

		pasteFromWordRemoveFontStyles : false,
		pasteFromWordRemoveStyles : false,
		disableNativeSpellChecker: false,
		browserContextMenuOnCtrl: true,
		toolbar_Etudes:
		[
			[ 'Source','-','DocProps','Print','-'] ,
			[ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] ,
			[ 'Find','Replace','-','SelectAll','-','SpellChecker', 'Scayt' ] ,
			[ 'Bold','Italic','Underline','Strike','Subscript','Superscript','-','RemoveFormat' ] ,
			[ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote',
			'-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','-','BidiLtr','BidiRtl' ] ,
			[ 'Link','Unlink','Anchor'] ,
			[ 'Image','MediaEmbed','AudioRecorder','VTLTIConnect','Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak'] ,
			[ 'Maximize', 'Preview', 'ShowBlocks'] ,
			[ 'TextColor','BGColor' ] ,
			[ 'Styles','Format','Font','FontSize','ckeditor_wiris_formulaEditor']
		],
		toolbar: 'Etudes',
		removePlugins: 'elementspath',
		resize_dir: 'vertical',
		resize_enabled: false,
		toolbarCanCollapse : false,
		extraPlugins: 'MediaEmbed,audiorecorder,movieplayer,wordcount,onchange,ckeditor_wiris',
		scayt_srcUrl: "https://spell.etudes.org/spellcheck/lf/scayt/scayt.js",
		wsc_customLoaderScript: "https://spell.etudes.org/spellcheck/lf/22/js/wsc_fck2plugin.js",
		smiley_path: '/docs/smilies/',
		smiley_images:
		[
		 	'Straight-Face.png','Sun.png','Sweating.png','Thinking.png','Tongue.png',
			'Vomit.png','Wave.png','Whew.png','Win.png','Winking.png','Yawn.png','Yawn2.png',
			'Zombie.png','Angry.png','Balloon.png','Big-Grin.png','Bomb.png','Broken-Heart.png',
			'Cake.png','Cat.png','Clock.png','Clown.png','Cold.png','Confused.png','Cool.png',
			'Crying.png','Crying2.png','Dead.png','Devil.png','Dizzy.png','Dog.png',
			'Don\'t-tell-Anyone.png','Drinks.png','Drooling.png','Flower.png','Ghost.png','Gift.png',
			'Girl.png','Goodbye.png','Heart.png','Hug.png','Kiss.png','Laughing.png','Ligthbulb.png',
			'Loser.png','Love.png','Mail.png','Music.png','Nerd.png','Night.png','Ninja.png',
			'Not-Talking.png','on-the-Phone.png','Party.png','Pig.png','Poo.png','Rainbow.png',
			'Rainning.png','Sacred.png','Sad.png','Scared.png','Sick.png','Sick2.png','Silly.png',
			'Sleeping.png','Sleeping2.png','Sleepy.png','Sleepy2.png','smile.png','Smoking.png','Smug.png','Stars.png'
		],
		smiley_descriptions:
		[
			'Straight Face','Sun','Sweating','Thinking','Tongue',
			'Vomit','Wave','Whew','Win','Winking','Yawn','Yawn2',
			'Zombie','Angry','Balloon','Big Grin','Bomb','Broken Heart',
			'Cake','Cat','Clock','Clown','Cold','Confused','Cool',
			'Crying','Crying2','Dead','Devil','Dizzy','Dog',
			'Don\'t-tell-Anyone','Drinks','Drooling','Flower','Ghost','Gift',
			'Girl','Goodbye','Heart','Hug','Kiss','Laughing','Lightbulb',
			'Loser','Love','Mail','Music','Nerd','Night','Ninja',
			'Not Talking','On The Phone','Party','Pig','Poo','Rainbow',
			'Raining','Sacred','Sad','Scared','Sick','Sick2','Silly',
			'Sleeping','Sleeping2','Sleepy','Sleepy2','smile','Smoking','Smug','Stars'
		],
		smiley_columns: 9,
		protectedSource: [/<link[\s\S]*?\/>/g]
	},		

	minorModes:
	[
		{
			title: "Home",
			elementId: "home_main",
			element: null,
			navBarElementId: null,
			toolActionsElementId: null,
			toolItemTableElementId: null,
			
			icon: "folder_page.png",

			start: function(obj, mode)
			{
				obj.loadHomePage(obj);
			},

			resize: function(obj)
			{
				obj.populateMain(obj);
				adjustForNewHeight();
			}
		},
		{
			title: "Configure",
			elementId: "home_configure",
			element: null,
			navBarElementId: ["home_configure_navbar", "home_configure_navbar_top"],

			icon: "folder_page.png",

			toolActionsElementId: "home_content_actions",
			toolItemTableElementId: "home_content_item_table",
			actions:
			[
				{title: "Add", icon: "document_add.png", click: function(){tool_obj.editContent(tool_obj, null);return false;}, selectRequired: null},
				{title: "Delete", icon: "delete.png", click: function(){tool_obj.deleteContent(tool_obj);return false;}, selectRequired: "selectContent"},
				{title: "Publish", icon: "publish.png", click: function(){tool_obj.publishContent(tool_obj);return false;}, selectRequired: "selectContent"},
				{title: "Unpublish", icon: "publish_rmv.png", click: function(){tool_obj.unpublishContent(tool_obj);return false;}, selectRequired: "selectContent"}
			],
			headers:
			[
				{title: null, type: "checkbox", sort: false, checkboxId: "selectContent"},
				{title: null, type: "center", sort: false},
				{title: null, type: "center", sort: false},
				{title: "Title", type: null, sort: false},
				{title: "Release Date", type: null, sort: false},
				{title: "Source", type: null, sort: false},
				{title: "Last Edited", type: null, sort: false}
			],

			navbar:
			[
				{title: "Return", icon: "return.png", access: "r", popup: "Return", click: function(){tool_obj.main(tool_obj);return false;}}
			],

			start: function(obj, mode)
			{
				$("#e3_tool_configure").addClass("e3_offstage");
				$("#e3_tool_reset_ui").addClass("e3_offstage");
				obj.clearChatTimer(obj);
				obj.loadHomePageConfigure(obj);
			},

			stop: function(obj, mode, save)
			{
				$("#e3_tool_reset_ui").removeClass("e3_offstage");
			}
		}
	],	

	// start the tool
	start: function(obj, data)
	{
		obj.siteId = data.siteId;

		$("#home_configure_options_link").unbind("click").click(function(){obj.configureOptions(obj);return false;});

		$("#home_status_bar_pm").unbind('click').click(function(){selectDirectTool("/" + obj.tools.jforum + "/pm/inbox/date/d.page");return false;});
		$("#home_status_bar_post").unbind('click').click(function(){selectDirectTool("/" + obj.tools.jforum + "/recentTopics/list.page");return false;});
		$("#home_status_bar_visit").unbind('click').click(function(){selectDirectTool("/" + obj.tools.activitymeter + "/alert/0A/overview/1A");return false;});
		$("#home_status_bar_review").unbind('click').click(function(){obj.showReviewOptions(obj); return false;});
		$("#home_review_options_mneme").unbind('click').click(function(){selectDirectTool("/" + obj.tools.mneme);return false;});
		$("#home_review_options_jforum").unbind('click').click(function(){selectDirectTool("/" + obj.tools.jforum);return false;});

		$('#home_configure_content_title').unbind('change').change(function(){obj.validateContent(obj, 1, false); return true;});
		$('#home_configure_content_url').unbind('change').change(function(){obj.showPreview(obj); obj.validateContent(obj, 3, false); return true;});
		$('#home_configure_content_height').unbind('change').change(function(){obj.showPreview(obj); return true;});
		$('#home_configure_content_youtube').unbind('change').change(function(){obj.showPreview(obj); obj.validateContent(obj, 4, false); return true;});
		$("#home_configure_content_youtube_ratio_square").unbind('click').click(function(){obj.showPreview(obj);return true;});
		$("#home_configure_content_youtube_ratio_43").unbind('click').click(function(){obj.showPreview(obj);return true;});
		$("#home_configure_content_youtube_ratio_169").unbind('click').click(function(){obj.showPreview(obj);return true;});
		$("#home_configure_content_source_authored").unbind('click').click(function(){obj.adjustForSource(obj);return true;});
		$("#home_configure_content_source_web").unbind('click').click(function(){obj.adjustForSource(obj);return true;});
		$("#home_configure_content_source_youtube").unbind('click').click(function(){obj.adjustForSource(obj);return true;});
		$("#home_configure_content_source_file").unbind('click').click(function(){obj.adjustForSource(obj);return true;});
		$("#home_configure_youtube_help").unbind("click").click(function(){openAlert("home_alertYoutube_help");return false;});
		setupDialog("prefs_zone_dialog", "Done", function(){return obj.saveZone(obj);});
		$("#prefs_zone_link").unbind("click").click(function(){obj.editZone(obj);return false;});

		$("#home_alertBlank_help_configure").unbind("click").click(function(){$("#home_alertBlank_help").dialog("close");obj.configure(obj);return false;});
		$("#home_alertBlank_help_configure2").unbind("click").click(function(){$("#home_alertBlank_help2").dialog("close");obj.configure(obj);return false;});

		setTitle(obj.title);
		populateToolModes(obj);
		selectMinorMode(0, obj);
	},

	// respond to browser window resize
	resize: function(obj)
	{
		if (obj.currentMode.resize != undefined) obj.currentMode.resize(obj);
	},

	// stop the tool
	stop: function(obj, save)
	{
		$("#e3_tool_configure").addClass("e3_offstage");
		obj.clearChatTimer(obj);
	},

	reset: function(obj)
	{
		obj.loadHomePage(obj);
	},

	// choose the configure mode
	configure: function(obj)
	{
		selectMinorMode(1, obj);
	},

	// chose the main mode
	main: function(obj)
	{
		selectMinorMode(0, obj);
	},

	// main view

	checkDate: function(obj, date)
	{
		if ((obj.eventsDays.year = date.getFullYear()) && (obj.eventsDays.month = (date.getMonth()+1)) && (obj.eventsDays.days.indexOf(date.getDate()) != -1)) return [true, "hasEvent", "event"];
		return [false, "", ""];
	},

	processHomePage: function(obj, data)
	{
 		obj.current = data.current;
		obj.pending = data.pending;
		obj.drafts = data.drafts;
		obj.past = data.past;
		obj.itemShowing = data.itemShowing;
		obj.itemPendingTitle = data.itemPendingTitle;
		obj.itemPendingReleaseDate = data.itemPendingReleaseDate;
		obj.announcementsOptions = data.announcementsOptions;
		obj.scheduleOptions = data.scheduleOptions;
		obj.chatOptions = data.chatOptions;
		obj.optionsModifiedDate = data.optionsModifiedDate;
		obj.optionsModifiedUser = data.optionsModifiedUser;
		obj.mayConfigure = (data.mayConfigure == 1);
		obj.format = data.format;
		obj.anncFull = data.anncFull;
		obj.order = data.order;
		obj.announcements = data.announcements;
		obj.eventsDays = data.eventsDays;
		obj.eventsDate = data.eventsDate;
		obj.eventsDateZone = data.eventsDateZone;
		obj.events = data.events;
		obj.chat = data.chat;
		obj.siteTitle = data.siteTitle;
		obj.status = data.status;
		obj.tools = data.tools;
		obj.timezone = data.timezone;
		obj.enableVT = data.enableVT;
		obj.serverUrl = data.serverUrl;
		obj.siteId = data.siteId;
	},

	loadHomePage: function(obj)
	{
		var params = new Object();
		params.siteId = obj.siteId;
		params.currentOnly = "true";
		if (obj.selectedDate != null)	
		{
			params.year = obj.selectedDate.getFullYear().toString();
			params.month = (obj.selectedDate.getMonth()+1).toString();
			params.day = obj.selectedDate.getDate().toString();
		}
		requestCdp("homepage_homePage", params, function(data)
		{
			obj.processHomePage(obj, data);
			obj.populateMain(obj);
			$("#home_calendar").datepicker("refresh");
			adjustForNewHeight();
		});
	},

	wl: 0,
	measureHomeLeft: function(obj)
	{
		// hide the real left and right
		$("#home_left").addClass("e3_offstage");
		$("#home_right").addClass("e3_offstage");
		
		// adjust simple right based on format
		if (obj.format == "0")
		{
			// content-centric, right 250px format
			$("#home_right_measure").removeClass("home_main_rside_split").addClass("home_main_rside_250");
		}
		else
		{
			// split screen
			$("#home_right_measure").removeClass("home_main_rside_250").addClass("home_main_rside_split");
		}

		// surface the simple left and right for measuring
		$("#home_left_measure").removeClass("e3_offstage");
		if (((obj.announcementsOptions.display == 1) && (obj.tools.announcement != null)) || (obj.scheduleOptions.display == 1) || ((obj.chatOptions.display == 1) & (obj.tools.chat != null)))
		{
			$("#home_right_measure").removeClass("e3_offstage");
		}

		// measure the left
		obj.wl = $("#home_left_measure").width();
		// TODO: adjust wl by -16?
		
		// restore
		$("#home_right_measure").addClass("e3_offstage");
		$("#home_left_measure").addClass("e3_offstage");
		$("#home_left").removeClass("e3_offstage");
		$("#home_right").removeClass("e3_offstage");
	},

	populateMain: function(obj)
	{
		obj.measureHomeLeft(obj);

		// setup the left
		$("#home_left").empty().html('<div id="home_left_content" style="overflow:hidden;" />');

		// set the format for the right
		if (obj.format == "0")
		{
			// content-centric, right 250px format
			$("#home_right").removeClass("home_main_rside_split").addClass("home_main_rside_250");
		}
		else
		{
			// split screen
			$("#home_right").removeClass("home_main_rside_250").addClass("home_main_rside_split");
		}

		// hide the right if we have nothing to display there
		if (((obj.announcementsOptions.display == 0) || (obj.tools.announcement == null)) && (obj.scheduleOptions.display == 0) && ((obj.chatOptions.display == 0) || (obj.tools.chat == null)))
		{
			$("#home_right").addClass("e3_offstage");
		}

		// else configure and empty the right for the options we are using
		else
		{
			$("#home_right").removeClass("e3_offstage");

			if ((obj.announcementsOptions.display == 0) || (obj.tools.announcement == null))
			{
				$("#home_announcements_header").addClass("e3_offstage");
				$("#home_announcements").addClass("e3_offstage");
			}
			else
			{
				$("#home_announcements_header").removeClass("e3_offstage");
				$("#home_announcements").removeClass("e3_offstage");
				$("#home_announcements").empty();
			}
	
			if (obj.scheduleOptions.display == 0)
			{
				$("#home_events_header").addClass("e3_offstage");
				$("#home_events").addClass("e3_offstage");
				$("#home_calendar").datepicker("destroy");
				$("#home_calendar").addClass("e3_offstage");
				$("#home_timezone_header").addClass("e3_offstage");
				$("#home_timezone_display").addClass("e3_offstage");
			}
			else
			{
				$("#home_events_header").removeClass("e3_offstage");
				$("#home_events").removeClass("e3_offstage");
				$("#home_calendar").removeClass("e3_offstage");
				$("#home_timezone_header").removeClass("e3_offstage");
				$("#home_timezone_display").removeClass("e3_offstage");
				$("#home_calendar").datepicker(obj.eventsDatepickerConfig);
				$("#home_event_date").empty();
				$("#home_events").empty();
			}

			if ((obj.chatOptions.display == 0) || (obj.tools.chat == null))
			{
				$("#home_chat_header").addClass("e3_offstage");
				$("#home_chat").addClass("e3_offstage");
			}
			else
			{
				$("#home_chat_header").removeClass("e3_offstage");
				$("#home_chat").removeClass("e3_offstage");
				$("#home_chat").empty();
			}
			
			var rightSide = $("#home_right");
			for (var i=0; i < obj.order.length; i++)
			{
				if ("A" == obj.order.charAt(i))
				{
					if ((obj.announcementsOptions.display == 1) && (obj.tools.announcement != null))
					{
						$(rightSide).append($("#home_announcements_header"));
						$(rightSide).append($("#home_announcements"));
					}
				}
				else if ("S"== obj.order.charAt(i))
				{
					if (obj.scheduleOptions.display == 1)
					{
						$(rightSide).append($("#home_calendar"));
						$(rightSide).append($("#home_timezone_header"));
						$(rightSide).append($("#home_timezone_display"));
						$(rightSide).append($("#home_events_header"));
						$(rightSide).append($("#home_events"));
					}
				}
				else if ("C"== obj.order.charAt(i))
				{
					if ((obj.chatOptions.display == 1) && (obj.tools.chat != null))
					{
						$(rightSide).append($("#home_chat_header"));
						$(rightSide).append($("#home_chat"));
					}
				}
			}
		}

		// populate the right
		if (((obj.announcementsOptions.display == 1) && (obj.tools.announcement != null)) || (obj.scheduleOptions.display == 1) || ((obj.chatOptions.display == 1) && (obj.tools.chat != null)))
		{
			if ((obj.announcementsOptions.display == 1) && (obj.tools.announcement != null))
			{
				obj.populateAnnouncements(obj);
			}
	
			if (obj.scheduleOptions.display == 1)
			{
				obj.populateEvents(obj);
			}
	
			if ((obj.chatOptions.display == 1) && (obj.tools.chat != null))
			{
				obj.populateChat(obj);
			}

			// for split screen, set the width on the right
			if (obj.format == "1")
			{
				$("#home_right").width(obj.wl);
			}
			else
			{
				$("#home_right").width("");
			}
		}

		// populate the left

		// get the selected content
		var selected = obj.current[0];
		if (selected != null)
		{
			if (selected.title != null)
			{
				var div = $('<div class="e3_dialog_header e3_font" />').text(selected.title);
				$("#home_left_content").append(div);
			}
	
			if (selected.htmlBody != null)
			{
				var div = $("<div />").html(selected.htmlBody);
				$("#home_left_content").append(div);
				processMathMl();
			}
			else
			{
				loadContent("home_left_content", selected, obj.wl, function(){adjustForNewHeight();});
			}
		}

		// for no content available
		else
		{
			$("#home_left_content").append('<div class="e3_dialog_header e3_font">Welcome to ' + obj.siteTitle + '</div>');

			// a special note for the instructor
			if (obj.mayConfigure)
			{
				var d = $('<div style="font-size:13px; margin:100px 50px 0px 50px;" />');
				$("#home_left_content").append(d);	

				if ("1" == obj.itemShowing)
				{
					$(d).append("The home item &quot;<b>" + obj.itemPendingTitle + "</b>&quot; is scheduled to be displayed on <b>" + obj.itemPendingReleaseDate + "</b>. ");
					$("#home_alertBlank_help2_title").text(obj.itemPendingTitle);
					$("#home_alertBlank_help2_date").text(obj.itemPendingReleaseDate);
					var a = $('<a href="" />').text("(?)");
					$(a).addClass("e3_toolUiLinkU");
					$(a).click(function(){openAlert("home_alertBlank_help2");return false;});
					$(d).append(a);
				}
				else if ("2" == obj.itemShowing)
				{
					$(d).append('No home items are published. ');
					var a = $('<a href="" />').text("(?)");
					$(a).addClass("e3_toolUiLinkU");
					$(a).click(function(){openAlert("home_alertBlank_help");return false;});
					$(d).append(a);
				}
				else if ("3" == obj.itemShowing)
				{
					$(d).append('No home items are defined. ');
					var a = $('<a href="" />').text("(?)");
					$(a).addClass("e3_toolUiLinkU");
					$(a).click(function(){openAlert("home_alertBlank_help");return false;});
					$(d).append(a);
				}
			}

			$("#home_left_content").width(obj.wl);
		}

		if (obj.mayConfigure)
		{
			$("#e3_tool_configure").removeClass("e3_offstage").unbind("click").click(function(){obj.configure(obj);return false;});
		}
		else
		{
			$("#e3_tool_configure").addClass("e3_offstage");
		}

		obj.populateBar(obj, obj.status);
	},

	badgeValue : function(value)
	{
		if (value < 1000)
		{
			return value.toString();
		}
		return "!!!";
	},

	// populate the status bar
	populateBar: function(obj, status)
	{
		// load the status - pm count text
		if (status.pmCount > 0)
		{
			$("#home_status_bar_pm").removeClass("dimBadged");
			$("#home_status_bar_pm_badge").text(obj.badgeValue(status.pmCount));
			$("#home_status_bar_pm_badge").removeClass("e3_offstage");
		}
		else
		{
			$("#home_status_bar_pm").addClass("dimBadged");
			$("#home_status_bar_pm_badge").addClass("e3_offstage");
		}

		// load the status - post count text
		if (status.postCount > 0)
		{
			$("#home_status_bar_post").removeClass("dimBadged");
			$("#home_status_bar_post_badge").text(obj.badgeValue(status.postCount));
			$("#home_status_bar_post_badge").removeClass("e3_offstage");
		}
		else
		{
			$("#home_status_bar_post").addClass("dimBadged");
			$("#home_status_bar_post_badge").addClass("e3_offstage");
		}
		
		// load the status - not visited count text
		if (status.instructorStatus == 1)
		{
			$("#home_status_bar_review").addClass("e3_offstage");
			$("#home_status_bar_visit").removeClass("e3_offstage");

			if (status.noVisitCount > 0)
			{
				$("#home_status_bar_visit").removeClass("dimBadged");
				$("#home_status_bar_visit_badge").text(obj.badgeValue(status.noVisitCount));
				$("#home_status_bar_visit_badge").removeClass("e3_offstage");
			}
			else
			{
				$("#home_status_bar_visit").addClass("dimBadged");
				$("#home_status_bar_visit_badge").addClass("e3_offstage");
			}
		}
		else
		{
			$("#home_status_bar_visit").addClass("e3_offstage");
			$("#home_status_bar_review").removeClass("e3_offstage");
			
			// load the mneme / jforum not reviewed count
			var reviewCount = parseInt(status.reviewCountMneme) + parseInt(status.reviewCountJForum);

			if (reviewCount > 0)
			{
				$("#home_status_bar_review").removeClass("dimBadged");
				$("#home_status_bar_review_badge").text(obj.badgeValue(reviewCount));
				$("#home_status_bar_review_badge").removeClass("e3_offstage");
			}
			else
			{
				$("#home_status_bar_review").addClass("dimBadged");
				$("#home_status_bar_review_badge").addClass("e3_offstage");
			}
		}
	},

	loadEvents: function(obj)
	{
		var params = new Object();
		params.siteId = obj.siteId;
		if (obj.selectedDate != null)	
		{
			params.year = obj.selectedDate.getFullYear().toString();
			params.month = (obj.selectedDate.getMonth()+1).toString();
			params.day = obj.selectedDate.getDate().toString();
		}
		requestCdp("homepage_events", params, function(data)
		{
			obj.eventsDate = data.eventsDate;
			obj.eventsDateZone = data.eventsDateZone;
			obj.events = data.events;

			obj.populateEvents(obj);
			adjustForNewHeight();
		});
	},

	loadEventsDays: function(obj, year, month)
	{
		obj.eventsDays.days = [];
		var params = new Object();
		params.siteId = obj.siteId;
		params.year = year.toString();
		params.month = month.toString();
		requestCdp("homepage_eventsDays", params, function(data)
		{
			obj.eventsDays = data.eventsDays;
			$("#home_calendar").datepicker("refresh");
			adjustForNewHeight();
		});
	},

	populateEvents: function(obj)
	{
		$("#home_timezone").empty().text(obj.eventsDateZone);
		$("#home_event_date").empty().text(obj.eventsDate);

		var div = $("#home_events").empty();

		if (obj.events.length > 0)
		{
			$.each(obj.events, function(index, value)
			{
				var aDiv = $('<div class="dashboard_item" />');
				div.append(aDiv);
				var typeClass = obj.classForType(obj, value);
				if (typeClass != null)
				{
					typeClass = 'class="e3_toolUiLinkU ' + typeClass + '" ';
				}
				else
				{
					typeClass = 'class="e3_toolUiLinkU" ';
				}
				var aLink = $('<a ' + typeClass + 'style="font-weight:bold" href="" />').text(value.title).click(function(){obj.showEvent(obj, value);return false;});
				aDiv.append(aLink);
				var aTime = $('<div class="dashboard_item_info_gotcha" />').text(value.time);
				aDiv.append(aTime);
				var aFrom = $('<div class="dashboard_item_info" />').text(obj.displayTextForType(obj, value));
				aDiv.append(aFrom);
			});
		}
		else
		{
			$(div).append('<div class="dashboard_item"><i>none</i></div>');
		}
	},

	loadChat: function(obj)
	{
		var params = new Object();
		params.siteId = obj.siteId;
		requestCdp("homepage_chat", params, function(data)
		{
			obj.chat = data.chat;
			obj.populateChat(obj);
			adjustForNewHeight();
		});
	},

	clearChatTimer: function(obj)
	{
		if (obj.chatTimer != null)
		{
			clearTimeout(obj.chatTimer);
			obj.chatTimer = null;
		}
	},

	populateChat: function(obj)
	{
		var div = $("#home_chat").empty();
		obj.clearChatTimer(obj);

		if (obj.chat.length > 0)
		{
			$.each(obj.chat, function(index, value)
			{
				// if it is time for a time stamp
				if ((index == 0) || (value.timegap == "1"))
				{
					var aDate = $('<div class="home_chat_date" />').text(value.date);
					div.append(aDate);
				}

				var aDiv = $('<div class="home_chat" />');
				div.append(aDiv);
				var nameSpan = $('<span class="home_chat_from" />').text(value.from + ": ");
				var messageSpan = $('<span class="home_chat_msg" />').html(value.message);
				aDiv.append(nameSpan);
				aDiv.append(messageSpan);
			});
		}
		else
		{
			$(div).append('<div class="dashboard_item"><i>none</i></div>');
		}
		
		obj.chatTimer = setTimeout(function(){obj.loadChat(obj);}, 10000);
	},

	classForType: function(obj, event)
	{
		if (event.type == "Academic Calendar") return "dashboard_academic_calendar";
		if (event.type == "Activity") return "dashboard_activity";
		if (event.type == "Cancellation") return "dashboard_cancellation";
		if (event.type == "Class section - Discussion") return "dashboard_class_section_discussion";
		if (event.type == "Class section - Lab") return "dashboard_class_section_lab";
		if (event.type == "Class section - Lecture") return "dashboard_class_section_lecture";
		if (event.type == "Class section - Small Group") return "dashboard_class_section_small_group";
		if (event.type == "Class session") return "dashboard_class_session";
		if (event.type == "Computer Session") return "dashboard_computer_session";
		if (event.type == "Deadline") return "dashboard_deadline";
		if (event.type == "Exam") return "dashboard_exam";
		if (event.type == "Meeting") return "dashboard_meeting";
		if (event.type == "Multidisciplinary Conference") return "dashboard_multidisciplinary_conference";
		if (event.type == "Quiz") return "dashboard_quiz";
		if (event.type == "Special event") return "dashboard_special_event";
		if (event.type == "Web Assignment") return "dashboard_web_assignment";
		if (event.type == "Holiday") return "dashboard_holiday";
		if (event.type == "CourseMap Date")
		{
			if (event.cmType == "assignment") return "dashboard_cm_assignment";
			if (event.cmType == "category") return "dashboard_cm_jforum";
			if (event.cmType == "forum") return "dashboard_cm_jforum";
			if (event.cmType == "module") return "dashboard_cm_melete";
			if (event.cmType == "survey") return "dashboard_cm_survey";
			if (event.cmType == "test") return "dashboard_cm_test";
			if (event.cmType == "topic") return "dashboard_cm_jforum";

			return "dashboard_coursemap";
		}

		return null;
	},

	displayTextForType: function(obj, event)
	{
		if (event.type == "CourseMap Date")
		{
			if (event.cmType == "assignment") return "AT&S";
			if (event.cmType == "category") return "Discussions";
			if (event.cmType == "forum") return "Discussions";
			if (event.cmType == "module") return "Modules";
			if (event.cmType == "survey") return "AT&S";
			if (event.cmType == "test") return "AT&S";
			if (event.cmType == "topic") return "Discussions";
		}
		
		return event.type;
	},

	showEvent: function(obj, event)
	{
		$("#home_display_event_title").empty().text(event.title);
		$("#home_display_event_title").removeClass();
		$("#home_display_event_title").addClass("dashboard_display_event_title");
		var typeClass = obj.classForType(obj, event);
		if (typeClass != null) $("#home_display_event_title").addClass(typeClass);
		$("#home_display_event_time").empty().text(event.time);
		$("#home_display_event_from").empty().text(obj.displayTextForType(obj, event));
		$("#home_display_event_body").empty().html(event.description);
		processMathMl();
		if ((event.location != null) && (event.location != ""))
		{
			$("#home_display_event_location").empty().text(event.location);
			$("#home_display_event_location_div").removeClass("e3_offstage");
		}
		else
		{
			$("#home_display_event_location_div").addClass("e3_offstage");			
		}
		if (event.attachments != null)
		{
			$("#home_display_event_attachments").removeClass("e3_offstage");
			var ul = $("#home_display_event_attachments_list");
			$(ul).empty();
			$.each(event.attachments, function(index, value)
			{
				var li = $("<li />");
				ul.append(li);
				var aLink = $('<a target="_blank"></a>').html(value.description).attr("href", value.url);
				li.append(aLink);
			});
		}
		else
		{
			$("#home_display_event_attachments").addClass("e3_offstage");
		}

		$("#home_display_event_perform").addClass("e3_offstage");
		$("#home_display_event_review").addClass("e3_offstage");
		$("#home_display_event_edit").addClass("e3_offstage");
		$("#home_display_actions").addClass("e3_offstage");
		if (event.actionPerform != null)
		{
			$("#home_display_actions").removeClass("e3_offstage");
			$("#home_display_event_perform").removeClass("e3_offstage");
			$("#home_display_event_perform").empty().text("Go to " + event.cmType.capitalize());
			$("#home_display_event_perform").unbind("click").click(function(){selectDirectTool(event.actionPerform);return false;});
		}
		if (event.actionReview != null)
		{
			$("#home_display_actions").removeClass("e3_offstage");
			$("#home_display_event_review").removeClass("e3_offstage");
			$("#home_display_event_review").unbind("click").click(function(){selectDirectTool(event.actionReview);return false;});
		}
		if (event.actionEdit != null)
		{
			$("#home_display_actions").removeClass("e3_offstage");
			$("#home_display_event_edit").removeClass("e3_offstage");
			$("#home_display_event_edit").unbind("click").click(function(){selectDirectTool(event.actionEdit);return false;});
		}

		openAlert("home_display_event");
	},

	showReviewOptions: function(obj)
	{
		$("#home_review_options_count_mneme").empty().text(obj.status.reviewCountMneme);
		$("#home_review_options_count_jforum").empty().text(obj.status.reviewCountJForum);

		openAlert("home_review_options");
	},

	populateAnnouncement: function(obj, div, value, full)
	{
		var aDiv = $('<div class="dashboard_item" />');
		div.append(aDiv);
		var subject = $('<a class="e3_toolUiLinkU" style="font-weight:bold" href="" />').text(value.subject).click(function(){obj.showAnnouncement(obj, value);return false;});
		aDiv.append(subject);
		var from = $('<div class="dashboard_item_info" />').text(value.from + ", " + value.date);
		aDiv.append(from);
		if (full)
		{
			var body = $('<div class="dashboard_display_announcement_body" />').html(value.body);
			aDiv.append(body);
			processMathMl();
			if (value.attachments != null)
			{
				var attachDiv = $('<div />').addClass("dashboard_attachments");
				aDiv.append(attachDiv);
				attachDiv.append($('<span style="color: #808080;" />').text("Attachments:"));
				var ul = $('<ul />');
				attachDiv.append(ul);
				$.each(value.attachments, function(index, attch)
				{
					var li = $("<li />");
					ul.append(li);
					var aLink = $('<a target="_blank" />').text(attch.description).attr("href", attch.url);
					li.append(aLink);
				});
			}
			aDiv.append($('<hr style="color:#9a661f" />'));
		}
	},

	// populate the announcements
	populateAnnouncements: function(obj)
	{
		var div = $("#home_announcements").empty();
		if (obj.announcements.length > 0)
		{
			$.each(obj.announcements, function(index, value)
			{
				obj.populateAnnouncement(obj, div, value, ((index == 0) && (obj.anncFull == "1")));
			});
		}
		else
		{
			$(div).append('<div class="dashboard_item"><i>none</i></div>');
		}
	},

	// display a full announcement in a dialog
	showAnnouncement: function(obj, annc)
	{
		$("#home_display_announcement_subject").empty().text(annc.subject);
		$("#home_display_announcement_from").empty().text(annc.from + ", " + annc.date);
		$("#home_display_announcement_body").empty().html(annc.body);
		processMathMl();

		if (annc.attachments != null)
		{
			$("#home_display_announcement_attachments").removeClass("e3_offstage");
			var ul = $("#home_display_announcement_attachments_list");
			$(ul).empty();
			$.each(annc.attachments, function(index, value)
			{
				var li = $("<li />");
				ul.append(li);
				var aLink = $('<a target="_blank" />').text(value.description).attr("href", value.url);
				li.append(aLink);
			});
		}
		else
		{
			$("#home_display_announcement_attachments").addClass("e3_offstage");
		}

		openAlert("home_display_announcement");
	},

	// configure view

	adjustFinder: function(name)
	{
		// disable maximize, which does NOT work in our dialogs
		$("#" + name + " iframe").contents().find(".cke_button_maximize").remove();
		adjustDialogHeight();
	},

	adjustEditor: function(e)
	{
		adjustDialogHeight();
	},
	
	checkVT : function (obj)
	{
		if (obj.enableVT)
			{
				obj.ckconfig.extraPlugins="MediaEmbed,audiorecorder,movieplayer,wordcount,onchange,ckeditor_wiris,VTLTIConnect";
				obj.ckconfig.serverUrl = obj.serverUrl;
				obj.ckconfig.siteId = obj.siteId;
				obj.ckconfig.resourceId = "homepage_page.htm";
			}
	},
	 
	adjustForSource: function(obj)
	{
		$("#home_configure_web_options").addClass("e3_offstage");
		$("#home_configure_author_options").addClass("e3_offstage");
		$("#home_configure_youtube_options").addClass("e3_offstage");
		$("#home_configure_image_options").addClass("e3_offstage");
		$("#home_configure_preview_section").addClass("e3_offstage");
		if (obj.imageFinderApi != null) obj.imageFinderApi.destroy();
		$("#home_configure_image_options_finder").empty();
		obj.imageFinderApi = null;
		if (obj.editorApi != null) obj.editorApi.destroy();
		$("#home_configure_content_editor_holder").empty();
		obj.editorApi = null;

		var source = $('input:radio[name=home_configure_content_source]:checked').val();
		if (source == "W")
		{
			$("#home_configure_web_options").removeClass("e3_offstage");
			$("#home_configure_preview_section").removeClass("e3_offstage");
			$("#home_configure_preview_help").unbind("click").click(function(){openAlert("home_alertPreview_helpW");return false;});
			$("#home_configure_preview_help").removeClass("e3_offstage");
			obj.showPreview(obj);
		}
		else if (source == "A")
		{
			obj.checkVT(obj); 
			obj.editorApi = CKEDITOR.appendTo('home_configure_content_editor_holder', obj.ckconfig, ((obj.editingContent == null) ? '' : obj.editingContent.content));

			obj.editorApi.on('instanceReady', function(e){obj.adjustEditor(e);});
			$("#home_configure_author_options").removeClass("e3_offstage");			
			adjustDialogHeight();
		}
		else if (source == "Y")
		{
			$("#home_configure_youtube_options").removeClass("e3_offstage");
			$("#home_configure_preview_section").removeClass("e3_offstage");
			$("#home_configure_preview_help").unbind("click").click(function(){openAlert("home_alertPreview_helpY");return false;});
			$("#home_configure_preview_help").removeClass("e3_offstage");
			obj.showPreview(obj);
		}
		else if (source == "F")
		{
			var f = new CKFinder({}, function(){obj.adjustFinder("home_configure_image_options_finder");});
			f.basePath = '/ckfinder/';
			f.selectActionFunction = obj.imageFinderSelectedImage;
			obj.imageFinderApi = f.appendTo($("#home_configure_image_options_finder")[0],
			{
				connectorInfo: "siteId=" + obj.siteId + "&rtype=Files",
				connectorPath: "/resources/connector",
				startupPath: obj.siteTitle + " Files:/Home/",
				disableHelpButton: true,
				rememberLastFolder: false,
				startupFolderExpanded: true,
				defaultViewType: "thumbnails",
				disableThumbnailSelection: true
			});

			$("#home_configure_image_options").removeClass("e3_offstage");
			$("#home_configure_preview_section").removeClass("e3_offstage");
			$("#home_configure_preview_help").addClass("e3_offstage");

			obj.showPreview(obj);
		}
		obj.validateContent(obj, 6, false);
	},

	nameFromUrl: function(fileUrl)
	{
		if (fileUrl == null) return "not selected";

		var parts = fileUrl.split("/");
		var rv = "";
		for (var i = 6; i < parts.length-1; i++)
		{
			rv = rv + parts[i] + "/";
		}
		rv = rv + parts[parts.length-1];

		try
		{
			rv = decodeURI(rv);
		}
		catch (err)
		{
		}

		return rv;
	},

	loadHomePageConfigure: function(obj)
	{
		var params = new Object();
		params.siteId = obj.siteId;
		if (obj.selectedDate != null)	
		{
			params.year = obj.selectedDate.getFullYear().toString();
			params.month = (obj.selectedDate.getMonth()+1).toString();
			params.day = obj.selectedDate.getDate().toString();
		}
		requestCdp("homepage_homePage", params, function(data)
		{
			obj.processHomePage(obj, data);
			obj.populateConfiguration(obj);
			adjustForNewHeight();
		});
	},

	populateConfiguration: function(obj)
	{
		obj.populateOptions(obj);
		obj.populateContent(obj);
	},

	populateOptions: function(obj)
	{
		$("#home_configure_format_setting").empty().text(obj.format == "0" ? "Wide Home Item" : "Split Screen");
		
		var order = "";
		for (var i=0; i < obj.order.length; i++)
		{
			if ("A" == obj.order.charAt(i))
			{
				if ((obj.announcementsOptions.display == 1) && (obj.tools.announcement != null)) order += "Announcements, ";
			}
			else if ("S"== obj.order.charAt(i))
			{
				if (obj.scheduleOptions.display == 1) order += "Calendar, ";
			}
			else if ("C"== obj.order.charAt(i))
			{
				if ((obj.chatOptions.display == 1) && (obj.tools.chat != null)) order += "Chat, ";
			}
		}
		if (order == "")
		{
			order = "<i>none</i>";
		}
		else
		{
			order = order.substring(0, order.length-2);
		}
		$("#home_configure_display_order_setting").empty().html(order);
		
		if ((obj.announcementsOptions.display == 1) && (obj.tools.announcement != null))
		{
			$("#home_configure_display_num_anncs_section").removeClass("e3_offstage");
			$("#home_configure_num_anncs_setting").empty().text(obj.announcementsOptions.count.toString() + (obj.anncFull == "1" ? " - First announcement will be shown in full." : ""));
		}
		else
		{
			$("#home_configure_display_num_anncs_section").addClass("e3_offstage");
		}

		$("#home_configure_options_modified").empty();
		if (obj.optionsModifiedDate != null)
		{
			$("#home_configure_options_modified").html('Last Edited by ' + obj.optionsModifiedUser + ' on ' + obj.optionsModifiedDate);
		}
	},

	populateOneContent: function(obj, index, value, icon, iconText)
	{
		var tr = $("<tr />");
		$("#home_content_item_table tbody").append(tr);

		// select box
		createSelectCheckboxTd(obj, tr, "selectContent", value.contentId);

		// view
		createIconTd(tr, "magnifier.png", "preview", function(){obj.showPreviewPopup(obj, value);return false;});

		// icon
		createIconTd(tr, icon, iconText);

		// title
		createHotTd(tr, value.title, function(){obj.editContent(obj, value);return false;});

		// release date
		createTextTd(tr, value.releaseDate, "white-space:nowrap;");

		// source
		if (value.source == "W")
		{
			var name = value.url;
			if (name == null) name = "not selected";

			var type = value.type;
			if (type == "?") type = guessContentType(value.url);

			if (type.indexOf("image/") == 0)
			{
				createTextTd(tr, "Image: " + name);
			}
			else if (type.indexOf("text/html") == 0)
			{
				createTextTd(tr, "Web Page: " + name + ((value.style == null) ? "" : (" (" + value.style + " px)")));
			}
			else
			{
				createTextTd(tr, "Web Resource: " + name);
			}
		}
		else if (value.source == "Y")
		{
			var name = value.url;
			if (name == null) name = "not selected";
			createTextTd(tr, "YouTube Video: " + name + " (" + value.style + ")");
		}
		else if (value.source == "A")	
		{
			createTextTd(tr, "Authored Item");
		}
		else if (value.source == "F")
		{
			createTextTd(tr, "Uploaded File: " + obj.nameFromUrl(value.url));
		}
		else
		{
			createTextTd(tr, value.source);
		}
	 
		// modified info
		createHtmlTd(tr, '<span style="white-space:nowrap;">' + value.modifiedUser + '</span> on <span style="white-space:nowrap;">' + value.modifiedDate + '</span>',"font-style:italic; font-size:9px;");
	},

	populateNoneContent: function(obj)
	{
		var tr = $("<tr />");
		$("#home_content_item_table tbody").append(tr);

		// select box
		createTextTd(tr, "");

		// icon
		createTextTd(tr, "");

		// view
		createTextTd(tr, "");

		// title
		createTextTd(tr, "none", "font-style:italic");

		// release date
		createTextTd(tr, "");

		// source
		createTextTd(tr, "");		
	},

	// populate the content list
	populateContent: function(obj)
	{
		clearSelectAll("selectContent");
		$("#home_content_item_table tbody").empty();
		$("#noContent").addClass("offstage");
		
		// heading
		var tr = $("<tr />");
		$("#home_content_item_table tbody").append(tr);
		createHeaderTd(tr, "Now Showing");

		// the currently released item
		var any = false;
		$.each(obj.current, function(index, value)
		{
			any = true;
			obj.populateOneContent(obj, index, value, "publish.png", "Now Showing");
		});
		if (!any)
		{
			obj.populateNoneContent(obj);
		}

		// heading
		var tr = $("<tr />");
		$("#home_content_item_table tbody").append(tr);
		createHeaderTd(tr, "Coming Soon");

		// the scheduled items
		any = false;
		$.each(obj.pending, function(index, value)
		{
			any = true;
			obj.populateOneContent(obj, index, value, "invisible.png", "Scheduled");
		});
		if (!any)
		{
			obj.populateNoneContent(obj);
		}

		// heading
		var tr = $("<tr />");
		$("#home_content_item_table tbody").append(tr);
		createHeaderTd(tr, "Not Published");

		// the draft items
		any = false;
		$.each(obj.drafts, function(index, value)
		{
			any = true;
			obj.populateOneContent(obj, index, value, "remove.png", "Not Published");
		});
		if (!any)
		{
			obj.populateNoneContent(obj);
		}

		// heading
		var tr = $("<tr />");
		$("#home_content_item_table tbody").append(tr);
		createHeaderTd(tr, "Past Items");

		// the past-released items
		any = false;
		$.each(obj.past, function(index, value)
		{
			any = true;
			obj.populateOneContent(obj, index, value, "closed.gif", "Past Item");
		});
		if (!any)
		{
			obj.populateNoneContent(obj);
		}
		
		updateSelectStatus(obj, "selectContent");
	},

	setupOrderEditKbd : function(obj)
	{
		$('*.home_configure_order_row').unbind("keydown").keydown(function(event)
		{
			if (event.target == event.currentTarget)
			{
				// arrow up
				if (event.which == 38)
				{
					var prev = $(event.currentTarget).prev('tr')[0];
					if (prev != null) $(event.currentTarget).insertBefore(prev);
					$(event.currentTarget).focus();
					return false;
				}
				// arrow down
				else if (event.which == 40)
				{
					var next = $(event.currentTarget).next('tr')[0];
					if (next != null) $(event.currentTarget).insertAfter(next);
					$(event.currentTarget).focus();
					return false;
				}
			}
			return true;
		});
		
		$('*.home_configure_order_row').unbind("focus").focus(function(event)
		{
			$(this).addClass("e3_kbd_selected");
			return true;
		});

		$('*.home_configure_order_row').unbind("blur").blur(function(event)
		{
			$(this).removeClass("e3_kbd_selected");
			return true;
		});
	},

	addOrderEntry: function(obj, title, selected, checkId, oid)
	{
		var tr = $("<tr />");
		$(tr).attr("tabindex", 0).addClass("home_configure_order_row");
		$("#home_configure_order tbody").append(tr);
		createReorderIconTd(tr, "home_configure_order_oid", oid);
		createCheckboxTd(tr, selected, checkId, null);
		createLabelTd(tr, title, checkId);
		obj.setupOrderEditKbd(obj);
		return tr;
	},

	configureOptions: function(obj)
	{
		$('input:radio[name=home_configure_format][value="' + obj.format + '"]').prop('checked', true);
		
		$("#home_configure_order tbody").empty();
		var aDone = false;
		var sDone = false;
		var cDone = false;
		for (var i=0; i < obj.order.length; i++)
		{
			if ("A" == obj.order.charAt(i))
			{
				aDone = true;
				if (obj.tools.announcement != null)
				{
					obj.addOrderEntry(obj, "Announcements", (obj.announcementsOptions.display == 1), "home_configure_announcement_include", "A");
				}
			}
			else if ("S"== obj.order.charAt(i))
			{
				sDone = true;
				obj.addOrderEntry(obj, "Calendar", (obj.scheduleOptions.display == 1), "home_configure_schedule_include", "S");
			}
			else if ("C"== obj.order.charAt(i))
			{
				cDone = true;
				if (obj.tools.chat != null)
				{
					obj.addOrderEntry(obj, "Chat", (obj.chatOptions.display == 1), "home_configure_chat_include", "C");
				}
			}
		}

		// catch any that were missing
		if (!aDone)
		{
			if (obj.tools.announcement != null)
			{
				obj.addOrderEntry(obj, "Announcements", (obj.announcementsOptions.display == 1), "home_configure_announcement_include", "A");
			}
		}

		if (!sDone)
		{
			obj.addOrderEntry(obj, "Calendar", (obj.scheduleOptions.display == 1), "home_configure_schedule_include", "S");
		}

		if (!cDone)
		{
			if (obj.tools.chat != null)
			{
				obj.addOrderEntry(obj, "Chat", (obj.chatOptions.display == 1), "home_configure_chat_include", "C");
			}
		}
	
		$("#home_configure_order tbody").sortable({axis:"y", containment:"#home_configure_order tbody", handle:".e3_reorder", tolerance:"pointer"});

		$("#home_configure_num_anncs").val(obj.announcementsOptions.count);
		$("#home_configure_announcement_include").unbind("click").click(function(){obj.adjustForAnncs(obj);return true;});
		$("#home_configure_announcement_full").attr('checked', obj.anncFull == "1");
		obj.adjustForAnncs(obj);

		openDialog("home_configure_options_dialog", [{text:"Done", click:function()
		{
			if (obj.saveConfigureOptions(obj))
			{
				$("#home_configure_options_dialog").dialog("close");
			}
			return false;
		}}]);
	},

	adjustForAnncs: function(obj)
	{
		var anncDisplay = (obj.tools.announcement != null) ? ($("#home_configure_announcement_include").is(':checked') ? true : false) : false;
		if (anncDisplay)
		{
			$("#home_configure_num_anncs_section").removeClass("e3_offstage");
		}
		else
		{
			$("#home_configure_num_anncs_section").addClass("e3_offstage");
		}
	},

	saveConfigureOptions: function(obj)
	{
		var params = new Object();
		params.siteId = obj.siteId;
		params.format = $('input:radio[name=home_configure_format]:checked').val();
		params.anncDisplay = (obj.tools.announcement != null) ? ($("#home_configure_announcement_include").is(':checked') ? "1" : "0") : "0";
		params.schdDisplay = $("#home_configure_schedule_include").is(':checked') ? "1" : "0";
		params.chatDisplay = (obj.tools.chat != null) ? ($("#home_configure_chat_include").is(':checked') ? "1" : "0") : "0";
		params.order = arrayToString(collectAllOidsArray("home_configure_order_oid"));
		params.announcements = $("#home_configure_num_anncs").val();
		params.anncFull = (obj.tools.announcement != null) ? ($("#home_configure_announcement_full").is(':checked') ? "1" : "0") : "0";
		requestCdp("homepage_setOptions", params, function(data)
		{
			obj.processHomePage(obj, data);
			obj.populateConfiguration(obj);
		});

		return true;
	},

	editorFinderSelected: function(fileUrl, data, allFiles)
	{
		CKEDITOR.tools.callFunction(tool_obj.editorFinderCallback, fileUrl);
		$("#home_finder_dialog").dialog('close');
	},

//	getContent: function(obj, contentId)
//	{
//		var found = null;
//		$.each(obj.current, function(index, value)
//		{
//			if (value.contentId == contentId) found = value;
//		});
//		if (found == null)
//		{
//			$.each(obj.pending, function(index, value)
//			{
//				if (value.contentId == contentId) found = value;
//			});			
//		}
//		if (found == null)
//		{
//			$.each(obj.draft, function(index, value)
//			{
//				if (value.contentId == contentId) found = value;
//			});			
//		}
//		if (found == null)
//		{
//			$.each(obj.past, function(index, value)
//			{
//				if (value.contentId == contentId) found = value;
//			});			
//		}
//		return found;
//	},

	editingContent: null,

	imageFinderSelectedImage: function(fileUrl, data, allFiles)
	{
		try
		{
			fileUrl = decodeURI(fileUrl);
		}
		catch (err)
		{		
		}

		tool_obj.imageFinderSelection = fileUrl;
		$("#home_configure_image_options_selected").empty().text(tool_obj.nameFromUrl(tool_obj.imageFinderSelection));
		tool_obj.validateContent(tool_obj, 5, false);
		tool_obj.showPreview(tool_obj);
	},

	editContent: function(obj, content)
	{
		obj.editingContent = content;

//		$("#home_configure_content_source_file").prop('checked', true);
		$('input:radio[name=home_configure_content_source]').prop('checked', false);
		$("#home_configure_content_title").val("");
		$("#home_configure_content_date").val("");
		$("#home_configure_content_date").datetimepicker("destroy");
		$("#home_configure_content_date").datetimepicker(obj.dateTimePickerConfig);
		$('#home_configure_content_date').unbind('change').change(function(){obj.validateContent(obj, 2, false);return true;});
		$("#home_configure_content_url").val("");
		$("#home_configure_content_height").val("");
		$("#home_configure_content_youtube").val("");
		$("#home_configure_content_youtube_ratio_169").prop('checked', true);
		$("#home_configure_image_options_selected").empty().html("<i>none</i>");
		$("#home_configure_content_W_alt").val("");
		$("#home_configure_content_F_alt").val("");
		
		obj.imageFinderSelection = null;

		if (content != null)
		{
			$('input:radio[name=home_configure_content_source][value="' + content.source + '"]').prop('checked', true);
			$("#home_configure_content_title").val(content.title);
			if (content.releaseDate != null) $("#home_configure_content_date").datetimepicker("setDate", content.releaseDate);
			if (content.source == "W")
			{
				$("#home_configure_content_url").val(content.url);
				$("#home_configure_content_height").val(content.style);				
				$("#home_configure_content_W_alt").val(content.alt);
			}
			else if (content.source == "Y")
			{
				$("#home_configure_content_youtube").val(content.url);
				if (content.style != null) $('input:radio[name=home_configure_content_youtube_ratio][value="' + content.style + '"]').prop('checked', true);				
			}
			else if (content.source == "F")
			{
				obj.imageFinderSelection = content.url;
				if (obj.imageFinderSelection != null)
				{
					$("#home_configure_image_options_selected").empty().text(obj.nameFromUrl(obj.imageFinderSelection));
				}
				$("#home_configure_content_F_alt").val(content.alt);
			}			
		}

		openDialog("home_configure_content_dialog",[{text:"Done", click:function()
		{
			if(obj.saveEditContent(obj, false))
			{
				$("#home_configure_content_dialog").dialog("close");
			}
			return false;
		}},
		{text:"Publish", click:function()
		{
			if(obj.saveEditContent(obj, true))
			{
				$("#home_configure_content_dialog").dialog("close");
			}
			return false;
		}}]);

		obj.adjustForSource(obj);
		obj.validateContent(obj, 1, false);
	},

	extractYoutubeId : function(obj, url)
	{
		var regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|\&v=)([^#\&\?]*).*/;
		var match = url.match(regExp);
		if (match && match[2].length == 11)
		{
			return match[2];
		}
		return url;
	},

	assureTransportInUrl: function(obj, url)
	{
		if (url == "") return url;
		if (url.startsWith("/")) return url;

		if ((!url.startsWith("http://")) && (!url.startsWith("https://")) && (!url.startsWith("//")))
		{
			// add relative scheme
			return "//" + url;
		}
		
		return url;
	},

	showPreview: function(obj)
	{
		var source = $('input:radio[name=home_configure_content_source]:checked').val();
		var destination = $("#home_configure_preview");
		var loading = $("#home_configure_preview_loading");
		$(destination).empty();
		$(loading).addClass("e3_offstage");
		if (source == "Y")
		{
			var url = $.trim($("#home_configure_content_youtube").val());
			var youtubeId = obj.extractYoutubeId(obj, url);
			$("#home_configure_content_youtube").val(youtubeId);

			var content = new Object();
			content.url = youtubeId;
			content.style = $('input:radio[name=home_configure_content_youtube_ratio]:checked').val();
	
			loadYoutubeContent(destination, content, 600, function(){$("#home_configure_content_dialog").dialog("option", "position", ["center", e3_top]); adjustDialogHeight();}, loading);
		}
		else if (source == "W")
		{
			var content = new Object();
			content.url = $.trim($("#home_configure_content_url").val());
			content.url = obj.assureTransportInUrl(obj, content.url);
			$("#home_configure_content_url").val(content.url);
			content.style = parseInt($.trim($("#home_configure_content_height").val())).toString();
			if (isNaN(content.style) || (content.style == 0)) content.style = "";
			$("#home_configure_content_height").val(content.style);
			content.type = "?";
	
			loadWebContent(destination, content, 600, function(){$("#home_configure_content_dialog").dialog("option", "position", ["center", e3_top]); adjustDialogHeight();}, loading);
		}
		else if (source == "F")
		{
			var content = new Object();
			content.url = obj.imageFinderSelection;
			content.style = "500";
			content.type = "?";
	
			loadWebContent(destination, content, 600, function(){$("#home_configure_content_dialog").dialog("option", "position", ["center", e3_top]); adjustDialogHeight();}, loading);
		}
	},

	showPreviewPopup: function(obj, item)
	{
		$("#home_alertPreview_title").empty().text(item.title);

		var loading = $("#home_alertPreview_loading");
		$(loading).addClass("e3_offstage");

		var destination = $("#home_alertPreview_preview");
		$(destination).empty();
		
		openAlert("home_alertPreview");

		loadContent("home_alertPreview_preview", item, 600, function(){$("#home_alertPreview_preview").dialog("option", "position", ["center", e3_top]);adjustDialogHeight();}, loading);
	},

	validateContent: function(obj, which, published)
	{
		$("#home_configure_content_title_alert").addClass("e3_offstage");
		$("#home_configure_content_date_alert").addClass("e3_offstage");
		$("#home_configure_content_url_alert").addClass("e3_offstage");
		$("#home_configure_content_youtube_alert").addClass("e3_offstage");
		$("#home_configure_content_file_alert").addClass("e3_offstage");
		$("#home_configure_content_source_alert").addClass("e3_offstage");

		if ((which == 0) | (which == 1))
		{
			var title = $.trim($("#home_configure_content_title").val());
			if (title.length == 0)
			{
				$("#home_configure_content_title_alert").removeClass("e3_offstage");
				return 1;
			}
		}

		if ((which == 0) || (which == 2))
		{
			var releaseDate = $.trim($("#home_configure_content_date").val());
			if ((releaseDate.length == 0) && published)
			{
				$("#home_configure_content_date_alert").removeClass("e3_offstage");
				return 2;
			}
		}

		var source = $('input:radio[name=home_configure_content_source]:checked').val();

		if ((which == 0) || (which == 3))
		{
			if (source == "W")
			{
				var url = $.trim($("#home_configure_content_url").val());
				if (url.length == 0)
				{
					$("#home_configure_content_url_alert").removeClass("e3_offstage");
					return 3;
				}
			}
		}

		if ((which == 0) || (which == 4))
		{
			if (source == "Y")
			{
				var youtubeId = $.trim($("#home_configure_content_youtube").val());
				if (youtubeId.length == 0)
				{
					$("#home_configure_content_youtube_alert").removeClass("e3_offstage");
					return 4;
				}
			}
		}

		if ((which == 0) || (which == 5))
		{
			if (source == "F")
			{
				var selected = $.trim(obj.imageFinderSelection);
				if (selected.length == 0)
				{
					$("#home_configure_content_file_alert").removeClass("e3_offstage");
					return 5;
				}
			}
		}
		
		if ((which == 0) || (which == 6))
		{
			if (source == null)
			{
				$("#home_configure_content_source_alert").removeClass("e3_offstage");
				return 6;
			}
		}

		return 0;
	},

	saveEditContent: function(obj, published)
	{
		// validate: assure title, and if published, assure date
		var valid = obj.validateContent(obj, 0, published);
		if (valid > 0)
		{
			openAlert("home_alertContent_" + valid);
			return false;
		}

		var params = new Object();
		params.siteId = obj.siteId;
		if ($('input:radio[name=home_configure_content_source]:checked').length == 0)
		{
			params.source = "-";
		}
		else
		{
			params.source = $('input:radio[name=home_configure_content_source]:checked').val();
		}
		params.title = $.trim($("#home_configure_content_title").val());
		params.releaseDate = $.trim($("#home_configure_content_date").val());
		params.published = published ? "1" : "0";
		if (obj.editingContent != null) params.contentId = obj.editingContent.contentId;
		if (params.source == "W")
		{
			params.url = $.trim($("#home_configure_content_url").val());

			var h = parseInt($.trim($("#home_configure_content_height").val())).toString();			
			if (isNaN(h) || (h == 0)) h = null;
			if (h != null) params.style = h;
			params.alt = $.trim($("#home_configure_content_W_alt").val());
		}
		else if (params.source == "Y")
		{
			params.url = obj.extractYoutubeId(obj, $.trim($("#home_configure_content_youtube").val()));
			params.style = $('input:radio[name=home_configure_content_youtube_ratio]:checked').val();
		}
		else if (params.source == "A")
		{
			params.content = obj.editorApi.getData();			
		}
		else if (params.source == "F")
		{
			params.url = $.trim(obj.imageFinderSelection);
			params.alt = $.trim($("#home_configure_content_F_alt").val());
		}

		requestCdp("homepage_saveContent", params, function(data)
		{
	 		obj.current = data.current;
			obj.pending = data.pending;
			obj.drafts = data.drafts;
			obj.past = data.past;

			obj.editingContent = null;

			obj.populateConfiguration(obj);
		});

		return true;
	},

	deleteContent: function(obj)
	{
		if (anyOidsSelected("selectContent"))
		{
			openConfirm("home_confirmDelete", "Delete", function(){obj.doDelete(obj);});
		}
		
		else
		{
			openAlert("home_alertSelect");
		}
	},

	doDelete: function(obj)
	{
		// get ids selected
		var params = new Object();
		params.contentIds = collectSelectedOids("selectContent");
		params.siteId = obj.siteId;

		// if any selected
		if (params.contentIds.length > 0)
		{
			requestCdp("homepage_deleteContent", params, function(data)
			{
		 		obj.current = data.current;
				obj.pending = data.pending;
				obj.drafts = data.drafts;
				obj.past = data.past;

				obj.populateConfiguration(obj);
			});
		}
	},

	publishContent: function(obj)
	{
		if (anyOidsSelected("selectContent"))
		{
			openConfirm("home_confirmPublish", "Publish", function(){obj.doPublish(obj);});
		}
		
		else
		{
			openAlert("home_alertSelect");
		}
	},

	doPublish: function(obj)
	{
		// get ids selected
		var params = new Object();
		params.contentIds = collectSelectedOids("selectContent");
		params.siteId = obj.siteId;

		// if any selected
		if (params.contentIds.length > 0)
		{
			requestCdp("homepage_publishContent", params, function(data)
			{
		 		obj.current = data.current;
				obj.pending = data.pending;
				obj.drafts = data.drafts;
				obj.past = data.past;

				obj.populateConfiguration(obj);
			});
		}
	},

	unpublishContent: function(obj)
	{
		if (anyOidsSelected("selectContent"))
		{
			openConfirm("home_confirmUnpublish", "Unpublish", function(){obj.doUnpublish(obj);});
		}
		
		else
		{
			openAlert("home_alertSelect");
		}
	},

	doUnpublish: function(obj)
	{
		// get ids selected
		var params = new Object();
		params.contentIds = collectSelectedOids("selectContent");
		params.siteId = obj.siteId;

		// if any selected
		if (params.contentIds.length > 0)
		{
			requestCdp("homepage_unpublishContent", params, function(data)
			{
		 		obj.current = data.current;
				obj.pending = data.pending;
				obj.drafts = data.drafts;
				obj.past = data.past;

				obj.populateConfiguration(obj);
			});
		}
	},
	
	editZone: function(obj)
	{
		$("#prefs_zone_option").val(obj.timezone);

		$("#prefs_zone_dialog").dialog('open');
	},
	
	saveZone: function(obj)
	{
		var data = new Object();
		data.timezone = $("#prefs_zone_option").val();
		requestCdp("preferences_setPreferences", data, function(data)
		{
			obj.reset(obj);
		});

		return true;
	}
};

completeToolLoad();
