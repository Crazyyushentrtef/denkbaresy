/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.flow.type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.core.inference.condition.Condition;
import de.d3web.diaFlux.inference.FlowchartProcessedCondition;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.condition.D3webCondition;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;


/**
 * 
 * @author Reinhard Hatko
 * @created 15.11.2010
 */
public class FlowchartProcessedConditionType extends D3webCondition<FlowchartProcessedConditionType> {

	public static final String REGEX = "PROCESSED\\[([^\\]]*)\\]";

	private final Pattern pattern;

	public FlowchartProcessedConditionType() {
		pattern = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);
	}

	@Override
	public void cleanStoredInfos(String articleName) {

	}

	@Override
	protected void init() {
		setSectionFinder(new RegexSectionFinder(REGEX, Pattern.CASE_INSENSITIVE));
	}

	@Override
	protected Condition createCondition(KnowWEArticle article, Section<FlowchartProcessedConditionType> section) {

		Matcher matcher = pattern.matcher(section.getOriginalText());

		if (!matcher.matches()) {
			return null;
		}
		else {
			String flowName = matcher.group(1);

			return new FlowchartProcessedCondition(flowName);
		}

	}


}
