/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.taghandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.ArticleComparator;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.KDOMPositionComparator;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.preview.PreviewManager;
import de.knowwe.core.preview.PreviewRenderer;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.search.Result;
import de.knowwe.kdom.search.SearchEngine;
import de.knowwe.kdom.search.SearchOption;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolUtils;

/**
 * ObjectInfo TagHandler
 * 
 * This TagHandler gathers information about a specified Object. The TagHanlder
 * shows the article in which the object is defined and all articles with
 * references to this object.
 * 
 * Additionally there is a possibility to rename this object in all articles and
 * to create a wiki page for this object.
 * 
 * @author Sebastian Furth
 * @created 01.12.2010
 */
public class ObjectInfoTagHandler extends AbstractTagHandler {

	// Parameter used in the request
	public static final String OBJECT_NAME = "objectname";
	public static final String TERM_IDENTIFIER = "termIdentifier";
	private static final String HIDE_DEF = "hideDefinition";
	private static final String HIDE_REFS = "hideReferences";
	private static final String HIDE_PLAIN = "hidePlainTextOccurrences";
	private static final String HIDE_RENAME = "hideRename";
	private static final String RENAMED_ARTICLES = "renamedArticles";

	// internal counter used to create unique IDs
	private int panelCounter = 0;
	private int sectionCounter = 0;
	private int lastResultLength = 0;

	private static DefaultMarkupRenderer defaultMarkupRenderer = new DefaultMarkupRenderer();

	// KnowWE-ResourceBundle
	private ResourceBundle rb;

	public ObjectInfoTagHandler(String tag) {
		super(tag, true);
	}

	public ObjectInfoTagHandler() {
		this("ObjectInfo");
	}

	@Override
	public String getExampleString() {
		StringBuilder example = new StringBuilder();
		example.append("[{KnowWEPlugin objectInfo [");
		example.append(", objectname=\u00ABname of object\u00BB ");
		example.append(", termIdentifier=\u00ABexternal term identifier form of object\u00BB ");
		example.append(", hideDefinition=\u00ABtrue|false\u00BB ");
		example.append(", hideReferences=\u00ABtrue|false\u00BB ");
		example.append(", hidePlainTextOccurrences=\u00ABtrue|false\u00BB ");
		example.append(", hideRename=\u00ABtrue|false\u00BB] ");
		example.append("}])\n ");
		example.append("The parameters in [ ] are optional.");
		return example.toString();
	}

	@Override
	public final synchronized void render(Section<?> section, UserContext userContext,
			Map<String, String> parameters, RenderResult result) {
		// prepare rendering (single threaded, see synchronized above)
		panelCounter = 0;
		sectionCounter = 0;
		lastResultLength = 0;

		rb = Messages.getMessageBundle(userContext);

		RenderResult content = new RenderResult(userContext);
		renderContent(section, userContext, parameters, content);

		Section<TagHandlerTypeContent> tagNameSection = Sections.findSuccessor(
				section, TagHandlerTypeContent.class);
		String sectionID = section.getID();
		Tool[] tools = ToolUtils.getTools(tagNameSection, userContext);

		// RenderResult jspMasked = new RenderResult(result);
		String cssClassName = "type_" + section.get().getName();
		defaultMarkupRenderer.renderDefaultMarkupStyled(getTagName(), content.toStringRaw(),
				sectionID, cssClassName, tools, userContext, result);
		// result.appendJSPWikiMarkup(jspMasked);
	}

