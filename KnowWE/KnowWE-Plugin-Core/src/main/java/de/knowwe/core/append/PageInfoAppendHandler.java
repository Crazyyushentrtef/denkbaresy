/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.append;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.wikiConnector.WikiConnector;

/**
 * This handler appends some basic information about the page and the current
 * user to the page.
 * 
 * @author Reinhard Hatko
 * @created 18.10.2012
 */
public class PageInfoAppendHandler implements PageAppendHandler {

	@Override
	public void append(String web, String title, UserContext user, RenderResult html) {
		WikiConnector connector = Environment.getInstance().getWikiConnector();
		int version = connector.getVersion(title);
		long modDate = connector.getLastModifiedDate(title, -1).getTime();
		String userName = user.getUserName();

		// username and topic can not contain special chars, so no masking
		// should be necessary
		html.appendHtml("<input type='hidden' id='knowWEInfoPageName' value='" + title + "'>");
		html.appendHtml("<input type='hidden' id='knowWEInfoPageVersion' value='" + version + "'>");
		html.appendHtml("<input type='hidden' id='knowWEInfoPageDate' value='" + modDate + "'>");
		html.appendHtml("<input type='hidden' id='knowWEInfoUser' value='" + userName + "'>");
	}

	@Override
	public boolean isPre() {
		return false;
	}

}
