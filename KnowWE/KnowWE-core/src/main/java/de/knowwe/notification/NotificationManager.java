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

package de.knowwe.notification;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import de.knowwe.core.Attributes;
import de.knowwe.core.user.UserContext;

/**
 * This class stores all {@link Notification}s. The class itself is stored in
 * each user's {@link HTTPSession} and is accessible by calling:
 * 
 * <pre>
 * httpSession.getAttribute(Attributes.NOTIFICATIONMANAGER)
 * </pre>
 * 
 * on a {@link HTTPSession} object. For convenience there exist a static utility
 * method:
 * 
 * <pre>
 * NotificationManager.getNotificationManager(UserContext context)
 * </pre>
 * 
 * which does all the dirty work for you by using KnowWE's {@link UserContext}.
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * @created 20.04.2012
 */
public class NotificationManager {

	private final Map<String, Notification> notifications = new HashMap<String, Notification>();

	/**
	 * Returns the NotificationManager object for a specified
	 * {@link UserContext}. If there is no NotificationManager object, a new one
	 * will be created and stored in the provided UserContext, i. e.
	 * HTTPSession.
	 * 
	 * Please be aware that this method only works with an UserContext backed by
	 * a real HTTPSession. Otherwise there is no place to store and retrieve the
	 * NotificationManager object.
	 * 
	 * @created 20.04.2012
	 * @param context UserContext of the current user.
	 * @return NotificationManager object associated to the user
	 */
	public static NotificationManager getNotificationManager(UserContext context) {
		HttpSession httpSession = context.getSession();
		NotificationManager manager = null;
		if (httpSession != null) {
			manager = (NotificationManager) httpSession.getAttribute(Attributes.NOTIFICATIONMANAGER);
			if (manager == null) {
				manager = new NotificationManager();
				context.getSession().setAttribute(Attributes.NOTIFICATIONMANAGER, manager);
			}
		}
		return manager;
	}

	/**
	 * Returns all Notifications associated with this NotificationManager.
	 * 
	 * @created 20.04.2012
	 * @return all notifications for this user.
	 */
	public Collection<Notification> getNotifications() {
		return Collections.unmodifiableCollection(notifications.values());
	}

	/**
	 * Adds a {@link Notification} to the NotificationManager belonging to the
	 * specified {@link UserContext}. The notification will be indexed using
	 * it's ID in order to allow the removal of the notification by actions
	 * etc...
	 * 
	 * @created 20.04.2012
	 * @param context
	 * @param notification
	 */
	public static void addNotification(UserContext context, Notification notification) {
		NotificationManager manager = NotificationManager.getNotificationManager(context);
		manager.addNotification(notification);
	}

	/**
	 * Removes the notification with the specified ID from the
	 * NotificationManager belonging to the specified {@link UserContext}. The
	 * id of a standard notification is it's hash-code, but in general the id
	 * should be provided in the html markup...
	 * 
	 * @created 20.04.2012
	 * @param context
	 * @param id
	 */
	public static void removeNotification(UserContext context, String id) {
		NotificationManager manager = NotificationManager.getNotificationManager(context);
		manager.removeNotification(id);
	}

	/**
	 * Adds a {@link Notification} to this NotificationManager. The notification
	 * will be indexed using it's ID in order to allow the removal of the
	 * notification by actions etc...
	 * 
	 * @created 20.04.2012
	 * @param notification the notification to be added.
	 */
	public void addNotification(Notification notification) {
		if (!notifications.values().contains(notification)) {
			notifications.put(notification.getID(), notification);
		}
	}

	/**
	 * Removes the notification with the specified ID. The id of a standard
	 * notification is it's hash-code, but in general the id should be provided
	 * in the html markup...
	 * 
	 * @created 20.04.2012
	 * @param id the id of the notification
	 */
	public void removeNotification(String id) {
		notifications.remove(id);
	}

}
