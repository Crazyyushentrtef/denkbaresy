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

package de.d3web.we.quicki;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import de.d3web.core.inference.LoopTerminator;
import de.d3web.core.inference.LoopTerminator.LoopStatus;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.notification.NotificationManager;
import de.knowwe.notification.OutDatedSessionNotification;
import de.knowwe.notification.StandardNotification;

public class QuickInterviewAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String result = callQuickInterviewRenderer(context);
		if (result != null && context.getWriter() != null) {
			context.setContentType("text/html; charset=UTF-8");
			context.getWriter().write(result);
		}
	}

	/**
	 * First initializes everything needed for using knowledge / using an
	 * interview, then calls the appropriate renderer with the created session
	 * 
	 * @created 15.07.2010
	 * @param topic
	 * @param user
	 * @param request
	 * @param web
	 * @return
	 */
	public static String callQuickInterviewRenderer(UserContext usercontext) {
		if (usercontext == null || usercontext.getSession() == null) {
			return "";
		}

		String topic = usercontext.getTitle();
		String web = usercontext.getParameter(Attributes.WEB);
		HttpServletRequest request = usercontext.getRequest();

		ResourceBundle rb = D3webUtils.getD3webBundle(request);

		KnowledgeBase kb = D3webUtils.getKnowledgeBase(web, topic);
		if (kb == null) return rb.getString("KnowWE.quicki.error");
		Session session = SessionProvider.getSession(usercontext, kb);

		// check if the latest knowledge base is used
		if (SessionProvider.hasOutDatedSession(usercontext, kb)) {
			NotificationManager.addNotification(usercontext,
					new OutDatedSessionNotification(usercontext.getTitle()));
		}

		// check if the session has terminated due to loop detection
		LoopStatus loopStatus = LoopTerminator.getInstance().getLoopStatus(session);
		if (loopStatus.hasTerminated()) {
			NotificationManager.addNotification(usercontext,
					new StandardNotification("A loop has been detected in objects " +
							loopStatus.getLoopObjects(), Type.WARNING));
		}

		Article article = Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB).getArticle(
				topic);
		Section<QuickInterviewMarkup> markup = Sections.findSuccessor(article.getRootSection(),
				QuickInterviewMarkup.class);

		String annotation = null;
		if (markup != null) {
			annotation = DefaultMarkupType.getAnnotation(markup, QuickInterviewMarkup.SAVE_KEY);
		}

		boolean saveSession = false;

		if (annotation != null && annotation.equalsIgnoreCase("true")) {
			saveSession = true;
		}

		return QuickInterviewRenderer.renderInterview(session, web, usercontext, saveSession);
	}
}
