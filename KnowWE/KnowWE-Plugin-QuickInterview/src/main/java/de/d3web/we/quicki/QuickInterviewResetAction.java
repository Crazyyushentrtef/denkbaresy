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

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * For resetting the QuickInterview
 * 
 * @author Martina Freiberg
 * @created 28.08.2010
 */
public class QuickInterviewResetAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String result = resetQuickInterview(context);
		if (result != null && context.getWriter() != null) {
			context.getWriter().write(result);
		}

	}

	public String resetQuickInterview(UserActionContext context) {

		// get knowledge base
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(context.getWeb(), context.getTitle());

		// remove old session and create new session
		SessionProvider provider = SessionProvider.getSessionProvider(context);
		provider.removeSession(kb);
		provider.createSession(kb);

		// render
		return QuickInterviewAction.callQuickInterviewRenderer(context);
	}
}
