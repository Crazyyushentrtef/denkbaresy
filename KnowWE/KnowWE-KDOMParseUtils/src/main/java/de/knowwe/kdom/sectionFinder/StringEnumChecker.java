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

package de.knowwe.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;

public class StringEnumChecker<T extends Type> extends SubtreeHandler<T> {

	private final String[] values;
	private final Message error;
	private final int startOffset;
	private final int endOffset;

	public StringEnumChecker(String[] values, Message error, int startOffset, int endoffset) {
		this.values = Arrays.copyOf(values, values.length);
		this.error = error;
		this.startOffset = startOffset;
		this.endOffset = endoffset;
	}

	public StringEnumChecker(String[] values, Message error) {
		this(values, error, 0, 0);
	}

	@Override
	public Collection<Message> create(Article article, Section<T> s) {

		// cut offsets and trim
		String sectionContent = s.getText();
		sectionContent = sectionContent.substring(startOffset);
		sectionContent = sectionContent.substring(0,
					sectionContent.length() - endOffset);
		String checkContent = sectionContent.trim();

		// check against string values
		boolean found = false;
		for (String string : values) {
			if (checkContent.equalsIgnoreCase(string)) {
				found = true;
				break;
			}
		}
		List<Message> msgs = new ArrayList<Message>();
		if (!found) {
			msgs.add(error);
		}
		return msgs;
	}

}
