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

package de.d3web.we.kdom.questionTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.CondNumEqual;
import de.d3web.core.inference.condition.CondNumGreater;
import de.d3web.core.inference.condition.CondNumGreaterEqual;
import de.d3web.core.inference.condition.CondNumIn;
import de.d3web.core.inference.condition.CondNumLess;
import de.d3web.core.inference.condition.CondNumLessEqual;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.we.object.QASetDefinition;
import de.d3web.we.object.QuestionDefinition;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.objects.KnowWETerm;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.objects.TermReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.dashtree.DashSubtree;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeUtils;

public class QuestionDashTreeUtils {

	/**
	 * Creates a condition from a List of DashTreeElements. The List is supposed
	 * to start with a Section containing a QuestionTreeAnswerID, followed by a
	 * Section containing a QuestionID. After that it starts again with a
	 * QuestionTreeAnswerID and so forth. If thats not the case, <tt>null</tt>
	 * will be returned. Such a List of DashTreeElements can be created with
	 * <tt>DashTreeElement.getDashTreeAncestors(Section s)</tt>, if <tt>s</tt>
	 * is the child Section of an answer in a valid DashTree.
	 * 
	 * @param article TODO
	 */
	public static Condition createCondition(
			KnowWEArticle article, List<Section<? extends DashTreeElement>> ancestors) {

		List<Condition> simpleConds = new ArrayList<Condition>();

		for (int i = 0; i + 2 <= ancestors.size(); i += 2) {
			Condition simpleCond = createSimpleCondition(article, ancestors
					.get(i), ancestors.get(i + 1));
			if (simpleCond != null) {
				simpleConds.add(simpleCond);
			}
		}

		if (simpleConds.isEmpty()) {
			return null;
		}
		else if (simpleConds.size() == 1) {
			return simpleConds.get(0);
		}
		else {
			return new CondAnd(simpleConds);
		}

	}

	/**
	 * Creates a condition from the two DashTreeElements father an grandfather.
	 * Father is supposed to contain a QuestionTreeAnswerID, the grandfather a
	 * QuestionID. If thats not the case, <tt>null</tt> will be returned.
	 */
	public static Condition createSimpleCondition(
			KnowWEArticle article,
			Section<? extends DashTreeElement> father,
			Section<? extends DashTreeElement> grandFather) {

		if (father.hasErrorInSubtree(article) || grandFather.hasErrorInSubtree(article)) {
			return null;
		}

		Section<QuestionTreeAnswerDefinition> answerSec =
					Sections.findSuccessor(father, QuestionTreeAnswerDefinition.class);
		Section<QuestionDefinition> qSec = Sections.findSuccessor(grandFather,
				QuestionDefinition.class);

		if (qSec == null) {
			return null;
		}

		Question q = qSec.get().getTermObject(article, qSec);

		if (answerSec != null && q instanceof QuestionChoice) {
			Choice a = answerSec.get().getTermObject(article, answerSec);
			if (a != null) {
				CondEqual c = new CondEqual(q, new ChoiceValue(
						a));
				return c;
			}
		}

		Section<NumericCondLine> numCondSec =
					Sections.findSuccessor(father, NumericCondLine.class);

		if (numCondSec != null && q instanceof QuestionNum) {
			if (NumericCondLine.isIntervall(numCondSec)) {
				NumericalInterval ival = NumericCondLine.getNumericalInterval(numCondSec);
				if (ival != null) return new CondNumIn((QuestionNum) q, ival);
			}
			else {
				Double d = NumericCondLine.getValue(numCondSec);
				if (d == null) return null;
				String comp = NumericCondLine.getComparator(numCondSec);

				if (d != null && comp != null) return createCondNum(father.getArticle(),
						numCondSec, comp, d,
								(QuestionNum) q);
				;
			}
		}

		return null;

	}

	private static Condition createCondNum(KnowWEArticle article,
			Section<NumericCondLine> comp, String comparator, Double valueOf,
			QuestionNum questionNum) {
		Messages.clearMessages(article, comp, QuestionDashTreeUtils.class);

		if (comparator.equals("=")) return new CondNumEqual(questionNum, valueOf);
		else if (comparator.equals(">")) return new CondNumGreater(questionNum, valueOf);
		else if (comparator.equals(">=")) return new CondNumGreaterEqual(questionNum,
				valueOf);
		else if (comparator.equals("<")) return new CondNumLess(questionNum, valueOf);
		else if (comparator.equals("<=")) return new CondNumLessEqual(questionNum,
				valueOf);
		else {
			Messages.storeMessage(article, comp,
					QuestionDashTreeUtils.class,
					Messages.error("Unkown comparator '" + comparator + "'."));
			return null;
		}
	}

