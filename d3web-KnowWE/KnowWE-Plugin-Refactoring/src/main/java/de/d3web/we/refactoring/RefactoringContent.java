package de.d3web.we.refactoring;

import de.d3web.we.kdom.rendering.EditSectionRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.refactoring.renderer.GroovyDisplayRenderer;

public class RefactoringContent extends XMLContent {
	
	KnowWEDomRenderer defaultRenderer;
	
	public RefactoringContent() {
		defaultRenderer = new EditSectionRenderer(new GroovyDisplayRenderer());
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see de.d3web.we.kdom.AbstractKnowWEObjectType#getDefaultRenderer()
	 */
	@Override
	protected KnowWEDomRenderer getDefaultRenderer() {
		// TODO Auto-generated method stub
		return defaultRenderer;
	}
}
