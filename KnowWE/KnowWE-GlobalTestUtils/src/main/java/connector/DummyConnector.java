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

package connector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import de.knowwe.core.Environment;
import de.knowwe.core.action.ActionDispatcher;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.wikiConnector.ConnectorAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;

public class DummyConnector implements WikiConnector {

	private DummyPageProvider dummyPageProvider = null;

	public DummyConnector() {
	}

	public DummyConnector(DummyPageProvider dummyPageProvider) {
		this.dummyPageProvider = dummyPageProvider;
	}

	@Override
	public String appendContentToPage(String topic, String pageContent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createWikiPage(String topic, String newContent, String author) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean doesPageExist(String Topic) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ActionDispatcher getActionDispatcher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getAllActiveUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getAllArticles(String web) {
		if (dummyPageProvider == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"PageProvider not given, so there are no articles available");

			return null;
		}
		return dummyPageProvider.getAllArticles();
	}

	@Override
	public String[] getAllUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getApplicationRootPath() {
		return new File("").getAbsolutePath();
	}

	@Override
	public String getArticleSource(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getArticleSource(String name, int version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConnectorAttachment getAttachment(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAttachmentFilenamesForPage(String pageName) {
		return new ArrayList<String>();
	}

	@Override
	public Collection<ConnectorAttachment> getAttachments() {
		if (dummyPageProvider == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"PageProvider not given, so there are no attachments available");
			return null;
		}
		return new ArrayList<ConnectorAttachment>(dummyPageProvider.getAllAttachments().values());
	}

	@Override
	public String getAuthor(String name, int version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBaseUrl() {
		return "http://valid_base_url/";
	}

	/**
	 * This returns a path, that enables the use of this connector in tests of
	 * projects.
	 * 
	 * @author Sebastian Furth
	 * @return relative Path to KnowWEExtensions
	 */
	@Override
	public String getKnowWEExtensionPath() {
		String hackedPath = System.getProperty("user.dir");
		hackedPath = hackedPath.replaceAll("Research", "KnowWE");
		if (hackedPath.contains("KnowWE-App")) {
			hackedPath = hackedPath.replaceAll("KnowWE-App", "KnowWE");
		}
		else {
			hackedPath += "/..";
		}
		hackedPath += "/KnowWE-Resources/src/main/webapp/KnowWEExtension/";
		File file = new File(hackedPath);
		try {
			return file.getCanonicalPath();
		}
		catch (IOException e) {
			throw new Error(e);
		}
	}

	@Override
	public Date getLastModifiedDate(String name, int version) {
		return null;
	}

	@Override
	public Locale getLocale() {
		return Locale.CANADA_FRENCH;
	}

	@Override
	public Locale getLocale(HttpServletRequest request) {
		return Locale.CANADA_FRENCH;
	}

	@Override
	public Map<Integer, Date> getModificationHistory(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealPath() {
		return "some-path";
	}

	@Override
	public String getSavePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getVersion(String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map<String, Integer> getVersionCounts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPageLocked(String articlename) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPageLockedCurrentUser(String articlename, String user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String normalizeStringTo(String string) {
		// TODO Auto-generated method stub
		return string;
	}

	@Override
	public String renderWikiSyntax(String pagedata, UserActionContext userContext) {
		return null;
	}

	@Override
	public boolean setPageLocked(String articlename, String user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean storeAttachment(String wikiPage, String user, File attachmentFile) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean storeAttachment(String wikiPage, String filename, String user, InputStream stream) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void undoPageLocked(String articlename) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean userCanEditPage(String articlename) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean userCanEditPage(String articlename, HttpServletRequest r) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean userCanViewPage(String articlename) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean userCanViewPage(String articlename, HttpServletRequest r) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean userIsMemberOfGroup(String username, String groupname, HttpServletRequest r) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String wikiSyntaxToHtml(String syntax) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean writeArticleToWikiEnginePersistence(String title, String content, UserContext context) {

		// This is only needed for the test environment. In the running
		// wiki, this is automatically called after the change to the
		// persistence.
		Environment.getInstance().buildAndRegisterArticle(content, title, context.getWeb());
		if (dummyPageProvider != null) {
			dummyPageProvider.setArticleContent(title, content);
		}
		return true;
	}

}
