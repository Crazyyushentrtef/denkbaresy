/*
 * Copyright (C) 2015 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

/**
 * The KNOWWE global namespace object. If KNOWWE is already defined, the
 * existing KNOWWE object will not be overwritten so that defined namespaces are
 * preserved.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
	var KNOWWE = {};
}

/**
 * The KNOWWE.core global namespace object. If KNOWWE.core is already defined,
 * the existing KNOWWE.core object will not be overwritten so that defined
 * namespaces are preserved.
 */
if (typeof KNOWWE.core == "undefined" || !KNOWWE.core) {
	KNOWWE.core = {};
}

/**
 * The KNOWWE.core global namespace object. If KNOWWE.core is already defined,
 * the existing KNOWWE.core object will not be overwritten so that defined
 * namespaces are preserved.
 */
if (typeof KNOWWE.core.plugin == "undefined" || !KNOWWE.core.plugin) {
	KNOWWE.core.plugin = {};
}

/**
 * Namespace: KNOWWE.core.plugin.rightPanel for debugging D3web expressions in KnowWE
 */
KNOWWE.core.plugin.rightPanel = function () {

	var rightPanelStorageKey = "rightPanel";

	var rightPanel = null;

	var showSidebar = false;

	var globalFloatingTime = 500;

	var initScrolling = null;

	var windowWidth;

	function getSelected() {
		var t = '';
		if (window.getSelection) {
			t = window.getSelection();
		} else if (document.getSelection) {
			t = document.getSelection();
		} else if (document.selection) {
			t = document.selection.createRange().text;
		}
		return t;

	}

	function rightPanelScroll() {
		var element = $("rightPanel");
		if (!element)
			return;
		var originY = initScrolling;
		var wHeight = window.getHeight();

		var docHeight = getDocHeight();
		var favHeight = element.clientHeight;
		var scrollY = window.getScrollTop();
		var scrollMax = docHeight - wHeight;
		var favToScroll = favHeight - wHeight;
		var actionsBottom = $("actionsBottom");
		var disableFixing = (actionsBottom == null
		|| favHeight >= actionsBottom.offsetTop + actionsBottom.clientHeight);
		if (scrollY <= originY || disableFixing) {
			// when reaching top of page or if page height is made by leftMenu
			// align fav originally to page
			element.style.position = "absolute";
			element.style.top = originY + "px";
		} else if (scrollMax - scrollY <= favToScroll) {
			// when reaching end of page
			// align bottom of fav to bottom of page
			element.style.position = "absolute";
			element.style.top = (docHeight - favHeight) + "px";
		} else {
			// otherwise fix fav to the top of the viewport
			element.style.position = "fixed";
			element.style.top = "0px";

		}

		function getDocHeight() {
			var D = document;
			return Math.max(Math.max(D.body.scrollHeight,
				D.documentElement.scrollHeight), Math.max(D.body.offsetHeight,
				D.documentElement.offsetHeight), Math.max(D.body.clientHeight,
				D.documentElement.clientHeight));
		}
	}


	function makeRightPanelResizable() {
		rightPanel.resizable({
			handles: "w",
			minWidth: 300,
			maxWidth: jq$(window).width()
			- (jq$(".tabmenu a:last-child").first().offset().left
			+ jq$(".tabmenu a:last-child").first().outerWidth()
			+ jq$("#actionsTop").outerWidth()
			+ jq$("#rightPanel .ui-resizable-w").width() + 20),
			alsoResize: "#watches textarea, #watches .watchlistline",
			resize: function (event, ui) {
				jq$(window).resize();
			}
		});
	}

	function restoreLayout() {
		var resize = jq$(window).width() - jq$("#favorites").outerWidth() - 300;
		jq$("#page").css("width", resize + "px");
		jq$(rightPanel).css("width", "300px");
		var pagesRightOffset = (jq$(window).width() - (jq$("#actionsTop").offset().left + jq$("#actionsTop").width()));
		rightPanel.css("left", (jq$(window).width() - pagesRightOffset) + "px");
	}

	function floatRightPanel() {

		showSidebar = true;

		var options = {right: '0px'};

		rightPanel.animate(options, globalFloatingTime, function () {
			//"left" is needed for resizable to work properly
			var pagesRightOffset = (jq$(window).width() - (jq$("#actionsTop").offset().left + jq$("#actionsTop").width()));
			rightPanel.css("left", (jq$(window).width() - pagesRightOffset) + "px");
		});
		initScrolling = jq$(".tabs").offset().top;
		jq$(window).scroll(function () {
			rightPanelScroll();
		});

		windowWidth = jq$(window).width();

		jq$(window).resize(function () {
			if (showSidebar) {
				windowWidth = jq$(window).width();

				var resize = jq$(window).width() - jq$('#rightPanel').width() - jq$("#page").offset().left;
				jq$("#page").css("width", resize + "px");

				var pagesRightOffset = (jq$(window).width() - (jq$("#actionsTop").offset().left + jq$("#actionsTop").width()));
				rightPanel.css("left", (jq$(window).width() - pagesRightOffset) + "px");
			}
		});

		//make sidebar resizable
		makeRightPanelResizable();
	}

	function shrinkPage() {
		jq$("#page").animate({
			'width': "-=300px"
		}, globalFloatingTime);
		jq$("#pagecontent").css("margin-right", "5px");
		jq$("#actionsBottom").css("margin-right", "5px");
	}

	function growPage() {
		jq$("#morepopup").css("display", "none");
		var pageWidth = jq$('#page').width() + jq$('#rightPanel').width();
		jq$("#page").animate({
			'width': pageWidth
		}, globalFloatingTime, function () {
			jq$('#page').css("width", "auto");
		});
		rightPanel.animate({
			left: (jq$(window).width() + "px")
		}, globalFloatingTime, function () {
			rightPanel.remove();
			jq$("#morepopup").css("display", "block");
		});
		jq$("#pagecontent").css("margin-right", "auto");
		jq$("#actionsBottom").css("margin-right", "auto");
	}

	function buildRightPanel() {
		var offsetTop = jq$(".tabs").offset().top;
		var scrollTop = jq$(window).scrollTop();
		rightPanel = jq$('<div/>', {
			'id': 'rightPanel',
			'css': {
				'position': 'absolute',
				'top': (offsetTop - scrollTop) + 'px',
				'right': '-300px',
				'width': "300px",
				'overflow-x': 'hidden',
				'overflow-y': 'hidden'
			}
		});

		var rightPanelHide = jq$('<div/>', {
			'class': 'rightpanelhide'
		});


		var rightPanelHideText = jq$('<span/>', {
			'text': 'Hide'

		});

		var rightPanelHideIcon = jq$('<img/>', {
			'src': 'KnowWEExtension/images/arrow_right.png'

		});

		rightPanelHide.append(rightPanelHideText);
		rightPanelHide.append(rightPanelHideIcon);
		rightPanel.append(rightPanelHide);
		jq$("#content").append(rightPanel);

	}

	function initRightPanelTools() {
		KNOWWE.core.plugin.rightPanel.watches.initWatchesTool();
	}

	function setRightPanelCookie(b) {
		var storage = simpleStorage.get(rightPanelStorageKey);
		if (typeof storage == 'undefined') {
			storage = {}
		}

		else {
			simpleStorage.deleteKey(rightPanelStorageKey);
		}
		simpleStorage.set(rightPanelStorageKey, b);

	}

	function bindCollapseIcons() {
		jq$("#rightPanel").on("click", ".tool .topbar", function () {
			if (jq$(this).find("i").hasClass("fa-caret-down")) {
				jq$(this).find("i").removeClass("fa-caret-down").addClass("fa-caret-right");
				jq$(this).parent().find(".content").first().slideUp();
			}
			else {
				if (jq$(this).find("i").hasClass("fa-caret-right")) {
					jq$(this).find("i").removeClass("fa-caret-right").addClass("fa-caret-down");
					jq$(this).parent().find(".content").first().slideDown();
				}
			}
		});
	}


	function bindHideFunctions() {
		jq$("#morebutton .watches").unbind();
		bindHideInPanel();
		bindHideInMoreMenu();


		function bindHideInMoreMenu() {
			jq$("#morebutton .watches").prop("title", "Hide Right Panel");
			jq$("#morebutton .watches").on("click", function () {
				terminateRightPanel();
			});
			jq$("#morebutton .watches").text("Hide Right Panel");
		}


		function bindHideInPanel() {
			jq$("#rightPanel .rightpanelhide").on("click", function () {
				terminateRightPanel();
			})
		}
	}

	function changeHideToShow() {
		jq$("#morebutton .watches").prop("title", "Show Right Panel");
		jq$("#morebutton .watches").unbind();
		jq$("#morebutton .watches").on("click", function () {
			KNOWWE.plugin.core.rightPanel.showRightPanel();
		});
		jq$("#morebutton .watches").text("Show Right Panel");
	}

	function bindUiActions() {
		bindCollapseIcons();
		bindHideFunctions();
	}

	function initRightPanel(fromCookie) {
		if (!showSidebar) {
			showSidebar = true;
			if (fromCookie) {
				globalFloatingTime = 0;
			}
			else {
				globalFloatingTime = 500;
			}
			shrinkPage();
			buildRightPanel();
			floatRightPanel();
			setRightPanelCookie(true);
			bindUiActions();
			initRightPanelTools();
			globalFloatingTime = 500;
		}
	}

	function terminateRightPanel() {
		growPage();
		changeHideToShow();
		setRightPanelCookie(false);
		showSidebar = false;
	}

	function buildToolContainer(id) {
		return tool = jq$('<div/>', {
			'id': id,
			'class': 'tool',
			'css': {
				'position': 'relative'
			}
		});
	}

	function buildTopBar(title) {
		var toolTopbar = jq$('<div/>', {
			'class': 'topbar'
		});
		var collapseIcon = jq$('<i/>', {
			'class': 'collapseicon fa fa-fw fa-caret-down'
		});
		var toolTitle = jq$('<div/>', {
			'class': 'title',
			'text': title
		});
		toolTitle.prepend(collapseIcon);
		toolTopbar.append(toolTitle);
		return toolTopbar;
	}

	function appendNewToolToRightPanel(tool, topbar, div) {
		tool.append(topbar);
		tool.append(div);
		rightPanel.append(tool);
	}

	function buildToolContent(pluginDiv) {
		var content = jq$('<div/>', {
				'class': 'content'
			}
		);
		content.append(pluginDiv);
		return content;
	}

	function isRightPanelShown() {
		return simpleStorage.get(rightPanelStorageKey) == true;
	}

	function initRightPanelToggleButton(isShown) {
		var orientation = (isShown ? "right" : "left");
		var status = (isShown ? "Hide" : "Show");
		jq$('#morebutton').after("<li><a id='rightPanel-toggle-button' title='" + status + " right panel'"
			+ " class='action fa fa-angle-double-" + orientation + "'></a></li>");
		jq$('#rightPanel-toggle-button').unbind('click').click(function () {
			var $this = jq$(this);
			if (isRightPanelShown()) {
				KNOWWE.core.plugin.rightPanel.hideRightPanel();
				$this.removeClass("fa-angle-double-right").addClass("fa-angle-double-left").attr("title", "Show right panel");
			} else {
				KNOWWE.core.plugin.rightPanel.showRightPanel();
				$this.removeClass("fa-angle-double-left").addClass("fa-angle-double-right").attr("title", "Hide right panel");
			}
		});
	}

	return {

		showRightPanel: function () {
			initRightPanel(false);
		},

		hideRightPanel: function () {
			terminateRightPanel();
		},

		init: function () {
			var isShown = isRightPanelShown();
			if (isShown) {
				initRightPanel(true);
			}
			initRightPanelToggleButton(isShown);
		},

		addToolToRightPanel: function (title, id, pluginDiv) {
			var tool = buildToolContainer(id);
			var topbar = buildTopBar(title);
			var content = buildToolContent(pluginDiv);
			appendNewToolToRightPanel(tool, topbar, content);
		}

	}
}();


