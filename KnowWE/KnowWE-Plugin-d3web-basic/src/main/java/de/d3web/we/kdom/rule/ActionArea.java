/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.kdom.rule;

import java.util.ArrayList;
import java.util.List;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.utils.StringFragment;
import de.knowwe.core.utils.Strings;

public class ActionArea extends AbstractType {

	public ActionArea(AbstractType action) {

		this.addChildType(new SingleAction(action));
	}

	class SingleAction extends AbstractType {

		public SingleAction(AbstractType t) {
			this.setSectionFinder(new SingleActionFinder());

			this.addChildType(t);
			t.setSectionFinder(new AllTextFinderTrimmed());
		}
	}

	class SingleActionFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			List<StringFragment> actions = Strings.splitUnquoted(text, ";");
			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			for (StringFragment string : actions) {
				result.add(new SectionFinderResult(string.getStart(), string.getStart()
						+ string.getContent().length()));
			}
			return result;
		}

	}

}