	private void renderContent(Section<?> section, UserContext user,
			Map<String, String> parameters, RenderResult result) {

		renderLookUpForm(section, result);
		Map<String, String> urlParameters = user.getParameters();

		// First try the URL-Parameter, if null try the TagHandler-Parameter.
		String objectName = null;
		if (urlParameters.get(OBJECT_NAME) != null) {
			objectName = Strings.decodeURL(urlParameters.get(OBJECT_NAME));
		}
		else if (parameters.get(OBJECT_NAME) != null) {
			objectName = Strings.decodeURL(parameters.get(OBJECT_NAME));
		}
		// If name is not defined stop rendering contents
		if (Strings.isBlank(objectName)) return;

		String externalTermIdentifierForm = null;
		if (urlParameters.get(TERM_IDENTIFIER) != null) {
			externalTermIdentifierForm = Strings.decodeURL(urlParameters
					.get(TERM_IDENTIFIER));
		}
		else if (parameters.get(TERM_IDENTIFIER) != null) {
			externalTermIdentifierForm = Strings.decodeURL(parameters
					.get(TERM_IDENTIFIER));
		}
		if (externalTermIdentifierForm == null) externalTermIdentifierForm = objectName;
		Identifier termIdentifier = Identifier.fromExternalForm(externalTermIdentifierForm);

		// Get TermDefinitions and TermReferences
		Set<Section<?>> definitions = new LinkedHashSet<Section<?>>();
		Set<Section<?>> references = new LinkedHashSet<Section<?>>();

		Iterator<Article> iter = Environment.getInstance()
				.getArticleManager(section.getWeb()).getArticleIterator();
		while (iter.hasNext()) {
			Article currentArticle = iter.next();
			// Get global and local term definitions
			getTermDefinitions(currentArticle, termIdentifier, definitions);
			getTermReferences(currentArticle, termIdentifier, references);
		}
		// Get global and local term references
		getTermDefinitions(null, termIdentifier, definitions);
		getTermReferences(null, termIdentifier, references);

		// Render
		renderHeader(externalTermIdentifierForm,
				getTermObjectClass(definitions, references), result);
		renderRenamingForm(externalTermIdentifierForm, objectName,
				section.getWeb(), parameters, urlParameters, result);
		renderObjectInfo(definitions, references, parameters, user, result);
		renderPlainTextOccurrences(objectName, section.getWeb(), parameters, result);
	}

	private String getTermObjectClass(Set<Section<?>> definitions,
			Set<Section<?>> references) {
		String termObjectClassString = "Object";
		Section<?> termSection = null;
		if (!definitions.isEmpty()) {
			termSection = definitions.iterator().next();
		}
		else if (!references.isEmpty()) {
			termSection = references.iterator().next();
		}
		if (termSection != null && termSection.get() instanceof Term) {
			Section<Term> simpleTermSection = Sections.cast(termSection,
					Term.class);
			Class<?> termObjectClass = simpleTermSection.get()
					.getTermObjectClass(simpleTermSection);
			termObjectClassString = termObjectClass.getSimpleName();
		}
		return termObjectClassString;
	}

	private void renderHeader(String objectName, String termClassString, RenderResult result) {
		result.appendHtml("<h3><span id=\"objectinfo-src\">");
		result.append(objectName);
		result.appendHtml("</span>");
		// Render type of (first) TermDefinition
		result.appendHtml(" <em>(");
		result.append(termClassString);
		result.appendHtml(")</em>");
		result.appendHtml("</h3>\n");
	}

	private void renderLookUpForm(Section<?> section, RenderResult result) {
		renderSectionStart(rb.getString("KnowWE.ObjectInfoTagHandler.lookUp"), result);
		result.appendHtml("<form action=\"\" method=\"get\" class=\"ui-widget\" >")
				.appendHtml("<input type=\"hidden\" id=\"objectinfo-web\" value=\"")
				.append(section.getWeb())
				.appendHtmlTag("\" />");
		result.appendHtml("<input type=\"hidden\" name=\"page\" value=\"")
				.append(Strings.encodeURL(section.getTitle()))
				.appendHtml("\" />");
		result.appendHtml("<div style=\"display:none\" id=\"objectinfo-terms\" name=\"terms\" >");
		result.appendJSPWikiMarkup(getTerms(section.getWeb()).toString());
		result.appendHtml("</div>");
		result.appendHtml("<input type=\"text\" size=\"60\" name=\"")
				.append(OBJECT_NAME)
				.appendHtml("\" id=\"objectinfo-search\" />&nbsp;");
		result.appendHtml("<input type=\"submit\" value=\"go to\" />");
		result.appendHtml("</form>");
		renderSectionEnd(result);
	}

