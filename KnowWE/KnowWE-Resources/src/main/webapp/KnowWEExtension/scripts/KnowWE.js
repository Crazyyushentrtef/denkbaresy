/**
 * Title: KnowWE-core
 * Contains javascript functions the KnowWE core needs to functions properly.
 * The functions are based upon some KnowWE helper functions and need the
 * KNOWWE-helper.js in order to work correct.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
    /**
     * The KNOWWE global namespace object.  If KNOWWE is already defined, the
     * existing KNOWWE object will not be overwritten so that defined
     * namespaces are preserved.
     */
    var KNOWWE = {};
}
/**
 * Namespace: KNOWWE.core
 * The KNOWWE core namespace.
 * Contains some init functions.
 */
KNOWWE.core = function(){
    return {
        /**
         * Function: init
         * Core init functions.
         */
        init : function(){
            KNOWWE.core.util.addCollabsiblePluginHeader();
            KNOWWE.core.actions.init();
        }
    }
}();
/**
 * Namespace: KNOWWE.core.actions
 * The KNOWWE actions namespace object.
 * Contains all actions that can be triggered in KnowWE per javascript.
 */
KNOWWE.core.actions = function(){
    return {
        /**
         * Function: init
         * Core KnowWE actions.
         */
        init : function(){           
            //init show extend panel
            els = _KS('.show-extend'); 
            if( els ){
                els.each(function(element){
                    _KE.add('click', element, KNOWWE.core.util.form.showExtendedPanel); 
                });
            }
            
            //enable clearHTML
            _KS('.clear-element').each(function(element){
                _KE.add('click', element, KNOWWE.core.actions.clearHTML);
            });
            
            _KS('.js-cell-change').each(function(element){
                _KE.add('change', element, KNOWWE.core.actions.cellChanged);
            });
        },
        /**
         * Function: clearHTML
         * Clears the inner HTML of a given element.
         * 
         * Parameters:
         *     e - The occurred event.
         */
        clearHTML : function( e ){
            var el = KNOWWE.helper.event.target( e );
            if( el.id ){
                _KS(el.id)._clear();
            }
        },
        /**
         * Function: cellChanged
         * 
         * Parameters:
         *     e - The occurred event.
         */
        cellChanged : function( e ) {
            var el = KNOWWE.helper.event.target( e );
            var rel = el.getAttribute('rel');
            
            if(!rel) return;
            rel = rel.parseToObject();
            
            var nodeID = rel.id;
            var topic = rel.title;
            
            el = _KS('#' + nodeID);
            if( el ) {
                var selectedOption = el.options[el.selectedIndex].value; 
             
                var params = {
                    action : 'ReplaceKDOMNodeAction',
                    TargetNamespace : nodeID,
                    KWikitext : selectedOption,
                    KWiki_Topic : topic
                }
                var options = {
                    url : KNOWWE.helper.getURL( params ),
                    response : {
                        action : none,
                        fn : null
                    }
                }
                new _KA( options ).send();
            }
        }
    }
}();

/**
 * Namespace: KNOWWE.core.util
 * The KNOWWE core util namespace object.
 * Contains some helper functions. For detailed information read the comments
 * above each function.
 */
