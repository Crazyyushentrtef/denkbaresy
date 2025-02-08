
function Rule(id, sourceNode, guard, targetNode) {
	this.flowchart = sourceNode.flowchart;
	this.fcid = id || this.flowchart.createID('rule');
	this.sourceNode = sourceNode;
	this.guard = guard;
	this.guardPane = null;
	this.guardEditor = null;
	this.targetNode = targetNode;
	this.dom = null;
	this.coordinates = [];
	
	// add to parent flowchart
	this.flowchart.addRule(this);
	
	// and inherit the visibility
	this.setVisible(this.flowchart.isVisible());
}

// register select click events for flowchart
CCEvents.addClassListener('click', 'Rule', 
	function(event) {
		this.__rule.select();
	}
);


Rule.prototype.getDOM = function() {
	return this.dom;
}

Rule.prototype.isVisible = function() {
	return (this.dom != null);
}

Rule.prototype.setVisible = function(visible) {
	if (!this.isVisible() && visible) {
		// ==> show Node
		var selected = this.flowchart.isSelected(this);
		this.dom = this.render(selected);
		this.flowchart.getContentPane().appendChild(this.dom);
		this.draggable = this.createDraggable();
		this.setGuardVisible(!selected, selected);
	}
	else if (this.isVisible() && !visible) {
		// ==> hide Node
		this.setGuardVisible(false, false);
		this.draggable.destroy();
		this.draggable = null;
		this.flowchart.getContentPane().removeChild(this.dom);
		this.dom = null;
	}
}

Rule.prototype.render = function(selected) {
	var html = '';
	var highlight = 
			'<div class="rule_highlight" style="position:absolute;' +
			(!selected ? ' visibility: hidden;' : '') +
			'">';
	var selector = '<div onclick="this.parentNode.__rule.select();" style="position:absolute; visibility: visible; z-index:0;">';
	
	for (var i=0; i<this.coordinates.length-1; i++) {
		var x1 = this.coordinates[i][0];
		var y1 = this.coordinates[i][1];
		var x2 = this.coordinates[i+1][0];
		var y2 = this.coordinates[i+1][1];
		var x, y, w, h, clazz, arrow;
		if (x1==x2) {
			// vertical line
			x = x1;
			y = Math.min(y1, y2);
			w = 1;
			h = Math.abs(y1 - y2)+1;
			clazz = "v_line";
			arrow = (y1 > y2) ? "arrow_up" : "arrow_down";
		}
		else {
			// horizontal line
			x = Math.min(x1, x2);
			y = y1;
			w = Math.abs(x1 - x2)+1;
			h = 1;
			clazz = "h_line";
			arrow = (x1 > x2) ? "arrow_left" : "arrow_right";
		}
		if (i+2 < this.coordinates.length) arrow="no_arrow"; 
		html += '<div class=' + clazz + ' style="position:absolute; overflow:visible; ' +
			'left: ' + x + 'px; ' +
			'top: ' + y + 'px; ' +
			'width: ' + w + 'px; ' +
			'height: ' + h + 'px;">' +
			'<div class='+arrow+'></div>' +
			'</div>';
		highlight += 
			// highlight
			'<div ' +
			'class=' + clazz + '_highlight ' +
			'style="position: absolute; overflow:hidden; ' +
			'left: ' + (x-1) + 'px; ' +
			'top: ' + (y-1) + 'px; ' +
			'width: ' + (w+2) + 'px; ' +
			'height: ' + (h+2) + 'px;"></div>';
		selector +=
			// selector
			'<div ' +
			'style="position: absolute; overflow:hidden; ' +
			'left: ' + (x-3) + 'px; ' +
			'top: ' + (y-3) + 'px; ' +
			'width: ' + (w+6) + 'px; ' +
			'height: ' + (h+6) + 'px;"></div>';
	}

	if (this.sourceAnchor && this.coordinates.length > 0) {
		var rect = this.sourceAnchor.getGuardPosition();
		html += 
			'<div style="position:absolute; overflow: visible; ' +
			'left: '+this.coordinates[0][0]+'px; top: '+this.coordinates[0][1]+'px; width: 0px; height: 0px;">' + 
			'<div class="guard" style="position:absolute;';
		if (rect.top) html += ' top: '+rect.top+'px; ';
		if (rect.bottom) html += ' bottom: '+rect.bottom+'px; ';
		if (rect.left) html += ' left: '+rect.left+'px; ';
		if (rect.right) html += ' right: '+rect.right+'px; ';
		if (rect.width) html += ' max-width: '+rect.width+'px; ';
		if (rect.height) html += ' max-height: '+rect.height+'px; ';
		html += '"></div></div>';
	}
	highlight += "</div>";
	selector += "</div>";
	
	var dom = Builder.node('div', {
		id: this.fcid,
		className: 'Rule',
		style: "position:absolute; left: 0px; top: 0px; overflow: visible; cursor: crosshair;"
	});
	dom.innerHTML = html + highlight + selector;
	dom.__rule = this;
	return dom;
}

