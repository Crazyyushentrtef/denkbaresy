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
package de.knowwe.core.dashtree;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.rendering.DefaultTextRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DashesPrefix extends AbstractType {

	@Override
	protected void init() {
		this.sectionFinder = new DashesPrefixFinder();
		this.setCustomRenderer(new KnowWEDomRenderer<Type>() {

			@Override
			public void render(KnowWEArticle article, Section<Type> sec, KnowWEUserContext user, StringBuilder string) {
				if (sec.getOriginalText().trim().startsWith("-")) {
					string.append('~');
				}
				DefaultTextRenderer.getInstance().render(article, sec, user, string);
			}
		});
	}

	class DashesPrefixFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section<?> father, Type type) {

			int leadingSpaces = text.indexOf(text.trim());

			int index = leadingSpaces;
			while (text.charAt(index) == '-') {
				index++;
			}
			ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			result.add(new SectionFinderResult(0, index));

			return result;
		}

	}

}
