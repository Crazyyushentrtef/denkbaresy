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
package de.d3web.we.kdom.solutionTree;

import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.constraint.ConstraintSectionFinder;
import de.d3web.we.kdom.constraint.SingleChildConstraint;
import de.d3web.we.kdom.dashTree.DashTreeElementContent;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.objects.SolutionDefinition;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.RelationCreatedMessage;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.terminology.D3webSubtreeHandler;

/**
 * @author Jochen
 *
 *         A DashTreeElementContent for the Solution-DashTree. It is injected
 *         into a dash-tree @see {@link SolutionsDashTree} It contains a
 *         SolutionDef type (which itself interally creates a solution object)
 *         and CreateSubSolutionRelationHandler which established the
 *         hierarchical relations defined by the dashtree
 *
 *
 */
public class SolutionDashTreeElementContent extends DashTreeElementContent implements KnowWETerm<Object> {

	public SolutionDashTreeElementContent() {
		this.addSubtreeHandler(new CreateSubSolutionRelationHandler());
		SolutionDefinition solutionDef = new SolutionDefinition();
		ConstraintSectionFinder f = new ConstraintSectionFinder(new AllTextFinderTrimmed());
		f.addConstraint(SingleChildConstraint.getInstance());
		solutionDef.setSectionFinder(f);
		this.addChildType(solutionDef);
	}

	/**
	 * @author Jochen
	 *
	 *         This handler establishes sub-solution-relations defined by the
	 *         solutionDashTree in the knowledge base i.e., if a solution is a
	 *         dashTree-child of another solution we add it as child in the
	 *         knowledge base
	 *
	 */
	class CreateSubSolutionRelationHandler extends D3webSubtreeHandler<SolutionDashTreeElementContent> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<SolutionDashTreeElementContent> s) {
			Section<? extends DashTreeElementContent> fatherSolutionContent = DashTreeElementContent.getDashTreeFatherContent(
					s);
			Section<SolutionDefinition> localSolutionDef = s.findSuccessor(SolutionDefinition.class);
			Solution localSolution = localSolutionDef.get().getTermObject(article, localSolutionDef);

			if (fatherSolutionContent != null && localSolution != null) {

				Section<SolutionDefinition> solutionDef = fatherSolutionContent.findSuccessor(SolutionDefinition.class);
				if (solutionDef != null) {
					Solution superSolution = solutionDef.get().getTermObject(article, solutionDef);
					// here the actual taxonomic relation is established
					superSolution.addChild(localSolution);
					return Arrays.asList((KDOMReportMessage) new RelationCreatedMessage(
							s.getClass().getSimpleName()
									+ " " + localSolution.getName() + "sub-solution of "
									+ superSolution.getName()));
				}
			}

			return null;
		}

	}

	@Override
	public String getTermName(Section<? extends KnowWETerm<Object>> s) {
		return s.getOriginalText().trim();
	}

}
