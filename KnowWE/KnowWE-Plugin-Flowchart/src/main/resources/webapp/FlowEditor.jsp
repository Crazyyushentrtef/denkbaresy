<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN">

<%@page import="de.d3web.we.kdom.Section"%>
<%@page import="de.d3web.we.kdom.KnowWEArticle"%>
<%@page import="de.d3web.we.flow.type.DiaFluxType"%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="de.d3web.we.jspwiki.*" %>
<%@ page import="java.util.*" %>
<%@ page import="de.d3web.we.core.*" %>
<%@ page import="de.d3web.we.javaEnv.*" %>
<%@ page import="de.d3web.we.wikiConnector.*" %>
<%@ page import="de.d3web.we.action.*" %>
<%@ page import="de.d3web.we.flow.kbinfo.*" %>
<%@ page import="de.d3web.we.flow.*" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context; authorization check not needed
    WikiContext wikiContext = wiki.createContext( request, WikiContext.VIEW );

    if (!KnowWEEnvironment.isInitialized()) {
		KnowWEEnvironment.initKnowWE(new JSPWikiKnowWEConnector(wiki));
	}
	KnowWEEnvironment env = KnowWEEnvironment.getInstance();
	KnowWEUserContextImpl userContext =  new KnowWEUserContextImpl(wikiContext.getWikiSession().getUserPrincipal().getName(), wikiContext.getHttpRequest().getParameterMap());
	KnowWEParameterMap map = new KnowWEParameterMap(userContext, request, response, wiki.getServletContext(), env);
	
	map.put("KWikiUser",wikiContext.getWikiSession().getUserPrincipal().getName());
	if(!map.containsKey(KnowWEAttributes.WEB)) {
		map.put(KnowWEAttributes.WEB, "default_web");
	}
	String topic = map.getTopic();
	String web = map.getWeb();
	KnowWEArticle article = env.getArticle(web, topic);
	
	JSPHelper jspHelper = new JSPHelper(map);
	String kdomID = map.get("kdomID");
	Section diafluxSection = article.findSection(kdomID);
	String title = DiaFluxType.getFlowchartName(diafluxSection);
%>

<script>
	var topic = "<%= topic %>";
	var nodeID = "<%= kdomID %>";
</script>

<html>
<head>
<title>Edit Flowchart: <%= title %></title>
	<style type="text/css">
		.toolbar .propertyTitle {
			font-family: Arial,Helvetica;
			font-size: 10pt;
			font-weight: bold;
		}
		.toolbar .short {
			width: 40px;
		}
		.toolbar .long {
			width: 120px;
		}
		.toolbar .propertyText, .toolbar button {
			font-family: Arial,Helvetica;
			font-size: 10pt;
		}
		.toolbar button {
			padding-left: 5px;
			padding-right: 5px;
			width: 100%;
			height: 100%;
		}
		.toolbar .propertyBlock {
			display: inline-block;
			padding-left: 10px;
			padding-right: 10px;
		}
		.toolbar .propertyArea {
			background-color: #eee;
			border: 1px solid gray;
		}
	</style>
	
	
	<script src="cc/scriptaculous-js/lib/prototype.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/builder.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/effects.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/dragdrop.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/controls.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/slider.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/sound.js" type="text/javascript"></script>
	<!-- script src="cc/scriptaculous-js/src/scriptaculous.js" type="text/javascript"></script-->
	
	<script src="cc/kbinfo/kbinfo.js" type="text/javascript"></script>
	<script src="cc/kbinfo/extensions.js" type="text/javascript"></script>
	<script src="cc/kbinfo/dropdownlist.js" type="text/javascript"></script>
	<script src="cc/kbinfo/objectselect.js" type="text/javascript"></script>
	<script src="cc/kbinfo/objecttree.js" type="text/javascript"></script>
	
	<script src="cc/flow/floweditor.js" type="text/javascript"></script>
	<script src="cc/flow/flowchart.js" type="text/javascript"></script>
	<script src="cc/flow/action.js" type="text/javascript"></script>
	<script src="cc/flow/guard.js" type="text/javascript"></script>
	<script src="cc/flow/node.js" type="text/javascript"></script>
	<script src="cc/flow/rule.js" type="text/javascript"></script>
	<script src="cc/flow/nodeeditor.js" type="text/javascript"></script>
	<script src="cc/flow/router.js" type="text/javascript"></script>
	<script src="cc/flow/rollup.js" type="text/javascript"></script>
	
	
	<link rel="stylesheet" type="text/css" href="cc/kbinfo/dropdownlist.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/kbinfo/objectselect.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/kbinfo/objecttree.css"></link>


	<link rel="stylesheet" type="text/css" href="cc/flow/floweditor.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/flowchart.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/nodeeditor.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/node.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/rule.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/guard.css"></link>
	
</head>


<body onload="KBInfo._updateCache($('ajaxKBInfo'));showEditor();">

<%-- default kbinfo objects delivered from server --%>
<xml id="articleKBInfo" style="display:none;">
<%= jspHelper.getArticleInfoObjectsAsXML() %>
</xml>

