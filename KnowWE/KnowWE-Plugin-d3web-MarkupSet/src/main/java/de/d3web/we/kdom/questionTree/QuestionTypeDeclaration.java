package de.d3web.we.kdom.questionTree;

import java.util.Arrays;
import java.util.List;

import de.d3web.we.kdom.questionTree.QuestionLine.QuestionTypeChecker;
import de.d3web.we.object.QuestionDefinition;
import de.d3web.we.object.QuestionDefinition.QuestionType;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.SplitUtility;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.StringEnumChecker;

/**
 * A Type for the question-type declaration keys "[oc],[mc],[num],..."
 * 
 * @author Jochen
 */
public class QuestionTypeDeclaration extends
		AbstractType {

	public static QuestionType getQuestionType(Section<QuestionTypeDeclaration> typeSection) {

		if (typeSection == null) return null;
		String embracedContent = typeSection.getText();
		if (embracedContent.startsWith("[")) {
			embracedContent = embracedContent.substring(1);
		}
		if (embracedContent.endsWith("]")) {
			embracedContent = embracedContent.substring(0,
					embracedContent.length() - 1);
		}
		String questionTypeDeclaration = embracedContent.trim();

		if (questionTypeDeclaration.equalsIgnoreCase("oc")) {
			return QuestionType.OC;
		}
		else if (questionTypeDeclaration.equalsIgnoreCase("mc")) {
			return QuestionType.MC;
		}
		else if (questionTypeDeclaration.equalsIgnoreCase("num")) {
			return QuestionType.NUM;
		}
		else if (questionTypeDeclaration.equalsIgnoreCase("jn")
				|| questionTypeDeclaration.equalsIgnoreCase("yn")) {
			return QuestionType.YN;
		}
		else if (questionTypeDeclaration.equalsIgnoreCase("date")) {
			return QuestionType.DATE;
		}
		else if (questionTypeDeclaration.equalsIgnoreCase("info")) {
			return QuestionType.INFO;
		}
		else if (questionTypeDeclaration.equalsIgnoreCase("text")) {
			return QuestionType.TEXT;
		}
		else {
			return null;
		}

	}

	public static final String[] QUESTION_DECLARATIONS = {
			"oc", "mc",
			"yn", "jn", "num", "date", "text", "info" };

	public QuestionTypeDeclaration() {
		SectionFinder typeFinder = new SectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text,
					Section<?> father, Type type) {

				return SectionFinderResult
						.createSingleItemList(new SectionFinderResult(
								SplitUtility.indexOfUnquoted(text, "["),
								SplitUtility.indexOfUnquoted(text, "]") + 1));
			}
		};
		this.setSectionFinder(typeFinder);
		this.setCustomRenderer(new StyleRenderer(StyleRenderer.OPERATOR.getCssStyle()) {

			@Override
			public void render(KnowWEArticle article, @SuppressWarnings("rawtypes") Section section,
					UserContext user, StringBuilder string) {
				StringBuilder temp = new StringBuilder();
				super.render(article, section, user, temp);
				string.append(temp.toString());
			}
		});
		String allowedTypes = Arrays.asList(QUESTION_DECLARATIONS).toString();
		allowedTypes = allowedTypes.substring(1, allowedTypes.length() - 1);
		Message errorMsg = Messages.error(D3webUtils.getD3webBundle()
				.getString("KnowWE.questiontree.allowingonly")
				+ allowedTypes);
		this.addSubtreeHandler(new StringEnumChecker<QuestionTypeDeclaration>(
				QUESTION_DECLARATIONS, errorMsg, 1, 1));
		this.addSubtreeHandler(new QuestionTypeChecker());
	}

	public Section<QuestionDefinition> getQuestionDefinition(Section<QuestionTypeDeclaration> typeDeclaration) {
		return Sections.findSuccessor(typeDeclaration.getFather(),
				QuestionDefinition.class);
	}

}