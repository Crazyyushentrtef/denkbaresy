/*
 * Copyright (C) 2016 denkbares GmbH, Germany
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

package de.knowwe.tools;

import java.util.Collections;
import java.util.Map;

import org.json.JSONObject;

import de.knowwe.core.action.Action;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.util.Icon;

import static de.knowwe.core.Attributes.SECTION_ID;
import static de.knowwe.core.Attributes.TOPIC;

/**
 * A tool causing the given action being called asynchronously passing the given section id, waits for action completion
 * and triggers a page reload.
 *
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 24.03.16.
 */
public class AsynchronousActionTool extends DefaultTool {

	public AsynchronousActionTool(Icon icon, String title, String description, Class<? extends Action> action, Section<?> section) {
		this(icon, title, description, action, section, Collections.emptyMap());
	}

	public AsynchronousActionTool(Icon icon, String title, String description, Class<? extends Action> action, Section<?> section, Map<String, String> params) {
		super(icon, title, description,
				buildJsAction(action, section, "window.location.reload()", params),
				Tool.ActionType.ONCLICK, null);
	}

	public AsynchronousActionTool(Icon icon, String title, String description, Class<? extends Action> action, Section<?> section, String redirectPage) {
		this(icon, title, description, action, section, redirectPage, null);
	}

	public AsynchronousActionTool(Icon icon, String title, String description, Class<? extends Action> action, Section<?> section, String redirectPage, String category) {
		super(icon, title, description,
				buildJsAction(action, section, "window.location='Wiki.jsp?page=" + redirectPage + "'", Collections.emptyMap()),
				Tool.ActionType.ONCLICK, category);
	}

	private static String buildJsAction(Class<? extends Action> action, Section<?> section, String successFunction, Map<String, String> params) {
		return "jq$.ajax({url : 'action/" + action.getSimpleName() + "', " +
				"cache : false, " +
				"data : " + createData(section, params) + "," +
				"success : function() {" + successFunction + "} })";
	}

	private static String createData(Section<?> section, Map<String, String> params) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(SECTION_ID, section.getID());
		jsonObject.put(TOPIC, section.getTitle());
		for (Map.Entry<String, String> stringStringEntry : params.entrySet()) {
			jsonObject.put(stringStringEntry.getKey(), stringStringEntry.getValue());
		}
		return jsonObject.toString();
	}
}
