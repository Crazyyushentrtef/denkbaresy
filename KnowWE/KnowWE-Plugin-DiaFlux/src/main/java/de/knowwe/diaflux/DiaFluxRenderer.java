package de.knowwe.diaflux;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.diaflux.type.FlowchartEditProvider;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * 
 * @author Reinhard Hatko
 * @created 24.11.2010
 */
public class DiaFluxRenderer extends DefaultMarkupRenderer<DefaultMarkupType> {

	public DiaFluxRenderer() {
		super("KnowWEExtension/flowchart/icon/flowchart24.png");
	}

	@Override
	protected String getTitleName(Section<DefaultMarkupType> section, UserContext user) {
		Section<FlowchartType> flowchart = Sections.findSuccessor(section, FlowchartType.class);

		if (flowchart == null) {
			return "New flowchart";
		}
		else {
			return FlowchartType.getFlowchartName(flowchart);
		}
	}

	@Override
	protected void renderContents(Section<DefaultMarkupType> section, UserContext user, StringBuilder string) {

		Section<FlowchartType> flowchart = Sections.findSuccessor(section, FlowchartType.class);

		if (flowchart == null) {
			string.append("No flowchart created yet. ");
			String link = "<a href=\""
					+ FlowchartEditProvider.createEditLink(section, user)
					+ "\">"
					+ "Click here to create one." + "</a>";
			string.append(KnowWEUtils.maskHTML(link));
		}

		super.renderContents(section, user, string);
	}
}