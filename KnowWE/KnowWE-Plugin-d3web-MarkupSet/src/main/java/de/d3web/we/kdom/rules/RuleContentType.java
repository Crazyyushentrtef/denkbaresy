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

package de.d3web.we.kdom.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.manage.RuleFactory;
import de.d3web.core.session.Session;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.CondKnown;
import de.d3web.we.kdom.condition.CondKnownUnknown;
import de.d3web.we.kdom.condition.CondUnknown;
import de.d3web.we.kdom.condition.Finding;
import de.d3web.we.kdom.condition.KDOMConditionFactory;
import de.d3web.we.kdom.condition.NumericalFinding;
import de.d3web.we.kdom.condition.NumericalIntervallFinding;
import de.d3web.we.kdom.condition.SolutionStateCond;
import de.d3web.we.kdom.condition.UserRatingConditionType;
import de.d3web.we.kdom.rule.ConditionActionRule;
import de.d3web.we.kdom.rule.ConditionActionRuleContent;
import de.d3web.we.kdom.rule.ExceptionConditionArea;
import de.d3web.we.kdom.rules.action.D3webRuleAction;
import de.d3web.we.kdom.rules.action.RuleAction;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.SuccessorNotReusedConstraint;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.CommentLineType;
import de.knowwe.core.kdom.basicType.UnrecognizedSyntaxType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.ReRenderSectionMarkerRenderer;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.AllTextFinderDivCorrectTrimmed;

/**
 * @author Jochen
 * 
 *         A type for the content of the RuleMarkup-block. It allocates all the
 *         text tries to create Rules from the content
 * 
 */
public class RuleContentType extends AbstractType {

	public static final String ruleStoreKey = "RULE_STORE_KEY";

	/**
	 * Here the type is configured. It takes (mostly) all the text it gets. A
	 * ConditionActionRule-type is initialized and inserted as child-type (which
	 * itself gets a child-type: ConditionActionRuleContent).
	 * 
	 */
	public RuleContentType() {
		// take nearly all the text that is passed (kind of trimmed)
		this.sectionFinder = new AllTextFinderDivCorrectTrimmed();

		// configure the rule
		ConditionActionRule rule = new ConditionActionRule();
		RuleAction ruleAction = new RuleAction();
		ConditionActionRuleContent ruleContent = new ConditionActionRuleContent(
				ruleAction);

		// add handler to create the rules in the d3web knowledge base
		ruleAction.addSubtreeHandler(Priority.LOW, new RuleCompiler());

		ruleContent.setRenderer(new ReRenderSectionMarkerRenderer(
				new RuleHighlightingRenderer()));
		List<Type> termConds = getTerminalConditions();
		ruleContent.setTerminalConditions(termConds);

		// register the configured rule-content-type as child
		rule.addChildType(ruleContent);

		// register the configured rule-type as child
		this.childrenTypes.add(new CommentLineType());
		this.addChildType(rule);
		this.addChildType(new UnrecognizedSyntaxType());
	}

	public static List<Type> getTerminalConditions() {
		List<Type> termConds = new ArrayList<Type>();

		// add all the various allowed TerminalConditions here
		termConds.add(new SolutionStateCond());
		termConds.add(new UserRatingConditionType());
		termConds.add(new CondKnownUnknown());
		termConds.add(new Finding());
		termConds.add(new CondUnknown());
		termConds.add(new CondKnown());
		termConds.add(new NumericalFinding());
		termConds.add(new NumericalIntervallFinding());
		return termConds;
	}

	/**
	 * @author Jochen
	 * 
	 *         This handler compiles a parsed rule into the d3web knowledge base
	 *         (if it doesn't have errors)
	 * 
	 */
	class RuleCompiler extends D3webSubtreeHandler<RuleAction> {

		public RuleCompiler() {
			this
					.registerConstraintModule(new SuccessorNotReusedConstraint<RuleAction>());
		}

