package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.utils.KnowWEObjectTypeUtils;

public class QuestionTreeAnswerID extends AnswerID {

	@Override
	public Section<QuestionID> getQuestion(Section<AnswerID> s) {

		Section<DashTreeElement> localDashTreeElement = KnowWEObjectTypeUtils.getAncestorOfType(s, new DashTreeElement());
		
		Section<? extends DashTreeElement> dashTreeFather = DashTreeElement.getDashTreeFather(localDashTreeElement);
		
		Section<QuestionID> qid = dashTreeFather.findSuccessor(new QuestionID());
		
		return qid;
	}

}
