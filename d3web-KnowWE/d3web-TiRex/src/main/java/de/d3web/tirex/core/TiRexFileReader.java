package de.d3web.tirex.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.persistence.xml.PersistenceManager;
import de.d3web.persistence.xml.XCLModelPersistenceHandler;

/**
 * This singleton class contains the data and methods needed for TiRex to
 * function. A knowledgebase, a wiki-file, a file with the synonym-sets and one
 * with regular expressions can be set and loaded along with the TiRex-Settings
 * saved in a ResourceBundle.
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public class TiRexFileReader {

	/**
	 * The wiki-file (has to be explicitely set by another class just as the
	 * other files)
	 */
	private String wikiFile;

	/**
	 * The file that contains the knowledgebase
	 */
	private KnowledgeBase knowledgeBase;

	/**
	 * The file with the synonym sets
	 */
	private String synonymSetsFile;

	/**
	 * The file containing the regex-knoffice pairs
	 */
	private String regexKnofficePairsFile;

	/**
	 * The file containing the TiRex settings
	 */
	private String tiRexSettingsFile;

	/**
	 * The unique instance.
	 */
	private static TiRexFileReader instance;

	private TiRexFileReader() {
		// empty
	}

	/**
	 * @return The unique instance of the TiRexFileReader.
	 */
	public static TiRexFileReader getInstance() {
		if (instance == null) {
			instance = new TiRexFileReader();
		}

		return instance;
	}

	/**
	 * @param file
	 *            The File, from which the knowledgebase is to be read.
	 * @return The loaded knowledgebase.
	 * @throws MalformedURLException
	 */
	public KnowledgeBase loadKnowledgebase(File file)
			throws MalformedURLException {
		return loadKnowledgebase(file.toURI().toURL());
	}

	/**
	 * @param url
	 *            The link to the location of the knowledgebase.
	 * @return The loaded knowledgebase.
	 */
	public KnowledgeBase loadKnowledgebase(URL url) {
		PersistenceManager mgr = PersistenceManager.getInstance();
		mgr.addPersistenceHandler(new XCLModelPersistenceHandler());

		return mgr.load(url);
	}

	/**
	 * @param path
	 *            The path to the location of the knowledgebase.
	 * @return The loaded knowledgebase.
	 * @throws MalformedURLException
	 */
	public KnowledgeBase loadKnowledgebase(String path)
			throws MalformedURLException {
		URL url = new URL(path);

		return loadKnowledgebase(url);
	}

	public String getWikiFile() {
		return wikiFile;
	}

	public void setWikiFile(String wikiFile) {
		this.wikiFile = wikiFile;
	}

	public KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	public void setKnowledgeBase(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	public String getSynonymSetsFile() {
		return synonymSetsFile;
	}

	public void setSynonymSetsFile(String synonymSetsFile) {
		this.synonymSetsFile = synonymSetsFile;
	}

	public String getRegexKnofficePairsFile() {
		return regexKnofficePairsFile;
	}

	public void setRegexKnofficePairsFile(String regexKnofficePairsFile) {
		this.regexKnofficePairsFile = regexKnofficePairsFile;
	}

	public String getTiRexSettingsFile() {
		return regexKnofficePairsFile;
	}

	public void setTiRexSettingsFile(String tiRexSettingsFile) {
		this.tiRexSettingsFile = tiRexSettingsFile;
	}
}
