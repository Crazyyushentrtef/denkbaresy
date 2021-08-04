package de.d3web.we.core;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.tagging.Tags;
import de.d3web.we.kdom.tagging.TagsContent;
import de.d3web.we.module.semantic.owl.UpperOntology;

/**
 * Centralised management of tags. Takes care of adding/removing tags. And
 * answers tag-queries.
 * 
 * @author Fabian Haupt
 * 
 */
public class TaggingMangler {

	private static TaggingMangler me;

	private TaggingMangler() {

	}

	public static synchronized TaggingMangler getInstance() {
		if (me == null) {
			me = new TaggingMangler();
		}
		return me;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * adds a tag to a page. The new tag is added into the <tags></tags> part.
	 * If there is none, it's created at the end of the page
	 * 
	 * @param pagename
	 * @param tag
	 */
	public void addTag(String pagename, String tag, KnowWEParameterMap params) {
		KnowWEEnvironment ke = KnowWEEnvironment.getInstance();
		KnowWEArticle article = ke.getArticle(KnowWEEnvironment.DEFAULT_WEB,
				pagename);
		ArrayList<Section> tagslist = new ArrayList<Section>();
		article.getSection().findSuccessorsOfType(TagsContent.class, tagslist);
		HashSet<String> tags = new HashSet<String>();
		if (tagslist.size() > 0) {
			boolean multiple = tagslist.size() > 1;
			for (Section cur : tagslist) {
				for (String temptag : cur.getOriginalText().split(" |,")) {
					tags.add(temptag.trim());
				}
			}
			if (!tags.contains(tag)) {
				tags.add(tag);
			}
			String output = "";
			for (String temptag : tags) {
				output += temptag + " ";
			}
			Section keep = tagslist.get(0);
			if (multiple) {
				for (int i = 1; i < tagslist.size(); i++) {
					article.getSection().removeChild(tagslist.get(i));
				}
			}
			ke.getArticleManager(KnowWEEnvironment.DEFAULT_WEB)
					.replaceKDOMNode(params, pagename, keep.getId(), output);
		} else {
			addNewTagSection(pagename,tag,params);
		}
	}

	/**
	 * removes a tag from a page. <tags></tags> is checked first. If the tag is
	 * not found there, the page is checked for inline annotations.
	 * 
	 * @param pagename
	 * @param tag
	 */
	public void removeTag(String pagename, String tag, KnowWEParameterMap params) {
		KnowWEEnvironment ke = KnowWEEnvironment.getInstance();
		KnowWEArticle article = ke.getArticle(KnowWEEnvironment.DEFAULT_WEB,
				pagename);
		ArrayList<Section> tagslist = new ArrayList<Section>();
		article.getSection().findSuccessorsOfType(TagsContent.class, tagslist);
		HashSet<String> tags = new HashSet<String>();
		boolean multiple = tagslist.size() > 1;
		for (Section cur : tagslist) {
			for (String temptag : cur.getOriginalText().split(" |,")) {
				tags.add(temptag.trim());
			}
		}
		if (!tags.contains(tag)) {
			tags.add(tag);
		}
		String output = "";
		for (String temptag : tags) {
			if (!temptag.equals(tag))
				output += temptag + " ";
		}
		Section keep = tagslist.get(0);
		if (multiple) {
			for (int i = 1; i < tagslist.size(); i++) {
				article.getSection().removeChild(tagslist.get(i));
			}
		}
		ke.getArticleManager(KnowWEEnvironment.DEFAULT_WEB).replaceKDOMNode(
				params, pagename, keep.getId(), output);
	}

	/**
	 * returns a list of pages that are tagged with the given tag.
	 * 
	 * @param tag
	 * @return
	 */
	public ArrayList<String> getPages(String tag) {
		String querystring = "SELECT ?q \n" + "WHERE {\n" + "?t rdf:object \""
				+ tag + "\" .\n" + "?t rdf:predicate ns:hasTag .\n"
				+ "?t rdfs:isDefinedBy ?o .\n" + "?o ns:hasTopic ?q .\n" + "}";
		return SemanticCore.getInstance().simpleQueryToList(querystring, "q");
	}

	/**
	 * Creates a list of tags this page is associated with. Always returns a
	 * list. Any errors result in an empty list.
	 * 
	 * @param pagename
	 *            the topic in question
	 * @return a list a tags for this topic
	 */
	public ArrayList<String> getPageTags(String pagename) {
		String topicenc = pagename;
		try {
			topicenc = URLEncoder.encode(pagename, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String querystring = "SELECT ?q \n" + "WHERE {\n"
				+ "?t rdf:object ?q .\n" + "?t rdf:predicate lns:hasTag .\n"
				+ "?t rdfs:isDefinedBy ?o .\n" + "?o ns:hasTopic \"" + topicenc
				+ "\" .\n" + "}";
		return SemanticCore.getInstance().simpleQueryToList(querystring, "q");
	}

	/**
	 * returns a list of all existing tags
	 * 
	 * @return
	 */
	public ArrayList<String> getAllTags() {
		ArrayList<String> erg = new ArrayList<String>();
		for (String cur : getAllTagsWithDuplicates())
			if (!erg.contains(cur))
				erg.add(cur);
		return erg;
	}

	/**
	 * returns a hashmap of the tags and an integer, that can be used as
	 * font-size (scaled between minSize and maxSize)
	 * 
	 * @param minSize
	 * @param maxSize
	 * @return
	 */
	public HashMap<String, Integer> getCloudList(int minSize, int maxSize) {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		HashMap<String, Float> weighted = getAllTagsWithWeight();
		float factor = maxSize - minSize;
		for (Entry<String, Float> cur : weighted.entrySet()) {
			result.put(cur.getKey(), Math.round(minSize
					+ (cur.getValue() * factor)));
		}
		return result;
	}

	/**
	 * returns a list of all existing tags with normalized weights
	 * 
	 * @return
	 */
	public HashMap<String, Float> getAllTagsWithWeight() {
		ArrayList<String> tags = getAllTagsWithDuplicates();
		HashMap<String, Float> countlist = new HashMap<String, Float>();
		float max = 0;
		for (String cur : tags) {
			if (countlist.get(cur) == null)
				countlist.put(cur, new Float(0));
			float c = countlist.get(cur) + 1;
			countlist.put(cur, c);
			max = c > max ? c : max;
		}
		HashMap<String, Float> weighted = new HashMap<String, Float>();
		for (Entry<String, Float> cur : countlist.entrySet()) {
			weighted.put(cur.getKey(), cur.getValue() / max);
		}
		return weighted;
	}

	private ArrayList<String> getAllTagsWithDuplicates() {
		String querystring = "SELECT ?q \n" + "WHERE {\n"
				+ "?t rdf:object ?q .\n" + "?t rdf:predicate lns:hasTag .\n"
				+ "}";
		return SemanticCore.getInstance().simpleQueryToList(querystring, "q");

	}

	// /**
	// * clears all tags from a topic
	// *
	// * @param topic
	// */
	// public void clearAllTags(String topic) {
	// // i know .. slow and such..find some time to do this better
	// for (String cur : getPageTags(topic))
	// removeTag(topic, cur);
	//
	// }

	/**
	 * sets tags to tag and replaces old ones
	 * 
	 * @param topic
	 * @param tag
	 *            comma/space separated list of tags
	 */
	public void setTags(String topic, String tag, KnowWEParameterMap params) {
		KnowWEEnvironment ke = KnowWEEnvironment.getInstance();
		KnowWEArticle article = ke.getArticle(KnowWEEnvironment.DEFAULT_WEB,
				topic);
		ArrayList<Section> tagslist = new ArrayList<Section>();
		article.getSection().findSuccessorsOfType(TagsContent.class, tagslist);
		boolean multiple = tagslist.size() > 1;
		String output = "";
		for (String temptag : tag.split(" |,")) {
			output += temptag + " ";
		}

		if (tagslist.size() > 0) {
			Section keep = tagslist.get(0);
			if (multiple) {
				for (int i = 1; i < tagslist.size(); i++) {
					article.getSection().removeChild(tagslist.get(i));
				}
			}
			ke.getArticleManager(KnowWEEnvironment.DEFAULT_WEB)
					.replaceKDOMNode(params, topic, keep.getId(), output);
		}else {
			addNewTagSection(topic,output,params);
		}
	}

	/**
	 * adds a new tags-section - the hardcore way
	 * 
	 */
	public void addNewTagSection(String topic,String content,KnowWEParameterMap params){
		KnowWEEnvironment ke = KnowWEEnvironment.getInstance();
		KnowWEArticle article = ke.getArticle(KnowWEEnvironment.DEFAULT_WEB,
				topic);
		Section asection=article.getSection();
		String text=asection.getOriginalText();
		text+="<tags>"+content+"</tags>";
		ke.getArticleManager(KnowWEEnvironment.DEFAULT_WEB)
		.replaceKDOMNode(params, topic, asection.getId(), text);
	}
} 