		@SuppressWarnings("unchecked")
		@Override
		public Collection<Message> create(Article article,
				Section<RuleAction> actionS) {

			Section<ConditionActionRuleContent> rule = Sections
					.findAncestorOfType(actionS,
							ConditionActionRuleContent.class);

			if (rule.hasErrorInSubtree(article)) {
				return Messages
						.asList(Messages.creationFailedWarning(
								Rule.class.getSimpleName()));
			}

			// create condition
			Section<CompositeCondition> cond = Sections.findSuccessor(rule,
					CompositeCondition.class);
			Condition d3Cond = KDOMConditionFactory.createCondition(article,
					cond);

			// create action
			@SuppressWarnings("rawtypes")
			Section<D3webRuleAction> action = Sections.findSuccessor(actionS,
					D3webRuleAction.class);
			if (action == null) {
				return Messages
						.asList(Messages.creationFailedWarning(
								D3webUtils.getD3webBundle().getString(
										"KnowWE.rulesNew.notcreated")
										+ " : no valid action found"));
			}
			PSAction d3action = action.get().getAction(article, action);

			// create exception (if exists)
			Section<ExceptionConditionArea> exceptionCondSec = Sections
					.findSuccessor(rule, ExceptionConditionArea.class);
			Condition exceptionCond = null;
			if (exceptionCondSec != null) {
				Section<CompositeCondition> exceptionCompCondSec = Sections
						.findSuccessor(exceptionCondSec,
								CompositeCondition.class);
				if (exceptionCompCondSec != null) {
					exceptionCond = KDOMConditionFactory.createCondition(
							article, exceptionCompCondSec);
				}
			}

			// create actual rule
			if (d3action != null && d3Cond != null) {
				Rule r = RuleFactory.createRule(d3action, d3Cond,
						exceptionCond, action.get().getActionPSContext());
				if (r != null) {
					KnowWEUtils.storeObject(article, actionS, ruleStoreKey, r);
					return Messages.asList(Messages.objectCreatedNotice(
							"Rule"));
				}

			}

			// should not happen
			return Messages.asList(Messages.creationFailedWarning(
					D3webUtils.getD3webBundle().getString(
							"KnowWE.rulesNew.notcreated")));
		}

		@Override
		public void destroy(Article article, Section<RuleAction> rule) {
			Rule kbr = (Rule) rule.getSectionStore().getObject(article,
					ruleStoreKey);
			if (kbr != null) {
				kbr.remove();
			}
		}

	}

	/**
	 * @author Johannes Dienst
	 * 
	 *         Highlights Rules according to state.
	 * 
	 */
	class RuleHighlightingRenderer implements
			Renderer {

		@Override
		public void render(Section<?> sec,
				UserContext user, RenderResult string) {
			Article article = KnowWEUtils.getCompilingArticles(sec).iterator().next();
			Section<RuleAction> ruleAction = Sections.findSuccessor(sec,
					RuleAction.class);
			Rule rule = null;
			if (ruleAction != null) {
				rule = (Rule) KnowWEUtils.getStoredObject(article, ruleAction,
						RuleContentType.ruleStoreKey);
			}

			string.appendHtml("<span id='" + sec.getID()
					+ "'>");

			KnowledgeBase kb = D3webUtils.getKnowledgeBase(user.getWeb(), article.getTitle());
			Session session = SessionProvider.getSession(user, kb);

			highlightRule(article, sec, rule, session, user, string);
			string.appendHtml("</span>");
		}

		private static final String highlightMarker = "HIGHLIGHT_MARKER";

		/**
		 * Stores the Renderer used in <b>highlightRule<b>
		 */
		StyleRenderer firedRenderer = StyleRenderer.getRenderer(
				highlightMarker, "", StyleRenderer.CONDITION_FULLFILLED);

		StyleRenderer exceptionRenderer = StyleRenderer.getRenderer(
				highlightMarker, "", null);

		/**
		 * Renders the Rule with highlighting.
		 * 
		 * @param sec
		 * @param rc
		 * @param session
		 * @return
		 */
		private void highlightRule(Article article,
				Section<?> sec, Rule r,
				Session session, UserContext user, RenderResult string) {

			RenderResult newContent = new RenderResult(string);
			if (r == null || session == null) {
				DelegateRenderer.getInstance().render(sec, user, newContent);
			}
			else {
				try {
					if (r.hasFired(session)) {
						this.firedRenderer.render(sec, user, newContent);
					}
					else {
						DelegateRenderer.getInstance().render(sec, user,
								newContent);
					}
				}
				catch (Exception e) {
					this.exceptionRenderer.render(sec, user, newContent);
				}
			}
			string.append(newContent.toStringRaw());
		}

	}
}
