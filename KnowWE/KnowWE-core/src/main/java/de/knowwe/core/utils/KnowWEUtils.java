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

package de.knowwe.core.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TermRegistrationScope;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.SimpleTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.SectionStore;
import de.knowwe.core.wikiConnector.ConnectorAttachment;

public class KnowWEUtils {

	public static void appendToFile(String path, String entry) {

		try {
			FileWriter fstream = new FileWriter(path, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(entry);
			out.close();
		}
		catch (Exception e) {
			Logger.getLogger(KnowWEUtils.class.getName()).log(
					Level.WARNING, "Unable to append to File: " + e.getMessage());
		}
	}

	public static String convertUmlaut(String text) {
		if (text == null) return null;
		String result = text;
		result = result.replaceAll("Ä", "&Auml;");
		result = result.replaceAll("Ö", "&Ouml;");
		result = result.replaceAll("Ü", "&Uuml;");
		result = result.replaceAll("ä", "&auml;");
		result = result.replaceAll("ö", "&ouml;");
		result = result.replaceAll("ü", "&uuml;");
		result = result.replaceAll("ß", "&szlig;");
		return result;
	}

	/**
	 * Escapes the given string for safely using user-input in web sites.
	 * 
	 * @param text Text to escape
	 * @return Sanitized text
	 */
	public static String escapeHTML(String text) {
		if (text == null) return null;

		return text.replaceAll("&", "&amp;").
				replaceAll("\"", "&quot;").
				replaceAll("'", "&#x27;").
				replaceAll("<", "&lt;").
				replaceAll(">", "&gt;");
	}

	/**
	 * Creates a unique anchor name for the section to link to. See method
	 * {@link #getWikiLink(Section)} for more details on how to use this method.
	 * 
	 * @param section the section to create the anchor for.
	 * @return the unique anchor name
	 */
	public static String getAnchor(Section<?> section) {
		// TODO: figure out how JSPWiki builds section anchor names
		return "section-"
				+ section.getArticle().getTitle().replace(' ', '+') + "-"
				+ Math.abs(section.getID().hashCode());
	}

	/**
	 * Returns the ConnectorAttachment for a specified filename on a specified
	 * wikipage
	 * 
	 * @created 27.01.2012
	 * @param title Title of the wikipage
	 * @param fileName filename of the attachment
	 * @return {@link ConnectorAttachment} fulfilling the specified parameters
	 *         or null, if no such attachment exists
	 */
	public static ConnectorAttachment getAttachment(String title, String fileName) {
		Collection<ConnectorAttachment> attachments = Environment.getInstance().getWikiConnector().getAttachments();
		ConnectorAttachment actualAttachment = null;
		for (ConnectorAttachment attachment : attachments) {
			if ((attachment.getFileName().equals(fileName)
					&& attachment.getParentName().equals(title))
					|| attachment.getFullName().equals(fileName)) {
				actualAttachment = attachment;
				break;
			}
		}
		return actualAttachment;
	}

	/**
	 * Returns all {@link ConnectorAttachment}s which full name fits to the
	 * regex or which filename matches to the regexp and which parent has the
	 * specified topic
	 * 
	 * @created 09.02.2012
	 * @param regex regular expression the attachments should match to
	 * @param topic Topic of the article
	 * @return Collection of {@link ConnectorAttachment}s
	 */
	public static Collection<ConnectorAttachment> getAttachments(String regex, String topic) {
		Collection<ConnectorAttachment> result = new LinkedList<ConnectorAttachment>();
		Collection<ConnectorAttachment> attachments = Environment.getInstance().getWikiConnector().getAttachments();
		Pattern pattern = Pattern.compile(regex);
		for (ConnectorAttachment attachment : attachments) {
			if (pattern.matcher(attachment.getFullName()).matches()
						|| (pattern.matcher(attachment.getFileName()).matches() && attachment.getParentName().equals(
								topic))) {
				result.add(attachment);
			}
		}
		return result;
	}

	/**
	 * Returns all master articles that compile the given Section. If no master
	 * article compiles the Section, at least the article of the Section itself
	 * is returned, so the Collection always at least contains one article.
	 * 
	 * @created 16.02.2012
	 * @param section is the Section for which you want to know the compiling
	 *        articles
	 * @return a non empty Collection of articles that compile the given Section
	 */
	public static Collection<Article> getCompilingArticles(Section<?> section) {
		Collection<Article> articles = new ArrayList<Article>();
		Environment env = Environment.getInstance();
		Set<String> referingArticleTitles = env.getPackageManager(section.getWeb()).getCompilingArticles(
				section);
		ArticleManager articleManager = env.getArticleManager(section.getWeb());
		for (String title : referingArticleTitles) {
			Article article =
					Article.getCurrentlyBuildingArticle(section.getWeb(), title);
			if (article == null) article = articleManager.getArticle(title);
			if (article == null) continue;
			articles.add(article);
		}
		if (articles.isEmpty()) articles.add(section.getArticle());
		return articles;
	}

	public static ResourceBundle getConfigBundle() {
		return ResourceBundle.getBundle("KnowWE_config");
	}

	public static String getErrorQ404(String question, String text) {
		String rendering = "<span class=\"semLink\"><a href=\"#\" title=\""
				+ "Question not found:"
				+ question
				+ "\" >"
				+ text
				+ "</a></span>";
		return rendering;

	}

	/**
	 * @return the {@link TerminologyManager} that handles global terms for this
	 *         web (similar to the former {@link TermRegistrationScope#GLOBAL}).
	 */
	public static TerminologyManager getGlobalTerminologyManager(String web) {
		return Environment.getInstance().getTerminologyManager(web, null);
	}

	// public static String repairUmlauts(String s) {
	// // then replace special characters
	// s = s.replaceAll("&szlig;","ß");
	// s = s.replaceAll("&auml;","ä");
	// s = s.replaceAll("&uuml;","ü");
	// s = s.replaceAll("&ouml;","ö");
	// s = s.replaceAll("&Auml;","Ä");
	// s = s.replaceAll("&Uuml;","Ü");
	// s = s.replaceAll("&Ouml;","Ö");
	// s = s.replaceAll("&deg;","°");
	// s = s.replaceAll("&micro;","µ");
	// s = s.replaceAll("&apos;", "'");
	// return(s);
	// }

	public static String getKnowWEExtensionPath() {
		return Environment.getInstance().getWikiConnector().getKnowWEExtensionPath();
	}

	public static String getApplicationRootPath() {
		return Environment.getInstance().getWikiConnector().getApplicationRootPath();
	}

	public static String getPageChangeLogPath() {
		return getVersionsSavePath() + "PageChangeLog.txt";
	}

	public static String getRealPath(String varPath) {
		if (varPath.contains("$root_path$")) {
			String rootPath = Environment.getInstance().getWikiConnector()
					.getApplicationRootPath();
			rootPath = rootPath.replace('\\', '/');
			rootPath = rootPath.replaceAll("/+$", "");
			varPath = varPath.replace("$root_path$", rootPath);
		}
		return varPath;
	}

	public static Object getStoredObject(Article article, Section<?> s, String key) {
		return s.getSectionStore().getObject(article, key);
	}

	public static Object getStoredObject(Section<?> s, String key) {
		return getStoredObject(null, s, key);
	}

	/**
	 * @created 08.02.2012
	 * @param termSection the Section which should implement the interface
	 *        SimpleTerm
	 * @returns the term identifier if the given Section has the type
	 *          SimpleTerm, the text of the Section else
	 */
	public static String getTermIdentifier(Section<?> termSection) {
		String termIdentifier = termSection.getText();
		if (termSection.get() instanceof SimpleTerm) {
			@SuppressWarnings("unchecked")
			Section<? extends SimpleTerm> simpleSection = (Section<? extends SimpleTerm>) termSection;
			termIdentifier = simpleSection.get().getTermIdentifier(simpleSection);
		}
		return termIdentifier;
	}

	/**
	 * @return the {@link TerminologyManager} for the given (master) article.
	 */
	public static TerminologyManager getTerminologyManager(Article article) {
		String web = article == null ? Environment.DEFAULT_WEB : article.getWeb();
		String title = article == null ? null : article.getTitle();
		return Environment.getInstance().getTerminologyManager(web, title);
	}

	public static TerminologyManager getTerminologyManager(Article article, TermRegistrationScope scope) {
		TerminologyManager tHandler;
		if (scope == TermRegistrationScope.GLOBAL) {
			tHandler = getGlobalTerminologyManager(article.getWeb());
		}
		else {
			tHandler = getTerminologyManager(article);
		}
		return tHandler;
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to the specified article.
	 * 
	 * @param article the article to create the link for
	 * @return the created link
	 * @see #getURLLink(Section)
	 * @see #getWikiLink(Section)
	 */
	public static String getURLLink(Article article) {
		return "Wiki.jsp?page=" + article.getTitle();
	}

	/**
	 * Creates a &lt;a href="..."&gt; styled link to this section. The created
	 * link navigates the user to the article of the section. If the section is
	 * rendered with an anchor (see method {@link #getAnchor(Section)}) the page
	 * is also scrolled to the section.
	 * 
	 * @param section the section to create the link for
	 * @return the created link
	 * @see #getURLLink(Article)
	 * @see #getWikiLink(Section)
	 */
	public static String getURLLink(Section<?> section) {
		return "Wiki.jsp?page=" + section.getTitle() + "#" + getAnchor(section);
	}

	public static String getVersionsSavePath() {
		String path = Environment.getInstance().getWikiConnector().getSavePath();
		if (path != null && !path.endsWith(File.pathSeparator)) path += File.separator;
		path += "OLD/";
		return path;
	}

	/**
	 * Creates a wiki-markup-styled link to this section. The created link
	 * navigates the user to the article of the section. If the section is
	 * rendered with an anchor (see method {@link #getAnchor(Section)}) the page
	 * is also scrolled to the section.
	 * <p>
	 * Please not that the link will only work if it is put into "[" ... "]"
	 * brackets and rendered through the wiki rendering pipeline.
	 * 
	 * @param section the section to create the link for
	 * @return the created link
	 * @see #getURLLink(Section)
	 * @see #getURLLink(Article)
	 */
	public static String getWikiLink(Section<?> section) {
		return section.getTitle() + "#" + Math.abs(section.getID().hashCode());
	}

	/**
	 * returns whether a text contains nothing except spaces and newlines
	 * 
	 * @param text
	 * @return
	 */
	public static boolean isEmpty(String text) {
		if (text == null) return true;
		if (text.length() == 0) return true;
		text = text.replaceAll("\r", "");
		text = text.replaceAll("\n", "");
		text = text.replaceAll(" ", "");

		if (text.length() == 0) return true;

		return false;

	}

	private static void mask(StringBuilder buffer, String toReplace) {
		int index = buffer.indexOf(toReplace);
		while (index >= 0) {
			// string starts with substring which should be replaced
			// or the char before the substring is not ~
			if (index == 0 || !buffer.substring(index - 1, index).equals("~")) {
				buffer.replace(index, index + toReplace.length(), "~" + toReplace);
			}
			index = buffer.indexOf(toReplace, index + 1);
		}
	}

	/**
	 * 
	 * masks output strings
	 * 
	 * @param htmlContent
	 * @return
	 */
	public static String maskHTML(String htmlContent) {
		htmlContent = htmlContent.replaceAll("\\[\\{",
				Environment.HTML_PLUGIN_BRACKETS_OPEN);
		htmlContent = htmlContent.replaceAll("}]",
				Environment.HTML_PLUGIN_BRACKETS_CLOSE);

		htmlContent = htmlContent.replaceAll("\"",
				Environment.HTML_DOUBLEQUOTE);
		htmlContent = htmlContent.replaceAll("'", Environment.HTML_QUOTE);
		htmlContent = htmlContent.replaceAll(">", Environment.HTML_GT);
		htmlContent = htmlContent.replaceAll("<", Environment.HTML_ST);

		htmlContent = htmlContent.replace("[",
				Environment.HTML_BRACKET_OPEN);
		htmlContent = htmlContent.replace("]",
				Environment.HTML_BRACKET_CLOSE);
		// htmlContent = htmlContent.replace("{",
		// Environment.HTML_CURLY_BRACKET_OPEN);
		// htmlContent = htmlContent.replace("}",
		// Environment.HTML_CURLY_BRACKET_CLOSE);
		return htmlContent;
	}

	/**
	 * Masks [, ], ----, {{{, }}} and %% so that JSPWiki will render and not
	 * interpret them, if the characters are already escaped, it will do nothing
	 * 
	 * @created 03.03.2011
	 */
	public static String maskJSPWikiMarkup(String string) {
		StringBuilder temp = new StringBuilder(string);
		maskJSPWikiMarkup(temp);
		return temp.toString();
	}

	/**
	 * Masks [, ], ----, {{{, }}} and %% so that JSPWiki will render and not
	 * interpret them, if the characters are already escaped, it will do nothing
	 * 
	 * @created 03.03.2011
	 * @param builder
	 */
	public static void maskJSPWikiMarkup(StringBuilder builder) {
		mask(builder, "[");
		mask(builder, "]");
		mask(builder, "----");
		mask(builder, "{{{");
		mask(builder, "}}}");
		mask(builder, "%%");
		mask(builder, "\\");
	}

	public static String maskNewline(String htmlContent) {
		htmlContent = htmlContent.replace("\n", Environment.NEWLINE);
		return htmlContent;
	}

	public static String replaceUmlaut(String text) {
		String result = text;
		result = result.replaceAll("Ä", "AE");
		result = result.replaceAll("Ö", "OE");
		result = result.replaceAll("Ü", "UE");
		result = result.replaceAll("ä", "ae");
		result = result.replaceAll("ö", "oe");
		result = result.replaceAll("ü", "ue");
		result = result.replaceAll("ß", "ss");
		return result;
	}

	/**
	 * Do not use this method anymore, use
	 * {@link SectionStore#storeObject(String, Object)} or
	 * {@link SectionStore#storeObject(Article, String, Object)} instead. Use
	 * {@link Section#getSectionStore()} to get the right {@link SectionStore}.
	 * 
	 * @created 08.07.2011
	 * @param article is the article you want to store the Object for... if the
	 *        Object is relevant for all articles, you can set the argument to
	 *        null
	 * @param s is the {@link Section} you want to store the object for
	 * @param key is key used to store and retrieve the Object
	 * @param o is the Object to store
	 */
	public static void storeObject(Article article, Section<?> s, String key, Object o) {
		s.getSectionStore().storeObject(article, key, o);
	}

	public static String trimQuotes(String text) {

		if (text == null) return null;

		String trimmed = text.trim();

		if (trimmed.equals("\"")) return "";

		if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
			trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
			// unmask "
			return trimmed.replace("\\\"", "\"");
		}

		return trimmed;
	}

	/**
	 * 
	 * Unmasks output strings
	 * 
	 * @param htmlContent
	 * @return
	 */
	public static String unmaskHTML(String htmlContent) {
		htmlContent = htmlContent.replaceAll(
				Environment.HTML_PLUGIN_BRACKETS_OPEN, "\\[\\{");
		htmlContent = htmlContent.replaceAll(
				Environment.HTML_PLUGIN_BRACKETS_CLOSE, "}]");
		htmlContent = htmlContent.replaceAll(
				Environment.HTML_DOUBLEQUOTE, "\"");
		htmlContent = htmlContent
				.replaceAll(Environment.HTML_QUOTE, "\'");
		htmlContent = htmlContent.replaceAll(Environment.HTML_GT, ">");
		htmlContent = htmlContent.replaceAll(Environment.HTML_ST, "<");

		htmlContent = htmlContent.replaceAll(
				Environment.HTML_BRACKET_OPEN, "[");
		htmlContent = htmlContent.replaceAll(
				Environment.HTML_BRACKET_CLOSE, "]");
		// htmlContent = htmlContent.replace(
		// Environment.HTML_CURLY_BRACKET_OPEN, "{");
		// htmlContent = htmlContent.replace(
		// Environment.HTML_CURLY_BRACKET_CLOSE, "}");

		return htmlContent;
	}

	public static String unmaskNewline(String htmlContent) {
		htmlContent = htmlContent.replace(Environment.NEWLINE, "\n");
		return htmlContent;
	}

	/**
	 * Performs URL decoding on the sting
	 * 
	 * @param text URLencoded string
	 * @return URLdecoded string
	 */
	@SuppressWarnings("deprecation")
	public static String urldecode(String text) {
		try {
			return URLDecoder.decode(text, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			return URLDecoder.decode(text);
		}
		catch (IllegalArgumentException e) {
			return text;
		}
	}

	/**
	 * Performs URL encoding on the sting
	 * 
	 * @param text
	 * @return URLencoded string
	 */
	@SuppressWarnings("deprecation")
	public static String urlencode(String text) {
		try {
			return URLEncoder.encode(text, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(text);
		}
	}

	public static String readFile(String fileName) {
		try {
			return readFile(new FileInputStream(fileName));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String readFile(InputStream inputStream) {
		StringBuffer inContent = new StringBuffer();
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream, "UTF-8"));
			int char1 = bufferedReader.read();
			while (char1 != -1) {
				inContent.append((char) char1);
				char1 = bufferedReader.read();
			}
			bufferedReader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return inContent.toString();
	}

	public static void writeFile(String path, String content) {

		try {
			FileWriter fstream = new FileWriter(path);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(content);
			out.close();
		}
		catch (Exception e) {
			Logger.getLogger(KnowWEUtils.class.getName()).log(
					Level.WARNING, "Unable to write File: " + e.getMessage());
		}
	}

}
