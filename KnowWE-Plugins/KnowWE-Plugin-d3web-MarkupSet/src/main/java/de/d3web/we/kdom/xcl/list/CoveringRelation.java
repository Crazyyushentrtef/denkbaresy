/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.kdom.xcl.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.denkbares.strings.Strings;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.NoAnswerException;
import de.d3web.core.inference.condition.UnknownAnswerException;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Session;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.KDOMConditionFactory;
import de.d3web.we.kdom.rules.RuleType;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.SolutionDefinition;
import de.d3web.we.reviseHandler.D3webHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.XCLRelationWeight;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.XCLRelation;
import de.d3web.xcl.XCLRelationType;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.ConditionalSectionFinder;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * KDOM type to define a single covering relation if a XCL relation list.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 28.11.2018
 */
class CoveringRelation extends AbstractType {

	private static final String RELATION_STORE_KEY = "xcl.relation";

	public CoveringRelation() {

		this.setSectionFinder(new ConditionalSectionFinder(new AllTextFinderTrimmed()) {

			// hack to allow for comment after last relation
			// TODO: find better way
			@Override
			protected boolean condition(String text, Section<?> father) {
				// if starts as a comment and there is no next line, there
				// is CoveringRelation in it
				return !text.trim().startsWith("//") || text.contains("\n");
			}
		});

		this.addCompileScript(Priority.LOW, new CreateXCLRelationHandler());
		this.setRenderer(new CoveringRelationRenderer());

		// here also a comment might occur:
		AnonymousType relationComment = new AnonymousType("comment");
		relationComment.setSectionFinder(new RegexSectionFinder("[\\t ]*//[^\r\n]*+" + "\\r?\\n"));
		relationComment.setRenderer(StyleRenderer.COMMENT);
		this.addChildType(relationComment);

		// take weights
		this.addChildType(new XCLWeight());

		// add composite condition and allow all recognized terminal conditions within
		CompositeCondition cond = new CompositeCondition();
		cond.setAllowedTerminalConditions(RuleType.getTerminalConditions());
		this.addChildType(cond);
	}

	/**
	 * this handler translates the parsed covering-relation-KDOM to the d3web knowledge base
	 *
	 * @author Jochen
	 */
	static class CreateXCLRelationHandler implements D3webHandler<CoveringRelation> {

		private List<Section<SolutionDefinition>> getCorrespondingSolutionDefinitions(Section<CoveringRelation> relation) {
			return $(relation).ancestor(CoveringList.class)
					.successor(XCLHeader.class)
					.successor(SolutionDefinition.class)
					.asList();
		}

		private List<XCLModel> getCorrespondingXCLModels(D3webCompiler compiler, Section<CoveringRelation> relation) {
			List<XCLModel> models = new ArrayList<>();
			for (Section<SolutionDefinition> solutionDef : getCorrespondingSolutionDefinitions(relation)) {
				// process if no solution has been created
				Solution solution = solutionDef.get().getTermObject(compiler, solutionDef);
				if (solution != null) {
					// add associated XCL model (if there is any)
					XCLModel xclModel = solution.getKnowledgeStore().getKnowledge(XCLModel.KNOWLEDGE_KIND);
					if (xclModel != null) models.add(xclModel);
				}
			}
			return models;
		}

		@Override
		public Collection<Message> create(D3webCompiler compiler, Section<CoveringRelation> section)  {

			Section<CompositeCondition> cond = Sections.successor(section, CompositeCondition.class);
			if (cond == null) {
				// no valid relation, do not revise
				return Messages.noMessage();
			}

			if (section.hasErrorInSubtree(compiler)) {
				return Messages.asList(Messages.warning(
						D3webUtils.getD3webBundle().getString("KnowWE.xcllist.relationfail")));
			}

			// prepare condition for relation
			Condition condition = KDOMConditionFactory.createCondition(compiler, cond);
			if (condition == null) {
				// no condition could be created
				return Messages.asList(Messages.warning(
						D3webUtils.getD3webBundle().getString("KnowWE.xcllist.conditionerror")));
			}

			// prepare the relation to be added and store it for later use (rendering and destroy)
			Section<XCLWeight> weight = Sections.successor(section, XCLWeight.class);
			XCLRelationType type = XCLRelationType.explains;
			double w = 1.0;
			if (weight != null) {
				// check the weight/relation type in square brackets
				type = weight.get().getXCLRealtionType(weight);
				if (type == XCLRelationType.explains) {
					w = weight.get().getXCLRealtionWeight(weight);
					if (w <= 0 || !Double.isFinite(w)) {
						return Messages.asList(Messages.invalidNumberWarning(weight.getText()));
					}
				}
			}
			XCLRelation relation = new XCLRelation(condition, w, type);
			KnowWEUtils.storeObject(compiler, section, RELATION_STORE_KEY, relation);

			// get the models to add the relation to
			List<XCLModel> models = getCorrespondingXCLModels(compiler, section);
			if (models.isEmpty()) {
				return Messages.asList(Messages.warning(
						D3webUtils.getD3webBundle().getString("KnowWE.xcllist.relationfail")));
			}

			// iterate all solutions associated with this covering list
			for (XCLModel xclModel : models) {
				// insert the relation into the current model
				xclModel.addRelation(relation);
			}
			return Messages.noMessage();
		}

