/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.d3web.we.object;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.knowwe.core.compile.terminology.TermIdentifier;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 15.02.2012
 */
public class NamedObjectRenderer implements Renderer {

	@Override
	public void render(Section<?> section, UserContext user, StringBuilder string) {
		Article article = KnowWEUtils.getCompilingArticles(section).iterator().next();
		TermIdentifier termIdentifier = KnowWEUtils.getTermIdentifier(section);
		TerminologyManager tManager = KnowWEUtils.getTerminologyManager(article);
		Renderer renderer;
		if (tManager.hasTermOfClass(termIdentifier, Question.class)) {
			renderer = new ValueTooltipRenderer(StyleRenderer.Question);
		}
		else if (tManager.hasTermOfClass(termIdentifier, QContainer.class)) {
			renderer = StyleRenderer.Questionaire;
		}
		else if (tManager.hasTermOfClass(termIdentifier, Solution.class)) {
			renderer = StyleRenderer.SOLUTION;
		}
		else if (tManager.hasTermOfClass(termIdentifier, Choice.class)) {
			renderer = StyleRenderer.CHOICE;
		}
		else if (tManager.hasTermOfClass(termIdentifier, KnowledgeBase.class)) {
			renderer = StyleRenderer.Questionaire;
		}
		else {
			renderer = PlainText.getInstance().getRenderer();
		}
		renderer.render(section, user, string);
	}
}