	private void renderRenamingForm(String externalTermIdentifierForm,
			String objectName, String web, Map<String, String> parameters,
			Map<String, String> urlParameters, RenderResult result) {

		// Check if rendering is suppressed
		if (checkParameter(HIDE_RENAME, parameters)) return;
		renderSectionStart(rb.getString("KnowWE.ObjectInfoTagHandler.renameTo"), result);

		String escapedExternalTermIdentifierForm = Strings.encodeHtml(externalTermIdentifierForm);
		String escapedObjectName = Strings.encodeHtml(objectName);

		result.appendHtml("<input type=\"hidden\" id=\"objectinfo-target\" value=\""
				+ escapedExternalTermIdentifierForm + "\" />");
		result.appendHtml("<input type=\"hidden\" id=\"objectinfo-web\" value=\""
				+ web + "\" />");
		result.appendHtml("<input action=\"" + getRenamingAction()
				+ "\" type=\"text\" size=\"60\" value=\"" + escapedObjectName
				+ "\" id=\"objectinfo-replacement\" />&nbsp;");
		result.appendHtml("<input type=\"button\" id=\"objectinfo-replace-button\" value=\"rename\" />");
		result.appendHtml("&nbsp;<span id=\"objectinfo-rename-result\">");

		// render message of previous renaming if available...
		String renamingMessage = urlParameters.get(RENAMED_ARTICLES);
		if (renamingMessage != null) {
			renderRenamingMessage(result, renamingMessage);
		}
		result.appendHtml("</span>");

		renderSectionEnd(result);
	}

	private void renderRenamingMessage(RenderResult result, String renamingMessage) {
		String[] articles = renamingMessage.split("###");
		if (articles.length > 0) {

			// successfully renamed
			String[] success = articles[0].split("##");
			result.appendHtml("<p style=\"color:green;\">");
			result.append(rb.getString("KnowWE.ObjectInfoTagHandler.renamingSuccessful"));
			result.appendHtml("</p>");
			renderList(result, success);

			// failure during renaming
			if (articles.length > 1) {
				String[] failure = articles[1].split("##");
				result.appendHtml("<p style=\"color:red;\">");
				result.append(rb.getString("KnowWE.ObjectInfoTagHandler.renamingFailed"));
				result.appendHtml("</p>");
				renderList(result, failure);
			}

		}

	}

	private void renderList(RenderResult result, String[] elements) {
		result.appendHtml("<ul>");
		for (String element : elements) {
			if (!element.trim().isEmpty()) {
				result.appendHtml("<li>");
				result.append(Strings.encodeHtml(element));
				result.appendHtml("</li>");
			}
		}
		result.appendHtml("</ul>");
	}

	private void renderObjectInfo(Set<Section<?>> definitions,
			Set<Section<?>> references, Map<String, String> parameters,
			UserContext user, RenderResult result) {

		if (!checkParameter(HIDE_DEF, parameters)) {
			renderTermDefinitions(definitions, user, result);
		}
		if (!checkParameter(HIDE_REFS, parameters)) {
			renderTermReferences(references, definitions, user, result);
		}
	}

	protected void getTermDefinitions(Article currentArticle,
			Identifier termIdentifier, Set<Section<?>> definitions) {
		TerminologyManager th = KnowWEUtils
				.getTerminologyManager(currentArticle);

		Collection<Section<?>> defininingSections = th.getTermDefiningSections(termIdentifier);

		definitions.addAll(defininingSections);

	}

