/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.kdom.rules.action;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.indication.inference.PSMethodStrategic;
import de.d3web.interview.indication.ActionRepeatedIndication;
import de.d3web.we.kdom.rules.action.ContraIndicationAction.QASetReferenceInBrackets;
import de.d3web.we.object.QASetReference;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * 
 * @author Reinhard Hatko
 * @author Jochen Reutelshoefer
 * @created 19.11.2010
 */
public class RepeatedIndication extends BracketsAction<RepeatedIndication> {

	public RepeatedIndication() {
		super(new String[] {
				"ALWAYS", "IMMER" });

	}

	@Override
	protected Type getObjectReference() {
		return new QASetReferenceInBrackets();
	}

	@Override
	public PSAction createAction(Article article, Section<RepeatedIndication> s) {
		Section<QASetReference> qSec = Sections.findSuccessor(s, QASetReference.class);
		QASet termObject = qSec.get().getTermObject(article, qSec);

		ActionRepeatedIndication repInd = new ActionRepeatedIndication();
		List<QASet> obs = new ArrayList<QASet>();
		obs.add(termObject);
		repInd.setQASets(obs);
		return repInd;
	}

	@Override
	public Class<? extends PSMethod> getActionPSContext() {
		return PSMethodStrategic.class;
	}

}
