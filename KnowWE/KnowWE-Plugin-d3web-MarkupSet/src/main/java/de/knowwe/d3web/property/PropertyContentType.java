/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.knowwe.d3web.property;

import java.util.regex.Pattern;

import de.d3web.core.knowledge.terminology.info.Property;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * Represents the content to be set to a {@link Property}.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.08.2011
 */
public class PropertyContentType extends AbstractType {

	public PropertyContentType() {
		this.setSectionFinder(new RegexSectionFinder("^\\s*=\\s*(.+)\\s*", Pattern.DOTALL, 1));
		this.setRenderer(StyleRenderer.CONTENT);
	}

	public String getPropertyContent(Section<PropertyContentType> s) {
		StringBuilder content = new StringBuilder(s.getText().trim());
		int start = 0;
		int end = content.length() - 1;
		while (start < end && content.charAt(start) == '"') {
			start++;
		}
		while (end >= start && content.charAt(end) == '"') {
			end--;
		}
		return content.substring(start, end + 1);
	}
}