	protected void getTermReferences(Article currentArticle,
			Identifier termIdentifier, Set<Section<?>> references) {
		TerminologyManager th = KnowWEUtils
				.getTerminologyManager(currentArticle);
		references.addAll(th.getTermReferenceSections(termIdentifier));
	}

	private void renderTermPreview(Section<?> previewSection, Collection<Section<?>> relevantSubSections, UserContext user, String cssClass, RenderResult result) {
		int count = relevantSubSections.size();
		if (count == 0) return;

		if (count > 1) {
			result.append(" (").append(count).append(" occurences)");
		}

		result.appendHtml(": <div class='objectinfo preview ").append(cssClass).appendHtml("'>");
		result.appendHtml("<div class='objectinfo type_")
				.append(previewSection.get().getName()).appendHtml("'>");
		PreviewManager previewManager = PreviewManager.getInstance();
		PreviewRenderer renderer = previewManager.getPreviewRenderer(previewSection);
		renderer.render(previewSection, relevantSubSections, user, result);
		result.appendHtml("</div>");
		result.appendHtml("</div>");
	}

	private void renderTermDefinitions(Set<Section<?>> definitions, UserContext user, RenderResult result) {
		renderSectionStart(rb.getString("KnowWE.ObjectInfoTagHandler.definition"), result);
		if (definitions.size() > 0) {
			result.appendHtml("<p>");
			if (definitions.size() > 1) result.appendHtml("<ul>");

			Map<Section<?>, Collection<Section<?>>> groupedByPreview =
					groupByPreview(definitions);
			for (Entry<Section<?>, Collection<Section<?>>> entry : groupedByPreview.entrySet()) {
				Section<?> previewSection = entry.getKey();
				Collection<Section<?>> group = entry.getValue();

				if (definitions.size() > 1) result.appendHtml("<li>");
				result.appendHtml("<div>");
				result.appendHtml("<strong>");
				result.append("Article '[").append(previewSection.getTitle()).append("]' ");
				result.appendHtml("</strong>");
				result.append("(");
				renderLinkToSection(previewSection, result);
				result.append(")");
				renderTermPreview(previewSection, group, user, "definition", result);
				result.appendHtml("</div>");
				if (definitions.size() > 1) result.appendHtml("</li>");
			}

			if (definitions.size() > 1) result.appendHtml("</ul>");
			result.appendHtml("</p>");
		}
		renderSectionEnd(result);
	}

	private void renderTermReferences(Set<Section<?>> references,
			Set<Section<?>> definitions, UserContext user, RenderResult result) {

		renderSectionStart(rb.getString("KnowWE.ObjectInfoTagHandler.references"), result);
		if (references.size() > 0) {

			// Render a warning if there is no definition for the references
			if (definitions.size() == 0) {
				result.appendHtml("<p style=\"color:red;\">");
				result.append(rb.getString("KnowWE.ObjectInfoTagHandler.noDefinitionAvailable"));
				result.appendHtml("</p>");
			}

			Map<Article, List<Section<?>>> groupedReferences = groupByArticle(references);
			for (Article article : groupedReferences.keySet()) {
				RenderResult innerResult = new RenderResult(result);
				innerResult.appendHtml("<ul>");
				Map<Section<?>, Collection<Section<?>>> groupedByPreview =
						groupByPreview(groupedReferences.get(article));
				for (Entry<Section<?>, Collection<Section<?>>> entry : groupedByPreview.entrySet()) {
					Section<?> previewSection = entry.getKey();
					Collection<Section<?>> group = entry.getValue();

					innerResult.appendHtml("<li><div>");
					renderLinkToSection(previewSection, innerResult);
					renderTermPreview(previewSection, group, user, "reference", innerResult);
					innerResult.appendHtml("</div></li>");
				}
				innerResult.appendHtml("</ul>");
				wrapInExtendPanel("Article '" + article.getTitle() + "'", innerResult, result);
			}
		}
		renderSectionEnd(result);
	}

