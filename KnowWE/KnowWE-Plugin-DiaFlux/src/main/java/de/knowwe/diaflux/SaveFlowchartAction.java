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

package de.knowwe.diaflux;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.diaflux.type.DiaFluxType;
import de.knowwe.diaflux.type.FlowchartType;

/**
 * Receives a xml-encoded flowchart from the editor and replaces the old kdom
 * node with the new content
 * 
 * @author Reinhard Hatko
 * @created 24.11.2010
 */
public class SaveFlowchartAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String web = context.getWeb();
		String nodeID = context.getParameter(Attributes.TARGET);
		String topic = context.getTitle();
		String newText = context.getParameter(Attributes.TEXT);

		if (nodeID == null) {
			saveNewFlowchart(context, web, topic, newText);
		}
		else {
			replaceExistingFlowchart(context, web, nodeID, topic, newText);
		}

	}

	/**
	 * Saves a flowchart when the surrounding %%DiaFlux markup exists.
	 * 
	 * @created 23.02.2011
	 * @param map
	 * @param web
	 * @param nodeID
	 * @param topic
	 * @param newText
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void replaceExistingFlowchart(UserActionContext context, String web, String nodeID, String topic, String newText) throws IOException {
		Section<DiaFluxType> diaFluxSection = (Section<DiaFluxType>) Sections.getSection(
				nodeID);

		Section<FlowchartType> flowchartSection = Sections.findSuccessor(diaFluxSection,
				FlowchartType.class);

		// new ID for section to be transmitted to the editor
		String id = null;

		// if flowchart is existing, replace flowchart
		if (flowchartSection != null) {
			Set<String> articles = Environment.getInstance().getPackageManager(web).getCompilingArticles(
					flowchartSection);
			save(context, topic, flowchartSection.getID(), newText);
			id = getSectionID(web, newText, articles);
		}
		else { // no flowchart, insert flowchart
			StringBuilder builder = new StringBuilder("%%DiaFlux");
			builder.append("\r\n");
			builder.append(newText);

			// one line version, breaks because of missing linebreak, see ticket
			// #172
			if (diaFluxSection.getText().matches("%%DiaFlux */?% *")) {
				builder.append("\r\n");
				builder.append("%");
			}
			else { // TODO this adds all content, just extract annotations
				builder.append(diaFluxSection.getText().substring(9));
			}

			save(context, topic, nodeID, builder.toString());
		}

		// if new sectionID was found, transfer it to the editor
		if (id != null) {
			context.getWriter().write(id);
		}
		// else {
		// TODO
		// This happens, if now flowchart section was found, i.e. if it was
		// created with this saving OR if it is not compiled in any article.
		// Then, the editor can not be reused and must be closed and opened
		// again.
		// ATM, the workaround was, to remove the "save-only" button in the
		// editor
		// it can be activated again, if the new section id can be found in
		// any case.
		// }
	}

	/*
	 * TODO returns the new ID of the section to deliver it to the editor. There
	 * should be a cleaner way.
	 */
	private String getSectionID(String web, String newText, Set<String> articles) {
		if (articles.isEmpty()) {
			// TODO
			return null;
		}

		Matcher matcher = Pattern.compile("name=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE).matcher(
				newText);
		if (!matcher.find()) {
			// TODO what now??
			return null;
		}

		String flowname = matcher.group(1);

		String title = articles.iterator().next();
		Article article = Environment.getInstance().getArticle(web, title);

		TerminologyManager handler = KnowWEUtils.getTerminologyManager(article);
		Section<?> section = handler.getTermDefiningSection(flowname);

		Section<DiaFluxType> diafluxSec = Sections.findAncestorOfExactType(section,
				DiaFluxType.class);

		return diafluxSec.getID();
	}

	/**
	 * Saves a flowchart for which no section exists in the article yet.
	 * Currently only used, when extracting a module with
	 * DiaFlux-Refactoring-Plugin.
	 * 
	 * @created 23.02.2011
	 * @param map
	 * @param web
	 * @param topic
	 * @param newText
	 * @throws IOException
	 */
	private void saveNewFlowchart(UserActionContext context, String web, String topic, String newText) throws IOException {
		ArticleManager mgr = Environment.getInstance().getArticleManager(
				context.getWeb());
		Article article = mgr.getArticle(topic);
		Section<Article> rootSection = article.getRootSection();

		// append flowchart to root section and replace it
		String newArticle = rootSection.getText() + "\r\n%%DiaFlux\r\n" + newText
				+ "\r\n%\r\n";
		String nodeID = rootSection.getID();

		save(context, topic, nodeID, newArticle);

	}

	private void save(UserActionContext context, String topic, String nodeID, String newText) throws IOException {
		Map<String, String> nodesMap = new HashMap<String, String>();
		nodesMap.put(nodeID, newText);
		Sections.replaceSections(context, nodesMap);

	}

}
