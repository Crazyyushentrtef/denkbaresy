package de.knowwe.diaflux;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.diaflux.type.FlowchartEditProvider;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.diaflux.type.FlowchartXMLHeadType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.tools.ToolSet;

/**
 * @author Reinhard Hatko
 * @created 24.11.2010
 */
public class DiaFluxRenderer extends DefaultMarkupRenderer {

	public DiaFluxRenderer() {
		super("KnowWEExtension/flowchart/icon/flowchart24.png");
	}

	@Override
	protected void appendHeader(String title,
								String sectionID,
								ToolSet tools,
								UserContext user,
								RenderResult temp) {

		// we want to hide the header during loading to reduce the flicker
		String hiderId = "menuHider" + sectionID;

		temp.appendHtmlTag("div", "id", hiderId, "style", "display: none");
		super.appendHeader(title, sectionID, tools, user, temp);
		temp.appendHtmlTag("/div");

		String script = "var fn = function() {\n"
				+ "var hider =  jq$('#" + hiderId + "');\n"
				+ "hider.parent().append(hider.children());\n"
				+ "hider.remove();\n"
				+ "}\n"
				+ "KNOWWE.helper.observer.subscribe('beforeflowchartrendered', fn);";
		temp.appendHtmlElement("script", script);

	}

	@Override
	protected String getTitleName(Section<?> section, UserContext user) {
		Section<FlowchartType> flowchart = Sections.successor(section, FlowchartType.class);

		if (flowchart == null) {
			return "New flowchart";
		}
		else {
			return FlowchartType.getFlowchartName(flowchart);
		}
	}

	@Override
	protected void renderContents(Section<?> section, UserContext user, RenderResult string) {

		Section<FlowchartType> flowchart = Sections.successor(section, FlowchartType.class);

		if (flowchart == null) {
			string.append("%%information ");
			string.append("No flowchart created yet.");
			string.appendHtml("<br><a href=\""
					+ FlowchartEditProvider.createEditLink(section, user)
					+ "\">"
					+ "Click here to create one." + "</a><br>");
		}

		super.renderContents(section, user, string);

		if (flowchart == null) {
			string.append(" %%\n");
		}
	}

	protected void renderTitle(Section<?> section, UserContext user, RenderResult string) {
		String icon = getTitleIcon(section, user);
		String title = getTitleName(section, user);

		// render icon
		if (icon != null) {
			string.appendHtml("<img class='markupIcon' src='" + icon + "' /> ");
		}
		Section<FlowchartXMLHeadType.FlowchartTermDef> termDefSection
				= Sections.successor(section, FlowchartXMLHeadType.FlowchartTermDef.class);
		if (termDefSection != null) {
			// render heading
			string.appendHtml("<span><span toolmenuidentifier='" + termDefSection.getID() + "'>")
					.append(title).appendHtml("</span></span>");
		}
	}
}