package de.d3web.we.kdom.rulesNew.terminalCondition;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

public class Number extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.addSubtreeHandler(new NumberChecker());
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR7));
	}

	public static Double getNumber(Section<Number> s) {
		try {
			return Double.parseDouble(s.getOriginalText().trim());
		}
		catch (Exception e) {

		}

		return null;
	}

	class NumberChecker implements SubtreeHandler {

		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {
			try {
				Double.parseDouble(s.getOriginalText().trim());
			}
			catch (Exception e) {
				KDOMReportMessage.storeError(s, this.getClass(), new
						InvalidNumberError(
						s.getOriginalText().trim()));
			}
			return null;
		}

	}

}