KNOWWE.core.plugin.rightPanel.watches = function () {

	var watchesStorageKey = "watches";

	var watchesArray;

	var watches;

	var restorableEntries = {};

	var watchlist;

	function bindUiActions() {
		watches.on("click", ".watchlistentry", function (e) {
			editWatch(this);
		});
		watches.on("click", ".addwatch", function (e) {
			addWatch();
		});
		watches.on("click", ".fromselection", function (e) {
			addWatchFromSelection();
		});
		watches.on("keydown", "textarea", function (e) {
			handleTextarea(this, e);
		});
		watches.on("click", ".deletewatch", function (e) {
			e.stopPropagation();
			removeWatch(this);
		});
	}

	function handleResponse(data) {
		var parsed = JSON.parse(data);
		var expressionArrays = parsed.values;
		var oldEntries = watchlist.find(".watchlistentry");
		jq$.each(expressionArrays, function (index, value) {
			var newEntry = createNewEntry(watchesArray[index], value);
			jq$(oldEntries[index]).replaceWith(newEntry);
		});
		enableAddWatch();
	}

	function updateOldWatchesList(data) {
		handleResponse(data);
	}

	function updateWatches() {
		getExpressionValue(watchesArray).success(function (data) {
			updateOldWatchesList(data);
		});


	}

	function enableAddWatch() {
		if (watches.find(".addwatch").prop("disabled") == true && watches.find(".newwatch").length == 0) {
			watches.find(".addwatch").prop("disabled", false);
			watches.find(".fromselection").prop("disabled", false);
		}
	}

	function disableAddWatch() {
		if (watches.find(".addwatch").prop("disabled") == false) {
			watches.find(".addwatch").prop("disabled", true);
			watches.find(".fromselection").prop("disabled", true);
		}
	}

	function addWatch(text) {
		var textarea = createTextarea(null, text);
		watchlist.append(textarea);
		jq$(textarea).find("textarea").focus();
		//allow only one new textarea - disable Add Watch
		disableAddWatch();
	}

	function addWatchFromSelection() {
		var text = getSelectionText();
		addWatch(text);
	}

	function removeWatch(that) {
		var index = getWatchesIndex(jq$(that).parent());
		watchesArray.splice(index, 1);
		jq$(that).parent().remove();
		updateCookies();
	}

	function saveOldEntry(index, that) {
		var oldEntry = jq$(that);
		restorableEntries[index] = oldEntry;
	}

	function editWatch(that) {
		var index = getWatchesIndex(that);
		saveOldEntry(index, that);
		watchesArray.splice(index, 1);
		var textarea = createTextarea(that);
		jq$(that).replaceWith(textarea);
		jq$(textarea).find("textarea").focus();
		//allow only one new textarea - disable Add Watch
		disableAddWatch();
	}

	function restoreEntry(entry, watchesIndex) {
		var restorableEntry = restorableEntries[watchesIndex];
		var restorableExpression = jq$(restorableEntry).find(".expression").text();
		watchesArray.splice(watchesIndex, 0, restorableExpression);
		jq$(entry).replaceWith(restorableEntry);
		delete restorableEntries[watchesIndex];
	}

	function handleTextarea(that, e) {
		var entry = jq$(that).parent();
		var watchesIndex = getWatchesIndex(entry);

		//escape
		if (e.keyCode == 27) {
			//is it an old element?
			if (watchesIndex <= watchesArray.length) {
				//restore it
				restoreEntry(entry, watchesIndex);
			}
			else {
				entry.remove();
			}
			enableAddWatch();
		}

		if (jq$(that).data('ui-tooltip') && jq$(that).val().trim() != "") {
			jq$(that).tooltip("destroy");
			jq$(that).attr("title", null);
		}

		var trimmedValue = jq$(that).val().trim();
		//shift+enter = newline, enter=submit
		if (e.keyCode == 13 && !e.shiftKey) {
			if (trimmedValue == "") {
				jq$(that).tooltip({position: {my: "right bottom", at: "left top"}});
				jq$(that).attr("title", "Please enter an expression.");
				jq$(that).trigger("mouseover");
				// prevent default behavior
				e.preventDefault();
				//alert("ok");
			}
			else {
				if (watchesIndex < watchesArray.length) {
					addExpression(jq$(that), watchesIndex);
				}
				else {
					jq$(that).val(trimmedValue);
					addExpression(jq$(that));
				}

			}
		}

	}

	function getWatchesIndex(that) {
		return jq$(that).index();
	}

	function getExpressionValue(expr, id) {
		var data = {expressions: expr, page: KNOWWE.helper.gup('page'), id: id};
		return jq$.ajax({
			type: 'post',
			url: 'action/GetExpressionValueAction',
			data: JSON.stringify(data),
			cache: false,
			contentType: 'application/json, UTF-8'
		});
	}


	function createTextarea(that, text) {
		var watchesNewEntry = jq$('<div/>', {
			'class': 'newwatch watchlistline'
		});
		var textarea = jq$('<textarea>', {});

		var textareaDom = textarea[0];

		if (typeof AutoComplete != "undefined") {
			new AutoComplete(textareaDom, function (callback, prefix) {
				var scope = "$d3web/condition";
				var data = {prefix: prefix, scope: scope};
				if (KNOWWE && KNOWWE.helper) {
					data.KWiki_Topic = KNOWWE.helper.gup('page');
				}
				jq$.ajax({
					url: 'action/CompletionAction',
					cache: false,
					data: data
				}).success(function (data) {
					callback(eval(data));
				});
			});
		}

		if (that) {
			var oldEntry = restorableEntries[getWatchesIndex(that)];
			var oldText = jq$(oldEntry).find(".expression").text();
			textarea.val(oldText);
		}

		if (typeof text != 'undefined') {
			textarea.val(text);
		}
		textarea.autosize({minHeight: "22px"});
		watchesNewEntry.append(textarea);
		return watchesNewEntry;
	}

	function createWatchesEntryValueSpan(value) {
		return jq$('<span/>', {
			'class': 'value tooltip',
			'text': value.value,
			'title': value.kbname
		});
	}


	function createWatchesEntryHistoryValueSpan(title) {
		return jq$('<span/>', {
			'class': 'value tooltip history',
			'title': title
		});
	}

	function handleDefaultResponse(watchesEntry, responseObject) {

		jq$.each(responseObject.kbsEntries, function iterateValuesFromDifferentKbs(index, value) {
			var watchesEntryValue = createWatchesEntryValueSpan(value);
			var tooltipcontent = jq$('<span style="padding-right: 5px" class="fa fa-book"></span><span>' + value.kbname + '  </span>');
			jq$(watchesEntryValue).tooltipster({
				content: tooltipcontent,
				position: "top-left",
				delay: 300,
				theme: ".tooltipster-knowwe"
			});
			watchesEntry.append(watchesEntryValue);
		});

		return watchesEntry;

	}

	function handleHistoryResponse(watchesEntry, responseObject) {

		jq$.each(responseObject.kbsEntries, function iterateValuesFromDifferentKbs(index, value) {
			var watchesEntryValue = createWatchesEntryHistoryValueSpan(value.kbname);

			jq$.each(value.value, function iterateValuesInHistory(index, value) {
				var historyEntrySpan = jq$('<span/>', {
					'class': 'value tooltip historyentry',
					'text': value.value
				});
				createTimestampsToolTip.call(this, historyEntrySpan);
				watchesEntryValue.append(historyEntrySpan);

			});
			watchesEntry.append(watchesEntryValue);
		});

		function createTimestampsToolTip(historyEntrySpan) {
			var start = this.timestamps[0];
			var end = this.timestamps[1];
			var tooltipcontent;
			if (start != end) {
				tooltipcontent = jq$('<span>Start: ' + start + '</span><br><span>End: ' + end + '  </span>');
			}
			else {
				tooltipcontent = jq$('<span>Start: ' + start + '</span>');
			}
			jq$(historyEntrySpan).tooltipster({
				content: tooltipcontent,
				position: "top-left",
				delay: 300,
				theme: ".tooltipster-knowwe"
			});
		}

		return watchesEntry;
	}

	function createNewEntry(expression, responseObject) {

		var watchesEntry = jq$('<div/>', {
			'class': 'watchlistline watchlistentry'

		});
		watchesEntry.uniqueId();
		var watchesEntryExpression = jq$('<span/>', {
			'class': 'expression',
			'text': expression
		});
		watchesEntry.append(watchesEntryExpression);


		var length = Object.keys(responseObject).length;
		if (length > 0) {
			switch (responseObject.info) {
				case 'history':
					handleHistoryResponse(watchesEntry, responseObject);
					break;
				default:
					handleDefaultResponse(watchesEntry, responseObject);
			}
		}
		else {
			var watchesEntryValue = jq$('<span/>', {
				'class': 'value expressionerror',
				'text': '<not a valid expression>'
			});
			watchesEntry.append(watchesEntryValue);
		}

		watchesEntry.append(createDeleteButton());
		return watchesEntry;
	}

	function addExpression(original, watchesIndex) {

		var expression = original.val();

		var watchesEntry = jq$('<div/>', {
			'class': 'watchlistline watchlistentry',
			'text': expression
		});
		jq$(original).parent().replaceWith(watchesEntry);

		if (typeof watchesIndex != 'undefined') {
			watchesArray.splice(watchesIndex, 0, expression);
		}
		else {
			watchesArray.push(expression);
		}
		updateCookies();

		getExpressionValue(watchesArray).success(function (data) {
			updateOldWatchesList(data);
		});
	}

	function buildBasicWatchesDiv() {

		var watchcontent = jq$('<div/>', {});

		var watchlist = jq$('<div/>', {
			'class': 'watchlist'
		});

		var watchesAddEntry = jq$("<button class='addwatch'><i class='fa fa-plus-circle'></i>&nbsp;Add Watch</button>");


		var watchesAddEntryFromSelection = jq$("<button class='fromselection'><i class='fa fa-paragraph'></i>&nbsp;from Selection</button>");

		watchcontent.append(watchlist);
		watchcontent.append(watchesAddEntry);
		watchcontent.append(watchesAddEntryFromSelection);

		KNOWWE.core.plugin.rightPanel.addToolToRightPanel("Watches", "watches", watchcontent);
	}

	function createDeleteButton() {

		var deleteContainer = jq$('<div/>', {
			'class': 'iconcontainer deletewatch select'
		});

		var deleteIcon = jq$("<a class=''><i class='fa fa-times-circle icon'></i></a>");


		return deleteContainer.append(deleteIcon);
	}

	function loadWatchesFromCookies() {
		watchesArray = simpleStorage.get(watchesStorageKey);
		if (typeof watchesArray != 'undefined') {
			getExpressionValue(watchesArray).success(function (data) {
				var parsed = JSON.parse(data);
				var expressionArrays = parsed.values;
				jq$.each(expressionArrays, function (index, value) {
					var newEntry = createNewEntry(watchesArray[index], value);
					watchlist.append(newEntry);
				});
			});
		}
		else {
			watchesArray = [];
		}
	}

	function updateCookies() {
		simpleStorage.set(watchesStorageKey, watchesArray);
	}

	function getSelectionText() {
		var text = "";
		if (window.getSelection) {
			text = window.getSelection().toString();
		} else if (document.selection && document.selection.type != "Control") {
			text = document.selection.createRange().text;
		}
		return text;
	}

	function initVariables() {
		watches = jq$("#watches");
		watchlist = watches.find(".watchlist");
	}

	return {
		initWatchesTool: function () {
			buildBasicWatchesDiv();
			loadWatchesFromCookies();
			initVariables();
			bindUiActions();
			KNOWWE.helper.observer.subscribe("update", function () {
				updateWatches();
			});
		},

		addToWatches: function (text) {
			KNOWWE.core.plugin.rightPanel.showRightPanel();
			addWatch(text);
		}
	}
}();

(function init() {

	window.addEvent('domready', _KL.setup);
	if (KNOWWE.helper.loadCheck(['Wiki.jsp'])) {
		window.addEvent('domready', function () {
			KNOWWE.core.plugin.rightPanel.init();
		});
	}
}());