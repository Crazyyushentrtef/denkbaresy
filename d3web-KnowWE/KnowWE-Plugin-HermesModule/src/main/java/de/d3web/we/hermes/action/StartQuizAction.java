package de.d3web.we.hermes.action;

import de.d3web.we.action.DeprecatedAbstractKnowWEAction;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.hermes.quiz.QuizSessionManager;
import de.d3web.we.hermes.taghandler.QuizHandler;

public class StartQuizAction extends DeprecatedAbstractKnowWEAction {

	@Override
	public boolean isAdminAction() {
		return false;
	}

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String user = parameterMap.getUser();
		QuizSessionManager.getInstance().createSession(user);
		return QuizHandler.renderQuizPanel(parameterMap.getUser(), QuizSessionManager.getInstance().getSession(user));
	}

}
