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

package de.knowwe.d3web.action;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.blackboard.Blackboard;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.blackboard.FactFactory;
import de.d3web.core.session.values.DateValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.TextValue;
import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.event.EventManager;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.d3web.event.FindingSetEvent;

public class SetSingleFindingAction extends AbstractAction {

	@Override
	public void execute(ActionContext context) throws IOException {
		KnowWEParameterMap map = context.getKnowWEParameterMap();
		String result = setValue(map);
		if (result != null && context.getWriter() != null) {
			context.getWriter().write(result);
		}

	}

	private String setValue(KnowWEParameterMap parameterMap) {

		String objectid = parameterMap.get(KnowWEAttributes.SEMANO_OBJECT_ID);
		String valuenum = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_NUM);
		String valuedate = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_DATE);
		String valueText = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_TEXT);
		String topic = parameterMap.getTopic();
		String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);
		String namespace = null;
		String term = null;
		String valueid = null;
		try {
			namespace = java.net.URLDecoder.decode(parameterMap
					.get(KnowWEAttributes.SEMANO_NAMESPACE), "UTF-8");
			String tempValueID = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_ID);
			if (tempValueID != null) valueid = URLDecoder.decode(tempValueID, "UTF-8");
			term = URLDecoder.decode(parameterMap.get(KnowWEAttributes.SEMANO_TERM_NAME), "UTF-8");
		}
		catch (UnsupportedEncodingException e1) {
			// should not occur
		}
		if (term != null && !term.equalsIgnoreCase("undefined")) {
			objectid = term;
		}

		if (namespace == null || objectid == null) {
			return "null";
		}

		KnowledgeBaseManagement kbm = D3webModule.getKnowledgeRepresentationHandler(web).getKBM(
					topic);
		Session session = D3webUtils.getSession(topic, user, web);
		Blackboard blackboard = session.getBlackboard();

		// Necessary for FindingSetEvent
		Question question = kbm.getKnowledgeBase().getManager().searchQuestion(objectid);
		if (question != null) {

			Value value = null;
			if (valueid != null) {
				value = kbm.findValue(question, valueid);
			}
			else if (valuenum != null) {
				try {
					value = new NumValue(Double.parseDouble(valuenum));
				}
				catch (NumberFormatException e) {
					// nothing to do, value will be null, field will be empty
				}

				// TODO set valuedate in Attributes
			}
			else if (valuedate != null) {
				final DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
				try {
					Date date = format.parse(valuedate);
					value = new DateValue(date);
				}
				catch (ParseException e) {
					e.printStackTrace();
				}
			}
			else if (valueText != null) {
				value = new TextValue(valueText);
			}
			if (value != null) {
				// synchronize to session as suggested for multi-threaded
				// kernel access applications
				synchronized (session) {
					Fact fact = FactFactory.createUserEnteredFact(question, value);
					blackboard.addValueFact(fact);
				}
				EventManager.getInstance().fireEvent(
						new FindingSetEvent(question, value, namespace, web, user));
			}
		}
		return null;
	}

}
