package de.d3web.we.hermes.kdom.conceptMining;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ConceptOccurrenceRenderer extends KnowWEDomRenderer {

	@Override
	public void render(Section arg0, KnowWEUserContext arg1, StringBuilder arg2) {
		
		String conceptName = arg0.getOriginalText();
		
		arg2.append(KnowWEEnvironment.maskHTML("<b>"+arg0.getOriginalText()+"</b>"+"<span class=\"conceptLink\" " 
			+ "rel=\"{type: '"+conceptName+"', objectID: '"+conceptName+"', termName: '"+conceptName+"', user:'"+arg1.getUsername()+"'}\">"
         	+ "!" + "</span>"));

	}

}
