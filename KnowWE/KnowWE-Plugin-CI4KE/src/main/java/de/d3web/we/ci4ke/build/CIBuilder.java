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

package de.d3web.we.ci4ke.build;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.io.progress.AjaxProgressListenerImpl;
import de.d3web.core.io.progress.ProgressListener;
import de.d3web.core.io.progress.ProgressListenerManager;
import de.d3web.testing.BuildResult;
import de.d3web.testing.TestExecutor;
import de.d3web.testing.TestObjectProvider;
import de.d3web.testing.TestObjectProviderManager;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.d3web.we.ci4ke.hook.CIHook;
import de.d3web.we.ci4ke.util.CIUtils;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CIBuilder {

	public static final String ACTUAL_BUILD_STATUS = "actualBuildStatus";
	public static final String BUILD_RESULT = "result";

	private final CIConfig config;
	private final CIDashboard dashboard;

	/**
	 * This constructor searches only the given dashboardArticle for dashboard
	 * with the given ID
	 * 
	 * @param dashboardArticleTitle
	 * @param dashboardName
	 */
	public CIBuilder(String web, String dashboardArticleTitle, String dashboardName) {
		this.dashboard = CIDashboard.getDashboard(web, dashboardArticleTitle, dashboardName);
		Article dashboardArticle = Environment.getInstance().getArticleManager(
				Environment.DEFAULT_WEB).getArticle(dashboardArticleTitle);
		Section<CIDashboardType> sec = CIUtils.
				findCIDashboardSection(dashboardArticleTitle, dashboardName);
		if (sec == null) {
			throw new IllegalArgumentException("No dashboard " +
					"with the given Name found on this article!!");
		}
		this.config = (CIConfig) KnowWEUtils.getStoredObject(dashboardArticle, sec,
				CIConfig.CICONFIG_STORE_KEY);

	}

	/**
	 * Convenience Constructor
	 * 
	 * @param hook
	 */
	public CIBuilder(CIHook hook) {
		this(hook.getWeb(), hook.getDashboardArticleTitle(), hook.getDashboardName());
	}

	/**
	 * This is the main method of a ci builder, which executes a new Build. -
	 * Therefore the TestExecutor is used. Adds the resultset to dashboard.
	 */
	public void executeBuild() {
		if (config == null) return;
		String dashboardName = dashboard.getDashboardName();

		// terminate current build (if one is running)
		CIUtils.deregisterAndTerminateBuildExecutor(dashboardName);

		List<TestObjectProvider> providers = new ArrayList<TestObjectProvider>();
		providers.add(DefaultWikiTestObjectProvider.getInstance());
		List<TestObjectProvider> pluggedProviders = TestObjectProviderManager.getTestObjectProviders();
		providers.addAll(pluggedProviders);

		ProgressListener listener = new AjaxProgressListenerImpl();
		ProgressListenerManager.getInstance().setProgressListener(dashboardName,
				listener);

		// create and run TestExecutor
		TestExecutor executor = new TestExecutor(providers, this.config.getTestSpecifications(),
				listener);

		CIUtils.registerBuildExecutor(dashboardName, executor);
		executor.run();

		BuildResult build = executor.getBuildResult();
		// set verbose persistence flag, will be considered by persistence
		boolean verbosePersistence = lookUpVerboseFlag();
		build.setVerbosePersistence(verbosePersistence);

		// add resulting build to dashboard
		if (build != null && !Thread.interrupted()) {
			dashboard.addNewBuild(build);
		}
		ProgressListenerManager.getInstance().removeProgressListener(dashboardName);
		CIUtils.deregisterAndTerminateBuildExecutor(dashboardName);
	}

	/**
	 * 
	 * @created 13.05.2013
	 * @param dashboard2
	 * @return
	 */
	private boolean lookUpVerboseFlag() {
		Section<CIDashboardType> ciDashboardSection = CIUtils.findCIDashboardSection(
				dashboard.getDashboardArticle(), dashboard.getDashboardName());
		String flagString = DefaultMarkupType.getAnnotation(ciDashboardSection,
				CIDashboardType.VERBOSE_PERSISTENCE_KEY);
		if (flagString != null && flagString.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}

}