KNOWWE.core.util = function(){

	var activityCounter = 0;
	var indicatorShouldBeVisible = false;
	
    return {
        /**
         * Function updateProcessingState
         *
         * Updates the hidden element in the page to contain
         * the current processing state
         */
        updateProcessingState : function (delta) {
        	activityCounter += delta;
        	var node = $('KnowWEProcessingState');
        	if (!node) {
        		node = new Element('div', {'id': 'KnowWEProcessingState', 'styles': { 
        			'display': 'none', 'position': 'absolute', 'top': '0px', 'left': '0px' }});
        		document.body.appendChild(node);
        	}
        	if (activityCounter > 0) {
        		node.innerHTML = "processing";
        		KNOWWE.core.util.showProcessingIndicator();
        	}
        	else {
        		// hide animation
        		node.innerHTML = "idle";
        		KNOWWE.core.util.hideProcessingIndicator();
        	}
        },
        showProcessingIndicator : function () {
        	indicatorShouldBeVisible = true;
        	window.setTimeout("KNOWWE.core.util.updateProcessingIndicator()", 100);
        },
        hideProcessingIndicator : function () {
        	indicatorShouldBeVisible = false;
        	KNOWWE.core.util.updateProcessingIndicator();
        },
        updateProcessingIndicator : function () {
       		var node = $('KnowWEProcessingIndicator');
    		if (!node && indicatorShouldBeVisible) {
    			// not visible but should be --> show it
        		node = new Element('div', {'id': 'KnowWEProcessingIndicator', 'styles': { 
        			'display': 'block', 'position': 'fixed', 'top': '0px', 'left': '0px' }});
        		document.body.appendChild(node);
        		node.innerHTML = '<img src="KnowWEExtension/images/ajax-loader16.gif"></img>';
        	}
        	else if (node && !indicatorShouldBeVisible) {
    			// visible but should not be --> delete it
    			node.remove();
        	}
        },
        /**
         * Function: addCollabsiblePluginHeader
         * Extends the headings of the KnowWEPlugin DIVs with collabs ability.
         * The function searches for all DIVs with an ".panel" class attribute and
         * extends them. The plugin DIV should have the following structure in order
         * to work properly:
         * (start code)
         * <div class='panel'><h3>Pluginname</h3><x>some plugin content</x></div>
         * (end)
         * 
         * Parameters: 
         *     id - Optional id attribute. Specifies the DOM element, the collabsible
         *          functionality should be applied to.
         */
        addCollabsiblePluginHeader : function( id ){
            var selector = "div .panel";
            if( id ) {
                selector = id;
            }
            
            var panels = _KS( selector );
            if( panels.length < 1 ) return;
            if( !panels.length ) panels = new Array(panels);
            
            for(var i = 0; i < panels.length; i++){
                var span = new _KN('span');
                span._setText('- ');
                
                var heading = panels[i].getElementsByTagName('h3')[0];
                if(!heading.innerHTML.startsWith('<span>')){
                     span._injectTop( heading );
                }
                _KE.add('click', heading , function(){
                    var el = new _KN( this );
                    var style = el._next()._getStyle('display');
                    style = (style == 'block') ? 'none' : ((style == '') ? 'none' : 'block');                    
                    
                    el._getChildren()[0]._setText( (style == 'block')? '- ' : '+ ' );
                    el._next()._setStyle('display', style);
                });
            }
        },
        /**
         * Function: getURL
         * Returns an URL created out of the given parameters.
         * e.g.: 
         * (start code)
         *  var params = {
         *      renderer : 'KWiki_dpsSolutions',
         *      KWikiWeb : 'default_web'
         *  }
         * KNOWWE.util.getURL( params ) --> KnowWE.jsp?renderer=KWiki_dpsSolutions&KWikiWeb=default_web
         * (end)
         * 
         * Parameters:
         *     params - The parameter for the URL.
         * 
         * Returns:
         *     The URL containing the elements of the params array.
         */
         getURL : function( params ){
            var baseURL = 'KnowWE.jsp';
            var tokens = [];
        
            if( !params && typeof params != 'object') return baseURL;
                        
            for( keys in params ){
                var value = params[keys] ; 
                if(typeof value != 'string') value = JSON.stringify( params[keys] ); 
                tokens.push(keys + "=" + escape(encodeURIComponent( value )));
            }
            
            //parse the url to add special token like debug etc.
            var p = document.location.search.replace('?','').split('&');
            for(var i = 0; i < p.length; i++){
                var t = p[i].split('=');
                if(!KNOWWE.helper.containsArr(tokens,t[0])){
                    tokens.push( t[0] + "=" + encodeURIComponent( t[1] ));
                }
            }
            tokens.push('tstamp='+new Date().getTime());            
            return baseURL + '?' + tokens.join('&');
        },       
        /**
         * Function: getWindowParams
         * Returns an URL which is used as the target URL for a popup window.
         * 
         * Parameters:
         *     params - The parameter for the popup window
         * Returns:
         *     The url for the popup window
         */
        getWindowParams : function( params ){
            if( !params && typeof params != 'object') return '';
            var tokens = [];
            for( keys in params ){
                if(keys == 'url') continue;
                tokens.push(keys + "=" + params[keys]);
            }
            return tokens.join(',');
        },
        /**
         * Function: replace
         * Used to replace elements in the DOM tree. All elements given in the ids
         * array are replaced with the value of the second parameter. If one
         * element is not found, nothing happens.
         * 
         * Parameters:
         *     ids - The ids of the elements that should be replaced.
         *     value - The value used for replacement.
         */
        replace : function(ids, value){
        	
            for( var i = 0; i < ids.length; i++){
                if( typeof ids[i] != 'string' ) continue;
                
                  var oldDOM = document.getElementById( ids[i] );
                  var newDOMwrapper = document.createElement("div");
                  newDOMwrapper.innerHTML = value;
                  
                  var domChildNodes = newDOMwrapper.children;
                  
                  for(var j = 0; j < domChildNodes.length; j++) {
                      var newDOM = domChildNodes[j];
                      oldDOM = document.getElementById(newDOM.id);
                      if(oldDOM) {
                          oldDOM.parentNode.replaceChild( newDOM, oldDOM );
                  	  }
                  }
            }
        }
    }
}();

