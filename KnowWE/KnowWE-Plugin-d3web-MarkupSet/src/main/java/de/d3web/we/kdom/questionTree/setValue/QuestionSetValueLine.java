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

package de.d3web.we.kdom.questionTree.setValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.abstraction.ActionSetValue;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.manage.RuleFactory;
import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.questionTree.QuestionDashTreeUtils;
import de.d3web.we.kdom.questionTree.RootQuestionChangeConstraint;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.CreateRelationFailed;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.sectionFinder.ConditionalSectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.SplitUtility;
import de.knowwe.core.dashtree.DashTreeUtils;

public class QuestionSetValueLine extends AbstractType {

	private static final String SETVALUE_ARGUMENT = "SetValueArgument";
	private static final String OPEN = "(";
	private static final String CLOSE = ")";

	/**
	 *
	 */
	@Override
	protected void init() {
		this.sectionFinder = new ConditionalSectionFinder(new AllTextSectionFinder()) {

			@Override
			protected boolean condition(String text, Section<?> father) {
				return SplitUtility.containsUnquoted(text, OPEN)
						&& SplitUtility.containsUnquoted(text, CLOSE);

			}
		};

		AnswerPart argumentType = new AnswerPart();
		this.childrenTypes.add(argumentType);
		this.childrenTypes.add(createObjectRefTypeBefore(argumentType));

	}

	private AbstractType createObjectRefTypeBefore(
			AbstractType typeAfter) {
		QuestionReference qid = new QuestionReference();
		qid.setSectionFinder(new AllBeforeTypeSectionFinder(typeAfter));
		qid.addSubtreeHandler(new CreateSetValueRuleHandler());
		return qid;
	}

	static class CreateSetValueRuleHandler extends D3webSubtreeHandler<QuestionReference> {

		public CreateSetValueRuleHandler() {
			this.registerConstraintModule(new RootQuestionChangeConstraint<QuestionReference>());
		}

		@Override
		public void destroy(KnowWEArticle article, Section<QuestionReference> s) {
			Rule kbr = (Rule) KnowWEUtils.getObjectFromLastVersion(article, s,
					SETVALUE_ARGUMENT);
			if (kbr != null) kbr.remove();
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<QuestionReference> s) {

			Question q = s.get().getTermObject(article, s);

			Section<AnswerReference> answerSec = Sections.findSuccessor(
					s.getFather(), AnswerReference.class);

			String answerName = answerSec.get().getTermName(answerSec);

			if (q != null) {
				Choice a = null;
				if (q instanceof QuestionChoice) {
					QuestionChoice qc = (QuestionChoice) q;
					List<Choice> allAlternatives = qc.getAllAlternatives();
					for (Choice answerChoice : allAlternatives) {
						if (answerChoice.getName().equals(answerName)) {
							a = answerChoice;
							break;
						}
					}

					if (a != null) {
						Condition cond = QuestionDashTreeUtils.createCondition(article,
								DashTreeUtils.getAncestorDashTreeElements(s));
						if (cond == null) {
							return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
									Rule.class.getSimpleName() + ": check condition"));
						}

						ActionSetValue ac = null;
						if (q != null && a != null) {
							ac = new ActionSetValue();
							ac.setQuestion(q);
							ac.setValue(a);
						}

						Rule r = null;
						if (ac != null) {
							r = RuleFactory.createRule(ac, cond, null,
									PSMethodAbstraction.class);
						}

						if (r != null) {
							KnowWEUtils.storeObject(article, s, SETVALUE_ARGUMENT, r);
							return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(
									r.getClass().toString()));
						}

					}
				}
			}

			return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
					Rule.class.getSimpleName()));

		}

	}

	/**
	 * 
	 * A type for an AnswerReference in brackets like '(AnswerXY)'
	 * 
	 * @author Jochen
	 * @created 26.07.2010
	 */
	class AnswerPart extends AbstractType {

		public AnswerPart() {
			this.sectionFinder = new SectionFinder() {

				@Override
				public List<SectionFinderResult> lookForSections(String text,
						Section<?> father, Type type) {

					return SectionFinderResult
							.createSingleItemList(new SectionFinderResult(
									SplitUtility.indexOfUnquoted(text, OPEN),
									SplitUtility.indexOfUnquoted(text, CLOSE) + 1));
				}
			};

			AnswerReferenceInsideBracket answerReferenceInsideBracket = new AnswerReferenceInsideBracket();
			answerReferenceInsideBracket.setSectionFinder(new SectionFinder() {
				@Override
				public List<SectionFinderResult> lookForSections(String text,
						Section<?> father, Type type) {

					return SectionFinderResult
							.createSingleItemList(new SectionFinderResult(
									1,
									text.length() - 1));
				}
			});
			this.addChildType(answerReferenceInsideBracket);
		}

		class AnswerReferenceInsideBracket extends AnswerReference {

			@Override
			public Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> s) {
				return Sections.findSuccessor(s.getFather().getFather(),
						QuestionReference.class);
			}

		}

	}

}