<%-- default kbinfo objects delivered from server --%>
<xml id="referredKBInfo" style="display:none;">
<%= jspHelper.getReferredInfoObjectsAsXML() %>
</xml>


<xml id="ajaxKBInfo" style="display:none;">
	<kbinfo>

	</kbinfo>
</xml>


<xml id="flowchartSource" style="display:none;">
<%= jspHelper.loadFlowchart(request.getParameter("kdomID")) %>
</xml>

<table>
<tr>
	<td valign=top>
		<div id=prototype_title class=rollup_title>Node types</div>
		<div id=prototype_content class=rollup_content style="display: none;">
			<div style="max-height: 280px; overflow: auto;">
				<div style="padding: 5px;">
					<div id=decision_prototype class=NodePrototype> 
						<div class=Node style="position: relative; width: 120px;">
							<div class=decision>
								<div class=decorator></div>
								<div class=title>use object</div>
								<div class=text>drag this element to use wiki objects.</div>
							</div>
						</div>			
					</div>			
				</div>			
				<div style="padding: 5px;">
					<div id=start_prototype class=NodePrototype>
						<div  class=Node style="position: relative; width: 120px;">
							<div class=start>
								<div class=decorator></div>
								<div class=title>new start</div>
								<div class=text>drag this element to add a new start point.</div>
							</div>
						</div>			
					</div>			
				</div>			
				<div style="padding: 5px;">
					<div id=exit_prototype class=NodePrototype>
						<div class=Node style="position: relative; width: 120px;">
							<div class=exit>
								<div class=decorator></div>
								<div class=title>new exit</div>
								<div class=text>drag this element to add a new exit point.</div>
							</div>
						</div>			
					</div>			
				</div>			
				<div style="padding: 5px;">
					<div id=comment_prototype class=NodePrototype>
						<div class=Node style="position: relative; width: 120px;">
							<div class=comment>
								<div class=decorator></div>
								<div class=title>new comment</div>
								<div class=text>drag this element to add a new comment.</div>
							</div>
						</div>			
					</div>			
				</div>			
				<div style="padding: 5px;">
					<div id=snapshot_prototype class=NodePrototype>
						<div class=Node style="position: relative; width: 120px;">
							<div class=snapshot>
								<div class=decorator></div>
								<div class=title>new snapshot</div>
								<div class=text>drag this element to add a new snapshot.</div>
							</div>
						</div>			
					</div>			
				</div>			
			</div>
		</div>
		<div id=prototype_bottom class=rollup_bottom></div>
		
		<div id=objects_title class=rollup_title>Wiki objects</div>
		<div id=objects_content class=rollup_content style="display: visible;"></div>
		<div id=objects_bottom class=rollup_bottom></div>


	</td>
	<td rowspan=2 valign=top>
		<div class=toolbar>
			<table>
				<tr>
					<td rowspan=2>
						<button onclick="saveFlowchart(true);">
							<table cellpadding=0 cellspacing=0>
							<tr>
							<td><img src="cc/image/toolbar/save_and_close.png"></img></td>
							<td><div style="padding-left:5px; text-align:center;">Save<br>&amp;<br>close</div></td>
							</tr>
							</table>
						</button>
					</td>
					<td>
						<button onclick="saveFlowchart(false);" style="width:100%;text-align:left;vertical-align:middle;">
							<table cellpadding=0 cellspacing=0>
							<tr>
							<td><img src="cc/image/toolbar/save.png" height=16 width=16></img></td>
							<td style="padding-left:5px;">Save</td>
							</tr>
							</table>
						</button>
					</td>
					<td>
						<button onclick="window.close();" style="width:100%;text-align:left;vertical-align:middle;">
							<table cellpadding=0 cellspacing=0>
							<tr>
							<td><img src="cc/image/toolbar/cancel.png" height=16 width=16></img></td>
							<td style="padding-left:5px;">Cancel
							</tr>
							</table>
						</button>
					</td>
					<td rowspan=2 valign=top class=propertyArea>
						<span class=propertyBlock>
							<table>
							<tr>
							<td class=propertyTitle>Name:</td>
							</tr>
							<tr>
							<td><input type=text id="properties.editName" class="propertyText long"></input></td>
							</tr>
							</table>
						</span>
						<span class=propertyBlock>
							<table>
							<tr>
							<td class=propertyTitle>Width:</td>
							<td><input type=text id="properties.editWidth" class="propertyText short"></input></td>
							</tr>
							<tr>
							<td class=propertyTitle>Height:</td>
							<td><input type=text id="properties.editHeight" class="propertyText short"></input></td>
							</tr>
							</table>
						</span>
					</td>
				</tr>
				<tr>
					<td>
						<button onclick="window.location.reload();" style="width:100%;text-align:left;vertical-align:middle;">
							<table cellpadding=0 cellspacing=0>
							<tr>
							<td><img src="cc/image/toolbar/refresh.png" height=16 width=16></img></td>
							<td style="padding-left:5px;">Refresh
							</tr>
							</table>
						</button>
					</td>
					<td>
						<button onclick="deleteFlowchart();" style="width:100%;text-align:left;vertical-align:middle;">
							<table cellpadding=0 cellspacing=0>
							<tr>
							<td><img src="cc/image/toolbar/delete.png" height=16 width=16></img></td>
							<td style="padding-left:5px;">Delete</td>
							</tr>
							</table>
						</button>
					</td>
				</tr>
			</table>
		</div>
		<div id=contents style="position:relative"></div>
	</td>