	/**
	 * Groups the specified sections by the ancestor section to be rendered as a
	 * preview. If a section has no ancestor to be rendered, the section itself
	 * will be used as a group with an empty collection of grouped sections.
	 * 
	 * @created 16.08.2013
	 * @param items list of sections to be grouped
	 * @return the groups of sections
	 */
	private Map<Section<?>, Collection<Section<?>>> groupByPreview(Collection<Section<?>> items) {
		List<Section<?>> list = new ArrayList<Section<?>>(items);
		Collections.sort(list, KDOMPositionComparator.getInstance());
		Map<Section<?>, Collection<Section<?>>> result = new LinkedHashMap<Section<?>, Collection<Section<?>>>();
		for (Section<?> section : list) {
			Section<?> previewSection = PreviewManager.getInstance().getPreviewAncestor(section);
			// handle if the section has no preview renderer
			if (previewSection == null) {
				result.put(section, Collections.<Section<?>> emptyList());
				continue;
			}
			// otherwise add section to preview group
			// or create group if it is new
			Collection<Section<?>> group = result.get(previewSection);
			if (group == null) {
				group = new LinkedList<Section<?>>();
				result.put(previewSection, group);
			}
			group.add(section);
		}
		return result;
	}

	private void renderLinkToSection(Section<?> reference, RenderResult result) {
		if (reference == null) return;
		// Render link to anchor (=uses div id as anchor))
		// html.append("<a href=\"Wiki.jsp?page=");
		// html.append(Strings.encodeURL(reference.getArticle()
		// .getTitle()));
		// html.append("#header_");
		// html.append(reference.getID());
		// html.append("\" >");
		result.appendHtml("<a href='");
		result.append(KnowWEUtils.getURLLink(reference));
		result.appendHtml("'>");
		// html.append(reference.getTitle());
		// html.append(" (");
		// Get a nice name
		result.append(getSurroundingMarkupName(reference));
		// html.append(")");
		result.appendHtml("</a>");
	}

	private String getSurroundingMarkupName(Section<?> section) {
		if (section.get() instanceof DefaultMarkupType) return section.get().getName();
		Section<?> root = Sections.findAncestorOfType(section, DefaultMarkupType.class);
		if (root != null) return root.get().getName();
		root = Sections.findAncestorOfType(section, TagHandlerType.class);
		if (root != null) return root.get().getName();
		return section.getFather().get().getName();
	}

	private Map<Article, List<Section<?>>> groupByArticle(Set<Section<?>> references) {
		Map<Article, List<Section<?>>> result =
				new TreeMap<Article, List<Section<?>>>(ArticleComparator.getInstance());
		for (Section<?> reference : references) {
			Article article = reference.getArticle();
			List<Section<?>> existingReferences = result.get(article);
			if (existingReferences == null) {
				existingReferences = new LinkedList<Section<?>>();
			}
			existingReferences.add(reference);
			result.put(article, existingReferences);
		}

		return result;
	}

