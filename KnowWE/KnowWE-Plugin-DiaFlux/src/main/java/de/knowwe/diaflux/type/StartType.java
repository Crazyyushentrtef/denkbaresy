/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.knowwe.diaflux.type;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.compile.terminology.TermRegistrationScope;
import de.knowwe.core.kdom.objects.SimpleDefinition;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.xml.AbstractXMLType;
import de.knowwe.kdom.xml.XMLContent;

/**
 * 
 * 
 * @author Reinhard Hatko
 * @created on: 08.10.2009
 */
public class StartType extends AbstractXMLType {

	private static StartType instance;

	private StartType() {
		super("start");
		addChildType(new XMLContent(new StartNodeDef()));
	}

	public static StartType getInstance() {
		if (instance == null) instance = new StartType();

		return instance;
	}

	public String getTermName(Section<? extends StartType> section) {
		Section<StartNodeDef> term = Sections.findSuccessor(section, StartNodeDef.class);
		return term.get().getTermName(term);
	}

	static class StartNodeDef extends SimpleDefinition {

		public StartNodeDef() {
			super(TermRegistrationScope.LOCAL, String.class);
			setSectionFinder(AllTextSectionFinder.getInstance());
			setRenderer(StyleRenderer.FlowchartStart);
		}

		@Override
		public Identifier getTermIdentifier(Section<? extends Term> section) {
			Section<FlowchartType> flowchart = Sections.findAncestorOfType(section,
					FlowchartType.class);
			String flowchartName = FlowchartType.getFlowchartName(flowchart);
			return new Identifier(flowchartName, getTermName(section));
		}

		@Override
		public String getTermName(Section<? extends Term> s) {
			return Strings.decodeHtml(s.getText());
		}

	}

}