</tr>
</table>

<div id=message></div>

<script>
	// kbinfo initialization
	KBInfo._updateCache($('articleKBInfo'));
	KBInfo._updateCache($('referredKBInfo'));
	
	// make rollups responsive
	var prototypes = new Rollup('prototype_title', 'prototype_content');
	var objects =  new Rollup('objects_title', 'objects_content');
	// and group them to display only one at the same time 
	// (can be comment out to enable independed toggle behaviour)
	new RollupGroup([prototypes, objects]);

	// initialize wiki tree tool
	new ObjectTree('objects_content', null, 
		<%= jspHelper.getArticleIDsAsArray() %>
	);

	var theFlowchart = null;

	// create example flowchart by delivered xml
	function showEditor() {
		theFlowchart = Flowchart.createFromXML('contents', $('flowchartSource'));
		theFlowchart.setVisible(true);
		$('properties.editName').value = theFlowchart.name || theFlowchart.id;
		$('properties.editWidth').value = theFlowchart.width;
		$('properties.editHeight').value = theFlowchart.height;
		$('properties.editName').onchange = updateProperties;
		$('properties.editWidth').onchange = updateProperties;
		$('properties.editHeight').onchange = updateProperties;
	}
	
	function updateProperties() {
		theFlowchart.name = $('properties.editName').value;
		theFlowchart.setSize($('properties.editWidth').value, $('properties.editHeight').value);
	}
	
	function deleteFlowchart() {
		var result = confirm('Wollen Sie das Flussdiagramm wirklich aus dem Wiki loeschen?');
		if (result) {
			_saveFlowchartText('', true);
		}
	}
	
	function saveFlowchart(closeOnSuccess) {
		theFlowchart.setSelection(null, false, false);
		var xml = theFlowchart.toXML(true); // include preview for us
		_saveFlowchartText(xml, closeOnSuccess);
	}
	
	function _saveFlowchartText(xml, closeOnSuccess) {
		var url = "KnowWE.jsp";
		new Ajax.Request(url, {
			method: 'post',
			parameters: {
				action: 'SaveFlowchartAction',
				KWiki_Topic: topic,			// article
				TargetNamespace: nodeID,	// KDOM nodeID
				KWikitext: xml				// content
			},
			onSuccess: function(transport) {
				if (closeOnSuccess) window.close();
			},
			onFailure: function() {
				CCMessage.warn(
					'AJAX Verbindungs-Fehler', 
					'Die Aenderungen konnten nicht gespeichert werden.');
			},
			onException: function(transport, exception) {
				CCMessage.warn(
					'AJAX interner Fehler, Aenderungen eventuell verloren',
					exception
					);
			}
		}); 		

		
	}


	// initialize prototypes to create new nodes	
	function revertIt(element,  top_offset, left_offset) {
		element = $(element);
	    element.makePositioned();
		var x = parseFloat(element.getStyle('left') || '0');
		var y = parseFloat(element.getStyle('top')  || '0');
		element.setStyle({
			left: (x - left_offset) + 'px',
			top:  (y - top_offset) + 'px'
		});
	}

	new Draggable('decision_prototype', { ghosting: true, revert: true, reverteffect: revertIt, starteffect: null });
	new Draggable('start_prototype', { ghosting: true, revert: true, reverteffect: revertIt, starteffect: null });
	new Draggable('exit_prototype', { ghosting: true, revert: true, reverteffect: revertIt, starteffect: null });
	new Draggable('comment_prototype', { ghosting: true, revert: true, reverteffect: revertIt, starteffect: null });
	new Draggable('snapshot_prototype', { ghosting: true, revert: true, reverteffect: revertIt, starteffect: null });
	$('decision_prototype').createNode = function(flowchart, left, top) { createActionNode(flowchart, left, top, {action: { markup: 'KnOffice', expression: ''}}); };
	$('start_prototype').createNode = function(flowchart, left, top) { createActionNode(flowchart, left, top, {start: 'start'}); };
	$('exit_prototype').createNode = function(flowchart, left, top) { createActionNode(flowchart, left, top, {exit: 'done'}); };
	$('comment_prototype').createNode = function(flowchart, left, top) { createActionNode(flowchart, left, top, {comment: 'Kommentar'}); };
	$('snapshot_prototype').createNode = function(flowchart, left, top) { createActionNode(flowchart, left, top, {snapshot: 'Snapshot'}); };
	
	function createActionNode(flowchart, left, top, nodeModel) {
		nodeModel.position = {left: left, top: top};
		var node = new Node(flowchart, nodeModel);
		node.select();
		node.edit();
	};
  
</script>

<wiki:Include page="<%=\"\"%>" />

</body>
</html>