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

package de.d3web.we.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

public class StringEnumChecker<T extends Type> extends SubtreeHandler<T> {

	private final String[] values;
	private final KDOMError error;
	private int startOffset;
	private int endOffset;

	public StringEnumChecker(String[] values, KDOMError error, int startOffset, int endoffset) {
		this.values = values;
		this.error = error;
		this.startOffset = startOffset;
		this.endOffset = endoffset;
	}

	public StringEnumChecker(String[] values, KDOMError error) {
		this.values = values;
		this.error = error;
		this.startOffset = 0;
		this.endOffset = 0;
	}

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {

		// cut offsets and trim
		String sectionContent = s.getOriginalText();
		sectionContent = sectionContent.substring(startOffset);
		sectionContent = sectionContent.substring(0,
					sectionContent.length() - endOffset);
		String checkContent = sectionContent.trim();

		// check against string values
		boolean found = false;
		for (String string : values) {
			if (checkContent.equalsIgnoreCase(string)) {
				found = true;
			}
		}
		List<KDOMReportMessage> msgs = new ArrayList<KDOMReportMessage>();
		if (!found) {
			msgs.add(error);
		}
		return msgs;
	}

}
