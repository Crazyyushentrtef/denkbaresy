/*
 * Copyright (C) 2013 denkbares GmbH
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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.diaflux.type.ExitType.ExitNodeDef;
import de.knowwe.diaflux.type.StartType.StartNodeDef;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 16.10.2013
 */
public class FlowchartGoToToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {

		ArrayList<Tool> tools = new ArrayList<Tool>();
		Article compilingArticle = KnowWEUtils.getCompilingArticles(section).iterator().next();
		TerminologyManager terminologyManager = KnowWEUtils.getTerminologyManager(compilingArticle);
		if (section.get() instanceof StartNodeDef || section.get() instanceof ExitNodeDef) {
			Collection<Section<?>> termRefSections = terminologyManager.getTermReferenceSections(KnowWEUtils.getTermIdentifier(section));
			for (Section<?> termSection : termRefSections) {
				createGoToTool(termSection, tools);
			}

		}
		if (section.get() instanceof FlowchartReference) {
			Section<?> termDefSection = terminologyManager.getTermDefiningSection(KnowWEUtils.getTermIdentifier(section));
			createGoToTool(termDefSection, tools);
		}
		return tools.toArray(new Tool[tools.size()]);
	}

	private void createGoToTool(Section<?> termSection, ArrayList<Tool> tools) {
		Section<FlowchartType> flowchartSection = Sections.findAncestorOfType(
				termSection, FlowchartType.class);
		String link = KnowWEUtils.getURLLink(flowchartSection);
		String linkType = termSection.get() instanceof TermDefinition ? "subflow" : "parent flow";
		String title = linkType + " '"
				+ FlowchartType.getFlowchartName(flowchartSection) + "'";
		String description = "Go to " + title;
		tools.add(new DefaultTool("KnowWEExtension/testcaseplayer/icon/testcaselink.png",
				StringUtils.capitalize(title), description, "window.location = '" + link + "'"));
	}
}
