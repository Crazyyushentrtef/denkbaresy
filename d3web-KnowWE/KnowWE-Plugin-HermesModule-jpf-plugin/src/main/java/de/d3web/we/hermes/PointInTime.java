package de.d3web.we.hermes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PointInTime {

	private String encodedTime;

	private double interpretableTime;

	private String description;

	public PointInTime(String encodedTime) {
		this.encodedTime = encodedTime;
		if (encodedTime != null && !encodedTime.isEmpty()) {
			parseTimeString(encodedTime);
		}
	}

	public String getDescription() {
		return description;
	}

	public String getEncodedTime() {
		return encodedTime;
	}

	public double getInterpretableTime() {
		return interpretableTime;
	}

	private void parseTimeString(String timeS) {
		interpretableTime = -10000.0;
		description = "";
		if (timeS.contains("x")) description += "ca. ";
		if (timeS.contains("b")) description += "bis ";
		if (timeS.contains("e")) description += "seit ";

		if (timeS.contains("c")) description += "Anfang (des) ";
		if (timeS.contains("e")) description += "Ende (des) ";
		if (timeS.contains("m")) description += "Mitte (des) ";

		if (timeS.contains("p")) description += "nach ";
		if (timeS.contains("a")) description += "vor ";

		if (timeS.contains("s")) description += "Sommer ";
		if (timeS.contains("f")) description += "Frühling ";
		if (timeS.contains("h")) description += "Herbst ";
		if (timeS.contains("w")) description += "Winter ";

		// find year:
		Pattern p = Pattern.compile("[0-9]+");
		Matcher m = p.matcher(timeS);

		while (m.find()) {
			String digits = m.group();
			try {
				interpretableTime = Double.parseDouble(digits);
			}
			catch (NumberFormatException nfe) {
				// do nothing
			}
			description += digits + " ";
		}

		if (timeS.contains("j")) {
			description += ". Jhd. ";
			interpretableTime *= 100;
		}

		if (timeS.contains("v")) {
			interpretableTime *= -1;
			description += "v. Chr.";
		}
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEncodedTime(String encodedTime) {
		this.encodedTime = encodedTime;
	}

	public void setInterpretableTime(double interpretableTime) {
		this.interpretableTime = interpretableTime;
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
