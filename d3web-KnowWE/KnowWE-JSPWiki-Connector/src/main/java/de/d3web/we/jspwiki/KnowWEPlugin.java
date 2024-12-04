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

package de.d3web.we.jspwiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.event.WikiEvent;
import com.ecyrd.jspwiki.event.WikiEventListener;
import com.ecyrd.jspwiki.event.WikiEventUtils;
import com.ecyrd.jspwiki.event.WikiPageEvent;
import com.ecyrd.jspwiki.filters.BasicPageFilter;
import com.ecyrd.jspwiki.filters.FilterException;
import com.ecyrd.jspwiki.plugin.PluginException;
import com.ecyrd.jspwiki.plugin.WikiPlugin;
import com.ecyrd.jspwiki.providers.ProviderException;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEScriptLoader;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.module.PageAppendHandler;
import de.d3web.we.search.MultiSearchEngine;
import de.d3web.we.utils.KnowWEUtils;

public class KnowWEPlugin extends BasicPageFilter implements WikiPlugin, WikiEventListener {

	private String topicName = "";
	
    /**
	 * To initialize KnowWE, even if the directory for the pages is empty.
     */
    public void initialize( WikiEngine engine, Properties properties )
        throws FilterException {
    	
    	super.initialize(engine, properties);
        m_engine = engine;
        initKnowWEEnvironmentIfNeeded(engine);
        
        File f = new File(KnowWEEnvironment.getInstance().getKnowWEExtensionPath());
        f = f.getParentFile();

        try {
            for (File s : f.listFiles()) {
            	if (s.getName().equals("WEB-INF")) {        		
					BufferedReader in = new BufferedReader(new FileReader(s.getPath()
							+ "/jspwiki.properties"));
					String zeile = null;
					File pagedir = null;
					while ((zeile = in.readLine()) != null) {
						if (!zeile.contains("#") && zeile.contains("jspwiki.fileSystemProvider.pageDir")) {
							zeile = zeile.trim();
							zeile = zeile.substring(zeile.lastIndexOf(" ") + 1);
							pagedir = new File(zeile);
							in.close();
							break;
						}
					}
					
					if (pagedir.exists()) {
						File [] files = pagedir.listFiles();						
						File coreDir = new File(s.getPath() + "/resources/core-pages");
						File[] cores = coreDir.listFiles();
						for (File cP : coreDir.listFiles()) {
							if (!cP.getName().endsWith(".txt")) continue;
							File newFile = new File(pagedir.getPath()+ "/" + cP.getName());
							if (!newFile.exists())
								FileUtils.copyFile(cP, newFile);
							}
						}
            	}
            }
        } catch (Exception e) {
        	// Nothing to do. Start wiki without pages.
        }
        
		WikiEventUtils.addWikiEventListener(engine.getPageManager(), WikiPageEvent.PAGE_DELETE_REQUEST, this);

    }
	
	private void initKnowWEEnvironmentIfNeeded(WikiEngine wEngine) {
		if (!KnowWEEnvironment.isInitialized()) {
			KnowWEEnvironment.initKnowWE(new JSPWikiKnowWEConnector(wEngine));
			MultiSearchEngine.getInstance().addProvider(new JSPWikiSearchConnector());
		}
	}

	public String execute(WikiContext context, Map params)
			throws PluginException {
		String result = "tag not found or error";

		try {
			JSPWikiUserContext userContext = new JSPWikiUserContext(context,
					parseRequestVariables(context));

			String topic = context.getPage().getName();
			if (context.getCommand().getRequestContext().equals(
					WikiContext.VIEW)) {
				initKnowWEEnvironmentIfNeeded(context.getEngine());
				result = KnowWEEnvironment.getInstance().renderTags(params,
						topic, userContext, KnowWEEnvironment.DEFAULT_WEB);
			}
		} catch (Throwable t) {
			System.out.println("****Exception EXECUTE***");
			System.out.println("****Exception EXECUTE***");
			System.out.println("****Exception EXECUTE***");
			t.printStackTrace();
		}

		return result;
		// return "KnowWEplugin";
	}

	@Override
	public void postSave(WikiContext wikiContext, String content)
			throws FilterException {
		// setWikiContextAndEngine(wikiContext);

		// get the Page-name
		String topic = wikiContext.getPage().getName();
		String user = wikiContext.getWikiSession().getUserPrincipal().getName();

		initKnowWEEnvironmentIfNeeded(wikiContext.getEngine());

		// process this article in KnowWE
		KnowWEEnvironment.getInstance().processAndUpdateArticle(user, content,
				topic, "default_web");
	}

