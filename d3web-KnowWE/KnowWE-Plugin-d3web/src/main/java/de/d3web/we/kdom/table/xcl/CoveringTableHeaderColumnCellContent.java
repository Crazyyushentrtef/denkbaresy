/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.table.xcl;

import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.kdom.table.TableColumnHeaderCellContent;
import de.d3web.we.kdom.table.Table;
import de.d3web.we.kdom.table.TableLine;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
import de.d3web.we.utils.KnowWEUtils;

/**
 * @author jochen
 * 
 *         For the first column of the table - Containing questions and answers
 * 
 */
public class CoveringTableHeaderColumnCellContent extends
		TableColumnHeaderCellContent {

	public static final String QUESTION_CELL = "question_cell";
	
	public static final String QUESTIONNAIRE_CELL = "questionnaire_cell";

	@Override
	public void init() {
		this.addSubtreeHandler(new CoveringTableHeaderColumnCellContentHandler());
	}

	class CoveringTableHeaderColumnCellContentHandler implements
			SubtreeHandler {

		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {
			KnowledgeBaseManagement mgn = D3webModule
					.getKnowledgeRepresentationHandler(article.getWeb()).getKBM(article, this, s);
			
			Section<CoveringTableHeaderColumnCellContent> cell = s;
			
			if (mgn == null) {
				return null;
			}
			
			String text = s.getOriginalText().trim();
			
			//if it is an already defined QContainer
			Section<Table> tableContentSection = KnowWEObjectTypeUtils.getAncestorOfType(cell,
			Table.class);
			if(mgn.findQContainer(text) != null) {
				Section<TableLine> lineSec = KnowWEObjectTypeUtils.getAncestorOfType(cell,
						TableLine.class);
				KnowWEUtils.storeSectionInfo(s.getArticle().getWeb(), s
						.getTitle(), tableContentSection.getId(), QUESTIONNAIRE_CELL, s);
				
				lineSec.setType(QuestionnaireLine.getInstance());
			}
			
			//if it is a marked up new QContainer
			if(QuestionnaireCellContent.hasQContainerDeclarationSyntax(text)) {
				Section<TableLine> lineSec = KnowWEObjectTypeUtils.getAncestorOfType(cell,
						TableLine.class);
				KnowWEUtils.storeSectionInfo(s.getArticle().getWeb(), s
						.getTitle(), tableContentSection.getId(), QUESTIONNAIRE_CELL, s);
				
				lineSec.setType(QuestionnaireLine.getInstance());
			}
			
			
			//if it is an already defined Question
			if (mgn.findQuestion(text) != null) {
				Section<TableLine> lineSec = KnowWEObjectTypeUtils.getAncestorOfType(cell,
						TableLine.class);
				
				KnowWEUtils.storeSectionInfo(s.getArticle().getWeb(), s
						.getTitle(), tableContentSection.getId(), QUESTION_CELL, s);
				
				lineSec.setType(QuestionLine.getInstance());

			}
			
			//if it is a new marked up question
			if (containsQTypeMarkup(text)) {
				Section<TableLine> lineSec = KnowWEObjectTypeUtils.getAncestorOfType(cell,
						TableLine.class);

				KnowWEUtils.storeSectionInfo(s.getArticle().getWeb(), s
						.getTitle(), tableContentSection.getId(), QUESTION_CELL, s);
				lineSec.setType(QuestionLine.getInstance());

			} else {
				// else: its is an answer
				
				
				Section questionSection = (Section) KnowWEUtils
						.getStoredObject(cell.getArticle().getWeb(), cell.getTitle(),
								tableContentSection.getId(),
								QUESTION_CELL);
				if (questionSection != null) {
					KnowWEUtils.storeSectionInfo(s.getArticle().getWeb(), s
							.getTitle(), s.getId(), QUESTION_CELL,
							questionSection);
					s.setType(AnswerCellContent.getInstance());

				}
			}
			return null;

		}
		
		
		// TODO refactor
		private boolean containsQTypeMarkup(String text) {
			return text.contains("[num]") || text.contains("[oc]")
					|| text.contains("[mc]") || text.contains("[jn]");
		}

		// TODO refactor
//		private String removeQTypeMarkup(String text) {
//			text = text.replaceAll("\\[num\\]", "");
//			text = text.replaceAll("\\[oc\\]", "");
//			text = text.replaceAll("\\[mc\\]", "");
//			text = text.replaceAll("\\[jn\\]", "");
//			text = text.replaceAll("__", "");
//			text = text.replaceAll("\\|", "");
//			return text.trim();
//		}

	}
}
