package de.d3web.we.kdom.namespaces;

import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.semantic.ISemanticCore;
import de.d3web.we.core.semantic.SemanticCore;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class NamespacesContentRenderer extends KnowWEDomRenderer {

	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
		ISemanticCore sc = SemanticCore.getInstance();
		StringBuffer buffy = new StringBuffer();
		ResourceBundle rb = KnowWEEnvironment.getInstance()
				.getKwikiBundle(user);
		String content = sec.getOriginalText();
		HashMap<String, String> namespaces = sc.getNameSpaces();
		if (content.trim().length() > 0) {
			for (String line : content.split("\r\n|\r|\n")) {
				if (line.contains(":")) {
					int i = line.indexOf(":");
					String key = line.substring(0, i).trim();
					String val = line.substring(i + 1).trim();
					if (!namespaces.containsKey(key)) {
						sc.addNamespace(key, val);
					}
				}
			}
		}
		namespaces = sc.getNameSpaces();

		buffy.append("<div id='knoffice-panel' class='panel'>");
		buffy.append("<h3>" + rb.getString("KnowWE.Namespaces.Default.header")
				+ "</h3>");

		buffy.append("<table>");
		buffy.append("<tr><th>Custom Namespaces</th></tr>");
		for (Entry<String, String> cur : namespaces.entrySet()) {
			buffy.append("<tr><td>");
			buffy.append(cur.getKey() + "</td><td>" + cur.getValue());
			buffy.append("</td></tr>");
		}
		buffy.append("<tr><th>Defaults</th></tr>");
		for (Entry<String, String> cur : sc.getDefaultNameSpaces().entrySet()) {
			buffy.append("<tr><td>");
			buffy.append(cur.getKey() + "</td><td>" + cur.getValue());
			buffy.append("</td></tr>");
		}
		buffy.append("</table>");
		buffy.append("</div>");
		string.append(KnowWEEnvironment.maskHTML(buffy.toString()));
	}

}