	@Override
	public String postTranslate(WikiContext wikiContext, String htmlContent)
			throws FilterException {

		try {

			htmlContent = KnowWEUtils.unmaskHTML(htmlContent);

			return htmlContent;
		} catch (Exception e) {
			System.out.println("****Exception in POST TRANSLATE***");
			System.out.println("****Exception in POST TRANSLATE***");
			System.out.println("****Exception in POST TRANSLATE***");
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public String preTranslate(WikiContext wikiContext, String content)
			throws FilterException {
		// setWikiContextAndEngine(wikiContext);

		if (!wikiContext.getCommand().getRequestContext().equals(
				WikiContext.VIEW)) {
			return content;
		}
		String pagedata = "";
		WikiEngine engine = wikiContext.getEngine();
		if (engine != null) {
			pagedata = engine.getPureText(wikiContext.getPage().getName(),
					wikiContext.getPage().getVersion());
			if (!content.equals(pagedata))
				return content;
		}
		try {

			/* creating KnowWEUserContext with username and requestParamteters */
			JSPWikiUserContext userContext = new JSPWikiUserContext(
					wikiContext, parseRequestVariables(wikiContext));

			topicName = wikiContext.getPage().getName();

			initKnowWEEnvironmentIfNeeded(engine);

			String newContent = "articleContent";

			KnowWEArticle article = KnowWEEnvironment.getInstance().getArticle(
					KnowWEEnvironment.DEFAULT_WEB, topicName);
			if (article != null) {
				String originalText = article.getSection().getOriginalText();
				String parse = userContext.getUrlParameterMap().get("parse");
				boolean fullParse = parse != null && (parse.equals("full") || parse.equals("true"));
				if ((fullParse/* && !article.isFullParse()*/) || !originalText.equals(content)) {
					article = new KnowWEArticle(content, topicName,
							KnowWEEnvironment.getInstance().getRootTypes(),
							KnowWEEnvironment.DEFAULT_WEB, fullParse);
					KnowWEEnvironment.getInstance().getArticleManager("default_web")
						.saveUpdatedArticle(article);
				}
			} else {
				article = new KnowWEArticle(content, topicName,
						KnowWEEnvironment.getInstance().getRootTypes(),
						KnowWEEnvironment.DEFAULT_WEB);
				if (pagedata.endsWith(content)) {
					// INITIALISATION PHASE: when page is first requested
					// after
					// server-start article is stored in manager
					KnowWEEnvironment.getInstance().getArticleManager(
							"default_web").saveUpdatedArticle(article);
				}
			}

			StringBuilder articleString = new StringBuilder();

			// long timeStart = System.currentTimeMillis();

			
			// Render Pre-PageAppendHandlers
			List<PageAppendHandler> ap = KnowWEEnvironment.getInstance().getAppendHandlers();
			for (PageAppendHandler pageAppendHandler : ap) {
				if (pageAppendHandler.isPre()) {
					articleString.append(pageAppendHandler.getDataToAppend(
							topicName, KnowWEEnvironment.DEFAULT_WEB,
							userContext));
				}
			}
			
			// RENDER PAGE
			article.getRenderer().render(article, article.getSection(), userContext, articleString);
			
			// Render Post-PageAppendHandlers
			for (PageAppendHandler pageAppendHandler : ap) {
				if (!pageAppendHandler.isPre()) {
					articleString.append(pageAppendHandler.getDataToAppend(
							topicName, KnowWEEnvironment.DEFAULT_WEB,
							userContext));
				}
			}
		
			KnowWEScriptLoader.getInstance().add("KnowWE.js", true);
			KnowWEScriptLoader.getInstance().add("KnowWE-helper.js", true);
			
			String jsModuleIncludes = KnowWEScriptLoader.getInstance().getIncludes();
			articleString.append( KnowWEUtils.maskHTML(jsModuleIncludes) );
				

			// long timeEnde = System.currentTimeMillis();

			// long time = timeEnde - timeStart;

			// double seconds = ((double) time) / 1000;

			// System.out.println("Rendered "+article.getTitle()
			// +" in "+seconds+" seconds");

			return articleString.toString();
		} catch (Exception e) {
			System.out.println("*****EXCEPTION IN preTranslate !!! *********");
			System.out.println("*****EXCEPTION IN preTranslate !!! *********");
			System.out.println("*****EXCEPTION IN preTranslate !!! *********");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parses the request variables (GET and POST) using a wiki context object.
	 * 
	 * @param context
	 *            WikiContext to be used
	 * @return A Map containing all request variables
	 */
	private Map<String, String> parseRequestVariables(WikiContext context) {
		HttpServletRequest req = context.getHttpRequest();

		Map<String, String> parameter = new HashMap<String, String>();

		if (req == null)
			return parameter;

		Enumeration p = req.getParameterNames();

		while (p.hasMoreElements()) {
			String param = (String) p.nextElement();
			try {
				parameter.put(param, URLDecoder.decode((String) req
						.getParameter(param), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				parameter.put(param, URLDecoder.decode((String) req
						.getParameter(param)));
			}
		}

		return parameter;
	}
	
	/**
	 * Handles events passed from JSPWiki
	 */
	@Override
	public void actionPerformed(WikiEvent event) {
		// When deleting a page, remove it from the ArticleManager and invalidate all knowledge
		if ((event instanceof WikiPageEvent) && (event.getType() == WikiPageEvent.PAGE_DELETE_REQUEST)) {
			WikiPageEvent e = (WikiPageEvent) event;
			
			KnowWEArticleManager amgr = KnowWEEnvironment.getInstance().getArticleManager(KnowWEEnvironment.DEFAULT_WEB);
			
			amgr.deleteArticle(amgr.getArticle(e.getPageName()));
		}
		
	}

}
