/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
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

package de.d3web.we.utils;

import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.info.DCElement;
import de.d3web.core.knowledge.terminology.info.DCMarkup;
import de.d3web.core.knowledge.terminology.info.MMInfoObject;
import de.d3web.core.knowledge.terminology.info.MMInfoStorage;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.scoring.Score;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.objects.QuestionDef;
import de.d3web.we.kdom.objects.QuestionnaireDef;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class D3webUtils {

	public static de.d3web.core.knowledge.terminology.Question getQuestion(KnowledgeServiceSession kss, String qid) {
		if(kss instanceof D3webKnowledgeServiceSession) {
			D3webKnowledgeServiceSession session = ((D3webKnowledgeServiceSession)kss);
			KnowledgeBase kb = session.getBaseManagement().getKnowledgeBase();
			return session.getBaseManagement().findQuestion(qid);

		}

		return null;
	}

	/**
	 *
	 * Adds a MMInfo DCMarkup to the NamedObject
	 *
	 * @param o
	 * @param title
	 * @param subject
	 * @param content
	 * @param language
	 */
	public static void addMMInfo(NamedObject o, String title, String subject,
			String content, String language) {
		if (o == null)
			return;
		if (content == null)
			return;

		if (content.startsWith("\"") && content.endsWith("\"")
				&& content.length() > 1) {
			content = content.substring(1, content.length() - 1);
		}

		MMInfoStorage mmis;
		DCMarkup dcm = new DCMarkup();
		dcm.setContent(DCElement.TITLE, title);
		dcm.setContent(DCElement.SUBJECT, subject);
		dcm.setContent(DCElement.SOURCE, o.getId());
		if (language != null)
			dcm.setContent(DCElement.LANGUAGE, language);
		MMInfoObject mmi = new MMInfoObject(dcm, content);
		if (o.getProperties().getProperty(Property.MMINFO) == null) {
			mmis = new MMInfoStorage();
		}
		else {
			mmis = (MMInfoStorage) o.getProperties().getProperty(
					Property.MMINFO);
		}
		o.getProperties().setProperty(Property.MMINFO, mmis);
		mmis.addMMInfo(mmi);
	}

	// public static Choice getAnswer(KnowledgeServiceSession kss, String aid,
	// String qid) {
	// Question q = getQuestion(kss, qid);
	// if(q != null) {
	// D3webKnowledgeServiceSession session =
	// ((D3webKnowledgeServiceSession)kss);
	// KnowledgeBase kb = session.getBaseManagement().getKnowledgeBase();
	// return (Choice) session.getBaseManagement().findAnswer(q, aid);
	// }
	// return null;
	// }

	/**
	 * Gets the Session Object.
	 *
	 * @param sec
	 * @param user
	 */
	public static Session getSession(Section sec, KnowWEUserContext user) {

		String sessionId = sec.getTitle() + ".." + KnowWEEnvironment.generateDefaultID(sec.getTitle());
		Broker broker = D3webModule.getBroker(user.getUsername(), sec.getWeb());
		KnowledgeServiceSession kss = broker.getSession().getServiceSession(sessionId);
		Session session = null;

		if (kss instanceof D3webKnowledgeServiceSession) {

			D3webKnowledgeServiceSession d3webKSS = (D3webKnowledgeServiceSession) kss;
			session = d3webKSS.getSession();
		}
		return session;
	}

	/**
	 * Gets the Session Object.
	 *
	 * @param user
	 */
	public static Session getSession(String topic, KnowWEUserContext user, String web) {

		String sessionId = topic + ".." + KnowWEEnvironment.generateDefaultID(topic);
		Broker broker = D3webModule.getBroker(user.getUsername(), web);
		KnowledgeServiceSession kss = broker.getSession().getServiceSession(sessionId);
		Session session = null;

		if (kss instanceof D3webKnowledgeServiceSession) {

			D3webKnowledgeServiceSession d3webKSS = (D3webKnowledgeServiceSession) kss;
			session = d3webKSS.getSession();
		}
		return session;
	}

	/**
	 * finds the (d3web-) parent QASet of a QuestionDefinition
	 *
	 * @param s
	 * @param mgn
	 * @return
	 */
	public static QASet findParent(Section<QuestionDef> s, KnowledgeBaseManagement mgn) {

		// current DashTreeElement
		Section<DashTreeElement> element = KnowWEObjectTypeUtils
				.getAncestorOfType(s, new DashTreeElement());
		// get dashTree-father
		Section<? extends DashTreeElement> dashTreeFather = DashTreeElement
				.getDashTreeFather(element);

		if (dashTreeFather == null) {
			return null;
		}

		// climb up tree and look for QASet
		QASet foundAncestorQASet = null;
		while (foundAncestorQASet == null) {
			foundAncestorQASet = findParentObject(dashTreeFather, mgn);
			dashTreeFather = DashTreeElement
					.getDashTreeFather(dashTreeFather);
			if (dashTreeFather == null)
				break;

		}

		if (foundAncestorQASet == null) {
			// root QASet as default parent
			foundAncestorQASet = mgn.getKnowledgeBase().getRootQASet();
		}

		return foundAncestorQASet;
	}

	private static QASet findParentObject(
			Section<? extends DashTreeElement> dashTreeElement,
			KnowledgeBaseManagement mgn) {

		if (dashTreeElement.findSuccessor(QuestionnaireDef.class) != null) {
			String qClassName = dashTreeElement.findSuccessor(QuestionnaireDef.class).getOriginalText();
			QASet parent = mgn.findQContainer(qClassName);
			if (parent != null)
				return parent;
		}

		if (dashTreeElement.findSuccessor(QuestionDef.class) != null) {
			String qName = dashTreeElement.findSuccessor(QuestionDef.class)
					.getOriginalText();
			QASet parent = mgn.findQuestion(qName);
			if (parent != null)
				return parent;
		}

		return null;
	}

	public static Score getScoreForString(String argument) {
		Score score = null;
		List<Score> allScores = Score.getAllScores();
		for (Score sc : allScores) {
			if(sc.getSymbol().equals(argument)) {
				score = sc;
				break;
			}
		}
		return score;
	}

}
