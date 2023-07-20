/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.action;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.d3web.we.action.AbstractKnowWEAction;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sectionizer;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.decisionTree.SolutionID;
import de.d3web.we.kdom.rules.Rule;
import de.d3web.we.kdom.rules.RulesSectionContent;
import de.d3web.we.refactoring.RefactoringTagHandler;

/**
 * @author Franz Schwab
 */
public class RefactoringAction extends AbstractKnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String id = parameterMap.get("KnowledgeElement");
		String topic = parameterMap.getTopic();
		String web = parameterMap.getWeb();
		KnowWEArticleManager articleManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		KnowWEArticle article = articleManager.getArticle(topic);
		Section articleSection = article.getSection();

		// Section mit der id holen
		Section knowledgeSection = articleSection.findChild(id);
		
		// Alle Finding 's dieser Section holen
		List<Section> findingSections = new ArrayList<Section>();
		knowledgeSection.findSuccessorsOfType(Finding.class, findingSections); 
		
		// SolutionID holen
		Section solutionID = knowledgeSection.findSuccessor(SolutionID.class);
		
		// Regelstring bauen
		StringBuilder sb = new StringBuilder("\nIF ");
		for(Iterator<Section> iter = findingSections.iterator(); iter.hasNext(); ) {
			Section sec = iter.next();
			sb.append(sec.getOriginalText() + " ");
			if (iter.hasNext())
				sb.append("\nAND ");
		}
		sb.append("\nTHEN ");
		sb.append(solutionID.getOriginalText() + " = P7\n");
		
		//Lösche entsprechende XCList
		articleManager.replaceKDOMNodeWithoutSave(parameterMap, topic, knowledgeSection.getId(), "\n");
		
		//Füge Regel ein und speichere Artikel
		List<Section> rulesSectionContentSections = new ArrayList<Section>();
		articleSection.findSuccessorsOfType(RulesSectionContent.class, rulesSectionContentSections);
		Section rulesSectionContent = rulesSectionContentSections.get(0);
		articleManager.replaceKDOMNode(parameterMap, topic, rulesSectionContent.getId(), rulesSectionContent.getOriginalText() + sb.toString());
		
		return "";
	}

}