	private void renderPlainTextOccurrences(String objectName, String web,
			Map<String, String> parameters, RenderResult result) {

		// Check if rendering is suppressed
		if (checkParameter(HIDE_PLAIN, parameters)) return;
		renderSectionStart(rb.getString("KnowWE.ObjectInfoTagHandler.plaintextoccurrences"), result);

		// Search for plain text occurrences
		SearchEngine se = new SearchEngine(Environment.getInstance()
				.getArticleManager(web));
		se.setOption(SearchOption.FUZZY);
		se.setOption(SearchOption.CASE_INSENSITIVE);
		se.setOption(SearchOption.DOTALL);
		Map<Article, Collection<Result>> results = se.search(objectName,
				PlainText.class);

		// Flag which is set to true if there are appropriate Sections
		boolean appropriateSections = false;

		for (Article article : results.keySet()) {
			RenderResult innerResult = new RenderResult(result);
			innerResult.appendHtml("<ul style=\"list-style-type:none;\">");
			for (Result r : results.get(article)) {
				Section<?> s = r.getSection();
				if (s.getFather() != null
						&& s.getFather().get().equals(article.getRootType())) {
					appropriateSections = true;
					innerResult.appendHtml("<li>");
					innerResult.appendHtml("<pre style=\"margin:1em -1em;\">");
					String textBefore = r.getAdditionalContext(-35).replaceAll("(\\{|\\})", "");
					if (!article.getRootSection().getText().startsWith(textBefore)) {
						innerResult.appendHtml("...");
					}
					innerResult.append(textBefore);
					innerResult.appendHtml("<a href=\"Wiki.jsp?page=");
					innerResult.append(article.getTitle());
					innerResult.appendHtml("#");
					innerResult.appendHtml(s.getID());
					innerResult.appendHtml("\" >");
					innerResult.append(s.getText().substring(r.getStart(), r.getEnd()));
					innerResult.appendHtml("</a>");
					String textAfter = r.getAdditionalContext(40).replaceAll("(\\{|\\})", "");
					innerResult.append(textAfter);
					if (!article.getRootSection().getText().endsWith(textAfter)) innerResult.appendHtml("...");
					innerResult.appendHtml("</pre>");
					innerResult.appendHtml("</li>");
				}
			}
			innerResult.appendHtml("</ul>");
			// append the html only if there are appropriate sections!
			if (appropriateSections) {
				wrapInExtendPanel(article.getTitle(), innerResult, result);
				appropriateSections = false;
			}
		}
		renderSectionEnd(result);
	}

	private void wrapInExtendPanel(String title, RenderResult content, RenderResult result) {
		result.appendHtml("<p id=\"objectinfo-")
				.appendHtml(String.valueOf(panelCounter++))
				.appendHtml("-show-extend\" class=\"show-extend pointer extend-panel-right\" >");
		result.appendHtml("<strong>");
		result.append(title);
		result.appendHtml("</strong>");
		result.appendHtml("</p>");
		result.appendHtml("<div class=\"hidden\">");
		result.append(content);
		result.appendHtml("</div>");
	}

	private String renderHR() {
		return "<div style=\"margin-left:-4px; height:1px; width:102%; background-color:#DDDDDD;\"></div>";
	}

	private void renderSectionStart(String title, RenderResult result) {
		result.appendHtml(sectionCounter > 0 ? renderHR() : "");
		result.appendHtml("<div>");
		result.appendHtml("<p><strong>");
		result.appendHtml(title);
		result.appendHtml("</strong></p>");
		lastResultLength = result.length();
	}

	private void renderSectionEnd(RenderResult result) {
		if (lastResultLength == result.length()) result.append("N/A");
		result.appendHtml("</div>\n");
		sectionCounter++;
	}

	private boolean checkParameter(String parameter,
			Map<String, String> parameters) {
		return parameters.get(parameter) != null ? Boolean
				.parseBoolean(parameters.get(parameter)) : false;
	}

	protected String getRenamingAction() {
		return "TermRenamingAction";
	}

	protected String getLookUpAction() {
		return "LookUpAction";
	}

	public JSONObject getTerms(String web) {
		// gathering all terms
		List<String> allTerms = new ArrayList<String>();
		Iterator<Article> iter = Environment.getInstance()
				.getArticleManager(web).getArticleIterator();
		Article currentArticle;

		TerminologyManager terminologyManager;
		while (iter.hasNext()) {
			currentArticle = iter.next();
			terminologyManager = KnowWEUtils
					.getTerminologyManager(currentArticle);
			Collection<Identifier> allDefinedTerms = terminologyManager
					.getAllDefinedTerms();
			for (Identifier definition : allDefinedTerms) {
				if (!allTerms.contains(definition.toExternalForm())) {
					allTerms.add(definition.toExternalForm());
				}
			}
		}
		JSONObject response = new JSONObject();
		try {
			response.accumulate("allTerms", allTerms);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return response;

	}

}
