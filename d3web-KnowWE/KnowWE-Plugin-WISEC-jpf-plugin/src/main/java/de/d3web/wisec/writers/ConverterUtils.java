package de.d3web.wisec.writers;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jxl.Cell;

public class ConverterUtils {
	/**
	 * Removes "0" after comma, when available.
	 */
	public static String toShoppedString(String doubleValue) {
		if (doubleValue.endsWith(".0")) {
			doubleValue = doubleValue.substring(0, doubleValue.length()-2);
		}
		return doubleValue;
	}

	public static Writer createWriter(String filename) throws IOException {
		return createWriter(filename, "%%sup [[generated]/%");
	}

	public static Writer createWriter(String filename, String headerText) throws IOException {
		Writer w = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));
		// Writer w = new FileWriter(new File(filename));
		w.append(headerText+ "\n");
		return w;
	}

	public static String clean(String string) {
		if (string == null) {
			return "";
		}
		string = string.replaceAll("\\[", "[[");
		string = string.replaceAll("\\n", "\\\\");
		string = string.replaceAll("&", " and ");
		string = string.replaceAll("__", "_ _");
		// evil character, that is not displayed
		string = string.replaceAll("", " ");
		return string;
	}

	public static List<String> rowToStringArray(Cell[] row) {
		List<String> cells = new ArrayList<String>(row.length);
		for (Cell cell : row) {
			cells.add(cell.getContents());
		}
		return cells;
	}

	public static String cleanForFilename(String string) {
		string = string.replaceAll("&", "_and_");
		string = string.replaceAll("ä", "ae");
		string = string.replaceAll("ö", "oe");
		string = string.replaceAll("ü", "ue");
		string = string.replaceAll("ß", "ss");
		string = string.replaceAll("/", "_");
		string = string.replaceAll(" ", "");
		string = string.replaceAll("\\(", "-");
		string = string.replaceAll("\\)", "-");
		string = string.replaceAll(",", "-");
		string = string.replaceAll("\\.", "-");
		string = string.replaceAll(":", "-");
		return string;
	}

	/**
	 * Creates a pretty formated, Wiki-save String of the given Collection.
	 * 
	 * @created 22.07.2010
	 * @param collection
	 * @return
	 */
	public static String asString(Collection<String> collection) {
		if (collection != null) {
			return collection.toString().replaceAll("\\[", "[[");
		}
		else {
			return "[[]";
		}
	}

	public static String asStringNoBraces(Collection<String> collection) {
		if (collection != null) {
			String str = collection.toString().replaceAll("\\[", "[[");
			return str.substring(2, str.length() - 1);
		}
		else {
			return "";
		}
	}

	public static String asString(String string) {
		if (string != null) {
			return string.replaceAll("\\[", "[[");
		}
		else {
			return "[[]";
		}
	}

	public static String asBulletList(Collection<String> collection, int indent) {
		if (collection != null) {
			StringBuffer buffy = new StringBuffer();
			for (String string : collection) {
				for (int i = 0; i < indent; i++) {
					buffy.append("*");
				}
				buffy.append(" " + asString(string) + "\n");
			}
			return buffy.toString();
		}
		else {
			return "";
		}
	}
	
}