	/**
	 * Checks if the Subtree of the root Question has changed. Ignores
	 * TermReferences!
	 */
	@SuppressWarnings({
			"unchecked", "rawtypes" })
	public static Section<DashSubtree> getRootQuestionSubtree(KnowWEArticle article, Section<?> s) {

		Section<DashSubtree> rootQuestionSubtree = null;

		Section<DashTreeElement> thisElement = Sections.findAncestorOfType(s,
				DashTreeElement.class);
		if (s.get() instanceof QuestionDefinition
				&& DashTreeUtils.getDashLevel(thisElement) == 0) {
			rootQuestionSubtree = Sections.findAncestorOfType(thisElement,
					DashSubtree.class);
		}

		if (rootQuestionSubtree == null) {
			Section<DashSubtree> lvl1SubtreeAncestor = DashTreeUtils.getAncestorDashSubtree(
					s, 1);
			if (lvl1SubtreeAncestor != null) {
				Section<DashTreeElement> lvl1Element = Sections.findChildOfType(
						lvl1SubtreeAncestor, DashTreeElement.class);
				Section<? extends KnowWETerm> termRefSection = Sections.findSuccessor(
						lvl1Element,
						KnowWETerm.class);

				if (termRefSection.get() instanceof QASetDefinition) {
					rootQuestionSubtree = lvl1SubtreeAncestor;
				}
				else {
					rootQuestionSubtree = (Section<DashSubtree>) lvl1SubtreeAncestor.getFather();
				}
			}
		}

		return rootQuestionSubtree;
	}

	/**
	 * Checks if the Subtree of the root Question has changed. Ignores
	 * TermReferences!
	 */
	public static boolean isChangeInRootQuestionSubtree(KnowWEArticle article, Section<?> s) {

		Section<DashSubtree> rootQuestionSubtree = getRootQuestionSubtree(article, s);

		if (rootQuestionSubtree != null) {
			return isChangeInQuestionSubtree(article, rootQuestionSubtree);
		}
		return false;
	}

	public static boolean isChangeInQuestionSubtree(KnowWEArticle article, Section<DashSubtree> s) {

		List<Class<? extends Type>> filteredTypes =
				new ArrayList<Class<? extends Type>>(1);
		filteredTypes.add(TermReference.class);

		HashSet<Section<DashSubtree>> visited = new HashSet<Section<DashSubtree>>();
		visited.add(s);
		boolean change = isChangeInQuestionSubtree(article, s, filteredTypes, visited);
		return change;
	}

	@SuppressWarnings({
			"unchecked", "rawtypes" })
	private static boolean isChangeInQuestionSubtree(KnowWEArticle article, Section<DashSubtree> s, List<Class<? extends Type>> filteredTypes, HashSet<Section<DashSubtree>> visited) {
		List<Section<?>> nodes = new LinkedList<Section<?>>();
		Sections.getAllNodesPostOrder(s, nodes);
		for (Section<?> node : nodes) {
			if (node.get() instanceof TermDefinition) {
				Section<TermDefinition> tdef = (Section<TermDefinition>) node;
				Collection<Section<? extends TermDefinition>> termDefs = new ArrayList<Section<? extends TermDefinition>>();
				termDefs.addAll(KnowWEUtils.getTerminologyHandler(article.getWeb()).getRedundantTermDefiningSections(
								article, tdef.get().getTermIdentifier(tdef),
						tdef.get().getTermScope()));
				termDefs.add(KnowWEUtils.getTerminologyHandler(
						article.getWeb()).getTermDefiningSection(article,
						tdef.get().getTermIdentifier(tdef), tdef.get().getTermScope()));
				for (Section<?> tDef : termDefs) {
					if (tDef != null && tDef != node) {
						Section<DashSubtree> dashSubtree = getRootQuestionSubtree(
								article, tDef);
						if (dashSubtree != null && !visited.contains(dashSubtree)) {
							visited.add(dashSubtree);
							if (isChangeInQuestionSubtree(article, dashSubtree,
									filteredTypes,
									visited)) {
								return true;
							}
						}
					}
				}
			}
			if (node.isChanged(article.getTitle(), filteredTypes)) {
				return true;
			}
		}
		return false;
	}

}
