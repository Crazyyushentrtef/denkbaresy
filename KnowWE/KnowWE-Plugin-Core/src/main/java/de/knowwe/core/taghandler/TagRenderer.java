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

package de.knowwe.core.taghandler;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.taghandler.TagHandler;
import de.d3web.we.taghandler.TagHandlerAttributeSubTreeHandler;
import de.d3web.we.user.UserContext;
import de.d3web.we.utils.KnowWEUtils;

public class TagRenderer extends KnowWEDomRenderer<TagHandlerType> {

	@SuppressWarnings("unchecked")
	@Override
	public void render(KnowWEArticle article, Section<TagHandlerType> sec, UserContext user, StringBuilder string) {
		Section<TagHandlerTypeContent> attrContent = Sections.findChildOfType(sec,
				TagHandlerTypeContent.class);
		if (attrContent == null) {
			string.append(KnowWEUtils
					.maskHTML("<div><p class='info box'>"));
			string.append(KnowWEEnvironment
					.getInstance().getKwikiBundle(user).getString(
							"KnowWE.Taghandler.notFoundError"));
			string.append(" '"
					+ ((Section<?>) sec.getChildren().get(1)).getOriginalText()
					+ "'");
			string.append(KnowWEUtils.maskHTML("</p></div>"));
			return;
		}

		StringBuilder buffi = new StringBuilder();

		Type type = attrContent.get();

		if (type instanceof TagHandlerTypeContent) {
			Map<String, String> attValues = null; // ((TagHandlerTypeContent)type).getValuesForSections().get(attrContent);
			String id = sec.getID();
			Object storedValues = sec.getSectionStore().getObject(sec.getArticle(),
					TagHandlerAttributeSubTreeHandler.ATTRIBUTE_MAP);
			if (storedValues != null) {
				if (storedValues instanceof Map) {
					attValues = (Map<String, String>) storedValues;
				}
			}

			if (attValues != null) {
				attValues.put("kdomid", id);
				for (String elem : attValues.keySet()) {
					HashMap<String, TagHandler> defaultTagHandlers = KnowWEEnvironment.getInstance().getDefaultTagHandlers();
					String key = elem.toLowerCase();
					if (defaultTagHandlers.containsKey(key)) {
						TagHandler handler = defaultTagHandlers.get(key);
						boolean autoUpdate = handler.requiresAutoUpdate();
						// if (autoUpdate) {
						// buffi.append(KnowWEUtils.maskHTML(
						// "<span class=\"ReRenderSectionMarker\"" +
						// " id=\"" + id + "\"" +
						// " rel=\"{id:'" + id +
						// "'}\"" +
						// ">"));
						// }
						String wrappingTag = "div";
						if (attValues.containsKey("wrap")) {
							if (attValues.get("wrap").contains("span")) {
								wrappingTag = "span";
							}
						}
						if (key.equals("quickinterview")) {
							buffi.append(KnowWEUtils.maskHTML("<" + wrappingTag + " id=\""
									+ key + "\">"));
						}
						else {
							buffi.append(KnowWEUtils.maskHTML("<" + wrappingTag + " id=\""
									+ key + "_" + sec.getID() + "\">"));
						}
						String resultText =
								handler.render(article, sec, user, attValues);
						buffi.append(resultText).append(" \n");
						buffi.append(KnowWEUtils.maskHTML("</" + wrappingTag + ">"));
						// if (autoUpdate) {
						// buffi.append(KnowWEUtils.maskHTML("</span>"));
						// }
					}
				}

			}
			string.append(buffi.toString());
		}
	}

}
