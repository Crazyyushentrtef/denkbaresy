/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.knowwe.jspwiki.types;

import java.util.regex.Pattern;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.user.UserContext;

/**
 * Markup type to detect jsp-wiki definitions.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 14.02.2014
 */
public class DefinitionType extends AbstractType {

	protected static class DefinitionData extends AbstractType {

		public DefinitionData() {
			this.setSectionFinder(new RegexSectionFinder(":\\s*([^\n\r]+?)\\s*((\n\r?)|$)", 0, 1));
		}
	}

	protected static class DefinitionHead extends AbstractType {

		public DefinitionHead() {
			this.setSectionFinder(new RegexSectionFinder(";;?\\s*([^;:\n\r]+?)\\s*:", 0, 1));
		}
	}

	private static class DefinitionRenderer implements Renderer {

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			renderJSPWikiAnchor(Sections.cast(section, DefinitionType.class), result);
			result.append(section.getText());
		}

	}

	public DefinitionType() {
		setSectionFinder(new RegexSectionFinder(
				"^;[^;:\n\r]+:[^\n\r]+((\n\r?)|$)",
				Pattern.MULTILINE));
		setRenderer(new DefinitionRenderer());

		addChildType(new DefinitionData());
		addChildType(new DefinitionHead());
	}

	public String getHeadText(Section<? extends DefinitionType> section) {
		// due to the section finder, if this section is instantiated
		// we always have both head and data children
		return Sections.successor(section, DefinitionHead.class).getText();
	}

	public String getDataText(Section<? extends DefinitionType> section) {
		// due to the section finder, if this section is instantiated
		// we always have both head and data children
		return Sections.successor(section, DefinitionData.class).getText();
	}

	protected static void renderJSPWikiAnchor(Section<DefinitionType> section, RenderResult result) {
		String anchorName = "section-" +
				Strings.encodeURL(section.getTitle()) + "-" +
				Strings.encodeURL(cleanLink(section.get().getHeadText(section)));
		anchorName = anchorName.replace('%', '_').replace('/', '_');
		result.appendHtml("<a name='" + anchorName + "'></a>");
	}

	public static String cleanLink(String link) {
		final String allowedChars = "._";
		StringBuffer clean = new StringBuffer(link.length());

		// Remove non-alphanumeric characters that should not
		// be put inside WikiNames. Note that all valid
		// Unicode letters are considered okay for WikiNames.
		// It is the problem of the WikiPageProvider to take
		// care of actually storing that information.
		//
		// Also capitalize things, if necessary.

		boolean isWord = true; // If true, we've just crossed a word boundary
		boolean wasSpace = false;

		for (int i = 0; i < link.length(); i++) {
			char ch = link.charAt(i);

			// Cleans away repetitive whitespace and only uses the first one.
			if (Character.isWhitespace(ch)) {
				if (wasSpace) continue;
				wasSpace = true;
			}
			else {
				wasSpace = false;
			}

			// Check if it is allowed to use this char, and capitalize, if
			// necessary.
			if (Character.isLetterOrDigit(ch) || allowedChars.indexOf(ch) != -1) {
				// Is a letter
				if (isWord) ch = Character.toUpperCase(ch);
				clean.append(ch);
				isWord = false;
			}
			else {
				isWord = true;
			}
		}

		return clean.toString();
	}

}
