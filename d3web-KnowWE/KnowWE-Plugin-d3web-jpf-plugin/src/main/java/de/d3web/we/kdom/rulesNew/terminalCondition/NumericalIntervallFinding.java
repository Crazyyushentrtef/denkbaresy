package de.d3web.we.kdom.rulesNew.terminalCondition;

import java.util.List;

import de.d3web.core.inference.condition.CondNumIn;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.QuestionRef;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.SplitUtility;

public class NumericalIntervallFinding extends D3webTerminalCondition<NumericalFinding> {

	public NumericalIntervallFinding() {
		this.setSectionFinder(new NumericalIntervallFinder());

		this.addChildType(new Intervall());
		QuestionRef questionRef = new QuestionRef();
		questionRef.setSectionFinder(new AllTextFinderTrimmed());
		this.addChildType(questionRef);
	}

	@Override
	public TerminalCondition getTerminalCondition(Section<NumericalFinding> s) {
		Section<QuestionRef> qRef = s.findSuccessor(QuestionRef.class);

		Section<Intervall> intervall = s.findSuccessor(Intervall.class);

		Double number1 = intervall.get().getFirstNumber(intervall);
		Double number2 = intervall.get().getSecondNumber(intervall);

		Question q = qRef.get().getObject(qRef);

		if (!(q instanceof QuestionNum)) {
			// TODO some reasonable error handling here!
		}

		if (number1 != null && number2 != null && q != null && q instanceof QuestionNum) {
			return new CondNumIn((QuestionNum) q, number1, number2);
		}
		return null;
	}

	class NumericalIntervallFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {

			// has to end with ']'
			if (text.trim().endsWith("]")) {
				int bracketsStart = SplitUtility.lastIndexOfUnquoted(text, "[");
				if (bracketsStart == -1) return null;
				// get the content in brackets
				String brackets = text.substring(bracketsStart).trim();
				String content = brackets.substring(1, brackets.length() - 1);

				// find out whether there are exactly 2 chains of characters
				// separated by 1 one more spaces
				String[] nonEmptyParts = SplitUtility.getCharacterChains(content);

				// ..if so, take it all
				if (nonEmptyParts.length == 2) {
					return new AllTextFinderTrimmed().lookForSections(text, father);
				}

			}
			return null;
		}

	}



	class Intervall extends DefaultAbstractKnowWEObjectType {

		public Double getFirstNumber(Section<Intervall> s) {
			String text = s.getOriginalText();
			String content = text.substring(1, text.length() - 1);
			String[] parts = SplitUtility.getCharacterChains(content);
			if (parts.length == 2) {
				String firstNumber = parts[0];
				Double d = null;
				try {
					d = Double.parseDouble(firstNumber);
				} catch (NumberFormatException f) {

				}
				return d;
			}
			return null;
		}

		public Double getSecondNumber(Section<Intervall> s) {
			String text = s.getOriginalText();
			String content = text.substring(1, text.length() - 1);
			String[] parts = SplitUtility.getCharacterChains(content);
			if (parts.length == 2) {
				String secondNumber = parts[1];
				Double d = null;
				try {
					d = Double.parseDouble(secondNumber);
				}
				catch (NumberFormatException f) {

				}
				return d;
			}
			return null;
		}

		public Intervall() {
			this.setSectionFinder(new SectionFinder() {

				@Override
				public List<SectionFinderResult> lookForSections(String text, Section father) {
					if (text.trim().endsWith("]")) {
						int bracketsStart = SplitUtility.lastIndexOfUnquoted(text, "[");
						int bracketsEnd = SplitUtility.lastIndexOfUnquoted(text, "]");

						return SectionFinderResult.createSingleItemList(new SectionFinderResult(
								bracketsStart,
								bracketsEnd + 1));

					}
					return null;
				}
			});
		}
	}

}