Rule.prototype.createDraggable = function() {
	var newDrag = new Draggable(this.getDOM(), {
		ghosting: false,
		revert: true, 
		starteffect: null,
		endeffect: null
	});
	newDrag.__rule = this;
	return newDrag;	
}


Rule.prototype.intersects = function(x1, y1, x2, y2) {
	var xMin = Math.min(x1, x2);
	var xMax = Math.max(x1, x2);
	var yMin = Math.min(y1, y2);
	var yMax = Math.max(y1, y2);
	for (var i=0; i<this.coordinates.length-1; i++) {
		var lx1 = this.coordinates[i][0];
		var ly1 = this.coordinates[i][1];
		var lx2 = this.coordinates[i+1][0];
		var ly2 = this.coordinates[i+1][1];
		var lxMin = Math.min(lx1, lx2);
		var lxMax = Math.max(lx1, lx2);
		var lyMin = Math.min(ly1, ly2);
		var lyMax = Math.max(ly1, ly2);
		if ((xMin < lxMax) && (yMin < lyMax) && (xMax > lxMin) && (yMax > lyMin)) {
			return true;
		}
	}
}


Rule.prototype.select = function() {
	this.flowchart.setSelection(this);
}

Rule.prototype.setGuard = function(guard) {
	this.guard = guard;
	if (this.guardPane) {
		this.setGuardVisible(true, false);		
	}
}

Rule.prototype.notifyNodeChanged = function(node) {
	var visible = this.isVisible();
	this.setVisible(false);
	if (this.guardPane) {
		this.guardPane.checkProblems(this);
	}
	if (this.guard && !Object.isString(this.guard)) {
		this.guard.lookupDisplayHTML(this.sourceNode.getPossibleGuards());
	}
	this.setVisible(visible);
}

Rule.prototype.getGuardRoot = function() {
	return (this.dom ? this.dom.select('.guard')[0] : null);
}

Rule.prototype.setSelectionVisible = function(isSelected) {
	if (!this.isVisible()) return;
	var hightlight = this.dom.select('.rule_highlight')[0];
	if (isSelected) {
		// show highlight
		// TODO: avoid using id as dom id(s)
		hightlight.style.visibility = 'visible';
		this.setGuardVisible(false, true);
	}
	else {
		hightlight.style.visibility = 'hidden';
		this.setGuardVisible(true, false);
	}
}

Rule.prototype.setGuardVisible = function(paneVisible, editorVisible) {
	if (this.guardPane) {
		this.guardPane.destroy();
		this.guardPane = null;
	}
	if (this.guardEditor) {
		this.guardEditor.destroy();
		this.guardEditor = null;
	}
	if (paneVisible) {
		this.guardPane = new GuardPane(this.getGuardRoot(), this.guard, this);
	}
	if (editorVisible) {
		this.guardEditor = new GuardEditor(
			this.getGuardRoot(), 
			this.guard, 
			this.sourceNode.getPossibleGuards(),
			this.handleGuardSelected.bind(this));
	}
}

Rule.prototype.handleGuardSelected = function(guard) {
	this.setGuard(guard);
}

Rule.prototype.setSourceAnchor = function(anchor) {
	this.sourceAnchor = anchor;
}

Rule.prototype.getSourceAnchor = function() {
	return this.sourceAnchor;
}

Rule.prototype.getSourceNode = function() {
	return this.sourceNode;
}

Rule.prototype.setTargetAnchor = function(anchor) {
	this.targetAnchor = anchor;
}

Rule.prototype.getTargetAnchor = function() {
	return this.targetAnchor;
}

Rule.prototype.getTargetNode = function() {
	return this.targetNode;
}

Rule.prototype.getAnchor = function(node) {
	return (this.targetNode == node) ? this.targetAnchor : this.sourceAnchor;
}

Rule.prototype.setCoordinates = function(coordinates) {
	// check if coordinates have changed
	if (this.coordinates.equals(coordinates)) return;
	this.coordinates = coordinates;
	if (this.isVisible()) {
		this.setVisible(false);
		this.setVisible(true);
	}
}

Rule.prototype.getOtherNode = function(node) {
	var result = (this.targetNode == node) ? this.sourceNode : this.targetNode;
	//showMessage('Rule['+this.sourceNode.id+', '+this.targetNode.id+'].getOtherNode('+node+') --> '+result);
	return result;
}


Rule.prototype.destroy = function() {
	if (this._destroyed) return;
	this._destroyed = true;
	// deselect the item (if selected)
	this.flowchart.removeFromSelection(this);
	// this only works if there is no endeffekt in the draggable
	// because the case that the div has been removed is not
	// considered in the drag&drop framework.
	this.draggable.options.endeffekt = null;
	this.setVisible(false);
	this.flowchart.removeRule(this);
}