/**
 * Namespace: KNOWWE.core.util.form
 * Some helper functions concerning HTML form elements.
 */
KNOWWE.core.util.form = function(){
    return {
        /**
         * Function: getCursorPositionInTextArea
         * Does get the current position of the cursor inside a textarea.
         * 
         * Parameters:
         *     textarea - The textarea
         * 
         * Returns: 
         *     The position of the cursor inside the textarea.
         */
        getCursorPositionInTextArea : function( textarea ){
            if( document.selection ){
                var range = document.selection.createRange();
                var stored_range = range.duplicate();
                stored_range.moveToElementText( textarea );           
                stored_range.setEndPoint( 'EndToEnd', range );
                textarea.selectionStart = stored_range.text.length - range.text.length;            
                return textarea.selectionStart + range.text.length;
            } 
            else {
                if(textarea.selectionEnd){
                    textarea.focus();
                    return textarea.selectionEnd;
                }
            }
        },          
        /**
         * Function: insertAtCursor
         * Inserts an text element at the current cursor position in a textarea, etc.
         * 
         * Parameters:
         *     element - The textarea, etc.
         *     value - The text string
         */
        insertAtCursor : function(element, value) {
            if (document.selection) { 
                element.focus();
                sel = document.selection.createRange();
                sel.text = value;
            } else if(element.selectionStart || element.selectionStart == '0'){ 
                 var startPos = element.selectionStart;
                 var endPos = element.selectionEnd;
                 element.value = element.value.substring(0, startPos) + value
                     + element.value.substring(endPos, element.value.length);
                 element.setSelectionRange(endPos + value.length, endPos + value.length);
            } else {
                element.value = value;
            }
            element.focus();
        },
        /**
         * Function: addFormHints
         * Shows a small overlay text containing additional information about an
         * input HTMLElement. Used for e.g. in the KnofficeUploader.
         * 
         * Parameters:
         *     name - The name of the HTMLElement
         */
        addFormHints : function( name ){
            if(!_KS('#' + name)) return;
            
            var els = document.getElementById(name + '-extend').getElementsByTagName("input");
            for (var i = 0; i < els.length; i++){
                var tag = els[i].nextSibling.tagName;
                if( !tag) continue;

              if(tag.toLowerCase() == 'span'){
                _KE.add('focus', els[i], function (e) {
                   var el = _KE.target( e );
                   el.nextSibling.style.display = "inline";});
                _KE.add('blur', els[i], function (e) {
                   var el = _KE.target( e );
                   el.nextSibling.style.display = "none";});
              }
            }
        },
        /**
         * Function: showExtendedPanel
         * Shows a panel in certain plugin with additional options.
         */
        showExtendedPanel : function(){
            var el = this;

            var style = el._next().style;
            el.removeAttribute('class');           
         
            if(style['display'] == 'inline'){
                style['display'] = 'none';
                //el.setAttribute('class', 'show extend pointer extend-panel-down');
                el.setAttribute('class', 'show extend pointer extend-panel-right');
            }else{
                style['display'] = 'inline';
                el.setAttribute('class', 'show extend pointer extend-panel-down');
            }
        }
    }    
}();

