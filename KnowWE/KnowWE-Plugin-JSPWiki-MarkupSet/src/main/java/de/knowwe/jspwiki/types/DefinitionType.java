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

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;

/**
 * Markup type to detect jsp-wiki definitions.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 14.02.2014
 */
public class DefinitionType extends AbstractType {

	public static class DefinitionData extends AbstractType {

		public DefinitionData() {
			this.setSectionFinder(new RegexSectionFinder(":\\s*([^\n\r]+?)\\s*((\n\r?)|$)", 0, 1));
		}
	}

	public static class DefinitionHead extends AbstractType {

		public DefinitionHead() {
			this.setSectionFinder(new RegexSectionFinder(";;?\\s*([^;:\n\r]+?)\\s*:", 0, 1));
		}
	}

	public DefinitionType() {
		this.setSectionFinder(new RegexSectionFinder(
				"^;[^;:\n\r]+:[^\n\r]+((\n\r?)|$)",
				Pattern.MULTILINE));

		addChildType(new DefinitionData());
		addChildType(new DefinitionHead());
	}
}
