package de.d3web.tirex.core;

import de.d3web.kernel.domainModel.IDObject;
import de.d3web.tirex.core.extractionStrategies.ExtractionStrategy;

/**
 * An auxiliary class to hold a String matched in a wiki-page and the strategy
 * that it was matched with.
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public class OriginalMatchAndStrategy {

	/**
	 * The knowledge-String, that the match was extracted from
	 */
	private String knowledge;

	/**
	 * The "original" question or answer, as it is suggested by the knowledge
	 * base
	 */
	private IDObject idObject;

	/**
	 * The match, which was found in the wiki-page
	 */
	private String match;

	/**
	 * The strategy, that the match was extracted with
	 */
	private ExtractionStrategy strategy;

	/**
	 * The rating of the match.
	 */
	private double rating;

	public OriginalMatchAndStrategy(String knowledge, IDObject original,
			String match, ExtractionStrategy strategy) {

		this.knowledge = knowledge;
		this.idObject = original;
		this.match = match;
		this.strategy = strategy;

		// necessary because for extraction strategies which operate with edit
		// distances the matches are sometimes longer than the idObject
		double normalizedLength = (match.replaceAll("\r", "").length() <= idObject
				.toString().replaceAll("\r", "").length()) ? match.replaceAll(
				"\r", "").length() : idObject.toString().replaceAll("\r", "")
				.length();

		rating = getStrategy().getRating()
				* ((Math.pow(normalizedLength, 2)) / (double) idObject
						.toString().replaceAll("\r", "").length());
	}

	public String getKnowledge() {
		return knowledge;
	}

	public IDObject getIDObject() {
		return idObject;
	}

	public String getMatch() {
		return match;
	}

	public ExtractionStrategy getStrategy() {
		return strategy;
	}

	public double getRating() {
		return rating;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("Knowledge: " + getKnowledge() + "\n");
		sb.append("IDObject: " + getIDObject() + "\n");
		sb.append("Class: " + getIDObject().getClass() + "\n");
		sb.append("Match: " + getMatch() + "\n");
		sb.append("Strategy: " + getStrategy().getName() + "\n");
		sb.append("Rating: " + getRating() + "\n\n");

		return sb.toString();
	}
}
