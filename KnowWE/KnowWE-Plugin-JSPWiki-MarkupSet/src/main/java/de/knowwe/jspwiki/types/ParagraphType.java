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
 * 
 * @author Lukas Brehl
 * @created 05.05.2012
 */
public class ParagraphType extends AbstractType {

	public ParagraphType() {
		Pattern pattern = Pattern.compile("^.+?([\n\r]{4,}|\\z)",
				Pattern.MULTILINE + Pattern.DOTALL);
		this.setSectionFinder(new RegexSectionFinder(pattern));
		this.addChildType(new PrettifyType());
		this.addChildType(new VerbatimType());
		this.addChildType(new ListType());
		this.addChildType(new OrderedListType());
		this.addChildType(new BoldType());
		this.addChildType(new ItalicType());
		this.addChildType(new StrikeThroughType());
		this.addChildType(new ImageType());
		this.addChildType(new LinkType());
		this.addChildType(new WikiTextType());
	}
}
