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
package de.d3web.we.object;

import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.objects.SimpleTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Abstract type for referencing d3web-objects, such as solutions, questions,
 * questionnaires...
 * 
 * 
 * @author Jochen/Albrecht
 * @created 26.07.2010
 * @param <TermObject>
 */
public abstract class D3webTermReference<TermObject extends NamedObject> extends AbstractType implements D3webTerm<TermObject> {

	@Override
	public String getTermIdentifier(Section<? extends SimpleTerm> section) {
		return KnowWEUtils.trimQuotes(section.getText());
	}

	@Override
	public TermObject getTermObject(KnowWEArticle article, Section<? extends D3webTerm<TermObject>> section) {
		return D3webUtils.getTermObjectDefaultImplementation(article, section);
	}

}
