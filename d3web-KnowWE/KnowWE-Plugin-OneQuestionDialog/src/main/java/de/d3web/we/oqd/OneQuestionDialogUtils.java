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
package de.d3web.we.oqd;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.knowledge.InterviewObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.d3webModule.D3webModule;


/**
 * 
 * @author Florian Ziegler
 * @created 16.08.2010 
 */
public class OneQuestionDialogUtils {

	/**
	 * gets a session from a topic and a web
	 * 
	 * @created 31.08.2010
	 * @param topic
	 * @param web
	 * @return the session
	 */
	public static Session getSession(String topic, String web) {
		D3webKnowledgeService knowledgeService = D3webModule.getAD3webKnowledgeServiceInTopic(
				web, topic);
		
		return SessionFactory.createSession(knowledgeService.getBase()); 
	}

	/**
	 * Returns all alternatives of an InterviewObject
	 * 
	 * @created 31.08.2010
	 * @param o
	 * @return the alternatives
	 */
	public static List<Choice> getAllAlternatives(InterviewObject object) {
		List<Choice> answers = new ArrayList<Choice>();
		
		if (object instanceof QuestionChoice) {
			answers = ((QuestionChoice) object).getAllAlternatives();
		} else if (object instanceof QuestionYN) {
			answers = ((QuestionYN) object).getAllAlternatives();
		}
		
		return answers;
	}
	
	/**
	 * Creates a new Form with question and answers from an InterviewObject
	 * 
	 * @created 31.08.2010
	 * @param o
	 * @return the new form
	 */
	public static String createNewForm(InterviewObject o) {
		String type = "";
		if (o instanceof QuestionOC) {
			type = "radio";
		} else {
			type = "checkbox";
		}
		
		List<Choice> answers = OneQuestionDialogUtils.getAllAlternatives(o);
		
		StringBuilder html = new StringBuilder();
		html.append("<form>");
		html.append("<p>");
		html.append(o.getName());
		html.append("<input type=\"hidden\" name=\"" + o.getId() + "\" value=\"" + o.getId() + "\">");
		html.append("</p>");
		html.append("<table>");
		for (Choice c : answers) {
			html.append("<tr>");
			html.append("<td>");
			html.append("<input type=\"" + type +  "\" value=\"" + c.getName() + "\">" + c.getName());
			html.append("<input type=\"hidden\" name=\"" + c.getId() + "\" value=\"" + c.getId() + "\">");
			html.append("</td>");
			html.append("</tr>");
		}
		html.append("<tr><td><div class=\"sendButton\" style=\"height: 20px; width: 20px; background-color:#CDCDCD; border: 1px solid #CDCDCD; cursor: pointer\" onclick=\"return OneQuestionDialog.sendQuestion(this)\">(+)</div></td></tr>");
		html.append("</table>");
		html.append("</form>");
		
		return html.toString();
	}
}
