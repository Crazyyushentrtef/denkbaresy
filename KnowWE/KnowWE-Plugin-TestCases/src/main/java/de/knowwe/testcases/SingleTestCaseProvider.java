/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.knowwe.testcases;

import java.util.Collections;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.testcase.model.TestCase;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.report.Message;
import de.knowwe.core.user.UserContext;

/**
 * Capsules a {@link TestCase}
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 27.02.2012
 */
public class SingleTestCaseProvider implements TestCaseProvider {

	private TestCase testCase;
	private final Article article;
	private final String name;

	public SingleTestCaseProvider(TestCase testCase, Article article, String name) {
		super();
		this.testCase = testCase;
		this.article = article;
		this.name = name;
	}

	@Override
	public TestCase getTestCase() {
		return testCase;
	}

	@Override
	public Session getActualSession(UserContext user) {
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(user.getWeb(), article.getTitle());
		Session session = SessionProvider.getSession(user, kb);
		return session;
	}

	@Override
	public SessionDebugStatus getDebugStatus(UserContext user) {
		String key = "SessionDebugStatus_" + article.getTitle() + "/" + getName();
		SessionDebugStatus status = (SessionDebugStatus) user.getSession().getAttribute(key);
		if (status == null) {
			status = new SessionDebugStatus(getActualSession(user));
			user.getSession().setAttribute(key, status);
		}
		return status;
	}

	@Override
	public void storeSession(Session session, UserContext user) {
		SessionProvider provider = SessionProvider.getSessionProvider(user);
		provider.setSession(session);
		getDebugStatus(user).setSession(session);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<Message> getMessages() {
		return Collections.emptyList();
	}

}