Rule.prototype.toXML = function() {
	var xml = '\t<edge' +
			(this.fcid ? ' fcid="'+this.id+'"' : '')+
			'>\n';
	xml += '\t\t<origin>'+this.sourceNode.getNodeModel().fcid+'</origin>\n';
	xml += '\t\t<target>'+this.targetNode.getNodeModel().fcid+'</target>\n';
	if (this.guard && this.guard.getMarkup() != 'NOP') {
		if (Object.isString(this.guard)) {
			xml += '\t\t<guard>' + this.guard.escapeXML() + '</guard>\n';
		}
		else {
			xml += '\t\t<guard markup="'+this.guard.getMarkup()+'">' +
					this.guard.getConditionString().escapeXML()+
					'</guard>\n';
		}
	}
	xml += '\t</edge>\n';
	return xml;
}


Rule.createFromXML = function(flowchart, xmlDom, pasteOptions) {
	var id = pasteOptions.createID(xmlDom.getAttribute('fcid'));
	var sourceNodeID = pasteOptions.getID(KBInfo._getNodeValueIfExists(xmlDom, 'origin'));
	var targetNodeID = pasteOptions.getID(KBInfo._getNodeValueIfExists(xmlDom, 'target'));
	var sourceNode = flowchart.findNode(sourceNodeID);
	var targetNode = flowchart.findNode(targetNodeID);
	
	if (!sourceNode) return null;
	if (!targetNode) return null;
	
	var guard = null;
	var guardDoms = xmlDom.getElementsByTagName('guard');
	if (guardDoms && guardDoms.length > 0) {
		var markup = guardDoms[0].getAttribute('markup') || 'KnOffice';
		var conditionString = KBInfo._nodeText(guardDoms[0]);
		guard = new Guard(markup, conditionString);
		guard.lookupDisplayHTML(sourceNode.getPossibleGuards());
	}
	else {
		guard = new Guard('NOP', ' ', ' ');
	}
	
	return new Rule(id, sourceNode, guard, targetNode);
}


// ----
// Anchor for having rules anchored to the nodes
// ----

function Anchor(node, x, y, type, slide) {
	this.node = node;
	this.x = x;
	this.y = y;
	this.type = type;
	this.slide = slide;
}

/*
Anchor.prototype.getPossibleGuards = function() {
	var nodeModel = this.node.getNodeModel();
	if (!this._possibleGuards || this._lastNodeModel != nodeModel) {
		this._possibleGuards = Guard.createPossibleGuards(nodeModel);
		this._lastNodeModel = nodeModel;
	}
	return this._possibleGuards;
}
*/

Anchor.prototype.getGuardPosition = function() {
	if (this.type == 'top') {
		return { left: 2, bottom: 3, width: 50 };
	}
	else if (this.type == 'bottom') {
		return { left: 2, top: 3, width: 50 };
	}
	else if (this.type == 'left') {
		return { right: 3, bottom: 0, height: 20 };
	}
	else {
		return { left: 3, bottom: 0, height: 20 };
	}
}

/*
Anchor.prototype.createGuardEditorHTML = function(text, rule) {
	if (!text) text = '---';
	
	var pos;
	if (this.type == 'top') {
		pos = 'left: 2px; bottom: 3px;';
	}
	else if (this.type == 'bottom') {
		pos = 'left: 2px; top: 3px;'; 		
	}
	else if (this.type == 'left') {
		pos = 'right: 3px; bottom: 0px;';
	}
	else {
		pos = 'left: 3px; bottom: 0px;';
	}
	
	// create selector for the possible guards
	var guards = this.getPossibleGuards();
	var condition = 
		!rule.guard	? '' :
		Object.isFunction(rule.guard.getConditionString) ? rule.guard.getConditionString() : 
		Object.toHTML(rule.guard).escapeHTML();
	var editor = '';
	if (guards) {
		var hasCondition = false;
		for (var i=0; i<guards.length; i++) {
			var guard = guards[i];
			if (condition == guard.getConditionString()) hasCondition = true;
			editor += '<option value=' + i + 
					(condition == guard.getConditionString() ? ' selected': '') +
					'>' + guard.getDisplayHTML() + '</option>';
		}
		if (!hasCondition) {
			this._possibleGuards.push(guard);
			editor += '<optgroup label="--- unerwarteter Wert ---"></optgroup>'
			editor += '<option class=warning value='+guards.length+' selected>'+condition+'</option>'
		}
		editor = '<select class=guard onchange="' +
				'var rule = this.up(\'.Rule\').__rule;' +
				'var anchor = rule.getSourceAnchor();' +
				'rule.setGuard(anchor._possibleGuards[this.value]);">' + 
				editor + 
				'</select>';
	}
	else {
		//editor = '<input type=text value="'+condition+'"></input>';
	}
	
	var html = 
	'<div style="position:absolute; overflow: visible; z-index: 200; left: '+this.x+'px; top: '+this.y+'px; width: 0px; height: 0px;">' + 
		'<div class=guard style="position:absolute; ' + pos + '">' + 
			//'<input class=guard type=text value="' + text.escapeHTML() + '"></input>' +
			editor +
		'</div>' + 
	'</div>';
	return html;
}
*/
