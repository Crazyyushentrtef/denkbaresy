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

package de.d3web.we.basic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpSession;

import de.d3web.core.inference.PSMethod.Type;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;

/**
 * This class stores all {@link Sessions}. The class itself is stored in each
 * user's {@link HTTPSession} and is accessible by calling:
 * 
 * <pre>
 * httpSession.getAttribute(Attributes.SESSIONPROVIDER)
 * </pre>
 * 
 * on a {@link HTTPSession} object. For convenience there exist two static
 * utility methods:
 * 
 * <pre>
 * SessionProvider.getSessionProvider(UserContext context)
 * </pre>
 * 
 * and
 * 
 * <pre>
 * SessionProvider.getSession(UserContext context, KnowledgeBase base)
 * </pre>
 * 
 * which does all the dirty work for you by using KnowWE's {@link UserContext}.
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * @created 06.03.2012
 */
public class SessionProvider {

	private final Map<String, Session> sessions;

	private SessionProvider() {
		sessions = new HashMap<String, Session>();
	}

	/**
	 * Returns the SessionProvider object for a specified {@link UserContext}.
	 * If there is no SessionProvider object, a new one will be created and
	 * stored in the provided UserContext, i. e. HTTPSession.
	 * 
	 * Please be aware that this method only works with an UserContext backed by
	 * a real HTTPSession. Otherwise there is no place to store and retrieve the
	 * SessionProvider object.
	 * 
	 * @created 06.03.2012
	 * @param context UserContext of the current user.
	 * @return SessionProvider object associated to the user
	 */
	private static SessionProvider getSessionProvider(UserContext context) {
		HttpSession httpSession = context.getSession();
		SessionProvider provider = null;
		if (httpSession != null) {
			provider = (SessionProvider) httpSession.getAttribute(Attributes.SESSIONPROVIDER);
			if (provider == null) {
				provider = new SessionProvider();
				context.getSession().setAttribute(Attributes.SESSIONPROVIDER, provider);
			}
		}
		return provider;
	}

	private Session createSessionInternally(UserContext context, KnowledgeBase kb) {
		removeSessionInternally(context, kb);
		Session session = SessionFactory.createSession(kb);
		EventManager.getInstance().fireEvent(new SessionCreatedEvent(session));
		sessions.put(kb.getId(), session);
		return session;
	}

	/**
	 * Returns an existing {@link Session} for the provided knowledge base. If
	 * there exists no session for this knowledge base this method will create
	 * one. If the knowledge base of an existing session is not up to date and
	 * no user facts has been set, the knowledge base will be replaced
	 * automatically (the session will be reseted).
	 * 
	 * @created 06.03.2012
	 * @param kb the underlying knowledge base
	 * @return session for the specified knowledge base
	 */
	private Session getSessionInternally(UserContext context, KnowledgeBase kb) {
		Session session = sessions.get(kb.getId());
		if (session == null) {
			session = createSessionInternally(context, kb);
		}
		// check if existing session is out dated
		if (session.getKnowledgeBase() != kb) {
			// check if the session is empty
			for (TerminologyObject t : session.getBlackboard().getValuedObjects()) {
				Fact fact = session.getBlackboard().getValueFact(t);
				if (fact.getPSMethod() == null || fact.getPSMethod().hasType(Type.source)) {
					// session is not empty -> don't touch it!
					return session;
				}
			}
			// session is empty -> reset
			removeSessionInternally(context, kb);
			session = createSessionInternally(context, kb);
		}
		return session;
	}

	private void removeSessionInternally(UserContext context, KnowledgeBase kb) {
		Session removedSession = sessions.remove(kb.getId());
		if (removedSession != null) {
			EventManager.getInstance().fireEvent(new SessionRemovedEvent(removedSession, context));
			removedSession.getPropagationManager().terminate();
		}
	}

	private void setSessionInternally(UserContext context, Session session) {
		removeSessionInternally(context, session.getKnowledgeBase());
		sessions.put(session.getKnowledgeBase().getId(), session);
	}