/**
 * Namespace: KNOWWE.core.rerendercontent
 * Rerenders parts of the article.
 */
KNOWWE.core.rerendercontent = function(){
    return {
        /**
         * Function: init
         */
        init : function(){
            KNOWWE.helper.observer.subscribe( 'update', KNOWWE.core.rerendercontent.update );
        },
        /**
         * Function: updateNode
         * Updates a node.
         * 
         * Parameters:
         *     node - The node that should be updated.
         *     topic - The name of the page that contains the node.
         */
        updateNode : function(node, topic, ajaxToHTML) {
            var params = {
                action : 'ReRenderContentPartAction',
                KWikiWeb : 'default_web',
                KdomNodeId : node,
                KWiki_Topic : topic,
                ajaxToHTML : ajaxToHTML

            }
            var url = KNOWWE.core.util.getURL( params );
            KNOWWE.core.rerendercontent.execute(url, node, 'insert');
        },
        /**
         * Function: update
         * 
         */
        update : function() {
            var classlist = _KS('.ReRenderSectionMarker');             
            
            if ( classlist.length != 0 ) {
                for (var i = 0; i < classlist.length; i++) {
                    var rel = classlist[i].getAttribute('rel');
                    if(!rel) continue;
                    rel = eval("(" + rel + ")" );
                    
                    var params = {
                        action : 'ReRenderContentPartAction',
                        KWikiWeb : 'default_web',
                        KdomNodeId : rel.id,
                        //works for now, but when the IDs change it breaks again
                        //KWiki_Topic : rel.id.substring(0, rel.id.indexOf('/')), 
                        //KWiki_Topic : KNOWWE.helper.gup('page'),
                        ajaxToHTML : "render",
                        inPre : KNOWWE.helper.tagParent(_KS('#' + rel.id), 'pre') != document 
                    }           
                    var url = KNOWWE.core.util.getURL( params );
                    KNOWWE.core.rerendercontent.execute(url, rel.id, 'replace');
                }
            }
        },
        /**
         * Function: execute
         * Sends the rerendercontent AJAX request.
         * 
         * Parameters:
         *     url - The URL for the AJAX request.
         *     id - The id of the node that should be updated.
         */
        execute : function( url, id, action) {
            var options = {
                url : url,
                response : {
                    ids : [ id ],
                    action : action,
                    fn : function(){
			        	try {
	                        KNOWWE.core.actions.init();
	                        Collapsible.render( _KS('#page'), KNOWWE.helper.gup('page'));
			        	}
			        	catch (e) { /*ignore*/ }
			        	KNOWWE.core.util.updateProcessingState(-1);
                    },
                    onError : function () {
			        	KNOWWE.core.util.updateProcessingState(-1);                    	
                    }
                }
            }
        	KNOWWE.core.util.updateProcessingState(1);
            new _KA( options ).send();
        }
    }
}();

/**
 * Namespace: KNOWWE.plugin
 * The KNOWWE plugin namespace.
 * Initialized empty to ensure existence.
 */
KNOWWE.plugin = function(){
    return {
    }
}();

/**
 * Aliases for some often used namespaced function to reduce typing.
 */
var _KE = KNOWWE.helper.event;    /* Alias KNOWWE event. */
var _KA = KNOWWE.helper.ajax;     /* Alias KNOWWE ajax. */
var _KS = KNOWWE.helper.selector; /* Alias KNOWWE ElementSelector */
var _KL = KNOWWE.helper.logger;   /* Alias KNOWWE logger */
var _KN = KNOWWE.helper.element   /* Alias KNOWWE.helper.element */
var _KH = KNOWWE.helper.hash      /* Alias KNOWWE.helper.hash */

/* ############################################################### */
/* ------------- Onload Events  ---------------------------------- */
/* ############################################################### */
(function init(){
    
    window.addEvent( 'domready', _KL.setup );

    if( KNOWWE.helper.loadCheck( ['Wiki.jsp'] )){
        window.addEvent( 'domready', function(){
            KNOWWE.core.init();
            KNOWWE.core.rerendercontent.init();
            setTimeout(function(){KNOWWE.helper.observer.notify('onload')}, 50);
            //setTimeout(function(){KNOWWE.helper.observer.notify('update')}, 50);
        });
    };
}());