		@Override
		public void destroy(D3webCompiler compiler, Section<CoveringRelation> section) {
			// do nothing if no relation has been created (and therefore not added at all)
			XCLRelation rel = (XCLRelation) section.getObject(compiler, RELATION_STORE_KEY);
			if (rel == null) return;

			// otherwise remove relation from each xcl model
			for (XCLModel xclModel : getCorrespondingXCLModels(compiler, section)) {
				xclModel.removeRelation(rel);
			}
		}
	}

	/**
	 * Highlights XCLRelations. If answer matches: green; answer does not matches: red; answer is unknown/undefined: no
	 * highlighting
	 *
	 * @author Johannes Dienst
	 */
	private static class CoveringRelationRenderer implements Renderer {

		@Override
		public void render(Section<?> sec, UserContext user, RenderResult string) {

			// get the relation, but use plain rendering if there is no compiler configured, or no relation created
			D3webCompiler compiler = Compilers.getCompiler(sec, D3webCompiler.class);
			XCLRelation relation = (XCLRelation) KnowWEUtils.getStoredObject(compiler, sec, RELATION_STORE_KEY);
			if (relation == null) {
				DelegateRenderer.getInstance().render(sec, user, string);
				return;
			}

			// eval relation condition and highlight if evaluated to true or false
			Boolean eval = evalRelation(user, compiler, relation);
			boolean highlight = (eval != null);
			boolean fulfilled = highlight && eval;

			// wrapper for highlighting
			string.appendHtml("<span id='" + sec.getID() + "' class='XCLRelationInList'>");
			// delegate to children
			this.renderRelation(sec, user, highlight, fulfilled, string);
			// close the wrapper
			string.appendHtml("</span>");
		}

		private Boolean evalRelation(UserContext user, D3webCompiler compiler, XCLRelation relation) {
			// check if there is a session available
			if (compiler == null) return null;
			KnowledgeBase kb = D3webUtils.getKnowledgeBase(compiler);
			Session session = SessionProvider.getSession(user, kb);
			if (session == null) return null;

			// eval the relation to find the right rendering
			try {
				return relation.eval(session);
			}
			catch (NoAnswerException | UnknownAnswerException e) {
				return null;
			}
		}

		/***
		 * Replaces the SpecialDelegateRenderer functionality to enable
		 * highlighting of Relations without their RelationWeights.
		 */
		private void renderRelation(Section<?> relationSection,
									UserContext user, boolean highlight, boolean fulfilled, RenderResult string) {

			// find color to render the relation
			String color = (!highlight) ? null :
					fulfilled ? StyleRenderer.CONDITION_FULLFILLED : StyleRenderer.CONDITION_FALSE;

			// TODO: simplify by directly render into the render-result string
			// render the sub-sections
			StringBuilder buffi = new StringBuilder();
			List<Section<?>> children = relationSection.getChildren();
			for (Section<?> s : children) {
				buffi.append(this.renderRelationChild(s, user, color));
			}

			// and append buffer content as html
			string.appendHtml(buffi.toString());
		}

		/**
		 * Renders the children of a CoveringRelation.
		 */
		private String renderRelationChild(Section<?> sec, UserContext user, String color) {

			RenderResult buffi = new RenderResult(user);
			Type type = sec.get();

			if (type instanceof XCLRelationWeight) {
				// renders contradiction in red if fulfilled
				if (Strings.nonBlank(color) && sec.getText().trim().equals("[--]")) {
					StyleRenderer.OPERATOR.render(sec, user, buffi);
				}
				else {
					type.getRenderer().render(sec, user, buffi);
				}
			}
			else if (type instanceof CompositeCondition) {
				RenderResult temp = new RenderResult(buffi);
				// we do not want masked JPSWiki markup
				StyleRenderer.getRenderer(null, color).render(sec, user, temp);
				buffi.append(KnowWEUtils.unmaskJSPWikiMarkup(temp.toStringRaw()));
			}
			else {
				type.getRenderer().render(sec, user, buffi);
			}

			return buffi.toString();
		}
	}
}