	/**
	 * Creates and returns a new {@link Session} for the {@link KnowledgeBase}.
	 * The created session is accessible by using the id of the knowledge base.
	 * 
	 * @created 06.03.2012
	 * @param context the {@link UserContext} the session will belong to
	 * @param kb The underlying knowledge base
	 * @return the created session
	 */
	public static Session createSession(UserContext context, KnowledgeBase kb) {
		SessionProvider sessionProvider = getSessionProvider(context);
		if (sessionProvider == null) {
			return null;
		}
		return sessionProvider.createSessionInternally(context, kb);
	}

	/**
	 * Returns the {@link Session} for a specified {@link UserContext} and a
	 * {@link KnowledgeBase}. This methods tries to load an existing
	 * SessionProvider object from the user's HTTPSession. If there is no
	 * SessionProvider object, a new one will be created and stored in the
	 * provided UserContext, i. e. HTTPSession. If the knowledge base of an
	 * existing session is not up to date and no user facts has been set, the
	 * knowledge base will be replaced automatically (the session will be
	 * reseted).
	 * 
	 * Please be aware that this method only works with an UserContext backed by
	 * a real HTTPSession. Otherwise there is no place to store and retrieve the
	 * SessionProvider object.
	 * 
	 * @created 07.03.2012
	 * @param context UserContext of the current user.
	 * @param base the underlying knowledge base
	 * @return Session for the specified knowledge base
	 */
	public static Session getSession(UserContext context, KnowledgeBase base) {
		SessionProvider provider = getSessionProvider(context);
		if (provider == null) {
			return null;
		}
		return provider.getSessionInternally(context, base);
	}

	/**
	 * Returns a set of session of all knowledge bases that have been created,
	 * using the specified section. Thus this method will provide the current
	 * users d3web-session for every knowledge base that has been compiled using
	 * the packe the specified section belongs to.
	 * 
	 * @created 17.08.2012
	 * @param context the user context to get the sessions for
	 * @param section the section to determine the package for
	 * @return the session of the knowledge bases created out of the section
	 */
	public static Collection<Session> getSessions(UserContext context, Section<?> section) {
		SessionProvider provider = getSessionProvider(context);
		if (provider == null) {
			return Collections.emptyList();
		}
		String web = context.getWeb();
		PackageManager packageManager = Environment.getInstance().getPackageManager(web);
		Collection<Session> result = new LinkedList<Session>();
		for (String article : packageManager.getCompilingArticles(section)) {
			KnowledgeBase knowledgeBase = D3webUtils.getKnowledgeBase(web, article);
			result.add(provider.getSessionInternally(context, knowledgeBase));
		}
		return result;
	}

	/**
	 * Checks whether the current {@link Session} uses an out dated
	 * {@link KnowledgeBase}.
	 * 
	 * @created 22.03.2012
	 * @param context
	 * @param base
	 * @return
	 */
	public static boolean hasOutDatedSession(UserContext context, KnowledgeBase base) {
		Session session = getSession(context, base);
		return session.getKnowledgeBase() != base;
	}

	/**
	 * Removes an existing {@link Session} for the provided knowledge base. If
	 * there exists no session for this knowledge base ID this method will do
	 * nothing.
	 * 
	 * @created 06.03.2012
	 * @param context the {@link UserContext} to which the session belongs
	 * @param kb the underlying knowledge base
	 */
	public static void removeSession(UserContext context, KnowledgeBase kb) {
		SessionProvider sessionProvider = getSessionProvider(context);
		if (sessionProvider != null) {
			sessionProvider.removeSessionInternally(context, kb);
		}
	}

	/**
	 * Sets the session to the specified session by using the id of the
	 * session's underlying kb. An existing session will be overwritten!
	 * 
	 * @created 06.03.2012
	 * @param context the {@link UserContext} for which the session should be
	 *        set
	 * @param session the session to be set
	 */
	public static void setSession(UserContext context, Session session) {
		SessionProvider sessionProvider = getSessionProvider(context);
		if (sessionProvider != null) {
			sessionProvider.setSessionInternally(context, session);
		}
	}

}
