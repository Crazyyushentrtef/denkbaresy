var TestCasePlayer = {};

TestCasePlayer.init = function() {
	jq$(".type_TestCasePlayer").find(".wikitable").find("th").click(TestCasePlayer.initColumnHeaders);
}

TestCasePlayer.initColumnHeaders = function() {
	var column = jq$(this).attr("column");
	var collapsed = "";
	jq$(this).siblings().each(function() {
		if (jq$(this).hasClass("collapsedcolumn")) {
			collapsed += jq$(this).attr("column") + "#";
		}
	});
	collapsed += column;
	
	var id = jq$(this).parents(".type_TestCasePlayer").first().attr("id");
	var testCase = jq$("#" + id).find("select").find('[selected="selected"]').attr("value");
	
	document.cookie = "columnstatus_" + id + "_" + testCase + "=" + collapsed;
	
	var tds = jq$(this).parents(".wikitable").first().find('[column="' + column + '"]');
	if (jq$(this).hasClass("collapsedcolumn")) {
		tds.removeClass("collapsedcolumn");
	} else {			
		tds.addClass("collapsedcolumn");
	}
}

TestCasePlayer.send = function(sessionid, casedate, name, topic) {
			
            var params = {
        		action : 'ExecuteCasesAction',
       			KWiki_Topic : topic,
       			id : sessionid,
        		date : casedate,
        		testCaseName : name
    		}
            
            var options = {
                url : KNOWWE.core.util.getURL(params),
                response : {
                	action : 'none',
                	fn : function(){
			        	try {
	                		KNOWWE.helper.observer.notify('update');
			        	}
			        	catch (e) { /*ignore*/ }
			        	KNOWWE.core.util.updateProcessingState(-1);
                	},
                    onError : function () {
			        	KNOWWE.core.util.updateProcessingState(-1);
			        	if (this.status == null) return;
        	switch (this.status) {
        	  case 0:
        		// server not running, do nothing.
        		break;
        	  case 409:
          	    alert("The section has changed since you " 
          	    		+ "loaded this page. Please reload the page.");
        	    break;
        	  default:
        	    alert("Error " + this.status + ". Please reload the page.");
        	    break;
        	}                	
                    }
                }
            }
        	KNOWWE.core.util.updateProcessingState(1);
            new _KA( options ).send();         
        }
        
TestCasePlayer.change = function(key_sessionid, selectedvalue) {
 			var topic = KNOWWE.helper.gup('page');
			document.cookie = key_sessionid +"=" + TestCasePlayer.encodeCookieValue(selectedvalue);
           	KNOWWE.helper.observer.notify('update');
}

TestCasePlayer.addCookie = function(cookievalue) {
			var topic = KNOWWE.helper.gup('page');
			document.cookie = "additionalQuestions"+ TestCasePlayer.encodeCookieValue(topic) +"=" + TestCasePlayer.encodeCookieValue(cookievalue);
           	KNOWWE.helper.observer.notify('update');
}

TestCasePlayer.encodeCookieValue = function(cookievalue) {
			var temp = escape(cookievalue);
			temp = temp.replace('@', '%40');
			temp = temp.replace('+', '%2B');
			return temp;
}

jq$(document).ready(function() {
	TestCasePlayer.init();
});